package snapcharts.model;
import snap.geom.*;
import snap.gfx.Color;
import snap.util.*;

/**
 * A view to render a chart.
 */
public class Chart extends ChartPart {

    // The ChartDoc that owns this chart
    private Doc _doc;

    // The chart type
    private ChartType  _type = ChartType.LINE;

    // The title
    private String  _title;

    // The subtitle
    private String  _subtitle;

    // Whether legend is showing
    private boolean  _showLegend;

    // Whether to show partial Y axis intervals if min/max don't include zero
    private boolean  _showPartialY;

    // The Header
    private Header  _header;

    // The X Axis
    private AxisX  _axisX;

    // The Y Axis
    private AxisY  _axisY;

    // The DataSet
    private DataSetList _dsetList = new DataSetList(this);

    // The Legend
    private Legend  _legend;

    // The dataset colors
    private Color  _colors[] = COLORS;

    // The dataset shapes
    private Shape _markerShapes[];

    // The object holding specific chart types
    private AreaTypes _areaTypes = new AreaTypes(this);

    // Property constants
    public static final String Type_Prop = "Type";
    public static final String Title_Prop = "Title";
    public static final String Subtitle_Prop = "Subtitle";
    public static final String Colors_Prop = "Colors";
    public static final String ShowLegend_Prop = "ShowLegend";
    public static final String ShowPartialY_Prop = "ShowPartialY_Prop";

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

        // Create/set Header
        _header = new Header();
        _header._chart = this;
        _header.addPropChangeListener(pc -> chartPartDidPropChange(pc));

        // Create/set X Axis
        _axisX = new AxisX();
        _axisX._chart = this;
        _axisX.addPropChangeListener(pc -> chartPartDidPropChange(pc));

        // Create/set Y Axis
        _axisY = new AxisY();
        _axisY._chart = this;
        _axisY.addPropChangeListener(pc -> chartPartDidPropChange(pc));

        // Create/set Legend
        _legend = new Legend();
        _legend._chart = this;
        _legend.addPropChangeListener(pc -> chartPartDidPropChange(pc));

        // Start listening to DataSet changes
        _dsetList.addPropChangeListener(pc -> dataSetDidPropChange(pc));
    }

    /**
     * Returns the chart doc.
     */
    public Doc getDoc() { return _doc; }

    /**
     * Sets the doc.
     */
    protected void setDoc(Doc aDoc)
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
     * Returns the title.
     */
    public String getTitle()  { return _title; }

    /**
     * Sets the title.
     */
    public void setTitle(String aStr)
    {
        if (SnapUtils.equals(aStr, getTitle())) return;
        firePropChange(Title_Prop, _title, _title = aStr);
    }

    /**
     * Returns the subtitle.
     */
    public String getSubtitle()  { return _subtitle; }

    /**
     * Sets the subtitle.
     */
    public void setSubtitle(String aStr)
    {
        if (SnapUtils.equals(aStr, getSubtitle())) return;
        firePropChange(Subtitle_Prop, _subtitle, _subtitle = aStr);
    }

    /**
     * Returns whether to show legend.
     */
    public boolean isShowLegend()  { return _showLegend; }

    /**
     * Sets whether to show legend.
     */
    public void setShowLegend(boolean aValue)
    {
        if (aValue==isShowLegend()) return;
        firePropChange(ShowLegend_Prop, _showLegend, _showLegend=aValue);
    }

    /**
     * Returns whether to show partial Y axis intervals if min/max don't include zero.
     */
    public boolean isShowPartialY()  { return _showPartialY; }

    /**
     * Returns whether to show partial Y axis intervals if min/max don't include zero.
     */
    public void setShowPartialY(boolean aValue)
    {
        if (aValue==_showPartialY) return;
        firePropChange(ShowPartialY_Prop, _showPartialY, _showPartialY = aValue);
    }

    /**
     * Returns the Header object.
     */
    public Header getHeader()  { return _header; }

    /**
     * Returns the X axis object.
     */
    public AxisX getAxisX()  { return _axisX; }

    /**
     * Returns the Y axis object.
     */
    public AxisY getAxisY()  { return _axisY; }

    /**
     * Returns the Axis for given type.
     */
    public Axis getAxisForType(AxisType anAxisType)
    {
        switch (anAxisType) {
            case X: return getAxisX();
            case Y: return getAxisY();
            default: return null;
        }
    }

    /**
     * Returns the Legend object.
     */
    public Legend getLegend()  { return _legend; }

    /**
     * Returns the DataSetList.
     */
    public DataSetList getDataSetList()  { return _dsetList; }

    /**
     * Adds a new dataset.
     */
    public void addDataSet(DataSet aDataSet)
    {
        _dsetList.addDataSet(aDataSet);
    }

    /**
     * Returns the start value of the dataset.
     */
    public int getDataSetStartValue()  { return _dsetList.getStartValue(); }

    /**
     * Sets the start value of the dataset.
     */
    public void setDataSetStartValue(int aValue)  { _dsetList.setStartValue(aValue); }

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
     * Returns the dataset color at index.
     */
    public Color getColor(int anIndex)
    {
        if (anIndex<_colors.length) return _colors[anIndex];
        return COLORS[(anIndex - _colors.length)%COLORS.length];
    }

    /**
     * Returns the dataset shape at index.
     */
    public Shape getMarkerShape(int anIndex)
    {
        switch (getType()) {
            case LINE: return getMarkerShapes()[anIndex];
            default: return getMarkerShapes()[0];
        }
    }

    /**
     * Returns the marker shapes.
     */
    public Shape[] getMarkerShapes()
    {
        if (_markerShapes!=null) return _markerShapes;
        Shape shp0 = new Ellipse(0,0,8,8);
        Shape shp1 = new Polygon(4,0,8,4,4,8,0,4);
        Shape shp2 = new Rect(0,0,8,8);
        Shape shp3 = new Polygon(4,0,8,8,0,8);
        Shape shp4 = new Polygon(0,0,8,0,4,8);
        return _markerShapes = new Shape[] { shp0, shp1, shp2, shp3, shp4 };
    }

    /**
     * Returns the AreaTypes object.
     */
    public AreaTypes getAreaTypes()  { return _areaTypes; }

    /**
     * Called when chart part has prop change.
     */
    protected void chartPartDidPropChange(PropChange aPC)
    {
        _pcs.fireDeepChange(this, aPC);
    }

    /**
     * Called when dataset has prop change.
     */
    protected void dataSetDidPropChange(PropChange aPC)
    {
        _pcs.fireDeepChange(this, aPC);
    }

    /**
     * Add DeepChange listener.
     */
    public void addDeepChangeListener(DeepChangeListener aDCL)
    {
        if (_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        _pcs.addDeepChangeListener(aDCL);
    }

    /**
     * Remove DeepChange listener.
     */
    public void removeDeepChangeListener(DeepChangeListener aPCL)  { _pcs.removeDeepChangeListener(aPCL); }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive Type, Title, Subtitle
        e.add(Type_Prop, getType());
        if (getTitle()!=null && getTitle().length()>0)
            e.add(Title_Prop, getTitle());
        if (getSubtitle()!=null && getSubtitle().length()>0)
            e.add(Subtitle_Prop, getSubtitle());

        // Archive ShowLegend, ShowPartialY
        if (isShowLegend())
            e.add(ShowLegend_Prop, isShowLegend());
        if (isShowPartialY())
            e.add(ShowPartialY_Prop, isShowPartialY());

        // Archive AxisX, AxisY
        e.add(anArchiver.toXML(_axisX));
        e.add(anArchiver.toXML(_axisY));

        // Archive DataSetList
        e.add(anArchiver.toXML(_dsetList));

        // Return element
        return e;
    }

    /**
     * Unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic attributes
        super.fromXML(anArchiver, anElement);

        // Unarchive Type, Title, Subtitle
        setType(ChartType.get(anElement.getAttributeValue(Type_Prop)));
        setTitle(anElement.getAttributeValue(Title_Prop));
        setSubtitle(anElement.getAttributeValue(Subtitle_Prop));

        // Unarchive ShowLegend, ShowPartialY
        if (anElement.hasAttribute(ShowLegend_Prop))
            setShowLegend(anElement.getAttributeBoolValue(ShowLegend_Prop));
        if (anElement.hasAttribute(ShowPartialY_Prop))
            setShowPartialY(anElement.getAttributeBoolValue(ShowPartialY_Prop));

        // Unarchive AxisX, AxisY
        XMLElement axisX_XML = anElement.get("AxisX");
        if (axisX_XML!=null)
            anArchiver.fromXML(axisX_XML, _axisX, this);
        XMLElement axisY_XML = anElement.get("AxisY");
        if (axisY_XML!=null)
            anArchiver.fromXML(axisY_XML, _axisY, this);

        // Unarchive DataSetList
        XMLElement dsetListXML = anElement.get("DataSetList");
        if (dsetListXML!=null)
            anArchiver.fromXML(dsetListXML, _dsetList, this);

        // Return this part
        return this;
    }
}