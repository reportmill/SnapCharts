package snap.charts;
import java.util.*;
import java.util.function.Consumer;

import snap.geom.HPos;
import snap.geom.Insets;
import snap.view.*;

/**
 * A TableView subclass to emulate a spreadsheet.
 */
public class SheetView extends TableView <Object> {

    // The min row/col counts
    int                  _minRowCount, _minColCount;
    
    // The extra row/col counts
    int                  _extraRowCount = 1, _extraColCount = 1;
    
    // The default preferred width of columns
    double               _prefColWidth = 80;
    
    // The Column Configure method
    Consumer <TableCol>  _colConf;
    
    // The Rebuild run
    Runnable             _rebuildRun, _rebuildRunCached = () -> { rebuildNow(); _rebuildRun = null; };
    
/**
 * Creates a SheetView.
 */
public SheetView()
{
    setShowHeader(true); setEditable(true); setCellPadding(new Insets(6));
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
public Consumer <TableCol> getColConfigure()  { return _colConf; }

/**
 * Sets method to configure column (and column header).
 */
public void setColConfigure(Consumer <TableCol> aCC)  { _colConf = aCC; }

/**
 * Return column at index, adding if needed.
 */
public TableCol getColForce(int anIndex)
{
    // If column count too low, add columns
    if(anIndex>=getColCount()) { double colWidth = getPrefColWidth();
        while(anIndex>=getColCount()) {
            TableCol col = new TableCol(); col.setPrefWidth(colWidth); col.setWidth(colWidth);
            Label header = col.getHeader(); header.setAlign(HPos.CENTER);
            addCol(col);
        }
    }
    
    // Return column
    return getCol(anIndex);
}

/**
 * Rebuilds the sheet.
 */
protected void rebuild()  { if(_rebuildRun==null) getEnv().runLater(_rebuildRun=_rebuildRunCached); }

/**
 * Rebuilds the sheet now.
 */
protected void rebuildNow()
{
    // Calculate column count
    int colCount = getMinColCount() + getExtraColCount();
    
    // If too few columns, add more
    while(getColCount()<colCount)
        getColForce(getColCount());
    
    // If still extra space, add more extra columns
    double totalWidth = 0; for(TableCol col : getCols()) totalWidth += col.getWidth() + 2;
    if(totalWidth<getWidth()) while(totalWidth<getWidth()) {
        TableCol col = getColForce(getColCount());
        totalWidth += col.getWidth() + 2;
    }
    
    // If too many columns, remove them
    else {
        while(getColCount()>colCount && totalWidth>getWidth()) {
            TableCol lastCol = getCol(getColCount()-1);
            if(totalWidth - lastCol.getWidth() - 2>getWidth())
                removeCol(getColCount()-1);
            else break;
        }
    }
    
    // Configure columns/headers
    configureColumns();
    
    // Calculate row count
    int rowCount = getMinRowCount() + getExtraRowCount();
    double rowHeight = getRowHeight();
    
    // If too few rows, add more
    List items = new ArrayList(getItems());
    while(items.size()<rowCount)
        items.add(items.size());
    
    // If still extra height, add more extra rows
    double totalHeight = items.size()*rowHeight, tableHeight = getScrollView().getScroller().getHeight();
    if(totalHeight<tableHeight) while(totalHeight<tableHeight) {
        items.add(items.size()); totalHeight += rowHeight; }
    
    // If too many columns, remove them
    else {
        while(items.size()>rowCount && totalHeight>tableHeight) {
            if(totalHeight - rowHeight>getHeight())
                items.remove(items.size()-1);
            else break;
        }
    }
    
    // Reset items and update all
    setItems(items);
    updateItems();
}

/**
 * Called to configure columns/headers.
 */
protected void configureColumns()
{
    Consumer <TableCol> colConf = getColConfigure();
    for(int i=0;i<getColCount();i++) {
        TableCol col = getCol(i);
        if(colConf!=null) colConf.accept(col);
        else col.getHeader().setText(String.valueOf((char)('A' + i)));
    }
}

/**
 * Override to handle header col special.
 */
protected void configureCell(TableCol <Object> aCol, ListCell <Object> aCell)
{
    // Handle Header Col special
    if(aCell.getCol()<0) {
        aCell.setText(String.valueOf(aCell.getRow()+1)); aCell.setAlign(HPos.CENTER); return; }
        
    // Do normal version
    super.configureCell(aCol, aCell);
}

}