package snapcharts.views;
import java.util.*;
import snap.geom.*;
import snap.gfx.*;
import snapcharts.model.ChartType;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;

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
        // Get info
        DataSetList dataSetList = getDataSetList();
        int pointCount = getPointCount();

        // Get DataSets list, create paths list
        List<DataSet> dsets = dataSetList.getDataSets();
        List <Path> paths = new ArrayList();

        // Iterate over datasets
        for (DataSet dset : dsets) {

            // Create/add path for dataset
            Path path = new Path();
            paths.add(path);

            // Iterate over data points
            for (int j=0; j<pointCount; j++) {

                // Get data X/Y and disp X/Y
                double dataX = dset.getX(j);
                double dataY = dset.getY(j);
                Point dispXY = dataToView(dataX, dataY);
                if (j==0)
                    path.moveTo(dispXY.x, dispXY.y);
                else path.lineTo(dispXY.x, dispXY.y);
            }
        }

        // Return paths
        return paths;
    }

    /**
     * Paints chart.
     */
    protected void paintChart(Painter aPntr, double aX, double aY, double aW, double aH)
    {
        // Get DataSet list
        DataSetList dataSetList = getDataSetList();
        List<DataSet> dsets = dataSetList.getDataSets();
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
            aPntr.setStroke(dset==selSet ? Stroke3 : Stroke.Stroke2);
            aPntr.draw(path);
        }

        // Draw dataset points
        for (int i=0; i<dsetCount;i++) {

            // Get dataset and index (could be different if DataSetList has disabled sets)
            DataSet dset = dsets.get(i);
            int dsetIndex = dset.getIndex();

            // Iterate over values
            for (int j=0;j<pointCount;j++) {

                // Get data X/Y and disp X/Y
                double dataX = dset.getX(j);
                double dataY = dset.getY(j);
                double dispX = dataToViewX(dataX);
                double dispY = dataToViewY(dataY);

                Shape marker = getMarkerShape(dsetIndex).copyFor(new Transform(dispX-4,dispY-4));
                Color c = getColor(dsetIndex);

                if (dset==selSet && j==selIndex) {
                    aPntr.setColor(c.blend(Color.CLEARWHITE, .5));
                    aPntr.fill(new Ellipse(dispX - 10,dispY - 10,20,20));
                    aPntr.setStroke(Stroke5); aPntr.setColor(Color.WHITE);
                    aPntr.draw(marker);
                    aPntr.setStroke(Stroke3); aPntr.setColor(c);
                    aPntr.draw(marker);
                }
                aPntr.setColor(c);
                aPntr.fill(marker);
            }
        }

        // If reveal not full, resture gstate
        if (getReveal()<1) aPntr.restore();
    }
}