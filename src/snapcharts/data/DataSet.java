/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import snap.props.Prop;
import snap.props.PropObject;
import snap.props.PropSet;
import snap.util.ArrayUtils;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snapcharts.util.MinMax;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class is the representation of a data set for SnapCharts. Conceptually it holds an array of points, which are
 * in the form of XY pairs (2D scatter), XYZ triplets (3D), CY (bar), TR/TRZ (polar).
 *
 * It provides a simple API for defining the DataType (which defines the data format/schema), the number of data
 * points/rows, and methods for getting/setting individual channel values (X, Y, ...) of the data for each point/row.
 */
public abstract class DataSet extends PropObject implements Cloneable, XMLArchiver.Archivable {

    // The name
    private String  _name;

    // The format of the data
    private DataType  _dataType;

    // The DataArrays
    protected DataArray[]  _dataArrays;

    // Cached DataArrays for common channels X/Y/Z
    protected NumberArray  _dataX, _dataY, _dataZ;

    // Cached DataArrays for common channel C
    protected StringArray  _dataC;

    // The number of points
    protected int  _pointCount;

    // Properties
    public static final String Name_Prop = "Name";
    public static final String DataType_Prop = "DataType";

    // Constants for defaults
    public final DataType DEFAULT_DATA_TYPE = DataType.XY;

    /**
     * Constructor.
     */
    public DataSet()
    {
        _dataType = DEFAULT_DATA_TYPE;
    }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name.
     */
    public void setName(String aName)
    {
        if (Objects.equals(aName, _name)) return;
        firePropChange(Name_Prop, _name, _name = aName);
    }

    /**
     * Returns the DataType.
     */
    public DataType getDataType()  { return _dataType; }

    /**
     * Sets the DataType.
     */
    public void setDataType(DataType aDataType)
    {
        if (Objects.equals(aDataType, _dataType)) return;
        firePropChange(DataType_Prop, _dataType, _dataType = aDataType);
    }

    /**
     * Returns the DataArrays.
     */
    public DataArray[] getDataArrays()  { return _dataArrays; }

    /**
     * Sets the DataArrays.
     */
    public void setDataArrays(DataArray[] aDataArray)
    {
        // Set DataArrays
        _dataArrays = aDataArray;

        // Get DataType info
        DataType dataType = getDataType();
        DataChan[] channels = dataType.getChannelsXY();
        int channelCount = channels.length;

        // Iterate over channels and set XYZ DataArrays
        for (int i = 0; i < channelCount; i++) {
            DataChan chan = channels[i];
            DataArray dataArray = _dataArrays[i];
            switch (chan) {
                case X: _dataX = (NumberArray) dataArray; break;
                case Y: _dataY = (NumberArray) dataArray; break;
                case Z: _dataZ = (NumberArray) dataArray; break;
                case C: _dataC = (StringArray) dataArray; break;
                default: break;
            }
        }

        // Set PointCount
        _pointCount = _dataZ != null ? _dataZ.getLength() :
                _dataY != null ? _dataY.getLength() :
                _dataX != null ? _dataX.getLength() : 0;
    }

    /**
     * Sets DataArrays from data.
     */
    public void setDataArraysFromArrays(Object ... theArrays)
    {
        // Get DataArrays
        DataArray[] dataArrays = DataArray.newDataArraysForArrays(theArrays);

        // Set known arrays
        DataType dataType = getDataType();
        DataChan[] channels = dataType.getChannelsXY();
        int channelCount = channels.length;
        if (channelCount > dataArrays.length) {
            dataArrays = DataArray.newDataArraysForDataType(dataType);
            System.out.println("DataSetImpl.setDataArraysFromArrays: Missing data");
        }

        // Set DataArrays
        setDataArrays(dataArrays);
    }

    /**
     * Returns an array of dataset X values.
     */
    public NumberArray getDataArrayX()  { return _dataX; }

    /**
     * Returns an array of dataset Y values.
     */
    public NumberArray getDataArrayY()  { return _dataY; }

    /**
     * Returns an array of dataset Z values.
     */
    public NumberArray getDataArrayZ()  { return _dataZ; }

    /**
     * Returns an array of dataset C values.
     */
    public StringArray getDataArrayC()  { return _dataC; }

    /**
     * Returns the DataArray for given channel.
     */
    public DataArray getDataArrayForChannel(DataChan aChan)
    {
        // Get chan
        DataChan chan = aChan;
        if (chan == DataChan.T)  chan = DataChan.X;
        else if (chan == DataChan.R)  chan = DataChan.Y;

        // Get index of channel
        DataType dataType = getDataType();
        int index = ArrayUtils.indexOfId(dataType.getChannels(), chan);
        if (index < 0)
            index = ArrayUtils.indexOfId(dataType.getChannelsXY(), chan);

        // Get DataArray at index
        DataArray[] dataArrays = getDataArrays();
        if (dataArrays != null && index >= 0 && index < dataArrays.length)
            return dataArrays[index];
        return null;
    }

    /**
     * Returns the NumberArray for given channel.
     */
    public NumberArray getNumberArrayForChannel(DataChan aChan)
    {
        DataArray dataArray = getDataArrayForChannel(aChan);
        return dataArray instanceof NumberArray ? (NumberArray) dataArray : null;
    }

    /**
     * Returns the units for Theta data.
     */
    public DataUnit getThetaUnit()
    {
        NumberArray thetaArray = getNumberArrayForChannel(DataChan.T);
        DataUnit thetaUnit = thetaArray != null ? thetaArray.getUnit() : null;
        return thetaUnit != null ? thetaUnit : DataUnit.DEFAULT_THETA_UNIT;
    }

    /**
     * Sets the units for Theta data.
     */
    public void setThetaUnit(DataUnit aValue)
    {
        NumberArray thetaArray = getNumberArrayForChannel(DataChan.T);
        if (thetaArray != null)
            thetaArray.setUnit(aValue);
        else System.out.println("DataSet.setThetaUnit: Theta data not found");
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
    public double getX(int anIndex)
    {
        return _dataX != null ? _dataX.getDouble(anIndex) : anIndex;
    }

    /**
     * Returns the Y value at given index.
     */
    public double getY(int anIndex)
    {
        return _dataY != null ? _dataY.getDouble(anIndex) : 0;
    }

    /**
     * Returns the Z value at given index.
     */
    public double getZ(int anIndex)
    {
        return _dataZ != null ? _dataZ.getDouble(anIndex) : 0;
    }

    /**
     * Returns the C value at given index.
     */
    public String getC(int anIndex)
    {
        return _dataC != null ? _dataC.getString(anIndex) : null;
    }

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
     * Returns the value for channel and record index.
     */
    public Object getValueForChannel(DataChan aChan, int anIndex)
    {
        DataArray dataArray = getDataArrayForChannel(aChan);
        return dataArray != null ? dataArray.getValue(anIndex) : null;
    }

    /**
     * Returns the data point at given index.
     */
    public DataPoint getPoint(int anIndex)
    {
        return new DataPoint(this, anIndex);
    }

    /**
     * Adds a point for X and Y values.
     */
    public abstract void addPoint(DataPoint aPoint, int anIndex);

    /**
     * Removes a point at index.
     */
    public abstract void removePoint(int anIndex);

    /**
     * Sets a point for X and Y values.
     */
    public abstract void setPoint(DataPoint aPoint, int anIndex);

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
        NumberArray dataArray = getDataArrayX();
        return dataArray != null ? dataArray.getDoubleArray() : null;
    }

    /**
     * Returns an array of dataset Y values.
     */
    public double[] getDataY()
    {
        NumberArray dataArray = getDataArrayY();
        return dataArray != null ? dataArray.getDoubleArray() : null;
    }

    /**
     * Returns an array of dataset Z values.
     */
    public double[] getDataZ()
    {
        NumberArray dataArray = getDataArrayZ();
        return dataArray != null ? dataArray.getDoubleArray() : null;
    }

    /**
     * Returns an array of dataset C values.
     */
    public String[] getDataC()
    {
        StringArray dataArray = getDataArrayC();
        return dataArray != null ? dataArray.getStringArray() : null;
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
        NumberArray dataArray = getDataArrayX();
        return dataArray != null ? dataArray.getMinMax() : new MinMax(0, 0);
    }

    /**
     * Returns the MinMax of Y in this dataset.
     */
    public MinMax getMinMaxY()
    {
        NumberArray dataArray = getDataArrayY();
        return dataArray != null ? dataArray.getMinMax() : new MinMax(0, 0);
    }

    /**
     * Returns the MinMax of Z in this dataset.
     */
    public MinMax getMinMaxZ()
    {
        NumberArray dataArray = getDataArrayZ();
        return dataArray != null ? dataArray.getMinMax() : new MinMax(0, 0);
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public DataSet clone()
    {
        DataSet clone;
        try { clone = (DataSet) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
        return clone;
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = new XMLElement("DataSet");
        toXML(anArchiver, e);
        return e;
    }

    /**
     * Override to return props for DataType channels.
     */
    @Override
    public Prop[] getPropsForArchivalExtra()
    {
        // Get DataType and channel count
        DataType dataType = getDataType();
        int chanCount = dataType.getChannelCount();

        // Extend props array and add channels
        Prop[] propsForDataType = new Prop[chanCount];
        for (int i = 0; i < chanCount; i++) {
            DataChan dataChan = dataType.getChannel(i);
            Class<?> propClass = dataChan.getDataArrayClass();
            propsForDataType[i] = new Prop(dataChan.toString(), propClass, null);
        }

        // Return
        return propsForDataType;
    }

    /**
     * Override to configure props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Name, DataType
        aPropSet.addPropNamed(Name_Prop, String.class, null);
        aPropSet.addPropNamed(DataType_Prop, DataType.class, null);
    }

    /**
     * Override to support data type props.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        // Handle DataType props
        Object dataTypePropValue = getDataTypePropValue(aPropName);
        if (dataTypePropValue != null)
            return null;

        // Do normal version
        return super.getPropDefault(aPropName);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle DataType props
        Object dataTypePropValue = getDataTypePropValue(aPropName);
        if (dataTypePropValue != null)
            return dataTypePropValue;

        // Handle standard props
        switch (aPropName) {

            // Name, DataType
            case Name_Prop: return getName();
            case DataType_Prop: return getDataType();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Returns a DataArray primitive array for PropName if it matches DataType channel.
     */
    private Object getDataTypePropValue(String aPropName)
    {
        // Handle DataType props
        DataType dataType = getDataType();
        int chanCount = dataType.getChannelCount();

        // Iterate over DataChannels and return primitive array if it matches
        for (int i = 0; i < chanCount; i++) {
            DataChan dataChan = dataType.getChannel(i);
            if (aPropName.equals(dataChan.toString())) {
                DataArray dataArray = getDataArrayForChannel(dataChan);
                if (dataArray instanceof NumberArray)
                    return ((NumberArray) dataArray).getDoubleArray();
                if (dataArray instanceof StringArray)
                    return ((StringArray) dataArray).getStringArray();
                System.err.println("DataSet.getDataTypePropValue: Unknown DataChan: " + dataChan);
            }
        }

        // Return null since not found
        return null;
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Name, DataType
            case Name_Prop: setName(SnapUtils.stringValue(aValue)); break;
            case DataType_Prop: setDataType((DataType) aValue); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue); break;
        }
    }

    /**
     * Archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver, XMLElement e)
    {
        // Archive DataType
        DataType dataType = getDataType();
        e.add(DataType_Prop, dataType);

        // Legacy: Archive ThetaUnit
        if (dataType.isPolar() && getThetaUnit() != DataUnit.DEFAULT_THETA_UNIT)
            e.add("ThetaUnit", getThetaUnit());

        // If DataType has X, add DataX values
        DataType dataTypeXY = dataType.getDataTypeXY();
        if (dataTypeXY.hasChannel(DataChan.X)) {
            double[] dataX = getDataX();
            String dataStr = DataUtils.getStringForDoubleArray(dataX);
            e.add(new XMLElement("DataX", dataStr));
        }

        // If DataType has Y, add DataY values
        if (dataTypeXY.hasChannel(DataChan.Y)) {
            double[] dataY = getDataY();
            String dataStr = DataUtils.getStringForDoubleArray(dataY);
            e.add(new XMLElement("DataY", dataStr));
        }

        // If DataType has Z, add DataZ values
        if (dataTypeXY.hasChannel(DataChan.Z)) {
            double[] dataZ = getDataZ();
            String dataStr = DataUtils.getStringForDoubleArray(dataZ);
            e.add(new XMLElement("DataZ", dataStr));
        }

        // If DataType has C, add DataC values
        if (dataType.hasChannel(DataChan.C)) {
            String dataStr = Arrays.toString(getDataC());
            e.add(new XMLElement("DataC", dataStr));
        }

        // Return element
        return e;
    }

    /**
     * Unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive DataType
        String dataTypeStr = anElement.getAttributeValue(DataType_Prop);
        DataType dataType = DataType.valueOf(dataTypeStr);
        setDataType(dataType);

        // Handle XYZZ special
        if (dataType == DataType.XYZZ)
            return new DataSetXYZZ().fromXML(anArchiver, anElement);

        // Legacy: Unarchive ThetaUnit
        if (anElement.hasAttribute("ThetaUnit")) {
            String unitName = anElement.getAttributeValue("ThetaUnit");
            DataUnit dataUnit = DataUnit.getUnitForName(unitName, DataUnit.DEFAULT_THETA_UNIT);
            setThetaUnit(dataUnit);
        }

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

        // Get DataC
        String[] dataC = null;
        XMLElement dataC_XML = anElement.get("DataC");
        if (dataC_XML != null) {
            String dataCStr = dataC_XML.getValue();
            dataC = DataUtils.getStringArrayForString(dataCStr);
        }

        // Add Data points
        setDataArraysFromArrays(dataC, dataX, dataY, dataZ);

        // Return this part
        return this;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String className = getClass().getSimpleName();
        String propString = toStringProps();
        return className + " { " + propString + " }";
    }

    /**
     * Standard toStringProps implementation.
     */
    public String toStringProps()
    {
        // Add Name
        StringBuffer sb = new StringBuffer();
        String name = getName();
        if (name != null)
            sb.append("Name=").append(getName()).append(", ");

        // Add DataType
        DataType dataType = getDataType();
        sb.append("DataType=").append(dataType);

        // Add PointCount
        sb.append(", PointCount=").append(getPointCount());

        // Add DataArrays
        DataChan[] dataChans = dataType.getChannels();
        for (DataChan dataChan : dataChans) {
            DataArray dataArray = getDataArrayForChannel(dataChan);
            if (dataArray != null)
                sb.append(",\nData").append(dataChan).append("={ ").append(dataArray.toStringProps()).append(" }");
        }

        // Return string
        return sb.toString();
    }

    /**
     * Returns new DataSet instance.
     */
    public static DataSet newDataSet()
    {
        return new DataSetImpl();
    }

    /**
     * Returns new DataSet instance for type and array values.
     */
    public static DataSet newDataSetForTypeAndValues(DataType aDataType, Object ... theArrays)
    {
        // Handle XYZZ special
        if (aDataType == DataType.XYZZ)
            return new DataSetXYZZ(theArrays);

        // Handle other
        return new DataSetImpl(aDataType, theArrays);
    }
}