


package kisthep.file;

import java.io.*;
import kisthep.util.*;

import java.util.regex.*;

import javax.swing.JOptionPane;
public class ActionOnFileRead extends ActionOnFile {

    private BufferedReader  inputBuffer; // input stream buffer
    
    
   
    /** Creates new Class , constructor */
    public ActionOnFileRead (String name, String wantedFileType) throws IOException {
     // name : the fileName associated with this reading action
     // only orca, molpro, gaussian09, nwchem, gamess, kisthelp , session, or regressionOutput  file types are authorized, 
    //  but, nevertheless, according to "wantedFiletype"IOException can be thrown
     // wantedFileType: the file type required by the user (can be either "anyFileType" (pointing out
     // a gaussian09, or a kisthep File Type), or "kisthepFileType")
     
    	    
       super(name, "read");  // calls the parent constructor
       
        
       

       try {   // checks for a buffer IO error
    	       inputBuffer = new BufferedReader (new FileReader (name));
            
	   
           }
       catch (IOException e){
    	   String message = e.getMessage() + Constants.newLine;            
    	   message = message + "IOexception caught in class ActionOnFileRead, in constructor ActionOnFileRead(String name " + name + ")" + Constants.newLine;
    	   message = message + "while trying to create an inputBuffer for reading file " + name + Constants.newLine;
    	   JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
    			   JOptionPane.ERROR_MESSAGE);					        

    	   throw new IOException();            
        }
        // automatically detects the type of the workfile associated to this Reading Action
	// compares to the wanted fileType,
	// and, if correct, returns to the buffer beginning
	        String  currentLine;
	        Pattern ADFP;
	        Pattern orcaP;
	        Pattern molproP;
            Pattern gaussian09P;
            Pattern gms2012P;
            Pattern nwcP;
            Pattern kisthepP;
	        Pattern sessionP;
	        Pattern regOutputP;

	        Matcher ADFM;
	        Matcher orcaM;
	        Matcher molproM;
            Matcher gaussian09M;
            Matcher gms2012M;
            Matcher nwcM;
            Matcher kisthepM;
	        Matcher sessionM;
	        Matcher regOutputM;

	        
	     // pattern for ADF 
            ADFP =  Pattern.compile("Amsterdam Modeling Suite");
            
		   // pattern for MOLPRO 
            molproP =  Pattern.compile("PROGRAM SYSTEM MOLPRO");
	        
	       // pattern for ORCA 
            orcaP =  Pattern.compile("O   R   C   A");
            	        
            // pattern for g09 or g03
            gaussian09P =  Pattern.compile("Gaussian, Inc.");

            // pattern for GAMESS2012
            gms2012P =  Pattern.compile("(GAMESS VERSION =).*((2012)|(2013)|(2018))");

            // pattern for NWchem
            nwcP =  Pattern.compile("Northwest Computational Chemistry Package \\(NWChem\\)");
            
            kisthepP     =  Pattern.compile("\\*MASS");
            sessionP     =  Pattern.compile("CLASSNAME");
	        regOutputP   =  Pattern.compile("Linear Regression with intercept");


            currentLine = this.oneString();
	        int lineNumber  = 0;
	    
            while (currentLine != null) {						          
	       		
            	
	 	lineNumber = lineNumber + 1;		
	 	ADFM         = ADFP.matcher(currentLine);
	 	molproM      = molproP.matcher(currentLine);
	 	orcaM        = orcaP.matcher(currentLine);
	 	gms2012M     = gms2012P.matcher(currentLine);
	 	nwcM         = nwcP.matcher(currentLine);
	 	gaussian09M  = gaussian09P.matcher(currentLine); 		          
	 	kisthepM     = kisthepP.matcher(currentLine);
		sessionM     = sessionP.matcher(currentLine);
		regOutputM   = regOutputP.matcher(currentLine);

		if (ADFM.find()) {				          
			  workFile.setType(Constants.ADFFileType); 
			  break;
		 			              }// fin du if  ADF 2024.102
		
		if (molproM.find()) {				          
			  workFile.setType(Constants.molproFileType); 
			  break;
		 			              }// fin du if  MOLPRO (2015.1)

		
		if (orcaM.find()) {				          
			  workFile.setType(Constants.orcaFileType); 
			  break;
		 			              }// fin du if  ORCA (2019, version 4.0.1.2)
		
	 	if (gms2012M.find()) {				          
			  workFile.setType(Constants.gms2012FileType); 
			  break;
		 			              }// fin du if  GAMESS2012

	 	
	 	if (nwcM.find()) {				          
			  workFile.setType(Constants.nwcFileType); 
			  break;
		 			              }// end of if  NWchem 6.0

		
	 	if (gaussian09M.find()) {			          
		  workFile.setType(Constants.g09FileType); 
		  break;
	 			              }// fin du if  gaussian09
					      
	 	if (kisthepM.find() ) {				          
		  workFile.setType(Constants.kInpFileType); 
		  break;
	 			              }// fin du if kisthep
					      
	 	if ( (sessionM.find()) && (lineNumber==1) ) {				          		  
		  workFile.setType(Constants.sessionFile); 			  		  
		  break;							  		  
	 			              }// fin du if  gaussian09 	  		    		  
										  						
					      
                currentLine = this.oneString();
	 	}// while end						          
	    				          
	    
	    


//      check whether the file type was found or not
	if (workFile.getType()==Constants.unknownFile) {
               inputBuffer.close();
         	   String message = "Error in Class ActionOnFileRead, in constructor"   + Constants.newLine;      
          	       message = message + "Unknown file type, nor ADF or MOLPRO or ORCA or Gamess or gaussian09 or NWChem or .kinp or .kstp session file" + Constants.newLine;
          	       message = message + "ADF                        : ** Amsterdam Modeling Suite (AMS) **  pattern not found" + Constants.newLine;
          	       message = message + "MOLPRO                     : ** PROGRAM SYSTEM MOLPRO **  pattern not found" + Constants.newLine;
          	       message = message + "ORCA                       : ** O   R   C   A **  pattern not found" + Constants.newLine;
        	       message = message + "NWChem                     : **Northwest Computational Chemistry Package (NWChem)**  pattern not found" + Constants.newLine;
        	       message = message + "gaussian 09                : **Gaussian, Inc.** pattern not found" + Constants.newLine;
        	       message = message + "Gamess                     : **GAMESS VERSION =... 2012 (or 2013)** pattern not found" + Constants.newLine;
        	       message = message + "kisthep (.kinp)            : ** *MASS ** pattern not found" + Constants.newLine;
        	       message = message + "kisthep session (.kstp)    : **CLASSNAME** pattern not found" + Constants.newLine;
        	       JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
        		   JOptionPane.ERROR_MESSAGE);				        
		        throw new IOException();
	}
	        
	else { // the file type is ok but, we have to compare it to the wanted file type
	       
	       
	       if (wantedFileType.equals(workFile.getType())  ||	       
	          (wantedFileType.equals(Constants.anyAllowedDataFile) && (workFile.isDataFile()))){
	       
	          
	    	   // close the inputBuffer and reopen it (reposition the cursor at the beginning)
	    	   inputBuffer.close(); 
	    	   try {   // checks for a buffer IO error
	    		   inputBuffer = new BufferedReader (new FileReader (name));

	    	   }
	    	   catch (IOException e){

	    		   String message = e.getMessage() + Constants.newLine;            
	    		   message = message + "IOException caught in class ActionOnFileRead, in constructor ActionOnFileRead(String name) " + Constants.newLine;
	    		   message = message + "while trying to create an inputBuffer for file " + name + Constants.newLine;
	    		   JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
	    				   JOptionPane.ERROR_MESSAGE);				        

	    		   throw new IOException();	      
	    	   }// fin du catch
 	        } // if end
	       else { // the file type is authorized but is not of the wanted file type !
	    	   String message = "Error while reading input file ..."+ Constants.newLine;            
 	    	   message = message + "wanted file type   : " + wantedFileType + Constants.newLine;
 	    	   message = message + "detected file type : " + workFile.getType()+ Constants.newLine;
 	    	   JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
 	    	   JOptionPane.ERROR_MESSAGE);				        

 	          throw new IOException();
	       
	       } // else end
	       
	       
	}// end of  else
    }// constructor end
   
    
    public void end()  throws IOException  {  // closes the stream associated to the work file
        
        try {
            inputBuffer.close(); 
            KisthepFile.remove(workFile.getName());
            }

        catch (IOException e) {    
	    	   String message = e.getMessage() + Constants.newLine;            
	    	   message = message + "IOException caught in class ActionOnFileRead in the end method" + Constants.newLine;
	    	   message = message + "while trying to close the inputBuffer for file " + workFile.getName() + Constants.newLine;
	    	   JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
	    			   JOptionPane.ERROR_MESSAGE);				        
	       throw new IOException();

            
                              }// end of catch
                       } // end of method end 
    
    
    public String oneString() throws IOException {    // a string is read from the work file
        
        String readLine=null;
        
	try {  // checks for an IO error
         readLine = inputBuffer.readLine();
	
            }
         catch (IOException e){
	    	   String message = e.getMessage() + Constants.newLine;            
	    	   message = message + "IOException caught in class ActionOnFileRead in the oneString method; probably end of file" + workFile.getName() + Constants.newLine;
	    	   JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
	    			   JOptionPane.ERROR_MESSAGE);				        

	     throw new IOException();
                              }
	 return readLine;          
        
   
        }
 
         
     
     
     public int oneInt() throws IllegalDataException, IOException{  // an integer is read from the work file
      int n=0;
      String readLine=null;
         
	 try {  // checks for a Number Format Exception 
	     readLine = this.oneString();
         n = Integer.parseInt(readLine);
         }       
         catch (NumberFormatException e) {
        	 
	    	   String message = e.getMessage() + Constants.newLine;            
	    	   message = message + "NumberFormatException caught in class ActionOnFileRead in the oneInt method" + Constants.newLine;
	    	   message = message + "the string read in file "+ workFile.getName() + ": "+ readLine + " does not point out an integer" + Constants.newLine;

	    	   JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
	    			   JOptionPane.ERROR_MESSAGE);					        
         throw new IllegalDataException();
         }
	 
         
      
       	 return n;          
     
         
         
     }
        
     public float oneFloat()  throws IllegalDataException, IOException {  // a float number is read from the work file
      
         String readLine=null;
         float n=0.0f;
         try {  // checks for a Number Format Exception
	     readLine = this.oneString();	 
         n = Float.parseFloat(readLine);
	
         }      
         catch (NumberFormatException e) {
        	 
	    	   String message = e.getMessage() + Constants.newLine;            
	    	   message = message + "NumberFormatException caught in class ActionOnFileRead in the oneFloat method" + Constants.newLine;
	    	   message = message + "the string read in file "+ workFile.getName() + ": "+ readLine + " does not point out a float number" + Constants.newLine;
	    	   JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
	    			   JOptionPane.ERROR_MESSAGE);				        

         throw new IllegalDataException();         
         }
         
         return n;          

         
                
     }
     public double oneDouble()  throws IllegalDataException, IOException { // a double number is read from the work file
      
         String readLine=null;
         double n=0.0;
                
         try { // checks for a Number Format Exception
	     readLine = this.oneString();	 	 
	     n = Double.parseDouble(readLine);

         }       
         catch (NumberFormatException e) {
	    	   String message = e.getMessage() + Constants.newLine;            
	    	   message = message + "NumberFormatException caught in class ActionOnFileRead in the oneDouble method" + Constants.newLine;
	    	   message = message + "the string read in file "+ workFile.getName() + ": "+ readLine + " does not point out a double number" + Constants.newLine;
	    	   JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
	    			   JOptionPane.ERROR_MESSAGE);				        
         throw new IllegalDataException();         
         }
         
	 return n;          
         
         
         
     }// oneDouble() end


     public Complex oneComplex() throws IOException, IllegalDataException { // a complex number is read from the work file
      

    	 double realPart=0.0;
    	 double imagPart=0.0;
    	 String complexString=null;
    	 int    plusPosition;
    	 int    iPosition;

         try { // checks for a Number Format Exception
	 
	 complexString = this.oneString();
	 plusPosition = complexString.indexOf("+");
	 if (plusPosition != -1) { // there is a real and an imaginary part
	 
	    iPosition = complexString.indexOf("i");
	    realPart = Double.parseDouble(complexString.substring(0,plusPosition) );
	    imagPart = Double.parseDouble(complexString.substring(plusPosition+1, iPosition ) );
	                         }
	 
	 else  {                  // there is only a real part or only  an imaginary part
	 
	    iPosition = complexString.indexOf("i");
            if (iPosition == -1) { // there is only a real part 
	    
	         realPart = Double.parseDouble(complexString);
		 imagPart = 0.0;
	    
	                          } 
				  
	     else  {               // there is only an imaginary part 
	    
	         realPart = 0.0;
		 imagPart = Double.parseDouble(complexString.substring(0, iPosition) );
	           }	  
	 
	       }
				 
        

         } // end of the try bloc
	 
         catch (NumberFormatException e) {
        	 
	    	   String message = e.getMessage() + Constants.newLine;            
	    	   message = message + "NumberFormatException caught in class ActionOnFileRead in the oneComplex method" + Constants.newLine;
	    	   message = message + "the string read in file "+ workFile.getName() + ": "+ complexString + " does not point out a complex number" + Constants.newLine;
	    	   JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,
	    			   JOptionPane.ERROR_MESSAGE);					        

         throw new IllegalDataException();         

         }

	 
	 return  new Complex(realPart, imagPart);



     }

    
}
