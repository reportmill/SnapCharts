package snap.charts;
import java.util.*;

import snap.geom.*;
import snap.gfx.*;

/**
 * A ChartArea subclass to display the contents of line chart.
 */
public class ChartAreaLine extends ChartArea {

/**
 * Creates a ChartAreaLine.
 */
public ChartAreaLine()
{
    setPadding(5,10,0,10); // Add padding so endpoints aren't on edges
}

/**
 * Returns the list of paths for each series.
 */
public List <Path> getSeriesPaths()
{
    // Get series paths
    List <Path> paths = new ArrayList();
    int seriesCount = getSeriesCount();
    int pointCount = getPointCount();
    
    // Iterate over series
    for(int i=0; i<seriesCount;i++) { DataSeries series = getSeries(i); if(series.isDisabled()) continue;
    
        Path path = new Path(); paths.add(path);
        
        // Iterate over values
        for(int j=0;j<pointCount;j++) { double val = series.getValueX(j);
            Point p = seriesToLocal(j, val);
            if(j==0) path.moveTo(p.x,p.y); else path.lineTo(p.x,p.y);
        }
    }
    return paths;
}

/**
 * Paints chart.
 */
protected void paintChart(Painter aPntr, double aX, double aY, double aW, double aH)
{
    // Get Series list
    List <DataSeries> seriesList = getActiveSeries();
    int scount = seriesList.size();
    
    int pointCount = getPointCount();
    DataPoint selPoint = _chartView.getTargDataPoint();
    DataSeries selSeries = selPoint!=null? selPoint.getSeries() : null;
    int selIndex = selPoint!=null? selPoint.getIndex() : -1;
    
    // If reveal is not full (1) then clip
    if(getReveal()<1) {
        aPntr.save(); aPntr.clipRect(0,0,getWidth()*getReveal(),getHeight()); }
        
    // Draw series paths
    List <Path> paths = getSeriesPaths();
    for(int i=0;i<paths.size();i++) { Path path = paths.get(i); DataSeries series = seriesList.get(i);
        aPntr.setColor(getColor(series.getIndex()));
        aPntr.setStroke(Stroke.Stroke2); if(series==selSeries) aPntr.setStroke(Stroke3);
        aPntr.draw(path);
    }
    
    // Draw series points
    for(int i=0; i<scount;i++) { DataSeries series = seriesList.get(i);
    
        // Iterate over values
        for(int j=0;j<pointCount;j++) { double val = series.getValueX(j);
        
            Point p = seriesToLocal(j, val);
            
            Shape marker = getMarkerShape(series.getIndex()).copyFor(new Transform(p.x-4,p.y-4));
            Color c = getColor(series.getIndex());
            
            if(series==selSeries && j==selIndex) {
                aPntr.setColor(c.blend(Color.CLEARWHITE, .5));
                aPntr.fill(new Ellipse(p.x-10,p.y-10,20,20));
                aPntr.setStroke(Stroke5); aPntr.setColor(Color.WHITE); aPntr.draw(marker);
                aPntr.setStroke(Stroke3); aPntr.setColor(c); aPntr.draw(marker);
            }
            aPntr.setColor(c); aPntr.fill(marker);
        }
    }
    
    // If reveal not full, resture gstate
    if(getReveal()<1) aPntr.restore();
}

}