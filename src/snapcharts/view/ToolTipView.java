package snapcharts.view;
import snap.geom.*;
import snap.gfx.*;
import snap.view.*;
import snapcharts.model.*;

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

        // If alt down, add index
        int pointIndex = dataPoint.getIndex();
        if (ViewUtils.isAltDown()) {
            if (dset.getDataType() == DataType.XYZZ) {
                addChild(createToolTipEntry("Row: " + dataPoint.getRowIndex()));
                addChild(createToolTipEntry("Col: " + dataPoint.getColIndex()));
            }
            addChild(createToolTipEntry("Index: " + pointIndex));
        }

        // Add children
        int chanCount = dset.getDataType().getChannelCount();
        for (int i=0; i<chanCount; i++) {

            // Get text
            DataChan chan = dset.getDataType().getChannel(i);
            Object val = dset.getValueForChannel(chan, pointIndex);
            String valStr = val instanceof String ? (String) val : _fmt.format(val);
            String text = chan.toString() + ": " + valStr;

            // Create label and add
            View entryView = createToolTipEntry(text);
            addChild(entryView);
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
        ChartHelper chartHelper = _chartView.getChartHelper();
        Point targPoint = chartHelper.getViewXYForDataPoint(_chartView, dataPoint);
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
     * Creates a label for given text.
     */
    private View createToolTipEntry(String aStr)
    {
        StringView valLabel = new StringView();
        valLabel.setMargin(0, 0, 0, 5);
        valLabel.setFont(Font.Arial11.getBold());
        valLabel.setText(aStr);
        return valLabel;
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