
package kisthep.util;

import java.io.IOException;

import javax.swing.JOptionPane;

import kisthep.file.ActionOnFileRead;
import kisthep.file.ActionOnFileWrite;

public class Constants {



/* Chemical Constants */
	
    public static final double P0 = 1E05; // Pascal 
    
    public static final double waterMeltingTemperature = 273.15; // Kelvin
    
    public static final double roomTemperature = 298.0; // Kelvin
    
    // atomic masses from https://physics.nist.gov/cgi-bin/Compositions/stand_alone.pl
    private static final double[] atomicMass = new double[]{
    	1.0078, 4.0026, 7.0160, 9.0122, 11.0093, 12.0000, 14.0031, 15.9949, 18.9984, 19.9924,
    	22.9898, 23.9850, 26.9815, 27.9769, 30.9738, 31.9721, 34.9689, 39.9624,  
    38.9637, 39.9626, 44.9559, 47.9479, 50.9440, 51.9405, 54.9380, 55.9349, 58.9332, 57.9353, 62.9296, 63.9291, 68.9256, 73.9212, 74.9216, 79.9165, 78.9183, 83.9115,
    84.9118, 87.9056, 88.9058, 89.9047, 92.9064, 97.9054, 97.9072, 101.9043, 102.9055, 105.9035, 106.9051, 113.9034, 114.9039, 119.9022, 120.9038, 129.9062, 126.9045, 131.9042,
    132.9055, 137.9052, 138.9064, 139.9054, 140.9077, 141.9077, 144.9128, 151.9197, 152.9212, 157.9241, 158.9254, 163.9292, 164.9303, 165.9303, 168.9342, 173.9389, 174.9408, 179.9466, 180.9480, 183.9509, 186.9558, 
    191.9615, 192.9629, 194.9648, 196.9666, 201.9706, 204.9744, 207.9767, 208.9804, 208.9824, 209.9871, 222.0176, 223.0197, 226.0254, 227.0278, 232.0381, 231.0389, 238.0508, 237.0482, 244.0642, 243.0614, 247.0704, 247.0703, 251.0796,   
    252.0830, 257.0951, 258.0984, 259.1010, 262.1096, 267.1218, 268.1257, 271.1339, 272.1383, 270.1343, 276.1516, 281.1645, 280.1651, 285.1771, 284.1787, 289.1904, 288.1927, 293.2045, 292.2075, 294.2139
    };
    
 // Array to associate atom names to atomic numbers (atom index + 1)
   private static final String[] elementNames = {
            "H", "He", 
            "Li", "Be", "B", "C", "N", "O", "F", "Ne",
            "Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar",
            "K", "Ca", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "Ga", "Ge", "As", "Se", "Br", "Kr", 
            "Rb", "Sr", "Y", "Zr", "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb", "Te", "I", "Xe", 
            "Cs", "Ba", "La", "Ce", "Pr", "Nd", "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb", "Lu", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg", "Tl", "Pb", "Bi", "Po", "At", "Rn", 
            "Fr", "Ra", "Ac", "Th", "Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm", "Md", "No", "Lr", "Rf", "Db", "Sg", "Bh", "Hs", "Mt", "Ds", "Rg", "Cn", "Nh", "Fl", "Mc", "Lv", "Ts", "Og"
        };
      
   public static double getAtomicMass(String atomName) throws IllegalDataException {
          for (int i = 0; i < elementNames.length; i++) {
              if (elementNames[i].equals(atomName)) {
                  return atomicMass[i];
              }
          }
          String message = "Error in Class Constants, in method getAtomicMass" + Constants.newLine;            
          message = message + "searching for atomic mass for name = " + atomName + Constants.newLine;
          JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);	
          throw new IllegalDataException();	
      }
    
    
    public  static double getAtomicMass(int Z) throws IllegalDataException { 
 
    	if ( (Z>0) && (Z<=118) ) {
      	return atomicMass[Z-1];
    }
    	else {
    		String message = "Error in Class Constants, in method getAtomicMass" + Constants.newLine;            
        message = message + "searching for atomic mass for Z = " + Z + Constants.newLine;
        JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);	
        throw new IllegalDataException();	   	
    	}
    	
    }
    
/* Physical constants from http://physics.nist.gov/cuu/Constants/Table/allascii.txt */

    public static final double a0 = 0.529177211E-10; // bohr radius (m) 
 
    public static final double kb = 1.380649E-23; // J.K-1 Boltzmann Cte

    public static final double NA = 6.0221413E23; //mol-1 // Avogadro Number

    public static final double R = NA * kb; // J.K-1.mol-1  

    public static final double c = 2.99792458E8 ; // m.s-1  // light celerity
    
    public static final double h = 6.6260696E-34; // J.s // Planck cte
	
    public static final double convertCm_1ToM_1=1.0E2;   // conversion cm-1 ==> m-1 

    public static final double convertCm_1ToKelvin= 100*c*h/kb;  // conversion cm-1 ==> K

    public static final double convertHartreeToJoule_perMolec= 4.3597443e-18;  // conversion hartree => Joule/molec
   
    public static final double convertHartreeToJoule=convertHartreeToJoule_perMolec*NA ; //conversion: hartree=> JOULES/MOL
    
    public static final double convertCalToJoule=4.184; //conversion: CAL => JOULE as defined by Pure and Applied Chemistry, 51 (1979) 1.
                                                        // will determine the 627.51 conversion factor (hartree to kcal/mol) !!

    public static final double convertAmuToKg=1.6605389E-27; //conversion: amu=> kg 

    public static final double convertTorrToPa = 133.322368; // Torr ==> Pa

    public static final double convertBarToPa = P0; // Bar ==> Pa


    public static final double convertAtmToPa = 1.01325*P0; // Atm ==> Pa

    public static final double convertJTocm_1 = 1.0/(kb * convertCm_1ToKelvin); // J ==> cm-1

    public static final double convertTorr_1ToCm3Molec_1Kelvin_1 = R/(convertTorrToPa * NA * 1E-6); // Torr-1 ==> molec-1.cm3.K-1
    public static final double convertJToKCalPerMol = Constants.NA*1.0E-3/convertCalToJoule; // J/molec ==> kcal/mol 
    public static final double convertGHzToAmuBohr2 = Constants.h/ (1.0E09* 8 * Math.pow(Math.PI,2)*(Constants.convertAmuToKg*Math.pow(Constants.a0,2)));
    public static final double convertcm_1ToAmuBohr2 = Constants.h/ (8 * Math.pow(Math.PI,2)*c*100*(Constants.convertAmuToKg*Math.pow(Constants.a0,2)));
     
    
 
    
/* Kisthelp Constants */
    
    public static final int    maxAtom = 1000; // maximum number of atoms that can be treated (ORCA : compute inertia tensor from atom list)
    public static final int    thermo3DChemArrayLimit = 25000;// to limit the size of the 2D array "Z" for plot3DPanel.gridPlot ...
    public static final double nthEpsilon = 10000; // the peakEnd of the function y = P(E)e^-(E/kBT) will be detected at yMax/
    public static final int    minArraySize   = 5; // the minimum number of elements of the arrays for the thermo and Kinetics properties calculations
    public static final String kisthepMessage = "K i S T h e l P ";
    public static final double inertiaEpsilon = 0.1; // in GHz (to detect identical Rotational constants) 
    public static final double inertiaEpsilon2 = 0.001; // in A.U. (to detect identical inertia moments)
    public static final double massEpsilon = 0.001; // in g/mol (to detect reactants and TS of different mass)
    public static final double highEnergy = 1000.0 ; // in J/molec (to avoid hindered rotor treatment for ex.)
    
    //public static final String askingFileString="Please, enter the datafile name for ";
    public static final String askingLocationString="Enter location ";
    public static final String askingFileString="FILE: ";
    public static final double defaultScalingFactor = 1.0; // 
    public static final double nullValue = 0.0; //          
    public static final int    stateDensity = 0; 
    public static final int    stateSum = 1;

    public static final double tMinDefault = roomTemperature  ;
    public static final double tStepDefault = 1.0  ;
    public static final double tMaxDefault = tMinDefault; 
    public static final int    nStepDefault = 500;
  
    public static final double pStepDefault = 0.1*P0  ;
    
    public static final int GLPolynomiaDegree = 15;  // !! current version limited to 15-point Gauss-Laguerre integration

    public static final double MaxwBoltzMaxThresh = 1.0E-18;  //J/molec The maximum energy value allowed to find the maximum of the MB distribution function 

// allowed input files 
    public static final String ADFFileType          = "ADF";  //ADF output (.out file)
    public static final String molproFileType       = "molpro";  //molpro output (.out file)
    public static final String orcaFileType         = "orca";  //orca output
    public static final String g09FileType          = "gaussian09";  //gaussian 09 output
    public static final String nwcFileType          = "NWChem6.0";  //NWCHEM 6.0 output
    public static final String gms2012FileType      = "gms2012";  //Gamess output (2012 and 2013)
    public static final String kInpFileType         = "kisthepInput";  //KISTHEP input file 
    public static final String sessionFile          = "sessionFileType";  // session File Type (kisthep save file format)
    public static final String csvOutputFile        = "csvOutputFileType";  // output at excel file format

// all allowed files:
    public static final String anyAllowedDataFile = "anyAllowedData";  //any authorized input file type (kinp, gaussian, gamess, nwchem, orca)

//  unauthorized input files for kisthep application
    public static final String unknownFile    = "unknownFile";  //unAuthorized input file type 

//  any input files
    public static final String anyFile    = "anyFile";  //any file type for any application

//  size of the main panel in the Kisthep Interface
    public static final int mainPaneWidth  = 1100; // pixels
    public static final int mainPaneHeight = 1000 ; // pixels

// system constants    
    public static String newLine = System.getProperty("line.separator");
    
}// Constants
