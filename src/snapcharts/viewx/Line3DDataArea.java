/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Painter;
import snap.gfx3d.*;
import snap.util.PropChange;
import snap.view.ViewAnim;
import snapcharts.model.*;
import snapcharts.view.AxisViewX;
import snapcharts.view.AxisViewY;
import snapcharts.view.ChartHelper;
import snapcharts.view.DataArea;

/**
 * A DataArea subclass to display the contents of bar chart.
 */
public class Line3DDataArea extends DataArea {

    // The Camera
    protected CameraView  _camView;

    // The Camera
    private Camera3D _camera;

    // The Scene
    private Scene3D _scene;

    // The SceneBuilder
    private Line3DSceneBuilder  _sceneBuilder;

    /**
     * Constructor.
     */
    public Line3DDataArea(ChartHelper aChartHelper, Trace aTrace)
    {
        super(aChartHelper, aTrace);

        // Create/add CameraView
        _camView = new CameraView() {
            protected void layoutImpl() { rebuildScene(); }
        };
        addChild(_camView);

        // Get Sceme
        _scene = _camView.getScene();

        // Create/set Bar3DSceneBuilder to create 3D scene
        _sceneBuilder = new Line3DSceneBuilder(this, _scene);

        // Get/configure camera
        _camera = _camView.getCamera();
        _camera.setYaw(26);
        _camera.setPitch(10);
        _camera.setFocalLength(8*72);
    }

    /**
     * Returns the number of suggested ticks between the intervals of the RPG'd graph.
     */
    public int getMinorTickCount()
    {
        // Calculate height per tick - if height greater than 1 inch, return 4, greater than 3/4 inch return 3, otherwise 1
        int ivalCount = getIntervalsY().getCount();
        double heightPerTick = getHeight()/(ivalCount - 1);
        return heightPerTick>=72 ? 4 : heightPerTick>=50 ? 3 : 1;
    }

    /**
     * Rebuilds the chart.
     */
    protected void rebuildScene()
    {
        _sceneBuilder.rebuildScene();
    }

    /**
     * Override to rebuild chart.
     */
    /*public void setReveal(double aValue)
    {
        super.setReveal(aValue);
        _camView.relayout();
        if (aValue==0) {
            _camView.setYaw(90);
            _camView.setPitch(0);
            _camView.setOffsetZ(200);
            ViewAnim anim = _camView.getAnimCleared(1000);
            anim.setValue(CameraView.Yaw_Prop,26);
            anim.setValue(CameraView.Pitch_Prop,10);
            anim.setValue(CameraView.OffsetZ_Prop,0).setLinear();
            anim.play();
        }
    }*/

    /**
     * Override to resize CamView.
     */
    protected void layoutImpl()
    {
        double viewW = getWidth();
        double viewH = getHeight();
        _camView.setSize(viewW, viewH);
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

        // Handle Data changes
        Object src = aPC.getSource();
        if (src instanceof Trace || src instanceof TraceList) {
            _camView.relayout();
        }
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