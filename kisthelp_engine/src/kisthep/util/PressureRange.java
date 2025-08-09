
package kisthep.util;

public class PressureRange{

// P R O P E R T Y

private double pMin; // the pressure min
private double pMax; // the pressure max
private double pStep; // the pressure step


// C O N S T R U C T O R
public PressureRange(double pMin, double pMax, double pStep) {
    this.pMin = pMin;
    this.pMax = pMax;
    this.pStep = pStep;
}



// M E T H O D 

public double  getPMin() {return pMin;}

public double  getPMax() {return pMax;}

public double  getPStep() {return pStep;}

} // PressureRange
