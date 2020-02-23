package snap.charts;
import java.text.DecimalFormat;

import snap.geom.*;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;
import snap.web.WebURL;

/**
 * A view to render a chart.
 */
public class ChartView extends ColView {
    
    // The title
    StringView         _titleView;
    
    // The subtitle
    StringView         _subtitleView;
    
    // The ChartArea
    ChartArea          _chartArea;
    
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
    
    // The DataSet
    DataSet            _dataSet = new DataSet(this);
    
    // Whether to show partial Y axis intervals if min/max don't include zero
    boolean            _showPartialY;
    
    // The graph colors
    Color              _colors[] = COLORS;
    
    // The series shapes
    Shape _markerShapes[];
    
    // The selected and targeted (under mouse) data point
    DataPoint          _selPoint, _targPoint;
    
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
    _dataSet.addSeriesForNameAndValues("Sample", 1d, 2d, 2d, 3d, 4d, 5d);
    reloadContents(true);
}

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
 * Returns the ChartTypes object.
 */
public ChartTypes getChartTypes()  { return _chartTypes; }

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
public DataSet getDataSet()  { return _dataSet; }

/**
 * Adds a new series.
 */
public void addSeries(DataSeries aSeries)  { _dataSet.addSeries(aSeries); }

/**
 * Returns the start of the series.
 */
public int getSeriesStart()  { return _dataSet.getSeriesStart(); }

/**
 * Sets the start of the series.
 */
public void setSeriesStart(int aValue)  { _dataSet.setSeriesStart(aValue); }

/**
 * Returns whether to show partial Y axis intervals if min/max don't include zero. 
 */
public boolean isShowPartialY()  { return _showPartialY; }

/**
 * Returns whether to show partial Y axis intervals if min/max don't include zero. 
 */
public void setShowPartialY(boolean aValue)
{
    if(aValue==_showPartialY) return;
    _showPartialY = aValue; reloadContents(true);
}

/**
 * Returns the colors.
 */
public Color[] getColors()  { return _colors; }

/**
 * Sets the graph colors.
 */
public void setColors(Color ... theColors)
{
    _colors = theColors;
    reloadContents(true);
}

/**
 * Returns the series color at index.
 */
public Color getColor(int anIndex)
{
    if(anIndex<_colors.length) return _colors[anIndex];
    return COLORS[(anIndex - _colors.length)%COLORS.length];
}

/**
 * Returns the series shape at index.
 */
public Shape getMarkerShape(int anIndex)
{
    switch(getType()) {
        case LINE_TYPE: return getMarkerShapes()[anIndex];
        default: return getMarkerShapes()[0];
    }
}

/**
 * Returns the marker shapes.
 */
public Shape[] getMarkerShapes()
{
    if(_markerShapes!=null) return _markerShapes;
    Shape shp0 = new Ellipse(0,0,8,8);
    Shape shp1 = new Polygon(4,0,8,4,4,8,0,4);
    Shape shp2 = new Rect(0,0,8,8);
    Shape shp3 = new Polygon(4,0,8,8,0,8);
    Shape shp4 = new Polygon(0,0,8,0,4,8);
    return _markerShapes = new Shape[] { shp0, shp1, shp2, shp3, shp4 };
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
 * Loads the ChartView from JSON source.
 */
public void loadFromSource(Object aSrc)
{
    WebURL url = WebURL.getURL(aSrc);
    String jsonText = url.getText();
    loadFromString(jsonText);
}

/**
 * Loads the ChartView from JSON string.
 */
public void loadFromString(String aStr)
{
    _dataSet.clear();
    ChartParser parser = new ChartParser(this);
    parser.parseString(aStr);
    if(_dataSet.isEmpty()) _dataSet.addSeriesForNameAndValues("Sample", 1d, 2d, 3d, 3d, 4d, 5d);
    reloadContents(true);
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