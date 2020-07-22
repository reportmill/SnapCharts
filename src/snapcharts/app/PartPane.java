package snapcharts.app;
import snap.view.View;
import snap.view.ViewOwner;

/**
 * The base class for ChartPart editors.
 */
public class PartPane extends ViewOwner {

    /**
     * Returns the view for the ChartPart.
     */
    public View getPartView()  { return null; }
}
