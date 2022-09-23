package snapcharts.notebook;
import snap.web.WebFile;
import snap.web.WebURL;

/**
 * A StaticResolverGen for SnapCharts.
 */
public class StaticResolverGenSC extends javakit.reflect.StaticResolverGen {

    /**
     * Standard main implementation.
     */
    public static void main(String[] args)
    {
        _package = "snapcharts.notebook";
        StaticResolverGenSC codeGen = new StaticResolverGenSC();
        codeGen.generateStaticResolverForClasses(_javaClasses, _whiteListStrings, _blackListStrings);

        WebFile webFile = WebURL.getURL("/tmp/StaticResolver.java").createFile(false);
        webFile.setText(_sb.toString());
        webFile.save();
    }

    // Packages
    private static Class[]  _javaClasses = {

            snapcharts.data.DoubleArray.class,
            snapcharts.data.DataArray.class,
            snapcharts.data.DataSet.class,
            snapcharts.notebook.ChartsREPL.class,
            snapcharts.notebook.Quick3D.class,
    };

    // WhiteList
    protected static String[] _whiteListStrings = {

            // Object
            "clone",

            // DoubleArray
            "length", "map", "filter", "doubleArray", "toArray", "of", "fromMinMax", "fromMinMaxCount",

            // DataArray

            // DataSet

            // ChartsREPL
            "doubleArray", "dataArray", "dataSet", "chart", "chart3D", "minMaxArray", "mapXY",
            "getTextForSource", "getImageForSource",

            // Quick3D
            "createCube", "createImage3D"
    };
    private static String[] _blackListStrings = {

            "java.lang.String.getBytes(int,int,byte[],int)",

    };
}
