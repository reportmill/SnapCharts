package snapcharts.app;

import snap.gfx.Font;
import snap.gfx.ShadowEffect;
import snap.view.*;

/**
 * A class to manage charts/data in a ChartBook.
 */
public class EditorPane extends ViewOwner {
    
    // The chartView
    private ChartView  _chartView;
    
    // The ChartBox
    private BoxView  _chartBox;
    
    // The DataPane to handle chart property editing
    private DataPane  _dataPane;

    // The Inspector
    private InspectorPane  _insp;

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()
    {
        return _chartView;
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        // Get ColView
        RowView topRowView = (RowView)super.createUI();

        // Create ChartView
        _chartView = new ChartView();
        _chartView.setEffect(new ShadowEffect());

        // Create ChartBox
        _chartBox = (BoxView)topRowView.getChild("ChartBox");
        _chartBox.setContent(_chartView);

        // Create DataPane
        _dataPane = new DataPane(_chartView);
        _dataPane.getUI().setPrefHeight(300);
        _dataPane.getUI().setFont(Font.Arial13);

        // Configure TopColView
        ColView topColView = (ColView)topRowView.getChild("TopColView");
        topColView.setChildren(_chartBox, _dataPane.getUI());
        SplitView splitView = SplitView.makeSplitView(topColView);
        splitView.setDividerSpan(5);

        // Create InspectorPane
        _insp = new InspectorPane(this);
        topRowView.addChild(_insp.getUI());

        // Return TopRowView
        return topRowView;
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        System.out.println("EditorPane.respondUI: " + anEvent);
    }
}