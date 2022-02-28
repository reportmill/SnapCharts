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
     * Returns the intervals for Z axis.
     */
    public Intervals getIntervalsZ()  { return null; }

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
        wall.setName(backZ == 0 ? "AxisSideXY" : "AxisFront");
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
        Intervals intervalsX = getIntervalsX();
        Intervals intervalsY = getIntervalsY();
        Path2D gridX = getGridX(null, intervalsX, width, height);
        Path2D gridXY = getGridY(gridX, intervalsY, width, height);

        // Create Grid shape and add to wall
        Path3D grid3D = new Path3D(gridXY, backZ);
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
        wall.setName(sideX == 0 ? "AxisSideYZLeft" : "AxisSideYZRight");
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
        Intervals intervalsY = getIntervalsY();
        Intervals intervalsZ = getIntervalsZ();
        Path2D gridY = getGridY(null, intervalsY, depth, height);
        Path2D gridYZ = getGridX(gridY, intervalsZ, depth, height);

        // Add grid to wall
        Path3D grid3D = new Path3D(gridYZ, 0);
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
        // Create wall shape
        Path3D wall = new Path3D();
        wall.setName("AxisSideZX");
        wall.setColor(Color.LIGHTGRAY);
        wall.setStroke(Color.BLACK, 1);
        wall.setOpacity(.8f);
        wall.moveTo(0, height, 0);
        wall.lineTo(width, height, 0);
        wall.lineTo(width, height, depth);
        wall.lineTo(0, height, depth);
        wall.close();

        // Get 2D grid
        Intervals intervalsZ = getIntervalsZ();
        Intervals intervalsX = getIntervalsX();
        Path2D gridZ = getGridY(null, intervalsZ, width, depth);
        Path2D gridZX = getGridX(gridZ, intervalsX, width, depth);

        // Add grid to wall
        if (gridZX != null) {
            Path3D grid3D = new Path3D(gridZX, 0);
            grid3D.setName("AxisSideZXGrid");
            grid3D.setStroke(Color.BLACK, 1);
            wall.addLayer(grid3D);
        }

        // Add to scene and return
        _scene.addShape(wall);

        // Add another floor facing opposite direction
        Path3D floor2 = wall.clone();
        floor2.reverse();
        _scene.addShape(floor2);
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
