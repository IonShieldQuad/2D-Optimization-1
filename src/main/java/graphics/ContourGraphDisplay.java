package graphics;

import math.PointDouble;
import org.mariuszgromada.math.mxparser.Function;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContourGraphDisplay extends JPanel {
    private static final int MARGIN_X = 50;
    private static final int MARGIN_Y = 50;
    private static final double EXTRA_AMOUNT = 0.0;
    private static final Color GRID_COLOR = Color.GRAY;
    private static final Color HIGH_COLOR = new Color(0x7fbff3);
    private static final Color LOW_COLOR = new Color(0xec80aa);
    private static final Color CONTOUR_COLOR = new Color(0xbb88ff);
    private static final Color POINT_COLOR = Color.YELLOW;
    private static final int POINT_SIZE = 2;
    
    private Function function;
    private FunctionCache cache;
    
    private double lowerX;
    private double upperX;
    private double lowerY;
    private double upperY;
    private double lowerZ = Double.NEGATIVE_INFINITY;
    private double upperZ = Double.POSITIVE_INFINITY;
    private int resolution = 50;
    
    private double contours = 20;
    private double contourWidth = 0.05;
    private double contourOffset = 0;
    
    private boolean usingColors = true;
    
    private List<PointDouble> points = new ArrayList<>();
    
    public ContourGraphDisplay() {
        super();
    }
    
    public void setFunction(Function function) {
        this.function = function;
        if (cache != null && !Objects.equals(cache.getFunction().getFunctionExpressionString(), function.getFunctionExpressionString())) {
            cache.invalidate();
        }
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    
        if (function != null && (cache == null || !cache.isValid())) {
            cache = new FunctionCache(function, resolution, lowerX, upperX, lowerY, upperY);
        }
        
        drawGrid(g);
        if (function != null) {
            drawGraph(g);
        }
        if (points != null) {
            drawPoints(g);
        }
    }
    
    
    private void drawGraph(Graphics g) {
        //g.setColor(new Color(Color.HSBtoRGB((float) Math.random(), 1.0f, 1.0f)));
        //int prev = 0;
        double max = Math.min(upperZ, cache.max);
        double min = Math.max(lowerZ, cache.min);
        double dz = (max - min) / (double)contours;
        boolean matched;
        for (int i = 0; i < graphWidth(); i++) {
            for (int j = 0; j < graphHeight(); j++) {
                PointDouble in = graphToValue(new PointDouble(i + MARGIN_X, j + MARGIN_Y));
                double val = cache.get(in.getX(), in.getY());
                matched = false;
                for (int k = 0; k <= contours; k++) {
                    if (val >= min + (k - 0.5 * contourWidth + contourOffset) * dz && val <= min + (k + 0.5 * contourWidth + contourOffset) * dz) {
                        matched = true;
                        break;
                    }
                }
                if (matched) {
                    if (isUsingColors()) {
                        g.setColor(CONTOUR_COLOR);
                    }
                    else {
                        g.setColor(Color.BLACK);
                    }
                }
                else {
                    if (isUsingColors()) {
                        g.setColor(interpolate(LOW_COLOR, HIGH_COLOR, Math.min(Math.max((val - min) / (max - min), 0), 1)));
                    }
                    else {
                        g.setColor(Color.WHITE);
                    }
                }
                
                g.drawRect(i + MARGIN_X, j + MARGIN_Y, 0, 0);
                /*val = new PointDouble(val.getX(), op.applyAsDouble(val.getX()));
                val = valueToGraph(val);
                if (i != 0) {
                    g.drawLine(MARGIN_X + i - 1, prev, (int) Math.round(val.getX()), (int) Math.round(val.getY()));
                }
                prev = (int) Math.round(val.getY());*/
                
            }
        }
    }
    
    private void drawGrid(Graphics g) {
        g.setColor(GRID_COLOR);
        g.drawLine(MARGIN_X, getHeight() - MARGIN_Y, getWidth() - MARGIN_X, getHeight() - MARGIN_Y);
        g.drawLine(MARGIN_X, MARGIN_Y + (int)(graphHeight() * (1 - EXTRA_AMOUNT)), getWidth() - MARGIN_X, MARGIN_Y + (int)(graphHeight() * (1 - EXTRA_AMOUNT)));
        g.drawLine(MARGIN_X, MARGIN_Y + (int)(graphHeight() * EXTRA_AMOUNT), getWidth() - MARGIN_X, MARGIN_Y + (int)(graphHeight() * EXTRA_AMOUNT));
        
        g.drawLine(MARGIN_X, getHeight() - MARGIN_Y, MARGIN_X, MARGIN_Y);
        g.drawLine(MARGIN_X + (int)(graphWidth() * EXTRA_AMOUNT), getHeight() - MARGIN_Y, MARGIN_X + (int)(graphWidth() * EXTRA_AMOUNT), MARGIN_Y);
        g.drawLine(MARGIN_X + (int)(graphWidth() * (1 - EXTRA_AMOUNT)), getHeight() - MARGIN_Y, MARGIN_X + (int)(graphWidth() * (1 - EXTRA_AMOUNT)), MARGIN_Y);
        
        g.drawString(Double.toString(lowerX()), MARGIN_X + (int)(graphWidth() * EXTRA_AMOUNT), getHeight() - MARGIN_Y / 2);
        g.drawString(Double.toString(upperX()), MARGIN_X + (int)(graphWidth() * (1 - EXTRA_AMOUNT)), getHeight() - MARGIN_Y / 2);
        g.drawString(Double.toString(lowerY()), MARGIN_X / 4, MARGIN_Y + (int)(graphHeight() * (1 - EXTRA_AMOUNT)));
        g.drawString(Double.toString(upperY()), MARGIN_X / 4, MARGIN_Y + (int)(graphHeight() * EXTRA_AMOUNT));
    }
    
    private void drawPoints(Graphics g) {
        g.setColor(POINT_COLOR);
        points.stream().map(this::valueToGraph).forEach(p -> g.drawOval((int)Math.round(p.getX()) - POINT_SIZE / 2, (int)Math.round(p.getY()) - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE));
    }
    
    private int graphWidth() {
        return getWidth() - 2 * MARGIN_X;
    }
    
    private int graphHeight() {
        return getHeight() - 2 * MARGIN_Y;
    }
    
    private double lowerX() {
        return lowerX;
    }
    
    private double upperX() {
        return upperX;
    }
    
    private double lowerY() {
        return lowerY;
    }
    
    private double upperY() {
        return upperY;
    }
    
    public void setLowerX(double lowerX) {
        this.lowerX = lowerX;
        if (cache != null && cache.getLowerX() != lowerX) {
            cache.invalidate();
        }
    }
    
    public void setUpperX(double upperX) {
        this.upperX = upperX;
        if (cache != null && cache.getUpperX() != upperX) {
            cache.invalidate();
        }
    }
    
    public void setLowerY(double lowerY) {
        this.lowerY = lowerY;
        if (cache != null && cache.getLowerY() != lowerY) {
            cache.invalidate();
        }
    }
    
    public void setUpperY(double upperY) {
        this.upperY = upperY;
        if (cache != null && cache.getUpperY() != upperY) {
            cache.invalidate();
        }
    }
    
    private PointDouble valueToGraph(PointDouble point) {
        double valX = (point.getX() - lowerX()) / (upperX() - lowerX());
        double valY = (point.getY() - lowerY()) / (upperY() - lowerY());
        return new PointDouble(MARGIN_X + (int)((graphWidth() * EXTRA_AMOUNT) * (1 - valX) + (graphWidth() * (1 - EXTRA_AMOUNT)) * valX), getHeight() - MARGIN_Y - (int)((graphHeight() * EXTRA_AMOUNT) * (1 - valY) + (graphHeight() * (1 - EXTRA_AMOUNT)) * valY));
    }
    
    private PointDouble graphToValue(PointDouble point) {
        double valX = (point.getX() - (MARGIN_X + (graphWidth() * EXTRA_AMOUNT))) / ((MARGIN_X + (graphWidth() * (1 - EXTRA_AMOUNT))) - (MARGIN_X + (graphWidth() * EXTRA_AMOUNT)));
        double valY = (point.getY() - (MARGIN_Y + (graphHeight() * (1 - EXTRA_AMOUNT)))) / ((MARGIN_Y + (graphHeight() * EXTRA_AMOUNT)) - (MARGIN_Y + (graphHeight() * (1 - EXTRA_AMOUNT))));
        return new PointDouble(lowerX() * (1 - valX) + upperX() * valX, lowerY() * (1 - valY) + upperY() * valY);
    }
    
    public List<PointDouble> getPoints() {
        return points;
    }
    
    public void setPoints(List<PointDouble> points) {
        this.points = points;
    }
    
    public static double interpolate(double a, double b, double alpha) {
        return b * alpha + a * (1 - alpha);
    }
    
    public static Color interpolate(Color c1, Color c2, double alpha) {
        double gamma = 2.2;
        int r = (int)Math.round(255 * Math.pow(Math.pow(c2.getRed() / 255.0, gamma) * alpha + Math.pow(c1.getRed() / 255.0, gamma) * (1 - alpha), 1 / gamma));
        int g = (int)Math.round(255 * Math.pow(Math.pow(c2.getGreen() / 255.0, gamma) * alpha + Math.pow(c1.getGreen() / 255.0, gamma) * (1 - alpha), 1 / gamma));
        int b = (int)Math.round(255 * Math.pow(Math.pow(c2.getBlue() / 255.0, gamma) * alpha + Math.pow(c1.getBlue() / 255.0, gamma) * (1 - alpha), 1 / gamma));
        
        return new Color(r, g, b);
    }
    
    public double lowerZ() {
        return lowerZ;
    }
    
    public void setLowerZ(double lowerZ) {
        this.lowerZ = lowerZ;
    }
    
    public double getUpperZ() {
        return upperZ;
    }
    
    public void setUpperZ(double upperZ) {
        this.upperZ = upperZ;
    }
    
    public boolean isUsingColors() {
        return usingColors;
    }
    
    public void setUsingColors(boolean usingColors) {
        this.usingColors = usingColors;
    }
    
    public double getContours() {
        return contours;
    }
    
    public void setContours(double contours) {
        this.contours = contours;
    }
    
    public double getResolution() {
        return resolution;
    }
    
    public void setResolution(int resolution) {
        this.resolution = resolution;
        if (cache != null && cache.getResolution() != resolution) {
            cache.invalidate();
        }
    }
    
    public double getContourWidth() {
        return contourWidth;
    }
    
    public void setContourWidth(double contourWidth) {
        this.contourWidth = contourWidth;
    }
    
    public double getContourOffset() {
        return contourOffset;
    }
    
    public void setContourOffset(double contourOffset) {
        this.contourOffset = contourOffset;
    }
    
    private static class FunctionCache {
        private Function function;
        private ArrayList<ArrayList<Double>> data;
        private int resolution;
        private double lowerX;
        private double upperX;
        private double lowerY;
        private double upperY;
        private double min;
        private double max;
        private boolean valid;
        
        public FunctionCache(Function function, int resolution, double lowerX, double upperX, double lowerY, double upperY) {
            if (resolution < 1 || function == null) {
                throw new IllegalArgumentException();
            }
            double dx = (upperX - lowerX) / (double)resolution;
            double dy = (upperY - lowerY) / (double)resolution;
            min = Double.NaN;
            max = Double.NaN;
            
            this.function = function;
            this.resolution = resolution;
            this.lowerX = lowerX;
            this.upperX = upperX;
            this.lowerY = lowerY;
            this.upperY = upperY;
            this.valid = true;
            
            data = new ArrayList<>();
            for (int i = 0; i <= resolution; i++) {
                data.add(new ArrayList<>());
                for (int j = 0; j <= resolution; j++) {
                    data.get(i).add(Double.NaN);
                }
            }
            
            for (int i = 0; i <= resolution; i++) {
                for (int j = 0; j <= resolution; j++) {
                    double val = function.calculate(lowerX + dx * j, lowerY + dy * i);
                    set(val, i, j);
                    if ((Double.isNaN(min) && !Double.isNaN(val)) || val < min) {
                        min = val;
                    }
                    if ((Double.isNaN(max) && !Double.isNaN(val)) || val > max) {
                        max = val;
                    }
                }
            }
        }
        
        public double get(double x, double y) {
            double lt;
            double rt;
            double lb;
            double rb;
            double t;
            double b;
            
            double col = resolution * (clampX(x) - lowerX) / (upperX - lowerX);
            double row = resolution * (clampY(y) - lowerY) / (upperY - lowerY);
            
            lb = get((int)Math.floor(row), (int)Math.floor(col));
            rb = get((int)Math.floor(row), (int)Math.ceil(col));
            lt = get((int)Math.ceil(row), (int)Math.floor(col));
            rt = get((int)Math.ceil(row), (int)Math.ceil(col));
            
            t = interpolate(lt, rt, col - (long)col);
            b = interpolate(lb, rb, col - (long)col);
            return interpolate(b, t, row - (long)row);
        }
        
        private double clampX(double x) {
            return Math.min(Math.max(x, lowerX), upperX);
        }
    
        private double clampY(double y) {
            return Math.min(Math.max(y, lowerY), upperY);
        }
    
        private double get(int row, int col) {
            return data.get(row).get(col);
        }
        
        private void set(double value, int row, int col) {
            data.get(row).set(col, value);
        }
    
        public Function getFunction() {
            return function;
        }
    
        public int getResolution() {
            return resolution;
        }
    
        public double getLowerX() {
            return lowerX;
        }
    
        public double getUpperX() {
            return upperX;
        }
    
        public double getLowerY() {
            return lowerY;
        }
    
        public double getUpperY() {
            return upperY;
        }
    
        public double getMin() {
            return min;
        }
    
        public double getMax() {
            return max;
        }
    
        public boolean isValid() {
            return valid;
        }
    
        public void invalidate() {
            this.valid = false;
        }
    }
}
