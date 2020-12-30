package snapcharts.views;
import snap.geom.Insets;
import snap.geom.Rect;
import snap.gfx.*;
import snapcharts.model.AxisX;
import snapcharts.model.ChartType;
import snapcharts.model.Intervals;

/**
 * A view to paint Chart X Axis.
 */
public class AxisViewX<T extends AxisX> extends AxisView<T> {

    // Constants
    private final int AXIS_TITLE_MARGIN = 8;

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
     * Called to reset view from Chart.
     */
    protected void resetView()
    {
        // Do basic axis update
        super.resetView();
    }

    /**
     * Paints chart x axis.
     */
    protected void paintFront(Painter aPntr)
    {
        paintAxis(aPntr);
    }

    /**
     * Paint axis.
     */
    protected void paintAxis(Painter aPntr)
    {
        // Set font
        Font font = getFont();
        aPntr.setFont(font);

        // Get axis and info
        AxisX axis = getAxis();
        double labelsYOff = axis.getLabelsY();
        double fontHeight = Math.ceil(font.getAscent());
        double labelY = labelsYOff + fontHeight;
        double tickLen = axis.getTickLength();

        // Get Intervals info
        Intervals intervals = getIntervals();
        int count = intervals.getCount();
        double delta = intervals.getDelta();

        // Get whether this is Axis for Bar chart
        boolean isBar = getChart().getType()==ChartType.BAR;

        // Set color/stroke for axis ticks
        aPntr.setColor(AXIS_LABELS_COLOR);
        aPntr.setStroke(Stroke.Stroke1);

        // Iterate over intervals
        for (int i=0; i<count; i++) {

            // Get X in data and display coords and draw tick line
            double dataX = intervals.getInterval(i);
            double dispX = Math.round(dataToViewX(dataX));

            // If Bar, handle special: Shift labels to mid interval and skip last
            if (isBar) {
                if (i+1==count)
                    break;
                dataX = dataX + delta/2;
                dispX = Math.round(dataToViewX(dataX));
            }

            // If edge div too close to next div, skip
            if (i==0 || i+1==count) {
                double nextX = intervals.getInterval(i==0 ? 1 : count-2);
                double delta2 = i==0 ? (nextX - dataX) : (dataX - nextX);
                if (delta2<delta*.67)
                    continue;
            }

            // Get label
            String str = getLabelStringForValueAndDelta(dataX, delta);
            double strW = font.getStringAdvance(str);
            double labelX = dispX - Math.round(strW/2);
            aPntr.drawString(str, labelX, labelY);
        }
    }

    /**
     * Returns the bounds of the Tick Labels.
     */
    protected Rect getTickLabelsBounds()
    {
        double viewW = getWidth();
        Font font = getFont();
        double labelsH = Math.ceil(font.getLineHeight());
        AxisX axis = getAxis();
        double yoff = axis.getLabelsY();
        double tickLen = axis.getTickLength();
        double prefH = Math.max(labelsH + yoff, tickLen);
        return new Rect(0, 0, viewW, prefH);
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
        Rect tickLabelsBounds = getTickLabelsBounds();
        double titleH = _titleView.getPrefHeight();
        return tickLabelsBounds.getMaxY() + AXIS_TITLE_MARGIN + titleH + AXIS_TITLE_MARGIN;
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        // Get size of this view
        double viewW = getWidth();

        // Get bounds of tick labels
        Rect tickLabelsBounds = getTickLabelsBounds();

        // Get TitleView width, height, X
        double titleW = _titleView.getPrefWidth();
        double titleH = _titleView.getPrefHeight();
        double titleX = Math.round((viewW - titleW)/2);
        double titleY = tickLabelsBounds.getMaxY() + AXIS_TITLE_MARGIN;

        // Set TitleView bounds
        _titleView.setBounds(titleX, titleY, titleW, titleH);
    }
}