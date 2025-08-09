
package kisthep.file;

import kisthep.util.*;

import java.util.*;
import java.io.*;

import javax.swing.JOptionPane;


public class KisthepFile extends File{
/*
a file class pointing out either an input or output KISTHELP file or a gaussian output or
a GAMESS or a nwChem output file used for reading necessary data or writing results during a KISTHEP session

*/

    private static Vector   openFileList = new Vector(); // a list containing the (String)  name
                                                                 // but the basename of the file !!
                                                                 // of EACH open file (that is, the end action has not been set off with this file)
    																// that is, several files with same name can be open, but successively only, but closed before new opening !

    private        String   type; // file Type (input or output Kisthep file or Gaussian or GAMESS or nwChem output file or unknown)
    
    /** Creates new Class; C O N S T R U C T O R */
    public KisthepFile (String name)  throws IOException {

// the absolute pathname must be given !!!         
        super(name);
        this.type= Constants.unknownFile; // cannot be detected (by reading action e.g.) 
                                         //  at that time because the ActionOnFile instance (that
                                         //is currently creating this KisthepFile instance) is under construction
	
        // checks whether or not the file is already open (in reading or writing mode)  (testing
	// the Basename of the file !! via getName())
         
        if (openFileList.contains(this.getName()) )
        { 
        	String message = "in class KisthepFile, in constructor File(String name)"+ Constants.newLine;
        	message = message +  "file " + name +  " currently open"+ Constants.newLine;
        	message = message +  "Please contact the authors" + Constants.newLine;
        	JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                  
        	throw new IOException() ;}

        openFileList.addElement(this.getName());



    } // end of public KisthepFile constructor
     

    /*
     * duplicate the file in a new file using the file name parameter
     */     
     public File duplicate(String fileName) throws IOException {    
    	 // fileName: the name of the file destination
    	 
    	 
    	 
    	 File duplicate; // the file that will contain the copy
    	 
    	 ActionOnFileWrite write = new ActionOnFileWrite(fileName);
    	 
    
    	 
    	 // open a reading action on the file source of this instance
    	 ActionOnFileRead readFromCurrentFile= new ActionOnFileRead (this.getAbsolutePath(),Constants.anyAllowedDataFile); 
	 String currentLine = readFromCurrentFile.oneString();  

	 
	 // reading loop      				
	 if (currentLine == null) {
		 JOptionPane.showMessageDialog(null,"Warning : the file " + this.getName() + " is empty ...");  
	 }
	 else {
		 do {  
			 write.oneString(currentLine);
			 currentLine = readFromCurrentFile.oneString(); 
		 } while (currentLine != null) ;
	 } // end of else

    	 
    	 
    	 duplicate = write.getWorkFile();
    	 readFromCurrentFile.end();
    	 write.end();
    	 
    	 return duplicate;
     } // end of method duplicate

    
    
    
    /*
     * remove the static list of open KISTHEPFILES
     */     
     public static void cleanOpenFileList()  {    
    	 openFileList.clear();
     }
     
     

     /*
     * remove the fileName from the static list of open KISTHEPFILES
     */     
     public static void remove (String name) throws IOException {

    	 if (openFileList.contains(name)) {
    		 openFileList.remove(name);
    	 }// if end
    	 else {
    		 String message = "Error in class KisthepFile, in method remove(String name)"+ Constants.newLine;
    		 message = message +  "file " + name +  " was not open before ... impossible to remove it from openFileList "+ Constants.newLine;
    		 message = message +  "Please contact the authors"+ Constants.newLine;
    		 JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                    
    		 throw new IOException() ;
    	 } // else end


     } // end of remove     

     /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        } // end of if
        return ext;
    } // end of public static String getExtension(File f) {


     /*
     * Get the prefix of a fileName STRING !! (not of a file) 
     */
    public static String getPrefix(String f) {
        String pref = null;
        String s = f;
        int i = s.lastIndexOf('.');

        if (i > 0 ) {
            // pref = s.substring(0, i).toLowerCase(); // but problem under Linux system that accounts for capitals in the filename
        	    pref = s.substring(0, i); 
        } // end of if
        return pref;
    } // end of public static String getPrefix(String f) {



     public String getType() { // return the type of the file 
        return this.type;
     } // end of getType
     
     public void setType(String type) { // set the type of the file 
        this.type= type;
     } // end of setType
    
 
 
      public boolean isDataFile() { // return true if the file is precisely a .kinp, g09, a NWchem or a GAMESS or ORCA or MOLPRO data file ( 
      
      if (type.equals(Constants.kInpFileType) ||
          type.equals(Constants.molproFileType) ||
    	  type.equals(Constants.orcaFileType) ||
          type.equals(Constants.g09FileType) ||
          type.equals(Constants.gms2012FileType) ||
          type.equals(Constants.nwcFileType) ||
          type.equals(Constants.ADFFileType)) {
	  
	  return true; 
	  } // if end 
      else return false; 
      
      
     } // end of isDataFile

      public boolean isQuantumOutputFile() { // return true if the file is precisely a g09, a NWchem or a GAMESS or ORCA or MOLPRO data file ( 
          
          if (type.equals(Constants.g09FileType) ||
              type.equals(Constants.gms2012FileType) ||
              type.equals(Constants.orcaFileType) ||
              type.equals(Constants.molproFileType) ||
              type.equals(Constants.nwcFileType) ||
              type.equals(Constants.ADFFileType)) {
    	  
    	  return true; 
    	  } // if end 
          else return false; 
          
          
         } // end of isQuantumOutputFile


      
      
             
}  // class end


