
import javax.swing.*;

import java.awt.*;
import java.net.URL;

import kisthep.util.Constants;
import kisthep.util.runTimeException;

public class Logo extends JPanel {



  // P R O P E R T I E S
  	
  private ImageIcon im;
  private Dimension dimension;
  


  // C O N S T R U C T O R
  public Logo() {

	  URL address =   getClass().getResource("logo.gif");
	  if (address == null) {
			String message = "Error in class Logo in constructor"+ Constants.newLine;
			message = message +  "File logo.gif not found"+ Constants.newLine;
			message = message +  "Please contact the authors"+ Constants.newLine;
			JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                      
	  }
	  else {
		  im = new ImageIcon(address); 
	  }     

  }  // end of constructor				 

  // M E T H O D    
  

  
   /********************************/
   /* p a i n t C o m p o n e n t  */
   /********************************/


  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    dimension = getSize();
    g.drawImage(im.getImage(), 0, 0, dimension.width, dimension.height, this); 
                                         } // end of the paintComponent method
  

}

