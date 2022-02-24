/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Path2D;
import snap.geom.Point;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx3d.Path3D;
import snap.gfx3d.Scene3D;
import snapcharts.model.Trace;
import snapcharts.model.TraceList;
import snapcharts.model.Intervals;
import snapcharts.view.AxisViewX;

/**
 * This class builds the Line3D scene.
 */
public class Line3DSceneBuilder extends AxisBoxSceneBuilder {

    // The Line3DDataArea
    private Line3DDataArea  _dataArea;

    /**
     * Constructor.
     */
    public Line3DSceneBuilder(Line3DDataArea aDataArea, Scene3D aScene)
    {
        super(aDataArea, aScene);
        _dataArea = aDataArea;
    }

    /**
     * Returns the intervals.
     */
    public Intervals getIntervalsY()  { return _dataArea.getIntervalsY(); }

    /**
     * Returns the minor tick count.
     */
    public int getMinorTickCount()  { return _dataArea.getMinorTickCount(); }

    /**
     * Returns the section count.
     */
    public int getSectionCount()  { return 1; }

    /**
     * Rebuilds the chart.
     */
    protected void rebuildScene()
    {
        // Do normal version
        super.rebuildScene();

        // Add Line3D shapes
        addLine3Ds();
    }

    /**
     * Adds the Line3D shapes.
     */
    protected void addLine3Ds()
    {
        // Get info
        TraceList traceList = _dataArea.getTraceList();
        Trace[] traces = traceList.getEnabledTraces();
        int traceCount = traces.length;

        // Iterate over traces and add Line3D shape for each
        for (int i = 0; i < traceCount; i++) {
            Trace trace = traces[i];
            addLine3D(trace, i, traceCount);
        }
    }

    /**
     * Adds the Line3D shape for Trace.
     */
    protected void addLine3D(Trace aTrace, int anIndex, int aCount)
    {
        // Create 2d path
        Shape path = createDataPath(aTrace);
        Color dataStrokeColor = aTrace.getLineColor();
        Color dataFillColor = dataStrokeColor.blend(Color.CLEARWHITE, .25);

        // Get depth, and Z values for back/front
        double prefDepth = getPrefDepth();
        double sectionDepth = prefDepth / aCount;
        double lineZ = sectionDepth * (anIndex + .5);

        // Create/configure line area path3d and add to scene
        Path3D areaPath = new Path3D(path, lineZ);
        areaPath.setColor(dataFillColor);
        areaPath.setStroke(dataStrokeColor, 1);
        _scene.addShape(areaPath);

        // Create backside path3d and add to scene
        Path3D areaPathBack = areaPath.clone();
        areaPathBack.reverse();
        _scene.addShape(areaPathBack);
    }

    /**
     * Returns Path2D for painting Trace.
     */
    protected Shape createDataPath(Trace aTrace)
    {
        // Create/add path for trace
        int pointCount = aTrace.getPointCount();
        Path2D path = new Path2D();

        // Get area bounds
        double inset = 1;
        double areaX = inset;
        double areaY = inset;
        double areaW = _dataArea.getWidth();
        double areaH = _dataArea.getHeight();
        double areaMaxX = areaW - inset;
        double areaMaxY = areaH - inset;

        // Iterate over data points, get in display coords and add path line segment
        for (int j = 0; j < pointCount; j++) {

            // Get data point in display coords
            double dataX = aTrace.getX(j);
            double dataY = aTrace.getY(j);
            Point dispXY = _dataArea.dataToView(dataX, dataY);

            // Clamp to area/axis bounds
            dispXY.x = Math.max(areaX, Math.min(dispXY.x, areaMaxX));
            dispXY.y = areaH - Math.max(areaY, Math.min(dispXY.y, areaMaxY));

            // Add to path
            if (j == 0)
                path.moveTo(dispXY.x, dispXY.y);
            else path.lineTo(dispXY.x, dispXY.y);
        }

        // Close path
        Point point0 = path.getPoint(0);
        Point pointLast = path.getPoint(pointCount - 1);
        double zeroPointY = areaH - Math.max(areaY, Math.min(_dataArea.dataToViewY(0), areaMaxY));
        path.lineTo(pointLast.x, zeroPointY);
        path.lineTo(point0.x, zeroPointY);
        path.close();

        // Return path
        return path;
    }

    /**
     * Rebuild gridlines.
     */
    protected void rebuildGridLines()
    {
        _grid.clear();
        _gridMinor.clear();
        _gridWithoutSep.clear();

        // Get graph bounds
        double areaX = 0;
        double areaY = 0;
        double areaW = _dataArea.getWidth();
        double areaH = _dataArea.getHeight();

        // Get graph min interval and max interval
        Intervals intervalsY = getIntervalsY();
        int countY = intervalsY.getCount();
        double minY = intervalsY.getMin();
        double maxY = intervalsY.getMax();
        double rangeY = maxY - minY;

        // Get grid max
        double gridMax = areaH;
        int minorTickCountY = getMinorTickCount();
        double majorDeltaY = intervalsY.getInterval(1) - minY;
        double minorDeltaY = gridMax * majorDeltaY / rangeY / (minorTickCountY+1);

        // Iterate over graph intervals
        for (int i = 0, iMax = countY; i < iMax - 1; i++) {

            // Get interval ratio and line x & y
            double intervalRatio = i / (iMax - 1f);
            double lineY = areaY + areaH * intervalRatio;

            // DrawMajorAxis
            if (i > 0) {
                addGridLineMajor(areaX, lineY, areaX + areaW, lineY);
            }

            // Draw minor axis
            /*for (int j=0; j<minorTickCountY; j++) {
                double minorLineY = lineY + (j+1) * minorDeltaY;
                addGridLineMinor(areaX, minorLineY, areaX + areaW, minorLineY);
            }*/
        }

        // Get graph min interval and max interval
        AxisViewX axisViewX = _dataArea.getAxisViewX();
        Intervals intervalsX = axisViewX.getIntervals();
        int countX = intervalsX.getCount();

        // Iterate over graph intervals
        for (int i = 0; i < countX; i++) {
            double dataX = intervalsX.getInterval(i);
            double dispX = axisViewX.dataToView(dataX);
            addGridLineSeparator(dispX, areaY, dispX, areaY + areaH);
        }

        // Get info
        TraceList traceList = _dataArea.getTraceList();
        int traceCount = traceList.getTraceCount();

        // Iterate over traces and add separator for side
        if (traceCount > 1) {
            double sectionDepth = areaW / traceCount;
            for (int i = 1; i < traceCount; i++) {
                double lineZ2 = sectionDepth * i;
                _gridWithoutSep.moveTo(lineZ2, 0);
                _gridWithoutSep.lineTo(lineZ2, areaH);
            }
        }
    }
}
