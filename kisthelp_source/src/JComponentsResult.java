


import java.awt.event.*;



import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;

import kisthep.file.ActionOnFileRead;
import kisthep.file.ActionOnFileWrite;
import kisthep.file.ReadWritable;
import kisthep.util.*;

import javax.swing.text.*;
import org.math.plot.*;
import org.math.plot.plotObjects.*;

public class JComponentsResult extends Vector implements ActionListener {

	
  // P R O P E R T I E S
	Vector graphicVector; // a vector returned by getGraphicsResults of the four major components (Bimolecular, Unimol, Equil, InertStat)
	                      // that will be listen to manage events on buttons
	
	Vector graphic3DVector; // a vector returned by get3DGraphicsResults of the four major components (Bimolecular, Unimol, Equil, InertStat)
    // that will be listen to manage events on buttons

 // C O N S T R U C T O R

   public JComponentsResult() throws runTimeException, IllegalDataException, OutOfRangeException {
   
   
   double tMin = Session.getCurrentSession().getTemperatureMin();
   double tMax = Session.getCurrentSession().getTemperatureMax();
   double tStep = Session.getCurrentSession().getStepTemperature();



// get pressure in ISU
   double pMin = Session.getCurrentSession().getPressureMin();
   double pMax = Session.getCurrentSession().getPressureMax();
   double pStep = Session.getCurrentSession().getStepPressure();

   
   // initialize the two vectors with empty vectors;
   graphicVector = new Vector();
   graphic3DVector = new Vector();

   // only the first element of the session is considered at present time
   SessionComponent currentObject = (SessionComponent) Session.getCurrentSession().getSessionContent().firstElement();

   // case of one temperature  and one pressure (can eventually concern the range temperature with TMin=TMax !)    
   if ( (tMin == tMax) && (pMin==pMax) ){

	   
		// remove possible content of ForGraphicsButtonsBox (because we are in the single Temperature/pressure case)
		if (Interface.getForGraphicsButtonsBox()!=null) {Interface.getForGraphicsButtonsBox().removeAll();}

       // get the results to be displayed  (a String and a JTable are returned by getTextResults, sometimes more...)
       // add all results returned by getTextresults in the Vector property of this object
		Vector v = currentObject.getTextResults(); // return a vector of titledPanes
       for (int i=0; i<v.size(); i++) {
    	      this.add(v.get(i)); 
    	   }
    	
		

    } // end of if (TMin == TMax)&& (pMin==pMax)

   // case of a range of temperature with TMin < TMax
   if ( (tMin != tMax)  && (pMin==pMax) )
   {
	   
	  
   // get all the graphs    
    graphicVector = currentObject.getGraphicsResults(new TemperatureRange(tMin, tMax, tStep) );

    // remove possible content of ForGraphicsButtonsBox (because we are in the single Temperature/pressure case)
	if (Interface.getForGraphicsButtonsBox()!=null) {Interface.getForGraphicsButtonsBox().removeAll();}
    // put one button per Graph on the ForGraphicsButton Box (EAST)
    forGraphicsButtons();
   
   // build plot the first graph   
   plot2DResults(Session.getCurrentSession().getCurrentGraphIndex()); // at first time, when workSession is created, currentGraphIndex is set to 0

   
   } // end of if (tMin != tMax)

   if ( (pMin != pMax) && (tMin == tMax) ) {
       
  // case of a range of pressure with pMin < pMax

    // get the graphs (returned in a Vector)    
    graphicVector = currentObject.getGraphicsResults(new PressureRange(pMin, pMax, pStep));
   
	// remove possible content of ForGraphicsButtonsBox (because we are in the single Temperature/pressure case)
	if (Interface.getForGraphicsButtonsBox()!=null) {Interface.getForGraphicsButtonsBox().removeAll();}
   // put one button per Graph on the ForGraphicsButton Box (EAST)
    forGraphicsButtons();
   
   // build plot the first graph   
    plot2DResults(Session.getCurrentSession().getCurrentGraphIndex()); // at first time, the first graph (index 0) is plotted
    
   
   
   }// end of  if (pMin != pMax)
       
   if ( (pMin != pMax) && (tMin != tMax) ) {
       
	   
	   
	   
	  
  // case of a range of pressure with pMin < pMax AND range of temperature with tMin < tMax

    // get the graphs (returned in a Vector = a set of dataToPlot3D objects)    
	   graphic3DVector = currentObject.get3DGraphicsResults(new TemperatureRange(tMin, tMax, tStep), new PressureRange(pMin, pMax, pStep));
   
	// remove possible content of ForGraphicsButtonsBox (because we are in the single Temperature/pressure case)
	if (Interface.getForGraphicsButtonsBox()!=null) {Interface.getForGraphicsButtonsBox().removeAll();}
   // put one button per Graph on the ForGraphicsButton Box (EAST)
    for3DGraphicsButtons();
    
   
   // build plot the first graph   
    plot3DResults(Session.getCurrentSession().getCurrentGraphIndex()); // at first time, the first graph (index 0) is plotted
 
   
   
   
   }// end of  if (pMin != pMax)&& (tMin == tMax)
       
 
   
   

} // end of Constructor JComonentsResult

//M E T H O D S
   /**********************************/
   /*  p l o t 2 D R e s u l t s     */
   /**********************************/
  public void plot2DResults(int iGraph) throws OutOfRangeException {

	  
   // create your PlotPanel (you can use it as a JPanel)
   Plot2DPanel plot = new Plot2DPanel();  

   // add a title
	BaseLabel title = new BaseLabel(((DataToPlot)(graphicVector.get(iGraph))).getTitle(), Color.RED, 0.5, 1.1);
	title.setFont(new Font("Courier", Font.BOLD, 20));
	plot.addPlotable(title);
   
   // define the legend position
   plot.addLegend("SOUTH");
   
   
   // define Labels
   String xLabel = ((DataToPlot)(graphicVector.get(iGraph))).getLabelAbscissa(); 
   String yLabel = ((DataToPlot)(graphicVector.get(iGraph))).getLabelOrdinate();  
   ((PlotPanel)(plot)).setAxisLabels(xLabel, yLabel);
       
   // add n line plots to the PlotPanel for graph iGraph
   if (((DataToPlot)graphicVector.get(iGraph)).getDim()!=0) { // if there is at least one curve to plot
	   DataToPlot dtp=(DataToPlot)(graphicVector.get(0));
	   for (int i=0; i<((DataToPlot)graphicVector.get(iGraph)).getDim(); i++) {	 
		   dtp = (DataToPlot)(graphicVector.get(iGraph));
		   plot.addLinePlot(dtp.getLegend(i), dtp.getValueX(i), dtp.getValueY(i) );

	   } 


	   // wrap Plot2DPanel and its title into a TitledPane
	   TitledPane titledPane = new TitledPane(dtp.getTitle(), plot);

	   // replace (or add if it is the first time) the plot at 0th position in the current object JComponentsResults with the new one 
	   if (this.isEmpty()) {this.add(titledPane);} else {this.set(0, titledPane);}


   } // end of if (((DataToPlot)graphicVector.get(iGraph)).getDim()!=0)
   
  } // end of plot2DResults method

  
  /**********************************/
  /*  p l o t 3 D R e s u l t s     */
  /**********************************/
 public void plot3DResults(int iGraph) throws OutOfRangeException {

	 
	  
  // create your PlotPanel (you can use it as a JPanel)
  Plot3DPanel plot = new Plot3DPanel();  
 
  
  // add a title // take care !!! the following method (addPlotable) is incompatible with Plot3DPanel !!
  //BaseLabel title = new BaseLabel(((DataToPlot3D)(graphic3DVector.get(iGraph))).getTitle(), Color.RED, 0.5, 1.1);
  //title.setFont(new Font("Courier", Font.BOLD, 20));
  //plot.addPlotable(title);

  

  
  // define the legend position
  plot.addLegend("SOUTH");
  
  
  // define Labels
  String xLabel = ((DataToPlot3D)(graphic3DVector.get(iGraph))).getLabelX(); 
  String yLabel = ((DataToPlot3D)(graphic3DVector.get(iGraph))).getLabelY();  
  String zLabel = ((DataToPlot3D)(graphic3DVector.get(iGraph))).getLabelZ();  

  ((Plot3DPanel)(plot)).setAxisLabels(xLabel, yLabel, zLabel);
  
  
  // add n line plots to the PlotPanel for graph iGraph
	    
	    DataToPlot3D   dtp = (DataToPlot3D)(graphic3DVector.get(iGraph));
	    
	    plot.addGridPlot(dtp.getLegend(), dtp.getValueX(), dtp.getValueY(), dtp.getValueZ() );

	    
	    
	   // wrap Plot3DPanel and its title into a TitledPane
	   TitledPane titledPane = new TitledPane(dtp.getTitle(), plot);

	   // replace (or add if it is the first time) the plot at 0th position in the current object JComponentsResults with the new one 
	   if (this.isEmpty()) {this.add(titledPane);} else {this.set(0, titledPane);}

 	    
	    
 } // end of plot3DResults method
 
   
  
  /********************************************/
  /*  f o r G r a p h i c s B u t t o n s     */
  /*******************************************/
 public void forGraphicsButtons()  {
 
 
   // Display all the buttons to activate selected graphics
  // because user can change temperature range, ... 
  // the  last button index must be known
	 
	 
  // get  the panelBox
 	Box buttonsBox = Interface.getForGraphicsButtonsBox();
 	Dimension boxDimension = buttonsBox.getSize();
 	ButtonGroup groupe = new ButtonGroup();
 	
 	// get the first JRadioButton and set it to true
 	((DataToPlot)(graphicVector.get(Session.getCurrentSession().getCurrentGraphIndex()))).getRadioButton().setSelected(true);
 	
  // browse the graphic vector to get and put all the buttons in the box
 	JRadioButton currentJb;
    for (int i=0; i< graphicVector.size(); i++){
      currentJb =	((DataToPlot)(graphicVector.get(i))).getRadioButton();
   	  buttonsBox.add(currentJb);  // add all the radioButton in the Box (in central Pane, EAST)
   	  currentJb.addActionListener(this);
   	  buttonsBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.05))); // a space between each JRadioButton
   	  groupe.add(((DataToPlot)(graphicVector.get(i))).getRadioButton());  // group all the radioButton in a single Group
    }
   
  
  
} // end of forGraphicsButtons
  
  
 /********************************************/
 /*  f o r 3 D G r a p h i c s B u t t o n s     */
 /*******************************************/
public void for3DGraphicsButtons()  {

  // Display all the buttons to activate selected graphics
 // because user can change temperature range, ... 
 // the  last button index must be known
	 
	 
 // get  the panelBox
	Box buttonsBox = Interface.getForGraphicsButtonsBox();
	Dimension boxDimension = buttonsBox.getSize();
	ButtonGroup groupe = new ButtonGroup();
	
	// get the first JRadioButton and set it to true
	((DataToPlot3D)(graphic3DVector.get(Session.getCurrentSession().getCurrentGraphIndex()))).getRadioButton().setSelected(true);
	
 // browse the graphic vector to get and put all the buttons in the box
   JRadioButton currentJb;
   
  
   
   for (int i=0; i< graphic3DVector.size(); i++){
      currentJb =	((DataToPlot3D)(graphic3DVector.get(i))).getRadioButton();
  	  buttonsBox.add(currentJb);  // add all the radioButtons in the Box (in central Pane, EAST)
  	  currentJb.addActionListener(this);
  	  buttonsBox.add(Box.createVerticalStrut((int)(boxDimension.height*0.05))); // a space between each JRadioButton
  	  groupe.add(((DataToPlot3D)(graphic3DVector.get(i))).getRadioButton());  // group all the radioButtons in a single Group
   }
  
 
 
} // end of forGraphicsButtons
 
 
  /***********************************/
  /*  a c t i o n P e r f o r m e d  */
  /***********************************/
 
 public void actionPerformed(ActionEvent ev) {
   Object source = ev.getSource();

   
   
   JRadioButton currentJb;
   
   
   try {
	   for (int i=0; i< graphicVector.size(); i++){
   	   
	   currentJb =	((DataToPlot)(graphicVector.get(i))).getRadioButton(); 
   
       if (source==currentJb) { // detect the activated button in buttonsBox (EAST)
    	     
    	      plot2DResults(i); // build the plot with the ith graph
    	      Session.getCurrentSession().setCurrentGraphIndex(i);
    	      Session.getCurrentSession().refreshJResult();
        }// end of if (source==currentJb)
       
       
	   } // end of for on 2D graphics
	   
   
   
   for (int i=0; i< graphic3DVector.size(); i++){
	   
	   currentJb =	((DataToPlot3D)(graphic3DVector.get(i))).getRadioButton(); 
   
       if (source==currentJb) { // detect the activated button in buttonsBox (EAST)
    	     
    	      plot3DResults(i); // build the plot with the ith graph
    	      Session.getCurrentSession().setCurrentGraphIndex(i);
    	      Session.getCurrentSession().refreshJResult();
        }
       
       
   } // end of for on 3D Graphics if any
   
   
   } 
   catch (runTimeException error) {}
   catch (OutOfRangeException error) {}
                       
 } // end of actionperformed method
  
  
  } // end of JComponentsResults class
