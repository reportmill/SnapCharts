package snapcharts.data;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * This DataSet subclass holds tabular XYZ data. X values are represented as columns, Y as rows.
 */
public class DataSetXYZZ extends DataSet {

    // Cached arrays of X/Y/Z data
    private double[]  _dataX, _dataY, _dataZ;

    /**
     * Constructor.
     */
    public DataSetXYZZ()
    {
        setDataType(DataType.XYZZ);
    }

    /**
     * Constructor.
     */
    public DataSetXYZZ(double[] dataX, double[] dataY, double[] dataZ)
    {
        setDataType(DataType.XYZZ);

        _dataX = dataX;
        _dataY = dataY;
        _dataZ = dataZ;
    }

    /**
     * Returns the number of rows.
     */
    public int getRowCount()  { return _dataY.length; }

    /**
     * Returns the number of columns.
     */
    public int getColCount()  { return _dataX.length; }

    /**
     * Returns the number of points.
     */
    @Override
    public int getPointCount()
    {
        return _dataZ.length;
    }

    @Override
    public void setPointCount(int aValue)
    {
        throw new RuntimeException("DataSetXYZZ.setPointCount: XYZZ cannot add points dynamically");
    }

    @Override
    public double getX(int anIndex)
    {
        int colCount = getColCount();
        int index = colCount > 0 ? anIndex % colCount : anIndex;
        return _dataX[index];
    }

    @Override
    public double getY(int anIndex)
    {
        int colCount = getColCount();
        int index = colCount > 0 ? anIndex / colCount : anIndex;
        return _dataY[index];
    }

    @Override
    public double getZ(int anIndex)
    {
        return _dataZ[anIndex];
    }

    @Override
    public String getC(int anIndex)  { return null; }

    @Override
    public void setC(String aValue, int anIndex)  { }

    @Override
    public Double getValueX(int anIndex)
    {
        return getX(anIndex);
    }

    @Override
    public void setValueX(Double aValue, int anIndex)  { }

    @Override
    public Double getValueY(int anIndex)
    {
        return getY(anIndex);
    }

    @Override
    public void setValueY(Double aValue, int anIndex)  { }

    @Override
    public Double getValueZ(int anIndex)
    {
        return getZ(anIndex);
    }

    @Override
    public void setValueZ(Double aValue, int anIndex)  { }

    @Override
    public void addPoint(DataPoint aPoint, int anIndex)  { }

    @Override
    public void removePoint(int anIndex)  { }

    @Override
    public void setPoint(DataPoint aPoint, int anIndex)  { }

    @Override
    public void clearPoints()  { }

    @Override
    public boolean isClear()  { return false; }

    /**
     * Override to handle XYZZ.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Get DataX
        XMLElement dataX_XML = anElement.get("DataX");
        if (dataX_XML != null) {
            String dataXStr = dataX_XML.getValue();
            _dataX = DataUtils.getDoubleArrayForString(dataXStr);
        }

        // Get DataY
        XMLElement dataY_XML = anElement.get("DataY");
        if (dataY_XML != null) {
            String dataYStr = dataY_XML.getValue();
            _dataY = DataUtils.getDoubleArrayForString(dataYStr);
        }

        // Get DataZ
        XMLElement dataZ_XML = anElement.get("DataZ");
        if (dataZ_XML != null) {
            String dataZStr = dataZ_XML.getValue();
            _dataZ = DataUtils.getDoubleArrayForString(dataZStr);
        }

        // Return
        return this;
    }

    /**
     * Returns the column index.
     */
    public static int getColIndex(DataPoint aDataPoint)
    {
        DataSet dataSet = aDataPoint.getDataSet();
        if (dataSet == null || dataSet.getDataType() != DataType.XYZZ)
            return 0;

        DataSetXYZZ dataSetXYZZ = (DataSetXYZZ) dataSet;
        int index = aDataPoint.getIndex();
        int colCount = dataSetXYZZ.getColCount(); if (colCount == 0) return index;
        return index % colCount;
    }

    /**
     * Returns the row index.
     */
    public static int getRowIndex(DataPoint aDataPoint)
    {
        DataSet dataSet = aDataPoint.getDataSet();
        int index = aDataPoint.getIndex();
        if (dataSet == null || dataSet.getDataType() != DataType.XYZZ)
            return index;

        DataSetXYZZ dataSetXYZZ = (DataSetXYZZ) dataSet;
        int colCount = dataSetXYZZ.getColCount(); if (colCount == 0) return index;
        return index / colCount;
    }

    /**
     * Returns new DataSet instance for type and array values.
     */
    public static DataSetXYZZ newDataSetForValues(Object ... theArrays)
    {
        double[] dataX = (double[]) theArrays[0];
        double[] dataY = (double[]) theArrays[1];
        double[] dataZ = (double[]) theArrays[2];
        return new DataSetXYZZ(dataX, dataY, dataZ);
    }
}
