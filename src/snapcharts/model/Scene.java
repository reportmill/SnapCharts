/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.gfx3d.Side3D;
import snap.props.PropSet;
import snap.util.*;
import java.util.Arrays;

/**
 * This class holds attributes for Chart3D.
 */
public class Scene extends ChartPart {

    // Aspect modes
    private AspectMode  _aspectMode;

    // Aspect scales
    private double  _aspectScaleX;
    private double  _aspectScaleY;
    private double  _aspectScaleZ;

    // The sides that show projections
    private Side3D[]  _projectedSides;

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
    public static final String ProjectedSides_Prop = "ProjectedSides";

    // Constants for defaults
    public static final AspectMode DEFAULT_ASPECT_MODE = AspectMode.View;
    public static final double DEFAULT_ASPECT_SCALE_X = 1;
    public static final double DEFAULT_ASPECT_SCALE_Y = 1;

    /**
     * Constructor.
     */
    public Scene()
    {
        _aspectMode = DEFAULT_ASPECT_MODE;
        _aspectScaleX = DEFAULT_ASPECT_SCALE_X;
        _aspectScaleY = DEFAULT_ASPECT_SCALE_Y;
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
    public double getAspectScaleZ()
    {
        if (_aspectScaleZ > 0) return _aspectScaleZ;
        return getDefaultAspectScaleZ();
    }

    /**
     * Sets the scale factor to be applied to axis aspect ratio for Z axis.
     */
    public void setAspectScaleZ(double aValue)
    {
        if (aValue == getAspectScaleZ()) return;
        firePropChange(AspectScaleZ_Prop, _aspectScaleZ, _aspectScaleZ = aValue);
    }

    /**
     * Returns the projected sides.
     */
    public Side3D[] getProjectedSides()  { return _projectedSides; }

    /**
     * Sets the projected sides.
     */
    public void setProjectedSides(Side3D[] theSides)
    {
        // If already set, just return
        Side3D[] sides = theSides != null ? theSides : new Side3D[0];
        Arrays.sort(sides, (s1, s2) -> Integer.compare(s1.ordinal(), s2.ordinal()));
        if (Arrays.equals(sides, _projectedSides != null ? _projectedSides : new Side3D[0])) return;

        // Set and firePropChange
        firePropChange(ProjectedSides_Prop, _projectedSides, _projectedSides = sides.length > 0 ? sides : null);
    }

    /**
     * Returns whether given side is selected.
     */
    public boolean isProjectedSide(Side3D aSide)
    {
        return _projectedSides != null && ArrayUtils.containsId(_projectedSides, aSide);
    }

    /**
     * Adds a projected side.
     */
    public void addProjectedSide(Side3D aSide)
    {
        Side3D[] sides = getProjectedSides();
        if (sides != null && ArrayUtils.containsId(sides, aSide)) return;
        sides = sides != null ? ArrayUtils.add(sides, aSide) : new Side3D[] { aSide };
        setProjectedSides(sides);
    }

    /**
     * Removes a projected side.
     */
    public void removeProjectedSide(Side3D aSide)
    {
        Side3D[] sides = getProjectedSides();
        if (sides == null || !ArrayUtils.containsId(sides, aSide)) return;
        sides = ArrayUtils.removeId(sides, aSide);
        setProjectedSides(sides);
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
        TraceType traceType = getChart().getTraceType();
        boolean isAxisYUp = traceType == TraceType.Bar3D || traceType == TraceType.Line3D;

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
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // AspectMode
        aPropSet.addPropNamed(AspectMode_Prop, AspectMode.class, DEFAULT_ASPECT_MODE);

        // AspectScaleX, AspectScaleY, AspectScaleZ
        aPropSet.addPropNamed(AspectScaleX_Prop, double.class, DEFAULT_ASPECT_SCALE_X);
        aPropSet.addPropNamed(AspectScaleY_Prop, double.class, DEFAULT_ASPECT_SCALE_Y);
        aPropSet.addPropNamed(AspectScaleZ_Prop, double.class, 0d);

        // ProjectedSides
        aPropSet.addPropNamed(ProjectedSides_Prop, Side3D[].class, null);
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

            // ProjectedSides
            case ProjectedSides_Prop: return getProjectedSides();

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
            case AspectScaleX_Prop: setAspectScaleX(SnapUtils.doubleValue(aValue)); break;
            case AspectScaleY_Prop: setAspectScaleY(SnapUtils.doubleValue(aValue)); break;
            case AspectScaleZ_Prop: setAspectScaleZ(SnapUtils.doubleValue(aValue)); break;

            // ProjectedSides
            case ProjectedSides_Prop: setProjectedSides((Side3D[]) aValue); break;

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

            // AspectScaleZ
            case AspectScaleZ_Prop: return getDefaultAspectScaleZ();

            // Superclass properties
            default: return super.getPropDefault(aPropName);
        }
    }

    /**
     * Return default AspectScaleZ for TraceType.
     */
    public double getDefaultAspectScaleZ()
    {
        TraceType traceType = getChart().getTraceType();
        if ((traceType == TraceType.Bar3D || traceType == TraceType.Pie3D))
            return .2;
        return 1;
    }
}
