package math;

public class PowellSolver extends Solver {
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
        PointDouble axisX = AXIS_X;
        PointDouble axisY = AXIS_Y;
        while(!(i > 1000 || (i > 0 && Math.sqrt(Math.pow(currX - prevX, 2) + Math.pow(currY - prevY, 2)) < EPSILON))) {
            prevX = currX;
            prevY = currY;
            PointDouble res;
    
            addToLog(i + ") Begin: x = " + currX + "; y = " + currY);
            
            res = findMinOnAxis(axisX, new PointDouble(currX, currY));
            addLine(new PointDouble(prevX, prevY), res);
            currX = res.getX();
            currY = res.getY();
    
            addToLog(i + ") Minimum on axis X: x = " + currX + "; y = " + currY);
            
            res = findMinOnAxis(axisY, new PointDouble(currX, currY));
            addLine(new PointDouble(currX, currY), res);
            currX = res.getX();
            currY = res.getY();
    
            addToLog(i + ") Minimum on axis Y: x = " + currX + "; y = " + currY);
    
            addLine(new PointDouble(prevX, prevY), res);
            
            axisX = new PointDouble(axisX.getX() / 2.0, axisX.getY() / 2.0);
            axisY = new PointDouble(axisY.getX() / 2.0, axisY.getY() / 2.0);
            
            res = findMinOnAxis(new PointDouble((currX - prevX) / 10.0, (currY - prevY) / 10.0), new PointDouble(currX, currY));
            addLine(new PointDouble(currX, currY), res);
            currX = res.getX();
            currY = res.getY();
    
            addToLog(i + ") Minimum on diagonal axis: x = " + currX + "; y = " + currY);
            
            i++;
        }
        
        return new PointDouble(currX, currY);
    }
    
    @Override
    protected int getLogBatchSize() {
        return 1;
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
