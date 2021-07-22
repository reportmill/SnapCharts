package snapcharts.util;
import snap.util.ListSel;
import snap.util.SnapUtils;
import snapcharts.model.DataSet;
import snapcharts.model.DataType;

/**
 * Utility methods for DataSet class.
 */
public class DataSetUtils {

    /**
     * Deletes data for given DataSet and selection.
     */
    public static void deleteDataForSelection(DataSet aDataSet, ListSel aSel)
    {
        int[] indexes = aSel.getIndexes();
        for (int i = indexes.length - 1; i >= 0; i--) {
            int ind = indexes[i];
            if (ind < aDataSet.getPointCount())
                aDataSet.removePoint(ind);
        }
    }

    /**
     * Replaces data for given DataSet and selection.
     */
    public static void replaceDataForSelection(DataSet aDatatSet, ListSel aSel, String[][] theCells)
    {
        DataType dataType = aDatatSet.getDataType();
        int[] indexes = aSel.getIndexes();

        // Remove currently selected cells
        for (int i=indexes.length-1; i>=0; i--) {
            int ind = indexes[i];
            if (ind<aDatatSet.getPointCount())
                aDatatSet.removePoint(ind);
        }

        // Update DataType
        if (dataType==DataType.UNKNOWN || aDatatSet.getPointCount()==0) {
            dataType = DataUtils.guessDataType(theCells);
        }

        // Add Cells
        for (String[] line : theCells) {

            if (line.length == 0) continue;

            // Get vals: If only one val on line it's Y, X is index
            String valX = line.length > 1 ? line[0] : null;
            String valY = line.length > 1 ? line[1] : line[0];

            switch (dataType) {

                case IY: {
                    double y = valY != null ? SnapUtils.doubleValue(valY) : 0;
                    aDatatSet.addPointXYZC(null, y, null, null);
                    break;
                }

                case XY: {
                    double x = valX != null ? SnapUtils.doubleValue(valX) : 0;
                    double y = valY != null ? SnapUtils.doubleValue(valY) : 0;
                    aDatatSet.addPointXYZC(x, y, null, null);
                    break;
                }

                case CY: {
                    double y = valY != null ? SnapUtils.doubleValue(valY) : 0;
                    aDatatSet.addPointXYZC(null, y, null, valX);
                    break;
                }

                default:
                    System.out.println("DataSet.replaceData: Unsupported data type: " + dataType);
                    return;
            }
        }
    }
}