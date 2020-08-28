package snapcharts.views;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.view.ColView;
import snap.view.ParentView;
import snap.view.StringView;
import snapcharts.model.Chart;

/**
 * A view to hold elements at top of ChartView.
 */
public class ChartViewTop extends ChartPartView {

    // The title
    private StringView _titleView;

    // The subtitle
    private StringView  _subtitleView;

    /**
     * Constructor.
     */
    public ChartViewTop(ChartView aChartView)
    {
        // Basic config
        setAlign(Pos.CENTER);
        setSpacing(8);
        setMargin(0, 0, 8, 0);

        // Create configure TitleView
        _titleView = new StringView();
        _titleView.setFont(Font.Arial14.getBold().deriveFont(20));
        addChild(_titleView);

        // Create configure SubtitleView
        _subtitleView = new StringView();
        _subtitleView.setTextFill(Color.GRAY);
        _subtitleView.setFont(Font.Arial12.getBold());
        addChild(_subtitleView);
    }

    /**
     * Called to reset view from Chart.
     */
    protected void resetView()
    {
        // Get info
        Chart chart = getChart();

        // Reset Title
        String title = chart.getTitle();
        _titleView.setText(title);
        _titleView.setVisible(title != null && title.length() > 0);

        // Reset Subtitle
        String subtitle = chart.getSubtitle();
        _subtitleView.setText(subtitle);
        _subtitleView.setVisible(subtitle != null && subtitle.length() > 0);
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
