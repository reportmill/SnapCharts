package snapcharts.views;
import snap.geom.Insets;
import snap.gfx.*;
import snap.text.StringBox;
import snapcharts.model.AxisX;
import snapcharts.model.ChartType;
import snapcharts.model.Intervals;

/**
 * A view to paint Chart X Axis.
 */
public class AxisViewX<T extends AxisX> extends AxisView<T> {

    /**
     * Creates the ChartXAxis.
     */
    public AxisViewX()
    {
        super();
        setFont(Font.Arial12);
    }

    /**
     * Returns the axis.
     */
    public AxisX getAxis()
    {
        return getChart().getAxisX();
    }

    /**
     * Converts a coord from view coords to data coords.
     */
    public double viewToData(double dispX)
    {
        Insets ins = getInsetsAll();
        Intervals intervals = getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();
        double dispMin = ins.left;
        double dispMax = dispMin + getWidth() - ins.getWidth();
        return dataMin + (dispX - dispMin)/(dispMax - dispMin)*(dataMax - dataMin);
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return 200;
    }

    /**
     * Override to calculate from labels and ticks.
     */
    protected double getPrefHeightImpl(double aW)
    {
        double ticksH = getTickLabels()[0].getStringHeight();
        double titleH = _titleView.getPrefHeight();
        return AXIS_MARGIN + ticksH + TITLE_TICKS_SPACING + titleH + AXIS_MARGIN;
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        // Get area bounds
        double areaW = getWidth();

        // Set TitleView bounds
        double ticksH = getTickLabels()[0].getStringHeight();
        double titleW = _titleView.getPrefWidth();
        double titleH = _titleView.getPrefHeight();
        double titleX = Math.round((areaW - titleW)/2);
        double titleY = AXIS_MARGIN + ticksH + TITLE_TICKS_SPACING;
        _titleView.setBounds(titleX, titleY, titleW, titleH);
    }

    /**
     * Layout TickLabels.
     */
    protected void layoutTickLabels()
    {
        // Get TickLabels and Intervals
        StringBox[] tickLabels = getTickLabels();
        Intervals intervals = getIntervals();
        int count = intervals.getCount();
        double delta = intervals.getDelta();

        // Get whether this is Axis for Bar chart
        boolean isBar = getChart().getType()==ChartType.BAR;

        // Iterate over tick labels and set location
        for (int i=0; i<count; i++) {

            // Get X in data and display coords and draw tick line
            double dataX = intervals.getInterval(i);
            double dispX = Math.round(dataToViewX(dataX));

            // If Bar, handle special: Shift labels to mid interval and skip last
            if (isBar) {
                if (i + 1 == count)
                    break;
                dataX = dataX + delta / 2;
                dispX = Math.round(dataToViewX(dataX));
            }

            // If edge div too close to next div, skip
            if (i == 0 || i + 1 == count) {
                double nextX = intervals.getInterval(i == 0 ? 1 : count - 2);
                double delta2 = i == 0 ? (nextX - dataX) : (dataX - nextX);
                if (delta2 < delta * .67) {
                    tickLabels[i].setString("");
                    continue;
                }
            }

            // Get/set TickLabel bounds
            StringBox tickLabel = tickLabels[i];
            double tickW = tickLabel.getStringWidth();
            double tickH = tickLabel.getStringHeight();
            double tickX = dispX - Math.round(tickW / 2);
            tickLabel.setRect(tickX, AXIS_MARGIN, tickW, tickH);
        }
    }
}