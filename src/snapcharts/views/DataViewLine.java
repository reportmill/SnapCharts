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
public class DataViewLine extends DataViewPanZoom {

    // Constants for defaults
    protected static Stroke Stroke2 = new Stroke(2, Stroke.Cap.Round, Stroke.Join.Round, 0);
    protected static Stroke Stroke3 = new Stroke(3, Stroke.Cap.Round, Stroke.Join.Round, 0);
    protected static Stroke Stroke5 = new Stroke(5, Stroke.Cap.Round, Stroke.Join.Round, 0);

    /**
     * Creates a ChartAreaLine.
     */
    public DataViewLine()
    {
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()
    {
        return ChartType.LINE;
    }

    /**
     * Returns the list of paths for each dataset.
     */
    public List<Path> getDataSetPaths()
    {
        // Get info
        DataSetList dataSetList = getDataSetList();
        List<DataSet> dsets = dataSetList.getDataSets();
        List<Path> paths = new ArrayList<>();

        // Iterate over datasets
        for (DataSet dset : dsets) {

            // Create/add path for dataset
            int pointCount = dset.getPointCount();
            Path path = new Path();
            paths.add(path);

            // Iterate over data points
            for (int j = 0; j < pointCount; j++) {

                // Get data X/Y and disp X/Y
                double dataX = dset.getX(j);
                double dataY = dset.getY(j);
                Point dispXY = dataToView(dataX, dataY);
                if (j == 0)
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

        DataPoint selPoint = _chartView.getTargDataPoint();
        DataSet selSet = selPoint != null ? selPoint.getDataSet() : null;

        // If reveal is not full (1) then clip
        if (getReveal() < 1) {
            aPntr.save();
            aPntr.clipRect(0, 0, getWidth() * getReveal(), getHeight());
        }

        // Draw dataset paths
        List<Path> paths = getDataSetPaths();
        for (int i=0; i<paths.size(); i++) {
            Path path = paths.get(i);
            DataSet dset = dsets.get(i);
            aPntr.setColor(getColor(dset.getIndex()));
            aPntr.setStroke(dset==selSet ? Stroke3 : Stroke2);
            aPntr.draw(path);
        }

        // Get DataSets that ShowSymbols
        List<DataSet> dsetsSymb = dataSetList.getDataSetsFiltered(dset -> dset.isShowSymbols());

        // Draw dataset points
        for (DataSet dset : dsetsSymb) {

            // Get dataset index (could be different if DataSetList has disabled sets)
            int dsetIndex = dset.getIndex();
            int pointCount = dset.getPointCount();

            // Iterate over values
            for (int j=0; j<pointCount; j++) {

                // Get data X/Y and disp X/Y
                double dataX = dset.getX(j);
                double dataY = dset.getY(j);
                double dispX = dataToViewX(dataX);
                double dispY = dataToViewY(dataY);

                // Get symbol and color and paint
                Shape symbol = getSymbolShape(dsetIndex).copyFor(new Transform(dispX - 4, dispY - 4));
                Color color = getColor(dsetIndex);
                aPntr.setColor(color);
                aPntr.fill(symbol);
            }
        }

        // Paint selected point
        paintSelPoint(aPntr);

        // If reveal not full, resture gstate
        if (getReveal() < 1) aPntr.restore();
    }

    /**
     * Paints selected point.
     */
    protected void paintSelPoint(Painter aPntr)
    {
        // Get DataSet list
        DataPoint selPoint = _chartView.getTargDataPoint(); if (selPoint == null) return;
        DataSet dset = selPoint.getDataSet();
        int dsetIndex = dset.getIndex();
        int selIndex = selPoint.getIndex();

        // Get data X/Y and disp X/Y
        double dataX = dset.getX(selIndex);
        double dataY = dset.getY(selIndex);
        double dispX = dataToViewX(dataX);
        double dispY = dataToViewY(dataY);

        Shape symbol = getSymbolShape(dsetIndex).copyFor(new Transform(dispX - 4, dispY - 4));
        Color color = getColor(dsetIndex);

        aPntr.setColor(color.blend(Color.CLEARWHITE, .5));
        aPntr.fill(new Ellipse(dispX - 10, dispY - 10, 20, 20));
        aPntr.setStroke(Stroke5);
        aPntr.setColor(Color.WHITE);
        aPntr.draw(symbol);
        aPntr.setStroke(Stroke3);
        aPntr.setColor(color);
        aPntr.draw(symbol);
    }
}