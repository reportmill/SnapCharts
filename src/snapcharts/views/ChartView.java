package snapcharts.views;
import snap.geom.*;
import snap.gfx.*;
import snap.util.DeepChangeListener;
import snap.util.PropChange;
import snap.util.PropChangeListener;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.app.ToolTipView;
import snapcharts.model.Chart;
import snapcharts.model.ChartType;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;

/**
 * A view to render a chart.
 */
public class ChartView extends ColView {

    // The Chart
    private Chart  _chart;

    // The title
    private StringView  _titleView;
    
    // The subtitle
    private StringView  _subtitleView;
    
    // The DataView
    private DataView  _dataView;
    
    // The XAxis
    protected AxisViewX  _axisX;
    
    // The YAxis
    protected AxisViewY  _axisY;
    
    // The Legend
    private LegendView  _legend;
    
    // The view to hold DataView and X/Y axis views
    private DataViewBox  _dataViewBox;
    
    // The view to hold DataViewBox and Legend
    private RowView  _rowView;

    // The ToolTipView
    private ToolTipView  _toolTipView;

    // The selected and targeted (under mouse) data point
    private DataPoint  _selPoint, _targPoint;

    // The runnable to trigger resetView() before layout/paint
    private Runnable  _resetViewRun, _resetViewRunShared = () -> { resetView(); _resetViewRun = null; };

    // The PropChangeListener
    private PropChangeListener  _pcl = pc -> chartDidPropChange();
    
    // The DeepChangeListener
    private DeepChangeListener  _dcl = (src,pc) -> chartDidDeepChange(pc);

    // Constants
    public static final String SelDataPoint_Prop = "SelDataPoint";
    public static final String TargDataPoint_Prop = "TargDataPoint";
    
    /**
     * Creates a ChartView.
     */
    public ChartView()
    {
        // Create new chart
        setChart(new Chart());

        // Configure this view
        setPadding(10,10,10,10); setAlign(Pos.CENTER); setSpacing(8); setGrowWidth(true);
        setFill(Color.WHITE);

        // Create configure TitleView
        _titleView = new StringView();
        _titleView.setFont(Font.Arial14.getBold().deriveFont(20));
        addChild(_titleView);

        // Create configure SubtitleView
        _subtitleView = new StringView(); _subtitleView.setTextFill(Color.GRAY);
        _subtitleView.setFont(Font.Arial12.getBold());
        addChild(_subtitleView);

        // Create RowView
        _rowView = new RowView(); _rowView.setAlign(Pos.CENTER_LEFT); _rowView.setSpacing(8);
        _rowView.setGrowWidth(true); _rowView.setGrowHeight(true);
        addChild(_rowView);

        // Create XAxis and YAxis
        _axisX = new AxisViewX();
        _axisY = new AxisViewY();

        // Create/add DataViewBox
        _dataViewBox = new DataViewBox();
        _rowView.addChild(_dataViewBox);

        // Create/set DataView
        setDataView(DataView.createDataViewForType(ChartType.LINE));

        // Create/configure ChartLegend
        _legend = new LegendView();
        _rowView.addChild(_legend);

        // Create ToolTipView
        _toolTipView = new ToolTipView(this);

        // Set sample values
        //setTitle("Sample Growth by Sector, 2012-2018");
        getDataSetList().addDataSetForNameAndValues("Sample", 1d, 2d, 2d, 3d, 4d, 5d);
        resetLater();
    }

    /**
     * Returns the Chart.
     */
    public Chart getChart()  { return _chart; }

    /**
     * Sets the Chart.
     */
    public void setChart(Chart aChart)
    {
        // If already set, just return
        if (aChart==_chart) return;

        // Stop listening to old chart
        if (_chart!=null) {
            _chart.removePropChangeListener(_pcl);
            _chart.removeDeepChangeListener(_dcl);
        }

        // Set Chart
        _chart = aChart;

        // Start listening to new chart
        _chart.addPropChangeListener(_pcl);
        _chart.addDeepChangeListener(_dcl);

        // Reset
        resetLater();
    }

    /**
     * Returns the dataset.
     */
    public DataSetList getDataSetList()  { return getChart().getDataSetList(); }

    /**
     * Returns the DataView.
     */
    public DataView getDataView()  { return _dataView; }

    /**
     * Sets the DataView.
     */
    protected void setDataView(DataView aDataView)
    {
        if (_dataView !=null) _dataView.deactivate();

        _dataViewBox.setDataView(aDataView);
        _dataView.setChartView(this);
        _dataView.activate();
    }

    /**
     * Sets the type.
     */
    public void setDataViewForType(ChartType aType)
    {
        // If already set, just return
        if (aType==getDataView().getType()) return;

        // Get DataView for type, set in ChartView and reload contents
        DataView dataView = DataView.createDataViewForType(aType);
        setDataView(dataView);
        resetLater();
    }

    /**
     * Returns the X Axis View.
     */
    public AxisViewX getAxisX()  { return _axisX; }

    /**
     * Returns the Y Axis View.
     */
    public AxisViewY getAxisY()  { return _axisY; }

    /**
     * Returns the Legend.
     */
    public LegendView getLegend()  { return _legend; }

    /**
     * Returns whether to show legend.
     */
    public boolean isShowLegend()  { return _legend.isVisible(); }

    /**
     * Sets whether to show legend.
     */
    public void setShowLegend(boolean aValue)
    {
        if (aValue==isShowLegend()) return;
        _legend.setVisible(aValue);
    }

    /**
     * Returns the tool tip view.
     */
    public ToolTipView getToolTipView()  { return _toolTipView; }

    /**
     * Called to reset view from Chart.
     */
    protected void resetView()
    {
        // Get info
        Chart chart = getChart();

        // Reset Type
        setDataViewForType(chart.getType());

        // Reset Title
        String title = chart.getTitle();
        _titleView.setText(title);
        _titleView.setVisible(title!=null && title.length()>0);

        // Reset Subtitle
        String subtitle = chart.getSubtitle();
        _subtitleView.setText(subtitle);
        _subtitleView.setVisible(subtitle!=null && subtitle.length()>0);

        // Reset ShowLegend
        boolean showLegend = chart.isShowLegend();
        setShowLegend(showLegend);

        // Reset X Axis
        getAxisX().resetView();

        // Reset Y Axis
        getAxisY().resetView();

        // Reset Legend
        if (showLegend)
            _legend.reloadContents();

        // Reset DataView
        _dataView.reactivate();

        // Trigger animate
        _dataView.animate();
        _axisY.repaint();
        _axisX.repaint();
    }

    /**
     * Registers view to reset later.
     */
    public void resetLater()
    {
        if (_resetViewRun!=null) return;
        ViewUpdater updater = getUpdater();
        if (updater!=null)
            updater.runBeforeUpdate(_resetViewRun = _resetViewRunShared);
    }

    @Override
    protected void setShowing(boolean aValue)
    {
        if (aValue==isShowing()) return; super.setShowing(aValue);
        if (aValue)
            resetLater();
    }

    /**
     * Returns the selected data point.
     */
    public DataPoint getSelDataPoint()  { return _selPoint; }

    /**
     * Sets the selected data point.
     */
    public void setSelDataPoint(DataPoint aDP)
    {
        if (SnapUtils.equals(aDP, _selPoint)) return;
        firePropChange(SelDataPoint_Prop, _selPoint, _selPoint = aDP);
        repaint();
    }

    /**
     * Returns the targeted data point.
     */
    public DataPoint getTargDataPoint()  { return _targPoint; }

    /**
     * Sets the targeted data point.
     */
    public void setTargDataPoint(DataPoint aDP)
    {
        if (SnapUtils.equals(aDP, _targPoint)) return;
        firePropChange(TargDataPoint_Prop, _targPoint, _targPoint = aDP);
        _toolTipView.reloadContents();
    }

    /**
     * Returns the given data point in local coords.
     */
    public Point dataPointInLocal(DataPoint aDP)
    {
          DataView carea = _dataView;
          Point pnt = carea.dataPointInLocal(aDP);
          return carea.localToParent(pnt.x, pnt.y, this);
    }

    /**
     * Called when Chart has a PropChange.
     */
    protected void chartDidPropChange()
    {
        resetLater();
    }

    /**
     * Called when Chart has a DeppChange.
     */
    protected void chartDidDeepChange(PropChange aPC)
    {
        resetLater();

        // If DataSet change, clear caches
        Object src = aPC.getSource();
        if (src instanceof DataSet || src instanceof DataSetList) {
            getDataView().clearCache();
        }
    }

    /**
     * A class to layout DataView and X/Y axis views.
     */
    private class DataViewBox extends ParentView {

        /** Create DataViewBox. */
        public DataViewBox()  { setGrowWidth(true); setGrowHeight(true); setChildren(_axisY, _axisX); }

        /** Sets the DataView. */
        protected void setDataView(DataView aCA)
        {
            if (_dataView !=null) removeChild(_dataView);
            addChild(_dataView = aCA, 1);
            _axisY._dataView = _axisX._dataView = aCA;
        }

        /** Calculates the preferred width. */
        protected double getPrefWidthImpl(double aH)
        {
            double pw = _dataView.getPrefWidth();
            if (_axisY.isVisible()) pw += _axisY.getPrefWidth();
            return pw;
        }

        /** Calculates the preferred height. */
        protected double getPrefHeightImpl(double aW)
        {
            double ph = _dataView.getPrefHeight();
            if (_axisX.isVisible()) ph += _axisX.getPrefHeight();
            return ph;
        }

        /** Actual method to layout children. */
        protected void layoutImpl()
        {
            // Set chart area height first, since height can effect yaxis label width
            double pw = getWidth(), ph = getHeight();
            double ah = _axisX.isVisible()? _axisX.getPrefHeight() : 0;
            _dataView.setHeight(ph - ah);

            // Now set bounds of areay, xaxis and yaxis
            double aw = _axisY.isVisible()? _axisY.getPrefWidth(ph - ah) : 0;
            double cw = pw - aw, ch = ph - ah;
            _dataView.setBounds(aw,0,cw,ch);
            _axisX.setBounds(aw,ch,cw,ah);
            _axisY.setBounds(0,0,aw,ch);
        }
    }
}