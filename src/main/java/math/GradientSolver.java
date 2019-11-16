package math;

public class GradientSolver extends Solver {
    private int I_MAX = 0x00000fff;
    private double DESC_RATE = 1;
    
    @Override
    protected PointDouble solveInternal(PointDouble... data) {
        if (data.length < 1) {
            throw new IllegalArgumentException("No parameters passed to solver");
        }
    
        PointDouble curr = data[0];
        PointDouble prev = curr;
        int i = 0;
        PointDouble gradient = gradient(curr);
        do {
            prev = curr;
            gradient = gradient(prev);
            curr = prev.add(gradient.scale(-DESC_RATE));
            
            addLine(prev, curr);
            addPoint(prev);
            addPoint(curr);
            addToLog(i + ") Point = " + prev.toString(PRECISION) + "; End = " + curr.toString(PRECISION) + "; Gradient = " + gradient.toString(PRECISION));
    
            i++;
        } while (i < I_MAX && curr.add(prev.scale(-1)).length() > EPSILON);
        addLine(prev, curr);
        addPoint(prev);
        addPoint(curr);
        addToLog(i + ") Point = " + prev.toString(PRECISION) + "; End = " + curr.toString(PRECISION) + "; Gradient = " + gradient.toString(PRECISION));
        return curr;
    }
    
    @Override
    protected int getLogBatchSize() {
        return 1;
    }
}
