package snapcharts.views;

import snap.geom.Pos;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.text.StringBox;
import snapcharts.model.DataSet;

/**
 * A DataArea subclass to display ChartType CONTOUR.
 */
public class DataAreaContour extends DataArea {

    /**
     * Constructor.
     */
    public DataAreaContour(ChartHelper aChartHelper, DataSet aDataSet)
    {
        super(aChartHelper, aDataSet);
    }

    /**
     * Paints chart content.
     */
    @Override
    protected void paintChart(Painter aPntr)
    {
        StringBox sbox = new StringBox("Contour Chart");
        sbox.setFont(Font.Arial16);
        sbox.setAlign(Pos.CENTER);
        sbox.setRect(0, 0, getWidth(), getHeight());
        sbox.paint(aPntr);
    }
}
