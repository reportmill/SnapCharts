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
    }

    /**
     * Does simple grid layout.
     */
    private void layoutChartsInGrid()
    {
        Insets ins = _page.getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = _page.getWidth() - ins.getWidth();
        double areaH = _page.getHeight() - ins.getHeight();

        double spaceW = 0; //20
        double spaceH = 0; //20

        int rowCount = getRowCount();
        int colCount = getColCount();
        double chartW = Math.round((areaW - spaceW * (colCount-1)) / colCount);
        double chartH = Math.round((areaH - spaceH * (rowCount-1)) / rowCount);

        for (int i=0; i<rowCount; i++) {
            for (int j=0; j<colCount; j++) {
                ViewProxy<ChartView> chart = getChartForRowCol(i, j); if (chart==null) break;
                double chartX = areaX + (chartW + spaceW) * j;
                double chartY = areaY + (chartH + spaceH) * i;
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
        double pageAreaW = _pageProxy.getWidth() - _pageProxy.getInsetsAll().getWidth();
        double totalDataAreaW = pageAreaW - totalMarginColsW;
        int dataAreaW = (int) (totalDataAreaW / colCount);

        // Set all charts to have new data area width and margin column widths
        setDataAreaWidthAndMarginsForAllCharts(dataAreaW, marginColsW);

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
        double pageAreaH = _pageProxy.getHeight() - _pageProxy.getInsetsAll().getHeight();
        int realRowCount = getChartCount() / getColCount() + (getChartCount() % getColCount() != 0 ? 1 : 0);
        if (realRowCount!=getRowCount())
            pageAreaH = (int) Math.round(pageAreaH * realRowCount / rowCount);
        double totalDataAreaH = pageAreaH - totalMarginRowsH;
        int dataAreaH = (int) (totalDataAreaH / rowCount);

        // Set all charts to have new data area height and margin column height
        setDataAreaHeightAndMarginsForAllCharts(dataAreaH, marginRowsH);
    }

    /**
     * Returns the width of given margin column index (the space between Chart.DataAreas).
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
            double marginA = chartBefore!=null ? chartBefore.getWidth() - getDataAreaBounds(chartBefore).getMaxX() : 0;
            double marginB = chartAfter!=null ? getDataAreaBounds(chartAfter).getX() : 0;
            double spacing = chartBefore!=null && chartAfter!=null ? chartAfter.getX() - chartBefore.getMaxX() : 0;
            maxWidth = (int) Math.max(maxWidth, marginA + marginB + spacing);
        }
        return maxWidth;
    }

    /**
     * Returns the height of given margin row index (the space between Chart.DataAreas).
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
            double marginA = chartBefore!=null ? chartBefore.getHeight() - getDataAreaBounds(chartBefore).getMaxY() : 0;
            double marginB = chartAfter!=null ? getDataAreaBounds(chartAfter).getY() : 0;
            double spacing = chartBefore!=null && chartAfter!=null ? chartAfter.getY() - chartBefore.getMaxY() : 0;
            maxHeight = (int) Math.max(maxHeight, marginA + marginB + spacing);
        }
        return maxHeight;
    }

    /**
     * Sets the given dataArea width in all page charts and updates so charts have given margins.
     */
    private void setDataAreaWidthAndMarginsForAllCharts(int dataAreaW, int[] marginColsW)
    {
        // Get info
        int rowCount = getRowCount();
        int colCount = getColCount();

        // Iterate over charts and reset DataArea.X/Width and Chart.Width
        for (int i=0; i<rowCount; i++)
        {
            for (int j=0; j<colCount; j++)
            {
                // Get ChartProxy, Chart.DataAreaBounds
                ViewProxy<ChartView> chartProxy = getChartForRowCol(i, j); if (chartProxy==null) break;
                Rect dataAreaBounds = getDataAreaBounds(chartProxy); if (dataAreaBounds.isEmpty()) continue;

                // Get PrevChart right margin
                ViewProxy<ChartView> prevChart = j>0 ? getChartForRowCol(i, j-1) : null;
                double prevChartInsRight = prevChart!=null ? (prevChart.getWidth() - getDataAreaBounds(prevChart).getMaxX()) : 0;

                // Calculate new DataArea.X (gets shifted right if MarginCols[i] > margin between prevChart and chart)
                double dataAreaX = dataAreaBounds.getX();
                double insLeft = dataAreaBounds.getX();
                double insLeft2 = marginColsW[j] - prevChartInsRight;
                double dataAreaDiffX = insLeft2 - insLeft;
                double dataAreaDiffW = dataAreaW - dataAreaBounds.getWidth();

                // Set new Chart.PrefDataAreaBounds (update DataArea too, for later use as 'prevChart')
                double dataAreaX2 = dataAreaX + dataAreaDiffX;
                setPrefDataAreaBounds(chartProxy, new Rect(dataAreaX2, 0, dataAreaW, 0));
                dataAreaBounds.setX(dataAreaX2);
                dataAreaBounds.setWidth(dataAreaW);

                // Calculate Chart width change. If shrinking, adjust for next Chart.
                double chartDiffW = dataAreaDiffX + dataAreaDiffW;
                if (chartDiffW<0)
                {
                    // If last chart, reset diff so chart.MaxX is at MarginCol.MaxX
                    if (j+1==colCount)
                    {
                        double chartMaxX = dataAreaBounds.getMaxX() + marginColsW[j+1];
                        chartDiffW = chartMaxX - chartProxy.getWidth();
                    }

                    // Calc min diff to accommodate rightMargin and nextChartLeftMargin and split diff
                    else
                    {
                        ViewProxy<ChartView> nextChart = getChartForRowCol(i, j + 1); if (nextChart==null) break;
                        double nextChartLeftMargin = getDataAreaBounds(nextChart).getX();
                        double chartMaxX = dataAreaBounds.getMaxX() + marginColsW[j + 1] - nextChartLeftMargin;
                        double minDiffW = chartMaxX - chartProxy.getWidth();
                        chartDiffW = (chartDiffW + minDiffW) / 2;
                    }
                }

                // Set new Chart.Width to accommodate shifted/resized DataArea
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
     * Sets the given dataArea width in all page charts and updates so charts have given margins.
     */
    private void setDataAreaHeightAndMarginsForAllCharts(int dataAreaH, int[] marginRowsH)
    {
        // Get info
        int rowCount = getRowCount();
        int colCount = getColCount();

        // Iterate over charts and reset DataArea.Y/Height and Chart.Height
        for (int i = 0; i < colCount; i++)
        {
            for (int j = 0; j < rowCount; j++)
            {
                // Get Chart, Chart.DataAreaBounds
                ViewProxy<ChartView> chartProxy = getChartForRowCol(j, i);
                if (chartProxy == null)
                    break;
                Rect dataAreaBounds = getDataAreaBounds(chartProxy);
                if (dataAreaBounds.isEmpty())
                    continue;

                // Get PrevChart bottom margin
                ViewProxy<ChartView> prevChart = j > 0 ? getChartForRowCol(j - 1, i) : null;
                double prevChartInsBottom = prevChart != null ? (prevChart.getHeight() - getDataAreaBounds(prevChart).getMaxY()) : 0;

                // Calculate new DataArea.Y (gets shifted down if MarginRows[i] > margin between prevChart and chart)
                double dataAreaY = dataAreaBounds.getY();
                double insTop = dataAreaBounds.getY();
                double insTop2 = marginRowsH[j] - prevChartInsBottom;
                double dataAreaDiffY = insTop2 - insTop;
                double dataAreaDiffH = dataAreaH - dataAreaBounds.getHeight();

                // Set new Chart.PrefDataAreaBounds (update DataArea too, for later use as 'prevChart')
                double dataAreaY2 = dataAreaY + dataAreaDiffY;
                Rect prefBounds = getPrefDataAreaBounds(chartProxy);
                prefBounds.setY(dataAreaY2);
                prefBounds.setHeight(dataAreaH);
                dataAreaBounds.setY(dataAreaY2);
                dataAreaBounds.setHeight(dataAreaH);

                // Calculate Chart height change. If shrinking, adjust for next chart
                double chartDiffH = dataAreaDiffY + dataAreaDiffH;
                if (chartDiffH<0)
                {
                    // If last chart, reset diff to so chart.MaxY is at MarginRow.MaxY
                    if (j+1==rowCount)
                    {
                        double chartMaxY = dataAreaBounds.getMaxY() + marginRowsH[j+1];
                        chartDiffH = chartMaxY - chartProxy.getHeight();
                    }

                    // Calc min diff to accommodate bottomMargin and nextChartTopMargin and split diff
                    else
                    {
                        ViewProxy<ChartView> nextChart = getChartForRowCol(j + 1, i); if (nextChart==null) break;
                        double nextChartTopMargin = getDataAreaBounds(nextChart).getY();
                        double chartMaxY = dataAreaBounds.getMaxY() + marginRowsH[j + 1] - nextChartTopMargin;
                        double minDiffH = chartMaxY - chartProxy.getHeight();
                        chartDiffH = (chartDiffH + minDiffH) / 2;
                    }
                }

                // Set new Chart.Height to accommodate shifted/resized DataArea
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
    private Rect getDataAreaBounds(ViewProxy<ChartView> aChartProxy)
    {
        ViewProxy<DataView> dataViewProxy = aChartProxy.getChildForClass(DataView.class);
        return dataViewProxy;
    }
    private Rect getPrefDataAreaBounds(ViewProxy<ChartView> aChartProxy)
    {
        ChartView chartView = aChartProxy.getView();
        return chartView._layout._prefDataAreaBounds;
    }
    private void setPrefDataAreaBounds(ViewProxy<ChartView> aChartProxy, Rect aRect)
    {
        ChartView chartView = aChartProxy.getView();
        chartView._layout._prefDataAreaBounds = aRect;
        chartView.relayout();
    }
}
