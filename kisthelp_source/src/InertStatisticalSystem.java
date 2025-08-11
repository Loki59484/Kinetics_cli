
import javax.swing.*;
import javax.swing.table.*;

import org.math.plot.Plot2DPanel;
import org.math.plot.PlotPanel;
import org.math.plot.plotObjects.BaseLabel;

import kisthep.file.*;
import kisthep.util.*;

import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

// this class has been created to be included in a Session, in contrast with StatisticalSystem
public class InertStatisticalSystem extends StatisticalSystem
		implements SessionComponent, ReadWritable, ActionListener {

	/* P R O P E R T I E S */
	// CvElec = 0 since only the electronic ground state is considered

	protected double CvVib; // Vibrational heat capacity at constant volume
	protected double CvTrans; // Vibrational heat capacity at constant volume
	protected double CvRot; // Vibrational heat capacity at constant volume
	protected double CvTot; // Vibrational heat capacity at constant volume
	protected double CpVib; // Vibrational heat capacity at constant pressure
	protected double CpTrans; // Vibrational heat capacity at constant pressure
	protected double CpRot; // Vibrational heat capacity at constant pressure
	protected double CpTot; // Vibrational heat capacity at constant pressure

	/*
	 * P R O P E R T I E S to Listen the FillInformationBox, not necessary to
	 * implement them in Save and Load methods
	 */
	/* since they will be rebuilt as necessary by the session */
	JTextField massTxtF, upTxtF, edTxtF, rotNbTxtF;
	JComboBox inerJCB, vibJCB;
	JComboBox titi;

	JButton dvJB, toto1;
	int selectedInertia = 0; // default value selectedInertia;
	int selectedVib = 0; // default value selected vib freq;

	/* C O N S T R U C T O R 1 */

	public InertStatisticalSystem(double T) throws CancelException, IllegalDataException, IOException {

		super(ChemicalSystem.anyPoint, T);
		computeCvCpTot();

	} // end of the CONSTRUCTOR

	/* C O N S T R U C T O R 1bis to take into account pressure */

	public InertStatisticalSystem(double T, double P) throws CancelException, IllegalDataException, IOException {

		super(ChemicalSystem.anyPoint, T, P);
		computeCvCpTot();

	} // end of the CONSTRUCTOR

	// Constructors to accept command line input
	public InertStatisticalSystem(double T, File clinpFile) throws CancelException, IllegalDataException, IOException {

		super(ChemicalSystem.anyPoint, T, clinpFile);
		computeCvCpTot();

	} // end of the CONSTRUCTOR

	public InertStatisticalSystem(double T, double P, File clinpFile)
			throws CancelException, IllegalDataException, IOException {

		super(ChemicalSystem.anyPoint, T, P, clinpFile);
		computeCvCpTot();

	} // end of the CONSTRUCTOR

	/* C O N S T R U C T O R 2 (to reload) from a Session file */

	public InertStatisticalSystem(ActionOnFileRead read) throws IOException, IllegalDataException {

		load(read);

		// super(read);
		// the call to the super load is unnecessary here, because there are no
		// properties

	} // end of the CONSTRUCTOR

	/* C O N S T R U C T O R 3 (to reload) from a Session file */

	public InertStatisticalSystem() {
	}

	/* M E T H O D S */
	/*-------------------------------------------------------------------*/
	/* C O M P U T E CvCp V I B */
	/*-------------------------------------------------------------------*/

	public void computeCvCpVib() throws IllegalDataException {
		/* heat capacity at constant volume or at constant pressure) */

		// local variables
		int arraySize;
		double currentFrequency; // in K !!
		double tampon;

		// variables for HRDS correction
		// taken from Richard B. McClurg, Richard C. Flagan and William A. Goddard, J.
		// Chem. Phys. 106 (16), 22 April 1997
		double hrdsCorrection = 0;
		double teta, r, tmp, i2, i1, i0;

		// initialisation of variable
		CvVib = 0.;
		CpVib = 0.;

		// compute
		if (atomic == false) {

			for (int iFreq = 0; iFreq < vibFreq.length; iFreq++) { // loop for scanning all the frequencies

				if (vibFreq[iFreq].getImagPart() == 0.0) { // We test the value of frequencie to avoid imaginary
															// frequencies

					currentFrequency = vibFreq[iFreq].getRealPart(); // we get the real part of complex (in K!!)

					tampon = Math.pow((currentFrequency / T), 2) * Math.exp(currentFrequency / T);
					CvVib = CvVib + tampon / (Math.pow(Math.exp(currentFrequency / T) - 1, 2)); // unit less at this
																								// stage

					// !! if current frequency mode is treated as a hindered rotor, a correction
					// must be included
					if (hrdsBarrier[iFreq] != Constants.highEnergy) {

						teta = T / currentFrequency; // unitless
						r = hrdsBarrier[iFreq] / (currentFrequency * Constants.kb);// unitless
						tmp = r / (2 * teta); // unitless
						if (tmp <= 3.75) {
							i0 = Maths.bessel1(0, tmp);
							i1 = Maths.bessel1(1, tmp);
							i2 = Maths.bessel1(2, tmp);
						} // use of bessel1 function
						else {
							i0 = Maths.bessel2(0, tmp);
							i1 = Maths.bessel2(1, tmp);
							i2 = Maths.bessel2(2, tmp);
						} // use of bessel2 function

						hrdsCorrection = -0.5 + 0.5 * tmp * tmp * (1.0 - 2 * (i1 * i1 / (i0 * i0)) + (i2 / i0)); // unitless
						CvVib = CvVib + hrdsCorrection; // unitless at this stage
					} // end of if (hrdsPeriodic[iFreq] != 0)

				} // end of if (vibFreq...
			} // end of for
			CvVib = CvVib * Constants.R;
			CpVib = CvVib;
		} // end if atomic != true

	}// End of computeCvCpVib() method

	/*-------------------------------------------------------------------*/
	/* C O M P U T E CvCp R O T */
	/*-------------------------------------------------------------------*/

	public void computeCvCpRot() {
		/* heat capacity at constant volume or at constant pressure) */

		CvRot = 0.0;
		CpRot = 0.0;

		if (atomic == true) { // atomic
			CvRot = 0.0;
			CpRot = 0.0;

		} // end if atomic

		if (atomic == false) {

			if (linear == true) {

				CvRot = Constants.R;
				CpRot = CvRot;

			} // End of linear

			if (linear == false) {
				CvRot = 1.5 * Constants.R;
				CpRot = CvRot;

			} // End of linear

		} // End of atomic

	}// End of computeCvCpRot() method

	/*-------------------------------------------------------------------*/
	/* C O M P U T E CvCp T R A N S */
	/*-------------------------------------------------------------------*/

	public void computeCvCpTrans() {
		/* heat capacity at constant volume or at constant pressure) */

		CvTrans = 1.5 * Constants.R;
		CpTrans = CvTrans + Constants.R;

	}// End of computeCvCpTrans() method

	/*-------------------------------------------------------------------*/
	/* C O M P U T E CvCp T O T */
	/*-------------------------------------------------------------------*/

	public void computeCvCpTot() throws IllegalDataException {

		computeCvCpTrans();

		computeCvCpVib();

		computeCvCpRot();

		// CvElec = 0;

		CvTot = CvTrans + CvVib + CvRot;
		CpTot = CpTrans + CpVib + CpRot;

	}// End of computeCvCpTot() method

	/*-------------------------------------------------------------------*/
	/* S E T T E M P E R A T U R E */
	/*-------------------------------------------------------------------*/

	public void setTemperature(double T) throws IllegalDataException {

		this.T = T;
		statistThermCompute();
		computeCvCpTot();

	}

	/* method that fill the table of results */
	public Vector getTableResults() throws runTimeException {

		Vector tableVector = new Vector();

		String[] columnNames = {
				"     ",
				"Translation",
				"Vibration",
				"Rotation",
				"Electronic",
				"Total",
				"Total (to hartree)",
				"Total to (k)cal" };

		Object[][] data = {
				{ "Q", Maths.format(getZTrans() / Constants.NA, "0.00E00"),
						Maths.format(getZVib(), "0.00E00"), Maths.format(getZRot(), "0.00E00"),
						Maths.format(getZElec(), "0.000"), Maths.format(getZTot() / Constants.NA, "0.00E00"), "", "" },
				{ "", "", "", "", "", "", "", "" },
				{ "Up (kJ/mol)", "", "", "", Maths.format(up * 1e-3, "0.00"), Maths.format(up * 1e-3, "0.00"),
						Maths.format(up / Constants.convertHartreeToJoule, "0.00000"),
						Maths.format(up * 1e-3 / Constants.convertCalToJoule, "0.00") },
				{ "ZPE (kJ/mol)", "", Maths.format(ZPE * 1.0E-3, "0.00"), "", "", Maths.format(ZPE * 1.0E-3, "0.00"),
						Maths.format(ZPE / Constants.convertHartreeToJoule, "0.00000"),
						Maths.format(ZPE * 1e-3 / Constants.convertCalToJoule, "0.00") },
				{ "H_0K (kJ/mol)", "", Maths.format(ZPE * 1.0E-3, "0.00"), "", Maths.format(up * 1e-3, "0.00"),
						Maths.format((up + ZPE) * 1e-3, "0.00"),
						Maths.format((up + ZPE) / Constants.convertHartreeToJoule, "0.00000"),
						Maths.format((up + ZPE) * 1e-3 / Constants.convertCalToJoule, "0.00") },
				{ "", "", "", "", "", "", "", "" },
				{ "U\u00B0 (kJ/mol)", Maths.format(getUTrans() * 1.0E-3, "0.00"),
						Maths.format((ZPE + getUVib()) * 1.0E-3, "0.00"), Maths.format(getURot() * 1.0E-3, "0.00"),
						Maths.format(up * 1.0E-3, "0.00"), Maths.format((ZPE + up + getUTot()) * 1.0E-3, "0.00"),
						Maths.format((ZPE + up + getUTot()) / Constants.convertHartreeToJoule, "0.00000"),
						Maths.format((ZPE + up + getUTot()) * 1e-3 / Constants.convertCalToJoule, "0.00") },
				{ "U  (kJ/mol)", Maths.format(getUTrans() * 1.0E-3, "0.00"),
						Maths.format((ZPE + getUVib()) * 1.0E-3, "0.00"), Maths.format(getURot() * 1.0E-3, "0.00"),
						Maths.format(up * 1.0E-3, "0.00"), Maths.format((ZPE + up + getUTot()) * 1.0E-3, "0.00"),
						Maths.format((ZPE + up + getUTot()) / Constants.convertHartreeToJoule, "0.00000"),
						Maths.format((ZPE + up + getUTot()) * 1e-3 / Constants.convertCalToJoule, "0.00") },
				{ "", "", "", "", "", "", "", "" },
				{ "H\u00B0 (kJ/mol)", Maths.format(getHTrans() * 1.0E-3, "0.00"),
						Maths.format((ZPE + getHVib()) * 1.0E-3, "0.00"), Maths.format(getHRot() * 1.0E-3, "0.00"),
						Maths.format(up * 1.0E-3, "0.00"), Maths.format((getHTot()) * 1.0E-3, "0.00"),
						Maths.format((getHTot()) / Constants.convertHartreeToJoule, "0.00000"),
						Maths.format((getHTot()) * 1e-3 / Constants.convertCalToJoule, "0.00") },
				{ "H  (kJ/mol)", Maths.format(getHTrans() * 1.0E-3, "0.00"),
						Maths.format((ZPE + getHVib()) * 1.0E-3, "0.00"), Maths.format(getHRot() * 1.0E-3, "0.00"),
						Maths.format(up * 1.0E-3, "0.00"), Maths.format((getHTot()) * 1.0E-3, "0.00"),
						Maths.format((getHTot()) / Constants.convertHartreeToJoule, "0.00000"),
						Maths.format((getHTot()) * 1e-3 / Constants.convertCalToJoule, "0.00") },
				{ "", "", "", "", "", "", "", "" },
				{ "S\u00B0 (J/mol/K)", Maths.format(getS0Trans(), "0.00"), Maths.format(getSVib(), "0.00"),
						Maths.format(getSRot(), "0.00"), Maths.format(getSElec(), "0.00"),
						Maths.format(getS0Tot(), "0.00"), "",
						Maths.format(getS0Tot() / Constants.convertCalToJoule, "0.00") },
				{ "S  (J/mol/K)", Maths.format(getSTrans(), "0.00"), Maths.format(getSVib(), "0.00"),
						Maths.format(getSRot(), "0.00"), Maths.format(getSElec(), "0.00"),
						Maths.format(getSTot(), "0.00"), "",
						Maths.format(getSTot() / Constants.convertCalToJoule, "0.00") },
				{ "", "", "", "", "", "", "", "" },
				{ "G\u00B0 (kJ/mol)", Maths.format(getG0Trans() * 1.0E-3, "0.00"),
						Maths.format(getGVib() * 1.0E-3, "0.00"), Maths.format(getGRot() * 1.0E-3, "0.00"),
						Maths.format(getGElec() * 1.0E-3, "0.00"), Maths.format(getG0Tot() * 1.0E-3, "0.00"),
						Maths.format(getG0Tot() / Constants.convertHartreeToJoule, "0.00000"),
						Maths.format(getG0Tot() * 1e-3 / Constants.convertCalToJoule, "0.00") },
				{ "G  (kJ/mol)", Maths.format(getGTrans() * 1.0E-3, "0.00"), Maths.format(getGVib() * 1.0E-3, "0.00"),
						Maths.format(getGRot() * 1.0E-3, "0.00"), Maths.format(getGElec() * 1.0E-3, "0.00"),
						Maths.format(getGTot() * 1.0E-3, "0.00"),
						Maths.format(getGTot() / Constants.convertHartreeToJoule, "0.00000"),
						Maths.format(getGTot() * 1e-3 / Constants.convertCalToJoule, "0.00") },
				{ "", "", "", "", "", "", "", "" },
				{ "Cv (J/mol/K)", Maths.format(CvTrans, "0.00"), Maths.format(CvVib, "0.00"),
						Maths.format(CvRot, "0.00"), "0", Maths.format(CvTot, "0.00"), "",
						Maths.format(CvTot / Constants.convertCalToJoule, "0.00") },
				{ "Cp (J/mol/K)", Maths.format(CpTrans, "0.00"), Maths.format(CpVib, "0.00"),
						Maths.format(CpRot, "0.00"), "0", Maths.format(CpTot, "0.00"), "",
						Maths.format(CpTot / Constants.convertCalToJoule, "0.00") },
		};

		JTable table = new JTable(data, columnNames);
		table.setDefaultRenderer(Object.class, new jTableRendererInert());
		table.setFont(new Font("Verdana", Font.PLAIN, 12));

		TableColumn column = null;
		column = table.getColumnModel().getColumn(0);
		column.setPreferredWidth(60);
		column = table.getColumnModel().getColumn(1);
		column.setPreferredWidth(60);
		column = table.getColumnModel().getColumn(2);
		column.setPreferredWidth(60);
		column = table.getColumnModel().getColumn(3);
		column.setPreferredWidth(60);

		column = table.getColumnModel().getColumn(4);
		column.setPreferredWidth(90);

		column = table.getColumnModel().getColumn(5);
		column.setPreferredWidth(90);

		table.setPreferredScrollableViewportSize(new Dimension(750, 320));

		// tableVector.add(table);
		tableVector.add(table);

		return tableVector;

	}

	/* results display BUILDING */
	public Vector getTextResults() throws runTimeException {

		TitledPane titledPane = new TitledPane();
		Vector vectorResult = new Vector();

		final int titleStyle = Font.BOLD;

		// ----------------- create a DisplayPanel
		JPanel displayPanel = new JPanel();

		// a title
		JTextArea titleArea = new JTextArea();
		titleArea.setBackground(new Color(255, 233, 103));
		titleArea.setEditable(false);
		int characterSize = 13;

		// get the results put in one (or more) table(s)
		final Vector table = getTableResults(); // table can contain one or more tables

		if (atomic) {
			titleArea.setText("Atomic system  at " + Maths.format(T, "000.00") + " K and "
					+ Session.getCurrentSession().getUnitSystem().convertToPressureUnit(P) + " "
					+ Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit());
		} else {
			titleArea.setText("Molecular system at " + Maths.format(T, "000.00") + " K and "
					+ Session.getCurrentSession().getUnitSystem().convertToPressureUnit(P) + " "
					+ Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit() + "(scaling Factor = "
					+ Session.getCurrentSession().getScalingFactor() + ")");
		}
		titleArea.setFont(new Font("SansSerif", titleStyle, characterSize));

		// the layout of the BoxresultsPane
		GridBagLayout gbl1 = new GridBagLayout();
		displayPanel.setLayout(gbl1);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		displayPanel.add(titleArea, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		displayPanel.add(new JScrollPane((Component) (table.get(0))), gbc);

		// wrap displayPanel and its title into a TitledPane
		titledPane = new TitledPane("Properties", new JScrollPane(displayPanel));

		// add TitlePane to vectorResult
		vectorResult.add(titledPane);

		// ----------------- add a new plot2DPanel with N(E)=f(E)
		Plot2DPanel plot2 = new Plot2DPanel();

		String titlePlot, labelAbscissa, labelOrdinate, system;
		if (atomic) {
			system = "Atom with 3 degrees of freedom";
		} else {

			if (linear) {
				system = "Molecule with " + (getVibFreedomDegrees() + 5) + " degrees of freedom";
			} else {
				system = "Molecule with " + (getVibFreedomDegrees() + 6) + " degrees of freedom";
			}

		} // end else

		titlePlot = "Fractional population Ni/N = f(E) ---- " + system;
		labelAbscissa = "E/kbT ";
		labelOrdinate = "Ni/N";

		// add a title
		BaseLabel title = new BaseLabel(titlePlot, Color.RED, 0.5, 1.15);
		title.setFont(new Font("Verdana", Font.BOLD, 20));
		plot2.addPlotable(title);

		// define the legend position
		plot2.addLegend("SOUTH");

		// define Labels
		String xLabel = labelAbscissa;
		String yLabel = labelOrdinate;
		((PlotPanel) (plot2)).setAxisLabels(xLabel, yLabel);
		double upperLimit;
		if (atomic) {
			upperLimit = 9 * Constants.kb * T;
		} else {
			upperLimit = 3 * getVibFreedomDegrees() * Constants.kb * T;
		}

		int N = 100;
		double step = (upperLimit - 0.0) / N; // step size in J/molec
		double currentEner = 0;
		double[] x = new double[N + 1];
		double[] y = new double[N + 1];

		for (int iEner = 1; iEner <= 100; iEner++) {

			currentEner = iEner * step;
			x[iEner] = currentEner / (Constants.kb * T);
			y[iEner] = getNETot(currentEner, T, P) * Math.exp(-currentEner / (Constants.kb * T)) * step
					/ (getZTot() / Constants.NA);

		} // end of for iener

		// add a line plot to the PlotPanel
		plot2.addLinePlot("Gas-phase thermal Maxwell-Boltzmann distribution", x, y);

		// wrap Plot2DPanel and its title into a TitledPane
		titledPane = new TitledPane("Ni/N = f(E)", plot2);

		// add TitlePane to vectorResult
		vectorResult.add(titledPane);

		return vectorResult;

	} // end of getTextResults method

	/*********************************/
	/* s a v e T x t R e s u l t s */
	/*******************************/
	// similar to getTextResults, except the output format is appropriate
	// to put results to text CSV file instead of screen

	public void saveTxtResults(ActionOnFileWrite writeResults) throws runTimeException {

		// prepare the title
		if (atomic)
			writeResults.oneString("Atomic system  at " + T + " K and "
					+ Session.getCurrentSession().getUnitSystem().convertToPressureUnit(P) + " "
					+ Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit());
		else {
			writeResults.oneString("Molecular system at " + T + " K and "
					+ Session.getCurrentSession().getUnitSystem().convertToPressureUnit(P) + " "
					+ Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit() + "(scaling Factor = "
					+ Session.getCurrentSession().getScalingFactor() + ")");
		}

		// get the results put in a table
		Vector tables = getTableResults();
		String line;
		writeResults.oneString("");

		// prepare the table headers
		writeResults.oneString(",Translation,Vibration,Rotation,Electronic,Total");
		JTable table = (JTable) (tables.get(0));

		for (int row = 0; row < table.getRowCount(); row++) {
			line = "";
			for (int col = 0; col < table.getColumnCount(); col++) {
				line = line + table.getValueAt(row, col) + ","; // CSV format
			}
			writeResults.oneString(line);
		}

	} // end of saveTxtResults method

	/***********************************************************/
	/* s a v e G r a p h i c s R e s u l t s for different T */
	/**********************************************************/

	/*
	 * D E P R E C A T E D
	 * 
	 * public void saveGraphicsResults(ActionOnFileWrite writeResults,
	 * TemperatureRange temperatureRange) {
	 * 
	 * 
	 * double tMin = temperatureRange.getTMin();
	 * double tMax = temperatureRange.getTMax();
	 * double tStep = temperatureRange.getTStep();
	 * 
	 * 
	 * // local variables
	 * int thermoChemistryArraySize = (int) ((tMax - tMin) / tStep) + 1 ;
	 * 
	 * double[] hTotArray = new double[thermoChemistryArraySize];
	 * double[] sTotArray = new double[thermoChemistryArraySize];
	 * double[] gTotArray = new double[thermoChemistryArraySize];
	 * 
	 * 
	 * double currentTemperature = tMin - tStep ;
	 * 
	 * 
	 * 
	 * writeResults.oneString("P = "+
	 * Session.getCurrentSession().getUnitSystem().convertToPressureUnit(P)+ " "
	 * +Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit());
	 * writeResults.
	 * oneString("T(K)		H(kJ.mol-1)		S(J.mol-1.K-1)		G(kJ.mol-1)");
	 * 
	 * 
	 * // fills the free energy, enthalpy, entropy arrays
	 * 
	 * for (int jElement = 0 ; jElement < thermoChemistryArraySize ; jElement =
	 * jElement + 1){
	 * 
	 * currentTemperature = currentTemperature + tStep ;
	 * setTemperature(currentTemperature) ;
	 * 
	 * writeResults.oneString(currentTemperature+"		" +'\t'+
	 * Maths.format(getHTot() * 1e-3,"0.00")+
	 * "		" +
	 * Maths.format(sTot,"0.00") +
	 * "			" +
	 * Maths.format(gTot * 1e-3,"0.00") );
	 * 
	 * }// end of for (int jElement = 0 ; ...
	 * 
	 * 
	 * } // end of saveGraphicsResults method
	 * 
	 */

	/***********************************************************/
	/* s a v e G r a p h i c s R e s u l t s for different P */
	/**********************************************************/

	/*
	 * D E P R E C A T E D
	 * 
	 * public void saveGraphicsResults(ActionOnFileWrite writeResults, PressureRange
	 * pressureRange) {
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
	 * double[] hTotArray = new double[thermoChemistryArraySize];
	 * double[] sTotArray = new double[thermoChemistryArraySize];
	 * double[] gTotArray = new double[thermoChemistryArraySize];
	 * 
	 * 
	 * double currentPressure = pMin - pStep ;
	 * 
	 * 
	 * writeResults.oneString("T = "+ T+ "K");
	 * writeResults.oneString("P("+
	 * Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit()
	 * +")		H(kJ.mol-1)		S(J.mol-1.K-1)		G(kJ.mol-1)");
	 * 
	 * 
	 * // fills the free energy, enthalpy, entropy arrays
	 * 
	 * for (int jElement = 0 ; jElement < thermoChemistryArraySize ; jElement =
	 * jElement + 1){
	 * 
	 * currentPressure = currentPressure + pStep ;
	 * setPressure(currentPressure) ;
	 * 
	 * writeResults.oneString(Session.getCurrentSession().getUnitSystem().
	 * convertToPressureUnit(currentPressure)+"		" +'\t'+
	 * Maths.format(getHTot() * 1e-3,"0.00")+
	 * "		" +
	 * Maths.format(sTot,"0.00") +
	 * "			" +
	 * Maths.format(gTot * 1e-3,"0.00") );
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
	/* g e t G r a p h i c s R e s u l t s */
	/*******************************************************************************************************/

	public Vector getGraphicsResults(TemperatureRange temperatureRange) throws IllegalDataException {

		double tMin = temperatureRange.getTMin(); // in K
		double tMax = temperatureRange.getTMax(); // in K
		double tStep = temperatureRange.getTStep(); // in K
		Vector graphicVector = new Vector(); // the result is represented by a vector containing all the information

		final int x = 0;
		final int y = 1;

		int thermoChemistryArraySize = (int) ((tMax - tMin) / tStep) + 1;

		double[][] hTotArray = new double[2][thermoChemistryArraySize];
		double[][] sTotArray = new double[2][thermoChemistryArraySize];
		double[][] gTotArray = new double[2][thermoChemistryArraySize];
		double[][] zTransArray = new double[2][thermoChemistryArraySize];
		double[][] zVibArray = new double[2][thermoChemistryArraySize];
		double[][] zRotArray = new double[2][thermoChemistryArraySize];
		double[][] zTotArray = new double[2][thermoChemistryArraySize];
		double[][] cpTotArray = new double[2][thermoChemistryArraySize];
		double[][] cpTotOverTArray = new double[2][thermoChemistryArraySize];

		double temperature = tMin - tStep; // in K
		double tempInUserUnit;

		// fills the arrays for, free energy, enthalpy, entropy

		for (int jElement = 0; jElement < thermoChemistryArraySize; jElement = jElement + 1) {

			temperature = temperature + tStep;
			setTemperature(temperature); // temperature in K, all properties are refreshed

			// don't forget to convert temperature to be displayed to the User unit !
			tempInUserUnit = Session.getCurrentSession().getUnitSystem().convertToTemperatureUnit(temperature);
			zTransArray[x][jElement] = tempInUserUnit;
			zTransArray[y][jElement] = getZTrans();

			zVibArray[x][jElement] = tempInUserUnit;
			zVibArray[y][jElement] = getZVib();

			zRotArray[x][jElement] = tempInUserUnit;
			zRotArray[y][jElement] = getZRot();

			zTotArray[x][jElement] = tempInUserUnit;
			zTotArray[y][jElement] = getZTot();

			cpTotArray[x][jElement] = tempInUserUnit;
			cpTotArray[y][jElement] = getCpTot();

			cpTotOverTArray[x][jElement] = temperature; // only in kelvin here !!
			cpTotOverTArray[y][jElement] = getCpTot() / getT(); // T=0 is not allowed in Kisthep

			hTotArray[x][jElement] = tempInUserUnit;
			hTotArray[y][jElement] = getHTot() / 1000.0; // in kJ/mol

			sTotArray[x][jElement] = tempInUserUnit;
			sTotArray[y][jElement] = sTot; // in J/mol/K

			gTotArray[x][jElement] = tempInUserUnit;
			gTotArray[y][jElement] = gTot / 1000.0; // in kJ/mol

		}

		// buils all graphics
		DataToPlot dataToplot;
		String title, labelAbscissa, labelOrdinate;
		Vector v, w;

		// get the symbol of the current user temperature unit
		String tempSymbol = Session.getCurrentSession().getUnitSystem().getTemperatureSymbol();

		// detect the kind of system
		String system;
		if (atomic) {
			system = "Atom";
		} else {
			system = "Molecule";
		}

		// build graphics 1
		title = "G = f ( T ) ---- " + system;
		labelAbscissa = "Temperature (" + tempSymbol + ")";
		labelOrdinate = "Gibbs Free Energy (kJ/mol)";

		v = new Vector();
		v.add("G");
		w = new Vector();
		w.add(gTotArray);

		dataToplot = new DataToPlot("G", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "ZTrans = f ( T ) ---- " + system;
		labelAbscissa = "Temperature (" + tempSymbol + ")";
		labelOrdinate = "ZTrans";

		v = new Vector();
		v.add("ZTrans");
		w = new Vector();
		w.add(zTransArray);

		dataToplot = new DataToPlot("ZTrans", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "ZRot = f ( T ) ---- " + system;
		labelAbscissa = "Temperature (" + tempSymbol + ")";
		labelOrdinate = "ZRot";

		v = new Vector();
		v.add("ZRot");
		w = new Vector();
		w.add(zRotArray);

		dataToplot = new DataToPlot("ZRot", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "ZVib = f ( T ) ---- " + system;
		labelAbscissa = "Temperature (" + tempSymbol + ")";
		labelOrdinate = "ZVib";

		v = new Vector();
		v.add("ZVib");
		w = new Vector();
		w.add(zVibArray);

		dataToplot = new DataToPlot("ZVib", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "ZTot = f ( T ) ---- " + system;
		labelAbscissa = "Temperature (" + tempSymbol + ")";
		labelOrdinate = "ZTot";

		v = new Vector();
		v.add("ZTot");
		w = new Vector();
		w.add(zTotArray);

		dataToplot = new DataToPlot("ZTot", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "Cp = f ( T ) ---- " + system;
		labelAbscissa = "Temperature (" + tempSymbol + ")";
		labelOrdinate = "Cp (J/mol/K)";

		v = new Vector();
		v.add("Cp");
		w = new Vector();
		w.add(cpTotArray);

		dataToplot = new DataToPlot("Cp", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "Cp/T = f ( T ) ---- " + system;
		labelAbscissa = "Temperature (K)";// exceptionnally only in K here
		labelOrdinate = "Cp/T (J/mol/K/K)";

		v = new Vector();
		v.add("Cp/T");
		w = new Vector();
		w.add(cpTotOverTArray);

		dataToplot = new DataToPlot("Cp/T", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "S = f ( T ) ---- " + system;
		labelAbscissa = "Temperature (" + tempSymbol + ")";
		labelOrdinate = "Entropie (J/mol/K)"; // we keep J/mol/K unit for S (calculates as it) even though it can be
												// plotted versus temperature in celsius

		v = new Vector();
		v.add("S");
		w = new Vector();
		w.add(sTotArray);

		dataToplot = new DataToPlot("S", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.0");
		graphicVector.add(dataToplot);

		// build graphics
		title = "Htot = f ( T ) ---- " + system;
		labelAbscissa = "Temperature (" + tempSymbol + ")";
		labelOrdinate = "Htot (kJ/mol)";

		v = new Vector();
		v.add("H");
		w = new Vector();
		w.add(hTotArray);

		dataToplot = new DataToPlot("H", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		return graphicVector;

	} // end of getResultGraphics

	/*******************************************************************************************************/
	/* g e t 3 D G r a p h i c s R e s u l t s */
	/*******************************************************************************************************/

	public Vector get3DGraphicsResults(TemperatureRange temperatureRange, PressureRange pressureRange)
			throws IllegalDataException {

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

			String message = "Error: too much points on the generated surface (limit = 25000 points)"
					+ Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		}

		double[] P = new double[thermoChemistryPArraySize];
		double[] T = new double[thermoChemistryTArraySize];

		double[][] hTotArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
		double[][] sTotArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
		double[][] gTotArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
		double[][] zTransArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
		double[][] zVibArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
		double[][] zRotArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
		double[][] zTotArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
		double[][] cpTotArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];

		double pressure = pMin - pStep; // in Pa
		double pressureInUserUnit;

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
			temperature = temperature + tStep;
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

				zTransArray[kElement][jElement] = getZTrans();

				zVibArray[kElement][jElement] = getZVib();

				zRotArray[kElement][jElement] = getZRot();

				zTotArray[kElement][jElement] = getZTot();

				cpTotArray[kElement][jElement] = getCpTot();

				hTotArray[kElement][jElement] = getHTot() / 1000.0; // in kJ/mol

				sTotArray[kElement][jElement] = sTot; // in J/mol/K

				gTotArray[kElement][jElement] = gTot / 1000.0; // in kJ/mol

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

		// detect the kind of system
		String system;
		if (atomic) {
			system = "Atom";
		} else {
			system = "Molecule";
		}

		// common features
		labelX = "Pressure (" + pressSymbol + ")";
		labelY = "Temperature (" + tempSymbol + ")";

		// build graphics 1
		title = "G = f (P,T) ---- " + system;
		labelZ = "Gibbs Free Energy (kJ/mol)";

		v = "G";

		dataToplot = new DataToPlot3D("G", title, v, labelX, labelY, labelZ, P, T, gTotArray, "0.00E00", "0.0",
				"0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "ZTrans = f (P,T) ---- " + system;
		labelZ = "ZTrans";

		v = "ZTrans";
		dataToplot = new DataToPlot3D("ZTrans", title, v, labelX, labelY, labelZ, P, T, zTransArray, "0.00E00", "0.0",
				"0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "ZRot = f (P,T) ----(*) " + system;
		labelZ = "ZRot";

		v = "ZRot (pressure independent)";
		dataToplot = new DataToPlot3D("ZRot", title, v, labelX, labelY, labelZ, P, T, zRotArray, "0.00E00", "0.0",
				"0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "ZVib = f (P,T) ----(*) " + system;
		labelZ = "ZVib";

		v = "ZVib (pressure independent)";
		dataToplot = new DataToPlot3D("ZVib", title, v, labelX, labelY, labelZ, P, T, zVibArray, "0.00E00", "0.0",
				"0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "ZTot = f (P,T) ---- " + system;
		labelZ = "ZTot";

		v = "ZTot";
		dataToplot = new DataToPlot3D("ZTot", title, v, labelX, labelY, labelZ, P, T, zTotArray, "0.00E00", "0.0",
				"0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "Cp = f (P,T) ----(*) " + system;
		labelZ = "Cp (J/mol/K)";

		v = "Cp (pressure independent)";
		dataToplot = new DataToPlot3D("Cp", title, v, labelX, labelY, labelZ, P, T, cpTotArray, "0.00E00", "0.0",
				"0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "S = f (P,T) ---- " + system;
		labelZ = "Entropie (J/mol/K)"; // we keep J/mol/K unit for S (calculates as it) even though it can be plotted
										// versus temperature in celsius

		v = "S";
		dataToplot = new DataToPlot3D("S", title, v, labelX, labelY, labelZ, P, T, sTotArray, "0.00E00", "0.0", "0.0");
		graphicVector.add(dataToplot);

		// build graphics
		title = "Htot = f (P,T) ----(*) " + system;
		labelZ = "Htot (kJ/mol)";

		v = "H (pressure independent)";
		dataToplot = new DataToPlot3D("H", title, v, labelX, labelY, labelZ, P, T, hTotArray, "0.00E00", "0.0",
				"0.00E00");
		graphicVector.add(dataToplot);

		return graphicVector;

	} // end of get3DResultGraphics

	/*******************************************************************************************************/
	/* g e t G r a p h i c s R e s u l t s */
	/*******************************************************************************************************/
	public Vector getGraphicsResults(PressureRange pressureRange) throws IllegalDataException {

		double pMin = pressureRange.getPMin(); // in Pa
		double pMax = pressureRange.getPMax(); // in Pa
		double pStep = pressureRange.getPStep(); // in Pa
		Vector graphicVector = new Vector(); // the result is represented by a vector containing all the information
												// the convention is the following
												// it is a vector of DataToPlot objects, each containing:
												// a) a JButton ! with the appropriate label ! to be put in the
												// ForGraphicsButtonsBox
												// b) title of the graph
												// c) abscissa label
												// d) ordinate label
												// e) 2 dimensions array [X][Y]
												// f) 2 Math.Formats

		final int x = 0;
		final int y = 1;

		int thermoChemistryArraySize = (int) ((pMax - pMin) / pStep) + 1;

		double[][] hTotArray = new double[2][thermoChemistryArraySize];
		double[][] sTotArray = new double[2][thermoChemistryArraySize];
		double[][] gTotArray = new double[2][thermoChemistryArraySize];
		double[][] zTransArray = new double[2][thermoChemistryArraySize];
		double[][] zVibArray = new double[2][thermoChemistryArraySize];
		double[][] zRotArray = new double[2][thermoChemistryArraySize];
		double[][] zTotArray = new double[2][thermoChemistryArraySize];
		double[][] cpTotArray = new double[2][thermoChemistryArraySize];

		double pressure = pMin - pStep;
		double pressureInUserUnit;

		// fills the arrays for, free energy, enthalpy, entropy

		for (int jElement = 0; jElement < thermoChemistryArraySize; jElement = jElement + 1) {

			pressure = pressure + pStep;
			setPressure(pressure);// pressure in Pa, all properties are refreshed

			// don't forget to convert pressure to be displayed to the pressure User unit !
			pressureInUserUnit = Session.getCurrentSession().getUnitSystem().convertToPressureUnit(pressure);

			zTransArray[x][jElement] = pressureInUserUnit;
			zTransArray[y][jElement] = getZTrans();

			zVibArray[x][jElement] = pressureInUserUnit;
			zVibArray[y][jElement] = getZVib();

			zRotArray[x][jElement] = pressureInUserUnit;
			zRotArray[y][jElement] = getZRot();

			zTotArray[x][jElement] = pressureInUserUnit;
			zTotArray[y][jElement] = getZTot();

			cpTotArray[x][jElement] = pressureInUserUnit;
			cpTotArray[y][jElement] = getCpTot(); // in J/mol/K

			hTotArray[x][jElement] = pressureInUserUnit;
			hTotArray[y][jElement] = getHTot() / 1000.0; // in kJ/mol

			sTotArray[x][jElement] = pressureInUserUnit;
			sTotArray[y][jElement] = sTot; // in J/mol/K

			gTotArray[x][jElement] = pressureInUserUnit;
			gTotArray[y][jElement] = gTot / 1000.0; // in kJ/mol

		}

		// buils all graphics
		DataToPlot dataToplot;
		String title, labelAbscissa, labelOrdinate;
		Vector v, w;

		// detect the kind of system
		String system;
		if (atomic) {
			system = "Atom";
		} else {
			system = "Molecule";
		}

		// get the symbol of the current user pressure unit
		String pressSymbol = Session.getCurrentSession().getUnitSystem().getPressureSymbol();

		// build graphics 1
		title = "G = f ( P ) ---- " + system;
		labelAbscissa = "Pressure (" + pressSymbol + ")";
		labelOrdinate = "Gibbs Free Energy (kJ/mol)";

		v = new Vector();
		v.add("G");
		w = new Vector();
		w.add(gTotArray);
		dataToplot = new DataToPlot("G", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "ZTrans = f ( P ) ---- " + system;
		labelAbscissa = "Pressure (" + pressSymbol + ")";
		labelOrdinate = "ZTrans";

		v = new Vector();
		v.add("ZTrans");
		w = new Vector();
		w.add(zTransArray);

		dataToplot = new DataToPlot("ZTrans", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "ZRot = f ( P ) ---- " + system;
		labelAbscissa = "Pressure (" + pressSymbol + ")";
		labelOrdinate = "ZRot";

		v = new Vector();
		v.add("ZRot");
		w = new Vector();
		w.add(zRotArray);

		dataToplot = new DataToPlot("ZRot", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "ZVib = f ( P ) ---- " + system;
		labelAbscissa = "Pressure (" + pressSymbol + ")";
		labelOrdinate = "ZVib";

		v = new Vector();
		v.add("ZVib");
		w = new Vector();
		w.add(zVibArray);

		dataToplot = new DataToPlot("ZVib", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "ZTot = f ( P ) ---- " + system;
		labelAbscissa = "Pressure (" + pressSymbol + ")";
		labelOrdinate = "ZTot";

		v = new Vector();
		v.add("ZTot");
		w = new Vector();
		w.add(zTotArray);

		dataToplot = new DataToPlot("ZTot", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "Cp = f ( P ) ---- " + system;
		labelAbscissa = "Pressure (" + pressSymbol + ")";
		labelOrdinate = "Cp (J/mol/K)";

		v = new Vector();
		v.add("Cp");
		w = new Vector();
		w.add(cpTotArray);

		dataToplot = new DataToPlot("Cp", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		// build graphics
		title = "S = f ( P ) ---- " + system;
		labelAbscissa = "Pressure (" + pressSymbol + ")";
		labelOrdinate = "Entropie (J/mol/K)";

		v = new Vector();
		v.add("S");
		w = new Vector();
		w.add(sTotArray);

		dataToplot = new DataToPlot("S", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.0");
		graphicVector.add(dataToplot);

		// build graphics
		title = "Htot = f ( P ) ---- " + system;
		labelAbscissa = "Pressure (" + pressSymbol + ")";
		labelOrdinate = "Htot (kJ/mol)";

		v = new Vector();
		v.add("H");
		w = new Vector();
		w.add(hTotArray);

		dataToplot = new DataToPlot("H", title, v, labelAbscissa, labelOrdinate, w, "0.0", "0.00E00");
		graphicVector.add(dataToplot);

		return graphicVector;

	} // end of getResultGraphics

	/******************************************************/
	/* d i s p l a y I n f o r m a t i o n P a n e l */
	/*****************************************************/

	/*
	 * (this method is called each time an new calculation is carried
	 * out, thus each time a new object is added to current sessionContent).
	 */

	public void fillInformationBox() throws runTimeException {

		String stringBuffer;
		JPanel wrapper = new JPanel();
		wrapper.setBackground(new Color(255, 133, 103));
		wrapper.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		// get the Box panel
		Box informationBox = Interface.getCalculationFeatureBox();
		Dimension boxDimension = informationBox.getSize();

		// fill the panel (title, filename associated with calculation ...)

		/* ***** element***** */
		JLabel titleLabel = new JLabel(getTitle());
		gbc.gridx = 0;
		gbc.gridy = 0;

		gbc.gridwidth = 2;
		gbc.gridheight = 1;

		gbc.weighty = 0;

		gbc.anchor = GridBagConstraints.CENTER;

		gbc.insets = new Insets(10, 15, 0, 0);

		wrapper.add(titleLabel, gbc);

		/* ***** element FILENAME***** */
		StringVector nameList = Session.getCurrentSession().getFilenameUsed();
		JLabel filename = new JLabel((String) (nameList.firstElement()));
		gbc.gridx = 0;
		gbc.gridy = 1;

		gbc.gridwidth = 2;
		gbc.gridheight = 1;

		gbc.weighty = 0;

		gbc.anchor = GridBagConstraints.CENTER;

		gbc.insets = new Insets(10, 15, 0, 0);

		wrapper.add(filename, gbc);

		/* ***** element NBATOMS***** */
		if (!atomic) {
			stringBuffer = " atoms";
		} else {
			stringBuffer = " atom";
		}
		JLabel atomLab = new JLabel((int) getAtomNb() + stringBuffer);

		gbc.gridx = 0;
		gbc.gridy = 2;

		gbc.gridwidth = 2;
		gbc.gridheight = 1;

		gbc.weighty = 0.0;

		gbc.anchor = GridBagConstraints.CENTER;

		gbc.insets = new Insets(10, 15, 0, 0);

		wrapper.add(atomLab, gbc);

		/* ***** element MASS ***** */

		JLabel massLab = new JLabel("Mass(amu)");

		gbc.gridx = 0;
		gbc.gridy = 3;

		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		gbc.weighty = 0.03;

		gbc.anchor = GridBagConstraints.CENTER;

		gbc.insets = new Insets(10, 15, 0, 0);

		wrapper.add(massLab, gbc);

		/* ***** TxtF MASS ***** */

		massTxtF = new JTextField("", 5);
		massTxtF.setText(Maths.format(getMass(), "0.00"));
		massTxtF.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = 3;

		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		gbc.weighty = 0.03;

		gbc.anchor = GridBagConstraints.CENTER;

		gbc.insets = new Insets(10, 15, 0, 0);

		wrapper.add(massTxtF, gbc);

		/* ***** element UP ***** */
		JLabel upLab = new JLabel("PE(au)");

		gbc.gridx = 0;
		gbc.gridy = 4;

		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		gbc.weighty = 0.03;
		gbc.anchor = GridBagConstraints.CENTER;

		gbc.insets = new Insets(10, 15, 0, 0);

		wrapper.add(upLab, gbc);

		/* ***** element TxtF UP ***** */
		upTxtF = new JTextField("", 7);
		upTxtF.setText(Maths.format(getUp() / Constants.convertHartreeToJoule, "0.00000"));
		upTxtF.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = 4;

		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		gbc.weighty = 0.03;
		gbc.anchor = GridBagConstraints.CENTER;

		gbc.insets = new Insets(10, 15, 0, 0);

		wrapper.add(upTxtF, gbc);

		/* ***** ELEC DEGEN***** */
		JLabel edLab = new JLabel("Elec. deg.");

		gbc.gridx = 0;
		gbc.gridy = 5;

		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weighty = 0.03;
		gbc.anchor = GridBagConstraints.CENTER;

		gbc.insets = new Insets(10, 15, 0, 0);

		wrapper.add(edLab, gbc);

		/* ***** element TxtF ElecDegener ***** */
		edTxtF = new JTextField("", 2);
		edTxtF.setText(Maths.format(getElecDegener(), "0"));
		edTxtF.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = 5;

		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		gbc.weighty = 0.03;
		gbc.anchor = GridBagConstraints.CENTER;

		gbc.insets = new Insets(10, 15, 0, 0);

		wrapper.add(edTxtF, gbc);

		if (!atomic) {

			/* ***** R O T A T I O N A L S Y M. N U M B E R ***** */

			JLabel rotNbLab = new JLabel("Rot.Sym. Numb.");

			gbc.gridx = 0;
			gbc.gridy = 6;

			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weighty = 0.03;
			gbc.anchor = GridBagConstraints.CENTER;

			gbc.insets = new Insets(10, 15, 0, 0);

			wrapper.add(rotNbLab, gbc);

			/* ***** element TxtF ElecDegener ***** */
			rotNbTxtF = new JTextField("", 2);
			rotNbTxtF.setText(Maths.format(getSymNumb(), "0"));
			rotNbTxtF.addActionListener(this);

			gbc.gridx = 1;
			gbc.gridy = 6;

			gbc.gridwidth = 1;
			gbc.gridheight = 1;

			gbc.weighty = 0.03;
			gbc.anchor = GridBagConstraints.CENTER;

			gbc.insets = new Insets(10, 15, 0, 0);

			wrapper.add(rotNbTxtF, gbc);

			/* ***** INERTIA MOMENTS***** */
			String[] inerList = new String[3];

			JTextArea linearArea = new JTextArea();
			linearArea.setEditable(false);
			linearArea.setBackground(new Color(255, 133, 103));

			if (!linear) {
				stringBuffer = "          not linear \n 3 inertia moments (au)";
				inerList[0] = new String(Maths.format(inertia[0], "0.00"));
				inerList[1] = new String(Maths.format(inertia[1], "0.00"));
				inerList[2] = new String(Maths.format(inertia[2], "0.00"));
			} else {
				stringBuffer = "            linear \n 1 inertia moment (au)";
				inerList[0] = new String(Maths.format(inertia[0], "0.00"));
			}
			linearArea.setText(stringBuffer);

			gbc.gridx = 0;
			gbc.gridy = 7;

			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridheight = 1;
			gbc.weighty = 0.03;
			gbc.anchor = GridBagConstraints.CENTER;

			gbc.insets = new Insets(10, 15, 0, 0);

			wrapper.add(linearArea, gbc);

			/* ***** I N E R T I A T X T F I E L D ***** */

			inerJCB = new JComboBox();
			inerJCB.setEditable(true);
			DefaultComboBoxModel model1 = new DefaultComboBoxModel(); //// use of this special model solves the problem
																		//// of
			// having duplicate string inertia value in the JComboBox
			// that causes the index of first element of the list with the selected value to
			// be returned instead
			// of the correct index : getSelectedIndex() always returns the index of the
			// first duplicate

			JComboElement elem;
			for (int i = 0; i < inerList.length; i++) {
				elem = new JComboElement(inerList[i], i);
				model1.addElement(elem);
			}

			inerJCB.setModel(model1);
			inerJCB.addActionListener(this);

			gbc.gridx = 0;
			gbc.gridy = 8;

			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridheight = 1;
			gbc.weighty = 0.03;
			gbc.anchor = GridBagConstraints.CENTER;

			gbc.insets = new Insets(10, 15, 0, 0);

			wrapper.add(inerJCB, gbc);

		} // end of atomic
		/* **************** */

		/* V I B R A T I O N A L P A R T */

		if (!atomic) {
			String[] vibList = (String[]) getVibRealFreqInCM_1().get(0);
			/* ***** VIB ***** */
			JTextArea titleArea = new JTextArea();
			titleArea.setEditable(false);
			titleArea.setBackground(new Color(255, 133, 103));
			String text = "   " + getNbVibReal() + " real Vib. Freq. (cm-1)";
			if (getNbHinderedRotor() != 0) {
				text = text + "\n( of which " + getNbHinderedRotor() + " hindered rotors)";
			}
			if (getNbVibImg() != 0) {

				JOptionPane.showMessageDialog(Interface.getKisthepInterface(),
						getNbVibImg() + " imaginary vibrational freq. excluded", Constants.kisthepMessage,
						JOptionPane.WARNING_MESSAGE);
				text = text + "\n(" + getNbVibImg() + " imaginary freq excluded)";
			} // end of if (getNbVibImg()!=0)
			titleArea.setText(text);

			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 9;

			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridheight = 1;
			gbc.weighty = 0.03;
			gbc.anchor = GridBagConstraints.CENTER;

			gbc.insets = new Insets(10, 15, 0, 0);

			// wrapper.add(vibLab, gbc);
			wrapper.add(titleArea, gbc);

			/* ***** VIB TXTF ***** */
			vibJCB = new JComboBox();
			vibJCB.setEditable(true);

			DefaultComboBoxModel model2 = new DefaultComboBoxModel(); //// use of this special model solves the problem
																		//// of
			// having duplicate string inertia value in the JComboBox
			// that causes the index of first element of the list with the selected value to
			// be returned instead
			// of the correct index : getSelectedIndex() always returns the index of the
			// first duplicate
			JComboElement elem;
			for (int i = 0; i < vibList.length; i++) {
				elem = new JComboElement(vibList[i], i);
				model2.addElement(elem);
			}

			vibJCB.setModel(model2);
			vibJCB.addActionListener(this);

			gbc.gridx = 0;
			gbc.gridy = 10;

			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridheight = 1;
			gbc.weighty = 0.03;
			gbc.anchor = GridBagConstraints.CENTER;

			gbc.insets = new Insets(0, 15, 0, 0);

			wrapper.add(vibJCB, gbc);

		} // end of atomic
		/*-----------------------------------------------*/

		/* ***** D E F A U L T V A L U E S ***** */

		dvJB = new JButton("default values");
		dvJB.addActionListener(this);

		gbc.gridx = 0;
		gbc.gridy = 11;

		gbc.gridwidth = 2;
		gbc.gridheight = 1;

		gbc.weighty = 0.4;
		gbc.anchor = GridBagConstraints.CENTER;

		gbc.insets = new Insets(10, 15, 0, 0);

		wrapper.add(dvJB, gbc);

		/* ***** F I L L IN F O R M A T I O N B O X ***** */
		// if informationBox already filled => erase old wrapper and replace it after
		if (informationBox != null) {
			informationBox.removeAll();
		}
		informationBox.add(wrapper);

	} // end of fillInformationBox method

	/*********************************************/
	/* g e t C p */
	/********************************************/

	public double getCpTot() {

		return CpTot;

	} // end of getCpTot

	/*********************************************/
	/* g e t T i t l e */
	/********************************************/

	public String getTitle() {

		if (atomic) {
			return "ATOM";
		} else {
			return "MOLECULE";
		}

	} // end of getTitle

	/*********************************************/
	/*
	 * s a v e (involved when saving a session)
	 * /
	 ********************************************/
	public void save(ActionOnFileWrite write) throws IOException {

		super.save(write);
		write.oneString("CvTrans :");
		write.oneDouble(CvTrans);
		write.oneString("CvVib :");
		write.oneDouble(CvVib);
		write.oneString("CvRot :");
		write.oneDouble(CvRot);
		write.oneString("CvTot :");
		write.oneDouble(CvTot);
		write.oneString("CpTrans :");
		write.oneDouble(CpTrans);
		write.oneString("CpVib :");
		write.oneDouble(CpVib);
		write.oneString("CpRot :");
		write.oneDouble(CpRot);
		write.oneString("CpTot :");
		write.oneDouble(CpTot);

	}// end of save(..)

	/*********************************************/
	/* l o a d */
	/********************************************/

	public void load(ActionOnFileRead read) throws IOException, IllegalDataException {
		super.load(read);
		read.oneString();
		CvTrans = read.oneDouble();
		read.oneString();
		CvVib = read.oneDouble();
		read.oneString();
		CvRot = read.oneDouble();
		read.oneString();
		CvTot = read.oneDouble();
		read.oneString();
		CpTrans = read.oneDouble();
		read.oneString();
		CpVib = read.oneDouble();
		read.oneString();
		CpRot = read.oneDouble();
		read.oneString();
		CpTot = read.oneDouble();

	}// end of load(..)

	/***********************************/
	/* a c t i o n P e r f o r m e d */
	/***********************************/

	public void actionPerformed(ActionEvent ev) {
		Object source = ev.getSource();

		/* -- M A S S -- */

		if (source == massTxtF) {
			double newMass;
			try {
				newMass = Double.parseDouble(massTxtF.getText()); // get back the txt entered by user
				setMass(newMass);
				setTemperature(T);// simply refresh a current temperature of this object
				// display a pane containing all the results
				Session.getCurrentSession().displayResults();

			} catch (IllegalDataException err) {
			} catch (runTimeException error) {
			} catch (OutOfRangeException error) {
			} catch (NumberFormatException error) {
				String message = "Warning: please supply a float number" + Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);
			}

		} // end of if massTxtF

		/* -- U P -- */

		if (source == upTxtF) {
			double newUp;

			try {
				newUp = Double.parseDouble(upTxtF.getText()); // get back the txt entered by user in ua (to convert to
																// J/mol)
				setUp(newUp * Constants.convertHartreeToJoule);
				setTemperature(T);// simply refresh a current temperature of this object
				// display a pane containing all the results
				Session.getCurrentSession().displayResults();
			} catch (IllegalDataException err) {
			} catch (runTimeException error) {
			} catch (OutOfRangeException error) {
			} catch (NumberFormatException error) {
				String message = "Warning: please supply a float number" + Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);
			}

		} // end of if UpTxtF

		/* -- E l e c D e g e n e r -- */

		if (source == edTxtF) {
			int newElecDegener;

			try {
				newElecDegener = Integer.parseInt(edTxtF.getText()); // get back the txt entered by user (integer)
				setElecDegener(newElecDegener);
				setTemperature(T);// simply refresh a current temperature of this object
				// display a pane containing all the results
				Session.getCurrentSession().displayResults();
			} catch (IllegalDataException err) {
			} catch (runTimeException error) {
			} catch (OutOfRangeException error) {
			} catch (NumberFormatException error) {
				String message = "Warning: please supply an integer" + Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);
			}

		} // end of source==edTxtF

		/* -- R o t a t i o n a l s y m m e t r y n u m b e r -- */

		if (source == rotNbTxtF) {
			int newRotNb;

			try {
				newRotNb = Integer.parseInt(rotNbTxtF.getText()); // get back the txt entered by user (integer)
				setSymNumb(newRotNb);
				setTemperature(T);// simply refresh a current temperature of this object
				// display a pane containing all the results
				Session.getCurrentSession().displayResults();
			} catch (IllegalDataException err) {
			} catch (runTimeException error) {
			} catch (OutOfRangeException error) {
			} catch (NumberFormatException error) {
				String message = "Warning: please supply an integer" + Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);
			}

		} // end of source==rotNbTxtF

		/* -- I N E R T I A -- */

		if (source == inerJCB) {
			double newInertia;
			int newInertiaIndex;
			String stringElement;
			String[] stringInerList;
			String className = inerJCB.getSelectedItem().getClass().getName();

			if (inerJCB.getSelectedIndex() == -1) { // user tries to change the current value

				if (className.equals("JComboElement")) {
					JComboElement elem = (JComboElement) inerJCB.getSelectedItem(); // get back the txt entered by user
																					// in au
					stringElement = elem.toString();
				} else {
					stringElement = (String) inerJCB.getSelectedItem();
				} // get back the txt entered by user in au}
				try { // className.equals("java.lang.String")
					newInertia = Double.parseDouble(stringElement);
					newInertiaIndex = selectedInertia;

					setInertia(newInertia, newInertiaIndex);
					DefaultComboBoxModel model = new DefaultComboBoxModel();
					stringInerList = getInertiaString();
					for (int i = 0; i < stringInerList.length; i++) {
						JComboElement elem = new JComboElement(stringInerList[i], i);
						model.addElement(elem);
					} // for end
					inerJCB.setModel(model); // update JCOMBOBOX
					inerJCB.setSelectedIndex(selectedInertia);

					setTemperature(T);// simply refresh a current temperature of this object
					Session.getCurrentSession().displayResults();// display a pane containing all the results
				} // end try
				catch (IllegalDataException err) {
				} catch (runTimeException error) {
				} catch (OutOfRangeException error) {
				} catch (NumberFormatException error) {
					String message = "Warning: please supply a float number" + Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);
				}

			} // end of if getSelected index == -1

			if (inerJCB.getSelectedIndex() != -1) {
				selectedInertia = inerJCB.getSelectedIndex();
			} // update history of the selected Inertia moment if selected index !=-1 !!
		} // end of if inerJCB

		/* -- V I B F R E Q -- */

		if (source == vibJCB) {
			double newVib;
			int newVibIndex;
			String stringElement;
			String className = vibJCB.getSelectedItem().getClass().getName();

			if (vibJCB.getSelectedIndex() == -1) { // user tries to change the current value

				if (className.equals("JComboElement")) {
					JComboElement elem = (JComboElement) vibJCB.getSelectedItem(); // get back the txt entered by user
																					// in cm-1
					stringElement = elem.toString();
				} else {
					stringElement = (String) vibJCB.getSelectedItem();
				} // get back the txt entered by user in cm-1
				try { // className.equals("java.lang.String")
					newVib = Double.parseDouble(stringElement);
					newVib = newVib * Constants.convertCm_1ToKelvin; // convert from cm-1 to K
					newVibIndex = selectedVib;

					int[] originalVibIndex = (int[]) getVibRealFreqInCM_1().get(1); // the correspondence table between
																					// array of reals and original array
					setUnscaledVib(newVib, originalVibIndex[newVibIndex]); // updates the frequency in the unscaled !
																			// original array in K
					DefaultComboBoxModel model = new DefaultComboBoxModel();
					String[] vibList = (String[]) getVibRealFreqInCM_1().get(0); // get the updated array of real
																					// frequencies in cm-1
					double buffer;
					for (int i = 0; i < vibList.length; i++) {
						JComboElement elem = new JComboElement(vibList[i], i);
						model.addElement(elem);
					} // for end
					vibJCB.setModel(model); // update JCOMBOBOX
					vibJCB.setSelectedIndex(selectedVib);
					setTemperature(T);// simply refresh a current temperature of this object
					Session.getCurrentSession().displayResults();// display a pane containing all the results
				} // end try
				catch (IllegalDataException err) {
				} catch (runTimeException error) {
				} catch (OutOfRangeException error) {
				} catch (NumberFormatException error) {
					String message = "Warning: please supply a float number" + Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);
				}

			} // end of if getSelected index == -1

			if (vibJCB.getSelectedIndex() != -1) {
				selectedVib = vibJCB.getSelectedIndex();
			} // update history of the selected Inertia moment only if selected index !=-1 !!

		} // end of if vibJCB

		if (source == dvJB) { // return to initial values

			try {

				/* * -- M A S S -- * */
				double newMass;
				newMass = getInitialMass();
				massTxtF.setText(Maths.format(newMass, "0.00")); // get back the initial mass value
				setMassToInitialValue();

				/* * -- U p -- * */
				double newUp;
				newUp = getInitialUp(); // in J/mol
				upTxtF.setText(Maths.format(newUp / Constants.convertHartreeToJoule, "0.00000")); // get back the
																									// initial up value
																									// in ua
				setUpToInitialValue();

				/* * -- E l e c D e g e n -- * */
				int newElecDegener;
				newElecDegener = getInitialElecDegener();
				edTxtF.setText(Maths.format(newElecDegener, "0")); // get back the initial elecDegener
				setElecDegenerToInitialValue();

				if (!atomic) {
					/* * -- R o t a t i o n a l S y m m e t r y N u m b e r -- * */
					int newRotNb;
					newRotNb = getInitialSymNumb();
					rotNbTxtF.setText(Maths.format(newRotNb, "0")); // get back the initial Symmetry Number
					setSymNumbToInitialValue();

					/* * -- I n e r t i a -- * */
					String[] stringInerList;
					stringInerList = getInitialInertia(); // return the String inertia Array of appropriate size (1 or
															// 3)
					DefaultComboBoxModel model = new DefaultComboBoxModel(); // set a new JCOMBOBOX model

					for (int i = 0; i < stringInerList.length; i++) {
						JComboElement elem = new JComboElement(stringInerList[i], i);
						model.addElement(elem);
					} // for end
					inerJCB.setModel(model); // update JCOMBOBOX
					inerJCB.setSelectedIndex(0); // set the first item selected // will cause ActionPerformed to be
													// called once more !
					selectedInertia = 0;
					setInertiaToInitialValue();

					/* * -- V I B -- * */
					String[] stringVibList;
					stringVibList = getInitialRealVib(); // return the String Array of initial scaled vib frequencies in
															// cm-1

					for (int i = 0; i < stringVibList.length; i++) {
						JComboElement elem = new JComboElement(stringVibList[i], i);
						model.addElement(elem);
					} // for end
					vibJCB.setModel(model); // update JCOMBOBOX
					vibJCB.setSelectedIndex(0);// set the first item selected // will cause ActionPerformed to be called
												// once more !
					selectedVib = 0;
					setUnscaledVibToInitialValue(); // set Vib To Initial Values;

				} // end of if (!atomic)

				setTemperature(T);// simply refresh a current temperature of this object
				// display a pane containing all the results

				Session.getCurrentSession().displayResults();
			} // end of try
			catch (runTimeException error) {
			} catch (IllegalDataException error) {
			} catch (OutOfRangeException error) {
			}

		} // end of if dvJChB

	} // end of actionPerformed

}// InertStatisticalSystem
