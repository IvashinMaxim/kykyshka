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
    private JTextField iterationsField;
    private JComboBox<String> functionCombo;

    public MainFrame() {
        super("Cuckoo Search Optimizer");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        initUI();

    }

    private void initUI() {
        // Панель управления
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(0, 2, 5, 5));

        controlPanel.add(new JLabel("Population Size:"));
        populationField = new JTextField("20");
        controlPanel.add(populationField);

        controlPanel.add(new JLabel("Discovery Probability (pa):"));
        paField = new JTextField("0.25");
        controlPanel.add(paField);

        controlPanel.add(new JLabel("Step Size:"));
        stepField = new JTextField("0.5");
        controlPanel.add(stepField);

        controlPanel.add(new JLabel("Max Iterations:"));
        iterationsField = new JTextField("100");
        controlPanel.add(iterationsField);

        controlPanel.add(new JLabel("Function:"));
        functionCombo = new JComboBox<>(new String[]{"Sphere", "Rosenbrock"});
        controlPanel.add(functionCombo);

        JButton startButton = new JButton("Start Optimization");
        startButton.addActionListener(e -> startOptimization());
        controlPanel.add(startButton);


        // Область лога
        logArea = new JTextArea();
        logArea.setEditable(false);

        // График
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Optimization Process",
                "X", "Y",
                null,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        chartPanel = new ChartPanel(chart);

        // Компоновка
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(logArea),
                chartPanel
        );
        splitPane.setDividerLocation(300);

        add(controlPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        progressBar = new JProgressBar();
        controlPanel.add(progressBar);
    }

    public void updateChart(Nest[] population, Nest bestNest) {
        if (population == null || bestNest == null) return;
        // Очистка предыдущих данных
        XYSeriesCollection dataset = new XYSeriesCollection();

        // Серия для всех гнезд
        XYSeries nestsSeries = new XYSeries("Nests");
        for (Nest nest : population) {
            double[] pos = nest.getPosition();
            nestsSeries.add(pos[0], pos[1]);
        }
        dataset.addSeries(nestsSeries);

        // Серия для лучшего гнезда
        XYSeries bestSeries = new XYSeries("Best Nest");
        double[] bestPos = bestNest.getPosition();
        bestSeries.add(bestPos[0], bestPos[1]);
        dataset.addSeries(bestSeries);

        // Обновление графика
        XYPlot plot = chartPanel.getChart().getXYPlot();
        plot.setDataset(dataset);

        // Настройка отображения
        plot.getRenderer().setSeriesPaint(0, Color.BLUE);    // Все гнезда
        plot.getRenderer().setSeriesPaint(1, Color.RED);     // Лучшее гнездо
        plot.getRenderer().setSeriesShape(1, new Ellipse2D.Double(-3, -3, 6, 6));

        // Перерисовка графика
        chartPanel.repaint();
    }

    private void startOptimization() {
        try {
            // Парсим параметры
            int population = Integer.parseInt(populationField.getText());
            double pa = Double.parseDouble(paField.getText());
            double step = Double.parseDouble(stepField.getText());
            int iterations = Integer.parseInt(iterationsField.getText());
            int dimensions = 2; // Для 2D визуализации

            // Границы поиска
            double[][] bounds = new double[dimensions][2];
            for (int i = 0; i < bounds.length; i++) {
                bounds[i][0] = -5;
                bounds[i][1] = 5;
            }

            // Создаем алгоритм
            CuckooSearch.FunctionType functionType =
                    functionCombo.getSelectedIndex() == 0 ?
                            CuckooSearch.FunctionType.SPHERE :
                            CuckooSearch.FunctionType.ROSENBROCK;

            CuckooSearch cs = new CuckooSearch(
                    dimensions, population, pa, step,
                    functionType, bounds
            );

            // Запускаем в фоновом потоке
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