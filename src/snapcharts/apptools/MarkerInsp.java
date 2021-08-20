/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.geom.Pos;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.util.StringUtils;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.ChartPart;
import snapcharts.model.Legend;
import snapcharts.model.Marker;
import snapcharts.view.MarkerView;

/**
 * A class to manage UI to edit a Chart Marker.
 */
public class MarkerInsp extends ChartPartInsp {

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
        ChartPane chartPane = getChartPane();
        ChartPart selPart = chartPane.getSelChartPart();
        if (selPart instanceof Marker)
            return selPart;

        MarkerView[] markerViews = chartPane.getChartView().getMarkerViews();
        if (markerViews.length > 0)
            return markerViews[0].getMarker();

        return null;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Marker
        ChartPane chartPane = getChartPane();
        ChartPart selPart = chartPane.getSelChartPart();
        Marker marker = selPart instanceof Marker ? (Marker) selPart : null;
        if (marker == null)
            return;

        // Reset XSpaceXButton, XSpaceDataButton, XSpaceChartButton
        Marker.CoordSpace coordSpaceX = marker.getCoordSpaceX();
        setViewValue("XSpaceXButton", coordSpaceX == Marker.CoordSpace.X);
        setViewValue("XSpaceDataButton", coordSpaceX == Marker.CoordSpace.DataBounds);
        setViewValue("XSpaceChartButton", coordSpaceX == Marker.CoordSpace.ChartBounds);

        // Reset YSpaceYButton, YSpaceY2Button, YSpaceY3Button, YSpaceY4Button, YSpaceDataButton, YSpaceChartButton
        Marker.CoordSpace coordSpaceY = marker.getCoordSpaceY();
        setViewValue("YSpaceYButton", coordSpaceY == Marker.CoordSpace.Y);
        setViewValue("YSpaceY2Button", coordSpaceY == Marker.CoordSpace.Y2);
        setViewValue("YSpaceY3Button", coordSpaceY == Marker.CoordSpace.Y3);
        setViewValue("YSpaceY4Button", coordSpaceY == Marker.CoordSpace.Y4);
        setViewValue("YSpaceDataButton", coordSpaceY == Marker.CoordSpace.DataBounds);
        setViewValue("YSpaceChartButton", coordSpaceY == Marker.CoordSpace.ChartBounds);

        // Reset XText, WText, FractionalXCheckBox
        setViewValue("XText", marker.getX());
        setViewValue("WText", marker.getWidth());
        setViewValue("FractionalXCheckBox", marker.isFractionalX());

        // Reset YText, HText, FractionalYCheckBox
        setViewValue("YText", marker.getY());
        setViewValue("HText", marker.getHeight());
        setViewValue("FractionalYCheckBox", marker.isFractionalY());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Marker
        ChartPane chartPane = getChartPane();
        ChartPart selPart = chartPane.getSelChartPart();
        Marker marker = selPart instanceof Marker ? (Marker) selPart : null;
        if (marker == null)
            return;

        // Handle XSpaceXButton, XSpaceDataButton, XSpaceChartButton
        if (anEvent.equals("XSpaceXButton"))
            marker.setCoordSpaceX(Marker.CoordSpace.X);
        if (anEvent.equals("XSpaceDataButton"))
            marker.setCoordSpaceX(Marker.CoordSpace.DataBounds);
        if (anEvent.equals("XSpaceChartButton"))
            marker.setCoordSpaceX(Marker.CoordSpace.ChartBounds);

        // Reset YSpaceYButton, YSpaceY2Button, YSpaceY3Button, YSpaceY4Button, YSpaceDataButton, YSpaceChartButton
        if (anEvent.equals("YSpaceYButton"))
            marker.setCoordSpaceY(Marker.CoordSpace.Y);
        if (anEvent.equals("YSpaceY2Button"))
            marker.setCoordSpaceY(Marker.CoordSpace.Y2);
        if (anEvent.equals("YSpaceY3Button"))
            marker.setCoordSpaceY(Marker.CoordSpace.Y3);
        if (anEvent.equals("YSpaceY4Button"))
            marker.setCoordSpaceY(Marker.CoordSpace.Y4);
        if (anEvent.equals("YSpaceDataButton"))
            marker.setCoordSpaceY(Marker.CoordSpace.DataBounds);
        if (anEvent.equals("XSpaceChartButton"))
            marker.setCoordSpaceY(Marker.CoordSpace.ChartBounds);

        // Reset XText, WText, FractionalXCheckBox
        if (anEvent.equals("XText"))
            marker.setX(anEvent.getFloatValue());
        if (anEvent.equals("WText"))
            marker.setWidth(anEvent.getFloatValue());
        if (anEvent.equals("FractionalXCheckBox"))
            marker.setFractionalX(anEvent.getBoolValue());

        // Reset YText, HText, FractionalYCheckBox
        if (anEvent.equals("YText"))
            marker.setY(anEvent.getFloatValue());
        if (anEvent.equals("HText"))
            marker.setHeight(anEvent.getFloatValue());
        if (anEvent.equals("FractionalYCheckBox"))
            marker.setFractionalY(anEvent.getBoolValue());
    }
}