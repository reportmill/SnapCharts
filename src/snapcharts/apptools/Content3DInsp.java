/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx3d.*;
import snap.util.ArrayUtils;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.model.AxisType;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.Scene;
import snapcharts.view.ChartHelper;
import snapcharts.viewx.*;
import snapcharts.view.ChartView;

/**
 * Tool for visual editing RMScene3D.
 */
public class Content3DInsp extends ChartPartInsp {
    
    // The Trackball control for rotating selected scene3d
    private Trackball  _trackball;

    // Constant for number of RendererButtons
    private static final int RENDERER_BUTTON_COUNT = 3;

    // Constant for default focal length
    private static double FOCAL_LENGTH_DEFAULT = 8 * 72;

    /**
     * Constructor.
     */
    public Content3DInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    @Override
    public String getName()
    {
        return "3D Settings";
    }

    @Override
    public ChartPart getChartPart()
    {
        return getChart();
    }

    /**
     * Returns the DataArea3D.
     */
    public DataArea3D getDataArea3D()
    {
        ChartView chartView = _chartPane.getChartView();
        ChartHelper chartHelper = chartView.getChartHelper();
        ChartHelper3D chartHelper3D = chartHelper instanceof ChartHelper3D ? (ChartHelper3D) chartHelper : null;
        if (chartHelper3D == null)
            return null;
        return chartHelper3D.getDataArea3D();
    }

    /**
     * Returns the Camera View.
     */
    public CameraView getCameraView()
    {
        DataArea3D dataArea3D = getDataArea3D();
        CameraView cameraView = dataArea3D != null ? dataArea3D.getCameraView() : null;
        return cameraView;
    }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Get Trackball
        _trackball = getView("Trackball", Trackball.class);

        // Initialize RendererButtonX
        String[] rendererFactoryNames = RendererFactory.getFactoryNames();
        for (int i = 0, iMax = rendererFactoryNames.length; i < iMax && i < RENDERER_BUTTON_COUNT; i++) {
            String rendererName = rendererFactoryNames[i];
            ToggleButton button = getView("RendererButton" + i, ToggleButton.class);
            button.setText(rendererName);
            button.setPosition(iMax == 1 ? null : i == 0 ? Pos.CENTER_LEFT : i + 1 < iMax ? Pos.CENTER : Pos.CENTER_RIGHT);
        }
        for (int i = rendererFactoryNames.length; i < RENDERER_BUTTON_COUNT; i++) {
            ToggleButton button = getView("RendererButton" + i, ToggleButton.class);
            button.setVisible(false);
            button.setEnabled(rendererFactoryNames.length > 1);
        }
    }

    /**
     * Updates UI panel from currently selected scene3d.
     */
    public void resetUI()
    {
        // Get the selected scene
        Chart chart = getChart();
        Scene scene = chart.getScene();
        CameraView cameraView = getCameraView(); if (cameraView == null) return;
        Camera camera = cameraView.getCamera();

        // Reset AspectModeViewButton, AspectModeDataButton, AspectModeDirectButton
        Scene.AspectMode aspectMode = scene.getAspectMode();
        setViewValue("AspectModeViewButton", aspectMode == Scene.AspectMode.View);
        setViewValue("AspectModeDataButton", aspectMode == Scene.AspectMode.Data);
        setViewValue("AspectModeDirectButton", aspectMode == Scene.AspectMode.Direct);

        // Reset AspectScaleXText, AspectScaleYText, AspectScaleZText
        double scaleX = scene.getAspectScaleX(), defaultScaleX = scene.getAspectScaleDefault(AxisType.X);
        double scaleY = scene.getAspectScaleY(), defaultScaleY = scene.getAspectScaleDefault(AxisType.Y);
        double scaleZ = scene.getAspectScaleZ(), defaultScaleZ = scene.getAspectScaleDefault(AxisType.Z);
        setViewValue("AspectScaleXText", scaleX);
        setViewValue("AspectScaleYText", scaleY);
        setViewValue("AspectScaleZText", scaleZ);

        // Reset AspectScaleXResetButton, AspectScaleYResetButton, AspectScaleZResetButton
        setViewVisible("AspectScaleXResetButton", scaleX != defaultScaleX);
        setViewVisible("AspectScaleYResetButton", scaleY != defaultScaleY);
        setViewVisible("AspectScaleZResetButton", scaleZ != defaultScaleZ);

        // Reset RendererButtonX
        String[] rendererFactoryNames = RendererFactory.getFactoryNames();
        String rendererName = camera.getRenderer().getName();
        int rendererIndex = ArrayUtils.indexOf(rendererFactoryNames, rendererName);
        for (int i = 0; i < RENDERER_BUTTON_COUNT; i++)
            setViewValue("RendererButton" + i, i == rendererIndex);

        // Reset YawSpinner, PitchSpinner, RollSpinner
        setViewValue("YawSpinner", Math.round(camera.getYaw()));
        setViewValue("PitchSpinner", Math.round(camera.getPitch()));
        setViewValue("RollSpinner", Math.round(camera.getRoll()));

        // Reset scene control
        _trackball.syncFrom(camera);

        // Reset FOVSlider, FOVText, FOVResetButton
        double focalLen = camera.getFocalLength();
        setViewValue("FOVSlider", focalLen / 72);
        setViewValue("FOVText", focalLen / 72);
        getView("FOVResetButton").setPaintable(focalLen != FOCAL_LENGTH_DEFAULT);

        // Reset GimbalRadiusThumbWheel, GimbalRadiusText, GimbalRadiusResetButton
        double gimbalRadius = camera.getPrefGimbalRadius();
        boolean gimbalRadiusSet = camera.isPrefGimbalRadiusSet();
        setViewValue("GimbalRadiusThumbWheel", gimbalRadius);
        setViewValue("GimbalRadiusText", gimbalRadius);
        getView("GimbalRadiusResetButton").setPaintable(gimbalRadiusSet);
        Color gimbalRadiusTextColor = gimbalRadiusSet ? Color.BLACK : Color.GRAY;
        getView("GimbalRadiusText", TextField.class).setTextFill(gimbalRadiusTextColor);

        // Reset OrthoViewButton
        setViewValue("OrthoViewButton", camera.isOrtho());
    }

    /**
     * Updates currently selected scene 3d from UI panel controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get the currently selected scene
        Chart chart = getChart();
        Scene scene = chart.getScene();
        CameraView cameraView = getCameraView(); if (cameraView == null) return;
        Camera camera = cameraView.getCamera();

        // Handle AspectModeViewButton, AspectModeDataButton, AspectModeDirectButton
        if (anEvent.equals("AspectModeViewButton"))
            scene.setAspectMode(Scene.AspectMode.View);
        if (anEvent.equals("AspectModeDataButton"))
            scene.setAspectMode(Scene.AspectMode.Data);
        if (anEvent.equals("AspectModeDirectButton"))
            scene.setAspectMode(Scene.AspectMode.Direct);

        // Reset AspectScaleXText, AspectScaleYText, AspectScaleZText
        if (anEvent.equals("AspectScaleXText"))
            scene.setAspectScaleX(anEvent.getFloatValue());
        if (anEvent.equals("AspectScaleYText"))
            scene.setAspectScaleY(anEvent.getFloatValue());
        if (anEvent.equals("AspectScaleZText"))
            scene.setAspectScaleZ(anEvent.getFloatValue());

        // Reset AspectScaleXResetButton, AspectScaleYResetButton, AspectScaleZResetButton
        if (anEvent.equals("AspectScaleXResetButton"))
            scene.setAspectScaleX(scene.getAspectScaleDefault(AxisType.X));
        if (anEvent.equals("AspectScaleYResetButton"))
            scene.setAspectScaleY(scene.getAspectScaleDefault(AxisType.Y));
        if (anEvent.equals("AspectScaleZResetButton"))
            scene.setAspectScaleZ(scene.getAspectScaleDefault(AxisType.Z));

        // Handle RendererButtonX
        if (anEvent.getName().startsWith("RendererButton")) {
            String rendererFactoryName = anEvent.getView().getText();
            RendererFactory rendererFactory = RendererFactory.getFactoryForName(rendererFactoryName);
            if (rendererFactory != null) {
                Renderer renderer = rendererFactory.newRenderer(camera);
                camera.setRenderer(renderer);
            }
        }

        // Handle YawSpinner, PitchSpinner, RollSpinner
        if (anEvent.equals("YawSpinner"))
            camera.setYaw(anEvent.getFloatValue());
        if (anEvent.equals("PitchSpinner"))
            camera.setPitch(anEvent.getFloatValue());
        if (anEvent.equals("RollSpinner"))
            camera.setRoll(anEvent.getFloatValue());

        // Handle Trackball
        if (anEvent.equals("Trackball"))
            _trackball.syncTo(camera);

        // Handle FOVSlider or FOVText
        if (anEvent.equals("FOVSlider") || anEvent.equals("FOVText")) {
            double focalLen = anEvent.equals("FOVSlider") ? anEvent.getIntValue() * 72 : anEvent.getFloatValue() * 72;
            camera.setFocalLength(focalLen);
        }
        if (anEvent.equals("FOVResetButton"))
            camera.setFocalLength(FOCAL_LENGTH_DEFAULT);

        // Handle GimbalRadiusThumbWheel or GimbalRadiusText
        if (anEvent.equals("GimbalRadiusThumbWheel") || anEvent.equals("GimbalRadiusText")) {
            double dist = anEvent.equals("GimbalRadiusThumbWheel") ? anEvent.getIntValue() : anEvent.getFloatValue();
            camera.setPrefGimbalRadius(dist);
        }

        // Handle GimbalRadiusResetButton
        if (anEvent.equals("GimbalRadiusResetButton"))
            camera.setPrefGimbalRadius(0);

        // Handle TopViewButton, BottomViewButton, LeftViewButton, RightViewButton, FrontViewButton, BackViewButton
        if (anEvent.equals("TopViewButton")) setCameraViewToSide(Side3D.TOP);
        if (anEvent.equals("BottomViewButton")) setCameraViewToSide(Side3D.BOTTOM);
        if (anEvent.equals("LeftViewButton")) setCameraViewToSide(Side3D.LEFT);
        if (anEvent.equals("RightViewButton")) setCameraViewToSide(Side3D.RIGHT);
        if (anEvent.equals("FrontViewButton")) setCameraViewToSide(Side3D.FRONT);
        if (anEvent.equals("BackViewButton")) setCameraViewToSide(Side3D.BACK);
        if (anEvent.equals("CameraViewResetButton")) setCameraViewToSide(null);

        // Handle OrthoViewButton
        if (anEvent.equals("OrthoViewButton"))
            camera.setOrtho(anEvent.getBoolValue());
    }

    /**
     * Set camera view to given side.
     */
    private void setCameraViewToSide(Side3D aSide)
    {
        // Reset PrefGimbalRadius
        CameraView cameraView = getCameraView(); if (cameraView == null) return;
        Camera camera = cameraView.getCamera();
        boolean isPrefGimbalRadiusSet = camera.isPrefGimbalRadiusSet();

        // Get animator and startAutoRegisterChanges
        ViewAnim anim = cameraView.getAnimCleared(1000);
        anim.startAutoRegisterChanges(Camera.Yaw_Prop, Camera.Pitch_Prop, Camera.Roll_Prop, Camera.PrefGimbalRadius_Prop);

        // Change Camera to view side
        if (aSide == null) {
            DataArea3D dataArea3D = getDataArea3D();
            dataArea3D.setDefaultViewTransform();
        }
        else camera.setYawPitchRollForSide(aSide);

        // If PrefGimbalRadius set, reset to pref radius in final orientation
        if (isPrefGimbalRadiusSet) {
            double prefGR = camera.calcPrefGimbalRadius();
            camera.setPrefGimbalRadius(prefGR);
        }

        // stopAutoRegisterChanges and register to clear PrefGimbalRadius if it was set
        anim.stopAutoRegisterChanges();
        if (isPrefGimbalRadiusSet)
            anim.setOnFinish(() -> camera.setPrefGimbalRadius(0));

        // Play animations
        anim.play();
    }
}