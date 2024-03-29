package snapcharts.apptools;
import snap.util.FormatUtils;
import snap.util.StringUtils;
import snap.view.ToggleButton;
import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snapcharts.app.ChartSetPane;
import snapcharts.doc.DocItemGroup;
import snapcharts.charts.PageDisplay;

/**
 * A class to manage UI to edit a ChartSetPane.
 */
public class ChartSetBasicTool extends ViewOwner {

    // The DocPane
    private ChartSetPane _chartSetPane;

    /**
     * Constructor.
     */
    public ChartSetBasicTool(ChartSetPane aCSP)
    {
        _chartSetPane = aCSP;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get doc
        ChartSetPane csp = _chartSetPane;
        DocItemGroup docItem = csp.getDocItem();

        // Reset PortraitButton, LandscapeButton
        setViewValue("PortraitButton", docItem.isPortrait());
        setViewValue("LandscapeButton", !docItem.isPortrait());

        // Reset SingleLayoutButton, ContinuousLayoutButton
        setViewValue("SingleLayoutButton", docItem.getPageDisplay() == PageDisplay.SINGLE);
        setViewValue("ContinuousLayoutButton", docItem.getPageDisplay() == PageDisplay.CONTINUOUS);

        // Reset OneUpButton, TwoUpButton, ThreeUpButton, FourUpButton
        setViewValue("OneUpButton", docItem.getItemsPerPage()==1);
        setViewValue("TwoUpButton", docItem.getItemsPerPage()==2);
        setViewValue("ThreeUpButton", docItem.getItemsPerPage()==3);
        setViewValue("FourUpButton", docItem.getItemsPerPage()==4);
        setViewValue("SixUpButton", docItem.getItemsPerPage()==6);
        setViewValue("NineUpButton", docItem.getItemsPerPage()==9);

        // Reset ChartScaleButtons (ChartScaleButton0.5, 0.75, 1, 1.25, 1.5, 1.75, ChartScaleButton2)
        double chartScale = docItem.getChartScale();
        String chartScaleStr = FormatUtils.formatNum("#.##", chartScale);
        setViewText("ChartScaleTitleView", "Chart Scale - " + chartScaleStr + "x");
        if (chartScaleStr.startsWith(".")) chartScaleStr = '0' + chartScaleStr; // TeaVM?
        String chartScaleButtonStr = "ChartScaleButton" + chartScaleStr;
        ToggleButton chartScaleButton = getView(chartScaleButtonStr, ToggleButton.class);
        if (chartScaleButton!=null)
            chartScaleButton.setSelected(true);
        else getToggleGroup("toggle1").setSelected(null);

        // Disable ChartScaleButton0.5, ChartScaleButton.75 if too many charts on page
        setViewEnabled("ChartScaleButton0.5", docItem.getItemsPerPage()<4);
        setViewEnabled("ChartScaleButton0.75", docItem.getItemsPerPage()<6);
   }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get doc
        ChartSetPane csp = _chartSetPane;
        DocItemGroup docItem = csp.getDocItem();

        // Handle PortraitButton, LandscapeButton
        if (anEvent.equals("PortraitButton"))
            docItem.setPortrait(true);
        if (anEvent.equals("LandscapeButton"))
            docItem.setPortrait(false);

        // Handle SingleLayoutButton, ContinuousLayoutButton
        if(anEvent.equals("SingleLayoutButton"))
            docItem.setPageDisplay(PageDisplay.SINGLE);
        if(anEvent.equals("ContinuousLayoutButton"))
            docItem.setPageDisplay(PageDisplay.CONTINUOUS);

        // Handle OneUpButton, TwoUpButton, ThreeUpButton, FourUpButton
        if (anEvent.equals("OneUpButton"))
            docItem.setItemsPerPageAndMore(1);
        if (anEvent.equals("TwoUpButton"))
            docItem.setItemsPerPageAndMore(2);
        if (anEvent.equals("ThreeUpButton"))
            docItem.setItemsPerPageAndMore(3);
        if (anEvent.equals("FourUpButton"))
            docItem.setItemsPerPageAndMore(4);
        if (anEvent.equals("SixUpButton"))
            docItem.setItemsPerPageAndMore(6);
        if (anEvent.equals("NineUpButton"))
            docItem.setItemsPerPageAndMore(9);

        // Handle ChartScaleButtons (ChartScaleButton.5, .75, 1, 1.25, 1.5, 1.75, ChartScaleButton2)
        String name = anEvent.getName();
        if (name.startsWith("ChartScaleButton")) {
            double chartScale = StringUtils.doubleValue(name);
            docItem.setChartScale(chartScale);
        }
    }
}