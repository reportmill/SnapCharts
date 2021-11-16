package snapcharts.doc;
import snapcharts.model.ChartPart;
import snapcharts.model.Trace;

/**
 * A DocItem subclass to hold a chart.
 */
public class DocItemDataSet extends DocItem<Trace> {

    /**
     * Constructor.
     */
    public DocItemDataSet(Trace aTrace)
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
