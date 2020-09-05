package snapcharts.app;
import snap.gfx.ShadowEffect;
import snap.view.*;
import snapcharts.model.Chart;
import snapcharts.model.ChartArchiver;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;
import snapcharts.views.ChartView;
import java.util.List;

/**
 * A class to manage charts/data in a ChartBook.
 */
public class ChartPane extends DocItemPane {
    
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

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()
    {
        return _chartView;
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
        RowView topRowView = (RowView)super.createUI();

        // Create ChartView
        _chartView = new ChartView();
        _chartView.setEffect(new ShadowEffect());

        // Create ChartBox
        _chartBox = (BoxView)topRowView.getChild("ChartBox");
        _chartBox.setContent(_chartView);
        _chartBox.setFill(ChartSetPane.BACK_FILL);

        // Create TabView
        _tabView = new TabView();
        _tabView.setPrefHeight(300);

        // If ChartPane is in DataSetMode, change some things
        if (_dataSetMode) {
            _tabView.setPrefHeight(-1);
            _tabView.setGrowHeight(true);
            _chartBox.setPrefHeight(400);
            _chartBox.setGrowHeight(false);
            _chartBox.setPadding(30, 60, 30, 60);
        }

        // Configure TopColView
        ColView topColView = (ColView)topRowView.getChild("TopColView");
        topColView.setChildren(_chartBox, _tabView);
        SplitView splitView = SplitView.makeSplitView(topColView);
        splitView.setDividerSpan(5);

        // Create/add InspectorPane
        _insp = new ChartPaneInsp(this);
        topRowView.addChild(_insp.getUI());

        // Create configure ChartPaneSel
        _selHpr = new ChartPaneSel(this);

        // Return TopRowView
        return topRowView;
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
        // Handle TabView
        if (anEvent.equals(_tabView)) {
            int selIndex = _tabView.getSelIndex();
            DataSet dset = _chartView.getDataSetList().getDataSet(selIndex);
            getSel().setSelChartPart(dset);
        }
    }
}