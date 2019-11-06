package math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimplexSolver extends Solver {
    private static final int I_MAX = 1024;
    private double alpha = 1;
    private double beta = 2;
    private double gamma = 0.5;
    private double initStep = 1;
    
    @Override
    protected PointDouble solveInternal(PointDouble... data) {
        
        PointDouble p1;
        PointDouble p2;
        PointDouble p3;
        if (data.length < 1) {
            throw new IllegalArgumentException("No parameters passed to solver");
        }
        switch (data.length) {
            case 1:
                p1 = data[0];
                p2 = new PointDouble(p1.getX() + initStep, p1.getY());
                p3 = new PointDouble(p2.getX(), p2.getY() + initStep);
                break;
            case 2:
                p1 = data[0];
                p2 = data[1];
                p3 = new PointDouble(p2.getX(), p2.getY() + initStep);
                break;
            default:
                p1 = data[0];
                p2 = data[1];
                p3 = data[2];
        }
        
        List<PointDouble> points = Arrays.asList(p1, p2, p3);
        List<Double> pointValues;
        int ph = 2;
        int pg = 1;
        int pl = 0;
        
        int iteration = 0;
        
        while (iteration < I_MAX) {
            iteration++;
            double sqrAvg = 0;
            double avg = 0;
            pointValues = points.stream().map(p -> getF().apply(p.getX(), p.getY())).collect(Collectors.toList());
            avg = (pointValues.get(0) + pointValues.get(1) + pointValues.get(2)) / 3;
            /*for (Double v : pointValues) {
                sqrAvg += Math.pow(v - avg, 2) / 3.0;
            }*/
            sqrAvg = ((pointValues.get(0) - avg) * (pointValues.get(0) - avg) + (pointValues.get(1) - avg) * (pointValues.get(1) - avg) + (pointValues.get(2) - avg) * (pointValues.get(2) - avg)) / 3.0;
            
            points.forEach(this::addPoint);
            addLine(new LineDouble(points.get(0), points.get(1)));
            addLine(new LineDouble(points.get(1), points.get(2)));
            addLine(new LineDouble(points.get(2), points.get(0)));
            addToLog(iteration + ") p1 = " + points.get(0) + "; p2 = " + points.get(1)  + "; p3 = " + points.get(2));
            
            //Finish condition
            if (Math.sqrt(sqrAvg) < EPSILON) {
                break;
            }
            //Point 1
            if (pointValues.get(0) > pointValues.get(1)) {
                if (pointValues.get(0) > pointValues.get(2)) {
                    ph = 0;
                }
                else {
                    pg = 0;
                }
            }
            else {
                if (pointValues.get(0) > pointValues.get(2)) {
                    pg = 0;
                }
                else {
                    pl = 0;
                }
            }
            //Point 2
            if (pointValues.get(1) > pointValues.get(0)) {
                if (pointValues.get(1) > pointValues.get(2)) {
                    ph = 1;
                }
                else {
                    pg = 1;
                }
            }
            else {
                if (pointValues.get(1) > pointValues.get(2)) {
                    pg = 1;
                }
                else {
                    pl = 1;
                }
            }
            //Point 3
            if (pointValues.get(2) > pointValues.get(1)) {
                if (pointValues.get(2) > pointValues.get(0)) {
                    ph = 2;
                }
                else {
                    pg = 2;
                }
            }
            else {
                if (pointValues.get(2) > pointValues.get(0)) {
                    pg = 2;
                }
                else {
                    pl = 2;
                }
            }
            //Find center
            PointDouble pc = (points.get(pl).add(points.get(pg))).scale(0.5);
            double valPc = getF().apply(pc.getX(), pc.getY());
            //Mirror
            PointDouble po = pc.scale(1 + alpha).add(points.get(ph).scale(- alpha));
            double valPo = getF().apply(po.getX(), po.getY());
            
            PointDouble pr = po;
            double valPr = valPo;
            //Extend
            if (valPo < pointValues.get(pl)) {
                pr = pc.scale(1 - beta).add(po.scale(beta));
                valPr = getF().apply(pr.getX(), pr.getY());
                addPoint(pr);
                
                //Iteration finish
                if (valPr < pointValues.get(pl)) {
                    points.set(ph, pr);
                    addToLog("\r\nSimplex extended");
                    continue;
                }
                else {
                    points.set(ph, po);
                    addToLog("\r\nSimplex mirrored");
                    continue;
                }
            }
    
            //Possible iteration finish
            if (valPo <= pointValues.get(pg)) {
                points.set(ph, po);
                addToLog("\r\nSimplex mirrored");
                continue;
            }
            
            PointDouble ps;
            double valPs;
            //Shrink
            if (valPo < pointValues.get(ph)) {
                ps = po.scale(gamma).add(pc.scale(1 - gamma));
                valPs = getF().apply(ps.getX(), ps.getY());
            }
            else {
                ps = points.get(ph).scale(gamma).add(pc.scale(1 - gamma));
                valPs = getF().apply(ps.getX(), ps.getY());
            }
            addPoint(ps);
            //Possible iteration finish
            if (valPs < pointValues.get(ph)) {
                points.set(ph, ps);
                addToLog("\r\nSimplex shrunk");
                continue;
            }
    
            //Decrease simplex scale
            for (int i = 0; i < points.size(); i++) {
                if (i == pl) {
                    continue;
                }
                points.set(i, points.get(i).add(points.get(i).add(points.get(pl).scale(-1)).scale(0.5)));
                pointValues.set(i, getF().apply(points.get(i).getX(), points.get(i).getY()));
            }
            addToLog("\r\nSimplex scale halved");
        }
        
        return points.get(pl);
    }
    
    @Override
    protected int getLogBatchSize() {
        return 2;
    }
}
