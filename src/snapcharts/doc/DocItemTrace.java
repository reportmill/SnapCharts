package snapcharts.doc;
import snapcharts.charts.ChartPart;
import snapcharts.charts.Trace;

/**
 * A DocItem subclass to hold a chart.
 */
public class DocItemTrace extends DocItem<Trace> {

    /**
     * Constructor.
     */
    public DocItemTrace(Trace aTrace)
    {
        super(aTrace);
    }

    /**
     * Returns the Trace.
     */
    public Trace getTrace()  { return _content; }

    /**
     * Override to return Trace name.
     */
    @Override
    public String getName()
    {
        return getTrace().getName();
    }

    /**
     * Override to return Trace.
     */
    @Override
    public ChartPart getChartPart()  { return getTrace(); }
}
