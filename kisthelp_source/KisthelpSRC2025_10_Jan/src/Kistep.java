



import kisthep.file.*;
import kisthep.util.*;

import java.awt.Toolkit;
import java.io.File ;

import javax.swing.JFrame;

public class Kistep {

public static String userCurrentDirectory ;
public static Interface currentInterface;

/***************************************************************/
/*        M A I N      M E T H O D  */
/***************************************************************/

  public static void main (String[] args) {

	//if one parameter when the user run kisthep

        String path = null;	


                // L E T 'S    G O
                path = System.getProperty("user.dir");
                setUserCurrentDirectory(path);
                Interface fenetre = new Interface();
    			// and set icon image in place of the standard cup of coffee...
    			//fenetre.setIconImage(Toolkit.getDefaultToolkit().getImage("bin/smallLogo.png"));

                fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                fenetre.setVisible(true);
                currentInterface = fenetre;





    
   }//End of main

/***************************************************************/
/*  g e t U s e r C u r r e n t D i r e c t o r y */
/***************************************************************/
public static String getUserCurrentDirectory(){


	return userCurrentDirectory ;

}
/***************************************************************/
/* s e t U s e r C u r r e n t D i r e c t o r y */
/***************************************************************/
public static void setUserCurrentDirectory(String path){

	userCurrentDirectory = path;


}






} //End of Kistep Classes

