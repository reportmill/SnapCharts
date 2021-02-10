package snapcharts.views;
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
        // Get ticks height
        Font font = getFont();
        int ascent = (int) Math.ceil(font.getAscent());
        int descent = (int) Math.ceil(font.getDescent());
        double ticksH = ascent + descent;

        // Get TitleView height
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

        // Get ticks height
        Font font = getFont();
        int ascent = (int) Math.ceil(font.getAscent());
        int descent = (int) Math.ceil(font.getDescent());
        double ticksH = ascent + descent;

        // Set TitleView bounds
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
        boolean log = isLog();

        // Get whether this is Axis for Bar chart
        boolean isBar = getChart().getType()==ChartType.BAR;
        boolean isPolar = getChart().getType()==ChartType.POLAR;
        double shiftX = isPolar ? getX() - getDataView().getX() : 0;

        // Iterate over tick labels and set location
        for (int i=0; i<count; i++) {

            // Get X in data and display coords and draw tick line
            double dataX = intervals.getInterval(i);
            double dispX = Math.round(dataToView(dataX));

            if (isPolar)
                dispX -= shiftX;

            // If Bar, handle special: Shift labels to mid interval and skip last
            if (isBar) {
                if (i + 1 == count) {
                    tickLabels[i].setString("");
                    break;
                }
                dataX = dataX + delta / 2;
                dispX = Math.round(dataToView(dataX));
            }

            // If edge div too close to next div, skip
            else if (!log && (i == 0 || i + 1 == count)) {
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