


import kisthep.file.*;
import java.io.*;

import kisthep.util.*;



public class WeightedReactingSystem extends StatisticalSystem implements ReadWritable {



/* P R O P E R T I E S */

private double stoichioNumb;


/* C O N S T R U C T O R 1*/

    public WeightedReactingSystem (String nature, double T, double stoichioNumb) throws CancelException, IllegalDataException, IOException{
	
         super(nature, T);	 
	 this.stoichioNumb = stoichioNumb;
	
    }

/* C O N S T R U C T O R 1bis*/

    public WeightedReactingSystem (String nature, double T, double P, double stoichioNumb) throws CancelException, IllegalDataException, IOException{
	
         super(nature, T, P);	 
	 this.stoichioNumb = stoichioNumb;
	
    }


/* C O N S T R U C T O R 2*/
  
    public WeightedReactingSystem (ActionOnFileRead read) throws IOException, IllegalDataException{

    	load(read);
    }



/* M E T H O D S */

   public double getStoichioNumb() {return     stoichioNumb;};
    





    	/****************************************************************/       
        /* 2 methods (read, write) to save on file (or load from file) */ 
	/* the current object                                        */
    	/***********************************************************/

 /*********************************************/
/* s a v e                                   */ 
/********************************************/  


    public void save(ActionOnFileWrite write) throws IOException {
      super.save(write);
      write.oneString("stoichioNumb :");
      write.oneDouble(stoichioNumb);      

/*********************************************/
/* l o a d                                   */ 
/********************************************/  

                                              } 
    public void load(ActionOnFileRead read) throws IOException, IllegalDataException {
      super.load(read);
      read.oneString();
      stoichioNumb = read.oneDouble();
                                            }					        
  
    
}// WeightedReactingSystem
