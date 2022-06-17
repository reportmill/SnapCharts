package snapcharts.app;
import snap.geom.HPos;
import snap.util.FormatUtils;
import snap.util.ListSel;
import snap.props.PropChange;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.appmisc.SheetView;
import snapcharts.data.*;

/**
 * This ViewOwner subclass provides display and editing of a DataSet.
 */
public class DataSetPane extends ViewOwner {

    // The DataSet
    protected DataSet  _dataSet;

    // The SheetView
    protected SheetView  _sheetView;

    // Constants for actions
    private final String Cut_Action = "CutAction";
    private final String Paste_Action = "PasteAction";
    private final String SelectAll_Action = "SelectAllAction";
    //private final String Delete_Action = "DeleteAction";

    /**
     * Constructor.
     */
    public DataSetPane(DataSet aDataSet)
    {
        setDataSet(aDataSet);
    }

    /**
     * Returns the DataSet.
     */
    public DataSet getDataSet()  { return _dataSet; }

    /**
     * Sets the DataSet.
     */
    protected void setDataSet(DataSet aDataSet)
    {
        _dataSet = aDataSet;
    }

    /**
     * Called to paste from clipboard.
     */
    public void paste()
    {
        // Get clipboard (if not loaded (browser), come back when it is)
        Clipboard cb = Clipboard.get();
        if (!cb.isLoaded()) {
            cb.addLoadListener(() -> paste());
            return;
        }

        // Get string (just return if not there)
        String str = cb.hasString() ? cb.getString() : null; if (str == null) return;

        // Get cells
        String[][] cells = DataUtils.getCellData(str);
        if (cells != null) {
            ListSel sel = _sheetView.getSel();
            replaceDataForSelection(sel, cells);
        }

        // Reset
        resetLater();
    }

    /**
     * Called to delete selection.
     */
    public void delete()
    {
        // Just return if editing
        if (_sheetView.getEditingCell() != null) return;

        // Get selection (just return if empty)
        ListSel sel = _sheetView.getSel(); if (sel.isEmpty()) return;

        // Delete data for selection
        deleteDataForSelection(sel);

        // Reset selection
        _sheetView.setSelIndex(sel.getMin()-1);
    }

    /**
     * Create UI.
     */
    protected void initUI()
    {
        // Get/configure SheetView
        _sheetView = getView("SheetView", SheetView.class);
        _sheetView.setPrefColWidth(100);
        _sheetView.setExtraColCount(1);
        _sheetView.setColConfigure(c -> configureColumn(c));
        _sheetView.setCellConfigure(c -> configureCell(c));
        _sheetView.addPropChangeListener(pc -> editingCellChanged(pc), TableView.EditingCell_Prop);

        // Add PasteAction
        addKeyActionHandler(Cut_Action, "Shortcut+X");
        addKeyActionHandler(Paste_Action, "Shortcut+V");
        addKeyActionHandler(SelectAll_Action, "Shortcut+A");
        //addKeyActionFilter(Delete_Action, "DELETE") / (Delete_Action, "BACKSPACE");
    }

    /**
     * Resets the UI.
     */
    protected void resetUI()
    {
        // Get DataSet info
        DataSet dataSet = getDataSet();
        String dataName = dataSet.getName();
        DataType dataType = dataSet.getDataType();

        // Update DataNameText
        setViewValue("DataNameText", dataName);

        // Set TableView row & col count
        int pointCount = dataSet.getPointCount();
        int rowCount = pointCount;
        int colCount = dataType.getChannelCount();
        _sheetView.setMinRowCount(rowCount);
        _sheetView.setMinColCount(colCount);

        // Update PointCountLabel
        String str = "Points: " + pointCount + "   |   ";
        str += "Min X: " + FormatUtils.formatNum(dataSet.getMinX()) + "   |   ";
        str += "Max X: " + FormatUtils.formatNum(dataSet.getMaxX()) + "   |   ";
        str += "Min Y: " + FormatUtils.formatNum(dataSet.getMinY()) + "   |   ";
        str += "Max Y: " + FormatUtils.formatNum(dataSet.getMaxY());
        setViewValue("PointCountLabel", str);
    }

    /**
     * Resets the UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle Cut_Action
        if (anEvent.equals(Cut_Action)) {
            delete();
            anEvent.consume();
        }

        // Handle Paste_Action
        if (anEvent.equals(Paste_Action))
            paste();

        // Handle SelectAll_Action
        if (anEvent.equals(SelectAll_Action)) {
            _sheetView.setSel(new ListSel(0, _sheetView.getRowCount()));
        }
    }

    /**
     * Configures a table column headers.
     */
    private void configureColumn(TableCol aCol)
    {
        // Get DataSet info
        DataSet dataSet = getDataSet();
        DataType dataType = dataSet.getDataType();

        // Get column and DataChan
        int col = aCol.getColIndex();
        DataChan dchan = col < dataType.getChannelCount() ? dataType.getChannel(col) : null;

        // Get HeaderText string
        String headerText = "";
        if (dchan==DataChan.I)
            headerText = "Index";
        else if (dchan!=null)
            headerText = dchan.toString();

        // Set Column.Header.Text and PrefWidth
        Label header = aCol.getHeader();
        header.setText(headerText);
    }

    /**
     * Configures a table cell.
     */
    private void configureCell(ListCell aCell)
    {
        // Get DataSet info
        DataSet dataSet = getDataSet();
        DataType dataType = dataSet.getDataType();
        int chanCount = dataType.getChannelCount();
        int pointCount = dataSet.getPointCount();

        // Set Cell.MinSize
        int minCellHeight = (int) Math.ceil(aCell.getFont().getLineHeight());
        aCell.getStringView().setMinSize(40, minCellHeight);

        // Get dataSet count, point count, row and column
        int row = aCell.getRow();
        int col = aCell.getCol();
        if (row >= pointCount || col >= chanCount) {
            aCell.setText("");
            return;
        }

        // Get/set Cell value/text
        Object val = getValueForChannelIndex(col, row);
        String valStr = SnapUtils.stringValue(val);
        aCell.setText(valStr);
        aCell.setAlignX(HPos.RIGHT);
    }

    /**
     * Called when cell editing changes.
     */
    private void editingCellChanged(PropChange aPC)
    {
        // If cell that stopped editing (just return if null)
        ListCell cell = (ListCell) aPC.getOldValue(); if (cell == null) return;

        // Get row/col and make sure there are dataSet/points to cover it
        String text = cell.getText();
        int row = cell.getRow();
        int col = cell.getCol();
        expandDataSetSize(row);

        // Set value
        setValueForChannelIndex(text, col, row);

        // Update row and trim DataSet in case dataSet/points were cleared
        _sheetView.updateItems(cell.getItem());
        trimDataSet();
        resetLater();
    }

    /**
     * Make sure DataSet is at least given size.
     */
    private void expandDataSetSize(int aSize)
    {
        DataSet dataSet = getDataSet();
        if (aSize >= dataSet.getPointCount())
            dataSet.setPointCount(aSize + 1);
    }

    /**
     * Removes empty DataSet and slices.
     */
    private void trimDataSet()
    {
        // While last slice is empty, remove it
        DataSet dataSet = getDataSet();
        int pointCount = dataSet.getPointCount();

        // FIX THIS!!!
        // while (pointCount > 1 && dataSet.getPoint(pointCount - 1).getValueY() == null)
        //     dataSet.setPointCount(--pointCount);
    }

    /**
     * Returns the value for given channel index and record index.
     */
    public Object getValueForChannelIndex(int aChanIndex, int anIndex)
    {
        // Get DataChan for ChanIndex
        DataType dataType = _dataSet.getDataType();
        DataChan chan = dataType.getChannel(aChanIndex);

        // Return DataSet value for channel and index
        return _dataSet.getValueForChannel(chan, anIndex);
    }

    /**
     * Sets given value for given channel index and record index.
     */
    public void setValueForChannelIndex(Object aValue, int aChanIndex, int anIndex)
    {
        ViewUtils.beep();
    }

    /**
     * Deletes data for given Trace and selection.
     */
    public void deleteDataForSelection(ListSel aSel)
    {
        ViewUtils.beep();
    }

    /**
     * Replaces data for given Trace and selection.
     */
    public void replaceDataForSelection(ListSel aSel, String[][] theCells)
    {
        ViewUtils.beep();
    }
}
