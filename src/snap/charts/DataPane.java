package snap.charts;
import java.util.List;

import snap.geom.HPos;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * A class to manage the datasets.
 */
public class DataPane extends ViewOwner {
    
    // The ChartView
    ChartView               _chartView;
    
    // The SheetView
    SheetView               _sheetView;

    // A Cell Action event listener to handle cell text changes
    EventListener           _cellEditLsnr;

/**
 * Creates a DataPane for given ChartView.
 */
public DataPane(ChartView aCV)  { _chartView = aCV; }

/**
 * Returns the DataSet.
 */
public DataSet getDataSet()  { return _chartView.getDataSet(); }

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
    DataSet dset = getDataSet();
    List <DataSeries> seriesList = dset.getSeries();
    
    // Update SeriesSpinner, PointSpinner
    setViewValue("SeriesSpinner", seriesList.size());
    setViewValue("PointSpinner", dset.getPointCount());
    
    // Set TableView row & col count
    _sheetView.setMinRowCount(dset.getSeriesCount());
    _sheetView.setMinColCount(dset.getPointCount()+1);
}

/**
 * Resets the UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle ClearButton
    if(anEvent.equals("ClearButton")) {
        DataSet dset = getDataSet();
        dset.clear();
        dset.addSeriesForNameAndValues("Series 1", 1d, 2d, 3d);
        _sheetView.setSelCell(0,0); _sheetView.requestFocus();
    }
    
    // Handle SeriesSpinner
    if(anEvent.equals("SeriesSpinner")) {
        DataSet dset = getDataSet();
        dset.setSeriesCount(anEvent.getIntValue());
    }
    
    // Handle PointSpinner
    if(anEvent.equals("PointSpinner")) {
        DataSet dset = getDataSet();
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
    
    // Get series count and point count
    DataSet dset = getDataSet();
    int seriesCount = dset.getSeriesCount();
    int pointCount = dset.getPointCount();
    
    // Get series count, point count, row and column
    int row = aCell.getRow(); if(row>=seriesCount) return;
    int col = aCell.getCol(); if(col>pointCount) return;
    
    // Get DataSet and cell series
    DataSeries series = dset.getSeries(row);
    
    // Get column and column count
    if(col==0) { aCell.setText(series.getName()); return; }
    
    // Get value
    Double val = series.getValue(col-1);
    aCell.setText(val!=null? StringUtils.toString(val) : null);
    aCell.setAlign(HPos.RIGHT);
}

/**
 * Configures a table column.
 */
void configureColumn(TableCol aCol)
{
    // Get dataset, series and column index
    DataSet dset = getDataSet();
    DataSeries series = dset.getSeries(0);
    int col = aCol.getColIndex(); if(col>dset.getPointCount()) { aCol.getHeader().setText(null); return; }
    
    // Handle first column: Set header to "Series Name" (left aligned) with adjustable width
    if(col==0) {
        Label hdr = aCol.getHeader(); hdr.setText("Series Name"); hdr.setAlign(HPos.LEFT);
        aCol.setPrefWidth(-1);
        return;
    }
    
    // Set the rest of column headers to Series.Point[i].KeyString
    DataPoint dpnt = series.getPoint(col-1);
    String hdrText = dpnt.getKeyString();
    aCol.getHeader().setText(hdrText);
}

/**
 * Called when cell starts editing.
 */
//void cellEditStart(ListCell <DataSeries> aCell)  { aCell.setEditing(true); }

/**
 * Called when cell stops editing.
 */
void cellEditEnd(ListCell aCell)
{
    // Get row/col and make sure there are series/points to cover it
    String text = aCell.getText();
    int row = aCell.getRow(), col = aCell.getCol();
    expandDataSet(row, col);
    
    // Get series
    DataSet dset = getDataSet();
    DataSeries series = dset.getSeries(row);
    
    // If header column, set series name and return
    if(col==0)
        series.setName(text);
    
    // Get data point for series col and set value
    else {
        Double newVal = text!=null && text.length()>0? SnapUtils.doubleValue(text) : null;
        DataPoint dpoint = series.getPoint(col-1);
        dpoint.setValue(newVal);
        _sheetView.updateItems(series);
    }
    
    // Update row and trim DataSet in case series/points were cleared
    _sheetView.updateItems(aCell.getItem());
    trimDataSet();
    resetLater();
}

/**
 * Updates DataSet Series count and Point count to include given row/col.
 */
void expandDataSet(int aRow, int aCol)
{
    DataSet dset = getDataSet();
    if(aRow>=dset.getSeriesCount())
        dset.setSeriesCount(aRow+1);
    if(aCol>=dset.getPointCount())
        dset.setPointCount(aCol+1);
}

/**
 * Removes empty series and slices.
 */
void trimDataSet()
{
    // While last series is clear, remove it
    DataSet dset = getDataSet();
    int sc = dset.getSeriesCount();
    while(sc>1 && dset.getSeries(sc-1).isClear())
        dset.removeSeries(--sc);
        
    // While last slice is empty, remove it
    int pc = dset.getPointCount();
    while(pc>1 && dset.isSliceEmpty(pc-1))
        dset.setPointCount(--pc);
}

}