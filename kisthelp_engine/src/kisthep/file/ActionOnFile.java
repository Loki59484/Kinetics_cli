


package kisthep.file;
import java.io.*;

import javax.swing.JOptionPane;

import kisthep.util.Constants;
import kisthep.util.runTimeException;
public abstract class ActionOnFile {// does not herit from the File class

    private String actionType;  // (reading or writing mode
    protected KisthepFile workFile;  // The File associated to the actionOnFile object 
                                          
   
    /** Creates new Class , constructeur */
    public ActionOnFile (String name, String actionType) throws IOException{
        
        try { 
        workFile = new KisthepFile(name); // the file must be closed before to be open and read !
                                                    // this will be checked in KisthepFile class
             }
	 catch (IOException err) 
	 {
		 String message = "Error in class ActionOnFile in constructor ActionOnFile(String name, String actionType)"+ Constants.newLine;
		 message = message +  "while attempting to access file "+name+" with "+actionType+" action"+ Constants.newLine;
		 JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                  
		 throw new IOException();
	 }    
        this.actionType = actionType;  
    }
    
      
    public abstract void end() throws IOException; // closes the stream associated to workFile
    
    public KisthepFile getWorkFile()  {  // return the file associated to the action
      
         return workFile; 	
     }

           
}
