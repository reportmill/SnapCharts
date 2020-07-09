package snapcharts.app;
import snap.geom.*;
import snap.gfx.*;
import snap.view.*;
import snapcharts.model.Chart;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSeries;
import snapcharts.model.DataSet;

/**
 * A view to show tooltip.
 */
public class ToolTipView extends ColView {
    
    // The ChartView
    ChartView      _chartView;
    
    // The selected data point
    DataPoint _selPoint;
    
    // A runnable to reload contents
    Runnable       _reloadLater, _reloadRun = () -> { reloadContentsNow(); _reloadLater = null; };

/**
 * Creates a ToolTipView.
 */
public ToolTipView(ChartView aCV)
{
    _chartView = aCV;
    setManaged(false); setPickable(false);
}

/**
 * Sets the X/Y in area coords.
 */
public void setXYInChartArea(Point aPnt)
{
    Point pnt = _chartView.getChartArea().localToParent(aPnt.x, aPnt.y, _chartView);
    setXYInChartView(pnt);
}

/**
 * Sets the X/Y in area coords.
 */
public void setXYInChartView(Point aPnt)
{
    // If no data point, just return
    if(_chartView.getTargDataPoint()==null) return;
    
    // Get new point, set and clear animations
    double nx = aPnt.x - getWidth()/2;
    double ny = aPnt.y - getHeight() - 8;
    setXY(nx,ny);
    getAnimCleared(0);
}

/**
 * Called when tooltip contents changed.
 */
public void reloadContents()
{
    if(_reloadLater==null)
        getEnv().runLater(_reloadLater = _reloadRun);
}

/**
 * Called when tooltip contents changed.
 */
protected void reloadContentsNow()
{
    // Go ahead and repaint chartView
    _chartView.repaint();
    
    // Get DataPoint - if null - remove view
    Chart chart = _chartView.getChart();
    DataSet dset = chart.getDataSet();
    DataPoint dataPoint = _chartView.getTargDataPoint();
    if(dataPoint==null) {
        getAnimCleared(1000).setOpacity(0).setOnFinish(a -> _chartView.removeChild(this)).play(); return; }
        
    // Get series and value
    DataSeries series = dataPoint.getSeries();
    String selKey = dataPoint.getKeyString();
    double selValue = dataPoint.getValueX();
        
    // Remove children and reset opacity, padding and spacing
    removeChildren(); setOpacity(1);
    setPadding(7,7,15,7); setSpacing(5);
    
    // Set KeyLabel string
    StringView keyLabel = new StringView(); keyLabel.setFont(Font.Arial10); addChild(keyLabel);
    keyLabel.setText(selKey);
    
    // Create RowView: BulletView
    Color color = chart.getColor(series.getIndex());
    ShapeView bulletView = new ShapeView(new Ellipse(0,0,5,5)); bulletView.setFill(color);
    
    // Create RowView: NameLabel, ValLabel
    StringView nameLabel = new StringView(); nameLabel.setFont(Font.Arial12);
    nameLabel.setText(series.getName() + ":");
    StringView valLabel = new StringView(); valLabel.setFont(Font.Arial12.deriveFont(13).getBold());
    valLabel.setText(ChartView._fmt.format(selValue));
    
    // Create RowView and add BulletView, NameLabel and ValLabel
    RowView rview = new RowView(); rview.setSpacing(5);
    rview.setChildren(bulletView, nameLabel, valLabel);
    addChild(rview);
    
    // Calculate and set new size, keeping same center
    double oldWidth = getWidth(), oldHeight = getHeight();
    double newWidth = getPrefWidth(), newHeight = getPrefHeight();
    setSize(newWidth, newHeight);
    setX(getX() - (newWidth/2 - oldWidth/2));
    setY(getY() - (newHeight/2 - oldHeight/2));
    
    // Create background shape
    RoundRect shp0 = new RoundRect(1,1,newWidth-2,newHeight-8,3); double midx = shp0.getMidX();
    Shape shp1 = new Polygon(midx-6,newHeight-8,midx+6,newHeight-8,midx,newHeight-2);
    Shape shp2 = Shape.add(shp0,shp1);
    
    // Create background shape view and add
    ShapeView shpView = new ShapeView(shp2); shpView.setManaged(false); shpView.setPrefSize(newWidth,newHeight+10);
    shpView.setFill(Color.get("#F8F8F8DD")); shpView.setBorder(color,1); //shpView.setEffect(new ShadowEffect());
    addChild(shpView, 0);
    
    // Colculate new location
    Point pnt = _chartView.dataPointInLocal(dataPoint);
    double nx = pnt.x - getWidth()/2;
    double ny = pnt.y - getHeight() - 8;
    
    // If not onscreen, add and return
    if(getParent()==null) {
        setXY(nx, ny); _chartView.addChild(this); return; }
        
    // Otherwise animate move
    getAnimCleared(300).setX(nx).setY(ny).play();
}

}