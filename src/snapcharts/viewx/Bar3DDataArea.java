/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx3d.*;
import snap.util.MathUtils;
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
     * Override to add bar chart.
     */
    @Override
    protected AxisBoxShape createAxisBoxShape()
    {
        // Do normal version
        AxisBoxShape axisBoxShape = super.createAxisBoxShape();

        // If Reveal is zero, just return
        double reveal = getReveal();
        if (MathUtils.equalsZero(reveal))
            return axisBoxShape;

        // Get BarDataArea and reset
        BarDataArea barDataArea = _barDataArea;
        _barDataArea.setSize(getWidth(), getHeight());
        barDataArea.clearSections();
        barDataArea.getSections();

        // Iterate over sections
        int traceCount = barDataArea._traceCount;
        int pointCount = barDataArea._pointCount;
        BarDataArea.Section[] sections = barDataArea.getSections();
        for (int i = 0; i < pointCount; i++) {
            BarDataArea.Section section = sections[i];

            // Iterate over traces and draw bars
            for (int j = 0; j < traceCount; j++) {
                BarDataArea.Bar bar = section.bars[j];
                addBar(axisBoxShape, bar.x, 1, bar.width, bar.height, bar.color);
            }
        }

        // Return shape
        return axisBoxShape;
    }

    /**
     * Adds a 3d bar to scene.
     */
    private void addBar(AxisBoxShape axisBoxShape, double aX, double aY, double aW, double aH, Color aColor)
    {
        // If reveal is set, modify Y/H
        double reveal = getReveal();
        if (reveal < 1)
            aH *= reveal;

        // Get depth, and Z values for back/front
        double sceneDepth = getAxisBoxPrefDepth();
        double barDepth = Math.min(aW, sceneDepth * .8);
        double z0 = sceneDepth / 2 - barDepth / 2;
        double z1 = sceneDepth / 2 + barDepth / 2;

        // Create/configure bar path/path3d and add to scene
        Shape barShape = new Rect(aX, aY, aW, aH);
        PathBox3D barShape3D = new PathBox3D(barShape, z0, z1);
        barShape3D.setColor(aColor);
        barShape3D.setStroke(Color.BLACK, 1);
        axisBoxShape.addChild(barShape3D);
    }

    /**
     * Override to rebuild chart.
     */
    @Override
    public void setReveal(double aValue)
    {
        // Do normal version
        super.setReveal(aValue);

        if (_cameraView == null) return;
        rebuildChart();

        // Animate camera rotation
        Camera camera = _cameraView.getCamera();
        camera.setYaw(90 + (DEFAULT_YAW - 90) * aValue);
        camera.setPitch(0 + (DEFAULT_PITCH - 0) * aValue);

        // Forward to BarDataArea (2D)
        _barDataArea.setReveal(aValue);
    }
}