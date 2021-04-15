package snapcharts.model;
import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.Paint;
import snap.util.*;
import snapcharts.doc.ChartArchiver;
import snapcharts.doc.Doc;

/**
 * A view to render a chart.
 */
public class Chart extends ChartPart {

    // The ChartDoc that owns this chart
    private Doc _doc;

    // The chart type
    private ChartType  _type = ChartType.LINE;

    // The Header
    private Header  _header;

    // The X Axis
    private AxisX  _axisX;

    // The Y Axis
    private AxisY  _axisY;

    // The Y2 Axis
    private AxisY  _axisY2;

    // The Y3 Axis
    private AxisY  _axisY3;

    // The Y4 Axis
    private AxisY  _axisY4;

    // The Z Axis
    private AxisZ _axisZ;

    // The ColorBar
    private ColorBar  _colorBar;

    // The DataSet
    private DataSetList _dsetList = new DataSetList(this);

    // The Legend
    private Legend  _legend;

    // The dataset colors
    private Color[]  _colors = GT_COLORS;

    // The dataset shapes
    private Shape[] _symbolShapes;

    // The object holding specific chart type properties
    private ChartStyleHpr _chartStyleHpr = new ChartStyleHpr(this);

    // Property constants
    public static final String Type_Prop = "Type";
    public static final String Colors_Prop = "Colors";

    // Colors
    private static Color[]  COLORS = new Color[] {
        Color.get("#88B4E7"), Color.get("#434348"), Color.get("#A6EB8A"),
        Color.get("#EBA769"), Color.get("#8185E2"), Color.get("#E06681"), Color.get("#E1D369"),
        Color.get("#4A8E8E"), Color.get("#E26561")
    };

    // GTColors
    private static Color[] GT_COLORS = new Color[] {
            new Color(255,0,0),
            new Color(0, 0, 255),
            new Color(0,185,25),
            new Color(255,0,255),
            new Color(0,210,240),
            new Color(153,5,190),
            new Color(225,116,79),
            new Color(255,0,140),
            new Color(64,64,64),
            new Color(199,199,0),
            new Color(221,100,251),
            new Color(0,127,127),
            new Color(178,0,86),
            new Color(255,200,0),
            new Color(0,127,192),
            new Color(179,21,21),
            new Color(0,60,127),
            new Color(60,191,145),
            new Color(60,191,145),
            new Color(252,182,239),
            new Color(0,255,0),
            new Color(58,122,80),
            new Color(127,150,3),
            new Color(255,255,0),
            new Color(134,134,134)
    };

    /**
     * Creates a ChartView.
     */
    public Chart()
    {
        // Set chart
        _chart = this;

        // Configure
        setFill((Paint) getPropDefault(Fill_Prop));

        // Create/set Header
        _header = new Header();
        _header._chart = this;
        _header.addPropChangeListener(pc -> chartPartDidPropChange(pc));

        // Create/set X Axis
        _axisX = new AxisX();
        _axisX._chart = this;
        _axisX.addPropChangeListener(pc -> chartPartDidPropChange(pc));

        // Create/set Y Axis
        _axisY = new AxisY(this, AxisType.Y);
        _axisY.addPropChangeListener(pc -> chartPartDidPropChange(pc));

        // Create/set Y2 Axis
        _axisY2 = new AxisY(this, AxisType.Y2);
        _axisY2.addPropChangeListener(pc -> chartPartDidPropChange(pc));

        // Create/set Y3 Axis
        _axisY3 = new AxisY(this, AxisType.Y3);
        _axisY3.addPropChangeListener(pc -> chartPartDidPropChange(pc));

        // Create/set Y Axis
        _axisY4 = new AxisY(this, AxisType.Y4);
        _axisY4.addPropChangeListener(pc -> chartPartDidPropChange(pc));

        // Create/set Z Axis
        _axisZ = new AxisZ(this);
        _axisZ.addPropChangeListener(pc -> chartPartDidPropChange(pc));

        // Create/set ColorBar
        _colorBar = new ColorBar(this);
        _colorBar.addPropChangeListener(pc -> chartPartDidPropChange(pc));

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
     * Returns the Y2 axis object.
     */
    public AxisY getAxisY2()  { return _axisY2; }

    /**
     * Returns the Y3 axis object.
     */
    public AxisY getAxisY3()  { return _axisY3; }

    /**
     * Returns the Y4 axis object.
     */
    public AxisY getAxisY4()  { return _axisY4; }

    /**
     * Returns the Z axis object.
     */
    public AxisZ getAxisZ()  { return _axisZ; }

    /**
     * Returns the ColorBar object (which is modelled as an axis).
     */
    public ColorBar getColorBar()  { return _colorBar; }

    /**
     * Returns the Axis for given type.
     */
    public Axis getAxisForType(AxisType anAxisType)
    {
        switch (anAxisType) {
            case X: return getAxisX();
            case Y: return getAxisY();
            case Y2: return getAxisY2();
            case Y3: return getAxisY3();
            case Y4: return getAxisY4();
            case Z: return getAxisZ();
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
     * Returns the symbol shape at index.
     */
    public Shape getSymbolShape(int anIndex)
    {
        // Get index in range (wrapped if needed)
        Shape[] symbShapes = getSymbolShapes();
        int index = anIndex % symbShapes.length;

        switch (getType()) {
            case LINE: return symbShapes[index];
            default: return symbShapes[0];
        }
    }

    /**
     * Returns the symbol shapes.
     */
    public Shape[] getSymbolShapes()
    {
        if (_symbolShapes !=null) return _symbolShapes;
        Shape shp0 = new Ellipse(0,0,8,8);
        Shape shp1 = new Polygon(4,0,8,4,4,8,0,4);
        Shape shp2 = new Rect(0,0,8,8);
        Shape shp3 = new Polygon(4,0,8,8,0,8);
        Shape shp4 = new Polygon(0,0,8,0,4,8);
        return _symbolShapes = new Shape[] { shp0, shp1, shp2, shp3, shp4 };
    }

    /**
     * Returns the ChartStyleHpr that provides/manages ChartStyles.
     */
    public ChartStyleHpr getChartStyleHelper()  { return _chartStyleHpr; }

    /**
     * Returns the ChartStyle for this chart (ChartType).
     */
    public ChartStyle getChartStyle()  { return _chartStyleHpr.getChartStyle(); }

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
     * Returns the value for given key.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        // Handle properties
        switch (aPropName) {
            case Fill_Prop: return Color.WHITE;
            default: return super.getPropDefault(aPropName);
        }
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive Type
        e.add(Type_Prop, getType());

        // Archive Header
        XMLElement header_XML = anArchiver.toXML(_header);
        e.add(header_XML);

        // Archive AxisX, AxisY
        XMLElement axisX_XML = anArchiver.toXML(_axisX);
        e.add(axisX_XML);
        XMLElement axisY_XML = anArchiver.toXML(_axisY);
        e.add(axisY_XML);

        // Archive Legend
        XMLElement legendXML = anArchiver.toXML(_legend);
        e.add(legendXML);

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
        // Set Chart
        if (anArchiver instanceof ChartArchiver)
            ((ChartArchiver) anArchiver).setChart(this);

        // Unarchive basic attributes
        super.fromXML(anArchiver, anElement);

        // Unarchive Type
        setType(ChartType.get(anElement.getAttributeValue(Type_Prop)));

        // Unarchive Header
        XMLElement header_XML = anElement.get("Header");
        if (header_XML!=null)
            anArchiver.fromXML(header_XML, _header, this);

        // Unarchive AxisX, AxisY
        XMLElement axisX_XML = anElement.get("AxisX");
        if (axisX_XML!=null)
            anArchiver.fromXML(axisX_XML, _axisX, this);
        XMLElement axisY_XML = anElement.get("AxisY");
        if (axisY_XML!=null)
            anArchiver.fromXML(axisY_XML, _axisY, this);

        // Unarchive Legend
        XMLElement legend_XML = anElement.get("Legend");
        if (legend_XML!=null)
            anArchiver.fromXML(legend_XML, _legend, this);

        // Unarchive DataSetList
        XMLElement dsetListXML = anElement.get("DataSetList");
        if (dsetListXML!=null)
            anArchiver.fromXML(dsetListXML, _dsetList, this);

        // Legacy: Unarchive Title, Subtitle, ShowLegend
        if (anElement.hasAttribute(Header.Title_Prop))
            getHeader().setTitle(anElement.getAttributeValue(Header.Title_Prop));
        if (anElement.hasAttribute(Header.Subtitle_Prop))
            getHeader().setSubtitle(anElement.getAttributeValue(Header.Subtitle_Prop));
        if (anElement.hasAttribute(Legend.ShowLegend_Prop))
            getLegend().setShowLegend(anElement.getAttributeBoolValue(Legend.ShowLegend_Prop));

        // Return this part
        return this;
    }
}