import kisthep.file.*;
import kisthep.util.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.util.*;
import java.io.File;
import java.util.concurrent.Callable;
import javax.swing.JFrame;

@Command(name = "kisthep", mixinStandardHelpOptions = true, version = "CLI version 0.0", description = "Starts the Kistep Program", subcommands = {
    CLIinterface.StartSession.class, CLIinterface.Calculations.class })
public class Kistep implements Callable<Integer> {

  public static String userCurrentDirectory;
  public static Interface currentInterface;

  // INTERFACE COMMAND

  @Option(names = "--headless", description = "Starts KiSThelP in Command Line Mode")
  private boolean headless;

  public boolean isHeadless() {
    return this.headless;
  }

  @Override
  public Integer call() throws Exception {

    if (headless) {
      System.err.println("Error: Please specify a direction to proceed).");
            new CommandLine(this).usage(System.err);
            return 1;
    } else {
      launchgui();
      return 0;
    }
  }

  /***************************************************************/
  /* M A I N M E T H O D */
  /***************************************************************/

  public static void main(String[] args) {
    int exitcode = new CommandLine(new Kistep()).execute(args);
  }

  /***************************************************************/
  /* g e t U s e r C u r r e n t D i r e c t o r y */
  /***************************************************************/

  private void launchgui() {
    String path = null;

    // L E T 'S G O
    path = System.getProperty("user.dir");
    setUserCurrentDirectory(path);
    Interface fenetre = new Interface();
    // and set icon image in place of the standard cup of coffee...
    // fenetre.setIconImage(Toolkit.getDefaultToolkit().getImage("bin/smallLogo.png"));

    fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    fenetre.setVisible(true);
    currentInterface = fenetre;

  }

  public static String getUserCurrentDirectory() {
    return userCurrentDirectory;

  }

  /***************************************************************/
  /* s e t U s e r C u r r e n t D i r e c t o r y */
  /***************************************************************/
  public static void setUserCurrentDirectory(String path) {
    userCurrentDirectory = path;

  }

} // End of Kistep Classes
