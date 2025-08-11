


import javax.swing.*;
import javax.swing.table.TableColumn;

import kisthep.file.*;
import kisthep.util.*;

import java.io.*;
import java.awt.*;
import java.util.*;

public class Equilibrium extends Reaction implements SessionComponent, ReadWritable {




// P R O P E R T I E S


    private double kEq;
    private WeightedReactingSystem[] reactant, product;
    private int reactantNumber, productNumber;


/* C O N S T R U C T O R 1*/
    public Equilibrium (double T) throws CancelException, IllegalDataException, IOException {
	String txt;
	String question;


	this.T = T;
	
	/* size input of reactant and product arrays */
	txt = KisthepDialog.requireInteger("Please Enter the number of equilibrium reactants", "KISTHEP");	
	reactantNumber = Integer.parseInt(txt);
	reactant = new WeightedReactingSystem[reactantNumber];

	txt = KisthepDialog.requireInteger("Please Enter the number of equilibrium products", "KISTHEP");
	productNumber = Integer.parseInt(txt);
	product = new WeightedReactingSystem[productNumber];

// construction of the vector containing the file names to be read
    
 	for (int iReactant=1; iReactant<= reactantNumber; iReactant++) {
    
        Session.getCurrentSession().getFilesToBeRead().add(" reactant "+iReactant);
    
       }


 	for (int iProduct=1; iProduct<= productNumber; iProduct++) {
    
        Session.getCurrentSession().getFilesToBeRead().add(" product "+iProduct);
    
       }



	
	/* filling weighted equilibrium reactant and product arrays */
        
	for (int iReactant=0; iReactant<= reactantNumber-1; iReactant++) {
	       
	        question = "Please Enter the positive stoichiometric number of reactant ";
		question = question.concat(Integer.toString(iReactant+1));
		txt = KisthepDialog.requireDouble(question, "KISTHEP");
                reactant[iReactant]=new WeightedReactingSystem(ChemicalSystem.minimum, this.T,Double.parseDouble(txt));
	     
	                                              }; // end of for (iReactant ...)
	

	for (int iProduct=0; iProduct<= productNumber-1; iProduct++) {
	       
	        question = "Please Enter the positive stoichiometric number of product ";
		question = question.concat(Integer.toString(iProduct+1));
		txt = KisthepDialog.requireDouble(question, "KISTHEP");
                product[iProduct]=new WeightedReactingSystem(ChemicalSystem.minimum, this.T, Double.parseDouble(txt));
	     
	                                              }; // end of for (iProduct ...)
	
	
	// computes all the variation properties (deltaH, deltaS ...)
	
        computeDeltaProperties();
	
		

        // computes the equilibrium constant at temperature T (thus, deltaG0)
	computeEquilibriumConstant();
	
	
    } // End of the CONSTRUCTOR 1

 
/* C O N S T R U C T O R 1bis to take into account the pressure effect*/
    public Equilibrium (double T, double P) throws CancelException, IllegalDataException, IOException {
	String txt;
	String question;


	this.T = T;
    this.P = P;
	
	/* size input of reactant and product arrays */
	txt = KisthepDialog.requireInteger("Please Enter the number of equilibrium reactants", "KISTHEP");	
	reactantNumber = Integer.parseInt(txt);
	reactant = new WeightedReactingSystem[reactantNumber];

	txt = KisthepDialog.requireInteger("Please Enter the number of equilibrium products", "KISTHEP");
	productNumber = Integer.parseInt(txt);
	product = new WeightedReactingSystem[productNumber];

// construction of the vector containing the file names to be read
    
 	for (int iReactant=1; iReactant<= reactantNumber; iReactant++) {
    
        Session.getCurrentSession().getFilesToBeRead().add(" reactant "+iReactant);
    
       }


 	for (int iProduct=1; iProduct<= productNumber; iProduct++) {
    
        Session.getCurrentSession().getFilesToBeRead().add(" product "+iProduct);
    
       }



	
	/* filling weighted equilibrium reactant and product arrays */
        
	for (int iReactant=0; iReactant<= reactantNumber-1; iReactant++) {
	       
	        question = "Please Enter the positive stoichiometric number of reactant ";
		question = question.concat(Integer.toString(iReactant+1));
		txt = KisthepDialog.requireDouble(question, "KISTHEP");
                reactant[iReactant]=new WeightedReactingSystem(ChemicalSystem.minimum, this.T, this.P, Double.parseDouble(txt));
	     
	                                              }; // end of for (iReactant ...)
	

	for (int iProduct=0; iProduct<= productNumber-1; iProduct++) {
	       
	        question = "Please Enter the positive stoichiometric number of product ";
		question = question.concat(Integer.toString(iProduct+1));
		txt = KisthepDialog.requireDouble(question, "KISTHEP");
                product[iProduct]=new WeightedReactingSystem(ChemicalSystem.minimum, this.T, this.P, Double.parseDouble(txt));
	     
	                                              }; // end of for (iProduct ...)
	
	
	// computes all the variation properties (deltaH, deltaS ...)
	
        computeDeltaProperties();
	
		

        // computes the equilibrium constant at temperature T (thus, deltaG0)
	computeEquilibriumConstant();
	
	
    } // End of the CONSTRUCTOR 1bis

 







/* C O N S T R U C T O R 2*/

    public Equilibrium (ActionOnFileRead read) throws IOException,IllegalDataException {

    	load(read);

    } // end of the CONTRUCTOR 2


    /* method that fill the table of results*/
    public Vector getTableResults() throws runTimeException{

    	Vector tableVector = new Vector();
    	
  	 String[] columnNames1 = {
			 "Property",
             "Value"        };

    Object[][] data1 = {
    {"\u0394Up (kJ/mol)", Maths.format(deltaUp * 1e-3,"0.00")},
    {"\u0394ZPE( J/mol)", Maths.format(deltaZPE,"0.00")},
    {"\u0394H(0K) (kJ/mol)", Maths.format((deltaUp+deltaZPE) * 1e-3,"0.00")},
    {"", ""},

    {"\u0394H\u00B0 (kJ/mol)", Maths.format(deltaH0 * 1e-3,"0.00")},
    {"\u0394S\u00B0 (J/mol/K)", Maths.format(deltaS0,"0.00")},
    {"\u0394G\u00B0 (kJ/mol)", Maths.format(deltaG0 * 1e-3,"0.00")},
    {"Keq", Maths.format(kEq,"0.00E00")},
    {"", ""},
    {"\u0394H (kJ/mol)", Maths.format(deltaH * 1e-3,"0.00")},
    {"\u0394S (J/mol/K)", Maths.format(deltaS,"0.00")},
    {"\u0394G (kJ/mol)", Maths.format(deltaG * 1e-3,"0.00")},
   
    
    };
      

    JTable table1 = new JTable(data1, columnNames1);	
 
    
    table1.setPreferredScrollableViewportSize(new Dimension(500,220));

    TableColumn column = null;

    
    
    tableVector.add(table1);
   
   
    return tableVector;
 }// end of getTableResults
 

   

/* M E T H O D S */

 
 public Vector getTextResults() throws runTimeException{

	 TitledPane titledPane = new TitledPane();
	 Vector vectorResult = new Vector();
	 
	 final int titleStyle = Font.BOLD;
	 
	   // create a DisplayPanel
	   JPanel displayPanel = new JPanel();


		// a title
		JTextArea titleArea= new JTextArea();
		titleArea.setBackground(new Color(255,233,103));
	    titleArea.setEditable(false);
	    int characterSize=13;
	    
	  // get the results put in a table
	   final Vector table = getTableResults();  // table can contain one or more tables
	  	   
	   String title = "Equilibrium at "+Maths.format(T,"0.00")+" K and "+Session.getCurrentSession().getUnitSystem().convertToPressureUnit(P)+" "+Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit()+ " (scaling Factor = "+Session.getCurrentSession().getScalingFactor()+")";
	 
	   
	   // put the equilibrium EQUATION
	   String equilibriumEquation;

	   equilibriumEquation = reactant[0].getStoichioNumb() + " x R1";
	   for (int iReact=1; iReact<reactantNumber; iReact++) {
	       
	   equilibriumEquation = equilibriumEquation + " + " + reactant[iReact].getStoichioNumb() + " x R"+(iReact+1);
	   
	   
	   }// end of for reactant
	   
	   equilibriumEquation = equilibriumEquation + " \u21C6 " + product[0].getStoichioNumb() + " x P1";
	     for (int iProduct=1; iProduct<productNumber; iProduct++) {
	       
	   equilibriumEquation = equilibriumEquation + " + " + product[iProduct].getStoichioNumb() + " x P"+(iProduct+1);
	   
	   
	   }// end of for product
	   titleArea.setText(title + "\n                "+ equilibriumEquation);
	   titleArea.setFont(new Font("SansSerif", titleStyle, characterSize));
	   
	   // the layout of the BoxresultsPane
	   GridBagLayout gbl1= new GridBagLayout(); 
	   displayPanel.setLayout(gbl1) ;
	      
	   GridBagConstraints gbc = new GridBagConstraints();
	   gbc.gridx = 0;
	   gbc.gridy = 0;
	   displayPanel.add(titleArea, gbc);

	   gbc.gridx = 0;
	   gbc.gridy = 1;
	   displayPanel.add(new JScrollPane((Component)(table.get(0))), gbc);

	   // wrap displayPanel and its title into a TitledPane
	   titledPane = new TitledPane("Equilibrium properties", new JScrollPane(displayPanel));
	   
	   // add TitlePane to vectorResult
	   vectorResult.add(titledPane);

	   return vectorResult;
	   // put the results
	 
   
   
      } // end of getResults method
 


/*********************************/
/*  s a v e T x t R e s u l t s */
/*******************************/
// similar to getTextResults, except the output format is appropriate
// to put results to text file  instead  of screen                                         
 
                                             
                                             
public void saveTxtResults(ActionOnFileWrite writeResults) throws runTimeException{
   
   writeResults.oneString("Equilibrium at "+Maths.format(T,"0.00")+" K and "+Session.getCurrentSession().getUnitSystem().convertToPressureUnit(P)+" "+Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit());

   writeResults.oneString("     (scaling Factor = "+Session.getCurrentSession().getScalingFactor()+")");
  
   // put the equilibrium EQUATION
   String equilibriumEquation;
   equilibriumEquation = reactant[0].getStoichioNumb() + " x R1";
   for (int iReact=1; iReact<reactantNumber; iReact++) {
       
   equilibriumEquation = equilibriumEquation + " + " + reactant[iReact].getStoichioNumb() + " x R"+(iReact+1);
   
   
   }// end of for reactant
   
   equilibriumEquation = equilibriumEquation + " <--> " + product[0].getStoichioNumb() + " x P1";
     for (int iProduct=1; iProduct<productNumber; iProduct++) {
       
   equilibriumEquation = equilibriumEquation + " + " + product[iProduct].getStoichioNumb() + " x P"+(iProduct+1);
   
   
   }// end of for product
   writeResults.oneString(equilibriumEquation);
 
   
   // get the results put in a table
   Vector tables = getTableResults();
   String line;
   writeResults.oneString("");
   
   // prepare the table headers
   writeResults.oneString("Property, Value");
   JTable table = (JTable)(tables.get(0));
   
   for (int row = 0; row < table.getRowCount(); row++) {
	    line = "";
	    for (int col = 0; col < table.getColumnCount(); col++) {
	    	   line = line +  table.getValueAt(row, col) + ",";  // CSV format
	    }
	    writeResults.oneString(line);
	}
 
      }  // end of saveTxtResults method

/*************************************************************/
/*  s a v e G r a p h i c s  R e s u l t s for different T  */
/***********************************************************/

/*  D E P R E C A T E D

public void saveGraphicsResults(ActionOnFileWrite writeResults,TemperatureRange temperatureRange){
   

double tMin = temperatureRange.getTMin();
double tMax = temperatureRange.getTMax();
double tStep = temperatureRange.getTStep();

// local variables
int thermoChemistryArraySize = (int) ((tMax - tMin) / tStep) + 1 ;


double currentTemperature = tMin - tStep ;




    writeResults.oneString("P = "+ Session.getCurrentSession().getUnitSystem().convertToPressureUnit(P)+  " " +Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit());
    writeResults.oneString("T(K)	Keq		delta H(kJ.mol-1)	deltaS(J.mol-1.K-1)	delta G(kJ.mol-1)");
      

// fills the  free energy, enthalpy, entropy arrays

	for (int jElement = 0 ; jElement < thermoChemistryArraySize ; jElement = jElement + 1){
	
		currentTemperature = currentTemperature + tStep ;
		setTemperature(currentTemperature) ;
		
		writeResults.oneString(currentTemperature+"	" +
				   Maths.format(kEq,"0.00E00") +
				   "		" +
                                   Maths.format(deltaH * 1e-3,"0.00")+
                                   "			"      +
                                   Maths.format(deltaS,"0.00")   +
                                   "			"      +
                                   Maths.format(deltaG * 1e-3,"0.00") );

 	}// end of for (int jElement = 0 ; ...

  
       }  // end of saveGraphicsResults method


*/

/*************************************************************/
/*  s a v e G r a p h i c s  R e s u l t s for different P  */
/***********************************************************/


/*  D E P R E C A T E D

public void saveGraphicsResults(ActionOnFileWrite writeResults,PressureRange pressureRange){
   

double pMin = pressureRange.getPMin();
double pMax = pressureRange.getPMax();
double pStep = pressureRange.getPStep();

// local variables
int thermoChemistryArraySize = (int) ((pMax - pMin) / pStep) + 1 ;


double currentPressure = pMin - pStep ;



    writeResults.oneString("T = "+ T+ "K");
    writeResults.oneString("P ("+ Session.getCurrentSession().getUnitSystem().getCurrentPressureUnit()+")	Keq		delta H(kJ.mol-1)	deltaS(J.mol-1.K-1)	delta G(kJ.mol-1)");
      

// fills the  free energy, enthalpy, entropy arrays

	for (int jElement = 0 ; jElement < thermoChemistryArraySize ; jElement = jElement + 1){
	
		currentPressure = currentPressure + pStep ;
		setPressure(currentPressure) ;
		
		writeResults.oneString(Session.getCurrentSession().getUnitSystem().convertToPressureUnit(currentPressure)+"	" +
				   Maths.format(kEq,"0.00E00") +
				   "		" +
                                   Maths.format(deltaH * 1e-3,"0.00")+
                                   "			"      +
                                   Maths.format(deltaS,"0.00")   +
                                   "			"      +
                                   Maths.format(deltaG * 1e-3,"0.00") );

 	}// end of for (int jElement = 0 ; ...

  
       }  // end of saveGraphicsResults method


*/





/*******************************************************************************************************/
/*  g e t R e s u l t s G r a p h i c s */
/*******************************************************************************************************/

public Vector getGraphicsResults(TemperatureRange temperatureRange) throws IllegalDataException{

    
double tMin= temperatureRange.getTMin();
double tMax= temperatureRange.getTMax();
double tStep= temperatureRange.getTStep();
Vector graphicVector = new Vector() ;

final int x = 0 ;
final int y = 1 ;

int thermoChemistryArraySize = (int) ((tMax - tMin) / tStep) + 1 ;

double[][] deltaH0Array = new double[2][thermoChemistryArraySize];
double[][] deltaS0Array = new double[2][thermoChemistryArraySize];
double[][] deltaG0Array = new double[2][thermoChemistryArraySize];
double[][] deltaHArray = new double[2][thermoChemistryArraySize];
double[][] deltaSArray = new double[2][thermoChemistryArraySize];
double[][] deltaGArray = new double[2][thermoChemistryArraySize];
double[][] logKEqArray = new double[2][thermoChemistryArraySize];


double temperature = tMin - tStep ;
double tempInUserUnit;

// fills the arrays for, free energy, enthalpy, entropy

        for (int jElement = 0 ; jElement < thermoChemistryArraySize ; jElement = jElement + 1){

                temperature = temperature + tStep ;
                setTemperature(temperature) ;// temperature in K, all properties are refreshed

                // don't forget to convert temperature to be displayed to the User unit !
                tempInUserUnit = Session.getCurrentSession().getUnitSystem().convertToTemperatureUnit(temperature);

                deltaHArray[x][jElement] = tempInUserUnit;
                deltaHArray[y][jElement] = deltaH/1000.0 ; // in kJ/mol

                deltaSArray[x][jElement] = tempInUserUnit;
                deltaSArray[y][jElement] = deltaS  ;// in J/K/mol

                deltaGArray[x][jElement] =tempInUserUnit;
                deltaGArray[y][jElement] = deltaG/1000.0 ; // in kJ/mol

                
                deltaH0Array[x][jElement] = tempInUserUnit;
                deltaH0Array[y][jElement] = deltaH0/1000.0 ; // in kJ/mol

                deltaS0Array[x][jElement] = tempInUserUnit;
                deltaS0Array[y][jElement] = deltaS0  ;// in J/K/mol

                deltaG0Array[x][jElement] =tempInUserUnit;
                deltaG0Array[y][jElement] = deltaG0/1000.0 ; // in kJ/mol

                
		logKEqArray[x][jElement] = ( 1.0 / temperature) ; //we keep the Kelvin unit here !
		logKEqArray[y][jElement] = Math.log(kEq) / Math.log(10.0) ; //WARNING --> log !!

	
        }

        // buils all graphics
        DataToPlot dataToplot;
        String title, labelAbscissa, labelOrdinate ;
        Vector v,w;

        //  get the symbol of the current user temperature unit
        String tempSymbol= Session.getCurrentSession().getUnitSystem().getTemperatureSymbol();
 
        // build  graphics 1 
        title = "log_10 Keq = f ( 1/T ) ---- Equilibrium Reaction" ;
	    labelAbscissa =  "1 / T (K-1)";
	    labelOrdinate = "log_10 Keq " ;

	    v = new Vector();
	    v.add("Keq");
	    w = new Vector();
	    w.add(logKEqArray);
	    
	    dataToplot = new DataToPlot("Keq",title, v, labelAbscissa,labelOrdinate, w, "0.00E00", "0.00");
	    graphicVector.add(dataToplot);

// build graphics 2

        title = "\u0394H\u00B0 = f ( T ) ---- Equilibrium Reaction" ;
	    labelAbscissa =  "Temperature ("+tempSymbol+")";
        labelOrdinate = "Reaction Enthalpy  (kJ/mol)" ;

        v = new Vector();
        v.add("\u0394H\u00B0");
        w = new Vector();
        w.add(deltaH0Array);
        
        dataToplot = new DataToPlot("\u0394H\u00B0",title, v, labelAbscissa,labelOrdinate,w, "0.0", "0.00E00") ;
        graphicVector.add(dataToplot);

// build graphics 3

        title = "\u0394S\u00B0 = f ( T ) ---- Equilibrium Reaction" ;
        labelAbscissa =  "Temperature ("+tempSymbol+")";
        labelOrdinate = "Reaction Entropy (J/mol/K)" ;

        v = new Vector();
        v.add("\u0394S\u00B0");
        w = new Vector();
        w.add(deltaS0Array);
        
        dataToplot = new DataToPlot("\u0394S\u00B0",title, v, labelAbscissa,labelOrdinate,w,  "0.0", "0.0") ;
        graphicVector.add(dataToplot);


// build graphics 4

        title = "\u0394G\u00B0 = f ( T ) ---- Equilibrium Reaction" ;
        labelAbscissa =  "Temperature ("+tempSymbol+")";
        labelOrdinate = "Reaction Gibbs Free Energy (kJ/mol)";

        v = new Vector();
        v.add("\u0394G\u00B0");
        w = new Vector();
        w.add(deltaG0Array);
        
        dataToplot = new DataToPlot("\u0394G\u00B0",title, v, labelAbscissa,labelOrdinate, w,  "0.0", "0.00E00") ;
        graphicVector.add(dataToplot);
	
     // build graphics 5

        title = "\u0394H = f ( T ) ---- Equilibrium Reaction" ;
	    labelAbscissa =  "Temperature ("+tempSymbol+")";
        labelOrdinate = "Reaction Enthalpy  (kJ/mol)" ;

        v = new Vector();
        v.add("\u0394H");
        w = new Vector();
        w.add(deltaHArray);
        
        dataToplot = new DataToPlot("\u0394H",title, v, labelAbscissa,labelOrdinate, w, "0.0", "0.00E00") ;
        graphicVector.add(dataToplot);

// build graphics 6

        title = "\u0394S = f ( T ) ---- Equilibrium Reaction" ;
        labelAbscissa =  "Temperature ("+tempSymbol+")";
        labelOrdinate = "Reaction Entropy (J/mol/K)" ;

        v = new Vector();
        v.add("\u0394S");
        w = new Vector();
        w.add(deltaSArray);
        
        dataToplot = new DataToPlot("\u0394S",title, v, labelAbscissa,labelOrdinate, w,  "0.0", "0.0") ;
        graphicVector.add(dataToplot);


// build graphics 7

        title = "\u0394G = f ( T ) ---- Equilibrium Reaction" ;
        labelAbscissa =  "Temperature ("+tempSymbol+")";
        labelOrdinate = "Reaction Gibbs Free Energy (kJ/mol)";

        v = new Vector();
        v.add("\u0394G");
        w = new Vector();
        w.add(deltaGArray);
        
        dataToplot = new DataToPlot("\u0394G",title, v, labelAbscissa,labelOrdinate, w,  "0.0", "0.00E00") ;
        graphicVector.add(dataToplot);

	return graphicVector ;

} // end of getGraphicsResults

  

/*******************************************************************************************************/
/*  g e t R e s u l t s G r a p h i c s */
/*******************************************************************************************************/

public Vector getGraphicsResults(PressureRange pressureRange) throws IllegalDataException{

    
double pMin= pressureRange.getPMin();
double pMax= pressureRange.getPMax();
double pStep= pressureRange.getPStep();
Vector graphicVector = new Vector() ;

final int x = 0 ;
final int y = 1 ;

int thermoChemistryArraySize = (int) ((pMax - pMin) / pStep) + 1 ;

double[][] deltaH0Array = new double[2][thermoChemistryArraySize];
double[][] deltaS0Array = new double[2][thermoChemistryArraySize];
double[][] deltaG0Array = new double[2][thermoChemistryArraySize];
double[][] deltaHArray = new double[2][thermoChemistryArraySize];
double[][] deltaSArray = new double[2][thermoChemistryArraySize];
double[][] deltaGArray = new double[2][thermoChemistryArraySize];

double[][] logKEqArray = new double[2][thermoChemistryArraySize];


double pressure = pMin - pStep ;
double pressureInUserUnit;

// fills the arrays for, free energy, enthalpy, entropy

        for (int jElement = 0 ; jElement < thermoChemistryArraySize ; jElement = jElement + 1){

                pressure = pressure + pStep ;
                setPressure(pressure) ;// pressure in Pa, all properties are refreshed

                // don't forget to convert pressure to be displayed to the pressure User unit !
                pressureInUserUnit = Session.getCurrentSession().getUnitSystem().convertToPressureUnit(pressure);

                deltaH0Array[x][jElement] = pressureInUserUnit;
                deltaH0Array[y][jElement] = deltaH0 /1000.0 ; // in kJ/mol

                deltaS0Array[x][jElement] = pressureInUserUnit;
                deltaS0Array[y][jElement] = deltaS0 ;// in J/mol/K

                deltaG0Array[x][jElement] = pressureInUserUnit;
                deltaG0Array[y][jElement] = deltaG0 /1000.0 ; // in kJ/mol
 
                deltaHArray[x][jElement] = pressureInUserUnit;
                deltaHArray[y][jElement] = deltaH /1000.0 ; // in kJ/mol

                deltaSArray[x][jElement] = pressureInUserUnit;
                deltaSArray[y][jElement] = deltaS ;// in J/mol/K

                deltaGArray[x][jElement] = pressureInUserUnit;
                deltaGArray[y][jElement] = deltaG /1000.0 ; // in kJ/mol

		logKEqArray[x][jElement] = ( Math.log(pressure) / Math.log (10.0) ) ;
		logKEqArray[y][jElement] = Math.log(kEq) / Math.log(10.0) ; //WARNING --> log !!

	
        }
     // buils all graphics
        DataToPlot dataToplot;
        String title, labelAbscissa, labelOrdinate ;
        Vector v, w;

//  get the symbol of the current user pressure unit
        String pressSymbol= Session.getCurrentSession().getUnitSystem().getPressureSymbol();

// build graphics 1
 
        title = "log_10 Keq = f ( log_10 P ) ---- Equilibrium Reaction" ;
	    labelAbscissa =  "log_10 P";
        labelOrdinate = "log_10 Keq " ;

        v = new Vector();
        v.add("Keq");
        w = new Vector();
        w.add(logKEqArray);
        
        dataToplot = new DataToPlot("Keq", title, v, labelAbscissa,labelOrdinate, w,  "0.0", "0.0");
	    graphicVector.add(dataToplot);

// build graphics 2

        title = "\u0394H\u00B0 = f ( P ) ---- Equilibrium Reaction" ;
       	labelAbscissa =  "Pressure("+pressSymbol+")";
        labelOrdinate = "Reaction Enthalpy  (kJ/mol)" ;

        v = new Vector();
        v.add("\u0394H\u00B0");
        w = new Vector();
        w.add(deltaH0Array);
        
        dataToplot = new DataToPlot("\u0394H\u00B0", title, v, labelAbscissa,labelOrdinate,w,  "0.0", "0.00E00") ;
        graphicVector.add(dataToplot);

// build graphics 3

        title = "\u0394S\u00B0 = f ( P ) ---- Equilibrium Reaction" ;
        labelOrdinate = "Reaction Entropy (J/mol/K)" ;

        v = new Vector();
        v.add("\u0394S\u00B0");
        w = new Vector();
        w.add(deltaS0Array);
        
        dataToplot = new DataToPlot("\u0394S\u00B0",title, v, labelAbscissa,labelOrdinate,w,  "0.0", "0.0") ;
        graphicVector.add(dataToplot);


// build  graphics 4

        title = "\u0394G\u00B0 = f ( P ) ---- Equilibrium Reaction" ;
        labelAbscissa =  "Pressure("+pressSymbol+")";
        labelOrdinate = "Reaction Gibbs Free Energy (kJ/mol)";

        v = new Vector();
        v.add("\u0394G\u00B0");
        w = new Vector();
        w.add(deltaG0Array);
        
        dataToplot = new DataToPlot("\u0394G\u00B0",title, v, labelAbscissa,labelOrdinate, w,  "0.0", "0.00E00") ;
        graphicVector.add(dataToplot);
	
     // build graphics 5

        title = "\u0394H = f ( P ) ---- Equilibrium Reaction" ;
       	labelAbscissa =  "Pressure("+pressSymbol+")";
        labelOrdinate = "Reaction Enthalpy  (kJ/mol)" ;

        v = new Vector();
        v.add("\u0394H");
        w = new Vector();
        w.add(deltaHArray);
        
        dataToplot = new DataToPlot("\u0394H", title, v, labelAbscissa,labelOrdinate, w,  "0.0", "0.00E00") ;
        graphicVector.add(dataToplot);

// build graphics 6

        title = "\u0394S = f ( P ) ---- Equilibrium Reaction" ;
        labelOrdinate = "Reaction Entropy (J/mol/K)" ;

        v = new Vector();
        v.add("\u0394S");
        w = new Vector();
        w.add(deltaSArray);
        
        dataToplot = new DataToPlot("\u0394S",title, v, labelAbscissa,labelOrdinate, w,  "0.0", "0.0") ;
        graphicVector.add(dataToplot);


// build  graphics 7

        title = "\u0394G = f ( P ) ---- Equilibrium Reaction" ;
        labelAbscissa =  "Pressure("+pressSymbol+")";
        labelOrdinate = "Reaction Gibbs Free Energy (kJ/mol)";

        v = new Vector();
        v.add("\u0394G");
        w = new Vector();
        w.add(deltaGArray);
        
        dataToplot = new DataToPlot("\u0394G",title, v, labelAbscissa,labelOrdinate, w,  "0.0", "0.00E00") ;
        graphicVector.add(dataToplot);

	return graphicVector ;

} // end of getGraphicsResults
        

/*******************************************************************************************************/
/*  g e t   3 D   G r a p h i c s R e s u l t s */
/*******************************************************************************************************/


public Vector get3DGraphicsResults(TemperatureRange temperatureRange, PressureRange pressureRange) throws runTimeException, IllegalDataException{

	
	
double tMin = temperatureRange.getTMin(); // in K
double tMax= temperatureRange.getTMax(); // in K
double tStep=temperatureRange.getTStep(); // in K
double pMin = pressureRange.getPMin();
double pMax= pressureRange.getPMax();
double pStep=pressureRange.getPStep();


Vector graphicVector = new Vector() ; // the result is represented by a vector containing all the information
                                      // i.e. a set of 3D plots

int thermoChemistryTArraySize = (int) ((tMax - tMin) / tStep) + 1 ;
int thermoChemistryPArraySize = (int) ((pMax - pMin) / pStep) + 1 ;

// only accept Array with at must 25000 values (Kisthelp Plot3DPanel limitation)

if (thermoChemistryTArraySize*thermoChemistryPArraySize > Constants.thermo3DChemArrayLimit){
	
	String message = "Error: too much points on the generated surface (limit = "+Constants.thermo3DChemArrayLimit+" points)"+ Constants.newLine;
	JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                  
	throw new IllegalDataException();
}

double[] P = new double[thermoChemistryPArraySize];
double[] T = new double[thermoChemistryTArraySize];
double[] TInverse = new double[thermoChemistryTArraySize];

double[][] deltaH0Array = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
double[][] deltaS0Array = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
double[][] deltaG0Array = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
double[][] deltaHArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
double[][] deltaSArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];
double[][] deltaGArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];

double[][] logKEqArray = new double[thermoChemistryTArraySize][thermoChemistryPArraySize];


double pressure = pMin - pStep ; // in Pa
double pressureInUserUnit;

// fills the pressure array 

	for (int jElement = 0 ; jElement < thermoChemistryPArraySize ; jElement = jElement + 1){	
		pressure = pressure + pStep ;
		// don't forget to convert temperature to be displayed to the User unit ! 
		pressureInUserUnit = Session.getCurrentSession().getUnitSystem().convertToPressureUnit(pressure);
		P[jElement]=pressureInUserUnit;
	}


double temperature = tMin - tStep ; // in K
double tempInUserUnit;
// fills the temperature array

	for (int jElement = 0 ; jElement < thermoChemistryTArraySize ; jElement = jElement + 1){	
		temperature = temperature + tStep ; // in K
		TInverse[jElement]=1.0 / temperature;
		
		// don't forget to convert temperature to be displayed to the User unit ! 
		tempInUserUnit = Session.getCurrentSession().getUnitSystem().convertToTemperatureUnit(temperature);
		T[jElement]=tempInUserUnit;
		
	}



pressure = pMin - pStep ; // in Pa
//fills the 2D-array for all properties
for (int jElement = 0 ; jElement < thermoChemistryPArraySize ; jElement = jElement + 1){ // pressure loop

	pressure = pressure + pStep ; // current pressure in Pa
	setPressure(pressure) ;// pressure in Pa, all properties are refreshed

	
    temperature = tMin - tStep ; // in K
    	for (int kElement = 0 ; kElement < thermoChemistryTArraySize ; kElement = kElement + 1){ // temperature loop
    		// take care !, the internal index is the first index of the arrays here
	
		temperature = temperature + tStep ;
		setTemperature(temperature) ; // temperature in K, all properties are refreshed

        deltaH0Array[kElement][jElement] = deltaH0/1000.0 ; // in kJ/mol

        deltaS0Array[kElement][jElement] = deltaS0 ; // in J/K/mol

        deltaG0Array[kElement][jElement] = deltaG0/1000.0 ; // in kJ/mol

        deltaHArray[kElement][jElement] = deltaH/1000.0 ; // in kJ/mol

        deltaSArray[kElement][jElement] = deltaS ; // in J/K/mol

        deltaGArray[kElement][jElement] = deltaG/1000.0 ; // in kJ/mol

		logKEqArray[kElement][jElement] = Math.log(kEq) / Math.log(10.0) ; //WARNING --> log !!

 		
	}// end of temperature loop
} // end of pressure loop


// builds all graphics
	DataToPlot3D dataToplot;
	String title, labelX, labelY, labelZ ;
	String v;

	
//  get the symbol of the current user temperature unit
	String tempSymbol= Session.getCurrentSession().getUnitSystem().getTemperatureSymbol();
//  get the symbol of the current user pressure unit
	String pressSymbol= Session.getCurrentSession().getUnitSystem().getPressureSymbol();


	
	// build graphics 1
	title = "log_10 Keq = f (P, 1/T ) ---- Equilibrium Reaction(*)" ;
    
    labelX =  "P("+pressSymbol+")";
    labelY =  "1 / T (/K)";
    labelZ = "log_10 Keq " ;
    
    
   
        v="log_10 Keq (pressure independent)";    
        dataToplot = new DataToPlot3D("Keq",title, v, labelX, labelY, labelZ, P, TInverse, logKEqArray,  "0.00E00", "0.0", "0.00E00") ;
        graphicVector.add(dataToplot);
  

          
    
   	// build graphics 2
    title = "\u0394H\u00B0 = f ( P, T ) ---- Equilibrium Reaction(*)" ;

    labelX =  "P("+pressSymbol+")";
    labelY =  "Temperature ("+tempSymbol+")";
    labelZ = "Reaction Enthalpy  (kJ/mol)" ;

    
    v="\u0394H\u00B0 (pressure independent)";

    dataToplot = new DataToPlot3D("\u0394H\u00B0",title, v, labelX, labelY, labelZ, P, T, deltaH0Array,  "0.00E00", "0.0", "0.00E00") ;
    graphicVector.add(dataToplot);

    
    
    
//build  graphics 3

    title = "\u0394S\u00B0 = f ( P, T ) ---- Equilibrium Reaction(*)" ;
    labelX =  "P("+pressSymbol+")";
    labelY =  "Temperature ("+tempSymbol+")";
    labelZ = "Reaction Entropy (J/mol/K)" ;// we keep J/mol/K unit for S (calculates as it) even though it can be plotted versus temperature in celsius


    v="\u0394S\u00B0  (pressure independent)";
    dataToplot = new DataToPlot3D("\u0394S\u00B0",title, v, labelX, labelY, labelZ, P, T, deltaS0Array,  "0.00E00", "0.0", "0.00E00") ;
    graphicVector.add(dataToplot);

//build graphics 4

    title = "\u0394G\u00B0 = f ( P, T ) ---- Equilibrium Reaction(*)" ;
    labelX =  "P("+pressSymbol+")";
    labelY =  "Temperature ("+tempSymbol+")";
    labelZ = "Reaction Gibbs Free Energy (kJ/mol)";


    v="\u0394G\u00B0 (pressure independent)";

    dataToplot = new DataToPlot3D("\u0394G\u00B0",title, v, labelX, labelY, labelZ, P, T, deltaG0Array,  "0.00E00", "0.0", "0.00E00") ;  
    graphicVector.add(dataToplot);

   	// build graphics 5
    title = "\u0394H = f ( P, T ) ---- Equilibrium Reaction(*)" ;
    labelX =  "P("+pressSymbol+")";
    labelY =  "Temperature ("+tempSymbol+")";
    labelZ = "Reaction Enthalpy  (kJ/mol)" ;

    
    v="\u0394H (pressure independent)";

    dataToplot = new DataToPlot3D("\u0394H",title, v, labelX, labelY, labelZ, P, T, deltaHArray,  "0.00E00", "0.0", "0.00E00") ;
    graphicVector.add(dataToplot);

    
    
    
//build  graphics 6

    title = "\u0394S = f ( P, T ) ---- Equilibrium Reaction" ;
    labelX =  "P("+pressSymbol+")";
    labelY =  "Temperature ("+tempSymbol+")";
    labelZ = "Reaction Entropy (J/mol/K)" ;// we keep J/mol/K unit for S (calculates as it) even though it can be plotted versus temperature in celsius


    v="\u0394S";
    dataToplot = new DataToPlot3D("\u0394S",title, v, labelX, labelY, labelZ, P, T, deltaSArray,  "0.00E00", "0.0", "0.00E00") ;
    graphicVector.add(dataToplot);

//build graphics 7

    title = "\u0394G = f ( P, T ) ---- Equilibrium Reaction" ;
    labelX =  "P("+pressSymbol+")";
    labelY =  "Temperature ("+tempSymbol+")";
    labelZ = "Reaction Gibbs Free Energy (kJ/mol)";


    v="\u0394G";

    dataToplot = new DataToPlot3D("\u0394G",title, v, labelX, labelY, labelZ, P, T, deltaGArray,  "0.00E00", "0.0", "0.00E00") ;  
    graphicVector.add(dataToplot);
	

	

return graphicVector;


} // end of getResultGraphics
     

       
    	/*********************************************/      
        /* this methods changes the temperature T and */
	/* recomputes the equilibrium constant        */
    	/*********************************************/
    public void setTemperature(double T) throws IllegalDataException{
    
    this.T = T;

	/* changes temperature of  equilibrium reactants and products  */
        /*********************************************/
	for (int iReactant=0; iReactant<= reactantNumber-1; iReactant++) {
	       
                reactant[iReactant].setTemperature(T);                  }
	     
	                                              
	

	for (int iProduct=0; iProduct<= productNumber-1; iProduct++) {
	       
                product[iProduct].setTemperature(T);	     
	                                                           } 
	

	// computes all the variation properties (deltaH, deltaS ...)
          computeDeltaProperties();
	
   
        computeEquilibriumConstant();
	
    } // end of setTemperature method
    
 
      
    	/*********************************************/      
        /* this methods changes the pressure P and */
	/* recomputes the thermodynamic properties    */
    	/*********************************************/
    public void setPressure(double P) throws IllegalDataException{
    
    this.P = P;

	/* changes pressure of  equilibrium reactants and products  */
        /*********************************************/
	for (int iReactant=0; iReactant<= reactantNumber-1; iReactant++) {
	       
                reactant[iReactant].setPressure(P);                  }
	     
	                                              
	

	for (int iProduct=0; iProduct<= productNumber-1; iProduct++) {
	       
                product[iProduct].setPressure(P);	     
	                                                           } 
	

	// computes all the variation properties (deltaH, deltaS ...)
          computeDeltaProperties();
	
	
    } // end of setPressure method
    
 
    
    
    
     	/*********************************************/ 
       /* compute the equilibrium constant at temperature T */
      /* the method computes and fills the kEq property */
    /*********************************************/
    
    public void computeEquilibriumConstant() {
    
    
    
    kEq = Math.exp(-deltaG0/(Constants.R*T) );
 
    } // end of computeKEq method




         /* return the equilibrium constant at temperature T */
    	/*********************************************/
    
    public double getKEq() {return kEq;}




    	/***************************************************/
        /* computes deltaUp for general equilibrium: aA+ bB + ... <=> cC + dD + ...*/
    	/*************************************************/

    public void computeDeltaUp() {
   
      
        deltaUp=0;
   	                                              
	

	for (int iProduct=0; iProduct<= productNumber-1; iProduct++) {
	       
                deltaUp = deltaUp + product[iProduct].getUp()*product[iProduct].getStoichioNumb();	     
	                                                           }; 
								   
        for (int iReactant=0; iReactant<= reactantNumber-1; iReactant++) {
	       
                                 
                deltaUp = deltaUp - reactant[iReactant].getUp()*reactant[iReactant].getStoichioNumb();	     
	     
                                                                        };

   
   } // end of the computeDeltaUp method



    	/***************************************************/
        /* computes deltaH for general equilibrium: aA+ bB + ... <=> cC + dD + ...*/
    	/*************************************************/

    public void computeDeltaH() {
   
      
        deltaH=0;
   	                                              
	

	for (int iProduct=0; iProduct<= productNumber-1; iProduct++) {
	       
                deltaH = deltaH + product[iProduct].getHTot()*product[iProduct].getStoichioNumb();	     
	                                                           }; 
								   
        for (int iReactant=0; iReactant<= reactantNumber-1; iReactant++) {
	       
                                 
                deltaH = deltaH - reactant[iReactant].getHTot()*reactant[iReactant].getStoichioNumb();	     
	     
                                                                        };

   
   } // end of the computeDeltaH method
   

    	/***************************************************/
        /* computes deltaH for general equilibrium: aA+ bB + ... <=> cC + dD + ...*/
    	/*************************************************/

    public void computeDeltaH0() {
   
      
        deltaH0=0;
   	                                              
	

	for (int iProduct=0; iProduct<= productNumber-1; iProduct++) {
	       
                deltaH0 = deltaH0 + product[iProduct].getH0Tot()*product[iProduct].getStoichioNumb();	     
	                                                           }; 
								   
        for (int iReactant=0; iReactant<= reactantNumber-1; iReactant++) {
	       
                                 
                deltaH0 = deltaH0 - reactant[iReactant].getH0Tot()*reactant[iReactant].getStoichioNumb();	     
	     
                                                                        };

   
   } // end of the computeDeltaH method
  


    	/***************************************************/
        /* computes deltaS for general equilibrium: aA+ bB + ... <=> cC + dD + ...*/
    	/*************************************************/

    public void computeDeltaS() {
   
      
        deltaS=0;
   	                                              
	

	for (int iProduct=0; iProduct<= productNumber-1; iProduct++) {
	       
                deltaS = deltaS + product[iProduct].getSTot()*product[iProduct].getStoichioNumb();	     
	                                                           }; 
								   
        for (int iReactant=0; iReactant<= reactantNumber-1; iReactant++) {
	       
                                 
                deltaS = deltaS - reactant[iReactant].getSTot()*reactant[iReactant].getStoichioNumb();	     
	     
                                                                        };

   
   } // end of the computeDeltaS method


    	/***************************************************/
        /* computes deltaS0 for general equilibrium: aA+ bB + ... <=> cC + dD + ...*/
    	/*************************************************/

    public void computeDeltaS0() {
   
      
        deltaS0=0;
   	                                              
	

	for (int iProduct=0; iProduct<= productNumber-1; iProduct++) {
	       
                deltaS0 = deltaS0 + product[iProduct].getS0Tot()*product[iProduct].getStoichioNumb();	     
	                                                           }; 
								   
        for (int iReactant=0; iReactant<= reactantNumber-1; iReactant++) {
	       
                                 
                deltaS0 = deltaS0 - reactant[iReactant].getS0Tot()*reactant[iReactant].getStoichioNumb();	     
	     
                                                                        };

   
   } // end of the computeDeltaS0 method



    	/***************************************************/
        /* computes deltaZPE for general equilibrium: aA+ bB + ... <=> cC + dD + ...*/
    	/*************************************************/

    public void computeDeltaZPE() {
   
      
        deltaZPE=0;
   	                                              
	

	for (int iProduct=0; iProduct<= productNumber-1; iProduct++) {
	       
                deltaZPE = deltaZPE + product[iProduct].getZPE()*product[iProduct].getStoichioNumb();	     
	                                                           }; 
								   
        for (int iReactant=0; iReactant<= reactantNumber-1; iReactant++) {
	       
                                 
                deltaZPE = deltaZPE - reactant[iReactant].getZPE()*reactant[iReactant].getStoichioNumb();	     
	     
                                                                        };

   
   } // end of the computeDeltaZPE method





    	/***************************************************/
        /* computes deltaG for general equilibrium: aA+ bB + ... <=> cC + dD + ...*/
    	/*************************************************/

    public void computeDeltaG() {
   
      
        deltaG=0;
   	                                              
	

	for (int iProduct=0; iProduct<= productNumber-1; iProduct++) {
	       
                deltaG = deltaG + product[iProduct].getGTot()*product[iProduct].getStoichioNumb();	     
	                                                           }; 
								   
        for (int iReactant=0; iReactant<= reactantNumber-1; iReactant++) {
	       
                                 
                deltaG = deltaG - reactant[iReactant].getGTot()*reactant[iReactant].getStoichioNumb();	     
	     
                                                                        };

   
   } // end of the computeDeltaG method



    	/***************************************************/
        /* computes deltaG0 for general equilibrium: aA+ bB + ... <=> cC + dD + ...*/
    	/*************************************************/

    public void computeDeltaG0() {
   
      
        deltaG0=0;
   	                                              
	

	for (int iProduct=0; iProduct<= productNumber-1; iProduct++) {
	       
                deltaG0 = deltaG0 + product[iProduct].getG0Tot()*product[iProduct].getStoichioNumb();	     
	                                                           }; 
								   
        for (int iReactant=0; iReactant<= reactantNumber-1; iReactant++) {
	       
                                 
                deltaG0 = deltaG0 - reactant[iReactant].getG0Tot()*reactant[iReactant].getStoichioNumb();	     
	     
                                                                        };

   
   } // end of the computeDeltaG0 method



    	/****************************************************************/       
        /* 2 methods (read, write) to save on file (or load from file) */ 
	/* the current object                                        */
    	/***********************************************************/


 /*********************************************/
/* s a v e                                   */ 
/********************************************/  


     public void save(ActionOnFileWrite write) throws IOException {
      super.save(write);
      write.oneString("kEq :");
      write.oneDouble(kEq);
      write.oneString("reactantNumber :");
      write.oneInt(reactantNumber);
      write.oneString("productNumber :");
      write.oneInt(productNumber);
      write.oneString("lenght of reactant array :");
      write.oneInt(reactant.length);
      write.oneString("CLASSNAME "+reactant.getClass().getName());
      write.oneString("reactants :");
      for (int iComponent=0; iComponent<reactant.length; iComponent++) {        
	reactant[iComponent].save(write);
                                                                       }
      write.oneString("length of product array :");
      write.oneInt(product.length);
      write.oneString("CLASSNAME "+product.getClass().getName());
      write.oneString("products :");							       
      for (int jComponent=0; jComponent<product.length; jComponent++) {
	product[jComponent].save(write);
	                      	                                      }
                                                                 } // end of the save method


/*********************************************/
/* l o a d                                   */ 
/********************************************/  

					      
    public void load(ActionOnFileRead read) throws IOException, IllegalDataException {
      super.load(read);
      read.oneString();
      kEq = read.oneDouble();
      read.oneString();
      reactantNumber = read.oneInt();
      read.oneString();
      productNumber = read.oneInt();
      read.oneString();
      reactant = new WeightedReactingSystem[read.oneInt()];
      read.oneString();
      read.oneString();
      for (int iComponent=0; iComponent<reactant.length; iComponent++) {
        reactant[iComponent] = new WeightedReactingSystem(read); 
                                                                       }								       
      read.oneString();								       
      product = new WeightedReactingSystem[read.oneInt()];
      read.oneString();
      read.oneString();
      for (int jComponent=0;jComponent<product.length;jComponent++) {
        product[jComponent] = new WeightedReactingSystem(read); 
                                                                    }							       
                                            }  // end of the load method					      


/******************************************************/
/*  f i l l I n f o r m a t i o n P a n e l     */
/*****************************************************/

/* (this method is called each time an new calculation is carried
out, thus each time a new object is added to current sessionContent).*/


public void fillInformationBox() {


// create the panel
Box informationBox = Interface.getCalculationFeatureBox();
Dimension boxDimension = informationBox.getSize();

//remove all previous component (if any) from this container
informationBox.removeAll();


// fill the panel (title, filename associated with calculation ...)
JLabel titleLabel = new JLabel(getTitle());
titleLabel.setForeground(Color.black);
informationBox.add(titleLabel);
informationBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.05)));   



JLabel dataFileLabel = new JLabel("DATA FILENAMES :");
dataFileLabel.setForeground(Color.black);
informationBox.add(dataFileLabel);

informationBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.05)));   

StringVector nameList = Session.getCurrentSession().getFilenameUsed();
for (int iFilename=0; iFilename<nameList.size(); iFilename++) 

{
JLabel filename = new JLabel((String) (nameList.get(iFilename))  );
informationBox.add(filename);
informationBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.01)));   

} // end of for (iFilename=1; ...
informationBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.7)));   


informationBox.setBackground(Color.yellow);


} // end of displayinformationBoxl method


 /*********************************************/
/* g e t T i t l e                           */ 
/********************************************/  

public String getTitle() {return "CHEMICAL EQUILIBRIUM";}



}// Equilibrium
