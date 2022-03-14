/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Color;
import snap.gfx3d.*;
import snap.util.MathUtils;
import snapcharts.model.*;
import snapcharts.view.AxisView;
import snapcharts.view.DataArea;

/**
 * A Scene to draw an axis box.
 */
public abstract class AxisBoxSceneBuilder {

    // The DataArea
    private DataArea  _dataArea;

    // The Scene
    private Scene3D  _scene;

    /**
     * Constructor.
     */
    public AxisBoxSceneBuilder(DataArea aDataArea, Scene3D aScene)
    {
        _dataArea = aDataArea;
        _scene = aScene;
    }

    /**
     * Returns the preferred width of the scene.
     */
    public double getPrefWidth()
    {
        // Get Scene (3D chart info) and DataView size
        Chart chart = _dataArea.getChart();
        Scene scene = chart.getScene();
        double viewW = _dataArea.getWidth();
        double viewH = _dataArea.getHeight();

        // Get Aspect for Y Axis and PrefHeight
        double aspectY = scene.getAspect(AxisType.Y, viewW, viewH);
        double prefH = aspectY * viewH;

        // Calculate PrefWidth using AspectY and PrefHeight
        double aspectX = scene.getAspect(AxisType.X, viewW, viewH);
        double prefW = prefH / aspectY * aspectX;
        return prefW;
    }

    /**
     * Returns the preferred height of the scene.
     */
    public double getPrefHeight()
    {
        // Get Scene (3D chart info) and DataView size
        Chart chart = _dataArea.getChart();
        Scene scene = chart.getScene();
        double viewW = _dataArea.getWidth();
        double viewH = _dataArea.getHeight();

        // Get Aspect for Y Axis and PrefHeight
        double aspectY = scene.getAspect(AxisType.Y, viewW, viewH);
        double prefH = aspectY * viewH;
        return prefH;
    }

    /**
     * Returns the preferred depth of the scene.
     */
    public double getPrefDepth()
    {
        // Get Scene (3D chart info) and DataView size
        Chart chart = _dataArea.getChart();
        Scene scene = chart.getScene();
        double viewW = _dataArea.getWidth();
        double viewH = _dataArea.getHeight();

        // Get Aspect for Y Axis and PrefHeight
        double aspectY = scene.getAspect(AxisType.Y, viewW, viewH);
        double prefH = aspectY * viewH;

        // Calculate PrefDepth using AspectY and PrefHeight
        double aspectZ = scene.getAspect(AxisType.Z, viewW, viewH);
        double prefD = prefH / aspectY * aspectZ;
        return prefD;
    }

    /**
     * Returns the axes pair for each axis box side.
     */
    protected AxisType[] getAxesForSide(Side3D aSide)
    {
        // Handle weird Bar3D and Line3D
        if (_dataArea.getChartType() == ChartType.BAR_3D || _dataArea.getChartType() == ChartType.LINE_3D)
            return getAxesForSideForForwardZ(aSide);

        // Handle standard XYZ (Z Up) AxisBox
        switch (aSide) {

            // Front/Back is XZ plane
            case FRONT: case BACK: return new AxisType[] { AxisType.X, AxisType.Z };

            // Left/Right is YZ plane
            case LEFT: case RIGHT: return new AxisType[] { AxisType.Y, AxisType.Z };

            // Top/Bottom is XY plane
            case TOP: case BOTTOM: return new AxisType[] { AxisType.X, AxisType.Y };

            // Other is error
            default: throw new RuntimeException("AxisBoxSceneBuilder.getAxesForSide: Invalid side: " + aSide);
        }
    }

    /**
     * Returns the axes pair for each axis box side for Bar3D and Line3D.
     */
    protected AxisType[] getAxesForSideForForwardZ(Side3D aSide)
    {
        switch (aSide) {

            // Handle Front/Back: XY plane
            case FRONT: case BACK: return new AxisType[] { AxisType.X, AxisType.Y };

            // Handle Left/Right: YZ plane
            case LEFT: case RIGHT: return new AxisType[] { AxisType.Z, AxisType.Y };

            // Handle Top/Bottom: XZ plane
            case TOP: case BOTTOM: return new AxisType[] { AxisType.X, AxisType.Z };

            // Other is error
            default: throw new RuntimeException("AxisBoxSceneBuilder.getAxesForSide: Invalid side: " + aSide);
        }
    }

    /**
     * Returns the intervals for X axis.
     */
    public Intervals getIntervalsX()
    {
        AxisView axisView = _dataArea.getAxisViewX();
        return axisView.getIntervals();
    }

    /**
     * Returns the intervals for Y axis.
     */
    public Intervals getIntervalsY()
    {
        AxisView axisView = _dataArea.getAxisViewY();
        return axisView.getIntervals();
    }

    /**
     * Returns the intervals for Z axis.
     */
    public Intervals getIntervalsZ()
    {
        // Handle Line3D: Return intervals from 0 to TraceCount
        ChartType chartType = _dataArea.getChartType();
        if (chartType == ChartType.LINE_3D) {
            TraceList traceList = _dataArea.getTraceList();
            int traceCount = traceList.getTraceCount();
            return Intervals.getIntervalsSimple(0, traceCount);
        }

        return null;
    }

    /**
     * Returns the intervals for given axis type.
     */
    protected Intervals getIntervalsForAxisType(AxisType anAxisType)
    {
        switch (anAxisType) {
            case X: return getIntervalsX();
            case Y: return getIntervalsY();
            case Z: return getIntervalsZ();
            default: throw new RuntimeException("AxisBoxSceneBuilder.getIntervalsForAxis: Invalid axis: " + anAxisType);
        }
    }

    /**
     * Rebuilds the chart.
     */
    protected void rebuildScene()
    {
        // Remove all existing children
        _scene.removeChildren();

        // Get AxisBoxShape and add to scene
        Shape3D axisBoxShape = getAxisBoxShape();
        _scene.addChild(axisBoxShape);
    }

    /**
     * Rebuilds the chart.
     */
    public Shape3D getAxisBoxShape()
    {
        Shape3D axisBoxShape = createAxisBoxShape();
        double width = getPrefWidth();
        double height = getPrefHeight();
        double depth = getPrefDepth();
        Bounds3D bounds3D = new Bounds3D(0, 0, 0, width, height, depth);
        axisBoxShape.setBounds3D(bounds3D);
        return axisBoxShape;
    }

    /**
     * Rebuilds the chart.
     */
    protected ParentShape3D createAxisBoxShape()
    {
        // Get preferred width, height, depth
        double width = getPrefWidth();
        double height = getPrefHeight();
        double depth = getPrefDepth();

        // Add shape for front/back sides
        Shape3D frontShape = addSideFrontBack(width, height, 0);
        Shape3D backShape = addSideFrontBack(width, height, depth);

        // Add shape for left/right sides
        Shape3D leftShape = addSideLeftRight(0, height, depth);
        Shape3D rightShape = addSideLeftRight(width, height, depth);

        // Add shape for top/bottom sides
        Shape3D bottomShape = addSideTopBottom(width, 0, depth);

        // Reset shapes
        ParentShape3D axisBoxShape = new ParentShape3D();
        axisBoxShape.setChildren(frontShape, backShape, leftShape, rightShape, bottomShape);
        return axisBoxShape;
    }

    /**
     * Adds geometry for front/back sides.
     */
    private Shape3D addSideFrontBack(double width, double height, double sideZ)
    {
        // Create wall shape
        Path3D side = new Path3D();
        side.setName(sideZ == 0 ? "AxisBack" : "AxisFront");
        side.setOpacity(.8f);
        side.setColor(Color.WHITE); //if (_backFill!=null) back.setColor(_backFill.getColor());

        // Add side points
        side.moveTo(0, 0, sideZ);
        side.lineTo(width, 0, sideZ);
        side.lineTo(width, height, sideZ);
        side.lineTo(0, height, sideZ);
        side.close();

        // If facing wrong direction, reverse
        double normalZ = sideZ == 0 ? 1 : -1;
        if (!MathUtils.equals(side.getNormal().z, normalZ))
            side.reverse();

        // Create Painter3D for side border/grid, paint border, add to side
        Painter3D gridPainter = new Painter3D(width, height);
        gridPainter.addLayerOffset(1);
        gridPainter.drawRect(0, 0, width, height);
        side.setPainter(gridPainter);

        // Get 2D grid
        AxisType[] gridAxisTypes = getAxesForSide(Side3D.FRONT);
        Intervals intervalsAcross = getIntervalsForAxisType(gridAxisTypes[0]);
        Intervals intervalsDown = getIntervalsForAxisType(gridAxisTypes[1]);
        paintGridX(gridPainter, intervalsAcross, width, height);
        paintGridY(gridPainter, intervalsDown, width, height);

        // Return
        return side;
    }

    /**
     * Add geometry for left/right sides.
     */
    private Shape3D addSideLeftRight(double sideX, double height, double depth)
    {
        // Create side shape
        Path3D side = new Path3D();
        side.setName(sideX == 0 ? "AxisLeft" : "AxisRight");
        side.setColor(Color.WHITE);
        side.setOpacity(.8f);

        // Add side points
        side.moveTo(sideX, 0, 0);
        side.lineTo(sideX, height, 0);
        side.lineTo(sideX, height, depth);
        side.lineTo(sideX, 0, depth);
        side.close();

        // If facing wrong direction, reverse
        double normalX = sideX == 0 ? 1 : -1;
        if (!MathUtils.equals(side.getNormal().x, normalX))
            side.reverse();

        // Create Painter3D for side border/grid, paint border, add to side
        Painter3D gridPainter = new Painter3D(depth, height);
        gridPainter.addLayerOffset(.5);
        gridPainter.drawRect(0, 0, depth, height);
        side.setPainter(gridPainter);

        // Paint grid
        AxisType[] gridAxisTypes = getAxesForSide(Side3D.LEFT);
        Intervals intervalsAcross = getIntervalsForAxisType(gridAxisTypes[0]);
        Intervals intervalsDown = getIntervalsForAxisType(gridAxisTypes[1]);
        paintGridX(gridPainter, intervalsAcross, depth, height);
        paintGridY(gridPainter, intervalsDown, depth, height);

        // Return
        return side;
    }

    /**
     * Adds geometry for top/bottom sides.
     */
    private Shape3D addSideTopBottom(double width, double sideY, double depth)
    {
        // Create side shape
        Path3D side = new Path3D();
        side.setName(sideY == 0 ? "AxisBottom" : "AxisTop");
        side.setColor(Color.LIGHTGRAY);
        side.setOpacity(.8f);
        side.setDoubleSided(true);

        // Add side points
        side.moveTo(0, sideY, 0);
        side.lineTo(0, sideY, depth);
        side.lineTo(width, sideY, depth);
        side.lineTo(width, sideY, 0);
        side.close();

        // Create Painter3D for side border/grid, paint border, add to side
        Painter3D gridPainter = new Painter3D(width, depth);
        gridPainter.addLayerOffset(.5);
        gridPainter.drawRect(0, 0, width, depth);
        side.setPainter(gridPainter);

        // Paint grid
        AxisType[] gridAxisTypes = getAxesForSide(Side3D.TOP);
        Intervals intervalsAcross = getIntervalsForAxisType(gridAxisTypes[0]);
        Intervals intervalsDown = getIntervalsForAxisType(gridAxisTypes[1]);
        paintGridX(gridPainter, intervalsAcross, width, depth);
        paintGridY(gridPainter, intervalsDown, width, depth);

        // Return
        return side;
    }

    /**
     * Paints the grid path for X axis.
     */
    private void paintGridX(Painter3D aPntr, Intervals theIntervals, double aWidth, double aHeight)
    {
        // If no intervals, just return
        if (theIntervals == null) return;

        // Get X end points
        int intervalCount = theIntervals.getCount() - 1;
        double y1 = 0;
        double y2 = aHeight;

        // Iterate over intervals and paint grid line path
        for (int i = 1; i < intervalCount; i++) {
            double intervalRatio = i / (double) intervalCount;
            double lineX = aWidth * intervalRatio;
            aPntr.moveTo(lineX, y1);
            aPntr.lineTo(lineX, y2);
        }
    }

    /**
     * Paints the grid path for Y axis.
     */
    private void paintGridY(Painter3D aPntr, Intervals theIntervals, double aWidth, double aHeight)
    {
        // If no intervals, just return
        if (theIntervals == null) return;

        // Get X end points
        int intervalCount = theIntervals.getCount() - 1;
        double x1 = 0;
        double x2 = aWidth;

        // Iterate over intervals and paint grid line path
        for (int i = 1; i < intervalCount; i++) {
            double intervalRatio = i / (double) intervalCount;
            double lineY = aHeight * intervalRatio;
            aPntr.moveTo(x1, lineY);
            aPntr.lineTo(x2, lineY);
        }
    }
}
