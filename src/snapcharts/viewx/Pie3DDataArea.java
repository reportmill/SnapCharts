/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Arc;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx3d.PathBox3D;
import snap.gfx3d.Side3D;
import snapcharts.model.Trace;
import snapcharts.view.ChartHelper;

/**
 * A DataArea subclass to display the contents of 3D a pie chart.
 */
public class Pie3DDataArea extends DataArea3D {

    // The PieDataArea (2D) to build the 2D bar shapes
    private PieDataArea  _pieDataArea;

    /**
     * Constructor.
     */
    public Pie3DDataArea(ChartHelper aChartHelper, Trace aTrace)
    {
        super(aChartHelper, aTrace, true);

        // Create/set PieDataArea (2D)
        _pieDataArea = new PieDataArea(aChartHelper, aTrace);
        _pieDataArea._holeRatio = .3;
    }

    @Override
    protected void rebuildChartNow()
    {
        // Do normal version
        super.rebuildChartNow();
        if (getWidth() < 10 || getHeight() < 10)
            return;

        // Make axisBoxShape sides not visible
        AxisBoxShape axisBoxShape = getAxisBoxShape();
        for (Side3D side : Side3D.values())
            axisBoxShape.getSideShape(side).setVisible(false);
    }

    /**
     * Override to add bar chart.
     */
    @Override
    protected AxisBoxShape createAxisBoxShape()
    {
        // Do normal version
        AxisBoxShape axisBoxShape = super.createAxisBoxShape();

        // Get PieDataArea and reset
        PieDataArea pieDataArea = _pieDataArea;
        double size = getAxisBoxPrefHeight();
        _pieDataArea.setSize(size, size);

        // Iterate over wedges and add them as 3D
        PieDataArea.Wedge[] wedges = pieDataArea.getWedges();
        for(int i=0; i<wedges.length; i++) {
            PieDataArea.Wedge wedge = wedges[i];
            Color color = getColorMapColor(i);
            addWedgeToScene(axisBoxShape, wedge, color);
        }

        // Return shape
        return axisBoxShape;
    }

    /**
     * Adds a 3d wedge to scene.
     */
    protected void addWedgeToScene(AxisBoxShape axisBoxShape, PieDataArea.Wedge aWedge, Color aColor)
    {
        // Get depth, and Z values for back/front
        double depth = getAxisBoxPrefDepth();
        double z0 = 0; //-depth/2;
        double z1 = depth; //depth/2;

        // Get wedge arc
        //double reveal = getReveal();
        Arc arc = aWedge.getArc(true, false, false, 1, .5);

        // Create/configure bar path/path3d and add to scene
        PathBox3D bar = new PathBox3D(arc, z0, z1);
        bar.setColor(aColor);
        bar.setStroke(Color.BLACK, 1);

        // Add to scene
        axisBoxShape.addChild(bar);
    }

    @Override
    public double getAxisBoxPrefWidth()
    {
        double prefW = Math.min(getWidth(), getHeight());
        return prefW;
    }

    @Override
    public double getAxisBoxPrefHeight()
    {
        return getAxisBoxPrefWidth();
    }

    @Override
    public double getAxisBoxPrefDepth()
    {
        return Math.round(getAxisBoxPrefHeight() * .12); //return DEFAULT_DEPTH = 50;
    }

    @Override
    protected void paintDataArea(Painter aPntr)  { }
}
