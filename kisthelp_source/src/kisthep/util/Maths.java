

package kisthep.util;
import java.text.*;

import javax.swing.JOptionPane;


public class Maths{


 

  public static final  double Threshold =1.0E-3;       
 
  
  
/********* M E T H O D S *********/  

  public static double bessel1(int n, double x) { // Bessel function of order n 
		  
	 int kMax = 10;
	 double sum=0.0;
	 
	 for (int k=0; k< kMax; k++) {		 
		sum = sum + Math.pow(x/2.0, (n+2*k)) / (Fact(k)* Fact(k+n)) ;
	 }// end of for
	  
	 return sum; 
	                                
  }

  public static double bessel2(int n, double x) throws IllegalDataException { // Bessel function 
	  
	 int kMax = 10;
	 double sum=0.0;
	 
	 for (int k=0; k< kMax; k++) {		 
		sum = sum + Math.pow(-1, k) * gamma(n+k+0.5)/ ( Math.pow(2*x, k) * Fact(k)* gamma(n-k+0.5)) ;
	 }// end of for
	  
	 
	 return Math.exp(x)*sum / Math.sqrt(2*Math.PI*x); 
	                                
  }
 
  
  public static double gamma(double x) throws IllegalDataException{ // gamma function Gamma(x) = integral( t^(x-1) e^(-t), t = 0 .. infinity)
                                         //  Uses Lanczos approximation formula.
	  int  n = (int) Math.abs(x) + 1; // (x+n) is chosen as a positive number
	  double prod=1.0;
	  
	          if (x==0) {              
	          
	        	  String message = "Error occurred in class kisthep.Maths" + Constants.newLine;            
	        	  message = message + "in method gamma(x) for x = 0"+ Constants.newLine;
	        	  message = message + "The gamma function is defined for all complex numbers except the negative integers and zero.";
	        	  JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);	       	                       
	        	  throw new IllegalDataException();
	          }
	          
	          if (x>0) {
	        	     double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
	        	     double ser = 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
	        			  + 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
	        			  +  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5);
	        	     return Math.exp(tmp + Math.log(ser * Math.sqrt(2 * Math.PI)));
	          }// end of if (x>0) 

	          else {// the gamma function is defined for all negative non-integer  

	        	  // check if x is an integer or not
	           	  int integerPart = (int) x;
	           	  if (x == integerPart) {	        		  
	           		  String message = "Error occurred in class kisthep.Maths" + Constants.newLine;            
	           		  message = message + "in method gamma(x) for x = "+ x + Constants.newLine;
	           		  message = message + "The gamma function is defined for all complex numbers except the negative integers and zero.";
	           		  JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);	       	                       
	           		  throw new IllegalDataException();
	           	  }	// end of if (x == integerPart)        		  	        		  

	         	  else {
	        		  /* The behavior for nonpositive z is more intricate. Euler's integral does not converge for z ² 0, 
	        		   * but the function it defines in the positive complex half-plane has a unique analytic continuation to the negative half-plane.
                     One way to find that analytic continuation is to use Euler's integral for positive arguments and extend the domain 
                     to negative numbers by repeated application of the recurrence formula
	        		   */


                      for (int i=0; i<=(n-1); i++) {
                  	      prod = prod * (x+i);                  	   
                      }// end of for                  
 
	          	  }// end of x is non-integer and negative
	        	  
               	return gamma(x+n)/prod;
	        	  
	          }  // end of  if (x>0)
	        	  
  
  }// end of gamma method

  public static double[] getGaussLaguerreW(int n) throws IllegalDataException{
     
     if ( (n!=15) && (n!=32) ){
    	 
    	 
    	 String message = "Error occurred in class kisthep.Maths" + Constants.newLine;            
    	 message = message + "in method getGaussLaguerreW"+ Constants.newLine;
    	 message = message + "while carrying out a 15(or 6)-point Gauss-Laguerre integration"+ Constants.newLine;
    	 message = message + "the kisthelp current version is limited to 15(or 6) points ..."+ Constants.newLine;
    	 message = message + "Please, contact the authors";
    	 JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);	       	                       
    	 throw new IllegalDataException();

    	 
}
   
      
      
/*  the following commented lines correspond
 *  to the exact determination of the GaussLaguerre weights
 *  whatever the polynomia degree choosen
 *
    double[] xk = getGaussLaguerreX(n);
    double[] wk = new double[n];

    for (int iZero=0 ; iZero< n ; iZero++) {

      wk[iZero]= (n+1)*(n+1)*(Laguerre(n+1,xk[iZero]))*(Laguerre(n+1,xk[iZero]));
      wk[iZero]= Math.exp(xk[iZero])*xk[iZero]/wk[iZero];
       
 
    } // end of for
 */
/* to gain efficiency, the  weights of a 15 order GaussLaguerre polynomia
 * are returned */
  
  double[] wk = new double[n];
     
  if (n==15) {   
   
   
   wk[0]= 0.23957817031109838  ;
   wk[1]= 0.5601008427926257   ;
   wk[2]= 0.8870082629187681   ;
   wk[3]= 1.2236644021488354   ;
   wk[4]= 1.5744487216218261   ;
   wk[5]= 1.9447519764703447   ;
   wk[6]= 2.341502056770095    ;
   wk[7]= 2.774041925251557    ;
   wk[8]= 3.2556433502546467   ;
   wk[9]= 3.8063117104779813   ;
   wk[10]= 4.4584777485705755  ;
   wk[11]= 5.270017780631901   ;
   wk[12]= 6.359563470989392   ;
   wk[13]= 8.031787629250575   ;
   wk[14]= 11.527772100923935  ;

      
      }
  
  if (n==32) {   
	  
	   
	  wk[0]= 0.114187105768;
	  wk[1]=  0.266065216898;
	  wk[2]= 0.418793137325;
	  wk[3]= 0.572532846497;
	  wk[4]=  0.727648788453;
	  wk[5]= 0.884536718946;
	  wk[6]= 1.04361887597;
	  wk[7]= 1.20534920595;
	  wk[8]= 1.37022171968;
	  wk[9]= 1.53877595906;
	  wk[10]= 1.71164594592;
	  wk[11]= 1.8895649683;
	  wk[12]= 2.07318851235;
	  wk[13]= 2.26590144444;
	  wk[14]= 2.46997418988;
	  wk[15]= 2.64296709494;
	  wk[16]= 2.76464437462;
	  wk[17]=  3.22890542981;
	  wk[18]=  2.92019361963;
	  wk[19]=  4.3928479809;
	  wk[20]= 4.27908673189;
	  wk[21]=5.20480398519;
	  wk[22]= 5.11436212961;
	  wk[23]= 4.15561492173;
	  wk[24]=6.19851060567;
	  wk[25]= 5.34795780128;
	  wk[26]= 6.28339212457;
	  wk[27]= 6.89198340969;
	  wk[28]=  7.92091094244;
	  wk[29]= 9.20440555803;
	  wk[30]=  11.1637432904;
	  wk[31]=  15.3902417688;

	      
	     }

  return wk;
                                 } // end of Gauss-Laguerre weights




/* Gauss-Laguerre integration*/
/****************************/

/* !!: this method is currently limited to 15 order Gauss Laguerre polynomia
 whatever the value of n   */                                  
                                 
  public static double getGaussLaguerreIntg(double[] f, int n) throws IllegalDataException{

      if ( (n!=15) && (n!=32) ) {
    	  
     	 String message = "Error occurred in class kisthep.Maths" + Constants.newLine;            
     	 message = message + "in method getGaussLaguerreIntg"+ Constants.newLine;
     	 message = message + "while carrying out a 15(or 6)-point Gauss-Laguerre integration"+ Constants.newLine;
     	 message = message + "the kisthelp current version is limited to 15(or 6) points ..."+ Constants.newLine;
     	 message = message + "Please, contact the authors";
     	 JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);	       	                       
     	 throw new IllegalDataException();
    	  
}
      
  
   
   
   double[] wk =  getGaussLaguerreW(n);  
   double integral= 0.0;
   
   for (int iRoot=0; iRoot < n; iRoot++) {
  
     integral = integral + f[iRoot]*wk[iRoot];
  
  
  } // end of for (iRoot ...)


   return integral ;
  
  } // end of getGaussLaguerreIntg




/* getGaussLaguerreX returns the zeros of Laguerre Polynomial L_n(x) */
/********************************************************************/
  public static double[] getGaussLaguerreX(int n) throws IllegalDataException{


      if ( (n!=15) && (n!=32) ) {
    	  
      	 String message = "Error occurred in class kisthep.Maths" + Constants.newLine;            
      	 message = message + "in method getGaussLaguerreX"+ Constants.newLine;
      	 message = message + "while carrying out a 15(or 6)-point Gauss-Laguerre integration"+ Constants.newLine;
      	 message = message + "the kisthelp current version is limited to 15 points ..."+ Constants.newLine;
      	 message = message + "Please, contact the authors";
      	 JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);	       	                       
      	 throw new IllegalDataException();    	  
    	  }
     
      
      
/*  the following commented lines correspond
 *  to the exact determination of the GaussLaguerre roots
 *  whatever the polynomia degree choosen
 *
      
      

   double[] zero= new double[n];

// find the first zero 


    double zBar  = 0.0;
    double x     = zBar;
    double xBar  = zBar;
    double xStep;


 for (int iZero=0; iZero<=n-1; iZero++) {



     x     = zBar;

    
     if (iZero==0) {xStep = 0.005;}
  
     else {xStep=0.1;}




     do    // look for the segment (x-xBar) including the current zero 
    
     {

       xBar = x;
       x = xBar + xStep;
        } // end (local) do


     while (Laguerre(n,x)*Math.pow(-1,iZero) > 0.0);

  // apply the newton procedure into the dichotomic scheme 
  // newton scheme now : x = x0 - y(0)/y'(0)
          

     do {
      
       xBar = x;
      
       x = xBar -  (Laguerre(n, xBar)/LaguerreDerivative(n, xBar) );
      } 
     while ( Math.abs(x-xBar) > Threshold ) ;// end of newton procedure 
	  



  // one zero has been found ! 
    zero[iZero]=x;
    zBar = x;


   } // end of (global) for iZero=...


return zero;
*/

/* to gain efficiency, the  roots of a 15 order GaussLaguerre polynomia
 * are returned */
      
 double[] xk = new double[n];      
      
 if (n==15) {     
         

 xk[0]= 0.09330781201728182  ;   
 xk[1]= 0.4926917403018838   ;   
 xk[2]= 1.2155954120709531   ;   
 xk[3]= 2.269949526203813    ;  
 xk[4]= 3.667622721751349    ;   
 xk[5]= 5.425336627414452    ;   
 xk[6]= 7.565916226655837    ;   
 xk[7]= 10.120228567969578   ;   
 xk[8]= 13.130282481995314   ;   
 xk[9]= 16.654407708593787   ;   
 xk[10]=  20.776478899080423 ;   
 xk[11]=  25.623894226339683 ;   
 xk[12]=  31.407519169671307 ;   
 xk[13]=  38.53068330644345  ;   
 xk[14]=  48.02608557270582  ;  
 
 
 
 }
 
 if (n==32) {     
	        

	 xk[0]= 0.0444893658333;      
	 xk[1]=0.23452610952;        
	 xk[2]=0.576884629302;       
	 xk[3]= 1.07244875382;        
	 xk[4]= 1.72240877644;        
	 xk[5]= 2.52833670643;        
	 xk[6]= 3.49221327285;        
	 xk[7]= 4.61645677223;        
	 xk[8]= 5.90395848335;        
	 xk[9]= 7.3581268086;	      
	 xk[10]= 8.98294126732;        
	 xk[11]= 10.783012089;	      
	 xk[12]=12.763745476;	      
	 xk[13]= 14.9309117981;        
	 xk[14]= 17.2932661372;        
	 xk[15]= 19.8536236493;        
	 xk[16]= 22.6357789624;        
	 xk[17]= 25.6201482024;        
	 xk[18]= 28.8739336869;        
	 xk[19]= 32.3333294017;        
	 xk[20]= 36.1132042245;        
	 xk[21]= 40.1337377056;        
	 xk[22]= 44.5224085362;        
	 xk[23]=49.2086605665;        
	 xk[24]= 54.3501813324;        
	 xk[25]= 59.8791192845;        
	 xk[26]= 65.9833617041;        
	 xk[27]= 72.6842683222;        
	 xk[28]= 80.1883747906;        
	 xk[29]= 88.735192639;	      
	 xk[30]= 98.8295523184;        
	 xk[31]= 111.751398227;    	 
	 
	 }
 
 return xk;
  
  } // end of getGaussLaguerreX




/***********************************************************/

  public static long Fact(int x) {

      long fact=1;

     
       for (int i=1; i<=x; i++) {


          fact = fact*i;

                            } // end for



      return fact;



  }// end Factorielle




/***********************************************************/

  public static double Laguerre(int n , double x ) throws IllegalDataException{

	  double Ln;

	  if (x <0 ) {
	  
   	 String message = "Error occurred in class kisthep.Maths" + Constants.newLine;            
   	 message = message + "in method Laguerre when calculating Laguerre-polynomial..."+ Constants.newLine;
   	 message = message + "parameter x must be positive"+ Constants.newLine;
   	 message = message + "Please, contact the authors";
   	 JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);	       	                       
   	 throw new IllegalDataException();    	  

	  
	  
	  } //end if





/*  the following formula bugs when n>=20 ... 
      
      double Ln=0.0;
  
 for (int i=0; i<=n; i++) {


        Ln = Ln + Fact(n)*Math.pow(x,n-i)*Math.pow(-1,i)/(Math.pow(Fact(n-i),2)*Fact(i) );


      } // end for



      return Ln*Math.pow(-1,n);

*/
      
/* the following formula is recursive and uses the relation:
 
 (n+1) Ln+1(x) = (2n+1-x)Ln(x)-nLn-1(x)*/
      
      if (n==1) {Ln=1.0-x;} // end if_1
      else {
      
          if (n==0) {Ln=1.0;} // end if_2
          
          else {   
      Ln = (2*n-1-x)* Laguerre(n-1,x) - (n-1)*Laguerre(n-2,x);     
      Ln = Ln / n; } // end of else_2
          
      } // end of else_1

          
  return Ln;        
          
  }// end Laguerre

  

/***********************************************************/

  public static double LaguerreDerivative(int n , double x ) throws IllegalDataException{

  
    double x1, x2 ;
    double y1, y2;
      
    if (x==0) {
	       
	     	 String message = "Error occurred in class kisthep.Maths" + Constants.newLine;            
	       	 message = message + "in method LaguerreDerivative when calculating derivative..."+ Constants.newLine;
	       	 message = message + "parameter x must be non zero ..."+ Constants.newLine;
	       	 message = message + "Please, contact the authors";
	       	 JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);	       	                       
	       	 throw new IllegalDataException();    	     
    }

    if (n==0) {return 0.0; }


    else { 
          
//     return n*LaguerreDerivative(n-1, x) - n*Laguerre(n-1 , x); }
//       return n*( Laguerre(n, x) - Laguerre(n-1 , x)) / x; }
    
//  System.out.println("deriv 1 = "+  n*( Laguerre(n, x) - Laguerre(n-1 , x)) / x);    
        
     x1 = x- Threshold/100.0;
     x2 = x+ Threshold/100.0;
    
     y1 = Laguerre(n, x1);
     y2 = Laguerre(n, x2); 
//  System.out.println("deriv 2 = "+ (y2-y1)/(x2-x1));
//  System.out.println();
     return (y2-y1)/(x2-x1);}
  } // end LaguerreDerivative
          



   
    
public static String format(double doubleValue, String formatString) throws runTimeException {

    
    if ( (doubleValue < Double.POSITIVE_INFINITY) && (doubleValue > Double.NEGATIVE_INFINITY) ) 
    {
    
   DecimalFormat formatter=null ;
try {
   formatter = new DecimalFormat(formatString);  

// decimal separator is . 
   DecimalFormatSymbols dFS = formatter.getDecimalFormatSymbols();
   dFS.setDecimalSeparator('.');
   formatter.setDecimalFormatSymbols(dFS) ;
   }
   
 catch (IllegalArgumentException error) 
 {
	 
 	 String message = "Error occurred in class kisthep.Maths" + Constants.newLine;            
   	 message = message + "in method format"+ Constants.newLine;
   	 message = message + "IllegalArgumentException"+ Constants.newLine;
   	 message = message + "wrong string argument using DecimalFormat(String format) constructor";
   	 JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);	       	                       
   	 throw new runTimeException();    	     
  
  }
   return (formatter.format(doubleValue) );

   
    } // end of if ((doubleValue < Double.POSITIVE_INFINITY)...
    
    else   {return String.valueOf(doubleValue);}
} // end of format method

} // Math
