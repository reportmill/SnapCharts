/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.styler.StylerPane;
import snap.util.PropChange;
import snap.view.*;
import snap.viewx.TextPane;
import snapcharts.appmisc.Collapser;
import snapcharts.apptools.*;
import snapcharts.model.*;

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
    private AxisInsp  _axisXInsp;

    // The AxisInsp
    private AxisInsp  _axisYInsp;

    // The LegendInsp
    private LegendInsp  _legendInsp;

    // The DataViewInsp
    private DataViewInsp  _dataViewInsp;

    // The DataSet Inspector
    private DataSetInsp _dsetInsp;

    // The DataStyleInsp
    private DataStyleInsp _dataStyleInsp;

    // The array of ChartPartInsp
    private ChartPartInsp  _allInspectors[];

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
    public ChartPane getEditorPane()  { return _chartPane; }

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
        searchText.getLabel().setImage(Image.get(TextPane.class, "Find.png"));
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
            addInspector(_chartInsp, false);

            // Create/add HeaderInsp
            _headerInsp = new HeaderInsp(_chartPane);
            addInspector(_headerInsp, false);

            // Create/add AxisXInsp
            _axisXInsp = new AxisInsp(_chartPane, AxisType.X);
            addInspector(_axisXInsp, false);

            // Create/add AxisYInsp
            _axisYInsp = new AxisInsp(_chartPane, AxisType.Y);
            addInspector(_axisYInsp, false);

            // Create/add LegendInsp
            _legendInsp = new LegendInsp(_chartPane);
            addInspector(_legendInsp, false);

            // Create/add DataViewInsp
            _dataViewInsp = new DataViewInsp(_chartPane);
            addInspector(_dataViewInsp, false);
        }

        // Add DataSetInsp
        _dsetInsp = new DataSetInsp(_chartPane);
        addInspector(_dsetInsp, false);

        // Create/add DataStyleInsp
        _dataStyleInsp = new DataStyleInsp(_chartPane);
        addInspector(_dataStyleInsp, false);

        // Set all inspectors
        _allInspectors = new ChartPartInsp[] { _chartInsp, _headerInsp, _axisXInsp, _axisYInsp, _legendInsp,
                _dataViewInsp, _dataStyleInsp, _dsetInsp };
        if (!chartMode)
            _allInspectors = new ChartPartInsp[] { _dsetInsp };

        // Trigger initial open panel
        runLater(() -> chartPaneSelChanged());

        // Create/set StylerPane
        _stylerPane = new StylerPane(_chartPane.getStyler());
        //_inspColView.addChild(_stylerPane.getUI());

        // Create/set MiscInsp
        _miscInsp = new MiscInsp();
    }

    /**
     * Override to trigger update of DataStyleInsp.
     */
    @Override
    protected void initShowing()
    {
        if (_dataStyleInsp != null)
            _dataStyleInsp.resetLater();
    }

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
        Label label = aChartPartInsp.getLabel();
        label.addEventFilter(e -> runLater(() -> chartPartInspLabelMousePress(aChartPartInsp)), MousePress);
    }

    /**
     * Refreshes the inspector for the current editor selection.
     */
    public void resetUI()
    {
        // Get editor (and just return if null) and tool for selected shapes
        ChartPane chartPane = getEditorPane();

        // Reset DataSetInsp
        boolean isDataSetSelected = chartPane.getDataSet()!=null;
        if (isDataSetSelected) {
            _dsetInsp.getUI().setVisible(true);
            _dsetInsp.resetLater();
        }
        else _dsetInsp.getUI().setVisible(false);

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
     * Sets a view that overrides normal inspector UI.
     */
    public void setOverrideInspectorView(View aView)
    {
        View inspContent = aView != null ? aView : _inspColView;
        _inspScroll.setContent(inspContent);
    }

    /**
     * Returns the inspector for given ChartPart.
     */
    public ChartPartInsp getChartPartInsp(ChartPart aChartPart)
    {
        if (aChartPart instanceof Header)
            return _headerInsp;
        if (aChartPart instanceof AxisX)
            return _axisXInsp;
        if (aChartPart instanceof AxisY)
            return _axisYInsp;
        if (aChartPart instanceof Legend)
            return _legendInsp;
        if (aChartPart instanceof DataSetList)
            return _dataViewInsp;
        if (aChartPart instanceof DataSet)
            return _dsetInsp;
        return _chartInsp;
    }

    /**
     * Called when label gets mouse press.
     */
    public void chartPartInspLabelMousePress(ChartPartInsp anInsp)
    {
        // Get ChartPart for inspector
        ChartPart chartPart = anInsp.getChartPart();

        // DataStyleInsp/DataStyle is going to pretend to represent DataSetList
        if (chartPart instanceof DataStyle) {
            Chart chart = _chartPane.getChart();
            DataSetList dataList = chart.getDataSetList();
            chartPart = dataList.getDataSetCount() > 0 ? dataList.getDataSet(0) : chart;
        }

        // Set new ChartPane.SelChartPart
        _chartPane.getSel().setSelChartPart(chartPart);
    }

    /**
     * Called when the selection changes.
     */
    public void chartPaneSelChanged()
    {
        // Get SelPart and SelInsp
        ChartPart selPart = _chartPane.getSelChartPart();
        ChartPartInsp selPartInsp = getChartPartInsp(selPart);

        // Iterate over all ChartPaneInsp and make SelPartInsp is expanded (and others not)
        for (ChartPartInsp insp : _allInspectors) {
            boolean isSelected = insp == selPartInsp ||
                (insp == _dataStyleInsp && selPartInsp == _dsetInsp);
            insp.setSelected(isSelected);
            if (isSelected)
                insp.resetLater();
        }

        // Reset inspector
        if (selPartInsp != null)
            selPartInsp.resetLater();
    }


    /**
     * Called when a ChartPart has change.
     */
    protected void chartPartDidPropChange(PropChange aPC)
    {
        // If not showing, just return
        if (!isUISet()) return;

        // Handle Chart.Type change
        String propName = aPC.getPropName();
        if (propName == Chart.Type_Prop)
            _dataStyleInsp.resetLater();
    }
}