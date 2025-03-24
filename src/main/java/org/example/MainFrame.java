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
import javax.swing.border.TitledBorder;
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
        setLocationRelativeTo(null); // Центрирование окна
    }

    private void initUI() {
        // Настройка цветов и шрифтов
        Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
        Color accentColor = new Color(0, 120, 215);

        controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout(10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Панель параметров
        JPanel paramsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        paramsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Параметры оптимизации",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                mainFont.deriveFont(Font.BOLD),
                accentColor));

        addLabelAndField(paramsPanel, "Population Size:", "20", mainFont, populationField = new JTextField());
        addLabelAndField(paramsPanel, "Discovery Probability (pa):", "0.25", mainFont, paField = new JTextField());
        addLabelAndField(paramsPanel, "Step Size:", "0.01", mainFont, stepField = new JTextField());
        addLabelAndField(paramsPanel, "Beta (1.0-2.0):", "1.5", mainFont, betaField = new JTextField());
        addLabelAndField(paramsPanel, "Max Iterations:", "100", mainFont, iterationsField = new JTextField());

        // Выбор функции
        JPanel functionPanel = new JPanel(new BorderLayout(5, 5));
        functionPanel.add(new JLabel("Function:"), BorderLayout.WEST);
        functionCombo = new JComboBox<>(new String[]{"Sphere", "Rosenbrock"});
        functionCombo.setFont(mainFont);
        functionPanel.add(functionCombo, BorderLayout.CENTER);
        paramsPanel.add(functionPanel);
        paramsPanel.add(new JLabel()); // Пустая ячейка для выравнивания

        controlPanel.add(paramsPanel, BorderLayout.CENTER);

        // Кнопка и прогресс-бар
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        JButton startButton = new JButton("Start Optimization");
        styleButton(startButton, accentColor, mainFont);
        startButton.addActionListener(e -> startOptimization());

        progressBar = new JProgressBar();
        progressBar.setForeground(accentColor);
        progressBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        progressBar.setStringPainted(true);

        bottomPanel.add(startButton, BorderLayout.NORTH);
        bottomPanel.add(progressBar, BorderLayout.SOUTH);
        controlPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Лог и график
        logArea = new JTextArea();
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(245, 245, 245));
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JFreeChart chart = createStyledChart();
        chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(logArea),
                chartPanel
        );
        splitPane.setDividerLocation(350);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        add(controlPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void addLabelAndField(JPanel panel, String labelText, String fieldText, Font font, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(font);
        panel.add(label);

        field.setText(fieldText);
        field.setFont(font);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        panel.add(field);
    }

    private void styleButton(JButton button, Color color, Font font) {
        button.setFont(font.deriveFont(Font.BOLD, 14f));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker()),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JFreeChart createStyledChart() {
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Optimization Process",
                "X", "Y",
                null,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(245, 245, 245));
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));

        plot.getDomainAxis().setAxisLinePaint(Color.DARK_GRAY);
        plot.getRangeAxis().setAxisLinePaint(Color.DARK_GRAY);

        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        return chart;
    }

    public void updateChart(Nest[] population, Nest bestNest) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        // Лучшее гнездо
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

        // Современные стили
        plot.getRenderer().setSeriesPaint(0, new Color(255, 89, 94)); // Красный акцент
        plot.getRenderer().setSeriesPaint(1, new Color(0, 120, 215)); // Синий
        plot.getRenderer().setSeriesShape(0, new Ellipse2D.Double(-5, -5, 10, 10));
        plot.getRenderer().setSeriesShape(1, new Ellipse2D.Double(-3, -3, 6, 6));

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