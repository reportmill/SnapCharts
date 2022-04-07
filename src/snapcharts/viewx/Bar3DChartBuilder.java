/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx3d.ParentShape;
import snap.gfx3d.PathBox3D;
import snap.gfx3d.Scene3D;
import snap.util.MathUtils;

/**
 * This class builds the Bar3D scene.
 */
public class Bar3DChartBuilder extends AxisBoxBuilder {

    // The Bar3DDataArea
    private Bar3DDataArea  _dataArea;

    /**
     * Constructor.
     */
    public Bar3DChartBuilder(Bar3DDataArea aDataArea, Scene3D aScene)
    {
        super(aDataArea, aScene);
        _dataArea = aDataArea;
    }

    /**
     * Returns the section count.
     */
    public int getSectionCount()  { return _dataArea.getSections().length; }

    /**
     * Override to add bar chart.
     */
    @Override
    protected ParentShape createAxisBoxShape()
    {
        // Do normal version
        ParentShape axisBoxShape = super.createAxisBoxShape();

        // If Reveal is zero, just return
        double reveal = _dataArea.getReveal();
        if (MathUtils.equalsZero(reveal))
            return axisBoxShape;

        // Iterate over sections
        int traceCount = _dataArea._traceCount;
        int pointCount = _dataArea._pointCount;
        BarDataArea.Section[] sections = _dataArea.getSections();
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
    private void addBar(ParentShape axisBoxShape, double aX, double aY, double aW, double aH, Color aColor)
    {
        // If reveal is set, modify Y/H
        double reveal = _dataArea.getReveal();
        if (reveal < 1)
            aH *= reveal;

        // Get depth, and Z values for back/front
        double sceneDepth = getPrefDepth();
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
}
