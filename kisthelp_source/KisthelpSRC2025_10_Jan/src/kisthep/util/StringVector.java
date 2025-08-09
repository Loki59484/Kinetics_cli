package kisthep.util;
import java.util.*;

public class StringVector extends Vector {

/* C O N S T R U C T O R */
  public StringVector() {
  
    super();
                        } // end of constructor
			
/* M E T H O D S*/

/********************/
/* c o n t a i n s */
/******************/
  public boolean contains(String string) {
  
    boolean answer = false;
    
    for (int iVector=0; iVector<= size()-1; iVector++)
       {if((String)get(iVector)==string)
           {answer = true;}
       }	    
   return answer;
                                         } // end of the contains method
					 
/****************************/
/* g e t A n d R e m o v e */
/**************************/
  public  String getFirstAndRemove() {
					 
      String result;
      
      if (size()>0) { // then get the first element and cut the vector			 
					 
      result = (String) firstElement();      
      removeElementAt(0);
                     }
      else { 
      
      result = "";
           }	     
		     
      return result;
      					 
					 
					 } // end of getAndRemove
                                         } // end of the StringVector class

