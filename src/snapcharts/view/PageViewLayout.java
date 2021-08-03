package snapcharts.view;
import snap.geom.Insets;
import snap.geom.Rect;
import snap.view.View;
import snap.view.ViewProxy;

/**
 * Layout functionality for PageView.
 */
public class PageViewLayout {

    // The page
    private PageView  _page;

    // The page
    private ViewProxy<PageView>  _pageProxy;

    // The page area bounds
    private int  _areaX, _areaY, _areaW, _areaH;

    // The proxies for ChartViews
    private ViewProxy<ChartView>[] _chartProxies;

    /**
     * Constructor.
     */
    public PageViewLayout(PageView aPage)
    {
        _page = aPage;
    }

    /**
     * Layout page.
     */
    public void layoutPage()
    {
        _pageProxy = new ViewProxy<>(_page);
        _chartProxies = _pageProxy.getChildrenForClass(ChartView.class);

        Insets ins = _page.getInsetsAll();
        _areaX = (int) Math.round(ins.left);
        _areaY = (int) Math.round(ins.top);
        _areaW = (int) Math.round(_page.getWidth() - ins.getWidth());
        _areaH = (int) Math.round(_page.getHeight() - ins.getHeight());

        // If ChartScale not 1, apply it
        double chartScale = _page.getChartScale();
        if (chartScale != 1) {
            _areaW = (int) Math.round(_areaW * chartScale);
            _areaH = (int) Math.round(_areaH * chartScale);
        }

        // Do simple layout of charts in grid
        layoutChartsInGrid();

        // Force charts to layout their children
        for (View child : _page.getChildren())
            if (child instanceof ChartView)
                ((ChartView)child).layout();

        // Set DataViews children in ChartView proxies
        for (ViewProxy<ChartView> chartProxy : _chartProxies) {
            DataView dataView = chartProxy.getView().getChildForClass(DataView.class);
            ViewProxy dataViewProxy = ViewProxy.getProxy(dataView);
            dataViewProxy.setBounds(dataView.getX(), dataView.getY(), dataView.getWidth(), dataView.getHeight());
            chartProxy.setChildren(new ViewProxy[] { dataViewProxy });
            chartProxy.getView().relayout();
        }

        // Synchronize Chart DataViews
        synchChartDataViewSizes();

        // Update Chart bounds from proxies (clear ChartProxy children so Chart bounds gets set)
        for (ViewProxy chartProxy : _chartProxies)
            chartProxy.setChildren(null);
        _pageProxy.setBoundsInClient();

        // If ChartScale not 1, adjust Chart scales and X/Y
        if (chartScale != 1) {
            double scaleBack = 1 / chartScale;
            for (ViewProxy<ChartView> chartProxy : _chartProxies) {
                ChartView chartView = chartProxy.getView();
                chartView.setScale(scaleBack);
                double chartX = _areaX + (chartProxy.x - _areaX) * scaleBack - chartProxy.width * (1 - scaleBack) / 2;
                double chartY = _areaY + (chartProxy.y - _areaY) * scaleBack - chartProxy.height * (1 - scaleBack) / 2;
                chartView.setXY(chartX, chartY);
            }
        }
    }

    /**
     * Does simple grid layout.
     */
    private void layoutChartsInGrid()
    {
        double spaceW = 0; //20
        double spaceH = 0; //20

        int rowCount = getRowCount();
        int colCount = getColCount();
        double chartW = Math.round((_areaW - spaceW * (colCount-1)) / colCount);
        double chartH = Math.round((_areaH - spaceH * (rowCount-1)) / rowCount);

        for (int i=0; i<rowCount; i++) {
            for (int j=0; j<colCount; j++) {
                ViewProxy<ChartView> chart = getChartForRowCol(i, j);
                if (chart==null) break;
                double chartX = _areaX + (chartW + spaceW) * j;
                double chartY = _areaY + (chartH + spaceH) * i;
                chart.setBounds(chartX, chartY, chartW, chartH);
            }
        }

        _pageProxy.setBoundsInClient();
    }

    /**
     * Calculate the optimal data area size and chart margin sizes and set in all page charts.
     */
    public void synchChartDataViewSizes()
    {
        // Get margin column widths and total margin col width
        int colCount = getColCount();
        int marginsXCount = colCount + 1;
        int[] marginColsW = new int[marginsXCount];
        int totalMarginColsW = 0;
        for (int i=0; i<marginsXCount; i++)
        {
            marginColsW[i] = getMaxMarginColWidth(i);
            totalMarginColsW += marginColsW[i];
        }

        // Calculate unified data area width
        double totalDataW = _areaW - totalMarginColsW;
        int dataW = (int) (totalDataW / colCount);

        // Set all charts to have new data area width and margin column widths
        setDataViewWidthForAllCharts(dataW, marginColsW);

        // Get margin row heights and total margin row height
        int rowCount = getRowCount();
        int marginsYCount = rowCount + 1;
        int[] marginRowsH = new int[marginsYCount];
        int totalMarginRowsH = 0;
        for (int i=0; i<marginsYCount; i++)
        {
            marginRowsH[i] = getMaxMarginRowHeight(i);
            totalMarginRowsH += marginRowsH[i];
        }

        // If not enough charts for row/cols, trim area W/H
        double areaH2 = _areaH;
        int chartCount = getChartCount();
        int realRowCount = chartCount / colCount + (chartCount % colCount != 0 ? 1 : 0);
        if (realRowCount!=rowCount) {
            areaH2 = (int) Math.round(areaH2 * realRowCount / rowCount);
            rowCount = realRowCount;
        }

        // Calculate unified data area height
        double totalDataH = areaH2 - totalMarginRowsH;
        int dataH = (int) (totalDataH / rowCount);

        // Set all charts to have new data area height and margin column height
        setDataViewHeightForAllCharts(dataH, marginRowsH);
    }

    /**
     * Returns the width of given margin column index (the space between Chart.DataViews).
     */
    private int getMaxMarginColWidth(int aCol)
    {
        int rowCount = getRowCount();
        int colCount = getColCount();
        int maxWidth = 0;
        for (int i=0; i<rowCount; i++)
        {
            ViewProxy<ChartView> chartBefore = aCol>0 ? getChartForRowCol(i, aCol-1) : null;
            ViewProxy<ChartView> chartAfter = aCol<colCount ? getChartForRowCol(i, aCol) : null;
            double marginA = chartBefore!=null ? chartBefore.getWidth() - getDataView(chartBefore).getMaxX() : 0;
            double marginB = chartAfter!=null ? getDataView(chartAfter).getX() : 0;
            double spacing = chartBefore!=null && chartAfter!=null ? chartAfter.getX() - chartBefore.getMaxX() : 0;
            maxWidth = (int) Math.max(maxWidth, marginA + marginB + spacing);
        }
        return maxWidth;
    }

    /**
     * Returns the height of given margin row index (the space between Chart.DataViews).
     */
    private int getMaxMarginRowHeight(int aRow)
    {
        int rowCount = getRowCount();
        int colCount = getColCount();
        int maxHeight = 0;
        for (int i=0; i<colCount; i++)
        {
            ViewProxy<ChartView> chartBefore = aRow>0 ? getChartForRowCol(aRow - 1, i) : null;
            ViewProxy<ChartView> chartAfter = aRow<rowCount ? getChartForRowCol(aRow, i) : null;
            double marginA = chartBefore!=null ? chartBefore.getHeight() - getDataView(chartBefore).getMaxY() : 0;
            double marginB = chartAfter!=null ? getDataView(chartAfter).getY() : 0;
            double spacing = chartBefore!=null && chartAfter!=null ? chartAfter.getY() - chartBefore.getMaxY() : 0;
            maxHeight = (int) Math.max(maxHeight, marginA + marginB + spacing);
        }
        return maxHeight;
    }

    /**
     * Sets the given DataView width in all page charts and updates so charts have given margins.
     */
    private void setDataViewWidthForAllCharts(int dataW, int[] marginColsW)
    {
        // Get info
        int rowCount = getRowCount();
        int colCount = getColCount();

        // Iterate over page rows to set chart location/size (X/Width)
        for (int i=0; i<rowCount; i++)
        {
            // Declare running variables for ChartX and MarginColMaxX
            int chartX = _areaX;
            int marginColMaxX = marginColsW[0] + chartX;

            // Iterate over page columns in current page row
            for (int j=0; j<colCount; j++)
            {
                // Get ChartView proxy and set new ChartX (just break if we ran out of charts)
                ViewProxy<ChartView> chartProxy = getChartForRowCol(i, j);
                if (chartProxy == null) break;
                chartProxy.setX(chartX);

                // Calculate new DataView.X and set new Chart.PrefDataViewBounds
                int dataX = marginColMaxX - chartX;
                setPrefDataViewBounds(chartProxy, new Rect(dataX, 0, dataW, 0));

                // If last column, set chart width to remaining space and break
                if (j+1 == colCount) {
                    int chartW = _areaX + _areaW - chartX;
                    chartProxy.setWidth(chartW);
                    break;
                }

                // Calc new ChartWidth based on given DataWidth and space on right of DataView
                ViewProxy<DataView> dataProxy = getDataView(chartProxy);
                int insRight = (int) Math.round(chartProxy.getWidth() - dataProxy.getMaxX());
                int chartW = dataX + dataW + insRight;

                // Get next chart. If no more charts in row, just set ChartWidth and break
                ViewProxy<ChartView> nextChart = getChartForRowCol(i, j+1);
                if (nextChart == null) {
                    chartProxy.setWidth(chartW);
                    break;
                }

                // Calc ChartWidth again based on NextChart.DataView.X. If extra space available, split difference
                ViewProxy<DataView> nextData = getDataView(nextChart);
                int nextDataX = (int) Math.round(nextData.getX());
                int nextMarginColMaxX = marginColMaxX + dataW + marginColsW[j+1];
                int chartW2 = nextMarginColMaxX - nextDataX - chartX;
                if (chartW2 > chartW)
                    chartW = (chartW + chartW2)/2;

                // Set new ChartWidth, update MarginColMaxX, ChartX
                chartProxy.setWidth(chartW);
                marginColMaxX = nextMarginColMaxX;
                chartX += chartW;
            }
        }
    }

    /**
     * Sets the given DataView width in all page charts and updates so charts have given margins.
     */
    private void setDataViewHeightForAllCharts(int dataH, int[] marginRowsH)
    {
        // Get info
        int rowCount = getRowCount();
        int colCount = getColCount();

        // Iterate over page cols to set chart location/size (Y/Height)
        for (int i = 0; i < colCount; i++)
        {
            // Declare running variables for ChartY and MarginRowMaxY
            int chartY = _areaY;
            int marginRowMaxY = marginRowsH[0] + chartY;

            // Iterate over page rows in current page column
            for (int j = 0; j < rowCount; j++)
            {
                // Get ChartView proxy and set new ChartY (just break if we ran out of charts)
                ViewProxy<ChartView> chartProxy = getChartForRowCol(j, i);
                if (chartProxy == null) break;
                chartProxy.setY(chartY);

                // Calculate new DataView.Y and set new Chart.PrefDataViewBounds
                int dataY = marginRowMaxY - chartY;
                Rect prefBounds = getPrefDataViewBounds(chartProxy);
                prefBounds.setY(dataY);
                prefBounds.setHeight(dataH);

                // If last row, set chart height to remaining space and break
                if (j+1 == rowCount) {
                    int chartH = _areaY + _areaH - chartY;
                    chartProxy.setHeight(chartH);
                    break;
                }

                // Calc new ChartHeight based on given DataHeight and space below DataView
                ViewProxy<DataView> dataProxy = getDataView(chartProxy);
                int insBottom = (int) Math.round(chartProxy.getHeight() - dataProxy.getMaxY());
                int chartH = dataY + dataH + insBottom;

                // Get next chart. If no more charts in column, just set ChartHeight and break
                ViewProxy<ChartView> nextChart = getChartForRowCol(j+1, i);
                if (nextChart == null) {
                    chartProxy.setHeight(chartH);
                    break;
                }

                // Calc ChartHeight again based on NextChart.DataView.Y. If extra space available, split difference
                ViewProxy<DataView> nextData = getDataView(nextChart);
                int nextDataY = (int) Math.round(nextData.getY());
                int nextMarginRowMaxY = marginRowMaxY + dataH + marginRowsH[j+1];
                int chartH2 = nextMarginRowMaxY - nextDataY - chartY;
                if (chartH2 > chartH)
                    chartH = (chartH + chartH2)/2;

                // Set new ChartHeight, update MarginRowMaxY, ChartY
                chartProxy.setHeight(chartH);
                marginRowMaxY = nextMarginRowMaxY;
                chartY += chartH;
            }
        }
    }

    // Conveniences for page
    private int getRowCount()  { return _page.getRowCount(); }
    private int getColCount()  { return _page.getColCount(); }
    private int getChartCount()  { return _page.getChartCount(); }
    private ViewProxy<ChartView> getChartForRowCol(int aRow, int aCol)
    {
        int index = aRow * getColCount() + aCol;
        return index < getChartCount() ? _chartProxies[index] : null;
    }

    // Conveniences for chart
    private ViewProxy<DataView> getDataView(ViewProxy<ChartView> aChartProxy)
    {
        return aChartProxy.getChildForClass(DataView.class);
    }
    private Rect getPrefDataViewBounds(ViewProxy<ChartView> aChartProxy)
    {
        ChartView chartView = aChartProxy.getView();
        return chartView.getPrefDataViewBounds();
    }
    private void setPrefDataViewBounds(ViewProxy<ChartView> aChartProxy, Rect aRect)
    {
        ChartView chartView = aChartProxy.getView();
        chartView.setPrefDataViewBounds(aRect);
    }
}
