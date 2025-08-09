 import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import kisthep.util.*;

import java.io.IOException;
import java.util.*;

public class DataToPlot {

	private JRadioButton radioButton;
	private Vector valueSetVect; // must be a vector of double[axis][iPoint] array, with axis=0 for x, axis=1 for y    
    private String labelAbscissa;
    private String labelOrdinate;
    private String title; 
    private Vector legendVect; // must be a Vector of Strings
    private String xMathFormat, yMathFormat;

 
    public DataToPlot (String buttonText, String title, Vector legendVect, String labelAbscissa, String labelOrdinate, Vector valueSetVect, 
                       String xMathFormat, String yMathFormat){
    	    this.radioButton = new JRadioButton(buttonText, false);
    	    this.title=title;
        this.legendVect=legendVect;
        this.labelAbscissa=labelAbscissa;
        this.labelOrdinate=labelOrdinate;
	    this.valueSetVect = valueSetVect;

        this.xMathFormat = xMathFormat;
        this.yMathFormat = yMathFormat;
   
        
       
        
        
    } // end of constructor

    
    
    
   /* M E T H O D S */
    public int getDim(){return legendVect.size();} // returns the number of embedded line plots
    
    
    public JRadioButton getRadioButton(){return radioButton;}

    
    public double[] getValueX(int iLine) throws OutOfRangeException {
    	
    	    if ( (iLine < 0) || (iLine> this.getDim()) ) {    	    	    		    
    		    
    			String message = "OutOfRangeException exception caught in class dataToPlot, in method getValueX(int iLine)"+ Constants.newLine;
    			message = message +  "iLine = "+ iLine + Constants.newLine;
    			message = message +  "but must be in the range 0 - "+ this.getDim()+ Constants.newLine;
    			JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                  
    		    throw new OutOfRangeException();

    	    }
    	    
    	    double[][] iArray = (double[][])valueSetVect.get(iLine);    	    
      	double[] x = new double [iArray[0].length];
    	
        for(int i=0;i<iArray[0].length;i++){
              x[i] = iArray[0][i];           
            } // end of for
	
      	return x;
    }// end of getValueX
  
    public double[] getValueY(int iLine) throws OutOfRangeException {
    	
	    if ( (iLine < 0) || (iLine> this.getDim()) ) {    	    	
			String message = "OutOfRangeException exception caught in class dataToPlot, in method getValueY(int iLine)"+ Constants.newLine;
			message = message +  "iLine = "+ iLine + Constants.newLine;
			message = message +  "but must be in the range 0 - "+ this.getDim()+ Constants.newLine;
			JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                  
		    throw new OutOfRangeException();
	    
	    }
	    
	    double[][] iArray = (double[][])valueSetVect.get(iLine);
  	    double[] y = new double [iArray[0].length];
	
        for(int i=0;i<iArray[0].length;i++){
            y[i] = iArray[1][i];           
        } // end of for

  	    return y;
  } //  end of getValueY

 

    public String getLabelAbscissa(){
        return labelAbscissa;
    }

    public String getLabelOrdinate(){
        return labelOrdinate;
    }

    public String getTitle(){return title;}

    public String getLegend(int i){return (String)legendVect.get(i);}

    public String getXMathFormat(){return xMathFormat;}

    public String getYMathFormat(){return yMathFormat;}

    
}// DataToPlot



