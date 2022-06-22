/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.geom.Insets;
import snap.gfx.Color;
import snap.props.PropChange;
import snap.props.PropSet;
import snap.util.*;
import snapcharts.doc.ChartArchiver;
import snapcharts.doc.Doc;
import snapcharts.util.ChartUtils;
import java.util.Objects;

/**
 * A view to render a chart.
 */
public class Chart extends ChartPart {

    // The ChartDoc that owns this chart
    private Doc  _doc;

    // The chart type
    private ChartType  _type = DEFAULT_TYPE;

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
    private AxisZ  _axisZ;

    // The ContourAxis
    private ContourAxis  _contourAxis;

    // The TraceList
    private TraceList  _traceList;

    // The Legend
    private Legend  _legend;

    // The Scene (for 3D charts)
    private Scene  _scene;

    // Array of marker objects used to highlight or annotate an area on the chart
    private Marker[]  _markers = new Marker[0];

    // The trace colors
    private Color[]  _colors = ColorMap.GT_COLORS;

    // Constants for properties
    public static final String Type_Prop = "Type";
    public static final String Colors_Prop = "Colors";

    // Constants for relations
    public static final String Header_Prop = "Header";
    public static final String AxisX_Prop = "AxisX";
    public static final String AxisY_Prop = "AxisY";
    public static final String AxisY2_Prop = "AxisY2";
    public static final String AxisY3_Prop = "AxisY3";
    public static final String AxisY4_Prop = "AxisY4";
    public static final String AxisZ_Prop = "AxisZ";
    public static final String Legend_Prop = "Legend";
    public static final String Markers_Rel = "Markers";

    // Constants for property defaults
    public static final ChartType  DEFAULT_TYPE = ChartType.SCATTER;
    public static final Color  DEFAULT_CHART_FILL = Color.WHITE;
    public static Insets  DEFAULT_CHART_PADDING = new Insets(5);

    /**
     * Creates a ChartView.
     */
    public Chart()
    {
        // Set default property values
        _fill = DEFAULT_CHART_FILL;
        _padding = DEFAULT_CHART_PADDING;

        // Create/set Header
        _header = new Header();

        // Create/set Axes
        _axisX = new AxisX();
        _axisY = new AxisY(AxisType.Y);
        _axisY2 = new AxisY(AxisType.Y2);
        _axisY3 = new AxisY(AxisType.Y3);
        _axisY4 = new AxisY(AxisType.Y4);
        _axisZ = new AxisZ();

        // Create/set ContourAxis
        _contourAxis = new ContourAxis();

        // Create/set Legend
        _legend = new Legend();

        // Create/set Scene
        _scene = new Scene();

        // Start listening to Trace changes
        _traceList = new TraceList();
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
     * Override to return this chart.
     */
    @Override
    public Chart getChart()
    {
        return this;
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

        // This is bogus
        _scene.chartTypeDidChange();
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
     * Returns the ContourAxis object (which is modelled as an axis).
     */
    public ContourAxis getContourAxis()  { return _contourAxis; }

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
     * Returns the Scene object (properties for 3D charts).
     */
    public Scene getScene()  { return _scene; }

    /**
     * Returns array of marker objects used to highlight or annotate an area on the chart.
     */
    public Marker[] getMarkers()  { return _markers; }

    /**
     * Returns the marker at given index.
     */
    public Marker getMarker(int anIndex)  { return _markers[anIndex]; }

    /**
     * Adds a marker object.
     */
    public void addMarker(Marker aMarker)
    {
        addMarker(aMarker, getMarkers().length);
    }

    /**
     * Adds a marker object at given index.
     */
    public void addMarker(Marker aMarker, int anIndex)
    {
        _markers = ArrayUtils.add(_markers, aMarker, anIndex);
        firePropChange(Markers_Rel, null, aMarker, anIndex);
    }

    /**
     * Removes the marker at given index.
     */
    public Marker removeMarker(int anIndex)
    {
        Marker marker = getMarker(anIndex);
        _markers = ArrayUtils.remove(_markers, anIndex);
        firePropChange(Markers_Rel, marker, null, anIndex);
        return marker;
    }

    /**
     * Removes the given marker.
     */
    public int removeMarker(Marker aMarker)
    {
        int index = ArrayUtils.indexOfId(_markers, aMarker);
        if (index >= 0)
            removeMarker(index);
        return index;
    }

    /**
     * Returns the marker with given name.
     */
    public Marker getMarker(String aName)
    {
        Marker[] markers = getMarkers();
        for (Marker marker : markers)
            if (Objects.equals(marker.getName(), aName))
                return marker;
        return null;
    }

    /**
     * Returns the TraceList.
     */
    public TraceList getTraceList()  { return _traceList; }

    /**
     * Adds a new Trace.
     */
    public void addTrace(Trace aTrace)
    {
        _traceList.addTrace(aTrace);
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
    }

    /**
     * Returns the trace color at index.
     */
    public Color getColor(int anIndex)
    {
        if (anIndex < _colors.length) return _colors[anIndex];
        int index = (anIndex - _colors.length) % ColorMap.COLORS.length;
        return ColorMap.COLORS[index];
    }

    /**
     * Called when chart part has prop change.
     */
    protected void chartPartDidPropChange(PropChange aPC)
    {
        _pcs.fireDeepChange(this, aPC);
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Override super defaults: Fill, Padding, Children
        aPropSet.getPropForName(Fill_Prop).setDefaultValue(DEFAULT_CHART_FILL);
        aPropSet.getPropForName(Padding_Prop).setDefaultValue(DEFAULT_CHART_PADDING);

        // Type
        aPropSet.addPropNamed(Type_Prop, ChartType.class, null);

        // Header
        aPropSet.addPropNamed(Header_Prop, Header.class, null);

        // AxisX, AxisY, AxisY2, AxisY3, AxisY4, AxisZ
        aPropSet.addPropNamed(AxisX_Prop, AxisX.class, null);
        aPropSet.addPropNamed(AxisY_Prop, AxisY.class, null);
        aPropSet.addPropNamed(AxisY2_Prop, AxisY.class, null);
        aPropSet.addPropNamed(AxisY3_Prop, AxisY.class, null);
        aPropSet.addPropNamed(AxisY4_Prop, AxisY.class, null);
        aPropSet.addPropNamed(AxisZ_Prop, AxisZ.class, null);

        // Legend, Markers
        aPropSet.addPropNamed(Legend_Prop, Legend.class, null);
        aPropSet.addPropNamed(Markers_Rel, Marker[].class, null);
    }

    /**
     * Override for Chart properties.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Type
            case Type_Prop: return getType();

            // Header
            case Header_Prop: return getHeader();

            // AxisX, AxisY, AxisY2, AxisY3, AxisY4, AxisZ
            case AxisX_Prop: return getAxisX();
            case AxisY_Prop: return getAxisY();
            case AxisY2_Prop: return getAxisY2();
            case AxisY3_Prop: return getAxisY3();
            case AxisY4_Prop: return getAxisY4();
            case AxisZ_Prop: return getAxisZ();

            // Legend, Markers
            case Legend_Prop: return getLegend();
            case Markers_Rel: return getMarkers();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override for Chart properties.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Type
            case Type_Prop: setType((ChartType) aValue); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue); break;
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
        ChartType chartType = getType();
        e.add(Type_Prop, chartType);

        // Archive Header
        XMLElement header_XML = anArchiver.toXML(_header);
        e.add(header_XML);

        // Archive AxisX
        XMLElement axisX_XML = anArchiver.toXML(_axisX);
        if (axisX_XML.getAttributeCount() > 0 || axisX_XML.getElementCount() > 0)
            e.add(axisX_XML);

        // Archive AxisY
        XMLElement axisY_XML = anArchiver.toXML(_axisY);
        if (axisY_XML.getAttributeCount() > 0 || axisY_XML.getElementCount() > 0)
            e.add(axisY_XML);

        // Archive AxisZ
        if (chartType == ChartType.CONTOUR_3D) {
            XMLElement axisZ_XML = anArchiver.toXML(_axisZ);
            if (axisZ_XML.getAttributeCount() > 0 || axisZ_XML.getElementCount() > 0)
                e.add(axisZ_XML);
        }

        // Archive ContourAxis
        if (chartType.isContourType()) {
            XMLElement contourAxisXML = anArchiver.toXML(_contourAxis);
            if (contourAxisXML.getAttributeCount() > 0 || contourAxisXML.getElementCount() > 0)
                e.add(contourAxisXML);
        }

        // Archive Legend
        if (getLegend().isShowLegend()) {
            XMLElement legendXML = anArchiver.toXML(_legend);
            if (legendXML.getAttributeCount() > 0 || legendXML.getElementCount() > 0)
                e.add(legendXML);
        }

        // Archive Scene
        if (chartType.is3D()) {
            XMLElement sceneXML = anArchiver.toXML(_scene);
            if (sceneXML.getAttributeCount() > 0 || sceneXML.getElementCount() > 0)
                e.add(sceneXML);
        }

        // Archive Markers
        Marker[] markers = getMarkers();
        if (markers.length > 0) {
            XMLElement markersXML = new XMLElement("Markers");
            for (Marker marker : markers) {
                XMLElement markerXML = anArchiver.toXML(marker);
                markersXML.add(markerXML);
            }
            e.add(markersXML);
        }

        // Archive TraceList
        XMLElement traceListXML = anArchiver.toXML(_traceList);
        e.add(traceListXML);

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
        String typeStr = anElement.getAttributeValue(Type_Prop);
        ChartType chartType = ChartType.get(typeStr);
        setType(chartType);

        // Unarchive Header
        XMLElement header_XML = anElement.get("Header");
        if (header_XML != null)
            anArchiver.fromXML(header_XML, _header, this);

        // Unarchive ContourAxis
        XMLElement contourAxisXML = anElement.get(ContourAxis.class.getSimpleName());
        if (contourAxisXML != null)
            anArchiver.fromXML(contourAxisXML, _contourAxis, this);

        // Unarchive AxisX
        XMLElement axisX_XML = anElement.get("AxisX");
        if (axisX_XML != null)
            anArchiver.fromXML(axisX_XML, _axisX, this);

        // Unarchive AxisY
        XMLElement axisY_XML = anElement.get("AxisY");
        if (axisY_XML != null)
            anArchiver.fromXML(axisY_XML, _axisY, this);

        // Unarchive AxisZ
        XMLElement axisZ_XML = anElement.get("AxisZ");
        if (axisZ_XML != null)
            anArchiver.fromXML(axisZ_XML, _axisZ, this);

        // Unarchive Legend
        XMLElement legend_XML = anElement.get("Legend");
        if (legend_XML != null)
            anArchiver.fromXML(legend_XML, _legend, this);

        // Unarchive Scene
        XMLElement scene_XML = anElement.get("Scene");
        if (scene_XML != null)
            anArchiver.fromXML(scene_XML, _scene, this);

        // Unarchive Markers
        XMLElement markersXML = anElement.getElement("Markers");
        if (markersXML != null) {
            XMLElement[] markersXMLs = markersXML.getElements("Marker").toArray(new XMLElement[0]);
            for (XMLElement markerXML : markersXMLs) {
                Marker marker = anArchiver.fromXML(markerXML, Marker.class, this);
                addMarker(marker);
            }
        }

        // Unarchive TraceList
        XMLElement traceListXML = anElement.get("TraceList");
        if (traceListXML == null)
            traceListXML = anElement.get("DataSetList");
        if (traceListXML != null)
            anArchiver.fromXML(traceListXML, _traceList, this);

        // Legacy: Unarchive Title, Subtitle, ShowLegend
        if (anElement.hasAttribute(Header.Title_Prop))
            getHeader().setTitle(anElement.getAttributeValue(Header.Title_Prop));
        if (anElement.hasAttribute(Header.Subtitle_Prop))
            getHeader().setSubtitle(anElement.getAttributeValue(Header.Subtitle_Prop));
        if (anElement.hasAttribute(Legend.ShowLegend_Prop))
            getLegend().setShowLegend(anElement.getAttributeBoolValue(Legend.ShowLegend_Prop));

        // Legacy
        if (typeStr.equalsIgnoreCase("LINE"))
            ChartUtils.setScatterType(this, ChartUtils.ScatterType.LINE);
        if (typeStr.equalsIgnoreCase("AREA"))
            ChartUtils.setScatterType(this, ChartUtils.ScatterType.AREA);

        // Return this part
        return this;
    }
}