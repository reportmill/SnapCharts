package snapcharts.views;
import snap.geom.Insets;
import snap.geom.Rect;
import snap.gfx.*;
import snap.view.StringView;
import snapcharts.model.AxisX;

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
        Insets ins = _dataView.getInsetsAll();
        paintAxis(aPntr, ins.left, getWidth() - ins.getWidth(), getHeight());
    }

    /**
     * Paints chart x axis.
     */
    protected void paintAxis(Painter aPntr, double aX, double aW, double aH)
    {
        // If Bar chart, go there instead
        if (_dataView instanceof DataViewBar) { paintAxisBar(aPntr, 0, getWidth(), aH); return; }

        // Set font, color
        Font font = getFont();
        aPntr.setFont(font);

        //
        AxisX axis = getAxis();
        double labelsYOff = axis.getLabelsY();
        double fontHeight = Math.ceil(font.getAscent());
        double labelY = labelsYOff + fontHeight;

        // Get number of data points
        int pointCount = _dataView.getPointCount();
        double sectionW = aW/(pointCount-1);
        double tickLen = axis.getTickLength();

        // Draw axis ticks
        aPntr.setColor(AXIS_LINES_COLOR); aPntr.setStroke(Stroke.Stroke1);
        for (int i=0;i<pointCount;i++) {
            double tickX = Math.round(aX + i*sectionW);
            aPntr.drawLine(tickX, 0, tickX, tickLen);
        }

        // Draw axis labels
        aPntr.setColor(AXIS_LABELS_COLOR);
        for (int i=0;i<pointCount;i++) {
            String str = axis.getLabel(i);
            Rect strBnds = aPntr.getStringBounds(str);
            double lx = aX + sectionW*i;
            double x = lx - Math.round(strBnds.getMidX());
            aPntr.drawString(str, x, labelY);
        }
    }

    /**
     * Paints chart x axis.
     */
    protected void paintAxisBar(Painter aPntr, double aX, double aW, double aH)
    {
        // Set font, color
        Font font = getFont();
        aPntr.setFont(font);
        AxisX axis = getAxis();
        double labelsYOff = axis.getLabelsY();

        double fontHeight = Math.ceil(font.getAscent());
        double labelY = labelsYOff + fontHeight;

        // Get number of data points
        int pointCount = _dataView.getPointCount();
        double sectionW = aW/pointCount;
        double tickLen = axis.getTickLength();

        // Draw axis ticks
        aPntr.setColor(AXIS_LINES_COLOR);
        aPntr.setStroke(Stroke.Stroke1);
        for (int i=0;i<pointCount+1;i++) {
            double tickX = Math.round(aX + i*sectionW);
            if (tickX<=0) tickX += .5;
            else if (tickX>=aW) tickX -= .5;
            aPntr.drawLine(tickX, 0, tickX, tickLen);
        }

        // Draw axis labels
        aPntr.setColor(AXIS_LABELS_COLOR);
        for (int i=0;i<pointCount;i++) {
            String str = axis.getLabel(i);
            Rect strBnds = aPntr.getStringBounds(str);
            double lx = aX + sectionW*i + sectionW/2;
            double x = lx - strBnds.getMidX(); x = Math.round(x);
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