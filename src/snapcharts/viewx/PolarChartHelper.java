package snapcharts.viewx;
import snap.geom.Rect;
import snap.util.PropChange;
import snapcharts.model.*;
import snapcharts.util.MinMax;
import snapcharts.view.*;
import java.util.List;

/**
 * A ChartHelper for Polar charts.
 */
public class PolarChartHelper extends ChartHelper {

    // The rect around the polar grid
    private Rect  _polarBounds;

    // The MinMax for Chart Radius data
    private MinMax  _radiusMinMax;

    /**
     * Constructor.
     */
    public PolarChartHelper(ChartView aChartView)
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
        return new PolarChartViewLayout(_chartView);
    }

    /**
     * Creates the DataAreas.
     */
    @Override
    protected DataArea[] createDataAreas()
    {
        DataSetList dataSetList = getDataSetList();
        DataSet[] dsets = dataSetList.getDataSets();
        int dsetCount = dsets.length;

        DataArea[] dataAreas = new DataArea[dsetCount];
        for (int i=0; i<dsetCount; i++) {
            DataSet dset = dsets[i];
            dataAreas[i] = new PolarDataArea(this, dset);
        }

        return dataAreas;
    }

    /**
     * Returns the polar bounds.
     */
    public Rect getPolarBounds()
    {
        // If already set, just return
        if (_polarBounds!=null) return _polarBounds;

        // Calc polar rect
        DataView dataView = getDataView();
        double viewW = dataView.getWidth();
        double viewH = dataView.getHeight();
        double areaX = 0;
        double areaY = 0;
        double areaW = viewW;
        double areaH = viewH;
        if (areaW < areaH) {
            areaH = areaW;
            areaY = areaY + Math.round((viewH - areaH)/2);
        }
        else {
            areaW = areaH;
            areaX = areaX + Math.round((viewW - areaW)/2);
        }

        // Set/return
        return _polarBounds = new Rect(areaX, areaY, areaW, areaH);
    }

    /**
     * Converts a polar theta/radius point to coord on axis in display coords.
     */
    public double polarDataToView(AxisType anAxisType, double aTheta, double aRadius)
    {
        // Convert from polar to XY
        double dataXY;
        if (anAxisType == AxisType.X)
            dataXY = Math.cos(aTheta) * aRadius;
        else dataXY = Math.sin(aTheta) * aRadius;

        // Do normal XY data to view conversion
        return dataToView(anAxisType, dataXY);
    }

    /**
     * Override to handle Polar special.
     */
    protected Intervals createIntervals(AxisView axisView)
    {
        // If Y Axis, substitute X axis intervals
        if (axisView.getAxisType().isAnyY())
            return getAxisViewX().getIntervals();

        // Do normal version
        return super.createIntervals(axisView);
    }

    /**
     * Override for polar.
     */
    public double getAxisMinForIntervalCalc(AxisView axisView)
    {
        // If explicitly set, just return
        if (axisView.isAxisMinOverrideSet())
            return axisView.getAxisMinOverride();

        // Return Min for radius
        return getMinMaxForRadius().getMin();
    }

    /**
     * Returns the axis max.
     */
    public double getAxisMaxForIntervalCalc(AxisView axisView)
    {
        // If explicitly set, just return
        if (axisView.isAxisMaxOverrideSet())
            return axisView.getAxisMaxOverride();

        // Return Min for radius
        return getMinMaxForRadius().getMax();
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

    /**
     * Returns the MinMax for radius.
     */
    private MinMax getMinMaxForRadius()
    {
        // If already set, just return
        if (_radiusMinMax != null) return _radiusMinMax;

        // Get DataSetList (if empty, just return silly range)
        DataSetList dataSetList = getDataSetList();
        if (dataSetList.getDataSetCount()==0 || dataSetList.getPointCount()==0)
            return new MinMax(0, 5);

        // Get Radius MinMax for all datasets
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        DataSet[] dataSets = dataSetList.getEnabledDataSets();
        for (DataSet dset : dataSets) {
            RawData rawData = dset.getProcessedData();
            MinMax minMax = rawData.getMinMaxR();
            min = Math.min(min, minMax.getMin());
            max = Math.max(max, minMax.getMax());
        }

        // Check Axis.ZeroRequired
        AxisView axisViewX = getAxisViewX();
        Axis axisX = axisViewX.getAxis();
        if (axisX.isZeroRequired()) {
            if (min > 0) min = 0;
            if (max < 0) max = 0;
        }

        // Set/return min/max
        return _radiusMinMax = new MinMax(min, max);
    }

    /**
     * Called when a ChartPart changes.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Handle DataSet/DataSetList change
        Object src = aPC.getSource();
        if (src instanceof DataSet || src instanceof DataSetList) {
            _radiusMinMax = null;
        }
    }

    /**
     * Override to clear PolarBounds.
     */
    @Override
    protected void dataViewDidChangeSize()
    {
        // Do normal version
        super.dataViewDidChangeSize();

        // Clear PolarBounds
        _polarBounds = null;
    }
}
