/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Path2D;
import snap.gfx.Color;
import snap.gfx3d.*;
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
    protected Scene3D  _scene;

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
        _scene.removeShapes();

        // Get preferred width, height, depth
        double width = getPrefWidth();
        double height = getPrefHeight();
        double depth = getPrefDepth();

        // Add shape for front/back sides
        addSideFrontBack(width, height, 0);
        addSideFrontBack(width, height, depth);

        // Add shape for left/right sides
        addSideLeftRight(0, height, depth);
        addSideLeftRight(width, height, depth);

        // Add shape for top/bottom sides
        addSideTopBottom(width, 0, depth);
    }

    /**
     * Adds geometry for front/back sides.
     */
    private void addSideFrontBack(double width, double height, double sideZ)
    {
        // Create wall shape
        Path3D side = new Path3D();
        side.setName(sideZ == 0 ? "AxisBack" : "AxisFront");
        side.setOpacity(.8f);
        side.setColor(Color.WHITE); //if (_backFill!=null) back.setColor(_backFill.getColor());
        side.setStroke(Color.BLACK, 1); //if (_backStroke!=null) back.setStroke(_backStroke.getColor(),_backStroke.getWidth());
        side.moveTo(0, 0, sideZ);
        side.lineTo(0, height, sideZ);
        side.lineTo(width, height, sideZ);
        side.lineTo(width, 0, sideZ);
        side.close();

        // If facing wrong direction, reverse
        Vector3D normal = new Vector3D(0, 0, sideZ == 0 ? 1 : -1);
        if (!side.getNormal().equals(normal))
            side.reverse();

        // Get 2D grid
        AxisType[] gridAxisTypes = getAxesForSide(Side3D.FRONT);
        Intervals intervalsAcross = getIntervalsForAxisType(gridAxisTypes[0]);
        Intervals intervalsDown = getIntervalsForAxisType(gridAxisTypes[1]);
        Path2D gridX = getGridX(null, intervalsAcross, width, height);
        Path2D gridXY = getGridY(gridX, intervalsDown, width, height);

        // Create Grid shape and add to wall
        Path3D grid3D = new Path3D(gridXY, sideZ);
        grid3D.setName(side.getName() + "Grid");
        grid3D.setStroke(Color.BLACK, 1);
        side.addLayer(grid3D);

        // Add to scene
        _scene.addShape(side);
    }

    /**
     * Add geometry for left/right sides.
     */
    private void addSideLeftRight(double sideX, double height, double depth)
    {
        // Create side shape
        Path3D side = new Path3D();
        side.setName(sideX == 0 ? "AxisLeft" : "AxisRight");
        side.setColor(Color.WHITE);
        side.setStroke(Color.BLACK, 1);
        side.setOpacity(.8f);
        side.moveTo(sideX, 0, 0);
        side.lineTo(sideX, height, 0);
        side.lineTo(sideX, height, depth);
        side.lineTo(sideX, 0, depth);
        side.close();

        // If facing wrong direction, reverse
        Vector3D normal = new Vector3D(sideX == 0 ? 1 : -1, 0, 0);
        if (!side.getNormal().equals(normal))
            side.reverse();

        // Get 2D grid
        AxisType[] gridAxisTypes = getAxesForSide(Side3D.LEFT);
        Intervals intervalsAcross = getIntervalsForAxisType(gridAxisTypes[0]);
        Intervals intervalsDown = getIntervalsForAxisType(gridAxisTypes[1]);
        Path2D gridAcross = getGridX(null, intervalsAcross, depth, height);
        Path2D gridAcrossDown = getGridY(gridAcross, intervalsDown, depth, height);

        // Add grid to wall
        Path3D grid3D = new Path3D(gridAcrossDown, 0);
        grid3D.setName(side.getName() + "Grid");
        grid3D.setStroke(Color.BLACK, 1);
        side.addLayer(grid3D);

        // Transform grid to wall coords
        Transform3D gridTrans = new Transform3D().rotateY(-90).translate(sideX, 0, 0);
        grid3D.transform(gridTrans);

        // Add to scene and return
        _scene.addShape(side);
    }

    /**
     * Adds geometry for top/bottom sides.
     */
    private void addSideTopBottom(double width, double sideY, double depth)
    {
        // Create side shape
        Path3D side = new Path3D();
        side.setName(sideY == 0 ? "AxisBottom" : "AxisTop");
        side.setColor(Color.LIGHTGRAY);
        side.setStroke(Color.BLACK, 1);
        side.setOpacity(.8f);
        side.moveTo(0, sideY, 0);
        side.lineTo(width, sideY, 0);
        side.lineTo(width, sideY, depth);
        side.lineTo(0, sideY, depth);
        side.close();

        // Get 2D grid
        AxisType[] gridAxisTypes = getAxesForSide(Side3D.TOP);
        Intervals intervalsAcross = getIntervalsForAxisType(gridAxisTypes[0]);
        Intervals intervalsDown = getIntervalsForAxisType(gridAxisTypes[1]);
        Path2D gridX = getGridX(null, intervalsAcross, width, depth);
        Path2D gridXY = getGridY(gridX, intervalsDown, width, depth);

        // Add grid to side
        if (gridXY != null) {

            // Create/configure shape for grid and add to side
            Path3D grid3D = new Path3D(gridXY, 0);
            grid3D.setName(side.getName() + "Grid");
            grid3D.setStroke(Color.BLACK, 1);
            side.addLayer(grid3D);

            // Transform grid to wall coords
            Transform3D gridTrans = new Transform3D().rotateX(90).translate(0, sideY, 0);
            grid3D.transform(gridTrans);
        }

        // Add to scene and return
        _scene.addShape(side);

        // Add another side shape facing opposite direction
        Path3D backSide = side.clone();
        backSide.reverse();
        _scene.addShape(backSide);
    }

    /**
     * Returns the grid path for X axis.
     */
    private Path2D getGridX(Path2D aPath, Intervals theIntervals, double aWidth, double aHeight)
    {
        // If no intervals, just return path
        if (theIntervals == null) return aPath;

        // Create path and get X end points
        Path2D path = aPath != null ? aPath : new Path2D();
        int intervalCount = theIntervals.getCount() - 1;
        double y1 = 0;
        double y2 = aHeight;

        // Iterate over intervals and add grid line
        for (int i = 1; i < intervalCount; i++) {
            double intervalRatio = i / (double) intervalCount;
            double lineX = aWidth * intervalRatio;
            path.moveTo(lineX, y1);
            path.lineTo(lineX, y2);
        }

        // Return path
        return path;
    }

    /**
     * Returns the grid path for Y axis.
     */
    private Path2D getGridY(Path2D aPath, Intervals theIntervals, double aWidth, double aHeight)
    {
        // If no intervals, just return path
        if (theIntervals == null) return aPath;

        // Create path and get X end points
        Path2D path = aPath != null ? aPath : new Path2D();
        int intervalCount = theIntervals.getCount() - 1;
        double x1 = 0;
        double x2 = aWidth;

        // Iterate over intervals and add grid line
        for (int i = 1; i < intervalCount; i++) {
            double intervalRatio = i / (double) intervalCount;
            double lineY = aHeight * intervalRatio;
            path.moveTo(x1, lineY);
            path.lineTo(x2, lineY);
        }

        // Return path
        return path;
    }
}
