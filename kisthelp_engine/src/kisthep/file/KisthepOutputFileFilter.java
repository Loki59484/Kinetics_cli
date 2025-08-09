package kisthep.file;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

import kisthep.util.Constants;

public class KisthepOutputFileFilter extends FileFilter {

	
	//PROPERTIES
	private String wantedFileType; // the desired file type; must be one of the file type constants values in Constants.java
	
	//CONSTRUCTOR
	public KisthepOutputFileFilter(String fileType) {
	    super();
	    this.wantedFileType = fileType;
		
	}// end of CONSTRUCTOR
	
	// Accept all directories and file type according to the property fileType
	public boolean accept(File f) {
		if (f.isDirectory()) {return true;} // end of if

		String extension = KisthepFile.getExtension(f);
		
		
		if (extension != null) {
			
		    if (this.wantedFileType.equals(Constants.csvOutputFile)) {
		   	    if (extension.equals("csv")) return true; return false;}  

		    if (this.wantedFileType.equals(Constants.kInpFileType)) {
		   	    if (extension.equals("kinp")) return true; return false;}  

	        if (this.wantedFileType.equals(Constants.anyFile)) {
		   	    return true;}
			    
		    else return true; // unknown wantedFileType 
		    
		}
		return false;
	}// end of accept method

	// The description of this filter
	public String getDescription() {
	    if (this.wantedFileType.equals(Constants.kInpFileType) ) return "*.kinp";
		if (this.wantedFileType.equals(Constants.csvOutputFile)) return "*.csv";
	    if (this.wantedFileType.equals(Constants.anyFile ) ) return "*.*";
	    else return "*.*";
	}// end of method getDescription()
}// class end
