
import kisthep.file.*;
import kisthep.util.*;
import java.io.*;
import java.util.*;
import java.lang.*;



public class StatisticalSystem extends ChemicalSystem implements ToEquilibrium, ReadWritable {

// z0Trans, z0Tot, s0Trans, s0Tot, g0Tot: computed at P0
// hT_h0 computed for an ideal gas (thus not dependent on pressure)
// h0 = H at 0 K
    protected double uTrans,uVib,uRot,uTot;  // internal energies contributions
    protected double hTrans,hVib,hRot,hTot;  // enthalpies contributions
    protected double gTrans,g0Trans, gVib,gRot,gElec;  // Gibbs free energies contributions

    protected double zElec, zTrans, z0Trans, zVib,zRot,zTot, z0Tot;  // partition functions
    protected double sTrans, s0Trans, sVib,sRot,sElec,sTot, s0Tot; // entropy contributions
    protected double hT_h0; // hT=uTot+PV ,  h0=up+ZPE
    protected double gTot, g0Tot; //Gibbs free energy
    protected double T; // temperature  in Kelvin
    protected double P; // pressure in Pascal


/* C O N S T R U C T O R  1 */

    public StatisticalSystem (String nature, double T) throws CancelException, IllegalDataException, IOException{

      super(nature);
	  this.T =T;
      this.P = Constants.P0;  



	  statistThermCompute();
	 
    	
    } // end of the CONSTRUCTOR


/* C O N S T R U C T O R  1 bis (to include pressure effect) */

    public StatisticalSystem (String nature, double T, double P) throws CancelException, IllegalDataException, IOException{

      super(nature);
	  this.T =T;
      this.P =P;  
	  statistThermCompute();
	 
    	
    } // end of the CONSTRUCTOR1 bis
    
// Constructors to accept command line inputs
    //without Pressure
    public StatisticalSystem (String nature, double T,File clinpFile) throws CancelException, IllegalDataException, IOException{

      super(nature,clinpFile);
	  this.T =T;
      this.P = Constants.P0;  



	  statistThermCompute();
	 
    	
    } // end of the CONSTRUCTOR
    //With Pressure
    public StatisticalSystem (String nature, double T, double P,File clinpFile) throws CancelException, IllegalDataException, IOException{

      super(nature,clinpFile);
	  this.T =T;
      this.P =P;  
	  statistThermCompute();
	 
    	
    } // 
   
 
 
/* C O N S T R U C T O R  2 (to read from a reactionpath file)*/

    public StatisticalSystem (String nature, double T, ActionOnFileRead read) throws IllegalDataException, IOException {

	super(nature, read);
	this.T =T;  

      this.P=Constants.P0;

	statistThermCompute();
	 
    	
    } // end of the CONSTRUCTOR2
    

/* C O N S T R U C T O R  3 (to reload) from a Session file*/

public StatisticalSystem (){
}
 
    
    
/* M E T H O D S */   

/*-------------------------------------------------------------------*/
/*         S E T       T E M P E R A T U R E                         */
/*-------------------------------------------------------------------*/
 
    
    public void setTemperature(double T) throws IllegalDataException {

	this.T = T;
	statistThermCompute();
    }



/*-------------------------------------------------------------------*/
/*         S E T       P R E S S U R E                               */
/*-------------------------------------------------------------------*/
 
 
    public void setPressure(double P) throws IllegalDataException {

	this.P = P;
	statistThermCompute();


    } // end of setPressure


   
    



   	/****************************************************************/       
        /* 2 methods (read, write) to save on file (or load from file) */ 
	/* the current object                                        */
    	/***********************************************************/

 /*********************************************/
/* s a v e                                   */ 
/********************************************/  


    public void save(ActionOnFileWrite write) throws IOException {
      super.save(write);
      write.oneString("uTrans :");
      write.oneDouble(uTrans);
      write.oneString("uVib :");
      write.oneDouble(uVib);
      write.oneString("uRot :");
      write.oneDouble(uRot);
      write.oneString("uTot :");
      write.oneDouble(uTot);
      write.oneString("hTrans :");
      write.oneDouble(hTrans);
      write.oneString("hVib :");
      write.oneDouble(hVib);
      write.oneString("hRot :");
      write.oneDouble(hRot);
      write.oneString("hTot :");
      write.oneDouble(hTot);
     
      write.oneString("gTrans :");
      write.oneDouble(gTrans);
      write.oneString("g0Trans :");
      write.oneDouble(g0Trans);
      write.oneString("gVib :");
      write.oneDouble(gVib);
      write.oneString("gRot :");
      write.oneDouble(gRot);    
      write.oneString("gElec :");
      write.oneDouble(gElec);
      write.oneString("zTrans :");
      write.oneDouble(zTrans);
      write.oneString("z0Trans :");
      write.oneDouble(z0Trans);
      write.oneString("zVib :");
      write.oneDouble(zVib);
      write.oneString("zRot :");
      write.oneDouble(zRot);
      write.oneString("zElec :");
      write.oneDouble(zElec);
      write.oneString("zTot :");
      write.oneDouble(zTot);
      write.oneString("z0Tot :");
      write.oneDouble(z0Tot);
      write.oneString("sTrans :");
      write.oneDouble(sTrans);
      write.oneString("s0Trans :");
      write.oneDouble(s0Trans);
      write.oneString("sVib :");
      write.oneDouble(sVib);
      write.oneString("sRot :");
      write.oneDouble(sRot);
      write.oneString("sElec :");
      write.oneDouble(sElec);
      write.oneString("sTot :");
      write.oneDouble(sTot);
      write.oneString("s0Tot :");
      write.oneDouble(s0Tot);
      write.oneString("hT_h0 :");
      write.oneDouble(hT_h0);
      write.oneString("gTot :");
      write.oneDouble(gTot);
      write.oneString("g0Tot :");
      write.oneDouble(g0Tot);
      write.oneString("T :");
      write.oneDouble(T);


       								      								                                           
      write.oneString("P :");
      write.oneDouble(P);                                                       }
           								      								                                           

  
/*********************************************/
/* l o a d                                 */ 
/********************************************/  
                                            
					      
    public void load (ActionOnFileRead read) throws IOException, IllegalDataException {
      
      
      super.load(read);
      read.oneString();
      uTrans = read.oneDouble();
      read.oneString();
      uVib = read.oneDouble();
      read.oneString();
      uRot = read.oneDouble();
      read.oneString();
      uTot = read.oneDouble();
      read.oneString();
      hTrans = read.oneDouble();
      read.oneString();
      hVib = read.oneDouble();
      read.oneString();
      hRot = read.oneDouble();
      read.oneString();
      hTot = read.oneDouble();
      read.oneString();
      gTrans = read.oneDouble();
      read.oneString();
      g0Trans = read.oneDouble();
      read.oneString();
      gVib = read.oneDouble();
      read.oneString();
      gRot = read.oneDouble();
      read.oneString();
      gElec = read.oneDouble();
      read.oneString();  
      zTrans = read.oneDouble();
      read.oneString();
      z0Trans = read.oneDouble();
      read.oneString();
      zVib = read.oneDouble();
      read.oneString();
      zRot = read.oneDouble();
      read.oneString();
      zElec = read.oneDouble();
      read.oneString();
      zTot = read.oneDouble();
      read.oneString();
      z0Tot = read.oneDouble();
      read.oneString();
      sTrans = read.oneDouble();
      read.oneString();
      s0Trans = read.oneDouble();
      read.oneString();
      sVib = read.oneDouble();
      read.oneString();
      sRot = read.oneDouble();
      read.oneString();
      sElec = read.oneDouble();
      read.oneString();
      sTot = read.oneDouble();
      read.oneString();
      s0Tot = read.oneDouble();
      read.oneString();
      hT_h0 = read.oneDouble();
      read.oneString();
      gTot = read.oneDouble();
      read.oneString();
      g0Tot = read.oneDouble();
      read.oneString();
      T = read.oneDouble(); 


      read.oneString();
      P = read.oneDouble(); 
      
                                    }// end of load


/* ------------------------------------------------------------------
I N T E R N A L      E N E R G I E S       C O N T R I B U T I O N S (ONLY THERMAL CONTRIBUTIONS)
---------------------------------------------------------------------*/
/*-------------------------------------------------------------------*/
/*       C O M P U T E        U T R A N S     (it is independent of P by definition )  */
/*-------------------------------------------------------------------*/

public void computeUtrans() {


	uTrans = 1.5 * Constants.R * T ;

	
}//end of computeUtrans() method



/*-------------------------------------------------------------------*/
/*            C O M P U T E     U V I B  (it is independent of P by definition ) */                    
/*-------------------------------------------------------------------*/

public void computeUvib() throws IllegalDataException {
	// without ZPE contribution
	
	// local variables
	int arraySize;
	double currentFrequency ; // in K !! 

	// variables for HRDS correction
	// taken from Richard B. McClurg, Richard C. Flagan and William A. Goddard, J. Chem. Phys. 106 (16), 22 April 1997
	double hrdsCorrection = 0;// in J/mol 
	double teta, r, tmp, i1, i0;

	
	
	// initialisation of variable
	uVib = 0.; // J/mol
	
	// compute
	if (atomic == false){
		
	     for (int iFreq=0; iFreq< vibFreq.length; iFreq++){ //loop for scanning all the frequencies (in K !!)
			
                   if ( vibFreq[iFreq].getImagPart() == 0.0)
		     { // We test the value of frequency to avoid imaginary frequencies
					
			        currentFrequency = vibFreq[iFreq].getRealPart();  // we get the real part of complex 
					uVib = uVib + (Constants.R * currentFrequency) / ( Math.exp(currentFrequency / T ) - 1 );
					
					// !! if current frequency mode is treated as a hindered rotor, a correction must be included
					if (hrdsBarrier[iFreq] != Constants.highEnergy) {
						
						teta = T/currentFrequency;//unit less
						r = hrdsBarrier[iFreq]/(currentFrequency*Constants.kb);//unit less
						tmp = r / (2*teta);
						if (tmp<=3.75) {
							i0 = Maths.bessel1(0, tmp);
							i1 = Maths.bessel1(1, tmp);
						}// use of bessel1 function
						else {
							i0 = Maths.bessel2(0, tmp);
							i1 = Maths.bessel2(1, tmp);
						}// use of bessel2 function
						
												
						hrdsCorrection = Constants.R*T*(-0.5 + tmp * (1 + i1/i0));// note that hrdsCorrection is converted to J/mol						
						uVib = uVib +hrdsCorrection;
						
						
					}// end of if (hrdsPeriodic[iFreq] != 0)
					
	             } 	
	       } // end of for
		
	} // end if atomic != true


	
	
} // End of computeUvib() method


/*-------------------------------------------------------------------*/
/*        C O M P U T E      U R O T  (it is independent of P by definition ) */                          
/*-------------------------------------------------------------------*/

public void computeUrot() {


	if (atomic == true){ // atomic Urot = 0.
	uRot = 0.;
	} //end if atomic  

	if (atomic == false){//compute Urot for an molecule

		if (linear == true){//The molecule is linear
		   uRot = Constants.R * T ;	
		}//End of linear 


		if (linear == false){//The molecule isn't linear
		uRot = 1.5 * Constants.R * T ;
		}//End of linear 

	}//End of atomic 

}//End of computeUrot() method


/*-------------------------------------------------------------------*/
/*          C O M P U T E        U T O T (it is independent of P by definition ) */                        
/*-------------------------------------------------------------------*/
// internal energies contributions

public void computeUtot() throws IllegalDataException {
	

	computeUtrans();
	
	computeUvib();

	computeUrot();

	uTot = uTrans + uVib + uRot; // without ZPE (it will be added separately)

}//End of computeUtot() method


/* ------------------------------------------------------------------
          E N T H A L P I E S       C O N T R I B U T I O N S (ONLY THERMAL CONTRIBUTIONS)
---------------------------------------------------------------------*/
/*-------------------------------------------------------------------*/
/*       C O M P U T E        H T R A N S (it is independent of P by definition )  */
/*-------------------------------------------------------------------*/

public void computeHtrans() {


	hTrans = getUTrans() + Constants.R * T ; // U+PV

	
}//end of computeHtrans() method


/*-------------------------------------------------------------------*/
/*            C O M P U T E     H V I B  (it is independent of P by definition ) */                    
/*-------------------------------------------------------------------*/

public void computeHvib() {
	
       hVib = getUVib();
} // End of computeHvib() method


/*-------------------------------------------------------------------*/
/*        C O M P U T E      H R O T  (it is independent of P by definition ) */                          
/*-------------------------------------------------------------------*/

public void computeHrot() {

       hRot = getURot();


}//End of computeHRot() method


/*-------------------------------------------------------------------*/
/*          C O M P U T E        H T O T (it is independent of P by definition ) */                        
/*-------------------------------------------------------------------*/
// internal energies contributions

public void computeHtot() {
	

	computeHtrans();
	
	computeHvib();

	computeHrot();

	hTot = hTrans + hVib + hRot; // idem as HT-H0K ! (without ZPE contribution)

}//End of computeHtot() method




/* ------------------------------------------------------------------
      P A R T I T I O N        F U N C T I O N S
---------------------------------------------------------------------*/
/*-------------------------------------------------------------------*/
/*        C O M P U T E       Z T R A N S (depends on P by definition) */
/*-------------------------------------------------------------------*/

public void computeZtrans() {




double number = 2*Math.PI*mass*(1.0E-03/Constants.NA)*Constants.kb*T/(Constants.h*Constants.h);
zTrans = Math.pow(number,1.5) *(Constants.R*T)/P;

//!!: zTrans is calculated for a molecule of mass "mass kg mol-1" in the molar volume
// of an ideal gas at the considered temperature and pressure
// since RT/P appears in the formula




}//End of computeZtrans() method


/*-------------------------------------------------------------------*/
/*        C O M P U T E       Z 0 T R A N S (does not depend on P by definition) */
/*-------------------------------------------------------------------*/

public void computeZ0trans() {




double number = 2*Math.PI*mass*(1.0E-03/Constants.NA)*Constants.kb*T/(Constants.h*Constants.h);
z0Trans = Math.pow(number,1.5) *(Constants.R*T)/Constants.P0;

//!!: z0Trans is calculated for a molecule of mass "mass kg mol-1" in the molar volume
// of an ideal gas at the considered temperature and pressure P0
//since RT/P appears in the formula


}//End of computeZ0trans() method


/*-------------------------------------------------------------------*/
/*          C O M P U T E      Z V I B                               */
/*-------------------------------------------------------------------*/

public void computeZvib() throws IllegalDataException {

        double currentFrequency;
	
	zVib = 1.0 ;
	
	// variables for HRDS correction
	// taken from Richard B. McClurg, Richard C. Flagan and William A. Goddard, J. Chem. Phys. 106 (16), 22 April 1997
	double hrdsCorrection = 0;
	double teta, r, tmp, i1, i0;


	//compute
	if (atomic == false){
		for (int iFreq = 0; iFreq < vibFreq.length; iFreq++){//we scan all the frequencies
		
			 // We test the value of frequencie to avoid imaginary frequencies
			if (vibFreq[iFreq].getImagPart() == 0.){
		     
		      
			    currentFrequency = vibFreq[iFreq].getRealPart();
			    // zVib is computed from the V=0 (vib ground state) rather than from the bottom of the potential energy curve
			    // thus, take care, the ZPE part has to be added next for the energy calculation
			    // In the expressions for heat capacity and entropy, this factor disappears since you differentiate with respect to temperature (T). 
			    zVib = zVib * ( 1 /( 1 - Math.exp( - currentFrequency / T )  )   );	
				
			    
				// !! if current frequency mode is treated as a hindered rotor, a correction must be included in partition function
				if (hrdsBarrier[iFreq] != Constants.highEnergy) {
					
					teta = T/currentFrequency;
					r = hrdsBarrier[iFreq]/(currentFrequency*Constants.kb);
					tmp = r / (2*teta);
					if (tmp<=3.75) {
						i0 = Maths.bessel1(0, tmp);
						
					}// use of bessel1 function
					else {
						i0 = Maths.bessel2(0, tmp);
						
					}// use of bessel2 function
					
					hrdsCorrection = Math.sqrt(Math.PI*r/teta)*Math.exp(-tmp)*i0; // exclusive ZPE correction
					zVib = zVib * hrdsCorrection;
					
					
				}// end of if (hrdsPeriodic[iFreq] != 0)

			    
			    
	
			}
		}//end of for 
		
		
	} //end of if atomic 

} //End of computeZvib() method 

/*-------------------------------------------------------------------*/
/*        C O M P U T E       Z R O T      without symmetry number  */
/*-------------------------------------------------------------------*/


public void computeZrotWithoutSymmetryNumber() {

        
	zRot = 1.;


	if (atomic == true){ // atomic zRot = 1.
	zRot = 1.;
	} //end if atomic 

	if (atomic == false){//to compute Urot for a molecule
		

		if (linear == true){//The molecule is linear

                   // converts from Amu.bohr^2 -> Kg.m^2
                   double inertia1 = inertia[0]*Constants.convertAmuToKg* Math.pow(Constants.a0,2);    
                   double teta = Math.pow(Constants.h/Math.PI,2)/(8*Constants.kb*inertia1);
                  zRot =  T/(teta);

          			
		}//End of linear 


		if (linear == false){//The molecule isn't linear
                 // converts from Amu.bohr^2 -> Kg.m^2
                 double inertia1 = inertia[0]*Constants.convertAmuToKg* Math.pow(Constants.a0,2);    
                 double inertia2 = inertia[1]*Constants.convertAmuToKg* Math.pow(Constants.a0,2);    
                 double inertia3 = inertia[2]*Constants.convertAmuToKg* Math.pow(Constants.a0,2);    

                 double teta1 = Math.pow(Constants.h/Math.PI,2)/(8*Constants.kb*inertia1);
                 double teta2 = Math.pow(Constants.h/Math.PI,2)/(8*Constants.kb*inertia2);
                 double teta3 = Math.pow(Constants.h/Math.PI,2)/(8*Constants.kb*inertia3);
                 zRot = Math.pow(Math.PI/(teta1*teta2*teta3),0.5)*Math.pow(T,1.5);
 
		}//End of linear 

	}//End of atomic 

}//end of computezRot() method  without symmetry number


/*-------------------------------------------------------------------*/
/*        C O M P U T E       Z R O T                                */
/*-------------------------------------------------------------------*/

public void computeZrot() {

 
                 computeZrotWithoutSymmetryNumber();			
                 
		 if  (atomic == false){
		 zRot = zRot / symmetryNumber;}
 
}//end of computezRot() method 


/*-------------------------------------------------------------------*/
/*         C O M P U T E        Z T O T                              */
/*-------------------------------------------------------------------*/

public void computeZtot()  throws IllegalDataException{


	computeZtrans();
	
	computeZvib();
	
	computeZrot();

	zElec = elecDegener;

	zTot = zTrans * zVib * zRot * zElec; // without "ZPE" contribution


}// End of method computeZtot()


/*-------------------------------------------------------------------*/
/*         C O M P U T E        Z 0 T O T                              */
/*-------------------------------------------------------------------*/

public void computeZ0tot() throws IllegalDataException {


	computeZ0trans();
	
	computeZvib();
	
	computeZrot();

	zElec = elecDegener;

	z0Tot = z0Trans * zVib * zRot * zElec;


}// End of method computeZ0tot()


/* ------------------------------------------------------------------
         E N T R O P Y          C O N T R I B U T I O N S
---------------------------------------------------------------------*/
/*-------------------------------------------------------------------*/
/*          C O M P U T E          S T R A N S (depends on P by definition) */
/*-------------------------------------------------------------------*/

public void computeStrans() {

	// ------------- warning: Math.log  is neperian log ------------


     sTrans = Constants.R*Math.log(zTrans/Constants.NA) + uTrans/T + Constants.R;
 




}// End of computeStrans method

/*-------------------------------------------------------------------*/
/*          C O M P U T E          S 0 T R A N S (does not depend on P by definition) */
/*-------------------------------------------------------------------*/

public void computeS0trans() {

	
     s0Trans = Constants.R*Math.log(z0Trans/Constants.NA) + uTrans/T + Constants.R;
 



}// End of computeS0trans method




/*-------------------------------------------------------------------*/
/*           C O M P U T E        S V I B                            */
/*-------------------------------------------------------------------*/

public void computeSvib() {

	// ------------- warning: Math.log  is napierian log ------------
	// ------------- uVib and zVib are without ZPE; anyway this factor 
	// --------------disappears since you differentiate with respect to temperature (T).

	// in the case of a hindered rotor, the appropriate treatment has been applied 
	// directly in zVib and uVib (without ZPE considerations).
	// thus nothing to do here (possible corrections are already taken into account in zVib as below described)
	// taken from Richard B. McClurg, Richard C. Flagan and William A. Goddard, J. Chem. Phys. 106 (16), 22 April 1997

	sVib = Constants.R * Math.log(zVib) + uVib / T ;


}//End of computeSvib() method


/*-------------------------------------------------------------------*/
/*           C O M P U T E       S R O T                             */
/*-------------------------------------------------------------------*/

public void computeSrot() {

	sRot = 0.;
	
	if (atomic == true){ //atomic Srot= 0.
	
		sRot = 0.;
	
	                    } //end if atomic

      else 

{

	sRot = Constants.R * Math.log(zRot) + uRot / T ;


} // end of else (linear or not linear but not atomic)
		





}//end of computeSrot() method


/*-------------------------------------------------------------------*/
/*             C O M P U T E       S E L E C                         */
/*-------------------------------------------------------------------*/

public void computeSelec() {


	sElec = Constants.R * Math.log(zElec);



}//end of computeSelec() method


/*-------------------------------------------------------------------*/
/*            C O M P U T E        S T O T                           */
/*-------------------------------------------------------------------*/

public void computeStot() {


	computeStrans();
	
	computeSvib();

	computeSrot();
	
	computeSelec();

	sTot = sTrans + sVib + sRot + sElec;

}// End of computeStot() method

/*-------------------------------------------------------------------*/
/*            C O M P U T E        S 0 T O T                           */
/*-------------------------------------------------------------------*/

public void computeS0tot() {


	computeS0trans();
	
	computeSvib();

	computeSrot();
	
	computeSelec();

	s0Tot = s0Trans + sVib + sRot + sElec;

}// End of computeS0tot() method



/* ------------------------------------------------------------------
                 C O M P U T E   Ht - H0
---------------------------------------------------------------------*/

public void computeHt_H0() {


	hT_h0 = uTrans + uVib + uRot + Constants.R * T ;



}//End of computeHt_H0() method



/* ------------------------------------------------------------------
G I B B S   F R E E   E N E R G Y        C O N T R I B U T I O N S
---------------------------------------------------------------------*/
/*-------------------------------------------------------------------*/
/*          C O M P U T E          G T R A N S (depends on P by definition) */
/*-------------------------------------------------------------------*/

public void computeGtrans() {

// ------------- warning: Math.log  is neperian log ------------


gTrans = hTrans -T*sTrans;


}// End of computeGtrans method

/*-------------------------------------------------------------------*/
/*          C O M P U T E          G 0 T R A N S (does not depend on P by definition) */
/*-------------------------------------------------------------------*/

public void computeG0trans() {


	g0Trans = hTrans -T*s0Trans;


}// End of computeG0trans method




/*-------------------------------------------------------------------*/
/*           C O M P U T E        S V I B                            */
/*-------------------------------------------------------------------*/

public void computeGvib() {

// ------------- warning: Math.log  is napierian log ------------
// ------------- uVib and zVib are without ZPE; anyway this factor 
// --------------disappears since you differentiate with respect to temperature (T).

	
	// in the case of a hindered rotor, the appropriate treatment has been applied 
	// directly in zVib and uVib (without ZPE considerations).
	// thus nothing to do here (possible corrections are already taken into account in gVib as below described)
	// taken from Richard B. McClurg, Richard C. Flagan and William A. Goddard, J. Chem. Phys. 106 (16), 22 April 1997

	// hVib only contains thermal contributions to H => we add ZPE
gVib = ZPE + hVib - T*sVib;


}//End of computeGvib() method


/*-------------------------------------------------------------------*/
/*           C O M P U T E       G R O T                             */
/*-------------------------------------------------------------------*/

public void computeGrot() {

	gRot = hRot - T*sRot;


}//end of computeGrot() method


/*-------------------------------------------------------------------*/
/*             C O M P U T E       G E L E C                         */
/*-------------------------------------------------------------------*/

public void computeGelec() {


	gElec = up - T*sElec;



}//end of computeSelec() method




/* ------------------------------------------------------------------
        G I B B S      F R E E      E N E R G Y  G
---------------------------------------------------------------------*/

public void computeGTot() {

    computeGtrans();
    
    computeGvib();
    
    computeGrot();
    
    computeGelec();
	gTot = up + ZPE + hT_h0 - T*sTot ;
      

}//End of computeGTot() method


/* ------------------------------------------------------------------
        G I B B S      F R E E      E N E R G Y  G0
---------------------------------------------------------------------*/

public void computeG0Tot() {

    computeG0trans();
    
    computeGvib();
    
    computeGrot();
    
    computeGelec();

	g0Tot = up + ZPE + hT_h0 - T*s0Tot ;
      

}//End of computeG0Tot() method




/*-------------------------------------------------------------------*/
/*     C O M P U T E     S T A T I S T          T H E R M            */
/*-------------------------------------------------------------------*/

public void statistThermCompute()  throws IllegalDataException{
	

// run the scaleVibFreq() method only
// if the system is not atomic
// because the scaleVibFreq() method calls the computeZPE method
// (no vibrational frequencies for an atom)
	
	/* !!!!
	 * Pay attention to call the following methods
	 * keeping the proposed order (U before, H; H before G ...)
	 * 
	 */
	
	// computeZPE(); included intoscaleVibFreq below !!
	if (atomic != true){
		scaleVibFreq();  // if the scaling factor has changed ...
	}

	computeUtot();// without ZPE (it will be added separately)
	
	computeHtot();
		
	computeZtot();
    computeZ0tot();

	computeStot();
    computeS0tot();

	computeHt_H0();

	computeGTot();
    computeG0Tot();    
    


}//End of statistThermCompute() method



                            
    /*******************/
   /*  g e t S T o t  */
   /*******************/

  public double getSTot () {
    return sTot;
                           }  // end of getSTot method                            
 
   /*******************/
   /*  g e t S 0 T o t  */
   /*******************/

  public double getS0Tot () {
    return s0Tot;
                           }  // end of getS0Tot method                            


     /*******************/
   /*  g e t h T o t  */
   /*******************/

  public double getHTot () { // ! pay attention: up and zpe are returned together with the thermal contribution
    return (hT_h0+up+ZPE);

                        }  // end of getHTot method

     /*******************/
   /*  g e t h 0 T o t  */  // !!!!! H does not depend on P for an ideal gas
   /*******************/   // !!!!!! in this version (13/11/03) H0Tot = HTot

  public double getH0Tot () {// ! pay attention: up and zpe are returned together with the thermal contribution
    return (hT_h0+up+ZPE);
                           }  // end of getH0Tot method



     /*******************/
   /*  g e t G T o t  */
   /*******************/

    public double getGTot() {return gTot;};   



     /*******************/
   /*  g e t G0 T o t  */
   /*******************/

  public double getG0Tot() {return g0Tot;};   


	                               
   /*************/
   /*  g e t T  */
   /*************/

  public double getT () {
    return T;
                           }  // end of getSTot method

 
    /*******************/
   /*  g e t Z E l e c  */
   /*******************/


  public double getZElec () {
    return zElec;
                           }  // end of getZElec method

    /*******************/
   /*  g e t Z V i b  */
   /*******************/


  public double getZVib () {
	  
    return zVib;
                           }  // end of getZVib method


    /*******************/
   /*  g e t Z R o t  */
   /*******************/


  public double getZRot () {
    return zRot;
                           }  // end of getZRot method


    /*******************/
   /*  g e t Z T r a n s   */
   /*******************/


  public double getZTrans () {
    return zTrans;
                           }  // end of getZTrans method

  /*******************/
   /*  g e t Z 0 T r a n s   */
   /*******************/


  public double getZ0Trans () {
    return z0Trans;
                           }  // end of getZ0Trans method



    /*******************/
   /*  g e t Z T o t  */
   /*******************/


  public double getZTot () {
    return zTot;
                           }  // end of getZTot method

   /*******************/
   /*  g e t Z 0 T o t  */
   /*******************/


  public double getZ0Tot () {
    return z0Tot;
                           }  // end of getZ0Tot method




  /*******************/
 /*  g e t U V i b  */
 /*******************/


public double getUVib () {
  return uVib;
                         }  // end of getUVib method


  /*******************/
 /*  g e t U R o t  */
 /*******************/


public double getURot () {
  return uRot;
                         }  // end of getURot method


  /*******************/
 /*  g e t U T r a n s   */
 /*******************/


public double getUTrans () {
  return uTrans;
                         }  // end of getUTrans method



  /*******************/
 /*  g e t U T o t  */
 /*******************/


public double getUTot () {
  return uTot;
                         }  // end of getUTot method

/*******************/
/*  g e t H V i b  */
/*******************/


public double getHVib () {
return hVib;
                       }  // end of getHVib method


/*******************/
/*  g e t H R o t  */
/*******************/


public double getHRot () {
return hRot;
                       }  // end of getHRot method


/*******************/
/*  g e t H T r a n s   */
/*******************/


public double getHTrans () {
return hTrans;
                       }  // end of getHTrans method




/*******************/
/*  g e t S V i b  */
/*******************/


public double getSVib () {
return sVib;
                       }  // end of getSVib method


/*******************/
/*  g e t S R o t  */
/*******************/


public double getSRot () {
return sRot;
                       }  // end of getSRot method


/*******************/
/*  g e t S T r a n s   */
/*******************/


public double getSTrans () {
return sTrans;
                       }  // end of getSTrans method


/*******************/
/*  g e t S0 T r a n s   */
/*******************/


public double getS0Trans () {
return s0Trans;
                       }  // end of getS0Trans method


/*******************/
/*  g e t S E l e c   */
/*******************/


public double getSElec () {
return sElec;
                       }  // end of getSElec method

/*******************/
/*  g e t G V i b  */
/*******************/


public double getGVib () {
return gVib;
                       }  // end of getGVib method


/*******************/
/*  g e t G R o t  */
/*******************/


public double getGRot () {
return gRot;
                       }  // end of getGRot method


/*******************/
/*  g e t G T r a n s   */
/*******************/


public double getGTrans () {
return gTrans;
                       }  // end of getGTrans method


/*******************/
/*  g e t G0 T r a n s   */
/*******************/


public double getG0Trans () {
return g0Trans;
                       }  // end of getG0Trans method


/*******************/
/*  g e t G E l e c   */
/*******************/


public double getGElec () {
return gElec;
                       }  // end of getGElec method



}// StatisticalSystem
