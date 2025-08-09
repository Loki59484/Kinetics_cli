

import javax.swing.*;
import java.io.*;

import kisthep.file.*;
import kisthep.util.*;

public class KisthepDialog {

/* P R O P E R T Y */



/* C O N S T R U C T O R */



/* M E T H O D */


 /*************************************************/
/* s e t K i n p L o c a t i o n                 */ 
/************************************************/
	  public static String setKinpLocation(File temporyFileName) throws CancelException {

   	   String question = Constants.askingLocationString + " for the automatically generated kinp file";

   	   // get the kinp location    	   
   	   String temporyLocationName = KisthepDialog.requireExistingLocation(question, temporyFileName);
   	   
   	   // get the suffix fileName
   	   String suffix = temporyFileName.getName();
   	   
   	   // concat prefix and suffix
   	   String kinpFileName = temporyLocationName + "/" + suffix;
   	   
   	   // change extension to kinp
   	   kinpFileName =  KisthepFile.getPrefix(kinpFileName ) + ".kinp";

	
	
	  return kinpFileName;
	
	  } // end of method setKinpLocation
	  
	  
	  
 /*************************************************/
/* r e q u i r e E x i s t i n g L o c a t i o n */ 
/************************************************/
  public static String requireExistingLocation(String question, File temporyFileName) throws CancelException {

 // question: the question to be asked to the user
    String locationName = null;
 
 // creates a LocationChooser	

    
       File temporyLocation = temporyFileName.getParentFile();
       JFileChooser locationChooser = new JFileChooser(temporyLocation);
       locationChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
       locationChooser.setMultiSelectionEnabled(false);       
       locationChooser.setApproveButtonText("OK");
       locationChooser.setDialogTitle(Constants.kisthepMessage + " "+ question);
       
       locationChooser.setSelectedFile(temporyFileName.getParentFile());
       int result;  
       
  
// try to get a selected existing location          
                
       result = locationChooser.showOpenDialog(Interface.getKisthepInterface());
       // update the userCurrentDirectory in Kistep
       if (result == locationChooser.CANCEL_OPTION) {
               locationName = null;              
	           throw new CancelException();
       }  // end of if (result == locationChooser.CANCEL_OPTION
       else {

              locationName  = locationChooser.getSelectedFile().toString();
              
			
       } // end of if (result == fi... else 
       
 
    Kistep.setUserCurrentDirectory(locationName) ;
    //return locationName;

    return locationName ;


      } // end of requireExistingLocationName  method
      

 /*************************************************/
/* r e q u i r e E x i s t i n g F i l e N a m e */ 
/************************************************/
  public static File requireExistingFilename(String question,javax.swing.filechooser.FileFilter  filter) throws CancelException {

 // question: the question to be asked to the user
 // filter : file filter
    String fileName = null;
    boolean oKFlag;
    File file = null ;

 
    
    
// creates a FileChooser	

       JFileChooser fileChooser = new JFileChooser(Kistep.getUserCurrentDirectory());
       fileChooser.addChoosableFileFilter(filter);
 
       fileChooser.setMultiSelectionEnabled(false);       
       fileChooser.setApproveButtonText("OK");
       fileChooser.setDialogTitle(Constants.kisthepMessage + " "+ question);
       int result;  
       
      

// try to get a selected existing filename           
    do{        
       oKFlag = true;
       result = fileChooser.showOpenDialog(Interface.getKisthepInterface());
       
       
       

       // update the userCurrentDirectory in Kistep
       if (result == fileChooser.CANCEL_OPTION) {
               fileName = null;              
	           throw new CancelException();
       }  // end of if (result == fileChooser.CANCEL_OPTION
       else {

                     file = fileChooser.getSelectedFile();
		    
       } // end of if (result == fi... else 
       
       
       

                                     
      if (!file.exists())          
	 {JOptionPane.showMessageDialog(Interface.getKisthepInterface(), "this file does not exist","WARNING",JOptionPane.YES_OPTION);
	  oKFlag=false;} 
      if (file.isDirectory() )
         {JOptionPane.showMessageDialog(Interface.getKisthepInterface(), "this is a directory","WARNING",JOptionPane.YES_OPTION);
	 oKFlag=false;} 
         	
	 	                                                             
      } // end of do
    while (oKFlag==false); 
  


    Kistep.setUserCurrentDirectory(fileChooser.getCurrentDirectory().getAbsolutePath()) ;
    

    return file ;


      } // end of requireExistingFileName  method
      


 /*************************************************/
/* r e q u i r e O u t p u t F i l e N a m e */ 
/************************************************/
    public static File requireOutputFilename(String question,javax.swing.filechooser.FileFilter filter) throws CancelException {
   
    	File file;
    	boolean oKFlag;


    	// creates a FileChooser	
    	JFileChooser fileChooser = new JFileChooser(Kistep.getUserCurrentDirectory());

    	// if a session exists, AND the session is not empty,  AND it has an inertStatistical calculation content    	
    	if (Session.getCurrentSession() !=null) {
    		if (Session.getCurrentSession().getSessionContent().size()>0) {
    			String currentClassName = Session.getCurrentSession().getSessionContent().get(0).getClass().getName();
    			if  (currentClassName.equalsIgnoreCase("InertStatisticalSystem")) {
                // propose to the user a filename deduced from the  name of the current session
    				String suggestedName = (String)Session.getCurrentSession().getFilenameUsed().get(0); 
    				suggestedName = KisthepFile.getPrefix(suggestedName);
    				fileChooser.setSelectedFile(new File(suggestedName));
    			}
    		}
    	}
       
       
       fileChooser.addChoosableFileFilter(filter); 
       fileChooser.setMultiSelectionEnabled(false);       
       fileChooser.setApproveButtonText("OK");
       fileChooser.setDialogTitle(Constants.kisthepMessage+", "+question);
       int result;  
        
       
// try to save as filename
   do {
       oKFlag = true;
       result = fileChooser.showSaveDialog(Interface.getKisthepInterface());
       if (result == fileChooser.CANCEL_OPTION) {throw new CancelException();}  // end of if (result == fileChooser.CANCEL_OPTION
       else {	
                     file = fileChooser.getSelectedFile();

		     //fileNameArray[0] = file.getName();			//contains the file name
		     //fileNameArray[1] = file.getAbsolutePath() ;	//contains the file name and the path

       } // end of if (result == fi... else 
       
       
       

                                     
      if (file.exists())          
         {
          int answer=JOptionPane.showConfirmDialog(Interface.getKisthepInterface(),"This file already exists, do you want to continue ?","Save as", JOptionPane.YES_NO_OPTION);
          if (answer==JOptionPane.YES_OPTION) {oKFlag=true;}

         } // end of  if (file.exists())

      if (file.isDirectory() )
         {JOptionPane.showMessageDialog(Interface.getKisthepInterface(), "this is a directory","WARNING",JOptionPane.YES_OPTION);
         oKFlag = false;} // end of   if (file.isDirectory() )
         	
    } // end of do while
  while (oKFlag == false);
    

	Kistep.setUserCurrentDirectory(fileChooser.getCurrentDirectory().getAbsolutePath()) ;
	
	return file ; 

      } // end of requireOutputFileName  method
      
      
    /*************************************************/
    /* r e q u i r e R P O u t p u t F i l e N a m e */ 
    /************************************************/
    
    // this routine is almost the same as the previous one (requireOutputFilename) but
    // it is specific to ask for a filename for a reaction path file ! 
    // some information can only be checked out of this procedure (in routine buildReactPath()  of Interface)
        public static File requireRPOutputFilename(String question,javax.swing.filechooser.FileFilter filter) throws CancelException {
       
        	File file;
        	boolean oKFlag;


        	// creates a FileChooser	
        	JFileChooser fileChooser = new JFileChooser(Kistep.getUserCurrentDirectory());
           
           
           fileChooser.addChoosableFileFilter(filter); 
           fileChooser.setMultiSelectionEnabled(false);       
           fileChooser.setApproveButtonText("OK");
           fileChooser.setDialogTitle(Constants.kisthepMessage+", "+question);
           int result;  
            
           
    // try to save as filename
       do {
           oKFlag = true;
           result = fileChooser.showSaveDialog(Interface.getKisthepInterface());
           if (result == fileChooser.CANCEL_OPTION) {throw new CancelException();}  // end of if (result == fileChooser.CANCEL_OPTION
           else {	
                         file = fileChooser.getSelectedFile();


           } // end of if (result == fi... else 
           
                                       
          if (file.isDirectory() )
             {JOptionPane.showMessageDialog(Interface.getKisthepInterface(), "this is a directory","WARNING",JOptionPane.YES_OPTION);
             oKFlag = false;} // end of   if (file.isDirectory() )
             	
        } // end of do while
      while (oKFlag == false);
        

    	Kistep.setUserCurrentDirectory(fileChooser.getCurrentDirectory().getAbsolutePath()) ;
    	
    	return file ; 

          } // end of requireRPOutputFileName  method
          
          



      

   /*******************************/
   /*  r e q u i r e D o u b l e  */
   /*******************************/
   
  public static String requireDouble (String question, String title) throws CancelException  {
    double repValue = 0.0 ;
    boolean booleanValue = false;
    String rep;
    do {
	 rep = JOptionPane.showInputDialog(Interface.getKisthepInterface(), question, title,
         JOptionPane.INFORMATION_MESSAGE);
         if (rep==null) { throw new CancelException(); }	    
	 try {      
	       repValue = Double.parseDouble(rep);
	       booleanValue = true;
             }
         catch (NumberFormatException error) {
                                              
             JOptionPane.showMessageDialog(Interface.getKisthepInterface(),"Warning, your value should be a double !",Constants.kisthepMessage,
                                                      JOptionPane.WARNING_MESSAGE);					        
					     }
       } 
    while ( booleanValue == false) ;
    return rep;						 	 
     } // end of the requireDouble method 
     


   /*********************************/
   /*  r e q u i r e I n t e g e r  */
   /*********************************/
   
  public static String requireInteger (String question, String title) throws CancelException  {
    int repValue = 0 ;
    boolean booleanValue = false;
    String rep;
    do {
    	rep = JOptionPane.showInputDialog(Interface.getKisthepInterface(), question, title,
    			JOptionPane.INFORMATION_MESSAGE);
    	if (rep==null) { throw new CancelException(); }	    
    	try {      
    		repValue = Integer.parseInt(rep);
    		booleanValue = true;
    	}
    	catch (NumberFormatException error) {
    		JOptionPane.showMessageDialog(Interface.getKisthepInterface(),"Warning, your value should be a integer !","Delta Up error",
    				JOptionPane.WARNING_MESSAGE);					        
    	}
       } 
    while ( booleanValue == false) ;
    return rep;						 	 
     } // end of the requireInteger method      
          
						 
   } // end of KisthepDialog class
