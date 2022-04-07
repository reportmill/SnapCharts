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
 * A DataArea subclass to display the contents of Contour3D chart.
 */
public class Contour3DDataArea extends DataArea {

    // The Camera
    protected CameraView  _camView;

    // The Camera
    private Camera3D _camera;

    // The Scene
    private Scene3D _scene;

    // The ChartBuilder to build chart shape
    private Contour3DChartBuilder  _chartBuilder;

    /**
     * Constructor.
     */
    public Contour3DDataArea(ChartHelper aChartHelper, Trace aTrace, boolean isVisible)
    {
        super(aChartHelper, aTrace);

        // If not visible, just return
        if (!isVisible) {
            setVisible(false);
            return;
        }

        // Create/add CameraView
        _camView = new CameraView();
        addChild(_camView);

        // Get Sceme
        _scene = _camView.getScene();

        // Create/set ChartBuilder
        _chartBuilder = new Contour3DChartBuilder(this, _scene);

        // Get/configure camera
        _camera = _camView.getCamera();
        _camera.setYaw(26);
        _camera.setPitch(10);
        _camera.setFocalLength(8*72);
    }

    /**
     * Returns the CameraView.
     */
    public CameraView getCameraView()  { return _camView; }

    /**
     * Rebuilds the chart.
     */
    protected void rebuildChart()
    {
        _chartBuilder.rebuildAxisBox();
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
     * Called when a ChartPart changes.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);
        if (_camView == null)
            return;

        // Handle Trace changes: Rebuild chart
        Object source = aPC.getSource();
        if (source instanceof Trace || source instanceof TraceList)
            rebuildChart();

        // Handle Scene changes: Rebuild chart
        if (source instanceof Scene)
            rebuildChart();
    }

    /**
     * Override to suppress.
     */
    @Override
    protected void paintFront(Painter aPntr) { }

    /**
     * Override to suppress.
     */
    @Override
    protected void paintDataArea(Painter aPntr)  { }
}