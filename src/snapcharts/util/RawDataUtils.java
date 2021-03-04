package snapcharts.util;
import snapcharts.model.DataType;
import snapcharts.model.RawData;

/**
 * Utilities for RawData.
 */
public class RawDataUtils {

    /**
     * Returns RawData for given polar type.
     */
    public static RawData getPolarRawDataForType(RawData aRawData, DataType aDataType)
    {
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
}
