package org.example;

import java.util.Arrays;

public class NewMain {
    public static void main(String[] args) {
        double[][] bounds = {{-5, 5}, {-5, 5}};
        CuckooSearch cs = new CuckooSearch(
                2,                 // Размерность
                25,                // Размер популяции
                0.25,              // pa
                0.01,              // stepSize
                CuckooSearch.FunctionType.SPHERE,
                bounds,
                1.5                // beta
        );

        cs.initializePopulation();
        for (int i = 0; i < 1000; i++) {
            cs.optimizeStep();
        }
        System.out.println("Лучшее решение: " + Arrays.toString(cs.getBestNest().getPosition()));
    }
}
