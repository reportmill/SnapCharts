package snapcharts.app;

import snap.geom.HPos;
import snap.util.*;
import snap.view.*;
import snapcharts.appmisc.SheetView;
import snapcharts.model.Trace;
import snapcharts.model.TraceList;
import snapcharts.view.ChartView;

/**
 * A class to manage the traces.
 */
public class DataPane extends ViewOwner {
    
    // The ChartView
    private ChartView  _chartView;
    
    // The SheetView
    private SheetView _sheetView;

    // A Cell Action event listener to handle cell text changes
    //EventListener           _cellEditLsnr;

    // Constants for Actions
    public static final String Paste_Action = "PasteAction";
    public static final String SelectAll_Action = "SelectAll";

    /**
     * Creates a DataPane for given ChartView.
     */
    public DataPane(ChartView aCV)  { _chartView = aCV; }

    /**
     * Returns the TraceList.
     */
    public TraceList getTraceList()  { return _chartView.getTraceList(); }

    /**
     * Create UI.
     */
    protected void initUI()
    {
        _sheetView = getView("SheetView", SheetView.class);
        _sheetView.setCellConfigure(c -> configureCell(c));  //_sheetView.setCellEditStart(c -> cellEditStart(c));
        _sheetView.setColConfigure(c -> configureColumn(c));
        _sheetView.addPropChangeListener(pc -> editingCellChanged(pc), TableView.EditingCell_Prop);

        addKeyActionHandler(Paste_Action, "Shortcut+V");
        addKeyActionHandler(SelectAll_Action, "Shortcut+A");
    }

    /**
     * Resets the UI.
     */
    protected void resetUI()
    {
        TraceList traceList = getTraceList();
        Trace[] traces = traceList.getTraces();

        // Update SeriesSpinner, PointSpinner
        setViewValue("SeriesSpinner", traces.length);
        setViewValue("PointSpinner", traceList.getPointCount());

        // Set TableView row & col count
        _sheetView.setMinRowCount(traceList.getTraceCount());
        _sheetView.setMinColCount(traceList.getPointCount()+1);
    }

    /**
     * Resets the UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle ClearButton
        if (anEvent.equals("ClearButton")) {
            TraceList traceList = getTraceList();
            traceList.clear();
            traceList.addTraceForNameAndValues("Series 1", 1d, 2d, 3d);
            _sheetView.setSelRowColIndex(0,0); _sheetView.requestFocus();
        }

        // Handle SeriesSpinner
        if (anEvent.equals("SeriesSpinner")) {
            TraceList traceList = getTraceList();
            traceList.setTraceCount(anEvent.getIntValue());
        }

        // Handle PointSpinner
        if (anEvent.equals("PointSpinner")) {
            TraceList traceList = getTraceList();
            traceList.setPointCount(anEvent.getIntValue());
        }

        // Handle PasteAction
        if (anEvent.equals("PasteAction")) {

        }
    }

    /**
     * Configures a table cell.
     */
    void configureCell(ListCell aCell)
    {
        // Make sure empty cells are minimum size
        aCell.getStringView().setMinSize(40, Math.ceil(aCell.getFont().getLineHeight()));

        // Get trace count and point count
        TraceList traceList = getTraceList();
        int traceCount = traceList.getTraceCount();
        int pointCount = traceList.getPointCount();

        // Get trace count, point count, row and column
        int row = aCell.getRow();
        int col = aCell.getCol();
        if (row>=traceCount || col>pointCount) {
            aCell.setText("");
            return;
        }

        // Get cell trace
        Trace trace = traceList.getTrace(row);

        // Get column and column count
        if (col==0) { aCell.setText(trace.getName()); return; }

        // Get value
        Double val = trace.getValueY(col-1);
        aCell.setText(val!=null? StringUtils.toString(val) : null);
        aCell.setAlignX(HPos.RIGHT);
    }

    /**
     * Configures a table column.
     */
    void configureColumn(TableCol aCol)
    {
        // Get TraceList, trace and column index
        TraceList traceList = getTraceList(); if (traceList.getTraceCount()==0) return;
        Trace trace = traceList.getTrace(0);
        int col = aCol.getColIndex(); if (col>traceList.getPointCount()) { aCol.getHeader().setText(null); return; }

        // Handle first column: Set header to "Trace Name" (left aligned) with adjustable width
        if (col==0) {
            Label hdr = aCol.getHeader();
            hdr.setText("Trace Name");
            hdr.setAlignX(HPos.LEFT);
            aCol.setPrefWidth(-1);
            return;
        }

        // Set the rest of column headers to Trace.Point[i].KeyString
        String hdrText = trace.getString(col - 1);
        aCol.getHeader().setText(hdrText);
    }

    /**
     * Called when cell editing changes.
     */
    private void editingCellChanged(PropChange aPC)
    {
        // If cell that stopped editing (just return if null)
        ListCell cell = (ListCell)aPC.getOldValue(); if (cell==null) return;

        // Get row/col and make sure there are trace/points to cover it
        String text = cell.getText();
        int row = cell.getRow();
        int col = cell.getCol();
        expandTraces(row, col);

        // Get trace
        TraceList traceList = getTraceList();
        Trace trace = traceList.getTrace(row);

        // If header column, set trace name and return
        if (col==0)
            trace.setName(text);

        // Get data point for trace col and set value
        else {
            Double newVal = text!=null && text.length()>0? SnapUtils.doubleValue(text) : null;
            trace.setValueY(newVal, col-1);
            _sheetView.updateItems(trace);
        }

        // Update row and trim trace in case trace/points were cleared
        _sheetView.updateItems(cell.getItem());
        trimTraces();
        resetLater();
    }

    /**
     * Updates TraceList Trace count and Point count to include given row/col.
     */
    void expandTraces(int aRow, int aCol)
    {
        TraceList traceList = getTraceList();
        if (aRow >= traceList.getTraceCount())
            traceList.setTraceCount(aRow+1);
        if (aCol >= traceList.getPointCount())
            traceList.setPointCount(aCol+1);
    }

    /**
     * Removes empty traces and slices.
     */
    void trimTraces()
    {
        // While last trace is clear, remove it
        TraceList traceList = getTraceList();
        int traceCount = traceList.getTraceCount();
        while (traceCount > 1 && traceList.getTrace(traceCount - 1).isClear())
            traceList.removeTrace(--traceCount);

        // While last slice is empty, remove it
        int pointCount = traceList.getPointCount();
        while (pointCount > 1 && traceList.isSliceEmpty(pointCount - 1))
            traceList.setPointCount(--pointCount);
    }
}