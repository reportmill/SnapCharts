package snapcharts.view;
import snap.geom.HPos;
import snap.geom.Insets;
import snap.geom.Rect;
import snap.geom.VPos;
import snap.gfx.Color;
import snap.view.TextArea;
import snapcharts.model.AxisType;
import snapcharts.model.DataSet;
import snapcharts.model.DataStore;
import snapcharts.model.Marker;
import snapcharts.util.MinMax;

/**
 * This ChartPartView subclass renders a Chart Marker.
 */
public class MarkerView extends ChartPartView<Marker> {

    // The TextView to show Marker text
    private TextArea  _textArea;

    /**
     * Constructor.
     */
    public MarkerView(Marker aMarker)
    {
        super(aMarker);
        setManaged(false);
        setPaintable(false);

        // Create/add TextArea
        _textArea = new TextArea();
        _textArea.setPlainText(true);
        _textArea.setBorder(Color.PINK, 1);
        addChild(_textArea);

        resetPaintProperties();
    }

    /**
     * Returns the Marker.
     */
    public Marker getMarker()  { return _chartPart; }

    /**
     * Override for MarkerView.
     */
    @Override
    protected void resetView()
    {
        super.resetView();

        resetPaintProperties();
    }

    /**
     * Resets the paint properties from Marker in view.
     */
    public void resetPaintProperties()
    {
        Marker marker = getMarker();
        setFill(marker.getFill());
        setBorder(marker.getBorder());
        setOpacity(marker.getOpacity());
        setPadding(marker.getPadding());

        // Update TextArea
        String text = marker.getText();
        if (text == null || text.length() == 0) {
            _textArea.setVisible(false);
            return;
        }

        // Update TextArea
        _textArea.setText(text);
        _textArea.setFont(marker.getFont());
        _textArea.setVisible(true);
        _textArea.setWrapLines(true);
        _textArea.setAlign(marker.getAlign());

        // Layout TextView
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();
        if (areaW < 50)
            areaW = 50;
        if (areaH < _textArea.getPrefHeight())
            areaH = _textArea.getPrefHeight();
        _textArea.setBounds(areaX, areaY, areaW, areaH);

        // Handle TextOutsideX
        if (marker.isTextOutsideX()) {
            if (marker.getAlignX() == HPos.LEFT) {
                _textArea.setX(-getWidth());
                _textArea.setAlignX(HPos.RIGHT);
            }
            else if (marker.getAlignX() == HPos.RIGHT) {
                _textArea.setX(getWidth());
                _textArea.setAlignX(HPos.LEFT);
            }
        }

        // Handle TextOutsideY
        if (marker.isTextOutsideY()) {
            if (marker.getAlignY() == VPos.TOP) {
                _textArea.setY(-getHeight());
                _textArea.setAlignY(VPos.BOTTOM);
            }
            else if (marker.getAlignY() == VPos.BOTTOM) {
                _textArea.setY(getHeight());
                _textArea.setAlignY(VPos.TOP);
            }
        }
    }

    /**
     * Returns the appropriate bounds for MarkerView in ChartView coords.
     */
    public Rect getPrefBoundsInChartViewCoords()
    {
        ChartHelper chartHelper = getChartHelper();
        DataView dataView = chartHelper.getDataView();
        Marker marker = getMarker();
        double markX;
        double markY;
        double markW;
        double markH;
        markX = markY = markW = markH = 0;

        // Get first DataArea
        DataArea[] dataAreas = chartHelper.getDataAreas();
        DataArea dataArea0 = dataAreas.length > 0 ? dataAreas[0] : null;

        // Get CoordSpace info for X
        Marker.CoordSpace coordSpaceX = marker.getCoordSpaceX();
        boolean isFractionalX = marker.isFractionalX();
        AxisType axisTypeX = coordSpaceX.getAxisType();

        // Handle CoordSpaceX Axis
        if (axisTypeX != null) {
            DataArea dataArea = dataArea0;
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
        else if (coordSpaceX == Marker.CoordSpace.DataView) {
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
        else if (coordSpaceX == Marker.CoordSpace.ChartView) {
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
            if (dataArea == null) {
                System.err.println("MarkerView.getPrefBoundsInChartViewCoords: Invalid Y axis: " + axisTypeY);
                dataArea = dataArea0;
            }
            double dataY0 = marker.getY();
            double dataY1 = dataY0 + marker.getHeight();
            if (isFractionalY) {
                MinMax dataMinMax = getStagedDataMinMaxYForAxisY(axisTypeY);
                double dataMin = dataMinMax.getMin();
                double dataMax = dataMinMax.getMax();
                dataY0 = dataMin + dataY0 * (dataMax - dataMin);
                dataY1 = dataMin + dataY1 * (dataMax - dataMin);
            }
            markY = dataArea.dataToViewY(dataY1);
            double markMaxY = dataArea.dataToViewY(dataY0);
            markH = markMaxY - markY;
        }

        // Handle CoordSpaceY DataBounds
        else if (coordSpaceY == Marker.CoordSpace.DataView) {
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
        else if (coordSpaceY == Marker.CoordSpace.ChartView) {
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

        // Convert X/Y from DataView to ChartView coords and return rect
        markX += dataView.getX();
        markY += dataView.getY();
        return new Rect(markX, markY, markW, markH);
    }

    /**
     * Returns the MinMax for all StagedData on given axis.
     */
    private MinMax getStagedDataMinMaxYForAxisY(AxisType anAxisType)
    {
        ChartHelper chartHelper = getChartHelper();
        DataArea[] dataAreas = chartHelper.getDataAreas();
        double min = Float.MAX_VALUE;
        double max = -Float.MAX_VALUE;
        for (DataArea dataArea : dataAreas) {
            DataSet dataSet = dataArea.getDataSet();
            if (dataSet.getAxisTypeY() != anAxisType || dataSet.isDisabled())
                continue;
            DataStore stagedData = dataArea.getStagedData();
            min = Math.min(min, stagedData.getMinY());
            max = Math.max(max, stagedData.getMaxY());
        }
        if (min == Float.MAX_VALUE)
            return new MinMax(0, 1);
        return new MinMax(min, max);
    }
}
