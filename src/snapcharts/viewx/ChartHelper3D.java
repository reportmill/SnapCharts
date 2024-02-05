/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx3d.Camera;
import snap.gfx3d.CameraView;
import snap.view.ViewAnim;
import snap.view.ViewUtils;
import snapcharts.charts.Axis;
import snapcharts.charts.AxisType;
import snapcharts.charts.ChartPart;
import snapcharts.view.*;

/**
 * This ChartHelper subclass adds support for 3D.
 */
public abstract class ChartHelper3D extends ChartHelper {

    // A TraceView3D for projections
    private TraceView3D  _projectionDataData;

    /**
     * Constructor.
     */
    protected ChartHelper3D(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the TraceView3D.
     */
    public TraceView3D getTraceView3D()
    {
        TraceView[] traceViews = getTraceViews();
        return traceViews.length > 0 && traceViews[0] instanceof TraceView3D ? (TraceView3D) traceViews[0] : null;
    }

    /**
     * Override to AxisProxy for Axis.
     */
    public ChartPartView getChartPartViewForPart(ChartPart aChartPart)
    {
        // Handle Axis: Return appropriate AxisProxy
        if (aChartPart instanceof Axis) {
            Axis axis = (Axis) aChartPart;
            AxisType axisType = axis.getType();
            TraceView3D traceView3D = getTraceView3D();
            if (axisType.isX())
                return traceView3D._axisProxyX;
            if (axisType.isAnyY())
                return traceView3D._axisProxyY;
            if (axisType.isZ())
                return traceView3D._axisProxyZ;
        }

        // Do normal version
        return super.getChartPartViewForPart(aChartPart);
    }

    /**
     * Returns a TraceView3D for projections.
     */
    public TraceView3D getProjectionTraceView()
    {
        if (_projectionDataData != null) return _projectionDataData;
        TraceView3D traceView = createProjectionTraceView();
        ContentView contentView = getContentView();
        ViewUtils.setParent(traceView, contentView);
        traceView.setProjection(true);
        return _projectionDataData = traceView;
    }

    /**
     * Creates a TraceView3D for projections.
     */
    protected abstract TraceView3D createProjectionTraceView();

    /**
     * Override to reset view transform.
     */
    @Override
    public void resetAxesAnimated()
    {
        TraceView3D traceView3D = getTraceView3D();
        CameraView cameraView = traceView3D.getCameraView();
        Camera camera = cameraView.getCamera();
        camera.setPrefGimbalRadius(camera.getPrefGimbalRadius());

        ViewAnim anim = cameraView.getAnimCleared(600);
        anim.startAutoRegisterChanges(Camera.Yaw_Prop, Camera.Pitch_Prop, Camera.Roll_Prop, Camera.PrefGimbalRadius_Prop);
        camera.setYaw(TraceView3D.DEFAULT_YAW);
        camera.setPitch(TraceView3D.DEFAULT_PITCH);
        camera.setRoll(0);
        camera.setPrefGimbalRadius(camera.calcPrefGimbalRadius());
        anim.stopAutoRegisterChanges();
        anim.setOnFinish(() -> camera.setPrefGimbalRadius(0));
        anim.play();
    }

    /**
     * Override for 3D.
     */
    @Override
    public void scaleAxesMinMaxForFactor(double aScale, boolean isAnimated)
    {
        double scale = aScale > 1.6 ? 1.6 : aScale < .625 ? .625 : aScale;
        TraceView3D traceView3D = getTraceView3D();
        CameraView cameraView = traceView3D.getCameraView();
        Camera camera = cameraView.getCamera();
        double gimbalRad = camera.getGimbalRadius();
        double gimbalRad2 = gimbalRad * scale;

        ViewAnim anim = cameraView.getAnimCleared(600);
        anim.setValue(CameraView.PrefGimbalRadius_Prop, gimbalRad2);
        anim.play();
    }
}
