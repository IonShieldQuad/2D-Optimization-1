package math;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public abstract class Solver {
    public static final Double EPSILON = 0.001;
    private BiFunction<Double, Double, Double> f;
    private List<String> log = new ArrayList<>();
    private List<PointDouble> points = new ArrayList<>();
    private List<LineDouble> lines = new ArrayList<>();
    
    public Solver(){}
    public Solver(BiFunction<Double, Double, Double> f) {
        this.f = f;
    }

    public BiFunction<Double, Double, Double> getF() {
        return this.f;
    }

    public void setF(BiFunction<Double, Double, Double> f) {
        this.f = f;
    }
    
    public PointDouble solve(PointDouble... data) {
        points.clear();
        log.clear();
        lines.clear();
        return solveInternal(data);
    }
    
    protected abstract PointDouble solveInternal(PointDouble... data);
    protected abstract int getLogBatchSize();

    protected void addToLog(String value) {
        log.add(value);
    }
    protected void addPoint(PointDouble point) {
        points.add(point);
    }
    protected void addLine(LineDouble line) {
        lines.add(line);
    }
    protected void addLine(PointDouble a, PointDouble b) {
        lines.add(new LineDouble(a, b));
    }

    /**@return Returns log as a list of string, each representing a solution step*/
    public List<String> getSolutionLog() {
        List<String> strings = new ArrayList<>();
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < this.log.size(); ++i) {
            if (i % getLogBatchSize() == 0 && string.length() != 0) {
                strings.add(string.toString());
                string = new StringBuilder();
            }
            string.append(this.log.get(i)).append(" ");
        }
        return strings;
    }
    
    public List<PointDouble> getPoints() {
        return points;
    }
    public List<LineDouble> getLines() {
        return lines;
    }
    
    public PointDouble gradient(PointDouble point) {
        return new PointDouble(fdx(point, 1), fdy(point, 1));
    }
    
    public double fdx(PointDouble point, int order) {
        if (order < 0) {
            throw new IllegalArgumentException("Derivative order has to be non-negative");
        }
        if (order == 0) {
            return f.apply(point.getX(), point.getY());
        }
        return fdx(point.add(EPSILON, 0), order - 1) - fdx(point, order - 1);
    }
    
    public double fdy(PointDouble point, int order) {
        if (order < 0) {
            throw new IllegalArgumentException("Derivative order has to be non-negative");
        }
        if (order == 0) {
            return f.apply(point.getX(), point.getY());
        }
        return fdy(point.add(0, EPSILON), order - 1) - fdy(point, order - 1);
    }
}
