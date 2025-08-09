import javax.swing.*;
import java.awt.*;
import java.util.*;
import kisthep.util.*;


public class AboutKisthepDisplayPane extends JPanel {

//P R O P E R T I E S

   final String police = "SansSerif";
   final int titleStyle = Font.BOLD;
   final int textStyle = Font.PLAIN;
   int characterSize = 14;
   int xCoordinate=20, yCoordinate = 20;
   String[] displayArray = new String [48] ;


// C O N S T R U C T O R
public AboutKisthepDisplayPane(){


     setBackground(Color.white);
     repaint();
	  
} //End of constructor


//M E T H O D S
 public void paintComponent(Graphics g){
super.paintComponent(g);

Dimension pannelDim = getSize();


displayArray[0] = "K i S T h e l P,  Version 2025 -- 10th January 2025 -- " ;
characterSize = 16;
g.setFont(new Font(police, titleStyle,characterSize));
yCoordinate = (int) (pannelDim.height - 0.97 * pannelDim.height) ;
g.drawString(displayArray[0],xCoordinate,yCoordinate);

displayArray[1] = "Kinetic and Statistical Thermodynamical Package";
characterSize = 14;
g.setFont(new Font(police, titleStyle,characterSize));
yCoordinate = (int) (pannelDim.height - 0.95 * pannelDim.height) ;
g.drawString(displayArray[1],xCoordinate,yCoordinate);



displayArray[2] = "Authors : S. Canneaux(1), F. Bohr(2), E. Henon(3)" ;
characterSize = 16;
g.setFont(new Font(police, textStyle,characterSize));
yCoordinate = (int) (pannelDim.height - 0.92 * pannelDim.height) ;
g.drawString(displayArray[2],xCoordinate,yCoordinate);

displayArray[3]="See the License terms below (*)";
characterSize = 10;
g.setFont(new Font(police, textStyle,characterSize));
yCoordinate = (int) (pannelDim.height - 0.90 * pannelDim.height) ;
g.drawString(displayArray[3],xCoordinate,yCoordinate);

displayArray[4] = "with the use of Apache and JMathPlot libraries (**)";
yCoordinate = (int) (pannelDim.height - 0.88 * pannelDim.height) ;
g.drawString(displayArray[4],xCoordinate,yCoordinate);



displayArray[5]="";
characterSize = 12;
yCoordinate = (int) (pannelDim.height - 0.87 * pannelDim.height) ;
g.setFont(new Font(police, textStyle,characterSize));
g.drawString(displayArray[5],xCoordinate,yCoordinate);

displayArray[8] = "(3) Institut de Chimie Moleculaire de Reims - UMR CNRS 7312 - University of Reims Champagne-Ardenne" ;
displayArray[7] = "(2) Laboratoire d'Ingenierie et Sciences des Materiaux - University of Reims Champagne-Ardenne" ;
displayArray[6] = "(1) University of Sciences and Technologies of Lille" ;
displayArray[9] = "" ;
displayArray[10] = "Contacting KiSThelP: eric.henon@univ-reims.fr" ;
displayArray[11] = "Insitut de Chimie Moleculaire de Reims, UMR CNRS 7312, University of Reims Champagne-Ardenne" ;
displayArray[12] = "Moulin de la Housse,B.P. 1039 - 51687 REIMS Cedex 2 - FRANCE " ;
displayArray[13] = "------------------------------------------------------------------------------------------------------------------------------------" ;
displayArray[14] = "" ;
displayArray[15] = "* Copyright 2013  Sebastien Canneaux, Frederic Bohr and Eric Henon";
displayArray[16] = "The terms of the free license are as follows:" ;
displayArray[17] = "KiSThelP can be used, modified and copied for NON-COMMERCIAL purpose, provided that" +
                         " a reference to the authors appears in all copies and associated documentation.";
			 
displayArray[18] = "Anyone who receives a copy of KiSThelP, or a modified release of KiSThelP, or a part " +
                         " of KiSThelP, has the right to redistribute copies, modified or not of it,but without fee." +
			 " Clearly, there is no right to sell copies" ;
			  
displayArray[19] = "or modified copies, or part of KiSThelP program. "+
                         " In redistributed, modified or unmodified form, all the conditions are granted for only NON-COMMERCIAL purpose." +
			 " Redistributions of the source code, or parts";
displayArray[20] = "of the source codes, involves this copyright notice" +
                         " be put in the package, as well as the disclaimer (see below). " +
			 " Redistribution of KiSThelP (or part of it) in binary form must be accompanied by this" ;			 
displayArray[21] = "copyright notice "+
                         "in the documentation and/or other materials provided with the distribution." +
			 "There is no guarranty that KiSThelP software is bug-free. No consulting or maintenance services"+
			 " are guaranted or implied." ; 
 
displayArray[22] = "The authors are not liable for any damages suffered as a result of using, modifying or "+ 
			 "distributing this software or its derivatives." ;
displayArray[23] = "" ;


displayArray[24] = "** KiSThelP employs the Apache and JMathPlot libraries which are distributed under the following terms:";
displayArray[25] = "";
                        
displayArray[26] = " - Licensed under the Apache License, Version 2.0;" + "you may not use this file except in compliance with the License."; 
                         
			 
			
displayArray[27] = "You may obtain a copy of the License at: " + 
                         " http://www.apache.org/licenses/LICENSE-2.0";
			  
displayArray[28] = " Unless required by applicable law or agreed to in writing, software" +
                         " distributed under the License is distributed on an 'AS IS' BASIS, ";
displayArray[29] = " See the License for the specific language governing permissions and" +
                   " limitations under the License." ;
displayArray[30] = "" ;
displayArray[31] = "- Licensed under the BSD License" ;
displayArray[32] = "Copyright (C) 2003, Yann RICHET All rights reserved." ;
displayArray[33] = "Redistribution and use in source and binary forms, with or without modification," ;
displayArray[34] = "are permitted provided that the following conditions are met:" ;
displayArray[35] = " * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer." ;
displayArray[36] = " * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation" ;

displayArray[37] = "   and/or other materials provided with the distribution.";
displayArray[38] = " * Neither the name of JMATHTOOLS nor the names of its contributors may be used to endorse or promote products derived from this software without";
displayArray[39] = "   specific prior written permission.";    
displayArray[40] = ""; 
displayArray[41] = "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 'AS IS' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE"; 
displayArray[42] = "IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE"; 
displayArray[43] = "LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF"; 
displayArray[44] = "SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN"; 
displayArray[45] = "CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE "; 
displayArray[46] = "POSSIBILITY OF SUCH DAMAGE." ;
displayArray[47] = "------------------------------------------------------------------------------------------------------------------------------------" ;
		
characterSize = 12;
g.setFont(new Font(police, textStyle,characterSize));


   for (int iTextLine=6; iTextLine<displayArray.length; iTextLine++){
	yCoordinate = (int) (pannelDim.height - ((0.87 - (iTextLine -5 ) * 0.02)) * pannelDim.height) ;
        g.drawString(displayArray[iTextLine],xCoordinate,yCoordinate);
//        yCoordinate += fm.getDescent()+fm.getLeading()+fm.getHeight();
   } //End of  for (int iTextLine=0; iTextLine<displayArray



 }//End of public void paintComponent(Graphics g)











} //End of public class AboutKiSThelPDisplayPane {






