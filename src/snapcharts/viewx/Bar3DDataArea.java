/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.*;
import snap.gfx3d.*;
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
    private Camera3D  _camera;
    
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
        super(aChartHelper, aTrace, isVisible);

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
        _chartBuilder.paintTickLabelsX(aPntr);
        _chartBuilder.paintTickLabelsY(aPntr);
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