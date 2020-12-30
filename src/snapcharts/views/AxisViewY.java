package snapcharts.views;
import snap.geom.Pos;
import snap.geom.Side;
import snap.text.StringBox;
import snapcharts.model.AxisType;
import snapcharts.model.AxisY;
import snapcharts.model.Intervals;

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
     * Converts a value from view coords to data coords.
     */
    public double viewToData(double dispY)
    {
        double areaY = 0;
        double areaH = getHeight();
        Intervals intervals = getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();
        return dataMax - (dispY - areaY)/areaH*(dataMax - dataMin);
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        double titleW = _titleViewBox.getPrefWidth();
        double ticksW = getTickLabelsMaxWidth();
        return AXIS_MARGIN + titleW + TITLE_TICKS_SPACING + ticksW + AXIS_MARGIN;
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        // Get area bounds
        double areaH = getHeight();

        // Set TitleView bounds
        double titleX = AXIS_MARGIN;
        double titleW = _titleViewBox.getPrefWidth();
        _titleViewBox.setBounds(titleX, 0, titleW, areaH);
    }

    /**
     * Layout TickLabels.
     */
    protected void layoutTickLabels()
    {
        // Get tick labels X
        double titleX = AXIS_MARGIN;
        double titleW = _titleViewBox.getPrefWidth();
        double ticksX = titleX + titleW + TITLE_TICKS_SPACING;
        double ticksW = getTickLabelsMaxWidth();

        // Get tick labels align
        Pos ticksAlign = getAxis().getSide() == Side.LEFT ? Pos.TOP_RIGHT : Pos.TOP_LEFT;

        // Get TickLabels and Intervals
        StringBox[] tickLabels = getTickLabels();
        Intervals intervals = getIntervals();
        int count = intervals.getCount();
        double delta = intervals.getDelta();

        // Iterate over tick labels and set location
        for (int i=0; i<count; i++) {

            // Get Y in data and display coords and draw tick line
            double dataY = intervals.getInterval(i);
            double dispY = Math.round(dataToViewY(dataY));

            // If edge div too close to next div, skip
            if (i==0 || i+1==count) {
                double nextY = intervals.getInterval(i==0 ? 1 : count - 2);
                double delta2 = i==0 ? (nextY - dataY) : (dataY - nextY);
                if (delta2<delta*.67) {
                    tickLabels[i].setString("");
                    continue;
                }
            }

            // Get/set TickLabel bounds
            StringBox tickLabel = tickLabels[i];
            double tickH = tickLabel.getStringHeight();
            double tickY = dispY - Math.round(tickH / 2);
            tickLabel.setAlign(ticksAlign);
            tickLabel.setRect(ticksX, tickY, ticksW, tickH);
        }
    }

    /**
     * Returns the max label width.
     */
    protected double getTickLabelsMaxWidth()
    {
        double max = 0;
        for (StringBox tickLabel : getTickLabels())
            max = Math.max(max, tickLabel.getStringWidth());
        return max;
    }
}