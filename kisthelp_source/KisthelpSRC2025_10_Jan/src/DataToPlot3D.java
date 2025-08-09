 import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import kisthep.util.*;

import java.io.IOException;
import java.util.*;

public class DataToPlot3D {

	private JRadioButton radioButton;
	private double[] x,y; // 1D-array containing x (or y) values
	private double[][] z; // 2D-array containing z values
    private String labelX, labelY, labelZ;
    
    private String title; 
    private String legend; 
    private String xMathFormat, yMathFormat, zMathFormat;

 
    public DataToPlot3D (String buttonText, String title, String legend, String labelX, String labelY, String labelZ, double[] x,double[] y,double[][]z, String xMathFormat, String yMathFormat, String zMathFormat){
    	    this.radioButton = new JRadioButton(buttonText, false);
    	    this.title=title;
        this.legend=legend;
        this.labelX=labelX;
        this.labelY=labelY;
        this.labelZ=labelZ;
	    this.x = x.clone();
	    this.y = y.clone();
	    this.z = new double[this.y.length][this.x.length]; // take care !, the internal index is the first index of the arrays here
	    
	    for (int iX = 0; iX < this.x.length; iX++)
	    	   for (int iY = 0; iY < this.y.length; iY++)
	    		     this.z[iY][iX]=z[iY][iX]; // take care !, the internal index is the first index of the arrays here
	    		   
	    
	    

        this.xMathFormat = xMathFormat;
        this.yMathFormat = yMathFormat;
        this.zMathFormat = zMathFormat;
   
          
    } // end of constructor

    
    
    
   /* M E T H O D S */   
    public JRadioButton getRadioButton(){return radioButton;}

    
    public double[] getValueX()  {  	    
       	return this.x;
    }// end of getValueX array
  
    public double[] getValueY()  {  	    
       	return this.y;
    }// end of getValueY array

    public double[][] getValueZ()  {  	    
       	return this.z;
    }// end of getValueZ 2D-array

 

    public String getLabelX(){
        return labelX;
    }
    public String getLabelY(){
        return labelY;
    }
    public String getLabelZ(){
        return labelZ;
    }


    public String getTitle(){return title;}

    public String getLegend(){return legend;}

    public String getXMathFormat(){return xMathFormat;}

    public String getYMathFormat(){return yMathFormat;}

    public String getZMathFormat(){return zMathFormat;}

    
}// DataToPlot3D



