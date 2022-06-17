package snapcharts.data;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * This DataSet subclass holds tabular XYZ data. X values are represented as columns, Y as rows.
 */
public class DataSetXYZZ extends DataSet {

    /**
     * Constructor.
     */
    public DataSetXYZZ()
    {
        double[] doubleArray = new double[0];
        initFromArraysXYZ(doubleArray, doubleArray, doubleArray);
    }

    /**
     * Constructor.
     */
    public DataSetXYZZ(Object ... theArrays)
    {
        // Create/set DataArrays
        double[] dataX = (double[]) theArrays[0];
        double[] dataY = (double[]) theArrays[1];
        double[] dataZ = (double[]) theArrays[2];
        initFromArraysXYZ(dataX, dataY, dataZ);
    }

    /**
     * Constructor.
     */
    public DataSetXYZZ(double[] dataX, double[] dataY, double[] dataZ)
    {
        initFromArraysXYZ(dataX, dataY, dataZ);
    }

    /**
     * Sets DataArrays.
     */
    private void initFromArraysXYZ(double[] dataX, double[] dataY, double[] dataZ)
    {
        // Set DataType XYZZ
        setDataType(DataType.XYZZ);

        // Create/set DataArrays
        _dataX = new NumberArray(dataX);
        _dataY = new NumberArray(dataY);
        _dataZ = new NumberArray(dataZ);
        _dataArrays = new DataArray[] { _dataX, _dataY, _dataZ };
    }

    /**
     * Returns the number of rows.
     */
    public int getRowCount()  { return _dataY.getLength(); }

    /**
     * Returns the number of columns.
     */
    public int getColCount()  { return _dataX.getLength(); }

    /**
     * Returns the number of points.
     */
    @Override
    public int getPointCount()  { return _dataZ.getLength(); }

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
        return _dataX.getDouble(index);
    }

    @Override
    public double getY(int anIndex)
    {
        int colCount = getColCount();
        int index = colCount > 0 ? anIndex / colCount : anIndex;
        return _dataY.getDouble(index);
    }

    /**
     * Override to forward to getX/Y.
     */
    @Override
    public Object getValueForChannel(DataChan aChan, int anIndex)
    {
        // Handle X
        if (aChan == DataChan.X)
            return getX(anIndex);

        // Handle Y
        if (aChan == DataChan.Y)
            return getY(anIndex);

        // Do normal version
        return super.getValueForChannel(aChan, anIndex);
    }

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
        double[] dataX = null;
        XMLElement dataX_XML = anElement.get("DataX");
        if (dataX_XML != null) {
            String dataXStr = dataX_XML.getValue();
            dataX = DataUtils.getDoubleArrayForString(dataXStr);
        }

        // Get DataY
        double[] dataY = null;
        XMLElement dataY_XML = anElement.get("DataY");
        if (dataY_XML != null) {
            String dataYStr = dataY_XML.getValue();
            dataY = DataUtils.getDoubleArrayForString(dataYStr);
        }

        // Get DataZ
        double[] dataZ = null;
        XMLElement dataZ_XML = anElement.get("DataZ");
        if (dataZ_XML != null) {
            String dataZStr = dataZ_XML.getValue();
            dataZ = DataUtils.getDoubleArrayForString(dataZStr);
        }

        // Set DataArrays
        _dataArrays = DataArray.newDataArraysForArrays(dataX, dataY, dataZ);
        _dataX = (NumberArray) _dataArrays[0];
        _dataY = (NumberArray) _dataArrays[1];
        _dataZ = (NumberArray) _dataArrays[2];

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
}
