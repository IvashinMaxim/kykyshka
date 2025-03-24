package org.example;

import javax.swing.*;
import java.util.List;

public class OptimizationWorker extends SwingWorker<Void, String> {
    private MainFrame frame;
    private CuckooSearch cs;
    private int maxIterations;

    public OptimizationWorker(MainFrame frame, CuckooSearch cs, int maxIterations) {
        this.frame = frame;
        this.cs = cs;
        this.maxIterations = maxIterations;
    }

    @Override
    protected Void doInBackground() {
        cs.initializePopulation();
        for (int iter = 0; iter < maxIterations; iter++) {
            cs.optimizeStep();
            publish("Iteration: " + iter + ", Best Fitness: " + cs.getBestNest().getFitness());
            setProgress((100 * iter) / maxIterations);
        }
        return null;
    }

    @Override
    protected void process(List<String> chunks) {
        for (String msg : chunks) {
            frame.appendLog(msg);
            frame.updateChart(cs.getPopulation(), cs.getBestNest());
        }
    }
}