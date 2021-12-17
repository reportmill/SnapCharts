/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.*;
import snap.gfx3d.*;
import snap.view.ViewAnim;
import snapcharts.model.Trace;
import snapcharts.view.AxisViewY;
import snapcharts.view.ChartHelper;

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
    
    /**
     * Constructor.
     */
    public Bar3DDataArea(ChartHelper aChartHelper, Trace aTrace)
    {
        super(aChartHelper, aTrace);

        _camView = new CameraView() {
            protected void layoutImpl() { rebuildScene(); }
        };
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
        _camera.setYaw(26);
        _camera.setPitch(10);
        _camera.setDepth(100);
        _camera.setFocalLength(8*72);
        _camera.setAdjustZ(true);
    }

    /**
     * Returns the number of suggested ticks between the intervals of the RPG'd graph.
     */
    public int getMinorTickCount()
    {
        // Calculate height per tick - if height greater than 1 inch, return 4, greater than 3/4 inch return 3, otherwise 1
        int ivalCount = getIntervalsY().getCount();
        double heightPerTick = getHeight()/(ivalCount - 1);
        return heightPerTick>=72? 4 : heightPerTick>=50? 3 : 1;
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
    protected void rebuildScene()
    {
        _sceneBuilder.rebuildScene();
    }

    /**
     * Override to rebuild chart.
     */
    @Override
    public void setReveal(double aValue)
    {
        super.setReveal(aValue);
        _camView.relayout();
        if (aValue==0) {
            _camView.setYaw(90);
            _camView.setPitch(0);
            _camView.setOffsetZ(200);
            resetViewMatrixAnimated();
        }
    }

    /**
     * Resets the ViewMatrix animated.
     */
    public void resetViewMatrixAnimated()
    {
        ViewAnim anim = _camView.getAnimCleared(1000);
        anim.setValue(CameraView.Yaw_Prop,26);
        anim.setValue(CameraView.Pitch_Prop,10);
        anim.setValue(CameraView.OffsetZ_Prop,0).setLinear().play();
        anim.setOnFinish(() -> _camera.setAdjustZ(true));
    }

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
     * Clears the sections when needed (change of data, size)
     */
    @Override
    protected void clearSections()
    {
        super.clearSections();
        _camView.relayout();
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