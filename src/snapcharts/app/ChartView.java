package snapcharts.app;
import java.text.DecimalFormat;
import snap.geom.*;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.model.Chart;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSet;

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
    
    // The ChartArea
    private ChartArea  _chartArea;
    
    // The XAxis
    ChartXAxis _axisX;
    
    // The YAxis
    ChartYAxis _axisY;
    
    // The Legend
    ChartLegend        _legend;
    
    // The object holding specific chart types
    ChartTypes         _chartTypes = new ChartTypes(this);
    
    // The view to hold ChartArea and X/Y axis views
    ChartAreaBox       _chartAreaBox;
    
    // The view to hold ChartAreaBox and Legend
    RowView            _rowView;
    
    // The chart type
    String             _type = LINE_TYPE;
    
    // The ToolTipView
    ToolTipView        _toolTipView;

    // The selected and targeted (under mouse) data point
    DataPoint _selPoint, _targPoint;

    // The runnable to trigger resetView() before layout/paint
    private Runnable  _resetViewRun, _resetViewRunShared = () -> { resetView(); _resetViewRun = null; };
    
    // Constants
    public static final String BAR_TYPE = "Bar";
    public static final String BAR3D_TYPE = "Bar3D";
    public static final String LINE_TYPE = "Line";
    public static final String PIE_TYPE = "Pie";
    public static final String SelDataPoint_Prop = "SelDataPoint";
    public static final String TargDataPoint_Prop = "TargDataPoint";
    
    // Shared
    static DecimalFormat _fmt = new DecimalFormat("#,###.##");
    
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
        _axisX = new ChartXAxis();
        _axisY = new ChartYAxis();

        // Create/add ChartAreaBox
        _chartAreaBox = new ChartAreaBox();
        _rowView.addChild(_chartAreaBox);

        // Create/set ChartArea
        setChartArea(_chartTypes.getLineChart());

        // Create/configure ChartLegend
        _legend = new ChartLegend();
        _rowView.addChild(_legend);

        // Create ToolTipView
        _toolTipView = new ToolTipView(this);

        // Set sample values
        //setTitle("Sample Growth by Sector, 2012-2018");
        getDataSet().addSeriesForNameAndValues("Sample", 1d, 2d, 2d, 3d, 4d, 5d);
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
        _chart = aChart;

        _chart.addPropChangeListener(pc -> chartDidPropChange());
        _chart.addDeepChangeListener((src,pc) -> chartDidDeepChange());
    }

    /**
     * Returns the dataset.
     */
    public DataSet getDataSet()  { return getChart().getDataSet(); }

    /**
     * Returns the type.
     */
    public String getType()  { return _type; }

    /**
     * Sets the type.
     */
    public void setType(String aType)
    {
        _type = aType;

        // Get ChartArea for type, set in ChartView and reload contents
        ChartArea chartArea = _chartTypes.getChart(aType);
        setChartArea(chartArea);
        resetLater();
    }

    /**
     * Returns the ChartArea.
     */
    public ChartArea getChartArea()  { return _chartArea; }

    /**
     * Sets the ChartArea.
     */
    protected void setChartArea(ChartArea aCA)
    {
        if (_chartArea!=null) _chartArea.deactivate();

        _chartAreaBox.setChartArea(aCA);
        _chartArea._chartView = this;
        _chartArea.activate();
    }

    /**
     * Returns the X Axis View.
     */
    public ChartXAxis getAxisX()  { return _axisX; }

    /**
     * Returns the Y Axis View.
     */
    public ChartYAxis getAxisY()  { return _axisY; }

    /**
     * Returns the Legend.
     */
    public ChartLegend getLegend()  { return _legend; }

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

        // Reset Y Axis
        String axisY_Title = chart.getAxisY().getTitle();
        getAxisY().setTitle(axisY_Title);

        // Reset Legend
        if (showLegend)
            _legend.reloadContents();

        // Reset ChartArea
        _chartArea.reactivate();

        // Trigger animate
        _chartArea.animate();
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
          ChartArea carea = _chartArea;
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
    protected void chartDidDeepChange()
    {
        resetLater();
    }

    /**
     * A class to layout ChartArea and X/Y axis views.
     */
    private class ChartAreaBox extends ParentView {

        /** Create ChartAreaBox. */
        public ChartAreaBox()  { setGrowWidth(true); setGrowHeight(true); setChildren(_axisY, _axisX); }

        /** Sets the ChartArea. */
        protected void setChartArea(ChartArea aCA)
        {
            if (_chartArea!=null) removeChild(_chartArea);
            addChild(_chartArea = aCA, 1);
            _axisY._chartArea = _axisX._chartArea = aCA;
        }

        /** Calculates the preferred width. */
        protected double getPrefWidthImpl(double aH)
        {
            double pw = _chartArea.getPrefWidth();
            if (_axisY.isVisible()) pw += _axisY.getPrefWidth();
            return pw;
        }

        /** Calculates the preferred height. */
        protected double getPrefHeightImpl(double aW)
        {
            double ph = _chartArea.getPrefHeight();
            if (_axisX.isVisible()) ph += _axisX.getPrefHeight();
            return ph;
        }

        /** Actual method to layout children. */
        protected void layoutImpl()
        {
            // Set chart area height first, since height can effect yaxis label width
            double pw = getWidth(), ph = getHeight();
            double ah = _axisX.isVisible()? _axisX.getPrefHeight() : 0;
            _chartArea.setHeight(ph - ah);

            // Now set bounds of areay, xaxis and yaxis
            double aw = _axisY.isVisible()? _axisY.getPrefWidth(ph - ah) : 0;
            double cw = pw - aw, ch = ph - ah;
            _chartArea.setBounds(aw,0,cw,ch);
            _axisX.setBounds(aw,ch,cw,ah);
            _axisY.setBounds(0,0,aw,ch);
        }
    }
}