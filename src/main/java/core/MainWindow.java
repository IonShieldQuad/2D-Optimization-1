package core;

import graphics.ContourGraphDisplay;
import math.*;
import org.mariuszgromada.math.mxparser.Function;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.Collectors;

public class MainWindow {
    private JPanel rootPanel;
    private JTextArea log;
    private JTextField lowerX;
    private JTextField upperX;
    private JButton calculateButton;
    private ContourGraphDisplay graph;
    private JTextField functionField;
    private JTextField lowerY;
    private JTextField upperY;
    private JComboBox methodBox;
    private JCheckBox useColorsCheckBox;
    private JTextField lowerZ;
    private JTextField upperZ;
    private JTextField resolutionField;
    private JTextField contoursField;
    private JTextField widthField;
    private JTextField offsetField;
    private JCheckBox alternateContoursCheckBox;
    private JTextArea fXYXTextArea;
    private JCheckBox displayValuesCheckBox;
    private JTextField startXField;
    private JTextField startYField;
    private JList<String> boundList;
    private JTextField boundField;
    private JButton deleteBoundButton;
    private JButton geBoundButton;
    private JCheckBox displayPenaltyFunctionCheckBox;
    private JComboBox penaltyFunctionBox;
    private JTextField displayIterationField;
    private JList constraintList;
    private JButton deleteConstraintButton;
    private JButton eqButton;
    private JButton leBoundButton;
    
    private static String TITLE = "2D-Optimization-1";
    private Function function;
    private Solver solver;
    PenaltyAdjuster pa;
    private List<Function> bounds = new ArrayList<>();
    private double k = 1;
    
    private MainWindow() {
        initComponents();
    }
    
    private void initComponents() {
        calculateButton.addActionListener(e -> calculate());
        boundList.setModel(new DefaultListModel<String>());
        deleteBoundButton.addActionListener(e -> {
            if (boundList.getSelectedIndex() != -1) {
                bounds.remove(boundList.getSelectedIndex());
            }
            updateBoundsList();
        });
        geBoundButton.addActionListener(e -> {
            try {
                Function function = new Function(boundField.getText());
                if (!function.checkSyntax()) {
                    throw new IllegalArgumentException("Invalid function syntax");
                }
                bounds.add(function);
            }
            catch (IllegalArgumentException ex) {
                log.append("\nAn error occurred: " + ex.getMessage());
            }
            updateBoundsList();
        });
    }
    
    private void updateBoundsList() {
        DefaultListModel<String> model = (DefaultListModel<String>) boundList.getModel();
        model.clear();
        for (Function f : bounds) {
            model.addElement(f.getFunctionName() + "(" + f.getParameterName(0) + ", " + f.getParameterName(1) + ")" + " = " + f.getFunctionExpressionString());
        }
    }
    
    private void calculate() {
        try {
            log.setText("");
            function = new Function(functionField.getText());
            if (!function.checkSyntax()) {
                throw new IllegalArgumentException("Invalid function syntax");
            }
            initSolver();
            k = 1;
            int displayIteration = Integer.parseInt(displayIterationField.getText());
            if (solver != null) {
                if (bounds == null) {
                    bounds = new ArrayList<>();
                }
                List<BiFunction<Double, Double, Double>> boundsProcessed = new ArrayList<>();
                bounds.forEach(b -> boundsProcessed.add((x, y) -> b.calculate(x, y)));
                
                BiFunction<List<Double>, Double, Double> penaltyFunction;
                switch (penaltyFunctionBox.getSelectedIndex()) {
                    case 0:
                        penaltyFunction = PenaltyAdjuster.INVERSE_PENALTY_FUNCTION;
                        break;
                    case 1:
                        penaltyFunction = PenaltyAdjuster.LOGARITHMIC_PENALTY_FUNCTION;
                        break;
                    default:
                        penaltyFunction = PenaltyAdjuster.QUADRATIC_PENALTY_FUNCTION;
                }
                
                pa = new PenaltyAdjuster(solver, penaltyFunction, boundsProcessed, true);
                pa.setDisplayIteration(displayIteration);
                
                solver.setF(function::calculate);
                List<PointDouble> startPoints = getStartPoints();
                PointDouble result = pa.solve(startPoints.toArray(new PointDouble[]{}));
                log.append("\nResult: " + new PointDouble(result.getX(), result.getY()).toString(Solver.PRECISION));
                log.append("\nLog:");
                pa.getSolutionLog().forEach(s -> log.append("\n" + s));
                k = displayIteration < 0 ? pa.getK() : Math.min(pa.getKOfIteration(displayIteration), pa.getK());
            }
            updateGraph();
        }
        catch (NumberFormatException e) {
            log.append("\nInvalid input format");
        }
        catch (IllegalArgumentException e) {
            log.append("\nAn error occurred: " + e.getMessage());
        }
    }
    
    private List<PointDouble> getStartPoints() {
        List<String> stringsX = Arrays.asList(startXField.getText().split(";\\s*"));
        List<String> stringsY = Arrays.asList(startYField.getText().split(";\\s*"));
        if (stringsX.size() != stringsY.size()) {
            throw new IllegalArgumentException("The amount of x and y values must be equal");
        }
        List<PointDouble> points = new ArrayList<>();
        for (int i = 0; i < stringsX.size(); i++) {
            points.add(new PointDouble(Double.parseDouble(stringsX.get(i)), Double.parseDouble(stringsY.get(i))));
        }
        return points;
    }
    
    private void updateGraph() {
        graph.setFunction(function);
        if (pa != null && solver!= null) {
            graph.setPoints(pa.getPoints());
            graph.setLines(pa.getLines());
        }
        else {
            graph.getPoints().clear();
            graph.getLines().clear();
        }
        if (bounds != null) {
            graph.getFunctionBounds().clear();
            bounds.forEach(b -> graph.getFunctionBounds().add((x, y) -> b.calculate(x, y)));
        }
        graph.setLowerX(Double.parseDouble(lowerX.getText()));
        graph.setUpperX(Double.parseDouble(upperX.getText()));
        graph.setLowerY(Double.parseDouble(lowerY.getText()));
        graph.setUpperY(Double.parseDouble(upperY.getText()));
        graph.setLowerZ(Double.parseDouble(lowerZ.getText()));
        graph.setUpperZ(Double.parseDouble(upperZ.getText()));
        graph.setResolution(Integer.parseInt(resolutionField.getText()));
        graph.setContours(Double.parseDouble(contoursField.getText()));
        graph.setContourWidth(Double.parseDouble(widthField.getText()));
        graph.setContourOffset(Double.parseDouble(offsetField.getText()));
        graph.setUsingColors(useColorsCheckBox.isSelected());
        graph.setAlternateContours(alternateContoursCheckBox.isSelected());
        graph.setDisplayingValues(displayValuesCheckBox.isSelected());
        graph.setDisplayPenaltyFunction(displayPenaltyFunctionCheckBox.isSelected());
        
        BiFunction<List<Double>, Double, Double> penaltyFunction;
        switch (penaltyFunctionBox.getSelectedIndex()) {
            case 0:
                penaltyFunction = PenaltyAdjuster.INVERSE_PENALTY_FUNCTION;
                break;
            case 1:
                penaltyFunction = PenaltyAdjuster.LOGARITHMIC_PENALTY_FUNCTION;
                break;
            default:
                penaltyFunction = PenaltyAdjuster.QUADRATIC_PENALTY_FUNCTION;
        }
        graph.setPenaltyFunction(l -> penaltyFunction.apply(l, k));
        /*graph.setPenaltyFunction(l -> {
            double sum = 0;
            for (double val : l) {
                if (val < 0) {
                    return Double.MAX_VALUE;
                }
                sum += 1 / val;
            }
            return Math.min(sum, Double.MAX_VALUE);
        });*/
        graph.repaint();
    }
    
    private void initSolver() {
        int index = methodBox.getSelectedIndex();
        switch (index) {
            case 0:
                //Scan
                solver = null;
                break;
            case 1:
                //Gauss-Seidel
                solver = new GaussSeidelSolver();
                break;
            case 2:
                //Powell
                solver = new PowellSolver();
                break;
            case 3:
                //Simplex
                solver = new SimplexSolver();
                break;
            case 4:
                //Gradient
                solver = new GradientSolver();
                break;
            case 5:
                //Fast Gradient
                solver = new FastGradientSolver();
                break;
            case 6:
                //Chain Gradient
                solver = new ChainGradientSolver();
                break;
            default:
                throw new IllegalArgumentException("No such method");
        }
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame(TITLE);
        MainWindow gui = new MainWindow();
        frame.setContentPane(gui.rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
