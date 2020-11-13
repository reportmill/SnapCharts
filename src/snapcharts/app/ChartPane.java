package snapcharts.app;
import rmdraw.app.MarkupEditorPane;
import snap.gfx.ShadowEffect;
import snap.util.Undoer;
import snap.view.*;
import snapcharts.model.Chart;
import snapcharts.model.ChartArchiver;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;
import snapcharts.views.ChartView;
import snapcharts.views.DataView;
import snapcharts.views.DataViewPanZoom;

import java.util.List;

/**
 * A class to manage charts/data in a ChartBook.
 */
public class ChartPane extends DocItemPane {

    // A MarkupEditorPane to hold ChartView and allow markup
    private MarkupEditorPane  _editorPane;

    // The chartView
    private ChartView  _chartView;
    
    // The ChartBox
    private BoxView  _chartBox;

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

    // The undoer
    private Undoer  _undoer;

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()
    {
        return _chartView;
    }

    /**
     * Returns the DataView for class.
     */
    public <T extends DataView> T getDataView(Class<T> aClass)
    {
        DataView dataView = getChartView().getDataView();
        return aClass==null || aClass.isInstance(dataView) ? (T)dataView : null;
    }

    /**
     * Returns the inspector.
     */
    public ViewOwner getInspector()  { return _insp; }

    /**
     * Returns the chart.
     */
    public Chart getChart()
    {
        return _chartView.getChart();
    }

    /**
     * Sets the Chart.
     */
    public void setChart(Chart aChart)
    {
        getUI();
        _chartView.setChart(aChart);
        resetLater();
    }

    /**
     * Returns the current DataSet.
     */
    public DataSet getDataSet()
    {
        int selIndex = _tabView!=null ? _tabView.getSelIndex() : -1;
        DataSetList dataSetList = getChart().getDataSetList();
        if (selIndex<0 || selIndex>=dataSetList.getDataSetCount())
            return null;
        return dataSetList.getDataSet(selIndex);
    }

    /**
     * Sets the DataSet, with chart.
     */
    public void setDataSet(DataSet aDataSet)
    {
        _dataSetMode = true;
        Chart chart = aDataSet.getChart();
        Chart chart2 = new ChartArchiver().copy(chart);
        DataSetList dataSetList = chart2.getDataSetList();
        while (dataSetList.getDataSetCount()>0)
            dataSetList.removeDataSet(0);
        DataSet dset2 = new ChartArchiver().copy(aDataSet);
        dataSetList.addDataSet(dset2);
        setChart(chart2);
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
     * Override to return the ChartView.
     */
    @Override
    public View getItemView()
    {
        return getChartView();
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        // Get ColView
        RowView topRowView = (RowView) super.createUI();

        // Create/add InspectorPane
        _insp = new ChartPaneInsp(this);
        topRowView.addChild(_insp.getUI());

        // Return TopRowView
        return topRowView;
    }

    @Override
    protected void initUI()
    {
        // Get/configure ToolBar
        RowView toolBar = getView("ToolBar", RowView.class);
        toolBar.setFill(ChartSetPane.BACK_FILL);

        // Get/set/configure ChartBox
        _chartBox = getView("ChartBox", BoxView.class);
        _chartBox.setFill(ChartSetPane.BACK_FILL);

        // Create ChartView
        _chartView = new ChartView();
        _chartView.setEffect(new ShadowEffect());
        _chartView.addEventFilter(e -> resetLater(), MouseRelease);
        _chartBox.setContent(_chartView);

        // Get/set TabView
        _tabView = getView("TabView", TabView.class);

        // If ChartPane is in DataSetMode, change some things
        if (_dataSetMode) {
            _tabView.setPrefHeight(-1);
            _tabView.setGrowHeight(true);
            _chartBox.setPrefHeight(400);
            _chartBox.setGrowHeight(false);
            _chartBox.setPadding(30, 60, 30, 60);
        }

        // Create configure ChartPaneSel
        _selHpr = new ChartPaneSel(this);
    }

    /**
     * Installs EditorPane.
     */
    private void installEditorPane()
    {
        // Get toolbar
        RowView toolBar = getView("ToolBar", RowView.class);
        toolBar.removeChild(getView("EditButton"));
        toolBar.setClipToBounds(true);

        // Install EditorPane
        SplitView splitView = (SplitView) _chartBox.getParent();
        splitView.removeItem(_chartBox);
        _editorPane = new MarkupEditorPane(_chartView);
        _editorPane.getEditor().setFill(ChartSetPane.BACK_FILL);
        _editorPane.getScrollView().setBorder(null);
        View editPaneUI = _editorPane.getUI();
        splitView.addItem(editPaneUI, 0);

        // WTF
        ViewUtils.setFocused(_editorPane.getEditor(), false);

        // Install ToolBar
        RowView toolsView = _editorPane.getTopToolBar().getToolsView();
        toolBar.addChild(toolsView, 0);
        for (View child : toolsView.getChildren())
            ((ToggleButton)child).setShowArea(false);

        // Animate in
        toolsView.setTransX(-100);
        toolsView.getAnim(700).setTransX(0).play();
        toolsView.getAnim(0).setOnFinish(() -> ((ToggleButton)toolsView.getChildLast()).fire());
    }

    /**
     * Initialize showing.
     */
    @Override
    protected void initShowing()
    {
        // Configure TabView with Chart.Datasets
        DataSetList dataSetList = _chartView.getDataSetList();
        List<DataSet> dsets = dataSetList.getDataSets();
        for (DataSet dset : dsets) {
            _tabView.addTab(dset.getName(), new Label(dset.getName()));
        }
    }

    /**
     * ResetUI.
     */
    @Override
    protected void resetUI()
    {
        // Update ZoomSelectButton
        DataViewPanZoom dataView = getDataView(DataViewPanZoom.class); if (dataView==null) return;
        setViewValue("ZoomSelectButton", dataView!=null && dataView.isZoomSelectMode());

        // Make sure TabView has DataSetPane UI view (not Label placeholder)
        int selTabIndex = _tabView.getSelIndex();
        if (selTabIndex>=0 && _tabView.getTabContent(selTabIndex) instanceof Label) {
            DataSet dset = _chartView.getDataSetList().getDataSet(selTabIndex);
            DataSetPane dsetPane = new DataSetPane();
            dsetPane.setDataSet(dset);
            _tabView.setTabContent(dsetPane.getUI(), selTabIndex);
        }

        // Reset inspector
        _insp.resetLater();
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle ZoomSelectButton
        if (anEvent.equals("ZoomSelectButton")) {
            DataViewPanZoom dataView = getDataView(DataViewPanZoom.class); if (dataView==null) return;
            dataView.setZoomSelectMode(anEvent.getBoolValue());
        }

        // Handle ZoomInButton, ZoomOutButton
        if (anEvent.equals("ZoomInButton") || anEvent.equals("ZoomOutButton")) {
            DataViewPanZoom dataView = getDataView(DataViewPanZoom.class); if (dataView==null) return;
            double scale = anEvent.equals("ZoomInButton") ? .5 : 2;
            dataView.scaleAxesMinMaxForFactor(scale, true);
        }

        // Handle ResetButton
        if (anEvent.equals("ResetButton")) {
            DataViewPanZoom dataView = getDataView(DataViewPanZoom.class); if (dataView==null) return;
            dataView.resetAxesAnimated();
        }

        // Handle TabView
        if (anEvent.equals(_tabView)) {
            int selIndex = _tabView.getSelIndex();
            DataSet dset = _chartView.getDataSetList().getDataSet(selIndex);
            getSel().setSelChartPart(dset);
        }

        // Handle EditButton
        if (anEvent.equals("EditButton"))
            runLater(() -> installEditorPane());
    }
}