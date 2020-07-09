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
    ChartXAxis         _xaxis;
    
    // The YAxis
    ChartYAxis         _yaxis;
    
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
    
    // Constants
    public static final String BAR_TYPE = "Bar";
    public static final String BAR3D_TYPE = "Bar3D";
    public static final String LINE_TYPE = "Line";
    public static final String PIE_TYPE = "Pie";
    public static final String SelDataPoint_Prop = "SelDataPoint";
    public static final String TargDataPoint_Prop = "TargDataPoint";
    
    // Colors
    static Color    COLORS[] = new Color[] { Color.get("#88B4E7"), Color.get("#434348"), Color.get("#A6EB8A"),
        Color.get("#EBA769"), Color.get("#8185E2"), Color.get("#E06681"), Color.get("#E1D369"),
        Color.get("#4A8E8E"), Color.get("#E26561") };
    
    // Shared
    static DecimalFormat _fmt = new DecimalFormat("#,###.##");
    
/**
 * Creates a ChartView.
 */
public ChartView()
{
    // Create new chart
    _chart = new Chart();

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
    _xaxis = new ChartXAxis();
    _yaxis = new ChartYAxis();
    
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
    reloadContents(true);
}

/**
 * Returns the Chart.
 */
public Chart getChart()  { return _chart; }

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
    reloadContents(true);
}

/**
 * Returns the title.
 */
public String getTitle()  { return _titleView.getText(); }

/**
 * Sets the title.
 */
public void setTitle(String aStr)
{
    _titleView.setText(aStr);
    _titleView.setVisible(aStr!=null && aStr.length()>0);
}

/**
 * Returns the subtitle.
 */
public String getSubtitle()  { return _subtitleView.getText(); }

/**
 * Sets the subtitle.
 */
public void setSubtitle(String aStr)
{
    _subtitleView.setText(aStr);
    _subtitleView.setVisible(aStr!=null && aStr.length()>0);
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
    if(_chartArea!=null) _chartArea.deactivate();
    
    _chartAreaBox.setChartArea(aCA);
    _chartArea._chartView = this;
    _chartArea.activate();
}

/**
 * Returns the XAxis View.
 */
public ChartXAxis getXAxis()  { return _xaxis; }

/**
 * Returns the YAxis View.
 */
public ChartYAxis getYAxis()  { return _yaxis; }

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
    if(aValue==isShowLegend()) return;
    _legend.setVisible(aValue);
}

/**
 * Returns the tool tip view.
 */
public ToolTipView getToolTipView()  { return _toolTipView; }

/**
 * Returns the dataset.
 */
public DataSet getDataSet()  { return getChart().getDataSet(); }

/**
 * Returns the selected data point.
 */
public DataPoint getSelDataPoint()  { return _selPoint; }

/**
 * Sets the selected data point.
 */
public void setSelDataPoint(DataPoint aDP)
{
    if(SnapUtils.equals(aDP, _selPoint)) return;
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
    if(SnapUtils.equals(aDP, _targPoint)) return;
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
 * Reloads chart view contents.
 */
public void reloadContents(boolean doAnim)
{
    _legend.reloadContents();
    _chartArea.reactivate();
    if(doAnim) _chartArea.animate();
    _yaxis.repaint();
    _xaxis.repaint();
}

/**
 * A class to layout ChartArea and X/Y axis views.
 */
private class ChartAreaBox extends ParentView {
    
    /** Create ChartAreaBox. */
    public ChartAreaBox()  { setGrowWidth(true); setGrowHeight(true); setChildren(_yaxis, _xaxis); }
    
    /** Sets the ChartArea. */
    protected void setChartArea(ChartArea aCA)
    {
        if(_chartArea!=null) removeChild(_chartArea);
        addChild(_chartArea = aCA, 1);
        _yaxis._chartArea = _xaxis._chartArea = aCA;
    }
    
    /** Calculates the preferred width. */
    protected double getPrefWidthImpl(double aH)
    {
        double pw = _chartArea.getPrefWidth();
        if(_yaxis.isVisible()) pw += _yaxis.getPrefWidth();
        return pw;
    }

    /** Calculates the preferred height. */
    protected double getPrefHeightImpl(double aW)
    {
        double ph = _chartArea.getPrefHeight();
        if(_xaxis.isVisible()) ph += _xaxis.getPrefHeight();
        return ph;
    }

    /** Actual method to layout children. */
    protected void layoutImpl()
    {
        // Set chart area height first, since height can effect yaxis label width
        double pw = getWidth(), ph = getHeight();
        double ah = _xaxis.isVisible()? _xaxis.getPrefHeight() : 0;
        _chartArea.setHeight(ph - ah);
        
        // Now set bounds of areay, xaxis and yaxis
        double aw = _yaxis.isVisible()? _yaxis.getPrefWidth(ph - ah) : 0;
        double cw = pw - aw, ch = ph - ah;
        _chartArea.setBounds(aw,0,cw,ch);
        _xaxis.setBounds(aw,ch,cw,ah);
        _yaxis.setBounds(0,0,aw,ch);
    }
}

}