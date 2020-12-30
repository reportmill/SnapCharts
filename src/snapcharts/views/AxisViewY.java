package snapcharts.views;
import snap.geom.Insets;
import snap.gfx.*;
import snap.view.*;
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
    
    /**
     * Constructor.
     */
    public AxisViewY(AxisType anAxisTypeY)
    {
        super();

        _axisType = anAxisTypeY;

        enableEvents(MousePress);

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
     * Returns the distance between the title and axis labels.
     */
    public double getTitleMargin()  { return getAxis().getTitleMargin(); }

    /**
     * Returns the additional offset of title.
     */
    public double getTitleX()  { return getAxis().getTitleX(); }

    /**
     * Returns the additional offset of title.
     */
    public double getTitleY()  { return getAxis().getTitleY(); }

    /**
     * Returns the distance between axis labels left edge and axis.
     */
    public double getLabelsOffset()  { return getMaxLabelWidth() + getLabelsMargin(); }

    /**
     * Returns the distance between axis labels right edge and the axis.
     */
    public double getLabelsMargin()  { return getAxis().getLabelsMargin(); }

    /**
     * Converts a value from view coords to data coords.
     */
    public double viewToData(double dispY)
    {
        Insets ins = getInsetsAll();
        Intervals intervals = getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();
        double dispMin = ins.top;
        double dispMax = dispMin + getHeight() - ins.getHeight();
        return dataMax - (dispY - dispMin)/(dispMax - dispMin)*(dataMax - dataMin);
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
        double titlePad = title!=null && title.length()>0 ? getTitleMargin() : 0;
        _titleViewBox.setPadding(0, titlePad, 0, 0);
    }

    /**
     * Paints chart y axis.
     */
    protected void paintFront(Painter aPntr)
    {
        Insets ins = _dataView.getInsetsAll();
        double viewW = getWidth();
        double viewH = getHeight();
        double axisW = getLabelsOffset();
        double axisX = viewW - axisW;
        paintAxis(aPntr, axisX, ins.top, axisW, viewH - ins.getHeight());
    }

    /**
     * Paints chart y axis.
     */
    protected void paintAxis(Painter aPntr, double aX, double aY, double aW, double aH)
    {
        // Set font, color
        Font font = Font.Arial12;
        aPntr.setFont(font);
        aPntr.setColor(AXIS_LABELS_COLOR);

        // Get intervals
        Intervals intervals = getIntervals();
        int count = intervals.getCount();
        double delta = intervals.getDelta();
        double marginx = getLabelsMargin();

        // Set color/stroke for axis ticks
        aPntr.setColor(AXIS_LABELS_COLOR);
        aPntr.setStroke(Stroke.Stroke1);

        //
        double labelYShift = Math.round((font.getAscent() - font.getDescent())/2d);

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

            // Get label
            String str = getLabelStringForValueAndDelta(dataY, delta);
            double strW = font.getStringAdvance(str);
            double labelX = aX + aW - strW - marginx;
            double labelY = dispY + labelYShift;
            aPntr.drawString(str, labelX, labelY);
        }
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        double titleOff = getTitleOffset() - getTitleX();
        double labelsOff = getLabelsOffset();
        double prefW = Math.max(titleOff, labelsOff);
        return prefW;
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        double viewH = getHeight();
        double titleOff = getTitleOffset() - getTitleX();
        double labelsOff = getLabelsOffset();
        double titleW = _titleViewBox.getPrefWidth();
        double titleX = titleOff>labelsOff ? 0 : labelsOff - titleOff;
        _titleViewBox.setBounds(titleX, getTitleY(), titleW, viewH);
    }

    /**
     * Returns the max label width.
     */
    protected double getMaxLabelWidth()
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

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MousePress: Toggle AxisY.ZeroRequired
        if (anEvent.isMousePress())
            getChart().getAxisY().setZeroRequired(!getChart().getAxisY().isZeroRequired());
    }
}