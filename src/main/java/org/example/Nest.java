// Nest.java (без изменений)
package org.example;

public class Nest {
    private double[] position;
    private double fitness;

    public Nest(double[] position, double fitness) {
        this.position = position.clone();
        this.fitness = fitness;
    }

    public double[] getPosition() {
        return position.clone();
    }

    public void setPosition(double[] position) {
        this.position = position.clone();
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}