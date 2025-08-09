
package kisthep.util;

import java.util.*;
import java.io.*;
import kisthep.file.*;
import kisthep.util.*;
import javax.swing.*;
import java.awt.*;


public class Unity implements ReadWritable {


  
  // P R O P E R T I E S
  
private String currentTemperatureUnit;
private String currentPressureUnit;
private String temperatureSymbol;
private String pressureSymbol;



  // C O N S T R U C T O R  1

  public Unity() {

currentTemperatureUnit = "Kelvin";
temperatureSymbol = "K";
currentPressureUnit = "Bar";
pressureSymbol = "bar";

}

  // C O N S T R U C T O R  2

  public Unity(ActionOnFileRead read) throws IOException, IllegalDataException {
        
            load(read);

}


  // M E T H O D S


/*************************/
/*  changeTemperatureUnit */



public void changeTemperatureUnit(String temperatureUnit)
{
currentTemperatureUnit = temperatureUnit;

// K 
if (currentTemperatureUnit.equals("Kelvin") )
{
temperatureSymbol = "K";
} // end of if


// C 
if (currentTemperatureUnit.equals("Celsius") )
{
temperatureSymbol = "\u00B0 C" ;
} // end of if


// F 
if (currentTemperatureUnit.equals("Fahrenheit") )
{
temperatureSymbol = "\u00B0 F";
} // end of if

} // end of changeTemperatureUnit


/*************************/
/*  changePressureUnit */


public void changePressureUnit(String pressureUnit)
{
currentPressureUnit = pressureUnit;

// Pa 
if (currentPressureUnit.equals("Pascal") )
{
pressureSymbol = "Pa";
} // end of if

// Torr 
if (currentPressureUnit.equals("Torr") )
{
pressureSymbol = "Torr";
} // end of if

// Bar 
if (currentPressureUnit.equals("Bar") )
{
pressureSymbol = "bar" ;
} // end of if

// atm 
if (currentPressureUnit.equals("Atmosphere") )
{
pressureSymbol = "atm" ;
} // end of if
 
} // end of changePressureUnit




/*************************/
/*  convertToTemperatureUnit */


public double convertToTemperatureUnit(double temperature) {
// temperature must be given in K

// K <-- K
if (currentTemperatureUnit.equals("Kelvin") ) 
{
// temperature = temperature; 
} // end of if


// Celsius <-- K
if (currentTemperatureUnit.equals("Celsius") ) 
{
temperature = temperature - Constants.waterMeltingTemperature ; 
} // end of if


// Fahrenheit <-- K
if (currentTemperatureUnit.equals("Fahrenheit") ) 
{
temperature = (temperature - Constants.waterMeltingTemperature)*(180.0/100.0)+32.0;
} // end of if



return temperature; // temperature is now returned in the user unit !


} // end of convertToTemperatureUnit


/*************************/
/*  convertToPressureUnit */


public double convertToPressureUnit(double pressure) {
// pressure must be given in Pa and will be converted to currentUnit

// Pa <-- Pa
if (currentPressureUnit.equals("Pascal") ) 
{
// do nothing 
} // end of if

// Torr <-- Pa
if (currentPressureUnit.equals("Torr") ) 
{
pressure = pressure / Constants.convertTorrToPa;
} // end of if

// Bar <-- Pa
if (currentPressureUnit.equals("Bar") ) 
{
pressure = pressure /  Constants.convertBarToPa ; 
} // end of if

// atm <-- Pa
if (currentPressureUnit.equals("Atmosphere") ) 
{
pressure = pressure /  Constants.convertAtmToPa ; 
} // end of if



return pressure;

} // end of convertToPressureUnit



/*************************/
/*  convertToPressureISU */


public double convertToPressureISU(double pressure) {
// pressure under CurrentUnit converted to Pascal unit

// Pa --> Pa
if (currentPressureUnit.equals("Pascal") ) 
{
// do nothing 
} // end of if

// Torr --> Pa
if (currentPressureUnit.equals("Torr") ) 
{
pressure = pressure * Constants.convertTorrToPa;
} // end of if

// Bar --> Pa
if (currentPressureUnit.equals("Bar") ) 
{
pressure = pressure *  Constants.convertBarToPa ; 
} // end of if

// atm --> Pa
if (currentPressureUnit.equals("Atmosphere") ) 
{
pressure = pressure *  Constants.convertAtmToPa ; 
} // end of if



return pressure;

} // end of convertToPressureISU



/*************************/
/*  convertToTemperatureISU */


public double convertToTemperatureISU(double temperature) {
// the number "temperature" under currentUnit will be converted to Kelvin

// K --> K
if (currentTemperatureUnit.equals("Kelvin") ) 
{
// temperature = temperature; 
} // end of if


// degree C --> K
if (currentTemperatureUnit.equals("Celsius") ) 
{
temperature = temperature + Constants.waterMeltingTemperature ; 
} // end of if


// degree F --> K
if (currentTemperatureUnit.equals("Fahrenheit") ) 
{
temperature = (temperature-32)*(100.0/180.0)+ Constants.waterMeltingTemperature ; 
} // end of if



return temperature; // always returns a temperature in kelvin !


} // end of convertToTemperatureISU


/******************************/
/*  getCurrentTemperatureUnit */


public String getCurrentTemperatureUnit() {
  return currentTemperatureUnit;} // end of getCurrentTemperatureUnit


/******************************/
/*  getCurrentPressureUnit */


public String getCurrentPressureUnit() {
  return currentPressureUnit;} // end of getCurrentPressureUnit

/******************************/
/*  getTemperatureSymbol */


public String getTemperatureSymbol() {
  return temperatureSymbol;} // end of getTemperatureSymbol


/******************************/
/*  getPressureSymbol */


public String getPressureSymbol() {
  return pressureSymbol;} // end of getPressureSymbol



/******************************/
/*  save                   */

public void save(ActionOnFileWrite write) throws IOException {

   write.oneString("current Temperature Unit:");
   write.oneString(currentTemperatureUnit);
   write.oneString("Temperature Symbol:");
   write.oneString(temperatureSymbol);

   write.oneString("current Pressure Unit:");
   write.oneString(currentPressureUnit);
   write.oneString("Pressure Symbol:");
   write.oneString(pressureSymbol);

} // end of save

/******************************/
/*  load                   */

public void load(ActionOnFileRead read) throws IOException, IllegalDataException {
   read.oneString();
   currentTemperatureUnit = read.oneString();
   read.oneString();
   temperatureSymbol = read.oneString();

   read.oneString();
   currentPressureUnit = read.oneString();
   read.oneString();
   pressureSymbol = read.oneString();

} // end of load 



} // end of Unity class
