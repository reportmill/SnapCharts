package snapcharts.views;
import java.util.*;
import snap.geom.*;
import snap.gfx.*;
import snapcharts.model.ChartType;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSet;

/**
 * A ChartArea subclass to display the contents of line chart.
 */
public class DataViewLine extends DataView {

    // Constants for defaults
    protected static Stroke Stroke3 = new Stroke(3);
    protected static Stroke Stroke5 = new Stroke(5);

    /**
     * Creates a ChartAreaLine.
     */
    public DataViewLine()
    {
        setPadding(5,10,0,10); // Add padding so endpoints aren't on edges
    }

    /**
     * Returns the type.
     */
    public ChartType getType()  { return ChartType.LINE; }

    /**
     * Returns the list of paths for each dataset.
     */
    public List <Path> getDataSetPaths()
    {
        // Get dataset paths
        List <Path> paths = new ArrayList();
        int dsetCount = getDataSetCount();
        int pointCount = getPointCount();

        // Iterate over datasets
        for (int i=0; i<dsetCount;i++) { DataSet dset = getDataSet(i); if (dset.isDisabled()) continue;

            Path path = new Path(); paths.add(path);

            // Iterate over values
            for (int j=0;j<pointCount;j++) { double val = dset.getValueX(j);
                Point p = dataToView(j, val);
                if (j==0) path.moveTo(p.x,p.y); else path.lineTo(p.x,p.y);
            }
        }
        return paths;
    }

    /**
     * Paints chart.
     */
    protected void paintChart(Painter aPntr, double aX, double aY, double aW, double aH)
    {
        // Get DataSet list
        List <DataSet> dsets = getActiveDataSets();
        int dsetCount = dsets.size();

        int pointCount = getPointCount();
        DataPoint selPoint = _chartView.getTargDataPoint();
        DataSet selSet = selPoint!=null? selPoint.getDataSet() : null;
        int selIndex = selPoint!=null? selPoint.getIndex() : -1;

        // If reveal is not full (1) then clip
        if (getReveal()<1) {
            aPntr.save(); aPntr.clipRect(0,0,getWidth()*getReveal(),getHeight()); }

        // Draw dataset paths
        List <Path> paths = getDataSetPaths();
        for (int i=0;i<paths.size();i++) { Path path = paths.get(i);
            DataSet dset = dsets.get(i);
            aPntr.setColor(getColor(dset.getIndex()));
            aPntr.setStroke(Stroke.Stroke2); if (dset==selSet) aPntr.setStroke(Stroke3);
            aPntr.draw(path);
        }

        // Draw dataset points
        for (int i=0; i<dsetCount;i++) { DataSet dset = dsets.get(i);

            // Iterate over values
            for (int j=0;j<pointCount;j++) { double val = dset.getValueX(j);

                Point p = dataToView(j, val);

                Shape marker = getMarkerShape(dset.getIndex()).copyFor(new Transform(p.x-4,p.y-4));
                Color c = getColor(dset.getIndex());

                if (dset==selSet && j==selIndex) {
                    aPntr.setColor(c.blend(Color.CLEARWHITE, .5));
                    aPntr.fill(new Ellipse(p.x-10,p.y-10,20,20));
                    aPntr.setStroke(Stroke5); aPntr.setColor(Color.WHITE); aPntr.draw(marker);
                    aPntr.setStroke(Stroke3); aPntr.setColor(c); aPntr.draw(marker);
                }
                aPntr.setColor(c); aPntr.fill(marker);
            }
        }

        // If reveal not full, resture gstate
        if (getReveal()<1) aPntr.restore();
    }
}