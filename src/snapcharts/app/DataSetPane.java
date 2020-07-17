package snapcharts.app;

import snap.geom.HPos;
import snap.gfx.Color;
import snap.gfx.ShadowEffect;
import snap.util.SnapUtils;
import snap.util.StringUtils;
import snap.view.*;
import snapcharts.model.Chart;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;
import snapcharts.views.ChartView;

import java.util.ArrayList;
import java.util.List;

/**
 * A ViewOwner to handle display of whole ChartDoc.
 */
public class DataSetPane extends ViewOwner {

    // The DataSet
    private DataSet  _dset;

    // The SheetView
    private SheetView  _sheetView;

    // The Inspector
    private DataSetPaneInsp _insp;

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
        _sheetView = getView("SheetView", SheetView.class);
        _sheetView.setColConfigure(c -> configureColumn(c));
        _sheetView.setCellConfigure(c -> configureCell(c));  //_sheetView.setCellEditStart(c -> cellEditStart(c));
        _sheetView.setCellEditEnd(c -> cellEditEnd(c));

        // Create/add InspectorPane
        RowView topRowView = getUI(RowView.class);
        _insp = new DataSetPaneInsp(this);
        topRowView.addChild(_insp.getUI());
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
        int valCount = 2; //dsetList.getDataSetCount();
        int pointCount = dset.getPointCount();

        // Get dataset count, point count, row and column
        int row = aCell.getRow();
        int col = aCell.getCol();
        if (row>=pointCount || col>=valCount) {
            aCell.setText("");
            return;
        }

        // Get cell value
        Object val;
        if (col==0)
            val = dset.getValueX(row);
        else val = dset.getValue(row);

        // Get cell text and set
        String text = SnapUtils.stringValue(val);
        aCell.setText(text);
        aCell.setAlign(HPos.RIGHT);
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
        int row = aCell.getRow();
        int col = aCell.getCol();
        expandDataSet(row, col);

        // Get dataset
        DataSet dset = getDataSet();

        // If header column, set dataset name and return
//        if (col==0)
//            dset.setName(text);

            // Get data point for dataset col and set value
//        else {
            Double newVal = text!=null && text.length()>0 ? SnapUtils.doubleValue(text) : null;
            dset.setValue(newVal, row);
            _sheetView.updateItems(dset);
//        }

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
        while (pc>1 && !dset.getPoint(pc-1).isValueSet())
            dset.setPointCount(--pc);
    }
}
