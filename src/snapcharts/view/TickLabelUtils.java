/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.Point;
import snap.geom.Size;
import snap.gfx.Font;
import snap.gfx.Paint;
import snapcharts.model.Axis;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;
import snapcharts.model.Intervals;
import java.util.ArrayList;
import java.util.List;

/**
 * A class with utility methods for TickLabels.
 */
public class TickLabelUtils {

    /**
     * Returns the widest tick labels.
     */
    private static TickLabel getMaxTickLabel(AxisView axisView)
    {
        // Get tickLabels
        TickLabel[] tickLabels = createTickLabels(axisView);
        TickLabel longLabel = null;

        for (TickLabel tickLabel : tickLabels) {
            if (longLabel == null || tickLabel.getPrefWidth() > longLabel.getPrefWidth())
                longLabel = tickLabel;
        }

        return longLabel;
    }

    /**
     * Returns the tick labels maximum rotated size.
     */
    public static Size getMaxTickLabelRotatedSize(AxisView axisView)
    {
        // Get non rotated max width/height
        TickLabel maxLabel = getMaxTickLabel(axisView);
        double ticksW = maxLabel.getPrefWidth();
        double ticksH = maxLabel.getPrefHeight();

        // If not rotated, just return size
        Axis axis = axisView.getAxis();
        double tickAngle = axis.getTickLabelRotation();
        if (tickAngle == 0)
            return new Size(ticksW, ticksH);

        // Calculate rotated size and return
        double radA = Math.toRadians(tickAngle);
        double sinA = Math.abs(Math.sin(radA));
        double cosA = Math.abs(Math.cos(radA));
        double rotW = Math.ceil(ticksW * cosA + ticksH * sinA - .1);
        double rotH = Math.ceil(ticksW * sinA + ticksH * cosA - .1);
        return new Size(rotW, rotH);
    }

    /**
     * Returns the array of tick label StringBoxes.
     */
    public static TickLabel[] createTickLabels(AxisView axisView)
    {
        // Handle Category axis special
        if (axisView.isCategoryAxis())
            return createTickLabelsForCategoryAxis(axisView);

        // Get Intervals info
        Intervals intervals = axisView.getIntervals();
        int intervalCount = intervals.getCount();

        // Get TickLabel attributes
        Axis axis = axisView.getAxis();
        Font tickLabelFont = axis.getFont();
        Paint tickTextFill = axis.getTextFill();
        TickLabelFormat tickFormat = new TickLabelFormat(axisView, intervals);

        // Create list
        List<TickLabel> tickLabels = new ArrayList<>(intervalCount);

        // Iterate over intervals
        for (int i = 0; i < intervalCount; i++) {

            // If not full interval, skip
            boolean fullInterval = intervals.isFullInterval(i);
            if (!fullInterval)
                continue;

            // Get interval
            double dataX = intervals.getInterval(i);

            // Create/config/add TickLabel
            TickLabel tickLabel = new TickLabel(axisView, dataX);
            String str = tickFormat.format(dataX);
            tickLabel.setText(str);
            tickLabel.setFont(tickLabelFont);
            tickLabel.setTextFill(tickTextFill);
            tickLabels.add(tickLabel);
        }

        // Create/return array of TickLabels
        return tickLabels.toArray(new TickLabel[0]);
    }

    /**
     * Creates TickLabels for Category Axis (e.g. for Bar charts).
     */
    public static TickLabel[] createTickLabelsForCategoryAxis(AxisView axisView)
    {
        // Get DataSet and pointCount
        DataSetList dataSetList = axisView.getDataSetList();
        DataSet dataSet = dataSetList.getDataSetCount() > 0 ? dataSetList.getDataSet(0) : null;
        int pointCount = dataSetList.getPointCount();
        TickLabel[] tickLabels = new TickLabel[pointCount];

        // Get TickLabel attributes
        Axis axis = axisView.getAxis();
        Font tickLabelFont = axis.getFont();
        Paint tickTextFill = axis.getTextFill();

        // Iterate over points and create/set TickLabel
        for (int i = 0; i < pointCount; i++) {
            TickLabel tickLabel = tickLabels[i] = new TickLabel(axisView, i + .5);
            String str = dataSet.getString(i); // was getC(i)
            tickLabel.setText(str);
            tickLabel.setFont(tickLabelFont);
            tickLabel.setTextFill(tickTextFill);
        }

        // Return TickLabels
        return tickLabels;
    }

    /**
     * Sets the rotation of a TickLabel such that it is also positioned correctly.
     */
    public static void setTickLabelRotation(AxisView axisView, TickLabel tickLabel, double tickRot)
    {
        // Set rotation (just return if zero)
        tickLabel.setRotate(tickRot);
        if (tickRot == 0)
            return;

        // Get point on label perimeter that we sync to with no rotation (in label parent coords)
        boolean vertical = axisView instanceof AxisViewY;
        double tickW = tickLabel.getWidth();
        double tickH = tickLabel.getHeight();
        double anchor1X = tickLabel.getX() + (vertical ? tickW : tickW / 2);
        double anchor1Y = tickLabel.getY() + (vertical ? tickH / 2 : 0);

        // Get angle of label bounds perimeter point radial that we want to sync to
        // If vertical graph and angle less than 60, just zero it out
        // If horizontal, we want to sync to 12 o'clock position instead of 3 o'clock position
        double angle = -tickRot;
        //if(vertical && Math.abs(angle) <= 60) angle = 0;
        if(!vertical)
            angle -= 90;

        // Get point on label perimeter that we sync to for given label rotation (in label parent coords)
        Point anchor2 = tickLabel.getBoundsLocal().getPerimeterPointForRadial(angle, true);
        anchor2 = tickLabel.localToParent(anchor2.x, anchor2.y);

        // Offset label location from original anchor location to new anchor location
        double transX = anchor1X - anchor2.x;
        double transY = anchor1Y - anchor2.y;
        tickLabel.setXY(tickLabel.getX() + transX, tickLabel.getY() + transY);
    }
}
