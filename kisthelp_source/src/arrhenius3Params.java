import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import java.util.*;

public class arrhenius3Params implements MultivariateVectorFunction {

private ArrayList<Double> x = new ArrayList<Double>();

public arrhenius3Params(double[] dataPoints) {
	
	for (int i=0; i<dataPoints.length; i++) {x.add(dataPoints[i]);}	
}



// function value will be called by LevenbergMarquardtOptimizer
public double[] value (double[] param) {
	
double[] values = new double[x.size()];

for (int i = 0; i < x.size(); ++i) {
   final double t = x.get(i);
   values[i] = param[0] + param[1]* Math.log(t) + param[2]/t; 
}
return values;

}// end of value function
}// end of class

