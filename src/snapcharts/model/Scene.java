/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;

import snap.util.PropDefaults;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * This class holds attributes for Chart3D.
 */
public class Scene extends ParentPart {

    // Aspect modes
    private AspectMode  _aspectMode;

    // Aspect scales
    private double  _aspectScaleX = 1;
    private double  _aspectScaleY = 1;
    private double  _aspectScaleZ = 1;

    // Constants for how X-Y-Z aspects are calculated
    public enum AspectMode {

        // Aspect mode to calc axis scale by using view width/height as base
        View,

        // Aspect mode to calc axis scale using data as base
        Data,

        // Aspect mode to calc axis scale just using scale factors
        Direct
    }

    // Constants for properties
    public static final String AspectMode_Prop = "AspectMode";
    public static final String AspectScaleX_Prop = "AspectScaleX";
    public static final String AspectScaleY_Prop = "AspectScaleY";
    public static final String AspectScaleZ_Prop = "AspectScaleZ";

    /**
     * Constructor.
     */
    public Scene()
    {
        _aspectMode = AspectMode.View;
    }

    /**
     * Returns the AspectMode used to determine how axes aspect ratios are calculated.
     */
    public AspectMode getAspectMode()  { return _aspectMode; }

    /**
     * Sets the AspectMode used to determine how axes aspect ratios are calculated.
     */
    public void setAspectMode(AspectMode aMode)
    {
        if (aMode == getAspectMode()) return;
        firePropChange(AspectMode_Prop, _aspectMode, _aspectMode = aMode);
    }

    /**
     * Returns the scale factor to be applied to axis aspect ratio for X axis.
     */
    public double getAspectScaleX()  { return _aspectScaleX; }

    /**
     * Sets the scale factor to be applied to axis aspect ratio for X axis.
     */
    public void setAspectScaleX(double aValue)
    {
        if (aValue == getAspectScaleX()) return;
        firePropChange(AspectScaleX_Prop, _aspectScaleX, _aspectScaleX = aValue);
    }

    /**
     * Returns the scale factor to be applied to axis aspect ratio for Y axis.
     */
    public double getAspectScaleY()  { return _aspectScaleY; }

    /**
     * Sets the scale factor to be applied to axis aspect ratio for Y axis.
     */
    public void setAspectScaleY(double aValue)
    {
        if (aValue == getAspectScaleY()) return;
        firePropChange(AspectScaleY_Prop, _aspectScaleY, _aspectScaleY = aValue);
    }

    /**
     * Returns the scale factor to be applied to axis aspect ratio for Z axis.
     */
    public double getAspectScaleZ()  { return _aspectScaleZ; }

    /**
     * Sets the scale factor to be applied to axis aspect ratio for Z axis.
     */
    public void setAspectScaleZ(double aValue)
    {
        if (aValue == getAspectScaleZ()) return;
        firePropChange(AspectScaleZ_Prop, _aspectScaleZ, _aspectScaleZ = aValue);
    }

    /**
     * Returns the axis scale for given AxisType.
     */
    public double getAspectScale(AxisType anAxisType)
    {
        switch (anAxisType) {
            case X: return getAspectScaleX();
            case Y: return getAspectScaleY();
            case Z: return getAspectScaleZ();
            default:
                System.err.println("Scene.getAxisScale: Unsupported AxisType: " + anAxisType);
                return getAspectScaleY();
        }
    }

    /**
     * Returns the actual aspect of X axis based on AspectMode and AspectScaleX.
     */
    public double getAspect(AxisType anAxisType, double aViewW, double aViewH)
    {
        // Get info
        AspectMode aspectMode = getAspectMode();
        double aspectScale = getAspectScale(anAxisType);
        boolean isAxisYUp = true;

        // Handle AspectModes
        switch (aspectMode) {

            // Handle AspectMode.View
            case View: {
                switch (anAxisType) {
                    case X: return aViewW / aViewH * aspectScale;
                    case Y:
                        if (isAxisYUp)
                            return aspectScale;
                        return aViewW / aViewH * aspectScale;
                    case Z: return aspectScale;
                }
            }

            // Handle AspectMode.Data
            case Data: {
                return 1;
            }

            // Handle AspectMode.Direct
            case Direct: return aspectScale;

            // Complain
            default:
                System.err.println("Scene.getAspect: Unknown AspectMode: " + aspectMode);
                return 1;
        }
    }

    /**
     * Called when Chart.Type changes.
     */
    protected void chartTypeDidChange()
    {
        _aspectMode = getAspectModeDefault();
        _aspectScaleX = getAspectScaleDefault(AxisType.X);
        _aspectScaleY = getAspectScaleDefault(AxisType.Y);
        _aspectScaleZ = getAspectScaleDefault(AxisType.Z);
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initPropDefaults(PropDefaults aPropDefaults)
    {
        // Do normal version
        super.initPropDefaults(aPropDefaults);

        // Add Props
        aPropDefaults.addProps(AspectMode_Prop, AspectScaleX_Prop, AspectScaleY_Prop, AspectScaleZ_Prop);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // AspectMode
            case AspectMode_Prop: return getAspectMode();

            // AspectScaleX, AspectScaleY, AspectScaleZ
            case AspectScaleX_Prop: return getAspectScaleX();
            case AspectScaleY_Prop: return getAspectScaleY();
            case AspectScaleZ_Prop: return getAspectScaleZ();

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

            // AspectMode
            case AspectMode_Prop: setAspectMode((AspectMode) aValue); break;

            // AspectScaleX, AspectScaleY, AspectScaleZ
            case AspectScaleX_Prop: setAspectScaleX(SnapUtils.doubleValue(aValue));
            case AspectScaleY_Prop: setAspectScaleY(SnapUtils.doubleValue(aValue));
            case AspectScaleZ_Prop: setAspectScaleZ(SnapUtils.doubleValue(aValue));

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * Returns the prop default value for given key.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // AspectMode
            case AspectMode_Prop: return getAspectModeDefault();

            // AspectScaleX, AspectScaleY, AspectScaleZ
            case AspectScaleX_Prop: return getAspectScaleDefault(AxisType.X);
            case AspectScaleY_Prop: return getAspectScaleDefault(AxisType.Y);
            case AspectScaleZ_Prop: return getAspectScaleDefault(AxisType.Z);

            // Superclass properties
            default: return super.getPropDefault(aPropName);
        }
    }

    /**
     * Returns default AspectMode for ChartType.
     */
    private AspectMode getAspectModeDefault()
    {
        ChartType chartType = getChartType();
        switch (chartType) {
            default: return AspectMode.View;
        }
    }

    /**
     * Return default AspectScale for ChartType.
     */
    public double getAspectScaleDefault(AxisType anAxisType)
    {
        ChartType chartType = getChartType();
        if ((chartType == ChartType.BAR_3D || chartType == ChartType.PIE_3D) && anAxisType == AxisType.Z)
            return .2;
        return 1;
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive AspectMode
        if (!isPropDefault(AspectMode_Prop))
            e.add(AspectMode_Prop, getAspectMode());

        // Archive AspectScaleX, AspectScaleY, AspectScaleZ
        if (!isPropDefault(AspectScaleX_Prop))
            e.add(AspectScaleX_Prop, getAspectScaleX());
        if (!isPropDefault(AspectScaleY_Prop))
            e.add(AspectScaleY_Prop, getAspectScaleY());
        if (!isPropDefault(AspectScaleZ_Prop))
            e.add(AspectScaleZ_Prop, getAspectScaleZ());

        // Return xml
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

        // Unarchive AspectMode
        if (anElement.hasAttribute(AspectMode_Prop))
            setAspectMode(anElement.getAttributeEnumValue(AspectMode_Prop, AspectMode.class, null));

        // Unarchive AspectScaleX
        if (anElement.hasAttribute(AspectScaleX_Prop))
            setAspectScaleX(anElement.getAttributeDoubleValue(AspectScaleX_Prop));
        if (anElement.hasAttribute(AspectScaleY_Prop))
            setAspectScaleY(anElement.getAttributeDoubleValue(AspectScaleY_Prop));
        if (anElement.hasAttribute(AspectScaleZ_Prop))
            setAspectScaleZ(anElement.getAttributeDoubleValue(AspectScaleZ_Prop));

        // Return
        return this;
    }
}
