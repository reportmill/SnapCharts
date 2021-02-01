package snapcharts.model;
import snap.geom.Pos;
import snap.util.SnapUtils;
import java.util.Arrays;

/**
 * Returns a Chart for given string.
 */
public class DocTextReader {

    // The Doc
    private Doc  _doc;

    // The current item group
    private DocItemGroup  _itemGroup;

    // The current chart
    private Chart  _chart;

    // The current DataSet
    private DataSet _dset;

    // Staged data arrays X/Y/Z
    private double[]  _dataX, _dataY, _dataZ;

    // Staged data array for C (text)
    private String[]  _dataC;

    /**
     * Constructor.
     */
    public DocTextReader()
    {
        _doc = new Doc();
        _itemGroup = _doc;
    }

    /**
     * Returns the Doc for given string.
     */
    public Doc getDocForString(String aStr)
    {
        readString(aStr);
        return _doc;
    }

    /**
     * Reads a string.
     */
    public void readString(String aStr)
    {
        // Get string lines
        String lines[] = aStr.split("\n");

        // Iterate over lines
        for (String line : lines) {

            // Get line split around '=' sign
            String keyVal[] = line.split("=");
            if (keyVal.length<2) continue;

            // Get key, val
            String key = keyVal[0];
            String val = keyVal[1];

            // If dataset data is staged, but key not data, apply staged data to current dataset
            boolean isDataStaged = _dataX!=null || _dataY!=null || _dataZ!=null || _dataC!=null;
            if (isDataStaged && !key.startsWith("DataSet.Data")) {
                applyStagedData();
            }

            // Handle Axis
            if (key.startsWith("Axis")) {
                readAxisKeyVal(key, val);
                continue;
            }

            // Handle Legend
            if (key.startsWith("Legend")) {
                readLegendKeyVal(key, val);
                continue;
            }

            // Other keys
            switch (key) {

                case "Doc.Name":
                    _doc.setName(val);
                    break;

                case "Group.Name":
                    _itemGroup = new DocItemGroup();
                    _itemGroup.setName(val);
                    _doc.addItem(_itemGroup);
                    break;

                case "Group.ItemsPerPage":
                    int itemsPerPage = SnapUtils.intValue(val);
                    _itemGroup.setItemsPerPage(itemsPerPage);
                    break;

                case "Chart.Name":
                    _chart = new Chart();
                    _chart.setName(val);
                    _itemGroup.addChart(_chart);
                    break;

                case "Chart.Title":
                    _chart.getHeader().setTitle(val);
                    break;

                case "Chart.Subtitle":
                    _chart.getHeader().setSubtitle(val);
                    break;

                case "Chart.ShowLegend":
                    _chart.getLegend().setShowLegend(SnapUtils.boolValue(val));
                    break;

                case "Chart.AxisX.Title":
                    _chart.getAxisX().setTitle(val);
                    break;

                case "Chart.AxisY.Title":
                    _chart.getAxisY().setTitle(val);
                    break;

                case "DataSet.Name":
                    _dset = new DataSet();
                    _dset.setName(val);
                    _chart.addDataSet(_dset);
                    break;

                case "DataSet.ShowSymbols":
                    _dset.setShowSymbols(SnapUtils.boolValue(val));
                    break;

                case "DataSet.DataX":
                    _dataX = getDoubleArrayForString(val);
                    break;

                case "DataSet.DataY":
                    _dataY = getDoubleArrayForString(val);
                    break;

                case "DataSet.DataZ":
                    _dataZ = getDoubleArrayForString(val);
                    break;

                case "DataSet.DataC":
                    _dataC = getStringArrayForString(val);
                    break;

                case "DataSet.AxisY":
                    AxisType axisTypeY = AxisType.valueOf(val);
                    _dset.setAxisTypeY(axisTypeY);
                    break;
            }
        }

        // Apply last data
        boolean isDataStaged = _dataX!=null || _dataY!=null || _dataZ!=null || _dataC!=null;
        if (isDataStaged) {
            applyStagedData();
        }
    }

    /**
     * Reads an axis key/val pair.
     */
    private void readAxisKeyVal(String aKey, String aVal)
    {
        // Get axis and axis key
        int dotInd = aKey.indexOf('.');
        String axisStr = aKey.substring(0, dotInd);
        String axisKey = aKey.substring(dotInd+1);
        String axisTypeStr = axisStr.substring("Axis".length());
        AxisType axisType = AxisType.valueOf(axisTypeStr);
        Axis axis = _chart.getAxisForType(axisType);

        // Handle AxisKey
        switch (axisKey) {

            // Handle Title
            case "Title":
                axis.setTitle(aVal);
                break;

            // Handle Log
            case "Log":
                axis.setLog(SnapUtils.boolValue(aVal));
                break;

            // Handle MinBound
            case "MinBound":
                AxisBound minBound = AxisBound.valueOf(aVal);
                axis.setMinBound(minBound);
                break;

            // Handle MaxBound
            case "MaxBound":
                AxisBound maxBound = AxisBound.valueOf(aVal);
                axis.setMaxBound(maxBound);
                break;

            // Handle MinValue
            case "MinValue":
                double minVal = SnapUtils.doubleValue(aVal);
                axis.setMinBound(AxisBound.VALUE);
                axis.setMinValue(minVal);
                break;

            // Handle MaxValue
            case "MaxValue":
                double maxVal = SnapUtils.doubleValue(aVal);
                axis.setMaxBound(AxisBound.VALUE);
                axis.setMaxValue(maxVal);
                break;

            // Handle unknown
            default: System.err.println("DocTextReader.readAxisKeyVal: Unknown axis key: " + axisKey);
        }
    }

    /**
     * Reads an legend key/val pair.
     */
    private void readLegendKeyVal(String aKey, String aVal)
    {
        // Get legend key and legend
        String legendKey = aKey.substring("Legend.".length());
        Legend legend = _chart.getLegend();

        legend.setShowLegend(true);

        // Handle LegendKey
        switch (legendKey)
        {
            // Handle Position
            case "Position":
                Pos pos = Pos.valueOf(aVal);
                legend.setPosition(pos);
                break;

            // Handle unknown
            default: System.err.println("DocTextReader.readLegendKeyVal: Unknown legend key: " + legendKey);
        }
    }

    /**
     * Applies previously staged data to current dataset.
     */
    private void applyStagedData()
    {
        // Get data type of staged data
        DataType dataType = DataType.getDataType(_dataX!=null, _dataY!=null, _dataZ!=null, _dataC!=null);

        // If no DataSet or DataType, complain and return
        if (_dset==null || dataType==DataType.UNKNOWN) {
            System.err.println("DocTextReader.applyStagedData: No DataSet or unknown type: " + dataType);
            _dset = null; _dataX = _dataY = _dataZ = null; _dataC = null;
            return;
        }

        // Set DataType
        _dset.setDataType(dataType);

        // Add points
        DataUtils.addDataSetPoints(_dset, _dataX, _dataY, _dataZ, _dataC);

        // Clear staged data
        _dataX = _dataY = _dataZ = null; _dataC = null;
    }

    /**
     * Returns an array of double values for given comma separated string.
     */
    public static double[] getDoubleArrayForString(String aStr)
    {
        // Get string, stripped of surrounding non-number chars
        String str = aStr.trim();
        int start = 0; while (start<str.length() && !isNumChar(str, start)) start++;
        int end = str.length(); while(end>0 && !isNumChar(str, end-1)) end--;
        str = str.substring(start, end);

        // Get strings for values separated by comma
        String valStrs[] = str.split("\\s*,\\s*");
        int len = valStrs.length;

        // Create array for return vals
        double vals[] = new double[len];
        int count = 0;

        // Iterate over strings and add valid numbers
        for (String valStr : valStrs) {
            if (valStr.length() > 0) {
                try {
                    double val = Double.parseDouble(valStr);
                    vals[count++] = val;
                }
                catch (Exception e)  { }
            }
        }

        // Return vals (trimmed to size)
        return count<len ? Arrays.copyOf(vals, count) : vals;
    }

    /**
     * Returns an array of String for given comma separated string.
     */
    public static String[] getStringArrayForString(String aStr)
    {
        String str = aStr.trim();
        String valStrs[] = str.split("\\s*,\\s*");
        return valStrs;
    }

    /**
     * Returns whether char at given index in given string is number char.
     */
    private static boolean isNumChar(String aStr, int anIndex)
    {
        char c = aStr.charAt(anIndex);
        return Character.isDigit(c) || c=='.' || c=='-';
    }
}
