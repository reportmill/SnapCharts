package snapcharts.app;
import snap.gfx.GFXEnv;
import snap.util.Prefs;

/**
 * The main class for SnapCharts app.
 */
public class App {


    /**
     * Standard main.
     */
    public static void main(String args[])
    {
        //DocPane docPane = new DocPane();
        //docPane.setWindowVisible(true);
        //ViewUtils.runLater(() -> docPane.loadSampleDoc());

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