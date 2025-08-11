import java.util.ArrayList;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;


public class arrhenius3ParamsJacobian implements MultivariateMatrixFunction {

private ArrayList<Double> x = new ArrayList<Double>();

public arrhenius3ParamsJacobian(double[] dataPoints) {
	
	for (int i=0; i<dataPoints.length; i++) {x.add(dataPoints[i]);}	
}

//function value will be called by LevenbergMarquardtOptimizer
public double[][] value (double[] param) {
	

double[][] jacobian = new double[x.size()][3];

for (int i = 0; i < x.size(); ++i) {
   final double t = x.get(i);
   
   jacobian[i][0] = 1; 
   jacobian[i][1] = Math.log(t);
   jacobian[i][2] = 1/t;
   
   
}
return jacobian;

}// end of value function
}// end of class

