/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Painter;
import snap.gfx3d.Camera3D;
import snap.gfx3d.CameraView;
import snap.gfx3d.Scene3D;
import snap.util.PropChange;
import snapcharts.model.Scene;
import snapcharts.model.Trace;
import snapcharts.model.TraceList;
import snapcharts.view.AxisViewX;
import snapcharts.view.AxisViewY;
import snapcharts.view.ChartHelper;
import snapcharts.view.DataArea;

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

    // The ChartBuilder
    protected AxisBoxBuilder  _chartBuilder;

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

        // Set default
        setDefaultViewTransform();
    }

    /**
     * Returns the CameraView.
     */
    public CameraView getCameraView()  { return _camView; }

    /**
     * Returns the ChartBuilder.
     */
    public AxisBoxBuilder getChartBuilder()
    {
        if (_chartBuilder != null) return _chartBuilder;
        _chartBuilder = createChartBuilder();
        return _chartBuilder;
    }

    /**
     * Creates the ChartBuilder.
     */
    protected abstract AxisBoxBuilder createChartBuilder();

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
        AxisBoxBuilder chartBuilder = getChartBuilder();
        chartBuilder.rebuildAxisBox();
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
        AxisBoxBuilder chartBuilder = getChartBuilder();
        AxisBoxPainter axisBoxPainter = _chartBuilder.getAxisBoxPainter();
        axisBoxPainter.paintTickLabels(aPntr);
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
