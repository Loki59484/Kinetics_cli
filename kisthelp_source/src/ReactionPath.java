
import javax.swing.*;

import kisthep.util.*;
import kisthep.file.*;
import java.util.*;
import java.io.*;

public class ReactionPath implements ToEquilibrium, ReadWritable {

    /* P R O P E R T I E S */

    private int pointNumber;
    private Vector trajectory; // a vector containing ReactionPathPoints only !
    private ReactionPathPoint ts; // the point with the reaction coordinate = 0.0

    private double T, P;

    /* C O N S T R U C T O R 1 */

    public ReactionPath(double T) throws CancelException, IOException, IllegalDataException {

        this.T = T;
        this.P = Constants.P0;

        trajectory = dataRead(); // the dataRead methods ensures the trajectory size will be positive (>0)

        pointNumber = trajectory.size();

        // could be interesting to order the path points in increasing order for example
        // (in view of BWK tunneling calculation)

        testCoherenceData(); // check that each point along the reaction path has the same number of
                             // frequencies ..

        ts = findTs(); // at first, once, to find the TS

        ts.setNature(ChemicalSystem.saddlePoint1);

    } // end of CONSTRUCTOR 1

    // Constructor 1 adaptation to accept commandline input file

    public ReactionPath(double T, File reactpathFile) throws CancelException, IOException, IllegalDataException {

        this.T = T;
        this.P = Constants.P0;

        trajectory = dataRead(reactpathFile); // the dataRead methods ensures the trajectory size will be positive (>0)

        pointNumber = trajectory.size();

        // could be interesting to order the path points in increasing order for example
        // (in view of BWK tunneling calculation)

        testCoherenceData(); // check that each point along the reaction path has the same number of
                             // frequencies ..

        ts = findTs(); // at first, once, to find the TS

        ts.setNature(ChemicalSystem.saddlePoint1);
    }

    /* C O N S T R U C T O R 2 */

    public ReactionPath(ActionOnFileRead read) throws IOException, IllegalDataException {

        load(read);

    } // end of the constructor 2

    /* M E T H O D S */

    /**************************************/
    /* t e s t C o h e r e n c e D a t a */ // TEST OF DATA COHERENCE
    /************************************/

    public void testCoherenceData() throws IllegalDataException {

        // check that every point of the reaction path has the same number
        // of frequencies, inertia moments, the same mass ...

        ReactionPathPoint currentPoint, firstPoint;

        firstPoint = (ReactionPathPoint) trajectory.get(0);
        int freqNb = firstPoint.getVibFreedomDegrees();
        double mass = firstPoint.getMass();
        int elecDegener = firstPoint.getElecDegener();
        double[] inertia = firstPoint.getInertia();

        for (int iPoint = 1; iPoint <= pointNumber - 1; iPoint++) {

            currentPoint = (ReactionPathPoint) trajectory.get(iPoint);

            // check that the number of frequencies for each point is the same

            if (currentPoint.getVibFreedomDegrees() != freqNb) {

                String message = "Error in class ReactionPath, in method testCoherenceData" + Constants.newLine;
                message = message + "while reading reactionpath information in .kinp file " + Constants.newLine;
                message = message + "path point 1" + " has " + freqNb + " frequencies" + Constants.newLine;
                message = message + "path point " + (iPoint + 1) + " has " + currentPoint.getVibFreedomDegrees()
                        + " frequencies ..." + Constants.newLine;
                message = message + "Should be the same" + Constants.newLine;
                JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
                throw new IllegalDataException();

            } // if end

            // check that the mass is the same
            if (currentPoint.getMass() != mass) {

                String message = "Error in class ReactionPath, in method testCoherenceData" + Constants.newLine;
                message = message + "while reading reactionpath information in .kinp file " + Constants.newLine;
                message = message + "path point 1 mass is:" + mass + Constants.newLine;
                message = message + "path point " + (iPoint + 1) + " mass is: " + currentPoint.getMass() + " ..."
                        + Constants.newLine;
                message = message + "Should be the same" + Constants.newLine;
                JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
                throw new IllegalDataException();

            } // if end

            // check that electronic degeneracy is the same
            if (currentPoint.getElecDegener() != elecDegener) {

                String message = "Error in class ReactionPath, in method testCoherenceData" + Constants.newLine;
                message = message + "while reading reactionpath information in .kinp file " + Constants.newLine;
                message = message + "path point 1 electronic degeneracy is: " + elecDegener + Constants.newLine;
                message = message + "path point " + (iPoint + 1) + " electronic degeneracy is: "
                        + currentPoint.getElecDegener() + " ..." + Constants.newLine;
                message = message + "Should be the same" + Constants.newLine;
                JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
                throw new IllegalDataException();

            } // if end

            // check that there is the same number of inertia moments
            if (currentPoint.getInertia().length != inertia.length) {

                String message = "Error in class ReactionPath, in method testCoherenceData" + Constants.newLine;
                message = message + "while reading reactionpath information in .kinp file " + Constants.newLine;
                message = message + "path point 1 inertia array length is:" + inertia.length + Constants.newLine;
                message = message + "path point " + (iPoint + 1) + " inertia array length is: "
                        + currentPoint.getInertia().length + " ..." + Constants.newLine;
                message = message + "Should be the same" + Constants.newLine;
                JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
                throw new IllegalDataException();

            } // if end

        } // for end

    } // end of testCohereneData method

    /*********************************************/
    /* the method finds and returns the transition state (i.e. the */
    /* point having its reaction coordinate = 0) */
    /*********************************************/

    public ReactionPathPoint findTs() throws IllegalDataException {

        ReactionPathPoint currentReactionPathPoint = null;
        boolean thereIsATransitionState = false;

        /* running over the trajectory vector to find the transition state */
        for (int iComponent = 0; iComponent <= trajectory.size() - 1; iComponent++) {
            currentReactionPathPoint = (ReactionPathPoint) trajectory.get(iComponent);
            if (currentReactionPathPoint.getReactionCoordinate() == 0.0) {
                thereIsATransitionState = true;

                break;
            }

        } // end of for

        if (thereIsATransitionState) {
            return currentReactionPathPoint;
        } else {

            String message = "Error in class ReactionPath, in method findTs" + Constants.newLine;
            message = message + "while attempting to find a transition state " + Constants.newLine;
            message = message + "no reaction coordinate = 0.0 found in the provided reaction path data"
                    + Constants.newLine;
            JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
            throw new IllegalDataException();
        } // if end

    } // end of findTs method

    /*********************************************/
    /* the method returns the trajectory */
    /*******************************************/

    public Vector getTrajectory() {
        return trajectory;
    }

    /*********************************************/
    /* the method returns the points number */
    /*******************************************/

    public int getPointNumber() {
        return pointNumber;
    }

    /******************************************************/
    /* the method returns the transition state (i.e. the */
    /* point having its reaction coordinate = 0) */
    /***************************************************/

    public ReactionPathPoint getTs() {
        return ts;
    }

    /*****************************************************/
    /* this method returns the maximum along the */
    /* free enthalpy reaction path (not necessarily the */
    /* point having its reaction coordinate = 0) */
    /**************************************************/

    public ReactionPathPoint getGMaximum() {

        ReactionPathPoint currentReactionPathPoint, maximum;

        /* running over the trajectory vector to find the GMaximum */
        maximum = (ReactionPathPoint) trajectory.firstElement();
        for (int iComponent = 1; iComponent <= trajectory.size() - 1; iComponent++) {
            currentReactionPathPoint = (ReactionPathPoint) trajectory.get(iComponent);
            if (currentReactionPathPoint.getGTot() > maximum.getGTot()) {
                maximum = currentReactionPathPoint;

            }
        } // end of for
        return maximum;

    } // end of getGMaximum method

    /**************************************************************/
    /* this method returns the maximum along the */
    /* STANDARD free enthalpy reaction path (not necessarily the */
    /* point having its reaction coordinate = 0) */
    /***********************************************************/

    public ReactionPathPoint getG0Maximum() {

        ReactionPathPoint currentReactionPathPoint, maximum;

        /* running over the trajectory vector to find the G0Maximum */
        maximum = (ReactionPathPoint) trajectory.firstElement();
        for (int iComponent = 1; iComponent <= trajectory.size() - 1; iComponent++) {
            currentReactionPathPoint = (ReactionPathPoint) trajectory.get(iComponent);
            if (currentReactionPathPoint.getG0Tot() > maximum.getG0Tot()) {
                maximum = currentReactionPathPoint;

            }
        } // end of for
        return maximum;

    } // end of getGMaximum method

    /*****************************************************/
    /* the method returns a vector containing the */
    /* information about each point of the reaction path */
    /*****************************************************/
    public Vector dataRead() throws IllegalDataException, IOException, CancelException {
        return dataRead(null);
    }

    public Vector dataRead(File temporyFileName) throws IllegalDataException, IOException, CancelException {

        Vector setOfPoints; // a vector containing each point of the Reaction Path
        double currentReactionCoordinate;
        String currentLine;
        ActionOnFileRead read;

        String question = Constants.askingFileString
                + Session.getCurrentSession().getFilesToBeRead().getFirstAndRemove();

        // ONLY a file of type "KISTHELP input" must be READ in this case:
        if (temporyFileName == null) {
            temporyFileName = KisthepDialog.requireExistingFilename(question,
                    new KisthepInputFileFilter(Constants.kInpFileType));
        }
        // pay attention: an absolute path is used below

        read = new ActionOnFileRead(temporyFileName.getAbsolutePath(), Constants.kInpFileType);
        /* the ReactionPath data on the file "filename has to be read */
        setOfPoints = new Vector();

        currentLine = read.oneString();
        if (currentLine == null) {
            String message = "Error in class ReactionPath, in method dataRead" + Constants.newLine;
            message = message + "while reading the first line of file " + read.getWorkFile().getName()
                    + Constants.newLine;
            message = message + "first line should be " + Keywords.startOfPointInputSection + Constants.newLine;
            JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
            throw new IllegalDataException();
        } // if end

        while (currentLine != null) {

            // each new reaction path point MUST start with POINT section !
            if (!currentLine.toUpperCase().startsWith(Keywords.startOfPointInputSection)) {
                String message = "Error in class ReactionPath, in method dataRead" + Constants.newLine;
                message = message + "while reading the StartOfPointInputSection keyword" + Constants.newLine;
                message = message + "currentline should be " + Keywords.startOfPointInputSection + Constants.newLine;
                message = message + "but is: " + currentLine + Constants.newLine;
                JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
                throw new IllegalDataException();
            }

            // read the reaction coordinate and next read all the data concerning this point

            currentLine = read.oneString(); // the reaction coordinate section MUST follow
            if (!currentLine.toUpperCase().startsWith(Keywords.startOfReactionCoordinateInputSection)) {
                String message = "Error in class ReactionPath, in method dataRead" + Constants.newLine;
                message = message + "while reading the reactionCoordinateInputSection keyword" + Constants.newLine;
                message = message + "currentline should be " + Keywords.startOfReactionCoordinateInputSection
                        + Constants.newLine;
                message = message + "but is: " + currentLine + Constants.newLine;
                JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
                throw new IllegalDataException();

            }

            currentReactionCoordinate = read.oneDouble();
            currentLine = read.oneString(); // MUST end with "END" pattern
            if (!currentLine.toUpperCase().startsWith(Keywords.endOfInputSection)) {

                String message = "Error in class ReactionPath, in method dataRead" + Constants.newLine;
                message = message + "while reading the endOfInputSection keyword following the reaction coordinate"
                        + Constants.newLine;
                message = message + "currentline should be " + Keywords.endOfInputSection + Constants.newLine;
                message = message + "but is: " + currentLine + Constants.newLine;
                JOptionPane.showMessageDialog(null, message, Constants.kisthepMessage, JOptionPane.ERROR_MESSAGE);
                throw new IllegalDataException();
            }

            // read the rest of data for the determined type of point
            if (currentReactionCoordinate == 0.0) {
                setOfPoints.add(new ReactionPathPoint(ChemicalSystem.saddlePoint1, T, read, currentReactionCoordinate));
            } // if end
            else {
                setOfPoints.add(new ReactionPathPoint(ChemicalSystem.pathPoint, T, read, currentReactionCoordinate));
            } // else end

            currentLine = read.oneString();

        }

        read.end(); // stops the read action

        Session.getCurrentSession().addFilenameUsed(temporyFileName.getName());

        return setOfPoints;

    } // end of dataRead method

    /*********************************************/
    /* this methods changes the temperature T */
    /*                                           */
    /*********************************************/
    public void setTemperature(double T) throws IllegalDataException {

        ReactionPathPoint currentReactionPathPoint;

        this.T = T;

        /* changes temperature of each point that belongs to the reaction path */
        /* and the temperature of the TS also ! */
        /*********************************************/

        for (int iComponent = 0; iComponent <= trajectory.size() - 1; iComponent++) {

            currentReactionPathPoint = (ReactionPathPoint) trajectory.get(iComponent);

            currentReactionPathPoint.setTemperature(T);
        } // end of for
        ts.setTemperature(T);

    } // end of setTemperature method

    /*********************************************/
    /* this methods changes the pressure P */
    /*                                           */
    /*********************************************/
    public void setPressure(double P) throws IllegalDataException {

        ReactionPathPoint currentReactionPathPoint;

        this.P = P;

        /* changes pressure of each point that belongs to the reaction path */
        /* and the pressure of the TS also ! */
        /*********************************************/

        for (int iComponent = 0; iComponent <= trajectory.size() - 1; iComponent++) {
            currentReactionPathPoint = (ReactionPathPoint) trajectory.get(iComponent);

            currentReactionPathPoint.setPressure(P);
        } // end of for
        ts.setPressure(P);

    } // end of setPressure method

    /****************************************************************/
    /* 2 methods (read, write) to save on file (or load from file) */
    /* the current object */
    /***********************************************************/

    /*********************************************/
    /* s a v e */
    /********************************************/

    public void save(ActionOnFileWrite write) throws IOException {
        ReactionPathPoint currentComponent;

        write.oneString("pointNumber :");
        write.oneInt(pointNumber);

        write.oneString("T :");
        write.oneDouble(T);

        write.oneString("P :");
        write.oneDouble(P);

        write.oneString("size of trajectory vector :");
        write.oneInt(trajectory.size());

        write.oneString("CLASSNAME " + "ReactionPathPoint");
        write.oneString("trajectory vector content :");
        for (int iComponent = 0; iComponent < trajectory.size(); iComponent++) {
            currentComponent = (ReactionPathPoint) trajectory.get(iComponent);
            currentComponent.save(write);
        } // end of for
        write.oneString("CLASSNAME " + ts.getClass().getName());
        ts.save(write);

    } // end of the save method

    /*********************************************/
    /* l o a d */
    /********************************************/

    public void load(ActionOnFileRead read) throws IOException, IllegalDataException {

        ReactionPathPoint currentObject;
        read.oneString();
        pointNumber = read.oneInt();

        read.oneString();
        T = read.oneDouble();

        read.oneString();
        P = read.oneDouble();

        read.oneString();
        trajectory = new Vector(read.oneInt());

        read.oneString();
        read.oneString();
        for (int iComponent = 0; iComponent < pointNumber; iComponent++) {
            currentObject = new ReactionPathPoint(read);
            trajectory.add(currentObject);
        } // end of for

        read.oneString();

        ts = new ReactionPathPoint(read);
    } // end of load method

}// ReactionPath
