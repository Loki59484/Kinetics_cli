

package kisthep.file;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;


public class KisthepSessionFileFilter extends FileFilter {
    
        // Accept all directories and all kinp files
 public boolean accept(File f) {
             if (f.isDirectory()) {
                return true;
             } // end of if

        String extension = KisthepFile.getExtension(f);
        if (extension != null) {
            if (extension.equals("kstp") )
 
                    {return true;}
            else {
                return false;
            }
        }

        return false;
    }
    
    // The description of this filter
    public String getDescription() {
        return "*.kstp";
    }
}
