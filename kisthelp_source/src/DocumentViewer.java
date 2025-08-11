

               
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.io.IOException;

import kisthep.util.Constants;
import kisthep.util.runTimeException;
               
              // Class --> for reading a html documentation (html or text)
              public class DocumentViewer extends JFrame 
                                          implements HyperlinkListener, 
                                                     ActionListener
              {
                // Swing component 
                JEditorPane viewer       = new JEditorPane ();
                JButton forward; 
                JButton reverse; 
                
                
                // for reading the URL
                JTextField  urlTextField = new JTextField (75);
                
             // history of the visited links
                ArrayList<String> linkHistory = new ArrayList<String>();
                int linkCursor;
               
                public DocumentViewer (String fileAddress) throws runTimeException {

                
                	setSize (Constants.mainPaneHeight, Constants.mainPaneWidth);
                	setTitle("KiSThelP");
                	linkCursor=-1;// not visited link
 
                	//graphic interface constructor               	                
                	JPanel inputPanel = new JPanel (new FlowLayout ());
                	JLabel label = new JLabel ("URL : ");    
                
                // button for history of links
                	reverse = new JButton("<HTML>&larr;</HTML>");
                	forward = new JButton("<HTML>&rarr;</HTML>");
                	
                	JPanel navigation = new JPanel(new BorderLayout ());
                	
                	
                	navigation.add(reverse, BorderLayout.WEST);
                	navigation.add(forward, BorderLayout.EAST);
                
                	                	
                	inputPanel.add (navigation);
                	inputPanel.add (label);
                	inputPanel.add (urlTextField);
                	
                	// logo
            		ImageIcon icon;
            		URL addressLogo =   getClass().getResource("smallLogo.png");
            		if (addressLogo == null) {
            			String message = "Error in class DocumentViewer in constructor"+ Constants.newLine;
            			message = message +  "File smallLogo.png not found"+ Constants.newLine;
            			message = message +  "Please contact the authors"+ Constants.newLine;
            			JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                      
            		}
            		else {
            			icon = new ImageIcon(addressLogo); 
            			JButton smallLogo = new JButton("");
            			smallLogo.setIcon(icon);
            			inputPanel.add(Box.createGlue());
            			inputPanel.add (smallLogo);
            		}



                	// scroll    
                	JScrollPane scrollPane = new JScrollPane (viewer);
                	// get the component to the windows
                	getContentPane ().add (inputPanel, BorderLayout.NORTH);
                	getContentPane ().add (scrollPane, BorderLayout.CENTER);
                	

                	// non editable mode for clicking to into the windows (hypertext) 
                	viewer.setEditable (false);
                	// listener for clicing to the link 
                	viewer.addHyperlinkListener (this);
                	// listener 
                	urlTextField.addActionListener (this);
                	reverse.addActionListener (this);
                	forward.addActionListener (this);


                	
                	try{
                		URL address=new URL (fileAddress);
                		viewer.setPage (address); 
                		linkHistory.add(address.toString());
                		linkCursor=linkCursor+1;
                		
                		
                	} catch (IOException ex) { 

                		String message = "Error in class DocumentViewer"+ Constants.newLine;
                		message = message +  "Cannot access file :  " + fileAddress+ Constants.newLine;
                		JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                  
                		throw new runTimeException();

                	}// end catch

                }// end of constructor
               
		//this method is called when the user clicks into the hyper text
                public void hyperlinkUpdate (HyperlinkEvent event) 
                {
                  if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                  {
                	    //cut the linkHistory from where the linkCursor is
                	  
                	    linkHistoryRemoveRange(linkCursor+1, linkHistory.size()-1);               	    
                	    linkHistory.add(event.getURL ().toString ());               	    
                	    linkCursor=linkCursor+1;
                	                  	    
                	    urlTextField.setText (event.getURL ().toString ());
                    if (event instanceof HTMLFrameHyperlinkEvent) 
                    {
                      // HTML Frame consideration
                      HTMLDocument doc = (HTMLDocument)viewer.getDocument ();
                      doc.processHTMLFrameHyperlinkEvent (
                                     (HTMLFrameHyperlinkEvent)event);
                    }
                    else
                      // load the page
                      loadPage (urlTextField.getText ());
                  }
                }
               
                public void actionPerformed (ActionEvent event)
                {                	                	
                	Object source = event.getSource();
                	if (source ==  reverse) {
                		
                		if (linkCursor>=1) {
                		linkCursor=linkCursor-1; 
                		
                		loadPage (linkHistory.get(linkCursor));
                		}// end of if linkCursor
                		
                	}
                	
                	if (source ==  forward) {
                		
                		
                		if (linkCursor<=(linkHistory.size()-2)) {
                		linkCursor=linkCursor+1;  
                		
                		loadPage (linkHistory.get(linkCursor));
                		}// end of if linkCursor               		
                		
                	}
               	
                	if (source == urlTextField) {
                	  
                  loadPage (urlTextField.getText ());
                
                	}
                	
                	}// end of actionPerformed
               
                
                public void  linkHistoryRemoveRange(int from, int to)
                //  remove elements of the ArrayList linkHistory from fromIndex (inclusive) to toIndex (inclusive)
                {
                	  if ( (from > 0) && ( from <= to)) {
                		  
                		  for (int iElement=from; iElement<=to; iElement++){
                			  linkHistory.remove(from);// since remove action shifts any subsequent elements to the left (subtracts one from their indices)
                			                           // we remove (from-to+1) times the element at position "from".
                		  }
                		  
                		  
                	  }
                	
                }// end of linkHistoryRemoveRang
                
                public void loadPage (String urlText)
                {
                  try 
                  {
                   viewer.setPage (new URL (urlText));                	  
                  } 
                  catch (IOException ex) 
                  { 
              		String message = "Error in class DocumentViewer"+ Constants.newLine;
              		message = message +  "Cannot access file :  " + urlText + Constants.newLine;
              		JOptionPane.showMessageDialog(null,message,Constants.kisthepMessage,JOptionPane.ERROR_MESSAGE);                  
                  }
                }
                
              }
