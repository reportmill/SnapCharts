package snapcharts.model;
import snapcharts.util.MinMax;

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

    // Min/Max values for X/Y/Z
    private MinMax  _minMaxX, _minMaxY, _minMaxZ, _minMaxT, _minMaxR;

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
        double maxAngle = 2 * Math.PI; // 360 degrees

        // Iterate over X values and convert to 0 - 360 scale
        for (int i=0;i<count;i++) {
            double valX = dataX[i];
            double valTheta = (valX - minX) / (maxX - minX) * maxAngle;
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
        if (_minMaxT != null) return _minMaxT;
        return _minMaxT = getMinMax(DataChan.T);
    }

    /**
     * Returns the MinMax of Radius in this dataset.
     */
    public MinMax getMinMaxR()
    {
        if (_minMaxR != null) return _minMaxR;
        return _minMaxR = getMinMax(DataChan.R);
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
     * Called when points are added, removed or modified.
     */
    protected void pointsDidChange()
    {
        _dataX = _dataY = _dataZ = null;
        _dataC = null;
        _minMaxX = _minMaxY = _minMaxZ = _minMaxT = _minMaxR = null;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String str = "RawData { " + "DataType=" + getDataType() + ", PointCount=" + getPointCount();
        for (DataChan chan : getDataType().getChannels()) {
            MinMax minMax = getMinMax(chan);
            str += ", Min" + chan + "=" + minMax.getMin() + ", Max" + chan + "=" + minMax.getMax();
        }
        return str + '}';
    }
}
