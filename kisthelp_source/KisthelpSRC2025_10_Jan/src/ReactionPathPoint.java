


import java.io.*;

import kisthep.util.*;
import kisthep.file.*;

public class ReactionPathPoint extends ReactingStatisticalSystem implements ReadWritable {



/* P R O P E R T I E S */

private double reactionCoordinate;


/* C O N S T R U C T O R 1*/



public ReactionPathPoint (String nature, double T, ActionOnFileRead read, double reactionCoordinate) throws IllegalDataException, IOException{

	super(nature, T, read);	
	this.reactionCoordinate = reactionCoordinate;

}





/* C O N S T R U C T O R 2*/


    public ReactionPathPoint (ActionOnFileRead read) throws IOException, IllegalDataException{

    	load(read);
    }



/* M E T H O D S */


  /***********************************/
  /* returns the reaction coordinate */
  /***********************************/
 
   public double getReactionCoordinate() {return reactionCoordinate;}
    


   	/****************************************************************/       
        /* 2 methods (read, write) to save on file (or load from file) */ 
	/* the current object                                        */
    	/***********************************************************/


 /*********************************************/
/* s a v e                                   */ 
/********************************************/  


    public void save(ActionOnFileWrite write) throws IOException {
      super.save(write);
      write.oneString("reactionCoordinate");
      write.oneDouble(reactionCoordinate);                        
                                              } // end of the save method


 /*********************************************/
/* l o a d                                   */ 
/********************************************/  


					      
    public void load(ActionOnFileRead read) throws IOException, IllegalDataException {
      super.load(read);
      read.oneString();
      reactionCoordinate = read.oneDouble();
                                            } // end of the load method

  


}// ReactionPathPoint
