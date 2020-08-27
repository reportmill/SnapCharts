package snapcharts.views;
import snap.view.ParentView;

/**
 * A class to layout DataView and X/Y axis views.
 */
public class DataViewBox extends ParentView {

    // The DataView
    private DataView  _dataView;

    // The AxisViews
    private AxisViewX  _axisX;
    private AxisViewY  _axisY;

    /**
     * Constructor.
     */
    public DataViewBox(ChartView aChartView)
    {
        setGrowWidth(true);
        setGrowHeight(true);

        _axisX = aChartView.getAxisX();
        _axisY = aChartView.getAxisY();
        addChild(_axisY);
        addChild(_axisX);
    }

    /**
     * Sets the DataView.
     */
    protected void setDataView(DataView aDA)
    {
        // Remove old
        if (_dataView !=null)
            removeChild(_dataView);

        // Set/add new
        _dataView = aDA;
        addChild(_dataView, 1);

        // Update Axes
        _axisY._dataView = _axisX._dataView = aDA;
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        double prefW = _dataView.getPrefWidth();
        if (_axisY.isVisible())
            prefW += _axisY.getPrefWidth();
        return prefW;
    }

    /**
     * Calculates the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        double prefH = _dataView.getPrefHeight();
        if (_axisX.isVisible())
            prefH += _axisX.getPrefHeight();
        return prefH;
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        // Set chart area height first, since height can effect yaxis label width
        double pw = getWidth(), ph = getHeight();
        double ah = _axisX.isVisible() ? _axisX.getPrefHeight() : 0;
        _dataView.setHeight(ph - ah);

        // Now set bounds of areay, xaxis and yaxis
        double aw = _axisY.isVisible()? _axisY.getPrefWidth(ph - ah) : 0;
        double cw = pw - aw, ch = ph - ah;
        _dataView.setBounds(aw,0,cw,ch);
        _axisX.setBounds(aw,ch,cw,ah);
        _axisY.setBounds(0,0,aw,ch);
    }
}
