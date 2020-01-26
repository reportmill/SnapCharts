package snap.charts;
//import org.teavm.jso.JSBody;
//import org.teavm.jso.JSObject;
//import snap.util.SnapUtils;
import snap.view.ViewUtils;
import snap.web.WebURL;

/**
 * A custom class.
 */
public class App {
    
    // The last created chart pane
    static ChartPane  _chartPane;

public static void main(String args[])
{
   //snaptea.TV.set();
    //if(SnapUtils.isTeaVM) {
    //    showChart();       //chartPane.getWindow().setMaximized(true);
    //} else
    
    ViewUtils.runLater(() -> {
        ChartPane chartPane = new ChartPane();
        chartPane.setWindowVisible(true);
        String jsonText = WebURL.getURL(App.class, "Sample.json").getText();
        //String jsonText = WebURL.getURL("/Temp/ChartSamples/ColBasic.json").getText();
        //String jsonText = WebURL.getURL("/Users/jeff/dev/ChartSamples/ColBasic3D.json").getText();
        //String jsonText = WebURL.getURL("/Temp/ChartSamples/PieBasic.json").getText();
        //String jsonText = WebURL.getURL("/Temp/ChartSamples/ThriveLeads.json").getText();
        chartPane._chartView.loadFromString(jsonText);
    });
}

/*public static void showChart(String anId, JSObject aMap)
{
    snaptea.TV.set();
    ChartPane chartPane = new ChartPane(); chartPane.setWindowVisible(true);
}*/

/*public static void showChart()
{
    // Get args from TeaVM env
    String arg0_containerName = getMainArg0();
    Object arg1_ConfigObject = getMainArg1();
    
    // If ChartPane already set, send arg there
    if(_chartPane!=null && arg0_containerName.startsWith("Action:")) {
        _chartPane.doAction(arg0_containerName); return; }
    
    // Create ad show ChartPane
    _chartPane = new ChartPane();
    if(arg0_containerName!=null) _chartPane.getWindow().setName(arg0_containerName);
    _chartPane.setWindowVisible(true);
    
    // Load chart from JSON string
    if(arg1_ConfigObject instanceof String) { String str = (String)arg1_ConfigObject;
        _chartPane._chartView.loadFromString(str);
    }
}*/

//@JSBody(params = { }, script = "return rmChartsMainArg0;")
public static native String getMainArg0();

//@JSBody(params = { }, script = "return rmChartsMainArg1;")
public static native String getMainArg1();

//@JSBody(params = { "anObj" }, script = "return JSON.stringify(anObj);")
//public static native String getJSON(JSObject anObj);

/**
 * A class to wrap around JavaScript objects to return properties by name.
 */
/*public interface JSMap extends JSObject {
    
    @JSBody(params = "aKey", script = "return this[aKey];")
    public JSMap get(String aKey);
}*/

}