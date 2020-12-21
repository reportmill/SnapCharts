package snapcharts.views;
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
    private double  _areaX, _areaY, _areaW, _areaH;

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
        _areaX = ins.left;
        _areaY = ins.top;
        _areaW = _page.getWidth() - ins.getWidth();
        _areaH = _page.getHeight() - ins.getHeight();

        // If PageScale not 1, apply it
        double pageScale = _page.getPageScale();
        if (pageScale != 1) {
            //_areaX = Math.round(_areaX * pageScale);
            //_areaY = Math.round(_areaY * pageScale);
            _areaW = Math.round(_areaW * pageScale);
            _areaH = Math.round(_areaH * pageScale);
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

        // If PageScale not 1, adjust Chart scales, bounds
        if (pageScale != 1) {
            double scaleDown = 1 / pageScale;
            for (ViewProxy<ChartView> chartProxy : _chartProxies) {
                ChartView chartView = chartProxy.getView();
                chartView.setScale(scaleDown);
                double chartX = _areaX + (chartView.getX() - _areaX) * scaleDown - (chartView.getWidth() - chartView.getWidth() * scaleDown) / 2;
                double chartY = _areaY + (chartView.getY() - _areaY) * scaleDown - (chartView.getHeight() - chartView.getHeight() * scaleDown) / 2;
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
                ViewProxy<ChartView> chart = getChartForRowCol(i, j); if (chart==null) break;
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
        setDataViewWidthAndMarginsForAllCharts(dataW, marginColsW);

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

        // Calculate unified data area height
        int realRowCount = getChartCount() / getColCount() + (getChartCount() % getColCount() != 0 ? 1 : 0);
        if (realRowCount!=getRowCount())
            _areaH = (int) Math.round(_areaH * realRowCount / rowCount);
        double totalDataH = _areaH - totalMarginRowsH;
        int dataH = (int) (totalDataH / rowCount);

        // Set all charts to have new data area height and margin column height
        setDataViewHeightAndMarginsForAllCharts(dataH, marginRowsH);
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
    private void setDataViewWidthAndMarginsForAllCharts(int dataW, int[] marginColsW)
    {
        // Get info
        int rowCount = getRowCount();
        int colCount = getColCount();

        // Iterate over charts and reset DataView.X/Width and Chart.Width
        for (int i=0; i<rowCount; i++)
        {
            for (int j=0; j<colCount; j++)
            {
                // Get ChartProxy, Chart.DataView
                ViewProxy<ChartView> chartProxy = getChartForRowCol(i, j); if (chartProxy==null) break;
                ViewProxy<DataView> dataProxy = getDataView(chartProxy); if (dataProxy.isEmpty()) continue;

                // Get PrevChart right margin
                ViewProxy<ChartView> prevChart = j>0 ? getChartForRowCol(i, j-1) : null;
                double prevChartInsRight = prevChart!=null ? (prevChart.getWidth() - getDataView(prevChart).getMaxX()) : 0;

                // Calculate new DataView.X (gets shifted right if MarginCols[i] > margin between prevChart and chart)
                double dataX = dataProxy.getX();
                double insLeft = dataProxy.getX();
                double insLeft2 = marginColsW[j] - prevChartInsRight;
                double dataDiffX = insLeft2 - insLeft;
                double dataDiffW = dataW - dataProxy.getWidth();

                // Set new Chart.PrefDataViewBounds
                double dataX2 = dataX + dataDiffX;
                setPrefDataViewBounds(chartProxy, new Rect(dataX2, 0, dataW, 0));
                dataProxy.setX(dataX2);
                dataProxy.setWidth(dataW);

                // Calculate Chart width change. If shrinking, adjust for next Chart.
                double chartDiffW = dataDiffX + dataDiffW;
                if (chartDiffW<0)
                {
                    // If last chart, reset diff so chart.MaxX is at MarginCol.MaxX
                    if (j+1==colCount)
                    {
                        double chartMaxX = dataProxy.getMaxX() + marginColsW[j+1];
                        chartDiffW = chartMaxX - chartProxy.getWidth();
                    }

                    // Calc min diff to accommodate rightMargin and nextChartLeftMargin and split diff
                    else
                    {
                        ViewProxy<ChartView> nextChart = getChartForRowCol(i, j + 1); if (nextChart==null) break;
                        double nextChartLeftMargin = getDataView(nextChart).getX();
                        double chartMaxX = dataProxy.getMaxX() + marginColsW[j + 1] - nextChartLeftMargin;
                        double minDiffW = chartMaxX - chartProxy.getWidth();
                        chartDiffW = (chartDiffW + minDiffW) / 2;
                    }
                }

                // Set new Chart.Width to accommodate shifted/resized DataView
                chartProxy.setWidth(chartProxy.getWidth() + chartDiffW);

                // Shift successive chart X to account for resizing loop chart
                for (int k=j+1; k<colCount; k++)
                {
                    ViewProxy<ChartView> nextChart = getChartForRowCol(i, k); if (nextChart==null) break;
                    nextChart.setX(nextChart.getX() + chartDiffW);
                }
            }
        }
    }

    /**
     * Sets the given DataView width in all page charts and updates so charts have given margins.
     */
    private void setDataViewHeightAndMarginsForAllCharts(int dataH, int[] marginRowsH)
    {
        // Get info
        int rowCount = getRowCount();
        int colCount = getColCount();

        // Iterate over charts and reset DataView.Y/Height and Chart.Height
        for (int i = 0; i < colCount; i++)
        {
            for (int j = 0; j < rowCount; j++)
            {
                // Get Chart, Chart.DataView (last row might have empty slots, so just break if no chart)
                ViewProxy<ChartView> chartProxy = getChartForRowCol(j, i);
                if (chartProxy == null)
                    break;
                ViewProxy<DataView> dataProxy = getDataView(chartProxy);
                if (dataProxy.isEmpty())
                    continue;

                // Get PrevChart bottom margin
                ViewProxy<ChartView> prevChart = j > 0 ? getChartForRowCol(j - 1, i) : null;
                double prevChartInsBottom = prevChart != null ? (prevChart.getHeight() - getDataView(prevChart).getMaxY()) : 0;

                // Calculate new DataView.Y (gets shifted down if MarginRows[i] > margin between prevChart and chart)
                double dataY = dataProxy.getY();
                double insTop = dataProxy.getY();
                double insTop2 = marginRowsH[j] - prevChartInsBottom;
                double dataDiffY = insTop2 - insTop;
                double dataDiffH = dataH - dataProxy.getHeight();

                // Set new Chart.PrefDataViewBounds
                double dataY2 = dataY + dataDiffY;
                Rect prefBounds = getPrefDataViewBounds(chartProxy);
                prefBounds.setY(dataY2);
                prefBounds.setHeight(dataH);
                dataProxy.setY(dataY2);
                dataProxy.setHeight(dataH);

                // Calculate Chart height change. If shrinking, adjust for next chart
                double chartDiffH = dataDiffY + dataDiffH;
                if (chartDiffH<0)
                {
                    // If last chart, reset diff to so chart.MaxY is at MarginRow.MaxY
                    if (j+1==rowCount)
                    {
                        double chartMaxY = dataProxy.getMaxY() + marginRowsH[j+1];
                        chartDiffH = chartMaxY - chartProxy.getHeight();
                    }

                    // Calc min diff to accommodate bottomMargin and nextChartTopMargin and split diff
                    else
                    {
                        ViewProxy<ChartView> nextChart = getChartForRowCol(j + 1, i); if (nextChart==null) break;
                        double nextChartTopMargin = getDataView(nextChart).getY();
                        double chartMaxY = dataProxy.getMaxY() + marginRowsH[j + 1] - nextChartTopMargin;
                        double minDiffH = chartMaxY - chartProxy.getHeight();
                        chartDiffH = (chartDiffH + minDiffH) / 2;
                    }
                }

                // Set new Chart.Height to accommodate shifted/resized DataView
                chartProxy.setHeight(chartProxy.getHeight() + chartDiffH);

                // Shift successive chart Y to account for resizing loop chart
                for (int k = j + 1; k < rowCount; k++)
                {
                    ViewProxy<ChartView> nextChart = getChartForRowCol(k, i); if (nextChart == null) break;
                    nextChart.setY(nextChart.getY() + chartDiffH);
                }
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
        return chartView._layout._prefDataViewBounds;
    }
    private void setPrefDataViewBounds(ViewProxy<ChartView> aChartProxy, Rect aRect)
    {
        ChartView chartView = aChartProxy.getView();
        chartView._layout._prefDataViewBounds = aRect;
        chartView.relayout();
    }
}
