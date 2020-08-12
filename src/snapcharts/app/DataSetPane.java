package snapcharts.app;

import snap.geom.HPos;
import snap.util.ListSel;
import snap.util.PropChange;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.model.DataSet;
import snapcharts.model.DataType;

/**
 * A ViewOwner to handle display of whole ChartDoc.
 */
public class DataSetPane extends DocItemPane {

    // The DataSet
    private DataSet  _dset;

    // The SheetView
    private SheetView  _sheetView;

    // The Inspector
    private DataSetPaneInsp _insp;

    // Constants for actions
    private final String Cut_Action = "CutAction";
    private final String Paste_Action = "PasteAction";
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
     * Create UI.
     */
    protected void initUI()
    {
        // Get/configure SheetView
        _sheetView = getView("SheetView", SheetView.class);
        _sheetView.setColConfigure(c -> configureColumn(c));
        _sheetView.setCellConfigure(c -> configureCell(c));
        _sheetView.addPropChangeListener(pc -> editingCellChanged(pc), TableView.EditingCell_Prop);

        // Create/add InspectorPane
        RowView topRowView = getUI(RowView.class);
        _insp = new DataSetPaneInsp(this);
        topRowView.addChild(_insp.getUI());

        // Add PasteAction
        addKeyActionHandler(Cut_Action, "Shortcut+X");
        addKeyActionHandler(Paste_Action, "Shortcut+V");
        //addKeyActionFilter(Delete_Action, "DELETE");
        //addKeyActionFilter(Delete_Action, "BACKSPACE");
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
    }

    /**
     * Resets the UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle Cut_Action
        if (anEvent.equals(Cut_Action)) {
            ListSel sel = _sheetView.getSel(); if (sel.isEmpty()) return;
            if (_sheetView.getEditingCell()!=null) return;
            getDataSet().deleteData(sel);
            _sheetView.setSelIndex(sel.getMin()-1);
            anEvent.consume();
        }

        // Handle Paste_Action
        if (anEvent.equals(Paste_Action)) {
            String dataCells[][] = DataUtils.getClipboardCellData();

            if (dataCells!=null) {
                getDataSet().replaceData(dataCells, _sheetView.getSel());
            }
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
            Label hdr = aCol.getHeader();
            hdr.setText("X");
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
