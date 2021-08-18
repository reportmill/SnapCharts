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
        setFill(aMarker.getFill());
        setBorder(aMarker.getBorder());
        setOpacity(aMarker.getOpacity());
        setManaged(false);
        setPaintable(false);
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
        ChartHelper chartHelper = getChartHelper();
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
            DataArea dataArea = chartHelper.getDataAreas()[0];
            double dataX0 = marker.getX();
            double dataX1 = dataX0 + marker.getWidth();
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
            DataView dataView = chartHelper.getDataView();
            markX = marker.getX();
            markW = marker.getWidth();
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
            markX = marker.getX();
            markW = marker.getWidth();
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
            DataArea dataArea = chartHelper.getDataAreaForAxisTypeY(axisTypeY);
            double dataY0 = marker.getY();
            double dataY1 = dataY0 + marker.getHeight();
            if (isFractionalY) {
                double dataMin = dataArea.getStagedData().getMinY();
                double dataMax = dataArea.getStagedData().getMaxY();
                dataY0 = dataMin + dataY0 * (dataMax - dataMin);
                dataY1 = dataMin + dataY1 * (dataMax - dataMin);
            }
            markY = dataArea.dataToViewY(dataY0);
            markH = markY - dataArea.dataToViewY(dataY1) ;
        }

        // Handle CoordSpaceY DataBounds
        else if (coordSpaceY == Marker.CoordSpace.DataBounds) {
            DataView dataView = chartHelper.getDataView();
            markY = marker.getY();
            markH = marker.getHeight();
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
            markY = marker.getY();
            markH = marker.getHeight();
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
