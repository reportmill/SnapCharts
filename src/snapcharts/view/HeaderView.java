package snapcharts.view;
import snap.view.ColViewLayout;
import snap.view.StringView;
import snapcharts.charts.Chart;
import snapcharts.charts.Header;

/**
 * A view to hold header elements at top of ChartView.
 */
public class HeaderView extends ChartPartView<Header> {

    // The title
    private StringView  _titleView;

    // The subtitle
    private StringView  _subtitleView;

    /**
     * Constructor.
     */
    public HeaderView()
    {
        // Create configure TitleView
        _titleView = new StringView();
        _titleView.setShrinkToFit(true);
        addChild(_titleView);

        // Create configure SubtitleView
        _subtitleView = new StringView();
        _subtitleView.setShrinkToFit(true);
        addChild(_subtitleView);
    }

    /**
     * Returns the ChartPart.
     */
    public Header getChartPart()  { return getChart().getHeader(); }

    /**
     * Called to reset view from Chart.
     */
    protected void resetView()
    {
        // Do normal version
        super.resetView();

        // Get info
        Chart chart = getChart();
        Header header = chart.getHeader();

        // Reset Title
        String title = header.getTitle();
        _titleView.setText(title);
        _titleView.setVisible(title != null && title.length() > 0);
        _titleView.setFont(header.getFont());
        _titleView.setTextColor(header.getTextFill());

        // Reset Subtitle
        String subtitle = header.getSubtitle();
        _subtitleView.setText(subtitle);
        _subtitleView.setVisible(subtitle != null && subtitle.length() > 0);
        _subtitleView.setFont(header.getSubtitleFont());
        _subtitleView.setTextColor(header.getTextFill());
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        ColViewLayout<?> viewProxy = getViewLayout();
        return viewProxy.getPrefWidth(aH);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        ColViewLayout<?> viewProxy = getViewLayout();
        return viewProxy.getPrefHeight(aW);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected void layoutImpl()
    {
        ColViewLayout<?> viewProxy = getViewLayout();
        viewProxy.layoutView();
    }

    /**
     * Returns the layout to layout this HeaderView.
     */
    protected ColViewLayout<?> getViewLayout()
    {
        ColViewLayout<?> viewProxy = new ColViewLayout<>(this);
        return viewProxy;
    }
}
