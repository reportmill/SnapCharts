package snapcharts.view;
import snap.geom.HPos;
import snap.geom.Insets;
import snap.geom.Rect;
import snap.geom.VPos;
import snap.util.MathUtils;
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
        setPickable(false);

        // Create/add TextArea
        _textArea = new TextArea();
        _textArea.setPlainText(true);
        addChild(_textArea);
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
        // Do normal version
        super.resetView();

        // Get Marker
        Marker marker = getMarker();

        // If showing text in axis, just return
        if (marker.isShowTextInAxis()) {
            _textArea.setVisible(false);
            return;
        }

        // Update TextArea properties
        String text = marker.getText();
        if (text == null || text.length() == 0) {
            _textArea.setVisible(false);
            return;
        }

        // Update TextArea
        _textArea.setText(text);
        _textArea.setFont(marker.getFont());
        _textArea.setVisible(true);
        _textArea.setWrapLines(marker.isFitTextToBounds());

        // Layout TextArea
        layoutTextArea();
    }

    /**
     * Resets the paint properties from Marker in view.
     */
    private void layoutTextArea()
    {
        // Handle simple FitTextToBounds
        Marker marker = getMarker();
        if (marker.isFitTextToBounds()) {
            layoutTextAreaFitToBounds();
            return;
        }

        // Get marker size
        double markerW = getWidth();
        double markerH = getHeight();

        // Get marker area bounds
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = markerW - ins.getWidth();
        double areaH = markerH - ins.getHeight();

        // If area width or height less than zero, center bounds
        if (areaW < 0) {
            areaX = markerW / 2;
            areaW = 0;
        }
        if (areaH < 0) {
            areaY = markerH / 2;
            areaH = 0;
        }

        // Calculate TextArea bounds
        double textX = areaX;
        double textY = areaY;
        double textW = _textArea.getPrefWidth();
        double textH = _textArea.getPrefHeight();

        // If there is extra space, shift by content
        double extraX = areaW - textW;
        double extraY = areaH - textH;
        if (extraX != 0 || extraY != 0) {
            double alignX = marker.getAlignX().doubleValue();
            double alignY = marker.getAlignY().doubleValue();
            textX += alignX * extraX;
            textY += alignY * extraY;
        }

        // Set bounds
        _textArea.setBounds(textX, textY, textW, textH);

        // Handle TextOutsideX
        if (marker.isTextOutsideX()) {
            if (marker.getAlignX() == HPos.LEFT)
                _textArea.setX(-textW - ins.right);
            else if (marker.getAlignX() == HPos.RIGHT)
                _textArea.setX(markerW + ins.left);
        }

        // Handle TextOutsideY
        if (marker.isTextOutsideY()) {
            if (marker.getAlignY() == VPos.TOP)
                _textArea.setY(-textH - ins.bottom);
            else if (marker.getAlignY() == VPos.BOTTOM)
                _textArea.setY(markerH + ins.top);
        }
    }

    /**
     * Layout Text area for FitTextToBounds: Fit in bounds.
     */
    private void layoutTextAreaFitToBounds()
    {
        // Get marker size
        double markerW = getWidth();
        double markerH = getHeight();

        // Get marker area bounds
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = markerW - ins.getWidth();
        double areaH = markerH - ins.getHeight();

        // If impossibly small, just hide TextArea
        if (areaW < 5 || areaH < 5) {
            _textArea.setVisible(false);
            return;
        }

        // Otherwise just put text in bounds
        _textArea.setBounds(areaX, areaY, areaW, areaH);

        // Set Align and make sure text fits
        Marker marker = getMarker();
        _textArea.setAlign(marker.getAlign());
        _textArea.setFontScale(1);
        _textArea.scaleTextToFit();
    }

    /**
     * Returns the appropriate bounds for MarkerView in ChartView coords.
     */
    public Rect getPrefBoundsInChartViewCoords()
    {
        // Get ChartView, DataView
        ChartHelper chartHelper = getChartHelper();
        ChartView chartView = getChartView();
        DataView dataView = chartHelper.getDataView();
        double dataViewX = dataView.getX();
        double dataViewY = dataView.getY();

        // Get Marker and Marker bounds
        Marker marker = getMarker();
        double markX = marker.getX();
        double markY = marker.getY();
        double markW = marker.getWidth();
        double markH = marker.getHeight();

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
                dataX0 = MathUtils.mapFractionalToRange(dataX0, dataMin, dataMax);
                dataX1 = MathUtils.mapFractionalToRange(dataX1, dataMin, dataMax);
            }
            markX = dataArea.dataToViewX(dataX0);
            markW = dataArea.dataToViewX(dataX1) - markX;
            markX += dataViewX;
        }

        // Handle CoordSpaceX DataBounds
        else if (coordSpaceX == Marker.CoordSpace.DataView) {
            if (isFractionalX) {
                double dispMin = 0;
                double dispMax = dataView.getWidth();
                markX = dispMin + markX * (dispMax - dispMin);
                markW = markW * (dispMax - dispMin);
            }
            markX += dataViewX;
        }

        // Handle CoordSpaceX ChartBounds
        else if (coordSpaceX == Marker.CoordSpace.ChartView) {
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
                dataY0 = dataMinMax.mapFractional(dataY0);
                dataY1 = dataMinMax.mapFractional(dataY1);
            }
            markY = dataArea.dataToViewY(dataY1);
            double markMaxY = dataArea.dataToViewY(dataY0);
            markH = markMaxY - markY;
            markY += dataViewY;
        }

        // Handle CoordSpaceY DataBounds
        else if (coordSpaceY == Marker.CoordSpace.DataView) {
            if (isFractionalY) {
                double dispMin = 0;
                double dispMax = dataView.getHeight();
                markY = dispMin + markY * (dispMax - dispMin);
                markH = markH * (dispMax - dispMin);
            }
            markY += dataViewY;
        }

        // Handle CoordSpaceY ChartBounds
        else if (coordSpaceY == Marker.CoordSpace.ChartView) {
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
