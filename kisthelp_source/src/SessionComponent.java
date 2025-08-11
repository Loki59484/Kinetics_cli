


// this interface allows for federating objects that can be added to an object of Class Session
// it does not need any methods.


import java.util.*;
import javax.swing.*;
import kisthep.file.*;
import kisthep.util.*;


public interface SessionComponent {


public String getTitle();

public void saveTxtResults(ActionOnFileWrite writeResults) throws runTimeException, IllegalDataException;

/*  D E P R E C A T E D
 * public void saveGraphicsResults(ActionOnFileWrite writeResults, TemperatureRange temperatureRange);
 */
                                
/*  D E P R E C A T E D
 * public void saveGraphicsResults(ActionOnFileWrite writeResults, PressureRange pressureRange);
 */
 
public Vector getTextResults() throws runTimeException, IllegalDataException; 
 
public Vector getGraphicsResults(TemperatureRange temperatureRange) throws runTimeException, IllegalDataException; 
 
public Vector getGraphicsResults(PressureRange pressureRange) throws runTimeException, IllegalDataException; 
 
public Vector get3DGraphicsResults(TemperatureRange temperatureRange, PressureRange pressureRange) throws runTimeException, IllegalDataException; 

 
public void fillInformationBox() throws runTimeException;   

}// SessionComponent
