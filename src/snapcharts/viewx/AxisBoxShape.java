/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Color;
import snap.gfx3d.*;
import snap.util.MathUtils;
import snapcharts.model.AxisType;
import snapcharts.model.Chart;
import snapcharts.model.Intervals;
import snapcharts.model.Scene;

/**
 * This ParentShape subclass displays an axis box.
 */
public class AxisBoxShape extends ParentShape {

    // The DataArea3D
    private DataArea3D  _dataArea;

    // The Front/Back sides
    private FacetShape  _frontSide, _backSide;

    // The Left/Right sides
    private FacetShape  _leftSide, _rightSide;

    // The Top/Bottom sides
    private FacetShape  _topSide, _bottomSide;

    /**
     * Constructor.
     */
    public AxisBoxShape(DataArea3D aDataArea)
    {
        super();
        _dataArea = aDataArea;

        addSides();
    }

    /**
     * Returns the side shape.
     */
    public FacetShape getSideShape(Side3D aSide)
    {
        switch (aSide) {
            case FRONT: return _frontSide;
            case BACK: return _backSide;
            case LEFT: return _leftSide;
            case RIGHT: return _rightSide;
            case TOP: return _topSide;
            case BOTTOM: return _bottomSide;
            default: return null;
        }
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
     * Updates the visibility of AxisBoxShape sides for camera.
     */
    public void setSidesVisibleForCamera()
    {
        boolean frontFacing = _dataArea.isSideFacingCamera(Side3D.FRONT);
        boolean leftFacing = _dataArea.isSideFacingCamera(Side3D.LEFT);
        boolean topFacing = !_dataArea.isSideFacingCamera(Side3D.BOTTOM);
        setSidesVisible(frontFacing, leftFacing, topFacing);
    }

    /**
     * Updates the visibility of AxisBoxShape sides for camera.
     */
    public void setSidesVisible(boolean frontFacing, boolean leftFacing, boolean topFacing)
    {
        _frontSide.setVisible(frontFacing);
        _backSide.setVisible(!frontFacing);
        _leftSide.setVisible(leftFacing);
        _rightSide.setVisible(!leftFacing);
        if (_topSide != null) {
            _topSide.setVisible(topFacing);
            _bottomSide.setVisible(!topFacing);
        }
    }

    /**
     * Build sides.
     */
    private void addSides()
    {
        // Get preferred width, height, depth
        double width = getPrefWidth();
        double height = getPrefHeight();
        double depth = getPrefDepth();

        // Add shape for front/back sides
        _frontSide = addSideFrontBack(width, height, depth);
        _backSide = addSideFrontBack(width, height, 0);

        // Add shape for left/right sides
        _leftSide = addSideLeftRight(0, height, depth);
        _rightSide = addSideLeftRight(width, height, depth);

        // Add shape for top/bottom sides
        _bottomSide = addSideTopBottom(width, 0, depth);
        if (!_dataArea.isForwardXY())
            _topSide = addSideTopBottom(width, height, depth);
        else _bottomSide.setDoubleSided(true);

        // Reset shapes
        if (_topSide != null)
            setChildren(_frontSide, _backSide, _leftSide, _rightSide, _topSide, _bottomSide);
        else setChildren(_frontSide, _backSide, _leftSide, _rightSide, _bottomSide);

        // Go ahead and set Bounds to avoid calculation
        Bounds3D bounds3D = new Bounds3D(0, 0, 0, width, height, depth);
        setBounds3D(bounds3D);
    }

    /**
     * Adds geometry for front/back sides.
     */
    private FacetShape addSideFrontBack(double width, double height, double sideZ)
    {
        // Create wall shape
        Poly3D side = new Poly3D();
        side.setName(sideZ == 0 ? "AxisBack" : "AxisFront");
        side.setOpacity(.8f);
        side.setColor(Color.WHITE); //if (_backFill!=null) back.setColor(_backFill.getColor());

        // Add side points
        side.addPoint(0, 0, sideZ);
        side.addPoint(width, 0, sideZ);
        side.addPoint(width, height, sideZ);
        side.addPoint(0, height, sideZ);

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
        AxisType[] gridAxisTypes = _dataArea.getAxesForSide(Side3D.FRONT);
        Intervals intervalsAcross = _dataArea.getIntervalsForAxisType(gridAxisTypes[0]);
        Intervals intervalsDown = _dataArea.getIntervalsForAxisType(gridAxisTypes[1]);
        paintGridX(gridPainter, intervalsAcross, width, height);
        paintGridY(gridPainter, intervalsDown, width, height);

        // Return
        return side;
    }

    /**
     * Add geometry for left/right sides.
     */
    private FacetShape addSideLeftRight(double sideX, double height, double depth)
    {
        // Create side shape
        Poly3D side = new Poly3D();
        side.setName(sideX == 0 ? "AxisLeft" : "AxisRight");
        side.setColor(Color.WHITE);
        side.setOpacity(.8f);

        // Add side points
        side.addPoint(sideX, 0, 0);
        side.addPoint(sideX, height, 0);
        side.addPoint(sideX, height, depth);
        side.addPoint(sideX, 0, depth);

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
        AxisType[] gridAxisTypes = _dataArea.getAxesForSide(Side3D.LEFT);
        Intervals intervalsAcross = _dataArea.getIntervalsForAxisType(gridAxisTypes[0]);
        Intervals intervalsDown = _dataArea.getIntervalsForAxisType(gridAxisTypes[1]);
        paintGridX(gridPainter, intervalsAcross, depth, height);
        paintGridY(gridPainter, intervalsDown, depth, height);

        // Return
        return side;
    }

    /**
     * Adds geometry for top/bottom sides.
     */
    private FacetShape addSideTopBottom(double width, double sideY, double depth)
    {
        // Create side shape
        Poly3D side = new Poly3D();
        side.setName(sideY == 0 ? "AxisBottom" : "AxisTop");
        side.setColor(sideY == 0 ? Color.LIGHTGRAY : Color.WHITE);
        side.setOpacity(.8f);

        // Add side points
        side.addPoint(0, sideY, 0);
        side.addPoint(0, sideY, depth);
        side.addPoint(width, sideY, depth);
        side.addPoint(width, sideY, 0);

        // If facing wrong direction, reverse
        double normalY = sideY == 0 ? 1 : -1;
        if (!MathUtils.equals(side.getNormal().y, normalY))
            side.reverse();

        // Create Painter3D for side border/grid, paint border, add to side
        Painter3D gridPainter = new Painter3D(width, depth);
        gridPainter.addLayerOffset(.5);
        gridPainter.drawRect(0, 0, width, depth);
        side.setPainter(gridPainter);

        // Paint grid
        AxisType[] gridAxisTypes = _dataArea.getAxesForSide(Side3D.TOP);
        Intervals intervalsAcross = _dataArea.getIntervalsForAxisType(gridAxisTypes[0]);
        Intervals intervalsDown = _dataArea.getIntervalsForAxisType(gridAxisTypes[1]);
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

        // Get interval min/max and Y end points
        double ivalMin = theIntervals.getMin();
        double ivalMax = theIntervals.getMax();
        double y1 = 0;
        double y2 = aHeight;

        // Iterate over intervals and paint grid line path
        for (int i = 0, iMax = theIntervals.getCount(); i < iMax; i++) {

            // If not full interval, just skip
            if (!theIntervals.isFullInterval(i)) continue;

            // Get interval, map to Width and draw line
            double ival = theIntervals.getInterval(i);
            double lineX = MathUtils.mapValueForRanges(ival, ivalMin, ivalMax, 0, aWidth);
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

        // Get interval min/max and X end points
        double ivalMin = theIntervals.getMin();
        double ivalMax = theIntervals.getMax();
        double x1 = 0;
        double x2 = aWidth;

        // Iterate over intervals and paint grid line path
        for (int i = 0, iMax = theIntervals.getCount(); i < iMax; i++) {

            // If not full interval, just skip
            if (!theIntervals.isFullInterval(i)) continue;

            // Get interval, map to Height and draw line
            double ival = theIntervals.getInterval(i);
            double lineY = MathUtils.mapValueForRanges(ival, ivalMin, ivalMax, 0, aHeight);
            aPntr.moveTo(x1, lineY);
            aPntr.lineTo(x2, lineY);
        }
    }
}
