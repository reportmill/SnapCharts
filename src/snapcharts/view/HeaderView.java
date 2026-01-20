package snapcharts.view;
import snap.view.ColViewLayout;
import snap.view.StringView;
import snap.view.ViewLayout;
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
        _titleView.setVisible(title != null && !title.isEmpty());
        _titleView.setFont(header.getFont());
        _titleView.setTextColor(header.getTextFill());

        // Reset Subtitle
        String subtitle = header.getSubtitle();
        _subtitleView.setText(subtitle);
        _subtitleView.setVisible(subtitle != null && !subtitle.isEmpty());
        _subtitleView.setFont(header.getSubtitleFont());
        _subtitleView.setTextColor(header.getTextFill());
    }

    /**
     * Override to return column layout.
     */
    @Override
    protected ViewLayout<?> getViewLayoutImpl()  { return new ColViewLayout<>(this); }
}
