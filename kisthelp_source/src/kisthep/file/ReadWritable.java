
package kisthep.file;
import kisthep.util.*;
import java.io.*;

public interface ReadWritable {
 	
// class devoted to other classes that will be saved 
// during a session => these classes (implementing ReadWritable interface) must have a save AND load method
// thus, if one add or modify a property  in one of class, it is
// easy to know if it is necessary to adapt the save or load method, because if this
// class implements the interface ReadWritable, you have to modify the load AND SAVE method
// in order to the new property to be saved or load to/from a session .kstp file

 	
    public void save(ActionOnFileWrite write) throws IOException;    
    public void load(ActionOnFileRead read) throws IOException, IllegalDataException;

    
}// ReadWritable
