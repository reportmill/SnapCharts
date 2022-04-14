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
    protected CameraView  _camView;

    // The Camera
    protected Camera3D  _camera;

    // The Scene
    protected Scene3D  _scene;

    // The AxisBox
    protected AxisBoxShape _axisBoxShape;

    // The AxisBoxPainter to paint tick labels
    private AxisBoxTextPainter _axisBoxPainter;

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
        _camView = new CameraView();
        addChild(_camView);

        // Set Scene and Camera
        _camera = _camView.getCamera();
        _scene = _camView.getScene();

        // Create/set painter
        _axisBoxPainter = new AxisBoxTextPainter(this, _scene);

        // Set default
        setDefaultViewTransform();
    }

    /**
     * Returns the CameraView.
     */
    public CameraView getCameraView()  { return _camView; }

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

        // Get AxisBoxShape and add to scene
        _axisBoxShape = null;
        Shape3D axisBoxShape = getAxisBoxShape();
        _scene.addChild(axisBoxShape);

        // Repaint DataArea and reset runnable
        repaint();
        _rebuildChartRun = null;
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
        if (viewW != _camView.getWidth() || viewH != _camView.getHeight()) {
            _camView.setSize(viewW, viewH);
            rebuildChart();
        }
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

    /**
     * Returns the intervals for X axis.
     */
    public Intervals getIntervalsX()
    {
        AxisView axisView = getAxisViewX();
        return axisView.getIntervals();
    }

    /**
     * Returns the intervals for Y axis.
     */
    public Intervals getIntervalsY()
    {
        AxisView axisView = getAxisViewY();
        return axisView.getIntervals();
    }

    /**
     * Returns the intervals for Z axis.
     */
    public Intervals getIntervalsZ()
    {
        AxisView axisView = getAxisViewZ();
        return axisView.getIntervals();
    }

    /**
     * Returns the intervals for given axis type.
     */
    protected Intervals getIntervalsForAxisType(AxisType anAxisType)
    {
        switch (anAxisType) {
            case X: return getIntervalsX();
            case Y: return getIntervalsY();
            case Z: return getIntervalsZ();
            default: throw new RuntimeException("AxisBoxBuilder.getIntervalsForAxis: Invalid axis: " + anAxisType);
        }
    }

    /**
     * Returns whether given side is facing camera.
     */
    public boolean isSideFacingCamera(Side3D aSide)
    {
        AxisBoxShape axisBoxShape = getAxisBoxShape();
        FacetShape axisSide = axisBoxShape.getSideShape(aSide);
        Vector3D axisSideNormal = axisSide.getNormal();
        Camera3D camera = _scene.getCamera();
        Matrix3D sceneToCamera = camera.getSceneToCamera();
        Vector3D axisSideNormalInCamera = sceneToCamera.transformVector(axisSideNormal.clone());
        Vector3D cameraNormal = camera.getNormal();
        boolean isFacing = !axisSideNormalInCamera.isAligned(cameraNormal, false);
        return isFacing;
    }

    /**
     * Returns given XYZ data point as Point3D in View coords.
     */
    public Point3D convertDataToView(double aX, double aY, double aZ)
    {
        // Get intervals and AxisBox
        Intervals intervalsX = getIntervalsX();
        Intervals intervalsY = getIntervalsY();
        Intervals intervalsZ = getIntervalsZ();

        // Get AxisBox
        Shape3D axisBox = _scene.getChild(0);
        double minX = axisBox.getMinX(), maxX = axisBox.getMaxX();
        double minY = axisBox.getMinY(), maxY = axisBox.getMaxY();
        double minZ = axisBox.getMinZ(), maxZ = axisBox.getMaxZ();

        // Get XYZ in AxisBox space
        boolean isForwardXY = isForwardXY();
        double axisBoxX = MathUtils.mapValueForRanges(aX, intervalsX.getMin(), intervalsX.getMax(), minX, maxX);
        double axisBoxY = isForwardXY ?
                MathUtils.mapValueForRanges(aY, intervalsY.getMin(), intervalsY.getMax(), minY, maxY) :
                MathUtils.mapValueForRanges(aZ, intervalsZ.getMin(), intervalsZ.getMax(), minY, maxY);
        double axisBoxZ = isForwardXY ?
                MathUtils.mapValueForRanges(aZ, intervalsZ.getMin(), intervalsZ.getMax(), minZ, maxZ) :
                MathUtils.mapValueForRanges(aY, intervalsY.getMin(), intervalsY.getMax(), minZ, maxZ);

        // Transform point from AxisBox (Scene) to View space
        Matrix3D sceneToView = _camera.getSceneToView();
        Point3D pointInView = sceneToView.transformPoint(axisBoxX, axisBoxY, axisBoxZ);

        // Return
        return pointInView;
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
     * Override to properly size hidden X Axis.
     */
    @Override
    public AxisViewX getAxisViewX()
    {
        AxisViewX axisViewX = super.getAxisViewX();
        axisViewX.setWidth(getWidth());
        return axisViewX;
    }

    /**
     * Override to properly size hidden Y Axis.
     */
    @Override
    public AxisViewY getAxisViewY()
    {
        AxisViewY axisViewY = super.getAxisViewY();
        axisViewY.setHeight(getHeight());
        return axisViewY;
    }

    /**
     * Override to properly size hidden Z Axis.
     */
    @Override
    public AxisViewZ getAxisViewZ()
    {
        AxisViewZ axisViewZ = super.getAxisViewZ();
        axisViewZ.setHeight(getHeight());
        return axisViewZ;
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
}
