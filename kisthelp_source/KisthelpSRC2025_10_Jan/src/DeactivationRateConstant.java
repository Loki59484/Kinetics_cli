


import kisthep.file.*;
import java.io.*;
import kisthep.util.*;


public class DeactivationRateConstant  extends RateConstantNVT implements ReadWritable   {

      /**********************/
    /* P R O P E R T I E S */
    /**********************/
private double collisionEfficiency ; // !: the rateconstant of reaction depends on this collision efficiency ...

private double mu;    // mass in g/mol, converted in the constructor in kg/molec = mass of the considered system going to deactivated
                        // !: the rateconstant of reaction depends on this collision efficiency ...

private double diluentEpsilonOverK, reactantEpsilonOverK;
private double epsilonOverK;// the depth of the L-J potential well in Kelvin (E/kb)
                           // !: the rateconstant of reaction depends on this collision efficiency ...
private double diluentDiameter, reactantDiameter;
private double diameter; //the collision diameter in cm (sigma), converted in m in constructor 
                         // !: the rateconstant of reaction depends on this collision efficiency ...


   /* C O N S T R U C T O R 1*/


public DeactivationRateConstant(ElementaryReaction reaction, double mass)throws CancelException {

    
    // related reaction = global reaction described by a mechanism using deactivation process
    // mass in g/mol = mass of the considered system going to deactivated

super("collisionTheory", reaction);    
collisionEfficiency = 1.0 ; // (default value = 1 <=> Strong Collision for rrkm calculations) 



// ask for diluent gas mass in g/mol

   String question = "DILUENT GAS: Please Enter the MASS (g/mol) of diluent gas";
   String txt = KisthepDialog.requireDouble(question, "KISTHEP");
   double diluentGasMass = Double.parseDouble(txt); 

// compute the reduced mass from species and diluent gas masses
   
   this.mu = mass*diluentGasMass/(mass+diluentGasMass);

    
    
// ask for epsilon/k in K for the reacting species

   question = "<html>REACTANT: Please Enter the DEPTH of the L-J potential well in Kelvin (&epsilon;/k<sub>b</sub>)</html>";
   txt = KisthepDialog.requireDouble(question, "KISTHEP");
   reactantEpsilonOverK = Double.parseDouble(txt); 

// ask for epsilon/k in K for the diluent species

   question = "<html>DILUENT GAS: Please Enter the DEPTH of the L-J potential well in Kelvin (&epsilon;/k<sub>b</sub>)</html>";
   txt = KisthepDialog.requireDouble(question, "KISTHEP");
   diluentEpsilonOverK = Double.parseDouble(txt); 

// compute epsilonOverK   
   epsilonOverK = Math.pow(reactantEpsilonOverK*diluentEpsilonOverK,0.5);
   
// ask for collision diameter in cm
   question = "<html>REACTANT: Please Enter the collision diameter in &#8491; (&sigma;)</html>";
   txt = KisthepDialog.requireDouble(question, "KISTHEP");
   reactantDiameter = Double.parseDouble(txt); 

// ask for collision diameter in cm
   question = "<html>DILUENT GAS: Please Enter the collision diameter in &#8491; (&sigma;)</html>";
   txt = KisthepDialog.requireDouble(question, "KISTHEP");
   diluentDiameter = Double.parseDouble(txt); 

// first, convert in USI:
   this.mu = this.mu * 0.001 / Constants.NA;
   diluentDiameter  = diluentDiameter * 1E-10; // in m
   reactantDiameter = reactantDiameter * 1E-10; // in m
   
// compute diameter
   diameter = 0.5*(reactantDiameter+diluentDiameter);
   
    
   computeValue(new Temperature(reaction.getTemperature()));   
} // end of Constructor


/* C O N S T R U C T O R 2*/


    public DeactivationRateConstant (ActionOnFileRead read, ElementaryReaction reaction) throws IOException, IllegalDataException {

    	this.reaction = reaction;

    	load(read);
 
                                                } // end of the constructor 2


     /************** */
    /* g e t Z L J */
    /**************/

public double getZLJ() {

// !! mass must be in g/mol
// !! epsilonOverK must be in Kelvin 
// !! diameter must be in cm
// !! T must be in Kelvin


double T = reaction.getTemperature();
double ZLJ;
double Z;


// determine the hard sphere collision frequency
Z = Math.PI*diameter*diameter;
Z = Z * Math.pow(8.0*Constants.kb*T/(Math.PI*mu),0.5); //(Z in m2. m.s^-1)
Z = Z * (Constants.NA/(Constants.R*T)); // Z in s^-1 .Pa^-1

// determine collision integral
double omega = 1.0/(0.636+(0.567*Math.log(T/epsilonOverK)/2.302385) );


// compute ZLJ
ZLJ = Z * omega;

ZLJ = ZLJ * Constants.convertTorrToPa;


return ZLJ; // in Torr^-1. s^-1

} // end of getZLJ

  	
  	/************************************/       
      /* returns the collision efficiency*/
    	/**********************************/
    public double getCollisionEfficiency() {return collisionEfficiency;}

     	/************************************/       
      /* returns the depth of the L-J potential */
      /* well in Kelvin (E/kb)                 */
    	/**********************************/
    public double getEpsilonOverK() {return epsilonOverK;}
    public double getDiluentEpsilonOverK()  {return diluentEpsilonOverK;}
    public double getReactantEpsilonOverK() {return reactantEpsilonOverK;}

    
 	/************************************/       
      /* returns the collision diameter (in m) */
    	/**********************************/
    public double getDiameter() {return diameter;}
    public double getDiluentDiameter()  {return diluentDiameter;}
    public double getReactantDiameter() {return reactantDiameter;}

  	/************************************/       
      /* set the collision efficiency*/
    	/**********************************/
    public void setCollisionEfficiency(double collisionEfficiency) throws runTimeException, IllegalDataException 
    {
    
       this.collisionEfficiency = collisionEfficiency;
       computeValue(new Temperature(reaction.getTemperature()));
       reaction.getRateConstant().computeValue(new Temperature(reaction.getTemperature())); // refresh the global rateconstant
                            // taking into account this new value of deactivation rate constant
                           // the computeValue(T) is preferred to the computeValue(P)
                           // because it is necessary to refresh k0 that depends on
                           // Bc !!
    } // end of setCollisionEfficiency


   	/******************************************/       
        /* c o m p u t e v a l u e  (Temperature) */
    	/*****************************************/

public void computeValue(Temperature T)  {
    
   value = getZLJ(); // ZL-J Torr^-1 . s^-1
  
// apply the collision efficiency factor
   value = value * collisionEfficiency;
 
} // end of computeValue
    
 
 
    
    
   	/******************************************/       
        /* c o m p u t e v a l u e  (Pressure) */
    	/*****************************************/

public void computeValue(Pressure P)  {

 // nothing to do in the collision theory case   
}
   
    
/*********************************************/
/* s a v e                                   */ 
/********************************************/  

    public void save(ActionOnFileWrite write) throws IOException {
      
      super.save(write);  
      write.oneString("collision efficiency :");
      write.oneDouble(collisionEfficiency);
      write.oneString("reduced mass of the (reactant+diluent gas) system :");
      write.oneDouble(mu);
      write.oneString("LJ well depth in K for diluent gas:");
      write.oneDouble(diluentEpsilonOverK);
      write.oneString("LJ well depth in K for reactant:");
      write.oneDouble(reactantEpsilonOverK);
      write.oneString("LJ well depth, for mixed system, in K:");
      write.oneDouble(epsilonOverK);      
      write.oneString("diluent Diameter (sigma):");
      write.oneDouble(diluentDiameter);      
      write.oneString("reactant diameter (sigma) :");
      write.oneDouble(reactantDiameter);      
      write.oneString("diameter for mixed system(sigma) :");
      write.oneDouble(diameter);      

      
      
      // relatedReaction is not saved here because already saved in Elementary reaction
    } // end of the save method

  /*********************************************/
/* l o a d                                   */ 
/********************************************/  
					      
    public void load(ActionOnFileRead read) throws IOException, IllegalDataException {
     
      super.load(read);
        
      String toto;
   
      toto = read.oneString(); 
      collisionEfficiency = read.oneDouble();  
  
      toto = read.oneString(); 
      mu = read.oneDouble();  

      toto = read.oneString(); 
      diluentEpsilonOverK = read.oneDouble();  

      toto = read.oneString(); 
      reactantEpsilonOverK = read.oneDouble();  
 
      toto = read.oneString(); 
      epsilonOverK = read.oneDouble();  
       
      
      toto = read.oneString(); 
      diluentDiameter = read.oneDouble(); 

      toto = read.oneString(); 
      reactantDiameter = read.oneDouble(); 
      
      toto = read.oneString(); 
      diameter = read.oneDouble();      

       
    }  // end of the load method					      
    
     
}// end of class
