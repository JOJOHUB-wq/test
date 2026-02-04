package ua.atherium.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.chart.ChartUtils;
import ua.atherium.DatabaseManager.StatPoint;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class GraphGenerator {

    public File generateStatsGraph(String title, List<StatPoint> stats, String colorHex, String bgUrl) {
        TimeSeries series = new TimeSeries("Online");
        for (StatPoint point : stats) {
            series.addOrUpdate(new Minute(new Date(point.timestamp())), point.online());
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title,
                "Time",
                "Online",
                dataset,
                false,
                false,
                false
        );

        customizeChart(chart, colorHex, bgUrl);

        return saveChart(chart);
    }

    public File generateCompareGraph(String title, Map<String, List<StatPoint>> data, String bgUrl) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();

        for (Map.Entry<String, List<StatPoint>> entry : data.entrySet()) {
            TimeSeries series = new TimeSeries(entry.getKey());
            for (StatPoint point : entry.getValue()) {
                series.addOrUpdate(new Minute(new Date(point.timestamp())), point.online());
            }
            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title,
                "Time",
                "Online",
                dataset,
                true,
                false,
                false
        );

        customizeChart(chart, null, bgUrl);

        return saveChart(chart);
    }

    private void customizeChart(JFreeChart chart, String colorHex, String bgUrl) {
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(30, 30, 30));
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));

        if (colorHex != null) {
            try {
                renderer.setSeriesPaint(0, Color.decode(colorHex));
            } catch (Exception e) {
                renderer.setSeriesPaint(0, Color.GREEN);
            }
        }

        if (bgUrl != null && !bgUrl.isEmpty() && !bgUrl.equalsIgnoreCase("удалить")) {
            try {
                BufferedImage bg = ImageIO.read(new URL(bgUrl));
                plot.setBackgroundImage(bg);
            } catch (Exception e) {
            }
        }
    }

    private File saveChart(JFreeChart chart) {
        try {
            File tempFile = File.createTempFile("chart_", ".png");
            ChartUtils.saveChartAsPNG(tempFile, chart, 800, 400);
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
