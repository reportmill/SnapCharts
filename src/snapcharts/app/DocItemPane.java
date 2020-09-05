package snapcharts.app;
import snap.view.View;
import snap.view.ViewOwner;

/**
 * The base class for DocItem editors.
 */
public class DocItemPane extends ViewOwner {

    // The DocPane that holds this DocItemPane
    private DocPane  _docPane;

    // Whether inspector is showing
    private boolean  _showInsp = true;

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

    /**
     * Returns whether inspector is visible.
     */
    public boolean isShowInspector()  { return _showInsp; }

    /**
     * Sets whether inspector is visible.
     */
    public void setShowInspector(boolean aValue)
    {
        // If already set, just return
        if (aValue==isShowInspector()) return;

        // Set value
        _showInsp = aValue;

        // Get inspector
        ViewOwner insp = getInspector(); if (insp==null) return;
        View inspView = insp.getUI();

        // Handle show inspector
        if (aValue) {
            inspView.setVisible(true);
            inspView.getAnimCleared(500).setPrefWidth(300).play();
        }

        // Handle hide inspector
        else {
            inspView.getAnimCleared(500).setPrefWidth(1);
            inspView.getAnim(500).setOnFinish(a -> inspView.setVisible(false)).play();
        }
    }

    /**
     * Returns the inspector.
     */
    public ViewOwner getInspector()  { return null; }
}
