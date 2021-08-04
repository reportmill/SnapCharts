package snapcharts.view;
import snap.geom.Line;
import snap.geom.Pos;
import snap.geom.Side;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.view.ColView;
import snap.view.ViewProxy;
import snapcharts.model.Axis;
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

        // Set Padding
        setPadding(AXIS_MARGIN, 0, AXIS_MARGIN, 0);
        _tickLabelBox.setGrowWidth(true);
    }

    /**
     * Returns the axis.
     */
    public AxisX getAxis()
    {
        return getChart().getAxisX();
    }

    /**
     * Override to paint axis line and ticks.
     */
    @Override
    protected void paintAxisLineAndTicks(Painter aPntr)
    {
        // Get axis line style properties
        Axis axis = getAxis();
        Color lineColor = axis.getLineColor();
        Stroke lineStroke = axis.getLineStroke();

        // Get Axis line
        double areaX = 0;
        double areaMaxX = areaX + getWidth();
        double lineY = getAxis().getSide() == Side.TOP ? getHeight() : 0;
        Line axisLine = new Line(areaX, lineY, areaMaxX, lineY);

        // Paint Axis line
        aPntr.setColor(lineColor);
        aPntr.setStroke(lineStroke);
        aPntr.draw(axisLine);
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
        ViewProxy<?> viewProxy = getViewProxy();
        return ColView.getPrefHeightProxy(viewProxy, aW);
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        // Layout as ColView
        ViewProxy<?> viewProxy = getViewProxy();
        ColView.layoutProxy(viewProxy, false);
        viewProxy.setBoundsInClient();

        // Layout TickLabels
        layoutTickLabels();
    }

    /**
     * Returns a ViewProxy for AxisView to layout as ColView.
     */
    private ViewProxy<?> getViewProxy()
    {
        ViewProxy<?> viewProxy = new ViewProxy<>(this);
        viewProxy.setAlign(Pos.BOTTOM_CENTER);
        ArrayUtils.reverse(viewProxy.getChildren());
        viewProxy.setSpacing(TITLE_TICKS_SPACING);
        return viewProxy;
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
            tickLabel.setBounds(tickX, 0, tickW, tickH);
        }
    }
}