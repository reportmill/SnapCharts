package snap.charts;
import java.util.List;

import snap.geom.Insets;
import snap.geom.Rect;
import snap.gfx.*;
import snap.view.View;

/**
 * A view to paint Chart X Axis.
 */
public class ChartXAxis extends View {

    // The ChartArea
    ChartArea         _chartArea;
    
    // The categories
    List <String>     _categories;
    
    // The x/y offset of labels
    double            _labelsX, _labelsY = 8;
    
    // The length of the vertical tick lines drawn from the X axis down twards it's labels and title
    double            _tickLength = 10;

    // Constants
    static Color           AXIS_LINES_COLOR = Color.LIGHTGRAY;
    static Color           AXIS_LABELS_COLOR = Color.GRAY;

/**
 * Creates the ChartXAxis.
 */
public ChartXAxis()
{
    setFont(Font.Arial12);
}

/**
 * Returns the categories.
 */
public List <String> getCategories()  { return _categories; }

/**
 * Sets the categories.
 */
public void setCategories(List <String> theStrings)
{
    _categories = theStrings;
}

/**
 * Returns the x of labels.
 */
public double getLabelsX()  { return _labelsX; }

/**
 * Sets the x of labels.
 */
public void setLabelsX(double aValue)  { _labelsX = aValue; }

/**
 * Returns the y offset of labels.
 */
public double getLabelsY()  { return _labelsY; }

/**
 * Returns the y offset of labels.
 */
public void setLabelsY(double aValue)  { _labelsY = aValue; }

/**
 * Returns the length of the vertical tick lines drawn from the X axis down twards it's labels and title.
 */
public double getTickLength()  { return _tickLength; }

/**
 * Sets the length of the vertical tick lines drawn from the X axis down twards it's labels and title.
 */
public void setTickLength(double aValue)  { _tickLength = aValue; }

/**
 * Returns the label string at given index.
 */
public String getLabel(int anIndex)
{
    // If categories exist, return the category string at index
    if(_categories!=null && anIndex<_categories.size())
        return _categories.get(anIndex);
        
    // Otherwise, return string for start value and index
    DataSet dset = _chartArea.getDataSet();
    int val = dset.getSeriesStart() + anIndex;
    return String.valueOf(val);
}

/**
 * Paints chart x axis.
 */
protected void paintFront(Painter aPntr)
{
    Insets ins = _chartArea.getInsetsAll();
    paintAxis(aPntr, ins.left, getWidth() - ins.getWidth(), getHeight());
}

/**
 * Paints chart x axis.
 */
protected void paintAxis(Painter aPntr, double aX, double aW, double aH)
{
    // If Bar chart, go there instead
    if(_chartArea instanceof ChartAreaBar) { paintAxisBar(aPntr, 0, getWidth(), aH); return; }
    
    // Set font, color
    Font font = getFont(); aPntr.setFont(font);
    double labelsYOff = getLabelsY(), fontHeight = Math.ceil(font.getAscent());
    double labelY = labelsYOff + fontHeight;
    
    // Get number of data points
    int pointCount = _chartArea.getPointCount();
    double sectionW = aW/(pointCount-1);
    double tickLen = getTickLength();
    
    // Draw axis ticks
    aPntr.setColor(AXIS_LINES_COLOR); aPntr.setStroke(Stroke.Stroke1);
    for(int i=0;i<pointCount;i++) {
        double tickX = Math.round(aX + i*sectionW);
        aPntr.drawLine(tickX, 0, tickX, tickLen);
    }
        
    // Draw axis labels
    aPntr.setColor(AXIS_LABELS_COLOR);
    for(int i=0;i<pointCount;i++) {
        String str = getLabel(i);
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
    Font font = getFont(); aPntr.setFont(font);
    double labelsYOff = getLabelsY(), fontHeight = Math.ceil(font.getAscent());
    double labelY = labelsYOff + fontHeight;
    
    // Get number of data points
    int pointCount = _chartArea.getPointCount();
    double sectionW = aW/pointCount;
    double tickLen = getTickLength();
    
    // Draw axis ticks
    aPntr.setColor(AXIS_LINES_COLOR); aPntr.setStroke(Stroke.Stroke1);
    for(int i=0;i<pointCount+1;i++) {
        double tickX = Math.round(aX + i*sectionW);
        if(tickX<=0) tickX += .5; else if(tickX>=aW) tickX -= .5;
        aPntr.drawLine(tickX, 0, tickX, tickLen);
    }
        
    // Draw axis labels
    aPntr.setColor(AXIS_LABELS_COLOR);
    for(int i=0;i<pointCount;i++) {
        String str = getLabel(i);
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
    double yoff = getLabelsY();
    double tickLen = getTickLength();
    double ph = Math.max(labelsHeight + yoff, tickLen);
    return ph;
}

}