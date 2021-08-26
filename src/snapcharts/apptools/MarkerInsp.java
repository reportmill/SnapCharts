/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.gfx.Color;
import snap.gfx.Stroke;
import snap.view.ListView;
import snap.view.TextField;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.Marker;
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

        // Reset XSpaceXButton, XSpaceDataButton, XSpaceChartButton
        Marker.CoordSpace coordSpaceX = marker.getCoordSpaceX();
        setViewValue("XSpaceXButton", coordSpaceX == Marker.CoordSpace.X);
        setViewValue("XSpaceDataButton", coordSpaceX == Marker.CoordSpace.DataView);
        setViewValue("XSpaceChartButton", coordSpaceX == Marker.CoordSpace.ChartView);

        // Reset YSpaceYButton, YSpaceY2Button, YSpaceY3Button, YSpaceY4Button, YSpaceDataButton, YSpaceChartButton
        Marker.CoordSpace coordSpaceY = marker.getCoordSpaceY();
        setViewValue("YSpaceYButton", coordSpaceY == Marker.CoordSpace.Y);
        setViewValue("YSpaceY2Button", coordSpaceY == Marker.CoordSpace.Y2);
        setViewValue("YSpaceY3Button", coordSpaceY == Marker.CoordSpace.Y3);
        setViewValue("YSpaceY4Button", coordSpaceY == Marker.CoordSpace.Y4);
        setViewValue("YSpaceDataButton", coordSpaceY == Marker.CoordSpace.DataView);
        setViewValue("YSpaceChartButton", coordSpaceY == Marker.CoordSpace.ChartView);

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
        Marker marker = getSelMarker();
        if (marker == null)
            return;

        // Handle NameText
        if (anEvent.equals("NameText")) {
            marker.setName(anEvent.getStringValue());
            _markersListView.updateItems(marker);
        }

        // Handle XSpaceXButton, XSpaceDataButton, XSpaceChartButton
        if (anEvent.equals("XSpaceXButton"))
            marker.setCoordSpaceX(Marker.CoordSpace.X);
        if (anEvent.equals("XSpaceDataButton"))
            marker.setCoordSpaceX(Marker.CoordSpace.DataView);
        if (anEvent.equals("XSpaceChartButton"))
            marker.setCoordSpaceX(Marker.CoordSpace.ChartView);

        // Handle YSpaceYButton, YSpaceY2Button, YSpaceY3Button, YSpaceY4Button, YSpaceDataButton, YSpaceChartButton
        if (anEvent.equals("YSpaceYButton"))
            marker.setCoordSpaceY(Marker.CoordSpace.Y);
        if (anEvent.equals("YSpaceY2Button"))
            marker.setCoordSpaceY(Marker.CoordSpace.Y2);
        if (anEvent.equals("YSpaceY3Button"))
            marker.setCoordSpaceY(Marker.CoordSpace.Y3);
        if (anEvent.equals("YSpaceY4Button"))
            marker.setCoordSpaceY(Marker.CoordSpace.Y4);
        if (anEvent.equals("YSpaceDataButton"))
            marker.setCoordSpaceY(Marker.CoordSpace.DataView);
        if (anEvent.equals("YSpaceChartButton"))
            marker.setCoordSpaceY(Marker.CoordSpace.ChartView);

        // Handle XText, WText, FractionalXCheckBox
        if (anEvent.equals("XText"))
            marker.setX(anEvent.getFloatValue());
        if (anEvent.equals("WText"))
            marker.setWidth(anEvent.getFloatValue());
        if (anEvent.equals("FractionalXCheckBox"))
            marker.setFractionalX(anEvent.getBoolValue());

        // Handle YText, HText, FractionalYCheckBox
        if (anEvent.equals("YText"))
            marker.setY(anEvent.getFloatValue());
        if (anEvent.equals("HText"))
            marker.setHeight(anEvent.getFloatValue());
        if (anEvent.equals("FractionalYCheckBox"))
            marker.setFractionalY(anEvent.getBoolValue());

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
        Marker marker = createDefaultMarker();

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

    /**
     * Creates a new marker.
     */
    private Marker createDefaultMarker()
    {
        Marker marker = new Marker();
        marker.setCoordSpaceX(Marker.CoordSpace.X);
        marker.setCoordSpaceY(Marker.CoordSpace.Y);
        marker.setFractionalX(true);
        marker.setFractionalY(true);
        marker.setBounds(.45, .45, .1, .1);
        marker.setFill(Color.RED.copyForAlpha(.2));
        marker.setLineColor(Color.RED.copyForAlpha(.2));
        marker.setLineWidth(3);
        marker.setLineDash(Stroke.DASH_DASH);

        // Set name
        for (int i = 1; true; i++) {
            String name = "Marker " + i;
            if (getChart().getMarker(name) == null) {
                marker.setName(name);
                break;
            }
        }

        // Return marker
        return marker;
    }
}