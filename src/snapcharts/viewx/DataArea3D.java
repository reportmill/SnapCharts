/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Painter;
import snap.gfx3d.*;
import snap.util.MathUtils;
import snap.util.PropChange;
import snapcharts.model.*;
import snapcharts.view.*;

/**
 * This DataArea subclass forms the basis for 3D DataAreas.
 */
public abstract class DataArea3D extends DataArea {

    // The Camera
    protected CameraView  _cameraView;

    // The Camera
    protected Camera3D  _camera;

    // The Scene
    protected Scene3D  _scene;

    // The preferred AxisBoxShape size
    private double  _axisBoxPrefWidth, _axisBoxPrefHeight, _axisBoxPrefDepth;

    // The Intervals for AxisType
    private Intervals  _intervalsX, _intervalsY, _intervalsZ;

    // The AxisBox
    protected AxisBoxShape  _axisBoxShape;

    // The AxisBoxPainter to paint tick labels
    private AxisBoxTextPainter  _axisBoxPainter;

    // Runnables to rebuild chart deferred/coalesced
    private Runnable  _rebuildChartRun, _rebuildChartRunImpl = () -> rebuildChartNow();

    // Constants
    public static final double DEFAULT_YAW = 26;
    public static final double DEFAULT_PITCH = 10;

    /**
     * Constructor.
     */
    public DataArea3D(ChartHelper aChartHelper, Trace aTrace, boolean isVisible)
    {
        super(aChartHelper, aTrace);

        // If not visible, just return
        if (!isVisible) {
            setVisible(false);
            return;
        }

        // Create and install CameraView
        _cameraView = new CameraView();
        addChild(_cameraView);

        // Set Scene and Camera
        _camera = _cameraView.getCamera();
        _scene = _cameraView.getScene();

        // Create/set painter
        _axisBoxPainter = new AxisBoxTextPainter(this, _scene);

        // Set default
        setDefaultViewTransform();
    }

    /**
     * Returns the CameraView.
     */
    public CameraView getCameraView()  { return _cameraView; }

    /**
     * Rebuilds the chart.
     */
    public AxisBoxShape getAxisBoxShape()
    {
        if (_axisBoxShape != null) return _axisBoxShape;
        AxisBoxShape axisBoxShape = createAxisBoxShape();
        return _axisBoxShape = axisBoxShape;
    }

    /**
     * Rebuilds the chart.
     */
    protected AxisBoxShape createAxisBoxShape()
    {
        return new AxisBoxShape(this);
    }

    /**
     * Resets the default view transform.
     */
    public void setDefaultViewTransform()
    {
        _camera.setYaw(DEFAULT_YAW);
        _camera.setPitch(DEFAULT_PITCH);
        _camera.setFocalLength(8 * 72);
    }

    /**
     * Rebuilds the chart.
     */
    protected void rebuildChart()
    {
        if (_rebuildChartRun == null)
            getUpdater().runBeforeUpdate(_rebuildChartRun = _rebuildChartRunImpl);
    }

    /**
     * Rebuilds the chart immediately.
     */
    protected void rebuildChartNow()
    {
        // Remove all existing children
        _scene.removeChildren();

        // Clear cached values
        _axisBoxPrefWidth = _axisBoxPrefHeight = _axisBoxPrefDepth = 0;
        _intervalsX = _intervalsY = _intervalsZ = null;
        _axisBoxShape = null;

        // If too small, just return
        if (getWidth() < 10 || getHeight() < 10) {
            _rebuildChartRun = null;
            return;
        }

        // Reset intervals
        updateIntervalsIfNeeded();

        // Get AxisBoxShape and add to scene
        AxisBoxShape axisBoxShape = getAxisBoxShape();
        _scene.addChild(axisBoxShape);

        // Update grids
        axisBoxShape.addSideGrids();

        // Repaint DataArea and reset runnable
        repaint();
        _rebuildChartRun = null;
    }

    /**
     * Returns whether the XY data plane is pointed forward, which happens for Bar3D, Line3D charts.
     */
    public boolean isForwardXY()
    {
        ChartType chartType = getChartType();
        return chartType == ChartType.BAR_3D || chartType == ChartType.LINE_3D;
    }

    /**
     * Returns the preferred width of the AxisBoxShape.
     */
    public double getAxisBoxPrefWidth()
    {
        // If already set, just return
        if (_axisBoxPrefWidth > 0) return _axisBoxPrefWidth;

        // Get Scene (3D chart info) and DataView size
        Chart chart = getChart();
        Scene scene = chart.getScene();
        double viewW = getWidth();
        double viewH = getHeight();

        // Get Aspect for Y Axis and PrefHeight
        double aspectY = scene.getAspect(AxisType.Y, viewW, viewH);
        double prefH = aspectY * viewH;

        // Calculate PrefWidth using AspectY and PrefHeight
        double aspectX = scene.getAspect(AxisType.X, viewW, viewH);
        double prefW = prefH / aspectY * aspectX;
        return _axisBoxPrefWidth = prefW;
    }

    /**
     * Returns the preferred height of the AxisBoxShape.
     */
    public double getAxisBoxPrefHeight()
    {
        // If already set, just return
        if (_axisBoxPrefHeight > 0) return _axisBoxPrefHeight;

        // Get Scene (3D chart info) and DataView size
        Chart chart = getChart();
        Scene scene = chart.getScene();
        double viewW = getWidth();
        double viewH = getHeight();

        // Get Aspect for Y Axis and PrefHeight
        double aspectY = scene.getAspect(AxisType.Y, viewW, viewH);
        double prefH = aspectY * viewH;
        return _axisBoxPrefHeight = prefH;
    }

    /**
     * Returns the preferred depth of the AxisBoxShape.
     */
    public double getAxisBoxPrefDepth()
    {
        // If already set, just return
        if (_axisBoxPrefDepth > 0) return _axisBoxPrefDepth;

        // Get Scene (3D chart info) and DataView size
        Chart chart = getChart();
        Scene scene = chart.getScene();
        double viewW = getWidth();
        double viewH = getHeight();

        // Get Aspect for Y Axis and PrefHeight
        double aspectY = scene.getAspect(AxisType.Y, viewW, viewH);
        double prefH = aspectY * viewH;

        // Calculate PrefDepth using AspectY and PrefHeight
        double aspectZ = scene.getAspect(AxisType.Z, viewW, viewH);
        double prefD = prefH / aspectY * aspectZ;
        return _axisBoxPrefDepth = prefD;
    }

    /**
     * Returns the intervals for X axis.
     */
    public Intervals getPrefIntervalsX()
    {
        double prefW = getAxisBoxPrefWidth();
        AxisView axisView = getAxisViewX();
        axisView.setWidth(prefW);
        return axisView.getIntervals();
    }

    /**
     * Returns the intervals for Y axis.
     */
    public Intervals getPrefIntervalsY()
    {
        double prefH = getAxisBoxPrefHeight();
        AxisView axisView = getAxisViewY();
        axisView.setHeight(prefH);
        return axisView.getIntervals();
    }

    /**
     * Returns the intervals for Z axis.
     */
    public Intervals getPrefIntervalsZ()
    {
        double prefD = getAxisBoxPrefDepth();
        AxisView axisView = getAxisViewZ();
        axisView.setHeight(prefD);
        return axisView.getIntervals();
    }

    /**
     * Returns the intervals for given axis type.
     */
    protected Intervals getPrefIntervalsForAxis(AxisType anAxisType)
    {
        switch (anAxisType) {
            case X: return getPrefIntervalsX();
            case Y: return getPrefIntervalsY();
            case Z: return getPrefIntervalsZ();
            default: throw new RuntimeException("AxisBoxBuilder.getPrefIntervalsForAxis: Invalid axis: " + anAxisType);
        }
    }

    /**
     * Checks to see if intervals have changed due to AxisBox orientation change.
     */
    public boolean updateIntervalsIfNeeded()
    {
        // Get AxisTypes and didUpdate flag
        AxisType[] axisTypes = _chartHelper.getAxisTypes();
        boolean didUpdate = false;

        // Iterate over axes types and recalc intervals for each axis length in view coords (based on chart orientation)
        for (AxisType axisType : axisTypes) {

            // Get preferred intervals and min/max
            Intervals prefIntervals = getPrefIntervalsForAxis(axisType);
            double prefMin = prefIntervals.getMin();
            double prefMax = prefIntervals.getMax();

            // Handle Category axis special
            if (axisType == AxisType.X && getAxisViewX().isCategoryAxis()) {
                if (getIntervalsX() == null)
                    setIntervalsForAxis(axisType, prefIntervals);
                continue;
            }

            // Calculate intervals for current axis length
            double axisLen = getAxisLengthInViewForAxis(axisType);
            //Size labelSize = _dataArea.getChartHelper().getAxisView(axisType).getMaxTickLabelRotatedSize();
            double divLen = 40;
            Intervals intervals = Intervals.getIntervalsForMinMaxLen(prefMin, prefMax, axisLen, divLen, true, true);

            // If not equal, then set updated intervals and mark didUpdate
            if (!intervals.equals(getIntervalsForAxis(axisType))) {
                setIntervalsForAxis(axisType, intervals);
                didUpdate = true;
            }
        }

        // Return
        return didUpdate;
    }

    /**
     * Returns the axis length for given axis type.
     */
    public double getAxisLengthInViewForAxis(AxisType anAxisType)
    {
        // Get AxisLine in Data space
        Line3D axisLine = getAxisLineInDataSpace(anAxisType);

        // Convert line to view coords
        convertDataToView(axisLine.getP1());
        convertDataToView(axisLine.getP2());

        // Return distance between line XY points
        return axisLine.getDistance2D();
    }

    /**
     * Returns the axis length for given axis type.
     */
    public Line3D getAxisLineInDataSpace(AxisType axisType)
    {
        // Get intervals
        Intervals intervalsX = getPrefIntervalsX();
        Intervals intervalsY = getPrefIntervalsY();
        Intervals intervalsZ = getPrefIntervalsZ();

        // Get axis info
        AxisBoxShape axisBoxShape = getAxisBoxShape();
        boolean isForwardXY = isForwardXY();
        boolean isX = axisType == AxisType.X;
        boolean isY = axisType.isAnyY();
        boolean isZ = axisType == AxisType.Z;

        // Get X component of line points
        Side3D xSide = Side3D.FRONT;
        boolean isXSideVisible = axisBoxShape.isSideVisible(xSide);
        double dataX1 = isX || isXSideVisible ? intervalsX.getMin() : intervalsX.getMax();
        double dataX2 = isX ? intervalsX.getMax() : dataX1;

        // Get Y component of line points
        Side3D ySide = isForwardXY ? Side3D.BOTTOM : Side3D.FRONT;
        boolean isYSideVisible = axisBoxShape.isSideVisible(ySide);
        double dataY1 = isY || isYSideVisible ? intervalsY.getMin() : intervalsY.getMax();
        double dataY2 = isY ? intervalsY.getMax() : dataY1;

        // Get Z component of line points
        Side3D zSide = isForwardXY ? Side3D.FRONT : Side3D.TOP;
        boolean isZSideVisible = axisBoxShape.isSideVisible(zSide);
        double dataZ1 = isZ || isZSideVisible ? intervalsZ.getMin() : intervalsZ.getMax();
        double dataZ2 = isZ ? intervalsZ.getMax() : dataZ1;

        // Return line
        return new Line3D(dataX1, dataY1, dataZ1, dataX2, dataY2, dataZ2);
    }

    /**
     * Returns the intervals for X axis.
     */
    public Intervals getIntervalsX()  { return _intervalsX; }

    /**
     * Returns the intervals for Y axis.
     */
    public Intervals getIntervalsY()  { return _intervalsY; }

    /**
     * Returns the intervals for Z axis.
     */
    public Intervals getIntervalsZ()  { return _intervalsZ; }

    /**
     * Returns the intervals for given axis type.
     */
    protected Intervals getIntervalsForAxis(AxisType anAxisType)
    {
        switch (anAxisType) {
            case X: return getIntervalsX();
            case Y: return getIntervalsY();
            case Z: return getIntervalsZ();
            default: throw new RuntimeException("DataArea3D.getIntervalsForAxis: Invalid axis: " + anAxisType);
        }
    }

    /**
     * Sets the intervals for given axis type.
     */
    protected void setIntervalsForAxis(AxisType anAxisType, Intervals theIntervals)
    {
        switch (anAxisType) {
            case X: _intervalsX = theIntervals; break;
            case Y: _intervalsY = theIntervals; break;
            case Z: _intervalsZ = theIntervals; break;
            default: throw new RuntimeException("DataArea3D.setIntervalsForAxis: Invalid axis: " + anAxisType);
        }
    }

    /**
     * Returns given XYZ data point as Point3D in View coords.
     */
    public void convertDataToView(Point3D aPoint)
    {
        // Get intervals and AxisBox
        Intervals intervalsX = getPrefIntervalsX();
        Intervals intervalsY = getPrefIntervalsY();
        Intervals intervalsZ = getPrefIntervalsZ();

        // Get AxisBox size
        double prefW = getAxisBoxPrefWidth();
        double prefH = getAxisBoxPrefHeight();
        double prefD = getAxisBoxPrefDepth();

        // If not ForwardXY, swap Y & Z axes
        boolean isForwardXY = isForwardXY();
        if (!isForwardXY) {
            double temp = aPoint.y; aPoint.y = aPoint.z; aPoint.z = temp;
            Intervals tempi = intervalsY; intervalsY = intervalsZ; intervalsZ = tempi;
        }

        // Get XYZ in AxisBox space
        aPoint.x = MathUtils.mapValueForRanges(aPoint.x, intervalsX.getMin(), intervalsX.getMax(), 0, prefW);
        aPoint.y = MathUtils.mapValueForRanges(aPoint.y, intervalsY.getMin(), intervalsY.getMax(), 0, prefH);
        aPoint.z = MathUtils.mapValueForRanges(aPoint.z, intervalsZ.getMin(), intervalsZ.getMax(), 0, prefD);

        // Transform point from AxisBox (Scene) to View space
        Matrix3D sceneToView = _camera.getSceneToView();
        sceneToView.transformPoint(aPoint);
    }

    /**
     * Returns given XYZ data point as Point3D in View coords.
     */
    public Point3D convertDataToView(double aX, double aY, double aZ)
    {
        Point3D point = new Point3D(aX, aY, aZ);
        convertDataToView(point);
        return point;
    }

    /**
     * Override to suppress.
     */
    @Override
    protected void paintDataArea(Painter aPntr)
    {
        // Update AxisBoxShape Sides Visibility
        AxisBoxShape axisBoxShape = getAxisBoxShape();
        axisBoxShape.setSidesVisibleForCamera();

        // Update Intervals if needed
        boolean didUpdateIntervals = updateIntervalsIfNeeded();
        if (didUpdateIntervals)
            axisBoxShape.addSideGrids();
    }

    /**
     * Override to suppress.
     */
    @Override
    protected void paintDataAreaAbove(Painter aPntr)
    {
        // If no AxisBox yet, just return
        if (_scene == null || _scene.getChildCount() == 0) return;

        // Clip AxisBox text (axis and tick labels)
        aPntr.save();
        aPntr.clipRect(0, 0, getWidth(), getHeight());

        // Paint Axis tick labels
        _axisBoxPainter.paintAxisBoxText(aPntr);

        // Restore clip
        aPntr.restore();
    }

    /**
     * Override to resize CamView.
     */
    protected void layoutImpl()
    {
        // Shouldn't need this!
        if (!isVisible()) return;

        // If CamView.Size needs update, setCamView size and rebuildChart()
        double viewW = getWidth();
        double viewH = getHeight();
        if (viewW != _cameraView.getWidth() || viewH != _cameraView.getHeight()) {
            _cameraView.setSize(viewW, viewH);
            rebuildChart();
        }
    }

    /**
     * Override to rebuild Scene.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // If not visible, just return
        if (!isVisible())
            return;

        // Handle Trace changes: Rebuild scene
        Object source = aPC.getSource();
        if (source instanceof Trace || source instanceof TraceList)
            rebuildChart();

        // If Chart.Scene change, rebuild scene
        if (source instanceof Scene)
            rebuildChart();
    }

    /**
     * Returns whether given side is facing camera.
     */
    public boolean isSideFacingCamera(Side3D aSide)
    {
        Vector3D sideNormal = aSide.getNormalInward();
        Camera3D camera = _scene.getCamera();
        Matrix3D sceneToCamera = camera.getSceneToCamera();
        Vector3D sideNormalInCamera = sceneToCamera.transformVector(sideNormal.clone());
        Vector3D cameraNormal = camera.getNormal();
        boolean isFacing = !sideNormalInCamera.isAligned(cameraNormal, false);
        return isFacing;
    }

    /**
     * Returns the View axis for given Data axis.
     */
    public AxisType getViewAxisForDataAxis(AxisType anAxisType)
    {
        // Handle weird Bar3D and Line3D: Data axis is same as View axis
        if (isForwardXY())
            return anAxisType;

        // Handle standard 3D where Z values go up/down and Y values are forward/back (Y & Z are swapped)
        if (anAxisType == AxisType.Z)
            return AxisType.Y;
        if (anAxisType == AxisType.Y)
            return AxisType.Z;
        return anAxisType;
    }

    /**
     * Returns the axes pair for each axis box side.
     */
    protected AxisType[] getAxesForSide(Side3D aSide)
    {
        // Handle weird Bar3D and Line3D
        if (isForwardXY()) {
            switch (aSide) {

                // Handle Front/Back: XY plane
                case FRONT: case BACK: return new AxisType[] { AxisType.X, AxisType.Y };

                // Handle Left/Right: YZ plane
                case LEFT: case RIGHT: return new AxisType[] { AxisType.Z, AxisType.Y };

                // Handle Top/Bottom: XZ plane
                case TOP: case BOTTOM: return new AxisType[] { AxisType.X, AxisType.Z };

                // Other is error
                default: throw new RuntimeException("AxisBoxBuilder.getAxesForSide: Invalid side: " + aSide);
            }
        }

        // Handle standard XYZ (Z Up) AxisBox
        switch (aSide) {

            // Front/Back is XZ plane
            case FRONT: case BACK: return new AxisType[] { AxisType.X, AxisType.Z };

            // Left/Right is YZ plane
            case LEFT: case RIGHT: return new AxisType[] { AxisType.Y, AxisType.Z };

            // Top/Bottom is XY plane
            case TOP: case BOTTOM: return new AxisType[] { AxisType.X, AxisType.Y };

            // Other is error
            default: throw new RuntimeException("AxisBoxBuilder.getAxesForSide: Invalid side: " + aSide);
        }
    }
}
