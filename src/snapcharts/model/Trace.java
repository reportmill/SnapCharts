/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snapcharts.data.*;
import snapcharts.util.MinMax;

import java.util.*;

/**
 * This class represents a 'rendered' or 'painted' dataset. It contains the original data (DataSet) and paint properties
 * as well as expressions, filters and more to provide processed data.
 */
public class Trace extends ChartPart {
    
    // The index in data set
    protected int  _index;

    // The Y Axis type
    private AxisType  _axisTypeY = AxisType.Y;

    // The expression to apply to X values
    private String  _exprX;

    // The expression to apply to Y values
    private String  _exprY;

    // The expression to apply to Z values
    private String  _exprZ;

    // Whether data is stacked
    private boolean  _stacked;

    // Whether trace is disabled
    private boolean  _disabled;

    // Whether to show legend entry
    private boolean  _showLegendEntry = true;

    // The DataStyleHpr
    private TraceStyleHpr _traceStyleHpr;

    // The RawData
    private DataSet _rawData = DataSet.newDataSet();

    // The Processed Data
    private DataSet _procData;

    // Processed Data in log form
    private DataSet[]  _logData;

    // Processed data in polar form
    private DataSet _polarData;

    // Processed data in polar XY form
    private DataSet _polarXYData;

    // Constants for properties
    public static final String DataType_Prop = DataSet.DataType_Prop;
    public static final String ThetaUhit_Prop = "ThetaUnit";
    public static final String AxisTypeY_Prop = "AxisTypeY";
    public static final String ExprX_Prop = "ExpressionX";
    public static final String ExprY_Prop = "ExpressionY";
    public static final String ExprZ_Prop = "ExpressionZ";
    public static final String Stacked_Prop = "Stacked";
    public static final String ShowLegendEntry_Prop = "ShowLegendEntry";
    public static final String Disabled_Prop = "Disabled";
    public static final String Point_Prop = "Points";

    /**
     * Constructor.
     */
    public Trace()
    {
        super();
    }

    /**
     * Returns the index in TraceList.
     */
    public int getIndex()  { return _index; }

    /**
     * Returns the DataType.
     */
    public DataType getDataType()
    {
        return _rawData.getDataType();
    }

    /**
     * Sets the DataType.
     */
    public void setDataType(DataType aDataType)
    {
        // If already set, just return
        if (aDataType == getDataType()) return;

        // Forward to RawData
        DataType old = getDataType();
        _rawData.setDataType(aDataType);

        // Clear cached data and firePropChange
        clearCachedData();
        firePropChange(DataType_Prop, old, aDataType);
    }

    /**
     * Returns the units for Theta data.
     */
    public DataSet.ThetaUnit getThetaUnit()  { return _rawData.getThetaUnit(); }

    /**
     * Sets the units for Theta data.
     */
    public void setThetaUnit(DataSet.ThetaUnit aValue)
    {
        // If already set, just return
        if (aValue == getThetaUnit()) return;

        // Forward to RawData
        DataSet.ThetaUnit old = getThetaUnit();
        _rawData.setThetaUnit(aValue);

        // Clear cached data and firePropChange
        clearCachedData();
        firePropChange(ThetaUhit_Prop, old, aValue);
    }

    /**
     * Returns the Y axis type.
     */
    public AxisType getAxisTypeY()  { return _axisTypeY; }

    /**
     * Sets the Y axis type.
     */
    public void setAxisTypeY(AxisType anAxisType)
    {
        // If already set, just return
        if (anAxisType == getAxisTypeY()) return;

        // If not Y AxisType, complain
        if (anAxisType==null || !anAxisType.isAnyY())
            throw new IllegalArgumentException("Trace.setAxisTypeY: Unsupported AxisTypeY: " + anAxisType);

        // Set and firePropChange
        firePropChange(AxisTypeY_Prop, _axisTypeY, _axisTypeY = anAxisType);
    }

    /**
     * Returns the expression to apply to X values.
     */
    public String getExprX()  { return _exprX; }

    /**
     * Sets the expressions to apply to X values.
     */
    public void setExprX(String anExpr)
    {
        if (Objects.equals(anExpr, getExprX())) return;
        firePropChange(ExprX_Prop, _exprX, _exprX = anExpr);
        clearCachedData();
    }

    /**
     * Returns the expression to apply to Y values.
     */
    public String getExprY()  { return _exprY; }

    /**
     * Sets the expressions to apply to Y values.
     */
    public void setExprY(String anExpr)
    {
        if (Objects.equals(anExpr, getExprY())) return;
        firePropChange(ExprY_Prop, _exprY, _exprY = anExpr);
        clearCachedData();
    }

    /**
     * Returns the expression to apply to Z values.
     */
    public String getExprZ()  { return _exprZ; }

    /**
     * Sets the expressions to apply to Z values.
     */
    public void setExprZ(String anExpr)
    {
        if (Objects.equals(anExpr, getExprZ())) return;
        firePropChange(ExprZ_Prop, _exprZ, _exprZ = anExpr);
        clearCachedData();
    }

    /**
     * Returns whether this trace is stacked.
     */
    public boolean isStacked()  { return _stacked; }

    /**
     * Sets whether this trace is stacked.
     */
    public void setStacked(boolean aValue)
    {
        if (aValue==isStacked()) return;
        firePropChange(Stacked_Prop, _stacked, _stacked = aValue);
    }

    /**
     * Returns whether this trace is disabled.
     */
    public boolean isDisabled()  { return _disabled; }

    /**
     * Sets whether this trace is disabled.
     */
    public void setDisabled(boolean aValue)
    {
        if (aValue==isDisabled()) return;
        firePropChange(Disabled_Prop, _disabled, _disabled = aValue);
    }

    /**
     * Returns whether this trace is enabled.
     */
    public boolean isEnabled()  { return !_disabled; }

    /**
     * Returns whether to show legend entry for this trace.
     */
    public boolean isShowLegendEntry()  { return _showLegendEntry; }

    /**
     * Sets whether to show legend entry for this trace.
     */
    public void setShowLegendEntry(boolean aValue)
    {
        if (aValue==isShowLegendEntry()) return;
        firePropChange(ShowLegendEntry_Prop, _showLegendEntry, _showLegendEntry = aValue);
    }

    /**
     * Returns the DataStyle for this trace (and ChartType).
     */
    public TraceStyle getTraceStyle()
    {
        if (_traceStyleHpr == null)
            _traceStyleHpr = new TraceStyleHpr(this);
        return _traceStyleHpr.getDataStyle();
    }

    /**
     * Returns the number of rows.
     */
    public int getRowCount()  { return _rawData.getRowCount(); }

    /**
     * Returns the number of columns.
     */
    public int getColCount()  { return _rawData.getColCount(); }

    /**
     * Returns the number of points.
     */
    public int getPointCount()
    {
        return _rawData.getPointCount();
    }

    /**
     * Sets the number of points.
     */
    public void setPointCount(int aValue)
    {
        _rawData.setPointCount(aValue);
    }

    /**
     * Returns the data point at given index.
     */
    public TracePoint getPoint(int anIndex)
    {
        return new TracePoint(this, anIndex);
    }

    /**
     * Adds a point for given components at given index.
     */
    public void addPoint(DataPoint aPoint, int anIndex)
    {
        _rawData.addPoint(aPoint, anIndex);
        clearCachedData();
        firePropChange(Point_Prop, aPoint, null, anIndex);
    }

    /**
     * Removes a point at given index.
     */
    public DataPoint removePoint(int anIndex)
    {
        // Get point at index
        DataPoint dataPoint = getPoint(anIndex);

        // Remove point from RawData
        _rawData.removePoint(anIndex);
        clearCachedData();
        firePropChange(Point_Prop, dataPoint, null, anIndex);

        // Return point
        return dataPoint;
    }

    /**
     * Clears all points.
     */
    public void clearPoints()
    {
        _rawData.clearPoints();
        clearCachedData();
    }

    /**
     * Adds a point for X/Y/Z/C values.
     */
    public void addPointXYZC(Double aX, Double aY, Double aZ, String aC)
    {
        DataPoint dpnt = new DataPoint(aX, aY, aZ, aC);
        addPoint(dpnt, getPointCount());
    }

    /**
     * Returns the X value at given index.
     */
    public double getX(int anIndex)
    {
        DataSet procData = getProcessedData();
        return procData.getX(anIndex);
    }

    /**
     * Returns the Y value at given index.
     */
    public double getY(int anIndex)
    {
        DataSet procData = getProcessedData();
        return procData.getY(anIndex);
    }

    /**
     * Returns the Z value at given index.
     */
    public double getZ(int anIndex)
    {
        DataSet procData = getProcessedData();
        return procData.getZ(anIndex);
    }

    /**
     * Returns the C value at given index.
     */
    public String getC(int anIndex)
    {
        DataSet procData = getProcessedData();
        return procData.getC(anIndex);
    }

    /**
     * Sets the C value at given index.
     */
    public void setValueC(String aValue, int anIndex)
    {
        _rawData.setC(aValue, anIndex);
        clearCachedData();
    }

    /**
     * Returns the X value at given index (null if not set).
     */
    public Double getValueX(int anIndex)
    {
        DataSet procData = getProcessedData();
        return procData.getValueX(anIndex);
    }

    /**
     * Sets the X value at given index.
     */
    public void setValueX(Double aValue, int anIndex)
    {
        _rawData.setValueX(aValue, anIndex);
        clearCachedData();
    }

    /**
     * Returns the Y value at given index (null if not set).
     */
    public Double getValueY(int anIndex)
    {
        DataSet procData = getProcessedData();
        return procData.getValueY(anIndex);
    }

    /**
     * Sets the Y value at given index.
     */
    public void setValueY(Double aValue, int anIndex)
    {
        _rawData.setValueY(aValue, anIndex);
        clearCachedData();
    }

    /**
     * Returns the Z value at given index (null if not set).
     */
    public Double getValueZ(int anIndex)
    {
        DataSet procData = getProcessedData();
        return procData.getValueZ(anIndex);
    }

    /**
     * Sets the Z value at given index.
     */
    public void setValueZ(Double aValue, int anIndex)
    {
        _rawData.setValueZ(aValue, anIndex);
        clearCachedData();
    }

    /**
     * Return data point as a string (either C or X).
     */
    public String getString(int anIndex)
    {
        // If point string is set, just return it
        String str = getC(anIndex);
        if(str!=null)
            return str;

        // If categories, return that
        Chart chart = getChart();
        List <String> cats = chart.getAxisX().getCategories();
        if (cats!=null && anIndex<cats.size())
            return cats.get(anIndex);

        // If start value is set
        int startValue = getTraceList().getStartValue();
        if (startValue!=0)
            return String.valueOf(startValue + anIndex);

        // Otherwise return x val (as int, if whole number)
        double val = getX(anIndex);
        if (val==(int)val)
            return String.valueOf((int)val);
        return DataUtils.formatValue(val);
    }

    /**
     * Returns the value for channel and record index.
     */
    public Object getValueForChannel(DataChan aChan, int anIndex)
    {
        switch (aChan) {
            case X: return getValueX(anIndex);
            case Y: return getValueY(anIndex);
            case Z: return getValueZ(anIndex);
            case I: return anIndex;
            case C: return getString(anIndex);
            case T: return getValueX(anIndex);
            case R: return getValueY(anIndex);
            default: throw new RuntimeException("Trace.getValueForChannelAndIndex: Unknown channel: " + aChan);
        }
    }

    /**
     * Sets the value for channel and record index.
     */
    public void setValueForChannel(Object aValue, DataChan aChan, int anIndex)
    {
        // Get point
        DataPoint dataPoint = getPoint(anIndex);

        switch (aChan) {
            case X:
                Double valX = aValue!=null ? SnapUtils.doubleValue(aValue) : null;
                setValueX(valX, anIndex);
                break;
            case Y:
                Double valY = aValue!=null ? SnapUtils.doubleValue(aValue) : null;
                setValueY(valY, anIndex);
                break;
            case Z:
                Double valZ = aValue!=null ? SnapUtils.doubleValue(aValue) : null;
                setValueZ(valZ, anIndex);
                break;
            case I:
                System.err.println("Trace.setValueForChannel: Shouldn't set value for index channel");
                break;
            case C:
                String valC = aValue!=null ? SnapUtils.stringValue(aValue) : null;
                setValueC(valC, anIndex);
                break;
            default: throw new RuntimeException("Trace.getValueForChannelAndIndex: Unknown channel: " + aChan);
        }

        // Get point
        DataPoint dataPoint2 = getPoint(anIndex);
        firePropChange(Point_Prop, dataPoint, dataPoint2, anIndex);
    }

    /**
     * Returns the value for given channel index and record index.
     */
    public Object getValueForChannelIndex(int aChanIndex, int anIndex)
    {
        DataType dataType = getDataType();
        DataChan chan = dataType.getChannel(aChanIndex);
        return getValueForChannel(chan, anIndex);
    }

    /**
     * Sets given value for given channel index and record index.
     */
    public void setValueForChannelIndex(Object aValue, int aChanIndex, int anIndex)
    {
        DataType dataType = getDataType();
        DataChan chan = dataType.getChannel(aChanIndex);
        setValueForChannel(aValue, chan, anIndex);
    }

    /**
     * Returns the raw data.
     */
    public DataSet getRawData()  { return _rawData; }

    /**
     * Sets the raw data.
     */
    public void setRawData(DataSet aDataSet)
    {
        _rawData = aDataSet;
        clearCachedData();
    }

    /**
     * Returns the Processed Data.
     */
    public DataSet getProcessedData()
    {
        // If already set, just return
        if (_procData != null) return _procData;

        // Get expressions
        String exprX = getExprX();
        String exprY = getExprY();
        String exprZ = getExprZ();
        DataSet procData = DataSetUtils.getProcessedData(_rawData, exprX, exprY, exprZ);
        return _procData = procData;
    }

    /**
     * Returns the ProcessedData converted to log.
     */
    public DataSet getLogData(boolean doLogX, boolean doLogY)
    {
        // If already set, just return
        int index = (doLogX && doLogY) ? 2 : doLogX ? 0 : 1;
        if (_logData != null && _logData[index] != null) return _logData[index];

        // Make sure LogData array is present
        if (_logData == null)
            _logData = new DataSet[3];

        // If already DataType.isPolar, set/return
        DataSet procData = getProcessedData();
        DataSet logData = DataSetUtils.getLogData(procData, doLogX, doLogY);
        return _logData[index] = logData;
    }

    /**
     * Returns the ProcessedData in polar form (just normal data if already DataType.isPolar).
     */
    public DataSet getPolarData()
    {
        // If already set, just return
        if (_polarData != null) return _polarData;

        // If already DataType.isPolar, set/return
        DataSet procData = getProcessedData();
        DataType dataType = getDataType();
        if (dataType.isPolar())
            return _polarData = procData;

        // Get Polar DataType that makes the most sense
        DataType polarDataType = DataType.TR;
        if (dataType.hasZ())
            polarDataType = dataType == DataType.XYZZ ? DataType.TRZZ : DataType.TRZ;

        // Convert, set, return
        DataSet polarData = DataSetUtils.getPolarDataForType(procData, polarDataType);
        return _polarData = polarData;
    }

    /**
     * Returns the PolarData converted to XY format.
     */
    public DataSet getPolarXYData()
    {
        // If already set, just return
        if (_polarXYData != null) return _polarXYData;

        // Get PolarData, convert to polarXY, set/return
        DataSet polarData = getPolarData();
        DataSet xyData = DataSetUtils.getPolarXYDataForPolar(polarData);
        return _polarXYData = xyData;
    }

    /**
     * Returns the minimum Z value in this trace.
     */
    public double getMinZ()
    {
        DataSet procData = getProcessedData();
        return procData.getMinZ();
    }

    /**
     * Returns the maximum Z value in this trace.
     */
    public double getMaxZ()
    {
        DataSet procData = getProcessedData();
        return procData.getMaxZ();
    }

    /**
     * Returns whether this trace is clear (no name and no values).
     */
    public boolean isClear()
    {
        if (getName()!=null && getName().length()>0)
            return false;
        return _rawData.isClear();
    }

    /**
     * Called when a points are added, removed or modified.
     */
    protected void clearCachedData()
    {
        _procData = null;
        _logData = null;
        _polarData = null;
        _polarXYData = null;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String str = "Trace { " + "Name=" + getName() + ", DataType=" + getDataType() + ", PointCount=" + getPointCount();
        DataSet dataSet = getRawData();
        for (DataChan chan : getDataType().getChannels()) {
            MinMax minMax = dataSet.getMinMax(chan);
            str += ", Min" + chan + "=" + minMax.getMin() + ", Max" + chan + "=" + minMax.getMax();
        }
        return str + '}';
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive AxisTypeY
        if (getAxisTypeY() != AxisType.Y)
            e.add(AxisTypeY_Prop, getAxisTypeY());

        // Archive ExprX, ExprY, ExprZ
        if (getExprX() != null && getExprX().length() > 0)
            e.add(ExprX_Prop, getExprX());
        if (getExprY() != null && getExprY().length() > 0)
            e.add(ExprY_Prop, getExprY());
        if (getExprZ() != null && getExprZ().length() > 0)
            e.add(ExprZ_Prop, getExprZ());

        // Archive Stacked, Disabled, ShowLegendEntry
        if (isStacked())
            e.add(Stacked_Prop, true);
        if (isDisabled())
            e.add(Disabled_Prop, true);
        if (!isShowLegendEntry())
            e.add(ShowLegendEntry_Prop, false);

        // Archive DataStyle
        TraceStyle traceStyle = getTraceStyle();
        XMLElement dataStyleXML = traceStyle.toXML(anArchiver);
        if (dataStyleXML.getAttributeCount() > 0) {
            dataStyleXML.setName("DataStyle");
            e.addElement(dataStyleXML);
        }

        // Archive RawData
        DataSet rawData = getRawData();
        rawData.toXML(anArchiver, e);

        // Return element
        return e;
    }

    /**
     * Unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic attributes
        super.fromXML(anArchiver, anElement);

        // Unarchive AxisTypeY
        String axisTypeStr = anElement.getAttributeValue(AxisTypeY_Prop);
        if (axisTypeStr != null)
            setAxisTypeY(AxisType.valueOf(axisTypeStr));

        // Unarchive ExprX, ExprY, ExprZ
        if (anElement.hasAttribute(ExprX_Prop))
            setExprX(anElement.getAttributeValue(ExprX_Prop));
        if (anElement.hasAttribute(ExprY_Prop))
            setExprY(anElement.getAttributeValue(ExprY_Prop));
        if (anElement.hasAttribute(ExprZ_Prop))
            setExprZ(anElement.getAttributeValue(ExprZ_Prop));

        // Unarchive Stacked, Disabled, ShowLegendEntry
        if (anElement.hasAttribute(Stacked_Prop))
            setStacked(anElement.getAttributeBoolValue(Stacked_Prop, false));
        if (anElement.hasAttribute(Disabled_Prop))
            setDisabled(anElement.getAttributeBoolValue(Disabled_Prop, false));
        if (anElement.hasAttribute(ShowLegendEntry_Prop))
            setShowLegendEntry(anElement.getAttributeBoolValue(ShowLegendEntry_Prop));

        // Unarchive DataStyle
        XMLElement dataStyleXML = anElement.getElement("DataStyle");
        if (dataStyleXML != null)
            getTraceStyle().fromXML(anArchiver, dataStyleXML);

        // Unarchive RawData
        DataSet rawData = getRawData();
        rawData.fromXML(anArchiver, anElement);

        // Legacy
        if (anElement.hasAttribute(TraceStyle.ShowSymbols_Prop)) {
            boolean showSymbols = anElement.getAttributeBoolValue(TraceStyle.ShowSymbols_Prop);
            getTraceStyle().setShowSymbols(showSymbols);
        }

        // Return this part
        return this;
    }
}