package snapcharts.util;
import snap.util.KeyChain;
import snapcharts.model.DataType;
import snapcharts.model.DataStore;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for DataStore.
 */
public class DataStoreUtils {

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
}
