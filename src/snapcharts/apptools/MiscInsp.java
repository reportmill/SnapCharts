/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.gfx.Font;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.appmisc.Collapser;

/**
 * This class provides UI editing for miscellaneous ChartPart properties.
 */
public class MiscInsp extends ViewOwner {

    // The ChartPane
    private ChartPane  _chartPane;

    // The LayoutInspector
    private LayoutInsp  _layoutInsp;

    // The MiscSillyInspector
    private MiscSillyInsp  _sillyInsp;

    // The ColView that holds UI for child inspectors
    private ColView  _inspColView;

    /**
     * Constructor.
     */
    public MiscInsp(ChartPane aChartPane)
    {
        super();
        _chartPane = aChartPane;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Get/configure ContentBox
        ScrollView inspScroll = getView("ContentBox", ScrollView.class);
        inspScroll.setBorder(null);
        inspScroll.setBarSize(12);
        inspScroll.setFillWidth(true);

        // Get InspColView
        _inspColView = getView("InspColView", ColView.class);

        // Create/add LayoutInsp
        _layoutInsp = new LayoutInsp(_chartPane);
        addInspector(_layoutInsp, true);
        _layoutInsp.setSelected(true);
        _layoutInsp.getLabel().setFont(Font.Arial14);

        // Create/add MiscSillyInsp
        _sillyInsp = new MiscSillyInsp(_chartPane);
        addInspector(_sillyInsp, true);
        _sillyInsp.setSelected(true);
        _sillyInsp.getLabel().setFont(Font.Arial14);
    }

    /**
     * Update UI.
     */
    @Override
    protected void resetUI()
    {
        // Reset LayoutInsp
        if (_layoutInsp.isShowing())
            _layoutInsp.resetLater();
    }

    /**
     * Respond UI.
     */
    //@Override
    //protected void respondUI(ViewEvent anEvent)  { }

    /**
     * Adds an inspector.
     */
    private void addInspector(ChartPartInsp aChartPartInsp, boolean isShowing)
    {
        // Get UI view and add to inspector
        View inspUI = aChartPartInsp.getUI();
        _inspColView.addChild(inspUI);

        // Trigger Collapser create
        Collapser collapser = aChartPartInsp.getCollapser();
        if (!isShowing)
            collapser.setCollapsed(true);

        // Add listener to update ChartPartInsp.Sel when label is clicked
        //Label label = aChartPartInsp.getLabel();
        //label.addEventFilter(e -> runLater(() -> chartPartInspLabelMousePress(aChartPartInsp)), MousePress);
    }
}
