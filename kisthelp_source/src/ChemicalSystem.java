
import kisthep.util.*;
import kisthep.file.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class ChemicalSystem implements ReadWritable {

	public static final String minimum = "minimum";
	public static final String saddlePoint1 = "saddlePoint1";
	public static final String pathPoint = "pathPoint";
	public static final String anyPoint = "anyPoint"; // an anypoint can have 0, 1, or more imaginary frequencies

	protected boolean atomic = false;
	protected boolean linear = false;
	protected double mass, initialMass; // in g.mol(-1)
	protected Complex[] vibFreq; // in K !!
	protected Complex[] unscaledVibFreq, initialUnscaledVibFreq; // in K !! // depends on the session (via scaling
																	// factor)
	protected double[] hrdsBarrier;// in J/Molec !, length identical to VibFreq length

	protected double[] inertia, initialInertia; // in Amu.bohr^2
	protected double up, initialUp; // the potential energy in hartree
	protected int elecDegener, initialElecDegener;
	protected String nature; // can be one of the static constants (minimum, saddlePoint1, pathPoint,
								// anyPoint)
	protected int symmetryNumber, initialSymmetryNumber;
	protected double ZPE = Constants.nullValue; // the zero point energy in J/mol ! (not in J/molec)
	protected StringVector locatedSection = new StringVector(); // sections already read in file
	protected int atomNb; // the number of atoms in the chemical system

	/* C O N S T R U C T O R 1 */

	// constructor 1 is used to build a chemical system that is not a reaction path
	// point
	// => the filename is required into this class because only molecular
	// information is required
	// => the possibility to directly read the data into a gaussian file (or
	// GAMESS...) is given

	/*
	 * be careful: to make a chemical system requires that
	 * the session is provided with a list of "Files To Be Read" ... !
	 */
	// Session.getCurrentSession().getFilesToBeRead().getFirstAndRemove();

	public ChemicalSystem(String nature) throws CancelException, IllegalDataException, IOException {

		this.nature = nature; // can be minimum, saddlePoint1, pathPoint, anyPoint;

		openTestReadingFile(null); // call to the openTestReadingFile method: open,test,read a file (contains the
								// dataRead method)
								// in the ChemicalSystem class
		testCoherenceData(); // call to the testCoherenceData method: test the coherence of data in the
								// ChemicalSystem class

		completeProperties(); // complete properties which doesn't need to be read

	} // end of CONSTRUCTOR 1

	/*
	 * C O N S T R U C T O R 2 , (Chemical system is to be read from a reactionpath
	 * file)
	 */
	// constructor 2 is used to build a chemical system that belongs to a reaction
	// path point
	// => the filename has been required before (in the ReactionPath Class) because
	// data (other than molecular information) was required
	// (i.e reaction coordinate along the reaction path, read in class ReactionPath)
	// other chemical systems (other than the one presently read) are included in
	// this same data file,
	// that is the reason why, in this case, the data file is already open and the
	// "read" action is a parameter
	// in this case, no possibility to directly read a gaussian file (or GAMESS ...)
	// is given ...

	public ChemicalSystem(String nature,File clinpFile) throws CancelException, IllegalDataException, IOException {

		this.nature = nature; // can be minimum, saddlePoint1, pathPoint, anyPoint;

		openTestReadingFile(clinpFile); // call to the openTestReadingFile method: open,test,read a file (contains the
								// dataRead method)
								// in the ChemicalSystem class
		testCoherenceData(); // call to the testCoherenceData method: test the coherence of data in the
								// ChemicalSystem class

		completeProperties(); // complete properties which doesn't need to be read

	} // end of CONSTRUCTOR 1


	public ChemicalSystem(String nature, ActionOnFileRead read) throws IllegalDataException, IOException {
		this.nature = nature;

		dataRead(read, Keywords.endOfPointSection); // call to the dataRead method: to read the file.kinp (must be of
													// KISTHEP type!)
		testCoherenceData(); // to test the coherence of data
		completeProperties(); // completeProperties which doesn't need to be read

	} // end of constructor 2

	/* C O N S T R U C T O R 3 (to reload) from a Session file */

	public ChemicalSystem() {
	}

	/*************************************************************/
	/* r e t u r n K i n p F i l e */
	/***********************************************************/

	// Constructor for no file
	public static File returnKinpFile(String nature, String question) throws CancelException, IllegalDataException, IOException {
			return returnKinpFile(nature, question,null);
		}
	// Constructor for no question but file
	public static File returnKinpFile(String nature,File clinpfFile) throws CancelException, IllegalDataException, IOException {
			return returnKinpFile(nature, "",clinpfFile);
		}

	public static File returnKinpFile(String nature, String question,File clinpFile)
			throws CancelException, IllegalDataException, IOException {

		File temporaryKinpFile; // kinp file that will be returned
		
		// static method that can be called independently of a session !
		// this method ask for a filename, and return a filled kinpFile describing a
		// chemicalSystem
		ChemicalSystem temporarySystem = new ChemicalSystem(); // return an empty object here (constructor 3 of new
																// ChemicalSystem)

		temporarySystem.nature = nature; // can be minimum, saddlePoint1, pathPoint, anyPoint;

		ActionOnFileRead read;
		File temporaryFileName;

		// ask for a filename, will serve as input, but can be either a .kinp or .out QM
		// file
		if (clinpFile==null){
		temporaryFileName = KisthepDialog.requireExistingFilename(question,
				new KisthepInputFileFilter(Constants.anyAllowedDataFile));
			}else{
				temporaryFileName=clinpFile;
			}


		// DETECT THE OUTPUT TYPE of this file (gaussian09, or GAMESS or NWchem or ORCA
		// or MOLPRO or kinp);
		// pay attention: an absolute path is used below
		read = new ActionOnFileRead(temporaryFileName.getAbsolutePath(), Constants.anyAllowedDataFile);

		// now, we are sure that the open file is ONLY of gaussian or GAMESS or NWchem
		// or ORCA or MOLPRO or kinp file type !
		String inputType = read.getWorkFile().getType();

		if (inputType.equals(Constants.kInpFileType))

		{

			// duplicate this kinp file, because it will be erased at the end (in
			// Interface/buildReactionPath)
			// and the user kinp file must not be erased

			String kinpFileName = KisthepFile.getPrefix(temporaryFileName.getAbsolutePath()) + "_copy" + ".kinp";

			read.end();// close the read action on user kinp file
						// but all the properties of the readAction are still available through obkect
						// "read" !
			temporaryFileName = read.getWorkFile().duplicate(kinpFileName);

			// finally read the data from the duplicated kinp file
			read = new ActionOnFileRead(temporaryFileName.getAbsolutePath(), Constants.kInpFileType);

			// now, directly read the data from the kinp type file
			temporarySystem.dataRead(read, Keywords.endOfFile);

		} // if end

		if (inputType.equals(Constants.g09FileType))

		{

			String kinpFileName = KisthepFile.getPrefix(temporaryFileName.getAbsolutePath()) + ".kinp";
			ActionOnFileWrite write = new ActionOnFileWrite(kinpFileName);

			// read all data from gaussian 09 freq file
			// at the same time, build the corresponding appropriate kinp input file
			// read g09 file and build the kinp file
			kinpFromG09(read, write);
			read.end();
			write.end();

			// finally read the data from the kinp type file
			// pay attention: kinpFileName is an absolute path
			read = new ActionOnFileRead(kinpFileName, Constants.kInpFileType);

			// now, directly read the data from the kinp type file
			temporarySystem.dataRead(read, Keywords.endOfFile);

		} // if end

		if (inputType.equals(Constants.gms2012FileType))

		{

			String kinpFileName = KisthepFile.getPrefix(temporaryFileName.getAbsolutePath()) + ".kinp";

			// read all data from a GAMESS2012/2013 freq file
			// at the same time, build the corresponding appropriate kinp input file
			ActionOnFileWrite write = new ActionOnFileWrite(kinpFileName);

			// read GAMESS2012/2013 file and build the kinp file
			kinpFromGms2012(read, write);
			read.end();
			write.end();
			// finally read the data from the kinp type file
			// pay attention: kinpFileName is an absolute path

			read = new ActionOnFileRead(kinpFileName, Constants.kInpFileType);

			// now, directly read the data from the kinp type file
			temporarySystem.dataRead(read, Keywords.endOfFile);

		} // if end GAMESS2012/2013

		if (inputType.equals(Constants.nwcFileType))

		{
			String kinpFileName = KisthepFile.getPrefix(temporaryFileName.getAbsolutePath()) + ".kinp";

			// read all data from a NWchem freq file
			// at the same time, build the corresponding appropriate kinp input file
			ActionOnFileWrite write = new ActionOnFileWrite(kinpFileName);

			// read NWchem file and build the kinp file
			kinpFromNwc(read, write);
			read.end();
			write.end();
			// finally read the data from the kinp type file
			// pay attention: kinpFileName is an absolute path

			read = new ActionOnFileRead(kinpFileName, Constants.kInpFileType);

			// now, directly read the data from the kinp type file
			temporarySystem.dataRead(read, Keywords.endOfFile);

		} // if end NWchem

		if (inputType.equals(Constants.orcaFileType))

		{
			String kinpFileName = KisthepFile.getPrefix(temporaryFileName.getAbsolutePath()) + ".kinp";

			// read all data from a ORCA freq file
			// at the same time, build the corresponding appropriate kinp input file
			ActionOnFileWrite write = new ActionOnFileWrite(kinpFileName);

			// read ORCA file and build the kinp file
			kinpFromOrca(read, write);
			read.end();
			write.end();
			// finally read the data from the kinp type file
			// pay attention: kinpFileName is an absolute path

			read = new ActionOnFileRead(kinpFileName, Constants.kInpFileType);

			// now, directly read the data from the kinp type file
			temporarySystem.dataRead(read, Keywords.endOfFile);

		} // if end ORCA

		if (inputType.equals(Constants.molproFileType))

		{
			String kinpFileName = KisthepFile.getPrefix(temporaryFileName.getAbsolutePath()) + ".kinp";

			// read all data from a MOLPRO freq file
			// at the same time, build the corresponding appropriate kinp input file
			ActionOnFileWrite write = new ActionOnFileWrite(kinpFileName);

			// read MOLPRO file and build the kinp file
			kinpFromMOLPRO(read, write);
			read.end();
			write.end();
			// finally read the data from the kinp type file
			// pay attention: kinpFileName is an absolute path

			read = new ActionOnFileRead(kinpFileName, Constants.kInpFileType);

			// now, directly read the data from the kinp type file
			temporarySystem.dataRead(read, Keywords.endOfFile);

		} // if end MOLPRO

		if (inputType.equals(Constants.ADFFileType))

		{

			String kinpFileName = KisthepFile.getPrefix(temporaryFileName.getAbsolutePath()) + ".kinp";
			ActionOnFileWrite write = new ActionOnFileWrite(kinpFileName);

			// read all data from ADF freq file
			// at the same time, build the corresponding appropriate kinp input file
			// read ADF file and build the kinp file
			kinpFromADF(read, write);
			read.end();
			write.end();

			// finally read the data from the kinp type file
			// pay attention: kinpFileName is an absolute path
			read = new ActionOnFileRead(kinpFileName, Constants.kInpFileType);

			// now, directly read the data from the kinp type file
			temporarySystem.dataRead(read, Keywords.endOfFile);

		} // if end ADF

		temporarySystem.testCoherenceData(); // call to the testCoherenceData method: test the coherence of data in the
												// ChemicalSystem class

		temporaryKinpFile = read.getWorkFile();
		read.end();
		return temporaryKinpFile;

	} // end of returnKinpFile

	/* M E T H O D S */
	/*************************************************************/

	/* c o m p l e t e P r o p e r t i e s */
	/***********************************************************/

	public void completeProperties() {

		// since all is ok, the atomNb property can be set
		if (atomic) {
			atomNb = 1;
		} else {
			if (linear) {
				atomNb = (vibFreq.length + 5) / 3;
			}

			else {
				atomNb = (vibFreq.length + 6) / 3;
			}
		}

		// set initial values
		initialMass = getMass();
		initialUp = getUp();
		initialElecDegener = getElecDegener();
		if (!atomic) {
			initialInertia = new double[inertia.length];
			for (int i = 0; i < inertia.length; i++) {
				initialInertia[i] = inertia[i];
			}
			initialUnscaledVibFreq = new Complex[unscaledVibFreq.length];
			for (int i = 0; i < unscaledVibFreq.length; i++) {
				initialUnscaledVibFreq[i] = (Complex) unscaledVibFreq[i].clone();
			}

			initialSymmetryNumber = symmetryNumber;
		} // end of if not atomic

	}// end of completeProperties method

	/*************************************************************/
	/* g e t V i b F r e e d o m D e g r e e s */
	/***********************************************************/

	public int getVibFreedomDegrees() {

		// useful only for non atomic systems

		if (atomic) {
			return 0;
		} else {
			return vibFreq.length;
		}
	} // end of getVibFreedomDegrees

	/*************************************************************/
	/* g e t N b H i n d e r e d R o t o r */
	/***********************************************************/

	public int getNbHinderedRotor() {

		// return the number of treated hindered rotors
		int nbHinder = 0;
		for (int i = 0; i < vibFreq.length; i++) {
			if (hrdsBarrier[i] != Constants.highEnergy) {
				nbHinder++;
			}
		}
		return nbHinder;
	} // end of getNbHinderedRotor()

	/*************************************************************/
	/* g e t N b V i b I m g */
	/***********************************************************/

	public int getNbVibImg() {

		// return the number of imaginary frequencies

		int nbImg = 0;
		for (int i = 0; i < vibFreq.length; i++) {
			if (vibFreq[i].getImagPart() != 0) {
				nbImg++;
			}
		}
		return nbImg;
	} // end of getVibImg

	/*************************************************************/
	/* g e t N b V i b R e a l */
	/***********************************************************/

	public int getNbVibReal() {

		// return the number of real frequencies

		int nbReal = 0;
		for (int i = 0; i < vibFreq.length; i++) {
			if (vibFreq[i].getImagPart() == 0) {
				nbReal++;
			}
		}
		return nbReal;
	} // end of getVibReal

	/*************************************************************/
	/* g e t 1 D R o t o r s (for RRKM calculations */
	/***********************************************************/

	public double get1DRotorsInertia() throws runTimeException {

		// useful only for non atomic and non diatomic systems
		// actually, KISTHEP manages RRKM rate constants only for non atomic and
		// diatomic systems!
		// assuming one 1D K rotor: the one with the smallest inertia moment of a
		// molecule with 3 inertia moments
		// or the only one rotor for a diatomic

		if (getVibFreedomDegrees() == 0) {
			String message = "Error in class ChemicalSystem in method get1DRotorInertia" + Constants.newLine;
			message = message + "attempt was made to get smallest Inertia moment" + Constants.newLine;
			message = message + "for an atom ..." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new runTimeException();

		}

		double smallest = inertia[0];
		for (int iInertia = 1; iInertia < inertia.length; iInertia++) {
			if (smallest > inertia[iInertia]) {
				smallest = inertia[iInertia];
			} // end if
		} // end for
		return smallest;
	} // end of get1DRotorsInertia

	/*************************************************************/
	/* g e t 2 D R o t o r s (for RRKM calculations */
	/***********************************************************/

	public double get2DRotorsInertia() throws runTimeException {

		// useful only for non atomic and non linear systems
		// actually, KISTHELP manages RRKM rate constants only for non atomic and not
		// LINEAR systems!
		// assuming one 2D rotor: the one corresponding to the largest inertia moment
		// averaging
		// for a molecule with 3 inertia moments

		double average;

		if (inertia.length != 3) {

			String message = "Error in class ChemicalSystem in method get2DRotorsInertia" + Constants.newLine;
			message = message + "attempt was made to get the two largest Inertia moments" + Constants.newLine;
			message = message + "for an atom or a diatomic system ..." + Constants.newLine;
			message = message + "KISTHELP manages RRKM rate constants only for non atomic and non LINEAR systems."
					+ Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new runTimeException();
		}

		average = inertia[0] + inertia[1] + inertia[2] - get1DRotorsInertia();
		average = average / 2.0;

		return average;
	} // end of get2DRotors

	/***********************************************/
	/* g e t T S t a r */
	/**********************************************/
	// compute the temperature
	// tStar corresponding to the saddle point
	// in the steepest descent method in evaluating Laplace inversion integral
	// Ref.: "Theory of Unimolecular Reactions", Wendel Forst, Academic press
	// New York and London, Inc., 1973

	public double getTStar(double E, int k, int r) throws runTimeException {

		// taken from Theory of Unimolecular Reactions
		// by Wendell Forst, page 112, relations 6-92
		// E: the energy at which the micro-canonical will be calculated
		// k=0 => density of states
		// k=1 => sum of states
		// r=the number of one dimensional rotors ("K rotor" + internal rotor
		// eventually)
		// used frequencies are in K

		// useful only for non atomic and non diatomic systems
		// actually, KISTHEP manages RRKM rate constants only for non atomic and
		// diatomic systems!

		if ((getVibFreedomDegrees() == 0)) {

			String message = "Error in class ChemicalSystem in method getTstar" + Constants.newLine;
			message = message + "attempt was made to calculate rovib. density of states for an atom "
					+ Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);
			throw new runTimeException();
		}

		/* apply the newton procedure */
		// newton scheme now : x = x0 - y(0)/y'(0) until convergence (x=tStar)

		double y0, y_derive0;
		double tStarBar;
		double tStar = 450; // 450 K
		double threshold = 0.01; // (in Kelvin)
		double temporary;
		double currentFrequency;

		do {

			tStarBar = tStar;

			/* compute y0 */

			// taken from Theory of Unimolecular Reactions
			// by Wendell Forst, page 112, relations 6-92
			// "r" corresponds to the rotational contribution to getTstar
			y0 = -E + (k + (double) r / 2.0) * (Constants.kb * tStarBar);

			for (int iFreq = 0; iFreq < vibFreq.length; iFreq++) {
				if (vibFreq[iFreq].getImagPart() == 0.0) { // We test the value of frequencie to avoid imaginary
															// frequencies

					currentFrequency = vibFreq[iFreq].getRealPart(); // we get the real part of complex
					temporary = currentFrequency * Constants.kb * Math.exp(-currentFrequency / tStarBar);
					temporary = temporary / (1.0 - Math.exp(-currentFrequency / tStarBar));
					y0 = y0 + temporary;

				} // end of if vibFreq[iFreq].getImagPart() == 0.0)
			} // end of for

			// "r" corresponds to the rotational contribution to getTstar
			y_derive0 = (k + (double) r / 2.0) * Constants.kb;

			for (int iFreq = 0; iFreq < vibFreq.length; iFreq++) {
				if (vibFreq[iFreq].getImagPart() == 0.0) { // We test the value of frequencie to avoid imaginary
															// frequencies

					currentFrequency = vibFreq[iFreq].getRealPart(); // we get the real part of complex

					temporary = Math.pow(currentFrequency * Constants.kb, 2) / (Constants.kb * tStarBar * tStarBar);
					temporary = temporary * Math.exp(-currentFrequency / tStarBar);
					temporary = temporary / Math.pow(1.0 - Math.exp(-currentFrequency / tStarBar), 2);

					y_derive0 = y_derive0 + temporary;

				} // end of if vibFreq[iFreq].getImagPart() == 0.0)
			} // end of for

			tStar = tStarBar - (y0 / y_derive0);

		} while (Math.abs(tStar - tStarBar) > threshold);// end of newton procedure

		return tStar;

	} // end of getTStar

	/***********************************/
	/* g e t N E T o t */ // compute total density of states (ro-vib, trans)
	/**********************************/

	public double getNETot(double E, double T, double P) throws runTimeException {

		// E in joules/molec
		// the total density of states for rovib modes and translational modes
		// is obtained by convolution using ro_vib and translational density of states
		// cf 6-32 p97 of "Theory of Unimolecular reactions by Wendel Forst, 1973
		// N_1,2(E) = int_0_to_E N1(E-x) * N2(x) dx
		// E must be > 0 and makes reference to energies above ZPE
		// thus, E only contains kinetic energy (no ZPE)
		// more over, only the electronic ground state electronic is considered
		// thus, N_tot = N_rov_vib_trans_elec = N_ro_vib_trans x P_elec(E) (P(E) =
		// electronic degeneracy)
		// in fact, P_elec does not depend on E since only the ground state is
		// considered here

		if (E < 0) {

			String message = "Error in class ChemicalSystem in method getNETot " + Constants.newLine;
			message = message + "Only positive energies are accepted" + Constants.newLine;
			message = message + "Density/summ required here for energy = " + E + " (J/molec)" + Constants.newLine;

			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);
			throw new runTimeException();
		}

		/**********************************************************************
		 * Integrate N(E) from a to b using Simpson's rule.
		 * Increase N for more precision.
		 **********************************************************************/

		double ya, yb, yx;
		double a = 0.0 + Constants.kb * T / 100; // lower limit of integration
		double b = E - Constants.kb * T / 100; // upper limit of integration
		int N = 100; // precision parameter
		double h = (b - a) / (N - 1); // step size in J/molec

		// 1/3 terms
		int k = Constants.stateDensity; // density is required

		int r; // the number of rotors to consider
		// test the case: atom, or diatomic or molecules with more than 2 atoms
		if (atomic) {
			return getNETranslation(E, T, P);
		} // a single atom

		if (inertia.length == 1) {
			r = 1;
		} // a linear molecule (can be with more than two atoms (HCN))
		else {
			r = 3;
		}

		ya = getNEWE(a, k, r); // get the ro-vibrational density of states at minimum energy on rovib = 0
		ya = ya * getNETranslation(b - a, T, P); // multiply by the translational density of states at E

		yb = getNEWE(b, k, r); // get the ro-vibrational density of states at E (all the energy is on rovib)
		yb = yb * getNETranslation(a, T, P); // multiply by the translational density of states with 0 energy

		double sum = (1.0 / 3.0) * (ya + yb); // 1/3 (f(a)+f(b))

		// 4/3 terms
		for (int i = 1; i < N - 1; i += 2) {
			double x = a + h * i;
			yx = getNEWE(x, k, r); // get the ro-vibrational density of states at Ex>0
			yx = yx * getNETranslation(b - x, T, P);// multiply by the translational density of states at E-Ex
			sum += (4.0 / 3.0) * yx; // 4/3 f(x)
		}

		// 2/3 terms
		for (int i = 2; i < N - 1; i += 2) {
			double x = a + h * i;
			yx = getNEWE(x, k, r); // get the ro-vibrational density of states at Ex> ZPE+epsilon
			yx = yx * getNETranslation(b - x, T, P);// multiply by the translational density of states at E-Ex

			sum += (2.0 / 3.0) * yx;
		}
		return (sum * h) * getElecDegener(); // to get the density of state, electronic degeneracy must be accounted for

	} // end of getNETot

	/***********************************************/
	/* g e t N E T r a n s l a t i o n */ // compute density of states for the translational mode (three)
	/**********************************************/

	public double getNETranslation(double E, double T, double P) throws runTimeException {

		// returns the translation density of states for the system of mass m
		// a semi-classical approach is employed here, rather than the Laplace-transform
		// method based on the inversion
		// of the partition function.
		// cf relation p280 of "Unimolecular reactions" by Wendel Forst, 2003
		// the density is obtained by derivation the expression of the sum of state
		// it depends on current pressure P and temperature T through the volume V
		// present in the partition function initially

		// E must be given in Joule/molec; it makes reference to the zero of
		// translational energy, being the 0 here.
		// it must be >0
		if (E < 0) {

			String message = "Error in class ChemicalSystem in method getNETranslation" + Constants.newLine;
			message = message + "Only positives energies of the considered species are accepted" + Constants.newLine;
			message = message + "Density/summ required here for energy = " + E + " (J/molec)" + Constants.newLine;

			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);
			throw new runTimeException();
		}

		double density;

		density = Math.pow(E, 0.5) / (0.5 * Math.pow(Math.PI, 0.5));
		double m = getMass() / (1000 * Constants.NA); // convert to kg/molecule
		density = density * Math.pow(2 * Math.PI * m / Math.pow(Constants.h, 2), 1.5);
		density = density * (Constants.R * T / P) / Constants.NA; // do not forget to convert to the volume occupied by
																	// one molecule

		return density; // in J^-1
	} // end of getNEtranslation

	/***********************************************/
	/* g e t N E W E */ // compute density of states or rovibrational sum of states
	/**********************************************/

	public double getNEWE(double E, int k, int r) throws runTimeException {

		// only harmonic oscillators are treated (hindered rotors are not treated)
		// r is the number of rotors
		// r must be 1 if only one-dimensional takes part in the density/sum of states :
		// it is the case in RRKM calculations where only the K-rotor (one-dimensional
		// rotor)
		// is considered in the active degrees of freedom or for diatomic molecules
		// else, in Kisthep, we can only consider r=3 if the vib+ global rotation (1
		// one-dimensional + 1 two-dimensional rotors) is needed
		// no hindered rotor are considered in this version for the density/sum of
		// states calculation

		// k=0 => density of states
		// k=1 => sum of states
		// get the temperature tStar corresponding to the saddle point
		// in the steepest descent in evaluating Laplace inversion integral
		// we consider only one one-dimensional rotator (the one with the smallest
		// inertia moment, the "K rotor")

		// Ref.: "Theory of Unimolecular Reactions", Wendel Forst, Academic press
		// New York and London, Inc., 1973
		// page 112, relations 6-92

		if ((getVibFreedomDegrees() == 0)) {

			String message = "Error in class ChemicalSystem in method getTstar" + Constants.newLine;
			message = message + "attempt was made to get to calculate rovib. density of states for an atom "
					+ Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);
			throw new runTimeException();

		}

		// E must be given in Joule/molec; it makes reference to the zero of energy
		// being the ZPE here.
		// thus, E contains only kinetic energy (no ZPE energy)
		// it must be >0
		if (E < 0) {
			String message = "Error in class ChemicalSystem in method getNEWE " + Constants.newLine;
			message = message + "Only positives energies of the considered species are accepted" + Constants.newLine;
			message = message + "Density/summ required here for energy = " + E + " (J/molec)" + Constants.newLine;

			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);
			throw new runTimeException();

		}

		// useful only for non atomic and non diatomic systems
		// actually, KISTHEP manages RRKM rate constants only for non atomic and
		// diatomic systems!

		if ((r != 1) && (r != 3)) {

			String message = "Error in class ChemicalSystem in method getNEWE " + Constants.newLine;
			message = message
					+ "only density/sum of states calculation for (a one-dimensional, r=1) or (a one-dimensional + a two-dimensional rotors, r=3) is  accepted"
					+ Constants.newLine;

			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);
			throw new runTimeException();
		}

		double tStar = getTStar(E, k, r); // relation 6-88 page 111 of "Theory of Unimolecular Reactions", Wendel Forst,
											// Academic press

		// k = 1 for sum of states, = 0 for density of states
		// r= n1 + n2 the number of one- and two-dimensional rotors, respectively
		// in this program r will be 1 (K rotator) or 3 (K rotor + a two-dimensional J
		// rotor)
		double temporary;
		double currentFrequency;
		double density, tetPhi;

		// calculate the density (or sum, depending on k=0 or k=1) of state at the
		// energy E (J)
		density = 1;
		tetPhi = (k + (double) r / 2.0) * Math.pow(Constants.kb * tStar, 2);// relation 6-92 page 112 of "Theory of
																			// Unimolecular Reactions", Wendel Forst,
																			// Academic press

		for (int iFreq = 0; iFreq < vibFreq.length; iFreq++) { // loop for scanning all the frequencies

			if (vibFreq[iFreq].getImagPart() == 0.0) { // We test the value of frequencie to avoid imaginary frequencies

				// preparing numerator
				currentFrequency = vibFreq[iFreq].getRealPart(); // we get the real part of complex
				density = density * 1.0 / (1.0 - Math.exp(-currentFrequency / tStar)); // relation 6-90 page 112 of
																						// "Theory of Unimolecular
																						// Reactions", Wendel Forst,
																						// Academic press
																						// vib partition function is
																						// used, relative to ZPE !!
				// preparing denominator
				// relation 6-92 page 112 of "Theory of Unimolecular Reactions", Wendel Forst,
				// Academic press
				temporary = Math.pow(currentFrequency * Constants.kb, 2) * Math.exp(-currentFrequency / tStar);
				temporary = temporary / Math.pow(1.0 - Math.exp(-currentFrequency / tStar), 2);
				tetPhi = tetPhi + temporary;
			}
		} // end of for

		// rotational contribution
		// note that, in our case (page 99, Forst)
		// r = n1 + n2 (one-dimensional rotors) n1 (two-dimensional rotors) n2,
		// for RRKM calculations, because two-dimensional rotors
		// do not belong to non-fixed energy modes. => r must be 1
		// but density/sum of states can be computed for the general case
		// where 3 rotations (a one-dimensional and a two-dimensional rotors) can be
		// considered: r=3 (r=n1+2n2)

		// old version:
		// density = density *
		// Math.pow(8*Math.pow(Math.PI,3)/Math.pow(Constants.h,2),(double)r/2.0);

		// new version (22/04/2013)
		density = density * Math.pow(8 * Math.pow(Math.PI, 2) / Math.pow(Constants.h, 2), (double) r / 2.0); // relation
																												// 6-42
																												// page
																												// 99 of
																												// "Theory
																												// of
																												// Unimolecular
																												// Reactions",
																												// Wendel
																												// Forst,
																												// Academic
																												// press
		density = density * Math.pow(Math.PI, 0.5); // only one rotor is considered for the one-dimensional rotors in
													// Kisthep (n1=1 always)(no hindered roors)

		// get the one-dimensional rotor inertia moment
		double inertiaMoment1D = get1DRotorsInertia();
		inertiaMoment1D = inertiaMoment1D * Constants.convertAmuToKg * Math.pow(Constants.a0, 2); // convert inertia
																									// moment from
																									// amu.bohr^2 to
																									// kg.m^2
		density = density * Math.pow(inertiaMoment1D, 0.5); // for the "K rotor"

		// IF and ONLY IF r=3 (all rotations are required for density/sum of state
		// calculation), include the
		// contribution of the two-dimensional rotor in the inertia moment contribution
		// (the other contribution of this two-dimensional rotor are already included
		// via "r" in the previous formula)

		if (r == 3) {
			// get the two-dimensional rotor inertia moment
			double inertiaMoment2D = get2DRotorsInertia();

			inertiaMoment2D = inertiaMoment2D * Constants.convertAmuToKg * Math.pow(Constants.a0, 2); // convert inertia
																										// moment from
																										// amu.bohr^2 to
																										// kg.m^2
			density = density * inertiaMoment2D; // not that for the "two-dimensional J rotor", the square root of
													// inertia moment is removed
		} // end of if r==3

		density = density * Math.exp(E / (Constants.kb * tStar));

		density = density * Math.pow(Constants.kb * tStar, k + (double) r / 2.0);
		density = density / Math.pow(2 * Math.PI * tetPhi, 0.5);
		return density;

	} // end of getNEWE

	/***********************************************/
	/* o p e n T e s t R e a d i n g F i l e */ // ASKS THE USER FOR THE FILE'S NAME, TEST ITS EXISTENCE, DETECT THE
	/**********************************************/ // OUTPUT TYPE (KISTHEP, GAUSSIAN, GAMESS, ORCA ...?), READS IT,
														// AND
														// EVENTUALLY CONVERTS IT TO KISTHEP INPUT TYPE

	public void openTestReadingFile(File temporyFileName) throws IllegalDataException, IOException, CancelException {

		ActionOnFileRead read;
		String question = Constants.askingFileString
				+ Session.getCurrentSession().getFilesToBeRead().getFirstAndRemove();

		// ask for a filename if not provided in cli
		if (temporyFileName == null) {

			temporyFileName = KisthepDialog.requireExistingFilename(question,
					new KisthepInputFileFilter(Constants.anyAllowedDataFile));
		}

		// pay attention: an absolute path is used below ; and DETECT THE OUTPUT TYPE
		// (gaussian09, or GAMESS or NWchem or ORCA or MOLPRO or kinp)
		read = new ActionOnFileRead(temporyFileName.getAbsolutePath(), Constants.anyAllowedDataFile);

		// now, we are sure that the open file is ONLY of gaussian or GAMESS or NWchem
		// or ORCA or MOLPRO or kinp file type !
		String inputType = read.getWorkFile().getType();

		if (inputType.equals(Constants.kInpFileType))

		{
			// directly read the data from the kinp type file
			dataRead(read, Keywords.endOfFile);
			read.end();
			Session.getCurrentSession().addFilenameUsed(temporyFileName.getName());

		} // if end

		if (inputType.equals(Constants.g09FileType))

		{

			String kinpFileName = KisthepFile.getPrefix(temporyFileName.getAbsolutePath()) + ".kinp";
			ActionOnFileWrite write = new ActionOnFileWrite(kinpFileName);

			// read all data from gaussian 09 freq file
			// at the same time, build the corresponding appropriate kinp input file
			// read g09 file and build the kinp file
			kinpFromG09(read, write);
			read.end();
			write.end();
			// finally read the data from the kinp type file
			// pay attention: kinpFileName is an absolute path

			read = new ActionOnFileRead(kinpFileName, Constants.kInpFileType);

			// now, directly read the data from the kinp type file
			dataRead(read, Keywords.endOfFile);
			read.end();
			// specify the baseName of the file below
			Session.getCurrentSession().addFilenameUsed(read.getWorkFile().getName());

		} // if end

		if (inputType.equals(Constants.gms2012FileType))

		{

			String kinpFileName = KisthepFile.getPrefix(temporyFileName.getAbsolutePath()) + ".kinp";

			// read all data from a GAMESS2012/2013 freq file
			// at the same time, build the corresponding appropriate kinp input file
			ActionOnFileWrite write = new ActionOnFileWrite(kinpFileName);

			// read GAMESS2012/2013 file and build the kinp file
			kinpFromGms2012(read, write);
			read.end();
			write.end();
			// finally read the data from the kinp type file
			// pay attention: kinpFileName is an absolute path

			read = new ActionOnFileRead(kinpFileName, Constants.kInpFileType);

			// now, directly read the data from the kinp type file
			dataRead(read, Keywords.endOfFile);
			read.end();
			// specify the baseName of the file below
			Session.getCurrentSession().addFilenameUsed(read.getWorkFile().getName());

		} // if end GAMESS2012/2013

		if (inputType.equals(Constants.nwcFileType))

		{

			String kinpFileName = KisthepFile.getPrefix(temporyFileName.getAbsolutePath()) + ".kinp";

			// read all data from a NWchem freq file
			// at the same time, build the corresponding appropriate kinp input file
			ActionOnFileWrite write = new ActionOnFileWrite(kinpFileName);

			// read NWchem file and build the kinp file
			kinpFromNwc(read, write);
			read.end();
			write.end();
			// finally read the data from the kinp type file
			// pay attention: kinpFileName is an absolute path

			read = new ActionOnFileRead(kinpFileName, Constants.kInpFileType);

			// now, directly read the data from the kinp type file
			dataRead(read, Keywords.endOfFile);
			read.end();
			// specify the baseName of the file below
			Session.getCurrentSession().addFilenameUsed(read.getWorkFile().getName());

		} // if end NWchem

		if (inputType.equals(Constants.orcaFileType))

		{

			String kinpFileName = KisthepFile.getPrefix(temporyFileName.getAbsolutePath()) + ".kinp";

			// read all data from a ORCA freq file
			// at the same time, build the corresponding appropriate kinp input file
			ActionOnFileWrite write = new ActionOnFileWrite(kinpFileName);

			// read ORCA file and build the kinp file
			kinpFromOrca(read, write);
			read.end();
			write.end();
			// finally read the data from the kinp type file
			// pay attention: kinpFileName is an absolute path

			read = new ActionOnFileRead(kinpFileName, Constants.kInpFileType);

			// now, directly read the data from the kinp type file
			dataRead(read, Keywords.endOfFile);
			read.end();
			// specify the baseName of the file below
			Session.getCurrentSession().addFilenameUsed(read.getWorkFile().getName());

		} // if end ORCA

		if (inputType.equals(Constants.molproFileType))

		{

			String kinpFileName = KisthepFile.getPrefix(temporyFileName.getAbsolutePath()) + ".kinp";

			// read all data from a MOLPRO freq file
			// at the same time, build the corresponding appropriate kinp input file
			ActionOnFileWrite write = new ActionOnFileWrite(kinpFileName);

			// read MOLPRO file and build the kinp file
			kinpFromMOLPRO(read, write);
			read.end();
			write.end();
			// finally read the data from the kinp type file
			// pay attention: kinpFileName is an absolute path

			read = new ActionOnFileRead(kinpFileName, Constants.kInpFileType);

			// now, directly read the data from the kinp type file
			dataRead(read, Keywords.endOfFile);
			read.end();
			// specify the baseName of the file below
			Session.getCurrentSession().addFilenameUsed(read.getWorkFile().getName());

		} // if end MOLPRO

		if (inputType.equals(Constants.ADFFileType))

		{

			String kinpFileName = KisthepFile.getPrefix(temporyFileName.getAbsolutePath()) + ".kinp";
			ActionOnFileWrite write = new ActionOnFileWrite(kinpFileName);

			// read all data from ADF freq file
			// at the same time, build the corresponding appropriate kinp input file
			// read ADF file and build the kinp file
			kinpFromADF(read, write);
			read.end();
			write.end();
			// finally read the data from the kinp type file
			// pay attention: kinpFileName is an absolute path

			read = new ActionOnFileRead(kinpFileName, Constants.kInpFileType);

			// now, directly read the data from the kinp type file
			dataRead(read, Keywords.endOfFile);
			read.end();
			// specify the baseName of the file below
			Session.getCurrentSession().addFilenameUsed(read.getWorkFile().getName());

		} // if end ADF

	} // end of openTestReadingFile

	/**********************/
	/* d a t a R e a d */
	/********************/

	// read into a KISTHEP type file and check if String read are decimal numbers,
	// or integer, ...

	public void dataRead(ActionOnFileRead read, String endOfSection) throws IllegalDataException, IOException {

		// end of section can be endOfFile (a simple kinp file is read) or
		// endOfPointSection (a reaction path is read)
		String currentLine;
		currentLine = read.oneString();

		// reading loop
		if (currentLine == null) {
			currentLine = Keywords.endOfFile;
		}

		while (!currentLine.equalsIgnoreCase(endOfSection)) {

			if (currentLine.toUpperCase().startsWith(Keywords.startOfMassInputSection)) {
				massRead(read);
				locatedSection.add(Keywords.startOfMassInputSection);
			}

			if (currentLine.toUpperCase().startsWith(Keywords.startOfFrequenciesInputSection)) {
				frequenciesRead(read);
				locatedSection.add(Keywords.startOfFrequenciesInputSection);
			}

			if (currentLine.toUpperCase().startsWith(Keywords.startOfSymmetryNumberInputSection)) {
				symmetryNumberRead(read);
				locatedSection.add(Keywords.startOfSymmetryNumberInputSection);
			}

			if (currentLine.toUpperCase().startsWith(Keywords.startOfMomentInertiaInputSection)) {
				inertiaRead(read);
				locatedSection.add(Keywords.startOfMomentInertiaInputSection);
			}

			if (currentLine.toUpperCase().startsWith(Keywords.startOfUpInputSection)) {
				potentialEnergyRead(read);
				locatedSection.add(Keywords.startOfUpInputSection);
			}

			if (currentLine.toUpperCase().startsWith(Keywords.startOfDegeneracyElectronicInputSection)) {
				electronicDegeneracyRead(read);
				locatedSection.add(Keywords.startOfDegeneracyElectronicInputSection);
			}

			if (currentLine.toUpperCase().startsWith(Keywords.startOfLinearInputSection)) {
				linearRead(read);
				locatedSection.add(Keywords.startOfLinearInputSection);
			}

			currentLine = read.oneString();

			// take care : in the case of a single input file (molecule case)
			// the expected endOfSection is endOfFile but
			// in the case of a reaction and reactionpathpoint reading,
			// endOfSection=endOfPathPoint, if currentLine == null is met before **END, user
			// forgot to put **END ...
			if (currentLine == null) {
				currentLine = Keywords.endOfFile;
			}
			if ((currentLine.equals(Keywords.endOfFile)) && (endOfSection.equals(Keywords.endOfPointSection))) {
				String message = "Error in Class ChemicalSystem, in method dataRead" + Constants.newLine;
				message = message + "while reading a reactionPathPoint section in file " + read.getWorkFile()
						+ Constants.newLine;
				message = message + "**END keywords lacks (expected to end the **POINT section)" + Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
				throw new IllegalDataException();
			}

		} // end of while

	} // end of dataRead method

	/**********************************/
	/* k i n p F r o m M O L P R O */ // build a kinp input file from data in a MOLPRO (from version 2015.1) frequency
										// outputfile
	/********************************/

	public static void kinpFromMOLPRO(ActionOnFileRead read, ActionOnFileWrite write)
			throws IllegalDataException, IOException {

		// molecular properties
		ArrayList<Complex> freq = new ArrayList<Complex>();// the frequencies are stored in a vector
		Vector inertia = new Vector(); // the inertia moments are stored in a vector
		String energy = "", degen = "";
		String symNumber = "***"; // thus, a final "***" string will signal a sym number not detected
		Boolean linearity = false; // default value
		int nAtoms = 0;
		int iAtom = -1, XYZLine = 0, ZAtom;
		int ilow = 0; // counter for the number of low frequencies
		double mass = 0.0; // thus, a final negative number will signal a mass not detected

		String[] freqBuffer = new String[7];
		String[] massBuffer, energyBuffer, symNumberBuffer;
		String[] coordsBuffer, xyzBuffer, atomBuffer, buffer;
		boolean flagMass = false, flagInertia = false, job = false, jobMolden = false, jobHessian = false;
		boolean tagFreqImagin = false, tagFreqReal = false, tagFreqLow = false, flagGetXYZ = false,
				flagTypeAtom = false;

		double[][] inertiaTensor = new double[3][3]; // inertia tensor of the molecule in order to compute the inertia
														// moments
		double[] atomMass = new double[Constants.maxAtom]; // mass of atoms in molecule
		double[][] atomXYZ = new double[Constants.maxAtom][3]; // atom cartesian coordinates

		String currentLine, stringBuffer;
		int lineNumber = 0, massLine = 0, inertiaLine = 0;
		int dotIndex;
		double decimalNumber;

		Pattern pMolden, pFreq, pFreqValue, pFreqImagin, pHessian, pAtom, pEnergy, pSymNumber;
		Pattern pJob, pMult, pFreqLow, pFreqReal, pTypeAtom;
		Pattern pTestPositDecimal, pTestNegatDecimal, pTestDecimal, pTestInt;
		Pattern pXYZ, pSym;
		Pattern pS2, pS2_1, pS2A, pNuclearCharge;

		Matcher mMolden, mFreq, mFreqValue, mFreqImagin, mHessian, mAtom, mEnergy, mSymNumber;
		Matcher mJob, mMult, mFreqLow, mFreqReal, mSym, mTypeAtom;
		Matcher mTestPositDecimal, mTestNegatDecimal, mTestDecimal, mTestInt;
		Matcher mXYZ;
		Matcher mS2, mS2_1, mS2A, mNuclearCharge;

		pEnergy = Pattern.compile(" energy=");
		pTypeAtom = Pattern.compile("Molecule type: Atom");
		pNuclearCharge = Pattern.compile("NUCLEAR CHARGE");
		pSym = Pattern.compile("Rotational Symmetry factor");
		pMolden = Pattern.compile("in style MOLDEN");
		pMult = Pattern.compile("SPIN SYMMETRY");
		pFreq = Pattern.compile("HESSIAN");
		pFreqValue = Pattern.compile("Wavenumbers");
		pFreqLow = Pattern.compile("Normal Modes of low");
		pTestInt = Pattern.compile("^[1-9][0-9]*$");
		pTestPositDecimal = Pattern.compile("^[0-9]+\\.[0-9]*$");
		pJob = Pattern.compile("Molpro calculation terminated");
		pTestNegatDecimal = Pattern.compile("^[-][0-9]+\\.[0-9]*$");
		pTestDecimal = Pattern.compile("^[-]?[0-9]+\\.[0-9]*$");
		pXYZ = Pattern.compile("Atomic Coordinates");

		pFreqReal = Pattern.compile("Normal Modes");
		pFreqImagin = Pattern.compile("Normal Modes of imaginary frequencies");

		currentLine = read.oneString();

		/************************************************/
		/* FIRST: filtering the MOLPRO output file */
		/************************************************/
		symNumber = "1"; // !! MOLPRO supplies the symmetry number only if the thermo analysis has been
							// required;
		// if this pattern is not found, then, default is 1 here;
		while (currentLine != null) {

			lineNumber = lineNumber + 1;
			/* C H E C K H E S S I A N R U N T Y P E */
			mFreq = pFreq.matcher(currentLine);
			if (mFreq.find()) {
				jobHessian = true;
			}

			/* C H E C K NORMAL TERMINATION */
			mJob = pJob.matcher(currentLine);
			if (mJob.find()) {
				job = true;
			}

			/* MOLDEN OUTPUT */
			mMolden = pMolden.matcher(currentLine);
			if (mMolden.find()) {
				jobMolden = true;
			}

			/* POTENTIAL ENERGY */

			mEnergy = pEnergy.matcher(currentLine);
			if (mEnergy.find() && jobMolden) {
				buffer = currentLine.split(" +");
				energy = buffer[3];
			}

			/* M U L T I P L I C I T Y */
			// string "SPIN SYMMETRY" is searched for
			mMult = pMult.matcher(currentLine);
			if (mMult.find()) {
				buffer = currentLine.split(" +");
				degen = buffer[10];

				// multiplicity is currently a text => convert it in an integer
				if (degen.equals("Singlet")) {
					degen = "1";
				} else if (degen.equals("Doublet")) {
					degen = "2";
				} else if (degen.equals("Triplet")) {
					degen = "3";
				} else if (degen.equals("Quartet")) {
					degen = "4";
				} else if (degen.equals("Quintet")) {
					degen = "5";
				} else if (degen.equals("sextet")) {
					degen = "6";
				} else if (degen.equals("septet")) {
					degen = "7";
				} else if (degen.equals("octet")) {
					degen = "8";
				} else if (degen.equals("nonet")) {
					degen = "9";
				} else
					degen = "";
			}

			/*
			 * IMAGINARY VIBRATIONAL FREQUENCIES (in MOLPRO, real and imaginary frequencies
			 * are reported in two different parts)
			 */
			mFreqImagin = pFreqImagin.matcher(currentLine);
			if (mFreqImagin.find()) {
				tagFreqImagin = true;
				tagFreqReal = false;
			}

			mFreqValue = pFreqValue.matcher(currentLine);
			if (tagFreqImagin && mFreqValue.find()) {
				freqBuffer = currentLine.split(" +");
				for (int iFreq = 3; iFreq <= freqBuffer.length - 1; iFreq++) {
					mTestDecimal = pTestDecimal.matcher(freqBuffer[iFreq]);
					if (!mTestDecimal.find()) {
						write.end(); // end the file building
						String message = "Error in Class ChemicalSystem, in method kinpFromMOLPRO" + Constants.newLine;
						message = message + "while reading vibrational frequencies in file " + read.getWorkFile()
								+ Constants.newLine;
						message = message + "=> " + freqBuffer[iFreq] + "  : not a decimal number" + Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);

						throw new IllegalDataException();
					} // if end
					freq.add(new Complex(0, Math.abs(Double.parseDouble(freqBuffer[iFreq]))));
				} // FOR END

			} // IF END

			/*
			 * LOW mode VIBRATIONAL FREQUENCIES (in MOLPRO, real and imaginary frequencies
			 * are reported in two different parts)
			 */
			/* low modes : end of reading real or imaginary frequencies */

			mFreqLow = pFreqLow.matcher(currentLine);
			if (mFreqLow.find()) {
				tagFreqImagin = false;
				tagFreqReal = false;
				tagFreqLow = true;
				ilow = 0;
			}

			mFreqValue = pFreqValue.matcher(currentLine);
			if (tagFreqLow && mFreqValue.find()) {
				freqBuffer = currentLine.split(" +");
				for (int iFreq = 3; iFreq <= freqBuffer.length - 1; iFreq++) {
					ilow = ilow + 1;
					mTestDecimal = pTestDecimal.matcher(freqBuffer[iFreq]);
					if (!mTestDecimal.find()) {
						write.end(); // end the file building
						String message = "Error in Class ChemicalSystem, in method kinpFromMOLPRO" + Constants.newLine;
						message = message + "while reading vibrational frequencies in file " + read.getWorkFile()
								+ Constants.newLine;
						message = message + "=> " + freqBuffer[iFreq] + "  : not a decimal number" + Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);
						throw new IllegalDataException();
					} // if end
					/*
					 * in MOLPRO, the section " Normal Modes of low/zero frequencies"
					 * may contain 6 or more low values ! When 7 or more values are read, the values
					 * beyond
					 * 6 corresponds likely to hindered rotors and have to be taken in the set of
					 * vib frequencies
					 */
					if (ilow > 6) {
						freq.add(new Complex(Double.parseDouble(freqBuffer[iFreq]), 0));
					}
				} // FOR END
			} // end of if (tagFreqLow && mFreqValue.find())

			/*
			 * REAL VIBRATIONAL FREQUENCIES (in MOLPRO, real and imaginary frequencies are
			 * reported in two different parts)
			 */
			mFreqReal = pFreqReal.matcher(currentLine);
			if (mFreqReal.find() && (!tagFreqImagin) && (!tagFreqLow)) {
				tagFreqReal = true;
			}

			mFreqValue = pFreqValue.matcher(currentLine);
			if (tagFreqReal && mFreqValue.find()) {
				freqBuffer = currentLine.split(" +");
				for (int iFreq = 3; iFreq <= freqBuffer.length - 1; iFreq++) {
					mTestDecimal = pTestDecimal.matcher(freqBuffer[iFreq]);
					if (!mTestDecimal.find()) {
						write.end(); // end the file building
						String message = "Error in Class ChemicalSystem, in method kinpFromMOLPRO" + Constants.newLine;
						message = message + "while reading vibrational frequencies in file " + read.getWorkFile()
								+ Constants.newLine;
						message = message + "=> " + freqBuffer[iFreq] + "  : not a decimal number" + Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);
						throw new IllegalDataException();
					} // if end

					freq.add(new Complex(Double.parseDouble(freqBuffer[iFreq]), 0));
				} // FOR END

			} // IF END

			/* MASS */
			// in MOLPRO, since it is mandatory to read cartesian coordinates with atom
			// masses
			// the total mass can be obtained from the atom mass sum using data stored in
			// Kisthelp
			// --> for an atom, there is no need to perform a frequency calculation within
			// MOLPRO
			// atom type or molecule ?
			mTypeAtom = pTypeAtom.matcher(currentLine);
			if (mTypeAtom.find()) {
				flagTypeAtom = true;
				nAtoms = 1;
			}

			mNuclearCharge = pNuclearCharge.matcher(currentLine);
			if (mNuclearCharge.find() && flagTypeAtom) {
				buffer = currentLine.split(" +");
				dotIndex = buffer[3].indexOf(".");
				if (dotIndex > 0) { // the nuclear charge is given as a real number
					ZAtom = Integer.parseInt(buffer[3].substring(0, dotIndex));
					atomMass[0] = Constants.getAtomicMass(ZAtom); // mass
					mass = atomMass[0]; // total mass here !
					flagTypeAtom = false;
				} // end of if (dotIndex>0)
				else {// the nuclear charge is given as an integer directly
					ZAtom = Integer.parseInt(buffer[3]);
					atomMass[0] = Constants.getAtomicMass(ZAtom); // mass
					mass = atomMass[0]; // total mass here !
					flagTypeAtom = false;
				}
			} // end of if (mNuclearCharge.find() && flagTypeAtom) {

			// EXTRACT XYZ coordinates and deduce atomic MASS to compute inertia moments
			mXYZ = pXYZ.matcher(currentLine);
			if (mXYZ.find() && jobHessian) {
				flagGetXYZ = true;
				XYZLine = lineNumber;
				iAtom = -1;
				mass = 0.0;
			}

			if (flagGetXYZ && (lineNumber >= (XYZLine + 4))) {

				buffer = currentLine.split(" +");
				if (buffer.length == 7) {

					iAtom = iAtom + 1;
					ZAtom = Integer.parseInt(buffer[3].substring(0, buffer[3].indexOf(".")));
					atomMass[iAtom] = Constants.getAtomicMass(ZAtom); // mass
					mass = mass + atomMass[iAtom]; // compute the total mass here !
					atomXYZ[iAtom][0] = Double.parseDouble(buffer[4]); // X
					atomXYZ[iAtom][1] = Double.parseDouble(buffer[5]); // Y
					atomXYZ[iAtom][2] = Double.parseDouble(buffer[6]); // Z
				} // end if (buffer.length == 6)
				else { // detect the end of the XYZ/mass section
					flagGetXYZ = false;
					nAtoms = iAtom + 1;

					// if and only if nAtoms > 1 : compute inertia moments
					if (nAtoms > 1) {
						// Construct the inertia tensor elements
						// XX
						inertiaTensor[0][0] = 0.0;
						for (int iat = 0; iat < nAtoms; iat++) {
							inertiaTensor[0][0] = inertiaTensor[0][0] + atomMass[iat] * (Math.pow(atomXYZ[iat][1], 2) +
									Math.pow(atomXYZ[iat][2], 2));
						}

						// YY
						inertiaTensor[1][1] = 0.0;
						for (int iat = 0; iat < nAtoms; iat++) {
							inertiaTensor[1][1] = inertiaTensor[1][1] + atomMass[iat] * (Math.pow(atomXYZ[iat][0], 2) +
									Math.pow(atomXYZ[iat][2], 2));
						}
						// ZZ
						inertiaTensor[2][2] = 0.0;
						for (int iat = 0; iat < nAtoms; iat++) {
							inertiaTensor[2][2] = inertiaTensor[2][2] + atomMass[iat] * (Math.pow(atomXYZ[iat][0], 2) +
									Math.pow(atomXYZ[iat][1], 2));
						}
						// XY
						inertiaTensor[0][1] = 0.0;
						for (int iat = 0; iat < nAtoms; iat++) {
							inertiaTensor[0][1] = inertiaTensor[0][1]
									- atomMass[iat] * atomXYZ[iat][0] * atomXYZ[iat][1];
						}
						inertiaTensor[1][0] = inertiaTensor[0][1];

						// XZ
						inertiaTensor[0][2] = 0.0;
						for (int iat = 0; iat < nAtoms; iat++) {
							inertiaTensor[0][2] = inertiaTensor[0][2]
									- atomMass[iat] * atomXYZ[iat][0] * atomXYZ[iat][2];
						}
						inertiaTensor[2][0] = inertiaTensor[0][2];
						// YZ
						inertiaTensor[1][2] = 0.0;
						for (int iat = 0; iat < nAtoms; iat++) {
							inertiaTensor[1][2] = inertiaTensor[1][2]
									- atomMass[iat] * atomXYZ[iat][1] * atomXYZ[iat][2];
						}
						inertiaTensor[2][1] = inertiaTensor[1][2];

						/* INERTIA MOMENTS and molecular Linearity */
						RealMatrix mat = new Array2DRowRealMatrix(inertiaTensor);
						EigenDecomposition decomp = new EigenDecomposition(mat);

						// detect linearity of the molecule
						double a = decomp.getRealEigenvalue(0);
						double b = decomp.getRealEigenvalue(1);
						double c = decomp.getRealEigenvalue(2);
						if ((a < Constants.inertiaEpsilon2) && (Math.abs(b - c) < Constants.inertiaEpsilon2)) {
							linearity = true;
							inertia.add(b);
						}

						if ((b < Constants.inertiaEpsilon2) && (Math.abs(c - a) < Constants.inertiaEpsilon2)) {
							linearity = true;
							inertia.add(c);
						}

						if ((c < Constants.inertiaEpsilon2) && (Math.abs(a - b) < Constants.inertiaEpsilon2)) {
							linearity = true;
							inertia.add(a);
						}

						if (!linearity) {
							inertia.add(a);
							inertia.add(b);
							inertia.add(c);
						}

					} // end of if (nAtoms>1)

				} // end of else { // detect the end of the XYZ/mass section
					// and compute inertia moments
			} // end of if (flagGetXYZ && (lineNumber>=(XYZLine+4)) )

			/* ROTATIONAL SYMMETRY NUMBER */
			mSym = pSym.matcher(currentLine);
			if (mSym.find()) {
				buffer = currentLine.split(" +");
				dotIndex = buffer[4].indexOf(".");
				if (dotIndex > 0) { // the sym number is given as a real number
					symNumber = buffer[4].substring(0, dotIndex);
				} else {
					symNumber = buffer[4];
				}
			} // end of if (mSym.find())

			currentLine = read.oneString();

		} // while end

		// ********************************************//
		// CHECKING properties existence //
		// ********************************************//

		// first of all: the job must be a Hessian job (only if nAtoms > 1)
		if (!jobHessian && (nAtoms > 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromMOLPRO" + Constants.newLine;
			message = message + "while checking the type of job in the outputfile " + read.getWorkFile()
					+ Constants.newLine;
			message = message + "=> a FREQUENCIES job is required" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException(); // if end
		}

		// JOB normally terminated ?
		if (!job) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromMOLPRO" + Constants.newLine;
			message = message + "while reading MOLPRO output file " + read.getWorkFile() + Constants.newLine;
			message = message + " string ** Molpro calculation terminated not found **, ..." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		/*
		 * ATOM number : needed to avoid printing Inertia moment and linearity
		 * information
		 */
		// checking the detected atom number
		if (nAtoms == 0) {
			write.end(); // end the file building
			String message = "Error in Class ChemicalSystem, in method kinpFromMOLPRO" + Constants.newLine;
			message = message + "while reading atomic coordinates, no atom found in file " + read.getWorkFile()
					+ Constants.newLine;
			message = message + "check section ** Atomic Coordinates **" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* ENERGY */

		if (energy.isEmpty()) {
			write.end(); // end the file building
			String message = "Error in Class ChemicalSystem, in method kinpFromMOLPRO" + Constants.newLine;
			message = message + "energy not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + " ** in style MOLDEN ** pattern  not found" + Constants.newLine;
			message = message + " or ** energy= ** pattern  not found" + Constants.newLine;
			message = message + " Use the ** put,molden MOLPRO input option **" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* DEGENERACY */
		if (degen.isEmpty()) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromMOLPRO" + Constants.newLine;
			message = message + "spin multiplicity not found in " + read.getWorkFile() + Constants.newLine;
			message = message + "** SPIN SYMMETRY ** pattern not found" + Constants.newLine;
			message = message + "or spin multiplicity > 9" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* ROTATIONAL SYMETRY NUMBER */
		// !! here, in MOLPRO, this rotational symmetry number is either detected or not
		// yet supplied ... then, default is 1 in Kisthelp

		/* FREQUENCIES */

		if ((freq.size() == 0) && (nAtoms > 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromMOLPRO" + Constants.newLine;
			message = message + "vibrational frequencies not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "** HESSIAN **  pattern not found" + Constants.newLine;
			message = message + "or ** Wavenumbers **  pattern not found" + Constants.newLine;
			message = message + "or ** Normal Modes **  pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* INERTIA MOMENTS */

		if ((inertia.size() == 0) && (nAtoms > 1)) {
			write.end(); // end the file building
			String message = "Error in Class ChemicalSystem, in method kinpFromMOLPRO" + Constants.newLine;
			message = message + "Cartesian coordinates not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "check section ** Atomic Coordinates **" + Constants.newLine;
			message = message + "This is required to compute inertia moments." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		// ********************************************//
		// NOW, WRITING properties on kinp input file //
		// ********************************************//

		/* MASS */

		write.oneString("*MASS (in amu)");
		write.oneDouble(mass);
		write.oneString("*END");

		/* ROTATIONAL SYMMETRY NUMBER IF AND ONLY IF NON-ATOMIC SYSTEM */

		if (nAtoms > 1) {
			write.oneString("*NUMBER OF SYMMETRY");
			write.oneString(symNumber); // always 1 here in ORCA since this number is not yet supplied by ORCA ...
			write.oneString("*END");
		} // if end

		/* VIBRATIONAL FREQUENCIES */

		if (nAtoms > 1) {
			// first check number of freq. consistency
			if ((linearity && freq.size() != (3 * nAtoms - 5)) || (!linearity && freq.size() != (3 * nAtoms - 6))) {
				{
					write.end(); // end the file building
					String message = "Error in Class ChemicalSystem, in method kinpFromMOLPRO" + Constants.newLine;
					message = message
							+ "while reading vibrational frequencies for a system with more than one atom in file "
							+ read.getWorkFile() + Constants.newLine;
					message = message
							+ "(linear molecule and nb of freq. !=3N-5) OR ( not linear && nb of freq. != 3N-6) detected"
							+ Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

					throw new IllegalDataException();
				} // if end
			}

			// first, remove the 6 or 5 lowest REAL frequencies (rotations, translations)
			// !! Here in ORCA, these low frequencies are not read by Kisthelp, in contrast
			// with GAMESS OUTPUT

			write.oneString("*FREQUENCIES (in cm-1)");
			String imaginaryFreq = "";

			for (int iFreq = 0; iFreq < freq.size(); iFreq++) {

				// testing a possible null frequency
				if ((freq.get(iFreq).getRealPart() == 0) && (freq.get(iFreq).getImagPart() == 0)) {
					{
						write.end(); // end the file building

						String message = "Error in Class ChemicalSystem, in method kinpFromMOLPRO" + Constants.newLine;
						message = message + "while reading vibrational frequencies in file " + read.getWorkFile()
								+ Constants.newLine;
						message = message + "a zero frequency has been found" + Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);

						throw new IllegalDataException();
					} // if end
				}

				// testing an imaginary frequency
				if (freq.get(iFreq).getImagPart() == 0) {
					write.oneDouble(freq.get(iFreq).getRealPart());
				} else {
					write.oneString(String.valueOf(freq.get(iFreq).getImagPart()) + "i");
				}

			} // for end
			write.oneString("*END");
		} // if nAtoms>1 end

		/* ELECTRONIC DEGENERACY */

		write.oneString("*ELECTRONIC DEGENERACY");
		write.oneString(degen);
		write.oneString("*END");

		/* INERTIA MOMENTS */

		if (nAtoms > 1) {

			write.oneString("*MOMENT OF INERTIA (in Amu.bohr**2)");

			if (!linearity) { // three components to be written
				for (int iInertia = 0; iInertia < 3; iInertia++) {
					write.oneDouble((Double) inertia.get(iInertia));
				} // for end

			} // if end
			else { // only one component to be written
				write.oneDouble((Double) inertia.get(0));
			} // else end

			write.oneString("*END");

			/* LINEARITY */
			write.oneString("*LINEAR");
			if (linearity) {
				write.oneString("linear");
			} else {
				write.oneString("not linear");
			}
			write.oneString("*END");

		} // end of if (nAtoms >1)

		/* POTENTIAL ENERGY */
		write.oneString("*POTENTIAL ENERGY (in hartree)");
		write.oneString(energy);
		write.oneString("*END");

	} // end of kinpFromgMOLPRO method

	/**********************************/
	/* k i n p F r o m O R C A */ // build a kinp input file from data in a ORCA (from version 4.0.1.2) frequency
									// outputfile
	/********************************/

	public static void kinpFromOrca(ActionOnFileRead read, ActionOnFileWrite write)
			throws IllegalDataException, IOException {

		// molecular properties
		ArrayList<Complex> freq = new ArrayList<Complex>();// the frequencies are stored in a vector
		Vector inertia = new Vector(); // the inertia moments are stored in a vector
		String energy = "", degen = "";
		String symNumber = "***"; // thus, a final "***" string will signal a sym number not detected
		Boolean linearity = false; // default value
		int nAtoms = 0;
		int iAtom = -1;
		double mass = 0.0; // thus, a final negative number will signal a mass not detected

		String[] freqBuffer = new String[7];
		String[] massBuffer, energyBuffer, symNumberBuffer;
		String[] coordsBuffer, xyzBuffer, atomBuffer, buffer;
		boolean flagMass = false, flagInertia = false, job = false, jobHessian = false;

		double[][] inertiaTensor = new double[3][3]; // inertia tensor of the molecule in order to compute the inertia
														// moments
		double[] atomMass = new double[Constants.maxAtom]; // mass of atoms in molecule
		double[][] atomXYZ = new double[Constants.maxAtom][3]; // atom cartesian coordinates

		String currentLine, stringBuffer;
		int lineNumber = 0, massLine = 0, inertiaLine = 0;
		double decimalNumber;

		Pattern pFreq, pFreqImagin, pHessian, pAtom, pEnergy, pSymNumber;
		Pattern pJob, pMult;
		Pattern pTestPositDecimal, pTestNegatDecimal, pTestDecimal, pTestInt;
		Pattern pXYZ;
		Pattern pS2, pS2_1, pS2A;

		Matcher mFreq, mFreqImagin, mHessian, mAtom, mEnergy, mSymNumber;
		Matcher mJob, mMult;
		Matcher mTestPositDecimal, mTestNegatDecimal, mTestDecimal, mTestInt;
		Matcher mXYZ;
		Matcher mS2, mS2_1, mS2A;

		pEnergy = Pattern.compile("FINAL SINGLE POINT ENERGY");
		pMult = Pattern.compile("Multiplicity           Mult ");
		pFreq = Pattern.compile("freq\\. ");
		pTestInt = Pattern.compile("^[1-9][0-9]*$");
		pTestPositDecimal = Pattern.compile("^[0-9]+\\.[0-9]*$");
		pJob = Pattern.compile("ORCA TERMINATED NORMALLY");
		pTestNegatDecimal = Pattern.compile("^[-][0-9]+\\.[0-9]*$");
		pTestDecimal = Pattern.compile("^[-]?[0-9]+\\.[0-9]*$");
		pXYZ = Pattern.compile("CARTESIAN COORDINATES \\(A\\.U\\.\\)");

		// temporary: to calculate the coordinate center of mass
		double xcom, ycom, zcom;
		xcom = 0.0;
		ycom = 0.0;
		zcom = 0.0;

		pFreqImagin = Pattern.compile("imaginary mode");
		currentLine = read.oneString();

		/************************************************/
		/* FIRST: filtering the ORCA output file */
		/************************************************/
		while (currentLine != null) {
			lineNumber = lineNumber + 1;
			/* C H E C K H E S S I A N R U N T Y P E */
			mFreq = pFreq.matcher(currentLine);
			if (mFreq.find()) {
				jobHessian = true;
			}

			/* C H E C K NORMAL TERMINATION */
			mJob = pJob.matcher(currentLine);
			if (mJob.find()) {
				job = true;
			}

			/* POTENTIAL ENERGY */
			mEnergy = pEnergy.matcher(currentLine);
			if (mEnergy.find()) {
				buffer = currentLine.split(" +");
				energy = buffer[4];
			}

			/* M U L T I P L I C I T Y */
			mMult = pMult.matcher(currentLine);
			if (mMult.find()) {
				buffer = currentLine.split(" +");
				degen = buffer[4];
			}

			/*
			 * IMAGINARY VIBRATIONAL FREQUENCIES (in ORCA, real and imaginary frequencies
			 * are reported in two different parts)
			 */
			/*
			 * pay attention with ORCA: imaginary frequencies are highlighted with the
			 * "imaginary" text
			 */
			mFreqImagin = pFreqImagin.matcher(currentLine);
			if (mFreqImagin.find()) {
				freqBuffer = currentLine.split(" +");
				mTestDecimal = pTestDecimal.matcher(freqBuffer[2]);
				if (!mTestDecimal.find()) {
					write.end(); // end the file building

					String message = "Error in Class ChemicalSystem, in method kinpFromOrca" + Constants.newLine;
					message = message + "while reading vibrational frequencies in file " + read.getWorkFile()
							+ Constants.newLine;
					message = message + "=> " + freqBuffer[2] + "  : not a decimal number" + Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
					throw new IllegalDataException();
				} // if end
				freq.add(new Complex(0, Math.abs(Double.parseDouble(freqBuffer[2]))));

			} // IF END

			/*
			 * REAL VIBRATIONAL FREQUENCIES (in ORCA, real and imaginary frequencies are
			 * reported in two different parts)
			 */
			/*
			 * pay attention with ORCA: imaginary frequencies are highlighted with the
			 * "imaginary" text
			 */
			mFreq = pFreq.matcher(currentLine);
			if (mFreq.find()) {
				freqBuffer = currentLine.split(" +");
				mTestDecimal = pTestDecimal.matcher(freqBuffer[1]);
				if (!mTestDecimal.find()) {
					write.end(); // end the file building

					String message = "Error in Class ChemicalSystem, in method kinpFromOrca" + Constants.newLine;
					message = message + "while reading vibrational frequencies in file " + read.getWorkFile()
							+ Constants.newLine;
					message = message + "=> " + freqBuffer[1] + "  : not a decimal number" + Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
					throw new IllegalDataException();
				} // if end
				freq.add(new Complex(Double.parseDouble(freqBuffer[1]), 0));

			} // IF END

			/* MASS */
			// in Orca, since it is mandatory to read cartesian coordinates with atom masses
			// the total mass can be obtained from the atom mass sum : no need for mass
			// matcher
			// --> for an atom, there is no need to perform a frequency calculation within
			// ORCA

			// EXTRACT XYZ coordinates and atomic MASS to compute inertia moments
			mXYZ = pXYZ.matcher(currentLine);
			if (mXYZ.find()) {

				xcom = 0.0;
				ycom = 0.0;
				zcom = 0.0;
				flagMass = true;
				massLine = lineNumber;
				iAtom = -1;
				mass = 0.0;
				// erase the inertia vector
				inertia.clear();
			}

			if (flagMass && (lineNumber >= (massLine + 3))) { // after coordinate starting block
				buffer = currentLine.split(" +");

				if (buffer.length != 9) {// detect the end of the XYZ/mass section => treatment
					flagMass = false;
					nAtoms = iAtom + 1;

					// if and only if nAtoms > 1 : compute inertia moments
					if (nAtoms > 1) {

						// compute the center of mass

						for (int iat = 0; iat < nAtoms; iat++) {

							xcom = xcom + atomXYZ[iat][0] * atomMass[iat];
							ycom = ycom + atomXYZ[iat][1] * atomMass[iat];
							zcom = zcom + atomXYZ[iat][2] * atomMass[iat];

						} // end of for (int iat=0; iat<nAtoms; iat++){
						xcom = xcom / mass;
						ycom = ycom / mass;
						zcom = zcom / mass;

						// change the coordinate center
						for (int iat = 0; iat < nAtoms; iat++) {

							atomXYZ[iat][0] = atomXYZ[iat][0] - xcom;
							atomXYZ[iat][1] = atomXYZ[iat][1] - ycom;
							atomXYZ[iat][2] = atomXYZ[iat][2] - zcom;

						} //

						// Construct the inertia tensor elements
						// XX
						inertiaTensor[0][0] = 0.0;
						for (int iat = 0; iat < nAtoms; iat++) {
							inertiaTensor[0][0] = inertiaTensor[0][0] + atomMass[iat] * (Math.pow(atomXYZ[iat][1], 2) +
									Math.pow(atomXYZ[iat][2], 2));
						}

						// YY
						inertiaTensor[1][1] = 0.0;
						for (int iat = 0; iat < nAtoms; iat++) {
							inertiaTensor[1][1] = inertiaTensor[1][1] + atomMass[iat] * (Math.pow(atomXYZ[iat][0], 2) +
									Math.pow(atomXYZ[iat][2], 2));
						}
						// ZZ
						inertiaTensor[2][2] = 0.0;
						for (int iat = 0; iat < nAtoms; iat++) {
							inertiaTensor[2][2] = inertiaTensor[2][2] + atomMass[iat] * (Math.pow(atomXYZ[iat][0], 2) +
									Math.pow(atomXYZ[iat][1], 2));
						}
						// XY
						inertiaTensor[0][1] = 0.0;
						for (int iat = 0; iat < nAtoms; iat++) {
							inertiaTensor[0][1] = inertiaTensor[0][1]
									- atomMass[iat] * atomXYZ[iat][0] * atomXYZ[iat][1];
						}
						inertiaTensor[1][0] = inertiaTensor[0][1];

						// XZ
						inertiaTensor[0][2] = 0.0;
						for (int iat = 0; iat < nAtoms; iat++) {
							inertiaTensor[0][2] = inertiaTensor[0][2]
									- atomMass[iat] * atomXYZ[iat][0] * atomXYZ[iat][2];
						}
						inertiaTensor[2][0] = inertiaTensor[0][2];
						// YZ
						inertiaTensor[1][2] = 0.0;
						for (int iat = 0; iat < nAtoms; iat++) {
							inertiaTensor[1][2] = inertiaTensor[1][2]
									- atomMass[iat] * atomXYZ[iat][1] * atomXYZ[iat][2];
						}
						inertiaTensor[2][1] = inertiaTensor[1][2];

						/* INERTIA MOMENTS and molecular Linearity */
						RealMatrix mat = new Array2DRowRealMatrix(inertiaTensor);
						EigenDecomposition decomp = new EigenDecomposition(mat);

						// detect linearity of the molecule
						double a = decomp.getRealEigenvalue(0);
						double b = decomp.getRealEigenvalue(1);
						double c = decomp.getRealEigenvalue(2);

						// case of linear and not diatomic
						if ((a < Constants.inertiaEpsilon2) && (Math.abs(b - c) < Constants.inertiaEpsilon2)) {
							linearity = true;
							inertia.add(b);
						}

						if ((b < Constants.inertiaEpsilon2) && (Math.abs(c - a) < Constants.inertiaEpsilon2)) {
							linearity = true;
							inertia.add(c);
						}

						if ((c < Constants.inertiaEpsilon2) && (Math.abs(a - b) < Constants.inertiaEpsilon2)) {
							linearity = true;
							inertia.add(a);
						}

						// case of not linear molecule
						if (!linearity) {
							inertia.add(a);
							inertia.add(b);
							inertia.add(c);
						}

					} // end of if (nAtoms>1)
				} // if (buffer.length != 9) : end of after the if coord / mass matrix : treatment
				if (flagMass) {
					iAtom = iAtom + 1;
					atomMass[iAtom] = Double.parseDouble(buffer[5]); // mass
					mass = mass + atomMass[iAtom]; // compute the total mass here !
					atomXYZ[iAtom][0] = Double.parseDouble(buffer[6]); // X
					atomXYZ[iAtom][1] = Double.parseDouble(buffer[7]); // Y
					atomXYZ[iAtom][2] = Double.parseDouble(buffer[8]); // Z
				} // if flagMass end
			} // end of if (flagMass && (lineNumber>=(massLine+3)) )

			/* ROTATIONAL SYMMETRY NUMBER */
			symNumber = "1"; // !! ORCA does not yet supply the symmetry number; default is 1 here; this is
								// to say: there is no problem
								// in processing symmetry here

			currentLine = read.oneString();
		} // while end

		// ********************************************//
		// CHECKING properties existence //
		// ********************************************//

		// first of all: the job must be a Hessian job (only if nAtoms > 1)
		if (!jobHessian && (nAtoms > 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromOrca" + Constants.newLine;
			message = message + "while checking the type of job in the outputfile " + read.getWorkFile()
					+ Constants.newLine;
			message = message + "=> a FREQ job is required" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException(); // if end
		}

		// JOB normally terminated ?
		if (!job) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromOrca" + Constants.newLine;
			message = message + "while reading ORCA output file " + read.getWorkFile() + Constants.newLine;
			message = message + " string EXECUTION OF GAMESS TERMINATED NORMALLY not found, ..." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		/*
		 * ATOM number : needed to avoid printing Inertia moment and linearity
		 * information
		 */
		// checking the detected atom number
		if (nAtoms == 0) {
			write.end(); // end the file building
			String message = "Error in Class ChemicalSystem, in method kinpFromOrca" + Constants.newLine;
			message = message + "while reading atomic mass, no atom found in file " + read.getWorkFile()
					+ Constants.newLine;
			message = message + " string ORCA TERMINATED NORMALLY not found, ..." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* ENERGY */

		if (energy.isEmpty()) {
			write.end(); // end the file building
			String message = "Error in Class ChemicalSystem, in method kinpFromOrca" + Constants.newLine;
			message = message + "energy not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "**FINAL SINGLE POINT ENERGY** pattern  not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* DEGENERACY */
		if (degen.isEmpty()) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromOrca" + Constants.newLine;
			message = message + "spin multiplicity not found in " + read.getWorkFile() + Constants.newLine;
			message = message + "**Multiplicity           Mult ** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* ROTATIONAL SYMETRY NUMBER */
		// !! here, in ORCA, this rotational symmetry number is not yet supplied ...
		// then, default is 1 in Kisthelp

		/* FREQUENCIES */

		if ((freq.size() == 0) && (nAtoms > 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromOrca" + Constants.newLine;
			message = message + "vibrational frequencies not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "**freq.**  pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* INERTIA MOMENTS */

		if ((inertia.size() == 0) && (nAtoms > 1)) {
			write.end(); // end the file building
			String message = "Error in Class ChemicalSystem, in method kinpFromOrca" + Constants.newLine;
			message = message + "Cartesian coordinates not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "**CARTESIAN COORDINATES (A.U.)** pattern not found" + Constants.newLine;
			message = message + "This is required to compute inertia moments." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		// ********************************************//
		// NOW, WRITING properties on kinp input file //
		// ********************************************//

		/* MASS */

		write.oneString("*MASS (in amu)");
		write.oneDouble(mass);
		write.oneString("*END");

		/* ROTATIONAL SYMMETRY NUMBER IF AND ONLY IF NON-ATOMIC SYSTEM */

		if (nAtoms > 1) {
			write.oneString("*NUMBER OF SYMMETRY");
			write.oneString(symNumber); // always 1 here in ORCA since this number is not yet supplied by ORCA ...
			write.oneString("*END");
		} // if end

		/* VIBRATIONAL FREQUENCIES */

		if (nAtoms > 1) {
			// first check number of freq. consistency

			if ((linearity && freq.size() != (3 * nAtoms - 5)) || (!linearity && freq.size() != (3 * nAtoms - 6))) {
				{
					write.end(); // end the file building
					String message = "Error in Class ChemicalSystem, in method kinpFromOrca" + Constants.newLine;
					message = message
							+ "while reading vibrational frequencies for a system with more than one atom in file "
							+ read.getWorkFile() + Constants.newLine;
					message = message
							+ "(linear molecule and nb of freq. !=3N-5) OR ( not linear && nb of freq. != 3N-6) detected"
							+ Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

					throw new IllegalDataException();
				} // if end
			}

			// first, remove the 6 or 5 lowest REAL frequencies (rotations, translations)
			// !! Here in ORCA, these low frequencies are not read by Kisthelp, in contrast
			// with GAMESS OUTPUT

			write.oneString("*FREQUENCIES (in cm-1)");
			String imaginaryFreq = "";

			for (int iFreq = 0; iFreq < freq.size(); iFreq++) {

				// testing a possible null frequency
				if ((freq.get(iFreq).getRealPart() == 0) && (freq.get(iFreq).getImagPart() == 0)) {
					{
						write.end(); // end the file building

						String message = "Error in Class ChemicalSystem, in method kinpFromOrca" + Constants.newLine;
						message = message + "while reading vibrational frequencies in file " + read.getWorkFile()
								+ Constants.newLine;
						message = message + "a zero frequency has been found" + Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);

						throw new IllegalDataException();
					} // if end
				}

				// testing an imaginary frequency
				if (freq.get(iFreq).getImagPart() == 0) {
					write.oneDouble(freq.get(iFreq).getRealPart());
				} else {
					write.oneString(String.valueOf(freq.get(iFreq).getImagPart()) + "i");
				}

			} // for end
			write.oneString("*END");
		} // if nAtoms>1 end

		/* ELECTRONIC DEGENERACY */

		write.oneString("*ELECTRONIC DEGENERACY");
		write.oneString(degen);
		write.oneString("*END");

		/* INERTIA MOMENTS */

		if (nAtoms > 1) {

			write.oneString("*MOMENT OF INERTIA (in Amu.bohr**2)");

			if (!linearity) { // three components to be written
				for (int iInertia = 0; iInertia < 3; iInertia++) {
					write.oneDouble((Double) inertia.get(iInertia));
				} // for end

			} // if end
			else { // only one component to be written
				write.oneDouble((Double) inertia.get(0));
			} // else end

			write.oneString("*END");

			/* LINEARITY */
			write.oneString("*LINEAR");
			if (linearity) {
				write.oneString("linear");
			} else {
				write.oneString("not linear");
			}
			write.oneString("*END");

		} // end of if (nAtoms >1)

		/* POTENTIAL ENERGY */
		write.oneString("*POTENTIAL ENERGY (in hartree)");
		write.oneString(energy);
		write.oneString("*END");

	} // end of kinpFromgORCA method

	/**********************************/
	/* k i n p F r o m N W c h e m 6.0 */ // build a kinp input file from data in a NWchem frequency outputfile
	/********************************/

	public static void kinpFromNwc(ActionOnFileRead read, ActionOnFileWrite write)
			throws IllegalDataException, IOException {

		// molecular properties
		ArrayList<Complex> freq = new ArrayList<Complex>();// the frequencies are stored in a vector
		Vector inertia = new Vector(); // the inertia moments are stored in a vector
		String energy = "", degen = "";
		String symNumber = "***"; // thus, a final "***" string will signal a sym number not detected
		Boolean linearity = false; // default value
		int nAtoms = 0;
		int iAtom = 0;
		double mass = -1.0, massAtom1 = -1.0; // thus, a final negative number will signal a mass not detected

		String[] freqBuffer = new String[7];
		String[] massBuffer, energyBuffer, symNumberBuffer;
		String[] coordsBuffer, xyzBuffer, atomBuffer, buffer;
		boolean flagMass = false, flagInertia = false, job = false, jobHessian = false, flagFreq = false;
		boolean flagAtom = false, flagAtomMass = false, flagMoreThanOneEnerg = false;

		String currentLine, stringBuffer;
		int lineNumber = 0, massLine = 0, inertiaLine = 0, freqLine = 0, atomLine = 0, atomMassLine = 0;
		double decimalNumber;

		Pattern pHessian, pInertia, pInertia2, pAtom, pEnergy, pSymNumber, pMass;
		Pattern pMult, pLinear, pAtomMass;
		Pattern pTestPositDecimal, pTestNegatDecimal, pTestDecimal, pTestInt, pTestPositDecimalWithExp;
		Pattern pCoord, pFreqDetect;
		Pattern pS2, pS2_1, pS2A;

		Matcher mFreq, mFreqKey, mHessian, mInertia, mInertia2, mAtom, mEnergy, mSymNumber, mMass;
		Matcher mMult, mFreqDetect, mLinear, mAtomMass;
		Matcher mTestPositDecimal, mTestNegatDecimal, mTestDecimal, mTestInt, mTestPositDecimalWithExp;
		Matcher mXCoord, mYCoord, mZCoord;
		Matcher mS2, mS2_1, mS2A;

		pEnergy = Pattern.compile("Total [a-zA-Z]+ energy =");
		pAtom = Pattern.compile("XYZ format geometry");
		pAtomMass = Pattern.compile("Atomic Mass");
		pLinear = Pattern.compile("Linear Molecule");
		pMult = Pattern.compile("Spin multiplicity\\:");
		pFreqDetect = Pattern.compile("Normal Eigenvalue \\|\\|    Projected Derivative Dipole Moments");
		pHessian = Pattern.compile("Vibrational analysis via the FX method");
		pInertia = Pattern.compile("Rotational Constants");
		pMass = Pattern.compile("\\-\\- Atom information \\-\\-");
		pTestInt = Pattern.compile("^[1-9][0-9]*$");
		pTestPositDecimal = Pattern.compile("^[0-9]+\\.[0-9]*$");
		pTestPositDecimalWithExp = Pattern.compile("^[0-9]*\\.[0-9]*E[\\-\\+]?[0-9]+");

		pTestNegatDecimal = Pattern.compile("^[-][0-9]+\\.[0-9]*$");
		pTestDecimal = Pattern.compile("^[-]?[0-9]+\\.[0-9]*$");

		pSymNumber = Pattern.compile("symmetry #  =");
		pS2 = Pattern.compile("S2=");
		pS2_1 = Pattern.compile("S2-1=");
		pS2A = Pattern.compile("S2A=");
		currentLine = read.oneString();

		/************************************************/
		/* FIRST: filtering the NWchem output file */
		/************************************************/
		while (currentLine != null) {
			lineNumber = lineNumber + 1;
			/* C H E C K H E S S I A N R U N T Y P E */

			mHessian = pHessian.matcher(currentLine);
			if (mHessian.find()) {
				jobHessian = true;
			}

			/* Nb of A T O M S */
			mAtom = pAtom.matcher(currentLine);
			if (mAtom.find()) {
				flagAtom = true;
				atomLine = lineNumber;
			}
			if (flagAtom && (lineNumber == (atomLine + 2))) {

				buffer = currentLine.split(" +");
				nAtoms = Integer.parseInt(buffer[1]);
			}

			/* POTENTIAL ENERGY */
			mEnergy = pEnergy.matcher(currentLine);
			if (mEnergy.find()) {
				buffer = currentLine.split(" +");
				if (!energy.isEmpty()) {
					flagMoreThanOneEnerg = true;
				} else {
					energy = buffer[5];
				}
			}

			/* M U L T I P L I C I T Y */
			mMult = pMult.matcher(currentLine);
			if (mMult.find()) {
				buffer = currentLine.split(" +");
				degen = buffer[3];
			}

			/* VIBRATIONAL FREQUENCIES */
			/*
			 * recall that a negative number represents an imaginary number in NWCHEM
			 * outputfile ...
			 */

			mFreqDetect = pFreqDetect.matcher(currentLine);
			if (mFreqDetect.find()) {
				flagFreq = true;
				freqLine = lineNumber;
			}
			if (flagFreq && (lineNumber >= (freqLine + 3))) {

				buffer = currentLine.split(" +");
				// detect the end of the freq section
				if (buffer.length < 3) {
					flagFreq = false;
				}
				if (flagFreq) {
					if (Double.parseDouble(buffer[2]) == 0.0) { // translationnal Rotational Degree of Freedom
						// String message = "Warning in Class ChemicalSystem, in method kinpFromNwc" +
						// Constants.newLine;
						// message = message + "Excluding small frequency: " + buffer[2] + " cm-1 from
						// the set of vib. frequencies" + Constants.newLine;
						// JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
						// JOptionPane.WARNING_MESSAGE);
						System.out.println("Warning in Class ChemicalSystem, in method kinpFromNwc");
						System.out.println(
								"Excluding small frequency: " + buffer[2] + " cm-1 from the set of vib. frequencies");

					} else {
						if (Double.parseDouble(buffer[2]) < 0) { // imaginary frequency
							freq.add(new Complex(0.0, Math.abs(Double.parseDouble(buffer[2]))));
						} else {
							freq.add(new Complex(Double.parseDouble(buffer[2]), 0.0));
						}
					} // end of if (Double.parseDouble(buffer[2])==0.0
				} // end of if (flagFreq)

			} // end of if (flagFreq && (lineNumber>=(freqLine+3)

			/* MASS */
			mMass = pMass.matcher(currentLine);

			if (mMass.find()) {
				flagMass = true;
				massLine = lineNumber;
				mass = 0.0;
			}
			if (flagMass && (lineNumber >= (massLine + 3))) {

				buffer = currentLine.split(" +");
				// detect the end of the mass section
				if (buffer.length != 7) {
					flagMass = false;
				}
				if (flagMass) {

					buffer[6] = buffer[6].replace("D", "E"); // only exponential notation with E is known from java
					mTestPositDecimalWithExp = pTestPositDecimalWithExp.matcher(buffer[6]);

					if (!mTestPositDecimalWithExp.find()) {
						write.end(); // end the file building

						String message = "Error in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
						message = message + "while reading molecular mass in file " + read.getWorkFile()
								+ Constants.newLine;
						message = message + "=> " + buffer[6] + ": not a positive decimal number" + Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);
						throw new IllegalDataException();

					} // if end

					mass = mass + Double.parseDouble(buffer[6]);
				} // end of if (flagMass)
			} // end of if (flagMass && (lineNumber>=(massLine+3)) )

			/* Atomic M A S S in the case of a single atom calculation ! */
			mAtomMass = pAtomMass.matcher(currentLine);
			if (mAtomMass.find()) {
				flagAtomMass = true;
				atomMassLine = lineNumber;
			}
			if (flagAtomMass && (lineNumber == (atomMassLine + 3))) {

				buffer = currentLine.split(" +");
				massAtom1 = Double.parseDouble(buffer[2]);
			}

			/* LINEARITY */
			mLinear = pLinear.matcher(currentLine); // try to detect linearity
			if (mLinear.find()) {
				linearity = true;
			}

			/* INERTIA MOMENTS and molecular Linearity */
			// if previous inertia moments (another previous geom) already loaded, erase
			// previous vector
			mInertia = pInertia.matcher(currentLine); // try to detect inertia moments of a molecule
			if (mInertia.find()) {
				flagInertia = true;
				inertiaLine = lineNumber;
				if (inertia.size() != 0) {
					inertia.removeAllElements();
				}
			}
			if (flagInertia && (lineNumber >= (inertiaLine + 2))) {

				buffer = currentLine.split(" +");
				// detect the end of the mass section
				if (buffer.length != 7) {
					flagInertia = false;
				} // even for linear molecules, 3 fields are expected in NWchem
				if (flagInertia) {

					// checking if rotational constant are decimal numbers
					mTestDecimal = pTestDecimal.matcher(buffer[2]);
					if (!mTestDecimal.find()) {
						String message = "Error in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
						message = message + "while reading Rotational Constants" + Constants.newLine;
						message = message + "=> " + buffer[2] + ", is not a decimal number" + Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);
						throw new IllegalDataException();
					} else {
						inertia.add(Double.parseDouble(buffer[2]));
					}

					// treatment

				} // end if (flagInertia)

			} // end of if (flagInertia && (lineNumber>=(inertiaLine+1)

			/* ROTATIONAL SYMMETRY NUMBER */
			mSymNumber = pSymNumber.matcher(currentLine);

			if (mSymNumber.find()) {

				symNumberBuffer = currentLine.split(" +");
				symNumber = symNumberBuffer[9];
				int dot = symNumber.lastIndexOf(')');
				symNumber = symNumber.substring(0, dot);
				mTestInt = pTestInt.matcher(symNumber);
				if (!mTestInt.find()) {
					write.end(); // end the file building

					String message = "Error in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
					message = message + "while reading Rotational Symmetry Number in file " + read.getWorkFile()
							+ Constants.newLine;
					message = message + "=> " + symNumber + ", not an integer" + Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
							JOptionPane.ERROR_MESSAGE);
					throw new IllegalDataException();
				} // if end

			} // IF END

			currentLine = read.oneString();
		} // while end

		// ********************************************//
		// CHECKING properties existence //
		// ********************************************//

		// More than one energy has been detected
		if (flagMoreThanOneEnerg) {
			String message = "Warning in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
			message = message + "Multi-Job detected, more than one total energy detected, first occurrence kept"
					+ Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);
		}

		// first of all: the job must be a Hessian job, except for atomic calculation
		if (!jobHessian && (nAtoms > 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
			message = message + "Frequencies not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "**vibrational analysis via the FX method** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException(); // if end
		}

		/*
		 * ATOM number : needed to avoid printing Inertia moment and linearity
		 * information
		 */
		// checking the detected atom number
		if (nAtoms == 0) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
			message = message + "while reading number of atoms," + Constants.newLine;
			message = message + "**XYZ format geometry** pattern not found in file " + read.getWorkFile()
					+ Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		/* MASS */

		if ((mass < 0) && (nAtoms > 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
			message = message + "Atomic masses not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "**-- Atom information --** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		if ((massAtom1 < 0) && (nAtoms == 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
			message = message + "Atomic mass not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "**Atomic Mass** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* ENERGY */

		if (energy.isEmpty()) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
			message = message + "energy not found in " + read.getWorkFile() + Constants.newLine;
			message = message + "**Total  ... energy** pattern  not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* DEGENERACY */

		if (degen.isEmpty()) {

			String message = "Warning in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
			message = message + "Warning, **Spin multiplicity:** pattern not found in " + read.getWorkFile()
					+ Constants.newLine;
			message = message + "Assume electronic degeneracy = 1" + Constants.newLine;
			message = message + "This can be changed directly from the graphical interface" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);
			degen = "1";
		} // if end

		/* ROTATIONAL SYMETRY NUMBER */

		if (symNumber.equals("***") && (nAtoms > 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
			message = message + "rotational symmetry number not found in file " + read.getWorkFile()
					+ Constants.newLine;
			message = message + "**symmetry #  =** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* FREQUENCIES */

		if ((freq.size() == 0) && (nAtoms > 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
			message = message + "vibrational frequencies not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "**Normal Eigenvalue ||    Projected Derivative Dipole Moments**  pattern not found"
					+ Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* INERTIA MOMENTS */

		if ((inertia.size() == 0) && (nAtoms > 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
			message = message + "Inertia moment(s) not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "**Rotational Constants** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		// ********************************************//
		// NOW, WRITING properties on kinp input file //

		/* MASS */

		write.oneString("*MASS (in amu)");
		if (nAtoms > 1) {
			write.oneDouble(mass);
		} else if (nAtoms == 1) {
			write.oneDouble(massAtom1);
		}
		;
		write.oneString("*END");

		/* ROTATIONAL SYMMETRY NUMBER IF AND ONLY IF NON-ATOMIC SYSTEM */

		if (nAtoms > 1) {
			write.oneString("*NUMBER OF SYMMETRY");
			write.oneString(symNumber);
			write.oneString("*END");
		} // if end

		/* VIBRATIONAL FREQUENCIES */

		if (nAtoms > 1) {

			// first check number of freq. consistency
			if ((linearity && (freq.size() != (3 * nAtoms - 5))) || (!linearity && (freq.size() != (3 * nAtoms - 6)))) {
				{
					write.end(); // end the file building

					String message = "Error in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
					message = message + nAtoms + " atoms and " + freq.size() + " vib. freqs in " + read.getWorkFile()
							+ Constants.newLine;
					message = message
							+ "(linear molecule and nb of freq. !=3N-5) OR ( not linear && nb of freq. !=3N-6)"
							+ Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
							JOptionPane.ERROR_MESSAGE);

					throw new IllegalDataException();
				} // if end
			}

			write.oneString("*FREQUENCIES (in cm-1)");
			String imaginaryFreq = "";

			for (int iFreq = 0; iFreq < freq.size(); iFreq++) {

				// testing an imaginary frequency
				if (freq.get(iFreq).getImagPart() == 0) {
					write.oneDouble(freq.get(iFreq).getRealPart());
				} else {
					write.oneString(String.valueOf(freq.get(iFreq).getImagPart()) + "i");
				}

			} // for end
			write.oneString("*END");
		} // if nAtoms>1 end

		/* ELECTRONIC DEGENERACY */

		write.oneString("*ELECTRONIC DEGENERACY");
		write.oneString(degen);
		write.oneString("*END");

		/* INERTIA MOMENTS IF AND ONLY IF NON-ATOMIC SYSTEM */

		if (nAtoms > 1) {
			write.oneString("*MOMENT OF INERTIA (in Amu.bohr**2)");

			if (!linearity) { // three components to be written
				for (int iInertia = 0; iInertia < 3; iInertia++) {

					if ((Double) inertia.get(iInertia) == 0.0) {
						write.end(); // end the file building

						String message = "Error in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
						message = message + "non linear system but one inertia moment is null ... in "
								+ read.getWorkFile() + Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);

						throw new IllegalDataException();
					} // if end

					else {
						write.oneDouble(Constants.convertcm_1ToAmuBohr2 / (Double) inertia.get(iInertia));
					}
				} // for end

			} // if end
			else { // only one component to be written, but which is null? (two are identical)

				double a = (Double) inertia.get(0);
				double b = (Double) inertia.get(1);
				double c = (Double) inertia.get(2);

				if (a == 0.0) {
					write.oneDouble(Constants.convertcm_1ToAmuBohr2 / (Double) inertia.get(1));
				} else if (b == 0.0) {
					write.oneDouble(Constants.convertcm_1ToAmuBohr2 / (Double) inertia.get(2));
				} else if (c == 0.0) {
					write.oneDouble(Constants.convertcm_1ToAmuBohr2 / (Double) inertia.get(0));
				}
			} // else end

			write.oneString("*END");

			/* LINEARITY */
			write.oneString("*LINEAR");
			if (linearity) {
				write.oneString("linear");
			} else {
				write.oneString("not linear");
			}
			write.oneString("*END");

		} // end of if (nAtoms >1)

		/* POTENTIAL ENERGY */
		write.oneString("*POTENTIAL ENERGY (in hartree)");
		write.oneString(energy);
		write.oneString("*END");

		// tell the user the file kinp has been built : DEPRECATED since a new
		// kisthelpDialog asking for kinp location is now provided
		// String message = "Input file " + write.getWorkFile().getName() + " was
		// successfully built";
		// JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
		// JOptionPane.INFORMATION_MESSAGE);
		// JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
		// JDialog dialog = pane.createDialog(null, "KiSThelP");
		// dialog.setModal(false); // this says not to block background components
		// dialog.setVisible(true);

	} // end of kinpFromNWchem method

	/**********************************/
	/* k i n p F r o m A D F 2 0 2 4 */ // build a kinp input file from data in a ADF frequency outputfile
	/********************************/

	public static void kinpFromADF(ActionOnFileRead read, ActionOnFileWrite write)
			throws IllegalDataException, IOException {

		// molecular properties
		ArrayList<Complex> freq = new ArrayList<Complex>();// the frequencies are stored in a vector
		Vector inertia = new Vector(); // the inertia moments are stored in a vector
		String energy = "", degen = "1"; // default in ADF is closed shell system
		String symNumber = "***"; // thus, a final "***" string will signal a sym number not detected
		Boolean linearity = false; // default value
		int nAtoms = 0; // needed to test if we have to write a rotational/freq section or not
		int iAtom = 0, zmatLINE = -1000;
		double mass = -1.0; // thus, a final negative number will signal a mass not detected

		String[] freqBuffer = new String[7];
		String[] massBuffer, energyBuffer, symNumberBuffer;
		String[] coordsBuffer, xyzBuffer, atomBuffer, buffer;
		boolean flagInertia = false, job = false, jobHessian = false, flagFreq = false;
		boolean flagAtom = false, flagAtomMass = false, flagMoreThanOneEnerg = false;
		boolean flagZMAT = false, flagEndZMAT = false;
		// boolean flagMass=false;

		String currentLine, stringBuffer;
		int lineNumber = 0, inertiaLine = 0, freqLine = 0, atomLine = 0, atomMassLine = 0;
		double decimalNumber;
		// int massLine=0;

		Pattern pHessian, pInertia, pInertia2, pEnergy, pSymNumber, pMass;
		Pattern pMult, pLinear;
		// Pattern pAtomMass, pAtom;
		Pattern pTestPositDecimal, pTestNegatDecimal, pTestDecimal, pTestInt, pTestPositDecimalWithExp;
		Pattern pCoord, pFreqDetect, pZMAT;

		Matcher mFreq, mFreqKey, mHessian, mInertia, mInertia2, mEnergy, mSymNumber, mMass;
		Matcher mMult, mFreqDetect, mLinear, mZMAT;
		Matcher mTestPositDecimal, mTestNegatDecimal, mTestDecimal, mTestInt, mTestPositDecimalWithExp;
		// Matcher mAtomMass,mAtom;

		pEnergy = Pattern.compile(" Energy from Engine");
		// pAtom = Pattern.compile("Single Atom");
		// pAtomMass = Pattern.compile("At\\.Mass");
		pLinear = Pattern.compile("Linear Molecule");
		pMult = Pattern.compile("SpinPolarization"); // if not present : restricted HF
		pFreqDetect = Pattern.compile("Index   Frequency \\(cm\\-1\\)");
		pInertia = Pattern.compile(" Moments of Inertia \\[amu");
		pHessian = Pattern.compile("Statistical Thermal Analysis");

		pTestInt = Pattern.compile("^[1-9][0-9]*$");
		pTestPositDecimal = Pattern.compile("^[0-9]+\\.[0-9]*$");
		pTestPositDecimalWithExp = Pattern.compile("^[0-9]*\\.[0-9]*E[\\-\\+]?[0-9]+");

		pTestNegatDecimal = Pattern.compile("^[-][0-9]+\\.[0-9]*$");
		pTestDecimal = Pattern.compile("^[-]?[0-9]+\\.[0-9]*$");

		pSymNumber = Pattern.compile("reported below were computed using sigma");
		pZMAT = Pattern.compile("Index Symbol   x");

		currentLine = read.oneString();

		/************************************************/
		/* FIRST: filtering the ADF output file */
		/************************************************/
		while (currentLine != null) {
			lineNumber = lineNumber + 1;

			/* M A S S */
			/* Z-MATRIX filtering (needed for DFTB calculations to get the masses) */
			/*
			 * Indeed, DFTB output does not display atomic masses used in thermal analysis
			 * ...
			 */
			mZMAT = pZMAT.matcher(currentLine);
			if (mZMAT.find() && (flagEndZMAT == false)) {
				flagZMAT = true;
				zmatLINE = lineNumber;
				nAtoms = 0;
				mass = 0.0;
			}
			;
			if (flagZMAT && (lineNumber >= (zmatLINE + 1))) {

				// if (currentLine.trim().isEmpty()) { // trim() remove any space or tabulation
				// in the line
				if (currentLine.matches("^\\s*$")) { // means: at the beginning of the line: 0 or more spaces ending the
														// line
					flagEndZMAT = true; // the end of the Z-MATRIX is reached
					flagZMAT = false;

				} else {
					// split the current line (filed separator = space)
					String[] parts = currentLine.trim().split("\\s+");
					String atomName = parts[1];
					nAtoms = nAtoms + 1;
					mass = mass + Constants.getAtomicMass(atomName); // needed to compare the sum of reactant masses
																		// with
																		// TS mass
				}
			} // end of if (flagZMAT && (lineNumber>=(zmatLINE+1)) )

			/* C H E C K H E S S I A N R U N T Y P E */
			mHessian = pHessian.matcher(currentLine);
			if (mHessian.find()) {
				jobHessian = true;
			}

			/* POTENTIAL ENERGY */
			mEnergy = pEnergy.matcher(currentLine);
			if (mEnergy.find()) {
				buffer = currentLine.split(" +");
				energy = buffer[4];
			}

			/* M U L T I P L I C I T Y */
			mMult = pMult.matcher(currentLine);
			if (mMult.find()) {
				buffer = currentLine.split(" +");
				degen = buffer[2]; /* take care, we retrieve only teh difference in alpha and beta e- */
				double multiplicity = Double.parseDouble(degen) + 1; // don't forget that degen is a String
				degen = Double.toString(multiplicity);
			}

			/* VIBRATIONAL FREQUENCIES */
			/*
			 * recall that a negative number represents an imaginary number in ADF
			 * outputfile ...
			 */

			mFreqDetect = pFreqDetect.matcher(currentLine);
			if (mFreqDetect.find()) {
				flagFreq = true;
				freqLine = lineNumber;
			}
			if (flagFreq && (lineNumber >= (freqLine + 1))) {

				buffer = currentLine.split(" +");
				// detect the end of the freq section
				if (buffer.length < 5) {
					flagFreq = false;
				}
				if (flagFreq) {
					if (Double.parseDouble(buffer[2]) < 0) { // imaginary frequency
						freq.add(new Complex(0.0, Math.abs(Double.parseDouble(buffer[2]))));
					} else {
						freq.add(new Complex(Double.parseDouble(buffer[2]), 0.0));
					}
				} // end of if (flagFreq)

			} // end of if (flagFreq && (lineNumber>=(freqLine+3)

			/* MASS */
			/*
			 * DEPRECATED, now masses are deduced from atomic name taken from z-matrix, see
			 * above
			 */
			/*
			 * mAtomMass= pAtomMass.matcher(currentLine);
			 * if (mAtomMass.find() ) {flagMass= true; massLine=lineNumber; mass=0.0;
			 * nAtoms=0;}
			 * if (flagMass && (lineNumber>=(massLine+2)) ){
			 * 
			 * buffer = currentLine.split(" +");
			 * // detect the end of the mass section
			 * if (buffer.length <8) {flagMass= false;}
			 * if (flagMass) {
			 * 
			 * if ( Double.parseDouble(buffer[8]) <= 0.0 )
			 * {write.end(); // end the file building
			 * 
			 * String message = "Error in Class ChemicalSystem, in method kinpFromADF" +
			 * Constants.newLine;
			 * message = message + "while reading molecular mass in file " +
			 * read.getWorkFile() + Constants.newLine;
			 * message = message + "=> " + buffer[8] + ": not a positive decimal number" +
			 * Constants.newLine;
			 * JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
			 * JOptionPane.ERROR_MESSAGE);
			 * throw new IllegalDataException();
			 * 
			 * } // if end Double.parseDouble(buffer[8]) <= 0.0 )
			 * 
			 * 
			 * mass = mass + Double.parseDouble(buffer[8]); // needed to compare the sum of
			 * reactants masses wit
			 * // TS mass
			 * nAtoms = nAtoms + 1;
			 * }// end of if (flagMass)
			 * } // end of if (flagMass && (lineNumber>=(massLine+3)) )
			 * 
			 */

			/* LINEARITY */
			mLinear = pLinear.matcher(currentLine); // try to detect linearity
			if (mLinear.find()) {
				linearity = true;
			}

			/* INERTIA MOMENTS and molecular Linearity */
			// if previous inertia moments (another previous geom) already loaded, erase
			// previous vector
			mInertia = pInertia.matcher(currentLine); // try to detect inertia moments of a molecule
			if (mInertia.find()) {
				flagInertia = true;
				inertiaLine = lineNumber;
				if (inertia.size() != 0) {
					inertia.removeAllElements();
				}
			}
			if (flagInertia && (lineNumber >= (inertiaLine + 3))) {

				buffer = currentLine.split(" +");
				// detect the end of the mass section
				if (buffer.length < 3) {
					flagInertia = false;
				} // even for linear molecules, 3 fields are expected in ADF
				if (flagInertia) {
					// read the three decimal numbers
					inertia.add(Double.parseDouble(buffer[1]));
					inertia.add(Double.parseDouble(buffer[2]));
					inertia.add(Double.parseDouble(buffer[3]));
				} // end if (flagInertia)

			} // end of if (flagInertia && (lineNumber>=(inertiaLine+3)

			/* ROTATIONAL SYMMETRY NUMBER */ // in ADF this section is always present (atom, linear or 3D)
			mSymNumber = pSymNumber.matcher(currentLine);

			if (mSymNumber.find()) {
				symNumberBuffer = currentLine.split("sigma =|,"); // '|' means 'OR'
				symNumber = symNumberBuffer[1].trim(); // then parts[1] is the number between 'sigma =' and ','
				mTestInt = pTestInt.matcher(symNumber);
				if (!mTestInt.find()) {
					write.end(); // end the file building

					String message = "Error in Class ChemicalSystem, in method kinpFromADF" + Constants.newLine;
					message = message + "while reading Rotational Symmetry Number in file " + read.getWorkFile()
							+ Constants.newLine;
					message = message + "=> " + symNumber + ", not an integer" + Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
							JOptionPane.ERROR_MESSAGE);
					throw new IllegalDataException();
				} // if (!mTestInt.find())end

			} // IF if (mSymNumber.find() )END

			currentLine = read.oneString();
		} // while end

		// ********************************************//
		// CHECKING properties existence //
		// ********************************************//

		// More than one energy has been detected: no possible in ADF

		// first of all: the job must be a Hessian job, even for atomic calculation
		if (!jobHessian) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromADF" + Constants.newLine;
			message = message + "Frequencies not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "** Statistical Thermal Analysis ** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException(); // if end
		}

		/*
		 * ATOM number : needed to avoid printing Inertia moment and linearity
		 * information
		 */
		// checking the detected atom number
		if (nAtoms == 0) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromADF" + Constants.newLine;
			message = message + "while reading number of atoms," + Constants.newLine;
			message = message + "no atom detected in " + read.getWorkFile() + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		/* ENERGY */

		if (energy.isEmpty()) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromADF" + Constants.newLine;
			message = message + "energy not found in " + read.getWorkFile() + Constants.newLine;
			message = message + "**Energy from Engine** pattern  not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* DEGENERACY */
		// no longer possible in ADF since if we do not find the pattern, default is
		// degen=1

		/* ROTATIONAL SYMETRY NUMBER */ // in ADF this section is always present (atom, linear or 3D)

		/* FREQUENCIES */

		if ((freq.size() == 0) && (nAtoms > 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromADF" + Constants.newLine;
			message = message + "vibrational frequencies not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "**Index   Frequency (cm-1)**  pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* INERTIA MOMENTS */

		if ((inertia.size() == 0) && (nAtoms > 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromADF" + Constants.newLine;
			message = message + "Inertia moment(s) not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "**Moments of Inertia** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
					JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		// ********************************************//
		// NOW, WRITING properties on kinp input file //

		/* MASS */

		write.oneString("*MASS (in amu)");
		write.oneDouble(mass);
		write.oneString("*END");

		/* ROTATIONAL SYMMETRY NUMBER IF AND ONLY IF NON-ATOMIC SYSTEM */

		if (nAtoms > 1) {
			write.oneString("*NUMBER OF SYMMETRY");
			write.oneString(symNumber);
			write.oneString("*END");
		} // if end

		/* VIBRATIONAL FREQUENCIES */

		if (nAtoms > 1) {

			// first check number of freq. consistency
			if ((linearity && (freq.size() != (3 * nAtoms - 5))) || (!linearity && (freq.size() != (3 * nAtoms - 6)))) {
				write.end(); // end the file building

				String message = "Error in Class ChemicalSystem, in method kinpFromNwc" + Constants.newLine;
				message = message + nAtoms + " atoms and " + freq.size() + " vib. freqs in " + read.getWorkFile()
						+ Constants.newLine;
				message = message + "(linear molecule and nb of freq. !=3N-5) OR ( not linear && nb of freq. !=3N-6)"
						+ Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
						JOptionPane.ERROR_MESSAGE);

				throw new IllegalDataException();
			} // if if ( (linearity && (freq.size() ... end

			write.oneString("*FREQUENCIES (in cm-1)");
			String imaginaryFreq = "";

			for (int iFreq = 0; iFreq < freq.size(); iFreq++) {

				// testing an imaginary frequency
				if (freq.get(iFreq).getImagPart() == 0) {
					write.oneDouble(freq.get(iFreq).getRealPart());
				} else {
					write.oneString(String.valueOf(freq.get(iFreq).getImagPart()) + "i");
				}

			} // for end
			write.oneString("*END");
		} // if nAtoms>1 end

		/* ELECTRONIC DEGENERACY */

		write.oneString("*ELECTRONIC DEGENERACY");
		// int intDegen = Integer.parseInt(degen);
		int intDegen = Integer.parseInt(String.valueOf(Math.round(Double.parseDouble(degen))));

		write.oneInt(intDegen);
		write.oneString("*END");

		/* INERTIA MOMENTS */

		if (nAtoms > 1) {
			write.oneString("*MOMENT OF INERTIA (in Amu.bohr**2)");

			if (!linearity) { // three components to be written
				for (int iInertia = 0; iInertia < 3; iInertia++) {

					if ((Double) inertia.get(iInertia) == 0.0) {
						write.end(); // end the file building

						String message = "Error in Class ChemicalSystem, in method kinpFromADF" + Constants.newLine;
						message = message + "non linear system but one inertia moment is null ... in "
								+ read.getWorkFile() + Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);

						throw new IllegalDataException();
					} // if end

					else {
						write.oneDouble((Double) inertia.get(iInertia));
					}
				} // for end (int iInertia=0; iInertia< 3; iInertia++)

			} // if (!linearity) end
			else { // only one component to be written, but which is null? (two are identical)

				double a = (Double) inertia.get(0);
				double b = (Double) inertia.get(1);
				double c = (Double) inertia.get(2);

				if (a == 0.0) {
					write.oneDouble((Double) inertia.get(1));
				} else if (b == 0.0) {
					write.oneDouble((Double) inertia.get(2));
				} else if (c == 0.0) {
					write.oneDouble((Double) inertia.get(0));
				}
			} // else (!linearity) end

			write.oneString("*END");

			/* LINEARITY */
			write.oneString("*LINEAR");
			if (linearity) {
				write.oneString("linear");
			} else {
				write.oneString("not linear");
			}
			write.oneString("*END");

		} // end of if (nAtoms >1)

		/* POTENTIAL ENERGY */
		write.oneString("*POTENTIAL ENERGY (in hartree)");
		write.oneString(energy);
		write.oneString("*END");

	} // end of kinpFromADF method

	/**********************************/
	/* k i n p F r o m G m s 2 0 1 2 */ // build a kinp input file from data in a Gamess2012 or Gamess2013 frequency
										// outputfile
	/********************************/

	public static void kinpFromGms2012(ActionOnFileRead read, ActionOnFileWrite write)
			throws IllegalDataException, IOException {

		// molecular properties
		ArrayList<Complex> freq = new ArrayList<Complex>();// the frequencies are stored in a vector
		Vector inertia = new Vector(); // the inertia moments are stored in a vector
		String energy = "", degen = "";
		String symNumber = "***"; // thus, a final "***" string will signal a sym number not detected
		Boolean linearity = false; // default value
		int nAtoms = 0;
		int iAtom = 0;
		double mass = -1.0; // thus, a final negative number will signal a mass not detected

		String[] freqBuffer = new String[7];
		String[] massBuffer, energyBuffer, symNumberBuffer;
		String[] coordsBuffer, xyzBuffer, atomBuffer, buffer;
		boolean flagMass = false, flagInertia = false, job = false, jobHessian = false;

		String currentLine, stringBuffer;
		int lineNumber = 0, massLine = 0, inertiaLine = 0;
		double decimalNumber;

		Pattern pFreq, pFreqKey, pHessian, pInertia, pInertia2, pAtom, pEnergy, pSymNumber, pMass;
		Pattern pJob, pMult;
		Pattern pTestPositDecimal, pTestNegatDecimal, pTestDecimal, pTestInt;
		Pattern pCoord;
		Pattern pS2, pS2_1, pS2A;

		Matcher mFreq, mFreqKey, mHessian, mInertia, mInertia2, mAtom, mEnergy, mSymNumber, mMass;
		Matcher mJob, mMult;
		Matcher mTestPositDecimal, mTestNegatDecimal, mTestDecimal, mTestInt;
		Matcher mXCoord, mYCoord, mZCoord;
		Matcher mS2, mS2_1, mS2A;

		pEnergy = Pattern.compile("TOTAL ENERGY =  ");
		pMult = Pattern.compile("SPIN MULTIPLICITY");
		pFreq = Pattern.compile("   FREQUENCY\\:  ");
		pHessian = Pattern.compile("HESSIAN MATRIX CONTROL PARAMETERS");
		pInertia = Pattern.compile("THE ROTATIONAL CONSTANTS ARE \\(IN GHZ\\)");
		pMass = Pattern.compile("ATOMIC WEIGHTS");
		pTestInt = Pattern.compile("^[1-9][0-9]*$");
		pTestPositDecimal = Pattern.compile("^[0-9]+\\.[0-9]*$");
		pJob = Pattern.compile("EXECUTION OF GAMESS TERMINATED NORMALLY");
		pTestNegatDecimal = Pattern.compile("^[-][0-9]+\\.[0-9]*$");
		pTestDecimal = Pattern.compile("^[-]?[0-9]+\\.[0-9]*$");

		pSymNumber = Pattern.compile("THE ROTATIONAL SYMMETRY NUMBER IS");
		pFreqKey = Pattern.compile("\\\\Freq\\\\");
		pS2 = Pattern.compile("S2=");
		pS2_1 = Pattern.compile("S2-1=");
		pS2A = Pattern.compile("S2A=");
		currentLine = read.oneString();

		/************************************************/
		/* FIRST: filtering the GAMESS 2012/2013 output file */
		/************************************************/
		while (currentLine != null) {
			lineNumber = lineNumber + 1;
			/* C H E C K H E S S I A N R U N T Y P E */

			mHessian = pHessian.matcher(currentLine);
			if (mHessian.find()) {
				jobHessian = true;
			}

			/* C H E C K NORMAL TERMINATION */

			mJob = pJob.matcher(currentLine);
			if (mJob.find()) {
				job = true;
			}

			/* POTENTIAL ENERGY */
			mEnergy = pEnergy.matcher(currentLine);
			if (mEnergy.find()) {
				buffer = currentLine.split(" +");
				energy = buffer[4];
			}

			/* M U L T I P L I C I T Y */
			mMult = pMult.matcher(currentLine);
			if (mMult.find()) {
				buffer = currentLine.split(" +");
				degen = buffer[4];
			}

			/* VIBRATIONAL FREQUENCIES */
			/*
			 * pay attention with gamess: imaginary frequencies are highlighted with the "I"
			 * character after a blank character, "3000 I" i.e
			 */
			// here, all the 3N "frequencies" are read ! (even the 6 first numbers)
			mFreq = pFreq.matcher(currentLine);
			if (mFreq.find()) {
				freqBuffer = currentLine.split(" +");
				int iFreq = 2; // frequencies start at field 2, but imaginary frequencies should be envisaged
								// => consider also field 3...
				while (iFreq <= freqBuffer.length - 1) {

					mTestDecimal = pTestDecimal.matcher(freqBuffer[iFreq]);
					if (!mTestDecimal.find()) {
						write.end(); // end the file building

						String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
						message = message + "while reading vibrational frequencies in file " + read.getWorkFile()
								+ Constants.newLine;
						message = message + "=> " + freqBuffer[iFreq - 1] + "  : not a decimal number"
								+ Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);

						throw new IllegalDataException();
					} // if end

					if ((iFreq + 1) <= freqBuffer.length - 1) {
						if (freqBuffer[iFreq + 1].equals("I")) { // case of imaginary frequency
							freq.add(new Complex(0, Double.parseDouble(freqBuffer[iFreq])));
							iFreq++;
						} else
							freq.add(new Complex(Double.parseDouble(freqBuffer[iFreq]), 0));
					} // end of if ( (iFreq+1)<=
					else {
						freq.add(new Complex(Double.parseDouble(freqBuffer[iFreq]), 0));
					}

					iFreq++;
				} // while END
			} // IF END

			/* MASS */
			mMass = pMass.matcher(currentLine);

			if (mMass.find()) {
				flagMass = true;
				massLine = lineNumber;
				mass = 0.0;
			}
			if (flagMass && (lineNumber >= (massLine + 2))) {

				buffer = currentLine.split(" +");
				// detect the end of the mass section
				if (buffer.length != 4) {
					flagMass = false;
					nAtoms = iAtom;
				}
				if (flagMass) {
					iAtom++;

					mTestPositDecimal = pTestPositDecimal.matcher(buffer[3]);
					if (!mTestPositDecimal.find()) {
						write.end(); // end the file building
						String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
						message = message + "while reading molecular mass in file " + read.getWorkFile()
								+ Constants.newLine;
						message = message + "=> " + buffer[3] + ": not a positive decimal number" + Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);

						throw new IllegalDataException();
					} // if end
					mass = mass + Double.parseDouble(buffer[3]);

				} // end of if (flagMass)
			} // end of if (flagMass && (lineNumber>=(massLine+2)) )

			/* INERTIA MOMENTS and molecular Linearity */
			// if previous inertia moments (another previous geom) already loaded, erase
			// previous vector
			mInertia = pInertia.matcher(currentLine); // try to detect inertia moments of a molecule
			if (mInertia.find()) {
				flagInertia = true;
				inertiaLine = lineNumber;
				if (inertia.size() != 0) {
					inertia.removeAllElements();
				}
			}
			if (flagInertia && (lineNumber >= (inertiaLine + 1))) {

				buffer = currentLine.split(" +");
				// detect the end of the mass section
				if (buffer.length != 4) {
					flagInertia = false;
				} // even for linear molecules, 3 fields are expected in GAMESS
				if (flagInertia) {

					// checking if rotational constant are decimal numbers
					for (int i = 1; i <= 3; i++) {

						mTestDecimal = pTestDecimal.matcher(buffer[i]);
						if (!mTestDecimal.find()) {
							String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
							message = message + "while reading Rotational Constants in file " + read.getWorkFile()
									+ Constants.newLine;
							message = message + "=> " + buffer[i] + ", is not a decimal number" + Constants.newLine;
							JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
									JOptionPane.ERROR_MESSAGE);

							throw new IllegalDataException();
						}

					} // end of for
					// treatment
					// detect linearity of the molecule
					double a = Double.parseDouble(buffer[1]);
					double b = Double.parseDouble(buffer[2]);
					double c = Double.parseDouble(buffer[3]);
					if ((a < Constants.inertiaEpsilon) && (Math.abs(b - c) < Constants.inertiaEpsilon)) {
						linearity = true;
						inertia.add(Constants.convertGHzToAmuBohr2 / b);
					}

					if ((b < Constants.inertiaEpsilon) && (Math.abs(c - a) < Constants.inertiaEpsilon)) {
						linearity = true;
						inertia.add(Constants.convertGHzToAmuBohr2 / c);
					}

					if ((c < Constants.inertiaEpsilon) && (Math.abs(a - b) < Constants.inertiaEpsilon)) {
						linearity = true;
						inertia.add(Constants.convertGHzToAmuBohr2 / a);
					}

					if (!linearity) {
						inertia.add(Constants.convertGHzToAmuBohr2 / a);
						inertia.add(Constants.convertGHzToAmuBohr2 / b);
						inertia.add(Constants.convertGHzToAmuBohr2 / c);
					}

				} // end if (flagInertia)

			} // end of if (flagInertia && (lineNumber>=(inertiaLine+1)

			/* ROTATIONAL SYMMETRY NUMBER */
			mSymNumber = pSymNumber.matcher(currentLine);

			if (mSymNumber.find()) {

				symNumberBuffer = currentLine.split(" +");
				symNumber = symNumberBuffer[6];
				int dot = symNumber.lastIndexOf('.');
				symNumber = symNumber.substring(0, dot);
				mTestInt = pTestInt.matcher(symNumber);
				if (!mTestInt.find()) {
					write.end(); // end the file building

					String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
					message = message + "while reading Rotational Symmetry Number in file " + read.getWorkFile()
							+ Constants.newLine;
					message = message + "=> " + symNumber + ", not an integer" + Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

					throw new IllegalDataException();
				} // if end

			} // IF END

			currentLine = read.oneString();
		} // while end

		// ********************************************//
		// CHECKING properties existence //
		// ********************************************//

		// first of all: the job must be a Hessian job
		if (!jobHessian) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
			message = message + "while checking the type of job in the outputfile " + read.getWorkFile()
					+ Constants.newLine;
			message = message + "=> a RUNTYP=HESSIAN job is required, even for atomic species" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException(); // if end
		}

		// JOB normally terminated ?
		if (!job) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
			message = message + "while reading GAMESS output file " + read.getWorkFile() + Constants.newLine;
			message = message + " string EXECUTION OF GAMESS TERMINATED NORMALLY not found, ..." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		/*
		 * ATOM number : needed to avoid printing Inertia moment and linearity
		 * information
		 */
		// checking the detected atom number
		if (nAtoms == 0) {
			write.end(); // end the file building
			String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
			message = message + "while reading atomic mass, no atom found in file " + read.getWorkFile()
					+ Constants.newLine;
			message = message + " string EXECUTION OF GAMESS TERMINATED NORMALLY not found, ..." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* MASS */

		if (mass < 0) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
			message = message + "Atomic masses not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "**ATOMIC WEIGHTS** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		/* ENERGY */

		if (energy.isEmpty()) {
			write.end(); // end the file building
			String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
			message = message + "energy not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "**TOTAL ENERGY =** pattern  not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* DEGENERACY */

		if (degen.isEmpty()) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
			message = message + "spin multiplicity not found in " + read.getWorkFile() + Constants.newLine;
			message = message + "**SPIN MULTIPLICITY** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* ROTATIONAL SYMETRY NUMBER */

		if (symNumber.equals("***") && (nAtoms > 1)) {
			write.end(); // end the file building
			String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
			message = message + "rotational symmetry number not found in file " + read.getWorkFile()
					+ Constants.newLine;
			message = message + "**THE ROTATIONAL SYMMETRY NUMBER IS** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		/* FREQUENCIES */

		if ((freq.size() == 0) && (nAtoms > 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
			message = message + "vibrational frequencies not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "**FREQUENCY:**  pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		/* INERTIA MOMENTS */

		if ((inertia.size() == 0) && (nAtoms > 1)) {
			write.end(); // end the file building
			String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
			message = message + "Inertia moment(s) not found in file " + read.getWorkFile() + Constants.newLine;
			message = message + "**THE ROTATIONAL CONSTANTS ARE (IN GHZ)** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		// ********************************************//
		// NOW, WRITING properties on kinp input file //
		// ********************************************//

		/* MASS */

		write.oneString("*MASS (in amu)");
		write.oneDouble(mass);
		write.oneString("*END");

		/* ROTATIONAL SYMMETRY NUMBER IF AND ONLY IF NON-ATOMIC SYSTEM */

		if (nAtoms > 1) {
			write.oneString("*NUMBER OF SYMMETRY");
			write.oneString(symNumber);
			write.oneString("*END");
		} // if end

		/* VIBRATIONAL FREQUENCIES */

		if (nAtoms > 1) {

			// first check number of freq. consistency
			if ((linearity && freq.size() < 5) || (!linearity && freq.size() < 6)) {
				{
					write.end(); // end the file building
					String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
					message = message
							+ "while reading vibrational frequencies for a system with more than one atom in file "
							+ read.getWorkFile() + Constants.newLine;
					message = message
							+ "(linear molecule and nb of freq. <5) OR ( not linear && nb of freq. <6) detected"
							+ Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

					throw new IllegalDataException();
				} // if end
			}

			// first, remove the 6 or 5 lowest REAL frequencies (rotations, translations)
			int nRotTrans = 0;
			if (linearity) {
				nRotTrans = 5;
			} else {
				nRotTrans = 6;
			}
			for (int iRotTrans = 1; iRotTrans <= nRotTrans; iRotTrans++) {
				double smallest = 100000000;
				Complex smallestCx = freq.get(0);

				for (int iFreq = 0; iFreq < freq.size(); iFreq++) {
					if ((freq.get(iFreq).getImagPart() == 0) && (freq.get(iFreq).getRealPart() < smallest)) {
						smallest = freq.get(iFreq).getRealPart();
						smallestCx = freq.get(iFreq);
					}
				} // end of for
					// remove the smallest element of the list
				String message = "Warning in Class ChemicalSystem, in method kinpFromGms2012" + Constants.newLine;
				message = message + "Warning, removing small frequency: " + smallestCx.getRealPart() + " cm-1"
						+ Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);
				freq.remove(smallestCx);
			} // end of for (int irotTrans=1;

			write.oneString("*FREQUENCIES (in cm-1)");
			String imaginaryFreq = "";

			for (int iFreq = 0; iFreq < freq.size(); iFreq++) {

				// testing a possible null frequency
				if ((freq.get(iFreq).getRealPart() == 0) && (freq.get(iFreq).getImagPart() == 0)) {
					{
						write.end(); // end the file building

						String message = "Error in Class ChemicalSystem, in method kinpFromGms" + Constants.newLine;
						message = message + "while reading vibrational frequencies in file " + read.getWorkFile()
								+ Constants.newLine;
						message = message + "a zero frequency has been found" + Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);

						throw new IllegalDataException();
					} // if end
				}

				// testing an imaginary frequency
				if (freq.get(iFreq).getImagPart() == 0) {
					write.oneDouble(freq.get(iFreq).getRealPart());
				} else {
					write.oneString(String.valueOf(freq.get(iFreq).getImagPart()) + "i");
				}

			} // for end
			write.oneString("*END");
		} // if nAtoms>1 end

		/* ELECTRONIC DEGENERACY */

		write.oneString("*ELECTRONIC DEGENERACY");
		write.oneString(degen);
		write.oneString("*END");

		/* INERTIA MOMENTS */

		if (nAtoms > 1) {

			write.oneString("*MOMENT OF INERTIA (in Amu.bohr**2)");

			if (!linearity) { // three components to be written
				for (int iInertia = 0; iInertia < 3; iInertia++) {
					write.oneDouble((Double) inertia.get(iInertia));
				} // for end

			} // if end
			else { // only one component to be written
				write.oneDouble((Double) inertia.get(0));
			} // else end

			write.oneString("*END");

			/* LINEARITY */
			write.oneString("*LINEAR");
			if (linearity) {
				write.oneString("linear");
			} else {
				write.oneString("not linear");
			}
			write.oneString("*END");

		} // end of if (nAtoms >1)

		/* POTENTIAL ENERGY */
		write.oneString("*POTENTIAL ENERGY (in hartree)");
		write.oneString(energy);
		write.oneString("*END");

		// tell the user the file kinp has been built : DEPRECATED since a new
		// kisthelpDialog asking for kinp location is now provided
		// String message = "Input file " + write.getWorkFile().getName() + " was
		// successfully built";
		// JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
		// JOptionPane.INFORMATION_MESSAGE);
		// JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
		// JDialog dialog = pane.createDialog(null, "KiSThelP");
		// dialog.setModal(false); // this says not to block background components
		// dialog.setVisible(true);

	} // end of kinpFromgGAMESS method

	/**********************************/
	/* k i n p F r o m G 0 9 */ // build a kinp input file from data into a g09 frequency outputfile
	/********************************/

	public static void kinpFromG09(ActionOnFileRead read, ActionOnFileWrite write)
			throws IllegalDataException, IOException {

		// molecular properties
		Vector freq = new Vector(); // the frequencies are stored in a vector
		Vector inertia = new Vector(); // the inertia moments are stored in a vector
		String mass = "***"; // molecular mass
		String energy = "";
		String archive = "";
		String symNumber = "***";
		String coords;
		Boolean linearity = false; // default value
		int nJobs = 0;
		int nAtoms = 0;

		String[] freqBuffer, inertiaBuffer, massBuffer, degenBuffer, energyBuffer, symNumberBuffer;
		String[] coordsBuffer, xyzBuffer, atomBuffer, versionBuffer;

		String currentLine, stringBuffer;
		int lineNumber = 0;
		double decimalNumber;
		boolean flagArchive = false;

		Pattern pFreq, pFreqKey, pInertia, pInertia2, pAtom, pEnergy, pSymNumber, pMass;
		Pattern pArchive, gaussianEnd, pJob;
		Pattern pTestPositDecimal, pTestNegatDecimal, pTestDecimal, pTestInt;
		Pattern pCoord;
		Pattern pS2, pS2_1, pS2A, pVersion;

		Matcher mFreq, mFreqKey, mInertia, mInertia2, mAtom, mEnergy, mSymNumber, mMass;
		Matcher mArchive, mEnd, mJob;
		Matcher mTestPositDecimal, mTestNegatDecimal, mTestDecimal, mTestInt;
		Matcher mXCoord, mYCoord, mZCoord;
		Matcher mS2, mS2_1, mS2A, mVersion;

		pFreq = Pattern.compile("Frequencies --");
		pInertia = Pattern.compile("Rotational constants \\(GHZ\\)\\:");
		pInertia2 = Pattern.compile("Rotational constant \\(GHZ\\)\\:");
		pMass = Pattern.compile("Molecular mass:");
		pTestInt = Pattern.compile("^[1-9][0-9]*$");
		pTestPositDecimal = Pattern.compile("^[0-9]+\\.[0-9]*$");
		pTestNegatDecimal = Pattern.compile("^[-][0-9]+\\.[0-9]*$");
		pTestDecimal = Pattern.compile("^[-]?[0-9]+\\.[0-9]*$");
		pArchive = Pattern.compile("1\\|1\\||1\\\\1\\\\"); // match either a Linux (\1\1) or Windows (1|1|) gaussian
															// output
		pEnergy = Pattern.compile("HF=");
		pSymNumber = Pattern.compile("Rotational symmetry number");
		gaussianEnd = Pattern.compile("\\@");
		pJob = Pattern.compile("1\\|1\\||1\\\\1\\\\");
		pFreqKey = Pattern.compile("\\\\Freq\\\\");
		pS2 = Pattern.compile("S2=");
		pS2_1 = Pattern.compile("S2-1=");
		pS2A = Pattern.compile("S2A=");
		pVersion = Pattern.compile("\\\\Version=");
		currentLine = read.oneString();

		Boolean newFreqSetFlag = true; // used to indicate whether a new frequency set is read or not during the file
										// parsing

		/******************************************/
		/* FIRST: filtering the g09 output file */
		/******************************************/
		while (currentLine != null) {

			lineNumber = lineNumber + 1;

			/* NUMBER OF JOBS already read by this subroutine INTO G09 file */
			/* pattern is detected at the end of the gaussian job */
			mJob = pJob.matcher(currentLine);
			if (mJob.find()) {

				nJobs = nJobs + 1;
				newFreqSetFlag = true;

			} // if end

			/* POTENTIAL ENERGY + COORDINATES */

			mArchive = pArchive.matcher(currentLine);
			if (mArchive.find()) {
				flagArchive = true;
				if (archive.length() != 0) {
					archive = "";
				}
			} // reinitialize archive for a multiple-job outputfile
			if (flagArchive) {

				archive = archive + currentLine.substring(1);
				flagArchive = !gaussianEnd.matcher(currentLine).find();
			} // if end

			/* VIBRATIONAL FREQUENCIES */
			/*
			 * recall that a negative number represents an imaginary number in gaussian
			 * outputfile ...
			 */
			/*
			 * here : only 3N-6(5) numbers are read since gaussian already removed the
			 * trans+rot modes
			 */

			mFreq = pFreq.matcher(currentLine);
			if (mFreq.find()) {

				/*
				 * Take care, if a second job is encountered and the two jobs in the file
				 * contains frequencies information
				 */
				/* we have to reset the frequency array and reset also the freqSet flag */
				if (newFreqSetFlag) {
					freq.clear();
					newFreqSetFlag = false;
				}

				freqBuffer = currentLine.split(" +");
				for (int iFreq = 3; iFreq <= freqBuffer.length - 1; iFreq++) {
					freq.add(freqBuffer[iFreq]);

					mTestDecimal = pTestDecimal.matcher(freqBuffer[iFreq]);
					if (!mTestDecimal.find()) {
						write.end(); // end the file building

						String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
						message = message + "while reading vibrational frequencies in file " + read.getWorkFile()
								+ Constants.newLine;
						message = message + "=> " + freqBuffer[iFreq] + "  : not a decimal number" + Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);

						throw new IllegalDataException();
					} // if end

				} // FOR END
			} // IF END

			/* MASS */
			mMass = pMass.matcher(currentLine);

			if (mMass.find()) {

				massBuffer = currentLine.split(" +");

				mass = massBuffer[3];
				mTestPositDecimal = pTestPositDecimal.matcher(massBuffer[3]);
				if (!mTestPositDecimal.find()) {
					write.end(); // end the file building

					String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
					message = message + "while reading molecular mass in file " + read.getWorkFile()
							+ Constants.newLine;
					message = message + "=> " + massBuffer[3] + ": not a positive decimal number" + Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
					throw new IllegalDataException();
				} // if end

			} // IF END

			/* INERTIA MOMENTS and molecular Linearity */

			mInertia2 = pInertia2.matcher(currentLine); // try to detect inertia moments of a linear molecule
			if (mInertia2.find()) {
				linearity = true;
				// if previous inertia moments (another previous geom) already loaded, erase
				// previous vector
				if (inertia.size() != 0) {
					inertia.removeAllElements();
				}
				inertiaBuffer = currentLine.split(" +");

				if (inertiaBuffer.length == 5) { // a blank plus three unused fields and one rotational cte
					inertiaBuffer[0] = inertiaBuffer[4]; // we use the first unused cell of inertiaBuffer array
				} else {
					write.end(); // end the file building

					String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
					message = message + "while reading a rotational constant in file " + read.getWorkFile()
							+ Constants.newLine;
					message = message + "only 5 fields were expected in line ** " + currentLine + " **"
							+ Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

					throw new IllegalDataException();
				} // end of if (inertiaBuffer.length==5)

				// checking if rotational constant are decimal numbers
				mTestDecimal = pTestDecimal.matcher(inertiaBuffer[0]);
				if (!mTestDecimal.find()) {
					String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
					message = message + "while reading Rotatonal Constants in file " + read.getWorkFile()
							+ Constants.newLine;
					message = message + "=> " + inertiaBuffer[0] + ", is not a decimal number" + Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
					throw new IllegalDataException();
				}
				// convert string to double and from GHz to Amu.bohr^2
				decimalNumber = Double.parseDouble(inertiaBuffer[0]);
				decimalNumber = decimalNumber * 1.0E09 * 8 * Math.pow(Math.PI, 2) / Constants.h;
				decimalNumber = 1 / decimalNumber;// in m2.kg
				decimalNumber = decimalNumber / (Constants.convertAmuToKg * Math.pow(Constants.a0, 2)); // in Amu.Bohr^2
				inertia.add(decimalNumber);

			} // end of if (mInertia2.find() )

			mInertia = pInertia.matcher(currentLine); // try to detect inertia moments of a non-linear molecule
			if (mInertia.find()) {
				// if previous inertia moments (another previous geom) already loaded, erase
				// previous vector
				if (inertia.size() != 0) {
					inertia.removeAllElements();
				}
				inertiaBuffer = currentLine.split(" +");

				if (inertiaBuffer.length == 7) { // a blank plus three unused fields and three rotational ctes
					inertiaBuffer[0] = inertiaBuffer[4]; // we use the first unused cell of inertiaBuffer array
					inertiaBuffer[1] = inertiaBuffer[5];
					inertiaBuffer[2] = inertiaBuffer[6];
				} else {
					write.end(); // end the file building
					String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
					message = message + "while reading rotational ctes in file " + read.getWorkFile()
							+ Constants.newLine;
					message = message + "only 6 fields were expected in line ** " + currentLine + " **"
							+ Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
					throw new IllegalDataException();
				} // end of if (inertiaBuffer.length==7)

				// checking if rotational constant are decimal numbers
				for (int i = 0; i < 3; i++) {
					mTestDecimal = pTestDecimal.matcher(inertiaBuffer[i]);
					if (!mTestDecimal.find()) {
						String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
						message = message + "while reading rotational constants in file " + read.getWorkFile()
								+ Constants.newLine;
						message = message + "=> " + inertiaBuffer[i] + ", is not a decimal number" + Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);

						throw new IllegalDataException();
					}
					// convert string to double and from GHz to Amu.bohr^2
					decimalNumber = Double.parseDouble(inertiaBuffer[i]);
					decimalNumber = decimalNumber * 1.0E09 * 8 * Math.pow(Math.PI, 2) / Constants.h;
					decimalNumber = 1 / decimalNumber;// in m2.kg
					decimalNumber = decimalNumber / (Constants.convertAmuToKg * Math.pow(Constants.a0, 2)); // in
																											// Amu.Bohr^2
					inertia.add(decimalNumber);
				} // end for

			} // end of if (mInertia.find() )

			/* ROTATIONAL SYMMETRY NUMBER */
			mSymNumber = pSymNumber.matcher(currentLine);

			if (mSymNumber.find()) {

				symNumberBuffer = currentLine.split(" +");
				symNumber = symNumberBuffer[4];
				int dot = symNumber.lastIndexOf('.');
				symNumber = symNumber.substring(0, dot);
				mTestInt = pTestInt.matcher(symNumber);
				if (!mTestInt.find()) {
					write.end(); // end the file building

					String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
					message = message + "while reading Rotational Symmetry Number in file " + read.getWorkFile()
							+ Constants.newLine;
					message = message + "=> " + symNumber + ", not an integer" + Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

					throw new IllegalDataException();
				} // if end

			} // IF END

			currentLine = read.oneString();
		} // while end

		// ********************************************//
		// NOW, WRITING properties on kinp input file //
		// ********************************************//
		// first of all: the archive must be present with keyword Freq

		// JOB NUMBER into the same gaussian output file
		if ((nJobs == 0)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
			message = message + "while reading Gaussian output file " + read.getWorkFile() + Constants.newLine;
			message = message + " \"1\\1\\\" or \"1|1\" pattern not found, job not terminated ..." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		if ((nJobs > 1)) {
			String message = "Warning: in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
			message = message + "while reading Gaussian output file " + read.getWorkFile() + Constants.newLine;
			message = message + "Multi-Job detected, last job is considered" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);
		} // if end

		if (archive.length() == 0) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
			message = message + "while reading electronic degeneracy in gaussian archive of " + read.getWorkFile()
					+ Constants.newLine;
			message = message + "=> \"1\\1\\\" or \"1|1\" pattern not found (archive not found)" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // if end

		else {

			// First, replace all pipe (gaussian under Windows) character present in the
			// archive string with a \ character (Linux)
			archive = archive.replace("|", "\\");

			// the archive must contains "\Freq\"
			// only needed to recognize a frequency job not to get the freq. values ...

			mFreqKey = pFreqKey.matcher(archive);
			if (!mFreqKey.find()) {
				write.end(); // end the file building

				String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
				message = message + "while checking the \\Freq\\ keyword in the archive of " + read.getWorkFile()
						+ Constants.newLine;
				message = message
						+ "=> a frequency job is required (when gaussian is employed), even for atomic species"
						+ Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

				throw new IllegalDataException(); // if end
			}
		} // end else archive.length

		/*
		 * ATOM number : needed to avoid printing Inertia moment and linearity
		 * information
		 */

		atomBuffer = archive.split("\\\\\\\\");
		atomBuffer = atomBuffer[3].split("\\\\");
		nAtoms = atomBuffer.length - 1;

		// checking the detected atom number
		if (nAtoms == 0) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
			message = message + "while reading atomic mass in file " + read.getWorkFile() + Constants.newLine;
			message = message + "pattern **has atomic number** not found; no atom found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		/* MASS */

		if (mass.equals("***")) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
			message = message + "while reading molecular mass in file " + read.getWorkFile() + Constants.newLine;
			message = message + "=> **Molecular mass:** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		else {
			write.oneString("*MASS (in amu)");
			write.oneString(mass);
			write.oneString("*END");
		} // else end

		/* ROTATIONAL SYMMETRY NUMBER IF AND ONLY IF NON-ATOMIC SYSTEM */

		if (symNumber.equals("***") && (nAtoms > 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
			message = message + "while reading rotational symmetry number for a system with more than one atom in file "
					+ read.getWorkFile() + Constants.newLine;
			message = message + "=> **Rotational symmetry number** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		else {
			if (nAtoms > 1) {
				write.oneString("*NUMBER OF SYMMETRY");
				write.oneString(symNumber);
				write.oneString("*END");
			} // if end
		} // else end

		/* VIBRATIONAL FREQUENCIES */
		if ((freq.size() == 0) && (nAtoms > 1)) {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
			message = message + "while reading vibrational frequencies for a system with more than one atom in file "
					+ read.getWorkFile() + Constants.newLine;
			message = message + "=> **Frequencies --** pattern not found" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		else {
			if (nAtoms > 1) {
				write.oneString("*FREQUENCIES (in cm-1)");
				String imaginaryFreq = "";

				for (int iFreq = 1; iFreq <= freq.size(); iFreq++) {

					// testing an imaginary frequency (represented by a negative number in gaussian)

					decimalNumber = Double.parseDouble((String) freq.get(iFreq - 1));
					if (decimalNumber > 0) {
						write.oneString((String) freq.get(iFreq - 1));
					} // if end
					else if (decimalNumber < 0) {
						imaginaryFreq = String.valueOf(Math.abs(decimalNumber)) + "i";
						write.oneString(imaginaryFreq);
					} // else if end
					else {// a frequency is null !!
						write.end(); // end the file building

						String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
						message = message + "while reading vibrational frequencies in file " + read.getWorkFile()
								+ Constants.newLine;
						message = message + "=> a frequency is null : " + (String) freq.get(iFreq - 1)
								+ Constants.newLine;
						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);

						throw new IllegalDataException();
					} // else decimalNumber < 0
				} // for end
				write.oneString("*END");
			} // if end
		} // else end

		/* ELECTRONIC DEGENERACY */

		write.oneString("*ELECTRONIC DEGENERACY");
		degenBuffer = archive.split(Pattern.quote("\\\\")); // a field ends with "//" symbol

		if (degenBuffer.length >= 4) {

			int backslash = degenBuffer[3].indexOf('\\'); // the fourth field is charge, multiplicity separated with a
															// comma
			stringBuffer = degenBuffer[3].substring(0, backslash); // extract charge and multiplicty
			int comma = stringBuffer.indexOf(','); // a comma separates charge and multiplicity
			String multi = degenBuffer[3].substring(comma + 1, backslash); // get the multiplicity number after the
																			// comma

			mTestInt = pTestInt.matcher(multi);
			if (!mTestInt.find()) {
				write.end(); // end the file building

				String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
				message = message + "while reading electronic degeneracy in file " + read.getWorkFile()
						+ Constants.newLine;
				message = message + "=> " + multi + ", not an integer" + Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
				throw new IllegalDataException();
			} // if end

			write.oneString(multi);
			write.oneString("*END");

			/* INERTIA MOMENTS */

			if (nAtoms > 1) {

				write.oneString("*MOMENT OF INERTIA (in Amu.bohr**2)");

				if (!linearity) { // three components to be written
					for (int iInertia = 0; iInertia < 3; iInertia++) {
						write.oneDouble((Double) inertia.get(iInertia));
					} // for end

				} // if end
				else { // only one component to be written
					write.oneDouble((Double) inertia.get(0));
				} // else end

				write.oneString("*END");

				/* LINEARITY */
				write.oneString("*LINEAR");
				if (linearity) {
					write.oneString("linear");
				} else {
					write.oneString("not linear");
				}
				write.oneString("*END");

			} // end of if (nAtoms >1)

		} // if end degenBuffer.length > 4

		else {// problem while reading the archive
			{
				write.end(); // end the file building

				String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
				message = message + "while reading electronic degeneracy from gaussian archive in " + read.getWorkFile()
						+ Constants.newLine;
				message = message + "the \\\\ pattern should be found three times at least in the archive ..."
						+ Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
				throw new IllegalDataException();
			} // else end
		}

		/* POTENTIAL ENERGY */

		write.oneString("*POTENTIAL ENERGY (in hartree)");
		// potential energy will be extracted from archive, in the section designed
		// by the "\\Version=" pattern : the subsection just before "S2=" pattern (for
		// unrestricted
		// calculation) or the "RMSD" pattern (for restricted computations) is the
		// energy value

		// get back the blocks of the archive after the "\\Version=" pattern
		mVersion = pVersion.matcher(archive);
		if (mVersion.find()) {
			versionBuffer = archive.split("\\\\Version=");
		} else {
			write.end(); // end the file building

			String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
			message = message + "while reading potential energy from gaussian archive in " + read.getWorkFile()
					+ Constants.newLine;
			message = message + "the \\\\Version= pattern not found in the archive ..." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} // else end

		// determine if it is a spin-unrestricted or spin-restricted job
		mS2 = pS2.matcher(archive);
		mS2_1 = pS2_1.matcher(archive);
		mS2A = pS2A.matcher(archive);
		if (mS2.find() && mS2_1.find() && mS2A.find()) {// spin unrestricted job
			// get back the fourth field (each field is separated by "//")

			energyBuffer = versionBuffer[1].split("\\\\S2=");
		}

		else {
			energyBuffer = versionBuffer[1].split("\\\\RMSD");
		} // spin-restricted job

		if (energyBuffer.length == 1) {
			write.end(); // end the file building
			String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
			message = message + "while reading potential energy from gaussian archive in file " + read.getWorkFile()
					+ Constants.newLine;
			message = message + "the \\RMSD pattern not found " + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		}
		energyBuffer = energyBuffer[0].split("\\\\");
		energyBuffer = energyBuffer[energyBuffer.length - 1].split("=");
		energy = energyBuffer[1]; // get the string value
		mTestNegatDecimal = pTestNegatDecimal.matcher(energy);
		if (!mTestNegatDecimal.find()) {
			write.end(); // end the file building
			String message = "Error in Class ChemicalSystem, in method kinpFromG09" + Constants.newLine;
			message = message + "while reading potential energy in file " + read.getWorkFile() + Constants.newLine;
			message = message + "=> " + energy + ", is not a negative decimal number" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} // if end

		else {
			write.oneString(energy);
			write.oneString("*END");
		}

		// tell the user the file kinp has been built : DEPRECATED since a new
		// kisthelpDialog asking for kinp location is now provided
		// String message = "Input file " + write.getWorkFile().getName() + " was
		// successfully built";
		// JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
		// JOptionPane.INFORMATION_MESSAGE);
		// JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
		// JDialog dialog = pane.createDialog(null, "KiSThelP");
		// dialog.setModal(false); // this says not to block background components
		// dialog.setLocation(50,50);
		// dialog.setVisible(true);

	} // end of kinpFromg09 method

	/********************/
	/* m a s s R e a d */ // to READ THE MASS in file.kinp , must be of KISTHEP file TYPE
	/******************/

	public void massRead(ActionOnFileRead read) throws IllegalDataException, IOException {

		String currentLine;

		mass = read.oneDouble();

		currentLine = read.oneString();// the next line must be the endOfInputSection
		if ((currentLine == null) || (!currentLine.toUpperCase().startsWith(Keywords.endOfInputSection)))

		{
			String message = "Error in Class ChemicalSystem, in method massRead" + Constants.newLine;
			message = message + "*END keyword is missing to end the MASS section in file " + read.getWorkFile()
					+ Constants.newLine;
			message = message + "=>" + currentLine + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		}

	} // end of massRead method

	/**********************************/
	/* f r e q u e n c i e s R e a d */ // to READ THE FREQUENCIES from file.kinp and convert them to Kelvin
	/********************************/ // computes the ZPE property. Read must be of KISTHEP file TYPE

	public void frequenciesRead(ActionOnFileRead read) throws IllegalDataException, IOException {

		String currentLine;
		String[] freqBuffer;
		boolean testOfEnd; // to test the end of the current read section in file.inp
		Vector freq = new Vector(); // the frequencies are stored in a vector
		int frequencyNumber;

		do {
			currentLine = read.oneString();
			freq.add(currentLine);

			if ((currentLine == null) || ((currentLine.startsWith("*"))
					&& (!currentLine.toUpperCase().startsWith(Keywords.endOfInputSection)))) {
				String message = "Error in class chemicalsystem, in method frequenciesRead" + Constants.newLine;
				message = message + " the string read is not a complex neither '*END', but: " + currentLine
						+ Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
				throw new IllegalDataException();
			}

			testOfEnd = currentLine.toUpperCase().startsWith(Keywords.endOfInputSection); // endOfInputSection
		} while (testOfEnd != true);

		freq.remove(freq.size() - 1); // remove the later element: "*END", which is not a frequency number ...

		frequencyNumber = freq.size();
		unscaledVibFreq = new Complex[frequencyNumber]; // creates an array of Complex class
		vibFreq = new Complex[frequencyNumber]; // creates an array of Complex class
		hrdsBarrier = new double[frequencyNumber]; // in J/molec, creates an array for barriers of the hindered rotors

		for (int iFreq = 0; iFreq < frequencyNumber; iFreq++) // filling the unscaled vibFreq array by the vector

		{

			// be sure that NONE blank character is at the beginning of the string !!
			// because java (split(" +")) will consider an additional field !! => " 3000"
			// will return an empty string AND 3000
			// thus, a buffer length of 2 instead of 1 !!
			// => one removes all starting blank charcater one the line

			currentLine = (String) freq.get(iFreq);
			if (currentLine.length() == 0) {// curious case

				String message = "Error in class ChemicalSystem, in method frequenciesRead" + Constants.newLine;
				message = message + "while trying to read a frequency " + Constants.newLine;
				message = message + "a line is empty in section *FREQUENCIES ... *END" + Constants.newLine;

				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
				throw new IllegalDataException();
			}

			int limit = currentLine.length();

			for (int iChar = 0; iChar < limit; iChar++) {// search and delete possible starting white space

				if (!currentLine.startsWith(" ")) {
					break;
				} else if (currentLine.length() > 1) {
					currentLine = currentLine.substring(1);
				}

			} // end of for (int iChar=0

			// test the case of a line containing only white spaces that became empty (more
			// precisely: it remains one white space !
			if ((currentLine.length() == 1) && (currentLine.charAt(0) == ' ')) {// case of only ONE starting blank
																				// character

				String message = "Error in class ChemicalSystem, in method frequenciesRead" + Constants.newLine;
				message = message + "while trying to read a frequency " + Constants.newLine;
				message = message + "a line contains only whitespaces in section *FREQUENCIES ... *END"
						+ Constants.newLine;

				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
				throw new IllegalDataException();
			}

			// next, split the full line in case of hindered rotor treatment (HRDS method)
			// in this case (HRDS), 2 values are expected instead of the simple frequency
			// value
			// namely: frequency (cm-1) and hindered rotor barrier in kJ/mol
			// System.out.println("avant paser");
			// System.out.println("0123456789012345");
			// System.out.println();
			freqBuffer = currentLine.split(" +");
			unscaledVibFreq[iFreq] = Complex.parseComplex(freqBuffer[0]); // first field in the line is the frequency
																			// value (cm-1) // either real or imaginary
																			// (one component)
			unscaledVibFreq[iFreq].times(Constants.convertCm_1ToKelvin);

			if (freqBuffer.length == 1) {

				// it is necessary to clone this array into the vibFreq array
				vibFreq[iFreq] = (Complex) unscaledVibFreq[iFreq].clone();
				hrdsBarrier[iFreq] = Constants.highEnergy; // in J/molec , no hindered rotor treatment

			} // end of if (freqBuffer.length==1) (harmonic oscillator)
			else {// (hindered rotor ?)
				if (freqBuffer.length == 2) {// hindered rotor treatment

					if (unscaledVibFreq[iFreq].getImagPart() != 0.0) {
						String message = "Error in class ChemicalSystem, in method frequenciesRead" + Constants.newLine;
						message = message + "while trying to read a frequency " + Constants.newLine;
						message = message + "a real number is expected for frequency for a hindered rotor treatment"
								+ Constants.newLine;

						JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage,
								JOptionPane.ERROR_MESSAGE);
						throw new IllegalDataException();

					}

					// it is necessary to clone this array into the vibFreq array
					vibFreq[iFreq] = (Complex) unscaledVibFreq[iFreq].clone();
					hrdsBarrier[iFreq] = Double.valueOf(freqBuffer[1]); // hindered rotor barrier in kJ/mol
					hrdsBarrier[iFreq] = hrdsBarrier[iFreq] * 1000 / Constants.NA; // convert from kJ/mol to J/molec

				} // end of if (freqBuffer.length==3)

				else {

					String message = "Error in class ChemicalSystem, in method frequenciesRead" + Constants.newLine;
					message = message + "while trying to read a frequency " + Constants.newLine;
					message = message + "expected number of fields is 1 or 2; found : " + freqBuffer.length
							+ Constants.newLine;

					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

					throw new IllegalDataException();

				}
			} // end of else if (freqBuffer.length==1)

			// testing if a frequency is null
			if (vibFreq[iFreq].getRealPart() == 0.0 && vibFreq[iFreq].getImagPart() == 0.0) {

				String message = "Error in class ChemicalSystem, in method frequenciesRead" + Constants.newLine;
				message = message + "while trying to read a frequency " + Constants.newLine;
				message = message + "A frequency is null:  " + (String) freq.get(iFreq) + Constants.newLine;

				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

				throw new IllegalDataException();
			} // if end

			// testing if an imaginary frequency has its imaginary part negative
			if (vibFreq[iFreq].getImagPart() < 0.0) {

				String message = "Error in class ChemicalSystem, in method frequenciesRead" + Constants.newLine;
				message = message + "while trying to read a frequency " + Constants.newLine;
				message = message + "The imaginary part of a frequency is negative:  " + (String) freq.get(iFreq)
						+ Constants.newLine;

				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

				throw new IllegalDataException();
			} // if end

		} // end of for (int iFreq=0;iFr ...

		// if and only if a session exists, get the scaling vib factor to scale the
		// frequencies
		// else, the chemical systems will be built without any information coming from
		// the session
		if (Session.getCurrentSession() != null) {
			scaleVibFreq(); // to compute ZPE !! and scale the vibrational frequencies by the scaling factor
							// updated in WorkSession
		}
	} // end of frequenciesRead

	/****************************************/
	/* s c a l e v i b f r e q */ //
	/**************************************/

	public void scaleVibFreq() {

		for (int iFreq = 0; iFreq < unscaledVibFreq.length; iFreq++) // filling the unscaled vibFreq array by the vector

		{

			vibFreq[iFreq].replacedBy(unscaledVibFreq[iFreq], Session.getCurrentSession().getScalingFactor());

		} // end of for (int iFreq=0;iFr ...

		computeZPE();

	} // end of scaleVibFreq()

	/****************************************/
	/* s y m m e t r y N u m b e r R e a d */ // to READ THE SYMMETRY NUMBER .kinp , must be of KISTHEP file TYPE
	/**************************************/

	public void symmetryNumberRead(ActionOnFileRead read) throws IllegalDataException, IOException {

		String currentLine;

		currentLine = read.oneString();
		StringTokenizer tok = new StringTokenizer(currentLine, " "); // a blank field separator
		try {
			symmetryNumber = Integer.parseInt(tok.nextToken());
		} catch (NoSuchElementException e) {

			String message = e.getMessage();
			message = message + "NoSuchElementException caught in Class ChemicalSystem, in Method symmetryNumberRead"
					+ Constants.newLine;
			message = message + "while attempting to read the symmetry number" + Constants.newLine;
			message = message + "=>" + currentLine + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		} catch (NumberFormatException err) {

			String message = err.getMessage();
			message = message + "NumberFormatException caught in Class ChemicalSystem, in Method symmetryNumberRead"
					+ Constants.newLine;
			message = message + "the string read in not a integer, but data read is: " + symmetryNumber
					+ Constants.newLine;
			message = message + "=>" + currentLine + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		}
		currentLine = read.oneString();
		if ((currentLine == null) || (!currentLine.toUpperCase().startsWith(Keywords.endOfInputSection))) {
			String message = "Error in Class ChemicalSystem, in Method symmetryNumberRead" + Constants.newLine;
			message = message + "*END keyword is missing to end this section" + Constants.newLine;
			message = message + "=>" + currentLine + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		}

	} // end of symmetryNumberRead method

	/**************************/
	/* i n e r t i a R e a d */ // to READ THE MOMENT OF INERTIA from file.kinp , must be of KISTHEP file TYPE
	/************************/

	public void inertiaRead(ActionOnFileRead read) throws IllegalDataException, IOException {

		String currentLine;
		boolean testOfEnd;
		Vector inert = new Vector();
		int inertiaMomentNumber;

		do {
			currentLine = read.oneString();
			inert.add(currentLine);

			if ((currentLine == null) || ((currentLine.startsWith("*"))
					&& (!currentLine.toUpperCase().startsWith(Keywords.endOfInputSection)))) {
				String message = "Error in Class ChemicalSystem, in Method InertiaRead" + Constants.newLine;
				message = message + "String read is not a double neither '*END', but: " + currentLine
						+ Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

				throw new IllegalDataException();
			}

			testOfEnd = currentLine.toUpperCase().startsWith(Keywords.endOfInputSection);
		} // end of do while

		while (testOfEnd != true);
		inert.remove(inert.size() - 1);
		inertiaMomentNumber = inert.size();
		inertia = new double[inertiaMomentNumber];
		try {
			for (int iInertia = 0; iInertia < inertiaMomentNumber; iInertia++) {
				inertia[iInertia] = Double.parseDouble((String) inert.get(iInertia));
			}
		} catch (NumberFormatException err) {

			String message = "NumberFormatException caught in Class ChemicalSystem, in Method inertiaRead"
					+ Constants.newLine;
			message = message + "String read is not a double, but data read is: " + currentLine + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		}
	} // end of inertiaRead

	/******************************************/
	/* p o t e n t i a l E n e r g y R e a d */ // to READ THE POTENTIAL ENERGY
	/****************************************/ // and convert it from hartree to joule .kinp , must be of KISTHEP file
												// TYPE

	public void potentialEnergyRead(ActionOnFileRead read) throws IllegalDataException, IOException {

		String currentLine;

		up = read.oneDouble() * Constants.convertHartreeToJoule;

		currentLine = read.oneString();
		if ((currentLine == null) || (!currentLine.toUpperCase().startsWith(Keywords.endOfInputSection)))

		{
			String message = "Error in Class ChemicalSystem, in Method potentialEnergyRead" + Constants.newLine;
			message = message + "String read is not '*END', but: " + currentLine + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		}
	} // end of potentialEnergyRead

	/****************************************************/
	/* e l e c t r o n i c D e g e n e r a c y R e a d */ // to READ THE ELECTRONIC DEGENERACY, .kinp , must be of
															// KISTHEP file TYPE
	/**************************************************/

	public void electronicDegeneracyRead(ActionOnFileRead read) throws IllegalDataException, IOException {

		String currentLine;

		currentLine = read.oneString();
		StringTokenizer tok = new StringTokenizer(currentLine, " ");
		try {
			elecDegener = Integer.parseInt(tok.nextToken());
		} catch (NoSuchElementException e) {

			String message = e.getMessage();
			message = message
					+ "NoSuchElementException caught in Class ChemicalSystem, in Method electronicDegeneracyRead"
					+ Constants.newLine;
			message = message + "while attempting to read the electronic degeneracy from file.kinp" + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} catch (NumberFormatException err) {

			String message = err.getMessage();
			message = message
					+ "NumberFormatException caught in Class ChemicalSystem, in Method electronicDegeneracyRead"
					+ Constants.newLine;
			message = message + "String read in not a integer, but data read is: " + currentLine + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		}
		currentLine = read.oneString();
		if ((currentLine == null) || (!currentLine.toUpperCase().startsWith(Keywords.endOfInputSection))) {

			String message = "Error in Class ChemicalSystem, in Method electronicDegeneracyRead" + Constants.newLine;
			message = message + "String read is not '*END', but: " + currentLine + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();
		}
	} // end of electronic DegeneracyRead method

	/************************/
	/* l i n e a r R e a d */ // to read linearity information, .kinp , must be of KISTHEP file TYPE
	/**********************/

	public void linearRead(ActionOnFileRead read) throws IllegalDataException, IOException {

		String currentLine;
		boolean testOfCurrentLine;

		currentLine = read.oneString();
		testOfCurrentLine = (((currentLine.toUpperCase().contains("LINEAR"))
				|| (currentLine.toUpperCase().contains("NOT LINEAR"))));
		if (testOfCurrentLine == false) {
			String message = "Error in Class ChemicalSystem, in method linearRead" + Constants.newLine;
			message = message + "String must be 'linear' or 'not linear' ...  found: " + currentLine
					+ Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		} else {
			if (currentLine.toUpperCase().contains("LINEAR")) {
				linear = true;
			}
			if (currentLine.toUpperCase().contains("NOT LINEAR")) {
				linear = false;
			}
			currentLine = read.oneString();
			if ((currentLine == null) || (!currentLine.toUpperCase().startsWith(Keywords.endOfInputSection))) {

				String message = "Error in Class ChemicalSystem, in method linearRead" + Constants.newLine;
				message = message + "String read is not '*END', but: " + currentLine + Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
				throw new IllegalDataException();
			}
		}
	} // end of linearRead

	/**************************************/
	/* t e s t C o h e r e n c e D a t a */ // TEST OF DATA COHERENCE
	/************************************/

	public void testCoherenceData() throws IllegalDataException {

		// for an atom, 3 section must be given: MASS, Potential energy, Electronic
		// degeneracy
		// for a molecule (linear or not linear), 7 sections must be given : MASS,
		// Potential energy, Electronic degeneracy +
		// FREQ, INERTIA, SYM NUMBER and LINEARITY

		int negativeCurvature = 0;
		if ((locatedSection.size() != 3) && (locatedSection.size() != 7)) {
			String message = "Error in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
			message = message + "wrong number of sections in .kinp data file " + Constants.newLine;
			message = message + "number of sections detected: " + locatedSection.size() + Constants.newLine;
			message = message
					+ "Please supply 3 sections for an atom, or 7 sections for a molecule; please see documentation"
					+ Constants.newLine;
			for (int iSection = 1; iSection <= locatedSection.size(); iSection++) {
				message = message + locatedSection.elementAt(iSection - 1) + Constants.newLine;
			} // end for
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

			throw new IllegalDataException();

		} // if end

		// possible ATOMIC case detected
		if (locatedSection.size() == 3) { // the 3 sections must be the 3 appropriate sections

			if (locatedSection.contains(Keywords.startOfUpInputSection) &&
					(locatedSection.contains(Keywords.startOfMassInputSection)) &&
					(locatedSection.contains(Keywords.startOfDegeneracyElectronicInputSection))) {
				atomic = true;
			} // if end
			else {

				String message = "Error in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
				message = message + "3 sections detected in .kinp data file, thus an atom is considered "
						+ Constants.newLine;
				message = message + "but sections should be : Mass, Electronic degeneracy and potential energy "
						+ Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
				throw new IllegalDataException();
			} // else end

		} // if end

		// possible MOLECULE case detected
		if (locatedSection.size() == 7) { // since each section was checked before, it is a molecule case
			atomic = false;

			// check if all 7 sections are present!
		} // if end

		// additionnal checking for a molecule
		// the type (Integer, decimal, Complex) of the properties have been already
		// checked into dataRead method or kinpFromGaussian
		// now, checking mathematical properties related to chemical properties (sign,
		// imaginary part ...)

		if (atomic == false) {

			if (mass <= 0) {
				String message = "Error in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
				message = message + "mass must be positive ...  " + mass + Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

				throw new IllegalDataException();
			}

			if (elecDegener <= 0) {
				String message = "Error in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
				message = message + "electronic degeneracy must be > 0  ... : " + elecDegener + Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
				throw new IllegalDataException();
			}

			if (up >= 0) {
				String message = "Error in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
				message = message + "a potential energy should be negative; value read is: "
						+ (up / Constants.convertHartreeToJoule) + Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);

				throw new IllegalDataException();
			}

			if (symmetryNumber <= 0) {
				String message = "Error in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
				message = message + "symmetry number should be positive; value read is: " + symmetryNumber
						+ Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
				throw new IllegalDataException();
			}

			if ((linear == true) && (inertia.length != 1)) {
				String message = "Error in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
				message = message + "Only one moment of inertia is expected for a linear system ..."
						+ Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
				throw new IllegalDataException();
			}
			if ((linear == false) && (inertia.length != 3)) {
				String message = "Error in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
				message = message + "Three moments of inertia are expected for a non-linear system ..."
						+ Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
				throw new IllegalDataException();

			}

			for (int i = 0; i < inertia.length; i++)
				if (inertia[i] <= 0) {
					String message = "Error in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
					message = message + "moment of inertia should be positive; data read is: " + inertia[i]
							+ Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
					throw new IllegalDataException();

				} // if end
			// for end

			// V I B F R E Q U E N C I E S
			Complex currentFreq = new Complex(0.0, 0.0);

			for (int iFreq = 0; iFreq < vibFreq.length; iFreq++) {

				currentFreq = vibFreq[iFreq];
				currentFreq.times(1.0 / Constants.convertCm_1ToKelvin);

				// testing if a frequency is null
				if (currentFreq.getRealPart() == 0.0 && currentFreq.getImagPart() == 0.0) {

					String message = "Error in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
					message = message + "while trying to read a frequency " + Constants.newLine;
					message = message + "One frequency is null:  " + currentFreq + Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
					throw new IllegalDataException();
				} // if end

				// testing if an imaginary frequency has its imaginary part negative
				if (currentFreq.getImagPart() < 0.0) {

					String message = "Error in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
					message = message + "while trying to read a frequency " + Constants.newLine;
					message = message + "The imaginary part of one frequency is negative:  " + currentFreq
							+ Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
					throw new IllegalDataException();
				} // if end

				// testing if a real frequency is negative
				if (currentFreq.getRealPart() < 0.0) {

					String message = "Error in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
					message = message + "while trying to read a frequency " + Constants.newLine;
					message = message + "The real part of one frequency is negative:  " + currentFreq
							+ Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
					throw new IllegalDataException();
				} // if end

				// each frequency must be purely real or purely imaginary
				if ((currentFreq.getRealPart() != 0.0) && (currentFreq.getImagPart() != 0.0)) {
					String message = "Error in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
					message = message + "while trying to read a frequency " + Constants.newLine;
					message = message + "Frequency must be x or iy, not x+iy; value found:  " + currentFreq
							+ Constants.newLine;
					JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
					throw new IllegalDataException();
				} // if end

				// count the number of imaginary frequencies
				if (currentFreq.getImagPart() != 0.0) {
					negativeCurvature = negativeCurvature + 1;
				}

			} // end of for (int iFreq=0;iFr ...

			// a TS must have all its frequencies real positive but only one imaginary
			if (nature.equals(saddlePoint1) && (negativeCurvature != 1)) {

				String message = "Error in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
				message = message + "while trying to read a frequency for a TS" + Constants.newLine;
				message = message + "For a TS, imaginary frequencies number should be one, but " + negativeCurvature
						+ " found" + Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
				throw new IllegalDataException();
			} // if end

			// an anypoint can have 0, 1, or more imaginary frequencies
			// For an anypoint, more than one imaginary is possible, and will be removed
			// from the partition, entropic and energetic calculations,

			// Rather, a reaction path point should ONLY have 1 imaginary point, but
			// practically, from electronic structure calculations
			// it can have 0, 1, or more than one imaginary boundary modes normal to the
			// reaction mode
			// In KISTHEP, 0 or 1, or more imaginary frequency will be accepted for a
			// reaction path point, but
			// a) if 0 imaginary frequency are found, then, the smallest frequency found in
			// the .kinp file
			// will design the reaction mode (thus removed from all partition function and
			// rate
			// constant treatments) !! That is the reason why, in this case, To handle a
			// homogeneous set of data the smallest frequency is set
			// to imaginary number to design the reaction mode;
			// b) if more than 1 imaginary frequency are found, corresponding
			// (negativeCurvature-1) imaginary frequencies are removed from statistical
			// treatments

			if (nature.equals(pathPoint) && (negativeCurvature > 1)) {

				String message = "Warning in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
				message = message
						+ "in reaction-path points file, for one of generalized-TS, more than one imaginary frequencies found"
						+ Constants.newLine;
				message = message + "=>         " + (negativeCurvature - 1)
						+ " imaginary frequencies removed from statistical treatments" + Constants.newLine;
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);

				// correcting for that by removing any generalized vib. mode (transversal to the
				// reaction path)
				// with imaginary frequency; frequency set to 0, except the largest negative
				// curvature that is kept

				double largestCurvat = 0.0;
				double currentImgFreq = 0.0;
				int iLargestCurvat = 0;
				for (int iFreq = 0; iFreq < vibFreq.length; iFreq++) {

					currentImgFreq = vibFreq[iFreq].getImagPart();
					if (currentImgFreq != 0.0) {
						if (currentImgFreq > largestCurvat) {
							largestCurvat = currentImgFreq;
							iLargestCurvat = iFreq;
						}
						vibFreq[iFreq] = new Complex(0.0, 0.0);
					} // end if (currentImgFreq!=0.0)
				} // end for (int iFreq=0;iFreq<vibFreq.length;iFreq++)
				vibFreq[iLargestCurvat] = new Complex(0.0, largestCurvat);

				// throw new IllegalDataException();
			} // if end

			if (nature.equals(pathPoint) && (negativeCurvature == 0)) {

				String message = "Warning in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
				message = message
						+ "in reaction-path points file, for one of generalized-TS, none imaginary frequencies found"
						+ Constants.newLine;
				message = message
						+ "=>  To handle a homogeneous set of data the smallest frequency is set to imaginary number to design the reaction mode";
				JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);

				// find
				double smallestCurvat = 1000000.0;
				double currentRealFreq = 0.0;
				int iSmallestCurvat = 0;
				for (int iFreq = 0; iFreq < vibFreq.length; iFreq++) {
					currentRealFreq = vibFreq[iFreq].getRealPart();
					if (currentRealFreq != 0.0) {
						if (currentRealFreq < smallestCurvat) {
							smallestCurvat = currentRealFreq;
							iSmallestCurvat = iFreq;
						}
					} // end if (currentRealFreq < smallestCurvat)
				} // for end
				vibFreq[iSmallestCurvat] = new Complex(0.0, smallestCurvat);

			} // if end

			// a minimum must have all its frequencies real positive, but only a warning
			// will be set to the user:
			if (nature.equals(minimum) && (negativeCurvature > 0)) {

				String message = "WARNING in Class ChemicalSystem, in method testCoherenceData" + Constants.newLine;
				message = message + negativeCurvature + " imaginary vibrational freq. have been detected"
						+ Constants.newLine;
				message = message + "For a minimum, frequencies have to be real, not imaginary";
				JOptionPane.showMessageDialog(Interface.getKisthepInterface(), message, Constants.kisthepMessage,
						JOptionPane.WARNING_MESSAGE);
				// throw new IllegalDataException();: DEPRECATED since a warning is sent instead
				// of an error (for students)
			} // if end

		} // end of if(atomic==false)

	} // end of testCoherenceData method

	/************************************************************************************************************************/

	public String getNature() {
		return nature;
	}

	/*********************************************/
	/* s e t N a t u r e */
	/********************************************/

	public void setNature(String nature) {
		this.nature = nature;
	}

	/*********************************************/
	/* g e t A t o m i c */
	/********************************************/

	public boolean getAtomic() {
		return atomic;
	}

	/*********************************************/
	/* g e t L i n e a r */
	/********************************************/
	public boolean getLinear() {
		return linear;
	}

	/*********************************************/
	/* s a v e */
	/********************************************/

	public void save(ActionOnFileWrite write) throws IOException {
		write.oneString("atomic :");
		write.oneString(String.valueOf(atomic));
		write.oneString("linearity :");
		write.oneString(String.valueOf(linear));
		write.oneString("mass :");
		write.oneDouble(mass);
		if (atomic == false) {
			write.oneString("length of vibFreq array :");
			write.oneInt(vibFreq.length);
			write.oneString("unscaled vibrational frequencies (in K):");
			for (int iComponent = 0; iComponent < vibFreq.length; iComponent++) {
				write.oneComplex(unscaledVibFreq[iComponent]);

			} // end of for

			write.oneString("scaled vibrational frequencies (in K):");
			for (int iComponent = 0; iComponent < vibFreq.length; iComponent++) {
				write.oneComplex(vibFreq[iComponent]);
			} // end of for

			write.oneString("Hindered rotor treatment for each vib. Frequency (the rotor barrier is given)");
			for (int iComponent = 0; iComponent < vibFreq.length; iComponent++) {
				write.oneDouble(hrdsBarrier[iComponent]);
			} // end of for

			write.oneString("length of inertia array :");
			write.oneInt(inertia.length);
			write.oneString("inertia moments :");
			for (int jComponent = 0; jComponent < inertia.length; jComponent++) {
				write.oneDouble(inertia[jComponent]);
			} // end of for
			write.oneString("symmetryNumber :");
			write.oneInt(symmetryNumber);
			write.oneString("ZPE :");
			write.oneDouble(ZPE);
		}
		write.oneString("Up :");
		write.oneDouble(up);
		write.oneString("elecDegener :");
		write.oneInt(elecDegener);
		write.oneString("nature :");
		write.oneString(nature);

	}

	/*********************************************/
	/* l o a d */
	/********************************************/

	public void load(ActionOnFileRead read) throws IOException, IllegalDataException {
		read.oneString();
		atomic = Boolean.valueOf(read.oneString()).booleanValue();
		read.oneString();
		linear = Boolean.valueOf(read.oneString()).booleanValue();
		read.oneString();

		mass = read.oneDouble();

		if (atomic == false) {
			read.oneString();

			int complexArraySize = read.oneInt();
			vibFreq = new Complex[complexArraySize];
			unscaledVibFreq = new Complex[complexArraySize];
			hrdsBarrier = new double[complexArraySize];
			read.oneString();

			for (int iComponent = 0; iComponent < vibFreq.length; iComponent++) {
				unscaledVibFreq[iComponent] = read.oneComplex();
			}

			read.oneString();
			for (int iComponent = 0; iComponent < vibFreq.length; iComponent++) {
				vibFreq[iComponent] = read.oneComplex();
			}

			read.oneString();
			for (int iComponent = 0; iComponent < vibFreq.length; iComponent++) {
				hrdsBarrier[iComponent] = read.oneDouble();
			}

			read.oneString();
			inertia = new double[read.oneInt()];
			read.oneString();
			for (int jComponent = 0; jComponent < inertia.length; jComponent++) {
				inertia[jComponent] = read.oneDouble();
			}
			read.oneString();
			symmetryNumber = read.oneInt();
			read.oneString();
			ZPE = read.oneDouble();
		}
		read.oneString();
		up = read.oneDouble();
		read.oneString();
		elecDegener = read.oneInt();
		read.oneString();
		nature = read.oneString();
	}

	/*-------------------------------------------------------------------*/
	/* C O M P U T E Z P E */
	/*-------------------------------------------------------------------*/
	public void computeZPE() {

		// compute ZPE in J/mol

		// initialise the ZPE property

		ZPE = 0.0; // in J/mol
		double currentFrequency;

		// variables for HRDS correction
		// taken from Richard B. McClurg, Richard C. Flagan and William A. Goddard, J.
		// Chem. Phys. 106 (16), 22 April 1997
		double hrdsCorrection = 0;
		double r;

		for (int iFreq = 0; iFreq < vibFreq.length; iFreq++) {

			if (vibFreq[iFreq].getImagPart() == 0.0) {
				currentFrequency = vibFreq[iFreq].getRealPart(); // we get the real part of complex (in K!!)

				ZPE = ZPE + 0.5 * Constants.R * currentFrequency; // ZPE in J/mol !

				// !! if current frequency mode is treated as a hindered rotor, a correction
				// must be included
				if (hrdsBarrier[iFreq] != Constants.highEnergy) {
					r = hrdsBarrier[iFreq] / (currentFrequency * Constants.kb); // (J/molec / J/molec) => unit less

					hrdsCorrection = Constants.R * currentFrequency / (2 + 16 * r); // thus, for r=0 (free rotor), (1/2
																					// hNU) is removed from the ZPE
																					// note that hrdsCorrection is
																					// converted to J/mol

					ZPE = ZPE - hrdsCorrection;

				} // end of if (hrdsPeriodic[iFreq] != 0)

			} // end of if frequencies

		} // end of for

	} // End of computeZPE() method
	/*-------------------------------------------------------------------*/

	/*****************/
	/* g e t Z P E */
	/*****************/

	public double getZPE() {
		return ZPE; // in J/mol
	} // end of the getZPE method

	/*****************/
	/* g e t S y m N u m b */
	/*****************/

	public int getSymNumb() {
		return symmetryNumber;
	} // end of the getSymNumb method

	/*****************/
	/* s e t S y m N u m b */
	/*****************/

	public void setSymNumb(int newRotSymNumb) throws IllegalDataException {

		if (newRotSymNumb > 0) {
			symmetryNumber = newRotSymNumb;
		} else {

			String message = "Error in Class ChemicalSystem, in method setSymNumb" + Constants.newLine;
			message = message + "Attempt is done to set rotational symmetry number <= 0 ..." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		}
	} // end of the setSymNumb method

	/*****************/
	/* g e t e l e c D e g e n e r */
	/*****************/

	public int getElecDegener() {
		return elecDegener;
	} // end of the getElecDegener method

	/*****************/
	/* g e t I n i t i a l E l e c D e g e n e r */
	/*****************/

	public int getInitialElecDegener() {
		return initialElecDegener;
	} // end of the getInitialElecDegener method

	/*****************/
	/* g e t I n i t i a l S y m N u m b */
	/*****************/

	public int getInitialSymNumb() {
		return initialSymmetryNumber;
	} // end of the getInitialSymNumb method

	/*****************/
	/* s e t I n i t i a l S y m N u m b */
	/*****************/

	public void setSymNumbToInitialValue() {
		symmetryNumber = initialSymmetryNumber;
	} // end of the setSymNumbToInitialValue method

	/*****************/
	/* s e t E l e c D e g e n e r */
	/*****************/

	public void setElecDegener(int newElecDegener) throws IllegalDataException {
		if (newElecDegener > 0) {
			elecDegener = newElecDegener;
		} else {
			String message = "Error in Class ChemicalSystem, in method setElecDegener" + Constants.newLine;
			message = message + "Attempt is done to set electronic degeneracy <= 0 ..." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		}
	} // end of the setElecDegener method

	/********************/
	/* s e t E l e c D e g e n e r T o I n i t i a l V a l u e */
	/********************/

	public void setElecDegenerToInitialValue() {
		elecDegener = initialElecDegener;
	} // end of setElecDegenerToInitialValue

	/***********************/
	/* g e t A t o m N b */
	/*********************/

	public double getAtomNb() {
		return atomNb;
	} // end of the getZPE method

	/********************/
	/* g e t M a s s */
	/********************/

	public double getMass() {
		return mass;
	} // end of getMass

	/********************/
	/* s e t M a s s */
	/********************/

	public void setMass(double newMass) throws IllegalDataException {

		if (mass > 0) {
			mass = newMass;
		} else {

			String message = "Error in Class ChemicalSystem, in method setMass" + Constants.newLine;
			message = message + "Attempt is done to set mass <= 0 ..." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		}
	} // end of setMass

	/********************/
	/* s e t M a s s T o I n i t i a l V a l u e */
	/********************/

	public void setMassToInitialValue() {
		mass = initialMass;
	} // end of setMassToInitialValue

	/**********************************/
	/* g e t I n i t i a l M a s s */
	/********************************/

	public double getInitialMass() {
		return initialMass;
	} // end of getInitialMass

	/***************************/
	/* g e t I n e r t i a */
	/************************/

	public double[] getInertia() {
		return inertia;
	} // end of getInertia

	/***************************/
	/* g e t I n e r t i a */
	/************************/

	public String[] getInertiaString() throws runTimeException {

		String[] stringInertia = new String[3];
		for (int i = 0; i < inertia.length; i++) {
			stringInertia[i] = Maths.format(inertia[i], "0.00");

		}

		return stringInertia;
	} // end of getInitialInertia

	/***************************/
	/* g e t I n i t i a l I n e r t i a */
	/************************/

	public String[] getInitialInertia() throws runTimeException {

		String[] stringInertia = new String[3];
		for (int i = 0; i < initialInertia.length; i++) {
			stringInertia[i] = Maths.format(initialInertia[i], "0.00");

		}

		return stringInertia;
	} // end of getInitialInertia

	/***************************/
	/* s e t I n e r t i a */
	/************************/

	public void setInertia(double newInertia, int inertiaIndex) throws IllegalDataException {

		if ((newInertia > 0) && ((inertiaIndex == 0) || (inertiaIndex == 1) || (inertiaIndex == 2))) {
			inertia[inertiaIndex] = newInertia;
		} else {
			String message = "Error in Class ChemicalSystem, in method setInertia" + Constants.newLine;
			message = message + "Attempt is done to set inertia <= 0, or inertia index out of range 0,1 or 2 ..."
					+ Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		}

	} // end of setInertia

	/********************/
	/* s e t I n e r t i a T o I n i t i a l V a l u e */
	/********************/

	public void setInertiaToInitialValue() {

		for (int i = 0; i < inertia.length; i++) {
			inertia[i] = initialInertia[i];
		}

	} // end of setInertiaToInitialValue

	/***************/
	/* g e t U p */
	/***************/

	public double getUp() {
		return up;
	} // end of the // modif fred eric getUp // fin modif method

	/********************/
	/* s e t U p */
	/********************/

	public void setUp(double newUp) throws IllegalDataException {

		if (newUp < 0) {
			up = newUp;
		} else {

			String message = "Error in Class ChemicalSystem, in method setUp" + Constants.newLine;
			message = message + "Attempt is done to set Potential Energy >= 0 ..." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		}
	} // end of setUp

	/********************/
	/* s e t u p T o I n i t i a l V a l u e */
	/********************/

	public void setUpToInitialValue() {
		up = initialUp;
	} // end of setUpToInitialValue

	/******************************/
	/* g e t I n i t i a l U p */
	/***************************/

	public double getInitialUp() {
		return initialUp;
	} // end of getInitialUp

	/*******************************/
	/* g e t V i b F r e q I m a g */
	/********************************/

	// RETURN the imaginary SCALED (!!) vibrational frequency
	public Complex getVibFreqImag() {

		Complex vibFreqImag = new Complex(0.0, 0);

		for (int ifreq = 0; ifreq < vibFreq.length; ifreq++) {

			if (vibFreq[ifreq].getImagPart() != 0.0) {

				vibFreqImag = new Complex(vibFreq[ifreq]);
				break;
			}

		}

		return vibFreqImag;

	} // end of the getVibFreqImag() method

	/*******************************/
	/* g e t U n s c a l e d V i b F r e q I m a g */
	/********************************/

	// RETURN the imaginary SCALED (!!) vibrational frequency
	public Complex getUnscaledVibFreqImag() {

		Complex vibFreqImag = new Complex(0.0, 0);

		for (int ifreq = 0; ifreq < unscaledVibFreq.length; ifreq++) {

			if (unscaledVibFreq[ifreq].getImagPart() != 0.0) {

				vibFreqImag = new Complex(unscaledVibFreq[ifreq]);
				break;
			}

		}

		return vibFreqImag;

	} // end of the getVibFreqImag() method

	/*******************************/
	/* g e t V i b R e a l F r e q */
	/********************************/

	// RETURNS all real vib freq SCALED (!!) in CM-1
	// with indexes correspondings to the indexes in the original array (including
	// possinle imaginary frequencies)
	public Vector getVibRealFreqInCM_1() throws runTimeException {

		Vector result = new Vector();
		String[] realFreq = new String[vibFreq.length]; // real frequency at position realFreqIndex in the original
														// array
		int[] realFreqIndex = new int[vibFreq.length];
		int iRealFreq = 0;
		for (int ifreq = 0; ifreq < vibFreq.length; ifreq++) {

			if (vibFreq[ifreq].getImagPart() == 0.0) {
				realFreq[iRealFreq] = Maths.format(vibFreq[ifreq].getRealPart() / Constants.convertCm_1ToKelvin, "0.0");
				realFreqIndex[iRealFreq] = ifreq;
				iRealFreq++;
			}
		}
		result.add(realFreq);
		result.add(realFreqIndex);
		return result;

	} // end of the getVibFreqImag() method

	/********************************************/
	/* g e t S o r t e d V i b R e a l F r e q */
	/********************************************/

	// RETURNS all real vib freq SCALED (!!) in CM-1, but sorted in ascendant order

	public double[] getSortedVibRealFreq() throws runTimeException {

		double[] realFreq = new double[vibFreq.length];
		int iRealFreq = 0;

		for (int ifreq = 0; ifreq < vibFreq.length; ifreq++) {

			if (vibFreq[ifreq].getImagPart() == 0.0) {

				realFreq[iRealFreq] = vibFreq[ifreq].getRealPart() / Constants.convertCm_1ToKelvin;
				iRealFreq = iRealFreq + 1;
			}
		}

		Arrays.sort(realFreq, 0, iRealFreq);
		return realFreq;

	} // end of the getSortedVibRealFreq() method

	/********************************************/
	/* g e t S o r t e d V i b I m F r e q */
	/********************************************/

	// RETURNS all Imaginary vib freq SCALED (!!) in CM-1, converted in negative
	// numbers, and sorted in ascendant order

	public double[] getSortedVibImFreq() throws runTimeException {

		double[] imFreq = new double[vibFreq.length];
		int iImFreq = 0;

		for (int ifreq = 0; ifreq < vibFreq.length; ifreq++) {

			if (vibFreq[ifreq].getRealPart() == 0.0) {

				imFreq[iImFreq] = -1 * vibFreq[ifreq].getImagPart() / Constants.convertCm_1ToKelvin;
				iImFreq = iImFreq + 1;
			}
		}

		Arrays.sort(imFreq, 0, iImFreq);
		return imFreq;

	} // end of the getSortedVibImFreq method

	/*******************************/
	/* g e t I n i t i a l V i b R e a l F r e q */
	/********************************/

	// RETURNS all initial real vib freq SCALED values in cm-1 (!!)
	public String[] getInitialRealVib() throws runTimeException {

		String[] realFreq = new String[initialUnscaledVibFreq.length]; // real frequency at position realFreqIndex in
																		// the original array UNSCALED !
		int iRealFreq = 0;
		for (int ifreq = 0; ifreq < initialUnscaledVibFreq.length; ifreq++) {

			if (initialUnscaledVibFreq[ifreq].getImagPart() == 0.0) {
				realFreq[iRealFreq] = Maths.format(initialUnscaledVibFreq[ifreq].getRealPart()
						* Session.getCurrentSession().getScalingFactor() / Constants.convertCm_1ToKelvin, "0.0");
				iRealFreq++;
			}
		}
		return realFreq;

	} // end of the getVibFreqImag() method

	/***************************/
	/* s e t U n s c a l e d V i B */
	/************************/
	// replace a vib frequency at the given index by a real frequency in the
	// UNSCALED VIB ARRAY !

	public void setUnscaledVib(double newVib, int vibIndex) throws IllegalDataException {

		if ((newVib > 0) && ((vibIndex >= 0) && (vibIndex < vibFreq.length))) {
			newVib = newVib / Session.getCurrentSession().getScalingFactor();
			unscaledVibFreq[vibIndex] = new Complex(newVib, 0);
			scaleVibFreq(); // very important to rescale frequencies
		}

		else {

			String message = "Error in Class ChemicalSystem, in method setUnscaledVib" + Constants.newLine;
			message = message + "vib. freq. index out of range ..." + Constants.newLine;
			JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
			throw new IllegalDataException();
		}

	} // end of setVib

	/********************/
	/* s e t U n s c a l e d V i b T o I n i t i a l V a l u e */
	/********************/

	public void setUnscaledVibToInitialValue() {

		for (int i = 0; i < initialUnscaledVibFreq.length; i++) {
			unscaledVibFreq[i] = (Complex) initialUnscaledVibFreq[i].clone();
		}

	} // end of setUnscaledVibToInitialValue

	/********************/
	/* s a v e I n p u t s */
	/********************/

	public void saveInputs(ActionOnFileWrite write) throws runTimeException {

		String tmp = "";

		// save all current molecular information in a .kinp file
		// (molecular information may be changed by user)

		/* MASS */

		write.oneString("*MASS (in amu)");
		tmp = Maths.format(mass, "0.00000");
		write.oneString(tmp);
		write.oneString("*END");

		/* ROTATIONAL SYMMETRY NUMBER IF AND ONLY IF NON-ATOMIC SYSTEM */

		if (!atomic) {
			write.oneString("*NUMBER OF SYMMETRY");
			write.oneInt(symmetryNumber);
			write.oneString("*END");
		} // if end

		/* VIBRATIONAL FREQUENCIES */

		if (!atomic) {

			write.oneString("*FREQUENCIES (in cm-1)");
			String imaginaryFreq = "";

			for (int iFreq = 0; iFreq < vibFreq.length; iFreq++) {

				// testing an imaginary frequency
				if (vibFreq[iFreq].getImagPart() == 0) {
					tmp = Maths.format(vibFreq[iFreq].getRealPart() / Constants.convertCm_1ToKelvin, "0.00");
					write.oneString(tmp);
				}

				else {
					tmp = Maths.format(vibFreq[iFreq].getImagPart() / Constants.convertCm_1ToKelvin, "0.00");
					write.oneString(tmp + "i");
				}
			} // for end
			write.oneString("*END");
		} // if nAtoms>1 end

		/* ELECTRONIC DEGENERACY */

		write.oneString("*ELECTRONIC DEGENERACY");
		write.oneInt(elecDegener);
		write.oneString("*END");

		/* INERTIA MOMENTS */

		if (!atomic) {

			write.oneString("*MOMENT OF INERTIA (in Amu.bohr**2)");

			if (!linear) { // three components to be written
				for (int iInertia = 0; iInertia < 3; iInertia++) {
					tmp = Maths.format(inertia[iInertia], "0.000");
					write.oneString(tmp);
				} // for end

			} // if end
			else { // only one component to be written
				tmp = Maths.format(inertia[0], "0.000");
				write.oneString(tmp);
			} // else end
			write.oneString("*END");

			/* LINEARITY */
			write.oneString("*LINEAR");
			if (linear) {
				write.oneString("linear");
			} else {
				write.oneString("not linear");
			}
			write.oneString("*END");

		} // end of if (!atomic)

		/* POTENTIAL ENERGY */
		write.oneString("*POTENTIAL ENERGY (in hartree)");
		tmp = Maths.format(up / Constants.convertHartreeToJoule, "0.000000");
		write.oneString(tmp);
		write.oneString("*END");

	} // end of saveInputs method

}// ChemicalSystem
