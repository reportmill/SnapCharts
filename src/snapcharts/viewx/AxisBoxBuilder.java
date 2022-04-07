/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Color;
import snap.gfx3d.*;
import snap.util.MathUtils;
import snap.view.ViewUtils;
import snapcharts.model.*;
import snapcharts.view.AxisView;
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

    // Runnables to rebuild chart deferred/coalesced
    private Runnable  _rebuildChartRun, _rebuildChartRunImpl = () -> rebuildAxisBoxNow();

    /**
     * Constructor.
     */
    public AxisBoxBuilder(DataArea aDataArea, Scene3D aScene)
    {
        _dataArea = aDataArea;
        _scene = aScene;
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
}
