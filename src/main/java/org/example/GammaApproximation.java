package org.example;

public class GammaApproximation {
    private static final double[] LANCZOS_COEFFS = {
            1.000000000190015,
            76.18009172947146,
            -86.50532032941677,
            24.01409824083091,
            -1.231739572450155,
            0.1208650973866179e-2,
            -0.5395239384953e-5
    };

    public static double gamma(double z) {
        if (z <= 0) throw new IllegalArgumentException("z must be positive");
        double g = 5.0;
        double sum = LANCZOS_COEFFS[0];
        for (int i = 1; i < LANCZOS_COEFFS.length; i++) {
            sum += LANCZOS_COEFFS[i] / (z + i);
        }
        double t = z + g + 0.5;
        return Math.sqrt(2 * Math.PI) * Math.pow(t, z + 0.5) * Math.exp(-t) * sum;
    }
}