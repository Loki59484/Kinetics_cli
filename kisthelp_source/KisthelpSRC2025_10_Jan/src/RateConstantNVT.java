
import kisthep.util.*;
import kisthep.file.*;

import java.awt.Dimension;
import java.io.*;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.stat.regression.SimpleRegression;


public abstract class RateConstantNVT implements ReadWritable{


    /* P R O P E R T I E S */

protected double value;  // the value of the rate constant
protected String kineticLevel; // computed at a specific level of theory
protected ElementaryReaction reaction;  // for the specified reaction (characterized by its Temperature)


public RateConstantNVT() {} // default constructor

    /* C O N S T R U C T O R 1*/

public RateConstantNVT (String kineticLevel, ElementaryReaction reaction) {
    
    this.kineticLevel = kineticLevel;
    this.reaction = reaction;

    	
    } // end of CONSTRUCTOR 1
    



/* M E T H O D S */
 

   	/*******************************/       
        /* c o m p u t e v a l u e   */
    	/*****************************/

  

public abstract void computeValue(Temperature T) throws runTimeException, IllegalDataException;

 

public abstract void computeValue(Pressure P) throws runTimeException, IllegalDataException;
 
 	/************************************/       
        /* fillinformationbox            */    //to complete the Elementary reaction information Box
    	/**********************************/
    public void fillInformationBox() throws runTimeException{}




   	/************************************/       
        /* returns the rate constant value */
    	/**********************************/
    public double getValue() {return value;}


   
   	/************************************/       
        /* returns the kinetic level       */
    	/**********************************/
    public String getKineticLevel() {return kineticLevel;}




	/******************************/
	/*       Arrhenius fit       */
	/***************************/
    public double[] getArrheniusFit(double temp1, double temp2)  throws runTimeException,  IllegalDataException {

// temp is the temperature around which the fit is performed
    	
double[] result = new double[5];
// two-parameters fit:
// [0] => A (/s-1 for unimolecular reactions, and cm3/molec/s for bimol.)
// [1] => Ea (/J/mol)
//three-parameters fit:
//[2] => A 
//[3] => n 
//[4] => Ea (/J/mol)
    	
//two-parameters Arrhenius FIT around the temperature
double upperLimit=  temp2;
double lowerLimit=  temp1;
int N=100; 
double step = (upperLimit-lowerLimit)/N;     // step size in K
double currentT=lowerLimit;

double[][] data  = new double[N][2];
double[]   dataPointsX = new double[N];
final double[] weights = new double[N];
final double[] observations = new double[N];


//compute k(T) for 0.8T < T < 1.2T	    
for (int iT=0; iT <100; iT++ ) {	    
	currentT = lowerLimit + iT * step;
	reaction.setTemperature(currentT) ; // temperature in K, all properties are refreshed
	data[iT][0]     =  1/currentT; // K-1
	data[iT][1]     =  Math.log(this.getValue());
	dataPointsX[iT] =  currentT; // K
	observations[iT] = data[iT][1]; 
	weights[iT] = 1.0; 
}

SimpleRegression regression = new SimpleRegression(); //  k = A exp(-Ea/RT)
regression.addData(data); // y = a x + b with a = -Ea/R, b = ln A 
	

double A = Math.exp(regression.getIntercept());
double Ea = -8.31 * regression.getSlope(); // in J/mol

	//System.out.println(" StdErr Slope = " + regression.getSlopeStdErr());
	//displays slope standard error

	//System.out.println(" Std Err Intercept : " + regression.getInterceptStdErr() );
	//will return Double.NaN, since we constrained the parameter to zero

	
result[0] = A;
result[1] = Ea;


//three-parameters Arrhenius FIT around the temperature : k = A T^n exp(-Ea/RT)
//weight Matrix

//guess
double[] startPoint = { Math.log(A),   0.5,   -Ea/(Constants.R*((temp1+temp2)/2.0))}; // y = a + b ln x +c/x, with a = ln A, b = n, c = -Ea/R

//build an optimizer
final LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

//build the function embedding dataX
arrhenius3Params f = new arrhenius3Params(dataPointsX);

//build the associated jacobian function embedding the same dataX
arrhenius3ParamsJacobian jF = new arrhenius3ParamsJacobian(dataPointsX);

//solve the problem associated with function myFunction1 and its Jacobian myJacobianFunction1
//number of parameters is known through length of array startPoint
//an initial guess is provided for the parameters
try {

final PointVectorValuePair optimum = optimizer.optimize( new MaxEval(100),
	                                                     new InitialGuess(startPoint),	                                                    
	                                                     new Target(observations),
	                                                     new Weight(weights),
	                                                     new ModelFunction(f),
	                                                     new ModelFunctionJacobian(jF));


//get the parameters
final double[] solution = optimum.getPoint();
result[2] = Math.exp(solution[0]);
result[3] = solution[1];
result[4] =  -8.31 * solution[2];

return result;	 

}
catch (TooManyEvaluationsException error) {
	
    String message = "Warning: in Class RateConstantNVT, in method getArrheniusFit(T1,T2)" + Constants.newLine;
    message = message + "TooManyEvaluationsException error " + Constants.newLine;
    message = message + "problem encountered while performing 3-parameters Arrhenius fit" + Constants.newLine;
    message = message + "during the optimization procedure." + Constants.newLine;
    message = message + "Arrhenius 3-parameters are set to 1.0, 0.0 and 0.0 (k=1.0)" + Constants.newLine;
    JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);	
    
    // in order for the global calculation to be continued, 0 values are given for the returned results
    result[2] = 1.0;// in order to get k = 1 (since ln 1 = 0 ...)
    result[3] = 0.0;
    result[4] = 0.0;
    
    return result; }



catch (DimensionMismatchException error) {
    String message = "Warning in Class RateConstantNVT, in method getArrheniusFit(T1,T2)" + Constants.newLine; 
    message = message + "DimensionMismatchException error " + Constants.newLine;
    message = message + "problem encountered while performing 3-parameters Arrhenius fit" + Constants.newLine;
    message = message + "during the optimization procedure." + Constants.newLine;
    message = message + "Arrhenius 3-parameters are set to 1.0, 0.0 and 0.0 (k=1.0)" + Constants.newLine;
    JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage, JOptionPane.WARNING_MESSAGE);	
    // in order for the global calculation to be continued, 0 values are given for the returned results
    result[2] = 1.0; // in order to get k = 1(since ln 1 = 0 ...)
    result[3] = 0.0;
    result[4] = 0.0;

    return result;}

    }// end of method getArrheniusFit();

    	/****************************************************************/       
      /* 2 methods (read, write) to save on file (or load from file) */ 
	/* the current object                                        */
    	/***********************************************************/    

 /*********************************************/
/* s a v e                                   */ 
/********************************************/  

    public void save(ActionOnFileWrite write) throws IOException {
      write.oneString("value :");
      write.oneDouble(value);
      write.oneString("kineticLevel :");
      write.oneString(kineticLevel);

      // reaction is not saved here because already saved in Elementary reaction
                                              } // end of the save method


 /*********************************************/
/* l o a d                                   */ 
/********************************************/  
					      
    public void load(ActionOnFileRead read) throws IOException, IllegalDataException {
      String toto = read.oneString();
      value = read.oneDouble();

      toto = read.oneString(); 
      kineticLevel = read.oneString();  
      }  // end of the load method					      

    
}// RateConstantNVT
