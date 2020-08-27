package snapcharts.views;
import snap.geom.Insets;
import snap.geom.Rect;
import snap.gfx.*;
import snap.view.StringView;
import snapcharts.model.AxisX;
import snapcharts.model.ChartType;
import snapcharts.model.Intervals;

/**
 * A view to paint Chart X Axis.
 */
public class AxisViewX extends AxisView {

    // Constants
    private final int AXIS_TITLE_MARGIN = 8;

    /**
     * Creates the ChartXAxis.
     */
    public AxisViewX()
    {
        // Basic configure
        setFont(Font.Arial12);

        // Create configure TitleView
        _titleView = new StringView();
        _titleView.setTextFill(Color.GRAY);
        _titleView.setFont(Font.Arial12.getBold().deriveFont(13));
        addChild(_titleView);
    }

    /**
     * Returns the axis.
     */
    public AxisX getAxis()
    {
        return getChart().getAxisX();
    }

    /**
     * Called to reset view from Chart.
     */
    protected void resetView()
    {
        // Reset title
        String title = getAxis().getTitle();
        _titleView.setText(title);
        //double titlePad = title!=null && title.length()>0 ? getTitleMargin() : 0;
        //_titleView.setPadding(0, titlePad, 0, 0);
    }

    /**
     * Paints chart x axis.
     */
    protected void paintFront(Painter aPntr)
    {
        paintAxis(aPntr);
    }

    /**
     * Paints chart x axis.
     */
    protected void paintAxis(Painter aPntr)
    {
        // Set font
        Font font = getFont();
        aPntr.setFont(font);

        // Get axis and info
        AxisX axis = getAxis();
        double labelsYOff = axis.getLabelsY();
        double fontHeight = Math.ceil(font.getAscent());
        double labelY = labelsYOff + fontHeight;
        double tickLen = axis.getTickLength();

        // Get Intervals info
        Intervals intervals = _dataView.getIntervalsX();
        int count = intervals.getCount();

        // Get whether this is Axis for Bar chart
        boolean isBar = _dataView.getType()==ChartType.BAR;

        // Set color/stroke for axis ticks
        aPntr.setColor(AXIS_LINES_COLOR);
        aPntr.setStroke(Stroke.Stroke1);

        // Iterate over intervals
        for (int i=0; i<count; i++) {

            // Get X in data and display coords and draw tick line
            double dataX = intervals.getInterval(i);
            double dispX = Math.round(dataToViewX(dataX));
            aPntr.drawLine(dispX, 0, dispX, tickLen);

            // If Bar, handle special: Shift labels to mid interval and skip last
            if (isBar) {
                if (i+1==count)
                    break;
                dataX = intervals.getInterval(i) + intervals.getDelta()/2;
                dispX = Math.round(dataToViewX(dataX));
            }

            // Get label
            String str = axis.getLabel(i);
            Rect strBnds = aPntr.getStringBounds(str);
            double x = dispX - Math.round(strBnds.getMidX());
            aPntr.drawString(str, x, labelY);
        }
    }

    /**
     * Returns the bounds of the Tick Labels.
     */
    protected Rect getTickLabelsBounds()
    {
        double labelsHeight = Math.ceil(getFont().getLineHeight());
        AxisX axis = getAxis();
        double yoff = axis.getLabelsY();
        double tickLen = axis.getTickLength();
        double ph = Math.max(labelsHeight + yoff, tickLen);
        return new Rect(0, 0, getWidth(), ph);
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
        Rect tickLabelsBounds = getTickLabelsBounds();
        double titleH = _titleView.getPrefHeight();
        return tickLabelsBounds.getMaxY() + AXIS_TITLE_MARGIN + titleH + AXIS_TITLE_MARGIN;
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        // Get size of this view
        double parW = getWidth();
        double parH = getHeight();

        // Get bounds of tick labels
        Rect tickLabelsBounds = getTickLabelsBounds();

        // Get TitleView width, height, X
        double titleW = _titleView.getPrefWidth();
        double titleH = _titleView.getPrefHeight();
        double titleX = Math.round((parW - titleW)/2);
        double titleY = tickLabelsBounds.getMaxY() + AXIS_TITLE_MARGIN;

        // Set TitleView bounds
        _titleView.setBounds(titleX, titleY, titleW, titleH);
    }
}