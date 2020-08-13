package snapcharts.app;
import snap.view.View;
import snap.view.ViewOwner;

/**
 * The base class for DocItem editors.
 */
public class DocItemPane extends ViewOwner {

    // The DocPane that holds this DocItemPane
    private DocPane  _docPane;

    /**
     * Returns the DocPane.
     */
    public DocPane getDocPane()  { return _docPane; }

    /**
     * Sets the DocPane.
     */
    public void setDocPane(DocPane aDP)
    {
        _docPane = aDP;
    }

    /**
     * Returns the view for the DocItem.
     */
    public View getItemView()  { return null; }
}
