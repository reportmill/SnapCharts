package snapcharts.viewx;
import snap.gfx.Painter;
import snap.util.PropChange;
import snapcharts.model.Axis;
import snapcharts.model.ChartTypeProps;
import snapcharts.model.DataSet;

/**
 * A (Polar)DataArea subclass for PolarContour charts.
 */
public class PolarContourDataArea extends PolarDataArea {

    // The ContourHelper
    private ContourHelper _contourHelper;

    // The ContourShapes
    private ContourPainter _contourPainter;

    /**
     * Constructor.
     */
    public PolarContourDataArea(PolarContourChartHelper aChartHelper, DataSet aDataSet)
    {
        super(aChartHelper, aDataSet);

        _contourHelper = aChartHelper._contourHelper;
        _contourPainter = new ContourPainter(_contourHelper, this);
    }

    /**
     * Paints chart content.
     */
    @Override
    protected void paintChart(Painter aPntr)
    {
        // Paint contours
        _contourPainter.paintAll(aPntr);

        // Repaint (semi-transparent) gridlines on top of contours
        aPntr.setOpacity(.4);
        paintGridlines(aPntr);
        aPntr.setOpacity(1);
    }

    /**
     * Override to clear cached contour data/shape values.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Handle changes
        Object src = aPC.getSource();
        if (src==getDataSet() || src instanceof Axis || src instanceof ChartTypeProps) {
            _contourPainter.clearContoursAll();
        }
    }

    /**
     * Override to clear cached contour shape values.
     */
    @Override
    protected void dataViewDidChangeSize()
    {
        // Do normal version
        super.dataViewDidChangeSize();

        // Handle changes
        _contourPainter.clearContours();
    }

    /**
     * Override to clear cached contour shape values.
     */
    @Override
    protected void axisViewDidChange(PropChange aPC)
    {
        // Do normal version
        super.axisViewDidChange(aPC);

        // Handle changes
        _contourPainter.clearContours();
    }
}
