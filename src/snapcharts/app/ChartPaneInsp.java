/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.styler.StylerPane;
import snap.view.*;
import snap.viewx.TextPane;
import snapcharts.appmisc.Collapser;
import snapcharts.apptools.*;
import snapcharts.charts.*;

/**
 * A class to manage inspector.
 */
public class ChartPaneInsp extends ViewOwner {

    // The ChartPane
    protected ChartPane _chartPane;
    
    // The Title label
    private Label  _titleLabel;
    
    // The ScrollView that holds UI for child inspectors
    private ScrollView _inspScroll;
    
    // The ColView that holds UI for child inspectors
    private ColView  _inspColView;

    // The ChartInsp
    private ChartInsp _chartInsp;

    // The HeaderInsp
    private HeaderInsp _headerInsp;

    // The AxisInsp
    private AxisInsp  _axisInsp;

    // The ContourAxisInsp
    private ContourAxisInsp  _contourAxisInsp;

    // The LegendInsp
    private LegendInsp  _legendInsp;

    // The MarkerInsp
    private MarkerInsp  _markerInsp;

    // The ContentInsp
    private ContentInsp  _contentInsp;

    // The DataSet Inspector
    private TraceInsp  _traceInsp;

    // The array of ChartPartInsp
    private ChartPartInsp[]  _allInspectors;

    // The StylerPane
    private StylerPane  _stylerPane;

    // Get MiscInspector
    private MiscInsp  _miscInsp;

    /**
     * Constructor.
     */
    public ChartPaneInsp(ChartPane anEP)
    {
        _chartPane = anEP;
    }

    /**
     * Returns the ChartPane.
     */
    public ChartPane getChartPane()  { return _chartPane; }

    /**
     * Initializes UI panel for the inspector.
     */
    public void initUI()
    {
        // Get/configure TitleLabel
        _titleLabel = getView("TitleLabel", Label.class);
        _titleLabel.setTextFill(Color.GRAY);

        // Get/configure SearchText: radius, prompt, image, animation
        TextField searchText = getView("SearchTextField", TextField.class);
        searchText.setPromptText("Search");
        searchText.getLabel().setImage(Image.getImageForClassResource(TextPane.class, "Find.png"));
        TextField.setBackLabelAlignAnimatedOnFocused(searchText, true);
        //searchText.addEventFilter(e -> ViewUtils.runLater(() -> textFieldKeyTyped(e)), KeyPress);

        // Get/configure ContentBox
        _inspScroll = getView("ContentBox", ScrollView.class);
        _inspScroll.setBorder(null);
        _inspScroll.setBarSize(12);
        _inspScroll.setFillWidth(true);

        // Get InspColView
        _inspColView = getView("InspColView", ColView.class);

        // Only Add Chart stuff for ChartMode
        boolean chartMode = !_chartPane._dataSetMode;
        if (chartMode) {

            // Create/add ChartInsp
            _chartInsp = new ChartInsp(_chartPane);
            addInspector(_chartInsp);

            // Create/add HeaderInsp
            _headerInsp = new HeaderInsp(_chartPane);
            addInspector(_headerInsp);

            // Create/add AxisInsp
            _axisInsp = new AxisInsp(_chartPane);
            addInspector(_axisInsp);

            // Create/add ContourAxisInsp
            _contourAxisInsp = new ContourAxisInsp(_chartPane);
            addInspector(_contourAxisInsp);

            // Create/add LegendInsp
            _legendInsp = new LegendInsp(_chartPane);
            addInspector(_legendInsp);

            // Create/add MarkerInsp
            _markerInsp = new MarkerInsp(_chartPane);
            addInspector(_markerInsp);

            // Create/add ContentInsp
            _contentInsp = new ContentInsp(_chartPane);
            addInspector(_contentInsp);
        }

        // Add TraceInsp
        _traceInsp = new TraceInsp(_chartPane);
        addInspector(_traceInsp);

        // Set all inspectors
        _allInspectors = new ChartPartInsp[] { _chartInsp, _headerInsp, _axisInsp, _contourAxisInsp,
                _contentInsp, _legendInsp, _markerInsp, _traceInsp };
        if (!chartMode)
            _allInspectors = new ChartPartInsp[] { _traceInsp };

        // Trigger initial open panel
        runLater(() -> chartPaneSelChartPartChanged());

        // Create/set StylerPane
        _stylerPane = new StylerPane(_chartPane.getStyler());
        //_inspColView.addChild(_stylerPane.getUI());

        // Create/set MiscInsp
        _miscInsp = new MiscInsp(_chartPane);
    }

    /**
     * Adds an inspector.
     */
    private void addInspector(ChartPartInsp aChartPartInsp)
    {
        // Get UI view and add to inspector
        View inspUI = aChartPartInsp.getUI();
        _inspColView.addChild(inspUI);

        // Trigger Collapser create
        Collapser collapser = aChartPartInsp.getCollapser();
         collapser.setCollapsed(true);

        // Add listener to update ChartPartInsp.Sel when label is clicked
        Label label = aChartPartInsp.getLabel();
        label.addEventFilter(e -> runLater(() -> chartPartInspLabelMousePress(aChartPartInsp)), MousePress);
    }

    /**
     * Refreshes the inspector for the current editor selection.
     */
    public void resetUI()
    {
        // Make ContourAxisInsp.Visible only if TraceType is contour
        Chart chart = _chartPane.getChart();
        TraceType traceType = chart.getTraceType();
        boolean isContour = traceType.isContourType();
        if (_contourAxisInsp != null) {
            _contourAxisInsp.getUI().setVisible(isContour);
            _contourAxisInsp.getLabel().setVisible(isContour);
        }

        // Make MarkerInsp.Visible only if chart has markers
        boolean hasMarkers = chart.getMarkers().length > 0;
        if (_markerInsp != null) {
            _markerInsp.getUI().setVisible(hasMarkers);
            _markerInsp.getLabel().setVisible(hasMarkers);
        }

        // Reset SelPartInsp
        ChartPart selPart = _chartPane.getSelChartPart();
        ChartPartInsp selPartInsp = getInspForChartPart(selPart);
        if (selPartInsp != null && selPartInsp.isShowing())
            selPartInsp.resetLater();

        // Reset TraceInsp
        boolean isTraceSelected = _chartPane.getTrace() != null;
        _traceInsp.getUI().setVisible(isTraceSelected);
        if (isTraceSelected)
            _traceInsp.resetLater();

        // Reset Styler
        if (_stylerPane.isShowing())
            _stylerPane.resetLater();

        // Reset MiscInsp
        if (_miscInsp.isShowing())
            _miscInsp.resetLater();
    }

    /**
     * Handles changes to the inspector UI controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Handle GeneralButton
        if (anEvent.equals("GeneralButton"))
            _inspScroll.setContent(_inspColView);
        else if (anEvent.equals("StyleButton"))
            _inspScroll.setContent(_stylerPane.getUI());
        else if (anEvent.equals("MiscButton"))
            _inspScroll.setContent(_miscInsp.getUI());
    }

    /**
     * Shows the Scene inspector.
     */
    public void showSceneInspector()
    {
        chartPartInspLabelMousePress(_contentInsp);
        _contentInsp.showContent3D();
    }

    /**
     * Returns the inspector for given ChartPart.
     */
    public ChartPartInsp getInspForChartPart(ChartPart aChartPart)
    {
        if (aChartPart instanceof Header)
            return _headerInsp;
        if (aChartPart instanceof Axis)
            return _axisInsp;
        if (aChartPart instanceof ContourAxis)
            return _contourAxisInsp;
        if (aChartPart instanceof Legend)
            return _legendInsp;
        if (aChartPart instanceof Marker)
            return _markerInsp;
        if (aChartPart instanceof Content)
            return _contentInsp;
        if (aChartPart instanceof Trace)
            return _traceInsp;
        return _chartInsp;
    }

    /**
     * Called when label gets mouse press.
     */
    public void chartPartInspLabelMousePress(ChartPartInsp anInsp)
    {
        // Get ChartPart for inspector
        ChartPart chartPart = anInsp.getChartPart();

        // TraceInsp/Trace is going to pretend to represent Content
//        if (chartPart instanceof Trace) {
//            Chart chart = _chartPane.getChart();
//            Content content = chart.getContent();
//            chartPart = content.getTraceCount() > 0 ? content.getTrace(0) : chart;
//        }

        // Set new ChartPane.SelChartPart
        _chartPane.getSel().setSelChartPart(chartPart);
    }

    /**
     * Called when ChartPane selection (SelChartPart) changes.
     */
    public void chartPaneSelChartPartChanged()
    {
        // Get SelPart and SelInsp
        ChartPart selPart = _chartPane.getSelChartPart();
        ChartPartInsp selPartInsp = getInspForChartPart(selPart);

        // Iterate over all ChartPaneInsp and make SelPartInsp is expanded (and others not)
        for (ChartPartInsp insp : _allInspectors) {
            boolean isSelected = insp == selPartInsp;
            insp.setSelected(isSelected);
            if (isSelected)
                insp.resetLater();
        }

        // Reset inspector
        if (selPartInsp != null)
            selPartInsp.resetLater();
    }
}