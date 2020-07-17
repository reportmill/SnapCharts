package snapcharts.app;
import java.util.List;

import snap.geom.HPos;
import snap.util.*;
import snap.view.*;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;
import snapcharts.views.ChartView;

/**
 * A class to manage the datasets.
 */
public class DataPane extends ViewOwner {
    
    // The ChartView
    private ChartView  _chartView;
    
    // The SheetView
    private SheetView  _sheetView;

    // A Cell Action event listener to handle cell text changes
    //EventListener           _cellEditLsnr;

    /**
     * Creates a DataPane for given ChartView.
     */
    public DataPane(ChartView aCV)  { _chartView = aCV; }

    /**
     * Returns the DataSet.
     */
    public DataSetList getDataSet()  { return _chartView.getDataSetList(); }

    /**
     * Create UI.
     */
    protected void initUI()
    {
        _sheetView = getView("SheetView", SheetView.class);
        _sheetView.setCellConfigure(c -> configureCell(c));  //_sheetView.setCellEditStart(c -> cellEditStart(c));
        _sheetView.setColConfigure(c -> configureColumn(c));
        _sheetView.setCellEditEnd(c -> cellEditEnd(c));
    }

    /**
     * Resets the UI.
     */
    protected void resetUI()
    {
        DataSetList dsetList = getDataSet();
        List <DataSet> dsets = dsetList.getDataSets();

        // Update SeriesSpinner, PointSpinner
        setViewValue("SeriesSpinner", dsets.size());
        setViewValue("PointSpinner", dsetList.getPointCount());

        // Set TableView row & col count
        _sheetView.setMinRowCount(dsetList.getDataSetCount());
        _sheetView.setMinColCount(dsetList.getPointCount()+1);
    }

    /**
     * Resets the UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle ClearButton
        if (anEvent.equals("ClearButton")) {
            DataSetList dset = getDataSet();
            dset.clear();
            dset.addDataSetForNameAndValues("Series 1", 1d, 2d, 3d);
            _sheetView.setSelCell(0,0); _sheetView.requestFocus();
        }

        // Handle SeriesSpinner
        if (anEvent.equals("SeriesSpinner")) {
            DataSetList dset = getDataSet();
            dset.setDataSetCount(anEvent.getIntValue());
        }

        // Handle PointSpinner
        if (anEvent.equals("PointSpinner")) {
            DataSetList dset = getDataSet();
            dset.setPointCount(anEvent.getIntValue());
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
        DataSetList dsetList = getDataSet();
        int dsetCount = dsetList.getDataSetCount();
        int pointCount = dsetList.getPointCount();

        // Get dataset count, point count, row and column
        int row = aCell.getRow();
        int col = aCell.getCol();
        if (row>=dsetCount || col>pointCount) {
            aCell.setText("");
            return;
        }

        // Get cell dataset
        DataSet dset = dsetList.getDataSet(row);

        // Get column and column count
        if (col==0) { aCell.setText(dset.getName()); return; }

        // Get value
        Double val = dset.getValue(col-1);
        aCell.setText(val!=null? StringUtils.toString(val) : null);
        aCell.setAlign(HPos.RIGHT);
    }

    /**
     * Configures a table column.
     */
    void configureColumn(TableCol aCol)
    {
        // Get DataSetList, dataset and column index
        DataSetList dsetList = getDataSet();
        DataSet dset = dsetList.getDataSet(0);
        int col = aCol.getColIndex(); if (col>dsetList.getPointCount()) { aCol.getHeader().setText(null); return; }

        // Handle first column: Set header to "DataSet Name" (left aligned) with adjustable width
        if (col==0) {
            Label hdr = aCol.getHeader();
            hdr.setText("DataSet Name");
            hdr.setAlign(HPos.LEFT);
            aCol.setPrefWidth(-1);
            return;
        }

        // Set the rest of column headers to DataSet.Point[i].KeyString
        DataPoint dpnt = dset.getPoint(col-1);
        String hdrText = dpnt.getKeyString();
        aCol.getHeader().setText(hdrText);
    }

    ///** Called when cell starts editing. */
    //void cellEditStart(ListCell <DataSet> aCell)  { aCell.setEditing(true); }

    /**
     * Called when cell stops editing.
     */
    void cellEditEnd(ListCell aCell)
    {
        // Get row/col and make sure there are dataset/points to cover it
        String text = aCell.getText();
        int row = aCell.getRow(), col = aCell.getCol();
        expandDataSet(row, col);

        // Get dataset
        DataSetList dsetList = getDataSet();
        DataSet dset = dsetList.getDataSet(row);

        // If header column, set dataset name and return
        if (col==0)
            dset.setName(text);

        // Get data point for dataset col and set value
        else {
            Double newVal = text!=null && text.length()>0? SnapUtils.doubleValue(text) : null;
            dset.setValue(newVal, col-1);
            _sheetView.updateItems(dset);
        }

        // Update row and trim DataSet in case dataset/points were cleared
        _sheetView.updateItems(aCell.getItem());
        trimDataSet();
        resetLater();
    }

    /**
     * Updates DataSetList DataSet count and Point count to include given row/col.
     */
    void expandDataSet(int aRow, int aCol)
    {
        DataSetList dset = getDataSet();
        if (aRow>=dset.getDataSetCount())
            dset.setDataSetCount(aRow+1);
        if (aCol>=dset.getPointCount())
            dset.setPointCount(aCol+1);
    }

    /**
     * Removes empty dataset and slices.
     */
    void trimDataSet()
    {
        // While last dataset is clear, remove it
        DataSetList dsetList = getDataSet();
        int sc = dsetList.getDataSetCount();
        while (sc>1 && dsetList.getDataSet(sc-1).isClear())
            dsetList.removeDataSet(--sc);

        // While last slice is empty, remove it
        int pc = dsetList.getPointCount();
        while (pc>1 && dsetList.isSliceEmpty(pc-1))
            dsetList.setPointCount(--pc);
    }
}