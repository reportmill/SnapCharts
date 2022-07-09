/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.geom.Point;
import snap.geom.Pos;
import snap.gfx.*;
import snap.gfx3d.CameraView;
import snap.gfx3d.CubeView;
import snap.props.DeepChangeListener;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.props.Undoer;
import snap.util.*;
import snap.view.*;
import snapcharts.doc.*;
import snapcharts.model.*;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import snapcharts.view.ContentView;
import snapcharts.viewx.DataArea3D;

import java.util.List;

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
    private TabView _traceTabView;
    
    // The Inspector
    protected ChartPaneInsp  _insp;

    // A helper for Marker tools
    protected ChartPaneTools  _chartPaneTools;

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
    private Undoer _undoer;

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
        else if (aDocItem instanceof DocItemTrace) {
            DocItemTrace docItemTrace = (DocItemTrace) aDocItem;
            Trace trace = docItemTrace.getTrace();
            setChartForSingleDataSet(trace);
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
    private void setChartForSingleDataSet(Trace aTrace)
    {
        // Put ChartPane in 'DataSetMode'
        _dataSetMode = true;

        // Copy DataSet chart
        Chart chart = aTrace.getChart();
        Chart chartCopy = new ChartArchiver().copy(chart);
        Content content = chartCopy.getContent();
        while (content.getTraceCount() > 0)
            content.removeTrace(0);

        // Copy DataSet and add to ChartCopy
        Trace traceCopy = new ChartArchiver().copy(aTrace);
        content.addTrace(traceCopy);

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
     * Returns the ChartView.ContentView
     */
    public ContentView getContentView()  { return _chartView.getContentView(); }

    /**
     * Returns the inspector.
     */
    public ChartPaneInsp getInspector()  { return _insp; }

    /**
     * Returns the ChartHelper.
     */
    public ChartHelper getChartHelper()  { return _chartView.getChartHelper(); }

    /**
     * Returns the Content.
     */
    private Content getContent()
    {
        Chart chart = getChart();
        return chart.getContent();
    }

    /**
     * Returns the current Trace.
     */
    public Trace getTrace()
    {
        Content content = getContent();
        int selIndex = _traceTabView != null ? _traceTabView.getSelIndex() : -1;
        if (selIndex < 0 || selIndex >= content.getTraceCount())
            return null;
        return content.getTrace(selIndex);
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
     * Returns whether TraceTabView is showing.
     */
    public boolean isShowTraceTabView()
    {
        return _traceTabView.getParent() != null;
    }

    /**
     * Sets whether TraceTabs are showing.
     */
    public void setShowTraceTabView(boolean aValue)
    {
        if (aValue == isShowTraceTabView()) return;
        if (aValue)
            showTraceTabView();
        else hideTraceTabView();
    }

    /**
     * Show TraceTabView.
     */
    public void showTraceTabView()
    {
        // If already showing, just return
        if (isShowTraceTabView()) return;

        // Add TabView
        double chartHeight = _chartBox.getPrefHeight();
        int minTabViewHeight = _dataSetMode ? 320 : 300;
        double tabViewHeight = Math.max(_splitView.getHeight() - chartHeight - 70, minTabViewHeight);
        _splitView.addItemWithAnim(_traceTabView, tabViewHeight);

        // Make TabView the primary grower if DataSetMode
        _traceTabView.setGrowHeight(_dataSetMode);
        _chartBox.setGrowHeight(!_dataSetMode);
    }

    /**
     * Hides TraceTabView.
     */
    public void hideTraceTabView()
    {
        // If already hidden, just return
        if (!isShowTraceTabView()) return;

        _splitView.removeItemWithAnim(_traceTabView);
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
        _traceTabView = getView("TabView", TabView.class);
        _splitView.removeItem(_traceTabView);
        _traceTabView.setOwner(this);

        // Create configure ChartPaneSel
        _selHpr = new ChartPaneSel(this);

        // If ChartPane is in DataSetMode, change some things
        if (_dataSetMode) {
            //_tabView.setPrefHeight(-1); _tabView.setGrowHeight(true);
            //_chartBox.setPrefHeight(400); _chartBox.setGrowHeight(false);
            _chartBox.setPadding(20, 20, 20, 20);
        }

        // Register for DragEvents
        enableEvents(getUI(), DragEvents);

        // Register for EscapeAction
        addKeyActionHandler("EscapeAction", "ESCAPE");

        // Add ChartPaneTools
        _chartPaneTools = new ChartPaneTools(this);
        _chartPaneTools.addChartPaneTools();
        _chartBox.addEventFilter(e -> _chartPaneTools.processMouseEvent(e), MouseEvents);

        // Add ShowDataButton
        if (!_dataSetMode) {
            Button showDataButton = new Button("Show Data");
            showDataButton.setName("ShowDataButton");
            showDataButton.setLean(Pos.BOTTOM_CENTER);
            showDataButton.setManaged(false);
            showDataButton.setPadding(4, 10, 4, 10);
            showDataButton.setMargin(5, 5, 5, 5);
            showDataButton.setSizeToPrefSize();
            ViewUtils.addChild(_chartBox, showDataButton);
        }

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
        rebuildTraceTabView();

        // If DataSetMode, showTraceTabView
        if (_dataSetMode)
            runLater(() -> showTraceTabView());

        // Trigger initChartLoaded (more needs to be done when ChartView supports Loaded property)
        runLater(() -> initChartLoaded());
    }

    /**
     * Called to do initialization after chart is loaded.
     */
    private void initChartLoaded()
    {
        ChartType chartType = getChart().getType();
        if (chartType.is3D())
            registerForCubeViewClick();
    }

    /**
     * Called to register to get 3D chart CubeView mousePress.
     */
    private void registerForCubeViewClick()
    {
        ContentView contentView = _chartView.getContentView();
        View contentChild = contentView.getChildCount() > 0 ? contentView.getChild(0) : null;
        if (contentChild instanceof DataArea3D) {
            DataArea3D dataArea3D = (DataArea3D) contentChild;
            CameraView cameraView = dataArea3D.getCameraView();
            CubeView cubeView = cameraView.getCubeView();
            cubeView.addEventFilter(e -> cubeViewDidMouseRelease(e), View.MouseRelease);
        }
    }

    /**
     * Called when 3D chart CubeView gets MouseRelease.
     */
    private void cubeViewDidMouseRelease(ViewEvent anEvent)
    {
        if (anEvent.isMouseClick())
            _insp.showSceneInspector();
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
        int selTabIndex = _traceTabView.getSelIndex();
        if (selTabIndex>=0 && _traceTabView.getTabContent(selTabIndex) instanceof Label) {

            // Get Trace and create DataSetPane
            Trace trace = getContent().getTrace(selTabIndex);
            DocItemTrace docItemTrace = new DocItemTrace(trace);
            TracePane dsetPane = new TracePane(docItemTrace);

            // Set TabView content
            _traceTabView.setTabContent(dsetPane.getUI(), selTabIndex);
        }

        // Reset inspector
        _insp.resetLater();
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle DragDrop events
        if (anEvent.isDragDropEvent())
            handleDragEvent(anEvent);

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
            int selIndex = _traceTabView.getSelIndex();
            Trace trace = getContent().getTrace(selIndex);
            getSel().setSelChartPart(trace);
        }

        // Handle EscapeAction
        if (anEvent.equals("EscapeAction"))
            getSel().popSelection();

        // Handle XXXToolButton
        String name = anEvent.getName();
        if (name.endsWith("ToolButton"))
            _chartPaneTools.respondToolButton(anEvent);

        // Handle ShowDataButton
        if (anEvent.equals("ShowDataButton")) {
            boolean showData = !isShowTraceTabView();
            setShowTraceTabView(showData);
            getView("ShowDataButton").setText(showData ? "Hide Data" : "Show Data");
        }
    }

    /**
     * Called when DragEvent is over ChartPane.
     */
    private void handleDragEvent(ViewEvent anEvent)
    {
        anEvent.acceptDrag();
        anEvent.consume();

        // Handle DragDropEvent: Call handleDragDropChartsFile (with loaded file)
        if(anEvent.isDragDropEvent()) {

            // If not Drag-Drop file, just return
            Clipboard clipboard = anEvent.getClipboard();
            if (!clipboard.hasFiles())
                return;

            // If no files, just return
            List<ClipboardData> cbFiles = clipboard.getFiles();
            if (cbFiles.size() == 0)
                return;

            // If file not '.charts' or '.simple', just return
            ClipboardData cbFile = cbFiles.get(0);
            String fileName = cbFile.getName();
            String ext = FilePathUtils.getType(fileName);
            if (!ext.equals("png") && !ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("gif"))
                return;

            if (cbFile.isLoaded())
                handleDragDropImageFile(anEvent, cbFile);
            else cbFile.addLoadListener(cbd -> handleDragDropImageFile(anEvent, cbd));
            anEvent.dropComplete();
        }
    }

    /**
     * Called when DragEvent drop has loaded file.
     */
    private void handleDragDropImageFile(ViewEvent anEvent, ClipboardData aFile)
    {
        // Get image name, bytes and image
        String name = aFile.getName();
        byte[] imageBytes = aFile.getBytes();
        Image image = Image.get(imageBytes);

        // When loaded, call handleDragDropImage(image)
        if (image.isLoaded())
            handleDragDropImage(anEvent, image, name);
        else image.addLoadListener(() -> handleDragDropImage(anEvent, image, name));
    }

    /**
     * Called when DragEvent drop has loaded image.
     */
    private void handleDragDropImage(ViewEvent anEvent, Image anImage, String aName)
    {
        // Create/configure marker
        Chart chart = getChart();
        Marker marker = new Marker();
        marker.setCoordSpaceX(Marker.CoordSpace.ChartView);
        marker.setCoordSpaceY(Marker.CoordSpace.ChartView);
        marker.setImage(anImage);
        marker.setName(aName);

        // Calculate bounds and set
        Point chartXY = anEvent.getPoint(_chartView);
        double imageW = anImage.getWidth();
        double imageH = anImage.getHeight();
        double imageX = chartXY.x - Math.round(imageW / 2);
        double imageY = chartXY.y - Math.round(imageH / 2);
        marker.setBounds(imageX, imageY, imageW, imageH);

        // Add marker to chart and select
        chart.addMarker(marker);
        getSel().setSelChartPart(marker);
    }

    /**
     * Rebuilds the TraceTabView tabs.
     */
    private void rebuildTraceTabView()
    {
        // Remove current tabs
        while (_traceTabView.getTabCount() > 0)
            _traceTabView.removeTab(0);

        // Configure TabView with Chart.Traces
        Content content = getContent();
        Trace[] traces = content.getTraces();
        for (Trace trace : traces) {
            _traceTabView.addTab(trace.getName(), new Label(trace.getName()));
        }
    }

    /**
     * Called when the selection changes.
     */
    public void chartPaneSelChanged()
    {
        // If SelPart is Trace, make sure tabPane is selected
        ChartPart selPart = getSelChartPart();
        if (selPart instanceof Trace) {
            Trace trace = (Trace) selPart;
            _traceTabView.setSelIndex(trace.getIndex());
        }

        // Notify Inspector
        _insp.chartPaneSelChartPartChanged();
    }

    /**
     * Called when a ChartPart has change.
     */
    private void chartPartDidPropChange(PropChange aPC)
    {
        // Handle Content
        Object src = aPC.getSource();
        String propName = aPC.getPropName();
        if (src instanceof Content && propName == Content.Trace_Prop) {
            if (aPC.getNewValue() instanceof Trace)
                contentAddedTrace((Trace) aPC.getNewValue());
            else if (aPC.getOldValue() instanceof Trace)
                contentRemovedTraceAtIndex(aPC.getIndex());
        }
    }

    /**
     * Called when Trace is added.
     */
    private void contentAddedTrace(Trace aTrace)
    {
        rebuildTraceTabView();
        getSel().setSelChartPart(aTrace);
        showTraceTabView();
    }

    /**
     * Called when Trace is removed.
     */
    private void contentRemovedTraceAtIndex(int anIndex)
    {
        rebuildTraceTabView();

        // Get next trace to select
        Content content = getContent();
        Trace nextTrace = anIndex < content.getTraceCount() ? content.getTrace(anIndex) : null;
        if (nextTrace == null && content.getTraceCount() > 0)
            nextTrace = content.getTrace(content.getTraceCount() - 1);

        // Get next selected chartPart
        ChartPart nextSel = nextTrace != null ? nextTrace : content;
        getSel().setSelChartPart(nextSel);
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
            if (_chartPaneTools.getCurrentTool() != null)
                _chartPaneTools.paintTool(aPntr, this);

            else {
                ChartPaneSel chartSel = getSel();
                chartSel.paintSelection(aPntr, this);
            }
        }
    }
}