package snapcharts.app;

import snap.util.SnapUtils;
import snapcharts.doc.Doc;

/**
 * A class to allow certain functionality to be pluggable depending on platform (desktop/web).
 */
public class AppEnv {

    // The shared instance
    private static AppEnv  _shared;

    /**
     * Open a chart doc in browser.
     */
    public void openChartsDocInBrowser(Doc aDoc)
    {
        System.out.println("AppEnv.openChartsDocInBrowser: Not supported");
    }

    /**
     * Returns the shared instance.
     */
    public static AppEnv getEnv()
    {
        // If already set, just return
        if (_shared!=null) return _shared;

        // Use generic for TEAVM, otherwise Swing version
        String cname = SnapUtils.getPlatform()==SnapUtils.Platform.TEAVM ? "snapcharts.app.AppEnv" : "snapcharts.app.AppEnvSwing";

        // Try to get/set class name instance
        try
        {
            return _shared = (AppEnv) Class.forName(cname).newInstance();
        }
        catch(Exception e)
        {
            System.err.println("AppEnv.getEnv: Can't set env: " + cname + ", " + e);
            return _shared = new AppEnv();
        }
    }

}
