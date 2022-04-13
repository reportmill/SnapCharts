/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.Painter;
import snap.gfx3d.*;
import snap.text.TextFormat;
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
    public AxisBoxTextPainter(DataArea3D aDataArea, Scene3D aScene)
    {
        _dataArea = aDataArea;
    }

    /**
     * Paints Text for AxisBoxShape (tick labels, axis labels).
     */
    public void paintAxisBoxText(Painter aPntr)
    {
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
        // Get Axis intervals and Y+Z location of label in data space
        Intervals intervalsX = _dataArea.getIntervalsX();
        Intervals intervalsY = _dataArea.getIntervalsY();
        Intervals intervalsZ = _dataArea.getIntervalsZ();
        double dataY1 = intervalsY.getMin();
        double dataY2 = dataY1;
        double dataZ1 = intervalsZ.getMin();
        double dataZ2 = intervalsZ.getMax();

        // If not Forward XY (Bar3D/Line3D), make DataZ1/DataZ2 constant and set DataY1/DataY2 to opposite points
        boolean isForwardXY = _dataArea.isForwardXY();
        if (!isForwardXY) {

            // If top side is facing camera, swap
            boolean isTopSideFacingCamera = !_dataArea.isSideFacingCamera(Side3D.BOTTOM);
            dataZ1 = dataZ2 = isTopSideFacingCamera ? intervalsZ.getMax() : intervalsZ.getMin();

            // Reset dataY1 dataY2
            boolean isBackSideFacingCamera = _dataArea.isSideFacingCamera(Side3D.BACK);
            dataY1 = isBackSideFacingCamera ? intervalsY.getMin() : intervalsY.getMax();
            dataY2 = isBackSideFacingCamera ? intervalsY.getMax() : intervalsY.getMin();
        }

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
            Point3D axisPoint1 = _dataArea.convertDataToView(dataX, dataY1, dataZ1);
            Point3D axisPoint2 = _dataArea.convertDataToView(dataX, dataY2, dataZ2);

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
        Intervals intervalsX = _dataArea.getIntervalsX();
        Intervals intervalsY = _dataArea.getIntervalsY();
        Intervals intervalsZ = _dataArea.getIntervalsZ();
        double dataZ1 = intervalsZ.getMin();
        double dataZ2 = intervalsZ.getMax();

        // Get DataX: Either Min/Max depending on which side is facing camera
        boolean isLeftSideFacingCamera = _dataArea.isSideFacingCamera(Side3D.LEFT);
        double dataX1 = isLeftSideFacingCamera ? intervalsX.getMin() : intervalsX.getMax();
        double dataX2 = dataX1;

        // If not Forward XY (Bar3D/Line3D), make DataZ1/DataZ2 constant and set DataX1/DataX2 to opposite points
        boolean isForwardXY = _dataArea.isForwardXY();
        if (!isForwardXY) {

            // If top side is facing camera, swap
            boolean isTopSideFacingCamera = !_dataArea.isSideFacingCamera(Side3D.BOTTOM);
            dataZ1 = dataZ2 = isTopSideFacingCamera ? intervalsZ.getMax() : intervalsZ.getMin();

            // Reset dataX1 dataX2
            dataX1 = isLeftSideFacingCamera ? intervalsX.getMax() : intervalsX.getMin();
            dataX2 = isLeftSideFacingCamera ? intervalsX.getMin() : intervalsX.getMax();
        }

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
            Point3D axisPoint1 = _dataArea.convertDataToView(dataX1, dataY, dataZ1);
            Point3D axisPoint2 = _dataArea.convertDataToView(dataX2, dataY, dataZ2);

            // Get/set TickLabel.Text
            String tickStr = tickFormat.format(dataY);
            tickLabel.setText(tickStr);

            // Paint TickLabel with correct bounds for axis line
            paintTickLabelForPoints(aPntr, tickLabel, axisPoint1, axisPoint2);
        }
    }

    /**
     * Paint TickLabels for Z axis.
     */
    protected void paintTickLabelsZ(Painter aPntr)
    {
        // Get Axis intervals
        Intervals intervalsX = _dataArea.getIntervalsX();
        Intervals intervalsY = _dataArea.getIntervalsY();
        Intervals intervalsZ = _dataArea.getIntervalsZ();

        // Get DataX: Either Min/Max depending on which side is facing camera
        boolean isLeftSideFacingCamera = _dataArea.isSideFacingCamera(Side3D.LEFT);
        double dataX1 = isLeftSideFacingCamera ? intervalsX.getMin() : intervalsX.getMax();
        double dataX2 = isLeftSideFacingCamera ? intervalsX.getMax() : intervalsX.getMin();

        // Get DataY: Either Min/Max depending on which side is facing camera
        double dataY1 = intervalsY.getMin();
        double dataY2 = dataY1;

        // If not Forward XY (Bar3D/Line3D), make DataZ1/DataZ2 constant and set DataX1/DataX2 to opposite points
        boolean isForwardXY = _dataArea.isForwardXY();
        if (!isForwardXY) {

            // If left side is facing camera, swap
            //dataX1 = isLeftSideFacingCamera ? intervalsX.getMin() : intervalsZ.getMin();

            // Reset dataX1 dataX2
            //boolean isLeftSideFacingCamera = !_axisBoxBuilder.isSideFacingCamera(Side3D.BOTTOM);
            //dataX1 = isLeftSideFacingCamera ? intervalsX.getMax() : intervalsX.getMin();
            //dataX2 = isLeftSideFacingCamera ? intervalsX.getMin() : intervalsX.getMax();
        }

        // Create/configure TickLabel
        TickLabel tickLabel = getTickLabelForAxis(AxisType.Y);
        Axis axis = _dataArea.getChart().getAxisForType(AxisType.Y);
        TextFormat tickFormat = axis.getTextFormat();

        // Iterate over intervals and configure/paint TickLabel for each
        for (int i = 0, iMax = intervalsZ.getCount(); i < iMax; i++) {

            // If not full interval, just skip
            if (!intervalsZ.isFullInterval(i)) continue;

            // Get 3D point for label
            double dataZ = intervalsZ.getInterval(i);
            Point3D axisPoint1 = _dataArea.convertDataToView(dataX1, dataY1, dataZ);
            Point3D axisPoint2 = _dataArea.convertDataToView(dataX2, dataY2, dataZ);

            // Get/set TickLabel.Text
            String tickStr = tickFormat.format(dataZ);
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
}
