/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Path2D;
import snap.gfx.Color;
import snap.gfx3d.*;
import snapcharts.model.AxisType;
import snapcharts.model.Chart;
import snapcharts.model.Intervals;
import snapcharts.model.Scene;
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

        // Add geometry for XY plane
        addPlaneXY(width, height, 0);
        addPlaneXY(width, height, depth);

        // Add geometry for YZ plane
        addPlaneYZ(0, height, depth);
        addPlaneYZ(width, height, depth);

        // Add geometry for ZX plane
        addPlaneZX(width, 0, depth);
    }

    /**
     * Adds geometry for XY plane.
     */
    private void addPlaneXY(double width, double height, double backZ)
    {
        // Create wall shape
        Path3D wall = new Path3D();
        wall.setName(backZ == 0 ? "AxisBack" : "AxisFront");
        wall.setOpacity(.8f);
        wall.setColor(Color.WHITE); //if (_backFill!=null) back.setColor(_backFill.getColor());
        wall.setStroke(Color.BLACK, 1); //if (_backStroke!=null) back.setStroke(_backStroke.getColor(),_backStroke.getWidth());
        wall.moveTo(0, 0, backZ);
        wall.lineTo(0, height, backZ);
        wall.lineTo(width, height, backZ);
        wall.lineTo(width, 0, backZ);
        wall.close();

        // If facing wrong direction, reverse
        Vector3D normal = new Vector3D(0, 0, backZ == 0 ? 1 : -1);
        if (!wall.getNormal().equals(normal))
            wall.reverse();

        // Get 2D grid
        Path2D grid = getGridX(width, height);
        Path2D gridY = getGridY(width, height);
        grid.addShape(gridY);

        // Create Grid shape and add to wall
        Path3D grid3D = new Path3D(grid, backZ);
        grid3D.setName("AxisBackGrid");
        grid3D.setStroke(Color.BLACK, 1);
        wall.addLayer(grid3D);

        // Add to scene
        _scene.addShape(wall);
    }

    /**
     * Add geometry for YZ plane.
     */
    private void addPlaneYZ(double sideX, double height, double depth)
    {
        // Create wall shape
        Path3D wall = new Path3D();
        wall.setName(sideX == 0 ? "AxisLeftSide" : "AxisRightSide");
        wall.setColor(Color.WHITE);
        wall.setStroke(Color.BLACK, 1);
        wall.setOpacity(.8f);
        wall.moveTo(sideX, 0, 0);
        wall.lineTo(sideX, height, 0);
        wall.lineTo(sideX, height, depth);
        wall.lineTo(sideX, 0, depth);
        wall.close();

        // If facing wrong direction, reverse
        Vector3D normal = new Vector3D(sideX == 0 ? 1 : -1, 0, 0);
        if (!wall.getNormal().equals(normal))
            wall.reverse();

        // Get 2D grid
        Path2D grid = getGridY(depth, height);
        //Path2D gridZ = getGridY(width, height); grid.addShape(gridZ);

        // Add grid to wall
        Path3D grid3D = new Path3D(grid, 0);
        grid3D.setName("AxisSideGrid");
        grid3D.setStroke(Color.BLACK, 1);
        wall.addLayer(grid3D);

        // Transform grid to wall coords
        Transform3D gridTrans = new Transform3D().rotateY(-90).translate(sideX, 0, 0);
        grid3D.transform(gridTrans);

        // Add to scene and return
        _scene.addShape(wall);
    }

    /**
     * Adds floor plane.
     */
    private void addPlaneZX(double width, double height, double depth)
    {
        // Create floor path shape
        Path3D floor = new Path3D();
        floor.setName("AxisFloor");
        floor.setColor(Color.LIGHTGRAY);
        floor.setStroke(Color.BLACK, 1);
        floor.setOpacity(.8f);
        floor.moveTo(0, height, 0);
        floor.lineTo(width, height, 0);
        floor.lineTo(width, height, depth);
        floor.lineTo(0, height, depth);
        floor.close();

        // Add to scene and return
        _scene.addShape(floor);

        // Add another floor facing opposite direction
        Path3D floor2 = floor.clone();
        floor2.reverse();
        _scene.addShape(floor2);
    }

    /**
     * Returns the grid path for X axis.
     */
    private Path2D getGridX(double aWidth, double aHeight)
    {
        // Get intervals for X axis
        Intervals intervals = getIntervalsX();
        int intervalCount = intervals.getCount();

        // Create path and get X end points
        Path2D path = new Path2D();
        double y1 = 0;
        double y2 = aHeight;

        // Iterate over intervals and add grid line
        for (int i = 1, iMax = intervalCount; i < iMax - 1; i++) {
            double intervalRatio = i / (iMax - 1f);
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
    private Path2D getGridY(double aWidth, double aHeight)
    {
        // Get intervals for Y axis
        Intervals intervals = getIntervalsY();
        int intervalCount = intervals.getCount();

        // Create path and get X end points
        Path2D path = new Path2D();
        double x1 = 0;
        double x2 = aWidth;

        // Iterate over intervals and add grid line
        for (int i = 1, iMax = intervalCount; i < iMax - 1; i++) {
            double intervalRatio = i / (iMax - 1f);
            double lineY = aHeight * intervalRatio;
            path.moveTo(x1, lineY);
            path.lineTo(x2, lineY);
        }

        // Return path
        return path;
    }
}
