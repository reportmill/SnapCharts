/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.charts.traces;
import snap.props.PropSet;
import snap.util.Convert;
import snapcharts.charts.Trace;
import snapcharts.charts.TraceType;

/**
 * A Trace subclass for Contour chart properties.
 */
public class ContourTrace extends Trace {

    // Whether to show contour lines
    private boolean  _showLines;

    // Whether to show mesh
    private boolean  _showMesh;

    // Constants for properties
    public final String ShowLines_Prop = "ShowLines";
    public final String ShowMesh_Prop = "ShowMesh";

    /**
     * Constructor.
     */
    public ContourTrace()
    {
        super();
    }

    /**
     * Returns Type.
     */
    @Override
    public TraceType getType()  { return TraceType.Contour; }

    /**
     * Returns whether to show contour lines.
     */
    public boolean isShowLines()  { return _showLines; }

    /**
     * Sets whether to show contour lines
     */
    public void setShowLines(boolean aValue)
    {
        if (aValue == isShowLines()) return;
        firePropChange(ShowLines_Prop, _showLines, _showLines = aValue);
    }

    /**
     * Returns whether to show mesh.
     */
    public boolean isShowMesh()  { return _showMesh; }

    /**
     * Sets whether to show mesh.
     */
    public void setShowMesh(boolean aValue)
    {
        if (aValue == isShowMesh()) return;
        firePropChange(ShowMesh_Prop, _showMesh, _showMesh = aValue);
    }

    /**
     * Override to configure props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // ShowLines, ShowMesh
        aPropSet.addPropNamed(ShowLines_Prop, boolean.class, false);
        aPropSet.addPropNamed(ShowMesh_Prop, boolean.class, false);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Handle ShowLines, ShowMesh
            case ShowLines_Prop: return isShowLines();
            case ShowMesh_Prop: return isShowMesh();

            // Handle super class properties (or unknown)
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Sets the prop value for given key.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // Handle properties
        switch (aPropName) {

            // Handle ShowLines, ShowMesh
            case ShowLines_Prop: setShowLines(Convert.boolValue(aValue)); break;
            case ShowMesh_Prop: setShowMesh(Convert.boolValue(aValue)); break;

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue);
        }
    }
}
