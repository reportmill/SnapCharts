package snapcharts.view;
import snap.geom.Pos;
import snap.geom.Side;
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
        addChild(_titleViewBox);
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
        double titleW = _titleViewBox.getPrefWidth();
        double spacing = titleW > 0 ? TITLE_TICKS_SPACING : 0;
        double ticksW = getTickLabelsMaxWidth();
        return AXIS_MARGIN + titleW + spacing + ticksW + AXIS_MARGIN;
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        // Get area bounds
        double areaH = getHeight();

        // Get TitleView bounds
        double titleX = AXIS_MARGIN;
        double titleW = _titleViewBox.getPrefWidth();

        // Get TickLabels X
        double spacing = titleW > 0 ? TITLE_TICKS_SPACING : 0;
        double ticksX = AXIS_MARGIN + titleW + spacing;

        // If on RIGHT, reset titleX
        if (getAxis().getSide() == Side.RIGHT) {
            double areaMaxX = getWidth();
            titleX = areaMaxX - AXIS_MARGIN - titleW;
            ticksX = AXIS_MARGIN;
        }

        // Update TickLabels X
        TickLabel[] tickLabels = getTickLabels();
        for (TickLabel tickLabel : tickLabels)
            tickLabel.setX(ticksX);

        // Set TitleView bounds
        _titleViewBox.setBounds(titleX, 0, titleW, areaH);

        // Layout TickLabels
        layoutTickLabels();
    }

    /**
     * Layout TickLabels.
     */
    private void layoutTickLabels()
    {
        // Get tick labels X
        double titleW = _titleViewBox.getPrefWidth();
        double spacing = titleW > 0 ? TITLE_TICKS_SPACING : 0;
        double ticksX = AXIS_MARGIN + titleW + spacing;
        double ticksW = getTickLabelsMaxWidth();
        Pos ticksAlign = Pos.TOP_RIGHT;

        // If on RIGHT, reset ticksX and align
        Side side = getAxis().getSide();
        if (side == Side.RIGHT) {
            ticksX = AXIS_MARGIN;
            ticksAlign = Pos.TOP_LEFT;
        }

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
            tickLabel.setBounds(ticksX, tickY, ticksW, tickH);
        }
    }
}