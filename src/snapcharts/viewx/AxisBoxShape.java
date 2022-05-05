/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.gfx3d.*;
import snap.util.MathUtils;
import snap.view.ViewUtils;
import snapcharts.model.AxisType;
import snapcharts.model.Intervals;
import snapcharts.model.Scene;

/**
 * This ParentShape subclass displays an axis box.
 */
public class AxisBoxShape extends ParentShape {

    // The DataArea3D
    private DataArea3D  _dataArea;

    // The Front/Back sides
    private Poly3D  _frontSide, _backSide;

    // The Left/Right sides
    private Poly3D  _leftSide, _rightSide;

    // The Top/Bottom sides
    private Poly3D  _topSide, _bottomSide;

    // Constants
    private static final Color SIDE_COLOR = Color.WHITE;
    private static final Color SIDE_BORDER_COLOR = Color.BLACK;
    private static final Color BOTTOM_COLOR = Color.LIGHTGRAY.brighter().brighter();
    private static final Color MINOR_GRID_COLOR = Color.LIGHTGRAY;
    private static final Color GRID_COLOR = Color.DARKGRAY;

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
    public Poly3D getSideShape(Side3D aSide)
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
     * Returns whether given side is visible.
     */
    public boolean isSideVisible(Side3D aSide)
    {
        switch (aSide) {
            case FRONT: return _frontSide.isVisible();
            case BACK: return _backSide.isVisible();
            case LEFT: return _leftSide.isVisible();
            case RIGHT: return _rightSide.isVisible();
            case BOTTOM: return _bottomSide.isVisible();
            case TOP: return !_bottomSide.isVisible();
            default: throw new RuntimeException("AxisBoxShape.isSideVisible: Unknown side: " + aSide);
        }
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
        double width = _dataArea.getAxisBoxPrefWidth();
        double height = _dataArea.getAxisBoxPrefHeight();
        double depth = _dataArea.getAxisBoxPrefDepth();

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
        if (_dataArea.isProjection());
        else if (_topSide != null)
            setChildren(_frontSide, _backSide, _leftSide, _rightSide, _topSide, _bottomSide);
        else setChildren(_frontSide, _backSide, _leftSide, _rightSide, _bottomSide);

        // Go ahead and set Bounds to avoid calculation
        Bounds3D bounds3D = new Bounds3D(0, 0, 0, width, height, depth);
        setBounds3D(bounds3D);
    }

    /**
     * Adds geometry for front/back sides.
     */
    private Poly3D addSideFrontBack(double width, double height, double sideZ)
    {
        // Create wall shape
        Poly3D side = new Poly3D();
        side.setName(sideZ == 0 ? "AxisBack" : "AxisFront");
        side.setOpacity(.8f);
        side.setColor(SIDE_COLOR); //if (_backFill!=null) back.setColor(_backFill.getColor());

        // Add side points
        side.addPoint(0, 0, sideZ);
        side.addPoint(width, 0, sideZ);
        side.addPoint(width, height, sideZ);
        side.addPoint(0, height, sideZ);

        // If facing wrong direction, reverse
        double normalZ = sideZ == 0 ? 1 : -1;
        if (!MathUtils.equals(side.getNormal().z, normalZ))
            side.reverse();

        // Return
        return side;
    }

    /**
     * Add geometry for left/right sides.
     */
    private Poly3D addSideLeftRight(double sideX, double height, double depth)
    {
        // Create side shape
        Poly3D side = new Poly3D();
        side.setName(sideX == 0 ? "AxisLeft" : "AxisRight");
        side.setColor(SIDE_COLOR);
        side.setOpacity(.8f);

        // Add side points
        if (sideX == 0) {
            side.addPoint(sideX, 0, depth);
            side.addPoint(sideX, 0, 0);
            side.addPoint(sideX, height, 0);
            side.addPoint(sideX, height, depth);
        }
        else {
            side.addPoint(sideX, 0, 0);
            side.addPoint(sideX, 0, depth);
            side.addPoint(sideX, height, depth);
            side.addPoint(sideX, height, 0);
        }

        // If facing wrong direction, reverse
        //double normalX = sideX == 0 ? 1 : -1;
        //if (!MathUtils.equals(side.getNormal().x, normalX)) side.reverse();

        // Return
        return side;
    }

    /**
     * Adds geometry for top/bottom sides.
     */
    private Poly3D addSideTopBottom(double width, double sideY, double depth)
    {
        // Create side shape
        Poly3D side = new Poly3D();
        side.setName(sideY == 0 ? "AxisBottom" : "AxisTop");
        side.setColor(sideY == 0 ? BOTTOM_COLOR : SIDE_COLOR);
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

        // Return
        return side;
    }

    /**
     * Adds grids to side shapes by creating/setting a Painter3D for each.
     */
    public void addSideGrids()
    {
        // Get preferred width, height, depth
        double width = _dataArea.getAxisBoxPrefWidth();
        double height = _dataArea.getAxisBoxPrefHeight();
        double depth = _dataArea.getAxisBoxPrefDepth();

        addSideGridFrontBack(width, height);
        addSideGridLeftRight(height, depth);
        addSideGridTopBottom(width, depth);
    }

    /**
     * Adds geometry for front/back sides.
     */
    private void addSideGridFrontBack(double width, double height)
    {
        // Create Painter3D for side border/grid, paint border, add to side
        Painter3D gridPainter = new Painter3D(width, height);
        gridPainter.addLayerOffset(.5);
        if (SIDE_BORDER_COLOR != null) {
            gridPainter.setColor(SIDE_BORDER_COLOR);
            gridPainter.drawRect(0, 0, width, height);
        }

        // Get 2D grid
        AxisType[] gridAxisTypes = _dataArea.getAxesForSide(Side3D.FRONT);
        Intervals intervalsAcross = _dataArea.getIntervalsForAxis(gridAxisTypes[0]);
        Intervals intervalsDown = _dataArea.getIntervalsForAxis(gridAxisTypes[1]);
        Intervals intervalsAcrossPref = _dataArea.getPrefIntervalsForAxis(gridAxisTypes[0]);
        Intervals intervalsDownPref = _dataArea.getPrefIntervalsForAxis(gridAxisTypes[1]);

        // Maybe paint minor intervals
        boolean doMinorAcross = intervalsAcrossPref.getCount() > intervalsAcross.getCount();
        boolean doMinorDown = intervalsDownPref.getCount() > intervalsDown.getCount();
        if (doMinorAcross || doMinorDown) {
            gridPainter.setColor(MINOR_GRID_COLOR);
            if (doMinorAcross)
                paintGridX(gridPainter, intervalsAcross, width, height, true);
            if (doMinorDown)
                paintGridY(gridPainter, intervalsDown, width, height, true);
            gridPainter.addLayerOffset(.25);
        }

        // Paint intervals
        gridPainter.setColor(GRID_COLOR);
        paintGridX(gridPainter, intervalsAcross, width, height, false);
        paintGridY(gridPainter, intervalsDown, width, height, false);

        // Add to front/back
        _frontSide.setPainter(gridPainter);
        _backSide.setPainter(gridPainter.clone());
    }

    /**
     * Add geometry for left/right sides.
     */
    private void addSideGridLeftRight(double height, double depth)
    {
        // Create Painter3D for side border/grid, paint border, add to side
        Painter3D gridPainter = new Painter3D(depth, height);
        gridPainter.addLayerOffset(.5);
        if (SIDE_BORDER_COLOR != null) {
            gridPainter.setColor(SIDE_BORDER_COLOR);
            gridPainter.drawRect(0, 0, depth, height);
        }

        // Paint grid
        AxisType[] gridAxisTypes = _dataArea.getAxesForSide(Side3D.LEFT);
        Intervals intervalsAcross = _dataArea.getIntervalsForAxis(gridAxisTypes[0]);
        Intervals intervalsDown = _dataArea.getIntervalsForAxis(gridAxisTypes[1]);
        Intervals intervalsAcrossPref = _dataArea.getPrefIntervalsForAxis(gridAxisTypes[0]);
        Intervals intervalsDownPref = _dataArea.getPrefIntervalsForAxis(gridAxisTypes[1]);

        // Maybe paint minor intervals
        boolean doMinorAcross = intervalsAcrossPref.getCount() > intervalsAcross.getCount();
        boolean doMinorDown = intervalsDownPref.getCount() > intervalsDown.getCount();
        if (doMinorAcross || doMinorDown) {
            gridPainter.setColor(MINOR_GRID_COLOR);
            if (doMinorAcross)
                paintGridX(gridPainter, intervalsAcross, depth, height, true);
            if (doMinorDown)
                paintGridY(gridPainter, intervalsDown, depth, height, true);
            gridPainter.addLayerOffset(.25);
        }

        // Paint intervals
        gridPainter.setColor(GRID_COLOR);
        paintGridX(gridPainter, intervalsAcross, depth, height, false);
        paintGridY(gridPainter, intervalsDown, depth, height, false);

        // Add to left/right
        _leftSide.setPainter(gridPainter);
        _rightSide.setPainter(gridPainter.clone());
    }

    /**
     * Adds geometry for top/bottom sides.
     */
    private void addSideGridTopBottom(double width, double depth)
    {
        // Create Painter3D for side border/grid, paint border, add to side
        Painter3D gridPainter = new Painter3D(width, depth);
        gridPainter.addLayerOffset(.5);
        if (SIDE_BORDER_COLOR != null) {
            gridPainter.setColor(SIDE_BORDER_COLOR);
            gridPainter.drawRect(0, 0, width, depth);
        }

        // Paint grid
        AxisType[] gridAxisTypes = _dataArea.getAxesForSide(Side3D.TOP);
        Intervals intervalsAcross = _dataArea.getIntervalsForAxis(gridAxisTypes[0]);
        Intervals intervalsDown = _dataArea.getIntervalsForAxis(gridAxisTypes[1]);
        Intervals intervalsAcrossPref = _dataArea.getPrefIntervalsForAxis(gridAxisTypes[0]);
        Intervals intervalsDownPref = _dataArea.getPrefIntervalsForAxis(gridAxisTypes[1]);

        // Maybe paint minor intervals
        boolean doMinorAcross = intervalsAcrossPref.getCount() > intervalsAcross.getCount();
        boolean doMinorDown = intervalsDownPref.getCount() > intervalsDown.getCount();
        if (doMinorAcross || doMinorDown) {
            gridPainter.setColor(MINOR_GRID_COLOR);
            if (doMinorAcross)
                paintGridX(gridPainter, intervalsAcross, width, depth, true);
            if (doMinorDown)
                paintGridY(gridPainter, intervalsDown, width, depth, true);
            gridPainter.addLayerOffset(.25);
        }

        // Paint intervals
        gridPainter.setColor(GRID_COLOR);
        paintGridX(gridPainter, intervalsAcross, width, depth, false);
        paintGridY(gridPainter, intervalsDown, width, depth, false);

        // Add to top/bottom
        _bottomSide.setPainter(gridPainter);
        if (_topSide != null)
            _topSide.setPainter(gridPainter.clone());
    }

    /**
     * Paints the grid path for X axis.
     */
    private void paintGridX(Painter3D aPntr, Intervals theIntervals, double aWidth, double aHeight, boolean doMinor)
    {
        // If no intervals, just return
        if (theIntervals == null) return;

        // Get interval min/max and Y end points
        double ivalMin = theIntervals.getMin();
        double ivalMax = theIntervals.getMax();
        double y1 = 0;
        double y2 = aHeight;
        double offset = doMinor ? theIntervals.getDelta() / 2 : 0;

        // Iterate over intervals and paint grid line path
        for (int i = 0, iMax = theIntervals.getCount(); i < iMax; i++) {

            // If not full interval, just skip
            if (!theIntervals.isFullInterval(i)) continue;
            if (doMinor && i + 1 == iMax) continue;

            // Get interval, map to Width and draw line
            double ival = theIntervals.getInterval(i) + offset;
            double lineX = MathUtils.mapValueForRanges(ival, ivalMin, ivalMax, 0, aWidth);
            aPntr.moveTo(lineX, y1);
            aPntr.lineTo(lineX, y2);
        }
    }

    /**
     * Paints the grid path for Y axis.
     */
    private void paintGridY(Painter3D aPntr, Intervals theIntervals, double aWidth, double aHeight, boolean doMinor)
    {
        // If no intervals, just return
        if (theIntervals == null) return;

        // Get interval min/max and X end points
        double ivalMin = theIntervals.getMin();
        double ivalMax = theIntervals.getMax();
        double x1 = 0;
        double x2 = aWidth;
        double offset = doMinor ? theIntervals.getDelta() / 2 : 0;

        // Iterate over intervals and paint grid line path
        for (int i = 0, iMax = theIntervals.getCount(); i < iMax; i++) {

            // If not full interval, just skip
            if (!theIntervals.isFullInterval(i)) continue;
            if (doMinor && i + 1 == iMax) continue;

            // Get interval, map to Height and draw line
            double ival = theIntervals.getInterval(i) + offset;
            double lineY = MathUtils.mapValueForRanges(ival, ivalMin, ivalMax, 0, aHeight);
            aPntr.moveTo(x1, lineY);
            aPntr.lineTo(x2, lineY);
        }
    }

    /**
     * Add side projections.
     */
    public void addSideProjections()
    {
        // Get ProjectionDataArea
        ChartHelper3D chartHelper3D = (ChartHelper3D) _dataArea.getChartHelper();
        DataArea3D projDataArea = chartHelper3D.getProjectionDataArea();

        // Set optimal size
        double width = getBounds3D().getWidth();
        projDataArea.setSize(width, width);
        CameraView cameraView = projDataArea.getCameraView();
        cameraView.setSize(width, width);

        // Rebuild chart
        projDataArea.resetAxisProxyBounds();
        projDataArea.rebuildChartNow();

        // Create and set texture for projected sides
        Scene chartScene = _dataArea.getChartScene();
        Side3D[] projectedSides = chartScene.getProjectedSides();
        for (Side3D side : projectedSides) {
            Texture texture = createTextureForSide(cameraView, side);
            setTextureForSide(texture, side);
        }

        // Repaint DataArea
        _dataArea.repaint();
    }

    /**
     * Generates the texture for a given side.
     */
    private Texture createTextureForSide(CameraView cameraView, Side3D aSide)
    {
        // Get camera and configure for side
        Camera camera = cameraView.getCamera();
        camera.setYawPitchRollForSide(aSide);
        camera.setOrtho(true);

        // Create image/texture and return
        Image image = ViewUtils.getImageForScale(cameraView, 1);
        Texture texture = new Texture(image);
        return texture;
    }

    /**
     * Sets the texture for a given side.
     */
    private void setTextureForSide(Texture aTexture, Side3D aSide)
    {
        Poly3D sideShape = getSideShape(aSide);
        sideShape.setTexture(aTexture);
        sideShape.addTexCoord(0, 0);
        sideShape.addTexCoord(1, 0);
        sideShape.addTexCoord(1, 1);
        sideShape.addTexCoord(0, 1);
    }
}
