

package kisthep.file;

import kisthep.util.*;
import java.io.File;
import javax.swing.filechooser.*;


public class KisthepInputFileFilter extends FileFilter {
 
 
private String wantedFileType; // the desired file type; one of the file type constants values in Constants.java

    /** constructor */
    public KisthepInputFileFilter(String wantedFileType) {
        
    super();
    this.wantedFileType = wantedFileType;

   }// constructor end    


        // according to wantedFileType value, will accept all directories, all "out" files or all or kinp" files, or ...
 public boolean accept(File f) {
    	if (f.isDirectory()) {
    	   return true;
    	} // end of if

    String extension = KisthepFile.getExtension(f);
	String prefix     = KisthepFile.getPrefix(f.getName());
	
        if (extension != null) {
            
	    
	    if (this.wantedFileType.equals(Constants.kInpFileType)) {
	    	    if (extension.equals("kinp")) return true; return false;}
		    
		    
	    if (this.wantedFileType.equals(Constants.g09FileType)) {
	   	    if (extension.equals("out")) return true; return false;}
	    
	    if (this.wantedFileType.equals(Constants.nwcFileType)) {
	   	    if (extension.equals("out")||extension.equals("log")) return true; return false;}


	    if (this.wantedFileType.equals(Constants.gms2012FileType)) {
	   	    if (extension.equals("log")) return true; return false;}
	    
	    
        if (this.wantedFileType.equals(Constants.anyAllowedDataFile)) {
	   	    if (extension.equals("out") ||  extension.equals("log") ||  extension.equals("kinp")) return true; return false;}

        if (this.wantedFileType.equals(Constants.anyFile)) {
	   	    return true;}
		    
	    else return true; // unknown wantedFileType 
	    
	    
	                
        } // if end
	else {return false;}

    } // end of accept method
    
    // The description of this filter
    public String getDescription() {

	    if (this.wantedFileType.equals(Constants.kInpFileType) ) {return "*.kinp";}
	    if (this.wantedFileType.equals(Constants.g09FileType ) ) {return "*.out";}
	    if (this.wantedFileType.equals(Constants.nwcFileType ) ) {return "*.out";}
	    if (this.wantedFileType.equals(Constants.gms2012FileType ) ) {return "*.log";}	    
    	    if (this.wantedFileType.equals(Constants.anyAllowedDataFile ) ) {return "*.kinp, *.out";}

	    if (this.wantedFileType.equals(Constants.anyFile ) ) {return "*.*";}
	    else return "*.*";
	    
	    
	    
    }// end of method getDescription()
    
}// class end
