/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.gfx.*;
import snap.util.DeepChangeListener;
import snap.util.PropChange;
import snap.util.PropChangeListener;
import snap.util.Undoer;
import snap.view.*;
import snapcharts.doc.DocItem;
import snapcharts.doc.DocItemChart;
import snapcharts.doc.DocItemDataSet;
import snapcharts.model.*;
import snapcharts.doc.ChartArchiver;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import snapcharts.view.DataView;

/**
 * A class to manage charts/data in a ChartBook.
 */
public class ChartPane<T extends DocItem> extends DocItemPane<T> {

    // The Chart
    private Chart  _chart;

    // The chartView
    private ChartView  _chartView;
    
    // The ChartBox
    protected BoxView  _chartBox;

    // The SplitView
    private SplitView  _splitView;

    // The TabView
    private TabView  _tabView;
    
    // The Inspector
    protected ChartPaneInsp  _insp;

    // Whether this ChartPane is in DataSet mode
    protected boolean  _dataSetMode;

    // The selection helper class
    private ChartPaneSel  _selHpr;

    // The ChartPart Styler
    private ChartStyler  _styler = new ChartStyler(this);

    // The PropChangeListener
    private PropChangeListener  _pcl = pc -> chartPartDidPropChange(pc);

    // The DeepChangeListener
    private DeepChangeListener  _dcl = (src, pc) -> chartPartDidPropChange(pc);

    // The undoer
    private Undoer  _undoer;

    // Constants
    public static Border CHART_BORDER = Border.createLineBorder(Color.GRAY, 1);
    public static Effect CHART_SHADOW = new ShadowEffect(12, Color.DARKGRAY, 0, 0).copySimple();

    /**
     * Constructor.
     */
    public ChartPane(T aDocItem)
    {
        super(aDocItem);
    }

    /**
     * Override.
     */
    @Override
    protected void setDocItem(T aDocItem)
    {
        super.setDocItem(aDocItem);

        // Handle DocItemChart
        if (aDocItem instanceof DocItemChart) {
            DocItemChart docItemChart = (DocItemChart) aDocItem;
            Chart chart = docItemChart.getChart();
            setChart(chart);
        }

        // Handle DocItemDataSet
        else if (aDocItem instanceof DocItemDataSet) {
            DocItemDataSet docItemDataSet = (DocItemDataSet) aDocItem;
            DataSet dataSet = docItemDataSet.getDataSet();
            setChartForSingleDataSet(dataSet);
        }
    }

    /**
     * Returns the chart.
     */
    public Chart getChart()  { return _chart; }

    /**
     * Sets the Chart.
     */
    private void setChart(Chart aChart)
    {
        _chart = aChart;
    }

    /**
     * Sets the DataSet, with chart.
     */
    private void setChartForSingleDataSet(DataSet aDataSet)
    {
        // Put ChartPane in 'DataSetMode'
        _dataSetMode = true;

        // Copy DataSet chart
        Chart chart = aDataSet.getChart();
        Chart chartCopy = new ChartArchiver().copy(chart);
        DataSetList dataSetList = chartCopy.getDataSetList();
        while (dataSetList.getDataSetCount() > 0)
            dataSetList.removeDataSet(0);

        // Copy DataSet and add to ChartCopy
        DataSet dataSetCopy = new ChartArchiver().copy(aDataSet);
        dataSetList.addDataSet(dataSetCopy);

        // Set Chart
        setChart(chartCopy);
    }

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()  { return _chartView; }

    /**
     * Returns the ChartBox (holds ChartView).
     */
    public BoxView getChartBox()  { return _chartBox; }

    /**
     * Returns the ChartView.DataView
     */
    public DataView getDataView()  { return _chartView.getDataView(); }

    /**
     * Returns the inspector.
     */
    public ChartPaneInsp getInspector()  { return _insp; }

    /**
     * Returns the ChartHelper.
     */
    public ChartHelper getChartHelper()  { return _chartView.getChartHelper(); }

    /**
     * Returns the DataSetList.
     */
    private DataSetList getDataSetList()
    {
        Chart chart = getChart();
        return chart.getDataSetList();
    }

    /**
     * Returns the current DataSet.
     */
    public DataSet getDataSet()
    {
        DataSetList dataSetList = getDataSetList();
        int selIndex = _tabView != null ? _tabView.getSelIndex() : -1;
        if (selIndex < 0 || selIndex >= dataSetList.getDataSetCount())
            return null;
        return dataSetList.getDataSet(selIndex);
    }

    /**
     * Returns the ChartPartStyler.
     */
    public ChartStyler getStyler()  { return _styler; }

    /**
     * Returns the undoer.
     */
    public Undoer getUndoer()  { return _undoer; }

    /**
     * Returns the ChartPaneSel.
     */
    public ChartPaneSel getSel()  { return _selHpr; }

    /**
     * Returns the selected chart part.
     */
    public ChartPart getSelChartPart()
    {
        ChartPaneSel sel = getSel();
        return sel.getSelChartPart();
    }

    /**
     * Override to return the ChartView.
     */
    @Override
    public View getItemView()
    {
        return getChartView();
    }

    /**
     * Returns whether DataSetTabs are showing.
     */
    public boolean isShowDataSetTabs()
    {
        return _tabView.getParent()!=null;
    }

    /**
     * Sets whether DataSetTabs are showing.
     */
    public void setShowDataSetTabs(boolean aValue)
    {
        if (aValue==isShowDataSetTabs()) return;
        if (aValue)
            showDataSetTabs();
        else hideDataSetTabs();
    }

    /**
     * Show dataset tabs.
     */
    public void showDataSetTabs()
    {
        // Add TabView
        double chartHeight = _chartBox.getPrefHeight();
        int minTabViewHeight = _dataSetMode ? 320 : 180;
        double tabViewHeight = Math.max(_splitView.getHeight() - chartHeight - 70, minTabViewHeight);
        _splitView.addItemWithAnim(_tabView, tabViewHeight);

        // Make TabView the primary grower if DataSetMode
        _tabView.setGrowHeight(_dataSetMode);
        _chartBox.setGrowHeight(!_dataSetMode);
    }

    /**
     * Hides DataSet tabs.
     */
    public void hideDataSetTabs()
    {
        _splitView.removeItemWithAnim(_tabView);
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        // Get ColView
        ColView topColView = (ColView) super.createUI();

        // Create custom RowView for painting selection
        RowView topRowView = new TopRowView();
        topRowView.setFillHeight(true);
        topRowView.addChild(topColView);

        // Create/add InspectorPane
        _insp = new ChartPaneInsp(this);
        topRowView.addChild(_insp.getUI());

        // Return TopRowView
        return topRowView;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Get/configure ToolBar
        RowView toolBar = getView("ToolBar", RowView.class);
        toolBar.setFill(ChartSetPane.BACK_FILL);

        // Get/set/configure ChartBox
        _chartBox = getView("ChartBox", BoxView.class);
        _chartBox.setFill(ChartSetPane.BACK_FILL);
        _chartBox.setCropHeight(true);

        // Create ChartView
        _chartView = new ChartView();
        _chartView.setBorder(CHART_BORDER);
        _chartView.setEffect(CHART_SHADOW);
        _chartView.addEventFilter(e -> resetLater(), MouseRelease);
        _chartBox.setContent(_chartView);

        // Get SplitView
        _splitView = getView("SplitView", SplitView.class);

        // Get TabView and remove from SplitView (default mode)
        _tabView = getView("TabView", TabView.class);
        _splitView.removeItem(_tabView);
        _tabView.setOwner(this);

        // Create configure ChartPaneSel
        _selHpr = new ChartPaneSel(this);

        // If ChartPane is in DataSetMode, change some things
        if (_dataSetMode) {
            //_tabView.setPrefHeight(-1); _tabView.setGrowHeight(true);
            //_chartBox.setPrefHeight(400); _chartBox.setGrowHeight(false);
            _chartBox.setPadding(20, 20, 20, 20);
        }

        // Register for EscapeAction
        addKeyActionHandler("EscapeAction", "ESCAPE");

        // Set Chart in ChartView
        Chart chart = getChart();
        _chartView.setChart(chart);

        // Start listening to Chart PropChanges
        chart.addPropChangeListener(_pcl);
        chart.addDeepChangeListener(_dcl);
    }

    /**
     * Initialize showing.
     */
    @Override
    protected void initShowing()
    {
        // Configure TabView with Chart.Datasets
        DataSetList dataSetList = getDataSetList();
        DataSet[] dsets = dataSetList.getDataSets();
        for (DataSet dset : dsets) {
            _tabView.addTab(dset.getName(), new Label(dset.getName()));
        }

        // If DataSetMode, showDataSetTabs
        if (_dataSetMode)
            runLater(() -> showDataSetTabs());
    }

    /**
     * ResetUI.
     */
    @Override
    protected void resetUI()
    {
        // Update ZoomSelectButton
        ChartHelper chartHelper = getChartHelper();
        setViewValue("ZoomSelectButton", chartHelper.isZoomSelectMode());

        // Make sure TabView has DataSetPane UI view (not Label placeholder)
        int selTabIndex = _tabView.getSelIndex();
        if (selTabIndex>=0 && _tabView.getTabContent(selTabIndex) instanceof Label) {

            // Get DataSet and create DataSetPane
            DataSet dataSet = getDataSetList().getDataSet(selTabIndex);
            DocItemDataSet docItemDataSet = new DocItemDataSet(dataSet);
            DataSetPane dsetPane = new DataSetPane(docItemDataSet);

            // Set TabView content
            _tabView.setTabContent(dsetPane.getUI(), selTabIndex);
        }

        // Reset inspector
        _insp.resetLater();

        // Update ShowDataSetTabs
        if (!_dataSetMode) {
            ChartPart selPart = getSelChartPart();
            boolean showDataSets = selPart instanceof DataSet;
            setShowDataSetTabs(showDataSets);
        }
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle ZoomSelectButton
        if (anEvent.equals("ZoomSelectButton")) {
            ChartHelper chartHelper = getChartHelper();
            chartHelper.setZoomSelectMode(anEvent.getBoolValue());
        }

        // Handle ZoomInButton, ZoomOutButton
        if (anEvent.equals("ZoomInButton") || anEvent.equals("ZoomOutButton")) {
            double scale = anEvent.equals("ZoomInButton") ? .5 : 2;
            ChartHelper chartHelper = getChartHelper();
            chartHelper.scaleAxesMinMaxForFactor(scale, true);
        }

        // Handle ResetButton
        if (anEvent.equals("ResetButton")) {
            ChartHelper chartHelper = getChartHelper();
            chartHelper.resetAxesAnimated();
        }

        // Handle TabView
        if (anEvent.equals("TabView")) {
            int selIndex = _tabView.getSelIndex();
            DataSet dset = getDataSetList().getDataSet(selIndex);
            getSel().setSelChartPart(dset);
        }

        // Handle EscapeAction
        if (anEvent.equals("EscapeAction"))
            getSel().popSelection();
    }

    /**
     * Called when the selection changes.
     */
    public void chartPaneSelChanged()
    {
        // If SelPart is DataSet, make sure tabPane is selected
        ChartPart selPart = getSelChartPart();
        if (selPart instanceof DataSet) {
            DataSet dataSet = (DataSet) selPart;
            _tabView.setSelIndex(dataSet.getIndex());
        }

        // Notify Inspector
        _insp.chartPaneSelChanged();
    }

    /**
     * Called when a ChartPart has change.
     */
    private void chartPartDidPropChange(PropChange aPC)
    {
        _insp.chartPartDidPropChange(aPC);
    }

    /**
     * A custom RowView for top level of UI, with hook to paint selection.
     */
    protected class TopRowView extends RowView {

        /**
         * Constructor.
         */
        public TopRowView()
        {
            super();
        }

        /**
         * Override to paint targeted view.
         */
        @Override
        protected void paintAbove(Painter aPntr)
        {
            getSel().paintSelection(aPntr, this);
        }
    }
}