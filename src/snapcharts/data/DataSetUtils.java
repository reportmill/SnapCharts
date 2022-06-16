package snapcharts.data;
import snap.util.KeyChain;
import snap.util.MathUtils;
import snapcharts.view.ChartViewUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for DataSet.
 */
public class DataSetUtils {

    /**
     * Adds data points to given DataSet for given data arrays.
     */
    public static void addDataPoints(DataSet aDataSet, double[] dataX, double[] dataY, double[] dataZ, String[] dataC)
    {
        // Get min length of staged data
        int xlen = dataX != null ? dataX.length : Integer.MAX_VALUE;
        int ylen = dataY != null ? dataY.length : Integer.MAX_VALUE;
        int zlen = dataZ != null ? dataZ.length : Integer.MAX_VALUE;
        int clen = dataC != null ? dataC.length : Integer.MAX_VALUE;
        int len = Math.min(xlen, Math.min(ylen, Math.min(zlen, clen)));
        if (len == Integer.MAX_VALUE)
            return;

        // Iterate over data arrays and add to DataSet
        for (int i = 0; i < len; i++) {
            Double valX = dataX != null ? dataX[i] : null;
            Double valY = dataY != null ? dataY[i] : null;
            Double valZ = dataZ != null ? dataZ[i] : null;
            String valC = dataC != null ? dataC[i] : null;
            DataPoint dataPoint = new DataPoint(valX, valY, valZ, valC);
            int index = aDataSet.getPointCount();
            aDataSet.addPoint(dataPoint, index);
        }
    }

    /**
     * Returns the index of the first value that is inside or inside adjacent for given min/max.
     */
    public static int getStartIndexForRange(DataSet aDataSet, double aMin, double aMax)
    {
        int start = 0;
        int pointCount = aDataSet.getPointCount();
        while (start<pointCount && !isArrayValueAtIndexInsideOrInsideAdjacent(aDataSet, start, pointCount, aMin, aMax))
            start++;
        return start;
    }

    /**
     * Returns the index of the last value that is inside or inside adjacent for given min/max.
     */
    public static int getEndIndexForRange(DataSet aDataSet, double aMin, double aMax)
    {
        int pointCount = aDataSet.getPointCount();
        int end = pointCount - 1;
        while (end>0 && !isArrayValueAtIndexInsideOrInsideAdjacent(aDataSet, end, pointCount, aMin, aMax))
            end--;
        return end;
    }

    /**
     * Returns true if given data/index value is inside range or adjacent to point inside.
     */
    private static final boolean isArrayValueAtIndexInsideOrInsideAdjacent(DataSet aDataSet, int i, int pointCount, double aMin, double aMax)
    {
        // If val at index in range, return true
        double val = aDataSet.getX(i);
        if (val >= aMin && val <= aMax)
            return true;

        // If val at next index in range, return true
        if (i+1 < pointCount)
        {
            double nextVal = aDataSet.getX(i + 1);
            if (val < aMin && nextVal >= aMin || val > aMax && nextVal <= aMax)
                return true;
        }

        // If val at previous index in range, return true
        if (i > 0)
        {
            double prevVal = aDataSet.getX(i - 1);
            if ( val < aMin && prevVal >= aMin || val > aMax && prevVal <= aMax)
                return true;
        }

        // Return false since nothing in range
        return false;
    }

    /**
     * Returns a copy of given DataSet processed with given expressions.
     */
    public static DataSet getProcessedData(DataSet aDataSet, String exprX, String exprY, String exprZ)
    {
        // If both expressions empty, just return
        boolean isEmptyX = exprX == null || exprX.length() == 0;
        boolean isEmptyY = exprY == null || exprY.length() == 0;
        boolean isEmptyZ = exprZ == null || exprZ.length() == 0;
        if (isEmptyX && isEmptyY && isEmptyZ)
            return aDataSet;

        // Get KeyChains
        KeyChain keyChainX = !isEmptyX ? KeyChain.getKeyChain(exprX.toLowerCase()) : null;
        KeyChain keyChainY = !isEmptyY ? KeyChain.getKeyChain(exprY.toLowerCase()) : null;
        KeyChain keyChainZ = !isEmptyZ ? KeyChain.getKeyChain(exprZ.toLowerCase()) : null;

        // Get DataX
        DataType dataType = aDataSet.getDataType();
        int pointCount = aDataSet.getPointCount();
        boolean hasZ = dataType.hasZ();
        double[] dataX = new double[pointCount];
        double[] dataY = new double[pointCount];
        double[] dataZ = hasZ ? new double[pointCount] : null;
        Map map = new HashMap();
        for (int i=0; i<pointCount; i++) {
            double valX = aDataSet.getX(i);
            double valY = aDataSet.getY(i);
            double valZ = hasZ ? aDataSet.getZ(i) : 0;
            map.put("x", valX);
            map.put("y", valY);
            if (hasZ)
                map.put("z", valZ);

            dataX[i] = isEmptyX ? valX : KeyChain.getDoubleValue(map, keyChainX);
            dataY[i] = isEmptyY ? valY : KeyChain.getDoubleValue(map, keyChainY);
            if (hasZ)
                dataZ[i] = isEmptyZ ? valZ : KeyChain.getDoubleValue(map, keyChainZ);
        }

        // Return new DataSet for type and values
        return DataSet.newDataSetForTypeAndValues(dataType, dataX, dataY, dataZ);
    }

    /**
     * Returns a copy of given DataSet with values converted to log.
     */
    public static DataSet getLogData(DataSet aDataSet, boolean doLogX, boolean doLogY)
    {
        // Get DataX
        DataType dataType = aDataSet.getDataType();
        int pointCount = aDataSet.getPointCount();
        boolean hasZ = dataType.hasZ();
        double[] dataX = new double[pointCount];
        double[] dataY = new double[pointCount];
        double[] dataZ = hasZ ? new double[pointCount] : null;
        for (int i=0; i<pointCount; i++) {
            double valX = aDataSet.getX(i);
            double valY = aDataSet.getY(i);
            double valZ = hasZ ? aDataSet.getZ(i) : 0;

            dataX[i] = doLogX ? ChartViewUtils.log10(valX) : valX;
            dataY[i] = doLogY ? ChartViewUtils.log10(valY) : valY;
            if (hasZ)
                dataZ[i] = valZ;
        }

        // Return new DataSet for type and values
        return DataSet.newDataSetForTypeAndValues(dataType, dataX, dataY, dataZ);
    }

    /**
     * Returns DataSet for given polar type.
     */
    public static DataSet getPolarDataForType(DataSet aDataSet, DataType aDataType)
    {
        // If already polar, just return
        if (aDataSet.getDataType().isPolar())
            return aDataSet;

        // Complain if DataType arg isn't polar
        if (!aDataType.isPolar())
            throw new IllegalArgumentException("DataSetUtils.getPolarDataForType: Come on, man: " + aDataType);

        // Otherwise, get DataX array and create dataT array
        int pointCount = aDataSet.getPointCount();
        double[] dataT = new double[pointCount];

        // Get min/max X to scale to polar
        double minX = aDataSet.getMinX();
        double maxX = aDataSet.getMaxX();
        double maxAngle = 2 * Math.PI; // 360 degrees

        // Iterate over X values and convert to 0 - 360 scale
        for (int i = 0; i < pointCount; i++) {
            double valX = aDataSet.getX(i);
            double valTheta = (valX - minX) / (maxX - minX) * maxAngle;
            dataT[i] = valTheta;
        }

        // Get DataR and DataZ
        double[] dataR = aDataSet.getDataY();
        double[] dataZ = aDataSet.getDataType().hasZ() ? aDataSet.getDataZ() : null;
        if (aDataType.hasZ() && dataZ == null)
            dataZ = new double[pointCount];

        // Create new DataSet for type and values and return
        DataSet polarData = DataSet.newDataSetForTypeAndValues(aDataType, dataT, dataR, dataZ);
        polarData.setThetaUnit(DataSet.ThetaUnit.Radians);
        return polarData;
    }

    /**
     * Returns DataSet of XY points for given Polar type DataSet.
     * This is probably bogus since it makes assumptions about the XY range.
     */
    public static DataSet getPolarXYDataForPolar(DataSet aDataSet)
    {
        // If already non-polar, just return
        if (!aDataSet.getDataType().isPolar())
            return aDataSet;

        // Get pointCount and create dataX/dataY arrays
        int pointCount = aDataSet.getPointCount();
        double[] dataX = new double[pointCount];
        double[] dataY = new double[pointCount];

        // Get whether to convert to radians
        boolean convertToRadians = aDataSet.getThetaUnit() != DataSet.ThetaUnit.Radians;

        // Iterate over X values and convert to 0 - 360 scale
        for (int i = 0; i < pointCount; i++) {

            // Get Theta and Radius
            double dataTheta = aDataSet.getT(i);
            double dataRadius = aDataSet.getR(i);
            if (convertToRadians)
                dataTheta = Math.toRadians(dataTheta);

            // Convert to display coords
            dataX[i] = Math.cos(dataTheta) * dataRadius;
            dataY[i] = Math.sin(dataTheta) * dataRadius;
        }

        // Get DataZ and DataType
        double[] dataZ = aDataSet.getDataType().hasZ() ? aDataSet.getDataZ() : null;
        DataType dataType = dataZ == null ? DataType.XY : DataType.XYZ;

        // Return new DataSet for type and values
        return DataSet.newDataSetForTypeAndValues(dataType, dataX, dataY, dataZ);
    }

    /**
     * Adds stacked data
     */
    public static DataSet addStackedData(DataSet aDataSet1, DataSet aDataSet2)
    {
        // Get new DataSet
        DataSet dataSet = aDataSet1.clone();

        // If DataSets have identical DataX, just add Y values
        if (isAlignedX(aDataSet1, aDataSet2)) {
            int pointCount = aDataSet1.getPointCount();
            for (int i = 0; i < pointCount; i++) {
                double y1 = aDataSet1.getY(i);
                double y2 = aDataSet2.getY(i);
                double y3 = y1 + y2;
                dataSet.setValueY(y3, i);
            }
        }

        // Otherwise, we must use interpolated values
        else {
            int pointCount = aDataSet1.getPointCount();
            for (int i = 0; i < pointCount; i++) {
                double x1 = aDataSet1.getX(i);
                double y1 = aDataSet1.getY(i);
                double y2 = getYForX(aDataSet2, x1);
                double y3 = y1 + y2;
                dataSet.setValueY(y3, i);
            }
        }

        // Return new DataSet
        return dataSet;
    }

    /**
     * Returns whether given DataSet is has same X values as this one.
     */
    public static boolean isAlignedX(DataSet aDataSet1, DataSet aDataSet2)
    {
        // If PointCounts don't match, return false
        int pointCount = aDataSet1.getPointCount();
        if (pointCount != aDataSet2.getPointCount())
            return false;

        // Iterate over X coords and return false if they don't match
        for (int i = 0; i < pointCount; i++) {
            double x0 = aDataSet1.getX(i);
            double x1 = aDataSet2.getX(i);
            if (!MathUtils.equals(x0, x1))
                return false;
        }

        // Return true since X coords are aligned
        return true;
    }

    /**
     * Returns the Y value for given X value.
     */
    public static double getYForX(DataSet aDataSet, double aX)
    {
        // If empty, just return
        int pointCount = aDataSet.getPointCount();
        if (pointCount == 0)
            return 0;

        // Get index for given X value
        double[] dataX = aDataSet.getDataX();
        int index = Arrays.binarySearch(dataX, aX);
        if (index >= 0)
            return aDataSet.getY(index);

        // Get lower/upper indexes
        int highIndex = -index - 1;
        int lowIndex = highIndex - 1;

        // If beyond end, just return last Y
        if (highIndex >= pointCount)
            return aDataSet.getY(pointCount - 1);

        // If before start, just return first Y
        if (lowIndex < 0)
            return aDataSet.getY(0);

        // Otherwise, return weighted average
        double x0 = aDataSet.getX(lowIndex);
        double y0 = aDataSet.getY(lowIndex);
        double x1 = aDataSet.getX(highIndex);
        double y1 = aDataSet.getY(highIndex);
        double weightX = (aX - x0) / (x1 - x0);
        double y = weightX * (y1 - y0) + y0;
        return y;
    }
}
