package snapcharts.appmisc;
import java.util.*;
import java.util.function.Consumer;
import snap.geom.HPos;
import snap.geom.Insets;
import snap.view.*;

/**
 * A TableView subclass to emulate a spreadsheet.
 */
public class SheetView extends TableView<Object> {

    // The min row counts
    private int  _minRowCount;

    // The min col counts
    private int  _minColCount;

    // The extra row counts
    private int  _extraRowCount;

    // The extra row/col counts
    private int  _extraColCount;

    // Whether to pad columns
    private boolean  _padCols;

    // Whether to pad rows
    private boolean  _padRows;

    // The default preferred width of columns
    private double  _prefColWidth = DEFAULT_PREF_COL_WIDTH;

    // The Column Configure method
    private Consumer<TableCol>  _colConf;

    // The Rebuild run
    private Runnable  _rebuildRun, _rebuildRunCached = () -> { rebuildImpl(); _rebuildRun = null; };

    // Constants
    private static final double DEFAULT_PREF_COL_WIDTH = 80;
    
    /**
     * Creates a SheetView.
     */
    public SheetView()
    {
        setShowHeader(true);
        setMultiSel(true);
        setEditable(true);
        setCellPadding(new Insets(6));
        setShowHeaderCol(true);
        getHeaderCol().setPrefWidth(35);

        getScrollView().getScroller().addPropChangeListener(pc -> rebuild(), Width_Prop, Height_Prop);
    }

    /**
     * Returns the min row count.
     */
    public int getMinRowCount()  { return _minRowCount; }

    /**
     * Sets the min row count.
     */
    public void setMinRowCount(int aValue)
    {
        // If already set, just return
        if (aValue == getMinRowCount()) return;

        // Set and rebuild
        _minRowCount = aValue;
        rebuild();
    }

    /**
     * Returns the min col count.
     */
    public int getMinColCount()  { return _minColCount; }

    /**
     * Sets the min col count.
     */
    public void setMinColCount(int aValue)
    {
        // If already set, just return
        if (aValue == getMinColCount()) return;

        // Set and rebuild
        _minColCount = aValue;
        rebuild();
    }

    /**
     * Returns the extra row count.
     */
    public int getExtraRowCount()  { return _extraRowCount; }

    /**
     * Sets the extra row count.
     */
    public void setExtraRowCount(int aValue)
    {
        // If already set, just return
        if (aValue == getExtraRowCount()) return;

        // Set and rebuild
        _extraRowCount = aValue;
        rebuild();
    }

    /**
     * Returns the extra col count.
     */
    public int getExtraColCount()  { return _extraColCount; }

    /**
     * Sets the extra col count.
     */
    public void setExtraColCount(int aValue)
    {
        // If already set, just return
        if (aValue == getExtraColCount()) return;

        // Set and rebuild
        _extraColCount = aValue;
        rebuild();
    }

    /**
     * Returns the default preferred width of columns.
     */
    public double getPrefColWidth()  { return _prefColWidth; }

    /**
     * Sets the default preferred width of columns.
     */
    public void setPrefColWidth(double aValue)
    {
        _prefColWidth = aValue;
        rebuild();
    }

    /**
     * Returns method to configure column (and column header).
     */
    public Consumer<TableCol> getColConfigure()  { return _colConf; }

    /**
     * Sets method to configure column (and column header).
     */
    public void setColConfigure(Consumer<TableCol> aCC)
    {
        _colConf = aCC;
        rebuild();
    }

    /**
     * Return column at index, adding if needed.
     */
    protected TableCol getColAddIfAbsent(int anIndex)
    {
        // If column count too low, add columns
        if (anIndex >= getColCount()) {
            double colWidth = getPrefColWidth();
            while (anIndex >= getColCount()) {
                TableCol col = new TableCol();
                col.setPrefWidth(colWidth);
                col.setWidth(colWidth);
                Label header = col.getHeader();
                header.setAlignX(HPos.CENTER);
                addCol(col);
            }
        }

        // Return column
        return getCol(anIndex);
    }

    /**
     * Rebuilds the sheet (deferred).
     */
    protected void rebuild()
    {
        if (_rebuildRun == null) {
            _rebuildRun = _rebuildRunCached;
            ViewUpdater viewUpdater = getUpdater();
            if (viewUpdater != null)
                viewUpdater.runBeforeUpdate(_rebuildRun);
            else getEnv().runLater(_rebuildRun);
        }
    }

    /**
     * Rebuilds the sheet (for real).
     */
    protected void rebuildImpl()
    {
        // Calculate column count
        int colCount = getMinColCount() + getExtraColCount();

        // If too few columns, add more
        while (getColCount() < colCount)
            getColAddIfAbsent(getColCount());

        // Add or remove pad columns
        if (_padCols)
            addOrRemovePadColumns();

        // Configure columns/headers
        configureColumns();

        // Set last column to grow
        for (TableCol tableCol : getCols())
            tableCol.setGrowWidth(false);
        TableCol lastCol = getCol(getColCount() - 1);
        lastCol.setGrowWidth(true);

        // Calculate row count
        int rowCount = getMinRowCount() + getExtraRowCount();

        // If too few rows, add more
        List items = getItems();
        if (items.size() < rowCount) {
            items = new ArrayList(items);
            while (items.size() < rowCount)
                items.add(items.size());
        }

        // Add or remove pad rows
        if (_padRows)
            addOrRemovePadRows(items);

        // Reset items and update all
        setItems(items);
        updateItems();
    }

    /**
     * Add or remove pad columns depending on available space.
     */
    private void addOrRemovePadColumns()
    {
        // Get width of all columns
        double totalW = 0;
        for (TableCol col : getCols())
            totalW += col.getWidth() + TableView.DIVIDER_SPAN;

        // If extra space, add more columns
        double tableW = getScrollView().getScroller().getWidth();
        if (totalW < tableW) {
            while (totalW < tableW) {
                TableCol col = getColAddIfAbsent(getColCount());
                totalW += col.getWidth() + TableView.DIVIDER_SPAN;
            }
        }

        // If too many columns, remove them
        else {
            int colCount = getMinColCount() + getExtraColCount();
            while (getColCount() > colCount && totalW > tableW) {
                TableCol lastCol = getCol(getColCount() - 1);
                if (totalW - lastCol.getWidth() - TableView.DIVIDER_SPAN > tableW)
                    removeCol(getColCount() - 1);
                else break;
            }
        }
    }

    /**
     * Add or remove pad rows depending on available space.
     */
    private void addOrRemovePadRows(List items)
    {
        // If still extra height, add more extra rows
        double rowHeight = getRowHeight();
        double totalH = items.size() * rowHeight;

        // If extra space, add rows
        double tableH = getScrollView().getScroller().getHeight();
        if (totalH < tableH) {
            while (totalH < tableH) {
                items.add(items.size());
                totalH += rowHeight;
            }
        }

        // If too many columns, remove them
        else {
            int rowCount = getMinRowCount() + getExtraRowCount();
            while (items.size() > rowCount && totalH > tableH) {
                if (totalH - rowHeight > tableH)
                    items.remove(items.size() - 1);
                else break;
            }
        }
    }

    /**
     * Called to configure columns/headers.
     */
    protected void configureColumns()
    {
        // Get column configure
        Consumer<TableCol> colConf = getColConfigure();

        // Iterate over columns and configure
        for (int i = 0; i < getColCount(); i++) {
            TableCol col = getCol(i);
            if (colConf != null)
                colConf.accept(col);
            else {
                Label colHeader = col.getHeader();
                colHeader.setText(String.valueOf((char) ('A' + i)));
            }
        }
    }
}