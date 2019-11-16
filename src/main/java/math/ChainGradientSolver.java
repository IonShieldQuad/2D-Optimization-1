package math;

public class ChainGradientSolver extends Solver {
    private int I_MAX = 0x00000fff;
    private double DESC_RATE = 1;
    
    @Override
    protected PointDouble solveInternal(PointDouble... data) {
        if (data.length < 1) {
            throw new IllegalArgumentException("No parameters passed to solver");
        }
        
        PointDouble curr = data[0];
        PointDouble prev = curr;
        PointDouble grad = gradient(curr);
        PointDouble s = grad.scale(-DESC_RATE);
        PointDouble prevS;
        PointDouble prevGrad;
        int i = 0;
        double delta;
        double beta;
        do {
            prev = curr;
            prevGrad = grad;
            prevS = s;
            grad = gradient(prev);
            beta = grad.lengthSquared() / prevGrad.lengthSquared();
            if (i > 0) {
                s = grad.scale(-DESC_RATE).add(prevS.scale(beta));
            }
            else {
                s = grad.scale(-DESC_RATE);
            }
            curr = findMinOnAxis(s, prev);
            
            addLine(prev, curr);
            addPoint(prev);
            addPoint(curr);
            addToLog(i + ") Start = " + prev.toString(PRECISION) + "; End = " + curr.toString(PRECISION) + "; Gradient = " + grad.toString(PRECISION) + "; Beta = " + beta);
            delta = curr.add(prev.scale(-1)).length();
            
            i++;
        } while (i < I_MAX && delta > EPSILON);
        addLine(prev, curr);
        addPoint(prev);
        addPoint(curr);
        addToLog(i + ") Start = " + prev.toString(PRECISION) + "; End = " + curr.toString(PRECISION) + "; Gradient = " + grad.toString(PRECISION) + "; Beta = " + beta);
        return curr;
    }
    
    @Override
    protected int getLogBatchSize() {
        return 1;
    }
}
