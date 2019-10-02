package core;

import graphics.ContourGraphDisplay;
import math.*;
import org.mariuszgromada.math.mxparser.Function;

import javax.swing.*;
import java.util.function.DoubleBinaryOperator;

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
    
    private static String TITLE = "2D-Optimization-1";
    private Function function;
    private Solver solver;
    
    private MainWindow() {
        initComponents();
    }
    
    private void initComponents() {
        calculateButton.addActionListener(e -> calculate());
    }
    
    
    
    private void calculate() {
        try {
            log.setText("");
            function = new Function(functionField.getText());
            if (!function.checkSyntax()) {
                throw new IllegalArgumentException("Invalid function syntax");
            }
            //initSolver();
            //solver.setF(function::calculate);
           // PointDouble result = solver.solve(Double.parseDouble(lowerX.getText()), Double.parseDouble(upperX.getText()));
            //log.append("\nResult: x = " + result.getX() + "; y = " + result.getY());
            //log.append("\nLog:");
            //solver.getSolutionLog().forEach(s -> log.append("\n" + s));
            updateGraph();
        }
        catch (NumberFormatException e) {
            log.append("\nInvalid input format");
        }
        catch (IllegalArgumentException e) {
            log.append("\nAn error occurred: " + e.getMessage());
        }
    }
    
    private void updateGraph() {
        graph.setFunction(function);
        //graph.setPoints(solver.getPoints());
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
        graph.repaint();
    }
    
    private void initSolver() {
        int index = methodBox.getSelectedIndex();
        switch (index) {
            case 0:
                solver = null;
                break;
            case 1:
                solver = null;
                break;
            case 2:
                solver = null;
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
