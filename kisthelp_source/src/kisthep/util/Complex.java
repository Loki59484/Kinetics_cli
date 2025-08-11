

package kisthep.util;
import java.io.*;

import javax.swing.JOptionPane;

public class Complex implements Cloneable{

    private double realPart;
    private double imagPart;
 

// constructor building  a complex number from to double numbers
    public Complex (double realPart, double imagPart){
	
	    this.realPart = realPart;
        this.imagPart = imagPart;

    }

// constructor building  a complex number = another complex x double number
    
     public Complex (Complex complex, double factor){
	
	    this.realPart = complex.getRealPart()* factor;
        this.imagPart = complex.getImagPart()* factor;

    }
    
 // constructor building  a complex number = another complex 
    
     public Complex (Complex complex){
	
	    this.realPart = complex.getRealPart();
        this.imagPart = complex.getImagPart();

    }
   


    public double getRealPart() {return realPart;}
    public double getImagPart() {return imagPart;}
    
    public String toString() {  // Returns a String representation of this Complex object.

       String representation=null;
       representation = Double.toString(realPart).concat("+");
       representation = representation.concat(Double.toString(imagPart) );
       representation = representation.concat("i");

       return representation;

                      } 
		   
    public static String toString(Complex n) {  // Returns a String representation of the argument.

       String representation=null;
       representation = Double.toString(n.getRealPart()).concat("+");
       representation = representation.concat(Double.toString(n.getImagPart()));

       return representation;

                      } 


 /***********************************************************************************/
/* to replace an existing complex by another one multiplied by a double number     */ 
/**********************************************************************************/    
		
     public  void replacedBy(Complex complex, double constant) {
     
     
     realPart = complex.getRealPart() * constant;
     imagPart = complex.getImagPart() * constant;      
           
} // end of replacedBy method



 /**********************************************************************/
/* to multiply the complex object by a double number constant          */ 
/*********************************************************************/    
		
     public  void times(double constant) {
     
     
     realPart = realPart * constant;
     imagPart = imagPart * constant;      
           
} // end of times method


 /**********************************************************************/
/* to multiply the complex object by another complex object           */ 
/*********************************************************************/    
		
     public  void times(Complex z2) {
     
     double realPartBuffer, imagPartBuffer;
     
     realPartBuffer = (realPart*z2.getRealPart()  - imagPart*z2.getImagPart() );
     imagPartBuffer = (realPart*z2.getImagPart()  + imagPart*z2.getRealPart() );  
     this.realPart = realPartBuffer;
     this.imagPart = imagPartBuffer; 
           
} // end of times method


 /**********************************************************************/
/* this methods analyses a string and converts it to the complex format */ 
/*********************************************************************/    
		
     public static Complex parseComplex(String pattern) throws IOException{
     
           

    	 double realPart=0.0;
    	 double imagPart=0.0;

	 int    plusPosition;
	 int    iPosition;

	 try { // to check the data format

		 plusPosition = pattern.indexOf("+");

		 if (plusPosition != -1) { // there is a real and an imaginary part

			 iPosition = pattern.indexOf("i");
			 if (iPosition==-1) {

				 String message = "Error in Class Complex in method parseComplex" + Constants.newLine;            
				 message = message + "a + b number found instead of a + bi ... "+ Constants.newLine;
				 JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);	       	                       
				 throw new IOException();
			 }


			 realPart = Double.parseDouble(pattern.substring(0,plusPosition) );
			 imagPart = Double.parseDouble(pattern.substring(plusPosition+1, iPosition ) );
		 }

		 else  {                  // there is only a real part or only  an imaginary part

			 iPosition = pattern.indexOf("i");
			 if (iPosition == -1) { // there is only a real part 

				 realPart = Double.parseDouble(pattern);
				 imagPart = 0.0;

			 } 

			 else  {               // there is only an imaginary part 

				 realPart = 0.0;
				 imagPart = Double.parseDouble(pattern.substring(0, iPosition) );
			 }	  

		 }



	 } // end of the try bloc

	 catch (NumberFormatException e) {


		 String message = e.getMessage() + Constants.newLine;            
		 message = message + "NumberFormatException caught in Class Complex in  method parseComplex" + Constants.newLine;
		 message = message + "the string read:"+ e.getMessage()+" does not match a complex number" + Constants.newLine;
		 JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
				 JOptionPane.ERROR_MESSAGE);				        

		 throw new IOException();
	 }


	 return  new Complex(realPart, imagPart);


     
     
     }  // end of the parseComplex method  
     
     
 /**********************************************************************/
/* this methods serves to clone a complex object                      */ 
/*********************************************************************/    
       
      public Object clone(){
      
      
    	  Object o=null;
    	  try {
    		  o=super.clone();
    	  }
    	  catch(CloneNotSupportedException err) {

    		  String message = "Error in class Complex"+ Constants.newLine;
    		  message = message +  "CloneNotSupportedException" + Constants.newLine;
    		  JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                  
    		  //throw new runTimeException();

    	  }
    	  return(o);

      } // end of clone method
    
    
}// Complex
