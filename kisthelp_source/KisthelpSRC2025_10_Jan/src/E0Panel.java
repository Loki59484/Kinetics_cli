
import javax.swing.*;
import kisthep.util.*;

import java.awt.*;
import java.awt.event.*;


public class E0Panel extends JPanel implements ActionListener {

 
    
 RateConstantRRKM rateConstantRRKM; // the rate constant characterized by E0   
 JRadioButton e0Button;
 JTextField txtE0;
 BoxLayout layout;
 
/* C O N S T R U C T O R 1*/
    
    public E0Panel(RateConstantRRKM rateConstantRRKM) throws runTimeException {
        
    this.rateConstantRRKM = rateConstantRRKM;    
    
    // layoutManager
    layout = new BoxLayout(this, BoxLayout.Y_AXIS);
    setLayout(layout);
    
   // add the E0 label
    JLabel labelE0 = new JLabel("Critical Energy E0 (kcal/mol)");
    labelE0.setForeground(Color.black);
    add(labelE0);
    labelE0.setEnabled(true);
   
// add the collision Efficiency textfield

    String txt = Maths.format(rateConstantRRKM.getE0()*Constants.convertJToKCalPerMol,"00.00");
    txtE0 = new JTextField(txt,5); 
    add(txtE0);
    txtE0.setEnabled(true);  
    txtE0.addActionListener(this);

// add the radioButton

   if (rateConstantRRKM.getE0()!=rateConstantRRKM.getE0PESValue())
    {e0Button  = new JRadioButton("E0 PES value", false);
     e0Button.setBackground(new Color(218,223,224));
     e0Button.setEnabled(true);
     add(e0Button); 
     e0Button.addActionListener(this);
    }
  else{
    e0Button  = new JRadioButton("E0 PES value", true);
    e0Button.setBackground(new Color(218,223,224));
    e0Button.setEnabled(false);
    add(e0Button);
    e0Button.addActionListener(this);
   }

    
    } // end of constructor
  
    /******************************/
    /*  g e t t x t E 0          */
    /****************************/
// return the object contained in the E0Panel
    public JTextField getTxtE0() {

    // return the JTExtField object
        
        return txtE0;
        
    }// end of getE0
   
    
    
    
    public void actionPerformed(ActionEvent ev) {
        
        
    Object source = ev.getSource();
 
    
 // action comes from textfield   
    if (source==txtE0) {
        
        double E0 = Double.parseDouble(txtE0.getText() ) / Constants.convertJToKCalPerMol; 
        try {
        rateConstantRRKM.setE0(E0);

        
        Session.getCurrentSession().displayResults();
        e0Button.setEnabled(true);
        e0Button.setSelected(false);
        }
        catch (runTimeException err) {} 
        catch (IllegalDataException err) {}
        catch (OutOfRangeException error) {}
        
    } // end of if textfield
    

// action comes from radio button
     if (source==e0Button) {

try {
        rateConstantRRKM.setE0ToPESValue();
        double E0 = rateConstantRRKM.getE0();
        txtE0.setText(Maths.format(E0*Constants.convertJToKCalPerMol, "00.00"));
        Session.getCurrentSession().displayResults();
        e0Button.setEnabled(false);
        e0Button.setSelected(true);         
   
     }
catch (runTimeException err) {}  
catch (IllegalDataException err) {}
catch (OutOfRangeException error) {}
         
    } // end of if radiobutton
   
    
        
        
    } // end of actionperformed
}// BimolecularReaction
