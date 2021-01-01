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

    // The format of the data
    private DataType _dataType;

    // The Y Axis type
    private AxisType  _axisTypeY = AxisType.Y;

    // Whether dataset is disabled
    private boolean  _disabled;

    // Whether to show symbols
    private boolean  _showSymbols;

    // The values
    private List <DataPoint>  _points = new ArrayList<>();
    
    // Cached array of Y values
    private double  _dataY[];

    // Cached array of ratios
    private double  _ratios[];

    // Cached total of Y
    private double  _total;

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
        if (_dataType!=null) return _dataType;

        return _dataType = guessDataType();
    }

    /**
     * Tries to guess the data type.
     */
    private DataType guessDataType()
    {
        if (getPointCount()==0)
            return DataType.UNKNOWN;

        for (DataPoint pnt : _points) {
            if (pnt.getC()!=null)
                return DataType.CY;
            if (pnt.getValueX()!=null)
                return DataType.XY;
        }
        return DataType.IY;
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
    public int getPointCount()  { return _points.size(); }

    /**
     * Sets the number of points.
     */
    public void setPointCount(int aValue)
    {
        // If silly value, just return
        if (aValue<1 || aValue>1000) return;

        // If not enough points, add
        while (aValue>getPointCount())
            addPointXY(null, null);

        // If too many points, remove
        while (aValue<getPointCount())
            removePoint(getPointCount()-1);
    }

    /**
     * Returns the data points list.
     */
    public List <DataPoint> getPoints()  { return _points; }

    /**
     * Returns the data point at given index.
     */
    public DataPoint getPoint(int anIndex)
    {
        return anIndex<getPointCount()? _points.get(anIndex) : null;
    }

    /**
     * Adds a point.
     */
    public void addPoint(DataPoint aPoint)
    {
        addPoint(aPoint, getPointCount());
    }

    /**
     * Adds a point.
     */
    public void addPoint(DataPoint aPoint, int anIndex)
    {
        aPoint._dset = this;
        aPoint._index = anIndex;
        _points.add(anIndex, aPoint);
        firePropChange(Point_Prop, null, aPoint, anIndex);

        // If data type still unknown, clear to re-evaluate
        if (_dataType == DataType.UNKNOWN)
            _dataType = null;
    }

    /**
     * Removes a point at given index.
     */
    public DataPoint removePoint(int anIndex)
    {
        DataPoint dpnt = _points.remove(anIndex);
        firePropChange(Point_Prop, dpnt, null, anIndex);
        return dpnt;
    }

    /**
     * Sets point at given index to new point.
     */
    public void setPoint(DataPoint aPoint, int anIndex)
    {
        removePoint(anIndex);
        addPoint(aPoint, anIndex);
    }

    /**
     * Adds a point for X and Y values.
     */
    public void addPointXY(Double aX, Double aY)
    {
        DataPoint dpnt = new DataPoint(aX, aY);
        addPoint(dpnt);
    }

    /**
     * Adds a point for a string and value.
     */
    public void addPointCY(String aStr, Double aValue)
    {
        DataPoint dpnt = new DataPoint(aStr, aValue);
        addPoint(dpnt);
    }

    /**
     * Returns the X value at given index.
     */
    public double getX(int anIndex)
    {
        DataPoint dp = getPoint(anIndex);
        return dp!=null ? dp.getX() : 0;
    }

    /**
     * Returns the Y value at given index.
     */
    public double getY(int anIndex)
    {
        DataPoint dp = getPoint(anIndex);
        return dp!=null ? dp.getY() : 0;
    }

    /**
     * Returns the C value at given index.
     */
    public String getC(int anIndex)
    {
        DataPoint dp = getPoint(anIndex);
        return dp!=null ? dp.getC() : null;
    }

    /**
     * Returns the value at given index.
     */
    public Double getValueX(int anIndex)
    {
        DataPoint dp = getPoint(anIndex);
        return dp!=null ? dp.getValueX() : null;
    }

    /**
     * Returns the value at given index.
     */
    public Double getValueY(int anIndex)
    {
        DataPoint dp = getPoint(anIndex);
        return dp!=null ? dp.getValueY() : null;
    }

    /**
     * Return data point as a string (either C or X).
     */
    public String getString(int anIndex)
    {
        // Get DataPoint
        DataPoint dpnt = getPoint(anIndex);
        if (dpnt==null)
            return null;

        // If point string is set, just return it
        String str = dpnt.getC();
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
        double kval = dpnt.getX();
        if (kval==(int)kval)
            return String.valueOf((int)kval);
        //return String.valueOf(kval);
        return DataUtils.formatValue(kval);
    }

    /**
     * Sets the X value at given index.
     */
    public void setValueX(Double aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex>=getPointCount()) setPointCount(anIndex+1);

        // Get old point, copy for new value, swap old point for new
        DataPoint pnt = getPoint(anIndex).copyForX(aValue);
        setPoint(pnt, anIndex);
    }

    /**
     * Sets the Y value at given index.
     */
    public void setValueY(Double aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex>=getPointCount()) setPointCount(anIndex+1);

        // Get old point, copy for new value, swap old point for new
        DataPoint pnt = getPoint(anIndex).copyForY(aValue);
        setPoint(pnt, anIndex);
    }

    /**
     * Sets the C value at given index.
     */
    public void setValueC(String aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex>=getPointCount()) setPointCount(anIndex+1);

        // Get old point, copy for new value, swap old point for new
        DataPoint pnt = getPoint(anIndex).copyForC(aValue);
        setPoint(pnt, anIndex);
    }

    /**
     * Sets the values.
     */
    public void setValues(Double ... theVals)
    {
        _points.clear();
        for (Double val : theVals)
            addPointXY(null, val);
    }

    /**
     * Returns an array of dataset X values.
     */
    public double[] getDataX()
    {
        int count = getPointCount();
        double vals[] = new double[count];
        for (int i=0;i<count;i++) { double v = getX(i); vals[i] = v; }
        return vals;
    }

    /**
     * Returns an array of dataset Y values.
     */
    public double[] getDataY()
    {
        if (_dataY !=null) return _dataY;
        int count = getPointCount(); _total = 0;
        double vals[] = new double[count];
        for (int i=0;i<count;i++) { double v = getY(i); vals[i] = v; _total += v; }
        return _dataY = vals;
    }

    /**
     * Returns an array of dataset C values.
     */
    public String[] getDataC()
    {
        int count = getPointCount();
        String vals[] = new String[count];
        for (int i=0;i<count;i++) { String v = getC(i); vals[i] = v; }
        return vals;
    }

    /**
     * Returns the total of all values.
     */
    public double getTotalY()
    {
        if (_dataY ==null) getDataY();
        return _total;
    }

    /**
     * Returns an array of dataset ratios.
     */
    public double[] getRatiosYtoTotalY()
    {
        // If value cached, just return
        if (_ratios!=null) return _ratios;

        // Calculate rations and return
        double vals[] = getDataY();
        double total = getTotalY();
        int count = vals.length;
        double ratios[] = new double[count];
        for (int i=0;i<count;i++) ratios[i] = vals[i]/total;
        return _ratios = ratios;
    }

    /**
     * Returns the minimum X value in this dataset.
     */
    public double getMinX()
    {
        double min = Float.MAX_VALUE;
        for (DataPoint dp : _points)
            if (dp.getX()<min)
                min = dp.getX();
        return min;
    }

    /**
     * Returns the maximum X value in this dataset.
     */
    public double getMaxX()
    {
        double max = -Float.MAX_VALUE;
        for (DataPoint dp : _points)
            if (dp.getX()>max)
                max = dp.getX();
        return max;
    }

    /**
     * Returns the minimum Y value in this dataset.
     */
    public double getMinY()
    {
        double min = Float.MAX_VALUE;
        for (DataPoint dp : _points)
            if (dp.getY()<min)
                min = dp.getY();
        return min;
    }

    /**
     * Returns the maximum Y value in this dataset.
     */
    public double getMaxY()
    {
        double max = -Float.MAX_VALUE;
        for (DataPoint dp : _points)
            if (dp.getY()>max)
                max = dp.getY();
        return max;
    }

    /**
     * Returns whether this dataset is clear (no name and no values).
     */
    public boolean isClear()
    {
        if (getName()!=null && getName().length()>0)
            return false;
        for (DataPoint dp : getPoints())
            if (dp.getValueY()!=null)
                return false;
        return true;
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
                    addPointCY(null, y);
                    break;
                }

                case XY: {
                    double x = valX != null ? SnapUtils.doubleValue(valX) : 0;
                    double y = valY != null ? SnapUtils.doubleValue(valY) : 0;
                    addPointXY(x, y);
                    break;
                }

                case CY: {
                    double y = valY != null ? SnapUtils.doubleValue(valY) : 0;
                    addPointCY(valX, y);
                    break;
                }

                default:
                    System.out.println("DataSet.replaceData: Unsupported data type: " + dataType);
                    return;
            }
        }
    }

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
                addPointCY(null, v);
        }

        // Handle XY
        else if (dataType==DataType.XY && dataX!=null && dataY!=null) {
            int len = Math.min(dataX.length, dataY.length);
            for (int i=0; i<len; i++)
                addPointXY(dataX[i], dataY[i]);
        }

        // Handle CY
        else if (dataType==DataType.CY && dataC!=null && dataY!=null) {
            int len = Math.min(dataC.length, dataY.length);
            for (int i=0; i<len; i++)
                addPointCY(dataC[i], dataY[i]);
        }

        // Complain
        else System.err.println("DataSet.toXML: Unknown DataType: " + dataType);

        // Return this part
        return this;
    }
}