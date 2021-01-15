package snapcharts.views;

import snap.geom.Rect;
import snapcharts.model.AxisType;
import snapcharts.model.ChartType;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;

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
     * Creates an AxisView for given type.
     */
    @Override
    protected AxisView createAxisView(AxisType anAxisType)
    {
        AxisView axisView = super.createAxisView(anAxisType);
        axisView.setRadial(true);
        return axisView;
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
}
