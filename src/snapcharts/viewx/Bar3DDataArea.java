/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.*;
import snap.gfx3d.*;
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

    // The SceneBuilder
    private Bar3DSceneBuilder  _sceneBuilder;

    // Runnables to rebuild chart deferred/coalesced
    private Runnable  _rebuildChartRun, _rebuildChartRunImpl = () -> rebuildChartNow();

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

        // Create/set Bar3DSceneBuilder to create 3D scene
        _sceneBuilder = new Bar3DSceneBuilder(this, _scene);

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
        if (_rebuildChartRun == null)
            getEnv().runLater(_rebuildChartRun = _rebuildChartRunImpl);
    }

    /**
     * Rebuilds the chart immediately.
     */
    protected void rebuildChartNow()
    {
        _sceneBuilder.rebuildScene();
        _camView.repaint();
        _rebuildChartRun = null;
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
    protected void paintDataAreaAbove(Painter aPntr)
    {
        // If no AxisBox yet, just return
        if (_scene.getChildCount() == 0) return;

        // Paint Axis X tick labels
        paintTickLabelsX(aPntr);
    }

    /**
     * Override to suppress.
     */
    protected void paintTickLabelsX(Painter aPntr)
    {
        // Get Axis intervals and Y+Z location of label in data space
        //Intervals intervalsX = _sceneBuilder.getIntervalsX();
        Intervals intervalsY = _sceneBuilder.getIntervalsY();
        Intervals intervalsZ = _sceneBuilder.getIntervalsZ();
        double dataY = intervalsY.getMin();
        double dataZ = intervalsZ.getMax() * 1.2;

        // Get StringView
        AxisViewX axisViewX = getAxisViewX();
        Axis axisX = axisViewX.getAxis();
        TickLabel tickLabel = new TickLabel(0);

        // Get Trace and pointCount
        TraceList traceList = getTraceList();
        Trace trace = traceList.getTraceCount() > 0 ? traceList.getTrace(0) : null;
        int pointCount = traceList.getPointCount();

        // Get TickLabel attributes
        Font tickLabelFont = axisX.getFont();
        Paint tickTextFill = axisX.getTextFill();
        tickLabel.setFont(tickLabelFont);
        tickLabel.setTextFill(tickTextFill);

        // Iterate over points and create/set TickLabel
        for (int i = 0; i < pointCount; i++) {

            // Get 3D point for label
            double dataX = i + .5;
            Point3D pointInView = convertDataToView(dataX, dataY, dataZ);

            // Configure TickLabel and paint
            String tickStr = trace.getString(i);
            tickLabel.setText(tickStr);
            tickLabel.setSizeToPrefSize();
            tickLabel.setCenteredXY(pointInView.x, pointInView.y);
            tickLabel.paintStringView(aPntr);
        }
    }

    /**
     * Returns given XYZ data point as Point3D in View coords.
     */
    public Point3D convertDataToView(double aX, double aY, double aZ)
    {
        // Get intervals and AxisBox
        Intervals intervalsX = _sceneBuilder.getIntervalsX();
        Intervals intervalsY = _sceneBuilder.getIntervalsY();
        Intervals intervalsZ = _sceneBuilder.getIntervalsZ();
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
}