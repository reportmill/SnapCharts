package snapcharts.model;
import snap.geom.*;
import snap.gfx.Color;
import snap.util.*;

/**
 * A view to render a chart.
 */
public class Chart extends ChartPart {

    // The ChartDoc that owns this chart
    private ChartDoc  _doc;

    // The chart type
    private ChartType  _type = ChartType.LINE;

    // The title
    private String  _titleView;

    // The subtitle
    private String  _subtitleView;

    // The object holding specific chart types
    private AreaTypes _areaTypes = new AreaTypes(this);

    // The XAxis
    private AxisX _axisX;

    // The YAxis
    private AxisY _axisY;

    // Whether legend is showing
    private boolean  _showLegend;

    // Whether to show partial Y axis intervals if min/max don't include zero
    private boolean  _showPartialY;

    // The graph colors
    private Color  _colors[] = COLORS;

    // The series shapes
    private Shape _markerShapes[];

    // The DataSet
    private DataSet  _dataSet = new DataSet(this);

    // Property constants
    public static final String Type_Prop = "Type";
    public static final String Title_Prop = "Title";
    public static final String Subtitle_Prop = "Subtitle";
    public static final String Colors_Prop = "Colors";
    public static final String ShowLegend_Prop = "ShowLegend";

    // Constants
    public static final String SelDataPoint_Prop = "SelDataPoint";
    public static final String TargDataPoint_Prop = "TargDataPoint";

    // Colors
    static Color    COLORS[] = new Color[] { Color.get("#88B4E7"), Color.get("#434348"), Color.get("#A6EB8A"),
        Color.get("#EBA769"), Color.get("#8185E2"), Color.get("#E06681"), Color.get("#E1D369"),
        Color.get("#4A8E8E"), Color.get("#E26561") };

    /**
     * Creates a ChartView.
     */
    public Chart()
    {
        // Set chart
        _chart = this;

        // Add X Axis
        _axisX = new AxisX();
        _axisX._chart = this;
        _axisX.addPropChangeListener(pc -> chartPartDidPropChange(pc));

        // Add Y Axis
        _axisY = new AxisY();
        _axisY._chart = this;
        _axisY.addPropChangeListener(pc -> chartPartDidPropChange(pc));
    }

    /**
     * Returns the chart doc.
     */
    public ChartDoc getDoc() { return _doc; }

    /**
     * Sets the doc.
     */
    protected void setDoc(ChartDoc aDoc)
    {
        _doc = aDoc;
    }

    /**
     * Returns the type.
     */
    public ChartType getType()  { return _type; }

    /**
     * Sets the type.
     */
    public void setType(ChartType aType)
    {
        if (aType==getType()) return;
        firePropChange(Type_Prop, _type, _type = aType);
    }

    /**
     * Returns the AreaTypes object.
     */
    public AreaTypes getAreaTypes()  { return _areaTypes; }

    /**
     * Returns the title.
     */
    public String getTitle()  { return _titleView; }

    /**
     * Sets the title.
     */
    public void setTitle(String aStr)
    {
        if (SnapUtils.equals(aStr, getTitle())) return;
        firePropChange(Title_Prop, _titleView, _titleView = aStr);
    }

    /**
     * Returns the subtitle.
     */
    public String getSubtitle()  { return _subtitleView; }

    /**
     * Sets the subtitle.
     */
    public void setSubtitle(String aStr)
    {
        if (SnapUtils.equals(aStr, getSubtitle())) return;
        firePropChange(Subtitle_Prop, _subtitleView, _subtitleView = aStr);
    }

    /**
     * Returns the X axis object.
     */
    public AxisX getAxisX()  { return _axisX; }

    /**
     * Returns the Y axis object.
     */
    public AxisY getAxisY()  { return _axisY; }

    /**
     * Returns whether to show legend.
     */
    public boolean isShowLegend()  { return _showLegend; }

    /**
     * Sets whether to show legend.
     */
    public void setShowLegend(boolean aValue)
    {
        if(aValue==isShowLegend()) return;
        firePropChange(ShowLegend_Prop, _showLegend, _showLegend=aValue);
    }

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
        _showPartialY = aValue;
        //reloadContents(true);
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
        firePropChange(Colors_Prop, _colors, _colors = theColors);
        //reloadContents(true);
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
            case LINE: return getMarkerShapes()[anIndex];
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
     * Returns the parent part.
     */
    public ChartPart getParent() { return getDoc(); }

    /**
     * Called when chart part has prop change.
     */
    protected void chartPartDidPropChange(PropChange aPC)
    {
        _pcs.fireDeepChange(this, aPC);
    }

    /**
     * Add DeepChange listener.
     */
    public void addDeepChangeListener(DeepChangeListener aDCL)
    {
        if(_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        _pcs.addDeepChangeListener(aDCL);
    }

    /**
     * Remove DeepChange listener.
     */
    public void removeDeepChangeListener(DeepChangeListener aPCL)  { _pcs.removeDeepChangeListener(aPCL); }
}