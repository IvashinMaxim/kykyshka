package org.example;

import java.util.Arrays;
import java.util.Random;

public class CuckooSearch {
    private int dimensions;
    private int populationSize;
    private double pa;
    private double stepSize;
    private Random random;
    private FunctionType functionType;
    private Nest[] population;
    private Nest bestNest;
    private double[][] bounds;

    public enum FunctionType {SPHERE, ROSENBROCK}

    public CuckooSearch(int dimensions, int populationSize, double pa, double stepSize,
                        FunctionType functionType, double[][] bounds) {
        this.dimensions = dimensions;
        this.populationSize = populationSize;
        this.pa = pa;
        this.stepSize = stepSize;
        this.random = new Random();
        this.functionType = functionType;
        this.bounds = bounds;
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
                    total += 100 * Math.pow(position[i + 1] - position[i] * position[i], 2) + Math.pow(1 - position[i], 2);
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
                .min((a, b) -> Double.compare(a.getFitness(), b.getFitness()))
                .orElse(population[0]);
    }

    public void optimizeStep() {
        // Генерация нового решения через Леви-полет
        int i = random.nextInt(populationSize);
        double[] newPosition = population[i].getPosition().clone();
        double[] levyStep = levyFlight(dimensions);

        for (int j = 0; j < dimensions; j++) {
            newPosition[j] += levyStep[j];
            // Проверка границ
            newPosition[j] = Math.max(bounds[j][0], Math.min(bounds[j][1], newPosition[j]));
        }
        double newFitness = objectiveFunction(newPosition);

        // Замена решения, если новое лучше
        if (newFitness < population[i].getFitness()) {
            population[i].setPosition(newPosition);
            population[i].setFitness(newFitness);

            // Обновляем лучшее решение
            if (newFitness < bestNest.getFitness()) {
                bestNest = population[i];
            }
        }

        // Обнаружение и замена худших решений с вероятностью pa
        for (int j = 0; j < populationSize; j++) {
            if (random.nextDouble() < pa) {
                double[] randomPosition = new double[dimensions];
                for (int k = 0; k < dimensions; k++) {
                    randomPosition[k] = bounds[k][0] + (bounds[k][1] - bounds[k][0]) * random.nextDouble();
                }
                population[j].setPosition(randomPosition);
                population[j].setFitness(objectiveFunction(randomPosition));
            }
        }
    }

    private double[] levyFlight(int dimensions) {
        double[] step = new double[dimensions];
        double beta = 1.5;
        for (int i = 0; i < dimensions; i++) {
            double u = random.nextGaussian() * 0.01;
            double v = random.nextGaussian();
            double numerator = GammaApproximation.gamma(1 + beta) * Math.sin(Math.PI * beta / 2);
            double denominator = GammaApproximation.gamma((1 + beta) / 2) * beta * Math.pow(2, (beta - 1) / 2);
            double sigma = Math.pow(numerator / denominator, 1 / beta);
            step[i] = stepSize * u / Math.pow(Math.abs(v), 1 / beta);
        }
        return step;
    }


}