/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Painter;
import snap.gfx3d.*;
import snap.util.MathUtils;
import snap.util.PropChange;
import snap.view.ViewUtils;
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
    protected AxisBoxTextPainter  _axisBoxPainter;

    // The X axis bounds
    protected ChartPartView  _axisProxyX, _axisProxyY, _axisProxyZ;

    // Runnables to rebuild chart deferred/coalesced
    private Runnable  _rebuildChartRun, _rebuildChartRunImpl = () -> rebuildChartNow();

    // Runnables to rebuild AxisProxy views when camera changes (deferred/coalesced)
    private Runnable  _cameraChangeRun, _cameraChangeRunImpl = () -> cameraDidChangeImpl();

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

        // Add listener for Camera changes to update AxisProxy bounds
        _camera.addPropChangeListener(pc -> cameraDidChange());

        // Create/set painter
        _axisBoxPainter = new AxisBoxTextPainter(this);

        // Set default
        setDefaultViewTransform();

        // Create/add AxisProxy views which allow for axis selection
        Chart chart = aChartHelper.getChart();
        _axisProxyX = new ChartPartView(chart.getAxisForType(AxisType.X));
        _axisProxyX.setPaintable(false);
        _axisProxyX.setPickable(false);
        addChild(_axisProxyX);
        _axisProxyY = new ChartPartView(chart.getAxisForType(AxisType.Y));
        _axisProxyY.setPaintable(false);
        _axisProxyY.setPickable(false);
        addChild(_axisProxyY);
        _axisProxyZ = new ChartPartView(chart.getAxisForType(AxisType.Z));
        _axisProxyZ.setPaintable(false);
        _axisProxyZ.setPickable(false);
        addChild(_axisProxyZ);
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
        // Iterate over axes types and updateIntervals for each
        AxisType[] axisTypes = _chartHelper.getAxisTypes();
        boolean didUpdate = false;
        for (AxisType axisType : axisTypes)
            didUpdate |= updateIntervalsForAxisIfNeeded(axisType);


        // Return whether any updated
        return didUpdate;
    }

    /**
     * Checks to see if intervals have changed due to AxisBox orientation change for given axis.
     */
    protected boolean updateIntervalsForAxisIfNeeded(AxisType axisType)
    {
        // Get preferred intervals and min/max
        Intervals prefIntervals = getPrefIntervalsForAxis(axisType);
        double prefMin = prefIntervals.getMin();
        double prefMax = prefIntervals.getMax();

        // Handle Category axis special
        if (axisType == AxisType.X && getAxisViewX().isCategoryAxis()) {
            if (getIntervalsX() == null)
                setIntervalsForAxis(axisType, prefIntervals);
            return false;
        }

        // Handle Bar3D Z axis special
        if (axisType == AxisType.Z && getChartType() == ChartType.BAR_3D) {
            if (getIntervalsZ() == null)
                setIntervalsForAxis(axisType, prefIntervals);
            return false;
        }

        // Calculate intervals for current axis length
        double axisLen = getAxisLengthInViewForAxis(axisType);
        //Size labelSize = _dataArea.getChartHelper().getAxisView(axisType).getMaxTickLabelRotatedSize();
        double divLen = 40;
        Intervals intervals = Intervals.getIntervalsForMinMaxLen(prefMin, prefMax, axisLen, divLen, true, true);

        // If equal, then set updated intervals and mark didUpdate
        if (intervals.equals(getIntervalsForAxis(axisType)))
            return false;

        // Set updated intervals and return true
        setIntervalsForAxis(axisType, intervals);
        return true;
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
        return axisLine.getLengthXY();
    }

    /**
     * Returns the axis line for given axis type.
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
        double dataX1 = intervalsX.getMin();
        double dataX2 = intervalsX.getMax();
        if (!isX) {
            if (isForwardXY) {
                boolean isMinX = axisBoxShape.isSideVisible(Side3D.RIGHT);
                dataX1 = dataX2 = isMinX ? dataX1 : dataX2;
            }
            else {
                if (isY) {
                    boolean isMinX = axisBoxShape.isSideVisible(Side3D.RIGHT);
                    dataX1 = dataX2 = isMinX ? dataX1 : dataX2;
                }
                else {
                    boolean isMinX = axisBoxShape.isSideVisible(Side3D.BACK);
                    dataX1 = dataX2 = isMinX ? dataX1 : dataX2;
                }
            }
        }

        // Get Y component of line points
        double dataY1 = intervalsY.getMin();
        double dataY2 = intervalsY.getMax();
        if (!isY) {
            if (isForwardXY) {
                boolean isMinY = axisBoxShape.isSideVisible(Side3D.BOTTOM);
                dataY1 = dataY2 = isMinY ? dataY1 : dataY2;
            }
            else {
                if (isX) {
                    boolean isMinY = axisBoxShape.isSideVisible(Side3D.FRONT);
                    dataY1 = dataY2 = isMinY ? dataY1 : dataY2;
                }
                else {
                    boolean isMinY = axisBoxShape.isSideVisible(Side3D.RIGHT);
                    dataY1 = dataY2 = isMinY ? dataY1 : dataY2;
                }
            }
        }

        // Get Z component of line points
        double dataZ1 = intervalsZ.getMin();
        double dataZ2 = intervalsZ.getMax();
        if (!isZ) {
            if (isForwardXY) {
                if (isX) {
                    boolean isMinZ = axisBoxShape.isSideVisible(Side3D.FRONT);
                    dataZ1 = dataZ2 = isMinZ ? dataZ1 : dataZ2;
                }
                else {
                    boolean isMinZ = axisBoxShape.isSideVisible(Side3D.BACK);
                    dataZ1 = dataZ2 = isMinZ ? dataZ1 : dataZ2;
                }
            }
            else {
                boolean isMinZ = axisBoxShape.isSideVisible(Side3D.BOTTOM);
                dataZ1 = dataZ2 = isMinZ ? dataZ1 : dataZ2;
            }
        }

        // Return line
        return new Line3D(dataX1, dataY1, dataZ1, dataX2, dataY2, dataZ2);
    }

    /**
     * Returns the axis grid line at axis min for given axis type (perpendicular to axis line).
     */
    public Line3D getAxisGridLineInDataSpace(AxisType axisType, double aFractionalOnAxisLine)
    {
        // Get intervals
        Intervals intervalsX = getPrefIntervalsX();
        Intervals intervalsY = getPrefIntervalsY();
        Intervals intervalsZ = getPrefIntervalsZ();

        // Get axis line
        Line3D line = getAxisLineInDataSpace(axisType);
        Point3D p1 = line.getP1();
        Point3D p2 = line.getP2();

        boolean isForwardXY = isForwardXY();
        boolean spreadX = false;
        boolean spreadY = false;
        boolean spreadZ = false;

        // Handle X axis
        if (axisType == AxisType.X) {
            p2.x = p1.x = MathUtils.mapFractionalToRangeValue(aFractionalOnAxisLine, intervalsX.getMin(), intervalsX.getMax());
            if (isForwardXY)
                spreadZ = true;
            else spreadY = true;
        }

        // Handle Y axis
        else if (axisType.isAnyY()) {
            p2.y = p1.y = MathUtils.mapFractionalToRangeValue(aFractionalOnAxisLine, intervalsY.getMin(), intervalsY.getMax());
            if (isForwardXY)
                spreadX = true;
            else spreadX = true;
        }

        // Handle Z axis
        else if (axisType == AxisType.Z) {
            p2.z = p1.z = MathUtils.mapFractionalToRangeValue(aFractionalOnAxisLine, intervalsZ.getMin(), intervalsZ.getMax());
            if (isForwardXY)
                spreadX = true;
            else {
                AxisBoxShape axisBoxShape = getAxisBoxShape();
                boolean isRightVisible = axisBoxShape.isSideVisible(Side3D.RIGHT);
                boolean isZX = isRightVisible ? MathUtils.equals(p1.x, intervalsX.getMin()) : MathUtils.equals(p1.x, intervalsX.getMax());
                if (isZX)
                    spreadX = true;
                else spreadY = true;
            }
        }

        // Handle spreading of grid line point component
        if (spreadX) {
            boolean isMaxX = MathUtils.equals(p1.x, intervalsX.getMax());
            p1.x = isMaxX ? intervalsX.getMin() : intervalsX.getMax();
            p2.x = isMaxX ? intervalsX.getMax() : intervalsX.getMin();
        }
        else if (spreadY) {
            boolean isMaxY = MathUtils.equals(p1.y, intervalsY.getMax());
            p1.y = isMaxY ? intervalsY.getMin() : intervalsY.getMax();
            p2.y = isMaxY ? intervalsY.getMax() : intervalsY.getMin();
        }
        else if (spreadZ) {
            boolean isMaxZ = MathUtils.equals(p1.z, intervalsZ.getMax());
            p1.z = isMaxZ ? intervalsZ.getMin() : intervalsZ.getMax();
            p2.z = isMaxZ ? intervalsZ.getMax() : intervalsZ.getMin();
        }

        // Return
        return line;
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
    public void convertDataToScene(Point3D aPoint)
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
    }

    /**
     * Returns given XYZ data point as Point3D in View coords.
     */
    public void convertDataToView(Point3D aPoint)
    {
        convertDataToScene(aPoint);
        convertSceneToView(aPoint);
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
     * Returns given XYZ data point as Point3D in View coords.
     */
    public void convertSceneToView(Point3D aPoint)
    {
        Matrix3D sceneToView = _camera.getSceneToView();
        sceneToView.transformPoint(aPoint);
    }

    /**
     * Override to suppress.
     */
    @Override
    protected void paintDataArea(Painter aPntr)
    {
        // If no content yet, just return
        if (_scene.getChildCount() == 0) return;

        // Update AxisBoxShape Sides Visibility
        AxisBoxShape axisBoxShape = getAxisBoxShape();
        axisBoxShape.setSidesVisibleForCamera();

        // Update Intervals if needed
        boolean didUpdateIntervals = updateIntervalsIfNeeded();
        if (didUpdateIntervals)
            axisBoxShape.addSideGrids();

        // Paint AxisBoxText
        paintAxisBoxText(aPntr);
    }

    /**
     * Paints the axis box text (axis and tick labels).
     */
    protected void paintAxisBoxText(Painter aPntr)
    {
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
     * Called when camera changes.
     */
    protected void cameraDidChange()
    {
        if (_cameraChangeRun == null) {
            if (ViewUtils.isMouseDown())
                ViewUtils.runOnMouseUp(_cameraChangeRun = _cameraChangeRunImpl);
            else getEnv().runLater(_cameraChangeRun = _cameraChangeRunImpl);
        }
    }

    /**
     * Called when camera changes.
     */
    protected void cameraDidChangeImpl()
    {
        _axisBoxPainter.configureAxisProxyForAxis(_axisProxyX, AxisType.X);
        _axisBoxPainter.configureAxisProxyForAxis(_axisProxyY, AxisType.Y);
        _axisBoxPainter.configureAxisProxyForAxis(_axisProxyZ, AxisType.Z);
        _cameraChangeRun = null;
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
