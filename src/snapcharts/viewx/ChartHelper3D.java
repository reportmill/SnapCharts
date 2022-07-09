/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx3d.Camera;
import snap.gfx3d.CameraView;
import snap.view.ViewAnim;
import snap.view.ViewUtils;
import snapcharts.model.Axis;
import snapcharts.model.AxisType;
import snapcharts.model.ChartPart;
import snapcharts.view.*;

/**
 * This ChartHelper subclass adds support for 3D.
 */
public abstract class ChartHelper3D extends ChartHelper {

    // A DataArea for projections
    private DataArea3D  _projectionDataData;

    /**
     * Constructor.
     */
    protected ChartHelper3D(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the DataArea3D.
     */
    public DataArea3D getDataArea3D()
    {
        DataArea[] dataAreas = getDataAreas();
        return dataAreas.length > 0 && dataAreas[0] instanceof DataArea3D ? (DataArea3D) dataAreas[0] : null;
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
            DataArea3D dataArea3D = getDataArea3D();
            if (axisType.isX())
                return dataArea3D._axisProxyX;
            if (axisType.isAnyY())
                return dataArea3D._axisProxyY;
            if (axisType.isZ())
                return dataArea3D._axisProxyZ;
        }

        // Do normal version
        return super.getChartPartViewForPart(aChartPart);
    }

    /**
     * Returns a DataArea3D for projections.
     */
    public DataArea3D getProjectionDataArea()
    {
        if (_projectionDataData != null) return _projectionDataData;
        DataArea3D dataArea3D = createProjectionDataArea();
        ContentView contentView = getContentView();
        ViewUtils.setParent(dataArea3D, contentView);
        dataArea3D.setProjection(true);
        return _projectionDataData = dataArea3D;
    }

    /**
     * Creates a DataArea3D for projections.
     */
    protected abstract DataArea3D createProjectionDataArea();

    /**
     * Override to reset view transform.
     */
    @Override
    public void resetAxesAnimated()
    {
        DataArea3D dataArea3D = getDataArea3D();
        CameraView cameraView = dataArea3D.getCameraView();
        Camera camera = cameraView.getCamera();
        camera.setPrefGimbalRadius(camera.getPrefGimbalRadius());

        ViewAnim anim = cameraView.getAnimCleared(600);
        anim.startAutoRegisterChanges(Camera.Yaw_Prop, Camera.Pitch_Prop, Camera.Roll_Prop, Camera.PrefGimbalRadius_Prop);
        camera.setYaw(DataArea3D.DEFAULT_YAW);
        camera.setPitch(DataArea3D.DEFAULT_PITCH);
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
        DataArea3D dataArea3D = getDataArea3D();
        CameraView cameraView = dataArea3D.getCameraView();
        Camera camera = cameraView.getCamera();
        double gimbalRad = camera.getGimbalRadius();
        double gimbalRad2 = gimbalRad * scale;

        ViewAnim anim = cameraView.getAnimCleared(600);
        anim.setValue(CameraView.PrefGimbalRadius_Prop, gimbalRad2);
        anim.play();
    }
}
