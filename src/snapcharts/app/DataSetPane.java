package snapcharts.app;

import snap.geom.HPos;
import snap.util.FormatUtils;
import snap.util.ListSel;
import snap.util.PropChange;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.appmisc.SheetView;
import snapcharts.model.DataChan;
import snapcharts.model.DataSet;
import snapcharts.model.DataStore;
import snapcharts.model.DataType;
import snapcharts.util.DataSetUtils;
import snapcharts.util.DataUtils;

/**
 * A ViewOwner to handle display of whole ChartDoc.
 */
public class DataSetPane extends DocItemPane {

    // The DataSet
    private DataSet  _dset;

    // The SheetView
    private SheetView _sheetView;

    // Constants for actions
    private final String Cut_Action = "CutAction";
    private final String Paste_Action = "PasteAction";
    private final String SelectAll_Action = "SelectAllAction";
    private final String Delete_Action = "DeleteAction";

    /**
     * Returns the DataSet.
     */
    public DataSet getDataSet()  { return _dset; }

    /**
     * Sets the DataSet.
     */
    public void setDataSet(DataSet aDS)
    {
        _dset = aDS;

        // Start listening to changes
        _dset.addPropChangeListener(pc -> dataSetDidChange(pc));
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
            DataSet dset = getDataSet();
            ListSel sel = _sheetView.getSel();
            DataSetUtils.replaceDataForSelection(dset, sel, cells);
        }

        // Reset
        resetLater();
    }

    /**
     * Called to delete selection.
     */
    private void delete()
    {
        // Just return if editing
        if (_sheetView.getEditingCell()!=null) return;

        // Get selection (just return if empty)
        ListSel sel = _sheetView.getSel(); if (sel.isEmpty()) return;

        // Get DataSet
        DataSet dset = getDataSet();
        DataSetUtils.deleteDataForSelection(dset, sel);

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
        _sheetView.setColConfigure(c -> configureColumn(c));
        _sheetView.setCellConfigure(c -> configureCell(c));
        _sheetView.addPropChangeListener(pc -> editingCellChanged(pc), TableView.EditingCell_Prop);
        setFirstFocus(_sheetView);

        // Add PasteAction
        addKeyActionHandler(Cut_Action, "Shortcut+X");
        addKeyActionHandler(Paste_Action, "Shortcut+V");
        addKeyActionHandler(SelectAll_Action, "Shortcut+A");
        //addKeyActionFilter(Delete_Action, "DELETE");
        //addKeyActionFilter(Delete_Action, "BACKSPACE");
    }

    /**
     * Initialization for first showing.
     */
    protected void initShowing()
    {
        // This sucks - twice to run after resetUI() + SheetView.rebuild()
        runLater(() -> runLater(() -> _sheetView.setSelRowColIndex(0, 0)));
    }

    /**
     * Resets the UI.
     */
    protected void resetUI()
    {
        // Get DataSet info
        DataSet dset = getDataSet();
        DataType dataType = dset.getDataType();

        // Set TableView row & col count
        int pointCount = dset.getPointCount();
        int rowCount = Math.min(pointCount + 1, 500); // SheetView/TableView can't handle 1000s
        int colCount = dataType.getChannelCount();
        _sheetView.setMinRowCount(rowCount);
        _sheetView.setMinColCount(colCount);

        // Update PointCountLabel
        DataStore dataStore = dset.getRawData();
        String str = "Points: " + pointCount + "   |   ";
        str += "Min X: " + FormatUtils.formatNum(dataStore.getMinX()) + "   |   ";
        str += "Max X: " + FormatUtils.formatNum(dataStore.getMaxX()) + "   |   ";
        str += "Min Y: " + FormatUtils.formatNum(dataStore.getMinY()) + "   |   ";
        str += "Max Y: " + FormatUtils.formatNum(dataStore.getMaxY());
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
        DataSet dset = getDataSet();
        DataType dtype = dset.getDataType();

        // Get column and DataChan
        int col = aCol.getColIndex();
        DataChan dchan = col<dtype.getChannelCount() ? dtype.getChannel(col) : null;

        // Get HeaderText string
        String headerText = "";
        if (dchan==DataChan.I)
            headerText = "Index";
        else if (dchan!=null)
            headerText = dchan.toString();

        // Set Column.Header.Text and PrefWidth
        Label header = aCol.getHeader();
        header.setText(headerText);
        aCol.setPrefWidth(80);
    }

    /**
     * Configures a table cell.
     */
    private void configureCell(ListCell aCell)
    {
        // Get DataSet info
        DataSet dset = getDataSet();
        DataType dataType = dset.getDataType();
        int chanCount = dataType.getChannelCount();
        int pointCount = dset.getPointCount();

        // Set Cell.MinSize
        int minCellHeight = (int) Math.ceil(aCell.getFont().getLineHeight());
        aCell.getStringView().setMinSize(40, minCellHeight);

        // Get dataset count, point count, row and column
        int row = aCell.getRow();
        int col = aCell.getCol();
        if (row>=pointCount || col>=chanCount) {
            aCell.setText("");
            return;
        }

        // Get/set Cell value/text
        Object val = dset.getValueForChannelIndex(col, row);
        String valStr = SnapUtils.stringValue(val);
        aCell.setText(valStr);
        aCell.setAlign(HPos.RIGHT);
    }

    /**
     * Called when cell editing changes.
     */
    private void editingCellChanged(PropChange aPC)
    {
        // Get DataSet info
        DataSet dset = getDataSet();

        // If cell that stopped editing (just return if null)
        ListCell cell = (ListCell) aPC.getOldValue(); if (cell == null) return;

        // Get row/col and make sure there are dataset/points to cover it
        String text = cell.getText();
        int row = cell.getRow();
        int col = cell.getCol();
        expandDataSetSize(row);

        // Set value
        dset.setValueForChannelIndex(text, col, row);

        // Update row and trim DataSet in case dataset/points were cleared
        _sheetView.updateItems(cell.getItem());
        trimDataSet();
        resetLater();
    }

    /**
     * Make sure dataset is at least given size.
     */
    private void expandDataSetSize(int aSize)
    {
        DataSet dset = getDataSet();
        if (aSize >= dset.getPointCount())
            dset.setPointCount(aSize + 1);
    }

    /**
     * Removes empty dataset and slices.
     */
    private void trimDataSet()
    {
        // While last slice is empty, remove it
        DataSet dset = getDataSet();
        int pc = dset.getPointCount();
        while (pc>1 && dset.getPoint(pc-1).getValueY()==null)
            dset.setPointCount(--pc);
    }

    /**
     * Called when DataSet has prop change.
     */
    private void dataSetDidChange(PropChange aPC)
    {
        String propName = aPC.getPropName();

        // Handle DataSet.DataType change
        if (propName == DataSet.DataType_Prop) {
            _sheetView.setMinColCount(0);
            resetLater();
        }
    }
}
