

import kisthep.file.*;
import java.lang.*;
import java.io.*;

import javax.swing.JOptionPane;

import kisthep.util.*;

public class ReactingStatisticalSystem extends StatisticalSystem{

// this class must be used only to get KINETIC properties ...

/* C O N S T R U C T O R  1 */

    public ReactingStatisticalSystem (String nature, double T) throws CancelException, IllegalDataException, IOException {

        super(nature, T);  
        symmetryNumber = 1;
	
	 
    	
    } // end of the CONSTRUCTOR 1
    


/* C O N S T R U C T O R  2*/

    public ReactingStatisticalSystem (String nature, double T, ActionOnFileRead read) throws IllegalDataException, IOException {

    	super(nature, T, read);      	
    	symmetryNumber = 1; 

    } // end of the CONSTRUCTOR 2

 



 
/* C O N S T R U C T O R 3*/

   public ReactingStatisticalSystem (ActionOnFileRead read) throws IOException, IllegalDataException {
	super.load(read);
    
    } // end of the CONSTRUCTOR 3   

  
/* C O N S T R U C T O R 4*/
   public ReactingStatisticalSystem () {

} // end of CONSTRUCTOR 4
  

/* M E T H O D S */


// redefinir les deux methodes de calculs: de ZROT et UROT
// qui font appel normalement au nombre de symetrie qui doit forcement etre =1 ici !!    
// les deux methodes ne devront pas faire appel a cette propriete qui aura 
// ete lue !=1 dans le fichier a l appel en cascade du constructeur ChemicalSystem 
// ainsi, les methodes heritees qui feront appel a ces deux methodes redefinies ici 
//  rempliront de la bonne maniere les proprietes qui dependent de Urot et Zrot  !					         				      



/* ------------------------------------------------------------------
      P A R T I T I O N        F U N C T I O N S
---------------------------------------------------------------------*/
/*-------------------------------------------------------------------*/
/*        C O M P U T E       Z R O T                                */
/*-------------------------------------------------------------------*/

// modif fred eric (attention: ici on a carrement supprime la partie sRot)
// et ramene la partie Zrot de StatisticalSystem

public void computeZrot() {

 computeZrotWithoutSymmetryNumber();

}//end of computezRot() method 


// modif eric

/*-------------------------------------------------------------------*/
/*        g e t 1 D R o t o r s Q             (for RRKM calculations)*/
/*-------------------------------------------------------------------*/

// computes the rotational partition function for one-dimensional rotors
// i.e, the "k rotor" and, if necessary internal rotors ...

public double get1DRotorsQ() throws runTimeException {

double result;
// useful only for non atomic and non diatomic systems
// actually, KISTHEP manages RRKM rate constants only for non atomic and diatomic systems! 
// assuming one 1D rotor: the one with the smallest inertia moment here ("K rotor")

if(vibFreq.length<=1){ 
	String message = "Error in class ReactingStatisticalSystem  in method get1DRotorQ"+ Constants.newLine;
	message = message +  "This program does not allow yet for an atom or diatomic system ..."+ Constants.newLine;
	JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                  
	throw new runTimeException();
}

  // converts from Amu.bohr^2 -> Kg.m^2
  double inertia1D = get1DRotorsInertia()*Constants.convertAmuToKg* Math.pow(Constants.a0,2);    
      

  double piCube = Math.pow(Math.PI,3);
  result = Math.pow(8*piCube*inertia1D*Constants.kb*T, 0.5)/ Constants.h;



return result;
} // end of get1DRotorsQ


/*-------------------------------------------------------------------*/
/*        g e t 2 D R o t o r s Q             (for RRKM calculations)*/
/*-------------------------------------------------------------------*/

// computes the rotational partition function for a two-dimensional rotor
// coresponding to the "J rotor"

public double get2DRotorsQ() throws runTimeException {

// useful only for non atomic and non diatomic systems
// actually, KISTHEP manages RRKM rate constants only for non atomic and diatomic systems! 
// assuming one 2D rotor: the one corresponding to the averaged largest unertia moment
    // "J rotors"

double result;

if(vibFreq.length<=1){
	
	String message = "Error in class ReactingStatisticalSystem  in method get2DRotorsQ"+ Constants.newLine;
	message = message +  "This program does not allow yet for an atom or diatomic system ..."+ Constants.newLine;
	JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                  
	throw new runTimeException();	
}


 // converts from Amu.bohr^2 -> Kg.m^2
   double inertia2D = get2DRotorsInertia()*Constants.convertAmuToKg* Math.pow(Constants.a0,2);    
      

  double piSquare = Math.pow(Math.PI,2);
  double hSquare  = Math.pow(Constants.h,2);
  result = 8*piSquare*inertia2D*Constants.kb*T/hSquare;


return result;

} // end of get2DRotorsQ



/*-------------------------------------------------------------------*/
/*        g e t V i b Q                       (for RRKM calculations)*/
/*-------------------------------------------------------------------*/

// computes the vibrational partition function for rrkm calculations
// (i.e., there is not necessary 3N-6 vibrational mode to consider
// if internal rotators are incorporated in rotational partition function ...
// at present time (14/06/2013): no correction for hindreed rotors are included in RRKM calculations

public double getVibQ() throws runTimeException {

double result;
// useful only for non atomic and non diatomic systems
// actually, KISTHEP manages RRKM rate constants only for non atomic and diatomic systems! 

if(vibFreq.length<=1){ 
                      
	String message = "Error in class ReactingStatisticalSystem in method getVibQ"+ Constants.newLine;
	message = message +  "This program does not allow yet for an atom or diatomic system ..."+ Constants.newLine;
	JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                  
	throw new runTimeException();	
}

 
       double currentFrequency;
	
	result = 1 ;

	//compute
		for (int iFreq = 0; iFreq < vibFreq.length; iFreq++){//we scan all the frequencies
		
			 // We test the value of frequencie to avoid imaginary frequencies
			if (vibFreq[iFreq].getImagPart() == 0.){
		
			    currentFrequency = vibFreq[iFreq].getRealPart();
			    result = result * ( 1.0 /( 1.0 - Math.exp( - currentFrequency / T )  )   );	
	
	 		}
		}//end of for  

 return result;
} // end of getVibQQ



}// ReactingStatisticalSystem
