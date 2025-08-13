
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.table.TableColumn;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import org.math.plot.Plot2DPanel;
import org.math.plot.PlotPanel;
import org.math.plot.plotObjects.BaseLabel;

import kisthep.file.*;
import kisthep.util.*;

//Writer for saving csv files of Plot data is asked from the command line
import java.io.*;
import java.awt.*;
import java.util.*;

public class BiMolecularReaction extends ElementaryReaction implements SessionComponent, ReadWritable {

    private ReactingStatisticalSystem reactant1, reactant2;
    public static boolean cli = false;
    public static File workfile;

    public static void write2DArraytoCSV(double[][] data, String outFile, String header) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {

            if (header != null && !header.isEmpty()) {
                writer.write(header);
                writer.newLine();
            }

            if (data == null || data.length != 2 || data[0].length != data[1].length) {
                throw new IllegalArgumentException("Input data must be a 2xN array.");
            }

            int numPoints = data[0].length;
            for (int i = 0; i < numPoints; i++) {
                double xValue = data[0][i];
                double yValue = data[1][i];
                String line = String.format(Locale.US, "%.6f,%.6f", xValue, yValue);
                writer.write(line);
                writer.newLine();
            }

        }

    }
    /* ! C O N S T R U C T O R 1 */

    // Constructor to accomodate calls without command line inputs
    public BiMolecularReaction(double T, String kineticLevel)
            throws CancelException, runTimeException, IOException, IllegalDataException {
        this(T, kineticLevel, null, null, null);
    }

    public BiMolecularReaction(double T, String kineticLevel, File react1, File react2, File reactpath)
            throws CancelException, runTimeException, IOException, IllegalDataException {
        this(T, kineticLevel, react1, react2, reactpath, 0.0);
    }

    // Edited to accept arguments from command line
    public BiMolecularReaction(double T, String kineticLevel, File react1, File react2, File reactpath,
            double revbarrier)
            throws CancelException, runTimeException, IOException, IllegalDataException {

        if (!(kineticLevel.equals("tst") || kineticLevel.equals("tst_w") || kineticLevel.equals("tst_eck")
                || kineticLevel.equals("vtst") || kineticLevel.equals("vtst_w") || kineticLevel.equals("vtst_eck"))) {

            String message = "Error occured in class BimolecularReaction in constructor 1" + Constants.newLine;
            message = message
                    + "only tst, vtst, tst_w, tst_eck, vtst_w, or vtst_eck rate constant calculations are authorized"
                    + Constants.newLine;
            message = message + "Please contact the authors" + Constants.newLine;
            JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
            throw new runTimeException();

        } // if end

        deltaNu = -1;
        this.T = T;

        Session.getCurrentSession().getFilesToBeRead().add(" reactant 1");
        Session.getCurrentSession().getFilesToBeRead().add(" reactant 2");
        Session.getCurrentSession().getFilesToBeRead().add(" TS	or reaction path");

        // No promp to ask input files if provided in cli else asked if files are null
        if (react1 == null) {
            reactant1 = new ReactingStatisticalSystem(ChemicalSystem.minimum, T);
        } else {
            reactant1 = new ReactingStatisticalSystem(ChemicalSystem.minimum, T, react1);
        }

        if (react2 == null) {
            reactant2 = new ReactingStatisticalSystem(ChemicalSystem.minimum, T);
        } else {
            reactant2 = new ReactingStatisticalSystem(ChemicalSystem.minimum, T, react2);

        }

        if (reactpath == null) {
            path = new ReactionPath(T);
        } else {
            path = new ReactionPath(T, reactpath);

        }

        testCoherenceData(); // check that reactant and TS have the same mass ...

        computeDeltaProperties();
        computeDeltaPropertiesMax();

        // if Eckart Tunneling required: ask the user for the reverse barrier (required)
        if (kineticLevel.equals("tst_eck") || kineticLevel.equals("vtst_eck")) {

            double deltaH0K = (deltaUp + deltaZPE) * 0.001;
            if (deltaH0K <= 0) {
                String message = "Error in class BiMolecularReaction in constructor" + Constants.newLine;
                message = message + "forward barrier must be positive" + Constants.newLine;
                message = message + "calculated \u0394H(0K)_forward = " + Maths.format(deltaH0K, "0.00") + "(kJ/mol)"
                        + Constants.newLine;
                JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
                throw new runTimeException();
            }
            if (cli && revbarrier!=0.0) {
                deltaH0K_rev = revbarrier* 1000; // saved in J;                
            }else {
                String txt = KisthepDialog.requireDouble(
                        "Calculated \u0394H(0K)_forward = " + Maths.format(deltaH0K, "0.00")
                                + "(kJ/mol). \n Please Enter the REVERSE zero-point corrected barrier height (kJ/mol)",
                        "KiSThelP");
                deltaH0K_rev = Double.parseDouble(txt) * 1000; // saved in J;
            }
            // deltaH0K_rev must be positive !!
            if (deltaH0K_rev <= 0) {
                String message = "Error in class BiMolecularReaction in constructor" + Constants.newLine;
                message = message + "reverse barrier must be positive" + Constants.newLine;
                JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
                throw new IllegalDataException();
            }

        } // end of Eckart test

        k = new RateConstantTST(kineticLevel, this);

    } // end C O N S T R U C T O R 1

    /* ! C O N S T R U C T O R 2 */

    public BiMolecularReaction(ActionOnFileRead read) throws IOException, IllegalDataException {

        load(read);

    } // end of the constructor 2

    /* M E T H O D S */

    /**************************************/
    /* t e s t C o h e r e n c e D a t a */ // TEST OF DATA COHERENCE
    /************************************/

    public void testCoherenceData() throws IllegalDataException {

        // ! check that reactants and TS (and path points) are compatible points

        ReactionPathPoint currentPoint;

        int freqNbR1 = reactant1.getVibFreedomDegrees();
        int freqNbR2 = reactant2.getVibFreedomDegrees();
        int expectedFreqNb = 0;
        double massR1 = reactant1.getMass();
        double massR2 = reactant2.getMass();

        int freqNb = 0;

        double mass = 0.0;
        int testLinearR1 = 0, testLinearR2 = 0, testLinear = 0;

        if (reactant1.getAtomic()) {
            testLinearR1 = 3;
        } else if (reactant1.getLinear()) {
            testLinearR1 = 1;
        }

        if (reactant2.getAtomic()) {
            testLinearR2 = 3;
        } else if (reactant2.getLinear()) {
            testLinearR2 = 1;
        }

        for (int iPoint = 0; iPoint <= path.getTrajectory().size() - 1; iPoint++) {

            currentPoint = (ReactionPathPoint) path.getTrajectory().get(iPoint);
            mass = currentPoint.getMass();
            freqNb = currentPoint.getVibFreedomDegrees();

            testLinear = 0;
            if (currentPoint.getAtomic()) {
                testLinear = 3;
            } else if (currentPoint.getLinear()) {
                testLinear = 1;
            }

            // ! compare masses
            if (Math.abs((massR1 + massR2) - mass) > Constants.massEpsilon) {

                String message = "exception caught in Class BimolecularReaction, in method testCoherenceData"
                        + Constants.newLine;
                message = message + "sum of reactants masses is: " + (massR1 + massR2) + Constants.newLine;
                message = message + "path point " + (iPoint + 1) + " mass is: " + mass + Constants.newLine;
                message = message + "Should be the same" + Constants.newLine;
                JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

                throw new IllegalDataException();
            } // if end

            // ! check that the number of frequencies for each point is compatible with the
            // reactants frequencies
            // ! depending on linearity of r1, R2 and TS (path point), and atomic property,
            // the formula is
            // ! freqNb(TS) = freqNb(R1+R2) + 6 + [testLinear(TS) - (testLinear(R1) +
            // testLinear(R2)) ]
            // ! with testLinear = 1 if linear, testLinear=0 if non-linear, testLinear=3 if
            // atomic

            // explanation of the following formula:
            // the regular case is : nothing is linear nor atomic, then we goes from the
            // situation of
            // two reactants having : (3N1 - 6) vib freq. and 3 global rot and 3 global
            // Trans
            // (3N2 - 6) vib freq. and 3 global rot and 3 global Trans
            // to a TS having : 3N1+3N2-6 vib freq. and 3 global rot and 3 global Trans
            // --> we see that 3N1+3N2-6 = freqNb(R1+R2) + 6 + 0

            // if one reactant is linear, it is already included in freqNbR1,
            // then if we want to predict the number of vib frequencies of the TS
            // without linearity, we have to substract 1 (testLinear(R1) contribution) to
            // freqNb(R1+R2)+6
            // and if the TS is also linear then a contribution of +1 (testLinear(TS))
            // must be taken into account in the calculation

            // similarly for the atomic case:
            // two reactants having : (3N1 - 3) vib freq. and 3 global Trans for the atom
            // (with 3N1-3 = 0 here)
            // (3N2 - 6) vib freq. and 3 global rot and 3 global Trans
            // to a TS having : 3N1+3N2-6 vib freq. and 3 global rot and 3 global Trans
            // --> we see that 3N1+3N2-6 = freqNb(R1+R2) + 6 -3 --> a contribution of -3
            // must be taken into account for each atomic reactant

            expectedFreqNb = freqNbR1 + freqNbR2 + 6 + (testLinear - (testLinearR1 + testLinearR2));
            if (freqNb != expectedFreqNb) {

                String message = "exception caught in Class BimolecularReaction, in method testCoherenceData"
                        + Constants.newLine;
                message = message + "reactant1 + reactant2 have " + (freqNbR1 + freqNbR2) + " frequencies"
                        + Constants.newLine;
                message = message + "path point " + (iPoint + 1) + " has: " + freqNb + " frequencies."
                        + Constants.newLine;
                message = message + " Should have " + expectedFreqNb + " frequencies" + Constants.newLine;
                message = message + "(or bad (reactant1 + reactant2) frequencies number" + Constants.newLine;
                JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
                throw new IllegalDataException();
            } // if end

        } // for end

    } // end of method testCoherenceData

    /* g e t T A B L E */
    /* method that fill the table of results */

    public Vector getTableResults() throws runTimeException, IllegalDataException {

        Vector tableVector = new Vector();

        // style title
        final int titleStyle = Font.PLAIN;

        // first, get Arrhenius fit
        double[] arrhParams = new double[5];
        double initialT = T;
        arrhParams = k.getArrheniusFit(0.8 * T, 1.2 * T);// the arrhenius fit is performed in the range T +/- 20%
        this.setTemperature(initialT); // don't forget to reset temperature and properties for current T!

        // Arrhenius Title
        JTextArea arrheniusTitle = new JTextArea();
        arrheniusTitle.setEditable(false);
        int characterSize = 13;
        arrheniusTitle.setText("Arrhenius parameters are computed in the temperature range T(K) +/- 20% ");
        arrheniusTitle.setFont(new Font("SansSerif", titleStyle, characterSize));

        double kArrh = arrhParams[0] * Math.exp(-arrhParams[1] / (Constants.R * T));
        double kArrh3 = arrhParams[2] * Math.pow(T, arrhParams[3]) * Math.exp(-arrhParams[4] / (Constants.R * T));

        String[] columnNamesArrhenius = {
                "k(cm3/molec/s)", "A(cm3/molec/s)", "Ea(kJ/mol)",
                "k_Arrhenius(cm3/molec/s) 2-parameters fit (k=A.exp(-Ea/RT))" };
        Object[][] dataArrhenius = {
                { Maths.format(k.getValue(), "0.0000E00"), Maths.format(arrhParams[0], "0.0000E00"),
                        Maths.format(arrhParams[1] * 1e-3, "0.00"), Maths.format(kArrh, "0.0000E00") }
        };

        JTable tableArrhenius = new JTable(dataArrhenius, columnNamesArrhenius);
        tableArrhenius.setPreferredScrollableViewportSize(new Dimension(750, 25));

        String[] columnNamesArrhenius3 = {
                "k(cm3/molec/s)", "A", "n", "Ea(kJ/mol)",
                "k_Arrhenius(cm3/molec/s) 3-parameters fit (k=A.T^n.exp(-Ea/RT))" };
        Object[][] dataArrhenius3 = {
                { Maths.format(k.getValue(), "0.0000E00"), Maths.format(arrhParams[2], "0.0000E00"),
                        Maths.format(arrhParams[3], "0.000E00"), Maths.format(arrhParams[4] * 1e-3, "0.00"),
                        Maths.format(kArrh3, "0.0000E00") }
        };

        JTable tableArrhenius3 = new JTable(dataArrhenius3, columnNamesArrhenius3);
        tableArrhenius3.setPreferredScrollableViewportSize(new Dimension(750, 25));

        // next, prepare display of Q, E, ... information
        /*******************************/
        /* TST */
        /*****************************/

        if (k.getKineticLevel().equals("tst") || k.getKineticLevel().equals("tst_w")
                || k.getKineticLevel().equals("tst_eck")) {

            // first, build the partition functions table1
            String[] columnNames1 = { "     ",
                    "Translation",
                    "Vibration",
                    "Rotation",
                    "Electronic",
                    "Total" };

            Object[][] data1 = {
                    { "Q(reactant1)", Maths.format(reactant1.getZTrans() / Constants.NA, "0.00E00"),
                            Maths.format(reactant1.getZVib(), "0.00E00"), Maths.format(reactant1.getZRot(), "0.00E00"),
                            Maths.format(reactant1.getZElec(), "0.000"),
                            Maths.format(reactant1.getZTot() / Constants.NA, "0.00E00") },
                    { "Q(reactant2)", Maths.format(reactant2.getZTrans() / Constants.NA, "0.00E00"),
                            Maths.format(reactant2.getZVib(), "0.00E00"), Maths.format(reactant2.getZRot(), "0.00E00"),
                            Maths.format(reactant2.getZElec(), "0.000"),
                            Maths.format(reactant2.getZTot() / Constants.NA, "0.00E00") },
                    { "Q(TS 1st order Saddle Point)",
                            Maths.format(getPath().getTs().getZTrans() / Constants.NA, "0.00E00"),
                            Maths.format(getPath().getTs().getZVib(), "0.00E00"),
                            Maths.format(getPath().getTs().getZRot(), "0.00E00"),
                            Maths.format(getPath().getTs().getZElec(), "0.000"),
                            Maths.format(getPath().getTs().getZTot() / Constants.NA, "0.00E00") },
            };

            // next, build the activation results table2

            String[] columnNames2 = {
                    "Property",
                    "Value",
                    "Value(J --> cal)" };

            Object[][] data2 = {
                    { "\u0394Up (kJ/mol)", Maths.format(deltaUp * 1e-3, "0.00"),
                            (Maths.format((deltaUp / Constants.convertCalToJoule) * 1e-3, "0.00")) },
                    { "\u0394ZPE( J/mol)", Maths.format(deltaZPE, "0.00"),
                            Maths.format((deltaZPE / Constants.convertCalToJoule), "0.00") },
                    { "\u0394H(0K) (kJ/mol)", Maths.format((deltaUp + deltaZPE) * 1e-3, "0.00"),
                            Maths.format(((deltaUp + deltaZPE) / Constants.convertCalToJoule) * 1e-3, "0.00") },
                    { "\u0394H\u00B0 (kJ/mol)", Maths.format(deltaH0 * 1e-3, "0.00"),
                            Maths.format((deltaH0 / Constants.convertCalToJoule) * 1e-3, "0.00") },
                    { "\u0394S\u00B0 (J/mol/K)", Maths.format(deltaS0, "0.00"),
                            Maths.format((deltaS0 / Constants.convertCalToJoule), "0.00") },
                    { "\u0394G\u00B0 (kJ/mol)", Maths.format(deltaG0 * 1e-3, "0.00"),
                            Maths.format((deltaG0 / Constants.convertCalToJoule) * 1e-3, "0.00") },
                    { "k (cm3/molec/s)", Maths.format(k.getValue(), "0.0000E00"), "" },
                    { "", "", "" } };

            if (k.getKineticLevel().equals("tst_w")) {

                data2[7][0] = "k (cm3/molec/s)";
                data2[7][1] = Maths.format(getTunnelingFactor(), "0.00") + "(Wigner X(T)) x " +
                        Maths.format(((RateConstantTST) k).getValueTST(), "0.0000E00") + " (kTST)";

            } // end if wigner

            if (k.getKineticLevel().equals("tst_eck")) {

                data2[7][0] = "k (cm3/molec/s)";
                data2[7][1] = Maths.format(getTunnelingFactor(), "0.00") + "(Eckart X(T)) x " +
                        Maths.format(((RateConstantTST) k).getValueTST(), "0.0000E00") + " (kTST)";

            } // end if Eckart

            JTable table1 = new JTable(data1, columnNames1);
            JTable table2 = new JTable(data2, columnNames2);

            table1.setPreferredScrollableViewportSize(new Dimension(750, 90));
            table2.setPreferredScrollableViewportSize(new Dimension(750, 150));

            TableColumn column = null;
            column = table1.getColumnModel().getColumn(0);
            column.setPreferredWidth(140);

            column = tableArrhenius.getColumnModel().getColumn(0);
            column.setPreferredWidth(16);

            column = tableArrhenius.getColumnModel().getColumn(1);
            column.setPreferredWidth(75);

            column = tableArrhenius.getColumnModel().getColumn(2);
            column.setPreferredWidth(50);

            column = tableArrhenius.getColumnModel().getColumn(3);
            column.setPreferredWidth(220);

            column = tableArrhenius3.getColumnModel().getColumn(0);
            column.setPreferredWidth(45);

            column = tableArrhenius3.getColumnModel().getColumn(1);
            column.setPreferredWidth(20);

            column = tableArrhenius3.getColumnModel().getColumn(2);
            column.setPreferredWidth(25);

            column = tableArrhenius3.getColumnModel().getColumn(4);
            column.setPreferredWidth(250);

            tableVector.add(table1);
            tableVector.add(table2);
            tableVector.add(arrheniusTitle);
            tableVector.add(tableArrhenius);
            tableVector.add(tableArrhenius3);

            return tableVector;

        } // end of TST or TST_w or TST_eck

        /*******************************/
        /* VTST */
        /*****************************/
        if (k.getKineticLevel().equals("vtst") || k.getKineticLevel().equals("vtst_w")
                || k.getKineticLevel().equals("vtst_eck")) {

            // first, build the partition functions table1
            String[] columnNames1 = { "     ",
                    "Translation",
                    "Vibration",
                    "Rotation",
                    "Electronic",
                    "Total" };

            Object[][] data1 = {
                    { "Q(reactant1)", Maths.format(reactant1.getZTrans() / Constants.NA, "0.00E00"),
                            Maths.format(reactant1.getZVib(), "0.00E00"), Maths.format(reactant1.getZRot(), "0.00E00"),
                            Maths.format(reactant1.getZElec(), "0.000"),
                            Maths.format(reactant1.getZTot() / Constants.NA, "0.00E00") },
                    { "Q(reactant2)", Maths.format(reactant2.getZTrans() / Constants.NA, "0.00E00"),
                            Maths.format(reactant2.getZVib(), "0.00E00"), Maths.format(reactant2.getZRot(), "0.00E00"),
                            Maths.format(reactant2.getZElec(), "0.000"),
                            Maths.format(reactant2.getZTot() / Constants.NA, "0.00E00") },
                    { "Q(TS 1st order Saddle Point)",
                            Maths.format(getPath().getTs().getZTrans() / Constants.NA, "0.00E00"),
                            Maths.format(getPath().getTs().getZVib(), "0.00E00"),
                            Maths.format(getPath().getTs().getZRot(), "0.00E00"),
                            Maths.format(getPath().getTs().getZElec(), "0.000"),
                            Maths.format(getPath().getTs().getZTot() / Constants.NA, "0.00E00") },
                    { "Q(TS at \u0394 G\u00B0 Max)",
                            Maths.format(getPath().getG0Maximum().getZTrans() / Constants.NA, "0.00E00"),
                            Maths.format(getPath().getG0Maximum().getZVib(), "0.00E00"),
                            Maths.format(getPath().getG0Maximum().getZRot(), "0.00E00"),
                            Maths.format(getPath().getG0Maximum().getZElec(), "0.000"),
                            Maths.format(getPath().getG0Maximum().getZTot() / Constants.NA, "0.00E00") },

            };

            // next, build the activation results table2

            String[] columnNames2 = {
                    "Property",
                    "Value (TS at first-order saddle point)",
                    "Value (TS at \u0394 G\u00B0 Max)" };

            Object[][] data2 = {
                    { "\u0394Up (kJ/mol)", Maths.format(deltaUp * 1e-3, "0.00"),
                            Maths.format(deltaUpMax * 1e-3, "0.00") },
                    { "\u0394ZPE( J/mol)", Maths.format(deltaZPE, "0.00"), Maths.format(deltaZPEMax, "0.00") },
                    { "\u0394H(0K) (kJ/mol)", Maths.format((deltaUp + deltaZPE) * 1e-3, "0.00"),
                            Maths.format((deltaUpMax + deltaZPEMax) * 1e-3, "0.00") },
                    { "\u0394H\u00B0 (kJ/mol)", Maths.format(deltaH0 * 1e-3, "0.00"),
                            Maths.format(deltaH0Max * 1e-3, "0.00") },
                    { "\u0394S\u00B0 (J/mol/K)", Maths.format(deltaS0, "0.00"), Maths.format(deltaS0Max, "0.00") },
                    { "\u0394G\u00B0 (kJ/mol)", Maths.format(deltaG0 * 1e-3, "0.00"),
                            Maths.format(deltaG0Max * 1e-3, "0.00") },
                    { "k (cm3/molec/s)",
                            Maths.format(((RateConstantTST) k).getValueTST() * getTunnelingFactor(), "0.0000E00"),
                            Maths.format(k.getValue(), "0.0000E00") },
                    // pay attention: getValueTST() returns the TST (not VTST) rate constant without
                    // tunneling (contrary to getValue)
                    { "", "", "" } };

            if (k.getKineticLevel().equals("vtst_w")) {

                data2[7][0] = "k (cm3/molec/s)";
                data2[7][1] = Maths.format(getTunnelingFactor(), "0.00") + "(Wigner X(T)) x " +
                        Maths.format(((RateConstantTST) k).getValueTST(), "0.0000E00") + " (kTST)";
                data2[7][2] = Maths.format(getTunnelingFactor(), "0.00") + "(Wigner X(T)) x " +
                        Maths.format(k.getValue() / getTunnelingFactor(), "0.0000E00") + " (kVTST)";

            } // end if Wigner

            if (k.getKineticLevel().equals("vtst_eck")) {

                data2[7][0] = "k (cm3/molec/s)";
                data2[7][1] = Maths.format(getTunnelingFactor(), "0.00") + "(Eckart X(T)) x " +
                        Maths.format(((RateConstantTST) k).getValueTST(), "0.0000E00") + " (kTST)";
                data2[7][2] = Maths.format(getTunnelingFactor(), "0.00") + "(Eckart X(T)) x " +
                        Maths.format(k.getValue() / getTunnelingFactor(), "0.0000E00") + " (kVTST)";

            } // end if Eckart

            JTable table1 = new JTable(data1, columnNames1);
            JTable table2 = new JTable(data2, columnNames2);

            table1.setPreferredScrollableViewportSize(new Dimension(750, 90));
            table2.setPreferredScrollableViewportSize(new Dimension(750, 150));

            TableColumn column = null;
            column = table1.getColumnModel().getColumn(0);
            column.setPreferredWidth(140);

            column = tableArrhenius.getColumnModel().getColumn(0);
            column.setPreferredWidth(16);

            column = tableArrhenius.getColumnModel().getColumn(1);
            column.setPreferredWidth(75);

            column = tableArrhenius.getColumnModel().getColumn(2);
            column.setPreferredWidth(10);

            column = tableArrhenius.getColumnModel().getColumn(3);
            column.setPreferredWidth(300);

            column = tableArrhenius3.getColumnModel().getColumn(0);
            column.setPreferredWidth(45);

            column = tableArrhenius3.getColumnModel().getColumn(1);
            column.setPreferredWidth(20);

            column = tableArrhenius3.getColumnModel().getColumn(2);
            column.setPreferredWidth(25);

            column = tableArrhenius3.getColumnModel().getColumn(3);
            column.setPreferredWidth(10);

            column = tableArrhenius3.getColumnModel().getColumn(4);
            column.setPreferredWidth(350);

            tableVector.add(table1);
            tableVector.add(table2);
            tableVector.add(arrheniusTitle);
            tableVector.add(tableArrhenius);
            tableVector.add(tableArrhenius3);

            return tableVector;

        } // end of vtst or vtst_w or vtst_eck

        else {

            // normally, the following statement is not executed
            String message = "Error occured in class BimolecularReaction in method getTableResults" + Constants.newLine;
            message = message + "kinetic level " + k.getKineticLevel() + " has not been recognized" + Constants.newLine;
            message = message + "Please contact the authors" + Constants.newLine;
            JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
            throw new IllegalDataException();

        }

    }// end of method getTableResuts

    public Vector getTextResults() throws runTimeException, IllegalDataException {

        TitledPane titledPane = new TitledPane();
        Vector vectorResult = new Vector();

        // ----------------- create a DisplayPanel
        JPanel displayPanel = new JPanel();

        // the layout of the resultsPane
        GridBagLayout gbl1 = new GridBagLayout();
        displayPanel.setLayout(gbl1);
        GridBagConstraints gbc = new GridBagConstraints();
        final int titleStyle = Font.BOLD;

        // a title
        JTextArea titleArea = new JTextArea();
        titleArea.setBackground(new Color(255, 233, 103));
        titleArea.setEditable(false);
        int characterSize = 13;

        // ONLY if Variational level of theory
        // add the information of the irc at which deltaG0 is maximum

        if (this.getRateConstant().getKineticLevel().contains("vtst")) {

            JTextArea infoArea = new JTextArea();
            infoArea.setEditable(false);
            characterSize = 13;
            infoArea.setText("Dividing surface position that maximizes \u0394G\u00B0: " +
                    this.getPath().getG0Maximum().getReactionCoordinate());
            infoArea.setFont(new Font("SansSerif", titleStyle, characterSize));

            gbc.gridx = 0;
            gbc.gridy = 1;
            displayPanel.add(infoArea, gbc);

        } // end of if kineticl level == variational

        // get the results put in one or more table(s)
        final Vector table = getTableResults(); // table can contain one or more tables

        titleArea.setText("Bimolecular reaction at " + Maths.format(T, "0.00") + " K and "
                + Session.getCurrentSession().getUnitSystem().convertToPressureUnit(P) + " "
                + Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit() +
                "(scaling Factor = " + Session.getCurrentSession().getScalingFactor() + " - reaction path degeneracy = "
                + statisticalFactor + ")");
        titleArea.setFont(new Font("SansSerif", titleStyle, characterSize));

        gbc.gridx = 0;
        gbc.gridy = 0;
        displayPanel.add(titleArea, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        displayPanel.add(new JScrollPane((Component) (table.get(0))), gbc);// partition functions

        gbc.gridx = 0;
        gbc.gridy = 3;
        displayPanel.add(new JScrollPane((Component) (table.get(1))), gbc);// energies, k , ... informations

        gbc.gridx = 0;
        gbc.gridy = 4;
        displayPanel.add(new JScrollPane((Component) (table.get(2))), gbc);// Arrhenius Title

        gbc.gridx = 0;
        gbc.gridy = 5;
        displayPanel.add(new JScrollPane((Component) (table.get(3))), gbc);// Arrhenius fit

        gbc.gridx = 0;
        gbc.gridy = 6;
        displayPanel.add(new JScrollPane((Component) (table.get(4))), gbc);// Arrhenius3 fit

        // wrap displayPanel and its title into a TitledPane
        titledPane = new TitledPane("Reaction properties", new JScrollPane(displayPanel));

        // add TitlePane to vectorResult
        vectorResult.add(titledPane);

        // ONLY if kinetic level is variational
        // ----------------- add a new plot2DPanel with deltaG(T)/or Up=f(IRC), or
        // deltaH0K
        // ----------------- and add a frequency plot2DPanel

        if (this.getRateConstant().getKineticLevel().contains("vtst")) {

            // plot2DPanel with deltaG(T)/or Up=f(IRC), or deltaH0K

            Plot2DPanel plot = new Plot2DPanel();
            String titlePlot, labelAbscissa, labelOrdinate, system;
            titlePlot = "Activation energy profile = f(IRC) ---- ";
            labelAbscissa = "reaction coordinate ";
            labelOrdinate = "\u0394E";

            // add a title
            BaseLabel title = new BaseLabel(titlePlot, Color.RED, 0.5, 1.15);
            title.setFont(new Font("Verdana", Font.BOLD, 20));
            plot.addPlotable(title);

            // define the legend position
            plot.addLegend("SOUTH");

            // define Labels
            String xLabel = labelAbscissa;
            String yLabel = labelOrdinate;
            ((PlotPanel) (plot)).setAxisLabels(xLabel, yLabel);

            // compute a series of deltaG and deltaH0K
            Vector traj = this.getPath().getTrajectory();
            double[] x = new double[traj.size()];
            double[] y1 = new double[traj.size()];
            double[] y2 = new double[traj.size()];
            double[] y3 = new double[traj.size()];

            ReactionPathPoint GTS; // generalized Transition State
            for (int iCoord = 0; iCoord < traj.size(); iCoord++) {
                GTS = (ReactionPathPoint) traj.get(iCoord);
                x[iCoord] = GTS.getReactionCoordinate();
                y1[iCoord] = (GTS.getUp() - (reactant1.getUp() + reactant2.getUp())) / 1000; // in kJ
                y2[iCoord] = (GTS.getG0Tot() - (reactant1.getG0Tot() + reactant2.getG0Tot())) / 1000; // in kJ
                y3[iCoord] = (GTS.getUp() + GTS.getZPE()
                        - (reactant1.getUp() + reactant1.getZPE() + reactant2.getUp() + reactant2.getZPE())) / 1000; // in
                                                                                                                     // kJ
            }

            // set x in ascending order ! Since user didn't necessary sort the IRC points
            int min;
            double bufferX, bufferY1, bufferY2, bufferY3;
            for (int iCoord = 0; iCoord < traj.size(); iCoord++) {
                min = iCoord;
                for (int jCoord = iCoord + 1; jCoord < traj.size(); jCoord++) { // detect the smallest IRC
                    if (x[jCoord] < x[min]) {
                        min = jCoord;
                    }
                } // end for jCoord
                  // set min a iCoord
                if (min != iCoord) {
                    bufferX = x[iCoord];
                    bufferY1 = y1[iCoord];
                    bufferY2 = y2[iCoord];
                    bufferY3 = y3[iCoord];
                    x[iCoord] = x[min];
                    y1[iCoord] = y1[min];
                    y2[iCoord] = y2[min];
                    y3[iCoord] = y3[min];
                    x[min] = bufferX;
                    y1[min] = bufferY1;
                    y2[min] = bufferY2;
                    y3[min] = bufferY3;
                }

            } // end for iCoord

            // add 2 line plots to the Plot2DPanel

            plot.addLinePlot("\u0394E", x, y1);
            plot.addLinePlot("\u0394H\u00B0(OK)", x, y3);
            plot.addLinePlot("\u0394G\u00B0(T)", x, y2);

            // wrap Plot2DPanel and its title into a TitledPane
            titledPane = new TitledPane("Energy profile", plot);

            // add TitlePane to vectorResult
            vectorResult.add(titledPane);

            // frequency plot2DPanel

            Plot2DPanel plotFreq = new Plot2DPanel();

            titlePlot = "Frequency = f(IRC) ---- ";
            labelAbscissa = "reaction coordinate ";
            labelOrdinate = "	\u03BD (cm-1)";

            // add a title
            title = new BaseLabel(titlePlot, Color.RED, 0.5, 1.15);
            title.setFont(new Font("Verdana", Font.BOLD, 20));
            plotFreq.addPlotable(title);

            // define the legend position
            plotFreq.addLegend("SOUTH");

            // define Labels
            xLabel = labelAbscissa;
            yLabel = labelOrdinate;
            ((PlotPanel) (plotFreq)).setAxisLabels(xLabel, yLabel);

            // get a series of vibrational frequencies along the IRC
            traj = this.getPath().getTrajectory();
            double[][] y = new double[6][traj.size()];// at most, 6 frequencies will be displayed
            double[] imFreq = new double[6];
            double[] realFreq = new double[6];
            double[] bufferY = new double[6];

            int nbImFreq, nbRealFreq, imagLimit, realLimit, nbFreq;
            nbFreq = 6; // a priori

            for (int iCoord = 0; iCoord < traj.size(); iCoord++) {
                GTS = (ReactionPathPoint) traj.get(iCoord);
                x[iCoord] = GTS.getReactionCoordinate();
                nbImFreq = GTS.getNbVibImg();
                nbRealFreq = GTS.getNbVibReal();

                // ascribing the six first lowest frequencies will depend on the number of
                // imaginary frequencies:
                imagLimit = 1;
                realLimit = 5; // default values

                if (nbImFreq >= 6) {
                    imagLimit = 6;
                    realLimit = -1;
                }
                if ((nbImFreq < 6) && (6 <= (nbImFreq + nbRealFreq))) {
                    imagLimit = nbImFreq;
                    realLimit = 6 - nbImFreq;
                }
                if ((nbImFreq + nbRealFreq) < 6) {
                    imagLimit = nbImFreq;
                    realLimit = nbRealFreq;
                    nbFreq = nbImFreq + nbRealFreq;
                } // less than 6 freq. to consider here

                // Imaginary part
                imFreq = GTS.getSortedVibImFreq();
                for (int iFreq = 0; iFreq < imagLimit; iFreq++) {
                    y[iFreq][iCoord] = imFreq[iFreq]; // in cm-1
                }

                // Real part
                realFreq = GTS.getSortedVibRealFreq();
                for (int iFreq = 0; iFreq < realLimit; iFreq++) {
                    y[iFreq + imagLimit][iCoord] = realFreq[iFreq]; // in cm-1
                }

            } // end of for (int iCoord=0; iCoord < traj.size();iCoord++ )

            // set x in ascending order ! Since user didn't necessary sort the IRC points
            for (int iCoord = 0; iCoord < traj.size(); iCoord++) {
                min = iCoord;
                for (int jCoord = iCoord + 1; jCoord < traj.size(); jCoord++) { // detect the smallest IRC
                    if (x[jCoord] < x[min]) {
                        min = jCoord;
                    }
                } // end for jCoord
                  // set min at iCoord
                if (min != iCoord) {
                    bufferX = x[iCoord];
                    for (int iFreq = 0; iFreq < nbFreq; iFreq++) {
                        bufferY[iFreq] = y[iFreq][iCoord];
                    }

                    x[iCoord] = x[min];
                    for (int iFreq = 0; iFreq < nbFreq; iFreq++) {
                        y[iFreq][iCoord] = y[iFreq][min];
                    }

                    x[min] = bufferX;
                    for (int iFreq = 0; iFreq < nbFreq; iFreq++) {
                        y[iFreq][min] = bufferY[iFreq];
                    }

                } // end of if (min != iCoord)

            } // end for iCoord

            // add 6(at most) line plots to the Plot2DPanel corresponding to the smallest 6
            // vib. Freq. of the reaction path point
            // among them, the imaginary frequencies if present along the reaction path
            for (int iFreq = 0; iFreq < nbFreq; iFreq++) {
                plotFreq.addLinePlot("\u03BD_" + iFreq, x, y[iFreq]);
            } // end of for (int iFreq=1; iFreq<=6; iFreq++)

            // wrap frequency Plot2DPanel and its title into a TitledPane
            titledPane = new TitledPane("Frequencies", plotFreq);

            // add TitlePane to vectorResult
            vectorResult.add(titledPane);

        } // end of of kineticlevel == variational

        return vectorResult;

    } // end of gettextResults method

    /*********************************/
    /* s a v e T x t R e s u l t s */
    /*******************************/
    // ! similar to getTextResults, except the output format is appropriate
    // ! to put results to text file instead of screen

    public void saveTxtResults(ActionOnFileWrite writeResults) throws runTimeException, IllegalDataException {

        String pathPoints = "";

        /***********************************/
        /* SET TITLE */
        /**********************************/
        writeResults.oneString("         Bimolecular reaction at " + Maths.format(T, "0.00") + " K and "
                + Session.getCurrentSession().getUnitSystem().convertToPressureUnit(P) + " "
                + Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit());
        writeResults.oneString("         (scaling Factor = " + Session.getCurrentSession().getScalingFactor()
                + " - reaction path degeneracy = " + statisticalFactor + ")");

        if (k.getKineticLevel().equals("vtst") || k.getKineticLevel().equals("vtst_w")
                || k.getKineticLevel().equals("vtst_eck")) {

            pathPoints = " / " + path.getPointNumber() + " point";
            if (path.getPointNumber() > 1) {
                pathPoints = pathPoints + "s";
            }

        } // if end

        writeResults.oneString('\t' + "         Kinetic Level: " + k.getKineticLevel() + pathPoints);

        writeResults.oneString("");

        if (k.getKineticLevel().equals("tst") || k.getKineticLevel().equals("tst_w")
                || k.getKineticLevel().equals("tst_eck")) {
            writeResults.oneString("Property, Value");
        } else if (k.getKineticLevel().equals("vtst") || k.getKineticLevel().equals("vtst_w")
                || k.getKineticLevel().equals("vtst_eck")) {
            writeResults
                    .oneString("Property, Value (TS at first-order saddle point), Value (TS at \u0394 G\u00B0 Max)");
        } else {
            writeResults
                    .oneString("not tst, not tst_w,not tst_eck not vtst, not vtst_w and not vtst_eck ... calculation");
        }

        // get the results put in a table
        Vector tables = getTableResults();
        String line;
        writeResults.oneString("");

        // prepare the table headers
        JTable table = (JTable) (tables.get(1));

        for (int row = 0; row < table.getRowCount(); row++) {
            line = "";
            for (int col = 0; col < table.getColumnCount(); col++) {
                line = line + table.getValueAt(row, col) + ","; // CSV format
            }
            writeResults.oneString(line);
        }

    } // end of saveTxtResults method

    /************************************************************/
    /* s a v e G r a p h i c s R e s u l t s for different T */
    /**********************************************************/

    /*
     * D E P R E C A T E D
     * 
     * public void saveGraphicsResults(ActionOnFileWrite writeResults,
     * TemperatureRange temperatureRange){
     * 
     * 
     * double tMin = temperatureRange.getTMin();
     * double tMax = temperatureRange.getTMax();
     * double tStep = temperatureRange.getTStep();
     * 
     * 
     * 
     * // local variables
     * double DH0=99999.9, DS0=99999.9, DG0=99999.9;
     * int thermoChemistryArraySize = (int) ((tMax - tMin) / tStep) + 1 ;
     * 
     * // fit variables
     * double[] xArray= new double[thermoChemistryArraySize];
     * double[] yArray= new double[thermoChemistryArraySize];
     * double[] yyArray= new double[thermoChemistryArraySize];
     * double [][] xxArray = new double[2][thermoChemistryArraySize];
     * 
     * double [][] cdata1 = null;// curves to be plotted
     * double [][] cdata2 = null;// curves to be plotted
     * cdata1 = PlotGraph.data(2, thermoChemistryArraySize);
     * cdata2 = PlotGraph.data(2, thermoChemistryArraySize);
     * 
     * double A, Ea;
     * double AA, EaEa, nn;
     * Regression reg;
     * double[] coef = new double[2];
     * double kArrhenius, modifiedKArrhenius;
     * 
     * 
     * double currentTemperature = tMin - tStep ;
     * String pathPoints="";
     */

    /***********************************/
    /* SET TITLE */
    /**********************************/

    /*
     * writeResults.oneString("         Bimolecular reaction at "+T+" K and "
     * +Session.getCurrentSession().getUnitSystem().convertToPressureUnit(P)+" "
     * +Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit());
     * writeResults.oneString("         (scaling Factor = "+Session.
     * getCurrentSession().getScalingFactor()
     * + " - statistical factor = "+ statisticalFactor +")");
     * 
     * if (k.getKineticLevel().equals("vtst") ||
     * k.getKineticLevel().equals("vtst_w") ||
     * k.getKineticLevel().equals("vtst_eck")) {
     * 
     * pathPoints = " / " + path.getPointNumber() + " point";
     * if (path.getPointNumber()>1) {pathPoints = pathPoints +"s";}
     * 
     * } // if end
     * 
     * writeResults.oneString('\t'+"         Kinetic Level: " + k.getKineticLevel()
     * + pathPoints);
     * writeResults.oneString("   ");
     * // P is a number, the unit of which is always Pascal
     * // but, must be converted in the right number if another unit is used !
     * writeResults.oneString("P = "+
     * Session.getCurrentSession().getUnitSystem().convertToPressureUnit(P)+ " "
     * +Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit());
     * 
     */
    /***********************************/
    /* Arrhenius fit */
    /**********************************/

    /*
     * for (int jElement = 0 ; jElement < thermoChemistryArraySize ; jElement =
     * jElement + 1){
     * 
     * currentTemperature = currentTemperature + tStep ;
     * setTemperature(currentTemperature) ;
     * 
     * yArray[jElement] = Math.log(k.getValue());
     * xArray[jElement] = 1.0/currentTemperature; // Temperature must be in Kelvin
     * !!
     * 
     * 
     * }// end of for (int jElement = 0 ; ...
     * reg = new Regression(xArray, yArray);
     * reg.
     * setTitle("Bimolecular Rate Constant: Arrhenius Fitting [ k(T) = A exp(-Ea/RT) ]"
     * );
     * reg.setXlegend("1/T , T(K)");
     * reg.setYlegend("ln k");
     * reg.linear();
     * 
     * coef = reg.getBestEstimates();
     * A = Math.exp(coef[0]); // frequency factor in cm3/molec/s
     * Ea = -1.0 * coef[1] * Constants.R/ 1000.0; // Arrhenius activation energy in
     * kJ.mol-1
     * 
     */

    /***********************************/
    /* Modified Arrhenius fit */
    /**********************************/
    /*
     * currentTemperature = tMin - tStep ;
     * for (int jElement = 0 ; jElement < thermoChemistryArraySize ; jElement =
     * jElement + 1){
     * 
     * currentTemperature = currentTemperature + tStep ;
     * setTemperature(currentTemperature) ;
     * 
     * yyArray[jElement] = Math.log(k.getValue());
     * xxArray[0][jElement] = Math.log(currentTemperature); // Temperature must be
     * in Kelvin !!
     * xxArray[1][jElement] = 1.0/currentTemperature; // Temperature must be in
     * Kelvin !!
     * 
     * 
     * }// end of for (int jElement = 0 ; ...
     * 
     * reg = new Regression(xxArray, yyArray);
     * reg.
     * setTitle("Bimolecular Rate Constant: modified Arrhenius Fitting [ k(T) = A T^n exp(-Ea/RT) ]"
     * );
     * reg.setXlegend("1/T , T(K)");
     * reg.setYlegend("ln k");
     * reg.linear();
     * coef = reg.getBestEstimates();
     * AA = Math.exp(coef[0]); // frequency factor in cm3/molec/s
     * nn = coef[1];
     * EaEa = -1.0 * coef[2] * Constants.R/ 1000.0; // Arrhenius activation energy
     * in kJ.mol-1
     * 
     * 
     */
    /**************************************************/
    /* Arrhenius AND Modified Arrhenius PLOTS */
    /**************************************************/
    // impossible to plot the results of a multlinear regression, thus
    // using another library after writing the text (see below)
    // ploting the modified Arrhenius and Arrhenius expressions against k KISTHEP
    /*
     * 
     * currentTemperature = tMin - tStep ;
     * for (int jElement = 0 ; jElement < thermoChemistryArraySize ; jElement =
     * jElement + 1){
     * 
     * currentTemperature = currentTemperature + tStep ;
     * 
     * 
     * cdata1[0][jElement] = xArray[jElement]; // temperature inverse
     * cdata1[1][jElement] = yArray[jElement]; // "experimental curve"
     * cdata1[2][jElement] = xArray[jElement]; // temperature inverse
     * cdata1[3][jElement] = Math.log(A * Math.exp(-1000.0 * Ea /
     * (Constants.R * currentTemperature))); // Arrhenius fit of y
     * 
     * cdata2[0][jElement] = xArray[jElement]; // temperature inverse
     * cdata2[1][jElement] = yArray[jElement]; // "experimental curve"
     * cdata2[2][jElement] = xArray[jElement]; // temperature inverse
     * cdata2[3][jElement] = Math.log(AA * Math.pow(currentTemperature, nn) *
     * Math.exp(-1000.0 * EaEa /
     * (Constants.R * currentTemperature))); // modified Arrhenius fit of y
     * 
     * 
     * }// end of for (int jElement = 0 ; ...
     * 
     * 
     * int[] lineOpt = {0,3}; // the experimental line is supressed (only point are
     * kept)
     * int[] pointOpt= {4,0}; // the fit points are removed (only the line is kept
     * for the fit)
     * 
     * PlotGraph curve1 = new PlotGraph(cdata1);
     * 
     * curve1.
     * setGraphTitle("Bimolecular Rate Constant: Arrhenius fitting [ k(T) = A exp(-Ea/RT) ]"
     * ); // Enter graph title
     * curve1.setXaxisLegend("1/T"); // Enter x-axis legend
     * curve1.setXaxisUnitsName("K-1"); // Enter x-axis units
     * curve1.setYaxisLegend("ln k"); // Enter y-axis legend
     * curve1.setLine(lineOpt);
     * curve1.setPoint(pointOpt);
     * curve1.plot();
     * 
     * PlotGraph curve2 = new PlotGraph(cdata2);
     * 
     * curve2.
     * setGraphTitle("Bimolecular Rate Constant: modified Arrhenius fitting [ k(T) = A.T^n  exp(-Ea/RT) ]"
     * ); // Enter graph title
     * curve2.setXaxisLegend("1/T"); // Enter x-axis legend
     * curve2.setXaxisUnitsName("K-1"); // Enter x-axis units
     * curve2.setYaxisLegend("ln k"); // Enter y-axis legend
     * curve2.setLine(lineOpt);
     * curve2.setPoint(pointOpt);
     * curve2.plot();
     * 
     */
    /***********************************/
    /* Filling the text file */
    /**********************************/
    /*
     * // set Title
     * writeResults.oneString("Fitting results:");
     * writeResults.
     * oneString("          Arrhenius expression: k(T) = A exp(-Ea/RT)    " +
     * "*** Ea= " + Maths.format(Ea, "00.00") + " kJ/mol, A= " + Maths.format(A,
     * "0.0000E00") + " cm3/molec/s               ***");
     * writeResults.
     * oneString("modified  Arrhenius expression: k(T) = A T^n exp(-Ea/RT)" +
     * "*** Ea= " + Maths.format(EaEa,"00.00") + " kJ/mol, A= " + Maths.format(AA,
     * "0.0000E00") + " cm3/molec/s/K^n, n="+
     * Maths.format(nn,"0.0000") + " ***");
     * writeResults.oneString("");
     * // without TUNNELING
     * if (k.getKineticLevel().equals("tst") || k.getKineticLevel().equals("vtst")
     * || k.getKineticLevel().equals("rrkmTightTs") ) {
     * writeResults.oneString(" T(K)" + '\t' + "k (cm3/molec/s)" + '\t' +
     * "k Arrhenius" + '\t' + "k modified Arrhenius" + '\t' +
     * "delta H0(kJ.mol-1)	delta S0(J.mol-1.K-1)	delta G0(kJ.mol-1)");
     * } // if end
     * 
     * // with TUNNELING
     * if (k.getKineticLevel().equals("tst_w") ||
     * k.getKineticLevel().equals("tst_eck") ||
     * k.getKineticLevel().equals("vtst_w")||
     * k.getKineticLevel().equals("vtst_eck")) {
     * writeResults.oneString(" T(K)" + '\t' + "X(T)"+ '\t'+ "k (cm3/molec/s)" +
     * '\t' + "k Arrhenius" + '\t' + "k modified Arrhenius" + '\t' +
     * "delta H0(kJ.mol-1)	delta S0(J.mol-1.K-1)	delta G0(kJ.mol-1)");
     * } // if end
     * 
     * 
     * currentTemperature = tMin - tStep ;
     * for (int jElement = 0 ; jElement < thermoChemistryArraySize ; jElement =
     * jElement + 1){
     * 
     * currentTemperature = currentTemperature + tStep ;
     * setTemperature(currentTemperature) ;
     * 
     * // according to the kinetic level, the deltaG0 or deltaG0MAx will be
     * displayed !
     * // AFTER the TEMPERATURE WAS UPDATED !!
     * if (k.getKineticLevel().equals("tst") || k.getKineticLevel().equals("tst_w")
     * ||
     * k.getKineticLevel().equals("tst_eck") ||
     * k.getKineticLevel().equals("rrkmTightTs")) {
     * 
     * DH0= deltaH0;
     * DS0= deltaS0;
     * DG0= deltaG0;
     * 
     * } // if end
     * 
     * 
     * if (k.getKineticLevel().equals("vtst") ||
     * k.getKineticLevel().equals("vtst_w") ||
     * k.getKineticLevel().equals("vtst_eck")) {
     * 
     * DH0= deltaH0Max;
     * DS0= deltaS0Max;
     * DG0= deltaG0Max;
     * 
     * } // if end
     * 
     * 
     * kArrhenius = A * Math.exp(-1000.0 * Ea / (Constants.R * currentTemperature));
     * modifiedKArrhenius = AA * Math.pow(currentTemperature, nn) * Math.exp(-1000.0
     * * EaEa / (Constants.R * currentTemperature));
     * 
     * // without TUNNELING
     * if (k.getKineticLevel().equals("tst") || k.getKineticLevel().equals("vtst")
     * || k.getKineticLevel().equals("rrkmTightTs") ) {
     * writeResults.oneString(" " + currentTemperature+ '\t' +
     * Maths.format(k.getValue(),"0.0000E00") +
     * '\t'+
     * Maths.format(kArrhenius, "0.0000E00") +'\t'+
     * Maths.format(modifiedKArrhenius, "0.0000E00") +'\t' + '\t' +
     * Maths.format(DH0 * 1e-3,"0.00")+
     * "			 " +
     * Maths.format(DS0,"0.00") +
     * "			 " +
     * Maths.format(DG0 * 1e-3,"0.00") );
     * }// end if without tunneling
     * 
     * 
     * // with TUNNELING
     * if (k.getKineticLevel().equals("tst_w") ||
     * k.getKineticLevel().equals("tst_eck") ||
     * k.getKineticLevel().equals("vtst_w")||
     * k.getKineticLevel().equals("vtst_eck")) {
     * writeResults.oneString(" " + currentTemperature+ '\t' +
     * Maths.format(getTunnelingFactor(),"##.00") +'\t' +
     * Maths.format(k.getValue(),"0.0000E00") +
     * '\t'+
     * Maths.format(kArrhenius, "0.0000E00") +'\t'+
     * Maths.format(modifiedKArrhenius, "0.0000E00") +'\t' + '\t' +
     * Maths.format(DH0 * 1e-3,"0.00")+
     * "			 " +
     * Maths.format(DS0,"0.00") +
     * "			 " +
     * Maths.format(DG0 * 1e-3,"0.00") );
     * }// end if with tunneling
     * 
     * 
     * 
     * }// end of for (int jElement = 0 ; ...
     * 
     * 
     * 
     * } // end of saveGraphicsResults method
     */

    /************************************************************/
    /* s a v e G r a p h i c s R e s u l t s for different P */
    /**********************************************************/

    /*
     * D E P R E C A T E D
     * 
     * public void saveGraphicsResults(ActionOnFileWrite writeResults, PressureRange
     * pressureRange){
     * 
     * 
     * double pMin = pressureRange.getPMin();
     * double pMax = pressureRange.getPMax();
     * double pStep = pressureRange.getPStep();
     * 
     * 
     * // local variables
     * int thermoChemistryArraySize = (int) ((pMax - pMin) / pStep) + 1 ;
     * 
     * 
     * double currentPressure = pMin - pStep ;
     * 
     * String pathPoints="";
     */

    /***********************************/
    /* SET TITLE */
    /**********************************/

    /*
     * writeResults.oneString("         Bimolecular reaction at "+T+" K and "
     * +Session.getCurrentSession().getUnitSystem().convertToPressureUnit(P)+" "
     * +Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit());
     * writeResults.oneString("         (scaling Factor = "+Session.
     * getCurrentSession().getScalingFactor()
     * + " - statistical factor = "+ statisticalFactor +")");
     * 
     * if (k.getKineticLevel().equals("vtst") ||
     * k.getKineticLevel().equals("vtst_w") ||
     * k.getKineticLevel().equals("vtst_eck")) {
     * 
     * pathPoints = " / " + path.getPointNumber() + " point";
     * if (path.getPointNumber()>1) {pathPoints = pathPoints +"s";}
     * 
     * } // if end
     * 
     * writeResults.oneString('\t'+"         Kinetic Level: " + k.getKineticLevel()
     * + pathPoints);
     * writeResults.oneString("   ");
     * 
     * 
     * writeResults.oneString("T = "+ T+ "K");
     * writeResults.
     * oneString("	               	delta H0(kJ.mol-1)	delta S0(J.mol-1.K-1)	delta G0(kJ.mol-1)"
     * );
     * 
     * writeResults.oneString(" " +'\t'+ '\t' + '\t' +
     * Maths.format(deltaH0 * 1e-3,"0.00")+
     * "			" +
     * Maths.format(deltaS0,"0.00") +
     * "			" +
     * Maths.format(deltaG0 * 1e-3,"0.00") );
     * 
     * 
     * 
     * writeResults.oneString("P ("+
     * Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit()
     * +")	k (cm3/molec/s)	");
     * 
     * 
     * // fills the standard free energy, enthalpy, entropy arrays
     * 
     * for (int jElement = 0 ; jElement < thermoChemistryArraySize ; jElement =
     * jElement + 1){
     * 
     * currentPressure = currentPressure + pStep ;
     * setPressure(currentPressure) ;
     * 
     * writeResults.oneString(Session.getCurrentSession().getUnitSystem().
     * convertToPressureUnit(currentPressure)+"	" +
     * Maths.format(k.getValue(),"0.0000E00") );
     * 
     * }// end of for (int jElement = 0 ; ...
     * 
     * 
     * } // end of saveGraphicsResults method
     * 
     * 
     * 
     */

    /*******************************************************************************************************/
    /* g e t R e s u l t s G r a p h i c s */
    /*******************************************************************************************************/

    public Vector getGraphicsResults(TemperatureRange temperatureRange) throws runTimeException, IllegalDataException {

        double tMin = temperatureRange.getTMin();
        double tMax = temperatureRange.getTMax();
        double tStep = temperatureRange.getTStep();
        Vector graphicVector = new Vector();// the result is represented by a vector containing all the information

        final int x = 0;
        final int y = 1;

        int thermoChemistryArraySize = (int) ((tMax - tMin) / tStep) + 1;

        if (thermoChemistryArraySize <= Constants.minArraySize) {

            String message = "Error in Class BimolecularReaction, in method getGraphicsResults(Temp)"
                    + Constants.newLine;
            message = message + "temperature range/temperature step too small for Arrhenius fitting"
                    + Constants.newLine;
            JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
            throw new runTimeException();
        }

        double[][] deltaH0Array = new double[2][thermoChemistryArraySize];
        double[][] deltaS0Array = new double[2][thermoChemistryArraySize];
        double[][] deltaG0Array = new double[2][thermoChemistryArraySize];
        double[][] deltaHArray = new double[2][thermoChemistryArraySize];
        double[][] deltaSArray = new double[2][thermoChemistryArraySize];
        double[][] deltaGArray = new double[2][thermoChemistryArraySize];

        double[][] logKArray = new double[2][thermoChemistryArraySize];
        double[][] logKArray2 = new double[2][thermoChemistryArraySize];
        double[][] logKArray3 = new double[2][thermoChemistryArraySize];
        double[][] logKArray4 = new double[2][thermoChemistryArraySize];
        double[][] logKArray5 = new double[2][thermoChemistryArraySize];

        double[][] KArray = new double[2][thermoChemistryArraySize];
        double[][] KArray2 = new double[2][thermoChemistryArraySize];
        double[][] KArray3 = new double[2][thermoChemistryArraySize];
        double[][] KArray4 = new double[2][thermoChemistryArraySize];

        double[][] logKArrhenius = new double[2][thermoChemistryArraySize];
        double[][] logKArrhenius3 = new double[2][thermoChemistryArraySize];
        double[][] transmissionCoeff = new double[2][thermoChemistryArraySize];

        double temperature = tMin - tStep;
        double tempInUserUnit;

        // get the Arrhenius 2-parameters fit
        double[] arrhParams = new double[5];
        double initialT = T;
        arrhParams = k.getArrheniusFit(tMin, tMax);// 2- and 3-parameters Arrhenius fit in middle of the interval
        this.setTemperature(initialT); // don't forget to reset temperature and properties for current T!

        boolean flagTun = false, flagVTST = false, flagVTSTTun = false;

        // fills the arrays for, free energy, enthalpy, entropy

        for (int jElement = 0; jElement < thermoChemistryArraySize; jElement = jElement + 1) {

            temperature = temperature + tStep;
            setTemperature(temperature); // temperature in K, all properties are refreshed

            // don't forget to convert temperature to be displayed to the User unit !
            tempInUserUnit = Session.getCurrentSession().getUnitSystem().convertToTemperatureUnit(temperature);

            deltaH0Array[x][jElement] = tempInUserUnit;
            deltaH0Array[y][jElement] = deltaH0 / 1000.0; // in kJ/mol

            deltaS0Array[x][jElement] = tempInUserUnit;
            deltaS0Array[y][jElement] = deltaS0; // in J/K/mol

            deltaG0Array[x][jElement] = tempInUserUnit;
            deltaG0Array[y][jElement] = deltaG0 / 1000.0;// in kJ/mol

            deltaHArray[x][jElement] = tempInUserUnit;
            deltaHArray[y][jElement] = deltaH / 1000.0; // in kJ/mol

            deltaSArray[x][jElement] = tempInUserUnit;
            deltaSArray[y][jElement] = deltaS;// in J/K/mol

            deltaGArray[x][jElement] = tempInUserUnit;
            deltaGArray[y][jElement] = deltaG / 1000.0;// in kJ/mol

            // always compute kTST (no RRKM here)
            logKArray[x][jElement] = (1.0 / temperature);// we keep the Kelvin unit here !
            logKArray[y][jElement] = Math.log(((RateConstantTST) k).getValueTST()) / Math.log(10.0); // WARNING --> log
                                                                                                     // !!

            KArray[x][jElement] = temperature;// we keep the Kelvin unit here !
            KArray[y][jElement] = ((RateConstantTST) k).getValueTST();

            // testing each the three additional possibles kinetic_level_cases
            if (this.getRateConstant().getKineticLevel().contains("_")) { // only tunneling effect TST/tunnel
                flagTun = true;
                logKArray2[x][jElement] = (1.0 / temperature);// we keep the Kelvin unit here !
                logKArray2[y][jElement] = Math.log(((RateConstantTST) k).getValueTST() * this.getTunnelingFactor())
                        / Math.log(10.0); // if VTST => tunneling = 1

                KArray2[x][jElement] = temperature;// we keep the Kelvin unit here !
                KArray2[y][jElement] = ((RateConstantTST) k).getValueTST() * this.getTunnelingFactor(); // if VTST =>
                                                                                                        // tunneling = 1

                transmissionCoeff[x][jElement] = tempInUserUnit;
                transmissionCoeff[y][jElement] = this.getTunnelingFactor();

            }

            if (this.getRateConstant().getKineticLevel().contains("vtst")) { // only variational effect : VTST
                flagVTST = true;
                logKArray3[x][jElement] = (1.0 / temperature);// we keep the Kelvin unit here !
                logKArray3[y][jElement] = Math.log(k.getValue() / this.getTunnelingFactor()) / Math.log(10.0); // if
                                                                                                               // VTST
                                                                                                               // =>
                                                                                                               // tunneling
                                                                                                               // = 1
                KArray3[x][jElement] = temperature;// we keep the Kelvin unit here !
                KArray3[y][jElement] = k.getValue() / this.getTunnelingFactor(); // if VTST => tunneling = 1
            }

            if ((this.getRateConstant().getKineticLevel().contains("vtst")) &&
                    (this.getRateConstant().getKineticLevel().contains("_"))) { // both tunneling and variational
                                                                                // effects VTST/tunnel
                flagVTSTTun = true;
                logKArray4[x][jElement] = (1.0 / temperature);// we keep the Kelvin unit here !
                logKArray4[y][jElement] = Math.log(k.getValue()) / Math.log(10.0);
                KArray4[x][jElement] = temperature;// we keep the Kelvin unit here !
                KArray4[y][jElement] = k.getValue();
            }

            // to be able to plot the arrhenius plot with together with the real level:
            logKArray5[x][jElement] = (1.0 / temperature);// k at the kinetic level choosen by user;
            logKArray5[y][jElement] = Math.log(k.getValue()) / Math.log(10.0);

            logKArrhenius[x][jElement] = (1.0 / temperature);// we keep the Kelvin unit here !
            logKArrhenius[y][jElement] = Math
                    .log(arrhParams[0] * Math.exp(-arrhParams[1] / (Constants.R * temperature))) / Math.log(10.0); // WARNING
                                                                                                                   // -->
                                                                                                                   // log
                                                                                                                   // !!

            logKArrhenius3[x][jElement] = (1.0 / temperature);// we keep the Kelvin unit here !
            logKArrhenius3[y][jElement] = Math.log(arrhParams[2] * Math.pow(temperature, arrhParams[3])
                    * Math.exp(-arrhParams[4] / (Constants.R * temperature))) / Math.log(10.0); // WARNING --> log !!

        } // end of for

        // buils all graphics
        DataToPlot dataToplot;
        String title, labelAbscissa, labelOrdinate;
        Vector v, w;

        // get the symbol of the current user temperature unit
        String tempSymbol = Session.getCurrentSession().getUnitSystem().getTemperatureSymbol();

        // build graphics 1 : 1 to 4 plots, depending on the level of theory
        title = "Rate Constant : log_10 k = f ( 1 / T ) ---- Bimolecular Reaction";
        labelAbscissa = "1 / T (/K)";
        labelOrdinate = "log_10 k(cm3/molec/s)";
        v = new Vector();
        w = new Vector();

        v.add("kTST");
        w.add(logKArray);

        if (flagTun) {
            v.add("kTST/Tunnel.");
            w.add(logKArray2);
        }

        if (flagVTST) {
            v.add("kVTST");
            w.add(logKArray3);
        }

        if (flagVTSTTun) {
            v.add("kVTST/Tunnel.");
            w.add(logKArray4);
        }

        dataToplot = new DataToPlot("log_10 k", title, v, labelAbscissa, labelOrdinate, w, "0.00E00", "0.0");
        graphicVector.add(dataToplot);

        // build graphics 1bis

        title = "Rate Constant :  k = f (T) ---- Bimolecular Reaction";
        labelAbscissa = "T(K)";
        labelOrdinate = "k(cm3/molec/s)";
        v = new Vector();
        w = new Vector();

        v.add("kTST");
        w.add(KArray);

        if (flagTun) {
            v.add("kTST/Tunnel.");
            w.add(KArray2);
        }

        if (flagVTST) {
            v.add("kVTST");
            w.add(KArray3);
        }

        if (flagVTSTTun) {
            v.add("kVTST/Tunnel.");
            w.add(KArray4);
        }

        dataToplot = new DataToPlot("k", title, v, labelAbscissa, labelOrdinate, w, "0.00E00", "0.0");
        graphicVector.add(dataToplot);

        // build graphics with three plots
        title = "k" + k.getKineticLevel() + " vs k_Arrhenius fit = f ( 1 / T ) ---- Bimolecular Reaction";
        labelAbscissa = "1 / T (/K)";
        labelOrdinate = "log_10 k(cm3/molec/s)";
        v = new Vector();
        v.add("k" + k.getKineticLevel());
        w = new Vector();
        w.add(logKArray5);

        v.add("2-param.: A = " + Maths.format(arrhParams[0], "0.000E00") + " Ea(kJ/mol) = "
                + Maths.format(arrhParams[1] / 1000.0, "0.00"));
        w.add(logKArrhenius);

        v.add("3-param.: A = " + Maths.format(arrhParams[2], "0.000E00") + " n = " + Maths.format(arrhParams[3], "0.00")
                + " Ea(kJ/mol) = " + Maths.format(arrhParams[4] / 1000.0, "0.00"));
        w.add(logKArrhenius3);

        dataToplot = new DataToPlot("k" + k.getKineticLevel() + " vs k_Arrhenius", title, v, labelAbscissa,
                labelOrdinate, w, "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        // build graphics
        if (flagTun) {
            title = "Tunneling effect  = f ( T ) ---- Bimolecular Reaction";
            labelAbscissa = "Temperature (" + tempSymbol + ")";
            labelOrdinate = "Transmission coefficient \u03A7(T)";
            v = new Vector();
            v.add("\u03A7(T)");
            w = new Vector();
            w.add(transmissionCoeff);

            dataToplot = new DataToPlot("\u03A7(T)", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
            graphicVector.add(dataToplot);
        }

        // build graphics

        title = "\u0394H\u00B0 = f ( T ) ---- Bimolecular Reaction";
        labelAbscissa = "Temperature (" + tempSymbol + ")";
        labelOrdinate = "standard Activation Enthalpy (kJ/mol)";
        v = new Vector();
        v.add("\u0394H\u00B0");
        w = new Vector();
        w.add(deltaH0Array);

        dataToplot = new DataToPlot("\u0394H\u00B0", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        // build graphics

        title = "\u0394S\u00B0 = f ( T ) ---- Bimolecular Reaction";
        labelAbscissa = "Temperature (" + tempSymbol + ")";
        labelOrdinate = "standard  Activation Entropy (J/mol/K)";// we keep J/mol/K unit for S (calculates as it) even
                                                                 // though it can be plotted versus temperature in
                                                                 // celsius

        v = new Vector();
        v.add("\u0394S\u00B0");
        w = new Vector();
        w.add(deltaS0Array);

        dataToplot = new DataToPlot("\u0394S\u00B0", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.0");
        graphicVector.add(dataToplot);

        // build graphics

        title = "\u0394G\u00B0 = f ( T ) ---- Bimolecular Reaction";
        labelOrdinate = "standard Activation Gibbs Free Energy (kJ/mol)";

        v = new Vector();
        v.add("\u0394G\u00B0");
        w = new Vector();
        w.add(deltaG0Array);

        dataToplot = new DataToPlot("\u0394G\u00B0", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        // build graphics

        title = "\u0394H = f ( T ) ---- Bimolecular Reaction";
        labelAbscissa = "Temperature (" + tempSymbol + ")";
        labelOrdinate = "Activation Enthalpy (kJ/mol)";

        v = new Vector();
        v.add("\u0394H");
        w = new Vector();
        w.add(deltaHArray);

        dataToplot = new DataToPlot("\u0394H", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        // build graphics

        title = "\u0394S = f ( T ) ---- Bimolecular Reaction";
        labelAbscissa = "Temperature (" + tempSymbol + ")";
        labelOrdinate = "Activation Entropy (J/mol/K)";// we keep J/mol/K unit for S (calculates as it) even though it
                                                       // can be plotted versus temperature in celsius

        v = new Vector();
        v.add("\u0394S");
        w = new Vector();
        w.add(deltaSArray);

        dataToplot = new DataToPlot("\u0394S", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.0");
        graphicVector.add(dataToplot);

        // build graphics

        title = "\u0394G = f ( T ) ---- Bimolecular Reaction";
        labelOrdinate = "Activation Gibbs Free Energy (kJ/mol)";

        v = new Vector();
        v.add("\u0394G");
        w = new Vector();
        w.add(deltaGArray);

        dataToplot = new DataToPlot("\u0394G", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        // if CLI active print all results to the screen
        if (cli) {
            try {
                String TemporaryFilename = workfile.getAbsolutePath();
                System.out
                        .println("All plotting data will be written to csv files.");
                write2DArraytoCSV(KArray, TemporaryFilename.replace(".kinp", "kTST"), "Temp,kTST");
                write2DArraytoCSV(KArray2, TemporaryFilename.replace(".kinp", "kTST_tunnelling"), "Temp,kTST_tunnel");
                write2DArraytoCSV(KArray3, TemporaryFilename.replace(".kinp", "kVTST"), "Temp,kVTST");
                write2DArraytoCSV(KArray4, TemporaryFilename.replace(".kinp", "kVTST_tunnelling"), "Temp,kVTST_tunnel");
            } catch (IOException error) {
                error.printStackTrace();
                ;
            }
        }
        return graphicVector;

    } // end of getGraphicsResults(Temperature ...)

    /*******************************************************************************************************/
    /* g e t R e s u l t s G r a p h i c s */
    /*******************************************************************************************************/
    public Vector getGraphicsResults(PressureRange pressureRange) throws runTimeException, IllegalDataException {

        double pMin = pressureRange.getPMin();
        double pMax = pressureRange.getPMax();
        double pStep = pressureRange.getPStep();

        Vector graphicVector = new Vector();

        final int x = 0;
        final int y = 1;

        int thermoChemistryArraySize = (int) ((pMax - pMin) / pStep) + 1;

        double[][] deltaH0Array = new double[2][thermoChemistryArraySize];
        double[][] deltaS0Array = new double[2][thermoChemistryArraySize];
        double[][] deltaG0Array = new double[2][thermoChemistryArraySize];
        double[][] deltaHArray = new double[2][thermoChemistryArraySize];
        double[][] deltaSArray = new double[2][thermoChemistryArraySize];
        double[][] deltaGArray = new double[2][thermoChemistryArraySize];

        double[][] logKArray = new double[2][thermoChemistryArraySize];

        double pressure = pMin - pStep;
        double pressureInUserUnit;

        // fills the arrays for, free energy, enthalpy, entropy

        for (int jElement = 0; jElement < thermoChemistryArraySize; jElement = jElement + 1) {

            pressure = pressure + pStep;
            setPressure(pressure);// pressure in Pa, all properties are refreshed

            // don't forget to convert pressure to be displayed to the pressure User unit !
            pressureInUserUnit = Session.getCurrentSession().getUnitSystem().convertToPressureUnit(pressure);

            deltaH0Array[x][jElement] = pressureInUserUnit;
            deltaH0Array[y][jElement] = deltaH0 / 1000.0; // in kJ/mol

            deltaS0Array[x][jElement] = pressureInUserUnit;
            deltaS0Array[y][jElement] = deltaS0; // in J/mol/K

            deltaG0Array[x][jElement] = pressureInUserUnit;
            deltaG0Array[y][jElement] = deltaG0 / 1000.0; // in kJ/mol

            deltaHArray[x][jElement] = pressureInUserUnit;
            deltaHArray[y][jElement] = deltaH / 1000.0; // in kJ/mol

            deltaSArray[x][jElement] = pressureInUserUnit;
            deltaSArray[y][jElement] = deltaS;// in J/K/mol

            deltaGArray[x][jElement] = pressureInUserUnit;
            deltaGArray[y][jElement] = deltaG / 1000.0;// in kJ/mol

            logKArray[x][jElement] = pressureInUserUnit;
            logKArray[y][jElement] = Math.log(k.getValue()) / Math.log(10.0); // WARNING --> log !!

        } // end of for

        // buils all graphics
        DataToPlot dataToplot;
        String title, labelAbscissa, labelOrdinate;
        Vector v, w;

        // get the symbol of the current user pressure unit
        String pressSymbol = Session.getCurrentSession().getUnitSystem().getPressureSymbol();

        // build graphics 1
        title = "Rate Constant : log_10 k = f ( P ) ---- Bimolecular Reaction";
        labelAbscissa = "P (" + pressSymbol + ")";
        labelOrdinate = "log_10 k(cm3/molec/s)";

        v = new Vector();
        v.add("k");
        w = new Vector();
        w.add(logKArray);

        dataToplot = new DataToPlot("log_10 k", title, v, labelAbscissa, labelOrdinate, w, "0.00E00", "0.0");
        graphicVector.add(dataToplot);

        // build graphics

        title = "\u0394H\u00B0 = f ( P ) ---- Bimolecular Reaction";
        labelAbscissa = "P (" + pressSymbol + ")";
        labelOrdinate = "standard Activation Enthalpy (kJ/mol)";

        v = new Vector();
        v.add("\u0394H\u00B0");
        w = new Vector();
        w.add(deltaH0Array);

        dataToplot = new DataToPlot("\u0394H\u00B0", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        // build graphics

        title = "\u0394S\u00B0 = f ( P ) ---- Bimolecular Reaction";
        labelAbscissa = "P (" + pressSymbol + ")";
        labelOrdinate = "standard Activation Entropy (J/mol/K)";// we keep J/mol/K unit for S (calculates as it) even
                                                                // though it can be plotted versus temperature in
                                                                // celsius

        v = new Vector();
        v.add("\u0394S\u00B0");
        w = new Vector();
        w.add(deltaS0Array);

        dataToplot = new DataToPlot("\u0394S\u00B0", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        // build graphics

        title = "\u0394G\u00B0 = f ( P ) ---- Bimolecular Reaction";
        labelAbscissa = "P (" + pressSymbol + ")";
        labelOrdinate = "standard Activation Gibbs Free Energy (kJ/mol)";

        v = new Vector();
        v.add("\u0394G\u00B0");
        w = new Vector();
        w.add(deltaG0Array);

        dataToplot = new DataToPlot("\u0394G\u00B0", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        // build graphics

        title = "\u0394H = f ( P ) ---- Bimolecular Reaction";
        labelAbscissa = "P (" + pressSymbol + ")";
        labelOrdinate = "Activation Enthalpy (kJ/mol)";

        v = new Vector();
        v.add("\u0394H");
        w = new Vector();
        w.add(deltaHArray);

        dataToplot = new DataToPlot("\u0394H", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        // build graphics

        title = "\u0394S = f ( P ) ---- Bimolecular Reaction";
        labelAbscissa = "P (" + pressSymbol + ")";
        labelOrdinate = "Activation Entropy (J/mol/K)";// we keep J/mol/K unit for S (calculates as it) even though it
                                                       // can be plotted versus temperature in celsius

        v = new Vector();
        v.add("\u0394S");
        w = new Vector();
        w.add(deltaSArray);

        dataToplot = new DataToPlot("\u0394S", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        // build graphics

        title = "\u0394G = f ( P ) ---- Bimolecular Reaction";
        labelAbscissa = "P (" + pressSymbol + ")";
        labelOrdinate = "Activation Gibbs Free Energy (kJ/mol)";

        v = new Vector();
        v.add("\u0394G");
        w = new Vector();
        w.add(deltaGArray);

        dataToplot = new DataToPlot("\u0394G", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        return graphicVector;

    } // end of getGraphicsResults(Pressure...)

    /*******************************************************************************************************/
    /* g e t 3 D G r a p h i c s R e s u l t s */
    /*******************************************************************************************************/

    public Vector get3DGraphicsResults(TemperatureRange temperatureRange, PressureRange pressureRange)
            throws runTimeException, IllegalDataException {

        double tMin = temperatureRange.getTMin(); // in K
        double tMax = temperatureRange.getTMax(); // in K
        double tStep = temperatureRange.getTStep(); // in K
        double pMin = pressureRange.getPMin();
        double pMax = pressureRange.getPMax();
        double pStep = pressureRange.getPStep();

        Vector graphicVector = new Vector(); // the result is represented by a vector containing all the information
                                             // i.e. a set of 3D plots

        int thermoChemistryTArraySize = (int) ((tMax - tMin) / tStep) + 1;
        int thermoChemistryPArraySize = (int) ((pMax - pMin) / pStep) + 1;

        // only accept Array with at must 25000 values (Kisthelp Plot3DPanel limitation)

        if (thermoChemistryTArraySize * thermoChemistryPArraySize > Constants.thermo3DChemArrayLimit) {

            String message = "Error: too much points on the generated surface (limit = "
                    + Constants.thermo3DChemArrayLimit + " points)" + Constants.newLine;
            JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
            throw new IllegalDataException();
        }

        double[] P = new double[thermoChemistryPArraySize];
        double[] T = new double[thermoChemistryTArraySize];
        double[] TInK = new double[thermoChemistryTArraySize];
        double[] TInverse = new double[thermoChemistryTArraySize];

        double[][] deltaH0Array = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
        double[][] deltaS0Array = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
        double[][] deltaG0Array = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
        double[][] deltaHArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
        double[][] deltaSArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
        double[][] deltaGArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];

        double[][] logKArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
        double[][] logKArray2 = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
        double[][] logKArray3 = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
        double[][] logKArray4 = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
        double[][] logKArray5 = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];

        double[][] KArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
        double[][] KArray2 = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
        double[][] KArray3 = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
        double[][] KArray4 = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];

        double[][] logKArrhenius = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
        double[][] logKArrhenius3 = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];

        double pressure = pMin - pStep; // in Pa
        double pressureInUserUnit;

        // get the Arrhenius 2-parameters fit
        double[] arrhParams = new double[5];
        double initialT = this.T; // temperature of this unimolecular reaction
        arrhParams = k.getArrheniusFit(tMin, tMax);// 2- and 3-parameters Arrhenius fit in middle of the interval
        this.setTemperature(initialT); // don't forget to reset temperature and properties for current T!

        boolean flagTun = false, flagVTST = false, flagVTSTTun = false;

        // fills the pressure array

        for (int jElement = 0; jElement < thermoChemistryPArraySize; jElement = jElement + 1) {
            pressure = pressure + pStep;
            // don't forget to convert temperature to be displayed to the User unit !
            pressureInUserUnit = Session.getCurrentSession().getUnitSystem().convertToPressureUnit(pressure);
            P[jElement] = pressureInUserUnit;
        }

        double temperature = tMin - tStep; // in K
        double tempInUserUnit;
        // fills the temperature array

        for (int jElement = 0; jElement < thermoChemistryTArraySize; jElement = jElement + 1) {
            temperature = temperature + tStep; // in K
            TInK[jElement] = temperature;
            TInverse[jElement] = 1.0 / temperature;

            // don't forget to convert temperature to be displayed to the User unit !
            tempInUserUnit = Session.getCurrentSession().getUnitSystem().convertToTemperatureUnit(temperature);
            T[jElement] = tempInUserUnit;

        }

        pressure = pMin - pStep; // in Pa
        // fills the 2D-array for all properties
        for (int jElement = 0; jElement < thermoChemistryPArraySize; jElement = jElement + 1) { // pressure loop

            pressure = pressure + pStep; // current pressure in Pa
            setPressure(pressure);// pressure in Pa, all properties are refreshed

            temperature = tMin - tStep; // in K
            for (int kElement = 0; kElement < thermoChemistryTArraySize; kElement = kElement + 1) { // temperature loop
                // take care !, the internal index is the first index of the arrays here

                temperature = temperature + tStep;
                setTemperature(temperature); // temperature in K, all properties are refreshed

                deltaH0Array[kElement][jElement] = deltaH0 / 1000.0; // in kJ/mol

                deltaS0Array[kElement][jElement] = deltaS0; // in J/K/mol

                deltaG0Array[kElement][jElement] = deltaG0 / 1000.0; // in kJ/mol

                deltaHArray[kElement][jElement] = deltaH / 1000.0; // in kJ/mol

                deltaSArray[kElement][jElement] = deltaS; // in J/K/mol

                deltaGArray[kElement][jElement] = deltaG / 1000.0; // in kJ/mol

                // always compute kTST (no RRKM here)
                logKArray[kElement][jElement] = Math.log(((RateConstantTST) k).getValueTST()) / Math.log(10.0); // WARNING
                                                                                                                // -->
                                                                                                                // log_10
                                                                                                                // !!
                KArray[kElement][jElement] = ((RateConstantTST) k).getValueTST();

                // testing each the three additional possibles kinetic_level_cases
                if (this.getRateConstant().getKineticLevel().contains("_")) { // only tunneling effect (tunnel)
                    flagTun = true;
                    logKArray2[kElement][jElement] = Math
                            .log(((RateConstantTST) k).getValueTST() * this.getTunnelingFactor()) / Math.log(10.0); // if
                                                                                                                    // VTST
                                                                                                                    // only
                                                                                                                    // choosen
                                                                                                                    // =>
                                                                                                                    // tunneling
                                                                                                                    // =
                                                                                                                    // 1
                    KArray2[kElement][jElement] = ((RateConstantTST) k).getValueTST() * this.getTunnelingFactor(); // if
                                                                                                                   // VTST
                                                                                                                   // only
                                                                                                                   // choosen
                                                                                                                   // =>
                                                                                                                   // tunneling
                                                                                                                   // =
                                                                                                                   // 1

                }

                if (this.getRateConstant().getKineticLevel().contains("vtst")) { // only variational effect : VTST
                    flagVTST = true;
                    logKArray3[kElement][jElement] = Math.log(k.getValue() / this.getTunnelingFactor())
                            / Math.log(10.0); // if VTST => tunneling = 1

                    KArray3[kElement][jElement] = k.getValue() / this.getTunnelingFactor(); // if VTST => tunneling = 1

                }

                if ((this.getRateConstant().getKineticLevel().contains("vtst")) &&
                        (this.getRateConstant().getKineticLevel().contains("_"))) { // both tunneling and variational
                                                                                    // effects VTST/tunnel
                    flagVTSTTun = true;
                    logKArray4[kElement][jElement] = Math.log(k.getValue()) / Math.log(10.0);
                    KArray4[kElement][jElement] = k.getValue();

                }

                logKArray5[kElement][jElement] = Math.log(k.getValue()) / Math.log(10.0);

                logKArrhenius[kElement][jElement] = Math
                        .log(arrhParams[0] * Math.exp(-arrhParams[1] / (Constants.R * temperature))) / Math.log(10.0); // WARNING
                                                                                                                       // -->
                                                                                                                       // log
                                                                                                                       // !!

                logKArrhenius3[kElement][jElement] = Math.log(arrhParams[2] * Math.pow(temperature, arrhParams[3])
                        * Math.exp(-arrhParams[4] / (Constants.R * temperature))) / Math.log(10.0); // WARNING --> log
                                                                                                    // !!

            } // end of temperature loop
        } // end of pressure loop

        // builds all graphics
        DataToPlot3D dataToplot;
        String title, labelX, labelY, labelZ;
        String v;

        // get the symbol of the current user temperature unit
        String tempSymbol = Session.getCurrentSession().getUnitSystem().getTemperatureSymbol();
        // get the symbol of the current user pressure unit
        String pressSymbol = Session.getCurrentSession().getUnitSystem().getPressureSymbol();

        // build graphics 1
        title = "Rate Constant :  log_10 k = f ( P, 1 / T ) ---- Bimolecular Reaction(*)";

        labelX = "P(" + pressSymbol + ")";
        labelY = "1 / T (/K)";
        labelZ = "log_10 k(cm3/molec/s) (pressure independent)";

        v = "kTST";
        dataToplot = new DataToPlot3D("log kTST", title, v, labelX, labelY, labelZ, P, TInverse, logKArray, "0.00E00",
                "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        if (flagTun) {
            v = "kTST/Tunnel.";
            dataToplot = new DataToPlot3D("log kTST/Tunnel", title, v, labelX, labelY, labelZ, P, TInverse, logKArray2,
                    "0.00E00", "0.0", "0.00E00");
            graphicVector.add(dataToplot);
        }

        if (flagVTST) {
            v = "kVTST";
            dataToplot = new DataToPlot3D("log kVTST", title, v, labelX, labelY, labelZ, P, TInverse, logKArray3,
                    "0.00E00", "0.0", "0.00E00");
            graphicVector.add(dataToplot);
        }

        if (flagVTSTTun) {
            v = "kVTST/Tunnel.";
            dataToplot = new DataToPlot3D("log kVTST/Tunnel", title, v, labelX, labelY, labelZ, P, TInverse, logKArray4,
                    "0.00E00", "0.0", "0.00E00");
            graphicVector.add(dataToplot);
        }

        // build graphics 1bis

        title = "Rate Constant :  k = f (P, T) ---- Bimolecular Reaction(*)";
        labelX = "P(" + pressSymbol + ")";
        labelY = "T (K)";
        labelZ = "k(cm3/molec/s) (pressure independent)";

        v = "kTST";
        dataToplot = new DataToPlot3D("kTST", title, v, labelX, labelY, labelZ, P, TInK, KArray, "0.00E00", "0.0",
                "0.00E00");
        graphicVector.add(dataToplot);

        if (flagTun) {
            v = "kTST/Tunnel.";
            dataToplot = new DataToPlot3D("kTST/Tunnel", title, v, labelX, labelY, labelZ, P, TInK, KArray2, "0.00E00",
                    "0.0", "0.00E00");
            graphicVector.add(dataToplot);
        }

        if (flagVTST) {
            v = "kVTST";
            dataToplot = new DataToPlot3D("kVTST", title, v, labelX, labelY, labelZ, P, TInK, KArray3, "0.00E00", "0.0",
                    "0.00E00");
            graphicVector.add(dataToplot);
        }

        if (flagVTSTTun) {
            v = "kVTST/Tunnel.";
            dataToplot = new DataToPlot3D("kVTST/Tunnel", title, v, labelX, labelY, labelZ, P, TInK, KArray4, "0.00E00",
                    "0.0", "0.00E00");
            graphicVector.add(dataToplot);
        }

        // build graphics 2
        title = "\u0394H\u00B0 = f ( P, T ) ---- Bimolecular Reaction(*)";

        labelX = "P(" + pressSymbol + ")";
        labelY = "Temperature (" + tempSymbol + ")";
        labelZ = "Activation Enthalpy (kJ/mol)";

        v = "\u0394H\u00B0 (pressure independent)";

        dataToplot = new DataToPlot3D("\u0394H\u00B0", title, v, labelX, labelY, labelZ, P, T, deltaH0Array, "0.00E00",
                "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        // build graphics 3

        title = "\u0394S\u00B0 = f ( P, T ) ---- Bimolecular Reaction(*)";
        labelX = "P(" + pressSymbol + ")";
        labelY = "Temperature (" + tempSymbol + ")";
        labelZ = "Activation Entropy (J/mol/K)";// we keep J/mol/K unit for S (calculates as it) even though it can be
                                                // plotted versus temperature in celsius

        v = "\u0394S\u00B0 (pressure independent)";
        dataToplot = new DataToPlot3D("\u0394S\u00B0", title, v, labelX, labelY, labelZ, P, T, deltaS0Array, "0.00E00",
                "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        // build graphics 4

        title = "\u0394G\u00B0 = f ( P, T ) ---- Bimolecular Reaction(*)";
        labelX = "P(" + pressSymbol + ")";
        labelY = "Temperature (" + tempSymbol + ")";
        labelZ = "Activation Gibbs Free Energy (kJ/mol)";

        v = "\u0394G\u00B0 (pressure independent)";

        dataToplot = new DataToPlot3D("\u0394G\u00B0", title, v, labelX, labelY, labelZ, P, T, deltaG0Array, "0.00E00",
                "0.0", "0.00E00");
        graphicVector.add(dataToplot);

        // build graphics 5
        title = "\u0394H = f ( P, T ) ---- Bimolecular Reaction(*)";
        labelX = "P(" + pressSymbol + ")";
        labelY = "Temperature (" + tempSymbol + ")";
        labelZ = "Activation Enthalpy (kJ/mol)";

        v = "\u0394H (pressure independent)";

        dataToplot = new DataToPlot3D("\u0394H", title, v, labelX, labelY, labelZ, P, T, deltaHArray, "0.00E00", "0.0",
                "0.00E00");
        graphicVector.add(dataToplot);

        // build graphics 6

        title = "\u0394S = f ( P, T ) ---- Bimolecular Reaction";
        labelX = "P(" + pressSymbol + ")";
        labelY = "Temperature (" + tempSymbol + ")";
        labelZ = "Activation Entropy (J/mol/K)";// we keep J/mol/K unit for S (calculates as it) even though it can be
                                                // plotted versus temperature in celsius

        v = "\u0394S";
        dataToplot = new DataToPlot3D("\u0394S", title, v, labelX, labelY, labelZ, P, T, deltaSArray, "0.00E00", "0.0",
                "0.00E00");
        graphicVector.add(dataToplot);

        // build graphics 7

        title = "\u0394G = f ( P, T ) ---- Bimolecular Reaction";
        labelX = "P(" + pressSymbol + ")";
        labelY = "Temperature (" + tempSymbol + ")";
        labelZ = "Activation Gibbs Free Energy (kJ/mol)";

        v = "\u0394G";

        dataToplot = new DataToPlot3D("\u0394G", title, v, labelX, labelY, labelZ, P, T, deltaGArray, "0.00E00", "0.0",
                "0.00E00");
        graphicVector.add(dataToplot);

        // if CLI active print all results to the screen
        if (cli) {
            try {
                String TemporaryFilename = workfile.getAbsolutePath();
                System.out
                        .println("All plotting data will be written to csv files.");
                write2DArraytoCSV(KArray, TemporaryFilename.replace(".kinp", "kTST"), "Temp,kTST");
                write2DArraytoCSV(KArray2, TemporaryFilename.replace(".kinp", "kTST_tunnelling"), "Temp,kTST_tunnel");
                write2DArraytoCSV(KArray3, TemporaryFilename.replace(".kinp", "kVTST"), "Temp,kVTST");
                write2DArraytoCSV(KArray4, TemporaryFilename.replace(".kinp", "kVTST_tunnelling"), "Temp,kVTST_tunnel");
            } catch (IOException error) {
                error.printStackTrace();
                ;
            }
        }
        return graphicVector;

    } // end of getResultGraphics

    /**************************************************************/
    /* changes the temperature T and recomputes the rate constant */
    /**************************************************************/
    public void setTemperature(double T) throws runTimeException, IllegalDataException {

        this.T = T;

        reactant1.setTemperature(T);
        reactant2.setTemperature(T);

        path.setTemperature(T);

        computeDeltaProperties();
        computeDeltaPropertiesMax();
        k.computeValue(new Temperature(T));

    } // end of setTemperature method

    /**************************************************************/
    /* changes the pressure P and recomputes the rate constant */
    /**************************************************************/
    public void setPressure(double P) throws runTimeException, IllegalDataException {

        this.P = P;

        reactant1.setPressure(P);
        reactant2.setPressure(P);

        path.setPressure(P);
        computeDeltaProperties();
        computeDeltaPropertiesMax();
        k.computeValue(new Pressure(P)); // at present time k=k(T) only

    } // end of setPressure method

    /******************************/
    /* returns the temperature T */
    /****************************/

    public double getTemperature() {
        return T;
    }

    /**********************************************************************************************/
    /* N O W, compute the deltaProperties, needed by CLASSICAL TST */
    /**********************************************************************************************/

    /********************************************************************/
    /* computes the deltaUp for the bimolecular equilibrium: A+ B <=> TS */
    /********************************************************************/

    public void computeDeltaUp() {

        deltaUp = path.getTs().getUp() - (reactant1.getUp() + reactant2.getUp());

    } // end of the computeDeltaUp method

    /********************************************************************/
    /* computes the deltaZPE for the bimolecular equilibrium: A+ B <=> TS */
    /********************************************************************/

    public void computeDeltaZPE() {

        deltaZPE = path.getTs().getZPE() - (reactant1.getZPE() + reactant2.getZPE());

    } // end of the computeDeltaZPE method

    /***************************************************/
    /* computes the deltaS for the bimolecular equilibrium: A+ B <=> TS */
    /*************************************************/

    public void computeDeltaS() {

        deltaS = path.getTs().getSTot() - (reactant1.getSTot() + reactant2.getSTot());

    } // end of the computeDeltaS method

    /***************************************************/
    /* computes the deltaS0 for the bimolecular equilibrium: A+ B <=> TS */
    /*************************************************/

    public void computeDeltaS0() {

        deltaS0 = path.getTs().getS0Tot() - (reactant1.getS0Tot() + reactant2.getS0Tot());

    } // end of the computeDeltaS0 method

    /***************************************************/
    /* computes the deltaH for the bimolecular equilibrium: A+ B <=> TS */
    /*************************************************/

    public void computeDeltaH() {

        deltaH = path.getTs().getHTot() - (reactant1.getHTot() + reactant2.getHTot());

    } // end of the computeDeltaH method

    /***************************************************/
    /* computes the deltaH0 for the bimolecular equilibrium: A+ B <=> TS */
    /*************************************************/

    public void computeDeltaH0() {

        deltaH0 = path.getTs().getH0Tot() - (reactant1.getH0Tot() + reactant2.getH0Tot());

    } // end of the computeDeltaH0 method

    /***************************************************/
    /* computes the deltaG for the bimolecular equilibrium: A+ B <=> TS */
    /*************************************************/

    public void computeDeltaG() {

        deltaG = path.getTs().getGTot() - (reactant1.getGTot() + reactant2.getGTot());

    } // end of the computeDeltaG method

    /***************************************************/
    /* computes the deltaG0 for the bimolecular equilibrium: A+ B <=> TS */
    /*************************************************/

    public void computeDeltaG0() {

        deltaG0 = path.getTs().getG0Tot() - (reactant1.getG0Tot() + reactant2.getG0Tot());

    } // end of the computeDeltaG0 method

    /**************************************************************************************************/
    /*
     * N O W, compute the deltaProperties MAX, not necessarily relative to the TS
     * but relative to the
     */
    /* pathPoint with G0 maximum ; needed by V T S T */
    /*************************************************************************************************/

    /*
     * X Max does not mean X is maximum, but X is computed for the pathpoint where
     * G0 is maximum !
     */

    /* ! *******************************************************************/
    /* computes the deltaUpMax for the bimolecular equilibrium: A+ B <=> TS */
    /********************************************************************/

    public void computeDeltaUpMax() {

        deltaUpMax = path.getG0Maximum().getUp() - (reactant1.getUp() + reactant2.getUp());

    } // end of the computeDeltaUpMax method

    /********************************************************************/
    /* ! computes the deltaZPEMax for the bimolecular equilibrium: A+ B <=> TS */
    /********************************************************************/

    public void computeDeltaZPEMax() {

        deltaZPEMax = path.getG0Maximum().getZPE() - (reactant1.getZPE() + reactant2.getZPE());

    } // end of the computeDeltaZPEMax method

    /***************************************************/
    /* ! computes the deltaSMAX for the bimolecular equilibrium: A+ B <=> TS */
    /*************************************************/

    public void computeDeltaSMax() {

        deltaSMax = path.getG0Maximum().getSTot() - (reactant1.getSTot() + reactant2.getSTot());

    } // end of the computeDeltaSMax method

    /***************************************************/
    /* ! computes the deltaS0MAX for the bimolecular equilibrium: A+ B <=> TS */
    /*************************************************/

    public void computeDeltaS0Max() {

        deltaS0Max = path.getG0Maximum().getS0Tot() - (reactant1.getS0Tot() + reactant2.getS0Tot());

    } // end of the computeDeltaS0Max method

    /***************************************************/
    /* ! computes the deltaHMax for the bimolecular equilibrium: A+ B <=> TS */
    /*************************************************/

    public void computeDeltaHMax() {

        deltaHMax = path.getG0Maximum().getHTot() - (reactant1.getHTot() + reactant2.getHTot());

    } // end of the computeDeltaHMax method

    /***************************************************/
    /* ! computes the deltaH0Max for the bimolecular equilibrium: A+ B <=> TS */
    /*************************************************/

    public void computeDeltaH0Max() {

        deltaH0Max = path.getG0Maximum().getH0Tot() - (reactant1.getH0Tot() + reactant2.getH0Tot());

    } // end of the computeDeltaH0Max method

    /***************************************************/
    /* ! computes the deltaGMax for the bimolecular equilibrium: A+ B <=> TS */
    /*************************************************/

    public void computeDeltaGMax() {

        deltaGMax = path.getG0Maximum().getGTot() - (reactant1.getGTot() + reactant2.getGTot());

    } // end of the computeDeltaGMax method

    /***************************************************/
    /* ! computes the deltaG0Max for the bimolecular equilibrium: A+ B <=> TS */
    /*************************************************/

    public void computeDeltaG0Max() {

        deltaG0Max = path.getG0Maximum().getG0Tot() - (reactant1.getG0Tot() + reactant2.getG0Tot());

    } // end of the computeDeltaG0Max method

    /****************************************************************/
    /* ! 2 methods (read, write) to save on file (or load from file) */
    /* the current object */
    /***********************************************************/

    /*********************************************/
    /* s a v e */
    /********************************************/

    public void save(ActionOnFileWrite write) throws IOException {
        super.save(write);
        write.oneString("CLASSNAME " + reactant1.getClass().getName());
        reactant1.save(write);
        write.oneString("CLASSNAME " + reactant2.getClass().getName());
        reactant2.save(write);
    } // end of the save method

    /*********************************************/
    /* l o a d */
    /********************************************/

    public void load(ActionOnFileRead read) throws IOException, IllegalDataException {
        super.load(read);
        read.oneString();
        reactant1 = new ReactingStatisticalSystem(read);
        read.oneString();
        reactant2 = new ReactingStatisticalSystem(read);

    } // end of the load method

    /*********************************************/
    /* g e t T i t l e */
    /********************************************/

    public String getTitle() {
        return "BIMOLECULAR REACTION";
    }

}// BimolecularReaction
