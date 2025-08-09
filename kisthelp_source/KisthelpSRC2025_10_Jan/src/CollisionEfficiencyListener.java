

import javax.swing.*;
import java.awt.event.*;

import kisthep.util.IllegalDataException;
import kisthep.util.OutOfRangeException;
import kisthep.util.runTimeException;


public class  CollisionEfficiencyListener implements ActionListener {


protected DeactivationRateConstant deactivationRateConstant; // calculated using collision efficiency Bc

/*********************************************/
/* c o n s t r u c t o r                      */ 
/********************************************/  

public CollisionEfficiencyListener (DeactivationRateConstant deactivationRateConstant)
 {this.deactivationRateConstant = deactivationRateConstant; }

 /*********************************************/
/* a c t i o n P e r f o r m e d              */ 
/********************************************/  



public void actionPerformed(ActionEvent ev) {

// collision efficiency has changed !
// thus, get new collision efficiency and recompute rateconstant.
   JTextField txtCollisionEfficiency = (JTextField)ev.getSource(); // only for rrkm calc.
    
    try {
    deactivationRateConstant.setCollisionEfficiency(Double.parseDouble(txtCollisionEfficiency.getText()));
   // display the pane containing all the results
    Session.getCurrentSession().displayResults();
    
    }
    catch (runTimeException error) {}
    catch (IllegalDataException error) {}
    catch (OutOfRangeException error) {}
 
}// end of actionPerformed



} // end of class
