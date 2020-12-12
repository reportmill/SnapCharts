package snapcharts.apptools;

import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snapcharts.app.ChartSetPane;
import snapcharts.model.DocItemGroup;

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
        DocItemGroup docItem = csp.getDocItem();

        // Reset PortraitButton, LandscapeButton
        setViewValue("PortraitButton", docItem.isPortrait());
        setViewValue("LandscapeButton", !docItem.isPortrait());

        // Reset OneUpButton, TwoUpButton, ThreeUpButton, FourUpButton
        setViewValue("OneUpButton", docItem.getItemsPerPage()==1);
        setViewValue("TwoUpButton", docItem.getItemsPerPage()==2);
        setViewValue("ThreeUpButton", docItem.getItemsPerPage()==3);
        setViewValue("FourUpButton", docItem.getItemsPerPage()==4);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get doc
        ChartSetPane csp = _csPane;
        DocItemGroup docItem = csp.getDocItem();

        // Handle PortraitButton, LandscapeButton
        if(anEvent.equals("PortraitButton")) docItem.setPortrait(true);
        if(anEvent.equals("LandscapeButton")) docItem.setPortrait(false);

        // Handle OneUpButton, TwoUpButton, ThreeUpButton, FourUpButton
        if (anEvent.equals("OneUpButton")) {
            docItem.setItemsPerPage(1);
            docItem.setPortrait(false);
        }
        if (anEvent.equals("TwoUpButton")) {
            docItem.setItemsPerPage(2);
            docItem.setPortrait(true);
        }
        if (anEvent.equals("ThreeUpButton")) {
            docItem.setItemsPerPage(3);
            docItem.setPortrait(true);
        }
        if (anEvent.equals("FourUpButton")) {
            docItem.setItemsPerPage(4);
            docItem.setPortrait(false);
        }

    }
}