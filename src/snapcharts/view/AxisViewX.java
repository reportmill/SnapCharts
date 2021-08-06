/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.Insets;
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
        ColView.layoutProxy(viewProxy);
        viewProxy.setBoundsInClient();

        // Layout TickLabels
        layoutTickLabels();
    }

    /**
     * Returns a ViewProxy for AxisView to layout as ColView.
     */
    private ViewProxy<?> getViewProxy()
    {
        // Create ViewProxy for AxisView
        ViewProxy<?> viewProxy = new ViewProxy<>(this);
        viewProxy.setAlign(Pos.BOTTOM_CENTER);
        ArrayUtils.reverse(viewProxy.getChildren());
        viewProxy.setSpacing(TITLE_TICKS_SPACING);

        // If tick is 'Outside' or 'Accross', adjust padding to accommodate tick inside axis bounds
        Axis axis = getAxis();
        Side axisSide = axis.getSide();
        Axis.TickPos tickPos = axis.getTickPos();
        double tickLength = axis.getTickLength();
        double tickIndent = tickPos == Axis.TickPos.Outside ? tickLength : tickPos == Axis.TickPos.Across ? tickLength / 2 : 0;
        if (tickIndent > 0) {
            Insets padding = viewProxy.getPadding().clone();
            if (axisSide == Side.TOP)
                padding.bottom += tickIndent;
            else padding.top += tickIndent;
            viewProxy.setPadding(padding);
        }

        // Return ViewProxy
        return viewProxy;
    }

    /**
     * Layout TickLabels.
     */
    private void layoutTickLabels()
    {
        // Get TickLabels and info
        TickLabel[] tickLabels = getTickLabels();
        boolean isPolar = getChartType().isPolarType();
        double shiftX = isPolar ? getX() - getDataView().getX() : 0;

        // Iterate over tick labels and set location
        for (TickLabel tickLabel : tickLabels) {

            // Get X in data and display coords and draw tick line
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