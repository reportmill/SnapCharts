/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Path2D;
import snap.geom.Point;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx3d.Poly3D;
import snap.gfx3d.Shape3D;
import snapcharts.model.*;
import snapcharts.view.ChartHelper;

/**
 * A DataArea3D subclass to display the contents of Line3D chart.
 */
public class Line3DDataArea extends DataArea3D {

    /**
     * Constructor.
     */
    public Line3DDataArea(ChartHelper aChartHelper, Trace aTrace, boolean isVisible)
    {
        super(aChartHelper, aTrace, isVisible);
    }

    /**
     * Override to add contour chart.
     */
    @Override
    protected AxisBoxShape createAxisBoxShape()
    {
        // Do normal version
        AxisBoxShape axisBoxShape = super.createAxisBoxShape();
        double prefDepth = axisBoxShape.getPrefDepth();

        // Get Trace info
        TraceList traceList = getTraceList();
        Trace[] traces = traceList.getEnabledTraces();
        int traceCount = traces.length;

        // Iterate over traces and add Line3D shape for each
        for (int i = 0; i < traceCount; i++) {
            Trace trace = traces[i];
            Shape3D lineShape = addLine3D(trace, i, traceCount, prefDepth);
            axisBoxShape.addChild(lineShape);
        }

        // Return
        return axisBoxShape;
    }

    /**
     * Adds the Line3D shape for Trace.
     */
    protected Shape3D addLine3D(Trace aTrace, int anIndex, int aCount, double prefDepth)
    {
        // Create 2d path
        Shape path = createDataPath(aTrace);
        Color dataStrokeColor = aTrace.getLineColor();
        Color dataFillColor = dataStrokeColor.blend(Color.CLEARWHITE, .25);

        // Get depth, and Z values for back/front
        double sectionDepth = prefDepth / aCount;
        double lineZ = sectionDepth * (anIndex + .5);

        // Create/configure line area Poly3D and add to scene
        Poly3D areaPath = new Poly3D(path, lineZ);
        areaPath.setColor(dataFillColor);
        areaPath.setStroke(dataStrokeColor, 1);
        areaPath.setDoubleSided(true);
        areaPath.reverse();
        return areaPath;
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
        double areaW = getWidth();
        double areaH = getHeight();
        double areaMaxX = areaW - inset;
        double areaMaxY = areaH - inset;

        // Iterate over data points, get in display coords and add path line segment
        for (int j = 0; j < pointCount; j++) {

            // Get data point in display coords
            double dataX = aTrace.getX(j);
            double dataY = aTrace.getY(j);
            Point dispXY = dataToView(dataX, dataY);

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
        double zeroPointY = areaH - Math.max(areaY, Math.min(dataToViewY(0), areaMaxY));
        path.lineTo(pointLast.x, zeroPointY);
        path.lineTo(point0.x, zeroPointY);
        path.close();

        // Return path
        return path;
    }
}