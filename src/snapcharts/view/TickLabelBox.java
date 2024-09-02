package snapcharts.view;
import snap.geom.Point;
import snap.geom.Size;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.view.ChildView;
import snapcharts.charts.Axis;
import snapcharts.charts.Trace;
import snapcharts.charts.Content;
import snapcharts.charts.Intervals;
import java.util.ArrayList;
import java.util.List;

/**
 * A container view to hold TickLabels.
 */
public class TickLabelBox extends ChildView {

    /**
     * Returns the AxisView.
     */
    public AxisView getAxisView()  { return (AxisView) getParent(); }

    /**
     * Override to return TickLabels.MaxWidth for AxisViewY.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        // If X axis, just return 200 (X axis doesn't have a PrefWidth)
        AxisView axisView = (AxisView) getParent();
        if (axisView instanceof AxisViewX)
            return 200;

        // Get/return max ticks size width
        Size maxTicksSize = axisView.getMaxTickLabelRotatedSize();
        return maxTicksSize.width;
    }

    /**
     * Override to return text height for AxisViewX.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        // If Y axis, just return 200 (Y axis doesn't have a PrefHeight)
        AxisView axisView = (AxisView) getParent();
        if (axisView instanceof AxisViewY)
            return 200;

        // Get/return max ticks size height
        Size maxTicksSize = axisView.getMaxTickLabelRotatedSize();
        return maxTicksSize.height;
    }

    /**
     * Returns the widest tick labels.
     */
    private static TickLabel getMaxTickLabel(AxisView axisView)
    {
        // Get tickLabels
        TickLabel[] tickLabels = createTickLabels(axisView);
        TickLabel longLabel = null;

        // Iterate over tick labels to find the widest
        for (TickLabel tickLabel : tickLabels) {
            if (longLabel == null || tickLabel.getPrefWidth() > longLabel.getPrefWidth())
                longLabel = tickLabel;
        }

        // Return
        return longLabel;
    }

    /**
     * Returns the tick labels maximum rotated size.
     */
    public static Size getMaxTickLabelRotatedSize(AxisView axisView)
    {
        // Get non rotated max width/height
        TickLabel maxLabel = getMaxTickLabel(axisView);
        if (maxLabel == null)
            return new Size();
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
        // Get Intervals info
        Intervals intervals = axisView.getIntervals();
        int intervalCount = intervals.getCount();

        // Handle category axis
        boolean isCategoryAxis = axisView.isCategoryAxis();
        Content content = axisView.getContent();
        Trace trace = content.getTraceCount() > 0 ? content.getTrace(0) : null;
        int pointCount = content.getPointCount();

        // Get TickLabel attributes
        Axis axis = axisView.getAxis();
        Font tickLabelFont = axis.getFont();
        Color tickTextFill = axis.getTextFill();
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

            // Get label string
            String tickStr = isCategoryAxis && i - 1 < pointCount ?
                    trace.getString(i - 1) :
                    tickFormat.format(dataX);

            // Create/config/add TickLabel
            TickLabel tickLabel = new TickLabel(dataX);
            tickLabel.setText(tickStr);
            tickLabel.setFont(tickLabelFont);
            tickLabel.setTextColor(tickTextFill);
            tickLabels.add(tickLabel);
        }

        // Create/return array of TickLabels
        return tickLabels.toArray(new TickLabel[0]);
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
