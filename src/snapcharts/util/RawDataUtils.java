package snapcharts.util;
import snap.util.KeyChain;
import snapcharts.model.DataType;
import snapcharts.model.RawData;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for RawData.
 */
public class RawDataUtils {

    /**
     * Returns the index of the first value that is inside or inside adjacent for given min/max.
     */
    public static int getStartIndexForRange(RawData aRawData, double aMin, double aMax)
    {
        int start = 0;
        int pointCount = aRawData.getPointCount();
        while (start<pointCount && !isArrayValueAtIndexInsideOrInsideAdjacent(aRawData, start, pointCount, aMin, aMax))
            start++;
        return start;
    }

    /**
     * Returns the index of the last value that is inside or inside adjacent for given min/max.
     */
    public static int getEndIndexForRange(RawData aRawData, double aMin, double aMax)
    {
        int pointCount = aRawData.getPointCount();
        int end = pointCount - 1;
        while (end>0 && !isArrayValueAtIndexInsideOrInsideAdjacent(aRawData, end, pointCount, aMin, aMax))
            end--;
        return end;
    }

    /**
     * Returns true if given data/index value is inside range or adjacent to point inside.
     */
    private static final boolean isArrayValueAtIndexInsideOrInsideAdjacent(RawData aRawData, int i, int pointCount, double aMin, double aMax)
    {
        // If val at index in range, return true
        double val = aRawData.getX(i);
        if (val >= aMin && val <= aMax)
            return true;

        // If val at next index in range, return true
        if (i+1 < pointCount)
        {
            double nextVal = aRawData.getX(i + 1);
            if (val < aMin && nextVal >= aMin || val > aMax && nextVal <= aMax)
                return true;
        }

        // If val at previous index in range, return true
        if (i > 0)
        {
            double prevVal = aRawData.getX(i - 1);
            if ( val < aMin && prevVal >= aMin || val > aMax && prevVal <= aMax)
                return true;
        }

        // Return false since nothing in range
        return false;
    }

    /**
     * Returns a copy of given RawData processed with given expressions.
     */
    public static RawData getProcessedData(RawData aRawData, String exprX, String exprY, String exprZ)
    {
        // If both expressions empty, just return
        boolean isEmptyX = exprX == null || exprX.length() == 0;
        boolean isEmptyY = exprY == null || exprY.length() == 0;
        boolean isEmptyZ = exprZ == null || exprZ.length() == 0;
        if (isEmptyX && isEmptyY && isEmptyZ)
            return aRawData;

        // Get KeyChains
        KeyChain keyChainX = !isEmptyX ? KeyChain.getKeyChain(exprX.toLowerCase()) : null;
        KeyChain keyChainY = !isEmptyY ? KeyChain.getKeyChain(exprY.toLowerCase()) : null;
        KeyChain keyChainZ = !isEmptyZ ? KeyChain.getKeyChain(exprZ.toLowerCase()) : null;

        // Get DataX
        DataType dataType = aRawData.getDataType();
        int pointCount = aRawData.getPointCount();
        boolean hasZ = dataType.hasZ();
        double[] dataX = new double[pointCount];
        double[] dataY = new double[pointCount];
        double[] dataZ = hasZ ? new double[pointCount] : null;
        Map map = new HashMap();
        for (int i=0; i<pointCount; i++) {
            double valX = aRawData.getX(i);
            double valY = aRawData.getY(i);
            double valZ = hasZ ? aRawData.getZ(i) : 0;
            map.put("x", valX);
            map.put("y", valY);
            if (hasZ)
                map.put("z", valZ);

            dataX[i] = isEmptyX ? valX : KeyChain.getDoubleValue(map, keyChainX);
            dataY[i] = isEmptyY ? valY : KeyChain.getDoubleValue(map, keyChainY);
            if (hasZ)
                dataZ[i] = isEmptyZ ? valZ : KeyChain.getDoubleValue(map, keyChainZ);
        }

        // Return new RawData for type and values
        return RawData.newRawDataForTypeAndValues(dataType, dataX, dataY, dataZ);
    }

    /**
     * Returns RawData for given polar type.
     */
    public static RawData getPolarRawDataForType(RawData aRawData, DataType aDataType)
    {
        // If already polar, just return
        if (aRawData.getDataType().isPolar())
            return aRawData;

        // Complain if DataType arg isn't polar
        if (!aDataType.isPolar())
            throw new IllegalArgumentException("RawDataUtils.getPolarRawDataForType: Come on, man: " + aDataType);

        // Otherwise, get DataX array and create dataT array
        int count = aRawData.getPointCount();
        double dataT[] = new double[count];

        // Get min/max X to scale to polar
        double minX = aRawData.getMinX();
        double maxX = aRawData.getMaxX();
        double maxAngle = 2 * Math.PI; // 360 degrees

        // Iterate over X values and convert to 0 - 360 scale
        for (int i=0;i<count;i++) {
            double valX = aRawData.getX(i);
            double valTheta = (valX - minX) / (maxX - minX) * maxAngle;
            dataT[i] = valTheta;
        }

        // Get DataR and DataZ
        double[] dataR = aRawData.getDataY();
        double[] dataZ = aRawData.getDataType().hasZ() ? aRawData.getDataZ() : null;
        if (aDataType.hasZ() && dataZ==null)
            dataZ = new double[count];

        // Return new RawData for type and values
        return RawData.newRawDataForTypeAndValues(aDataType, dataT, dataR, dataZ);
    }

    /**
     * Returns RawData for given polar type.
     */
    public static RawData getPolarXYRawDataForPolar(RawData aRawData)
    {
        // If already non-polar, just return
        if (!aRawData.getDataType().isPolar())
            return aRawData;

        // Otherwise, get DataX array and create dataT array
        int count = aRawData.getPointCount();
        double dataX[] = new double[count];
        double dataY[] = new double[count];

        // Iterate over X values and convert to 0 - 360 scale
        for (int i=0;i<count;i++) {
            double valT = aRawData.getT(i);
            double valR = aRawData.getR(i);
            dataX[i] = Math.cos(valT) * valR;
            dataY[i] = Math.sin(valT) * valR;
        }

        // Get DataZ and DataType
        double[] dataZ = aRawData.getDataType().hasZ() ? aRawData.getDataZ() : null;
        DataType dataType = dataZ == null ? DataType.XY : DataType.XYZ;

        // Return new RawData for type and values
        return RawData.newRawDataForTypeAndValues(dataType, dataX, dataY, dataZ);
    }
}
