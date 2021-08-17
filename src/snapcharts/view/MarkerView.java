package snapcharts.view;
import snap.geom.Rect;
import snapcharts.model.AxisType;
import snapcharts.model.Marker;

/**
 * This ChartPartView subclass renders a Chart Marker.
 */
public class MarkerView extends ChartPartView<Marker> {

    /**
     * Constructor.
     */
    public MarkerView(Marker aMarker)
    {
        super(aMarker);
        setManaged(false);
    }

    /**
     * Returns the Marker.
     */
    public Marker getMarker()  { return _chartPart; }

    /**
     * Returns the appropriate bounds for MarkerView.
     */
    public Rect getPrefBounds()
    {
        Marker marker = getMarker();
        double markX;
        double markY;
        double markW;
        double markH;
        markX = markY = markW = markH = 0;

        // Get CoordSpace info for X
        Marker.CoordSpace coordSpaceX = marker.getCoordSpaceX();
        boolean isFractionalX = marker.isFractionalX();
        AxisType axisTypeX = coordSpaceX.getAxisType();

        // Handle CoordSpaceX Axis
        if (axisTypeX != null) {
            DataArea dataArea = getChartHelper().getDataAreas()[0];
            double dataX0 = getX();
            double dataX1 = dataX0 + getWidth();
            if (isFractionalX) {
                double dataMin = dataArea.getStagedData().getMinX();
                double dataMax = dataArea.getStagedData().getMaxX();
                dataX0 = dataMin + dataX0 * (dataMax - dataMin);
                dataX1 = dataMin + dataX1 * (dataMax - dataMin);
            }
            markX = dataArea.dataToViewX(dataX0);
            markW = dataArea.dataToViewX(dataX1) - markX;
        }

        // Handle CoordSpaceX DataBounds
        else if (coordSpaceX == Marker.CoordSpace.DataBounds) {
            DataView dataView = getChartHelper().getDataView();
            markX = getX();
            markW = getWidth();
            if (isFractionalX) {
                double dispMin = 0;
                double dispMax = dataView.getWidth();
                markX = dispMin + markX * (dispMax - dispMin);
                markW = markW * (dispMax - dispMin);
            }
        }

        // Handle CoordSpaceX ChartBounds
        else if (coordSpaceX == Marker.CoordSpace.ChartBounds) {
            ChartView chartView = getChartView();
            markX = getX();
            markW = getWidth();
            if (isFractionalX) {
                double dispMin = 0;
                double dispMax = chartView.getWidth();
                markX = dispMin + markX * (dispMax - dispMin);
                markW = markW * (dispMax - dispMin);
            }
        }

        // Get CoordSpace info for Y
        Marker.CoordSpace coordSpaceY = marker.getCoordSpaceY();
        boolean isFractionalY = marker.isFractionalY();
        AxisType axisTypeY = coordSpaceY.getAxisType();

        // Handle CoordSpaceY Axis
        if (axisTypeY != null) {
            DataArea dataArea = getChartHelper().getDataAreaForAxisTypeY(axisTypeY);
            double dataY0 = getY();
            double dataY1 = dataY0 + getHeight();
            if (isFractionalY) {
                double dataMin = dataArea.getStagedData().getMinY();
                double dataMax = dataArea.getStagedData().getMaxY();
                dataY0 = dataMin + dataY0 * (dataMax - dataMin);
                dataY1 = dataMin + dataY1 * (dataMax - dataMin);
            }
            markY = dataArea.dataToViewY(dataY0);
            markH = dataArea.dataToViewY(dataY1) - markY;
        }

        // Handle CoordSpaceY DataBounds
        else if (coordSpaceY == Marker.CoordSpace.DataBounds) {
            DataView dataView = getChartHelper().getDataView();
            markY = getY();
            markH = getHeight();
            if (isFractionalY) {
                double dispMin = 0;
                double dispMax = dataView.getHeight();
                markY = dispMin + markY * (dispMax - dispMin);
                markH = markH * (dispMax - dispMin);
            }
        }

        // Handle CoordSpaceY ChartBounds
        else if (coordSpaceY == Marker.CoordSpace.ChartBounds) {
            ChartView chartView = getChartView();
            markY = getY();
            markH = getHeight();
            if (isFractionalY) {
                double dispMin = 0;
                double dispMax = chartView.getHeight();
                markY = dispMin + markY * (dispMax - dispMin);
                markH = markH * (dispMax - dispMin);
            }
        }

        // Return rect
        return new Rect(markX, markY, markW, markH);
    }
}
