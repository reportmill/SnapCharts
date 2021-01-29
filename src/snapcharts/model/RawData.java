package snapcharts.model;

/**
 * This is the cover class for holding the raw data.
 */
public abstract class RawData {

    // Cached arrays of X/Y/Z data
    private double[] _dataX, _dataY, _dataZ;

    // Cached array of C data
    private String[] _dataC;

    // Min values for X/Y/Z
    private Double  _minX, _maxX, _minY, _maxY, _minZ, _maxZ;

    // Constants for channels
    private enum Channel { X, Y, Z, C }

    /**
     * Returns the DataType.
     */
    public abstract DataType getDataType();

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
    public abstract DataPoint removePoint(int anIndex);

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
    public double getMinX()
    {
        if (_minX != null) return _minX;
        return _minX = getMin(Channel.X);
    }

    /**
     * Returns the maximum X value in this dataset.
     */
    public double getMaxX()
    {
        if (_maxX != null) return _maxX;
        return _maxX = getMax(Channel.X);
    }

    /**
     * Returns the minimum Y value in this dataset.
     */
    public double getMinY()
    {
        if (_minY != null) return _minY;
        return _minY = getMin(Channel.Y);
    }

    /**
     * Returns the maximum Y value in this dataset.
     */
    public double getMaxY()
    {
        if (_maxY != null) return _maxY;
        return _maxY = getMax(Channel.Y);
    }

    /**
     * Returns the minimum Z value in this dataset.
     */
    public double getMinZ()
    {
        if (_minZ != null) return _minZ;
        return _minZ = getMin(Channel.Z);
    }

    /**
     * Returns the maximum Z value in this dataset.
     */
    public double getMaxZ()
    {
        if (_maxZ != null) return _maxZ;
        return _maxZ = getMax(Channel.Z);
    }

    /**
     * Returns the minimum X value in this dataset.
     */
    private double getMin(Channel aChan)
    {
        double min = Float.MAX_VALUE;
        switch (aChan) {
            case X: for (int i=0, iMax=getPointCount(); i<iMax; i++) min = Math.min(min, getX(i)); break;
            case Y: for (int i=0, iMax=getPointCount(); i<iMax; i++) min = Math.min(min, getY(i)); break;
            case Z: for (int i=0, iMax=getPointCount(); i<iMax; i++) min = Math.min(min, getZ(i)); break;
        }
        return min;
    }

    /**
     * Returns the maximum X value in this dataset.
     */
    private double getMax(Channel aChan)
    {
        double max = -Float.MAX_VALUE;
        switch (aChan) {
            case X: for (int i=0, iMax=getPointCount(); i<iMax; i++) max = Math.max(max, getX(i)); break;
            case Y: for (int i=0, iMax=getPointCount(); i<iMax; i++) max = Math.max(max, getY(i)); break;
            case Z: for (int i=0, iMax=getPointCount(); i<iMax; i++) max = Math.max(max, getZ(i)); break;
        }
        return max;
    }

    /**
     * Called when points are added, removed or modified.
     */
    protected void pointsDidChange()
    {
        _dataX = _dataY = _dataZ = null;
        _dataC = null;
        _minX = _maxX = null;
        _minY = _maxY = null;
        _minZ = _maxZ = null;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        return "RawData { " + "PointCount=" + getPointCount() + ", MinX=" + getMinX() + ", MaxX=" + getMaxX() + '}';
    }
}
