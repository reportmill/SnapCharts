/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.charts;
import snap.geom.Insets;
import snap.gfx.Color;
import snap.props.Prop;
import snap.props.PropChange;
import snap.props.PropSet;
import snap.util.*;
import snapcharts.doc.Doc;
import java.util.Objects;

/**
 * A view to render a chart.
 */
public class Chart extends ParentPart {

    // The ChartDoc that owns this chart
    private Doc  _doc;

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

    // The Content
    private Content  _content;

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
    public static final String ContourAxis_Prop = "ContourAxis";
    public static final String Legend_Prop = "Legend";
    public static final String Scene_Prop = "Scene";
    public static final String Markers_Prop = "Markers";
    public static final String Content_Prop = "Content";

    // Constants for property defaults
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

        // Create/set Content
        _content = new Content();

        // Add children
        ChartPart[] children = { _header, _axisX, _axisY, _axisY2, _axisY3, _axisY4, _axisZ, _contourAxis, _legend, _scene, _content};
        for (ChartPart child : children)
            addChild(child);
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
     * Returns the base trace type of chart.
     */
    public TraceType getTraceType()
    {
        // Return First Content.Traces.Trace.Type
        Content content = getContent();
        Trace[] traces = content.getTraces();
        for (Trace trace : traces)
            return trace.getType();

        // Return Scatter (default) since no traces
        return TraceType.Scatter;
    }

    /**
     * Returns the base trace type of chart.
     */
    public void setTraceType(TraceType aTraceType)
    {
        Content content = getContent();
        Trace[] traces = content.getTraces();
        for (Trace trace : traces)
            content.setTraceType(trace, aTraceType);
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
     * Sets array of marker objects used to highlight or annotate an area on the chart.
     */
    public void setMarkers(Marker[] theMarkers)
    {
        // Remove any existing markers
        while (getMarkers().length > 0) removeMarker(0);

        // Add new markers
        for (Marker marker : theMarkers)
            addMarker(marker);
    }

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
        addChild(aMarker);
        firePropChange(Markers_Prop, null, aMarker, anIndex);
    }

    /**
     * Removes the marker at given index.
     */
    public Marker removeMarker(int anIndex)
    {
        Marker marker = getMarker(anIndex);
        _markers = ArrayUtils.remove(_markers, anIndex);
        removeChild(marker);
        firePropChange(Markers_Prop, marker, null, anIndex);
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
     * Returns the Content.
     */
    public Content getContent()  { return _content; }

    /**
     * Adds a new Trace.
     */
    public void addTrace(Trace aTrace)
    {
        _content.addTrace(aTrace);
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

        // Override super defaults: Fill, Padding
        aPropSet.getPropForName(Fill_Prop).setDefaultValue(DEFAULT_CHART_FILL);
        aPropSet.getPropForName(Padding_Prop).setDefaultValue(DEFAULT_CHART_PADDING);

        // Suppress Children archival
        aPropSet.getPropForName(Children_Prop).setSkipArchival(true);

        // Header
        aPropSet.addPropNamed(Header_Prop, Header.class, EMPTY_OBJECT);

        // AxisX, AxisY, AxisY2, AxisY3, AxisY4, AxisZ
        aPropSet.addPropNamed(AxisX_Prop, AxisX.class, EMPTY_OBJECT);
        aPropSet.addPropNamed(AxisY_Prop, AxisY.class, EMPTY_OBJECT);
        aPropSet.addPropNamed(AxisY2_Prop, AxisY.class, EMPTY_OBJECT);
        aPropSet.addPropNamed(AxisY3_Prop, AxisY.class, EMPTY_OBJECT);
        aPropSet.addPropNamed(AxisY4_Prop, AxisY.class, EMPTY_OBJECT);
        aPropSet.addPropNamed(AxisZ_Prop, AxisZ.class, EMPTY_OBJECT);

        // ContourAxis
        aPropSet.addPropNamed(ContourAxis_Prop, ContourAxis.class, EMPTY_OBJECT);

        // Legend, Scene, Markers
        aPropSet.addPropNamed(Legend_Prop, Legend.class, EMPTY_OBJECT);
        aPropSet.addPropNamed(Scene_Prop, Scene.class, EMPTY_OBJECT);
        aPropSet.addPropNamed(Markers_Prop, Marker[].class, EMPTY_OBJECT);

        // Content
        aPropSet.addPropNamed(Content_Prop, Content.class, EMPTY_OBJECT);

        // Set all above ChartPart props preexisting, so they will be used in place during unarchival
        Prop[] props = aPropSet.getProps();
        for (Prop prop : props)
            if (ChartPart.class.isAssignableFrom(prop.getPropClass()))
                prop.setPreexisting(true);
    }

    /**
     * Override for Chart properties.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Header
            case Header_Prop: return getHeader();

            // AxisX, AxisY, AxisY2, AxisY3, AxisY4, AxisZ
            case AxisX_Prop: return getAxisX();
            case AxisY_Prop: return getAxisY();
            case AxisY2_Prop: return getAxisY2();
            case AxisY3_Prop: return getAxisY3();
            case AxisY4_Prop: return getAxisY4();
            case AxisZ_Prop: return getAxisZ();

            // ContourAxis
            case ContourAxis_Prop: return getContourAxis();

            // Legend, Scene, Markers
            case Legend_Prop: return getLegend();
            case Scene_Prop: return getScene();
            case Markers_Prop: return getMarkers();

            // Content
            case Content_Prop: return getContent();

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

            // Markers
            case Markers_Prop: setMarkers((Marker[]) aValue); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue); break;
        }
    }
}