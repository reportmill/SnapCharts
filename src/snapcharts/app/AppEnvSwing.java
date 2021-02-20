package snapcharts.app;
import snap.view.ViewUtils;
import snapcharts.appmisc.ChartClient;
import snapcharts.doc.Doc;

/**
 * An AppEnv subclass to support desktop functionality.
 */
public class AppEnvSwing extends AppEnv {

    // The ChartClient
    private static ChartClient  _chartClient;

    /**
     * Open a chart doc in browser.
     */
    @Override
    public void openChartsDocInBrowser(Doc aDoc)
    {
        // Get ChartClient
        if (_chartClient == null)
            _chartClient = new ChartClient();

        // Determine whether to use official version or local version
        boolean doLocal = ViewUtils.isAltDown();

        // Open In browser
        _chartClient.openChartDocInBrowser(aDoc, "Untitled.charts", doLocal);
    }
}
