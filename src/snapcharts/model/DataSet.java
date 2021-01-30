package snapcharts.model;
import snap.util.ListSel;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

import java.util.*;

/**
 * A class to represent a list of data points.
 */
public class DataSet extends ChartPart {
    
    // The DataSetList that owns this dataset
    protected DataSetList  _dsetList;

    // The index in data set
    protected int  _index;

    // The Y Axis type
    private AxisType  _axisTypeY = AxisType.Y;

    // Whether dataset is disabled
    private boolean  _disabled;

    // Whether to show symbols
    private boolean  _showSymbols;

    // The RawData
    private RawData  _rawData = new RawDataAsPoints();

    // Constants for properties
    public static final String Disabled_Prop = "Disabled";
    public static final String Point_Prop = "Points";
    public static final String ShowSymbols_Prop = "ShowSymbols";
    public static final String AxisTypeY_Prop = "AxisTypeY";

    /**
     * Constructor.
     */
    public DataSet()
    {
        super();
    }

    /**
     * Returns the chart.
     */
    @Override
    public Chart getChart()  { return _dsetList !=null ? _dsetList.getChart() : null; }

    /**
     * Returns the dataset.
     */
    public DataSetList getDataSetList()  { return _dsetList; }

    /**
     * Returns the index in dataset.
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
        _rawData.setDataType(aDataType);
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
        if (anAxisType==getAxisTypeY()) return;
        if (anAxisType==null || !anAxisType.isAnyY())
            throw new IllegalArgumentException("DataSet.setAxisTypeY: Unsupported AxisTypeY: " + anAxisType);
        firePropChange(AxisTypeY_Prop, _axisTypeY, _axisTypeY = anAxisType);
    }

    /**
     * Returns whether to show symbols for this DataSet.
     */
    public boolean isShowSymbols()  { return _showSymbols; }

    /**
     * Sets whether to show symbols for this DataSet.
     */
    public void setShowSymbols(boolean aValue)
    {
        if (aValue==isShowSymbols()) return;
        firePropChange(ShowSymbols_Prop, _showSymbols, _showSymbols = aValue);
    }

    /**
     * Returns whether this dataset is disabled.
     */
    public boolean isDisabled()  { return _disabled; }

    /**
     * Sets whether this dataset is disabled.
     */
    public void setDisabled(boolean aValue)
    {
        if (aValue==isDisabled()) return;
        firePropChange(Disabled_Prop, _disabled, _disabled = aValue);
    }

    /**
     * Returns whether this dataset is enabled.
     */
    public boolean isEnabled()  { return !_disabled; }

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
    public DataPoint getPoint(int anIndex)
    {
        return new DataPoint(this, anIndex);
    }

    /**
     * Adds a point for given components at given index.
     */
    public void addPoint(DataPoint aPoint, int anIndex)
    {
        _rawData.addPoint(aPoint, anIndex);
        firePropChange(Point_Prop, aPoint, null, anIndex);
        pointsDidChange();
    }

    /**
     * Removes a point at given index.
     */
    public DataPoint removePoint(int anIndex)
    {
        DataPoint dpnt = _rawData.removePoint(anIndex);
        firePropChange(Point_Prop, dpnt, null, anIndex);
        pointsDidChange();
        return dpnt;
    }

    /**
     * Clears all points.
     */
    public void clearPoints()
    {
        _rawData.clearPoints();
        pointsDidChange();
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
        return _rawData.getX(anIndex);
    }

    /**
     * Returns the Y value at given index.
     */
    public double getY(int anIndex)
    {
        return _rawData.getY(anIndex);
    }

    /**
     * Returns the Z value at given index.
     */
    public double getZ(int anIndex)
    {
        return _rawData.getZ(anIndex);
    }

    /**
     * Returns the C value at given index.
     */
    public String getC(int anIndex)
    {
        return _rawData.getC(anIndex);
    }

    /**
     * Sets the C value at given index.
     */
    public void setValueC(String aValue, int anIndex)
    {
        _rawData.setC(aValue, anIndex);
        pointsDidChange();
    }

    /**
     * Returns the X value at given index (null if not set).
     */
    public Double getValueX(int anIndex)
    {
        return _rawData.getValueX(anIndex);
    }

    /**
     * Sets the X value at given index.
     */
    public void setValueX(Double aValue, int anIndex)
    {
        _rawData.setValueX(aValue, anIndex);
        pointsDidChange();
    }

    /**
     * Returns the Y value at given index (null if not set).
     */
    public Double getValueY(int anIndex)
    {
        return _rawData.getValueY(anIndex);
    }

    /**
     * Sets the Y value at given index.
     */
    public void setValueY(Double aValue, int anIndex)
    {
        _rawData.setValueY(aValue, anIndex);
        pointsDidChange();
    }

    /**
     * Returns the Z value at given index (null if not set).
     */
    public Double getValueZ(int anIndex)
    {
        return _rawData.getValueZ(anIndex);
    }

    /**
     * Sets the Z value at given index.
     */
    public void setValueZ(Double aValue, int anIndex)
    {
        _rawData.setValueZ(aValue, anIndex);
        pointsDidChange();
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
        int startValue = getDataSetList().getStartValue();
        if (startValue!=0)
            return String.valueOf(startValue + anIndex);

        // Otherwise return x val (as int, if whole number)
        double val = getX(anIndex);
        if (val==(int)val)
            return String.valueOf((int)val);
        return DataUtils.formatValue(val);
    }

    /**
     * Returns an array of dataset X values.
     */
    public double[] getDataX()
    {
        return _rawData.getDataX();
    }

    /**
     * Returns an array of dataset Y values.
     */
    public double[] getDataY()
    {
        return _rawData.getDataY();
    }

    /**
     * Returns an array of dataset Z values.
     */
    public double[] getDataZ()
    {
        return _rawData.getDataZ();
    }

    /**
     * Returns an array of dataset C values.
     */
    public String[] getDataC()
    {
        return _rawData.getDataC();
    }

    /**
     * Returns the minimum X value in this dataset.
     */
    public double getMinX()
    {
        return _rawData.getMinX();
    }

    /**
     * Returns the maximum X value in this dataset.
     */
    public double getMaxX()
    {
        return _rawData.getMaxX();
    }

    /**
     * Returns the minimum Y value in this dataset.
     */
    public double getMinY()
    {
        return _rawData.getMinY();
    }

    /**
     * Returns the maximum Y value in this dataset.
     */
    public double getMaxY()
    {
        return _rawData.getMaxY();
    }

    /**
     * Returns the minimum Z value in this dataset.
     */
    public double getMinZ()
    {
        return _rawData.getMinZ();
    }

    /**
     * Returns the maximum Z value in this dataset.
     */
    public double getMaxZ()
    {
        return _rawData.getMaxZ();
    }

    /**
     * Returns whether this dataset is clear (no name and no values).
     */
    public boolean isClear()
    {
        if (getName()!=null && getName().length()>0)
            return false;
        return _rawData.isClear();
    }

    /**
     * Replaces data.
     */
    public void deleteData(ListSel aSel)
    {
        int indexes[] = aSel.getIndexes();
        for (int i = indexes.length - 1; i >= 0; i--) {
            int ind = indexes[i];
            if (ind < getPointCount())
                removePoint(ind);
        }
    }

    /**
     * Replaces data.
     */
    public void replaceData(String theCells[][], ListSel aSel)
    {
        DataType dataType = getDataType();
        int indexes[] = aSel.getIndexes();

        // Remove currently selected cells
        for (int i=indexes.length-1; i>=0; i--) {
            int ind = indexes[i];
            if (ind<getPointCount())
                removePoint(ind);
        }

        // Update DataType
        if (dataType==DataType.UNKNOWN || getPointCount()==0) {
            dataType = DataUtils.guessDataType(theCells);
        }

        // Add Cells
        for (String line[] : theCells) {

            if (line.length==0) continue;

            // Get vals: If only one val on line it's Y, X is index
            String valX = line.length>1 ? line[0] : null;
            String valY = line.length>1 ? line[1] : line[0];

            switch (dataType) {

                case IY: {
                    double y = valY != null ? SnapUtils.doubleValue(valY) : 0;
                    addPointXYZC(null, y, null, null);
                    break;
                }

                case XY: {
                    double x = valX != null ? SnapUtils.doubleValue(valX) : 0;
                    double y = valY != null ? SnapUtils.doubleValue(valY) : 0;
                    addPointXYZC(x, y, null, null);
                    break;
                }

                case CY: {
                    double y = valY != null ? SnapUtils.doubleValue(valY) : 0;
                    addPointXYZC(null, y, null, valX);
                    break;
                }

                default:
                    System.out.println("DataSet.replaceData: Unsupported data type: " + dataType);
                    return;
            }
        }
    }

    /**
     * Called when a points are added, removed or modified.
     */
    protected void pointsDidChange()  { }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive DataType
        DataType dataType = getDataType();
        e.add("DataType", dataType);

        // Archive ShowSymbols, Disabled
        if (isShowSymbols())
            e.add(ShowSymbols_Prop, true);
        if (isDisabled())
            e.add(Disabled_Prop, true);

        // Archive AxisTypeY
        if (getAxisTypeY() != AxisType.Y)
            e.add(AxisTypeY_Prop, getAxisTypeY());

        // If XY, add DataX values
        if (dataType==DataType.XY) {
            String dataXStr = DataUtils.getStringForDoubleArray(getDataX());
            XMLElement dataX_XML = new XMLElement("DataX");
            dataX_XML.setValue(dataXStr);
            e.add(dataX_XML);
        }

        // If CY, add DataC values
        if (dataType==DataType.CY) {
            String dataCStr = Arrays.toString(getDataC());
            XMLElement dataC_XML = new XMLElement("DataC");
            dataC_XML.setValue(dataCStr);
            e.add(dataC_XML);
        }

        // If not Unknown, add DataY values
        if (dataType!=DataType.UNKNOWN) {
            String dataYStr = DataUtils.getStringForDoubleArray(getDataY());
            XMLElement dataY_XML = new XMLElement("DataY");
            dataY_XML.setValue(dataYStr);
            e.add(dataY_XML);
        }

        // Complain
        else System.err.println("DataSet.toXML: Unknown DataType: " + dataType);

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

        // Unarchive DataType
        String dataTypeStr = anElement.getAttributeValue("DataType");
        DataType dataType = DataType.valueOf(dataTypeStr);

        // Unarchive ShowSymbols, Disabled
        setShowSymbols(anElement.getAttributeBoolValue(ShowSymbols_Prop, false));
        setDisabled(anElement.getAttributeBoolValue(Disabled_Prop, false));

        // Archive AxisTypeY
        String axisTypeStr = anElement.getAttributeValue(AxisTypeY_Prop);
        if (axisTypeStr != null)
            setAxisTypeY(AxisType.valueOf(axisTypeStr));

        // Get DataX
        double dataX[] = null;
        XMLElement dataX_XML = anElement.get("DataX");
        if (dataX_XML!=null) {
            String dataXStr = dataX_XML.getValue();
            dataX = DataUtils.getDoubleArrayForString(dataXStr);
        }

        // Get DataY
        double dataY[] = null;
        XMLElement dataY_XML = anElement.get("DataY");
        if (dataY_XML!=null) {
            String dataYStr = dataY_XML.getValue();
            dataY = DataUtils.getDoubleArrayForString(dataYStr);
        }

        // Get DataC
        String dataC[] = null;
        XMLElement dataC_XML = anElement.get("DataC");
        if (dataC_XML!=null) {
            String dataCStr = dataC_XML.getValue();
            dataC = DataUtils.getStringArrayForString(dataCStr);
        }

        // Handle IY
        if (dataType==DataType.IY && dataY!=null) {
            for (double v : dataY)
                addPointXYZC(null, v, null, null);
        }

        // Handle XY
        else if (dataType==DataType.XY && dataX!=null && dataY!=null) {
            int len = Math.min(dataX.length, dataY.length);
            for (int i=0; i<len; i++)
                addPointXYZC(dataX[i], dataY[i], null, null);
        }

        // Handle CY
        else if (dataType==DataType.CY && dataC!=null && dataY!=null) {
            int len = Math.min(dataC.length, dataY.length);
            for (int i=0; i<len; i++)
                addPointXYZC(null, dataY[i], null, dataC[i]);
        }

        // Complain
        else System.err.println("DataSet.toXML: Unknown DataType: " + dataType);

        // Return this part
        return this;
    }
}