package snapcharts.views;

import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.Transform;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.view.*;
import snapcharts.model.*;

/**
 * A view to display chart legend.
 */
public class LegendView<T extends Legend> extends ChartPartView<T> {

    /**
     * Returns the ChartPart.
     */
    public T getChartPart()  { return (T) getChart().getLegend(); }

    /**
     * Reloads legend contents.
     */
    public void resetView()
    {
        // Do normal version
        super.resetView();

        // Get info
        Chart chart = getChart();
        Legend legend = chart.getLegend();
        DataSetList dsetList = chart.getDataSetList();

        // Handle visible
        boolean showLegend = legend.isShowLegend();
        setVisible(showLegend);
        if (!showLegend)
            return;

        // Remove children
        removeChildren();

        for (int i=0; i<dsetList.getDataSetCount(); i++) { DataSet dset = dsetList.getDataSet(i);

            // Get marker Shape (if LineChart, add crossbar)
            Shape shp = chart.getSymbolShape(i);
            shp = shp.copyFor(new Transform(6, 6));
            if (chart.getType() == ChartType.LINE) {
                Shape shp1 = new Rect(2,9,16,2);
                shp = Shape.add(shp, shp1);
            }

            // Create marker ShapeView
            ShapeView shpView = new ShapeView(shp);
            shpView.setPrefSize(20,20);

            // Set color
            shpView.setFill(chart.getColor(i));

            StringView sview = new StringView();
            sview.setFont(Font.Arial12.deriveFont(13).getBold());
            sview.setText(dset.getName());
            if (dset.isDisabled()) {
                shpView.setFill(Color.LIGHTGRAY);
                sview.setTextFill(Color.LIGHTGRAY);
            }
            RowView row = new RowView();
            row.addChild(shpView);
            row.addChild(sview);
            addChild(row);

            // Register row to enable/disable
            row.addEventHandler(e -> rowWasClicked(row), MouseRelease);
            //shpView.setPickable(false); sview.setPickable(false);
        }
    }

    /**
     * Called when legend row is clicked.
     */
    void rowWasClicked(RowView aRow)
    {
        // Get row/dataset index
        int index = ArrayUtils.indexOf(getChildren(), aRow);

        // Get dataset and disable
        ChartView chart = getChartView();
        DataSetList dsetList = chart.getDataSetList();
        DataSet dset = dsetList.getDataSet(index);
        dset.setDisabled(!dset.isDisabled());

        // Redraw chart and reload legend
        chart.resetLater();
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        return ColView.getPrefWidth(this, aH);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        return ColView.getPrefHeight(this, aW);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected void layoutImpl()
    {
        ColView.layout(this, false);
    }
}