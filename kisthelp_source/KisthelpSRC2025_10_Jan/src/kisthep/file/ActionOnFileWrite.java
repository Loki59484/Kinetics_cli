

package kisthep.file;
import java.io.*;

import javax.swing.JOptionPane;

import kisthep.util.*;
public class ActionOnFileWrite extends ActionOnFile {


    private PrintWriter textOutputStream; // output stream
    
    
   
    /** Creates new Class , constructor */
    public ActionOnFileWrite (String name) throws IOException {
        
       
    	super(name, "write"); 
    	try {   // // checks for a buffer IO error
    		textOutputStream = new PrintWriter (new FileWriter (name));
    	} 

    	catch (IOException e){
    		String message = "Error in class ActionOnFileWrite, in constructor ActionOnFileWrite(String name )"+ Constants.newLine;
    		message = message +  "while trying to create a textOutputStream for file " + name + Constants.newLine;
    		JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                      		
    		throw new IOException();

    	}
        
    }

    public void oneString(String lineToBeWritten) { // writes one string on the work file
        
        textOutputStream.println(lineToBeWritten);
        
    }

    public void oneInt(int n) {  // writes one integer on the work file
        
        textOutputStream.println(n);
        
    }
    
    public void oneFloat(float n) {  // writes one float number on the work file
        
        textOutputStream.println(n);
        
    }    
    
    public void oneDouble(double n) {   // writes one double number on the work file
        
        textOutputStream.println(n);
        
    }    
    
    
    public void oneComplex(Complex n) {   // writes one Complex on the work file
        
        textOutputStream.println(n.toString());
 
        
    }    
    
    
    public void end() throws IOException {  // closes the outputstream associated to the work file

    	try { 
    		textOutputStream.close();
    		KisthepFile.remove(workFile.getName());
    	}
    	catch (IOException e) {
    		String message = "Error in class ActionOnFileWrite in the end method"+ Constants.newLine;
    		message = message +  "while trying to close the textOutPutStream for file " + workFile.getName() + Constants.newLine;
    		JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                      		
    		throw new IOException();

    	}


    }  

}
