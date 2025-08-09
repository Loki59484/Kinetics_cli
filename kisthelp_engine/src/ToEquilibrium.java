import kisthep.util.IllegalDataException;
import kisthep.util.runTimeException;




// modif fred eric

interface ToEquilibrium {


    public void setTemperature(double T) throws runTimeException, IllegalDataException;
    public void setPressure(double P) throws runTimeException, IllegalDataException;
    

// fin modif fred eric

}// ToEquilibrium
