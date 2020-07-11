package snapcharts.views;

import snap.geom.Insets;
import snap.geom.Rect;
import snap.gfx.*;
import snap.view.View;
import snapcharts.model.AxisX;
import snapcharts.model.Chart;

/**
 * A view to paint Chart X Axis.
 */
public class AxisViewX extends View {

    // The ChartArea
    DataView _dataView;

    private AxisX  _axis;
    
    // Constants
    static Color           AXIS_LINES_COLOR = Color.LIGHTGRAY;
    static Color           AXIS_LABELS_COLOR = Color.GRAY;

/**
 * Creates the ChartXAxis.
 */
public AxisViewX()
{
    setFont(Font.Arial12);
}

/**
 * Returns the Chart.
 */
public Chart getChart()
{
    return _dataView.getChart();
}

/**
 * Returns the axis.
 */
public AxisX getAxis()
{
    if (_axis!=null) return _axis;
    _axis = getChart().getAxisX();
    return _axis;
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
    if(_dataView instanceof DataViewBar) { paintAxisBar(aPntr, 0, getWidth(), aH); return; }
    
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
    for(int i=0;i<pointCount+1;i++) {
        double tickX = Math.round(aX + i*sectionW);
        if(tickX<=0) tickX += .5; else if(tickX>=aW) tickX -= .5;
        aPntr.drawLine(tickX, 0, tickX, tickLen);
    }
        
    // Draw axis labels
    aPntr.setColor(AXIS_LABELS_COLOR);
    for(int i=0;i<pointCount;i++) {
        String str = axis.getLabel(i);
        Rect strBnds = aPntr.getStringBounds(str);
        double lx = aX + sectionW*i + sectionW/2;
        double x = lx - strBnds.getMidX(); x = Math.round(x);
        aPntr.drawString(str, x, labelY);
    }
}

/**
 * Override to calculate from labels and ticks.
 */
protected double getPrefHeightImpl(double aW)
{
    double labelsHeight = Math.ceil(getFont().getLineHeight());
    AxisX axis = getAxis();
    double yoff = axis.getLabelsY();
    double tickLen = axis.getTickLength();
    double ph = Math.max(labelsHeight + yoff, tickLen);
    return ph;
}

}