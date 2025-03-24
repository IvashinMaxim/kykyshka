// OptimizationWorker.java (исправленный)
package org.example;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class OptimizationWorker extends SwingWorker<Void, String> {
    private final MainFrame frame;
    private final CuckooSearch cs;
    private final int maxIterations;

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
            publish(String.format("Итерация: %d | Лучшая fitness: %.5f",
                    iter, cs.getBestNest().getFitness()));
            setProgress((100 * iter) / maxIterations);
        }
        return null;
    }



    @Override
    protected void process(List<String> chunks) {
        chunks.forEach(msg -> {
            frame.appendLog(msg);
            Nest bestNest = cs.getBestNest();
            frame.updateChart(cs.getPopulation(), bestNest);

            System.out.println("Лучшая позиция: " + Arrays.toString(bestNest.getPosition()));
        });
    }
}