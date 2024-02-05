/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Arc;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx3d.PathBox3D;
import snap.gfx3d.Side3D;
import snapcharts.charts.Trace;
import snapcharts.view.ChartHelper;

/**
 * A TraceView3D subclass to display the contents of 3D a pie chart.
 */
public class Pie3DTraceView extends TraceView3D {

    // The PieTraceView (2D) to build the 2D bar shapes
    private PieTraceView  _pieTraceView;

    /**
     * Constructor.
     */
    public Pie3DTraceView(ChartHelper aChartHelper, Trace aTrace)
    {
        super(aChartHelper, aTrace, true);

        // Create/set PieTraceView (2D)
        _pieTraceView = new PieTraceView(aChartHelper, aTrace);
        _pieTraceView._holeRatio = .3;
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

        // Get PieTraceView and reset
        PieTraceView pieTraceView = _pieTraceView;
        double size = getAxisBoxPrefHeight();
        _pieTraceView.setSize(size, size);

        // Iterate over wedges and add them as 3D
        PieTraceView.Wedge[] wedges = pieTraceView.getWedges();
        for(int i=0; i<wedges.length; i++) {
            PieTraceView.Wedge wedge = wedges[i];
            Color color = getColorMapColor(i);
            addWedgeToScene(axisBoxShape, wedge, color);
        }

        // Return shape
        return axisBoxShape;
    }

    /**
     * Adds a 3d wedge to scene.
     */
    protected void addWedgeToScene(AxisBoxShape axisBoxShape, PieTraceView.Wedge aWedge, Color aColor)
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
    protected void paintTrace(Painter aPntr)  { }
}
