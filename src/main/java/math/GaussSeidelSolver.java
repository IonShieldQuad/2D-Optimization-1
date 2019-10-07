package math;

import java.util.function.BiFunction;

public class GaussSeidelSolver extends Solver {
    private static final PointDouble AXIS_X = new PointDouble(1, 0);
    private static final PointDouble AXIS_Y = new PointDouble(0, 1);
    
    @Override
    protected PointDouble solveInternal(PointDouble... data) {
        if (data.length < 1) {
            throw new IllegalArgumentException("No parameters passed to solver");
        }
    
        PointDouble startPoint = data[0];
        
        double prevX = startPoint.getX();
        double prevY = startPoint.getY();
        double currX = startPoint.getX();
        double currY = startPoint.getY();
        int i = 0;
        while(!(i > 1000 || (i > 0 && Math.sqrt(Math.pow(currX - prevX, 2) + Math.pow(currY - prevY, 2)) < EPSILON))) {
            prevX = currX;
            prevY = currY;
            PointDouble res;
            
            res = findMinOnAxis(AXIS_X, new PointDouble(currX, currY));
            addLine(new PointDouble(prevX, prevY), res);
            currX = res.getX();
            currY = res.getY();
    
            res = findMinOnAxis(AXIS_Y, new PointDouble(currX, currY));
            addLine(new PointDouble(currX, currY), res);
            currX = res.getX();
            currY = res.getY();
            
            i++;
        }
        
        return null;
    }
    
    @Override
    protected int getLogBatchSize() {
        return 0;
    }
    
    private PointDouble findMinOnAxis(PointDouble axis, PointDouble startPoint) {
        
        int i = 0;
        double prevX = startPoint.getX();
        double prevY = startPoint.getY();
        double currX = startPoint.getX();
        double currY = startPoint.getY();
        double nextX = currX + axis.getX() * Math.pow(2, i);
        double nextY = currY + axis.getY() * Math.pow(2, i);
        i++;
        addPoint(new PointDouble(currX, currY));
        addPoint(new PointDouble(nextX, nextY));
        if (getF().apply(currX, currY) > getF().apply(nextX, nextY)) {
            //Positive direction
            while (getF().apply(currX, currY) > getF().apply(nextX, nextY)) {
                prevX = currX;
                prevY = currY;
                currX = nextX;
                currY = nextY;
                nextX = currX + axis.getX() * Math.pow(2, i);
                nextY = currY + axis.getY() * Math.pow(2, i);
                i++;
                addPoint(new PointDouble(nextX, nextY));
            }
            double minX = prevX;
            double maxX = nextX;
            double minY = prevY;
            double maxY = nextY;
            Solver1D solver = new GoldenRatioSolver();
            solver.setF(a -> getF().apply(minX * (1 - a) + maxX * a, minY * (1 - a) + maxY * a));
            PointDouble res = solver.solve(0, 1);
            PointDouble point = new PointDouble(maxX * res.getX() + minX * (1 - res.getX()), maxY * res.getX() + minY * (1 - res.getX()));
            addPoint(point);
            return point;
        }
        else {
            //Negative direction
            nextX = currX - axis.getX() * Math.pow(2, i - 1);
            nextY = currY - axis.getY() * Math.pow(2, i - 1);
            addPoint(new PointDouble(nextX, nextY));
            while (getF().apply(currX, currY) > getF().apply(nextX, nextY)) {
                prevX = currX;
                prevY = currY;
                currX = nextX;
                currY = nextY;
                nextX = currX - axis.getX() * Math.pow(2, i);
                nextY = currY - axis.getY() * Math.pow(2, i);
                i++;
                addPoint(new PointDouble(nextX, nextY));
            }
            double minX = prevX;
            double maxX = nextX;
            double minY = prevY;
            double maxY = nextY;
            Solver1D solver = new GoldenRatioSolver();
            solver.setF(a -> getF().apply(minX * (1 - a) + maxX * a, minY * (1 - a) + maxY * a));
            PointDouble res = solver.solve(0, 1);
            PointDouble point = new PointDouble(maxX * res.getX() + minX * (1 - res.getX()), maxY * res.getX() + minY * (1 - res.getX()));
            addPoint(point);
            return point;
        }
        
    }
}
