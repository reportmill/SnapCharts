/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.view.ListView;
import snap.view.TextField;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.app.MarkerTool;
import snapcharts.charts.AxisType;
import snapcharts.charts.Chart;
import snapcharts.charts.ChartPart;
import snapcharts.charts.Marker;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartPartView;
import snapcharts.view.ChartView;
import snapcharts.view.MarkerView;

/**
 * A class to manage UI to edit a Chart Marker.
 */
public class MarkerInsp extends ChartPartInsp {

    // The ListView to hold markers
    private ListView<Marker>  _markersListView;

    /**
     * Constructor.
     */
    public MarkerInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Marker Settings"; }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()
    {
        // If selected ChartPart is marker, return it
        ChartPane chartPane = getChartPane();
        ChartPart selPart = chartPane.getSelChartPart();
        if (selPart instanceof Marker)
            return selPart;

        // If there are any markers, return first
        ChartView chartView = chartPane.getChartView();
        MarkerView[] markerViews = chartView.getMarkerViews();
        if (markerViews.length > 0)
            return markerViews[0].getMarker();

        // Return null since no markers are available
        return null;
    }

    /**
     * Returns the selected marker.
     */
    private Marker getSelMarker()
    {
        ChartPane chartPane = getChartPane();
        ChartPart selPart = chartPane.getSelChartPart();
        return selPart instanceof Marker ? (Marker) selPart : null;
    }

    /**
     * Returns the selected marker.
     */
    private MarkerView getSelMarkerView()
    {
        ChartPane chartPane = getChartPane();
        ChartPartView selView = chartPane.getSel().getSelView();
        return selView instanceof MarkerView ? (MarkerView) selView : null;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Get MarkersListView
        _markersListView = getView("MarkersListView", ListView.class);
        _markersListView.setItemTextFunction((m) -> m.getName());
        _markersListView.setMaxHeight(80);
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Marker
        Marker marker = getSelMarker();

        // Reset MarkerListView
        Chart chart = getChart();
        Marker[] markers = chart.getMarkers();
        _markersListView.setItems(markers);
        _markersListView.setSelItem(marker);

        // Reset NameText
        setViewValue("NameText", marker != null ? marker.getName() : null);

        // If no marker, just bail
        if (marker == null)
            return;

        // Reset XSpaceChartButton, XSpaceContentButton, XSpaceXButton
        Marker.CoordSpace coordSpaceX = marker.getCoordSpaceX();
        setViewValue("XSpaceChartButton", coordSpaceX == Marker.CoordSpace.Chart);
        setViewValue("XSpaceContentButton", coordSpaceX == Marker.CoordSpace.Content);
        setViewValue("XSpaceXButton", coordSpaceX == Marker.CoordSpace.X);

        // Reset YSpaceChartButton, YSpaceContentButton, YSpaceYButton, YSpaceY2Button, YSpaceY3Button, YSpaceY4Button
        Marker.CoordSpace coordSpaceY = marker.getCoordSpaceY();
        setViewValue("YSpaceChartButton", coordSpaceY == Marker.CoordSpace.Chart);
        setViewValue("YSpaceContentButton", coordSpaceY == Marker.CoordSpace.Content);
        setViewValue("YSpaceYButton", coordSpaceY == Marker.CoordSpace.Y);
        setViewValue("YSpaceY2Button", coordSpaceY == Marker.CoordSpace.Y2);
        setViewValue("YSpaceY3Button", coordSpaceY == Marker.CoordSpace.Y3);
        setViewValue("YSpaceY4Button", coordSpaceY == Marker.CoordSpace.Y4);

        // Only show YSpace buttons for supported Y axes
        ChartHelper chartHelper = getChartPane().getChartHelper();
        setViewVisible("YSpaceY2Button", chartHelper.isAxisType(AxisType.Y2));
        setViewVisible("YSpaceY3Button", chartHelper.isAxisType(AxisType.Y3));
        setViewVisible("YSpaceY4Button", chartHelper.isAxisType(AxisType.Y4));

        // Reset XText, WText, FractionalXCheckBox
        setViewValue("XText", marker.getX());
        setViewValue("WText", marker.getWidth());
        setViewValue("FractionalXCheckBox", marker.isFractionalX());

        // Reset YText, HText, FractionalYCheckBox
        setViewValue("YText", marker.getY());
        setViewValue("HText", marker.getHeight());
        setViewValue("FractionalYCheckBox", marker.isFractionalY());

        // Reset TextText, TextOutsideXCheckBox, TextOutsideYCheckBox, FitTextToBoundsCheckBox, ShowTextInAxisCheckBox
        setViewValue("TextText", marker.getText());
        setViewValue("TextOutsideXCheckBox", marker.isTextOutsideX());
        setViewValue("TextOutsideYCheckBox", marker.isTextOutsideY());
        setViewValue("FitTextToBoundsCheckBox", marker.isFitTextToBounds());
        setViewValue("ShowTextInAxisCheckBox", marker.isShowTextInAxis());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle MarkersListView
        if (anEvent.equals("MarkersListView")) {
            ChartPane chartPane = getChartPane();
            Marker newMarker = _markersListView.getSelItem();
            chartPane.getSel().setSelChartPart(newMarker);
        }

        // Handle AddMarkerButton, RemoveMarkerButton
        if (anEvent.equals("AddMarkerButton"))
            addMarker();
        if (anEvent.equals("RemoveMarkerButton"))
            removeMarker();

        // Get Marker
        MarkerView markerView = getSelMarkerView();
        Marker marker = getSelMarker();
        if (marker == null)
            return;

        // Handle NameText
        if (anEvent.equals("NameText")) {
            marker.setName(anEvent.getStringValue());
            _markersListView.updateItem(marker);
        }

        // Handle XSpaceChartButton, XSpaceContentButton, XSpaceXButton
        if (anEvent.equals("XSpaceChartButton"))
            markerView.setCoordSpaceX(Marker.CoordSpace.Chart, marker.isFractionalX());
        if (anEvent.equals("XSpaceContentButton"))
            markerView.setCoordSpaceX(Marker.CoordSpace.Content, marker.isFractionalX());
        if (anEvent.equals("XSpaceXButton"))
            markerView.setCoordSpaceX(Marker.CoordSpace.X, marker.isFractionalX());

        // Handle YSpaceChartButton, YSpaceContentButton, YSpaceYButton, YSpaceY2Button, YSpaceY3Button, YSpaceY4Button
        if (anEvent.equals("YSpaceChartButton"))
            markerView.setCoordSpaceY(Marker.CoordSpace.Chart, marker.isFractionalY());
        if (anEvent.equals("YSpaceContentButton"))
            markerView.setCoordSpaceY(Marker.CoordSpace.Content, marker.isFractionalY());
        if (anEvent.equals("YSpaceYButton"))
            markerView.setCoordSpaceY(Marker.CoordSpace.Y, marker.isFractionalY());
        if (anEvent.equals("YSpaceY2Button"))
            markerView.setCoordSpaceY(Marker.CoordSpace.Y2, marker.isFractionalY());
        if (anEvent.equals("YSpaceY3Button"))
            markerView.setCoordSpaceY(Marker.CoordSpace.Y3, marker.isFractionalY());
        if (anEvent.equals("YSpaceY4Button"))
            markerView.setCoordSpaceY(Marker.CoordSpace.Y4, marker.isFractionalY());

        // Handle XText, WText, FractionalXCheckBox
        if (anEvent.equals("XText"))
            marker.setX(anEvent.getFloatValue());
        if (anEvent.equals("WText"))
            marker.setWidth(anEvent.getFloatValue());
        if (anEvent.equals("FractionalXCheckBox"))
            markerView.setCoordSpaceX(marker.getCoordSpaceX(), anEvent.getBoolValue());

        // Handle YText, HText, FractionalYCheckBox
        if (anEvent.equals("YText"))
            marker.setY(anEvent.getFloatValue());
        if (anEvent.equals("HText"))
            marker.setHeight(anEvent.getFloatValue());
        if (anEvent.equals("FractionalYCheckBox"))
            markerView.setCoordSpaceY(marker.getCoordSpaceY(), anEvent.getBoolValue());

        // Handle TextText, TextOutsideXCheckBox, TextOutsideYCheckBox, FitTextToBoundsCheckBox, ShowTextInAxisCheckBox
        if (anEvent.equals("TextText"))
            marker.setText(anEvent.getStringValue());
        if (anEvent.equals("TextOutsideXCheckBox"))
            marker.setTextOutsideX(anEvent.getBoolValue());
        if (anEvent.equals("TextOutsideYCheckBox"))
            marker.setTextOutsideY(anEvent.getBoolValue());
        if (anEvent.equals("FitTextToBoundsCheckBox"))
            marker.setFitTextToBounds(anEvent.getBoolValue());
        if (anEvent.equals("ShowTextInAxisCheckBox"))
            marker.setShowTextInAxis(anEvent.getBoolValue());
    }

    /**
     * Called when AddMarkerButton is pressed.
     */
    private void addMarker()
    {
        // Create new marker
        Marker marker = MarkerTool.createDefaultMarker(getChart());

        // Add to chart
        Chart chart = getChart();
        chart.addMarker(marker);

        // Select new marker
        ChartPane chartPane = getChartPane();
        chartPane.getSel().setSelChartPart(marker);

        // Select NameText
        runLater(() -> {
            requestFocus("NameText");
            getView("NameText", TextField.class).selectAll();
        });
    }

    /**
     * Called when RemoveMarkerButton is pressed.
     */
    private void removeMarker()
    {
        // Get selected marker
        Marker marker = getSelMarker();
        if (marker == null)
            return;

        // Get marker index
        Chart chart = getChart();
        int index = chart.removeMarker(marker);

        // Reset selection (either new marker at that index, or previous marker)
        if (index >= 0) {

            // If marker was at end, set index to previous
            Marker[] markers = chart.getMarkers();
            if (index >= markers.length)
                index--;

            // If new index is valid, select marker at index, otherwise chart
            ChartPart newSelPart = index >= 0 ? markers[index] : chart;
            ChartPane chartPane = getChartPane();
            chartPane.getSel().setSelChartPart(newSelPart);
        }
    }
}