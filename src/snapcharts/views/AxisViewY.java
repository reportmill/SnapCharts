package snapcharts.views;
import snap.geom.Insets;
import snap.gfx.*;
import snapcharts.model.AxisType;
import snapcharts.model.AxisY;
import snapcharts.model.Intervals;

/**
 * A view to paint Chart Y Axis.
 */
public class AxisViewY extends AxisView {
    
    // The Title view wrapper (to allow rotation)
    private WrapView  _titleViewBox;

    // The AxisType
    private AxisType  _axisType;

    // Constants
    private final int TITLE_MARGIN = 8;
    private final int TICKS_MARGIN = 5;
    private final int TITLE_TICKS_SPACING = 5;

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
     * Returns the distance from title left edge to axis.
     */
    public double getTitleOffset()
    {
        return _titleViewBox.getPrefWidth() + getLabelsOffset();
    }

    /**
     * Returns the distance between axis labels left edge and axis.
     */
    public double getLabelsOffset()  { return getTickLabelsMaxWidth() + TICKS_MARGIN; }

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
     * Called to reset view from Chart.
     */
    protected void resetView()
    {
        // Do basic axis update
        super.resetView();

        // Reset title
        AxisY axis = getAxis();
        String title = axis.getTitle();
        double titlePad = title!=null && title.length()>0 ? TITLE_MARGIN : 0;
        _titleViewBox.setPadding(0, titlePad, 0, 0);
    }

    /**
     * Paints chart y axis.
     */
    protected void paintFront(Painter aPntr)
    {
        // Get area bounds
        double areaX = 0;
        double areaW = getWidth();
        double areaMaxX = areaX + areaW;

        // Set font, color
        Font font = Font.Arial12;
        aPntr.setFont(font);
        aPntr.setColor(AXIS_LABELS_COLOR);

        // Get intervals
        Intervals intervals = getIntervals();
        int count = intervals.getCount();
        double delta = intervals.getDelta();

        // Set color/stroke for axis ticks
        aPntr.setColor(AXIS_LABELS_COLOR);
        aPntr.setStroke(Stroke.Stroke1);

        //
        double tickMidH = Math.round((font.getAscent() - font.getDescent())/2d);

        // Iterate over intervals
        for (int i=0; i<count; i++) {

            // Get Y in data and display coords and draw tick line
            double dataY = intervals.getInterval(i);
            double dispY = Math.round(dataToViewY(dataY));

            // If edge div too close to next div, skip
            if (i==0 || i+1==count) {
                double nextY = intervals.getInterval(i==0 ? 1 : count-2);
                double delta2 = i==0 ? (nextY - dataY) : (dataY - nextY);
                if (delta2<delta*.67)
                    continue;
            }

            // Get string and string width
            String str = getLabelStringForValueAndDelta(dataY, delta);
            double strW = font.getStringAdvance(str);

            // Get tick x/y and draw string
            double tickX = areaMaxX - strW - TICKS_MARGIN;
            double tickY = dispY + tickMidH;
            aPntr.drawString(str, tickX, tickY);
        }
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        double titleW = _titleViewBox.getPrefWidth();
        double ticksW = getTickLabelsMaxWidth();
        return TICKS_MARGIN + titleW + TITLE_TICKS_SPACING + ticksW + TICKS_MARGIN;
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        double areaH = getHeight();
        double titleW = _titleViewBox.getPrefWidth();
        _titleViewBox.setBounds(TICKS_MARGIN, 0, titleW, areaH);
    }

    /**
     * Returns the max label width.
     */
    protected double getTickLabelsMaxWidth()
    {
        // Get intervals
        Intervals intervals = getIntervals();
        int lineCount = intervals.getCount();
        double intervalDelta = intervals.getDelta();
        double intervalMax = intervals.getMax();

        // Get longest text
        String maxText = "";
        for (int i=0; i<lineCount; i++) {
            double lineVal = intervalMax - i*intervalDelta;
            String str = getLabelStringForValueAndDelta(lineVal, intervalDelta);
            if (str.length()>maxText.length())
                maxText = str;
        }

        int textW = (int) Math.ceil(Font.Arial12.getStringAdvance(maxText));
        return textW;
    }
}