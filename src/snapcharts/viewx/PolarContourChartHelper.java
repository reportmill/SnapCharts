package snapcharts.viewx;
import snap.util.PropChange;
import snapcharts.model.*;
import snapcharts.views.*;
import java.util.List;

/**
 * A (Polar)ChartHelper subclass for PolarContour charts.
 */
public class PolarContourChartHelper extends PolarChartHelper {

    // An object to help with Contours
    protected ContourHelper  _contourHelper;

    /**
     * Constructor.
     */
    public PolarContourChartHelper(ChartView aChartView)
    {
        super(aChartView);
        _contourHelper = new ContourHelper(this);
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return ChartType.POLAR_CONTOUR; }

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

        DataArea[] dataAreas = new DataArea[dsetCount];
        for (int i=0; i<dsetCount; i++) {
            DataSet dset = dsets.get(i);
            dataAreas[i] = new PolarContourDataArea(this, dset);
        }

        return dataAreas;
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
