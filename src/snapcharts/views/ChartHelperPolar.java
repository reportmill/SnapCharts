package snapcharts.views;

import snap.geom.Rect;
import snapcharts.model.*;

import java.util.List;

/**
 * A ChartHelper for Polar charts.
 */
public class ChartHelperPolar extends ChartHelper {

    // The first polar area
    private DataAreaPolar  _polarDataArea;

    /**
     * Constructor.
     */
    public ChartHelperPolar(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return ChartType.POLAR; }

    /**
     * Override to return ChartViewLayoutPolar.
     */
    @Override
    public ChartViewLayout createLayout()
    {
        return new ChartViewLayoutPolar(_chartView);
    }

    /**
     * Creates the DataAreas.
     */
    @Override
    protected DataArea[] createDataAreas()
    {
        DataSetList dataSetList = getDataSetList();
        List<DataSet> dsets = dataSetList.getDataSets();
        int dsetCount = dsets.size();

        DataArea[] dataAreas = new DataArea[dsetCount];
        for (int i=0; i<dsetCount; i++) {
            DataSet dset = dsets.get(i);
            dataAreas[i] = new DataAreaPolar(this, dset);
            if (i==0)
                _polarDataArea = (DataAreaPolar) dataAreas[0];
        }

        return dataAreas;
    }

    /**
     * Returns the polar bounds.
     */
    public Rect getPolarBounds()
    {
        return _polarDataArea.getPolarBounds();
    }

    /**
     * Override to handle Polar special.
     */
    protected Intervals createIntervals(AxisView axisView)
    {
        // If Y Axis, substitute X axis
        if (axisView.getAxisType().isAnyY())
            axisView = getAxisViewX();

        // Do normal version
        return super.createIntervals(axisView);
    }

    /**
     * Override to handle Polar special.
     */
    public double dataToView(AxisView axisView, double dataXY)
    {
        // Get data min/max (data coords)
        Intervals intervals = axisView.getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();

        // Handle log
        if (axisView.isLog()) {
            dataXY = ChartViewUtils.log10(dataXY);
            dataMin = ChartViewUtils.log10(dataMin);
            dataMax = ChartViewUtils.log10(dataMax);
        }

        // Get display len (min is zero)
        boolean isHor = axisView.getAxisType() == AxisType.X;
        Rect polarBounds = getPolarBounds();
        double areaX = isHor ? polarBounds.getMidX() : polarBounds.y;
        double areaW = isHor ? polarBounds.getWidth()/2 : polarBounds.getHeight()/2;

        // Convert data to display
        double dispXY = isHor ? areaX + (dataXY - dataMin) / (dataMax - dataMin) * areaW :
                areaX + areaW - (dataXY - dataMin) / (dataMax - dataMin) * areaW;

        // Return display val
        return dispXY;
    }

    /**
     * Override to handle Polar special.
     */
    public double viewToData(AxisView axisView, double dispXY)
    {
        // Get data min/max (data coords)
        Intervals intervals = axisView.getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();

        // Handle log
        if (axisView.isLog()) {
            dataMin = ChartViewUtils.log10(dataMin);
            dataMax = ChartViewUtils.log10(dataMax);
        }

        // Get display len (min is zero)
        boolean isHor = axisView.getAxisType() == AxisType.X;
        Rect polarBounds = getPolarBounds();
        double areaX = isHor ? polarBounds.getMidX() : polarBounds.y;
        double areaW = isHor ? polarBounds.getWidth()/2 : polarBounds.getHeight()/2;

        // Convert display to data
        double dataXY = isHor ? dataMin + (dispXY - areaX) / areaW * (dataMax - dataMin) :
                dataMax - (dispXY - areaX) / areaW * (dataMax - dataMin);

        // Handle log
        if (axisView.isLog())
            dataXY = ChartViewUtils.invLog10(dataXY);

        // Return data val
        return dataXY;
    }
}
