package snapcharts.view;
import snap.geom.*;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.data.DataChan;
import snapcharts.data.DataSet;
import snapcharts.data.DataSetXYZZ;
import snapcharts.data.DataType;
import snapcharts.model.*;
import java.text.DecimalFormat;

/**
 * A view to show tooltip.
 */
public class ToolTipView extends ColView {
    
    // The ChartView
    private ChartView  _chartView;
    
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
        setManaged(false);
        setPickable(false);
    }

    /**
     * Called when tooltip contents changed.
     */
    public void reloadContents()
    {
        if (_reloadLater == null)
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
        TracePoint dataPoint = _chartView.getTargDataPoint();
        Trace trace = dataPoint.getTrace();

        // Remove children and reset padding
        removeChildren();
        setPadding(5,5,10,5);

        // Create RowView: BulletView
        Color color = trace.getLineColor();

        // If alt down, add index
        int pointIndex = dataPoint.getIndex();
        if (ViewUtils.isAltDown()) {
            if (trace.getDataType() == DataType.XYZZ) {
                int rowIndex = DataSetXYZZ.getRowIndex(dataPoint);
                int colIndex = DataSetXYZZ.getColIndex(dataPoint);
                addChild(createToolTipEntry("Row: " + rowIndex));
                addChild(createToolTipEntry("Col: " + colIndex));
            }
            addChild(createToolTipEntry("Index: " + pointIndex));
        }

        // This is probably bogus - for wrapped traces
        if (pointIndex >= trace.getPointCount()) {
            SnapUtils.printlnOnce(System.err, "ToolTipView.reloadContentsNow: Need to handle wrapped datasets better");
            int pointCount = trace.getPointCount(); if (pointCount == 0) return;
            pointIndex = pointIndex % pointCount;
        }

        // Add row entry for each DataSet.DataType.DataChan
        DataSet dataSet = trace.getProcessedData();
        DataType dataType = dataSet.getDataType();
        int dataChanCount = dataType.getChannelCount();
        for (int i = 0; i < dataChanCount; i++) {

            // Get text
            DataChan chan = dataType.getChannel(i);
            Object val = dataSet.getValueForChannel(chan, pointIndex);
            String valStr = val instanceof String ? (String) val : _fmt.format(val);
            String text = chan + ": " + valStr;

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
        setX(getX() - Math.round(newWidth / 2 - oldWidth / 2));
        setY(getY() - Math.round(newHeight / 2 - oldHeight / 2));

        // Create background shape
        RoundRect shp0 = new RoundRect(1,1,newWidth - 2,newHeight - 8,3);
        double midx = shp0.getMidX();
        Shape shp1 = new Polygon(midx - 6, newHeight - 8, midx + 6, newHeight - 8, midx, newHeight-2);
        Shape shp2 = Shape.addShapes(shp0, shp1);

        // Create background shape view and add
        ShapeView shpView = new ShapeView(shp2);
        shpView.setManaged(false);
        shpView.setPrefSize(newWidth,newHeight + 10);
        shpView.setFill(Color.get("#F8F8F8DD"));
        shpView.setBorder(color,1); //shpView.setEffect(new ShadowEffect());
        addChild(shpView, 0);

        // Calc new location
        ChartHelper chartHelper = _chartView.getChartHelper();
        Point targPoint = chartHelper.getViewXYForDataPoint(_chartView, dataPoint);
        double ttipX = targPoint.x - getWidth() / 2;
        double ttipY = targPoint.y - getHeight() - 8;

        // If outside ContentView, just return
        Point targPointInContentView = _chartView.getContentView().parentToLocal(targPoint.x, targPoint.y, _chartView);
        if (!_chartView.getContentView().contains(targPointInContentView)) {
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
        if (getParent() == null)
            ViewUtils.addChild(_chartView, this);

        // Set location, reset opacity and make visible
        setXY(aX, aY);
        setOpacity(1);
        setVisible(true);
    }

    /**
     * Hides the window.
     */
    public void hideWindow()
    {
        // If not showing, just return
        if (getParent() == null) return;

        // Register anim to hide this view
        ViewAnim anim = getAnimCleared(300);
        anim.setOpacity(0);
        anim.setOnFinish(a -> setVisible(false));
        anim.play();
    }
}