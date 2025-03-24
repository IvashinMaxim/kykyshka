package org.example;

public class Nest {
    private double[] position; // Координаты в пространстве решений
    private double fitness;    // Значение целевой функции

    public Nest(double[] position, double fitness) {
        this.position = position.clone();
        this.fitness = fitness;
    }

    // Геттеры и сеттеры
    public double[] getPosition() { return position.clone(); }
    public double getFitness() { return fitness; }
    public void setPosition(double[] position) { this.position = position.clone(); }
    public void setFitness(double fitness) { this.fitness = fitness; }
}
