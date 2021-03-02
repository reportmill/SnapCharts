package snapcharts.model;

/**
 * This is the cover class for holding the raw data.
 */
public abstract class RawData {

    // The format of the data
    private DataType _dataType;

    // Cached arrays of X/Y/Z data
    private double[] _dataX, _dataY, _dataZ;

    // Cached array of C data
    private String[] _dataC;

    // Cached array of polar Theta/Radial data
    private double[]  _dataT, _dataR;

    // Min values for X/Y/Z
    private Double  _minX, _maxX, _minY, _maxY, _minZ, _maxZ;

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
        double[] dataT = getDataT();
        return dataT[anIndex];
    }

    /**
     * Returns the radius value at given index.
     */
    public double getR(int anIndex)
    {
        double[] dataR = getDataR();
        return dataR[anIndex];
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
     * Returns an array of dataset Theta values.
     */
    public double[] getDataT()
    {
        if (_dataT != null) return _dataT;
        return _dataT = getDataTImpl();
    }

    /**
     * Returns an array of dataset Radius values.
     */
    public double[] getDataR()
    {
        if (_dataR != null) return _dataR;
        return _dataR = getDataRImpl();
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
            case T: return getDataT();
            case R: return getDataR();
            default: throw new RuntimeException("RawData.getDataArrayForChannel: Invalid channel: " + aChannel);
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
     * Returns an array of dataset theta values.
     */
    protected double[] getDataTImpl()
    {
        // If Polar, just use X channel values
        if (getDataType().isPolar())
            return getDataX();

        // Otherwise, get DataX array and create dataT array
        double[] dataX = getDataX();
        int count = dataX.length;
        double dataT[] = new double[count];

        // Get min/max X to scale to polar
        double minX = getMinX();
        double maxX = getMaxX();
        double maxAngle = -2 * Math.PI; // 360 degrees
        double shiftAngle = Math.PI / 2;

        // Iterate over X values and convert to 0 - 360 scale
        for (int i=0;i<count;i++) {
            double valX = dataX[i];
            double valTheta = (valX - minX) / (maxX - minX) * maxAngle + shiftAngle;
            dataT[i] = valTheta;
        }

        // Return values
        return dataT;
    }

    /**
     * Returns an array of dataset radius values.
     */
    protected double[] getDataRImpl()
    {
        return getDataY();
    }

    /**
     * Returns the minimum X value in this dataset.
     */
    public double getMinX()
    {
        if (_minX != null) return _minX;
        return _minX = getMin(DataChan.X);
    }

    /**
     * Returns the maximum X value in this dataset.
     */
    public double getMaxX()
    {
        if (_maxX != null) return _maxX;
        return _maxX = getMax(DataChan.X);
    }

    /**
     * Returns the minimum Y value in this dataset.
     */
    public double getMinY()
    {
        if (_minY != null) return _minY;
        return _minY = getMin(DataChan.Y);
    }

    /**
     * Returns the maximum Y value in this dataset.
     */
    public double getMaxY()
    {
        if (_maxY != null) return _maxY;
        return _maxY = getMax(DataChan.Y);
    }

    /**
     * Returns the minimum Z value in this dataset.
     */
    public double getMinZ()
    {
        if (_minZ != null) return _minZ;
        return _minZ = getMin(DataChan.Z);
    }

    /**
     * Returns the maximum Z value in this dataset.
     */
    public double getMaxZ()
    {
        if (_maxZ != null) return _maxZ;
        return _maxZ = getMax(DataChan.Z);
    }

    /**
     * Returns the minimum X value in this dataset.
     */
    private double getMin(DataChan aChan)
    {
        double[] dataVals = getDataArrayForChannel(aChan);
        double min = Float.MAX_VALUE;
        for (int i=0, iMax=getPointCount(); i<iMax; i++) min = Math.min(min, dataVals[i]);
        return min;
    }

    /**
     * Returns the maximum X value in this dataset.
     */
    private double getMax(DataChan aChan)
    {
        double[] dataVals = getDataArrayForChannel(aChan);
        double max = -Float.MAX_VALUE;
        for (int i=0, iMax=getPointCount(); i<iMax; i++) max = Math.max(max, dataVals[i]);
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
