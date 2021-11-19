package snapcharts.app;
import snap.geom.HPos;
import snap.util.FormatUtils;
import snap.util.ListSel;
import snap.util.PropChange;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.appmisc.SheetView;
import snapcharts.doc.DocItemDataSet;
import snapcharts.data.DataChan;
import snapcharts.model.Trace;
import snapcharts.data.DataSet;
import snapcharts.data.DataType;
import snapcharts.util.TraceUtils;
import snapcharts.data.DataUtils;

/**
 * A ViewOwner to handle display of whole ChartDoc.
 */
public class DataSetPane extends DocItemPane<DocItemDataSet> {

    // The Trace
    private Trace  _trace;

    // The SheetView
    private SheetView  _sheetView;

    // Constants for actions
    private final String Cut_Action = "CutAction";
    private final String Paste_Action = "PasteAction";
    private final String SelectAll_Action = "SelectAllAction";
    private final String Delete_Action = "DeleteAction";

    /**
     * Constructor.
     */
    public DataSetPane(DocItemDataSet aDataSet)
    {
        super(aDataSet);

        setTrace(aDataSet.getTrace());
    }

    /**
     * Returns the Trace.
     */
    public Trace getTrace()  { return _trace; }

    /**
     * Sets the Trace.
     */
    public void setTrace(Trace aDS)
    {
        _trace = aDS;

        // Start listening to changes
        _trace.addPropChangeListener(pc -> traceDidChange(pc));
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
            Trace trace = getTrace();
            ListSel sel = _sheetView.getSel();
            TraceUtils.replaceDataForSelection(trace, sel, cells);
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

        // Get Trace
        Trace trace = getTrace();
        TraceUtils.deleteDataForSelection(trace, sel);

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
        //setFirstFocus(_sheetView);

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
        // Get Trace info
        Trace trace = getTrace();
        DataType dataType = trace.getDataType();

        // Set TableView row & col count
        int pointCount = trace.getPointCount();
        int rowCount = pointCount + 1;
        int colCount = dataType.getChannelCount();
        _sheetView.setMinRowCount(rowCount);
        _sheetView.setMinColCount(colCount);

        // Update PointCountLabel
        DataSet dataSet = trace.getDataSet();
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
        // Get Trace info
        Trace trace = getTrace();
        DataType dataType = trace.getDataType();

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
        aCol.setPrefWidth(80);
    }

    /**
     * Configures a table cell.
     */
    private void configureCell(ListCell aCell)
    {
        // Get Trace info
        Trace trace = getTrace();
        DataType dataType = trace.getDataType();
        int chanCount = dataType.getChannelCount();
        int pointCount = trace.getPointCount();

        // Set Cell.MinSize
        int minCellHeight = (int) Math.ceil(aCell.getFont().getLineHeight());
        aCell.getStringView().setMinSize(40, minCellHeight);

        // Get trace count, point count, row and column
        int row = aCell.getRow();
        int col = aCell.getCol();
        if (row>=pointCount || col>=chanCount) {
            aCell.setText("");
            return;
        }

        // Get/set Cell value/text
        Object val = trace.getValueForChannelIndex(col, row);
        String valStr = SnapUtils.stringValue(val);
        aCell.setText(valStr);
        aCell.setAlignX(HPos.RIGHT);
    }

    /**
     * Called when cell editing changes.
     */
    private void editingCellChanged(PropChange aPC)
    {
        // Get Trace info
        Trace trace = getTrace();

        // If cell that stopped editing (just return if null)
        ListCell cell = (ListCell) aPC.getOldValue(); if (cell == null) return;

        // Get row/col and make sure there are trace/points to cover it
        String text = cell.getText();
        int row = cell.getRow();
        int col = cell.getCol();
        expandTraceSize(row);

        // Set value
        trace.setValueForChannelIndex(text, col, row);

        // Update row and trim Trace in case trace/points were cleared
        _sheetView.updateItems(cell.getItem());
        trimTrace();
        resetLater();
    }

    /**
     * Make sure trace is at least given size.
     */
    private void expandTraceSize(int aSize)
    {
        Trace trace = getTrace();
        if (aSize >= trace.getPointCount())
            trace.setPointCount(aSize + 1);
    }

    /**
     * Removes empty trace and slices.
     */
    private void trimTrace()
    {
        // While last slice is empty, remove it
        Trace trace = getTrace();
        int pointCount = trace.getPointCount();
        while (pointCount > 1 && trace.getPoint(pointCount - 1).getValueY() == null)
            trace.setPointCount(--pointCount);
    }

    /**
     * Called when Trace has prop change.
     */
    private void traceDidChange(PropChange aPC)
    {
        String propName = aPC.getPropName();

        // Handle Trace.DataType change
        if (propName == Trace.DataType_Prop) {
            _sheetView.setMinColCount(0);
            resetLater();
        }
    }
}
