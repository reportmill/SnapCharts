package snapcharts.util;
import snap.util.KeyChain;
import snap.util.MathUtils;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSet;
import snapcharts.model.DataType;
import snapcharts.model.DataStore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for DataStore.
 */
public class DataStoreUtils {

    /**
     * Adds data points to given DataSet for given data arrays.
     */
    public static void addDataPoints(DataStore aDataStore, double[] dataX, double[] dataY, double[] dataZ, String[] dataC)
    {
        // Get min length of staged data
        int xlen = dataX != null ? dataX.length : Integer.MAX_VALUE;
        int ylen = dataY != null ? dataY.length : Integer.MAX_VALUE;
        int zlen = dataZ != null ? dataZ.length : Integer.MAX_VALUE;
        int clen = dataC != null ? dataC.length : Integer.MAX_VALUE;
        int len = Math.min(xlen, Math.min(ylen, Math.min(zlen, clen)));

        // Iterate over data arrays and add to DataSet
        for (int i=0; i<len; i++) {
            Double valX = dataX != null ? dataX[i] : null;
            Double valY = dataY != null ? dataY[i] : null;
            Double valZ = dataZ != null ? dataZ[i] : null;
            String valC = dataC != null ? dataC[i] : null;
            DataPoint dataPoint = new DataPoint(valX, valY, valZ, valC);
            int index = aDataStore.getPointCount();
            aDataStore.addPoint(dataPoint, index);
        }
    }

    /**
     * Adds data points to given DataSet for given data arrays.
     */
    public static void addDataPointsXYZZ(DataStore aDataStore, double[] dataX, double[] dataY, double[] dataZZ)
    {
        // Get rows and cols
        int colCount = dataX.length;
        int rowCount = dataY.length;

        // Set in DataStore
        aDataStore.setColCount(colCount);
        aDataStore.setRowCount(rowCount);

        // If insufficient Z, complain and pad with zero
        int pointCount = colCount * rowCount;
        if (pointCount>dataZZ.length) {
            System.err.println("DataStoreUtils.addDataPointsXYZZ: Insufficient number of Z values");
            dataZZ = Arrays.copyOf(dataZZ, pointCount);
        }

        // Iterate over rows/cols and add points
        for (int row=0; row<rowCount; row++) {
            for (int col=0; col<colCount; col++) {
                double xval = dataX[col];
                double yval = dataY[row];
                int zind = row * colCount + col;
                double zval = dataZZ[zind];
                DataPoint dataPoint = new DataPoint(xval, yval, zval, null);
                int index = aDataStore.getPointCount();
                aDataStore.addPoint(dataPoint, index);
            }
        }
    }

    /**
     * Returns the index of the first value that is inside or inside adjacent for given min/max.
     */
    public static int getStartIndexForRange(DataStore aDataStore, double aMin, double aMax)
    {
        int start = 0;
        int pointCount = aDataStore.getPointCount();
        while (start<pointCount && !isArrayValueAtIndexInsideOrInsideAdjacent(aDataStore, start, pointCount, aMin, aMax))
            start++;
        return start;
    }

    /**
     * Returns the index of the last value that is inside or inside adjacent for given min/max.
     */
    public static int getEndIndexForRange(DataStore aDataStore, double aMin, double aMax)
    {
        int pointCount = aDataStore.getPointCount();
        int end = pointCount - 1;
        while (end>0 && !isArrayValueAtIndexInsideOrInsideAdjacent(aDataStore, end, pointCount, aMin, aMax))
            end--;
        return end;
    }

    /**
     * Returns true if given data/index value is inside range or adjacent to point inside.
     */
    private static final boolean isArrayValueAtIndexInsideOrInsideAdjacent(DataStore aDataStore, int i, int pointCount, double aMin, double aMax)
    {
        // If val at index in range, return true
        double val = aDataStore.getX(i);
        if (val >= aMin && val <= aMax)
            return true;

        // If val at next index in range, return true
        if (i+1 < pointCount)
        {
            double nextVal = aDataStore.getX(i + 1);
            if (val < aMin && nextVal >= aMin || val > aMax && nextVal <= aMax)
                return true;
        }

        // If val at previous index in range, return true
        if (i > 0)
        {
            double prevVal = aDataStore.getX(i - 1);
            if ( val < aMin && prevVal >= aMin || val > aMax && prevVal <= aMax)
                return true;
        }

        // Return false since nothing in range
        return false;
    }

    /**
     * Returns a copy of given DataStore processed with given expressions.
     */
    public static DataStore getProcessedData(DataStore aDataStore, String exprX, String exprY, String exprZ)
    {
        // If both expressions empty, just return
        boolean isEmptyX = exprX == null || exprX.length() == 0;
        boolean isEmptyY = exprY == null || exprY.length() == 0;
        boolean isEmptyZ = exprZ == null || exprZ.length() == 0;
        if (isEmptyX && isEmptyY && isEmptyZ)
            return aDataStore;

        // Get KeyChains
        KeyChain keyChainX = !isEmptyX ? KeyChain.getKeyChain(exprX.toLowerCase()) : null;
        KeyChain keyChainY = !isEmptyY ? KeyChain.getKeyChain(exprY.toLowerCase()) : null;
        KeyChain keyChainZ = !isEmptyZ ? KeyChain.getKeyChain(exprZ.toLowerCase()) : null;

        // Get DataX
        DataType dataType = aDataStore.getDataType();
        int pointCount = aDataStore.getPointCount();
        boolean hasZ = dataType.hasZ();
        double[] dataX = new double[pointCount];
        double[] dataY = new double[pointCount];
        double[] dataZ = hasZ ? new double[pointCount] : null;
        Map map = new HashMap();
        for (int i=0; i<pointCount; i++) {
            double valX = aDataStore.getX(i);
            double valY = aDataStore.getY(i);
            double valZ = hasZ ? aDataStore.getZ(i) : 0;
            map.put("x", valX);
            map.put("y", valY);
            if (hasZ)
                map.put("z", valZ);

            dataX[i] = isEmptyX ? valX : KeyChain.getDoubleValue(map, keyChainX);
            dataY[i] = isEmptyY ? valY : KeyChain.getDoubleValue(map, keyChainY);
            if (hasZ)
                dataZ[i] = isEmptyZ ? valZ : KeyChain.getDoubleValue(map, keyChainZ);
        }

        // Return new DataStore for type and values
        return DataStore.newDataStoreForTypeAndValues(dataType, dataX, dataY, dataZ);
    }

    /**
     * Returns DataStore for given polar type.
     */
    public static DataStore getPolarDataForType(DataStore aDataStore, DataType aDataType)
    {
        // If already polar, just return
        if (aDataStore.getDataType().isPolar())
            return aDataStore;

        // Complain if DataType arg isn't polar
        if (!aDataType.isPolar())
            throw new IllegalArgumentException("DataStoreUtils.getPolarDataForType: Come on, man: " + aDataType);

        // Otherwise, get DataX array and create dataT array
        int count = aDataStore.getPointCount();
        double dataT[] = new double[count];

        // Get min/max X to scale to polar
        double minX = aDataStore.getMinX();
        double maxX = aDataStore.getMaxX();
        double maxAngle = 2 * Math.PI; // 360 degrees

        // Iterate over X values and convert to 0 - 360 scale
        for (int i=0;i<count;i++) {
            double valX = aDataStore.getX(i);
            double valTheta = (valX - minX) / (maxX - minX) * maxAngle;
            dataT[i] = valTheta;
        }

        // Get DataR and DataZ
        double[] dataR = aDataStore.getDataY();
        double[] dataZ = aDataStore.getDataType().hasZ() ? aDataStore.getDataZ() : null;
        if (aDataType.hasZ() && dataZ==null)
            dataZ = new double[count];

        // Return new DataStore for type and values
        return DataStore.newDataStoreForTypeAndValues(aDataType, dataT, dataR, dataZ);
    }

    /**
     * Returns DataStore for given polar type.
     */
    public static DataStore getPolarXYDataForPolar(DataStore aDataStore)
    {
        // If already non-polar, just return
        if (!aDataStore.getDataType().isPolar())
            return aDataStore;

        // Otherwise, get DataX array and create dataT array
        int count = aDataStore.getPointCount();
        double dataX[] = new double[count];
        double dataY[] = new double[count];

        // Iterate over X values and convert to 0 - 360 scale
        for (int i=0;i<count;i++) {
            double valT = aDataStore.getT(i);
            double valR = aDataStore.getR(i);
            dataX[i] = Math.cos(valT) * valR;
            dataY[i] = Math.sin(valT) * valR;
        }

        // Get DataZ and DataType
        double[] dataZ = aDataStore.getDataType().hasZ() ? aDataStore.getDataZ() : null;
        DataType dataType = dataZ == null ? DataType.XY : DataType.XYZ;

        // Return new DataStore for type and values
        return DataStore.newDataStoreForTypeAndValues(dataType, dataX, dataY, dataZ);
    }

    /**
     * Adds stacked data
     */
    public static DataStore addStackedData(DataStore aDataStore1, DataStore aDataStore2)
    {
        // Get new dataStore
        DataStore dataStore = aDataStore1.clone();

        // If DataStores have identical DataX, just add Y values
        if (isAlignedX(aDataStore1, aDataStore2)) {
            int pointCount = aDataStore1.getPointCount();
            for (int i=0; i<pointCount; i++) {
                double y1 = aDataStore1.getY(i);
                double y2 = aDataStore2.getY(i);
                double y3 = y1 + y2;
                dataStore.setValueY(y3, i);
            }
        }

        // Otherwise, we must use interpolated values
        else {
            int pointCount = aDataStore1.getPointCount();
            for (int i=0; i<pointCount; i++) {
                double x1 = aDataStore1.getX(i);
                double y1 = aDataStore1.getY(i);
                double y2 = aDataStore2.getYForX(x1);
                double y3 = y1 + y2;
                dataStore.setValueY(y3, i);
            }
        }

        // Return new DataStore
        return dataStore;
    }

    /**
     * Returns whether given DataStore is has same X values as this one.
     */
    public static boolean isAlignedX(DataStore aDataStore1, DataStore aDataStore2)
    {
        // If PointCounts don't match, return false
        int pointCount = aDataStore1.getPointCount();
        if (pointCount != aDataStore2.getPointCount())
            return false;

        // Iterate over X coords and return false if they don't match
        for (int i=0; i<pointCount; i++) {
            double x0 = aDataStore1.getX(i);
            double x1 = aDataStore2.getX(i);
            if (!MathUtils.equals(x0, x1))
                return false;
        }

        // Return true since X coords are aligned
        return true;
    }
}
