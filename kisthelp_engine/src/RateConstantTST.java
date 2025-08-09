

import kisthep.util.*;
import kisthep.file.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;

public class RateConstantTST extends RateConstantNVT implements ReadWritable{


    /* P R O P E R T I E S */

double valueTST; // value obtained within the TST theory, not the VTST
                 // maybe redundant with the value inherited from RateConstantNVT
                 // doesn not contain any tunneling nor variational effect

    /* C O N S T R U C T O R 1*/

    public RateConstantTST (String kineticLevel, ElementaryReaction reaction) throws IllegalDataException{
    
      super(kineticLevel, reaction);
      computeValue(new Temperature(reaction.getTemperature() ) );
  
	
    } // end of CONSTRUCTOR 1
    


/* C O N S T R U C T O R 2*/


    public RateConstantTST (ActionOnFileRead read, ElementaryReaction reaction) throws IOException, IllegalDataException{

    	this.reaction = reaction;

    	load(read);

                                                } // end of the constructor 2


/* M E T H O D S */
 

   	/******************************************/       
        /* c o m p u t e v a l u e  (Temperature) */
    	/*****************************************/

  

public void computeValue(Temperature temp) throws IllegalDataException {

  
   double T = temp.getT();
   double unitCoeff;

// R A T E   C O N S T A N T    V A L U E
// depending on the kinetic level : compute VARIATIONAL EFFECT OR NOT 


    // CLASSICAL TST
   if ( kineticLevel.equals("tst") || kineticLevel.equals("tst_w") || kineticLevel.equals("tst_eck")) {
   unitCoeff = Math.pow( ( (Constants.R*T) / Constants.P0)*(1.0e6/Constants.NA), (double) -reaction.getDeltaNu());
  
     // rate constant converted  from  USI to cm3/s (for bimolecular reaction, else to s-1 for unimolecular reaction)   
   value = (Constants.kb*T/Constants.h)* unitCoeff *Math.exp(-reaction.getDeltaG0()/(Constants.R*T) );

   // apply the statistical factor read from a text field
   value = value * reaction.getStatisticalFactor();
   valueTST = value; // only to differentiate (if required by user) from the VTST value 
   } // if end

    // VARIATIONNAL TST
   if ( kineticLevel.equals("vtst") || kineticLevel.equals("vtst_w") || kineticLevel.equals("vtst_eck")) {
   
   unitCoeff = Math.pow( ( (Constants.R*T) / Constants.P0)*(1.0e6/Constants.NA), (double) -reaction.getDeltaNu());
  
     // rate constant converted  from  USI to cm3/s (for bimolecular reaction, else to s-1 for unimolecular reaction)   
   value    = (Constants.kb*T/Constants.h)* unitCoeff *Math.exp(-reaction.getDeltaG0Max()/(Constants.R*T) );
   valueTST = (Constants.kb*T/Constants.h)* unitCoeff *Math.exp(-reaction.getDeltaG0()/(Constants.R*T) );

   // apply the statistical factor read from a text field
   value    = value    * reaction.getStatisticalFactor();
   valueTST = valueTST * reaction.getStatisticalFactor();// only to differentiate (if required by user) from the VTST value
 
   } // if end


// T U N N E L L I N G    E F F E C T
// depending on the kinetic level : compute Wigner or Eckart correction ALWAYS APPLIED TO THE BARRIER 
// defined by the "true" TS, that is, by the maximum on the PES (not the ZPE corrected energy at this Wigner level)
// and not by a generalized TS 

   if (kineticLevel.equals("tst_w") || kineticLevel.equals("vtst_w") ) {
	 // calculate the tunnel effect according to the wigner formula
	 reaction.wignerTunnel(T);
     value = value*reaction.getTunnelingFactor();
    }  // end of if (kineticLevel.equals("tst_w")) {

   if (kineticLevel.equals("tst_eck") || kineticLevel.equals("vtst_eck") ) {
	 // calculate the tunnel effect according to the Eckart methodology
	 reaction.eckartTunnel(T);
     value = value*reaction.getTunnelingFactor();
   }// if end
   
   
} // end of computeValue
   
   



/******************************************/       
/* c o m p u t e v a l u e  (Pressure) */
/*****************************************/

public void computeValue(Pressure P)  {

 // nothing to do in the TST case   
}



/******************************************/       
/* g e t V a l u e T S T*/
/*****************************************/

public double  getValueTST ()  {

return valueTST;
} // end of getvalueTST



public void fillInformationBox () {

   
// get the panel
    Box informationBox = Interface.getCalculationFeatureBox();
    Dimension boxDimension = informationBox.getSize();
    
    informationBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.7)));   
        
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
      write.oneString("kTST (without tunneling nor variational effect)");
      write.oneDouble(valueTST);
      

      // reaction is not saved here because already saved in Elementary reaction
                                              } // end of the save method


 /*********************************************/
/* l o a d                                   */ 
/********************************************/  
					      
    public void load(ActionOnFileRead read) throws IOException, IllegalDataException {
      super.load(read);
      String toto = read.oneString();
      valueTST = read.oneDouble();

                }  // end of the load method					      

    
}// RateConstantTST
