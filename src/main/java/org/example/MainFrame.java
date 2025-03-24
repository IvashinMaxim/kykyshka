// MainFrame.java (обновленный)
package org.example;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ChartFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;

public class MainFrame extends JFrame {
    private JPanel controlPanel;
    private JTextArea logArea;
    private ChartPanel chartPanel;
    private JProgressBar progressBar;
    private JTextField populationField;
    private JTextField paField;
    private JTextField stepField;
    private JTextField betaField; // Добавлено поле для beta
    private JTextField iterationsField;
    private JComboBox<String> functionCombo;

    public MainFrame() {
        super("Cuckoo Search Optimizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        initUI();
    }

    private void initUI() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(0, 2, 5, 5));

        controlPanel.add(new JLabel("Population Size:"));
        populationField = new JTextField("20");
        controlPanel.add(populationField);

        controlPanel.add(new JLabel("Discovery Probability (pa):"));
        paField = new JTextField("0.25");
        controlPanel.add(paField);

        controlPanel.add(new JLabel("Step Size:"));
        stepField = new JTextField("0.01");
        controlPanel.add(stepField);

        controlPanel.add(new JLabel("Beta (1.0-2.0):")); // Новое поле
        betaField = new JTextField("1.5");
        controlPanel.add(betaField);

        controlPanel.add(new JLabel("Max Iterations:"));
        iterationsField = new JTextField("100");
        controlPanel.add(iterationsField);

        controlPanel.add(new JLabel("Function:"));
        functionCombo = new JComboBox<>(new String[]{"Sphere", "Rosenbrock"});
        controlPanel.add(functionCombo);

        JButton startButton = new JButton("Start Optimization");
        startButton.addActionListener(e -> startOptimization());
        controlPanel.add(startButton);

        logArea = new JTextArea();
        logArea.setEditable(false);

        JFreeChart chart = ChartFactory.createScatterPlot(
                "Optimization Process",
                "X", "Y",
                null,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        chartPanel = new ChartPanel(chart);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(logArea),
                chartPanel
        );
        splitPane.setDividerLocation(300);

        progressBar = new JProgressBar();
        controlPanel.add(progressBar);

        add(controlPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    public void updateChart(Nest[] population, Nest bestNest) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        // Лучшее гнездо ДОЛЖНО добавляться первым
        XYSeries bestSeries = new XYSeries("Best Nest");
        double[] bestPos = bestNest.getPosition();
        if (bestPos.length >= 2) bestSeries.add(bestPos[0], bestPos[1]);
        dataset.addSeries(bestSeries);

        // Все гнезда
        XYSeries nestsSeries = new XYSeries("Nests");
        for (Nest nest : population) {
            double[] pos = nest.getPosition();
            if (pos.length >= 2) nestsSeries.add(pos[0], pos[1]);
        }
        dataset.addSeries(nestsSeries);

        XYPlot plot = chartPanel.getChart().getXYPlot();
        plot.setDataset(dataset);

        // Настройка стилей
        plot.getRenderer().setSeriesPaint(0, Color.RED);    // Первая серия - лучшая точка
        plot.getRenderer().setSeriesPaint(1, Color.BLUE);   // Вторая серия - все точки
        plot.getRenderer().setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6));

        chartPanel.repaint();
    }
//    public void updateChart(Nest[] population, Nest bestNest) {
//        XYSeriesCollection dataset = new XYSeriesCollection();
//
//        // Все гнезда
//        XYSeries nestsSeries = new XYSeries("Nests");
//        for (Nest nest : population) {
//            double[] pos = nest.getPosition();
//            if (pos.length >= 2) nestsSeries.add(pos[0], pos[1]);
//        }
//        dataset.addSeries(nestsSeries);
//
//        // Лучшее гнездо
//        XYSeries bestSeries = new XYSeries("Best Nest");
//        double[] bestPos = bestNest.getPosition();
//        if (bestPos.length >= 2) bestSeries.add(bestPos[0], bestPos[1]);
//        dataset.addSeries(bestSeries);
//
//        XYPlot plot = chartPanel.getChart().getXYPlot();
//        plot.setDataset(dataset);
//        plot.getRenderer().setSeriesPaint(0, Color.BLUE);
//        plot.getRenderer().setSeriesPaint(1, Color.RED);
//        plot.getRenderer().setSeriesShape(1, new Ellipse2D.Double(-3, -3, 6, 6));
//        chartPanel.repaint();
//    }

    private void startOptimization() {
        try {
            int population = Integer.parseInt(populationField.getText());
            double pa = Double.parseDouble(paField.getText());
            double step = Double.parseDouble(stepField.getText());
            double beta = Double.parseDouble(betaField.getText());
            int iterations = Integer.parseInt(iterationsField.getText());
            int dimensions = 2;

            double[][] bounds = new double[dimensions][2];
            for (int i = 0; i < dimensions; i++) {
                bounds[i][0] = -5;
                bounds[i][1] = 5;
            }

            CuckooSearch.FunctionType functionType = functionCombo.getSelectedIndex() == 0 ?
                    CuckooSearch.FunctionType.SPHERE :
                    CuckooSearch.FunctionType.ROSENBROCK;

            CuckooSearch cs = new CuckooSearch(
                    dimensions,
                    population,
                    pa,
                    step,
                    functionType,
                    bounds,
                    beta
            );

            OptimizationWorker worker = new OptimizationWorker(this, cs, iterations);
            worker.addPropertyChangeListener(evt -> {
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setValue((Integer) evt.getNewValue());
                }
            });
            worker.execute();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка ввода: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void appendLog(String message) {
        logArea.append(message + "\n");
    }
}