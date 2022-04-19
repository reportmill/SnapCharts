/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.Painter;
import snap.gfx3d.*;
import snap.text.TextFormat;
import snap.view.StringView;
import snapcharts.model.*;
import snapcharts.view.AxisViewX;
import snapcharts.view.TickLabel;

/**
 * This class paints axis labels and axis tick labels for AxisBoxShape.
 */
public class AxisBoxTextPainter {

    // The DataArea
    private DataArea3D  _dataArea;

    /**
     * Constructor.
     */
    public AxisBoxTextPainter(DataArea3D aDataArea)
    {
        _dataArea = aDataArea;
    }

    /**
     * Paints Text for AxisBoxShape (tick labels, axis labels).
     */
    public void paintAxisBoxText(Painter aPntr)
    {
        // Paint XYZ ticks
        paintTickLabelsX(aPntr);
        paintTickLabelsY(aPntr);
        if (_dataArea.getChartType() != ChartType.BAR_3D)
            paintTickLabelsZ(aPntr);
    }

    /**
     * Paint TickLabels for X axis.
     */
    protected void paintTickLabelsX(Painter aPntr)
    {
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

        // Get axis grid line end points
        Line3D gridLine = _dataArea.getAxisGridLineInDataSpace(AxisType.X, 0);
        Point3D gridLineP1 = gridLine.getP1();
        Point3D gridLineP2 = gridLine.getP2();

        // Get axis intervals
        Intervals intervalsX = _dataArea.getIntervalsX();

        // Iterate over intervals and configure/paint TickLabel for each
        for (int i = 0, iMax = intervalsX.getCount(); i < iMax; i++) {

            // If not full interval, just skip
            if (!intervalsX.isFullInterval(i)) continue;

            // Get 3D point for label
            double dataX = intervalsX.getInterval(i);
            Point3D axisPoint1 = _dataArea.convertDataToView(dataX, gridLineP1.y, gridLineP1.z);
            Point3D axisPoint2 = _dataArea.convertDataToView(dataX, gridLineP2.y, gridLineP2.z);

            // Get label string
            String tickStr = isCategoryAxis && i - 1 < pointCount ?
                    trace.getString(i - 1) :
                    tickFormat.format(dataX);

            // Configure TickLabel and paint
            tickLabel.setText(tickStr);

            // Paint TickLabel with correct bounds for axis line
            paintTickLabelForPoints(aPntr, tickLabel, axisPoint1, axisPoint2, false);
        }

        // Paint AxisLabel
        paintAxisLabelForAxis(aPntr, tickLabel, AxisType.X);
    }

    /**
     * Paint TickLabels for Y axis.
     */
    protected void paintTickLabelsY(Painter aPntr)
    {
        // Create/configure TickLabel
        TickLabel tickLabel = getTickLabelForAxis(AxisType.Y);
        Axis axis = _dataArea.getChart().getAxisForType(AxisType.Y);
        TextFormat tickFormat = axis.getTextFormat();

        // Get axis grid line end points
        Line3D gridLine = _dataArea.getAxisGridLineInDataSpace(AxisType.Y, 0);
        Point3D gridLineP1 = gridLine.getP1();
        Point3D gridLineP2 = gridLine.getP2();

        // Get axis intervals
        Intervals intervalsY = _dataArea.getIntervalsY();

        // Iterate over intervals and configure/paint TickLabel for each
        for (int i = 0, iMax = intervalsY.getCount(); i < iMax; i++) {

            // If not full interval, just skip
            if (!intervalsY.isFullInterval(i)) continue;

            // Get 3D point for label
            double dataY = intervalsY.getInterval(i);
            Point3D axisPoint1 = _dataArea.convertDataToView(gridLineP1.x, dataY, gridLineP1.z);
            Point3D axisPoint2 = _dataArea.convertDataToView(gridLineP2.x, dataY, gridLineP2.z);

            // Get/set TickLabel.Text
            String tickStr = tickFormat.format(dataY);
            tickLabel.setText(tickStr);

            // Paint TickLabel with correct bounds for axis line
            paintTickLabelForPoints(aPntr, tickLabel, axisPoint1, axisPoint2, false);
        }

        // Paint AxisLabel
        paintAxisLabelForAxis(aPntr, tickLabel, AxisType.Y);
    }

    /**
     * Paint TickLabels for Z axis.
     */
    protected void paintTickLabelsZ(Painter aPntr)
    {
        // Create/configure TickLabel
        TickLabel tickLabel = getTickLabelForAxis(AxisType.Z);
        Axis axis = _dataArea.getChart().getAxisForType(AxisType.Z);
        TextFormat tickFormat = axis.getTextFormat();

        // Get axis grid line end points
        Line3D gridLine = _dataArea.getAxisGridLineInDataSpace(AxisType.Z, 0);
        Point3D gridLineP1 = gridLine.getP1();
        Point3D gridLineP2 = gridLine.getP2();

        // Get axis intervals
        Intervals intervalsZ = _dataArea.getIntervalsZ();

        // Iterate over intervals and configure/paint TickLabel for each
        for (int i = 0, iMax = intervalsZ.getCount(); i < iMax; i++) {

            // If not full interval, just skip
            if (!intervalsZ.isFullInterval(i)) continue;

            // Get 3D point for label
            double dataZ = intervalsZ.getInterval(i);
            Point3D axisPoint1 = _dataArea.convertDataToView(gridLineP1.x, gridLineP1.y, dataZ);
            Point3D axisPoint2 = _dataArea.convertDataToView(gridLineP2.x, gridLineP2.y, dataZ);

            // Get/set TickLabel.Text
            String tickStr = tickFormat.format(dataZ);
            tickLabel.setText(tickStr);

            // Paint TickLabel with correct bounds for axis line
            paintTickLabelForPoints(aPntr, tickLabel, axisPoint1, axisPoint2, false);
        }

        // Paint AxisLabel
        paintAxisLabelForAxis(aPntr, tickLabel, AxisType.Z);
    }

    /**
     * Paints TickLabel for given axis line.
     */
    private static void paintTickLabelForPoints(Painter aPntr, TickLabel tickLabel, Point3D axisPoint1, Point3D axisPoint2, boolean doRotate)
    {
        // Get radial angle
        double dx = axisPoint2.x - axisPoint1.x;
        double dy = axisPoint2.y - axisPoint1.y;
        double angleDeg = getAngleBetweenPoints(1, 0, -dx, -dy);

        // Set TickLabel size and get rect
        tickLabel.setSizeToPrefSize();
        Rect tickLabelBounds = tickLabel.getBoundsLocal();

        // Position tickLabel so that
        Point perimiterPoint = tickLabelBounds.getPerimeterPointForRadial(angleDeg, true);
        double tickLabelX = axisPoint2.x - perimiterPoint.x;
        double tickLabelY = axisPoint2.y - perimiterPoint.y;
        tickLabel.setXY(tickLabelX, tickLabelY);

        // Maybe do rotate
        if (doRotate)
            tickLabel.setRotate(angleDeg + 90);

        // Paint string
        //tickLabel.setBorder(Color.PINK, 1);
        tickLabel.paintStringView(aPntr);
        if (doRotate)
            tickLabel.setRotate(0);

        //paintPoint(aPntr, axisPoint1, Color.GREEN); paintPoint(aPntr, axisPoint2, Color.ORANGE);
    }

    /**
     * Paint Axis label.
     */
    private void paintAxisLabelForAxis(Painter aPntr, StringView aStringView, AxisType axisType)
    {
        // Configure axis label text
        Axis axis = _dataArea.getChart().getAxisForType(axisType);
        String axisLabelText = axis.getTitle();
        if (axisLabelText == null || axisLabelText.length() == 0)
            return;
        aStringView.setText(axisLabelText);
        aStringView.setPadding(32, 32, 32, 32);
        aStringView.setSizeToPrefSize();

        // Get center grid line in camera space
        Line3D gridLine = _dataArea.getAxisGridLineInDataSpace(axisType, .5);
        Point3D gridLineP1 = gridLine.getP1();
        Point3D gridLineP2 = gridLine.getP2();
        _dataArea.convertDataToView(gridLineP1);
        _dataArea.convertDataToView(gridLineP2);

        // Get rotation of axis line in View coords
        Line3D axisLine = _dataArea.getAxisLineInDataSpace(axisType);
        _dataArea.convertDataToView(axisLine.getP1());
        _dataArea.convertDataToView(axisLine.getP2());
        double angleDeg = axisLine.getAngle2DInDeg();
        aStringView.setRotate(angleDeg);

        // Get radial angle
        double dx = gridLineP2.x - gridLineP1.x;
        double dy = gridLineP2.y - gridLineP1.y;
        double angleDeg2 = getAngleBetweenPoints(1, 0, -dx, -dy);

        // Position label so that
        Rect labelBounds = aStringView.getBoundsLocal();
        labelBounds.x = (labelBounds.width - 80) / 2; labelBounds.width = 80;
        Point perimiterPoint = labelBounds.getPerimeterPointForRadial(angleDeg2, true);
        double labelX = gridLine.getP2().x - perimiterPoint.x;
        double labelY = gridLine.getP2().y - perimiterPoint.y;
        aStringView.setXY(labelX, labelY);

        // Paint
        aStringView.paintStringView(aPntr);
    }

    /**
     * Returns a TickLabel for given AxisType.
     */
    private TickLabel getTickLabelForAxis(AxisType axisType)
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
     * Returns the angle between two XY points.
     */
    private static double getAngleBetweenPoints(double x1, double y1, double x2, double y2)
    {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double angleDeg = Math.toDegrees(angle);
        return angleDeg;
    }

    /*private static void paintPoint(Painter aPntr, Point3D aPoint, Color aColor)
    {
        aPntr.setColor(aColor);
        aPntr.fill(new snap.geom.Ellipse(aPoint.x - 3, aPoint.y - 3, 6, 6));
    }
    private void paintLine(Painter aPntr, AxisType axisType, Color aColor)
    {
        Line3D axisLine = _dataArea.getAxisLineInDataSpace(axisType);
        Point3D p1 = axisLine.getP1(); _dataArea.convertDataToView(p1);
        Point3D p2 = axisLine.getP2(); _dataArea.convertDataToView(p2);
        aPntr.setColor(aColor); aPntr.setStroke(Stroke.Stroke2);
        aPntr.drawLine(p1.x, p1.y, p2.x, p2.y);
    }
    private void paintAxisLines(Painter aPntr)
    {
        paintLine(aPntr, AxisType.X, Color.RED);
        paintLine(aPntr, AxisType.Y, Color.GREEN);
        paintLine(aPntr, AxisType.Z, Color.BLUE);
    }*/
}
