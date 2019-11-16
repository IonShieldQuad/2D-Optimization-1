package graphics;

import math.LineDouble;
import math.PointDouble;
import org.mariuszgromada.math.mxparser.Function;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;

public class ContourGraphDisplay extends JPanel {
    private static final int MARGIN_X = 50;
    private static final int MARGIN_Y = 50;
    private static final double EXTRA_AMOUNT = 0.0;
    private static final Color GRID_COLOR = Color.GRAY;
    private static final Color HIGH_COLOR = new Color(0x7fbff3);
    private static final Color LOW_COLOR = new Color(0xec80aa);
    private static final Color CONTOUR_COLOR = new Color(0x8869ff);
    private static final Color VALUE_COLOR = new Color(0xffff22);
    private static final Color POINT_COLOR = new Color(0x0044ff);
    private static final Color LINE_COLOR = new Color(0x66ff22);
    private static final Color BOUND_COLOR = new Color(0xff0000);
    private static final int POINT_SIZE = 2;
    
    private Function function;
    private FunctionCache cache;
    private List<BiFunction<Double, Double, Double>> bounds = new ArrayList<>();
    private java.util.function.Function<List<Double>, Double> penaltyFunction;
    private List<BiFunction<Double, Double, Double>> constraints = new ArrayList<>();
    private java.util.function.Function<List<Double>, Double> constraintPenaltyFunction;
    
    private double lowerX;
    private double upperX;
    private double lowerY;
    private double upperY;
    private double lowerZ = Double.NEGATIVE_INFINITY;
    private double upperZ = Double.POSITIVE_INFINITY;
    private int resolution = 50;
    
    private double contours = 20;
    private double contourWidth = 0.2;
    private double contourOffset = 0;
    
    private boolean usingColors = true;
    private boolean alternateContours = false;
    private boolean displayingValues = false;
    private boolean displayPenaltyFunction = false;
    
    private List<PointDouble> points = new ArrayList<>();
    private List<LineDouble> lines = new ArrayList<>();
    
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
            cache = new FunctionCache(function, resolution, lowerX, upperX, lowerY, upperY, displayPenaltyFunction ? penaltyFunction : null, bounds, displayPenaltyFunction ? constraintPenaltyFunction : null, constraints);
        }
        
        drawGrid(g);
        if (function != null) {
            drawGraph(g);
        }
        if (bounds != null) {
            drawBounds(g, bounds);
        }
        if (constraints != null) {
            drawBounds(g, constraints);
        }
        if (lines != null) {
            drawLines(g);
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
                if (alternateContours && contours > 0) {
                    for (int k = 0; k <= contours; k++) {
                        if (val >= min + (k - 0.5 * contourWidth + contourOffset + 0.5) * dz && val <= min + (k + 0.5 * contourWidth + contourOffset + 0.5) * dz) {
                            matched = true;
                            break;
                        }
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
        //Draw contours
        if (!alternateContours) {
            for (int k = 0; k < contours; k++) {
                double target = min + (k + contourOffset + 0.5) * dz;
                
                //Map of all points higher/lower then the target
                ArrayList<ArrayList<Boolean>> data = new ArrayList<>(graphWidth());
                for (int i = 0; i < graphWidth(); i++) {
                    data.add(new ArrayList<>(graphHeight()));
                    for (int j = 0; j < graphHeight(); j++) {
                        PointDouble in = graphToValue(new PointDouble(i + MARGIN_X, j + MARGIN_Y));
                        double val = cache.get(in.getX(), in.getY());
                        data.get(i).add(val > target);
                    }
                }
                
                //Edge detection filter
                ArrayList<ArrayList<Boolean>> filteredData = new ArrayList<>(graphWidth());
                for (int i = 0; i < graphWidth(); i++) {
                    filteredData.add(new ArrayList<>(graphHeight()));
                    for (int j = 0; j < graphHeight(); j++) {
                        boolean tl = data.get(Math.max(Math.min(i - 1, graphWidth() - 1), 0)).get(Math.max(Math.min(j + 1, graphHeight() - 1), 0));
                        boolean tc = data.get(Math.max(Math.min(i, graphWidth() - 1), 0)).get(Math.max(Math.min(j + 1, graphHeight() - 1), 0));
                        boolean tr = data.get(Math.max(Math.min(i + 1, graphWidth() - 1), 0)).get(Math.max(Math.min(j + 1, graphHeight() - 1), 0));
                        
                        boolean cl = data.get(Math.max(Math.min(i - 1, graphWidth() - 1), 0)).get(Math.max(Math.min(j, graphHeight() - 1), 0));
                        boolean cc = data.get(Math.max(Math.min(i, graphWidth() - 1), 0)).get(Math.max(Math.min(j, graphHeight() - 1), 0));
                        boolean ct = data.get(Math.max(Math.min(i + 1, graphWidth() - 1), 0)).get(Math.max(Math.min(j, graphHeight() - 1), 0));
                        
                        boolean bl = data.get(Math.max(Math.min(i - 1, graphWidth() - 1), 0)).get(Math.max(Math.min(j - 1, graphHeight() - 1), 0));
                        boolean bc = data.get(Math.max(Math.min(i, graphWidth() - 1), 0)).get(Math.max(Math.min(j - 1, graphHeight() - 1), 0));
                        boolean br = data.get(Math.max(Math.min(i + 1, graphWidth() - 1), 0)).get(Math.max(Math.min(j - 1, graphHeight() - 1), 0));
    
                        boolean res;
                        if (contourWidth > 0.5) {
                            res = (cc && (!tl || !tc || !tr || !cl || !ct || !bl || !bc || !br)) || (!cc && (tl || tc || tr || cl || ct || bl || bc || br));
                        }
                        else {
                            if (contourWidth > 0.25) {
                                res = (cc && (!tc || !cl || !ct || !bc)) || (!cc && (tc || cl || ct || bc));
                            }
                            else {
                                res = cc && (!tc || !cl || !ct || !bc);
                            }
                        }
                        filteredData.get(i).add(res);
                    }
                }
                
                double centerDistanceMin = Double.POSITIVE_INFINITY;
                int displayValueX = 0;
                int displayValueY = 0;
                
                //Draw contour
                if (usingColors) {
                    g.setColor(CONTOUR_COLOR);
                }
                else {
                    g.setColor(Color.BLACK);
                }
                for (int i = 0; i < graphHeight(); i++) {
                    for (int j = 0; j < graphWidth(); j++) {
                        if (filteredData.get(j).get(i)) {
                            g.drawRect(j + MARGIN_X, i + MARGIN_Y, 0, 0);
                            double dist = Math.sqrt(Math.pow(i - graphHeight() / 2.0, 2) + Math.pow(j - graphWidth() / 2.0, 2));
                            if (displayingValues && dist < centerDistanceMin) {
                                centerDistanceMin = dist;
                                displayValueX = j;
                                displayValueY = i;
                            }
                        }
                    }
                }
                //Display contour value text
                if (displayingValues && displayValueX > 0 && displayValueX < graphWidth() - 1 && displayValueY > 0 && displayValueY < graphHeight() - 1) {
                    if (usingColors) {
                        g.setColor(VALUE_COLOR);
                    }
                    else {
                        g.setColor(Color.GRAY);
                    }
                    g.setFont(g.getFont().deriveFont(10.0f));
                    //g.drawString(String.format("%.3f", target), displayValueX + MARGIN_X, displayValueY + MARGIN_Y);
                    g.drawString(new DecimalFormat("0.0###", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(target), displayValueX + MARGIN_X, displayValueY + MARGIN_Y);
                }
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
        g.setColor(usingColors ? POINT_COLOR : Color.DARK_GRAY);
        points.stream().map(this::valueToGraph).forEach(p -> g.drawOval((int)Math.round(p.getX()) - POINT_SIZE / 2, (int)Math.round(p.getY()) - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE));
    }
    
    private void drawLines(Graphics g) {
        g.setColor(usingColors ? LINE_COLOR : Color.LIGHT_GRAY);
        lines.stream().map(l -> new LineDouble(valueToGraph(l.a), valueToGraph(l.b))).forEach(l -> g.drawLine((int)Math.round(l.a.getX()), (int)Math.round(l.a.getY()), (int)Math.round(l.b.getX()), (int)Math.round(l.b.getY())));
    }
    
    private  void drawBounds(Graphics g, List<BiFunction<Double, Double, Double>> bounds) {
        for (BiFunction<Double, Double, Double> bound : bounds) {
            //Map of all points higher/lower then the target
            ArrayList<ArrayList<Boolean>> data = new ArrayList<>(graphWidth());
            for (int i = 0; i < graphWidth(); i++) {
                data.add(new ArrayList<>(graphHeight()));
                for (int j = 0; j < graphHeight(); j++) {
                    PointDouble in = graphToValue(new PointDouble(i + MARGIN_X, j + MARGIN_Y));
                    double val = bound.apply(in.getX(), in.getY());
                    data.get(i).add(val > 0);
                }
            }
    
            //Edge detection filter
            ArrayList<ArrayList<Boolean>> filteredData = new ArrayList<>(graphWidth());
            for (int i = 0; i < graphWidth(); i++) {
                filteredData.add(new ArrayList<>(graphHeight()));
                for (int j = 0; j < graphHeight(); j++) {
                    boolean tl = data.get(Math.max(Math.min(i - 1, graphWidth() - 1), 0)).get(Math.max(Math.min(j + 1, graphHeight() - 1), 0));
                    boolean tc = data.get(Math.max(Math.min(i, graphWidth() - 1), 0)).get(Math.max(Math.min(j + 1, graphHeight() - 1), 0));
                    boolean tr = data.get(Math.max(Math.min(i + 1, graphWidth() - 1), 0)).get(Math.max(Math.min(j + 1, graphHeight() - 1), 0));
            
                    boolean cl = data.get(Math.max(Math.min(i - 1, graphWidth() - 1), 0)).get(Math.max(Math.min(j, graphHeight() - 1), 0));
                    boolean cc = data.get(Math.max(Math.min(i, graphWidth() - 1), 0)).get(Math.max(Math.min(j, graphHeight() - 1), 0));
                    boolean ct = data.get(Math.max(Math.min(i + 1, graphWidth() - 1), 0)).get(Math.max(Math.min(j, graphHeight() - 1), 0));
            
                    boolean bl = data.get(Math.max(Math.min(i - 1, graphWidth() - 1), 0)).get(Math.max(Math.min(j - 1, graphHeight() - 1), 0));
                    boolean bc = data.get(Math.max(Math.min(i, graphWidth() - 1), 0)).get(Math.max(Math.min(j - 1, graphHeight() - 1), 0));
                    boolean br = data.get(Math.max(Math.min(i + 1, graphWidth() - 1), 0)).get(Math.max(Math.min(j - 1, graphHeight() - 1), 0));
            
                    boolean res;
                    if (contourWidth > 0.5) {
                        res = (cc && (!tl || !tc || !tr || !cl || !ct || !bl || !bc || !br)) || (!cc && (tl || tc || tr || cl || ct || bl || bc || br));
                    } else {
                        if (contourWidth > 0.25) {
                            res = (cc && (!tc || !cl || !ct || !bc)) || (!cc && (tc || cl || ct || bc));
                        } else {
                            res = cc && (!tc || !cl || !ct || !bc);
                        }
                    }
                    filteredData.get(i).add(res);
                }
            }
    
            if (usingColors) {
                g.setColor(BOUND_COLOR);
            }
            else {
                g.setColor(Color.BLACK);
            }
            for (int i = 0; i < graphHeight(); i++) {
                for (int j = 0; j < graphWidth(); j++) {
                    if (filteredData.get(j).get(i)) {
                        g.drawRect(j + MARGIN_X, i + MARGIN_Y, 0, 0);
                    }
                }
            }
            
        }
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
    
    public List<LineDouble> getLines() {
        return lines;
    }
    
    public void setLines(List<LineDouble> lines) {
        this.lines = lines;
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
    
    public boolean isAlternateContours() {
        return alternateContours;
    }
    
    public void setAlternateContours(boolean alternateContours) {
        this.alternateContours = alternateContours;
    }
    
    public boolean isDisplayingValues() {
        return displayingValues;
    }
    
    public void setDisplayingValues(boolean displayingValues) {
        this.displayingValues = displayingValues;
    }
    
    public List<BiFunction<Double, Double, Double>> getFunctionBounds() {
        return bounds;
    }
    
    public List<BiFunction<Double, Double, Double>> getFunctionConstraints() {
        return constraints;
    }
    
    public java.util.function.Function<List<Double>, Double> getPenaltyFunction() {
        return penaltyFunction;
    }
    
    public void setPenaltyFunction(java.util.function.Function<List<Double>, Double> penaltyFunction) {
        if (!Objects.equals(this.penaltyFunction, penaltyFunction) && cache != null) {
            cache.invalidate();
        }
        this.penaltyFunction = penaltyFunction;
    }
    
    public java.util.function.Function<List<Double>, Double> getConstraintPenaltyFunction() {
        return constraintPenaltyFunction;
    }
    
    public void setConstraintPenaltyFunction(java.util.function.Function<List<Double>, Double> constraintPenaltyFunction) {
        if (!Objects.equals(this.constraintPenaltyFunction, constraintPenaltyFunction) && cache != null) {
            cache.invalidate();
        }
        this.constraintPenaltyFunction = constraintPenaltyFunction;
    }
    
    public boolean isDisplayPenaltyFunction() {
        return displayPenaltyFunction;
    }
    
    public void setDisplayPenaltyFunction(boolean displayPenaltyFunction) {
        if (this.displayPenaltyFunction != displayPenaltyFunction && cache != null) {
            cache.invalidate();
        }
        this.displayPenaltyFunction = displayPenaltyFunction;
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
        
        private java.util.function.Function<List<Double>, Double> penaltyFunction;
        private List<BiFunction<Double, Double, Double>> bounds;
        private java.util.function.Function<List<Double>, Double> constraintPenaltyFunction;
        private List<BiFunction<Double, Double, Double>> constraints;
        
        public FunctionCache(Function function, int resolution, double lowerX, double upperX, double lowerY, double upperY, java.util.function.Function<List<Double>, Double> penaltyFunction, List<BiFunction<Double, Double, Double>> bounds, java.util.function.Function<List<Double>, Double> constraintPenaltyFunction, List<BiFunction<Double, Double, Double>> constraints) {
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
    
            this.penaltyFunction = penaltyFunction;
            this.bounds = bounds;
            if (this.bounds == null) {
                this.bounds = new ArrayList<>();
            }
    
            this.constraintPenaltyFunction = constraintPenaltyFunction;
            this.constraints = constraints;
            if (this.constraints == null) {
                this.constraints = new ArrayList<>();
            }
            
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
                    if (penaltyFunction != null) {
                        List<Double> penaltyArgs = new ArrayList<>();
                        for (int k = 0; k < bounds.size(); k++) {
                            penaltyArgs.add(bounds.get(k).apply(lowerX + dx * j, lowerY + dy * i));
                        }
                        val += penaltyFunction.apply(penaltyArgs);
                    }
                    if (constraintPenaltyFunction != null) {
                        List<Double> penaltyArgs = new ArrayList<>();
                        for (int k = 0; k < constraints.size(); k++) {
                            penaltyArgs.add(constraints.get(k).apply(lowerX + dx * j, lowerY + dy * i));
                        }
                        val += constraintPenaltyFunction.apply(penaltyArgs);
                    }
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
    
        public java.util.function.Function<List<Double>, Double> getPenaltyFunction() {
            return penaltyFunction;
        }
    
        public List<BiFunction<Double, Double, Double>> getBounds() {
            return bounds;
        }
    
        public java.util.function.Function<List<Double>, Double> getConstraintPenaltyFunction() {
            return constraintPenaltyFunction;
        }
    
        public List<BiFunction<Double, Double, Double>> getConstraints() {
            return constraints;
        }
    }
}
