/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Path;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx3d.*;
import snapcharts.model.Intervals;

/**
 * A Scene to draw an axis box.
 */
public abstract class AxisBoxSceneBuilder {

    // The Scene
    protected Scene3D  _scene;

    // Shapes for grid
    protected Path  _grid = new Path();

    // Shapes for the minor grid
    protected Path  _gridMinor = new Path();

    // The grid path without separators
    protected Path  _gridWithoutSep = new Path();

    /**
     * Constructor.
     */
    public AxisBoxSceneBuilder(Scene3D aScene)
    {
        _scene = aScene;
    }

    /**
     * Returns the preferred width of the scene.
     */
    public double getPrefWidth()
    {
        Camera3D camera3D = _scene.getCamera();
        return camera3D.getViewWidth();
    }

    /**
     * Returns the preferred height of the scene.
     */
    public double getPrefHeight()
    {
        Camera3D camera3D = _scene.getCamera();
        return camera3D.getViewHeight();
    }

    /**
     * Returns the preferred depth of the scene.
     */
    public abstract double getPrefDepth();

    /**
     * Returns the intervals.
     */
    public abstract Intervals getIntervalsY();

    /**
     * Returns the minor tick count.
     */
    public abstract int getMinorTickCount();

    /**
     * Returns the section count.
     */
    public abstract int getSectionCount(); // getSections().length

    /**
     * Rebuilds the chart.
     */
    protected void rebuildScene()
    {
        // Remove all existing children
        _scene.removeShapes();

        // Rebuild gridlines
        rebuildGridLines();

        // Get preferred width, height, depth
        double width = getPrefWidth();
        double height = getPrefHeight();
        double depth = getPrefDepth();

        // Add back planes
        addBackPlane(width, height, 0);
        addBackPlane(width, height, depth);

        // Add side planes
        addSidePlane(0, height, depth, true);
        addSidePlane(width, height, depth, true);

        // Create floor path shape
        addFloorPlane(width, 0, depth);
    }

    /**
     * Adds back plane for given backZ.
     */
    private void addBackPlane(double width, double height, double backZ)
    {
        // Create back plane shape
        Path3D back = new Path3D();
        back.setOpacity(.8f);
        back.setColor(Color.WHITE); //if (_backFill!=null) back.setColor(_backFill.getColor());
        back.setStroke(Color.BLACK, 1); //if (_backStroke!=null) back.setStroke(_backStroke.getColor(),_backStroke.getWidth());
        back.moveTo(0, 0, backZ);
        back.lineTo(0, height, backZ);
        back.lineTo(width, height, backZ);
        back.lineTo(width, 0, backZ);
        back.close();

        // Add Grid to back
        Path3D grid = new Path3D(_grid, backZ);
        grid.setName("AxisBackGrid");
        grid.setStroke(Color.BLACK, 1);
        back.addLayer(grid);

        // Add GridMinor to back
        Path3D gridMinor = new Path3D(_gridMinor, backZ);
        gridMinor.setName("AxisBackGridMinor");
        gridMinor.setStrokeColor(Color.LIGHTGRAY);
        back.addLayer(gridMinor);

        // If facing wrong direction, reverse
        Vector3D normal = new Vector3D(0, 0, backZ == 0 ? 1 : -1);
        if (!back.getNormal().equals(normal))
            back.reverse();

        // Set name
        back.setName(backZ == 0 ? "AxisBack" : "AxisFront");

        // Add to scene
        _scene.addShape(back);
    }

    /**
     * Adds side plane for given sideX.
     */
    private void addSidePlane(double sideX, double height, double depth, boolean vertical)
    {
        // Calculate whether side plane should be shifted to the right. Side normal = { 1, 0, 0 }.
        //boolean shiftSide = vertical && !_camera.isPseudo3D() && _camera.isFacingAway(_scene.localToCameraForVector(1,0,0));

        // Create side path shape
        Path3D side = new Path3D();
        side.setColor(Color.LIGHTGRAY);
        side.setStroke(Color.BLACK, 1);
        side.setOpacity(.8f);
        side.moveTo(sideX, 0, 0);
        side.lineTo(sideX, height, 0);
        side.lineTo(sideX, height, depth);
        side.lineTo(sideX, 0, depth);
        side.close();

        // Add grid
        if (vertical)
            addGridToSide(side, sideX, height, depth, vertical);

        // If facing wrong direction, reverse
        Vector3D normal = new Vector3D(sideX == 0 ? 1 : -1, 0, 0);
        if (!side.getNormal().equals(normal))
            side.reverse();

        // Set name
        side.setName(sideX == 0 ? "AxisLeftSide" : "AxisRightSide");

        // Add to scene and return
        _scene.addShape(side);
    }

    /**
     * Adds grid to given side/floor.
     */
    private void addGridToSide(Path3D side, double sideX, double height, double depth, boolean vertical)
    {
        // Determine whether side grid should be added to graph side or floor
        Rect gridWithoutSepBnds = _gridWithoutSep.getBounds();
        Rect gridMinorBnds = _gridMinor.getBounds();
        Rect gridRect = vertical ?
                new Rect(0, gridWithoutSepBnds.y, depth, gridWithoutSepBnds.height) :
                new Rect(gridWithoutSepBnds.x, 0, gridWithoutSepBnds.width, depth);
        Rect gridMinorRect = vertical ?
                new Rect(0, gridMinorBnds.y, depth, gridMinorBnds.height) :
                new Rect(gridMinorBnds.x, 0, gridMinorBnds.width, depth);
        Transform3D gridTrans = vertical ?
                new Transform3D().rotateY(-90).translate(sideX, 0, 0) :
                new Transform3D().rotateX(90).translate(0, height, 0);

        // Configure grid
        Path sideGridPath = _gridWithoutSep.copyFor(gridRect);
        Path3D sideGrid = new Path3D(sideGridPath, 0);
        sideGrid.setName("AxisSideGrid");
        sideGrid.transform(gridTrans);
        sideGrid.setStroke(Color.BLACK, 1);
        side.addLayer(sideGrid);

        // Add GridMinor to side3d
        Path sideGridPathMinor = _gridMinor.copyFor(gridMinorRect);
        sideGrid.setName("AxisSideGridMinor");
        Path3D sideGridMinor = new Path3D(sideGridPathMinor, 0);
        sideGridMinor.transform(gridTrans);
        sideGridMinor.setStroke(Color.LIGHTGRAY,1);
        side.addLayer(sideGridMinor);
        side.setColor(Color.WHITE); //if (_backFill!=null) sideGridBuddy.setColor(_backFill.getColor());
        side.setStroke(Color.BLACK, 1); //if (_backStroke!=null) sideGridBuddy.setStroke(_backStroke.getColor(), _backStroke.getWidth());
    }

    /**
     * Adds floor plane.
     */
    private void addFloorPlane(double width, double height, double depth)
    {
        // Create floor path shape
        Path3D floor = new Path3D(); floor.setName("AxisFloor");
        floor.setColor(Color.LIGHTGRAY);
        floor.setStroke(Color.BLACK, 1);
        floor.setOpacity(.8f);
        floor.moveTo(0, height + .5, 0);
        floor.lineTo(width, height + .5, 0);
        floor.lineTo(width, height + .5, depth);
        floor.lineTo(0, height + .5, depth);
        floor.close();

        // Add to scene and return
        _scene.addShape(floor);

        // Add another floor facing opposite direction
        Path3D floor2 = floor.clone();
        floor2.reverse();
        _scene.addShape(floor2);
    }

    /**
     * Rebuild gridlines.
     */
    protected void rebuildGridLines()
    {
        _grid.clear();
        _gridMinor.clear();
        _gridWithoutSep.clear();

        // Get graph min interval and max interval
        Intervals intervals = getIntervalsY();
        int intervalCount = intervals.getCount();
        double minInterval = intervals.getMin();
        double maxInterval = intervals.getMax();
        double totalInterval = maxInterval - minInterval;
        boolean vertical = true;

        // Get graph bounds
        Camera3D camera = _scene.getCamera();
        double boundsX = 0;
        double boundsY = 0;
        double boundsW = camera.getViewWidth();
        double boundsH = camera.getViewHeight();

        // Get grid line width/height
        double lineW = vertical ? boundsW : 0;
        double lineH = vertical ? 0 : boundsH;

        // Get grid max
        double gridMax = vertical ? boundsH : boundsW;
        double intervalSize = intervals.getInterval(1) - minInterval;
        int minorTickCount = getMinorTickCount();
        double minorTickInterval = gridMax*intervalSize/totalInterval/(minorTickCount+1);

        // Iterate over graph intervals
        for (int i=0, iMax=intervalCount; i<iMax - 1; i++) {

            // Get interval ratio and line x & y
            double intervalRatio = i/(iMax - 1f);
            double lineX = vertical ? boundsX : boundsX + boundsW * intervalRatio;
            double lineY = vertical ? boundsY + boundsH * intervalRatio : boundsY;

            // DrawMajorAxis
            if (i>0) {
                addGridLineMajor(lineX, lineY, lineX + lineW, lineY + lineH); //line.setFrame(lineX, lineY, lineW, lineH);
            }

            // If not drawing minor axis, just continue
            //if (!_graph.getValueAxis().getShowMinorGrid()) continue;

            // Draw minor axis
            for (int j=0; j<minorTickCount; j++) {
                double minorLineX = vertical ? boundsX : lineX + (j+1)*minorTickInterval;
                double minorLineY = vertical ? lineY + (j+1)*minorTickInterval : boundsY;
                addGridLineMinor(minorLineX, minorLineY, minorLineX + lineW, minorLineY + lineH);
            }
        }

        // Get whether zero axis line was added
        /*boolean zeroAxisLineAdded = false;
        if (_graph.getValueAxis().getShowMajorGrid())
            for (int i=0, iMax=getIntervalCount(); i<iMax; i++)
                zeroAxisLineAdded = zeroAxisLineAdded || MathUtils.equalsZero(getInterval(i));*/

        // If zero axis line not added, add it (happens when there are pos & neg values)
        /*if (!zeroAxisLineAdded) {
            RMLineShape line = new RMLineShape();
            double intervalRatio = minInterval/totalInterval;
            double lineX = isVertical() ? 0 : -bounds.width*intervalRatio;
            double lineY = isVertical() ? bounds.height + bounds.height*intervalRatio : 0;
            line.setFrame(lineX, lineY, lineW, lineH);
            _barShape.addGridLineMajor(line);
        }*/

        // If drawGroupSeparator, add separator line for each section, perpendicular to grid
        boolean getLabelAxis_getShowGridLines = true;
        if (getLabelAxis_getShowGridLines) {

            // Get SeriesCount, SeriesItemCount and SectionCount
            //int seriesCount = getSeriesCount(), seriesItemCount = seriesCount>0 ? getSeries(0).getPointCount() : 0;
            //int sectionCount = _meshed ? seriesItemCount : seriesCount;
            int sectionCount = getSectionCount();

            // Iterate over series
            for (int i=1, iMax=sectionCount; i<iMax; i++) {
                double lineX = vertical ? boundsX + boundsW * i / iMax : boundsX;
                double lineY = vertical ? boundsY : boundsY + boundsH * i / iMax;
                double lineX2 = lineX + (vertical ? 0 : boundsW);
                double lineY2 = lineY + (vertical ? boundsH : 0);
                addGridLineSeparator(lineX, lineY, lineX2, lineY2);
            }
        }
    }

    /** Adds a major grid line to the graph view. */
    protected void addGridLineMajor(double X0, double Y0, double X1, double Y1)
    {
        _grid.moveTo(X0, Y0);
        _grid.lineTo(X1, Y1);
        _gridWithoutSep.moveTo(X0, Y0);
        _gridWithoutSep.lineTo(X1, Y1);
    }

    /** Adds a minor grid line to the graph view. */
    protected void addGridLineMinor(double X0, double Y0, double X1, double Y1)
    {
        _gridMinor.moveTo(X0, Y0);
        _gridMinor.lineTo(X1, Y1);
    }

    /** Adds a grid line separator to the graph view. */
    protected void addGridLineSeparator(double X0, double Y0, double X1, double Y1)
    {
        _grid.moveTo(X0, Y0);
        _grid.lineTo(X1, Y1);
    }
}
