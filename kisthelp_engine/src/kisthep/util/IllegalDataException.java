
package kisthep.util;
import java.util.*;

public class IllegalDataException extends Exception{


/* P R O P E R T Y */

  Vector errorMessageVector = new Vector(); 



/* C O N S T R U C T O R */

  public IllegalDataException(Vector errorMessageVector){
  
   this.errorMessageVector = errorMessageVector; 
                                                   }


  public IllegalDataException(){ 
                                                   }



 }
