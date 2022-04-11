/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx3d.*;
import snap.text.TextFormat;
import snap.util.MathUtils;
import snap.view.ViewUtils;
import snapcharts.model.*;
import snapcharts.view.AxisView;
import snapcharts.view.AxisViewX;
import snapcharts.view.DataArea;
import snapcharts.view.TickLabel;

/**
 * This class builds the AxisBox shape to hold most 3D chart shapes.
 */
public abstract class AxisBoxBuilder {

    // The DataArea
    private DataArea  _dataArea;

    // The Scene
    private Scene3D  _scene;

    // The Camera
    private Camera3D  _camera;

    // Runnables to rebuild chart deferred/coalesced
    private Runnable  _rebuildChartRun, _rebuildChartRunImpl = () -> rebuildAxisBoxNow();

    /**
     * Constructor.
     */
    public AxisBoxBuilder(DataArea aDataArea, Scene3D aScene)
    {
        _dataArea = aDataArea;
        _scene = aScene;
        _camera = aScene.getCamera();
    }

    /**
     * Rebuilds the chart.
     */
    protected void rebuildAxisBox()
    {
        if (_rebuildChartRun == null)
            ViewUtils.runLater(_rebuildChartRun = _rebuildChartRunImpl);
    }

    /**
     * Rebuilds the chart immediately.
     */
    protected void rebuildAxisBoxNow()
    {
        // Remove all existing children
        _scene.removeChildren();

        // Get AxisBoxShape and add to scene
        Shape3D axisBoxShape = getAxisBoxShape();
        _scene.addChild(axisBoxShape);

        // Repaint DataArea and reset runnable
        _dataArea.repaint();
        _rebuildChartRun = null;
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
            default: throw new RuntimeException("AxisBoxBuilder.getAxesForSide: Invalid side: " + aSide);
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
            default: throw new RuntimeException("AxisBoxBuilder.getAxesForSide: Invalid side: " + aSide);
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

        // Just return default { 0, 1 } Intervals for Bar3D
        return Intervals.getIntervalsSimple(0, 1);
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
            default: throw new RuntimeException("AxisBoxBuilder.getIntervalsForAxis: Invalid axis: " + anAxisType);
        }
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
    protected ParentShape createAxisBoxShape()
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
        ParentShape axisBoxShape = new ParentShape();
        axisBoxShape.setChildren(frontShape, backShape, leftShape, rightShape, bottomShape);
        return axisBoxShape;
    }

    /**
     * Adds geometry for front/back sides.
     */
    private Shape3D addSideFrontBack(double width, double height, double sideZ)
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
        Poly3D side = new Poly3D();
        side.setName(sideY == 0 ? "AxisBottom" : "AxisTop");
        side.setColor(Color.LIGHTGRAY);
        side.setOpacity(.8f);
        side.setDoubleSided(true);

        // Add side points
        side.addPoint(0, sideY, 0);
        side.addPoint(0, sideY, depth);
        side.addPoint(width, sideY, depth);
        side.addPoint(width, sideY, 0);

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

    /**
     * Returns the side shape.
     */
    public FacetShape getSideShape(Side3D aSide)
    {
        ParentShape axisBoxShape = (ParentShape) _scene.getChild(0);
        switch (aSide) {
            case FRONT: return (FacetShape) axisBoxShape.getChild(0);
            case BACK: return (FacetShape) axisBoxShape.getChild(1);
            case LEFT: return (FacetShape) axisBoxShape.getChild(2);
            case RIGHT: return (FacetShape) axisBoxShape.getChild(3);
            case BOTTOM: return (FacetShape) axisBoxShape.getChild(4);
            default: return null;
        }
    }

    /**
     * Returns whether given side is facing camera.
     */
    public boolean isSideFacingCamera(Side3D aSide)
    {
        FacetShape axisSide = getSideShape(aSide);
        Vector3D axisSideNormal = axisSide.getNormal();
        Camera3D camera = _scene.getCamera();
        Matrix3D sceneToCamera = camera.getSceneToCamera();
        Vector3D axisSideNormalInCamera = sceneToCamera.transformVector(axisSideNormal.clone());
        Vector3D cameraNormal = camera.getNormal();
        boolean isFacing = !axisSideNormalInCamera.isAligned(cameraNormal, false);
        return isFacing;
    }

    /**
     * Returns a TickLabel for given AxisType.
     */
    public TickLabel getTickLabelForAxis(AxisType axisType)
    {
        Chart chart = _dataArea.getChart();
        Axis axis = chart.getAxisForType(axisType);

        TickLabel tickLabel = new TickLabel(0);
        tickLabel.setFont(axis.getFont());
        tickLabel.setTextFill(axis.getTextFill());
        tickLabel.setPadding(8, 8, 8, 8);
        return tickLabel;
    }

    /**
     * Paint TickLabels for X axis.
     */
    protected void paintTickLabelsX(Painter aPntr)
    {
        // Get Axis intervals and Y+Z location of label in data space
        Intervals intervalsX = getIntervalsX();
        Intervals intervalsY = getIntervalsY();
        Intervals intervalsZ = getIntervalsZ();
        double dataY = intervalsY.getMin();
        double dataZ1 = intervalsZ.getMin();
        double dataZ2 = intervalsZ.getMax();

        // Create/configure TickLabel
        TickLabel tickLabel = getTickLabelForAxis(AxisType.X);
        Axis axis = _dataArea.getChart().getAxisForType(AxisType.X);
        TextFormat tickFormat = axis.getTextFormat();

        // Handle category axis
        AxisViewX axisViewX = _dataArea.getAxisViewX();
        boolean isCategoryAxis = axisViewX.isCategoryAxis();
        TraceList traceList = axisViewX.getTraceList();
        Trace trace = traceList.getTraceCount() > 0 ? traceList.getTrace(0) : null;
        int pointCount = traceList.getPointCount();

        // Iterate over intervals and configure/paint TickLabel for each
        for (int i = 0, iMax = intervalsX.getCount(); i < iMax; i++) {

            // If not full interval, just skip
            if (!intervalsX.isFullInterval(i)) continue;

            // Get 3D point for label
            double dataX = intervalsX.getInterval(i);
            Point3D axisPoint1 = convertDataToView(dataX, dataY, dataZ1);
            Point3D axisPoint2 = convertDataToView(dataX, dataY, dataZ2);

            // Get label string
            String tickStr = isCategoryAxis && i - 1 < pointCount ?
                    trace.getString(i - 1) :
                    tickFormat.format(dataX);

            // Configure TickLabel and paint
            tickLabel.setText(tickStr);

            // Paint TickLabel with correct bounds for axis line
            paintTickLabelForPoints(aPntr, tickLabel, axisPoint1, axisPoint2);
        }
    }

    /**
     * Paint TickLabels for Y axis.
     */
    protected void paintTickLabelsY(Painter aPntr)
    {
        // Get Axis intervals and Y+Z location of label in data space
        Intervals intervalsX = getIntervalsX();
        Intervals intervalsY = getIntervalsY();
        Intervals intervalsZ = getIntervalsZ();
        double dataZ1 = intervalsZ.getMin();
        double dataZ2 = intervalsZ.getMax();

        // Get DataX: Either Min/Max depending on which side is facing camera
        boolean isLeftSideFacingCamera = isSideFacingCamera(Side3D.LEFT);
        double dataX = isLeftSideFacingCamera ? intervalsX.getMin() : intervalsX.getMax();

        // Create/configure TickLabel
        TickLabel tickLabel = getTickLabelForAxis(AxisType.Y);
        Axis axis = _dataArea.getChart().getAxisForType(AxisType.Y);
        TextFormat tickFormat = axis.getTextFormat();

        // Iterate over intervals and configure/paint TickLabel for each
        for (int i = 0, iMax = intervalsY.getCount(); i < iMax; i++) {

            // If not full interval, just skip
            if (!intervalsY.isFullInterval(i)) continue;

            // Get 3D point for label
            double dataY = intervalsY.getInterval(i);
            Point3D axisPoint1 = convertDataToView(dataX, dataY, dataZ1);
            Point3D axisPoint2 = convertDataToView(dataX, dataY, dataZ2);

            // Get/set TickLabel.Text
            String tickStr = tickFormat.format(dataY);
            tickLabel.setText(tickStr);

            // Paint TickLabel with correct bounds for axis line
            paintTickLabelForPoints(aPntr, tickLabel, axisPoint1, axisPoint2);
        }
    }

    /**
     * Returns given XYZ data point as Point3D in View coords.
     */
    public Point3D convertDataToView(double aX, double aY, double aZ)
    {
        // Get intervals and AxisBox
        Intervals intervalsX = getIntervalsX();
        Intervals intervalsY = getIntervalsY();
        Intervals intervalsZ = getIntervalsZ();
        Shape3D axisBox = _scene.getChild(0);

        // Get XYZ in AxisBox space
        double axisBoxX = MathUtils.mapValueForRanges(aX, intervalsX.getMin(), intervalsX.getMax(), axisBox.getMinX(), axisBox.getMaxX());
        double axisBoxY = MathUtils.mapValueForRanges(aY, intervalsY.getMin(), intervalsY.getMax(), axisBox.getMinY(), axisBox.getMaxY());
        double axisBoxZ = MathUtils.mapValueForRanges(aZ, intervalsZ.getMin(), intervalsZ.getMax(), axisBox.getMinZ(), axisBox.getMaxZ());

        // Transform point from AxisBox (Scene) to View space
        Matrix3D sceneToView = _camera.getSceneToView();
        Point3D pointInView = sceneToView.transformPoint(axisBoxX, axisBoxY, axisBoxZ);

        // Return
        return pointInView;
    }

    /**
     * Paints TickLabel for given axis line.
     */
    private static void paintTickLabelForPoints(Painter aPntr, TickLabel tickLabel, Point3D axisPoint1, Point3D axisPoint2)
    {
        // Make sure axisPoint1 is the close point
        if (axisPoint2.z < axisPoint1.z) {
            Point3D temp = axisPoint2;
            axisPoint2 = axisPoint1;
            axisPoint1 = temp;
        }

        // Get radial angle
        double dx = axisPoint2.x - axisPoint1.x;
        double dy = axisPoint1.y - axisPoint2.y;
        double angleDeg = -getAngleBetweenPoints(1, 0, dx, dy);

        // Set TickLabel size and get rect
        tickLabel.setSizeToPrefSize();
        Rect tickLabelBounds = tickLabel.getBoundsLocal();

        // Position tickLabel so that
        Point perimiterPoint = tickLabelBounds.getPerimeterPointForRadial(angleDeg, true);
        double tickLabelX = axisPoint1.x - perimiterPoint.x;
        double tickLabelY = axisPoint1.y - perimiterPoint.y;
        tickLabel.setXY(tickLabelX, tickLabelY);
        tickLabel.paintStringView(aPntr);

        //tickLabel.setBorder(Color.PINK, 1);
        //aPntr.setColor(Color.GREEN); aPntr.fill(new snap.geom.Ellipse(axisPoint1.x - 3, axisPoint1.y - 3, 6, 6));
        //aPntr.setColor(Color.ORANGE); aPntr.fill(new snap.geom.Ellipse(axisPoint2.x - 3, axisPoint2.y - 3, 6, 6));
    }

    /**
     * Returns the angle between two XY points.
     */
    private static double getAngleBetweenPoints(double x1, double y1, double x2, double y2)
    {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double angleDeg = Math.toDegrees(angle);
        return angleDeg;
    }
}
