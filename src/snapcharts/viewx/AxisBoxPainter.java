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
 * This class paints tick labels for AxisBox.
 */
public class AxisBoxPainter {

    // The AxisBoxBuilder
    private AxisBoxBuilder  _axisBoxBuilder;

    // The DataArea
    private DataArea  _dataArea;

    /**
     * Constructor.
     */
    public AxisBoxPainter(AxisBoxBuilder axisBoxBuilder, DataArea aDataArea, Scene3D aScene)
    {
        _axisBoxBuilder = axisBoxBuilder;
        _dataArea = aDataArea;
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
        Intervals intervalsX = _axisBoxBuilder.getIntervalsX();
        Intervals intervalsY = _axisBoxBuilder.getIntervalsY();
        Intervals intervalsZ = _axisBoxBuilder.getIntervalsZ();
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
            Point3D axisPoint1 = _axisBoxBuilder.convertDataToView(dataX, dataY, dataZ1);
            Point3D axisPoint2 = _axisBoxBuilder.convertDataToView(dataX, dataY, dataZ2);

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
        Intervals intervalsX = _axisBoxBuilder.getIntervalsX();
        Intervals intervalsY = _axisBoxBuilder.getIntervalsY();
        Intervals intervalsZ = _axisBoxBuilder.getIntervalsZ();
        double dataZ1 = intervalsZ.getMin();
        double dataZ2 = intervalsZ.getMax();

        // Get DataX: Either Min/Max depending on which side is facing camera
        boolean isLeftSideFacingCamera = _axisBoxBuilder.isSideFacingCamera(Side3D.LEFT);
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
            Point3D axisPoint1 = _axisBoxBuilder.convertDataToView(dataX, dataY, dataZ1);
            Point3D axisPoint2 = _axisBoxBuilder.convertDataToView(dataX, dataY, dataZ2);

            // Get/set TickLabel.Text
            String tickStr = tickFormat.format(dataY);
            tickLabel.setText(tickStr);

            // Paint TickLabel with correct bounds for axis line
            paintTickLabelForPoints(aPntr, tickLabel, axisPoint1, axisPoint2);
        }
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
