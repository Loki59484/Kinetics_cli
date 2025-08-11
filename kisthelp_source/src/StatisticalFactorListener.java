
import javax.swing.*;
import java.awt.event.*;

import kisthep.util.IllegalDataException;
import kisthep.util.OutOfRangeException;
import kisthep.util.runTimeException;



public class StatisticalFactorListener implements ActionListener {

ElementaryReaction reaction;// the reaction characterized by the statisticalfactor

public StatisticalFactorListener (ElementaryReaction reaction)
 {this.reaction = reaction; }

 /*********************************************/
/* a c t i o n P e r f o r m e d              */ 
/********************************************/  

/*( to Listen event concerning statisticalFactor textfield)*/

  public void actionPerformed(ActionEvent ev) {


// statistical factor has changed !
// thus, get new statisticalFactor and recompute rateconstant.
    JTextField txtStatisticalFactor = (JTextField)ev.getSource(); 
    
    try {
    reaction.setStatisticalFactor(Integer.parseInt(txtStatisticalFactor.getText())); // set new statistical factor and recompute rate constant
      
    // display the pane containing all the results
    Session.getCurrentSession().displayResults();
    
    }
    catch (runTimeException error) {}
    catch (IllegalDataException error) {}
    catch (OutOfRangeException error) {}
 
}// end of actionPerformed



} // end of class
