

import kisthep.util.*;
import kisthep.file.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;



public class RateConstantRRKM extends RateConstantNVT implements ReadWritable{


    /* P R O P E R T I E S */
private DeactivationRateConstant deactivationRateConstant; // 
private RateConstantNVE[] rateConstantNVESet;
private double kinf; // the high-pressure rate constant limit obtained analytically using partition functions 
private double kInfLimit; // the high-pressure rate constant limit obtained numerically at P-> infinity
private double k0Anal; // the low-pressure rate constant limit obtained using k2*Q2*/Q2
private double k0Limit; // the low-pressure rate constant limit obtained numerically at 1E-40 torr
private double E0;  //(J/molec!!!!) // the corrected ZPE (potential energy) barrier
                   // because this parameter can be changed to fit
		   // with unimolecular experimental results it is defined
		   // as a property of RateConstantRRKM rather
		   //than in the Reaction class.
private E0Panel e0Panel; // the panel showing the value of E0

private double gaussLaguerreFactor; // to fit the Gauss-Laguerre roots
                                             // within molecular energy scale
                                             // only used to integrate k_uni (not for k0)
                                            
    /* C O N S T R U C T O R 1*/

    public RateConstantRRKM (String kineticLevel, UnimolecularReaction reaction) throws CancelException, runTimeException, IllegalDataException {
    
    super(kineticLevel, reaction);

    double mass = ((UnimolecularReaction)reaction).getReactant().getMass();
    deactivationRateConstant = new DeactivationRateConstant(reaction, mass);
    // get the corrected ZPE energy barrier
    E0 = reaction.getDeltaUp()+ reaction.getDeltaZPE(); 
    E0 = E0/Constants.NA; // J.mol^-1 ---> J

    

    computeValue(new Temperature (reaction.getTemperature()) );

	
    } // end of CONSTRUCTOR 1
    


/* C O N S T R U C T O R 2*/


    public RateConstantRRKM (ActionOnFileRead read, UnimolecularReaction reaction) throws IOException,IllegalDataException {

    	this.reaction = reaction;

    	load(read);

                                                } // end of the constructor 2


/* M E T H O D S */
 
 	/******************************************************/       
        /* g e t R a t e C o n s t a n t N V E S e t          */
    	/****************************************************/

  

public RateConstantNVE[] getRateConstantNVESet()  {

    return rateConstantNVESet;
}                    
  	/******************************************************/       
        /* c o m p u t e R a t e C o n s t a n t N V E S e t */
    	/****************************************************/

  

public void computeRateConstantNVESet() throws runTimeException, IllegalDataException {

 // this methods returns the  values of microcanonical rateconstant 
 //  ka(E) needed for the rrkm rate constant calculation 
 //  curiously, ka(E) depends on TEMPERATURE because of the treatment of centrifugal energy correction!   
// moreover, ka(E) are computed at some E+ that change with T 
    RateConstantNVE[]  ka = new RateConstantNVE[Constants.GLPolynomiaDegree];   

   
   // double energyScaleFactor = reaction.getPath().getTs().getEnergyScaleFactor();

   
 // get  xk  of  Laguerre polynomial of n=Constants.GLPolynomiaDegree order ; these will be the energy E
 // at which NVE rate constant will be evaluated   
  double[] xk = Maths.getGaussLaguerreX(Constants.GLPolynomiaDegree);

   
  double EPlus;
 
  for (int iRoot=0; iRoot<Constants.GLPolynomiaDegree; iRoot++) 
    
  {
  // calculate ka at E+ above E0
    EPlus = xk[iRoot] * gaussLaguerreFactor; // the E+ energy of A+  , virtual unit (J/molec)
    ka[iRoot] =  new RateConstantNVE(EPlus, E0, (UnimolecularReaction) reaction); 
        
   }
   rateConstantNVESet = ka; 
 
                                                
                                                
}           // end of computeRateConstantNVESet
                                                
   	/*****************************************/       
        /* c o m p u t e v a l u e (temperature  */
    	/*****************************************/

  

public void computeValue(Temperature temp) throws runTimeException, IllegalDataException {

 //  T must be given Kelvin 
    
	
 // method needed to refresh properties that depends only on temperature or on E0 (entered by user)
   double T = temp.getT(); // temperature
   double P = reaction.getPressure(); // pressure
  
   
   
    // choose an appropriate factor to fit GaussLaguerre roots to molecular energy scale
   // it depends on TEMPERATURE !
    double[] xk = Maths.getGaussLaguerreX(Constants.GLPolynomiaDegree);

// define the number of active degrees of freedom to define
// the maximum energy to which the summation must be taken
//(Robinson/Holbrook p 162): As a general guidance, a maximum of
// E+ = E0 + (3N-6 + 1) * kbT ! That corrresponds to reactant
// energy = E* = 2 * E0 + (3N-6 + 1) * kbT
    gaussLaguerreFactor = Constants.kb*reaction.getTemperature();
    gaussLaguerreFactor = gaussLaguerreFactor * ((UnimolecularReaction)reaction).getActiveDegreesNumber();
    gaussLaguerreFactor = (E0+gaussLaguerreFactor) / xk[Constants.GLPolynomiaDegree-1]; 

   
    // refresh the values of the energies E at which the microcanonical rate constant
  // are computed  ; ka(E), curiously, depends on TEMPERATURE !
    // because of the centrifugal energy correction <deltaEj>=(1-I+/I)*kb T ! 

    computeRateConstantNVESet();
    deactivationRateConstant.computeValue(temp); 

 // kinf  depends on TEMPERATURE !  
// set a new value for kinf (obtained analytically at the RRKM level of theory, averaging 2 inertia moments)
   double Q_plus = reaction.getPath().getTs().get2DRotorsQ()* reaction.getPath().getTs().get1DRotorsQ()* reaction.getPath().getTs().getVibQ();
   double Q      = ((UnimolecularReaction)reaction).getReactant().get2DRotorsQ()* ((UnimolecularReaction)reaction).getReactant().get1DRotorsQ()* ((UnimolecularReaction)reaction).getReactant().getVibQ();
   kinf = reaction.getStatisticalFactor()*(Constants.kb*T/Constants.h)*(Q_plus/Q)*Math.exp(-E0/(Constants.kb*T));

   
// k0  depends on TEMPERATURE !   
// set a new value for k0
   // apply the Gauss-Laguerre integration scheme to get the Q2* (partition function for the activated A* species)
    double[] dQ2Star = new double[Constants.GLPolynomiaDegree];
// correction 28/04/2009: energyScaleFactor is replaced by gaussLaguerreFactor
// that depends on the number of active degrees of Freedom
//    double energyScaleFactor = ((UnimolecularReaction)reaction).getReactant().getEnergyScaleFactor();


    //  N(E*) depends on TEMPERATURE ! (for the same reason as ka(E))
    // evaluate N(E*) for each root of Laguerre polynomial
    for (int iRoot=0; iRoot<Constants.GLPolynomiaDegree; iRoot++) {
 
   // scale the energies     
// correction 28/04/2009
//       xk[iRoot] =  xk[iRoot] * energyScaleFactor;
        xk[iRoot] =  xk[iRoot] * gaussLaguerreFactor;
	
   // calculate the integrant at E*-E0
   // for only vib-rot active degrees of freedom (ony the K-rotor is considered r=1) 
   // cf relation 4-23 4-24 p78 and 79 of Unimolecular reactions by Robinson and Holbrook, Wiley Eds. 1972
       int r=1;
       dQ2Star[iRoot] = ((UnimolecularReaction)reaction).getReactant().getNEWE(xk[iRoot]+E0, Constants.stateDensity, r);
       dQ2Star[iRoot] = dQ2Star[iRoot] * Math.exp(-(xk[iRoot]+E0)/(Constants.kb*T));
 
} // end of for
   double Q2Star = Maths.getGaussLaguerreIntg(dQ2Star, Constants.GLPolynomiaDegree);

   // convert back energy to joule  
// correction 28/04/2009
//   Q2Star = Q2Star * energyScaleFactor;
     Q2Star = Q2Star * gaussLaguerreFactor;
     
   double Q2 = ((UnimolecularReaction)reaction).getReactant().get1DRotorsQ()* ((UnimolecularReaction)reaction).getReactant().getVibQ();    
   k0Anal  = deactivationRateConstant.getValue() *(Q2Star/Q2);
   

   // k0  depends on TEMPERATURE !
   // compute the numerical low-pressure limit rate constant
   computeValue(T,1E-40); // compute at 1E-40 Pascal !
   k0Limit = value/(1.0E-40/Constants.convertTorrToPa);
   

   // kinf  depends on TEMPERATURE !
    // compute the numerical high-pressure limit rate constant
   computeValue(T,1.0E40); // compute at 1E40 Pascal !

   kInfLimit = value;
       
// kuni  depends on TEMPERATURE ! of course ...   
// set a new value to the rrkm rate constant   
   computeValue(T,P); 
 
}// end of computeValue(T)



   	/*****************************************/       
        /* c o m p u t e v a l u e (pressure)    */
    	/*****************************************/

  

public void computeValue(Pressure press) throws runTimeException, IllegalDataException {

 
// attention, ici P doit etre passee en Pascal

   double T = reaction.getTemperature(); // temperature
   double P = press.getP(); // pressure
   computeValue(T,P);
  
}// end of computeValue(pressure)

// fin modif fred eric

  	/*****************************************/       
        /* c o m p u t e v a l u e               */
    	/*****************************************/

  

public void computeValue(double T, double P) throws runTimeException, IllegalDataException {

// T in Kelvin, P in Pascal!
   
   double[] delta_kuni = new double[Constants.GLPolynomiaDegree];  // integrant value for kuni at E*
   double sum ; // G(E+)
   
  
  // local variable
  double num, den;


   
  double EPlus;
  
  
  // evaluate ka(E*) for each root of Laguerre polynomial
    for (int iRoot=0; iRoot<Constants.GLPolynomiaDegree; iRoot++) {

       EPlus = rateConstantNVESet[iRoot].getEPlus(); // get scaled Gauss-Laguerre root xk[iRoot]

       
   // calculate the integrant delta_kuni at E*
        // calculate G(E+)  
         sum = rateConstantNVESet[iRoot].getGE();
       // calculate numerator
         num = sum * Math.exp(-EPlus/(Constants.kb*T));
         den = 1.0 +  rateConstantNVESet[iRoot].getValue()/(deactivationRateConstant.getValue() * (P/Constants.convertTorrToPa)) ;


         delta_kuni[iRoot] = num/den;     
 
} // end of for

  
  value = Maths.getGaussLaguerreIntg(delta_kuni, Constants.GLPolynomiaDegree);
  // convert back energy to joule  
 // value = value * reaction.getPath().getTs().getEnergyScaleFactor();

  value = value * gaussLaguerreFactor;

  
  // apply reaction path degeneracy
  value = value * reaction.getStatisticalFactor(); 

  value = value * Math.exp(-E0/ (Constants.kb*T) );
  value = value / Constants.h;

  // apply the centrifuge factor Q^+1/Q1 now:
  value = value * (reaction.getPath().getTs().get2DRotorsQ() ); // Q^+1 
  value = value / ((UnimolecularReaction)reaction).getReactant().get2DRotorsQ();//Q1
  // dividing by Q2
  value = value / ((UnimolecularReaction)reaction).getReactant().get1DRotorsQ(); // 
  value = value / ((UnimolecularReaction)reaction).getReactant().getVibQ();    

}// end of computeValue(T,P)


/******************************************************/
/*  g e t D e a c t i v a t i o n R a t e C o n s t a n t*/
/*****************************************************/

public DeactivationRateConstant getDeactivationRateConstant() {

  
    return deactivationRateConstant;
    
    
}


/********************/
/*  g e t K i n f 
/******************/

public double getKInf() {

  
    return kinf;
    
    
}// end of getKInf

/********************/
/*  g e t K i n f L i m i t
/******************/

public double getKInfLimit() {

  
    return kInfLimit;
    
    
}// end of getKInf


/********************/
/*  g e t K 0  A n a l*/
/******************/

public double getK0Anal() {

  
    return k0Anal; // in /torr/s
    
    
}// end of getK0Anal 

/********************/
/*  g e t K 0 L i m i t*/
/******************/

public double getK0Limit() {

  
    return k0Limit; // in /torr/s
    
    
}// end of getK0Limit


/********************/
/*  g e t E 0       */
/******************/

public double getE0() {

  
    return E0; // in J/molec
    
    
}// end of getE0

// modif. fred le 29/10/03
/*******************************/
/*  g e t E 0 P E S V a l u e  */
/*******************************/

public double getE0PESValue() {

    double E0PESValue = reaction.getDeltaUp()+ reaction.getDeltaZPE();
    E0PESValue = E0PESValue/Constants.NA; // J.mol^-1 ---> J


    return E0PESValue; // in J/molec


}// end of getE0PESValue

// fin modif.

/******************************/
/*  g e t E 0 P a n e l      */
/****************************/

public E0Panel getE0Panel() {

// return the panel showing the E0 value
    
    return e0Panel;
    
}// end of getE0




/********************/
/*  s e t E 0       */
/******************/

public void setE0(double E0) throws runTimeException, IllegalDataException{

// change the E0 value without changing
// the deltaUp computed from species information    
// in J/molec
// involves to recompute:
    // a) k0, kinf ...
    // b) ka(E)
    // c) kuni
// => compute(temperature) instead of compute(pressure)
    
    this.E0 = E0;
    computeValue(new Temperature(reaction.getTemperature()) );
    
}// end of getE0


/**************************/
/*  s e t E 0  To PES     */
/*************************/

public void setE0ToPESValue() throws runTimeException,IllegalDataException{

// change the E0 value to original value= deltaUp+deltaZPE 
// in J/molec
// involves to recompute:
    // a) k0, kinf ...
    // b) ka(E)
    // c) kuni
// => compute(temperature) instead of compute(pressure)
    
    E0 = reaction.getDeltaUp()+ reaction.getDeltaZPE(); 
    E0 = E0/Constants.NA; // J.mol^-1 ---> J

    computeValue(new Temperature(reaction.getTemperature()) );
    
}// end of setE0ToPESValue



/******************************************************/
/*  f i l l I n f o r m a t i o n P a n e             */
/*****************************************************/

/* (this method is called by elementaryreaction */


public void fillInformationBox () throws runTimeException{

   // get the panel
    Box informationBox = Interface.getCalculationFeatureBox();
    Dimension boxDimension = informationBox.getSize();

    
// add the panel with E0 information    
    e0Panel = new E0Panel(this);
    e0Panel.setBackground(new Color(218,223,224));
//    e0Panel.setPreferredSize(new Dimension(  boxDimension.width/2  , e0Panel.getHeight()) );
    informationBox.add(e0Panel);
 
    informationBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.05)));         
    
  // add the collisionEfficiency label

    JLabel labelCollisionEfficiency = new JLabel("Collision Efficiency Bc :");
    labelCollisionEfficiency.setForeground(Color.black);
    informationBox.add(labelCollisionEfficiency);
    labelCollisionEfficiency.setEnabled(true);

// add the collision Efficiency textfield

    String txt = String.valueOf(deactivationRateConstant.getCollisionEfficiency() );
    JTextField txtCollisionEfficiency = new JTextField(txt,3); 
    informationBox.add(txtCollisionEfficiency);
    txtCollisionEfficiency.setEnabled(true);  
    txtCollisionEfficiency.addActionListener(new CollisionEfficiencyListener(deactivationRateConstant) );

    
// add the energy distribution figure    
    
    
    informationBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.5)));   
        
}// end of fillInformationBox



    	/****************************************************************/       
      /* 2 methods (read, write) to save on file (or load from file) */ 
	/* the current object                                        */
    	/***********************************************************/    

 /*********************************************/
/* s a v e                                   */ 
/********************************************/  

    public void save(ActionOnFileWrite write) throws IOException {

        
      super.save(write);  
 
      write.oneString("analytical RRKM high-pressure limit kinf ");
      write.oneDouble(kinf);

      write.oneString("numerical RRKM high-pressure limit kinf ");
      write.oneDouble(kInfLimit);

      write.oneString("low-pressure limit k0 (k2 Q2*/Q2)");
      write.oneDouble(k0Anal);
 
      write.oneString("low-pressure limit k0 (limit p->0 kUni/P)");
      write.oneDouble(k0Limit);

      write.oneString("ZPE corrected potential energy  barrier");
      write.oneDouble(E0);
      
      write.oneString("Factor to scale the Gauss Laguerre roots");
      write.oneDouble(gaussLaguerreFactor);
      
      
       
      write.oneString("CLASSNAME "+deactivationRateConstant.getClass().getName());
      deactivationRateConstant.save(write);
     
      write.oneString("Array of "+rateConstantNVESet[0].getClass().getName());
      for (int iEner=0; iEner <Constants.GLPolynomiaDegree; iEner++) {
          
          rateConstantNVESet[iEner].save(write);
          
          
      }// end of for
        
        
        
      // reaction is not saved here because already saved in Elementary reaction
                                              } // end of the save method


 /*********************************************/
/* l o a d                                   */ 
/********************************************/  
					      
    public void load(ActionOnFileRead read) throws IOException, IllegalDataException {

      super.load(read); 
      String toto = read.oneString();
      kinf = read.oneDouble();
 
      toto = read.oneString();
      kInfLimit = read.oneDouble();

      
      toto = read.oneString();
      k0Anal = read.oneDouble();

      toto = read.oneString();
      k0Limit = read.oneDouble();
 
      toto = read.oneString();
      E0 = read.oneDouble();
      
      toto = read.oneString();
      gaussLaguerreFactor = read.oneDouble();

      toto = read.oneString();
      deactivationRateConstant = new DeactivationRateConstant(read, reaction);
     
      toto =read.oneString();
      rateConstantNVESet = new RateConstantNVE[Constants.GLPolynomiaDegree];
      for (int iEner=0; iEner <Constants.GLPolynomiaDegree; iEner++) {
          
          rateConstantNVESet[iEner] = new RateConstantNVE(read, (UnimolecularReaction)reaction);
          
          
      }// end of for
        
      }  // end of the load method					      

    
}// RateConstant
