/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.gfx3d.*;
import snap.text.TextFormat;
import snap.util.MathUtils;
import snap.util.PropChange;
import snap.view.ViewAnim;
import snapcharts.model.*;
import snapcharts.view.*;

/**
 * A DataArea subclass to display the contents of bar chart.
 */
public class Bar3DDataArea extends BarDataArea {
    
    // The Camera
    protected CameraView  _camView;
    
    // The Camera
    private Camera3D _camera;
    
    // The Scene
    private Scene3D  _scene;

    // The ChartBuilder to build chart shape
    private Bar3DChartBuilder  _chartBuilder;

    // Constants
    private static final double DEFAULT_YAW = 26;
    private static final double DEFAULT_PITCH = 10;

    /**
     * Constructor.
     */
    public Bar3DDataArea(ChartHelper aChartHelper, Trace aTrace, boolean isVisible)
    {
        super(aChartHelper, aTrace);

        // If not visible, just return
        if (!isVisible) {
            setVisible(false);
            return;
        }

        _camView = new CameraView();
        addChild(_camView);
        _camera = _camView.getCamera();
        _scene = _camView.getScene();

        // Create/set ChartBuilder
        _chartBuilder = new Bar3DChartBuilder(this, _scene);

        setDefaultViewTransform();
    }

    /**
     * Returns the CameraView.
     */
    public CameraView getCameraView()  { return _camView; }

    /**
     * Resets the default view transform.
     */
    private void setDefaultViewTransform()
    {
        _camera.setYaw(DEFAULT_YAW);
        _camera.setPitch(DEFAULT_PITCH);
        _camera.setFocalLength(8 * 72);
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
     * Rebuilds the chart.
     */
    protected void rebuildChart()
    {
        _chartBuilder.rebuildAxisBox();
    }

    /**
     * Override to rebuild chart.
     */
    @Override
    public void setReveal(double aValue)
    {
        // Do normal version
        super.setReveal(aValue);

        if (_camView == null) return;
        rebuildChart();

        // Animate camera rotation
        Camera3D camera3D = _camView.getCamera();
        camera3D.setYaw(90 + (DEFAULT_YAW - 90) * aValue);
        camera3D.setPitch(0 + (DEFAULT_PITCH - 0) * aValue);
    }

    /**
     * Resets the ViewMatrix animated.
     */
    public void resetViewMatrixAnimated()
    {
        ViewAnim anim = _camView.getAnimCleared(1000);
        anim.setValue(CameraView.Yaw_Prop, DEFAULT_YAW);
        anim.setValue(CameraView.Pitch_Prop, DEFAULT_PITCH);
        anim.play();
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
     * Clears the sections when needed (change of data, size)
     */
    @Override
    protected void clearSections()
    {
        super.clearSections();
        if (_camView == null) return;
        _camView.relayout();
    }

    /**
     * Override to suppress.
     */
    @Override
    protected void paintDataArea(Painter aPntr)  { }

    /**
     * Override to suppress.
     */
    @Override
    protected void paintDataAreaAbove(Painter aPntr)
    {
        // If no AxisBox yet, just return
        if (_scene == null || _scene.getChildCount() == 0) return;

        // Paint Axis X tick labels
        paintTickLabelsX(aPntr);
        paintTickLabelsY(aPntr);
    }

    /**
     * Paint TickLabels for X axis.
     */
    protected void paintTickLabelsX(Painter aPntr)
    {
        // Get Axis intervals and Y+Z location of label in data space
        Intervals intervalsY = _chartBuilder.getIntervalsY();
        Intervals intervalsZ = _chartBuilder.getIntervalsZ();
        double dataY = intervalsY.getMin();
        double dataZ1 = intervalsZ.getMin();
        double dataZ2 = intervalsZ.getMax();

        // Create/configure TickLabel
        TickLabel tickLabel = _chartBuilder.getTickLabelForAxis(AxisType.X);

        // Get Trace and pointCount
        TraceList traceList = getTraceList();
        Trace trace = traceList.getTraceCount() > 0 ? traceList.getTrace(0) : null;
        int pointCount = traceList.getPointCount();

        // Iterate over points and create/set TickLabel
        for (int i = 0; i < pointCount; i++) {

            // Get 3D point for label
            double dataX = i + .5;
            Point3D axisPoint1 = convertDataToView(dataX, dataY, dataZ1);
            Point3D axisPoint2 = convertDataToView(dataX, dataY, dataZ2);

            // Configure TickLabel and paint
            String tickStr = trace.getString(i);
            tickLabel.setText(tickStr);

            // Paint TickLabel with correct bounds for axis line
            paintTickLabelForPoints(aPntr, tickLabel, axisPoint1, axisPoint2);
        }
    }

    /**
     * Paint TickLabels for Y axis.
     */
    protected void paintTickLabelsY(Painter aPntr)
    {
        // Get Axis intervals and Y+Z location of label in data space
        Intervals intervalsX = _chartBuilder.getIntervalsX();
        Intervals intervalsY = _chartBuilder.getIntervalsY();
        Intervals intervalsZ = _chartBuilder.getIntervalsZ();
        double dataZ1 = intervalsZ.getMin();
        double dataZ2 = intervalsZ.getMax();

        // Get DataX: Either Min/Max depending on which side is facing camera
        boolean isLeftSideFacingCamera = _chartBuilder.isSideFacingCamera(Side3D.LEFT);
        double dataX = isLeftSideFacingCamera ? intervalsX.getMin() : intervalsX.getMax();

        // Create/configure TickLabel
        TickLabel tickLabel = _chartBuilder.getTickLabelForAxis(AxisType.Y);
        Axis axis = getChart().getAxisForType(AxisType.Y);
        TextFormat tickFormat = axis.getTextFormat();

        // Iterate over points and create/set TickLabel
        for (int i = 0, iMax = intervalsY.getCount(); i < iMax; i++) {

            // If not full interval, just skip
            if (!intervalsY.isFullInterval(i)) continue;

            // Get 3D point for label
            double dataY = intervalsY.getInterval(i);
            Point3D axisPoint1 = convertDataToView(dataX, dataY, dataZ1);
            Point3D axisPoint2 = convertDataToView(dataX, dataY, dataZ2);

            // Get/set TickLabel.Text
            String tickStr = tickFormat.format(dataY);
            tickLabel.setText(tickStr);

            // Paint TickLabel with correct bounds for axis line
            paintTickLabelForPoints(aPntr, tickLabel, axisPoint1, axisPoint2);
        }
    }

    /**
     * Paints TickLabel for given axis line.
     */
    private void paintTickLabelForPoints(Painter aPntr, TickLabel tickLabel, Point3D axisPoint1, Point3D axisPoint2)
    {
        // Make sure axisPoint1 is the close point
        if (axisPoint2.z < axisPoint1.z) {
            Point3D temp = axisPoint2;
            axisPoint2 = axisPoint1;
            axisPoint1 = temp;
        }

        // Get radial angle
        double dx = axisPoint2.x - axisPoint1.x;
        double dy = axisPoint1.y - axisPoint2.y;
        double angleDeg = -getAngleBetweenPoints(1, 0, dx, dy);

        // Set TickLabel size and get rect
        tickLabel.setSizeToPrefSize();
        Rect tickLabelBounds = tickLabel.getBoundsLocal();

        // Position tickLabel so that
        Point perimiterPoint = tickLabelBounds.getPerimeterPointForRadial(angleDeg, true);
        double tickLabelX = axisPoint1.x - perimiterPoint.x;
        double tickLabelY = axisPoint1.y - perimiterPoint.y;
        tickLabel.setXY(tickLabelX, tickLabelY);
        tickLabel.paintStringView(aPntr);

        //tickLabel.setBorder(Color.PINK, 1);
        //aPntr.setColor(Color.GREEN); aPntr.fill(new snap.geom.Ellipse(axisPoint1.x - 3, axisPoint1.y - 3, 6, 6));
        //aPntr.setColor(Color.ORANGE); aPntr.fill(new snap.geom.Ellipse(axisPoint2.x - 3, axisPoint2.y - 3, 6, 6));
    }

    /**
     * Returns given XYZ data point as Point3D in View coords.
     */
    public Point3D convertDataToView(double aX, double aY, double aZ)
    {
        // Get intervals and AxisBox
        Intervals intervalsX = _chartBuilder.getIntervalsX();
        Intervals intervalsY = _chartBuilder.getIntervalsY();
        Intervals intervalsZ = _chartBuilder.getIntervalsZ();
        Shape3D axisBox = _scene.getChild(0);

        // Get XYZ in AxisBox space
        double axisBoxX = MathUtils.mapValueForRanges(aX, intervalsX.getMin(), intervalsX.getMax(), axisBox.getMinX(), axisBox.getMaxX());
        double axisBoxY = MathUtils.mapValueForRanges(aY, intervalsY.getMin(), intervalsY.getMax(), axisBox.getMinY(), axisBox.getMaxY());
        double axisBoxZ = MathUtils.mapValueForRanges(aZ, intervalsZ.getMin(), intervalsZ.getMax(), axisBox.getMinZ(), axisBox.getMaxZ());

        // Transform point from AxisBox (Scene) to View space
        Matrix3D sceneToView = _camera.getSceneToView();
        Point3D pointInView = sceneToView.transformPoint(axisBoxX, axisBoxY, axisBoxZ);

        // Return
        return pointInView;
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
     * Returns the angle between two XY points.
     */
    private static double getAngleBetweenPoints(double x1, double y1, double x2, double y2)
    {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double angleDeg = Math.toDegrees(angle);
        return angleDeg;
    }
}