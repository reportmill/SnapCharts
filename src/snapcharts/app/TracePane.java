package snapcharts.app;
import snap.util.ListSel;
import snap.props.PropChange;
import snap.view.*;
import snapcharts.data.DataChan;
import snapcharts.data.DataPoint;
import snapcharts.data.DataSet;
import snapcharts.data.DataType;
import snapcharts.doc.DocItemTrace;
import snapcharts.model.Trace;
import snapcharts.util.TraceUtils;

/**
 * This ViewOwner subclass provides display and editing of a Trace and its DataSet.
 */
public class TracePane extends DocItemPane<DocItemTrace> {

    // The Trace
    private Trace  _trace;

    // The DataSetPane
    private DataSetPane  _dataSetPane;

    /**
     * Constructor.
     */
    public TracePane(DocItemTrace aTraceDocItem)
    {
        super(aTraceDocItem);

        // Set Trace and start listening to changes
        _trace = aTraceDocItem.getTrace();
        _trace.addPropChangeListener(pc -> traceDidChange(pc));
    }

    /**
     * Returns the Trace.
     */
    public Trace getTrace()  { return _trace; }

    /**
     * Create UI.
     */
    @Override
    protected View createUI()
    {
        // Create DataSetPane
        _dataSetPane = new TraceDataSetPane();

        // Create BoxView to hold DataSetPane
        BoxView boxView = new BoxView(_dataSetPane.getUI(), true, true);

        // Return
        return boxView;
    }

    /**
     * Initialization for first showing.
     */
    protected void initShowing()
    {
        //SheetView sheetView = _dataSetPane.getSheetView();
        //runLater(() -> sheetView.setSelRowColIndex(0, 0));
    }

    /**
     * Resets the UI.
     */
    protected void resetUI()
    {
        _dataSetPane.resetUI();
    }

    /**
     * Resets the UI.
     */
    protected void respondUI(ViewEvent anEvent)  { }

    /**
     * Called when Trace has prop change.
     */
    private void traceDidChange(PropChange aPC)
    {
        String propName = aPC.getPropName();

        // Handle DataSet.DataType change
        if (propName == DataSet.DataType_Prop) {
            if (_dataSetPane != null)
                _dataSetPane._sheetView.setMinColCount(0);
            resetLater();
        }
    }

    /**
     * This is a DataSetPane subclass to work with Trace.DataSet.
     */
    private class TraceDataSetPane extends DataSetPane {

        /**
         * Constructor.
         */
        public TraceDataSetPane()
        {
            super(_trace.getDataSet());
        }

        /**
         * Returns the value for given channel index and record index.
         */
        public Object getValueForChannelIndex(int aChanIndex, int anIndex)
        {
            // Get DataChan for ChanIndex
            DataType dataType = _dataSet.getDataType();
            DataChan chan = dataType.getChannel(aChanIndex);

            // Return DataSet value for channel and index
            return _dataSet.getValueForChannel(chan, anIndex);
        }

        /**
         * Sets given value for given channel index and record index.
         */
        public void setValueForChannelIndex(Object aValue, int aChanIndex, int anIndex)
        {
            // Get DataChan for ChanIndex
            DataType dataType = _trace.getDataType();
            DataChan chan = dataType.getChannel(aChanIndex);

            // Get original point
            DataPoint dataPoint = _trace.getPoint(anIndex);
            DataPoint dataPoint2 = dataPoint.copyForChannelValue(chan, aValue);

            // Forward to DataSet
            _trace.setPoint(dataPoint2, anIndex);
        }

        /**
         * Deletes data for given Trace and selection.
         */
        public void deleteDataForSelection(ListSel aSel)
        {
            TraceUtils.deleteDataForSelection(_trace, aSel);
        }

        /**
         * Replaces data for given Trace and selection.
         */
        public void replaceDataForSelection(ListSel aSel, String[][] theCells)
        {
            TraceUtils.replaceDataForSelection(_trace, aSel, theCells);
        }
    }
}
