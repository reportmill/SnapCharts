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

    // The TraceView3D
    private TraceView3D  _traceView;

    // The Front/Back sides
    private Poly3D  _frontSide, _backSide;

    // The Left/Right sides
    private Poly3D  _leftSide, _rightSide;

    // The Top/Bottom sides
    private Poly3D  _topSide, _bottomSide;

    // The highliner
    private AxisBoxHighliner  _highliner;

    // Constants
    private static final Color SIDE_COLOR = Color.WHITE;
    private static final Color SIDE_BORDER_COLOR = Color.BLACK;
    private static final Color BOTTOM_COLOR = Color.LIGHTGRAY.blend(Color.WHITE, .8);
    private static final Color MINOR_GRID_COLOR = Color.LIGHTGRAY;
    private static final Color MINOR_GRID_COLOR_LIGHTER = Color.LIGHTGRAY.blend(Color.WHITE, .5);
    private static final Color GRID_COLOR = Color.DARKGRAY;

    /**
     * Constructor.
     */
    public AxisBoxShape(TraceView3D aTraceView)
    {
        super();
        _traceView = aTraceView;

        addSides();
    }

    /**
     * Returns whether to show target data point highlight lines.
     */
    public boolean isShowHighliner()  { return _highliner != null; }

    /**
     * Sets whether to show target data point highlight lines.
     */
    public void setShowHighliner(boolean aValue)
    {
        // if already set, just return
        if (aValue == isShowHighliner()) return;

        // If activating, create/add Highliner
        if (aValue) {
            _highliner = new AxisBoxHighliner(this, _traceView);
            addChild(_highliner);
        }
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
        // Get visible sides and update
        boolean frontFacing = _traceView.isSideFacingCamera(Side3D.FRONT);
        boolean leftFacing = _traceView.isSideFacingCamera(Side3D.LEFT);
        boolean topFacing = !_traceView.isSideFacingCamera(Side3D.BOTTOM);
        setSidesVisible(frontFacing, leftFacing, topFacing);

        // If Hightliner set, update lines
        if (_highliner != null)
            _highliner.updateLines();
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
        double width = _traceView.getAxisBoxPrefWidth();
        double height = _traceView.getAxisBoxPrefHeight();
        double depth = _traceView.getAxisBoxPrefDepth();

        // Add shape for front/back sides
        _frontSide = addSideFrontBack(width, height, depth);
        _backSide = addSideFrontBack(width, height, 0);

        // Add shape for left/right sides
        _leftSide = addSideLeftRight(0, height, depth);
        _rightSide = addSideLeftRight(width, height, depth);

        // Add shape for top/bottom sides
        _bottomSide = addSideTopBottom(width, 0, depth);
        if (!_traceView.isForwardXY())
            _topSide = addSideTopBottom(width, height, depth);
        else _bottomSide.setDoubleSided(true);

        // Reset shapes
        if (_traceView.isProjection());
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

        // Add back side points
        if (sideZ == 0) {
            side.addPoint(0, 0, sideZ);
            side.addPoint(width, 0, sideZ);
            side.addPoint(width, height, sideZ);
            side.addPoint(0, height, sideZ);
        }

        // Add front side points
        else {
            side.addPoint(width, 0, sideZ);
            side.addPoint(0, 0, sideZ);
            side.addPoint(0, height, sideZ);
            side.addPoint(width, height, sideZ);
        }

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

        // Add left side points
        if (sideX == 0) {
            side.addPoint(sideX, 0, depth);
            side.addPoint(sideX, 0, 0);
            side.addPoint(sideX, height, 0);
            side.addPoint(sideX, height, depth);
        }

        // Add right side points
        else {
            side.addPoint(sideX, 0, 0);
            side.addPoint(sideX, 0, depth);
            side.addPoint(sideX, height, depth);
            side.addPoint(sideX, height, 0);
        }

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

        // Add bottom
        if (sideY == 0) {
            side.addPoint(0, sideY, depth);
            side.addPoint(width, sideY, depth);
            side.addPoint(width, sideY, 0);
            side.addPoint(0, sideY, 0);
        }

        // Add top
        else {
            side.addPoint(0, sideY, 0);
            side.addPoint(width, sideY, 0);
            side.addPoint(width, sideY, depth);
            side.addPoint(0, sideY, depth);
        }

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
        double width = _traceView.getAxisBoxPrefWidth();
        double height = _traceView.getAxisBoxPrefHeight();
        double depth = _traceView.getAxisBoxPrefDepth();

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
        AxisType[] gridAxisTypes = _traceView.getAxesForSide(Side3D.FRONT);
        Intervals intervalsAcross = _traceView.getIntervalsForAxis(gridAxisTypes[0]);
        Intervals intervalsDown = _traceView.getIntervalsForAxis(gridAxisTypes[1]);
        Intervals intervalsAcrossPref = _traceView.getPrefIntervalsForAxis(gridAxisTypes[0]);
        Intervals intervalsDownPref = _traceView.getPrefIntervalsForAxis(gridAxisTypes[1]);

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
        gridPainter.flipX();
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
        AxisType[] gridAxisTypes = _traceView.getAxesForSide(Side3D.LEFT);
        Intervals intervalsAcross = _traceView.getIntervalsForAxis(gridAxisTypes[0]);
        Intervals intervalsDown = _traceView.getIntervalsForAxis(gridAxisTypes[1]);
        Intervals intervalsAcrossPref = _traceView.getPrefIntervalsForAxis(gridAxisTypes[0]);
        Intervals intervalsDownPref = _traceView.getPrefIntervalsForAxis(gridAxisTypes[1]);

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
        gridPainter.flipX();
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
        AxisType[] gridAxisTypes = _traceView.getAxesForSide(Side3D.TOP);
        Intervals intervalsAcross = _traceView.getIntervalsForAxis(gridAxisTypes[0]);
        Intervals intervalsDown = _traceView.getIntervalsForAxis(gridAxisTypes[1]);
        Intervals intervalsAcrossPref = _traceView.getPrefIntervalsForAxis(gridAxisTypes[0]);
        Intervals intervalsDownPref = _traceView.getPrefIntervalsForAxis(gridAxisTypes[1]);

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
        gridPainter.flipY();
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

        // Handle minor grid special
        if (doMinor) {

            // Get minor interval delta: Usually half Intervals.Delta, but go with 1/5 if Intervals are factor of 5
            double delta = theIntervals.getDelta();
            double deltaBase = delta; while (deltaBase < 5) deltaBase *= 10; while (deltaBase > 5) deltaBase /= 10;
            boolean factorOf5 = MathUtils.equals(deltaBase, 5);
            double incr = factorOf5 ? delta / 5 : delta / 2;
            if (factorOf5)
                aPntr.setColor(MINOR_GRID_COLOR_LIGHTER);

            // Calculate start point (back down from interval 1)
            double ival0 = theIntervals.getInterval(0);
            double ival1 = theIntervals.getInterval(1);
            double dataX = ival1 - incr * 4; while (dataX < ival0) dataX += incr;

            // Iterate to end of intervals by delta and paint minor lines
            double lastIval = theIntervals.getInterval(theIntervals.getCount() - 1);
            while (dataX < lastIval) {
                double lineX = MathUtils.mapValueForRanges(dataX, ivalMin, ivalMax, 0, aWidth);
                aPntr.moveTo(lineX, y1);
                aPntr.lineTo(lineX, y2);
                dataX += incr;
            }
            aPntr.setColor(MINOR_GRID_COLOR);
            return;
        }

        // Iterate over intervals and paint grid line path
        for (int i = 0, iMax = theIntervals.getCount(); i < iMax; i++) {

            // If not full interval, just skip
            if (!theIntervals.isFullInterval(i)) continue;
            if (doMinor && i + 1 == iMax) continue;

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
    private void paintGridY(Painter3D aPntr, Intervals theIntervals, double aWidth, double aHeight, boolean doMinor)
    {
        // If no intervals, just return
        if (theIntervals == null) return;

        // Get interval min/max and X end points
        double ivalMin = theIntervals.getMin();
        double ivalMax = theIntervals.getMax();
        double x1 = 0;
        double x2 = aWidth;

        if (doMinor) {

            // Get minor interval delta: Usually half Intervals.Delta, but go with 1/5 if Intervals are factor of 5
            double delta = theIntervals.getDelta();
            double deltaBase = delta; while (deltaBase < 5) deltaBase *= 10; while (deltaBase > 5) deltaBase /= 10;
            boolean factorOf5 = MathUtils.equals(deltaBase, 5);
            double incr = factorOf5 ? delta / 5 : delta / 2;
            if (factorOf5)
                aPntr.setColor(MINOR_GRID_COLOR_LIGHTER);

            // Calculate start point (back down from interval 1)
            double ival0 = theIntervals.getInterval(0);
            double ival1 = theIntervals.getInterval(1);
            double dataY = ival1 - incr * 4; while (dataY < ival0) dataY += incr;

            // Iterate to end of intervals by delta and paint minor lines
            double lastIval = theIntervals.getInterval(theIntervals.getCount() - 1);
            while (dataY < lastIval) {
                double lineY = MathUtils.mapValueForRanges(dataY, ivalMin, ivalMax, 0, aHeight);
                aPntr.moveTo(x1, lineY);
                aPntr.lineTo(x2, lineY);
                dataY += incr;
            }
            return;
        }

        // Iterate over intervals and paint grid line path
        for (int i = 0, iMax = theIntervals.getCount(); i < iMax; i++) {

            // If not full interval, just skip
            if (!theIntervals.isFullInterval(i)) continue;
            if (doMinor && i + 1 == iMax) continue;

            // Get interval, map to Height and draw line
            double ival = theIntervals.getInterval(i);
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
        // Get ProjectionTraceView
        ChartHelper3D chartHelper3D = (ChartHelper3D) _traceView.getChartHelper();
        TraceView3D projTraceView = chartHelper3D.getProjectionTraceView();

        // Set optimal size
        double width = getBounds3D().getWidth();
        projTraceView.setSize(width, width);
        CameraView cameraView = projTraceView.getCameraView();
        cameraView.setSize(width, width);

        // Rebuild chart
        projTraceView.resetAxisProxyBounds();
        projTraceView.rebuildChartNow();

        // Create and set texture for projected sides
        Scene chartScene = _traceView.getChartScene();
        Side3D[] projectedSides = chartScene.getProjectedSides();
        for (Side3D side : projectedSides) {
            Texture texture = createTextureForSide(cameraView, side);
            setTextureForSide(texture, side);
        }

        // Repaint TraceView
        _traceView.repaint();
    }

    /**
     * Generates the texture for a given side.
     */
    private Texture createTextureForSide(CameraView cameraView, Side3D aSide)
    {
        // Get camera and configure for side
        Camera camera = cameraView.getCamera();
        camera.setYawPitchRollForSideAndPos(aSide, null);
        camera.setOrtho(true);

        // Create image
        Image image = ViewUtils.getImageForScale(cameraView, 1);

        // Create image/texture and return
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

        // Set texture coords Front/Back/Left/Right (flip X)
        if (aSide.isFrontOrBack() || aSide.isLeftOrRight()) {
            sideShape.addTexCoord(1, 0);
            sideShape.addTexCoord(0, 0);
            sideShape.addTexCoord(0, 1);
            sideShape.addTexCoord(1, 1);
        }

        // Set texture coords Top/Bottom (flip Y)
        else {
            sideShape.addTexCoord(0, 1);
            sideShape.addTexCoord(1, 1);
            sideShape.addTexCoord(1, 0);
            sideShape.addTexCoord(0, 0);
        }
    }
}
