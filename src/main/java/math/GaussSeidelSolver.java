package math;

import java.util.function.BiFunction;

public class GaussSeidelSolver extends Solver {
    private static final PointDouble AXIS_X = new PointDouble(1, 0);
    private static final PointDouble AXIS_Y = new PointDouble(0, 1);
    private static final int I_MAX = 1024;
    
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
        while(!(i > I_MAX || (i > 0 && Math.sqrt(Math.pow(currX - prevX, 2) + Math.pow(currY - prevY, 2)) < EPSILON))) {
            prevX = currX;
            prevY = currY;
            PointDouble res;
            
            addToLog(i + ") Begin: " + new PointDouble(currX, currY).toString(PRECISION));
            
            res = findMinOnAxis(axisX, new PointDouble(currX, currY));
            addLine(new PointDouble(prevX, prevY), res);
            currX = res.getX();
            currY = res.getY();
    
            addToLog(i + ") Minimum on axis X: " + new PointDouble(currX, currY).toString(PRECISION));
    
            res = findMinOnAxis(axisY, new PointDouble(currX, currY));
            addLine(new PointDouble(currX, currY), res);
            currX = res.getX();
            currY = res.getY();
    
            addToLog(i + ") Minimum on axis Y: " + new PointDouble(currX, currY).toString(PRECISION));
    
            axisX = new PointDouble(axisX.getX() / 2.0, axisX.getY() / 2.0);
            axisY = new PointDouble(axisY.getX() / 2.0, axisY.getY() / 2.0);
            
            i++;
        }
    
        return new PointDouble(currX, currY);
    }
    
    @Override
    protected int getLogBatchSize() {
        return 1;
    }
    
}
