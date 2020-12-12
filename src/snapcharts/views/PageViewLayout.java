package snapcharts.views;
import snap.geom.Insets;
import snap.geom.Rect;
import snap.view.ViewProxy;
import java.util.Arrays;

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

        layoutPlotsInGrid();
    }

    /**
     * Does simple grid layout.
     */
    private void layoutPlotsInGrid()
    {
        Insets ins = _page.getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = _page.getWidth() - ins.getWidth();
        double areaH = _page.getHeight() - ins.getHeight();

        double spaceW = 20;
        double spaceH = 20;

        int rowCount = getRowCount();
        int colCount = getColCount();
        double chartW = Math.round((areaW - spaceW * (colCount-1)) / colCount);
        double chartH = Math.round((areaH - spaceH * (rowCount-1)) / rowCount);

        for (int i=0; i<rowCount; i++) {
            for (int j=0; j<colCount; j++) {
                ViewProxy<ChartView> chart = getPlotForRowCol(i, j); if (chart==null) break;
                double chartX = areaX + (chartW + spaceW) * j;
                double chartY = areaY + (chartH + spaceH) * i;
                chart.setBounds(chartX, chartY, chartW, chartH);
            }
        }

        _pageProxy.setBoundsInClient();
    }

    /**
     * Calculate the optimal data area size and plot margin sizes and set in all page plots.
     */
    public void synchPlotsOnPage()
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
        double pageAreaW = _pageProxy.getWidth();
        double totalDataAreaW = pageAreaW - totalMarginColsW;
        int dataAreaW = (int) (totalDataAreaW / colCount);

        // Set all plots to have new data area width and margin column widths
        setDataAreaWidthAndMarginsForAllPlots(dataAreaW, marginColsW);

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
        double pageAreaH = _page.getBounds().getHeight();
        double totalDataAreaH = pageAreaH - totalMarginRowsH;
        int dataAreaH = (int) (totalDataAreaH / rowCount);

        // Convert dataAreaH to array of values for each row, to handle weird SharedAxis 'Vertical Plot Spacing'
        int[] dataAreaHs = new int[rowCount];
        Arrays.fill(dataAreaHs, dataAreaH);
        float[] ratios = null; //_page.getUserDefinedSpacingRatios();
        if (ratios!=null)
        {
            for (int i=0; i<rowCount; i++)
                dataAreaHs[i] = Math.round(dataAreaH * rowCount * ratios[i]);
        }

        // Set all plots to have new data area height and margin column height
        setDataAreaHeightAndMarginsForAllPlots(dataAreaHs, marginRowsH);
    }

    /**
     * Returns the width of given margin column index (the space between Plot.DataAreas).
     */
    private int getMaxMarginColWidth(int aCol)
    {
        int rowCount = getRowCount();
        int colCount = getColCount();
        int maxWidth = 0;
        for (int i=0; i<rowCount; i++)
        {
            ViewProxy<ChartView> plotBefore = aCol>0 ? getPlotForRowCol(i, aCol-1) : null;
            ViewProxy<ChartView> plotAfter = aCol<colCount ? getPlotForRowCol(i, aCol) : null;
            double marginA = plotBefore!=null ? plotBefore.getBounds().getMaxX() - getDataAreaBounds(plotBefore).getMaxX() : 0;
            double marginB = plotAfter!=null ? getDataAreaBounds(plotAfter).getX() - plotAfter.getBounds().getX() : 0;
            maxWidth = (int) Math.max(maxWidth, marginA + marginB);
        }
        return maxWidth;
    }

    /**
     * Returns the height of given margin row index (the space between Plot.DataAreas).
     */
    private int getMaxMarginRowHeight(int aRow)
    {
        int rowCount = getRowCount();
        int colCount = getColCount();
        int maxHeight = 0;
        for (int i=0; i<colCount; i++)
        {
            ViewProxy<ChartView> plotBefore = aRow>0 ? getPlotForRowCol(aRow - 1, i) : null;
            ViewProxy<ChartView> plotAfter = aRow<rowCount ? getPlotForRowCol(aRow, i) : null;
            double marginA = plotBefore!=null ? plotBefore.getBounds().getMaxY() - getDataAreaBounds(plotBefore).getMaxY() : 0;
            double marginB = plotAfter!=null ? getDataAreaBounds(plotAfter).getY() - plotAfter.getBounds().getY() : 0;
            maxHeight = (int) Math.max(maxHeight, marginA + marginB);
        }
        return maxHeight;
    }

    /**
     * Sets the given dataArea width in all page plots and updates so plots have given margins.
     */
    private void setDataAreaWidthAndMarginsForAllPlots(int dataAreaW, int[] marginColsW)
    {
        // Get info
        int rowCount = getRowCount();
        int colCount = getColCount();

        // Iterate over plots and reset DataArea.X/Width and Plot.Width
        for (int i=0; i<rowCount; i++)
        {
            for (int j=0; j<colCount; j++)
            {
                // Get Plot, PlotBounds, Plot.DataAreaBounds
                ViewProxy<ChartView> plot = getPlotForRowCol(i, j); if (plot==null) break;
                Rect plotBounds = plot.getBounds();
                Rect dataAreaBounds = getDataAreaBounds(plot); if (dataAreaBounds.isEmpty()) continue;

                // Get PrevPlot right margin
                ViewProxy<ChartView> prevPlot = j>0 ? getPlotForRowCol(i, j-1) : null;
                double prevPlotInsRight = prevPlot!=null ? (prevPlot.getBounds().getMaxX() - getDataAreaBounds(prevPlot).getMaxX()) : 0;

                // Calculate new DataArea.X (gets shifted right if MarginCols[i] > margin between prevPlot and plot)
                double dataAreaX = dataAreaBounds.getX();
                double insLeft = dataAreaBounds.getX() - plotBounds.getX();
                double insLeft2 = marginColsW[j] - prevPlotInsRight;
                double dataAreaDiffX = insLeft2 - insLeft;
                double dataAreaDiffW = dataAreaW - dataAreaBounds.getWidth();

                // Set new Plot.PrefDataAreaBounds (update DataArea too, for later use as 'prevPlot')
                double dataAreaX2 = dataAreaX + dataAreaDiffX;
                setPrefDataAreaBounds(plot, new Rect(dataAreaX2, 0, dataAreaW, 0));
                dataAreaBounds.setX(dataAreaX2);
                dataAreaBounds.setWidth(dataAreaW);

                // Calculate Plot width change. If shrinking, adjust for next plot.
                double plotDiffW = dataAreaDiffX + dataAreaDiffW;
                if (plotDiffW<0)
                {
                    // If last plot, reset diff so plot.MaxX is at MarginCol.MaxX
                    if (j+1==colCount)
                    {
                        double plotMaxX = dataAreaBounds.getMaxX() + marginColsW[j+1];
                        plotDiffW = plotMaxX - plotBounds.getMaxX();
                    }

                    // Calc min diff to accommodate rightMargin and nextPlotLeftMargin and split diff
                    else
                    {
                        ViewProxy<ChartView> nextPlot = getPlotForRowCol(i, j + 1); if (nextPlot==null) break;
                        double nextPlotLeftMargin = getDataAreaBounds(nextPlot).getX() - nextPlot.getBounds().getX();
                        double plotMaxX = dataAreaBounds.getMaxX() + marginColsW[j + 1] - nextPlotLeftMargin;
                        double minDiffW = plotMaxX - plotBounds.getMaxX();
                        plotDiffW = (plotDiffW + minDiffW) / 2;
                    }
                }

                // Set new Plot.Width to accommodate shifted/resized DataArea
                plotBounds.setWidth(plotBounds.getWidth() + plotDiffW);

                // Shift successive plot Bounds & DataArea.Bounds to account for resizing loop plot
                for (int k=j+1; k<colCount; k++)
                {
                    ViewProxy<ChartView> nextPlot = getPlotForRowCol(i, k); if (nextPlot==null) break;
                    getDataAreaBounds(nextPlot).setX(getDataAreaBounds(nextPlot).getX() + plotDiffW);
                    nextPlot.getBounds().setX(nextPlot.getBounds().getX() + plotDiffW);
                }
            }
        }
    }

    /**
     * Sets the given dataArea width in all page plots and updates so plots have given margins.
     */
    private void setDataAreaHeightAndMarginsForAllPlots(int[] dataAreaHs, int[] marginRowsH)
    {
        // Get info
        int rowCount = getRowCount();
        int colCount = getColCount();

        // Iterate over plots and reset DataArea.Y/Height and Plot.Height
        for (int i = 0; i < colCount; i++)
        {
            for (int j = 0; j < rowCount; j++)
            {
                // Get Plot, PlotBounds, Plot.DataAreaBounds
                ViewProxy<ChartView> plot = getPlotForRowCol(j, i);
                if (plot == null)
                    break;
                Rect plotBounds = plot.getBounds();
                Rect dataAreaBounds = getDataAreaBounds(plot);
                if (dataAreaBounds.isEmpty())
                    continue;

                // Get PrevPlot bottom margin
                ViewProxy<ChartView> prevPlot = j > 0 ? getPlotForRowCol(j - 1, i) : null;
                double prevPlotInsBottom = prevPlot != null ? (prevPlot.getBounds().getMaxY() - getDataAreaBounds(prevPlot).getMaxY()) : 0;

                // Calculate new DataArea.Y (gets shifted down if MarginRows[i] > margin between prevPlot and plot)
                double dataAreaY = dataAreaBounds.getY();
                int dataAreaH = dataAreaHs[j];
                double insTop = dataAreaBounds.getY() - plotBounds.getY();
                double insTop2 = marginRowsH[j] - prevPlotInsBottom;
                double dataAreaDiffY = insTop2 - insTop;
                double dataAreaDiffH = dataAreaH - dataAreaBounds.getHeight();

                // Set new Plot.PrefDataAreaBounds (update DataArea too, for later use as 'prevPlot')
                double dataAreaY2 = dataAreaY + dataAreaDiffY;
                Rect prefBounds = getPrefDataAreaBounds(plot);
                prefBounds.setY(dataAreaY2);
                prefBounds.setHeight(dataAreaH);
                dataAreaBounds.setY(dataAreaY2);
                dataAreaBounds.setHeight(dataAreaH);

                // Calculate Plot height change. If shrinking, adjust for next plot
                double plotDiffH = dataAreaDiffY + dataAreaDiffH;
                if (plotDiffH<0)
                {
                    // If last plot, reset diff to so plot.MaxY is at MarginRow.MaxY
                    if (j+1==rowCount)
                    {
                        double plotMaxY = dataAreaBounds.getMaxY() + marginRowsH[j+1];
                        plotDiffH = plotMaxY - plotBounds.getMaxY();
                    }

                    // Calc min diff to accommodate bottomMargin and nextPlotTopMargin and split diff
                    else
                    {
                        ViewProxy<ChartView> nextPlot = getPlotForRowCol(j + 1, i); if (nextPlot==null) break;
                        double nextPlotTopMargin = getDataAreaBounds(nextPlot).getY() - nextPlot.getBounds().getY();
                        double plotMaxY = dataAreaBounds.getMaxY() + marginRowsH[j + 1] - nextPlotTopMargin;
                        double minDiffH = plotMaxY - plotBounds.getMaxY();
                        plotDiffH = (plotDiffH + minDiffH) / 2;
                    }
                }

                // Set new Plot.Height to accommodate shifted/resized DataArea
                plotBounds.setHeight(plotBounds.getHeight() + plotDiffH);

                // Shift successive plot Bounds & DataArea.Bounds to account for resizing loop plot
                for (int k = j + 1; k < rowCount; k++)
                {
                    ViewProxy<ChartView> nextPlot = getPlotForRowCol(k, i); if (nextPlot == null) break;
                    getDataAreaBounds(nextPlot).setY(getDataAreaBounds(nextPlot).getY() + plotDiffH);
                    nextPlot.getBounds().setY(nextPlot.getBounds().getY() + plotDiffH);
                }
            }
        }
    }

    // Conveniences for page
    private int getRowCount()  { return _page.getRowCount(); }
    private int getColCount()  { return _page.getColCount(); }
    private int getChartCount()  { return _page.getChartCount(); }
    private ViewProxy<ChartView> getPlotForRowCol(int aRow, int aCol)
    {
        int index = aRow * getColCount() + aCol;
        return index < getChartCount() ? _chartProxies[index] : null;
    }

    // Conveniences for chart
    private Rect getDataAreaBounds(ViewProxy<ChartView> aChartProxy)
    {
        ChartView chartView = aChartProxy.getView();
        return null;//chartView.getDataAreaBounds();
    }
    private Rect getPrefDataAreaBounds(ViewProxy<ChartView> aChartProxy)
    {
        return null;
    }
    private void setPrefDataAreaBounds(ViewProxy<ChartView> aChartProxy, Rect aRect)
    {

    }
}
