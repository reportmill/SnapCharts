/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.Insets;
import snap.geom.Side;
import snap.util.ArrayUtils;
import snap.view.ColViewLayout;
import snap.view.Cursor;
import snap.view.ViewLayout;
import snapcharts.charts.Axis;
import snapcharts.charts.AxisZ;

/**
 * A view to paint Chart Z Axis.
 */
public class AxisViewZ<T extends AxisZ> extends AxisView<T> {

    /**
     * Constructor.
     */
    public AxisViewZ()
    {
        super();

        setCursor(Cursor.E_RESIZE);

        _tickLabelBox.setGrowHeight(true);
    }

    /**
     * Returns the axis.
     */
    public AxisZ getAxis()  { return getChart().getAxisZ(); }

    /**
     * Override to return column layout that also lays out tick and marker labels.
     */
    @Override
    protected ViewLayout<?> getViewLayoutImpl()
    {
        // Create column layout with override to layout tick labels and marker labels
        ViewLayout<?> viewLayout = new ColViewLayout<>(this) {
            @Override
            public void layoutView() {
                super.layoutView();
                layoutTickLabels();
                if (_markersBox.isVisible())
                    layoutMarkerLabels();
            }
        };

        // Get Axis
        Axis axis = getAxis();

        // If MarkersBox is visible, use margins instead
        if (_markersBox.isVisible()) {
            viewLayout.setSpacing(0);
            ViewLayout<?> markersBoxProxy = viewLayout.getChildForClass(MarkersBox.class);
            double axisSpacing = axis.getSpacing();
            markersBoxProxy.setMargin(new Insets(2, 0, axisSpacing - 2, 0));
        }

        // Reverse children (assumes Side == Bottom)
        ArrayUtils.reverse(viewLayout.getChildren());

        // If tick is 'Outside' or 'Across', adjust padding to accommodate tick inside axis bounds
        Side axisSide = axis.getSide();
        Axis.TickPos tickPos = axis.getTickPos();
        double tickLength = axis.getTickLength();
        double tickIndent = tickPos == Axis.TickPos.Outside ? tickLength : tickPos == Axis.TickPos.Across ? tickLength / 2 : 0;
        if (tickIndent > 0) {
            Insets padding = viewLayout.getPadding().clone();
            if (axisSide == Side.TOP)
                padding.bottom += tickIndent;
            else padding.top += tickIndent;
            viewLayout.setPadding(padding);
        }

        // Return
        return viewLayout;
    }

    /**
     * Layout TickLabels.
     */
    private void layoutTickLabels()
    {
        // Get TickLabels and info
        TickLabel[] tickLabels = getTickLabels();
        Axis axis = getAxis();
        boolean isPolar = getTraceType().isPolarType();
        double shiftX = isPolar ? getX() - getContentView().getX() : 0;

        // Get tick rotation
        double tickAngle = axis.getTickLabelRotation();
        double tickRot = -tickAngle;

        // Iterate over tick labels and set location
        for (TickLabel tickLabel : tickLabels) {

            // Get X in data and display coords
            double dataX = tickLabel.getCoord();
            double dispX = Math.round(dataToView(dataX));

            if (isPolar)
                dispX -= shiftX;

            // Get/set TickLabel bounds
            double tickW = tickLabel.getPrefWidth();
            double tickH = tickLabel.getPrefHeight();
            double tickX = dispX - Math.round(tickW / 2);
            double tickY = 0;
            tickLabel.setBounds(tickX, tickY, tickW, tickH);

            // Set tick label rotation
            TickLabelBox.setTickLabelRotation(this, tickLabel, tickRot);
        }
    }

    /**
     * Layout MarkerLabels.
     */
    private void layoutMarkerLabels()
    {
        // Get MarkerLabels and info
        TickLabel[] markerLabels = getMarkerLabels();

        // Iterate over marker labels and set location
        for (TickLabel markerLabel : markerLabels) {

            // Get X in data and display coords
            double dataX = markerLabel.getCoord();
            double dispX = Math.round(dataToView(dataX));

            // Get/set TickLabel bounds
            double tickW = markerLabel.getPrefWidth();
            double tickH = markerLabel.getPrefHeight();
            double tickX = dispX - Math.round(tickW / 2);
            markerLabel.setBounds(tickX, 0, tickW, tickH);
        }
    }
}