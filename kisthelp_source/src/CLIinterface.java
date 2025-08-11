import java.util.*;
import java.util.concurrent.Callable;
import java.lang.*;
import java.net.URL;
import javax.naming.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.FactoryConfigurationError;

import java.io.*;
import kisthep.util.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import kisthep.file.*;

public class CLIinterface {

    public final class Console {
        public static final Scanner SCANNER = new Scanner(System.in);

        private Console() {
        }
    }

    public static void guiLancher(){
    String path = null;

    // L E T 'S G O
    path = System.getProperty("user.dir");
    Kistep.setUserCurrentDirectory(path);
    Interface fenetre = new Interface();
    // and set icon image in place of the standard cup of coffee...
    // fenetre.setIconImage(Toolkit.getDefaultToolkit().getImage("bin/smallLogo.png"));

    fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    fenetre.setVisible(true);
    Kistep.currentInterface = fenetre;


    }

    public static class CommonOptions {

        @Option(names = { "-i",
                "-inputfile" }, description = "Enter a session file (.kstp) or Input file(.kinp)")
        private File inputfile;

        public File getInputfile() {
            return this.inputfile;
        }

        @Option(names = { "-o", "-outputfile" }, description = "Enter path for an output file")
        private File outputfile;

        public File getOutputfile() {
            return this.outputfile;
        }

        @Option(names = "-y", description = "Assumes yes for all yes/no questions.")
        private boolean Yestoall;

        public boolean getYestoall() {
            return this.Yestoall;
        }

        @Option(names = { "-T",
                "-Temperature" }, split = ",", description = "Sets Temperature values for a calculation. Expects input in the form: tMin, tMax, steps")
        private List<Double> Temperature;

        public List<Double> getTemp() {
            return this.Temperature;
        }

        @Option(names = { "-P",
                "-Pressure" }, split = ",", description = "Sets Pressure values for a calculation. Expects input in the form: pMin, pMax, steps")
        private List<Double> Pressure;

        public List<Double> getPressure() {
            return this.Pressure;
        }
        
        @Option(names = {"-sr","--show-results"}, description = "Displays results in KISTEP panel.")
        private boolean showResult;

        public boolean getShowresult() {
            return this.showResult;
        }

    }

    // SETUP SESSION
    @Command(name = "session", description = "Sets up a session. \"New\" creates a new session. \"Open\" opens a session from the session file")
    public static class StartSession implements Callable<Integer> {
        private Session workSession;

        public Session getsSession() {
            return this.workSession;
        }

        @Mixin
        private CommonOptions commonoptions;

        enum Sessiontype {
            New, Open
        }

        @Option(names = { "-t", "--type" }, description = "Session type: New or Open", defaultValue = "New")
        private Sessiontype sessiontype;

        public Sessiontype getSessiontype() {
            return this.sessiontype;
        }

        @Override
        public Integer call() {
            if (this.getSessiontype() == Sessiontype.Open) {
                if (commonoptions.getInputfile() == null) {
                    System.err.println("Error: A file path must be specified with --session=Open");
                    return 1; // Error
                } else {
                    System.out.println("Opening session from file :" + commonoptions.getInputfile().getAbsolutePath());
                }
            } else {
                System.out.println("Creating a new session");
                workSession = new Session();
            }
            return 0;
        }
    }

    // SETUP CALCULATION
    @Command(name = "calc", description = "Initiates the given type of calculation.", subcommands = {
            Calculations.calcRpath.class, Calculations.calcMolec.class })
    public static class Calculations implements Callable<Integer> {

        @Mixin
        private StartSession sessionoptions;

        @Override
        public Integer call() {
            System.err.println("Error: Please specify a calculation type (e.g., 'rpath').");
            new CommandLine(this).usage(System.err);
            return 1;
        }
        // REACTION PATH CALCULATION
        @Command(name = "Rpath", description = "Creates a reaction path for given files. Requires number of points to be read.",mixinStandardHelpOptions = true)
        public static class calcRpath implements Callable<Integer> {
            Session workSession = new Session();

            @Mixin
            private CommonOptions commonoptions;

            @Option(names = "--pts", required = true, description = "Please enter the number of points to be read (TST=1, VTST=n) including the TS itself ")
            private int nbPts;

            @Option(names = "--ircpoints", required = true, description = "Please enter an array of path points seperated by comma. Eg : -1.0,-0.9...,0.0,...0.9,1.0", split = ",")
            private ArrayList<Double> irc;

            @Option(names = "--kinpfiles", required = true, description = "Please enter path for .kinp files corresponding to the order of irc points provided.",split=",")
            private ArrayList<File> rawinputFiles;

            File temporaryFile, temporaryFile2;
            ActionOnFileRead readFromCurrentKinp;
            ActionOnFileWrite writeOnRPKinp;
            String temporaryFileName = "";
            String temporaryFileName2 = "";
            String currentLine;
            Boolean notFoundAGoodName, alreadyOpen;
            ArrayList<File> kinpFiles = new ArrayList<File>();

            public Integer call() {
                try {
                    System.out.println("Starting RPath Calculations for "+ nbPts +" points and "+rawinputFiles.size()+" Files");
                    System.exit(1);
                    if (nbPts <= 0) {
                        throw new CancelException();
                    } else if (nbPts == 1) {
                        irc.clear();
                        irc.add(0.0);// TST case
                    }
                    
                    for (File file : rawinputFiles) {
                        temporaryFile = ChemicalSystem.returnKinpFile(ChemicalSystem.pathPoint,file);
                        kinpFiles.add(temporaryFile);
                    }
                    temporaryFileName = commonoptions.getOutputfile().getAbsolutePath();

                    if (!temporaryFileName.endsWith(".kinp")) {
                        System.out.println(".kinp will be appended to the output file.");
                        temporaryFileName = temporaryFileName + ".kinp";
                    }

                    notFoundAGoodName = true;
                    while (notFoundAGoodName) {
                        System.out.println("Checking file in mem");
                        // check that this file is not already open (one of the kinp files in memory)
                        alreadyOpen = false;
                        for (int iFile = 0; iFile < nbPts; iFile++) {
                        System.out.println(iFile+" ");
                            
                            // get the absolute filename of the kinp file of the current path point
                            temporaryFile2 = (File) (kinpFiles.get(iFile));
                            temporaryFileName2 = temporaryFile2.getAbsolutePath();

                            // compare to the wanted filename for the reaction path:
                            if (temporaryFileName2.equals(temporaryFileName)) {
                                alreadyOpen = true;
                                System.err.println(
                                        "Sorry, this file is already in use in KiSThelP session. Please choose another name.");
                                break;
                            }
                        }
                        System.out.println("mem check complete");

                        if (alreadyOpen = false) {
                        System.out.println("instance check");

                            File f = new File(temporaryFileName);
                            if (f.exists()) {
                                if (!commonoptions.getYestoall()) {
                                    System.out.println(
                                            "This file Already exists and will be overwritten if you continue. Do you want to proceed? (Y/N) :");
                                    String input = Console.SCANNER.nextLine();
                                    if (input.equals("y") || input.equals("Y") || input.equals("yes")|| input.equals("YES")) {
                                        notFoundAGoodName = false;
                                    } else {
                                        System.out.println("Exiting with error.");
                                        return 1;
                                    }
                                }
                            }

                        }
                    }
                    System.out.println("Writing output file "+writeOnRPKinp.getWorkFile().getName());
                    writeOnRPKinp = null;
                    writeOnRPKinp = new ActionOnFileWrite(temporaryFileName);
                    for (int iFile = 0; iFile < nbPts; iFile++) {
                        // write the IRC Header
                        writeOnRPKinp.oneString(Keywords.startOfPointInputSection);
                        writeOnRPKinp.oneString(Keywords.startOfReactionCoordinateInputSection);
                        writeOnRPKinp.oneDouble(irc.get(iFile));
                        writeOnRPKinp.oneString(Keywords.endOfInputSection);

                        // now: directly copy the information from the kinp file to the reactionPath
                        // file
                        // write the kinp file content of the current point
                        temporaryFile = (File) (kinpFiles.get(iFile));
                        temporaryFileName = temporaryFile.getAbsolutePath();

                        readFromCurrentKinp = new ActionOnFileRead(temporaryFileName, Constants.kInpFileType);
                        currentLine = readFromCurrentKinp.oneString();

                        // reading loop
                        if (currentLine == null) {
                            System.err.println("Warning : the file " + temporaryFileName + " is empty ...");
                        } else {
                            do {
                                writeOnRPKinp.oneString(currentLine);
                                currentLine = readFromCurrentKinp.oneString();
                            } while (currentLine != null);
                        } // end of else
                          // write the IRC tail keyword
                        writeOnRPKinp.oneString(Keywords.endOfPointSection);

                        // remove the temporary current Kinp file from the Kisthelp list AND next erase
                        // the file
                        readFromCurrentKinp.end();
                        readFromCurrentKinp.getWorkFile().delete();

                    } // end of for (int iFile=0)
                    writeOnRPKinp.end();
                    System.out.println("file " + writeOnRPKinp.getWorkFile().getName() + " successfully written.");
                    return 0;
                } catch (

                CancelException error) {
                    new CommandLine(this).usage(System.err);
                    return 1;
                } catch (IOException error) {
                    new CommandLine(this).usage(System.err);
                    return 1;
                } catch (IllegalDataException error) {
                    new CommandLine(this).usage(System.err);
                    return 1;
                }

            }
        }// End of CalcRpath

        //THERMODYNAMIC CALCULATION FOR ATOMS/MOLECULES
        @Command(name = "Molec", description = "Saves a kinp file for the given atom/molecule containing its thermodynamic properties.",mixinStandardHelpOptions = true)
        public static class calcMolec implements Callable<Integer> {
            Session workSession = new Session();

            @Mixin
            private CommonOptions commonoptions;

            public Integer call() {
                try {
                    workSession.getFilesToBeRead().add("the system");
                    workSession.add(
                            new InertStatisticalSystem(workSession.getTemperatureMin(), workSession.getPressureMin(),commonoptions.getInputfile()));

                    // make available the results save menu if session is not empty AND !! if the
                    // single temperature or pressure range is invoked
                    if ((Session.getCurrentSession().getTemperatureMin() == Session.getCurrentSession()
                            .getTemperatureMax()) &&
                            (Session.getCurrentSession().getPressureMin() == Session.getCurrentSession()
                                    .getPressureMax())) {
                        //saveResults.setEnabled(true);
                    } else {
                        //saveResults.setEnabled(false);
                    }

                    //resetSetEnabled(true);
                    //saveInputs.setEnabled(true);
                    //reactPathBuild.setEnabled(false);

                    // display results
                    
                    if (commonoptions.getShowresult()){
                        guiLancher();
                    workSession.displayResults();
                    }
                    System.out.println(".kinp files written successfully.");
                    return 0;

                } catch (CancelException error) {
                    new CommandLine(this).usage(System.err);
                    return 1;
                } catch (IllegalDataException error) {
                    new CommandLine(this).usage(System.err);
                    return 1;
                } catch (IOException error) {
                    new CommandLine(this).usage(System.err);
                    return 1;
                } catch (runTimeException error) {
                    new CommandLine(this).usage(System.err);
                    return 1;
                } catch (OutOfRangeException error) {
                    new CommandLine(this).usage(System.err);
                    return 1;
                }

            }

        }

    }

}
