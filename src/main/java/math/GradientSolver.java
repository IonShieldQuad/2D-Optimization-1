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
        
        do {
            prev = curr;
            PointDouble gradient = gradient(prev);
            curr = prev.add(gradient.scale(-DESC_RATE));
            i++;
            
            addLine(prev, curr);
            addPoint(prev);
            addPoint(curr);
            addToLog(i + ") Point = " + prev + "; End = " + curr + "; Gradient = " + gradient);
    
            i++;
        } while (i < I_MAX && curr.add(prev.scale(-1)).length() > EPSILON);
        
        return curr;
    }
    
    @Override
    protected int getLogBatchSize() {
        return 1;
    }
}
