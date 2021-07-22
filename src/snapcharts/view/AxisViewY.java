package snapcharts.view;
import snap.geom.Pos;
import snap.geom.Side;
import snap.util.ArrayUtils;
import snap.view.RowView;
import snap.view.ViewProxy;
import snapcharts.model.AxisType;
import snapcharts.model.AxisY;

/**
 * An AxisView subclass for AxisY.
 */
public class AxisViewY extends AxisView {
    
    // The Title view wrapper (to allow rotation)
    private WrapView  _titleViewBox;

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
     * Returns a ViewProxy of AxisView to layout as RowView.
     */
    private ViewProxy<?> getViewProxy()
    {
        ViewProxy<?> viewProxy = new ViewProxy<>(this);
        viewProxy.setSpacing(TITLE_TICKS_SPACING);

        // If RightSide, reverse children
        AxisY axisY = getAxis();
        boolean isRightSide = axisY.getSide() == Side.RIGHT;
        viewProxy.setAlign(isRightSide ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        if (isRightSide)
            ArrayUtils.reverse(viewProxy.getChildren());
        return viewProxy;
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        ViewProxy viewProxy = getViewProxy();
        return RowView.getPrefWidthProxy(viewProxy, aH);
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        // Layout as RowView
        ViewProxy viewProxy = getViewProxy();
        RowView.layoutProxy(viewProxy, false);
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
        int tickLabelCount = tickLabels.length;

        // Polar stuff
        boolean isPolar = getChartType().isPolarType();
        double shiftY = isPolar ? getY() - getDataView().getY() : 0;

        // Iterate over tick labels and set location
        for (int i=0; i<tickLabelCount; i++) {

            // Get Y in data and display coords and draw tick line
            TickLabel tickLabel = tickLabels[i];
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
}