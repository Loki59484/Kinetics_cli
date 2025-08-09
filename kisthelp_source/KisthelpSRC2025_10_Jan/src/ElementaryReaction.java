


import java.awt.event.*;
import kisthep.file.*;
import java.io.*;
import kisthep.util.*;

import javax.swing.*;
import java.awt.*;



abstract  class ElementaryReaction extends Reaction {


/* P R O P E R T Y*/
	
protected int deltaNu; // the deltaNu number for an elementary reaction (e.g. -1 for a bimolecular reaction)
protected RateConstantNVT k; 
protected ReactionPath path; //several pathPoints about the TS (including the TS) 
protected int statisticalFactor; // reaction path degeneracy 
protected double tunnelingFactor; // possible tunneling effect on this reaction

// the delta Max properties (not necessarily relative to the TS; needed in VTST)
// if only the TS is given, then the maximum will be automatically the TS...
// DeltaX property is not the maximum of the X property but instead, the property
// calculated for the path point having the largest value along the reaction path at T
protected double deltaUpMax, deltaZPEMax, deltaG0Max, deltaGMax;
protected double deltaS0Max, deltaSMax, deltaH0Max, deltaHMax;


protected double deltaH0K_rev=0.0; // REVERSE zero-point corrected barrier height (J/mol)
// needed for Eckart tunneling calculation
// default is zero


/******************************************/       
/* C O N S T R U C T O R                  */
/*****************************************/

public ElementaryReaction () {
this.tunnelingFactor = 1; // defaultvalue=1 
this.statisticalFactor=1; //default value=1

} // end of CONSTRUCTOR
/* M E T H O D S*/


/******************************************/       
/* w i g n e r T u n n e  l ( )           */
/*****************************************/

public void wignerTunnel(double T)  {

double freqImag;
freqImag = this.getPath().getTs().getVibFreqImag().getImagPart()*(double)(Constants.convertCm_1ToM_1/Constants.convertCm_1ToKelvin);

double factor = Math.pow( (Constants.h * freqImag * (double) (Constants.c / (Constants.kb * T) )), 2.0 );

tunnelingFactor = 1.0 + (double)(1.0/24.0) * factor;

} // end of wignerTunnel



/******************************************/       
/* E c k a r t T u n n e  l ( )           */
/*****************************************/

public void eckartTunnel(double T) throws IllegalDataException  {

	// Eckart 1930 - Phys. rev. p1303 + W. Forst Unimolecular Reactions (barrier width related to imaginary freq)
	
	// get the frequency in M^-1	
	double freqImag = this.getPath().getTs().getVibFreqImag().getImagPart();	
	freqImag = this.getPath().getTs().getVibFreqImag().getImagPart()*(double)(Constants.convertCm_1ToM_1/Constants.convertCm_1ToKelvin);
	freqImag = freqImag * Constants.c; // freq in s-1
	

    // calculate A (in J/molec) (Vforward - Vreverse, whatever the reaction is endo or exothermic))
double deltaH0K_f = (this.deltaUp + this.deltaZPE)/Constants.NA; // convert to J/molec
double deltaH0K_r = this.getDeltaH0K_rev()/Constants.NA;// convert to J/molec

final double A = deltaH0K_f - deltaH0K_r; // can be negative for an exothermic reaction
final double B = Math.pow(Math.sqrt(deltaH0K_f)+Math.sqrt(deltaH0K_r), 2);
final double cRoot =  Constants.h*freqImag*Math.sqrt(Math.pow(B, 3)/Math.pow(A*A-B*B, 2));
final double C = 1.0/(2*cRoot);
final double D = C*Math.sqrt(B-cRoot*cRoot);

//define the number of active degrees of freedom to define
//the maximum energy to which the summation must be taken
 // similarly to the following book where the suggested limit is E0 + (3N-6 + 1) * kbT
 // because the function to be integrated is a sum of states divide by f(E)
 // here, we integrate P(E) exp(-E/kbT), with P(E) <=1 !! => we decide to find the best energy upperlimit differently
 // ********************
//(Robinson/Holbrook p 162): As a general guidance, a maximum of
//E+ = E0 + (3N-6 + 1) * kbT ! That corrresponds to reactant
//energy = E* = 2 * E0 + (3N-6 + 1) * kbT
 // ********************
 
 
//number of Degrees of Freedom of the all system (based on the TS)
 double nDOF = this.getPath().getTs().getVibFreedomDegrees(); 
  
//we map the last root to our selected energy upper limit
 // normally, in the endothermic case, we should consider the lower limit of E to be (V1-V2)
 // i.e. deltaH0K_f  - deltaH0K_r, since the integral will go from (V1-V2) to infinity 
 // however, it is easier to integrate from 0 to infinity using Gauss-Laguerre scheme
 // thus, E becomes E' = E - (V1-V2)
 //   0              (V1-V2)
  //  |-----------------|-----------------------------> E
  //  becomes
 //  -(V1-V2)           0
  //  |-----------------|-----------------------------> E'
 
 
 
 double threeBarrierEnergy, energyUpperLimit;
 double preFactor, shiftE;
 
 
 
 if (A>=0) { // endothermic (and athermic) reaction case	
	 threeBarrierEnergy = 3 * deltaH0K_r; 
	 preFactor = Math.exp((deltaH0K_r)/(Constants.kb*T))/(Constants.kb*T);
	 shiftE = A;
 } 
 else      { // exothermic reaction case
	 threeBarrierEnergy = 3 * deltaH0K_f;
	 preFactor = Math.exp((deltaH0K_f)/(Constants.kb*T))/(Constants.kb*T); 
	 shiftE = 0;
 }  
	 
 
 // considering that above (nDOF * Constants.kb*T) the population distribution is near zero,
 // it is unnecessary to examine energy above this threshold except when this threshold
 // is below three times the barrier (it is necessary to examine at least this energy range)
 
if( (nDOF * Constants.kb*T)  > threeBarrierEnergy ) {energyUpperLimit = nDOF * Constants.kb*T; }// to be improved by testing the boltzmann population behaviour
else                                                {energyUpperLimit = threeBarrierEnergy ; }


// loop to fill the function to be integrated : weighted Permeability
double E, alpha, beta, permeability;
//double[] weightedPE = new double[Constants.GLPolynomiaDegree]; // the function to be integrated = P(E) * exp(-E/kbT)
double[] weightedPE = new double[32]; // the function to be integrated = P(E) * exp(-E/kbT)
permeability = 0.0; // default value


// First, find the abscissa where the function vanishes (P(E) e^(-E/kbT) ); see "peakEnd" below
int nStep = (int)(energyUpperLimit/(Constants.kb*T));
double x, y;
double peakEnd = 0.0;
double xMax=-1.0;
double yMax=-1.0;

for (int i=1; i<nStep; i++) {
	
	x = i*Constants.kb*T;
	y = pE_exp(x, shiftE, C, A, D, T);
	
    if (y> yMax) {yMax = y;}
    else { peakEnd = x;   // the maximum has been reached, and now, y is decreasing 	   
    	   if (y<(yMax/Constants.nthEpsilon)) {break;}
    }
}// end of for


/**********************************************************************
 * Integrate P(E)e^-E/kBT from a to b using Simpson's rule.
 * Increase N for more precision.
 **********************************************************************/
// the Gauss-Laguerre scheme yielded bad results for some cases,
// the a Simpson integration was preferred here

double ya, yb, yx;
double a = 0.0;  // lower limit of integration
double b = peakEnd;   // upper limit of integration
// N:  precision parameter
int N = (int)(1 + (b-a)*5/(Constants.kb*T)  );// in order to have a step-size of about 1/5 of kBT
                                              // since h = (b - a) / (N - 1)
                                             // this leads to N = 1 + [ (b-a) * 5]/kBT
 double h = (b - a) / (N - 1);     // step size in J/molec


// 1/3 terms
ya = pE_exp(a, shiftE, C, A, D, T);
yb = pE_exp(b, shiftE, C, A, D, T);


double sum = (1.0 / 3.0) * (ya + yb); //1/3 (f(a)+f(b))

// 4/3 terms
for (int i = 1; i < N - 1; i += 2) {
	         x = a + h * i;
	         yx = pE_exp(x, shiftE, C, A, D, T); 	     	 
	         sum += (4.0 / 3.0) * yx; //4/3 f(x)
}


// 2/3 terms
for (int i = 2; i < N - 1; i += 2) {
	         x = a + h * i;
	         yx = pE_exp(x, shiftE, C, A, D, T);
	         sum += (2.0 / 3.0) * yx;//2/3 f(x)
}



tunnelingFactor = preFactor * sum * h;




} // end of eckartTunnel


/******************************************/       
/* P(E) e^(-E/kbT)                       */
/*****************************************/

public double  pE_exp(double x, double shiftE, double C, double A, double D, double T)  {
	
	double alpha, beta, permeability, product;

	alpha = C * Math.sqrt(x+shiftE);
	beta  = C * Math.sqrt(x+shiftE-A); // thanks to the shift in the endothermic case, (E-A) is always positive
    permeability = 1 - ((Math.cosh(2*Math.PI*(alpha-beta))+ Math.cosh(2*Math.PI*D)) / (Math.cosh(2*Math.PI*(alpha+beta))+Math.cosh(2*Math.PI*D)));

	product = permeability * Math.exp(-x/(Constants.kb*T));
	
return product;
} // end of pE_exp



/******************************************/       
/* g e t t u n n e l i n g f a c t o r */
/*****************************************/

public double  getTunnelingFactor ()  {

return tunnelingFactor;
} // end of getTunnelingFactor



/* G E T D E L T A X M A X    M E T H O D S*/
/* (the getDeltaX methods are basically defined in the parent Reaction Class)*/ 
/* DeltaX property is not the maximum of the X property but instead, the property
calculated for the path point having the largest value along the reaction path at T)*/


public double getDeltaUpMax () {return deltaUpMax;}

 public double getDeltaZPEMax() {return deltaZPEMax;}
 
 public double getDeltaSMax() {return deltaSMax;}

 public double getDeltaS0Max() {return deltaS0Max;}

 public double getDeltaHMax() {return deltaHMax;}

 public double getDeltaH0Max() {return deltaH0Max;}

 public double getDeltaGMax() {return deltaGMax;}
 
public double getDeltaG0Max() {return deltaG0Max;}
   


 	// computes all the MAX variation properties (deltaHMax, deltaSMax ...)
	
  public void computeDeltaPropertiesMax() {	
	
	computeDeltaUpMax();
	computeDeltaZPEMax();
	computeDeltaHMax();
	computeDeltaSMax();
	computeDeltaGMax();
    computeDeltaH0Max();
    computeDeltaS0Max();
    computeDeltaG0Max();
	
} // end of computeDeltaPropertiesMax method


 abstract public void computeDeltaUpMax();
 abstract public void computeDeltaZPEMax();

 abstract public void computeDeltaG0Max();
 abstract public void computeDeltaS0Max();
 abstract public void computeDeltaH0Max();

 abstract public void computeDeltaGMax();
 abstract public void computeDeltaHMax();
 abstract public void computeDeltaSMax();


 
	/***************************************************/
    /*! get the ZPE corrected reverse barrier height */
	/*************************************************/

public double getDeltaH0K_rev() {

   return deltaH0K_rev; // in j/mol					   

} // end of the getDeltaH0K_rev

   	/***********************************************************/       
        /* computes the difference in stoechiometric coefficients */
    	/*********************************************************/
    public  int  getDeltaNu() {return deltaNu;}


    	/***************************************************/
        /* returns the  rate  constant at temperature T */
    	/*************************************************/
    
    public RateConstantNVT getRateConstant() {return k;};


 /*********************************************/
/* s a v e                                   */ 
/********************************************/  

    public void save(ActionOnFileWrite write) throws IOException {
      super.save(write);

      write.oneString("tunneling Factor :");
      write.oneDouble(tunnelingFactor);
      write.oneString("deltaNu :");
      write.oneInt(deltaNu);
      write.oneString("statistical factor:");
      write.oneInt(statisticalFactor);
      write.oneString("deltaUpMax:");
      write.oneDouble(deltaUpMax);
      write.oneString("deltaZPEMax:");
      write.oneDouble(deltaZPEMax);
      write.oneString("deltaH0Max:");
      write.oneDouble(deltaH0Max);
      write.oneString("deltaHMax:");
      write.oneDouble(deltaHMax);
      write.oneString("deltaS0Max:");
      write.oneDouble(deltaS0Max);
      write.oneString("deltaSMax:");
      write.oneDouble(deltaSMax);
      write.oneString("deltaG0Max:");
      write.oneDouble(deltaG0Max);
      write.oneString("deltaGMax:");
      write.oneDouble(deltaGMax);
      write.oneString("deltaH0K_rev:");
      write.oneDouble(deltaH0K_rev);

            
      write.oneString("CLASSNAME "+path.getClass().getName());
      path.save(write);
      write.oneString("CLASSNAME "+k.getClass().getName());
      k.save(write);
                                              } // end of the save method


 /*********************************************/
/* l o a d                                   */ 
/********************************************/  

					      
    public void load(ActionOnFileRead read) throws IOException, IllegalDataException {
      super.load(read);

      
      String toto = read.oneString();
      tunnelingFactor = read.oneDouble();

      toto = read.oneString();
      deltaNu = read.oneInt();

      toto = read.oneString();
      statisticalFactor = read.oneInt();
 
      toto = read.oneString();
      deltaUpMax=read.oneDouble();
 
 
       toto = read.oneString();
      deltaZPEMax=read.oneDouble();


      toto = read.oneString();
      deltaH0Max=read.oneDouble();

      toto = read.oneString();
      deltaHMax=read.oneDouble();

      toto = read.oneString();
      deltaS0Max=read.oneDouble();

      toto = read.oneString();
      deltaSMax=read.oneDouble();

      toto = read.oneString();
      deltaG0Max=read.oneDouble();

      toto = read.oneString();
      deltaGMax=read.oneDouble();

      toto = read.oneString();
      deltaH0K_rev=read.oneDouble();
           
      toto = read.oneString();
      path = new ReactionPath(read);
     
// special case for rate constant
// actually: k can be either a 
// rrkm or tst rateconstant => to be read from the comment
      String rateConstantClassName  = read.oneString(); 
      if (rateConstantClassName.lastIndexOf("TST")!=-1 ) {
          k = new RateConstantTST(read, this);}
      
      if (rateConstantClassName.lastIndexOf("RRKM")!=-1 ) {
          k = new RateConstantRRKM(read, (UnimolecularReaction)this);}
     
      
      
      
                                            }  // end of the load method 					       


 /*********************************************/
/* g e t P a t h                              */ 
/********************************************/  

    public ReactionPath getPath() {

   return path;

}// end of getPath()

 /*************************************************/
/* g e t  S t a t i s t i c a l F a c t o r */ 
/************************************************/  


public int getStatisticalFactor() {


return statisticalFactor;



} //end of getStatisticalFactor





/******************************************************/
/*  f i l l I n f o r m a t i o n P a n e             */
/*****************************************************/

/* (this method is called each time an new calculation is carried
out, thus each time a new object is added to current sessionContent).*/


public void fillInformationBox() throws runTimeException {

		
// get the panel
Box informationBox = Interface.getCalculationFeatureBox();
Dimension boxDimension = informationBox.getSize();


// remove all previous component (if any) from this container
informationBox.removeAll();

// fill the panel (title, filename associated with calculation ...)
JLabel titleLabel = new JLabel(getTitle());
titleLabel.setForeground(Color.black);
informationBox.add(titleLabel);
informationBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.05)));   // a space between elements


  

// add data file names
JLabel dataFileLabel = new JLabel("DATA FILENAMES :");
dataFileLabel.setForeground(Color.black);
informationBox.add(dataFileLabel);
informationBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.02)));   // a space between elements

StringVector nameList = Session.getCurrentSession().getFilenameUsed();
for (int iFilename=0; iFilename<nameList.size(); iFilename++) 

{
JLabel filename = new JLabel((String) (nameList.get(iFilename)) );
informationBox.add(filename);
informationBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.01)));   // a space between elements
} // end of for (iFilename=1; ...
informationBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.04)));   // a space between elements

// add the statisticalfactor label

    JLabel labelStatisticalFactor = new JLabel("Reaction path degeneracy :");
    labelStatisticalFactor.setForeground(Color.black);
    informationBox.add(labelStatisticalFactor);
    labelStatisticalFactor.setEnabled(true);

// add the statisticalfactor textfield

    String txt = String.valueOf(statisticalFactor);
    JTextField txtStatisticalFactor = new JTextField(txt,3); // differs from the symmetry number ratio

    informationBox.add(txtStatisticalFactor);
    txtStatisticalFactor.setEnabled(true);  
    txtStatisticalFactor.addActionListener(new StatisticalFactorListener(this) );
 
    informationBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.05)));   // a space between elements



// add the kinetic level 
    JLabel kineticLevelTitle = new JLabel("KINETIC LEVEL :"); 
    kineticLevelTitle.setForeground(Color.black);
    informationBox.add(kineticLevelTitle);

     String kineticLevel = k.getKineticLevel();
     if (kineticLevel.equals("vtst") || kineticLevel.equals("vtst_w") ) {
     
        kineticLevel = kineticLevel+" / " + path.getPointNumber() + " point";
        if (path.getPointNumber()>1) {kineticLevel = kineticLevel+"s";}
     
     } // if end
     
     
    informationBox.add(new JLabel(kineticLevel));  
    if (kineticLevel.contains("eck")) {
    	String deltaH_rev = "<html> &#916;H<sup>0</sup><sub>rev</sub>(0K) = " + String.valueOf(deltaH0K_rev/1000) + " (kJ.mol<sup>-1</sup>)</html>";
    	informationBox.add(new JLabel(deltaH_rev));

    	informationBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.05)));  
    }

    k.fillInformationBox();
    
} // end of displayinformationBoxl method



/******************************************************/
/*  s e t S t a t i s t i c a l F a c t o r           */
/*****************************************************/


public void setStatisticalFactor(int statisticalFactor) throws runTimeException, IllegalDataException {

this.statisticalFactor = statisticalFactor;
getRateConstant().computeValue(new Temperature(T) ); // recompute the rate constant
// computeValue(T) is necessary  because in the case of  computeValue(P)
// in the case of TST, the rate constant is not recalculated ...
 

}


abstract public String getTitle(); 


}// ElementaryReaction
