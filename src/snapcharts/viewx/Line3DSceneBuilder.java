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
import snapcharts.model.Intervals;
import snapcharts.model.Trace;
import snapcharts.model.TraceList;

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
     * Rebuilds the chart.
     */
    protected void rebuildScene()
    {
        // Do normal version
        super.rebuildScene();

        // Get Trace info
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
        areaPath.setDoubleSided(true);
        _scene.addShape(areaPath);
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

//    /**
//     * Override to provide section dividers.
//     */
//    @Override
//    public Intervals getIntervalsX()
//    {
//        TraceList traceList = _dataArea.getTraceList();
//        int traceCount = traceList.getTraceCount();
//        return Intervals.getIntervalsSimple(0, traceCount);
//    }
}
