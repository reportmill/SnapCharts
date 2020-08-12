package snapcharts.apptools;

import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snapcharts.app.ChartSetPane;

/**
 * A class to manage UI to edit a ChartSetPane.
 */
public class ChartSetBasicTool extends ViewOwner {

    // The DocPane
    private ChartSetPane        _csPane;

    /**
     * Constructor.
     */
    public ChartSetBasicTool(ChartSetPane aCSP)
    {
        _csPane = aCSP;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get doc
        ChartSetPane csp = _csPane;

        // Reset PortraitButton, LandscapeButton
        setViewValue("PortraitButton", csp.isPortrait());
        setViewValue("LandscapeButton", !csp.isPortrait());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get doc
        ChartSetPane csp = _csPane;

        // Handle PortraitButton, LandscapeButton
        if(anEvent.equals("PortraitButton"))
            csp.setPortrait(true);
        if(anEvent.equals("LandscapeButton"))
            csp.setPortrait(false);
    }
}