/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.view.*;
import snap.gfx3d.Camera3D;
import snap.gfx3d.CameraView;
import snap.gfx3d.Trackball;
import snapcharts.app.ChartPane;
import snapcharts.model.ChartPart;
import snapcharts.view.DataArea;
import snapcharts.viewx.Bar3DDataArea;
import snapcharts.view.ChartView;
import snapcharts.view.DataView;

/**
 * Tool for visual editing RMScene3D.
 */
public class Thr3DTool extends ChartPartInsp {
    
    // The Trackball control for rotating selected scene3d
    private Trackball  _trackball;

    /**
     * Constructor.
     */
    public Thr3DTool(ChartPane aChartPane)
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

        // Initialize RenderingComboBox
        setViewItems("RenderingComboBox", new String[] { "Real 3D", "Pseudo 3D" });
    }

    /**
     * Updates UI panel from currently selected scene3d.
     */
    public void resetUI()
    {
        // Get the selected scene
        CameraView cameraView = getCameraView(); if (cameraView == null) return;
        Camera3D camera = cameraView.getCamera();

        // Reset Rendering radio buttons
        setViewSelIndex("RenderingComboBox", camera.isPseudo3D() ? 1 : 0);

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
        setViewValue("FOVSlider", camera.getFocalLength() / 72);
        setViewValue("FOVText", camera.getFocalLength() / 72);
    }

    /**
     * Updates currently selected scene 3d from UI panel controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get the currently selected scene3d
        CameraView cameraView = getCameraView(); if (cameraView == null) return;
        Camera3D camera = cameraView.getCamera();

        // Handle RenderingComboBox
        if (anEvent.equals("RenderingComboBox"))
            setPseudo3D(camera, anEvent.getSelIndex() == 1);

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
    }

    /**
     * Sets Psuedo3D with some good settings.
     */
    private void setPseudo3D(Camera3D aCam, boolean isPseudo3D)
    {
        // Set defaults for pseudo 3d
        aCam.setPseudo3D(isPseudo3D);
        if (isPseudo3D) {
            aCam.setPseudoSkewX(.3f);
            aCam.setPseudoSkewY(-.25f);
            aCam.setDepth(20);
            aCam.setFocalLength(60*72);
        }

        // Set defaults for true 3d
        else {
            aCam.setYaw(23);
            aCam.setPitch(12);
            aCam.setDepth(100);
            aCam.setFocalLength(8*72);
        }
    }
}