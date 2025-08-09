


import java.util.*;
import java.io.*;
import kisthep.file.*;
import kisthep.util.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;


public class Session  {


  
  // P R O P E R T I E 
  private int sessionMode;   // 0 -> single temperature mode
	                         // 1 -> range temperature mode
                             // 2 -> single pressure mode
                             // 3 -> range pressure mode
  
  private int lastChange;   // 0 -> no session mode change has occurred recently
                            // 1 -> a  session mode change has occurred recently
  
  private JTabbedPane tabbedPane; // a JTabbedPane to allow more than one pane to be displayed in the centralPane of Kisthelp	
  private int currentGraphIndex; // to remember the index of the current graph in JComponentsResult
  private int currentTabIndex; // to remember the index of the current Tab in the JTabbedPane put into centralPane
  
  
  private JComponentsResult sessionJResult; // the result : a Vector containing one or more jComponents actually displayed in the JDesktopPane
                                   // more important: though this object is not used once its components are added to the jDesktopPane,
                                   // it hosts a Listner !!
	
  private boolean loading = false ;
  private StringVector filesToBeRead;
  private StringVector filenameUsed;
  
  private Vector sessionContent; // a vector containing CLASS instances (Bimolecular Reactions, ...)
  private static Session currentSession=null; // no session at the beginning
  private static boolean toBeSaveFlag;


  private Unity unitSystem;

  

// temperatures automatically converted in Kelvin (or Pascal for pressure)  into this class in setTemperatureMin
// Thus, tMin, tMax, tSep are in Kelvin
// Thus, pMin, pMax, pStep are in Pascal
  private double tMin, tMax, tStep, pMin, pMax, pStep;

  private double scalingFactor; // scaling factor for vibrational frequencies
  private JScrollPane resultsScrollPane;
//  private AboutKisthepDisplayPane aboutKisthepPane ;

  private static boolean Radio1, Radio2;

    
// si plusieurs sessions alors prop static (ex : currentSessionIndex), de type Session, 
// attachee a l'indice de la session en cours


  
  // C O N S T R U C T O R
  
  public Session() {  
  
    filesToBeRead = new StringVector();
    filenameUsed  = new StringVector();
    
    sessionContent = new Vector();
   

    unitSystem = new Unity();

 
 // temperature range : (could replaced by a TemperatureRange object in a next kisthep version)   
    tMin =  Constants.tMinDefault ;
    tMax =  Constants.tMaxDefault;
    tStep = Constants.tStepDefault;


// pressure range : (could replaced by a TemperatureRange object in a next kisthep version)
  pMin =  Constants.P0 ;
  pMax =  Constants.P0;
  pStep = Constants.pStepDefault;


    
    scalingFactor = Constants.defaultScalingFactor;;
    currentSession = this; // static variable needed each time we want to add
                           // (elsewhere) an object to the current session   
    // currentSessionIndex = this;
    toBeSaveFlag = false;
    
    tabbedPane = new JTabbedPane(); // to show a tab for each JComponent of the sessionJresult
	tabbedPane.addChangeListener(new ChangeListener() { // a listener is added to this tabbedPane (which is unique)
		    public void stateChanged(ChangeEvent e) {
		    
		    if ( (tabbedPane.getTabCount() >1) && (tabbedPane.getSelectedIndex()>=0)) { // only adjust tabIndex if TabCount >0 (default is the first (index 0))
		    	

		    	Session.getCurrentSession().setCurrentTabIndex(tabbedPane.getSelectedIndex()); // to save the current selected tab
		      	
		    }
		      
		    }
		});

    currentGraphIndex = 0; // default value for the index of the graph to plotted in ResultsPane
    currentTabIndex = 0; // default value for the index of the tab selected in JtabbedPane
    sessionMode = 0; // default mode = single temperature mode
    lastChange = 0; // no recent change of sessionMode            
  
  } // end of C O N S T R U C T O R
  
  
  
  // M E T H O D S

  /* *******************************************/
  /* g e t c u r r e n t G r a p h I n d e x     */
  /* ******************************************* */
  public int getCurrentGraphIndex(){

  return currentGraphIndex;

  } // End of getCurrentGraphIndex method
  
  /* *******************************************/
  /* g e t c u r r e n t T a b I n d e x     */
  /* ******************************************* */
  public int getCurrentTabIndex(){

  return currentTabIndex;

  } // End of getCurrentTabIndex method
 
  /* *******************************************/
  /* g e t S e s s i o n M o d e     */
  /* ******************************************* */
  public int getSessionMode(){

  return sessionMode;

  } // End of getsessionMode() method

  /* *******************************************/
  /* g e t L a s t C h a n g e                   */
  /* ******************************************* */
  public int getLastChange(){

  return lastChange;

  } // End of getLastChange() method
  
  
  
  /* *******************************************/
  /* s e t c u r r e n t G r a p h I n d e x     */
  /* ******************************************* */
  public void setCurrentGraphIndex(int newIndex){

     currentGraphIndex = newIndex;
  	

  } // End of setCurrentTabIndex method

  /* *******************************************/
  /* s e t c u r r e n t T a b I n d e x     */
  /* ******************************************* */
  public void setCurrentTabIndex(int newIndex){

	 if (newIndex >=0){ 
     currentTabIndex = newIndex;
	 }
	 else currentTabIndex = 0;

  } // End of setCurrentTabIndex method

  /* *******************************************/
  /* s e t s e s s i o n M o d e                */
  /* ******************************************* */
  public void setSessionMode(int newMode){	  
	  
	  if ( (newMode >=0) && (newMode<=3) ) {
		  
		  if (newMode != sessionMode) {setLastChange(1);}
		  sessionMode = newMode;
		  
	  }
	  else sessionMode=0;
	  
  } // End of setSessionMode method
  
  /* *******************************************/
  /* s e t L a s t C h a n g e                  */
  /* ******************************************* */
  public void setLastChange(int newIndex){

	 if ( (newIndex==0) || ((newIndex==1)) ){ 
     lastChange = newIndex;
	 }
	 else lastChange = 0;

  } // End of setCurrentTabIndex method
  
  
  
/* *******************************************/
/* v e c t o r S i z e                       */
/* this method returns the size of the vector */
/*   of the current session           */
/*********************************************/
public int getSize(){

return sessionContent.size();

} // End of getSize() method

/******************************************************************/
/* A B O U T    K I S T H E P    D I S P L A Y */
/******************************************************************/
public static void aboutKisthepDisplay(){

Interface.getCentralPane().removeAll();
Interface.getCentralPane().validate();
AboutKisthepDisplayPane aboutKisthepPane = new AboutKisthepDisplayPane() ;
Interface.getCentralPane().add(aboutKisthepPane);
Interface.getCentralPane().validate();

}



   /**********************************/
   /*  d i s p l a y R e s u l t s   */
   /**********************************/
  public void displayResults() throws runTimeException,  IllegalDataException, OutOfRangeException{
 
 
 if (sessionContent.size()!=0) {  
 
 
   if ( (tMin <= tMax) && (pMin<=pMax) ) {  // to avoid unnecessary calculation with tMin > tMax ! for example !
   
	   
      // then build results (returned as Vector of JComponents to be embedded in the DesktopCentralPane)     
        sessionJResult = new JComponentsResult();
       
      // update display content in sessionJResult , including the informationBox (West)       
       refreshJResult();
        
   } // end of    if (tMin <= tMax) AND (pMin <=pMax) {
   
   
   else // no reason to keep the present display
   
   {

	Interface.getCentralPane().removeAll();
    Interface.getCentralPane().validate();
    Interface.getCentralPane().add(new JPanel());
	Interface.getCentralPane().validate();



     } // end of else
   
   
   
 
 } // end of  if (sessionContent.size()!=0) { 
 

  } // end of displayResults

  /**********************************/
  /*  r e f r e s h J R e s u l t    */
  /**********************************/
 public void refreshJResult() throws runTimeException{
    
	 
	 

					 
     // erase previous content of Desktop!
	 tabbedPane.removeAll();
	 
	 if (Interface.getCentralPane()!=null) {Interface.getCentralPane().removeAll();}

	 // add each element of the current JComponentsResult in the JDesktopPane 
	 TitledPane aTitledPane;
	 
     for (int iComponent=0; iComponent<sessionJResult.size(); iComponent++) {
          
    	        aTitledPane = (TitledPane)sessionJResult.get(iComponent);   
    	        tabbedPane.add(aTitledPane.getTitle(), aTitledPane.getJComp());
  }// end of for
     	
     
     // after refreshing two cases can be met:
     // a) user only changed single temperature (or pressure) value, or temperature unit ... => the last tabIndex must be kept
     // b) user change from single mode (one or several tabs) to range mode (a single tab), or from temperature mode (one or
     //    several tabs) to  pressure mode (one tab!), or from one range to another range => the number of tabs can change ! 
     //    => in this case set the new tabIndex to 0 !!
     
     if (this.getLastChange()==1) { // this is to check if sessionMode has just changed
    	 currentTabIndex = 0;
    	 setLastChange(0); // now, reset Mode last change to zero
     }
     else tabbedPane.setSelectedIndex(currentTabIndex); // selects the right tab (if previously selected before change temperature for ex.)
     
     
     
     Interface.getCentralPane().add(tabbedPane); 
     
     // refresh the FillInformation Box (WEST) for the current Object
     SessionComponent o = (SessionComponent)getCurrentSession().getSessionContent().firstElement();
     o.fillInformationBox();
     
     
     
   // refresh the overall JFrame
   Kistep.currentInterface.getContentPane().validate();
   Kistep.currentInterface.getContentPane().repaint();
  
   
   
 } // end of method refresJResult
 
 /**********************************/
   /*  s a v e R e s u l t s   */
   /**********************************/
  public void saveResults() throws IOException, CancelException,  runTimeException, IllegalDataException {
 
 
	  if (sessionContent.size()!=0) {  



		  // then save results 

		  SessionComponent currentObject = (SessionComponent) getCurrentSession().getSessionContent().firstElement();   

		  // ask for an output filename
		  File temporyFileName = KisthepDialog.requireOutputFilename("Results filename", new KisthepOutputFileFilter(Constants.csvOutputFile));
		  String fileName = temporyFileName.getAbsolutePath() ;

		  //  check the filename suffix
		  if (fileName.indexOf('.') == -1) {
			  fileName= fileName+".csv";		// to be read with excel												      
		  } // end of if


		  // to prepare the writing action
		  ActionOnFileWrite writeResults=null;

		  writeResults = new ActionOnFileWrite(fileName);



		  // compute results and save them on previous selected file
		  if ( (tMin == tMax) && (pMin == pMax) ) {currentObject.saveTxtResults(writeResults);  } // end of if 
    
/* deprecated
    if (tMin != tMax)

       {currentObject.saveGraphicsResults(writeResults, new TemperatureRange(tMin,tMax,tStep));
                                               
       }// end of if (  (tMin != tMax) ...

       
     if (pMin != pMax)

       {currentObject.saveGraphicsResults(writeResults, new PressureRange(pMin,pMax,pStep));
                                               
       }// end of if (  (pMin != pMax) ...

*/
    
// to close the writing action
    
    writeResults.end();
     
     
     
     
     } // end of  if (sessionContent.size()!=0) { 
 

  } // end of saveResults







   /**********************************/
   /*  s e s s i o n R e f r e s h T */ 
   /**********************************/
 
  public void refreshT() throws runTimeException,  IllegalDataException {
  
  
   
	if (sessionContent.size() != 0){


	       ToEquilibrium currentObject = (ToEquilibrium) sessionContent.get(0);
	       currentObject.setTemperature(tMin); // tMin is in Kelvin !!
               	       				  			
	}



       toBeSaveFlag = true;
} // end of the refreshT method
 


   /**********************************/
   /*  s e s s i o n R e f r e s h P */ 
   /**********************************/


 public void refreshP() throws runTimeException,  IllegalDataException{
  
  
   
	if (sessionContent.size() != 0){


	       ToEquilibrium currentObject = (ToEquilibrium) sessionContent.get(0);
	       currentObject.setPressure(pMin); 
               	       				  			
	}



       toBeSaveFlag = true;  
} // end of the refreshP method
 



   /******************************************************/
   /*  set of methods controlling temperature properties */ 
   /****************************************************/



public void setTemperatureMin(double tMin) throws TemperatureException,  runTimeException,  IllegalDataException  {

// tMin is in fact the text got from the User Graphical Interface 
// this number is given in User Unit
// this number is automatically converted in Kelvin into the class
   if (unitSystem.convertToTemperatureISU(tMin) > 0.0 ) {// T must be positive

      
      tMin = unitSystem.convertToTemperatureISU(tMin);
       this.tMin = tMin ;
	  refreshT();  // necessary only if the TMin Temperature has changed
			      // before to call setTemperatureMin

	// check, before, whether the range of temperature is null or not
        if (this.tMin == this.tMax) 
       {this.tStep = Constants.tStepDefault;}
     
        else { // if the tStep previous value was null then gives a non null value
             // only if tMax > tMin, because in the other case (tMax < tMin)
	     // no computation will be done
      
        if (this.tStep==0.0) {
           
       if (this.tMax > this.tMin) 
      {this.tStep=  (double)(this.tMax - this.tMin)/2.0d ;} // end of if (tMax > tMin) 
      
                             } // end of if (this.tStep==0.0) {

             } // end of if (tMin == tMax) else {     	
   

	
     }  // end of if
     
   else {throw new TemperatureException() ;}
             
   
   
      // end of if

}

public void setTemperatureMax(double tMax)throws TemperatureException {

// tMax is in fact the text got from the User Graphical Interface
// tMax must be given in User Unit
// automatically converted in Kelvin into the class

   if (unitSystem.convertToTemperatureISU(tMax) > 0.0 ) {

        tMax = unitSystem.convertToTemperatureISU(tMax);

        this.tMax = tMax ;
	
	// check, before, whether the range of temperature is null or not
        if (tMin == tMax) 
       {this.tStep = Constants.tStepDefault;}
     
        else { // if the tStep previous value was null then gives a nonnull value
             // only if tMax > tMin, because in the other case (tMax < tMin)
	     // no computation will be done
      
        if (this.tStep==0.0) {
           
       if (tMax > tMin) 
      {this.tStep=  (double)(tMax - tMin)/2.0d ;} // end of if (tMax > tMin) 
      
                             } // end of if (this.tStep==0.0) {

             } // end of if (tMin == tMax) else {     	
     }  // end of if
     
     
     
   else { throw new TemperatureException() ;}   
   
      // end of if

}

public void setStepTemperature(double tStep) throws TemperatureStepException{


// tStep is in fact the text got from the User Graphical Interface
// tStep must be in User Unit
// automatically converted in Kelvin into the class

   // be careful: temperature conversion is not necessary proportionality !
   tStep = unitSystem.convertToTemperatureISU(0.0+tStep) - unitSystem.convertToTemperatureISU(0.0) ;

   if (tStep > 0.0 ) {


// check, before, whether the range of temperature is null or not
      if (tMin == tMax) 
      {this.tStep = Constants.tStepDefault;}
      
      
      else {
        this.tStep = tStep ;
	
	
	    } 
     }  // end of if
     
   else {throw new TemperatureStepException();}   
   
      // end of if
}




   /******************************************************/
   /*  set of methods controlling pressure properties */ 
   /****************************************************/



public void setPressureMin(double pMin)throws PressureException, runTimeException,  IllegalDataException {


// pMin is in fact the text got from the User Graphical Interface
// pMin must be given in User Unit
// automatically converted in pascal into the class
// given pressure must always be positive  

   if (pMin > 0.0 ) {

        pMin = unitSystem.convertToPressureISU(pMin);

        this.pMin = pMin ;
	  refreshP();  // necessary only if the pMin pressure has changed

	// check, before, whether the pressure range is null or not
        if (this.pMin == this.pMax) 
       {this.pStep = Constants.pStepDefault;}
     
        else { // if the pStep previous value was null then gives a non null value
             // only if pMax > pMin, because in the other case (pMax < pMin)
	     // no computation will be done
      
        if (this.pStep==0.0) {
           
          if (this.pMax > this.pMin) 
          {this.pStep=  (double)(this.pMax - this.pMin)/2.0d ;} // end of if (pMax > pMin) 
      
                             } // end of if (this.pStep==0.0) {

             } // end of "if (pMin == pMax) else {   "  	
   

	
     }  // end of if
     
   else { throw new PressureException();  }  // end of else
   
   
      // end of if

}

public void setPressureMax(double pMax) throws PressureException {

// pMax is in fact the text got from the User Graphical Interface
// pMax must be given in User Unit
// automatically converted in pascal into the class
// given pressure must always be positive  


   if (pMax > 0.0 ) {

        pMax = unitSystem.convertToPressureISU(pMax);


        this.pMax = pMax ;
	
	// check, before, whether the pressure range is null or not
        if (this.pMin == this.pMax) 
       {this.pStep = Constants.pStepDefault;}
     
        else { // if the pStep previous value was null then gives a nonnull value
             // only if pMax > pMin, because in the other case (pMax < pMin)
	     // no computation will be done
      
            if (this.pStep==0.0) {
           
            if (this.pMax > this.pMin) 
            {this.pStep=  (double)(this.pMax - this.pMin)/2.0d ;} // end of if (pMax > pMin) 
      
                             } // end of if (this.pStep==0.0) {

             } // end of if (pMin == pMax) else {     	
     }  // end of if
     
     
     
   else { throw new PressureException();   }  // end of else
   
   
      // end of if

}

public void setStepPressure(double pStep) throws PressureStepException {

// pStep is in fact the text got from the User Graphical Interface
// pStep must be given in User Unit
// automatically converted in pascal into the class
// given pressure must always be positive  


   if (pStep > 0.0 ) {


 pStep = unitSystem.convertToPressureISU(pStep);

// check, before, whether the pressure range is null or not
      if (this.pMin == this.pMax) 
      {this.pStep = Constants.pStepDefault;}
      
      
      else {
        this.pStep = pStep ;
	
	
	    } 
     }  // end of if
     
   else { throw new PressureStepException() ; }  // end of else
   
   
      // end of if
}








/***********************************/
/**   get temperature information  */
/***********************************/


public double getTemperatureMin(){
return tMin; // in K
}

public double getTemperatureMax(){
return tMax;// in K
}

public double getStepTemperature(){
return tStep;// in K
}

/***********************************/
/**   get pressure information  */
/***********************************/


public double getPressureMin(){
return pMin;// in Pa
}

public double getPressureMax(){
return pMax;// in Pa
}

public double getStepPressure(){
return pStep;// in Pa
}


/***********************************/
/**   get unit information      */
/***********************************/

public Unity getUnitSystem(){
return unitSystem;
}

 
    /*************************************/
   /*  s e t S c a l i n g F a c t o r  */
   /*************************************/

  public void setScalingFactor(double scalingFactor) throws runTimeException,  IllegalDataException{

     if (scalingFactor > 0.0 & scalingFactor <= 1.0)  
     { this.scalingFactor = scalingFactor;  // /this.scalingFactor; ??
       refreshT(); // because the scaling factor has changed,
                   // it is necessary to recompute all the properties !
		   // as if with had changed the temperature
		   // (at least for the vibrational properties)
       toBeSaveFlag = true;
     }
     else 
     { JOptionPane.showMessageDialog(null,"Warning, 0.0 < scalingFactor <= 1.0","Temperature error",
       JOptionPane.WARNING_MESSAGE);
     }
                                                 } // end of the setScalingFactor method 


  
 
   /***********/
   /*  a d d  */  // adds a component to the session
                  // the components are those created in a Worksession
		  // thus, those corresponding to the menus (Equilibrium, Unimolecular, ...)
   /***********/  // session contains only one component (at present time)
  
  public void add(SessionComponent o) throws runTimeException{

    if (sessionContent.size() == 0){ // session must be empty before to add new component 
    sessionContent.add(o);
    toBeSaveFlag = true & (! loading);
    //o.fillInformationBox();
    //Kistep.currentInterface.getContentPane().validate();
} // end of if

    else {

    	String message = "Error occured in class Session.java in method add"+ Constants.newLine;
    	message = message +  "Session must be empty before to add new component"+ Constants.newLine;
    	message = message +  "Please contact the authors"+ Constants.newLine;
    	JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                  
    	throw new runTimeException();


    }
                                      } // end of add method



   /***************/
   /*  e r a s e  */
   /***************/
   				 
  public void calculationErase() {
    sessionContent.clear();  
    filesToBeRead.clear();
    filenameUsed.clear();  
    toBeSaveFlag = true;
    // reset to zero the index of the last button used in ForGraphicsButtonsBox
    setCurrentGraphIndex(0);
    // reset to zero the index of the last selected Tab in JTabbedPAne
    setCurrentTabIndex(0);
    

    
                      } // end of erase method
  




   /*************/
   /*  s a v e  */
   /*************/
   
  public void save(String safeguardFile) throws IOException, IllegalDataException {

    // safeguard of the vector content
    ActionOnFileWrite write = new ActionOnFileWrite(safeguardFile);	    
   
    // save the properties of the session

      write.oneString("CLASSNAME "+unitSystem.getClass().getName());
      unitSystem.save(write);

    write.oneString("currentGraphIndex");
    write.oneInt(currentGraphIndex);  
 
    write.oneString("currentTabIndex");
    write.oneInt(currentTabIndex);  

    write.oneString("sessionMode");
    write.oneInt(sessionMode);  

    write.oneString("lastChange");
    write.oneInt(lastChange);  
    
    write.oneString("minimum temperature :");
    write.oneDouble(tMin);
  
    write.oneString("maximum temperature :");
    write.oneDouble(tMax);
  
    write.oneString("temperature step:");
    write.oneDouble(tStep);

    write.oneString("minimum pressure :");
    write.oneDouble(pMin);
  
    write.oneString("maximum pressure :");
    write.oneDouble(pMax);
  
    write.oneString("pressure step:");
    write.oneDouble(pStep);   
    
    write.oneString("scaling factor :");
    write.oneDouble(scalingFactor);
    
    // save the vector containing all the filenames used
    write.oneString("number of filename used :");
    write.oneInt(filenameUsed.size());
    write.oneString("filename used :");
    for (int ifilename=0; ifilename<filenameUsed.size();ifilename++) {
    
        write.oneString((String)filenameUsed.get(ifilename));    
    
    } // end of for(ifilename=0;...

        
    // save the vector sessionContent
    String currentClassName;
    ReadWritable currentObject;

    for (int iComponent=0; iComponent<=sessionContent.size()-1; iComponent++) 
      { currentObject = (ReadWritable) sessionContent.get(iComponent) ;
	currentClassName = currentObject.getClass().getName();
	write.oneString("COMPONENT CLASSNAME "+currentClassName);
	currentObject.save(write);
      }  
    write.end();
    
    toBeSaveFlag = false;   
    
    
    //  do not forget in next futur to save the current session information
    // that can be done saving the index of the session in the sessionContent vector
    			
                                         } // end of save method



   /*************/		      
   /*  l o a d  */
   /*************/
   
   public void load(String safeguardFile) throws IOException, IllegalDataException, runTimeException, OutOfRangeException {   // read of the file and add of data to the session 
    
    ActionOnFileRead read = new ActionOnFileRead(safeguardFile, Constants.sessionFile);
    
    
    
    // specify the session is being loaded
    loading = true;
    // read the properties of the session

      read.oneString();
      unitSystem = new Unity(read);

    read.oneString();
    setCurrentGraphIndex(read.oneInt());

    read.oneString();
    setCurrentTabIndex(read.oneInt());

    read.oneString();
    setSessionMode(read.oneInt());
 
    read.oneString();
    setLastChange(read.oneInt());    
    
    read.oneString();
    tMin = read.oneDouble();
  
    read.oneString();
    tMax = read.oneDouble();

    read.oneString();
    tStep = read.oneDouble();
 
    read.oneString();
    pMin = read.oneDouble();
  
    read.oneString();
    pMax = read.oneDouble();

    read.oneString();
    pStep = read.oneDouble();

    read.oneString();
    scalingFactor = read.oneDouble();


    // read the vector containing all the filenames used
    filenameUsed = new StringVector();
    read.oneString();
    
    int nbFile = read.oneInt();
    read.oneString();
    for (int ifilename=0; ifilename<nbFile;ifilename++) {
    
        filenameUsed.add(read.oneString());    
    
    } // end of for(ifilename=0;...


    
    // read the file content and add object to the vector 
    String readLine = read.oneString();
    String currentClassName;
    while (readLine!=null) 
      { if (readLine.startsWith("COMPONENT CLASSNAME ")==true) {
	  if (readLine.substring(20).equalsIgnoreCase("BiMolecularReaction")) {
	    BiMolecularReaction currentObject = new BiMolecularReaction(read);
	    add(currentObject); 
	                                                     }
	  if (readLine.substring(20).equalsIgnoreCase("UnimolecularReaction")) {
	    UnimolecularReaction currentObject = new UnimolecularReaction(read);
            add(currentObject);
	                                                      }
	  if (readLine.substring(20).equalsIgnoreCase("Equilibrium")) {
	    Equilibrium currentObject = new Equilibrium(read);
	    add(currentObject);
	                                            }
	  if (readLine.substring(20).equalsIgnoreCase("InertStatisticalSystem")){
	    InertStatisticalSystem currentObject = new InertStatisticalSystem(read);
	    add(currentObject);
	                                                                        }											    	  		
	readLine = read.oneString();   }                        	 
       else {readLine = read.oneString();} 
      } // end of while (readLine!=null) 	


    //  do not forget in next futur to define the current session 
    // that can be done loading the index of the session in the sessionContent vector

    read.end();
     
     
 
    displayResults();





                                          } // end of load method


   /***************************************/
   /*  g e t S e s s i o n C o n t e n t  */
   /***************************************/
   
  public Vector getSessionContent () {
    return sessionContent;
                                     }  // end of the getSessionContent method





   /***************************************/
   /*  g e t C u r r e n t S e s s i o n  */
   /***************************************/
   
  public static Session getCurrentSession() {
    return currentSession;
                                            }


   /***********************************/
   /*  g e t T o B e S a v e F l a g  */
   /***********************************/
     
  public static boolean getToBeSaveFlag() {   
    return toBeSaveFlag;
                                } //end of the getToBeSaveFlag method
	
					    



   /************************************/	
   /*  g e t S c a l i n g F a c t o r */
   /************************************/
   				
  public double getScalingFactor() {
    return scalingFactor;
                                        } // end of the getScalingFactor method				



    /************************************/	
   /*  a d d F i l e n a m e U s e d   */
   /************************************/

   public void addFilenameUsed(String filename){

     filenameUsed.add(filename);
 
 
 } // end of addFilenameUsed



    /************************************/	
   /*  g e t F i l e n a m e U s e d   */
   /************************************/

   public StringVector getFilenameUsed(){

     return filenameUsed;
 
 
 } // end of getFilenameUsed
 
 
    /************************************/	
   /*  g e t F i l e s T o B e R e a d    */
   /************************************/
   				
  public StringVector getFilesToBeRead() {

  		return filesToBeRead ;}
		
		
				     	     		      			      	                    		     				  		      				 
                     } // end of class
