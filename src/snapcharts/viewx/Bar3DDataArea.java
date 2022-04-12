/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx3d.*;
import snap.view.ViewAnim;
import snapcharts.model.*;
import snapcharts.view.*;

/**
 * A DataArea subclass to display the contents of Bar3D chart.
 */
public class Bar3DDataArea extends DataArea3D {
    
    // The BarDataArea (2D) to build the 2D bar shapes
    protected BarDataArea  _barDataArea;

    /**
     * Constructor.
     */
    public Bar3DDataArea(ChartHelper aChartHelper, Trace aTrace, boolean isVisible)
    {
        super(aChartHelper, aTrace, isVisible);
        if (!isVisible)
            return;

        // Create/set BarDataArea (2D)
        _barDataArea = new BarDataArea(aChartHelper, aTrace, isVisible);
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

        // Forward to BarDataArea (2D)
        _barDataArea.setReveal(aValue);
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
        // Do normal version
        super.layoutImpl();

        // Shouldn't need this!
        if (!isVisible()) return;

        // Set size of BarDataArea (2D)
        _barDataArea.setSize(getWidth(), getHeight());
    }

    /**
     * Override to return new Bar3DChartBuilder.
     */
    protected AxisBoxBuilder createChartBuilder()
    {
        return new Bar3DChartBuilder(this, _scene);
    }
}