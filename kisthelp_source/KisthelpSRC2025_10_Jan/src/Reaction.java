



import kisthep.file.*;
import kisthep.util.*;

import java.io.*;

abstract class Reaction implements ToEquilibrium {

/* P R O P E R T I E S*/

protected double T ; // each component of the reaction
                   // is characterized by the temperature T : a double  the unit of which is always Kelvin
                  // but, must be converted into the right number if another unit is used !
		         // in this case, the unit system of the WorkSession can be used to convert to the right number

protected double P; // each component of the reaction
                    // is characterized by the pressure : a double  the unit of which is always Pascal
                  // but, must be converted into the right number if another unit is used !
		  // in this case, the unit system of the WorkSession can be used to convert to the right number


protected double deltaUp; // the energy difference between equilibrium species
protected double deltaG0, deltaG, deltaS0, deltaS, deltaH0, deltaH, deltaZPE;




/* C O N S T R U C T O R */

public Reaction() {P = Constants.P0;} // this is the default value for pressure



/* M E T H O D S*/

 public double getDeltaS() {return deltaS;}

 public double getDeltaS0() {return deltaS0;}

 public double getDeltaH() {return deltaH;}

 public double getDeltaH0() {return deltaH0;}
 
 public double getDeltaZPE() {return deltaZPE;}

 public double getDeltaG() {return deltaG;}
 
public double getDeltaG0() {return deltaG0;}
 
 public double getTemperature () {return T;};
    
 public double getPressure () {return P;};
   

 public double getDeltaUp () {return deltaUp;}
 
 
 
 	// computes all the variation properties (deltaH, deltaS ...)
	
  public void computeDeltaProperties() {	
	
	computeDeltaUp();
	computeDeltaH();
	computeDeltaS();
	computeDeltaZPE();
	computeDeltaG();
       computeDeltaH0();
      computeDeltaS0();
      computeDeltaG0();
	
} // end of computeDeltaProperties method

 
 abstract public void computeDeltaG0();
 abstract public void computeDeltaS0();
 abstract public void computeDeltaH0();

 abstract public void computeDeltaG();
 abstract public void computeDeltaUp();
 abstract public void computeDeltaH();
 abstract public void computeDeltaS();
 abstract public void computeDeltaZPE();
 
 abstract public void setTemperature(double T) throws runTimeException,  IllegalDataException; 

abstract public void setPressure(double P) throws runTimeException,  IllegalDataException; 

 /*********************************************/
/* s a v e                                   */ 
/********************************************/  


  public void save(ActionOnFileWrite write) throws IOException {
    write.oneString("T :");
    write.oneDouble(T);


    write.oneString("P :");
    write.oneDouble(P);  

  
    write.oneString("deltaUp :");
    write.oneDouble(deltaUp);
    write.oneString("deltaG :");
    write.oneDouble(deltaG);
    write.oneString("deltaG0 :");
    write.oneDouble(deltaG0);
    write.oneString("deltaS :");
    write.oneDouble(deltaS);
    write.oneString("deltaS0 :");
    write.oneDouble(deltaS0);
    write.oneString("deltaH :");
    write.oneDouble(deltaH);
    write.oneString("deltaH0 :");
    write.oneDouble(deltaH0);
    write.oneString("deltaZPE :");
    write.oneDouble(deltaZPE);
                                            } 


/*********************************************/
/* l o a d                                   */ 
/********************************************/  

		
  public void load(ActionOnFileRead read) throws IOException, IllegalDataException {
    read.oneString();
    T = read.oneDouble();
    read.oneString();
    P = read.oneDouble();
    read.oneString();
    deltaUp = read.oneDouble();
    read.oneString();
    deltaG = read.oneDouble();
    read.oneString();
    deltaG0 = read.oneDouble();
    read.oneString();
    deltaS = read.oneDouble();
    read.oneString();
    deltaS0 = read.oneDouble();
    read.oneString();
    deltaH = read.oneDouble();
    read.oneString();
    deltaH0 = read.oneDouble();
    read.oneString();
    deltaZPE = read.oneDouble();
                                                                   }

}// Reaction
