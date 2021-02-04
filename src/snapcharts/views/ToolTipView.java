package snapcharts.views;
import snap.geom.*;
import snap.gfx.*;
import snap.view.*;
import snapcharts.model.Chart;
import snapcharts.model.DataChan;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSet;
import java.text.DecimalFormat;

/**
 * A view to show tooltip.
 */
public class ToolTipView extends ColView {
    
    // The ChartView
    private ChartView _chartView;
    
    // A runnable to reload contents
    private Runnable  _reloadLater, _reloadRun = () -> { reloadContentsNow(); _reloadLater = null; };

    // Shared
    private static DecimalFormat _fmt = new DecimalFormat("#,###.##");

    /**
     * Creates a ToolTipView.
     */
    public ToolTipView(ChartView aCV)
    {
        _chartView = aCV;
        setManaged(false); setPickable(false);
    }

    /**
     * Called when tooltip contents changed.
     */
    public void reloadContents()
    {
        if (_reloadLater==null)
            getEnv().runLater(_reloadLater = _reloadRun);
    }

    /**
     * Called when tooltip contents changed.
     */
    protected void reloadContentsNow()
    {
        // Go ahead and repaint chartView
        _chartView.repaint();

        // If not ChartView.ShowTargDataPoint, hide and return
        if (!_chartView.isShowTargDataPoint()) {
            hideWindow();
            return;
        }

        // Get info
        Chart chart = _chartView.getChart();
        DataPoint dataPoint = _chartView.getTargDataPoint();
        DataSet dset = dataPoint.getDataSet();

        // Remove children and reset opacity, padding and spacing
        removeChildren();
        setOpacity(1);
        setPadding(5,5,10,5);

        // Create RowView: BulletView
        Color color = chart.getColor(dset.getIndex());
        //ShapeView bulletView = new ShapeView(new Ellipse(0,0,5,5));
        //bulletView.setFill(color);

        // Create RowView: NameLabel, ValLabel
        //StringView nameLabel = new StringView();
        //nameLabel.setFont(Font.Arial12);
        //nameLabel.setText(dset.getName() + ":");

        // Create RowView and add BulletView, NameLabel and ValLabel
        //RowView rview = new RowView();
        //rview.setSpacing(5);
        //rview.setMargin(0, 0, 3, 0);
        //rview.setChildren(bulletView, nameLabel);
        //addChild(rview);

        // Add children
        int chanCount = dset.getDataType().getChannelCount();
        for (int i=0; i<chanCount; i++) {

            // Get text
            DataChan chan = dset.getDataType().getChannel(i);
            Object val = dset.getValueForChannel(chan, dataPoint.getIndex());
            String valStr = val instanceof String ? (String) val : _fmt.format(val);
            String text = chan.toString() + ": " + valStr;

            // Create label
            StringView valLabel = new StringView();
            valLabel.setMargin(0, 0, 0, 5);
            valLabel.setFont(Font.Arial11.getBold());
            valLabel.setText(text);
            addChild(valLabel);
        }

        // Calculate and set new size, keeping same center
        double oldWidth = getWidth();
        double oldHeight = getHeight();
        double newWidth = getPrefWidth();
        double newHeight = getPrefHeight();
        setSize(newWidth, newHeight);
        setX(getX() - Math.round(newWidth/2 - oldWidth/2));
        setY(getY() - Math.round(newHeight/2 - oldHeight/2));

        // Create background shape
        RoundRect shp0 = new RoundRect(1,1,newWidth-2,newHeight-8,3);
        double midx = shp0.getMidX();
        Shape shp1 = new Polygon(midx-6, newHeight-8, midx+6, newHeight-8, midx, newHeight-2);
        Shape shp2 = Shape.add(shp0, shp1);

        // Create background shape view and add
        ShapeView shpView = new ShapeView(shp2);
        shpView.setManaged(false);
        shpView.setPrefSize(newWidth,newHeight+10);
        shpView.setFill(Color.get("#F8F8F8DD"));
        shpView.setBorder(color,1); //shpView.setEffect(new ShadowEffect());
        addChild(shpView, 0);

        // Calc new location
        Point targPoint = _chartView.getDataPointXYLocal(dataPoint);
        double ttipX = targPoint.x - getWidth()/2;
        double ttipY = targPoint.y - getHeight() - 8;

        // If outside DataView, just return
        Point targPointInDataView = _chartView.getDataView().parentToLocal(targPoint.x, targPoint.y, _chartView);
        if (!_chartView.getDataView().contains(targPointInDataView)) {
            hideWindow();
            return;
        }

        // Show window
        showWindowAt(ttipX, ttipY);
    }

    /**
     * Shows the window at given point in ChartView coords.
     */
    public void showWindowAt(double aX, double aY)
    {
        // If not onscreen, add and return
        if (getParent()==null) {
            setXY(aX, aY);
            ViewUtils.addChild(_chartView, this);
            return;
        }

        // Otherwise animate move
        getAnimCleared(300).setX(aX).setY(aY).play();
    }

    /**
     * Hides the window.
     */
    public void hideWindow()
    {
        if (getParent()==null) return;
        getAnimCleared(500).setOpacity(0).setOnFinish(a -> ViewUtils.removeChild(_chartView, this)).play();
    }
}