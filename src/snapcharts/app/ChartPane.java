package snapcharts.app;
import rmdraw.app.MarkupEditor;
import rmdraw.app.MarkupEditorPane;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Effect;
import snap.gfx.ShadowEffect;
import snap.util.DeepChangeListener;
import snap.util.PropChange;
import snap.util.PropChangeListener;
import snap.util.Undoer;
import snap.view.*;
import snapcharts.model.Chart;
import snapcharts.doc.ChartArchiver;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;
import snapcharts.view.ChartView;
import snapcharts.view.DataView;
import java.util.List;

/**
 * A class to manage charts/data in a ChartBook.
 */
public class ChartPane extends DocItemPane {

    // A MarkupEditorPane to hold ChartView and allow markup
    protected MarkupEditorPane  _editorPane;

    // The chartView
    private ChartView  _chartView;
    
    // The ChartBox
    private BoxView  _chartBox;

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
    private PropChangeListener _pcl = pc -> chartPartDidPropChange(pc);

    // The DeepChangeListener
    private DeepChangeListener _dcl = (src, pc) -> chartPartDidPropChange(pc);

    // The undoer
    private Undoer  _undoer;

    // Constants
    public static Border CHART_BORDER = Border.createLineBorder(Color.GRAY, 1);
    public static Effect CHART_SHADOW = new ShadowEffect(12, Color.DARKGRAY, 0, 0).copySimple();

    /**
     * Constructor.
     */
    public ChartPane()
    {
        super();
    }

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()  { return _chartView; }

    /**
     * Returns the ChartView.DataView
     */
    public DataView getDataView()  { return _chartView.getDataView(); }

    /**
     * Returns the inspector.
     */
    public ChartPaneInsp getInspector()  { return _insp; }

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

        // Start listening to changes
        aChart.addPropChangeListener(_pcl);
        aChart.addDeepChangeListener(_dcl);
    }

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
        int selIndex = _tabView!=null ? _tabView.getSelIndex() : -1;
        DataSetList dataSetList = getDataSetList();
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
        int minTabViewHeight = _dataSetMode ? 400 : 260;
        double tabViewHeight = Math.max(_splitView.getHeight() - chartHeight, minTabViewHeight);
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
        RowView topRowView = (RowView) super.createUI();

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
    }

    /**
     * Initialize showing.
     */
    @Override
    protected void initShowing()
    {
        // Configure TabView with Chart.Datasets
        DataSetList dataSetList = getDataSetList();
        List<DataSet> dsets = dataSetList.getDataSets();
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
        DataView dataView = getDataView();
        setViewValue("ZoomSelectButton", dataView!=null && dataView.isZoomSelectMode());

        // Make sure TabView has DataSetPane UI view (not Label placeholder)
        int selTabIndex = _tabView.getSelIndex();
        if (selTabIndex>=0 && _tabView.getTabContent(selTabIndex) instanceof Label) {
            DataSet dset = getDataSetList().getDataSet(selTabIndex);
            DataSetPane dsetPane = new DataSetPane();
            dsetPane.setDataSet(dset);
            _tabView.setTabContent(dsetPane.getUI(), selTabIndex);
        }

        // Reset inspector
        _insp.resetLater();

        // Update ShowDataSetTabs
        if (!_dataSetMode)
            setShowDataSetTabs(getSel().getSelChartPart() instanceof DataSet);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle ZoomSelectButton
        if (anEvent.equals("ZoomSelectButton")) {
            DataView dataView = getDataView();
            dataView.setZoomSelectMode(anEvent.getBoolValue());
        }

        // Handle ZoomInButton, ZoomOutButton
        if (anEvent.equals("ZoomInButton") || anEvent.equals("ZoomOutButton")) {
            DataView dataView = getDataView();
            double scale = anEvent.equals("ZoomInButton") ? .5 : 2;
            dataView.scaleAxesMinMaxForFactor(scale, true);
        }

        // Handle ResetButton
        if (anEvent.equals("ResetButton")) {
            ChartView chartView = getChartView();
            chartView.getChartHelper().resetAxesAnimated();
        }

        // Handle TabView
        if (anEvent.equals("TabView")) {
            int selIndex = _tabView.getSelIndex();
            DataSet dset = getDataSetList().getDataSet(selIndex);
            getSel().setSelChartPart(dset);
        }

        // Handle EditButton
        if (anEvent.equals("EditButton"))
            runLater(() -> installEditorPane());
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

        // Get EditorPane, Editor
        _editorPane = new MarkupEditorPane(_chartView);
        MarkupEditor editor = _editorPane.getEditor();
        editor.setFill(ChartSetPane.BACK_FILL);
        _editorPane.getScrollView().setBorder(null);

        // Install EditorPane
        SplitView splitView = (SplitView) _chartBox.getParent();
        splitView.removeItem(_chartBox);
        splitView.addItem(_editorPane.getUI(), 0);

        // WTF
        ViewUtils.setFocused(editor, false);

        // Install ToolBar
        RowView toolsView = _editorPane.getToolsView();
        toolBar.addChild(toolsView, 0);
        for (View child : toolsView.getChildren())
            ((ToggleButton)child).setShowArea(false);

        // Animate in
        toolsView.setTransX(-100);
        toolsView.getAnimCleared(700).setTransX(0).play();
        toolsView.getAnim(0).setOnFinish(() -> installEditorPaneAnimDone());
    }

    /**
     * MarkupEditor: Called after fully installed.
     */
    private void installEditorPaneAnimDone()
    {
        RowView toolsView = _editorPane.getToolsView();
        ((ToggleButton)toolsView.getChildLast()).fire();

        MarkupEditor editor = _editorPane.getEditor();
        editor.setNeedsInspector(false);
        editor.addPropChangeListener(pc -> markupEditorNeedsInspectorChanged(), MarkupEditor.NeedsInspector_Prop);
        editor.setNeedsInspector(true);
    }

    /**
     * MarkupEditor: Called when MarkupEditor.NeedsInspector changes.
     */
    private void markupEditorNeedsInspectorChanged()
    {
        _insp.setMarkupInspectorVisible(_editorPane.getEditor().isNeedsInspector());
    }

    /**
     * Called when a ChartPart has change.
     */
    private void chartPartDidPropChange(PropChange aPC)
    {
        _insp.chartPartDidPropChange(aPC);
    }
}