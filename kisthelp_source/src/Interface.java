
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import java.util.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.lang.*;
import java.net.URL;

import javax.naming.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import kisthep.util.*;
import kisthep.file.*;

public class Interface extends JFrame implements ActionListener {

	// P R O P E R T I E S

	private static Interface kisthepInterface;

	private InitialContext context;

	private JMenuBar bar;
	private JMenu file, dataMenu, calculation, help, tst, tst_w, tst_eck, vtst,
			vtst_w, vtst_eck, rrkm;
	private JMenu sessions, units;
	private JMenuItem New, open, save, save_as, print, close, exit, molec, keq,
			unimolTst, unimolTst_w, unimolTst_eck, unimolVtst, unimolVtst_w,
			unimolVtst_eck, bimolTst, bimolTst_w, bimolTst_eck, bimolVtst,
			bimolVtst_w, bimolVtst_eck, rrkmTightTs, rrkmLooseTs, session1,
			helpDocumentation, helpInputFile, aboutKisthep, kjPerMol;
			//kcalPerMol;

	private JMenuItem reset, dataRefresh, saveResults, saveInputs, showConstants, reactPathBuild, lennardJones;
	private JMenuItem[] tJMenuItemFileInactives;
	private static JMenuItem[] tJMenuItemCalculationInactives;
	private JCheckBoxMenuItem[] temperatureUnitMenu;
	private JCheckBoxMenuItem[] pressureUnitMenu;

	private String filename;
	private File existingFile;
	private Session workSession;
	private static JPanel temperaturePane, pressurePane;
	private static Box calculationFeatureBox;
	private static Box forGraphicsButtonsBox;
	private static JDesktopPane centralPane;

	private JLabel labelTemperature, emptyLabel, labelTemperatureMin,
			labelTemperatureMax, labelStepTemperature, labelScalingFactor,
			labelPressure, labelPressureMin, labelPressureMax,
			labelStepPressure;
	private JTextField txtTemperature, txtTemperatureMin, txtTemperatureMax,
			txtStepTemperature, txtScalingFactor, txtPressure, txtPressureMin,
			txtPressureMax, txtStepPressure;
	private JRadioButton temperatureRadio1, temperatureRadio2, pressureRadio1,
			pressureRadio2;
	private ButtonGroup tGroup, pGroup;
	private static Logo logo;

	private JMenu temperatureMenu, pressureMenu;
	private JCheckBoxMenuItem torrMenu, barMenu, paMenu, atmMenu;
	private JCheckBoxMenuItem kelvinMenu, celsiusMenu, fahrenheitMenu;

	private final int temperatureMenuNumber = 3;
	private final int kelvinIndex = 0;
	private final int celsiusIndex = 1;
	private final int fahrenheitIndex = 2;

	private final int pressureMenuNumber = 4;
	private final int paIndex = 0;
	private final int barIndex = 1;
	private final int atmIndex = 2;
	private final int torrIndex = 3;

	private final int calculationNumberItem = 10;
	private final int molecIndex = 0;
	private final int keqIndex = 1;
	private final int tstIndex = 2;
	private final int tst_wIndex = 3;
	private final int tst_eckIndex = 4;
	private final int vtstIndex = 5;
	private final int rrkmIndex = 6;
	private final int resetIndex = 7;
	private final int vtst_wIndex = 8;
	private final int vtst_eckIndex = 9;

	private final int fileNumberItem = 4;
	private final int saveAsIndex = 0;
	private final int printIndex = 1;
	private final int closeIndex = 2;
	private final int saveIndex = 3;

	// C O N S T R U C T O R

	public Interface() {

		kisthepInterface = this;
		setTitle("KiSThelP");
		// window's properties
		setTitle("K  i  S  T  h  e  l  P");
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenDim = tk.getScreenSize();

		setSize(Constants.mainPaneWidth, Constants.mainPaneHeight);
		setLocation((int) (screenDim.width / 7), (int) (screenDim.height / 7));

		// Bar's instantiation
		bar = new JMenuBar();
		setJMenuBar(bar);

		// Use of the newMainJMenu method
		file = newMainJMenu("Session");

		dataMenu = newMainJMenu("Data");
		calculation = newMainJMenu("Calculation");
		sessions = newMainJMenu("Window");
		units = newMainJMenu("Units");
		help = newMainJMenu("Help");
		

		// Use of the newJMenu method
		molec = newJMenuItem("Atom,Molecule", calculation);
		tst = newJMenu("k / TST", calculation);
		tst_w = newJMenu("k / TST/W", calculation);
		tst_eck = newJMenu("k / TST/Eck", calculation);

		vtst = newJMenu("k / VTST", calculation);
		vtst_w = newJMenu("k / VTST/W", calculation);
		vtst_eck = newJMenu("k / VTST/Eck", calculation);

		rrkm = newJMenu("k / RRKM", calculation);

		// Use of the newJMenuItem method
		New = newJMenuItem("New", file);
		open = newJMenuItem("Open", file);
		save = newJMenuItem("Save", file);
		save_as = newJMenuItem("Save as", file);
		//print = newJMenuItem("Print", file); // not yet written
		print = new JMenuItem();
		close = newJMenuItem("Close", file);
		exit = newJMenuItem("Exit", file);

		keq = newJMenuItem("Keq", calculation);
		reset = newJMenuItem("Reset", calculation);
		unimolTst = newJMenuItem("Unimolecular", tst);
		unimolTst_w = newJMenuItem("Unimolecular", tst_w);
		unimolTst_eck = newJMenuItem("Unimolecular", tst_eck);
		unimolVtst = newJMenuItem("Unimolecular", vtst);
		unimolVtst_w = newJMenuItem("Unimolecular", vtst_w);
		unimolVtst_eck = newJMenuItem("Unimolecular", vtst_eck);
		bimolTst = newJMenuItem("Bimolecular", tst);
		bimolTst_w = newJMenuItem("Bimolecular", tst_w);
		bimolTst_eck = newJMenuItem("Bimolecular", tst_eck);
		bimolVtst = newJMenuItem("Bimolecular", vtst);
		bimolVtst_w = newJMenuItem("Bimolecular", vtst_w);
		bimolVtst_eck = newJMenuItem("Bimolecular", vtst_eck);

		rrkmTightTs = newJMenuItem("tight TS", rrkm);
		rrkmLooseTs = newJMenuItem("loose TS", rrkm);
		rrkmLooseTs.setEnabled(false); // not implemented at present time
		dataRefresh = newJMenuItem("Refresh", dataMenu);
		saveInputs = newJMenuItem("Inputs/save as", dataMenu);
		saveResults = newJMenuItem("Results/save as", dataMenu);
		reactPathBuild = newJMenuItem("Build R\u00B0 Path .kinp", dataMenu);
		showConstants = newJMenuItem("KiSThelP Constants", dataMenu);
		lennardJones = newJMenuItem("Lennard-Jones parameters (for RRKM calculations)", dataMenu);

		// the kjPerMol is include in the Units Menu
		// kjPerMol = newJMenuItem("Results in kJ/mol (default)",units) ;
		kjPerMol = newJMenuItem("Energy units in kJ (default)", units);
		// the kcalPerMol is include in the Units Menu
		//kcalPerMol = newJMenuItem("Energy units in kcal", units);
		//kcalPerMol.setEnabled(false);

		temperatureMenu = new JMenu("T");
		pressureMenu = new JMenu("P");

		// setenable T and P in Units menu
		temperatureMenu.setEnabled(false);
		pressureMenu.setEnabled(false);

		atmMenu = new JCheckBoxMenuItem("Atm", false);
		barMenu = new JCheckBoxMenuItem("Bar", true);
		paMenu = new JCheckBoxMenuItem("Pa", false);
		torrMenu = new JCheckBoxMenuItem("Torr", false);

		kelvinMenu = new JCheckBoxMenuItem("Kelvin", true);
		celsiusMenu = new JCheckBoxMenuItem("Celsius", false);
		fahrenheitMenu = new JCheckBoxMenuItem("Fahrenheit", false);

		temperatureUnitMenu = new JCheckBoxMenuItem[temperatureMenuNumber];
		temperatureUnitMenu[kelvinIndex] = kelvinMenu;
		temperatureUnitMenu[celsiusIndex] = celsiusMenu;
		temperatureUnitMenu[fahrenheitIndex] = fahrenheitMenu;

		pressureUnitMenu = new JCheckBoxMenuItem[pressureMenuNumber];
		pressureUnitMenu[paIndex] = paMenu;
		pressureUnitMenu[barIndex] = barMenu;
		pressureUnitMenu[atmIndex] = atmMenu;
		pressureUnitMenu[torrIndex] = torrMenu;

		temperatureMenu.add(kelvinMenu);
		temperatureMenu.add(celsiusMenu);
		temperatureMenu.add(fahrenheitMenu);

		pressureMenu.add(paMenu);
		pressureMenu.add(barMenu);
		pressureMenu.add(atmMenu);
		pressureMenu.add(torrMenu);

		// add to units menu

		units.add(temperatureMenu);
		units.add(pressureMenu);

		helpInputFile = newJMenuItem("KiSThelP user help", help);
		// the helpDocumentation is include in the Help Menu
		helpDocumentation = newJMenuItem("KiSThelP programmer help", help);
		helpDocumentation.setEnabled(true); // now can access to programmer
											// documentation

		// the aboutKisthep is include in the Help Menu
		aboutKisthep = newJMenuItem("About KiSThelP ...", help);

		// not operational at present time
		// unimolVtst.setEnabled(false);
		// bimolVtst.setEnabled(false);

		// no results displayed at present time
		dataRefresh.setEnabled(false);
		saveResults.setEnabled(false);
		saveInputs.setEnabled(false);
		showConstants.setEnabled(true);
		lennardJones.setEnabled(true);		
		reactPathBuild.setEnabled(true);

		// Main pane management
		Container mainPane = getContentPane();
		mainPane.setBackground(new Color(218, 223, 224));
		BorderLayout g1 = new BorderLayout();
		mainPane.setLayout(g1);

		// Pane1 management
		// Temperature
		temperaturePane = new JPanel();
		FlowLayout g2 = new FlowLayout();
		temperaturePane.setLayout(g2);
		temperaturePane.setBorder(BorderFactory.createRaisedBevelBorder());
		temperaturePane.setBackground(new Color(236, 236, 220));
		// Pane2 management
		// Pressure
		pressurePane = new JPanel();
		FlowLayout g4 = new FlowLayout();
		pressurePane.setLayout(g4);
		pressurePane.setBorder(BorderFactory.createRaisedBevelBorder());
		pressurePane.setBackground(new Color(236, 236, 220));

		// Radiobutton1 management
		// Temperature
		
		ImageIcon icon;
		URL address =   getClass().getResource("/images/smallLogo.png");
		
		if (address == null) {
			String message = "Error in class Interface in constructor"+ Constants.newLine;
			message = message +  "File smallLogo.png not found"+ Constants.newLine;
			message = message +  "Please contact the authors"+ Constants.newLine;
			JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                      
		}
		else {
			icon = new ImageIcon(address); 
			JButton smallLogo = new JButton("");
			smallLogo.setIcon(icon);
			bar.add(Box.createGlue());
			bar.add(smallLogo);
		}
		
		temperatureRadio1 = new JRadioButton();
		temperatureRadio1.setBackground(new Color(236, 236, 220));
		temperatureRadio1.setSelected(true);
		temperatureRadio1.setEnabled(false);
		temperaturePane.add(temperatureRadio1);

		// Label1 managment
		// Temperature
		labelTemperature = new JLabel("Temp (K)");
		labelTemperature.setForeground(Color.black);
		temperaturePane.add(labelTemperature);

		// TextField1 managment
		// Temperature
		txtTemperature = new JTextField("", 5);
		temperaturePane.add(txtTemperature);
		txtTemperature.setEnabled(false); // Not used without the creation of a
											// session

		// Radiobutton2 managment
		// Temperature
		temperatureRadio2 = new JRadioButton();
		temperatureRadio2.setBackground(new Color(236, 236, 220));
		temperaturePane.add(temperatureRadio2);
		temperatureRadio2.setEnabled(false); // Tmin, Tmax and Step are not used
												// now

		// Label2 managment
		emptyLabel = new JLabel("    ");
		emptyLabel.setForeground(Color.black);
		// Temperature
		temperaturePane.add(emptyLabel);

		// Label3 managment
		// Temperature
		labelTemperatureMin = new JLabel("T min");
		labelTemperatureMin.setForeground(Color.black);
		temperaturePane.add(labelTemperatureMin);

		// TextField2 managment
		// Temperature
		txtTemperatureMin = new JTextField(5);
		temperaturePane.add(txtTemperatureMin);

		// Label4 managment
		// Temperature
		labelTemperatureMax = new JLabel(" T max");
		labelTemperatureMax.setForeground(Color.black);
		temperaturePane.add(labelTemperatureMax);

		// TextField3 managment
		// Temperature
		txtTemperatureMax = new JTextField(5);
		temperaturePane.add(txtTemperatureMax);

		// Label5 managment
		// Temperature
		labelStepTemperature = new JLabel(" Step");
		labelStepTemperature.setForeground(Color.black);
		temperaturePane.add(labelStepTemperature);

		// TextField4 managment
		// Temperature
		txtStepTemperature = new JTextField(3);
		temperaturePane.add(txtStepTemperature);

		// LabelScalingFactor managment
		// Temperature
		labelScalingFactor = new JLabel("    Vib. Scaling Factor");
		labelScalingFactor.setForeground(Color.black);
		temperaturePane.add(labelScalingFactor);
		labelScalingFactor.setEnabled(false);

		// TextFieldScalingFactor managment
		// Temperature
		txtScalingFactor = new JTextField(3);
		temperaturePane.add(txtScalingFactor);
		txtScalingFactor.setEnabled(false); // Not used without the creation of
											// a session

		// formation of a radiobuttons group
		tGroup = new ButtonGroup();
		tGroup.add(temperatureRadio1);
		tGroup.add(temperatureRadio2);

		// Pressure
		pressureRadio1 = new JRadioButton();
		pressureRadio1.setBackground(new Color(236, 236, 220));
		pressureRadio1.setSelected(true);
		pressureRadio1.setEnabled(false);
		pressurePane.add(pressureRadio1);

		// Pressure
		labelPressure = new JLabel("Pressure (bar)");
		labelPressure.setForeground(Color.black);
		labelPressure.setEnabled(false);
		pressurePane.add(labelPressure);

		// Pressure
		txtPressure = new JTextField("", 5);
		pressurePane.add(txtPressure);
		txtPressure.setEnabled(false);

		// Pressure
		pressureRadio2 = new JRadioButton();
		pressureRadio2.setBackground(new Color(236, 236, 220));
		pressurePane.add(pressureRadio2);
		pressureRadio2.setEnabled(false);

		// formation of a radiobuttons group
		pGroup = new ButtonGroup();
		pGroup.add(pressureRadio1);
		pGroup.add(pressureRadio2);

		// Pression
		pressurePane.add(emptyLabel);

		// Pressure
		labelPressureMin = new JLabel("P Min");
		labelPressureMin.setForeground(Color.black);
		labelPressureMin.setEnabled(false);
		pressurePane.add(labelPressureMin);

		// Pressure
		txtPressureMin = new JTextField(5);
		txtPressureMin.setEnabled(false);
		pressurePane.add(txtPressureMin);

		// Pressure
		labelPressureMax = new JLabel("P Max");
		labelPressureMax.setForeground(Color.black);
		labelPressureMax.setEnabled(false);
		pressurePane.add(labelPressureMax);

		// Pressure
		txtPressureMax = new JTextField(5);
		txtPressureMax.setEnabled(false);
		pressurePane.add(txtPressureMax);

		// Pressure
		labelStepPressure = new JLabel(" Step");
		labelStepPressure.setForeground(Color.black);
		labelStepPressure.setEnabled(false);
		pressurePane.add(labelStepPressure);

		// Pressure
		txtStepPressure = new JTextField(5);
		txtStepPressure.setEnabled(false);
		pressurePane.add(txtStepPressure);

		// add five panels to KisthepContentPane
		refreshFillKisthepContentPane();

		//
		labelTemperature.setEnabled(false); //
		labelTemperatureMin.setEnabled(false); //
		txtTemperatureMin.setEnabled(false); // Tmin, Tmax and Step are
		labelTemperatureMax.setEnabled(false); // not used now
		txtTemperatureMax.setEnabled(false); //
		labelStepTemperature.setEnabled(false); //
		txtStepTemperature.setEnabled(false); //

		// Addition of action listeners
		exit.addActionListener(this);
		open.addActionListener(this);
		save.addActionListener(this);
		save_as.addActionListener(this);
		close.addActionListener(this);
		New.addActionListener(this);
		molec.addActionListener(this);
		keq.addActionListener(this);
		unimolTst.addActionListener(this);
		unimolTst_w.addActionListener(this);
		unimolTst_eck.addActionListener(this);
		unimolVtst.addActionListener(this);
		unimolVtst_w.addActionListener(this);
		unimolVtst_eck.addActionListener(this);
		bimolTst.addActionListener(this);
		bimolTst_w.addActionListener(this);
		bimolTst_eck.addActionListener(this);
		bimolVtst.addActionListener(this);
		bimolVtst_w.addActionListener(this);
		bimolVtst_eck.addActionListener(this);

		rrkmTightTs.addActionListener(this);
		rrkmLooseTs.addActionListener(this);

		reset.addActionListener(this);
		dataRefresh.addActionListener(this);
		saveResults.addActionListener(this);
		saveInputs.addActionListener(this);
		showConstants.addActionListener(this);
		lennardJones.addActionListener(this);
		reactPathBuild.addActionListener(this);

		txtTemperature.addActionListener(this);
		txtTemperatureMin.addActionListener(this);
		txtTemperatureMax.addActionListener(this);
		txtStepTemperature.addActionListener(this);
		txtScalingFactor.addActionListener(this);
		temperatureRadio1.addActionListener(this);
		temperatureRadio2.addActionListener(this);

		pressureRadio1.addActionListener(this);
		pressureRadio2.addActionListener(this);
		kelvinMenu.addActionListener(this);
		celsiusMenu.addActionListener(this);
		fahrenheitMenu.addActionListener(this);
		paMenu.addActionListener(this);
		barMenu.addActionListener(this);
		atmMenu.addActionListener(this);
		torrMenu.addActionListener(this);
		txtPressure.addActionListener(this);
		txtPressureMin.addActionListener(this);
		txtPressureMax.addActionListener(this);
		txtStepPressure.addActionListener(this);

		

		helpDocumentation.addActionListener(this);
		aboutKisthep.addActionListener(this);
		kjPerMol.addActionListener(this);
		//kcalPerMol.addActionListener(this);
		helpInputFile.addActionListener(this);

		

		rrkm.addActionListener(this);
		

		// Create a table of the inactives file's submenus
		tJMenuItemFileInactives = new JMenuItem[fileNumberItem];
		tJMenuItemFileInactives[saveAsIndex] = save_as;
		tJMenuItemFileInactives[printIndex] = print;
		tJMenuItemFileInactives[closeIndex] = close;
		tJMenuItemFileInactives[saveIndex] = save;

		// Create a table of the inactives calculation's submenus

		tJMenuItemCalculationInactives = new JMenuItem[calculationNumberItem];
		tJMenuItemCalculationInactives[molecIndex] = molec;
		tJMenuItemCalculationInactives[keqIndex] = keq;
		tJMenuItemCalculationInactives[tstIndex] = tst;
		tJMenuItemCalculationInactives[tst_wIndex] = tst_w;
		tJMenuItemCalculationInactives[tst_eckIndex] = tst_eck;
		tJMenuItemCalculationInactives[vtstIndex] = vtst;
		tJMenuItemCalculationInactives[vtst_wIndex] = vtst_w;
		tJMenuItemCalculationInactives[vtst_eckIndex] = vtst_eck;
		tJMenuItemCalculationInactives[rrkmIndex] = rrkm;
		tJMenuItemCalculationInactives[resetIndex] = reset;

		// Use of File or calculation ItemsetEnabled method
		fileItemsetEnabled(false);
		calculationItemsetEnabled(false);

		/*****************************************************************************/
		/* s e t T o o l T i p T e x t */
		/*****************************************************************************/

		// File Menu
		file.setToolTipText("Session Menu");

		New.setToolTipText("Create a new session");
		open.setToolTipText("Open a session");
		save.setToolTipText("save the session");
		save_as.setToolTipText("Save the session");
		print.setToolTipText("Print");
		close.setToolTipText("Close the session");
		exit.setToolTipText("Exit KiSThelP application");

		// Data Menu
		dataMenu.setToolTipText("Data Menu");

		dataRefresh.setToolTipText("Refresh the results window");
		saveResults.setToolTipText("Save results as a .csv format file");
		saveInputs.setToolTipText("Save current molecular information as .kinp format file");
		showConstants.setToolTipText("show constants employed in KiSThelP");
		lennardJones.setToolTipText("recommended \u03B5/k and \u03C3 Lennard-Jones parameters, from litterature, for rrkm calculations");
		reactPathBuild.setToolTipText("concatenate several .kinp input files for VTST calculation");

		// Calculation Menu
		calculation.setToolTipText("Calculation Menu");

		tst.setToolTipText("kinetic level : Transition State Theory");
		tst_w.setToolTipText("kinetic level : TST with Wigner tunneling correction");
		tst_eck.setToolTipText("kinetic level : TST with Eckart tunneling correction");

		vtst.setToolTipText("kinetic level : Variational TST");
		vtst_w.setToolTipText("kinetic level : Variational TST with Wigner tunneling correction");
		vtst_eck.setToolTipText("kinetic level : Variational TST with Eckart tunneling correction");

		unimolTst.setToolTipText("One reactant");
		unimolTst_w.setToolTipText("One reactant");
		unimolTst_eck.setToolTipText("One reactant");
		unimolVtst.setToolTipText("One reactant");
		unimolVtst_w.setToolTipText("One reactant");
		unimolVtst_eck.setToolTipText("One reactant");
		bimolTst.setToolTipText("Two reactants");
		bimolTst_w.setToolTipText("Two reactants");
		bimolTst_eck.setToolTipText("Two reactants");
		bimolVtst.setToolTipText("Two reactants");
		bimolVtst_w.setToolTipText("Two reactants");
		bimolVtst_eck.setToolTipText("Two reactants");
		keq.setToolTipText("Chemical equilibrium constant");

		rrkm.setToolTipText("kinetic level : RRKM");
		rrkmTightTs.setToolTipText("Rigid TS model");
		rrkmLooseTs.setToolTipText("Loose TS model");

		molec.setToolTipText("Thermodynamic properties");
		reset.setToolTipText("Reset the current calculation");

		// Session Menu
		sessions.setToolTipText("Select Window");

		// Units Menu
		units.setToolTipText("Select units");

		kjPerMol.setToolTipText("Select the kJ unit");
		//kcalPerMol.setToolTipText("Select the kcal unit");
		temperatureMenu.setToolTipText("Set T unit");
		pressureMenu.setToolTipText("Set P unit");

		// Help Menu
		help.setToolTipText("Help Menu");

		helpInputFile.setToolTipText("how to use KiSThelP");
		helpDocumentation.setToolTipText("KiSThelP JAVA Classes");
		// aboutKisthep.setToolTipText("About the authors");

		// Temperature Bar
		temperatureRadio1.setToolTipText("Single temperature mode");
		temperatureRadio2.setToolTipText("Temperature range mode");
		txtStepTemperature.setToolTipText("Temperature step");

		// Scaling Factor
		txtScalingFactor.setToolTipText("Vib freq scaling factor");

		// Pressure Bar
		pressureRadio1.setToolTipText("Single pressure mode");
		pressureRadio2.setToolTipText("Pressure range mode");
		txtStepPressure.setToolTipText("Pressure step");

		/*****************************************************************************/
		/* KISTHEP WELCOME */
		/***************************************************************************/

		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");

		System.out
				.println("/*****************************************************************************/");
		System.out
				.println("/***                     K  i  S  T  h  e  l P                             ***/");
		System.out
				.println("/***************************************************************************/");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");

	} // End of constructor

	// M E T H O D S

	/************************************/
	/* r e s e t C a l c u l a t i o n */
	/************************************/

	public void resetCalculation() {
		// reset all the calculations
		// and clear the panes

		KisthepFile.cleanOpenFileList();
		resetSetEnabled(false);

		refreshFillKisthepContentPane();
		// if and only if a session exists:
		if (workSession != null) {workSession.calculationErase();}
		saveResults.setEnabled(false);
		saveInputs.setEnabled(false);
		dataRefresh.setEnabled(false);
		reactPathBuild.setEnabled(true);

		// refresh the overall JFrame
		getContentPane().validate();
		getContentPane().repaint();

		
	} // end of resetCalculation

	/**********************************************/
	/* g e t K i s t h e p I n t e r f a c e */
	/**********************************************/

	public static Interface getKisthepInterface() {
		return kisthepInterface;
	}

	/***********************************/
	/* a c t i o n P e r f o r m e d */
	/***********************************/

	public void actionPerformed(ActionEvent ev) {
		Object source = ev.getSource();

		// Exit management
		if (source == exit) {

			if ((workSession != null) && (Session.getToBeSaveFlag() == true)) {

				int rep = JOptionPane.showConfirmDialog(null,
						"This session is not saved, do you want to continue ?",
						"Exit", JOptionPane.WARNING_MESSAGE);
				if (rep == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			} else {
				int r = JOptionPane.showConfirmDialog(null,
						"Do you want to exit?", "exit",
						JOptionPane.YES_NO_OPTION);
				if (r == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			} // end of if (Session.getToBeSaveFlag()==true
		} // end of if (source==exit) {

		// Open management
		if (source == open) {
			try {

				File temporyFileName = KisthepDialog.requireExistingFilename(	"Session filename ?", new KisthepSessionFileFilter());

				filename = temporyFileName.getAbsolutePath();

				session1 = newJMenuItem("Session 1", sessions);
				workSession = new Session();
				workSession.load(filename);

				// setenable T and P in Units menu
				temperatureMenu.setEnabled(true);
				pressureMenu.setEnabled(true);

				// select appropriate T and P unit

				if (workSession.getUnitSystem().getCurrentTemperatureUnit()
						.equals("Kelvin")) {

					temperatureSelectOnly(kelvinMenu);
				}
				if (workSession.getUnitSystem().getCurrentTemperatureUnit()
						.equals("Celsius")) {

					temperatureSelectOnly(celsiusMenu);
				}
				if (workSession.getUnitSystem().getCurrentTemperatureUnit()
						.equals("Fahrenheit")) {

					temperatureSelectOnly(fahrenheitMenu);
				}

				if (workSession.getUnitSystem().getCurrentPressureUnit()
						.equals("Bar")) {

					pressureSelectOnly(barMenu);
				}

				if (workSession.getUnitSystem().getCurrentPressureUnit()
						.equals("Pascal")) {

					pressureSelectOnly(paMenu);
				}

				if (workSession.getUnitSystem().getCurrentPressureUnit()
						.equals("Torr")) {

					pressureSelectOnly(torrMenu);
				}

				if (workSession.getUnitSystem().getCurrentPressureUnit()
						.equals("Atmosphere")) {

					pressureSelectOnly(atmMenu);
				}

				// if session is empty (only temperature ...)
				if (workSession.getSize() == 0) {
				}

				resetSetEnabled(false);

				// if session is not empty (i.e. contains one sessionComponent
				// object)

				if (workSession.getSize() != 0) {

					resetSetEnabled(true);

				}// end of workSession.getSize!=0

				fileItemsetEnabled(true);
				open.setEnabled(false);
				New.setEnabled(false);
				print.setEnabled(false);

				labelScalingFactor.setEnabled(true);
				txtScalingFactor.setText(String.valueOf(Maths.format(
						workSession.getScalingFactor(), "0.00")));
				txtScalingFactor.setEnabled(true);

				txtTemperature.setText(String.valueOf(Maths.format(
						Session.getCurrentSession()
								.getUnitSystem()
								.convertToTemperatureUnit(
										workSession.getTemperatureMin()),
						"0.00")));

				labelTemperature.setText("Temp ("
						+ workSession.getUnitSystem().getTemperatureSymbol()
						+ ")");

				temperatureRadio1.setEnabled(true); // default (selected)
				temperatureRadio2.setEnabled(true);

				txtPressure.setText(String.valueOf(Maths.format(Session
						.getCurrentSession().getUnitSystem()
						.convertToPressureUnit(workSession.getPressureMin()),
						"0.00")));

				labelPressure
						.setText("Pressure ("
								+ workSession.getUnitSystem()
										.getPressureSymbol() + ")");

				pressureRadio1.setEnabled(true); // default (selected)
				pressureRadio2.setEnabled(true);

				// if loaded session corresponds to an unique temperature and an
				// unique pressure
				if ((workSession.getTemperatureMin() == workSession
						.getTemperatureMax())
						&& (workSession.getPressureMin() == workSession
								.getPressureMax())) {
					labelTemperature.setEnabled(true);
					txtTemperature.setEnabled(true);
					labelPressure.setEnabled(true);
					txtPressure.setEnabled(true);

					// put default values for range pressure and temperature
					txtPressureMin.setText(String.valueOf(Maths.format(Session
							.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.P0), "0.00")));
					txtPressureMax.setText(String.valueOf(Maths.format(Session
							.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(2 * Constants.P0), "0.00")));
					txtStepPressure.setText(String.valueOf(Maths.format(Session
							.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.pStepDefault),
							"0.00")));
					txtTemperatureMin
							.setText(String.valueOf(Maths.format(
									Session.getCurrentSession()
											.getUnitSystem()
											.convertToTemperatureUnit(
													Constants.roomTemperature),
									"0.00")));
					txtTemperatureMax.setText(String.valueOf(Maths.format(
							Session.getCurrentSession()
									.getUnitSystem()
									.convertToTemperatureUnit(
											Constants.roomTemperature + 10),
							"0.00")));

					double tStepTemporary = Session
							.getCurrentSession()
							.getUnitSystem()
							.convertToTemperatureUnit(
									0.0 + Constants.tStepDefault);
					tStepTemporary = tStepTemporary
							- Session.getCurrentSession().getUnitSystem()
									.convertToTemperatureUnit(0.0);
					txtStepTemperature.setText(String.valueOf(Maths.format(
							tStepTemporary, "0.00")));

				} // end of if (tMin==tMax AND pMin==pMax

				
				// temperature range at fixed pressure
				if ( (workSession.getTemperatureMin() != workSession.getTemperatureMax()) &&
					 (workSession.getPressureMin() == workSession.getPressureMax())	){

					// make available unique pressure
					txtPressure.setEnabled(true);
					labelPressure.setEnabled(true);
					// make unavailable the pressure range label and text
					// but enable pressure range possibility
					labelPressureMin.setEnabled(false);
					txtPressureMin.setEnabled(false);
					labelPressureMax.setEnabled(false);
					txtPressureMax.setEnabled(false);
					labelStepPressure.setEnabled(false);
					txtStepPressure.setEnabled(false);
					pressureRadio2.setEnabled(true);

					temperatureRadio2.setSelected(true); // radio2 selected now

					txtTemperatureMin.setEnabled(true);
					txtTemperatureMax.setEnabled(true);
					txtStepTemperature.setEnabled(true);

					labelTemperatureMin.setEnabled(true);
					labelTemperatureMax.setEnabled(true);
					labelStepTemperature.setEnabled(true);

					// put default values for range pressure and loaded values
					// for temperature
					txtPressureMin.setText(String.valueOf(Maths.format(Session
							.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.P0), "0.00")));
					txtPressureMax.setText(String.valueOf(Maths.format(Session
							.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(2 * Constants.P0), "0.00")));
					txtStepPressure.setText(String.valueOf(Maths.format(Session
							.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.pStepDefault),
							"0.00")));
					txtTemperatureMin.setText(String.valueOf(Maths.format(
							Session.getCurrentSession()
									.getUnitSystem()
									.convertToTemperatureUnit(
											workSession.getTemperatureMin()),
							"0.00")));
					txtTemperatureMax.setText(String.valueOf(Maths.format(
							Session.getCurrentSession()
									.getUnitSystem()
									.convertToTemperatureUnit(
											workSession.getTemperatureMax()),
							"0.00")));

					double tStepTemporary = Session
							.getCurrentSession()
							.getUnitSystem()
							.convertToTemperatureUnit(
									0.0 + workSession.getStepTemperature());
					tStepTemporary = tStepTemporary
							- Session.getCurrentSession().getUnitSystem()
									.convertToTemperatureUnit(0.0);
					txtStepTemperature.setText(String.valueOf(Maths.format(
							tStepTemporary, "0.00")));

				} // end of if Tmin!=Tmax at fixed pressure

				// if loaded session corresponds to a pressure range at fixed temperature
				if  ( (workSession.getPressureMin() != workSession.getPressureMax()) &&
					  (workSession.getTemperatureMin() == workSession.getTemperatureMax())) {

					// make available unique temperature
					txtTemperature.setEnabled(true);
					labelTemperature.setEnabled(true);

					// make unavailable the temperature range text and label 
					// but enable temperature range possibility
					labelTemperatureMin.setEnabled(false);
					txtTemperatureMin.setEnabled(false);
					labelTemperatureMax.setEnabled(false);
					txtTemperatureMax.setEnabled(false);
					labelStepTemperature.setEnabled(false);
					txtStepTemperature.setEnabled(false);
					temperatureRadio2.setEnabled(true);
					
					
					pressureRadio2.setSelected(true); // radio2 selected now

					txtPressureMin.setEnabled(true);
					txtPressureMax.setEnabled(true);
					txtStepPressure.setEnabled(true);

					labelPressureMin.setEnabled(true);
					labelPressureMax.setEnabled(true);
					labelStepPressure.setEnabled(true);

					// put default values for temperature range and loaded
					// values for pressure range
					txtPressureMin.setText(String.valueOf(Maths.format(
							Session.getCurrentSession()
									.getUnitSystem()
									.convertToPressureUnit(
											workSession.getPressureMin()),
							"0.00")));
					txtPressureMax.setText(String.valueOf(Maths.format(
							Session.getCurrentSession()
									.getUnitSystem()
									.convertToPressureUnit(
											workSession.getPressureMax()),
							"0.00")));
					txtStepPressure.setText(String.valueOf(Maths.format(
							Session.getCurrentSession()
									.getUnitSystem()
									.convertToPressureUnit(
											workSession.getStepPressure()),
							"0.00")));
					txtTemperatureMin
							.setText(String.valueOf(Maths.format(
									Session.getCurrentSession()
											.getUnitSystem()
											.convertToTemperatureUnit(
													Constants.roomTemperature),
									"0.00")));
					txtTemperatureMax.setText(String.valueOf(Maths.format(
							Session.getCurrentSession()
									.getUnitSystem()
									.convertToTemperatureUnit(
											Constants.roomTemperature + 10),
							"0.00")));

					double tStepTemporary = Session
							.getCurrentSession()
							.getUnitSystem()
							.convertToTemperatureUnit(
									0.0 + Constants.tStepDefault);
					tStepTemporary = tStepTemporary
							- Session.getCurrentSession().getUnitSystem()
									.convertToTemperatureUnit(0.0);
					txtStepTemperature.setText(String.valueOf(Maths.format(
							tStepTemporary, "0.00")));

				} // end of if Pmin!=Pmax at fixed temperature
				
				// if loaded session corresponds to a double range (pressure and temperature)
				if  ( (workSession.getPressureMin() != workSession.getPressureMax()) &&
					  (workSession.getTemperatureMin() != workSession.getTemperatureMax())) {

					// make available unique temperature
					txtTemperature.setEnabled(false);
					labelTemperature.setEnabled(false);

					// make available the temperature range text and label and temperature range possibility
					labelTemperatureMin.setEnabled(true);
					txtTemperatureMin.setEnabled(true);
					labelTemperatureMax.setEnabled(true);
					txtTemperatureMax.setEnabled(true);
					labelStepTemperature.setEnabled(true);
					txtStepTemperature.setEnabled(true);
					temperatureRadio2.setEnabled(true);
					
					
					pressureRadio2.setSelected(true); // radio2 selected now

					txtPressureMin.setEnabled(true);
					txtPressureMax.setEnabled(true);
					txtStepPressure.setEnabled(true);

					labelPressureMin.setEnabled(true);
					labelPressureMax.setEnabled(true);
					labelStepPressure.setEnabled(true);

					// put loaded values for temperature and pressure range 
					txtPressureMin.setText(String.valueOf(Maths.format(
							Session.getCurrentSession()
									.getUnitSystem()
									.convertToPressureUnit(
											workSession.getPressureMin()),
							"0.00")));
					txtPressureMax.setText(String.valueOf(Maths.format(
							Session.getCurrentSession()
									.getUnitSystem()
									.convertToPressureUnit(
											workSession.getPressureMax()),
							"0.00")));
					txtStepPressure.setText(String.valueOf(Maths.format(
							Session.getCurrentSession()
									.getUnitSystem()
									.convertToPressureUnit(
											workSession.getStepPressure()),
							"0.00")));
					txtTemperatureMin.setText(String.valueOf(Maths.format(
							Session.getCurrentSession()
									.getUnitSystem()
									.convertToTemperatureUnit(
											workSession.getTemperatureMin()),
							"0.00")));
					txtTemperatureMax.setText(String.valueOf(Maths.format(
							Session.getCurrentSession()
									.getUnitSystem()
									.convertToTemperatureUnit(
											workSession.getTemperatureMax()),
							"0.00")));

					double tStepTemporary = Session
							.getCurrentSession()
							.getUnitSystem()
							.convertToTemperatureUnit(
									0.0 + workSession.getStepTemperature());
					tStepTemporary = tStepTemporary
							- Session.getCurrentSession().getUnitSystem()
									.convertToTemperatureUnit(0.0);
					txtStepTemperature.setText(String.valueOf(Maths.format(
							tStepTemporary, "0.00")));

				} // end of if Pmin!=Pmax and Tmin!=Tmax
				
				

				if (workSession.getSessionContent().size() != 0) {
					dataRefresh.setEnabled(true);
					saveResults.setEnabled(true);
					workSession.displayResults();
				} // end of if (workSession.getSessionContent().size() != 0

			} // end of try

			catch (CancelException error) {resetCalculation();}
			catch (IOException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();}
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}
			
		} // end of if (source==open...

		// Save management
		if (source == save) {

			try {
				workSession.save(filename);
			}

			catch (IOException error) {} 
			catch (IllegalDataException error) {}
		}

		// Save_as management
		if (source == save_as) {
			try {
				File temporyFileName = KisthepDialog.requireOutputFilename(
						"Session filename", new KisthepSessionFileFilter());
				filename = temporyFileName.getAbsolutePath();
				// correction: 11/01/2009
				// check the filename suffix
				if (filename.indexOf('.') == -1) {
					workSession.save(filename + ".kstp");
				} else {
					workSession.save(filename);
				} // end of if
				tJMenuItemFileInactives[saveIndex].setEnabled(true);

			} // end of try

			catch (CancelException error) {resetCalculation();}
			catch (IOException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();}
		}

		// Close management
		if (source == close) {
			if (Session.getToBeSaveFlag() == true) {
				int rep = JOptionPane.showConfirmDialog(null,
						"This session is not saved, do you want to continue ?",
						"Close", JOptionPane.WARNING_MESSAGE);
				if (rep == JOptionPane.YES_OPTION) {

					refreshFillKisthepContentPane();
					fileItemsetEnabled(false);
					calculationItemsetEnabled(false);
					New.setEnabled(true);
					open.setEnabled(true);
					workSession.calculationErase();
					workSession = null;
					saveResults.setEnabled(false);
					dataRefresh.setEnabled(false);
					sessions.remove(session1);
					// disable T and P in Units menu
					temperatureMenu.setEnabled(false);
					pressureMenu.setEnabled(false);

				} // end of the "save the session"
			} // end of if then "to  be saved"
			else {

				int r = JOptionPane.showConfirmDialog(null,
						"Do you want to close this session?", "Close",
						JOptionPane.YES_NO_OPTION);
				if (r == JOptionPane.YES_OPTION) {

					refreshFillKisthepContentPane();
					fileItemsetEnabled(false);
					calculationItemsetEnabled(false);// end modif
					New.setEnabled(true);
					open.setEnabled(true);
					workSession.calculationErase();
					workSession = null;

					dataRefresh.setEnabled(false);
					saveResults.setEnabled(false);
					sessions.remove(session1);
					// disable T and P in Units menu
					temperatureMenu.setEnabled(false);
					pressureMenu.setEnabled(false);

				} // end of if (r==JOptionPane.YES_OPTION) {
			} // end of else "to be saved"

			// refresh the overall JFrame
			getContentPane().validate();
			getContentPane().repaint();

			// to update the temperature "menu"
			txtTemperature.setText("");
			txtTemperature.setEnabled(false);
			labelTemperature.setEnabled(false);
			temperatureRadio1.setSelected(true); // default: radio1 selected
			temperatureRadio1.setEnabled(false);

			// to update the pressure "menu"
			txtPressure.setText("");
			txtPressure.setEnabled(false);
			labelPressure.setEnabled(false);
			pressureRadio1.setSelected(true); // default: radio1 selected
			pressureRadio1.setEnabled(false);

			// to update the scaling factor "menu"

			txtScalingFactor.setText("");
			txtScalingFactor.setEnabled(false);
			labelScalingFactor.setEnabled(false);

			// to update the temperature range "menu"

			temperatureRadio2.setEnabled(false);
			labelTemperatureMin.setEnabled(false);
			labelTemperatureMax.setEnabled(false);
			labelStepTemperature.setEnabled(false);
			txtTemperatureMin.setText("");
			txtTemperatureMax.setText("");
			txtStepTemperature.setText("");
			txtTemperatureMin.setEnabled(false);
			txtTemperatureMax.setEnabled(false);
			txtStepTemperature.setEnabled(false);

			// to update the pressure range "menu"

			pressureRadio2.setEnabled(false);
			labelPressureMin.setEnabled(false);
			labelPressureMax.setEnabled(false);
			labelStepPressure.setEnabled(false);
			txtPressureMin.setText("");
			txtPressureMax.setText("");
			txtStepPressure.setText("");
			txtPressureMin.setEnabled(false);
			txtPressureMax.setEnabled(false);
			txtStepPressure.setEnabled(false);

		} // end of "if source ==close"

		// New management
		if (source == New) {
			try {
				workSession = new Session();

				// setenable T and P in Units menu
				temperatureMenu.setEnabled(true);
				pressureMenu.setEnabled(true);

				temperatureSelectOnly(kelvinMenu);

				pressureSelectOnly(barMenu);
				// refresh temperature label
				labelTemperature.setText("Temp ("
						+ workSession.getUnitSystem().getTemperatureSymbol() + ")");

				// refresh pressure label
				labelPressure.setText("Pressure ("
						+ workSession.getUnitSystem().getPressureSymbol() + ")");

				labelScalingFactor.setEnabled(true);
				txtScalingFactor.setText(String.valueOf(Maths.format(
						Constants.defaultScalingFactor, "0.00")));
				txtScalingFactor.setEnabled(true);

				txtTemperature.setText(String.valueOf(Maths.format(Session
						.getCurrentSession().getUnitSystem()
						.convertToTemperatureUnit(Constants.roomTemperature),
						"0.00")));
				txtTemperature.setEnabled(true);
				labelTemperature.setEnabled(true);

				temperatureRadio1.setEnabled(true); // default (selected)
				temperatureRadio2.setEnabled(true);
				txtTemperatureMin.setText(String.valueOf(Maths.format(Session
						.getCurrentSession().getUnitSystem()
						.convertToTemperatureUnit(Constants.tMinDefault), "0.00")));
				txtTemperatureMax
				.setText(String.valueOf(Maths.format(
						Session.getCurrentSession()
						.getUnitSystem()
						.convertToTemperatureUnit(
								Constants.tMaxDefault + Constants.nStepDefault
								* Constants.tStepDefault),
								"0.00")));
				double tStepTemporary = Session.getCurrentSession().getUnitSystem()
						.convertToTemperatureUnit(0.0 + Constants.tStepDefault);
				tStepTemporary = tStepTemporary
						- Session.getCurrentSession().getUnitSystem()
						.convertToTemperatureUnit(0.0);
				txtStepTemperature.setText(String.valueOf(Maths.format(
						tStepTemporary, "0.00")));

				txtPressure.setText(String.valueOf(Maths.format(
						Session.getCurrentSession().getUnitSystem()
						.convertToPressureUnit(Constants.P0), "0.00")));
				txtPressure.setEnabled(true);
				labelPressure.setEnabled(true);

				pressureRadio1.setEnabled(true); // default (selected)
				pressureRadio2.setEnabled(true);
				txtPressureMin.setText(String.valueOf(Maths.format(
						Session.getCurrentSession().getUnitSystem()
						.convertToPressureUnit(Constants.P0), "0.00")));
				txtPressureMax.setText(String.valueOf(Maths.format(
						Session.getCurrentSession().getUnitSystem()
						.convertToPressureUnit(2 * Constants.P0), "0.00")));
				txtStepPressure.setText(String.valueOf(Maths.format(
						Session.getCurrentSession().getUnitSystem()
						.convertToPressureUnit(Constants.pStepDefault),
						"0.00")));

				fileItemsetEnabled(true);
				save.setEnabled(false);
				print.setEnabled(false);
				New.setEnabled(false);
				open.setEnabled(false);
				resetSetEnabled(false);
				session1 = newJMenuItem("Session 1", sessions);

			}// end try
			catch (runTimeException err){resetCalculation();}



		} // end of if source == new


		/*****************************************************************/
		/* E V E N E M E N T S F O R */
		/* D A T A B U I L D I N G M E N U */
		/*****************************************************************/

		/*****************************************************************/
		/* E V E N E M E N T S F O R */
		/* C A L C U L A T I O N M E N U */
		/*****************************************************************/

		/***************************/
		/* Molecule management */
		/*************************/
		if (source == molec) {

			if (centralPane != null) {
				centralPane.removeAll();
			}
			
			
			

			try {
				workSession.getFilesToBeRead().add("the system");
				workSession.add(new InertStatisticalSystem(workSession.getTemperatureMin(), workSession.getPressureMin()));
				dataRefresh.setEnabled(true);
				
				
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}
				
				resetSetEnabled(true);
				saveInputs.setEnabled(true);
				reactPathBuild.setEnabled(false);

				// display results
				workSession.displayResults();

			} 
			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}


		}// end of source==molec

		/***************************/
		/* Equilibrium management */
		/*************************/

		if (source == keq) {
			centralPane.removeAll();
			resetSetEnabled(true);

			try {

				workSession.add(new Equilibrium(workSession.getTemperatureMin(), workSession.getPressureMin())); // note that here, we give a pressure 
                                                                                                                // parameter; P will be that of the pressure field
				dataRefresh.setEnabled(true);
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}

				reactPathBuild.setEnabled(false);

				// display results
				workSession.displayResults();


			} // end of try

			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}



		} // end of if (equi...)

		/***************************/
		/* Bimolecular/tst */
		/*************************/
		if (source == bimolTst) {
			centralPane.removeAll();
			resetSetEnabled(true);

			try {
				workSession.add(new BiMolecularReaction(workSession.getTemperatureMin(), "tst")); // note that here, we don't give any pressure 
				                                                                                  // parameter; P will be = P0 (in Reaction Class)
				// since pressure is necessary P0 at the beginning of this calculation, we have to 
				// update the pressure of the current session content to P0 (and also the txt display in the pressure field)	
				// indeed, the user may have changed the pressure in the previous calculation before reset
				
				// testing the mode (pressure value or pressure range)
				
				if (workSession.getPressureMin()==workSession.getPressureMax()) // we are in mode single pressure
				{
					txtPressure.setText(String.valueOf(Maths.format(Session.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.P0), "0.00")));

					try {   // remind that the pressure parameter in method setPressure must always be given as a number in the user unit !!
						// the method setpressure will convert it in the Kisthelp unit for pressure (Pa)
						workSession.setPressureMin(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
						workSession.setPressureMax(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
					}

					catch (PressureException error) {
						JOptionPane.showMessageDialog(null,"Warning : The Pressure must be positive");						
					}					
					catch (runTimeException error) {resetCalculation();}
					catch (IllegalDataException error) {resetCalculation();}
					
				}
				
				
				dataRefresh.setEnabled(true);
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}

				reactPathBuild.setEnabled(false);

				// display results
				workSession.displayResults();

			} 
			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}


		}// end of source== bimolTst

		

		/***************************/
		/* Bimolecular/tst_w */
		/*************************/
		if (source == bimolTst_w) {

			centralPane.removeAll();
			resetSetEnabled(true);

			try {
				workSession.add(new BiMolecularReaction(workSession.getTemperatureMin(), "tst_w"));
				
				// note that here, we don't give any pressure 
				// parameter; P will be = P0 (in Reaction Class)
				// since pressure is necessary P0 at the beginning of this calculation, we have to 
				// update the pressure of the current session content to P0 (and also the txt display in the pressure field)	
				// indeed, the user may have changed the pressure in the previous calculation before reset

				// testing the mode (pressure value or pressure range)

				if (workSession.getPressureMin()==workSession.getPressureMax()) // we are in mode single pressure
				{
					txtPressure.setText(String.valueOf(Maths.format(Session.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.P0), "0.00")));

					try {   // remind that the pressure parameter in method setPressure must always be given as a number in the user unit !!
						// the method setpressure will convert it in the Kisthelp unit for pressure (Pa)
						workSession.setPressureMin(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
						workSession.setPressureMax(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
					}

					catch (PressureException error) {
						JOptionPane.showMessageDialog(null,"Warning : The Pressure must be positive");						
					}					
					catch (runTimeException error) {resetCalculation();}
					catch (IllegalDataException error) {resetCalculation();}

				}
				
				
				
				dataRefresh.setEnabled(true);
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}

				reactPathBuild.setEnabled(false);

				// display results
				workSession.displayResults();

			} 
			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}



		}// end of source == bimolTst_w

		/***************************/
		/* Bimolecular/tst_eck */
		/*************************/
		if (source == bimolTst_eck) {

			centralPane.removeAll();
			resetSetEnabled(true);

			try {
				workSession.add(new BiMolecularReaction(workSession.getTemperatureMin(), "tst_eck"));
				
				
				// note that here, we don't give any pressure 
				// parameter; P will be = P0 (in Reaction Class)
				// since pressure is necessary P0 at the beginning of this calculation, we have to 
				// update the pressure of the current session content to P0 (and also the txt display in the pressure field)	
				// indeed, the user may have changed the pressure in the previous calculation before reset

				// testing the mode (pressure value or pressure range)

				if (workSession.getPressureMin()==workSession.getPressureMax()) // we are in mode single pressure
				{
					txtPressure.setText(String.valueOf(Maths.format(Session.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.P0), "0.00")));

					try {   // remind that the pressure parameter in method setPressure must always be given as a number in the user unit !!
						// the method setpressure will convert it in the Kisthelp unit for pressure (Pa)
						workSession.setPressureMin(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
						workSession.setPressureMax(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
					}

					catch (PressureException error) {
						JOptionPane.showMessageDialog(null,"Warning : The Pressure must be positive");						
					}					
					catch (runTimeException error) {resetCalculation();}
					catch (IllegalDataException error) {resetCalculation();}

				}
				
				
				dataRefresh.setEnabled(true);
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}

				reactPathBuild.setEnabled(false);

				// display results
				workSession.displayResults();

			} 
			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}



		}// end of source == bimolTst_eck

		/***************************/
		/* Bimolecular/vtst */
		/*************************/
		if (source == bimolVtst) {

			centralPane.removeAll();
			resetSetEnabled(true);

			try {
				workSession.add(new BiMolecularReaction(workSession.getTemperatureMin(), "vtst"));
				
				// note that here, we don't give any pressure 
				// parameter; P will be = P0 (in Reaction Class)
				// since pressure is necessary P0 at the beginning of this calculation, we have to 
				// update the pressure of the current session content to P0 (and also the txt display in the pressure field)	
				// indeed, the user may have changed the pressure in the previous calculation before reset

				// testing the mode (pressure value or pressure range)

				if (workSession.getPressureMin()==workSession.getPressureMax()) // we are in mode single pressure
				{
					txtPressure.setText(String.valueOf(Maths.format(Session.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.P0), "0.00")));

					try {   // remind that the pressure parameter in method setPressure must always be given as a number in the user unit !!
						// the method setpressure will convert it in the Kisthelp unit for pressure (Pa)
						workSession.setPressureMin(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
						workSession.setPressureMax(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
					}

					catch (PressureException error) {
						JOptionPane.showMessageDialog(null,"Warning : The Pressure must be positive");						
					}					
					catch (runTimeException error) {resetCalculation();}
					catch (IllegalDataException error) {resetCalculation();}

				}
				
				
				dataRefresh.setEnabled(true);
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}

				reactPathBuild.setEnabled(false);

				// display results
				workSession.displayResults();

			} 
			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}



		}// end of source==bimolVtst

		/***************************/
		/* Bimolecular/vtst_w */
		/*************************/
		if (source == bimolVtst_w) {

			centralPane.removeAll();
			resetSetEnabled(true);

			try {
				workSession.add(new BiMolecularReaction(workSession.getTemperatureMin(), "vtst_w"));
				
				// note that here, we don't give any pressure 
				// parameter; P will be = P0 (in Reaction Class)
				// since pressure is necessary P0 at the beginning of this calculation, we have to 
				// update the pressure of the current session content to P0 (and also the txt display in the pressure field)	
				// indeed, the user may have changed the pressure in the previous calculation before reset

				// testing the mode (pressure value or pressure range)

				if (workSession.getPressureMin()==workSession.getPressureMax()) // we are in mode single pressure
				{
					txtPressure.setText(String.valueOf(Maths.format(Session.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.P0), "0.00")));

					try {   // remind that the pressure parameter in method setPressure must always be given as a number in the user unit !!
						// the method setpressure will convert it in the Kisthelp unit for pressure (Pa)
						workSession.setPressureMin(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
						workSession.setPressureMax(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
					}

					catch (PressureException error) {
						JOptionPane.showMessageDialog(null,"Warning : The Pressure must be positive");						
					}					
					catch (runTimeException error) {resetCalculation();}
					catch (IllegalDataException error) {resetCalculation();}

				}
				
				
				dataRefresh.setEnabled(true);
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}

				reactPathBuild.setEnabled(false);

				// display results
				workSession.displayResults();

			} 
			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}



		}// end of source==bimolVtst_w

		/***************************/
		/* Bimolecular/vtst_eck */
		/*************************/
		if (source == bimolVtst_eck) {

			centralPane.removeAll();
			resetSetEnabled(true);

			try {
				workSession.add(new BiMolecularReaction(workSession.getTemperatureMin(), "vtst_eck"));
				
				
				// note that here, we don't give any pressure 
				// parameter; P will be = P0 (in Reaction Class)
				// since pressure is necessary P0 at the beginning of this calculation, we have to 
				// update the pressure of the current session content to P0 (and also the txt display in the pressure field)	
				// indeed, the user may have changed the pressure in the previous calculation before reset

				// testing the mode (pressure value or pressure range)

				if (workSession.getPressureMin()==workSession.getPressureMax()) // we are in mode single pressure
				{
					txtPressure.setText(String.valueOf(Maths.format(Session.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.P0), "0.00")));

					try {   // remind that the pressure parameter in method setPressure must always be given as a number in the user unit !!
						// the method setpressure will convert it in the Kisthelp unit for pressure (Pa)
						workSession.setPressureMin(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
						workSession.setPressureMax(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
					}

					catch (PressureException error) {
						JOptionPane.showMessageDialog(null,"Warning : The Pressure must be positive");						
					}					
					catch (runTimeException error) {resetCalculation();}
					catch (IllegalDataException error) {resetCalculation();}

				}
				
				
				dataRefresh.setEnabled(true);
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}

				reactPathBuild.setEnabled(false);

				// display results
				workSession.displayResults();

			} 
			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}



		}// end of source==bimolVtst_eck

		/***************************/
		/* Unimolecular/tst */
		/*************************/
		if (source == unimolTst) {

			centralPane.removeAll();
			resetSetEnabled(true);
			

			try {
				workSession.add(new UnimolecularReaction(workSession.getTemperatureMin(), "tst"));
				
				// note that here, we don't give any pressure 
				// parameter; P will be = P0 (in Reaction Class)
				// since pressure is necessary P0 at the beginning of this calculation, we have to 
				// update the pressure of the current session content to P0 (and also the txt display in the pressure field)	
				// indeed, the user may have changed the pressure in the previous calculation before reset

				// testing the mode (pressure value or pressure range)

				if (workSession.getPressureMin()==workSession.getPressureMax()) // we are in mode single pressure
				{
					txtPressure.setText(String.valueOf(Maths.format(Session.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.P0), "0.00")));

					try {   // remind that the pressure parameter in method setPressure must always be given as a number in the user unit !!
						// the method setpressure will convert it in the Kisthelp unit for pressure (Pa)
						workSession.setPressureMin(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
						workSession.setPressureMax(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
					}

					catch (PressureException error) {
						JOptionPane.showMessageDialog(null,"Warning : The Pressure must be positive");						
					}					
					catch (runTimeException error) {resetCalculation();}
					catch (IllegalDataException error) {resetCalculation();}

				}
				
				
				dataRefresh.setEnabled(true);
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}

				reactPathBuild.setEnabled(false);

				// display results
				workSession.displayResults();

				
			} 
			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}

		} // end of source==unimolTst

		/***************************/
		/* Unimolecular/tst_w */
		/*************************/
		if (source == unimolTst_w) {

			centralPane.removeAll();
			resetSetEnabled(true);

			try {
				workSession.add(new UnimolecularReaction(workSession.getTemperatureMin(), "tst_w"));
				
				
				// note that here, we don't give any pressure 
				// parameter; P will be = P0 (in Reaction Class)
				// since pressure is necessary P0 at the beginning of this calculation, we have to 
				// update the pressure of the current session content to P0 (and also the txt display in the pressure field)	
				// indeed, the user may have changed the pressure in the previous calculation before reset

				// testing the mode (pressure value or pressure range)

				if (workSession.getPressureMin()==workSession.getPressureMax()) // we are in mode single pressure
				{
					txtPressure.setText(String.valueOf(Maths.format(Session.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.P0), "0.00")));

					try {   // remind that the pressure parameter in method setPressure must always be given as a number in the user unit !!
						// the method setpressure will convert it in the Kisthelp unit for pressure (Pa)
						workSession.setPressureMin(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
						workSession.setPressureMax(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
					}

					catch (PressureException error) {
						JOptionPane.showMessageDialog(null,"Warning : The Pressure must be positive");						
					}					
					catch (runTimeException error) {resetCalculation();}
					catch (IllegalDataException error) {resetCalculation();}

				}
				
				dataRefresh.setEnabled(true);
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}

				reactPathBuild.setEnabled(false);

				// display results
				workSession.displayResults();

			} 
			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}



		} // end of source==unimolTst_w

		/***************************/
		/* Unimolecular/tst_eck */
		/*************************/
		if (source == unimolTst_eck) {

			centralPane.removeAll();
			resetSetEnabled(true);

			try {
				workSession.add(new UnimolecularReaction(workSession.getTemperatureMin(), "tst_eck"));
				
				// note that here, we don't give any pressure 
				// parameter; P will be = P0 (in Reaction Class)
				// since pressure is necessary P0 at the beginning of this calculation, we have to 
				// update the pressure of the current session content to P0 (and also the txt display in the pressure field)	
				// indeed, the user may have changed the pressure in the previous calculation before reset

				// testing the mode (pressure value or pressure range)

				if (workSession.getPressureMin()==workSession.getPressureMax()) // we are in mode single pressure
				{
					txtPressure.setText(String.valueOf(Maths.format(Session.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.P0), "0.00")));

					try {   // remind that the pressure parameter in method setPressure must always be given as a number in the user unit !!
						// the method setpressure will convert it in the Kisthelp unit for pressure (Pa)
						workSession.setPressureMin(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
						workSession.setPressureMax(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
					}

					catch (PressureException error) {
						JOptionPane.showMessageDialog(null,"Warning : The Pressure must be positive");						
					}					
					catch (runTimeException error) {resetCalculation();}
					catch (IllegalDataException error) {resetCalculation();}

				}
				
				
				dataRefresh.setEnabled(true);
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}

				reactPathBuild.setEnabled(false);


				// display results
				workSession.displayResults();

			} 
			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}



		} // end of source==unimolTst_eck

		/***************************/
		/* Unimolecular/vtst */
		/*************************/
		if (source == unimolVtst) {
			centralPane.removeAll();
			resetSetEnabled(true);

			try {
				workSession.add(new UnimolecularReaction(workSession.getTemperatureMin(), "vtst"));
				
				// note that here, we don't give any pressure 
				// parameter; P will be = P0 (in Reaction Class)
				// since pressure is necessary P0 at the beginning of this calculation, we have to 
				// update the pressure of the current session content to P0 (and also the txt display in the pressure field)	
				// indeed, the user may have changed the pressure in the previous calculation before reset

				// testing the mode (pressure value or pressure range)

				if (workSession.getPressureMin()==workSession.getPressureMax()) // we are in mode single pressure
				{
					txtPressure.setText(String.valueOf(Maths.format(Session.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.P0), "0.00")));

					try {   // remind that the pressure parameter in method setPressure must always be given as a number in the user unit !!
						// the method setpressure will convert it in the Kisthelp unit for pressure (Pa)
						workSession.setPressureMin(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
						workSession.setPressureMax(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
					}

					catch (PressureException error) {
						JOptionPane.showMessageDialog(null,"Warning : The Pressure must be positive");						
					}					
					catch (runTimeException error) {resetCalculation();}
					catch (IllegalDataException error) {resetCalculation();}

				}
				
				dataRefresh.setEnabled(true);
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}

				reactPathBuild.setEnabled(false);


				// display results
				workSession.displayResults();

			} 
			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}


		} // end of if source==unimolVtst

		/***************************/
		/* Unimolecular/vtst_w */
		/*************************/
		if (source == unimolVtst_w) {
			centralPane.removeAll();
			resetSetEnabled(true);

			try {
				workSession.add(new UnimolecularReaction(workSession.getTemperatureMin(), "vtst_w"));
				
				// note that here, we don't give any pressure 
				// parameter; P will be = P0 (in Reaction Class)
				// since pressure is necessary P0 at the beginning of this calculation, we have to 
				// update the pressure of the current session content to P0 (and also the txt display in the pressure field)	
				// indeed, the user may have changed the pressure in the previous calculation before reset

				// testing the mode (pressure value or pressure range)

				if (workSession.getPressureMin()==workSession.getPressureMax()) // we are in mode single pressure
				{
					txtPressure.setText(String.valueOf(Maths.format(Session.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.P0), "0.00")));

					try {   // remind that the pressure parameter in method setPressure must always be given as a number in the user unit !!
						// the method setpressure will convert it in the Kisthelp unit for pressure (Pa)
						workSession.setPressureMin(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
						workSession.setPressureMax(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
					}

					catch (PressureException error) {
						JOptionPane.showMessageDialog(null,"Warning : The Pressure must be positive");						
					}					
					catch (runTimeException error) {resetCalculation();}
					catch (IllegalDataException error) {resetCalculation();}

				}
				
				dataRefresh.setEnabled(true);
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}

				reactPathBuild.setEnabled(false);


				// display results
				workSession.displayResults();

			} 
			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}


		} // end of if source==unimolVtst_w

		/***************************/
		/* Unimolecular/vtst_eck */
		/*************************/
		if (source == unimolVtst_eck) {
			centralPane.removeAll();
			resetSetEnabled(true);

			try {
				workSession.add(new UnimolecularReaction(workSession.getTemperatureMin(), "vtst_eck"));
				
				
				// note that here, we don't give any pressure 
				// parameter; P will be = P0 (in Reaction Class)
				// since pressure is necessary P0 at the beginning of this calculation, we have to 
				// update the pressure of the current session content to P0 (and also the txt display in the pressure field)	
				// indeed, the user may have changed the pressure in the previous calculation before reset

				// testing the mode (pressure value or pressure range)

				if (workSession.getPressureMin()==workSession.getPressureMax()) // we are in mode single pressure
				{
					txtPressure.setText(String.valueOf(Maths.format(Session.getCurrentSession().getUnitSystem()
							.convertToPressureUnit(Constants.P0), "0.00")));

					try {   // remind that the pressure parameter in method setPressure must always be given as a number in the user unit !!
						// the method setpressure will convert it in the Kisthelp unit for pressure (Pa)
						workSession.setPressureMin(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
						workSession.setPressureMax(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(Constants.P0));
					}

					catch (PressureException error) {
						JOptionPane.showMessageDialog(null,"Warning : The Pressure must be positive");						
					}					
					catch (runTimeException error) {resetCalculation();}
					catch (IllegalDataException error) {resetCalculation();}

				}
				
				dataRefresh.setEnabled(true);
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}

				reactPathBuild.setEnabled(false);


				// display results
				workSession.displayResults();

			}
			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}


		} // end of if source==unimolVtst_eck

		/***************************/
		/* RESET */
		/*************************/
		if (source == reset) { 
			resetSetEnabled(false);

			refreshFillKisthepContentPane();
			workSession.calculationErase();
			saveResults.setEnabled(false); // the menu becomes grey
			saveInputs.setEnabled(false); // the menu becomes grey
			dataRefresh.setEnabled(false); // the menu becomes grey
			reactPathBuild.setEnabled(true);


			// refresh the overall JFrame
			getContentPane().validate();
			getContentPane().repaint();

		}

		/***************************/
		/* PAGE MANAGEMENT */
		/*************************/
		if (source == dataRefresh) {
			try {
			workSession.displayResults();
			}
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}

		}


		/***************************/
		/* S H O W C O N S T A N T S */
		/*************************/
		if (source == showConstants) {

			try {		
				// create an independant JFrame with kisthelp Constants inside
				JFrame constantsJFrame = new JFrame(); 			
				constantsJFrame.setLocation(Interface.getKisthepInterface().getLocation());
				constantsJFrame.setTitle("K i S T h e l P    c o n s t a n t s ");
				
				// Bar's instantiation
				JMenuBar constantsBar = new JMenuBar();
				constantsJFrame.setJMenuBar(constantsBar);
				
				ImageIcon icon;
				URL address =   getClass().getResource("/images/smallLogo.png");
				if (address == null) {
					String message = "Error in class Interface in constructor"+ Constants.newLine;
					message = message +  "File smallLogo.png not found"+ Constants.newLine;
					message = message +  "Please contact the authors"+ Constants.newLine;
					JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                      
				}
				else {
					icon = new ImageIcon(address); 
					JButton smallLogo = new JButton("");
					smallLogo.setIcon(icon);
					constantsBar.add(Box.createGlue());
					constantsBar.add(smallLogo);
				}

				
				
				
				

				/* --------------------T A B L E 1 ---------------------------------- */

				// create a JTable with constants			
				String[] columnNames1 = {
						"Constant",
						"  ",
						"Value"
				};


				// !!! to use html within String in JTable, String MUST START with <html> tag !!
				Object[][] data1 = {
						{"<html>Bohr radius (<i>a<sub>0</sub>)</i></html>", "=", Constants.a0+" m"},
						{"<html>1 Atomic mass unit (amu)</html>", "=", Constants.convertAmuToKg+" kg"},
						{"<html>Planck's constant (<i>h</i>)</html>", "=", Constants.h + " J.s"},
						{"<html> Avogadro's number (<i>N<sub>A</sub></i>) </html>", "=", Constants.NA},
						{"<html>1 cal. </html>", "=", Constants.convertCalToJoule + " J"},
						{"<html>1 Hartree (<i>E<sub>h</sub></i>)</html>", "=", Constants.convertHartreeToJoule_perMolec + " J"},
						{"<html>Speed of light in vacuum (<i>c<i>)</html> ", "=", "<html> " + Constants.c+" &nbsp; m.s<sup>-1</sup></html>"},
						{"<html>Boltzman constant (<i>k</i>)<html>", "=", "<html> " +Constants.kb + "&nbsp; J.K<sup>-1</sup></html>"},

				};

				JTable table1 = new JTable(data1, columnNames1);	
				table1.setRowHeight(21);

				table1.setFont(new Font("Verdana", Font.PLAIN, 12));

				TableColumn column = null;
				column = table1.getColumnModel().getColumn(0);
				column.setPreferredWidth(180); 
				kisthelpJTable.alignRight(table1, 0);

				column = table1.getColumnModel().getColumn(1);
				column.setPreferredWidth(10); 
				kisthelpJTable.alignCenter(table1, 1);

				column = table1.getColumnModel().getColumn(2);
				column.setPreferredWidth(180); 
				kisthelpJTable.alignLeft(table1, 2);

				TableCellRenderer rendererFromHeader1 = table1.getTableHeader().getDefaultRenderer();
				JLabel headerLabel1 = (JLabel) rendererFromHeader1;
				headerLabel1.setHorizontalAlignment(JLabel.CENTER);

				/* --------------------T A B L E 2 ---------------------------------- */


				// create a JTable with constants			
				String[] columnNames2 = {
						"Common conversion factors",
						"  ",
						"Value"
				};


				// !!! to use html within String in JTable, String MUST START with <html> tag !!
				Object[][] data2 = {

						{"1 Hartree", "=", "<html>" + Maths.format(Constants.convertHartreeToJoule/1000, "0.00")+" &nbsp; kJ.mol<sup>-1</sup></html>"},
						{"1 Hartree", "=", "<html>" + Maths.format(Constants.convertHartreeToJoule/(1000*Constants.convertCalToJoule), "0.00")+" &nbsp; kcal.mol<sup>-1</sup></html>"},
						{"<html> 1 cm<sup>-1 </html>", "=", Maths.format(Constants.convertCm_1ToKelvin, "0.00000")+" K"},
						{"1 GHz", "=", "<html>" + Maths.format(Constants.convertGHzToAmuBohr2, "0.00") + " &nbsp; amu.bohr<sup>2</sup></html>"},
						{"1 Torr", "=", Constants.convertTorrToPa + " Pa"},
						{"1 Atm.", "=", Constants.convertAtmToPa + " Pa"}
				};

				JTable table2 = new JTable(data2, columnNames2);	
				table2.setRowHeight(21);

				table2.setFont(new Font("Verdana", Font.PLAIN, 12));

				column = table2.getColumnModel().getColumn(0);
				column.setPreferredWidth(180); 
				kisthelpJTable.alignRight(table2, 0);

				column = table2.getColumnModel().getColumn(1);
				column.setPreferredWidth(10); 
				kisthelpJTable.alignCenter(table2, 1);

				column = table2.getColumnModel().getColumn(2);
				column.setPreferredWidth(180); 
				kisthelpJTable.alignLeft(table2, 2);

				TableCellRenderer rendererFromHeader2 = table2.getTableHeader().getDefaultRenderer();
				JLabel headerLabel2 = (JLabel) rendererFromHeader2;
				headerLabel2.setHorizontalAlignment(JLabel.CENTER);


				// set a gridLayout
				GridLayout gl = new GridLayout(2,1);
				constantsJFrame.setLayout(gl);


				// add table to JScrollPane		   
				constantsJFrame.add(new JScrollPane(table1));
				constantsJFrame.add(new JScrollPane(table2));

				constantsJFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				constantsJFrame.setBackground(Color.WHITE);
				constantsJFrame.setSize(400,450);
				//constantsJFrame.pack();
				constantsJFrame.setVisible(true);	

			} // end of try
			catch (runTimeException err){resetCalculation();}


		} // end of if (source == showConstants)
		

		
		/***************************/
		/* L E N N A R D J O N E S  */
		/*************************/
		if (source == lennardJones) {

			
			// create an independant JFrame with some lennardJones parameters inside, recommended from litterature
			// F. M. Mourits et al., Can. J. Chem. vol 55 , 1977. 
			JFrame constantsJFrame = new JFrame(); 
			constantsJFrame.setLayout(new FlowLayout(FlowLayout.LEFT));
			constantsJFrame.setLocation(Interface.getKisthepInterface().getLocation());
			constantsJFrame.setTitle("L e n n a r d - J o n e s  parameters");

			// Bar's instantiation
			JMenuBar constantsBar = new JMenuBar();
			constantsJFrame.setJMenuBar(constantsBar);
			
			ImageIcon icon;
			URL address =   getClass().getResource("/images/smallLogo.png");
			if (address == null) {
				String message = "Error in class Interface in constructor"+ Constants.newLine;
				message = message +  "File smallLogo.png not found"+ Constants.newLine;
				message = message +  "Please contact the authors"+ Constants.newLine;
				JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                      
			}
			else {
				icon = new ImageIcon(address); 
				JButton smallLogo = new JButton("");
				smallLogo.setIcon(icon);
				constantsBar.add(Box.createGlue());
				constantsBar.add(smallLogo);
			}


			
			
			/* --------------------T A B L E ---------------------------------- */
			
			// create a JTable with constants			
			String[] columnNames = {
					"Chemical system",
					"\u03C3 (cm)",
					"\u03B5/k (K)",
					"Mass (g.mol\u207B\u2071)",
					"Ref."
			};
			
			
			// !!! to use html within String in JTable, String MUST START with <html> tag !!
			Object[][] data = {
					
					{"He", "2.64E-08", "10.9", "4.003", "(c)"},
					{"Ne", "2.822E-08", "32.0", "20.180", "(a)"},
					{"Ar", "3.465E-08", "113.5", "39.948", "(a)"},
					{"Kr", "3.662E-08", "178.0", "83.798", "(a)"},
					{"Xe", "4.050E-08", "230.2", "131.293", "(a)"},					
					{"<html>N<sub>2</sub></html>", "3.738E-08", "82.0", "28.013", "(a)"},
					{"<html>O<sub>2</sub></html>", "3.480E-08", "102.6", "31.999", "(a)"},
					{"<html>CO<sub>2</sub></html>", "3.943E-08", "200.9", "44.010", "(a)"},
					{"<html>CS<sub>2</sub></html>", "4.575E-08", "414.6", "76.141", "(a)"},
					{"<html> SF<sub>6</sub> </html>", "5.199E-08", "212.0", "146.055",  "(a)"},
					{"<html>(CN)<sub>2</sub></html>", "4.571E-08", "275.7", "52.035",  "(a)"},
					{"<html>(CN)<sub>2</sub></html>", "4.361E-08", "348.6", "52.035",  "(b)"},
					{"<html>F<sub>2</sub></html>", "3.439E-08", "152.1", "37.997",  "(a)"},
					{"<html>Cl<sub>2</sub></html>", "4.240E-08", "307.2", "70.906",  "(a)"},
					{"<html>Br<sub>2</sub></html>", "4.266E-08", "437.3", "159.808",  "(a)"},
					{"<html>I<sub>2</sub></html>", "4.630E-08", "577.4", "253.809",  "(a)"},
					{"<html>CH<sub>4</sub></html>", "3.790E-08", "142.1", "16.043",  "(a)"},
					{"<html>CH<sub>4</sub></html>", "3.758E-08", "148.6", "16.043",  "(b)"},
					{"<html>CF<sub>4</sub></html>", "4.486E-08", "167.3", "88.004",  "(a)"},
					{"<html>CCl<sub>4</sub></html>", "5.611E-08", "415.5", "153.823",  "(a)"},
					{"<html>C<sub>2</sub>H<sub>6</sub></html>", "4.407E-08", "227.9", "30.069",  "(a)"},
					{"<html>C<sub>2</sub>H<sub>6</sub></html>", "4.443E-08", "215.7", "30.069",  "(b)"},
					{"<html>C<sub>3</sub>H<sub>8</sub></html>", "5.114E-08", "237.2", "44.096",  "(a)"},
					{"<html>n-C<sub>4</sub>H<sub>10</sub></html>", "5.405E-08", "305.0", "58.122",  "(a)"},
					{"<html>i-C<sub>4</sub>H<sub>10</sub></html>", "5.392E-08", "295.8", "58.122",  "(a)"},
					{"<html>n-C<sub>5</sub>H<sub>12</sub></html>", "5.916E-08", "308.3", "72.149",  "(a)"},
					{"<html>neo-C<sub>5</sub>H<sub>12</sub></html>", "5.757E-08", "312.2", "72.149",  "(a)"},
					{"<html>n-C<sub>6</sub>H<sub>14</sub></html>", "6.269E-08", "341.3", "86.175",  "(a)"},
					{"<html>n-C<sub>7</sub>H<sub>16</sub></html>", "6.650E-08", "351.4", "100.202",  "(a)"},
					{"<html>n-C<sub>8</sub>H<sub>18</sub></html>", "7.024E-08", "357.7", "114.229",  "(a)"},
					{"<html>n-C<sub>9</sub>H<sub>20</sub></html>", "7.351E-08", "360.3", "128.255",  "(a)"},
					{"<html>cyclo-C<sub>6</sub>H<sub>12</sub></html>", "5.771E-08", "394.8", "84.159",  "(a)"},
					{"<html>C<sub>2</sub>H<sub>4</sub></html>", "4.155E-08", "225.6", "28.053",  "(a)"},
					{"<html>C<sub>3</sub>H<sub>6</sub></html>", "4.778E-08", "271.2", "42.080",  "(a)"},
					{"butene-1", "5.274E-08", "302.4", "56.106",  "(a)"},
					{"i-butene", "5.282E-08", "299.6", "56.106",  "(a)"},
					{"<html>C<sub>2</sub>H<sub>2</sub></html>", "4.078E-08", "221.4", "26.037",  "(a)"},
					{"<html>C<sub>3</sub>H<sub>4</sub></html>", "4.667E-08", "284.7", "40.064",  "(a)"},
					{"<html>C<sub>6</sub>H<sub>6</sub></html>", "5.455E-08", "401.2", "78.112",  "(a)"},
					{"<html>C<sub>6</sub>H<sub>5</sub>CH<sub>3</sub></html>", "5.923E-08", "407.8", "92.138",  "(a)"},
					{"Mesitylene", "6.760E-08", "400.5", "120.192",  "(a)"},
					{"<html>N<sub>2</sub>O<sub></html>", "3.776E-08", "248.8", "44.013",  "(a)"},
					{"<html>N<sub>2</sub>O<sub></html>", "3.828E-08", "232.4", "44.013",  "(b)"},
					{"CO", "3.698E-08", "104.5", "28.010",  "(a)"},
					{"COS", "4.424E-08", "286.4", "60.075",  "(a)"},
					{"NO", "3.489E-08", "117.2", "30.006",  "(a)"},
					{"<html>HNO<sub>3</sub></html>", "4.24E-08", "390", "63.013",  "(b)"},
					{"HI", "4.080E-08", "333.6", "127.912",  "(a)"},
					{"HBr", "3.852E-08", "281.1", "80.912",  "(a)"},
					{"HCl", "3.168E-08", "322.6", "36.461",  "(a)"},
					{"HF", "3.148E-08", "330.0", "20.006",  "(a)"},
					{"<html>H<sub>2</sub>S</html>", "3.733E-08", "302.4", "34.081",  "(a)"},
					{"<html>H<sub>2</sub>S</html>", "3.623E-08", "301.1", "34.081",  "(b)"},
					{"<html>SO<sub>2</sub></html>", "4.102E-08", "328.5", "64.064",  "(a)"},
					{"<html>SO<sub>2</sub></html>", "4.112E-08", "335.4", "64.064",  "(b)"},
					{"HCN", "3.630E-08", "569.1", "27.025",  "(a,b)"},
					{"<html>NH<sub>3</sub></html>", "3.215E-08", "309.9", "17.031",  "(a)"},
					{"<html>H<sub>2</sub>O</html>", "2.710E-08", "506.0", "18.015",  "(a)"},
					{"<html>H<sub>2</sub>O</html>", "2.641E-08", "809.1", "18.015",  "(b)"},
					{"<html>H<sub>2</sub>O<sub>2</sub></html>", "4.196E-08", "289.3", "34.015",  "(b)"},
					{"<html>CHCl<sub>3</sub></html>", "5.179E-08", "381.7", "119.378",  "(a)"},
					{"<html>CH<sub>2</sub>Cl<sub>2</sub></html>", "4.752E-08", "403.5", "84.933",  "(a)"},
					{"<html>CH<sub>3</sub>Br</html>", "4.306E-08", "416.2", "94.939",  "(a)"},
					{"<html>CH<sub>3</sub>Cl</html>", "4.296E-08", "328.1", "50.488",  "(a)"},
					{"<html>C<sub>2</sub>H<sub>5</sub>Cl</html>", "4.490E-08", "402.9", "64.514",  "(a)"},
					{"<html>n-C<sub>3</sub>H<sub>7</sub>OH</html>", "4.867E-08", "480.2", "60.095",  "(a)"},
					{"<html>i-C<sub>3</sub>H<sub>7</sub>OH</html>", "4.739E-08", "456.0", "60.095",  "(a)"},
					{"<html>C<sub>2</sub>H<sub>5</sub>OH</html>", "4.317E-08", "450.2", "46.068",  "(a)"},
					{"<html>CH<sub>3</sub>OH</html>", "3.657E-08", "385.2", "32.042",  "(a)"},
					{"<html>(C<sub>2</sub>H<sub>5</sub>)<sub>2</sub>O</html>", "5.553E-08", "348.0", "74.122",  "(a)"},
					{"<html>(CH<sub>3</sub>)<sub>2</sub>O</html>", "4.230E-08", "352.3", "46.068",  "(a)"},
					{"<html>CH<sub>3</sub>CO<sub>2</sub>C<sub>2</sub>H<sub>5</sub></html>", "5.650E-08", "372.0", "88.105",  "(a)"},
					{"<html>CH<sub>3</sub>CO<sub>2</sub>CH<sub>3</sub></html>", "5.141E-08", "389.4", "74.078",  "(a)"},
					{"<html>(CH<sub>3</sub>)<sub>2</sub>CO</html>", "4.599E-08", "458.0", "58.079",  "(a)"},
					{"<html>CH<sub>3</sub>CHO</html>", "4.332E-08", "413.5", "44.053",  "(a)"},
					{"<html>HO<sub>2</sub></html>", "4.196E-08", "289.3", "33.007",  "(b)"},
					{"ClNO", "4.112E-08", "395.3", "65.459",  "(b)"},
					{"<html>O<sub>3</sub></html>", "3.980E-08", "161.2", "47.998",  "(b)"},
					{"BrNO", "4.12E-08", "500", "109.910",  "(b)"},
					{"INO", "4.5E-08", "500", "156.911",  "(b)"},
					{"FNO", "4E-08", "380", "49.005",  "(b)"},
					{"HNO", "3.492E-08", "116.7", "31.014",  "(b)"},
					{"<html>NO<sub>3</sub></html>", "4.112E-08", "395", "62.005",  "(b)"},
					{"<html>INO<sub>2</sub></html>", "4.5E-08", "550", "172.910",  "(b)"}
					
		    };
			
			
		    
		   JTable table = new JTable(data, columnNames);	
		   table.setRowHeight(21);
		   table.setFont(new Font("Verdana", Font.PLAIN, 12));
		   
		   TableColumn column = null;
		   column = table.getColumnModel().getColumn(0);
		   column.setPreferredWidth(100); 
		   kisthelpJTable.alignRight(table, 0);
		   
		   column = table.getColumnModel().getColumn(1);
		   column.setPreferredWidth(80); 
		   kisthelpJTable.alignRight(table, 1);
		   
		   column = table.getColumnModel().getColumn(2);
		   column.setPreferredWidth(60); 
		   kisthelpJTable.alignRight(table, 2);
		   
		   column = table.getColumnModel().getColumn(3);
		   column.setPreferredWidth(80); 
		   kisthelpJTable.alignRight(table, 3);

		   
		   column = table.getColumnModel().getColumn(4);
		   column.setPreferredWidth(40); 
		   kisthelpJTable.alignCenter(table, 4);

		   
		   TableCellRenderer rendererFromHeader = table.getTableHeader().getDefaultRenderer();
		   JLabel headerLabel1 = (JLabel) rendererFromHeader;
		   headerLabel1.setHorizontalAlignment(JLabel.CENTER);

		   // create the references
		   JLabel reference1 = new JLabel("(a) F. M. Mourits and F. H. A. Rummens, Can. J. Chem. 55 (1977) 3007");
		   JLabel reference2 = new JLabel("(b) J. Troe, J. Chem. Phys. 66 (1977) 4758");
		   JLabel reference3 = new JLabel("(c) J. O. Hirschfelder,  C. F. Curtiss, and R. B. Bird, Molecular Theory of Gases and Liquids, Wiley, New York, p.1114 (1954)");

		   
		   // add table and references to JScrollPane		   
		   constantsJFrame.add(new JScrollPane(table));
		   constantsJFrame.add(reference1);
		   constantsJFrame.add(reference2);
		   constantsJFrame.add(reference3);

		   constantsJFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		   constantsJFrame.setBackground(Color.WHITE);
		   constantsJFrame.setSize(480,500);
		   constantsJFrame.setVisible(true);	
		   

		   
		   
		} // end of if (source == lennardJones)
		


		/******************************************************************/
		// S O U R C E == I N P U T / S A V E
		/******************************************************************/
		if (source == saveInputs) { // assuming only one object is in the current session
			
			File temporaryFile=null;
			String temporaryFileName="";
			ActionOnFileWrite writeOnKinp=null;
			
			// get the name of the class corresponding to the current object of the session
			String currentClassName = Session.getCurrentSession().getSessionContent().get(0).getClass().getName();
			
			// if  object of current session is InertStatistical, save inputs results in .kinp file
			 if (currentClassName.equalsIgnoreCase("InertStatisticalSystem")){
					// ask for new filename for the resulting kinp file
				 try {
					
					  temporaryFile = KisthepDialog.requireOutputFilename(".kinp filename ?",new KisthepOutputFileFilter(Constants.kInpFileType));

					temporaryFileName = temporaryFile.getAbsolutePath();
				 

					// check the filename suffix
					if (temporaryFileName.indexOf('.') == -1) {
						temporaryFileName = temporaryFileName + ".kinp";
					}

					// to prepare the writing action
					
						writeOnKinp = new ActionOnFileWrite(temporaryFileName);
						InertStatisticalSystem currentSystem;
						currentSystem = (InertStatisticalSystem) Session.getCurrentSession().getSessionContent().get(0);
						currentSystem.saveInputs(writeOnKinp);
						writeOnKinp.end();

					 
						
				 }// try end
				 catch (CancelException err) {}
				 catch (IOException err) {resetCalculation();}
				 catch (runTimeException err) {resetCalculation();}
				                                                 
				 
			 }// end of if (currentClassName.equalsIgnoreCase("InertStatisticalSystem"))
			 else {				 
					String message = "Error in class Interface in method ActionPerformed (if (source == saveInputs))"+ Constants.newLine;
					message = message +  "while trying to save inputs of an InertStatisticalSystem"+ Constants.newLine;
					message = message +  "Current object in session is not InertStatisticalSystem but" + Constants.newLine;
					message = message +  currentClassName + " ..."+ Constants.newLine;
					JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                  
					resetCalculation();					
			 }
			
			
		}
		// ---END--- S O U R C E == I N P U T / S A V E
		/******************************************************************/

		
		/***************************/
		/* SAVE RESULTS */
		/*************************/
		if (source == saveResults) {

			try {
				workSession.saveResults();
			}			
			catch (CancelException error) {resetCalculation();}  
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}


		} // end of if (source==saveResults

		/***************************/
		/* REACT PATH BUILDING */
		/*************************/
		if (source == reactPathBuild) {

			try {
				buildReactPath();
			}
			catch (CancelException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();}
			catch (IllegalDataException error) {resetCalculation();}


		} // end of if (source==saveResults

		/***************************/
		/* RRKM Tight TS */
		/*************************/

		if (source == rrkmTightTs) {
			centralPane.removeAll();
			resetSetEnabled(true);

			try {
				UnimolecularReaction reaction = new UnimolecularReaction(
						workSession.getTemperatureMin(),
						workSession.getPressureMin(), "rrkmTightTs");
				Session.getCurrentSession().add(reaction);
				// make available save and display results menus
				dataRefresh.setEnabled(true);
				// make available the results save menu if session is not empty AND !! if the single temperature or pressure range is invoked
				if ( (Session.getCurrentSession().getTemperatureMin()==Session.getCurrentSession().getTemperatureMax()) && 
					 (Session.getCurrentSession().getPressureMin()==Session.getCurrentSession().getPressureMax()) )
				{saveResults.setEnabled(true);}
				else {saveResults.setEnabled(false);}


				// display results in the central pane
				workSession.displayResults();
				


			} // end of try

			catch (CancelException error) {resetCalculation();} 
			catch (IllegalDataException error) {resetCalculation();} 
			catch (IOException error) {resetCalculation();} 
		    catch (runTimeException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}
          

		} // end of source==rrkmTightTs

		/***************************/
		/* KELVIN MENU */
		/*************************/
		if (source == kelvinMenu) {

			// the session has got all the properties in ISU, that is: T in
			// Kelvin, P in Pascal
			// but instantaneously changing to another unity causes the number
			// in Kelvin to change !

			// get back the temperature number in the current user units:
			// T(K)->T(current User Unit)
			// T in Kelvin must be given to the method convertToTemperatureUnit
			// that returns a number in the new Unit
			// Example1: if current unit is Celsius, if current T text is 27,
			// then, if user changes to Kelvin,
			// workSession.getTemperature is called that returns the number 300,
			// convertToTemperatureUnit(300) returns number 27
			// since the textfield is not available here, we retrieved this
			// value through the 2 methods
			// workSession.getTemperature/convertToTemperatureUnit
			// setTemperatureMin(27) converts 27 to 27K in Session, and refreshT
			// is called with T=27K!!

			// Example2: if current unit is Kelvin, if current T text is 300,
			// then, if user changes to Celsius,
			// workSession.getTemperature is called that returns the number 300,
			// convertToTemperatureUnit(300) returns number 300
			// since the textfield is not available here, we retrieved this
			// value through the 2 methods
			// workSession.getTemperature/convertToTemperatureUnit
			// setTemperatureMin(300) converts 300 to 573 in Session, and
			// refreshT is called with T=573K!!

			// get back the temperature in the current user units:
			// T(K)->T(current User Unit)
			double tMinInUserUnit = Session.getCurrentSession().getUnitSystem()
					.convertToTemperatureUnit(workSession.getTemperatureMin());
			double tMaxInUserUnit = Session.getCurrentSession().getUnitSystem()
					.convertToTemperatureUnit(workSession.getTemperatureMax());
			double tStepInUserUnit = Session
					.getCurrentSession()
					.getUnitSystem()
					.convertToTemperatureUnit(
							0.0 + workSession.getStepTemperature())
					- Session.getCurrentSession().getUnitSystem()
							.convertToTemperatureUnit(0.0);

			String oldTemperatureUnit = Session.getCurrentSession()
					.getUnitSystem().getCurrentTemperatureUnit();
			try {
				// change the current unit : T (New User Unit) = T(current User
				// Unit) (the number does not change itself !)

				workSession.getUnitSystem().changeTemperatureUnit("Kelvin");

				// change temperatures to new current unit: T(new User Unit) ->
				// T(K)

				workSession.setTemperatureMin(tMinInUserUnit);
				workSession.setTemperatureMax(tMaxInUserUnit);
				workSession.setStepTemperature(tStepInUserUnit);
				// unselect the other choices (celsius ...)
				temperatureSelectOnly(kelvinMenu);

				// refresh temperature label
				labelTemperature.setText("Temp ("
						+ workSession.getUnitSystem().getTemperatureSymbol()
						+ ")");
				
				// display results in new units
				workSession.displayResults();

				
			} catch (TemperatureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature (in K) must be positive");
				kelvinMenu.setState(false);
				workSession.getUnitSystem().changeTemperatureUnit(
						oldTemperatureUnit);
			}

			catch (TemperatureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature step must be positive");
			}
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}

			
			
		} // end of kelvin menu

		/***************************/
		/* CELSIUS MENU */
		/*************************/
		if (source == celsiusMenu) {

			// the session has got all the properties in ISU, that is: T in
			// Kelvin, P in Pascal
			// but instantaneously changing to another unity causes the number
			// in Kelvin to change !

			// get back the temperature number in the current user units:
			// T(K)->T(current User Unit)
			// T in Kelvin must be given to the method convertToTemperatureUnit
			// that returns a number in the new Unit
			// Example1: if current unit is Celsius, if current T text is 27,
			// then, if user changes to Kelvin,
			// workSession.getTemperature is called that returns the number 300,
			// convertToTemperatureUnit(300) returns number 27
			// since the textfield is not available here, we retrieved this
			// value through the 2 methods
			// workSession.getTemperature/convertToTemperatureUnit
			// setTemperatureMin(27) converts 27 to 27K in Session, and refreshT
			// is called with T=27K!!

			// Example2: if current unit is Kelvin, if current T text is 300,
			// then, if user changes to Celsius,
			// workSession.getTemperature is called that returns the number 300,
			// convertToTemperatureUnit(300) returns number 300
			// since the textfield is not available here, we retrieved this
			// value through the 2 methods
			// workSession.getTemperature/convertToTemperatureUnit
			// setTemperatureMin(300) converts 300 to 573 in Session, and
			// refreshT is called with T=573K!!

			double tMinInUserUnit = Session.getCurrentSession().getUnitSystem()
					.convertToTemperatureUnit(workSession.getTemperatureMin());
			double tMaxInUserUnit = Session.getCurrentSession().getUnitSystem()
					.convertToTemperatureUnit(workSession.getTemperatureMax());
			double tStepInUserUnit = Session
					.getCurrentSession()
					.getUnitSystem()
					.convertToTemperatureUnit(
							0.0 + workSession.getStepTemperature())
					- Session.getCurrentSession().getUnitSystem()
							.convertToTemperatureUnit(0.0);

			String oldTemperatureUnit = Session.getCurrentSession()
					.getUnitSystem().getCurrentTemperatureUnit();

			// change temperatures to new current unit: T(new User Unit) -> T(K)
			try {

				// change the current unit : T (New User Unit) = T(current User
				// Unit) (the number does not change itself !)
				workSession.getUnitSystem().changeTemperatureUnit("Celsius");

				workSession.setTemperatureMin(tMinInUserUnit); // tMinInUserUnit
																// number is
																// converted to
																// Kelvin in
																// workSession !
				workSession.setTemperatureMax(tMaxInUserUnit); // tMaxInUserUnit
																// number is
																// converted to
																// Kelvin in
																// workSession !
				workSession.setStepTemperature(tStepInUserUnit); // tStepInUserUnit
																	// number is
																	// converted
																	// to Kelvin
																	// in
																	// workSession
																	// !
				// unselect the other choices (kelvin ...)
				temperatureSelectOnly(celsiusMenu);

				// refresh temperature label
				labelTemperature.setText("Temp ("
						+ workSession.getUnitSystem().getTemperatureSymbol()
						+ ")");
				
				
				// display results in new units
				workSession.displayResults();


			} catch (TemperatureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature (in K) must be positive");
				celsiusMenu.setState(false);
				workSession.getUnitSystem().changeTemperatureUnit(
						oldTemperatureUnit);
			}

			catch (TemperatureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature step must be positive");
			}

		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}

			
			
		} // end of celsius menu

		/***************************/
		/* FAHRENHEITMENU */
		/*************************/
		if (source == fahrenheitMenu) {

			// the session has got all the properties in ISU, that is: T in
			// Kelvin, P in Pascal
			// but instantaneously changing to another unity causes the number
			// in Kelvin to change !

			// get back the temperature number in the current user units:
			// T(K)->T(current User Unit)
			// T in Kelvin must be given to the method convertToTemperatureUnit
			// that returns a number in the new Unit
			// Example1: if current unit is Celsius, if current T text is 27,
			// then, if user changes to Kelvin,
			// workSession.getTemperature is called that returns the number 300,
			// convertToTemperatureUnit(300) returns number 27
			// since the textfield is not available here, we retrieved this
			// value through the 2 methods
			// workSession.getTemperature/convertToTemperatureUnit
			// setTemperatureMin(27) converts 27 to 27K in Session, and refreshT
			// is called with T=27K!!

			// Example2: if current unit is Kelvin, if current T text is 300,
			// then, if user changes to Celsius,
			// workSession.getTemperature is called that returns the number 300,
			// convertToTemperatureUnit(300) returns number 300
			// since the textfield is not available here, we retrieved this
			// value through the 2 methods
			// workSession.getTemperature/convertToTemperatureUnit
			// setTemperatureMin(300) converts 300 to 573 in Session, and
			// refreshT is called with T=573K!!

			// get back the temperature in the current user units:
			// T(K)->T(current User Unit)
			double tMinInUserUnit = Session.getCurrentSession().getUnitSystem()
					.convertToTemperatureUnit(workSession.getTemperatureMin());
			double tMaxInUserUnit = Session.getCurrentSession().getUnitSystem()
					.convertToTemperatureUnit(workSession.getTemperatureMax());
			double tStepInUserUnit = Session
					.getCurrentSession()
					.getUnitSystem()
					.convertToTemperatureUnit(
							0.0 + workSession.getStepTemperature())
					- Session.getCurrentSession().getUnitSystem()
							.convertToTemperatureUnit(0.0);

			String oldTemperatureUnit = Session.getCurrentSession()
					.getUnitSystem().getCurrentTemperatureUnit();

			// change temperatures to new current unit: T(new User Unit) -> T(K)
			try {

				// change the current unit : T (New User Unit) = T(current User
				// Unit) (the number does not change itself !)
				workSession.getUnitSystem().changeTemperatureUnit("Fahrenheit");

				workSession.setTemperatureMin(tMinInUserUnit);
				workSession.setTemperatureMax(tMaxInUserUnit);
				workSession.setStepTemperature(tStepInUserUnit);
				// unselect the other choices (kelvin ...)
				temperatureSelectOnly(fahrenheitMenu);

				// refresh temperature label
				labelTemperature.setText("Temp ("
						+ workSession.getUnitSystem().getTemperatureSymbol()
						+ ")");
				
				// display results in new units
				workSession.displayResults();


			} catch (TemperatureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature (in K) must be positive");
				fahrenheitMenu.setState(false);
				workSession.getUnitSystem().changeTemperatureUnit(
						oldTemperatureUnit);
			}

			catch (TemperatureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature step must be positive");
			}

		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}

		} // end of fahrenheit menu

		/***************************/
		/* Pascal MENU */
		/*************************/
		if (source == paMenu) {

			// get back the pressure in the current user units: P(Pa)->P(current
			// User Unit)
			double pMinInUserUnit = Session.getCurrentSession().getUnitSystem()
					.convertToPressureUnit(workSession.getPressureMin());
			double pMaxInUserUnit = Session.getCurrentSession().getUnitSystem()
					.convertToPressureUnit(workSession.getPressureMax());
			double pStepInUserUnit = Session.getCurrentSession()
					.getUnitSystem()
					.convertToPressureUnit(workSession.getStepPressure());

			String oldPressureUnit = Session.getCurrentSession()
					.getUnitSystem().getCurrentPressureUnit();

			try {

				// change the current unit : P (New User Unit) = P(current User
				// Unit) (the number does not change itself !)
				workSession.getUnitSystem().changePressureUnit("Pascal");

				// change pressure to new current unit: P(new User Unit) ->
				// P(Pa)
				workSession.setPressureMin(pMinInUserUnit);
				workSession.setPressureMax(pMaxInUserUnit);
				workSession.setStepPressure(pStepInUserUnit);

				// unselect the other choices (atm ...)
				pressureSelectOnly(paMenu);

				// refresh pressure label
				labelPressure
						.setText("Pressure ("
								+ workSession.getUnitSystem()
										.getPressureSymbol() + ")");
				
				
				// display results in new units
				workSession.displayResults();

			} catch (PressureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure must be positive");
				paMenu.setState(false);
				workSession.getUnitSystem().changePressureUnit(oldPressureUnit);
			}

			catch (PressureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure step must be positive");
			}
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}
			
			

		} // end of pascal menu

		/***************************/
		/* BAR Menu */
		/*************************/
		if (source == barMenu) {

			// get back the pressure in the current user units: P(Pa)->P(current
			// User Unit)
			double pMinInUserUnit = Session.getCurrentSession().getUnitSystem()
					.convertToPressureUnit(workSession.getPressureMin());
			double pMaxInUserUnit = Session.getCurrentSession().getUnitSystem()
					.convertToPressureUnit(workSession.getPressureMax());
			double pStepInUserUnit = Session.getCurrentSession()
					.getUnitSystem()
					.convertToPressureUnit(workSession.getStepPressure());

			String oldPressureUnit = Session.getCurrentSession()
					.getUnitSystem().getCurrentPressureUnit();

			try {

				// change the current unit : P (New User Unit) = P(current User
				// Unit) (the number does not change itself !)
				workSession.getUnitSystem().changePressureUnit("Bar");

				// change pressure to new current unit: P(new User Unit) ->
				// P(Pa)
				workSession.setPressureMin(pMinInUserUnit);
				workSession.setPressureMax(pMaxInUserUnit);
				workSession.setStepPressure(pStepInUserUnit);

				// unselect the other choices (atm ...)
				pressureSelectOnly(barMenu);

				// refresh pressure label
				labelPressure
						.setText("Pressure ("
								+ workSession.getUnitSystem()
										.getPressureSymbol() + ")");
				
				// display results in new units
				workSession.displayResults();

			} // end of try

			catch (PressureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure must be positive");
				paMenu.setState(false);
				workSession.getUnitSystem().changePressureUnit(oldPressureUnit);
			}

			catch (PressureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure step must be positive");
			}
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}


		} // end of bar menu

		/***************************/
		/* ATMOSPHERE MENU */
		/*************************/
		if (source == atmMenu) {

			// get back the pressure in the current user units: P(Pa)->P(current
			// User Unit)
			double pMinInUserUnit = Session.getCurrentSession().getUnitSystem()
					.convertToPressureUnit(workSession.getPressureMin());
			double pMaxInUserUnit = Session.getCurrentSession().getUnitSystem()
					.convertToPressureUnit(workSession.getPressureMax());
			double pStepInUserUnit = Session.getCurrentSession()
					.getUnitSystem()
					.convertToPressureUnit(workSession.getStepPressure());

			String oldPressureUnit = Session.getCurrentSession()
					.getUnitSystem().getCurrentPressureUnit();

			try {

				// change the current unit : P (New User Unit) = P(current User
				// Unit) (the number does not change itself !)
				workSession.getUnitSystem().changePressureUnit("Atmosphere");

				// change pressure to new current unit: P(new User Unit) ->
				// P(Pa)
				workSession.setPressureMin(pMinInUserUnit);
				workSession.setPressureMax(pMaxInUserUnit);
				workSession.setStepPressure(pStepInUserUnit);

				// unselect the other choices (bar ...)
				pressureSelectOnly(atmMenu);

				// refresh pressure label
				labelPressure
						.setText("Pressure ("
								+ workSession.getUnitSystem()
										.getPressureSymbol() + ")");
				
				// display results in new units
				workSession.displayResults();

			} // end of try

			catch (PressureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure must be positive");
				paMenu.setState(false);
				workSession.getUnitSystem().changePressureUnit(oldPressureUnit);
			}

			catch (PressureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure step must be positive");
			}
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}


		} // end of atmosphere menu

		/***************************/
		/* TORR MENU */
		/*************************/
		if (source == torrMenu) {

			// get back the pressure in the current user units: P(Pa)->P(current
			// User Unit)
			double pMinInUserUnit = Session.getCurrentSession().getUnitSystem()
					.convertToPressureUnit(workSession.getPressureMin());
			double pMaxInUserUnit = Session.getCurrentSession().getUnitSystem()
					.convertToPressureUnit(workSession.getPressureMax());
			double pStepInUserUnit = Session.getCurrentSession()
					.getUnitSystem()
					.convertToPressureUnit(workSession.getStepPressure());

			String oldPressureUnit = Session.getCurrentSession()
					.getUnitSystem().getCurrentPressureUnit();

			try {

				// change the current unit : P (New User Unit) = P(current User
				// Unit) (the number does not change itself !)
				workSession.getUnitSystem().changePressureUnit("Torr");

				// change pressure to new current unit: P(new User Unit) ->
				// P(Pa)
				workSession.setPressureMin(pMinInUserUnit);
				workSession.setPressureMax(pMaxInUserUnit);
				workSession.setStepPressure(pStepInUserUnit);

				// unselect the other choices (atm ...)
				pressureSelectOnly(torrMenu);

				// refresh pressure label
				labelPressure
						.setText("Pressure ("
								+ workSession.getUnitSystem()
										.getPressureSymbol() + ")");
				
				// display results in new units
				workSession.displayResults();

			} // end of try

			catch (PressureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure must be positive");
				paMenu.setState(false);
				workSession.getUnitSystem().changePressureUnit(oldPressureUnit);
			}

			catch (PressureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure step must be positive");
			}
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}


		} // end of torr menu

        /* pressure and temperatures possible states :
         * 
         *   
         *   Single Temperature : "ST"
         *   Single Pressure    : "SP"
         *   
         *   Range of Temperatures : "RT"
         *   Range of Pressures    : "RP"
         *   
         *   */

		/*******************************/
		/* TOWARDS SINGLE TEMPERATURE */
		/*****************************/
		
		/* 1:  RT_SP --> ST_SP  */
		
		if ((source == temperatureRadio1) && (pressureRadio1.isSelected())) {

			
			labelPressureMin.setEnabled(false);
			txtPressureMin.setEnabled(false);
			labelPressureMax.setEnabled(false);
			txtPressureMax.setEnabled(false);
			labelStepPressure.setEnabled(false);
			txtStepPressure.setEnabled(false);
			
			

			try {
				workSession.setTemperatureMin(Double.parseDouble(txtTemperature.getText()));
				workSession.setTemperatureMax(Double.parseDouble(txtTemperature.getText()));
				workSession.setStepTemperature(Constants.tStepDefault);
			

			
			// make available the results save menu if session is not empty
			if (Session.getCurrentSession().getSize() != 0) {saveResults.setEnabled(true);}
		
			// make unavailable the temperature range menu
			labelTemperatureMin.setEnabled(false);
			txtTemperatureMin.setEnabled(false);
			labelTemperatureMax.setEnabled(false);
			txtTemperatureMax.setEnabled(false);
			labelStepTemperature.setEnabled(false);


			labelTemperature.setEnabled(true);
			txtTemperature.setEnabled(true);
			txtStepTemperature.setEnabled(false);

			// single temperature mode is saved 
			Session.getCurrentSession().setSessionMode(0); 
			
			// display a pane containing all the results
			workSession.displayResults();

			
			} // end of try
			
			catch (TemperatureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature (in K) must be positive");
			} catch (TemperatureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature step must be positive");
			}
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}
			
		}
		/***************************/
		/* TEMPERATURE RANGE */
		/*************************/
		
		/* 2:   ST_SP --> RT_SP  */
		
		
		if ((source == temperatureRadio2) && (pressureRadio1.isSelected())) {

			// make unavailable the pressure range
			labelPressureMin.setEnabled(false);
			txtPressureMin.setEnabled(false);
			labelPressureMax.setEnabled(false);
			txtPressureMax.setEnabled(false);
			labelStepPressure.setEnabled(false);
			txtStepPressure.setEnabled(false);
			
			
			// make unavailable the results save menu
			saveResults.setEnabled(false);

			try { // automatically converted in Kelvin into workSession
				workSession.setTemperatureMin(Double
						.parseDouble(txtTemperatureMin.getText()));
				workSession.setTemperatureMax(Double
						.parseDouble(txtTemperatureMax.getText()));
				workSession.setStepTemperature(Double
						.parseDouble(txtStepTemperature.getText()));
				workSession.setCurrentGraphIndex(0);// reinitialize currentIndex
				// on the
				// ForGraphicsButtonBox
				// (EAST)




				labelTemperatureMin.setEnabled(true);
				txtTemperatureMin.setEnabled(true);
				labelTemperatureMax.setEnabled(true);
				txtTemperatureMax.setEnabled(true);
				labelStepTemperature.setEnabled(true);
				txtStepTemperature.setEnabled(true);


				labelTemperature.setEnabled(false);
				txtTemperature.setEnabled(false);

				// range temperature mode is saved 
				Session.getCurrentSession().setSessionMode(1); 
				
				// display a pane containing all the results
				workSession.displayResults();
	   	   }// end of try
			
			catch (TemperatureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature (in K) must be positive");
			} catch (TemperatureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature step must be positive");
			}
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}

			
		}

		/***************************/
		/* TOWARDS SINGLE PRESSURE */
		/*************************/

		
		/* 3:  ST_RP --> ST_SP  */
		
		
		
		if ((source == pressureRadio1) && (temperatureRadio1.isSelected())) {

			try {
				workSession.setPressureMin(Double.parseDouble(txtPressure	.getText()));
				workSession.setPressureMax(Double.parseDouble(txtPressure	.getText()));
				workSession.setStepPressure(Constants.pStepDefault);

				
				// make available the results save menu if session is not empty
				if (Session.getCurrentSession().getSize() != 0) {saveResults.setEnabled(true);}

				// make unavailable the pressure and temperature range
				labelTemperatureMin.setEnabled(false);
				txtTemperatureMin.setEnabled(false);
				labelTemperatureMax.setEnabled(false);
				txtTemperatureMax.setEnabled(false);
				labelStepTemperature.setEnabled(false);
				txtStepTemperature.setEnabled(false);
				

				labelPressureMin.setEnabled(false);
				txtPressureMin.setEnabled(false);
				labelPressureMax.setEnabled(false);
				txtPressureMax.setEnabled(false);
				labelStepPressure.setEnabled(false);
				txtStepPressure.setEnabled(false);
				
				labelPressure.setEnabled(true);
				txtPressure.setEnabled(true);
				
				
				// single pressure mode is saved 
				Session.getCurrentSession().setSessionMode(2); 
				
				// display a pane containing all the results
				workSession.displayResults();


			} // end try

			catch (PressureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure must be positive");
			}

			catch (PressureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure step must be positive");
			}
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}


		}
		/***************************/
		/* PRESSURE RANGE */
		/*************************/
		
		/* 4:  ST_SP --> ST_RP  */
		
		if ((source == pressureRadio2) && (temperatureRadio1.isSelected())) {

			try {

				workSession.setPressureMin(Double.parseDouble(txtPressureMin
						.getText()));
				workSession.setPressureMax(Double.parseDouble(txtPressureMax
						.getText()));
				workSession.setStepPressure(Double.parseDouble(txtStepPressure
						.getText()));

				workSession.setCurrentGraphIndex(0);// reinitialize currentIndex
													// on the
													// ForGraphicsButtonBox
													// (EAST)

				labelPressureMin.setEnabled(true);
				txtPressureMin.setEnabled(true);
				labelPressureMax.setEnabled(true);
				txtPressureMax.setEnabled(true);
				labelStepPressure.setEnabled(true);
				txtStepPressure.setEnabled(true);

				labelPressure.setEnabled(false);
				txtPressure.setEnabled(false);

				
				// make unavailable the results save menu
				saveResults.setEnabled(false);

				// make unavailable the temperature range
				labelTemperatureMin.setEnabled(false);
				txtTemperatureMin.setEnabled(false);
				labelTemperatureMax.setEnabled(false);
				txtTemperatureMax.setEnabled(false);
				labelStepTemperature.setEnabled(false);
				txtStepTemperature.setEnabled(false);
				
				
				// pressure range mode is saved 
				Session.getCurrentSession().setSessionMode(3); 
	
				// display a pane containing all the results
				workSession.displayResults();


			} // end try

			catch (PressureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure must be positive");
			}

			catch (PressureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure step must be positive");
			}
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}

		} // end of if source==pressureRadio2

		
		/******************************************/
		/* TOWARDS TEMPERATURE + PRESSURE RANGE */
		/****************************************/
		
		/* 5:  RT_SP --> RT_RP  */
		
		if ((source == pressureRadio2) && (temperatureRadio2.isSelected())) {

			try {

				workSession.setPressureMin(Double.parseDouble(txtPressureMin
						.getText()));
				workSession.setPressureMax(Double.parseDouble(txtPressureMax
						.getText()));
				workSession.setStepPressure(Double.parseDouble(txtStepPressure
						.getText()));

				workSession.setCurrentGraphIndex(0);// reinitialize currentIndex
													// on the
													// ForGraphicsButtonBox
													// (EAST)

				labelPressureMin.setEnabled(true);
				txtPressureMin.setEnabled(true);
				labelPressureMax.setEnabled(true);
				txtPressureMax.setEnabled(true);
				labelStepPressure.setEnabled(true);
				txtStepPressure.setEnabled(true);

				labelPressure.setEnabled(false);
				txtPressure.setEnabled(false);

				
				// make unavailable the results save menu
				saveResults.setEnabled(false);

				
				// pressure range mode is saved 
				Session.getCurrentSession().setSessionMode(4); 
	
				// display a pane containing all the results
				workSession.displayResults();


			} // end try

			catch (PressureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure must be positive");
			}

			catch (PressureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure step must be positive");
			}
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}

		} // end of if source==pressureRadio2

		
		/******************************/
		/* TOWARDS TEMPERATURE RANGE */
		/****************************/
		
		/* 6:   RT_RP --> RT_SP  */

		if ((source == pressureRadio1) && (temperatureRadio2.isSelected())) {

			
			// make unavailable the pressure range
			labelPressureMin.setEnabled(false);
			txtPressureMin.setEnabled(false);
			labelPressureMax.setEnabled(false);
			txtPressureMax.setEnabled(false);
			labelStepPressure.setEnabled(false);
			txtStepPressure.setEnabled(false);
			
			labelPressure.setEnabled(true);
			txtPressure.setEnabled(true);
			
			
			// make unavailable the results save menu
			saveResults.setEnabled(false);

			try { // automatically converted in Kelvin into workSession
				
				
				workSession.setPressureMin(Double.parseDouble(txtPressure	.getText()));
				workSession.setPressureMax(Double.parseDouble(txtPressure	.getText()));
				workSession.setStepPressure(Constants.pStepDefault);

				workSession.setCurrentGraphIndex(0);// reinitialize currentIndex
				// on the
				// ForGraphicsButtonBox
				// (EAST)


				// range temperature mode is saved 
				Session.getCurrentSession().setSessionMode(5); 
				
				// display a pane containing all the results
				workSession.displayResults();
	   	   }// end of try
			catch (PressureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure must be positive");
			}

			catch (PressureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure step must be positive");
			}

		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}

			
		}

		
		/******************************************/
		/* TOWARDS TEMPERATURE + PRESSURE RANGE */
		/****************************************/
		
		/* 7:  ST_RP --> RT_RP  */
		
		if ((source == temperatureRadio2) && (pressureRadio2.isSelected())) {

			try {
				
				

				workSession.setTemperatureMin(Double.parseDouble(txtTemperatureMin.getText()));
				workSession.setTemperatureMax(Double.parseDouble(txtTemperatureMax.getText()));
				workSession.setStepTemperature(Double.parseDouble(txtStepTemperature.getText()));

				
				
				
				workSession.setCurrentGraphIndex(0);// reinitialize currentIndex
													// on the
													// ForGraphicsButtonBox
													// (EAST)

				labelTemperatureMin.setEnabled(true);
				txtTemperatureMin.setEnabled(true);
				labelTemperatureMax.setEnabled(true);
				txtTemperatureMax.setEnabled(true);
				labelStepTemperature.setEnabled(true);
				txtStepTemperature.setEnabled(true);


				labelTemperature.setEnabled(false);
				txtTemperature.setEnabled(false);

			
				// make unavailable the results save menu
				saveResults.setEnabled(false);

				
				// pressure range mode is saved 
				Session.getCurrentSession().setSessionMode(6); 
	
				// display a pane containing all the results
				workSession.displayResults();


			} // end try

			catch (TemperatureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The temperature must be positive");
			}

			catch (TemperatureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature step must be positive");
			}
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}

		} // end of if source==pressureRadio2

	
		/***************************/
		/* TOWARDS PRESSURE RANGE */
		/*************************/
		
		/* 8:  RT_RP --> ST_RP  */
		
		if ((source == temperatureRadio1) && (pressureRadio2.isSelected())) {

			try {

				workSession.setTemperatureMin(Double.parseDouble(txtTemperature.getText()));
				workSession.setTemperatureMax(Double.parseDouble(txtTemperature.getText()));
				workSession.setStepTemperature(Constants.tStepDefault);

				
				workSession.setCurrentGraphIndex(0);// reinitialize currentIndex
													// on the
													// ForGraphicsButtonBox
													// (EAST)


				
				// make unavailable the results save menu
				saveResults.setEnabled(false);

				// make unavailable the temperature range
				labelTemperatureMin.setEnabled(false);
				txtTemperatureMin.setEnabled(false);
				labelTemperatureMax.setEnabled(false);
				txtTemperatureMax.setEnabled(false);
				labelStepTemperature.setEnabled(false);
				txtStepTemperature.setEnabled(false);
				
				labelTemperature.setEnabled(true);
				txtTemperature.setEnabled(true);
				
				// pressure range mode is saved 
				Session.getCurrentSession().setSessionMode(7); 
	
				// display a pane containing all the results
				workSession.displayResults();


			} // end try

			catch (TemperatureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The temperature must be positive");
			}

			catch (TemperatureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature step must be positive");
			}

		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}

		} // end of if source==pressureRadio2

		
		
		
		/***************************/
		/* CHANGE TEMPERATURE */
		/*************************/
		// TextField1 management
		if (source == txtTemperature) {

			try {
				// tMin = tMax = T NOW !
				workSession.setTemperatureMin(Double.parseDouble(txtTemperature
						.getText()));
				workSession.setTemperatureMax(Double.parseDouble(txtTemperature
						.getText()));
				workSession.setStepTemperature(Constants.tStepDefault);
				workSession.setCurrentGraphIndex(0);// reinitialize currentIndex
													// on the
													// ForGraphicsButtonBox
													// (EAST)

				// display a pane containing all the results
				workSession.displayResults();
				
				
				txtTemperature.setText(String.valueOf(Maths.format(Session
						.getCurrentSession().getUnitSystem()
						.convertToTemperatureUnit(workSession.getTemperatureMin()),
						"0.00")));


			} // end of try

			catch (TemperatureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature (in K) must be positive");
			}

			catch (TemperatureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature step must be positive");
			}

			catch (NumberFormatException error) {
				JOptionPane.showMessageDialog(null,
						"Warning, temperature should be a number !",
						"Temperature error", JOptionPane.WARNING_MESSAGE);
			} // end of catch (NumberFormatException error)

		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}
			
			
		} // end of if (source==txtTemperature)

		/***************************/
		/* CHANGE TEMPERATURE MIN */
		/*************************/
		if (source == txtTemperatureMin) {
			try {
				workSession.setTemperatureMin(Double
						.parseDouble(txtTemperatureMin.getText()));

				// refresh the pane containing all the results
				workSession.displayResults();
				
				
				// if tMax=tMin then update tStep

				txtTemperatureMin.setText(String.valueOf(Maths.format(Session
						.getCurrentSession().getUnitSystem()
						.convertToTemperatureUnit(workSession.getTemperatureMin()),
						"0.00")));

				double tStepTemporary = Session
						.getCurrentSession()
						.getUnitSystem()
						.convertToTemperatureUnit(
								0.0 + workSession.getStepTemperature());
				tStepTemporary = tStepTemporary
						- Session.getCurrentSession().getUnitSystem()
								.convertToTemperatureUnit(0.0);

				txtStepTemperature.setText(String.valueOf(Maths.format(
						tStepTemporary, "0.00")));


			} // end of try

			catch (TemperatureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature (in K) must be positive");
			}

			catch (NumberFormatException error) {
				JOptionPane.showMessageDialog(null,
						"Warning, temperature should be a number !",
						"Temperature error", JOptionPane.WARNING_MESSAGE);

			} // end of catch (NumberFormatException error) {
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}


		} // end of if (source==txtTemperatureMin) {

		/***************************/
		/* CHANGE TEMPERATURE MAX */
		/*************************/
		if (source == txtTemperatureMax) {

			try {
				workSession.setTemperatureMax(Double
						.parseDouble(txtTemperatureMax.getText()));

				// display a pane containing all the results
				workSession.displayResults();
				
				
				txtTemperatureMax.setText(String.valueOf(Maths.format(Session
						.getCurrentSession().getUnitSystem()
						.convertToTemperatureUnit(workSession.getTemperatureMax()),
						"0.00")));
				// if tMax=tMin then update tStep

				double tStepTemporary = Session
						.getCurrentSession()
						.getUnitSystem()
						.convertToTemperatureUnit(
								0.0 + workSession.getStepTemperature());
				tStepTemporary = tStepTemporary
						- Session.getCurrentSession().getUnitSystem()
								.convertToTemperatureUnit(0.0);

				txtStepTemperature.setText(String.valueOf(Maths.format(
						tStepTemporary, "0.00")));


			} // end of try

			catch (TemperatureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature (in K) must be positive");
			}

			catch (NumberFormatException error) {
				JOptionPane.showMessageDialog(null,
						"Warning, temperature should be a number !",
						"Temperature error", JOptionPane.WARNING_MESSAGE);

			} // end of catch (NumberFormatException error) {

		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}

			

		} // end of if (source == txtTemperatureMax) {

		/***************************/
		/* CHANGE TEMPERATURE STEP */
		/*************************/
		if (source == txtStepTemperature) {

			try {
				workSession.setStepTemperature(Double
						.parseDouble(txtStepTemperature.getText()));

				// display a pane containing all the results
				workSession.displayResults();
				
				double tStepTemporary = Session
						.getCurrentSession()
						.getUnitSystem()
						.convertToTemperatureUnit(
								0.0 + workSession.getStepTemperature());
				tStepTemporary = tStepTemporary
						- Session.getCurrentSession().getUnitSystem()
								.convertToTemperatureUnit(0.0);

				txtStepTemperature.setText(String.valueOf(Maths.format(
						tStepTemporary, "0.00")));


			} // end of try

			catch (TemperatureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Temperature step must be positive");
			}

			catch (NumberFormatException error) {
				JOptionPane.showMessageDialog(null,
						"Warning, temperature step should be a number !",
						"Temperature error", JOptionPane.WARNING_MESSAGE);

			} // end of catch (NumberFormatException error) {
			
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}


		} // end of if (source==txtStepTemperature) {

		/***************************/
		/* CHANGE PRESSURE */
		/*************************/
		if (source == txtPressure) {

			try {

				workSession.setPressureMin(Double.parseDouble(txtPressure
						.getText()));
				workSession.setPressureMax(Double.parseDouble(txtPressure
						.getText()));
				workSession.setStepPressure(Constants.pStepDefault);

				// display a pane containing all the results
				workSession.displayResults();
				
				txtPressure
				.setText(String.valueOf(Maths.format(
						Session.getCurrentSession()
								.getUnitSystem()
								.convertToPressureUnit(
										workSession.getPressureMin()),
						"0.00E00")));


			} // end of try

			catch (PressureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure must be positive");
			}

			catch (PressureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure step must be positive");
			}

			catch (NumberFormatException error) {
				JOptionPane.showMessageDialog(null,
						"Warning, pressure should be a number !",
						"Pressure error", JOptionPane.WARNING_MESSAGE);

			} // end of catch (NumberFormatException error)
			
		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}
			

		} // end of if (source==txtPressure)

		/***************************/
		/* CHANGE PRESSURE MIN */
		/*************************/
		if (source == txtPressureMin) {
			try {
				workSession.setPressureMin(Double.parseDouble(txtPressureMin
						.getText()));

				// display a pane containing all the results
				workSession.displayResults();

				txtPressureMin
				.setText(String.valueOf(Maths.format(
						Session.getCurrentSession()
						.getUnitSystem()
						.convertToPressureUnit(
								workSession.getPressureMin()),
								"0.00E00")));
				// if tMax=tMin then update tStep
				txtStepPressure.setText(String.valueOf(Maths.format(
						Session.getCurrentSession()
						.getUnitSystem()
						.convertToPressureUnit(
								workSession.getStepPressure()), "0.00")));


			} // end of try

			catch (PressureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure must be positive");
			}

			catch (NumberFormatException error) {
				JOptionPane.showMessageDialog(null,
						"Warning, Pressure should be a number !",
						"Pressure error", JOptionPane.WARNING_MESSAGE);

			} // end of catch (NumberFormatException error) {

		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}
			
			

		} // end of if (source==txtPressureMin) {

		/***************************/
		/* CHANGE PRESSURE MAX */
		/*************************/
		if (source == txtPressureMax) {

			try {
				workSession.setPressureMax(Double.parseDouble(txtPressureMax
						.getText()));

				// display a pane containing all the results
				workSession.displayResults();

				txtPressureMax
				.setText(String.valueOf(Maths.format(
						Session.getCurrentSession()
						.getUnitSystem()
						.convertToPressureUnit(
								workSession.getPressureMax()),
								"0.00")));
				// if tMax=tMin then update tStep
				txtStepPressure.setText(String.valueOf(Maths.format(
						Session.getCurrentSession()
						.getUnitSystem()
						.convertToPressureUnit(
								workSession.getStepPressure()), "0.00")));


			} // end of try

			catch (PressureException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure must be positive");
			}

			catch (NumberFormatException error) {
				JOptionPane.showMessageDialog(null,
						"Warning, Pressure should be a number !",
						"Pressure error", JOptionPane.WARNING_MESSAGE);

			} // end of catch (NumberFormatException error) {

		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}


		} // end of if (source == txtPressureMax) {

		/***************************/
		/* CHANGE PRESSURE STEP */
		/*************************/
		if (source == txtStepPressure) {

			try {
				workSession.setStepPressure(Double.parseDouble(txtStepPressure
						.getText()));

				// display a pane containing all the results
				workSession.displayResults();
				
				txtStepPressure.setText(String.valueOf(Maths.format(
						Session.getCurrentSession()
								.getUnitSystem()
								.convertToPressureUnit(
										workSession.getStepPressure()), "0.00")));


			} // end of try

			catch (PressureStepException error) {
				JOptionPane.showMessageDialog(null,
						"Warning : The Pressure step must be positive");
			}

			catch (NumberFormatException error) {
				JOptionPane.showMessageDialog(null,
						"Warning, Pressure step should be a number !",
						"Pressure error", JOptionPane.WARNING_MESSAGE);

			} // end of catch (NumberFormatException error) {

		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}
			
			

		} // end of if (source==txtStepPressure) {

		/***************************/
		/* CHANGE VIB SCALING FACTOR */
		/*************************/
		if (source == txtScalingFactor) {
			try {

				workSession.setScalingFactor(Double
						.parseDouble(txtScalingFactor.getText())); // all thermodynamic properties are updated (except, E0 for a RRKM computation) (see below)

				// display a pane containing all the results
				centralPane.removeAll();

				// if a RRKM calculation is going to be performed, update the E0 critical energy value BOX
				// since E0 = f(Up, vib. frequencies)				
				if (Session.getCurrentSession().getSessionContent().size()!=0) { // only if a calculation has been performed
					
					String currentClassName = Session.getCurrentSession().getSessionContent().get(0).getClass().getName();
					if (currentClassName=="UnimolecularReaction") {
						
						UnimolecularReaction currentReaction = (UnimolecularReaction)Session.getCurrentSession().getSessionContent().get(0);
						if (currentReaction.getRateConstant().getKineticLevel()=="rrkmTightTs") {
							// get the E0Panel to update the E0 value
							
							RateConstantRRKM currentRateConstant = (RateConstantRRKM)(currentReaction.getRateConstant());
							double E0 = currentRateConstant.getE0PESValue();
							currentRateConstant.getE0Panel().getTxtE0().setText(Maths.format(E0*Constants.convertJToKCalPerMol, "00.00"));
							currentRateConstant.setE0(E0); // refresh the E0 property of RRKMRateConstant (computeValue of k is here)
							
						}// end of if (currentReaction.getRateConstant().getKineticLevel()=="rrkmTightTs"
						
					}// end of if (currentClassName=="UnimolecularReaction")
				}// end of if (Session.getCurrentSession().getSessionContent().size()!=0)
				


				
				
				// display a pane containing all the results
				workSession.displayResults();
				
				
				txtScalingFactor.setText(String.valueOf(Maths.format(
						workSession.getScalingFactor(), "0.00")));
				
				
			} // end of try

			catch (NumberFormatException error) {
				JOptionPane.showMessageDialog(null,
						"Warning, scaling factor should be a number !",
						"Scaling Factor error", JOptionPane.WARNING_MESSAGE);

			} // end of catch (NumberFormatException error)

		    catch (runTimeException error) {resetCalculation();}
		    catch (IllegalDataException error) {resetCalculation();}
			catch (OutOfRangeException error) {resetCalculation();}
			

		} // end of if (source==txtScalingFactor) {

		/******************************************************************/
		/* S O U R C E == H E L P I N P U T F I L E */
		/******************************************************************/
		// this part of program display the html documentation to create a
		// kisthep input file
		if (source == helpInputFile) {

			try {		
				String indexName = "userIndex.html";
				URL address =   getClass().getResource(indexName);
				
				
				if (address == null) {
					String message = "Error in class Interface in ActionPerformed, if source==helpInputFile"+ Constants.newLine;
					message = message +  "File " + indexName + " not found"+ Constants.newLine;
					message = message +  "Please contact the authors"+ Constants.newLine;
					JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                      
			  }
			  else {				
				String filePath=address.toString(); 				 
				JFrame viewerFrameInputFile = new DocumentViewer(filePath);
				viewerFrameInputFile.setVisible(true);
			  }// end of if then else
			}// end of try
			catch (runTimeException err) {}


		}

		// ---END--- S O U R C E == H E L P I N P U T F I L E

		/******************************************************************/
		// S O U R C E == H E L P O P T I O N
		/******************************************************************/

		// this part of program display the html documentaion about the
		// differents kisthep java classes
		if (source == helpDocumentation) {
			
			
			try {		
				String indexName = "index.html";
				URL address =   getClass().getResource(indexName);
				if (address == null) {
					String message = "Error in class Interface in ActionPerformed, if source==helpInputFile"+ Constants.newLine;
					message = message +  "File " + indexName + " not found"+ Constants.newLine;
					message = message +  "Please contact the authors"+ Constants.newLine;
					JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                      
			  }
			  else {
				File index = new File(address.toString().substring(5)); 
				String filePath=address.toString(); 
				JFrame viewerFrameInputFile = new DocumentViewer(filePath);
				viewerFrameInputFile.setVisible(true);
			  }// end of if then else
			}
			catch (runTimeException err) {}
			
		}
		// ---E N D--- S O U R C E == H E L P O P T I O N

		/******************************************************************/
		// S O U R C E == A B O U T K I S T H E P
		/******************************************************************/
		// this part of program display a page to introduce the differents
		// authors of kisthep
		if (source == aboutKisthep) {
			Session.aboutKisthepDisplay();
		}
		// ---END--- S O U R C E == A B O U T K I S T H E P
		/******************************************************************/

	} // End of the actionPerformed method

	/*****************************************/
	/* F i l e I t e m s e t E n a b l e d */// Initial state of file options
	/*****************************************/

	public void fileItemsetEnabled(boolean state) {
		for (int iItem = 0; iItem < fileNumberItem; iItem++) {
			tJMenuItemFileInactives[iItem].setEnabled(state);
		}

	} // End of the fileItemsetEnabled method

	/*********************************************************/
	/* c a l c u l a t i o n I t e m s e t E n a b l e d */// Initial state of
															// file options
	/******************************************************/

	public void calculationItemsetEnabled(boolean state) {

		// change the state of the menu item of the menu Calculation (Atom/Molecule, kTST, ...)
		// indeed, according whether the calculation is reset or not, these item must be grey or not (disabled or enabled)
		for (int iItem = 0; iItem < calculationNumberItem; iItem++) {
			tJMenuItemCalculationInactives[iItem].setEnabled(state);
		}

	} // End of the FileItemsetEnabled method

	/*****************************/
	/* n e w M a i n J M e n u */// Instantiation and addition of menus to the
									// bar
	/*****************************/

	public JMenu newMainJMenu(String name) {
		JMenu menu = new JMenu(name);
		bar.add(menu);
		return menu;
	} // End of the newMainJMenu method

	/*********************/
	/* n e w J M e n u */// Instantiation and addition of submenus to menus
	/*********************/

	// modif fred eric
	public JMenu newJMenu(String menuName, JMenu choice) {
		JMenu subMenu = new JMenu(menuName);
		choice.add(subMenu);
		return subMenu;
	} // End of the newJMenu method

	// fin modif

	/*****************************/
	/* n e w J M e n u I t e m */// Instantiation and addition of options to
									// submenus
	/*****************************/

	public JMenuItem newJMenuItem(String menuItemName, JMenuItem option) {
		JMenuItem menuItem = new JMenuItem(menuItemName);
		option.add(menuItem);
		return menuItem;
	} // End of the newJMenuItem method

	/**********************************/
	/* g e t C e n t r a l P a n e */
	/**********************************/

	public static JDesktopPane getCentralPane() {
		return centralPane;
	} // end of getcentralPane method

	/********************************************************/
	/* g e t C a l c u l a t i o n F e a t u r e B o x */
	/*******************************************************/

	public static Box getCalculationFeatureBox() {
		return calculationFeatureBox;
	} // end of getCalculationFeatureBox method

	/********************************************************/
	/* g e t F o r G r a p h i c s B u t t o n s B o x */
	/*******************************************************/

	public static Box getForGraphicsButtonsBox() {
		return forGraphicsButtonsBox;
	} // end of getForGraphicsButtonsBox method

	/*******************/
	/* g e t P a g e */
	/*******************/

	public JMenuItem getDisplayResults() {
		return dataRefresh;
	} // end of getPage method

	/**********************************/
	/* g e t S a v e R e s u l t s */
	/********************************/

	public JMenuItem getSaveResults() {
		return saveResults;
	} // end of saveResults method

	/*********************/
	/* resetSetEnabled */
	/*********************/

	public void resetSetEnabled(boolean booleanValue) {

		calculationItemsetEnabled(!booleanValue);
		reset.setEnabled(booleanValue);
	}

	/*************************/
	/* temperatureSelectOnly */
	/************************/

	// set all the temperature unit menu to false
	// except the menu parameter one.
	public void temperatureSelectOnly(JCheckBoxMenuItem menu) {

		for (int iMenu = 0; iMenu < temperatureMenuNumber; iMenu++) {

			temperatureUnitMenu[iMenu].setState(false);

		} // end of for

		menu.setState(true);

	} // end of temperatureSelectOnly

	/*************************/
	/* pressureSelectOnly */
	/************************/

	// set all the pressure unit menu to false
	// except the menu parameter one.
	public void pressureSelectOnly(JCheckBoxMenuItem menu) {

		for (int iMenu = 0; iMenu < pressureMenuNumber; iMenu++) {

			pressureUnitMenu[iMenu].setState(false);

		} // end of for

		menu.setState(true);

	} // end of pressureSelectOnly

	/******************************************/
	/* refreshFillKisthepContentPane */
	/****************************************/
	public void refreshFillKisthepContentPane() {

		// first remove all components of the main pane, (at the first call,
		// there are no component) before to reconstruct them

		if (getContentPane() != null) {
			getContentPane().removeAll();
		}

		// calculationFeature
		calculationFeatureBox = Box.createVerticalBox();
		calculationFeatureBox.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		// a box to host buttons causing the graphics to be displayed (G=f(t),
		// G=f(T), ... )
		forGraphicsButtonsBox = Box.createVerticalBox();
		forGraphicsButtonsBox.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		// addition of pressure and temperature and calculation features Panes
		// Temperature
		getContentPane().add(temperaturePane, BorderLayout.NORTH);
		// Pressure
		getContentPane().add(pressurePane, BorderLayout.SOUTH);

		// calculationFeatures
		getContentPane().add(calculationFeatureBox, BorderLayout.WEST);

		// forGraphicsButtonsBox;
		getContentPane().add(forGraphicsButtonsBox, BorderLayout.EAST);

		// centralPane managment
		centralPane = new JDesktopPane();
	 
		//GridLayout g3 = new GridLayout(0, 1);
		centralPane.setLayout(new BoxLayout(centralPane, BoxLayout.Y_AXIS));
		//centralPane.setLayout(g3);
		centralPane.setBorder(BorderFactory.createLoweredBevelBorder());

		//centralPane backGroundColor
		centralPane.setBackground(Color.WHITE);
		
		// Kisthep logo managment
		logo = new Logo();
		centralPane.add(logo);

		// add the centralPane to the center of JFrame KisthepPane
		getContentPane().add(centralPane, BorderLayout.CENTER);

	}// end of refreshFillKisthepContentPane

	/******************************************/
	/* buildReactPath() */
	/****************************************/
	public void buildReactPath() throws CancelException, IOException, IllegalDataException {

		String message = "Build a single .kinp file to describe a TS or a Reaction Path in view of a TST or a VTST calculation\n (in this case, information from several files will be wrapped into a single one). \n\n"
				+ "Appropriate **POINT and *IRC keywords will be added.\n\n";
		JOptionPane.showMessageDialog(this, message, Constants.kisthepMessage, JOptionPane.INFORMATION_MESSAGE);

		// get the number of points (1 for a TST, n for a VTST calculation)
		// ask for intrinsic reaction coordinate		
		int nbPts;
		String questionNbpts="Please enter the number of points to be read (TST=1, VTST=n)\n Including the TS itself ";		
		String txt;
		txt = KisthepDialog.requireDouble(questionNbpts, "KiSThelP");
		nbPts = (int)(Double.parseDouble(txt));
		if (nbPts <=0) {throw new CancelException();}
		
		File temporaryFile, temporaryFile2;
		ActionOnFileRead  readFromCurrentKinp;
		ActionOnFileWrite writeOnRPKinp;
		String temporaryFileName = "";
		String temporaryFileName2= "";
		String currentLine;
		Boolean notFoundAGoodName, alreadyOpen;

		String question1 = "Please enter the corresponding Intrinsic Reaction Coordinate (IRC, ex.: -0.3)";
		String question2;
		

		ArrayList<Double> irc = new ArrayList<Double>();
		ArrayList<File> kinpFiles = new ArrayList<File>();

		
		// get the (irc/file) set the CanceException is implicitly handled by KisthepDialog.requireExistingFilename
		for (int iPoint=1; iPoint<=nbPts; iPoint++){

             // create a temporary chemicalsystem to test the data and create a kinp file is
			// the entered file is not of kinp type but is of quatum chemistry type
			question2 = "FILE: select a data file for PATH POINT " + iPoint + " / " + nbPts;
			temporaryFile = ChemicalSystem.returnKinpFile(ChemicalSystem.pathPoint, question2);
			
			// if ok, add this kinp file to the list of kinp files to be read next
			kinpFiles.add(temporaryFile);

			// if nbPts == 1 => simple TST calculation => irc = 0.0 automatically
			// ask for intrinsic reaction coordinate
			if (nbPts >1) { // VTST case
				txt = KisthepDialog.requireDouble(question1, "KiSThelP");
				irc.add(Double.parseDouble(txt));
			}
			else irc.add(0.0); // TST case


		} // end of for (int iPoint=1; iPoint<=nbPts; iPoint++)

		
		/*****************************************************/
		// ask for new filename for the resulting kinp file
		// but take care: the new filename must not be already open !
		temporaryFile = KisthepDialog.requireRPOutputFilename("Reaction path filename (e.g. RP.kinp) ?",new KisthepOutputFileFilter(Constants.kInpFileType));
		temporaryFileName = temporaryFile.getAbsolutePath();
		
		// if no file extension has been given, the default kinp is given
		if (temporaryFileName.indexOf('.') == -1) {
			temporaryFileName = temporaryFileName + ".kinp";
		}
		

		notFoundAGoodName=true;
		while (notFoundAGoodName) {
		   // check that this file is not already open (one of the kinp files in memory)			   
		   alreadyOpen=false;
		   for (int iFile = 0; iFile < nbPts; iFile++) {
			   // get the absolute filename of the kinp file of the current path point		   
			    temporaryFile2 = (File) (kinpFiles.get(iFile));
			    temporaryFileName2 = temporaryFile2.getAbsolutePath();
			   // compare to the wanted filename for the reaction path:
			    if (temporaryFileName2.equals(temporaryFileName)) {
			    	  alreadyOpen=true;		
			    	  JOptionPane.showMessageDialog(Interface.getKisthepInterface(), "Sorry, this file is already in use in KiSThelP session. Please choose another name.", Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);
			  	  temporaryFile = KisthepDialog.requireRPOutputFilename("Reaction path filename (e.g. RP.kinp) ?",new KisthepOutputFileFilter(Constants.kInpFileType));
			  	  temporaryFileName = temporaryFile.getAbsolutePath();
					// if no file extension has been given, the default kinp is given
					if (temporaryFileName.indexOf('.') == -1) {
						temporaryFileName = temporaryFileName + ".kinp";
					}
					break; // then, does not test the existence on the disk (hereafter, in the while statement)

			    }
		   } // end of for
		   
           // after this "for" loop on "list of IRC files" : check if this filename is already open or not
		   if (alreadyOpen==false) {
			   
			   // now, check that this file does not exist on the disk
			   // --> temporarily create a new temporary file with temporaryFileName as filename,
			   // because maybe the user has not provided the extension ...
			   File f = new File(temporaryFileName);
			   if(f.exists()) 			               
			         {
				      
			          int answer=JOptionPane.showConfirmDialog(Interface.getKisthepInterface(),"This file already exists, do you want to continue ?","Save as", JOptionPane.YES_NO_OPTION);
			          if (answer==JOptionPane.YES_OPTION) {
			        	     // then, the file name is not open by Kisthelp (as a kinp file), it already exists on the disk, and will be replaced
			        	     notFoundAGoodName=false; // one exits the while loop because the file does not exist in memory (in session), exist
			        	                              // in memory but the user decides to go on ...
			          }// end of if answer==JOptionPane.YES_OPTION
			          else {
			        	      // the file name is not open by Kisthelp (as a kinp file), it already exists on the disk, and will not be replaced
				        	  // ask for a new filename:
				        	  temporaryFile = KisthepDialog.requireRPOutputFilename("Reaction path filename (e.g. RP.kinp) ?",new KisthepOutputFileFilter(Constants.kInpFileType));
				        	  temporaryFileName = temporaryFile.getAbsolutePath();
	
				        	  // if no file extension has been given, the default kinp is given
				        	  if (temporaryFileName.indexOf('.') == -1) {
				        		  temporaryFileName = temporaryFileName + ".kinp";
				        	      }
					

			               }
			          
			         } // end of  if (file.exists())
			   else notFoundAGoodName=false;// one exits the while loop because the file does not exist on the disk nor in memory (in session)
		   } // end of if (alreadyOpen==false)
		} // end of the while (notFoundAGoodName)

		// to prepare the writing action
		writeOnRPKinp = null;
		writeOnRPKinp = new ActionOnFileWrite(temporaryFileName);


		// combine the file(s)
		for (int iFile = 0; iFile < nbPts; iFile++) {
			// write the IRC Header
			writeOnRPKinp.oneString(Keywords.startOfPointInputSection);
			writeOnRPKinp.oneString(Keywords.startOfReactionCoordinateInputSection);
			writeOnRPKinp.oneDouble(irc.get(iFile));
			writeOnRPKinp.oneString(Keywords.endOfInputSection);
			
			
			//now: directly copy the information from the kinp file to the reactionPath file 
			// write the kinp file content of the current point
			temporaryFile = (File) (kinpFiles.get(iFile));
			temporaryFileName = temporaryFile.getAbsolutePath();

			readFromCurrentKinp= new ActionOnFileRead (temporaryFileName,Constants.kInpFileType); 
			currentLine = readFromCurrentKinp.oneString();  

			// reading loop      				
			if (currentLine == null) {
				JOptionPane.showMessageDialog(null,"Warning : the file " + temporaryFileName + " is empty ...");  
			}
			else {
				do {  
					writeOnRPKinp.oneString(currentLine);
					currentLine = readFromCurrentKinp.oneString();
				} while (currentLine != null) ;
			} // end of else
			// write the IRC tail keyword
			writeOnRPKinp.oneString(Keywords.endOfPointSection);
			
			// remove the temporary current Kinp file from the Kisthelp list AND next erase the file 
			readFromCurrentKinp.end();
			readFromCurrentKinp.getWorkFile().delete();
			

		} // end of for (int iFile=0)
		writeOnRPKinp.end();
		JOptionPane.showMessageDialog(null,"file " + writeOnRPKinp.getWorkFile().getName() + " successfully written");
	} // end of method buildReactPath

} // End of class

