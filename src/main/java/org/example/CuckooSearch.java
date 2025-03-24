package org.example;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class CuckooSearch {
    private int dimensions;
    private int populationSize;
    private double pa;
    private double stepSize;
    private double beta;
    private Random random;
    private FunctionType functionType;
    private Nest[] population;
    private Nest bestNest;
    private double[][] bounds;

    public enum FunctionType {SPHERE, ROSENBROCK}

    public CuckooSearch(int dimensions, int populationSize, double pa, double stepSize,
                        FunctionType functionType, double[][] bounds, double beta) {
        this.dimensions = dimensions;
        this.populationSize = populationSize;
        this.pa = pa;
        this.stepSize = stepSize;
        this.random = new Random();
        this.functionType = functionType;
        this.bounds = bounds;
        this.beta = beta;
    }

    public Nest[] getPopulation() {
        return population;
    }

    public Nest getBestNest() {
        return bestNest;
    }

    private double objectiveFunction(double[] position) {
        switch (functionType) {
            case SPHERE:
                double sum = 0;
                for (double x : position) sum += x * x;
                return sum;
            case ROSENBROCK:
                double total = 0;
                for (int i = 0; i < position.length - 1; i++) {
                    total += 100 * Math.pow(position[i + 1] - position[i] * position[i], 2)
                             + Math.pow(1 - position[i], 2);
                }
                return total;
            default:
                throw new IllegalArgumentException("Unknown function");
        }
    }

    public void initializePopulation() {
        population = new Nest[populationSize];
        for (int i = 0; i < populationSize; i++) {
            double[] position = new double[dimensions];
            for (int j = 0; j < dimensions; j++) {
                position[j] = bounds[j][0] + (bounds[j][1] - bounds[j][0]) * random.nextDouble();
            }
            population[i] = new Nest(position, objectiveFunction(position));
        }
        bestNest = Arrays.stream(population)
                .min(Comparator.comparingDouble(Nest::getFitness))
                .orElseThrow();
    }

    public void optimizeStep() {
        // 1. Леви-полет для случайного гнезда
        int randomIndex = random.nextInt(populationSize);
        Nest currentNest = population[randomIndex];
        double[] currentPosition = currentNest.getPosition().clone();
        double[] levyStep = levyFlight(dimensions, beta);

        // Применение шага и корректировка границ
        for (int j = 0; j < dimensions; j++) {
            currentPosition[j] += levyStep[j];
            currentPosition[j] = Math.max(bounds[j][0], Math.min(bounds[j][1], currentPosition[j]));
        }
        double newFitness = objectiveFunction(currentPosition);

        // Обновление гнезда при улучшении
        if (newFitness < currentNest.getFitness()) {
            currentNest.setPosition(currentPosition);
            currentNest.setFitness(newFitness);
            updateBestNest(currentNest);
        }

//        Nest[] sortedPopulation = Arrays.stream(population)
//                .sorted((a, b) -> Double.compare(b.getFitness(), a.getFitness()))
//                .toArray(Nest[]::new);
//        int numToReplace = (int) (pa * populationSize);
//
//        for (int i = 0; i < numToReplace; i++) {
//            double[] randomPosition = new double[dimensions];
//            for (int j = 0; j < dimensions; j++) {
//                randomPosition[j] = bounds[j][0] + (bounds[j][1] - bounds[j][0]) * random.nextDouble();
//            }
//            newFitness = objectiveFunction(randomPosition);
//            sortedPopulation[i].setPosition(randomPosition);
//            sortedPopulation[i].setFitness(newFitness);
//            updateBestNest(sortedPopulation[i]);
//        }
//        System.arraycopy(sortedPopulation, 0, population, 0, populationSize);

        for (int i = 0; i < populationSize; i++) {
            Nest nest = population[i];
            if (i == randomIndex) continue; // Пропускаем гнездо, обновленное Леви-полетом
            if (random.nextDouble() < pa) {
                double[] oldPosition = nest.getPosition().clone();
                double oldFitness = nest.getFitness();

                // Генерация новой позиции
                double[] randomPosition = new double[dimensions];
                for (int j = 0; j < dimensions; j++) {
                    randomPosition[j] = bounds[j][0] + (bounds[j][1] - bounds[j][0]) * random.nextDouble();
                }
                newFitness = objectiveFunction(randomPosition);

                // Обновляем только при улучшении
                if (newFitness < oldFitness) {
                    nest.setPosition(randomPosition);
                    nest.setFitness(newFitness);
                    updateBestNest(nest);
                } else {
                    // Восстанавливаем старую позицию
                    nest.setPosition(oldPosition);
                    nest.setFitness(oldFitness);
                }
            }
        }
    }

    private void updateBestNest(Nest candidate) {
        if (candidate.getFitness() < bestNest.getFitness()) {
            bestNest = candidate;
        }
    }

    private double[] levyFlight(int dimensions, double beta) {
        double[] step = new double[dimensions];
        for (int i = 0; i < dimensions; i++) {
            double u = random.nextGaussian(); // Убрано * 0.01
            double v = random.nextGaussian();
            double numerator = GammaApproximation.gamma(1 + beta)
                               * Math.sin(Math.PI * beta / 2);
            double denominator = GammaApproximation.gamma((1 + beta)/2)
                                 * beta
                                 * Math.pow(2, (beta - 1)/2);
            double sigma = Math.pow(numerator / denominator, 1 / beta);
            step[i] = stepSize * u / Math.pow(Math.abs(v), 1 / beta);
        }
        return step;
    }

    // Реализация гамма-функции через аппроксимацию Ланцоша
    private static class GammaApproximation {
        private static final double[] COEFFICIENTS = {
                0.99999999999980993,
                676.5203681218851,
                -1259.1392167224028,
                771.32342877765313,
                -176.61502916214059,
                12.507343278686905,
                -0.13857109526572012,
                9.98455071718193e-6,
                1.5056327351493116e-7
        };

        public static double gamma(double x) {
            if (x < 0.5) {
                return Math.PI / (Math.sin(Math.PI * x) * gamma(1 - x));
            } else {
                x -= 1;
                double a = COEFFICIENTS[0];
                double t = x + 7.5;
                for (int i = 1; i < COEFFICIENTS.length; i++) {
                    a += COEFFICIENTS[i] / (x + i);
                }
                return Math.sqrt(2 * Math.PI)
                       * Math.pow(t, x + 0.5)
                       * Math.exp(-t)
                       * a;
            }
        }
    }
}