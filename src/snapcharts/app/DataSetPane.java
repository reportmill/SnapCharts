package snapcharts.app;

import snap.geom.HPos;
import snap.util.ListSel;
import snap.util.PropChange;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.model.DataSet;
import snapcharts.model.DataType;
import snapcharts.model.DataUtils;

/**
 * A ViewOwner to handle display of whole ChartDoc.
 */
public class DataSetPane extends DocItemPane {

    // The DataSet
    private DataSet  _dset;

    // The SheetView
    private SheetView  _sheetView;

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
    }

    /**
     * Called to paste from clipboard.
     */
    private void paste()
    {
        if (SnapUtils.isTeaVM) {
            Clipboard cb = Clipboard.get();
            cb.getApprovedClipboardAndRun(cb2 -> paste(cb2));
        }

        else {
            Clipboard cb = Clipboard.get();
            paste(cb);
        }
    }

    /**
     * Called to paste from clipboard.
     */
    private void paste(Clipboard cb)
    {

        if (cb.hasString()) {
            String str = cb.getString();
            String cells[][] = DataUtils.getCellData(str);
            if (cells != null)
                getDataSet().replaceData(cells, _sheetView.getSel());
            resetLater();
        }
    }

    /**
     * Called to delete selection.
     */
    private void delete()
    {
        ListSel sel = _sheetView.getSel(); if (sel.isEmpty()) return;
        if (_sheetView.getEditingCell()!=null) return;
        getDataSet().deleteData(sel);
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

        // Create/add InspectorPane
        RowView topRowView = getUI(RowView.class);

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
        DataSet dset = getDataSet();

        // Set TableView row & col count
        _sheetView.setMinRowCount(dset.getPointCount()+1);
        _sheetView.setMinColCount(2);

        // Update PointCountLabel
        setViewValue("PointCountLabel", dset.getPointCount() + " Points");
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
    void configureColumn(TableCol aCol)
    {
        int col = aCol.getColIndex();

        // Handle first column: Set header to "DataSet Name" (left aligned) with adjustable width
        if (col==0) {
            DataSet dset = getDataSet();
            DataType dtype = dset!=null ? dset.getDataType() : DataType.UNKNOWN;
            String str = dtype==DataType.IY ? "Index" : dtype==DataType.CY ? "C" : "X";
            Label hdr = aCol.getHeader();
            hdr.setText(str);
            aCol.setPrefWidth(80);
        }
        else if (col==1) {
            Label hdr = aCol.getHeader();
            hdr.setText("Y");
            aCol.setPrefWidth(80);
        }
    }

    /**
     * Configures a table cell.
     */
    void configureCell(ListCell aCell)
    {
        // Make sure empty cells are minimum size
        aCell.getStringView().setMinSize(40, Math.ceil(aCell.getFont().getLineHeight()));

        // Get dataset count and point count
        DataSet dset = getDataSet();
        int pointCount = dset.getPointCount();
        int colCount = 2;

        // Get dataset count, point count, row and column
        int row = aCell.getRow();
        int col = aCell.getCol();
        if (row>=pointCount || col>=colCount) {
            aCell.setText("");
            return;
        }

        // Get cell value
        Object val;
        if (col==0)
            val = dset.getString(row);
        else val = dset.getValueY(row);

        // Get cell text and set
        String text = SnapUtils.stringValue(val);
        aCell.setText(text);
        aCell.setAlign(HPos.RIGHT);
    }

    /**
     * Called when cell editing changes.
     */
    private void editingCellChanged(PropChange aPC)
    {
        // If cell that stopped editing (just return if null)
        ListCell cell = (ListCell)aPC.getOldValue(); if (cell==null) return;

        // Get row/col and make sure there are dataset/points to cover it
        String text = cell.getText();
        int row = cell.getRow();
        int col = cell.getCol();
        expandDataSet(row, col);

        // Get dataset
        DataSet dset = getDataSet();

        // Handle Col 0
        if (col==0) {
            if (dset.getDataType() == DataType.XY) {
                Double newVal = text != null && text.length() > 0 ? SnapUtils.doubleValue(text) : null;
                dset.setValueX(newVal, row);
            }
            else if (dset.getDataType() == DataType.CY) {
                dset.setValueC(text, row);
            }
            else {
                System.err.println("DataSetPane: cellEditEnd: Unknown data type: " + dset.getDataType());
                ViewUtils.beep();
            }
        }

        // Handle Col 1
        else if (col==1) {
            Double newVal = text!=null && text.length()>0 ? SnapUtils.doubleValue(text) : null;
            dset.setValueY(newVal, row);
        }

        // Update row and trim DataSet in case dataset/points were cleared
        _sheetView.updateItems(cell.getItem());
        trimDataSet();
        resetLater();
    }

    /**
     * Updates DataSetList DataSet count and Point count to include given row/col.
     */
    void expandDataSet(int aRow, int aCol)
    {
        DataSet dset = getDataSet();
        if (aRow>=dset.getPointCount())
            dset.setPointCount(aRow+1);
    }

    /**
     * Removes empty dataset and slices.
     */
    void trimDataSet()
    {
        // While last slice is empty, remove it
        DataSet dset = getDataSet();
        int pc = dset.getPointCount();
        while (pc>1 && dset.getPoint(pc-1).getValueY()==null)
            dset.setPointCount(--pc);
    }
}
