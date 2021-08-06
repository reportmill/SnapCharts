/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.Side;
import snap.util.ArrayUtils;
import snap.view.RowView;
import snap.view.ViewProxy;
import snapcharts.model.*;

/**
 * An AxisView subclass for AxisY.
 */
public class AxisViewY extends AxisView<AxisY> {
    
    // The Title view wrapper (to allow rotation)
    private WrapView  _titleViewBox;

    // A RowView to hold LegendEntryViews if AxisY.ShowLegendGraphic
    private RowView  _legendGraphicRowView;

    // A WrapView to hold rotated LegendGraphicRowView
    private WrapView  _legendGraphicBox;

    // The AxisType
    private AxisType  _axisType;

    /**
     * Constructor.
     */
    public AxisViewY(AxisType anAxisTypeY)
    {
        super();
        setVertical(true);

        _axisType = anAxisTypeY;

        // Create configure TitleView
        _titleView.setRotate(270);
        _titleViewBox = new WrapView(_titleView);
        _titleViewBox.setGrowHeight(true);
        addChild(_titleViewBox, 0);

        // Create/configure LegendGraphicBox
        _legendGraphicRowView = new RowView();
        _legendGraphicRowView.setSpacing(10);
        _legendGraphicRowView.setRotate(270);
        _legendGraphicBox = new WrapView(_legendGraphicRowView);
        _legendGraphicBox.setGrowHeight(true);
        addChild(_legendGraphicBox, 1);

        // Set Padding
        setPadding(0, AXIS_MARGIN, 0, AXIS_MARGIN);
        _tickLabelBox.setGrowHeight(true);
    }

    /**
     * Returns the AxisType.
     */
    public AxisType getType()  { return _axisType; }

    /**
     * Returns the axis.
     */
    public AxisY getAxis()
    {
        return (AxisY) getChart().getAxisForType(_axisType);
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        ViewProxy<?> viewProxy = getViewProxy();
        return RowView.getPrefWidthProxy(viewProxy, aH);
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        // Layout as RowView
        ViewProxy<?> viewProxy = getViewProxy();
        RowView.layoutProxy(viewProxy);
        viewProxy.setBoundsInClient();

        // Layout TickLabels
        layoutTickLabels();
    }

    /**
     * Layout TickLabels.
     */
    private void layoutTickLabels()
    {
        // Get tick labels width
        double ticksW = getTickLabelsMaxWidth();

        // Get TickLabel.Align (if Side RIGHT, align left)
        boolean isRightSide = getAxis().getSide() == Side.RIGHT;
        Pos ticksAlign = isRightSide ? Pos.TOP_LEFT : Pos.TOP_RIGHT;

        // Get TickLabels
        TickLabel[] tickLabels = getTickLabels();

        // Polar stuff
        boolean isPolar = getChartType().isPolarType();
        double shiftY = isPolar ? getY() - getDataView().getY() : 0;

        // Iterate over tick labels and set location
        for (TickLabel tickLabel : tickLabels) {

            // Get Y in data and display coords and draw tick line
            double dataY = tickLabel.getCoord();
            double dispY = Math.round(dataToView(dataY));

            if (isPolar)
                dispY -= shiftY;

            // Get/set TickLabel bounds
            double tickH = tickLabel.getPrefHeight();
            double tickY = dispY - Math.round(tickH / 2);
            tickLabel.setAlign(ticksAlign);
            tickLabel.setBounds(0, tickY, ticksW, tickH);
        }
    }

    /**
     * Returns a ViewProxy of AxisView to layout as RowView.
     */
    private ViewProxy<?> getViewProxy()
    {
        // Create ViewProxy for AxisView
        ViewProxy<?> viewProxy = new ViewProxy<>(this);
        viewProxy.setSpacing(TITLE_TICKS_SPACING);

        // If tick is 'Outside' or 'Accross', adjust padding to accommodate tick inside axis bounds
        Axis axis = getAxis();
        Side axisSide = axis.getSide();
        Axis.TickPos tickPos = axis.getTickPos();
        double tickLength = axis.getTickLength();
        double tickIndent = tickPos == Axis.TickPos.Outside ? tickLength : tickPos == Axis.TickPos.Across ? tickLength / 2 : 0;
        if (tickIndent > 0) {
            Insets padding = viewProxy.getPadding().clone();
            if (axisSide == Side.LEFT)
                padding.right += tickIndent;
            else padding.left += tickIndent;
            viewProxy.setPadding(padding);
        }

        // If RightSide, reverse children
        boolean isRightSide = axisSide == Side.RIGHT;
        viewProxy.setAlign(isRightSide ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        if (isRightSide)
            ArrayUtils.reverse(viewProxy.getChildren());
        return viewProxy;
    }

    /**
     * Override to configure LegendGraphicRowView.
     */
    @Override
    protected void resetView()
    {
        // Do normal version
        super.resetView();

        // Configure LegendGraphicRowView
        AxisY axisY = getAxis();
        boolean showLegendGraphic = axisY.isShowLegendGraphic();
        _legendGraphicBox.setVisible(showLegendGraphic);
        if (showLegendGraphic) {
            _legendGraphicRowView.removeChildren();
            Legend legend = axisY.getChart().getLegend();
            DataSet[] dataSets = getDataSetList().getDataSets();
            for (DataSet dataSet : dataSets) {
                if (dataSet.getAxisTypeY() == getAxisType()) {
                    LegendEntryView legendEntryView = new LegendEntryView(legend, dataSet);
                    legendEntryView.setShowText(false);
                    legendEntryView.getGraphic().setPrefWidth(40);
                    _legendGraphicRowView.addChild(legendEntryView);
                }
            }
        }
    }
}