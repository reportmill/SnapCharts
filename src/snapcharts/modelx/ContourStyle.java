/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.modelx;
import snap.props.PropSet;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snapcharts.model.TraceStyle;

/**
 * A TraceStyle subclass for Contour chart properties.
 */
public class ContourStyle extends TraceStyle {

    // Whether to show contour lines
    private boolean  _showLines;

    // Whether to show mesh
    private boolean  _showMesh;

    // Constants for properties
    public final String ShowLines_Prop = "ShowLines";
    public final String ShowMesh_Prop = "ShowMesh";

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
            case ShowLines_Prop: setShowLines(SnapUtils.boolValue(aValue)); break;
            case ShowMesh_Prop: setShowMesh(SnapUtils.boolValue(aValue)); break;

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue);
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

        // Archive ShowLines, ShowMesh
        if (!isPropDefault(ShowLines_Prop))
            e.add(ShowLines_Prop, isShowLines());
        if (!isPropDefault(ShowMesh_Prop))
            e.add(ShowMesh_Prop, isShowMesh());

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

        // Unarchive ShowLines, ShowMesh
        if (anElement.hasAttribute(ShowLines_Prop))
            setShowLines(anElement.getAttributeBoolValue(ShowLines_Prop));
        if (anElement.hasAttribute(ShowMesh_Prop))
            setShowMesh(anElement.getAttributeBoolValue(ShowMesh_Prop));

        // Return this part
        return this;
    }
}
