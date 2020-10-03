package snapcharts.apptools;

import snap.view.ListView;
import snap.view.ViewOwner;

public class MiscInsp extends ViewOwner {

    // Points of interest
    public static final String POI_FirstPoint = "First Point";
    public static final String POI_LastPoint = "Last Point";
    public static final String POI_LocalMin = "Local Min";
    public static final String POI_LocalMax = "Local Max";
    public static final String POI_SlopeMin = "Slope Min";
    public static final String POI_SlopeMax = "Slope Max";

    // POI array
    public static String ALL_POI[] = { POI_FirstPoint, POI_LastPoint, POI_LocalMin, POI_LocalMax, POI_SlopeMin, POI_SlopeMax };

    @Override
    protected void initUI()
    {
        ListView<String> listView = getView("ListView", ListView.class);
        listView.setItems(ALL_POI);
    }
}
