package snapcharts.app;
import snap.gfx.GFXEnv;
import snap.util.Prefs;
import snap.util.SnapUtils;
import snap.web.WebURL;

import java.io.File;

/**
 * The main class for SnapCharts app.
 */
public class App {

    // A file to open on launch
    private static WebURL  _openOnLaunchURL;

    // Constants for App args
    public static final String OpenOnLaunchFilename_Arg = "OpenOnLaunchFilename";
    public static final String OpenOnLaunchString_Arg = "OpenOnLaunchString";
    public static final String APP_ARG_FETCH_CHART = "fetch";

    /**
     * Standard main.
     */
    public static void main(String args[])
    {
        //DocPane docPane = new DocPane();
        //docPane.setWindowVisible(true);
        //ViewUtils.runLater(() -> docPane.loadSampleDoc());

        if (args.length > 1)
            processArgs(args);

        // Handle AppLaunchURL
        if (_openOnLaunchURL != null) {
            DocPane dpane = new DocPane().open(_openOnLaunchURL);
            dpane.setWindowVisible(true);
        }

        // Otherwise just present WelcomePanel
        else {
            WelcomePanel.getShared().setOnQuit(() -> quitApp());
            WelcomePanel.getShared().showPanel();
        }
    }

    /**
     * Process args.
     */
    private static void processArgs(String[] args)
    {
        // Prologue
        String openOnLaunchFilename = "Untitled.charts";

        // Iterate over args
        for (int i=0; i<args.length; i++) {

            // Get loop arg
            String arg = args[i];
            System.out.println("Process arg: " + arg);

            // Handle OpenOnLaunchFilename_Arg
            if (arg.equals(OpenOnLaunchFilename_Arg)) {
                String str = i + 1 < args.length ? args[++i] : null;
                if (str == null) { System.err.println("Process OpenOnLaunchFilename: Missing filename"); }
                else openOnLaunchFilename = str;
                System.out.println("Process OpenOnLaunchFilename = " + openOnLaunchFilename);
            }

            // Handle OpenOnLaunchString_Arg
            else if (arg.equals(OpenOnLaunchString_Arg)) {
                String openOnLaunchStr = i+1 < args.length ? args[++i] : null;
                if (openOnLaunchStr == null) { System.err.println("Process OpenOnLaunchString: Missing string"); }

                // Since this is TeaVM, just write string to root file and
                else {

                    // Get bytes
                    byte[] openOnLaunchBytes = openOnLaunchStr.getBytes();
                    System.out.println("Process OpenOnLaunchString: Write bytes: " + openOnLaunchFilename + ", size=" + openOnLaunchBytes.length);

                    // Write to file
                    File openOnLaunchFile = new File('/' + openOnLaunchFilename);
                    SnapUtils.writeBytes(openOnLaunchBytes, openOnLaunchFile);

                    // Set OpenOnLaunchURL
                    _openOnLaunchURL = WebURL.getURL(openOnLaunchFile);
                }
            }

            // Handle
            else if (arg.equals(APP_ARG_FETCH_CHART)) {
                String urls = "http://localhost:8008/" + args[1];
                _openOnLaunchURL = WebURL.getURL(urls);
            }
        }
    }

    /**
     * Exits the application.
     */
    public static void quitApp()
    {
        Prefs.get().flush();
        GFXEnv.getEnv().exit(0);
    }
}