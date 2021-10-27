/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.util.MathUtils;
import snap.view.*;
import snapcharts.model.AxisType;
import snapcharts.model.DataSet;
import snapcharts.model.DataStore;
import snapcharts.model.Marker;
import snapcharts.model.Marker.CoordSpace;
import snapcharts.util.MinMax;

/**
 * This ChartPartView subclass renders a Chart Marker.
 */
public class MarkerView extends ChartPartView<Marker> {

    // The TextView to show Marker text
    private TextArea  _textArea;

    // The ImageView to show Image
    private ImageView  _imageView;

    // The ShapeView to show SVG
    private ShapeView  _shapeView;

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
     * Override to return true if MarkerView in display space (ChartView or DataView).
     */
    @Override
    public boolean isMovable()
    {
        Marker marker = getMarker();
        boolean isDisplaySpaceX = marker.getCoordSpaceX().isDisplaySpace();
        boolean isDisplaySpaceY = marker.getCoordSpaceY().isDisplaySpace();
        return isDisplaySpaceX && isDisplaySpaceY;
    }

    /**
     * Called to handle a move event.
     */
    @Override
    public void processMoveEvent(ViewEvent anEvent, ViewEvent lastEvent)
    {
        // Get change in View X/Y
        double dx = anEvent.getX() - lastEvent.getX();
        double dy = anEvent.getY() - lastEvent.getY();

        // If Marker coords isFractional, convert to fraction
        Marker marker = getMarker();
        if (marker.isFractionalX()) {
            View view = marker.getCoordSpaceX() == CoordSpace.ChartView ? getChartView() : getDataView();
            dx /= view.getWidth();
        }
        if (marker.isFractionalY()) {
            View view = marker.getCoordSpaceY() == CoordSpace.ChartView ? getChartView() : getDataView();
            dy /= view.getHeight();
        }

        // Update new XY
        marker.setX(marker.getX() + dx);
        marker.setY(marker.getY() + dy);
    }

    /**
     * Override to return true if MarkerView in display space (ChartView or DataView).
     */
    @Override
    public boolean isResizable()
    {
        return isMovable();
    }

    /**
     * Called to handle a resize event.
     */
    @Override
    public void processResizeEvent(ViewEvent anEvent, ViewEvent lastEvent, Pos aHandlePos)
    {
        // Get change in View X/Y
        double dx = anEvent.getX() - lastEvent.getX();
        double dy = anEvent.getY() - lastEvent.getY();

        // If Marker coords isFractional, convert to fraction
        Marker marker = getMarker();
        if (marker.isFractionalX()) {
            View view = marker.getCoordSpaceX() == CoordSpace.ChartView ? getChartView() : getDataView();
            dx /= view.getWidth();
        }
        if (marker.isFractionalY()) {
            View view = marker.getCoordSpaceY() == CoordSpace.ChartView ? getChartView() : getDataView();
            dy /= view.getHeight();
        }

        // Calculate change in View Width/Height for handle (and maybe adjust change in X/Y)
        double dw = 0;
        double dh = 0;
        switch (aHandlePos.getHPos()) {
            case LEFT: dw = -dx; break;
            case CENTER: dx = 0; break;
            case RIGHT: dw = dx; dx = 0; break;
        }
        switch (aHandlePos.getVPos()) {
            case TOP: dh = -dy; break;
            case CENTER: dy = 0; break;
            case BOTTOM: dh = dy; dy = 0; break;
        }

        // Update new bounds
        marker.setX(marker.getX() + dx);
        marker.setY(marker.getY() + dy);
        marker.setWidth(marker.getWidth() + dw);
        marker.setHeight(marker.getHeight() + dh);
    }

    /**
     * Sets Marker CoordSpaceX and FractionalX while preserving the effective X/Width of marker in ChartView coords.
     */
    public void setCoordSpaceX(Marker.CoordSpace aCoordSpace, boolean isFractional)
    {
        // Get marker - if values already set, just return
        Marker marker = getMarker();
        if (marker.getCoordSpaceX() == aCoordSpace && marker.isFractionalX() == isFractional)
            return;

        // Get X/Width of marker for new CoordSpace + Fractional
        Rect viewBounds = getMarkerBoundsInChartViewCoords();
        double markX = mapChartViewToCoordSpaceX(aCoordSpace, isFractional, viewBounds.x);
        double markW = mapChartViewToCoordSpaceX(aCoordSpace, isFractional, viewBounds.getMaxX()) - markX;

        // Set new CoordSpace, Fractional, X, Width
        marker.setCoordSpaceX(aCoordSpace);
        marker.setFractionalX(isFractional);
        marker.setX(markX);
        marker.setWidth(markW);
    }

    /**
     * Sets Marker CoordSpaceY and FractionalY while preserving the effective Y/Height of marker in ChartView coords.
     */
    public void setCoordSpaceY(Marker.CoordSpace aCoordSpace, boolean isFractional)
    {
        // Get marker - if values already set, just return
        Marker marker = getMarker();
        if (marker.getCoordSpaceY() == aCoordSpace && marker.isFractionalY() == isFractional)
            return;

        // Get Y/Height of marker for new CoordSpace + Fractional
        Rect viewBounds = getMarkerBoundsInChartViewCoords();
        double markY = mapChartViewToCoordSpaceY(aCoordSpace, isFractional, viewBounds.y);
        double markH = mapChartViewToCoordSpaceY(aCoordSpace, isFractional, viewBounds.getMaxY()) - markY;
        if (markH < 0) {
            markY -= markH;
            markH = -markH;
        }

        // Set new CoordSpace, Fractional, Y, Height
        marker.setCoordSpaceY(aCoordSpace);
        marker.setFractionalY(isFractional);
        marker.setY(markY);
        marker.setHeight(markH);
    }

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

        // Layout Image
        if (marker.getImage() != null)
            layoutImageView();

        // Layout SVG
        if (marker.getSVG() != null)
            layoutSVGView();

        // If Text is set, update TextArea properties and layout
        String text = marker.getText();
        if (text != null && text.length() > 0) {

            // Update TextArea
            _textArea.setText(text);
            _textArea.setFont(marker.getFont());
            _textArea.setVisible(true);
            _textArea.setWrapLines(marker.isFitTextToBounds());

            // Layout TextArea
            layoutTextArea();
        }

        // Otherwise, hide text
        else _textArea.setVisible(false);
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
     * Does layout for Image.
     */
    private void layoutImageView()
    {
        Marker marker = getMarker();
        Image image = marker.getImage();

        // If no ImageView, create/add ImageView
        if (_imageView == null) {
            _imageView = new ImageView(image);
            _imageView.setFillWidth(true);
            _imageView.setFillHeight(true);
            addChild(_imageView, 0);
        }

        // Make sure image is up to date
        _imageView.setImage(image);

        // Get marker area bounds
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();

        // Set ImageView bounds
        _imageView.setBounds(areaX, areaY, areaW, areaH);
    }

    /**
     * Does layout for SVG.
     */
    private void layoutSVGView()
    {
        Marker marker = getMarker();

        // If no ImageView, create/add ImageView
        if (_shapeView == null) {
            String svgText = marker.getSVG();
            Path path = Path.getPathFromSVG(svgText);
            _shapeView = new ShapeView(path);
            _shapeView.setFillWidth(true);
            _shapeView.setFillHeight(true);
            _shapeView.setBorder(Color.RED, 5);
            addChild(_shapeView, 0);
        }

        // Make sure image is up to date
        //_imageView.setImage(image);
        _shapeView.setFill(marker.getFill());
        _shapeView.setBorder(marker.getBorder());
        setFill(null);
        setBorder(null);

        // Get marker area bounds
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();

        // Set ImageView bounds
        _shapeView.setBounds(areaX, areaY, areaW, areaH);
    }

    /**
     * Returns the bounds of the Marker in ChartView coords.
     */
    public Rect getMarkerBoundsInChartViewCoords()
    {
        // Get Marker and Marker bounds
        Marker marker = getMarker();
        double markX = marker.getX();
        double markY = marker.getY();
        double markW = marker.getWidth();
        double markH = marker.getHeight();

        // Get bounds for Marker in ChartView
        Marker.CoordSpace coordSpaceX = marker.getCoordSpaceX();
        Marker.CoordSpace coordSpaceY = marker.getCoordSpaceY();
        boolean isFractionalX = marker.isFractionalX();
        boolean isFractionalY = marker.isFractionalY();
        double viewX = mapCoordSpaceToChartViewX(coordSpaceX, isFractionalX, markX);
        double viewY = mapCoordSpaceToChartViewY(coordSpaceY, isFractionalY, markY);
        double viewW = mapCoordSpaceToChartViewX(coordSpaceX, isFractionalX, markX + markW) - viewX;
        double viewH = mapCoordSpaceToChartViewY(coordSpaceY, isFractionalY, markY + markH) - viewY;
        if (viewH < 0) {
            viewY -= viewH;
            viewH = -viewH;
        }

        // Return rect
        return new Rect(viewX, viewY, viewW, viewH);
    }

    /**
     * Converts the given ChartView X coord to given CoordSpace and fractional.
     */
    private double mapCoordSpaceToChartViewX(Marker.CoordSpace aCoordSpace, boolean isFractional, double aValue)
    {
        // Get ChartView, DataView
        ChartHelper chartHelper = getChartHelper();
        DataView dataView = chartHelper.getDataView();
        double dataViewX = dataView.getX();

        // Handle CoordSpaceX Axis
        AxisType axisTypeX = aCoordSpace.getAxisType();
        if (axisTypeX != null) {
            double dataX = aValue;
            if (isFractional) {
                DataArea[] dataAreas = chartHelper.getDataAreas();
                DataArea dataArea = dataAreas.length > 0 ? dataAreas[0] : null;
                MinMax dataRangeX = dataArea.getStagedData().getMinMaxX();
                dataX = dataRangeX.mapFractionalToRangeValue(dataX);
            }
            double dispX = chartHelper.dataToView(axisTypeX, dataX);
            return dispX + dataViewX;
        }

        // Handle CoordSpaceX DataBounds
        if (aCoordSpace == Marker.CoordSpace.DataView) {
            double dispX = aValue;
            if (isFractional)
                dispX = MathUtils.mapFractionalToRangeValue(dispX, 0, dataView.getWidth());
            return dispX + dataViewX;
        }

        // Handle CoordSpaceX ChartBounds
        if (aCoordSpace == Marker.CoordSpace.ChartView) {
            double dispX = aValue;
            if (isFractional)
                dispX = MathUtils.mapFractionalToRangeValue(dispX, 0, getChartView().getWidth());
            return dispX;
        }

        // Handle impossible CoordSpace: Throw a fit
        throw new RuntimeException("MarkerView.mapCoordSpaceToChartViewX: Unknown CoordSpace: " + aCoordSpace);
    }

    /**
     * Converts the given ChartView Y coord to given CoordSpace and fractional.
     */
    private double mapCoordSpaceToChartViewY(Marker.CoordSpace aCoordSpace, boolean isFractional, double aValue)
    {
        // Get ChartView, DataView
        ChartHelper chartHelper = getChartHelper();
        ChartView chartView = getChartView();
        DataView dataView = chartHelper.getDataView();
        double dataViewY = dataView.getY();

        // Handle CoordSpaceY Axis
        AxisType axisTypeY = aCoordSpace.getAxisType();
        if (axisTypeY != null) {
            DataArea dataArea = chartHelper.getDataAreaForAxisTypeY(axisTypeY);
            if (dataArea == null) {
                System.err.println("MarkerView.mapCoordSpaceToChartViewY: Invalid Y axis: " + axisTypeY);
                return 0;
            }
            double dataY = aValue;
            if (isFractional) {
                MinMax dataMinMax = getStagedDataMinMaxYForAxisY(axisTypeY);
                dataY = dataMinMax.mapFractionalToRangeValue(dataY);
            }
            double dispY = dataArea.dataToViewY(dataY);
            return dispY + dataViewY;
        }

        // Handle CoordSpaceY DataBounds
        else if (aCoordSpace == Marker.CoordSpace.DataView) {
            double dispY = aValue;
            if (isFractional)
                dispY = MathUtils.mapFractionalToRangeValue(dispY, 0, dataView.getHeight());
            return dispY + dataViewY;
        }

        // Handle CoordSpaceY ChartBounds
        else if (aCoordSpace == Marker.CoordSpace.ChartView) {
            double dispY = aValue;
            if (isFractional)
                dispY = MathUtils.mapFractionalToRangeValue(dispY, 0, chartView.getHeight());
            return dispY;
        }

        // Handle impossible CoordSpace: Throw a fit
        throw new RuntimeException("MarkerView.mapCoordSpaceToChartViewY: Unknown CoordSpace: " + aCoordSpace);
    }

    /**
     * Converts the given ChartView X coord to given CoordSpace and fractional.
     */
    private double mapChartViewToCoordSpaceX(Marker.CoordSpace aCoordSpace, boolean isFractional, double aValue)
    {
        // Get ChartView, DataView
        ChartHelper chartHelper = getChartHelper();
        DataView dataView = chartHelper.getDataView();
        double dataViewX = dataView.getX();

        // Handle CoordSpaceX Axis
        AxisType axisTypeX = aCoordSpace.getAxisType();
        if (axisTypeX != null) {
            double dispX = aValue - dataViewX;
            double dataX = chartHelper.viewToData(axisTypeX, dispX);
            if (isFractional) {
                DataArea[] dataAreas = chartHelper.getDataAreas();
                DataArea dataArea = dataAreas.length > 0 ? dataAreas[0] : null;
                MinMax minMax = dataArea.getStagedData().getMinMaxX();
                dataX = minMax.mapRangeValueToFractional(dataX);
            }
            return dataX;
        }

        // Handle CoordSpaceX DataBounds
        if (aCoordSpace == Marker.CoordSpace.DataView) {
            double dispX = aValue - dataViewX;
            if (isFractional)
                dispX = MathUtils.mapRangeValueToFractional(dispX, 0, dataView.getWidth());
            return dispX;
        }

        // Handle CoordSpaceX ChartBounds
        double dispX = aValue;
        if (isFractional)
            dispX = MathUtils.mapRangeValueToFractional(dispX, 0, getChartView().getWidth());
        return dispX;
    }

    /**
     * Converts the given ChartView Y coord to given CoordSpace and fractional.
     */
    private double mapChartViewToCoordSpaceY(Marker.CoordSpace aCoordSpace, boolean isFractional, double aValue)
    {
        // Get ChartView, DataView
        ChartHelper chartHelper = getChartHelper();
        DataView dataView = chartHelper.getDataView();
        double dataViewY = dataView.getY();

        // Handle CoordSpaceX Axis
        AxisType axisTypeY = aCoordSpace.getAxisType();
        if (axisTypeY != null) {
            double dispY = aValue - dataViewY;
            double dataY = chartHelper.viewToData(axisTypeY, dispY);
            if (isFractional) {
                MinMax minMax = getStagedDataMinMaxYForAxisY(axisTypeY);
                dataY = minMax.mapRangeValueToFractional(dataY);
            }
            return dataY;
        }

        // Handle CoordSpaceX DataBounds
        if (aCoordSpace == Marker.CoordSpace.DataView) {
            double dispY = aValue - dataViewY;
            if (isFractional)
                dispY = MathUtils.mapRangeValueToFractional(dispY, 0, dataView.getHeight());
            return dispY;
        }

        // Handle CoordSpaceX ChartBounds
        double dispY = aValue;
        if (isFractional)
            dispY = MathUtils.mapRangeValueToFractional(dispY, 0, getChartView().getHeight());
        return dispY;
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
