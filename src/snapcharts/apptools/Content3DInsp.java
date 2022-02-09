/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.gfx.Color;
import snap.gfx3d.*;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.model.ChartPart;
import snapcharts.view.DataArea;
import snapcharts.viewx.Bar3DDataArea;
import snapcharts.view.ChartView;
import snapcharts.view.DataView;

/**
 * Tool for visual editing RMScene3D.
 */
public class Content3DInsp extends ChartPartInsp {
    
    // The Trackball control for rotating selected scene3d
    private Trackball  _trackball;

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
     * Returns the Camera View.
     */
    public CameraView getCameraView()
    {
        ChartView chartView = _chartPane.getChartView();
        DataView dataView = chartView.getDataView();
        DataArea[] dataAreas = dataView.getDataAreas();
        DataArea dataArea = dataAreas.length > 0 ? dataAreas[0] : null;
        Bar3DDataArea da3d = dataArea instanceof Bar3DDataArea ? (Bar3DDataArea) dataArea : null;
        if (da3d == null) return null;
        return da3d.getCameraView();
    }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Get Trackball
        _trackball = getView("Trackball", Trackball.class);

        // Initialize RendererComboBox
        String[] rendererFactoryNames = RendererFactory.getFactoryNames();
        if (rendererFactoryNames.length > 1)
            setViewItems("RendererComboBox", rendererFactoryNames);
        else {
            setViewVisible("RendererComboBox", false);
            getView("RendererComboBox").getParent().setVisible(false);
        }
    }

    /**
     * Updates UI panel from currently selected scene3d.
     */
    public void resetUI()
    {
        // Get the selected scene
        CameraView cameraView = getCameraView(); if (cameraView == null) return;
        Camera3D camera = cameraView.getCamera();

        // Reset RendererComboBox
        if (isViewVisible("RendererComboBox")) {
            String rendererName = camera.getRenderer().getName();
            setViewSelItem("RendererComboBox", rendererName);
        }

        // Reset YawSpinner, PitchSpinner, RollSpinner
        setViewValue("YawSpinner", Math.round(camera.getYaw()));
        setViewValue("PitchSpinner", Math.round(camera.getPitch()));
        setViewValue("RollSpinner", Math.round(camera.getRoll()));

        // Reset scene control
        _trackball.syncFrom(camera);

        // Reset Depth slider/text
        setViewValue("DepthSlider", camera.getDepth());
        setViewValue("DepthText", camera.getDepth());

        // Reset Field of view slider/text
        double focalLen = camera.getFocalLength();
        setViewValue("FOVSlider", focalLen / 72);
        setViewValue("FOVText", focalLen / 72);

        // Reset GimbalRadiusThumbWheel, GimbalRadiusText, GimbalRadiusResetButton
        double gimbalRadius = camera.getPrefGimbalRadius();
        boolean gimbalRadiusSet = camera.isPrefGimbalRadiusSet();
        setViewValue("GimbalRadiusThumbWheel", gimbalRadius);
        setViewValue("GimbalRadiusText", gimbalRadius);
        setViewVisible("GimbalRadiusResetButton", gimbalRadiusSet);
        Color gimbalRadiusTextColor = gimbalRadiusSet ? Color.BLACK : Color.GRAY;
        getView("GimbalRadiusText", TextField.class).setTextFill(gimbalRadiusTextColor);
    }

    /**
     * Updates currently selected scene 3d from UI panel controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get the currently selected scene3d
        CameraView cameraView = getCameraView(); if (cameraView == null) return;
        Camera3D camera = cameraView.getCamera();

        // Handle RendererComboBox
        if (anEvent.equals("RendererComboBox")) {
            String rendererFactoryName = (String) anEvent.getSelItem();
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

        // Handle DepthSlider and DepthText
        if (anEvent.equals("DepthSlider") || anEvent.equals("DepthText")) {
            double depth = anEvent.equals("DepthSlider") ? anEvent.getIntValue() : anEvent.getFloatValue();
            camera.setDepth(depth);
        }

        // Handle FOVSlider or FOVText
        if (anEvent.equals("FOVSlider") || anEvent.equals("FOVText")) {
            double focalLen = anEvent.equals("FOVSlider") ? anEvent.getIntValue() * 72 : anEvent.getFloatValue() * 72;
            camera.setFocalLength(focalLen);
        }

        // Handle GimbalRadiusThumbWheel or GimbalRadiusText
        if (anEvent.equals("GimbalRadiusThumbWheel") || anEvent.equals("GimbalRadiusText")) {
            double dist = anEvent.equals("GimbalRadiusThumbWheel") ? anEvent.getIntValue() : anEvent.getFloatValue();
            camera.setPrefGimbalRadius(dist);
        }

        // Handle GimbalRadiusResetButton
        if (anEvent.equals("GimbalRadiusResetButton"))
            camera.setPrefGimbalRadius(0);
    }
}