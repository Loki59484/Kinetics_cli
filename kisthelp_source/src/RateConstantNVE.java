 

import kisthep.util.*;
import kisthep.file.*;
import java.io.*;

import javax.swing.JOptionPane;
public class RateConstantNVE implements ReadWritable{


    /* P R O P E R T I E S */

private double value;  // the value of the rate constant
private double E ; // the energy E* (in USI, i.e in J/molec) at which the NVE rate constant ka(E) is calculated
                   // E must be > E0 (the corrected ZPE energy barrier for the considered reaction)
                  // E* = E0 + EPlus + centrifugal energy correction (deltaEj)
private double EPlus; // the energy E+ above E0
private UnimolecularReaction reaction;  // for the specified reaction (characterized by its Temperature)

private double gE ; // G(EPlus) sum of states of transition state
private double nE ; // N(E*) density of states of energized reactant

private double E0; // the corrected ZPE potential energy barrier is the minimum energy 
                   // to compute ka(E)

    /* C O N S T R U C T O R 1*/

    public RateConstantNVE (double EPlus, double E0, UnimolecularReaction reaction) throws runTimeException{
    
 

// EPlus = E+, the energy of TS above E0; in energy unity!
//(already scaled by gaussLaguerreFactor!)

// E0:  the corrected ZPE potential energy barrier the reactant of energy E*  must go through ...
 //(E* generally = E0+EPlus , but may also depend on centrifugal correction deltaEj)   
    
// compute E*, the energy the reactant has initially before going through the barrier  
 //  deltaEj: the difference (Ej+) - (Ej) between the energies of the 
  //  adiabatic rotations in A* and A+ for an unimolecular reaction only
  // DEPENDS ON TEMPERATURE !!
        
     double E = E0 + EPlus +  reaction.getDeltaEj(); 


 if (EPlus <=0) {
 
	 String message = "Error occured in class RateConstantNVE, in constructor 1"+ Constants.newLine;
	 message = message +  "the energy E+ is negative"+ Constants.newLine;
	 message = message +  "pointing out a negative (scaled) Gauss-Laguerre root"+ Constants.newLine;
	 message = message +  "in RRKM calculation"+ Constants.newLine;
	 message = message +  "Please contact the authors"+ Constants.newLine;
	 JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                  
	 throw new runTimeException();

 
 }

// correction: 07/01/2009, on supprime ce test!
// en effet, si E* est < E0, et que l'on est en train
// d'envisager une  trajectoire reactive, c'est justement
// parce que la force centrifuge deverse suffisamment d'energie
// ds les modes rovib pour passer la barriere!!
// on est dans le cas EJ > EJ+, donc I+ > I, le cas classique
// qd une liaison s'allonge a l'etat de transition,
// et n'est pas compensee par une contraction de liaisons
// du reste de la molecule

/*    if (E >= E0 ) { */
/*  E (en fait E* ici), est definie par rapport
à = E0 + E+  + Ej+ - EJ => donc forcement
on ne peut pas se retrouver a faire des calculs
pour un E*+EJ qui serait < à E0 ! car par definition
E*+EJ = E0+E + + EJ+ 
ce qui n'empeche pas que l'on puisse
faire des calculs pour un E* < E0
*/       

    this.E =E;
    this.E0  = E0 ; // in J/molec
    this.EPlus = EPlus;

    this.reaction = reaction;

   
    computeValue();
/*	 } // end of if (E> E0)

/   else {System.out.println("Error occured in class RateConstantNVE, in constructor 1");
          System.out.println("the energy E* is smaller than E0 for the examined reaction");
          System.out.println("in RRKM calculation");
          System.out.println("Please contact the authors");
          System.exit(-1);
       }  // end of else 
*/
    } // end of constructor 1

    
       /* C O N S T R U C T O R 2 */
     public RateConstantNVE (ActionOnFileRead read, UnimolecularReaction reaction) throws IOException, IllegalDataException{

    	 this.reaction = reaction;

    	 load(read);

                                                } // end of the constructor 2

  
    
/* M E T H O D S */
    
   	/*************************************************/       
        /* computes the micro-canonical rate constant */
    	/***********************************************/
    public void computeValue() throws runTimeException {


// computes the sum of states for the transition state
// at E+ = E*-E0-deltaEj (E0 = the ZPE corrected energy barrier )
// E* being the examined energy 
// deltaEj being  the (averaged) difference (Ej+) - (Ej) between the energies of the 
//  adiabatic rotations in A* and A+ at temperature T for an unimolecular reaction
 
  
  // calculate W(E+) for vib-rot active degrees of freedom (only the K-rotor is considered r=1)
  int r=1;
  gE = reaction.getPath().getTs().getNEWE(EPlus,Constants.stateSum, r);

  // calculate N(E*)
  nE = reaction.getReactant().getNEWE(E,Constants.stateDensity, r);

   value = gE/(Constants.h*nE);
   value = value * reaction.getStatisticalFactor();


} // end of computevalue
   
   
   
   	/************************************/       
        /* returns the rate constant value */
    	/**********************************/
    public double getValue() {return value;}


   
   	/************************************/       
        /* returns the E value             */
    	/**********************************/
    public double getE() {return E;}
 
   	/************************************/       
        /* returns the EPlus value             */
    	/**********************************/
    public double getEPlus() {return EPlus;}

    
       	/************************************/       
        /* returns the gE value             */
    	/**********************************/
    public double getGE() {return gE;}

       	/************************************/       
        /* returns the nE value             */
    	/**********************************/
    public double getNE() {return nE;}


    	/****************************************************************/       
        /* 2 methods (read, write) to save on file (or load from file) */ 
	/* the current object                                        */
    	/***********************************************************/    

 /*********************************************/
/* s a v e                                   */ 
/********************************************/  

    public void save(ActionOnFileWrite write) throws IOException {
      write.oneString("value ka(E):");
      write.oneDouble(value);
    
      write.oneString("energy E :");
      write.oneDouble(E);

      write.oneString("critical energy E0:");
      write.oneDouble(E0);
       
          
      write.oneString("energy EPlus :");
      write.oneDouble(EPlus);
          
      write.oneString("sum of states g(E+) :");
      write.oneDouble(gE);
    
      write.oneString("density of states N(E*) :");
      write.oneDouble(nE);
    
 
                                                   } // end of the save method


 /*********************************************/
/* l o a d                                   */ 
/********************************************/  
					      
    public void load(ActionOnFileRead read) throws IOException, IllegalDataException {
      String toto = read.oneString();
      value = read.oneDouble();
      
      toto = read.oneString(); 
      E = read.oneDouble();

      toto = read.oneString(); 
      E0 = read.oneDouble();
       
      
      toto = read.oneString(); 
      EPlus = read.oneDouble();
       
      toto = read.oneString(); 
      gE = read.oneDouble();
      
      toto = read.oneString(); 
      nE = read.oneDouble();
      
    
      
                                               }  // end of the load method					      

    
}// RateConstantNVE
