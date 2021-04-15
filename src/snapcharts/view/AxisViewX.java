package snapcharts.view;
import snap.gfx.*;
import snapcharts.model.AxisX;

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
        double titleW = Math.min(_titleView.getPrefWidth(), areaW);
        double titleH = _titleView.getPrefHeight();
        double titleX = Math.round((areaW - titleW)/2);
        double titleY = AXIS_MARGIN + ticksH + TITLE_TICKS_SPACING;
        _titleView.setBounds(titleX, titleY, titleW, titleH);

        // Layout TickLabels
        layoutTickLabels();
    }

    /**
     * Layout TickLabels.
     */
    private void layoutTickLabels()
    {
        // Get TickLabels and info
        TickLabel[] tickLabels = getTickLabels();
        int tickLabelCount = tickLabels.length;
        boolean isPolar = getChartType().isPolarType();
        double shiftX = isPolar ? getX() - getDataView().getX() : 0;

        // Iterate over tick labels and set location
        for (int i=0; i<tickLabelCount; i++) {

            // Get X in data and display coords and draw tick line
            TickLabel tickLabel = tickLabels[i];
            double dataX = tickLabel.getCoord();
            double dispX = Math.round(dataToView(dataX));

            if (isPolar)
                dispX -= shiftX;

            // Get/set TickLabel bounds
            double tickW = tickLabel.getPrefWidth();
            double tickH = tickLabel.getPrefHeight();
            double tickX = dispX - Math.round(tickW / 2);
            tickLabel.setBounds(tickX, AXIS_MARGIN, tickW, tickH);
        }
    }
}