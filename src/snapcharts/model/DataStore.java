package snapcharts.model;
import snapcharts.util.MinMax;

/**
 * This class is a low-level representation of a chart data set.
 *
 * It provides a simple API for defining the DataType (which defines the data format/schema), the number of data
 * points/rows, and methods for getting/setting individual channel values (X, Y, ...) of the data for each point/row.
 */
public abstract class DataStore {

    // The format of the data
    private DataType _dataType = DataType.XY;

    // The number of rows
    private int  _rowCount;

    // The number of columns
    private int  _colCount;

    // Cached arrays of X/Y/Z data (and X/Y for ZZ)
    private double[] _dataX, _dataY, _dataZ;

    // Cached array of C data
    private String[] _dataC;

    // Cached arrays of X/Y for ZZ (for ZZ data types)
    private double[]  _dataXZZ, _dataYZZ;

    // Min/Max values for X/Y/Z
    private MinMax  _minMaxX, _minMaxY, _minMaxZ;

    /**
     * Returns the DataType.
     */
    public DataType getDataType()  { return _dataType; }

    /**
     * Sets the DataType.
     */
    public void setDataType(DataType aDataType)
    {
        _dataType = aDataType;
    }

    /**
     * Returns the number of rows.
     */
    public int getRowCount()  { return _rowCount; }

    /**
     * Sets the number of rows.
     */
    public void setRowCount(int aValue)  { _rowCount = aValue; }

    /**
     * Returns the number of columns.
     */
    public int getColCount()  { return _colCount; }

    /**
     * Sets the number of columns.
     */
    public void setColCount(int aValue)  { _colCount = aValue; }

    /**
     * Returns the number of points.
     */
    public abstract int getPointCount();

    /**
     * Sets the number of points.
     */
    public abstract void setPointCount(int aValue);

    /**
     * Returns the X value at given index.
     */
    public abstract double getX(int anIndex);

    /**
     * Returns the Y value at given index.
     */
    public abstract double getY(int anIndex);

    /**
     * Returns the Z value at given index.
     */
    public abstract double getZ(int anIndex);

    /**
     * Returns the C value at given index.
     */
    public abstract String getC(int anIndex);

    /**
     * Sets the C value at given index.
     */
    public abstract void setC(String aValue, int anIndex);

    /**
     * Returns the Theta value at given index.
     */
    public double getT(int anIndex)
    {
        return getX(anIndex);
    }

    /**
     * Returns the radius value at given index.
     */
    public double getR(int anIndex)
    {
        return getY(anIndex);
    }

    /**
     * Returns the X value at given index (null if not set).
     */
    public abstract Double getValueX(int anIndex);

    /**
     * Sets the X value at given index.
     */
    public abstract void setValueX(Double aValue, int anIndex);

    /**
     * Returns the Y value at given index (null if not set).
     */
    public abstract Double getValueY(int anIndex);

    /**
     * Sets the Y value at given index.
     */
    public abstract void setValueY(Double aValue, int anIndex);

    /**
     * Returns the Y value at given index (null if not set).
     */
    public abstract Double getValueZ(int anIndex);

    /**
     * Sets the Z value at given index.
     */
    public abstract void setValueZ(Double aValue, int anIndex);

    /**
     * Adds a point for X and Y values.
     */
    public abstract void addPoint(DataPoint aPoint, int anIndex);

    /**
     * Removes a point at index.
     */
    public abstract void removePoint(int anIndex);

    /**
     * Clears all points.
     */
    public abstract void clearPoints();

    /**
     * Returns whether this dataset is clear (no name and no values).
     */
    public abstract boolean isClear();

    /**
     * Returns an array of dataset X values.
     */
    public double[] getDataX()
    {
        if (_dataX!=null) return _dataX;
        return _dataX = getDataXImpl();
    }

    /**
     * Returns an array of dataset Y values.
     */
    public double[] getDataY()
    {
        if (_dataY !=null) return _dataY;
        return _dataY = getDataYImpl();
    }

    /**
     * Returns an array of dataset Z values.
     */
    public double[] getDataZ()
    {
        if (_dataZ !=null) return _dataZ;
        return _dataZ = getDataZImpl();
    }

    /**
     * Returns an array of dataset C values.
     */
    public String[] getDataC()
    {
        if (_dataC !=null) return _dataC;
        return _dataC = getDataCImpl();
    }

    /**
     * Returns the data array for given channel.
     */
    public double[] getDataArrayForChannel(DataChan aChannel)
    {
        switch (aChannel) {
            case X: return getDataX();
            case Y: return getDataY();
            case Z: return getDataZ();
            case T: return getDataX();
            case R: return getDataY();
            default: throw new RuntimeException("DataStore.getDataArrayForChannel: Invalid channel: " + aChannel);
        }
    }

    /**
     * Returns an array of dataset X values.
     */
    protected double[] getDataXImpl()
    {
        int count = getPointCount();
        double vals[] = new double[count];
        for (int i=0;i<count;i++) vals[i] = getX(i);
        return vals;
    }

    /**
     * Returns an array of dataset Y values.
     */
    protected double[] getDataYImpl()
    {
        int count = getPointCount();
        double vals[] = new double[count];
        for (int i=0;i<count;i++) vals[i] = getY(i);
        return vals;
    }

    /**
     * Returns an array of dataset Z values.
     */
    protected double[] getDataZImpl()
    {
        int count = getPointCount();
        double vals[] = new double[count];
        for (int i=0;i<count;i++) vals[i] = getZ(i);
        return vals;
    }

    /**
     * Returns an array of dataset C values.
     */
    protected String[] getDataCImpl()
    {
        int count = getPointCount();
        String vals[] = new String[count];
        for (int i=0;i<count;i++) vals[i] = getC(i);
        return vals;
    }

    /**
     * Returns the minimum X value in this dataset.
     */
    public double getMinX()  { return getMinMaxX().getMin(); }

    /**
     * Returns the maximum X value in this dataset.
     */
    public double getMaxX()  { return getMinMaxX().getMax(); }

    /**
     * Returns the minimum Y value in this dataset.
     */
    public double getMinY()  { return getMinMaxY().getMin(); }

    /**
     * Returns the maximum Y value in this dataset.
     */
    public double getMaxY()  { return getMinMaxY().getMax(); }

    /**
     * Returns the minimum Z value in this dataset.
     */
    public double getMinZ()  { return getMinMaxZ().getMin(); }

    /**
     * Returns the maximum Z value in this dataset.
     */
    public double getMaxZ()  { return getMinMaxZ().getMax(); }

    /**
     * Returns the MinMax of X in this dataset.
     */
    public MinMax getMinMaxX()
    {
        if (_minMaxX != null) return _minMaxX;
        return _minMaxX = getMinMax(DataChan.X);
    }

    /**
     * Returns the MinMax of Y in this dataset.
     */
    public MinMax getMinMaxY()
    {
        if (_minMaxY != null) return _minMaxY;
        return _minMaxY = getMinMax(DataChan.Y);
    }

    /**
     * Returns the MinMax of Z in this dataset.
     */
    public MinMax getMinMaxZ()
    {
        if (_minMaxZ != null) return _minMaxZ;
        return _minMaxZ = getMinMax(DataChan.Z);
    }

    /**
     * Returns the MinMax of Theta in this dataset.
     */
    public MinMax getMinMaxT()
    {
        return getMinMaxX();
    }

    /**
     * Returns the MinMax of Radius in this dataset.
     */
    public MinMax getMinMaxR()
    {
        return getMinMaxY();
    }

    /**
     * Returns the minimum X value in this dataset.
     */
    public MinMax getMinMax(DataChan aChan)
    {
        double[] dataVals = getDataArrayForChannel(aChan);
        double min = Float.MAX_VALUE;
        double max = -Float.MAX_VALUE;
        for (int i=0, iMax=getPointCount(); i<iMax; i++) {
            min = Math.min(min, dataVals[i]);
            max = Math.max(max, dataVals[i]);
        }
        return new MinMax(min, max);
    }

    /**
     * Returns the dataX values for ZZ.
     */
    public double[] getDataXforZZ()
    {
        // If already set, just return
        if (_dataXZZ != null) return _dataXZZ;

        // Create X array and load
        int colCount = getColCount();
        double[] dataX = new double[colCount];
        for (int i=0; i<colCount; i++)
            dataX[i] = getX(i);

        // Set/return
        return _dataXZZ = dataX;
    }

    /**
     * Returns the dataY values for ZZ.
     */
    public double[] getDataYforZZ()
    {
        // If already set, just return
        if (_dataYZZ != null) return _dataYZZ;

        // Create Y array and load
        int rowCount = getRowCount();
        int colCount = getColCount();
        double[] dataY = new double[rowCount];
        for (int i=0; i<rowCount; i++)
            dataY[i] = getY(i * colCount);

        // Set/return
        return _dataYZZ = dataY;
    }

    /**
     * Called when points are added, removed or modified.
     */
    protected void pointsDidChange()
    {
        _dataX = _dataY = _dataZ = _dataXZZ = _dataYZZ = null;
        _dataC = null;
        _minMaxX = _minMaxY = _minMaxZ = null;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String str = "DataStore { " + "DataType=" + getDataType() + ", PointCount=" + getPointCount();
        for (DataChan chan : getDataType().getChannels()) {
            MinMax minMax = getMinMax(chan);
            str += ", Min" + chan + "=" + minMax.getMin() + ", Max" + chan + "=" + minMax.getMax();
        }
        return str + '}';
    }

    /**
     * Returns new DataStore instance.
     */
    public static DataStore newDataStore()
    {
        return new DataStoreImpl();
    }

    /**
     * Returns new DataStore instance for type and array values.
     */
    public static DataStore newDataStoreForTypeAndValues(DataType aDataType, Object ... theArrays)
    {
        return new DataStoreImpl(aDataType, theArrays);
    }
}
