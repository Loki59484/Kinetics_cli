

package kisthep.util;

public class TemperatureRange{

// P R O P E R T Y

private double tMin; // the temperature min in K
private double tMax; // the temperature max in K
private double tStep; // the temperature step in K


// C O N S T R U C T O R
public TemperatureRange(double tMin, double tMax, double tStep) {
    this.tMin = tMin;
    this.tMax = tMax;
    this.tStep = tStep;
}



// M E T H O D 

public double  getTMin() {return tMin;}

public double  getTMax() {return tMax;}

public double  getTStep() {return tStep;}

} // TemperatureRange
