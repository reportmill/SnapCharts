package snapcharts.util;
import snap.util.Convert;
import snap.util.ListSel;
import snapcharts.data.DataPoint;
import snapcharts.data.DataUtils;
import snapcharts.model.Trace;
import snapcharts.data.DataType;

/**
 * Utility methods for Trace class.
 */
public class TraceUtils {

    /**
     * Deletes data for given Trace and selection.
     */
    public static void deleteDataForSelection(Trace aTrace, ListSel aSel)
    {
        int[] indexes = aSel.getIndexes();
        for (int i = indexes.length - 1; i >= 0; i--) {
            int ind = indexes[i];
            if (ind < aTrace.getPointCount())
                aTrace.removePoint(ind);
        }
    }

    /**
     * Replaces data for given Trace and selection.
     */
    public static void replaceDataForSelection(Trace aTrace, ListSel aSel, String[][] theCells)
    {
        DataType dataType = aTrace.getDataType();
        int[] indexes = aSel.getIndexes();

        // Remove currently selected cells
        for (int i = indexes.length-1; i >= 0; i--) {
            int index = indexes[i];
            if (index < aTrace.getPointCount())
                aTrace.removePoint(index);
        }

        // Update DataType
        if (dataType == DataType.UNKNOWN || aTrace.getPointCount() == 0)
            dataType = DataUtils.guessDataType(theCells);

        // Add Cells
        for (String[] line : theCells) {

            if (line.length == 0) continue;

            // Get x/y val strings: If only one val on line it's Y, X is index
            String xStr = line.length > 1 ? line[0] : null;
            String yStr = line.length > 1 ? line[1] : line[0];

            // Get x/y/c vals
            Double valX = null;
            Double valY = null;
            String valC = null;

            // Get DataPoint for DataType
            switch (dataType) {

                case IY: {
                    valY = yStr != null ? Convert.doubleValue(yStr) : 0;
                    break;
                }

                case XY: {
                    valX = xStr != null ? Convert.doubleValue(xStr) : 0;
                    valY = yStr != null ? Convert.doubleValue(yStr) : 0;
                    break;
                }

                case CY: {
                    valY = yStr != null ? Convert.doubleValue(yStr) : 0;
                    valC = xStr;
                    break;
                }

                default:
                    System.out.println("TraceUtils.replaceData: Unsupported data type: " + dataType);
                    return;
            }

            // Create/add data point
            DataPoint dataPoint = new DataPoint(valX, valY, null, valC);
            aTrace.addPoint(dataPoint, aTrace.getPointCount());
        }
    }
}
