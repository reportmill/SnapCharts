package snapcharts.app;
import snap.gfx.GFXEnv;
import snap.util.Prefs;
import snap.viewx.DialogBox;
import snap.web.WebURL;

/**
 * The main class for SnapCharts app.
 */
public class App {

    // Constants for App args
    public static final String APP_ARG_FETCH_CHART = "fetch";

    /**
     * Standard main.
     */
    public static void main(String args[])
    {
        //DocPane docPane = new DocPane();
        //docPane.setWindowVisible(true);
        //ViewUtils.runLater(() -> docPane.loadSampleDoc());

        if (args.length>1 && args[0].equals(APP_ARG_FETCH_CHART)) {
            String urls = "http://localhost:8008/" + args[1];
            DocPane dpane = new DocPane().open(urls);
            dpane.setWindowVisible(true);
            return;
        }

        WelcomePanel.getShared().setOnQuit(() -> quitApp());
        WelcomePanel.getShared().showPanel();
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