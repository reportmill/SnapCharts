package snapcharts.app;
import java.util.List;

import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;

/**
 * A ChartArea subclass to display the contents of bar chart.
 */
public class ChartAreaBar extends ChartArea {
    
    // The ratio of a section used to pad a group of bars
    double             _groupPad = .2;
    
    // The ratio of a group used to pad a bar
    double             _barPad = .1;
    
    // Whether to use colors for each series value instead of series
    boolean            _colorValues;

    // The number of series and values to chart
    int                _seriesCount, _pointCount;
    
    // The cached sections
    Section            _sections[];

/**
 * Creates a ChartAreaBar.
 */
public ChartAreaBar()
{
    setPadding(5,0,0,0); // Add top so top axis line isn't on edge
}

/**
 * Returns the group padding.
 */
public double getGroupPadding()  { return _groupPad; }

/**
 * Sets the group padding.
 */
public void setGroupPadding(double aValue)
{
    _groupPad = aValue;
    clearCache(); repaint();
}

/**
 * Returns the bar padding.
 */
public double getBarPadding()  { return _barPad; }

/**
 * Sets the bar padding.
 */
public void setBarPadding(double aValue)
{
    _barPad = aValue;
    clearCache(); repaint();
}

/**
 * Returns whether to use colors for each series value instead of series.
 */
public boolean isColorValues()  { return _colorValues; }

/**
 * Returns whether to use colors for each series value instead of series.
 */
public void setColorValues(boolean aValue)  { _colorValues = aValue; clearCache(); }

/**
 * Override to clear section/bar cache.
 */
public void setWidth(double aValue)  { super.setWidth(aValue); clearCache(); }

/**
 * Override to clear section/bar cache.
 */
public void setHeight(double aValue)  { super.setHeight(aValue); clearCache(); }

/**
 * Call to clear section/bar cache.
 */
protected void clearCache()  { _sections = null; }

/**
 * Returns the cached section (and section bars) objects.
 */
protected Section[] getSections()
{
    // If recacl not needed, just return
    int seriesCount = getActiveSeries().size(), pointCount = getPointCount();
    if(_sections!=null && _sections.length==pointCount && _seriesCount==seriesCount) return _sections;
    
    // Get ChartAreaBar info
    double cx = 0, cy = 0, cw = getWidth(), ch = getHeight();
    boolean colorSeries = !isColorValues();
    
    // Get number of series, points and section width
    List <DataSeries> seriesList = getActiveSeries();
    _seriesCount = seriesCount;
    _pointCount = getPointCount();
    double sectionWidth = getWidth()/_pointCount;

    // Get group widths
    double groupWidthRatio = 1 - _groupPad*2;
    double groupWidth = groupWidthRatio>=0? groupWidthRatio*sectionWidth : 1;
    double groupPadWidth = (sectionWidth - groupWidth)/2;
    
    // Get width of individual bar (bar count + bar spaces + bar&space at either end)
    double barWidthRatio = 1 - _barPad*2;
    double barWidth = barWidthRatio>=0? barWidthRatio*groupWidth/_seriesCount : 1;
    double barPadWidth = barWidthRatio>=0? _barPad*groupWidth/_seriesCount : 1;
    
    // Create new bars array
    Section sections[] = new Section[pointCount];
    
    // Iterate over sections
    for(int i=0;i<_pointCount;i++) {
        
        // Create/set new section and section.bars
        Section section = sections[i] = new Section(cx + i*sectionWidth, cy, sectionWidth, ch);
        section.bars = new Bar[_seriesCount];
        
        // Iterate over series
        for(int j=0;j<_seriesCount;j++) { DataSeries series = seriesList.get(j);
            DataPoint dataPoint = series.getPoint(i);
            double val = dataPoint.getValueX();
            
            // Draw bar
            Color color = colorSeries? getColor(series.getIndex()) : getColor(i);
            double bx = cx + i*sectionWidth + groupPadWidth + (j*2+1)*barPadWidth + j*barWidth;
            double by = seriesToLocal(i, val).y, bh = cy + ch - by;
            section.bars[j] = new Bar(dataPoint, bx, by, barWidth, bh, color);
        }
    }
    
    // Return sections
    return _sections = sections;
}
    
/**
 * Paints chart.
 */
protected void paintChart(Painter aPntr, double aX, double aY, double aW, double aH)
{
    // Get selected point index (section index)
    DataPoint dataPoint = _chartView.getTargDataPoint();
    int selIndex = dataPoint!=null? dataPoint.getIndex() : -1;
    
    double cx = 0, cy = 0, cw = getWidth(), ch = getHeight();
    Section sections[] = getSections();
    
    // If reveal is not full (1) then clip
    if(getReveal()<1) {
        aPntr.save(); aPntr.clipRect(0,getHeight()*(1-getReveal()),getWidth(),getHeight()*getReveal()); }
        
    // Iterate over sections
    for(int i=0;i<_pointCount;i++) { Section section = sections[i];
        
        // If selected section, draw background
        if(i==selIndex) {
            aPntr.setColor(Color.get("#4488FF09")); aPntr.fillRect(cx + i*section.width, cy, section.width, ch); }
        
        // Iterate over series and draw bars
        for(int j=0;j<_seriesCount;j++) { Bar bar = section.bars[j];
            aPntr.setColor(bar.color); aPntr.fillRect(bar.x, bar.y, bar.width, bar.height - .5);
        }
    }
    
    // If reveal not full, resture gstate
    if(getReveal()<1) aPntr.restore();
}

/**
 * Override to return point above bar.
 */
public Point dataPointInLocal(DataPoint aDP)
{
    // Get data point info
    int seriesIndex = aDP.getSeriesActiveIndex();
    int pointIndex = aDP.getIndex();
    
    // Get bar for data point and return top-center point
    Section sections[] = getSections(), section = sections[pointIndex];
    Bar bars[] = section.bars, bar = bars[seriesIndex];
    return new Point(Math.round(bar.x + bar.width/2), Math.round(bar.y));
}

/**
 * Returns the data point best associated with given x/y (null if none).
 */
protected DataPoint getDataPointAt(double aX, double aY)
{
    // Get sections array
    Section sections[] = getSections();
        
    // Iterate over sections (points) and bars (series) and if bar contains point, return data point
    for(int i=0;i<_pointCount;i++) { Section section = sections[i];
        for(int j=0;j<_seriesCount;j++) { Bar bar = section.bars[j];
            if(bar.contains(aX,aY))
                return bar.point;
        }
    }
    
    // Return null since bar not found for point
    return null;
}

/**
 * A class to hold section information.
 */
protected class Section {
    
    // Points
    double x, y, width, height;
    Bar bars[];
    
    /** Creates a Section. */
    public Section(double aX, double aY, double aW, double aH)
    {
        x = aX; y = aY; width = aW; height = aH;
    }
    
    /** Returns whether section contains point. */
    public boolean contains(double aX, double aY)  { return Rect.contains(x, y, width, height, aX, aY); }
}

/**
 * A class to hold bar information.
 */
protected class Bar {
    
    // Points
    DataPoint point;
    double x, y, width, height;
    Color color;
    
    /** Creates a bar. */
    public Bar(DataPoint aDP, double aX, double aY, double aW, double aH, Color aColor)
    {
        point = aDP; x = aX; y = aY; width = aW; height = aH; color = aColor;
    }
    
    /** Returns whether bar contains point. */
    public boolean contains(double aX, double aY)  { return Rect.contains(x, y, width, height, aX, aY); }
}

}