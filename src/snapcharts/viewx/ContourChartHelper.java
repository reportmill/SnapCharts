package snapcharts.viewx;
import snap.gfx.Color;
import snap.util.PropChange;
import snapcharts.model.*;
import snapcharts.util.MinMax;
import snapcharts.views.*;
import java.util.List;

/**
 * A ChartHelper for common Contour types.
 */
public class ContourChartHelper extends ChartHelper {

    // An object to help with Contours
    protected ContourHelper  _contourHelper;

    /**
     * Constructor.
     */
    public ContourChartHelper(ChartView aChartView)
    {
        super(aChartView);
        _contourHelper = new ContourHelper(this);
    }

    /**
     * Returns the ChartType.
     */
    @Override
    public ChartType getChartType()  { return ChartType.CONTOUR; }

    /**
     * Returns the ContourHelper.
     */
    public ContourHelper getContourHelper()  { return _contourHelper; }

    /**
     * Creates the DataAreas.
     */
    @Override
    protected DataArea[] createDataAreas()
    {
        DataSetList dataSetList = getDataSetList();
        List<DataSet> dsets = dataSetList.getDataSets();
        int dsetCount = dsets.size();

        DataArea[] dataAreas = new DataArea[dsetCount]; // *2
        for (int i=0; i<dsetCount; i++) {
            DataSet dset = dsets.get(i);
            //dataAreas[i*2] = new DataAreaXY(this, dset, ChartType.LINE);
            dataAreas[i] = new ContourDataArea(this, dset); // i*2+1
        }

        return dataAreas;
    }

    /**
     * Returns the contour legend.
     */
    public ContourAxisView getContourView()
    {
        return _chartView.getContourView();
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
        if (src instanceof DataSet || src instanceof DataSetList || src instanceof ChartTypeProps) {
            _contourHelper.resetCachedValues();
        }
    }
}
