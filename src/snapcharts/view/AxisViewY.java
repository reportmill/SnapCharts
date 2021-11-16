/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.Side;
import snap.util.ArrayUtils;
import snap.view.Cursor;
import snap.view.RowView;
import snap.view.RowViewProxy;
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

    // Default Title Rotation
    private static final double DEFAULT_TITLE_ROTATION = -90;

    /**
     * Constructor.
     */
    public AxisViewY(AxisType anAxisTypeY)
    {
        super();
        setVertical(true);
        setCursor(Cursor.N_RESIZE);

        _axisType = anAxisTypeY;

        // Create configure TitleView
        _titleView.setRotate(DEFAULT_TITLE_ROTATION);
        _titleViewBox = new WrapView(_titleView);
        _titleViewBox.setGrowHeight(true);
        addChild(_titleViewBox, 0);

        // Create/configure LegendGraphicBox
        _legendGraphicRowView = new RowView();
        _legendGraphicRowView.setSpacing(10);
        _legendGraphicRowView.setRotate(DEFAULT_TITLE_ROTATION);
        _legendGraphicBox = new WrapView(_legendGraphicRowView);
        _legendGraphicBox.setGrowHeight(true);
        addChild(_legendGraphicBox, 1);

        // Config TickLabelBox for vertical
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
        RowViewProxy<?> viewProxy = getViewProxy();
        return viewProxy.getPrefWidth(aH);
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        // Layout as RowView
        RowViewProxy<?> viewProxy = getViewProxy();
        viewProxy.layoutView();

        // Layout TickLabels
        layoutTickLabels();
    }

    /**
     * Returns a ViewProxy of AxisView to layout as RowView.
     */
    protected RowViewProxy<?> getViewProxy()
    {
        // Create ViewProxy for AxisView
        RowViewProxy<?> viewProxy = new RowViewProxy<>(this);

        // If tick is 'Outside' or 'Across', adjust padding to accommodate tick inside axis bounds
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
     * Layout TickLabels.
     */
    private void layoutTickLabels()
    {
        // Get width of TickBox
        double tickBoxW = _tickLabelBox.getWidth();

        // Get TickLabel.Align (if Side RIGHT, align left)
        Axis axis = getAxis();
        boolean isRightSide = axis.getSide() == Side.RIGHT;

        // Get TickLabels
        TickLabel[] tickLabels = getTickLabels();

        // Polar stuff
        boolean isPolar = getChartType().isPolarType();
        double shiftY = isPolar ? getY() - getDataView().getY() : 0;

        // Get tick rotation
        double tickAngle = axis.getTickLabelRotation();
        double tickRot = -tickAngle;

        // Iterate over tick labels and set location
        for (TickLabel tickLabel : tickLabels) {

            // Get Y in data and display coords and draw tick line
            double dataY = tickLabel.getCoord();
            double dispY = Math.round(dataToView(dataY));

            if (isPolar)
                dispY -= shiftY;

            // Get/set TickLabel bounds
            double tickW = tickLabel.getPrefWidth();
            double tickH = tickLabel.getPrefHeight();
            double tickX = isRightSide ? 0 : tickBoxW - tickW;
            double tickY = dispY - Math.round(tickH / 2);
            tickLabel.setBounds(tickX, tickY, tickW, tickH);

            // Set tick label rotation
            TickLabelBox.setTickLabelRotation(this, tickLabel, tickRot);
        }
    }

    /**
     * Override to configure LegendGraphicRowView.
     */
    @Override
    protected void resetView()
    {
        // Do normal version
        super.resetView();

        // Configure TitleView
        AxisY axisY = getAxis();
        double titleRotation = DEFAULT_TITLE_ROTATION + axisY.getTitleRotation();
        _titleView.setRotate(titleRotation);

        // Configure TitleViewBox
        Pos align = axisY.getAlign();
        _titleViewBox.setAlign(align);

        // Configure LegendGraphicBox, LegendGraphicRowView
        boolean showLegendGraphic = axisY.isShowLegendGraphic();
        _legendGraphicBox.setVisible(showLegendGraphic);
        if (showLegendGraphic) {

            // Set LegendGraphicBox align
            _legendGraphicBox.setAlignY(axisY.getAlignY());

            // Remove/add LegendEntryViews to LegendGraphicRowView
            _legendGraphicRowView.removeChildren();
            Legend legend = axisY.getChart().getLegend();
            Trace[] traces = getTraceList().getTraces();
            for (Trace trace : traces) {
                if (trace.getAxisTypeY() == getAxisType()) {
                    LegendEntryView legendEntryView = new LegendEntryView(legend, trace);
                    legendEntryView.setShowText(false);
                    legendEntryView.getGraphic().setPrefWidth(40);
                    _legendGraphicRowView.addChild(legendEntryView);
                }
            }
        }
    }
}