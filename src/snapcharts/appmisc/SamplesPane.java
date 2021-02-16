package snapcharts.appmisc;

import snap.geom.HPos;
import snap.geom.Pos;
import snap.geom.Size;
import snap.geom.VPos;
import snap.gfx.*;
import snap.util.StringUtils;
import snap.view.*;
import snap.viewx.DialogBox;
import snap.web.WebResponse;
import snap.web.WebURL;
import snapcharts.app.DocPane;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to show samples.
 */
public class SamplesPane extends ViewOwner {
    
    // The DocPane
    private DocPane _docPane;
    
    // The selected index
    private int  _selIndex;
    
    // The dialog box
    private SheetDialogBox  _dbox;
    
    // The shared document names
    private static String     _docNames[];
    
    // The shared document images
    private static Image      _docImages[];
    
    // The shared image paths
    private static String     _imgPaths[];
    
    // Constants
    private static final String SAMPLES_ROOT = "https://reportmill.com/snaptea/SnapChartsSamples/";
    private static final String SAMPLES_EXT = ".charts";
    private static final Effect SHADOW = new ShadowEffect();
    private static final Effect SHADOW_SEL = new ShadowEffect(10, Color.get("#038ec3"), 0, 0);
    
    /**
     * Shows the samples pane.
     */
    public void showSamples(DocPane aDP)
    {
        _docPane = aDP;
        ChildView aView = (ChildView)aDP.getUI();

        _dbox = new SheetDialogBox();
        _dbox.setContent(getUI());
        _dbox.showConfirmDialog(aView);
    }

    /**
     * Called when dialog box closed.
     */
    void dialogBoxClosed()
    {
        if (_dbox._cancelled) return;
        WebURL url = getDocURL(_selIndex);
        _docPane.open(url);
    }

    /**
     * Creates UI.
     */
    protected View createUI()
    {
        // Create main ColView to hold RowViews for samples
        ColView colView = new ColView(); colView.setName("ItemColView");
        colView.setSpacing(25); colView.setPadding(25,15,20,15);
        colView.setAlign(Pos.TOP_CENTER); colView.setFillWidth(true);
        colView.setFill(new Color(.97,.97,1d)); colView.setBorder(Color.GRAY,1);
        colView.setPrefWidth(557);

        // Add loading label
        Label loadLabel = new Label("Loading..."); loadLabel.setFont(Font.Arial16.deriveFont(32).getBold());
        loadLabel.setTextFill(Color.GRAY);
        colView.addChild(loadLabel);

        // Create ScrollView
        ScrollView scroll = new ScrollView(colView); scroll.setPrefHeight(420);
        scroll.setShowHBar(false); scroll.setShowVBar(true);

        // Create "Select template" label
        Label selectLabel = new Label("Select a Chart file:");
        selectLabel.setFont(Font.Arial16.deriveFont(20).getBold());

        // Create HeaderRow to hold SelectLabel
        RowView headerRow = new RowView();
        headerRow.addChild(selectLabel);

        // Create top level col view to hold HeaderRow and ColView
        ColView boxView = new ColView();
        boxView.setSpacing(8); boxView.setFillWidth(true);
        boxView.setChildren(headerRow, scroll);
        return boxView;
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        if (_docNames==null)
            loadIndexFile();
        else buildUI();
    }

    /**
     * Starts loading.
     */
    private void loadIndexFile()
    {
        WebURL url = WebURL.getURL(SAMPLES_ROOT + "index.txt");
        url.getResponseAndCall(resp -> indexFileLoaded(resp));
    }

    /**
     * Loads content.
     */
    private void indexFileLoaded(WebResponse aResp)
    {
        // If response is bogus, report it
        if (aResp.getCode()!=WebResponse.OK) {
            runLater(() -> indexFileLoadFailed(aResp)); return; }

        // Get text and break into lines
        String text = aResp.getText();
        String lines[] = text.split("\\s*\n\\s*");

        // Get names list from lines
        List <String> docNamesList = new ArrayList();
        for (String line : lines) { line = line.trim();
            if (line.length()>0)
                docNamesList.add(line);
        }

        // Get DocNames from list
        _docNames = docNamesList.toArray(new String[docNamesList.size()]);
        _docImages = new Image[_docNames.length];

        // Rebuild UI
        runLater(() -> buildUI());
    }

    /**
     * Loads failure condition.
     */
    private void indexFileLoadFailed(WebResponse aResp)
    {
        // Get error string and TextArea
        String str = "Failed to load index file.\n" + "Response code: " + aResp.getCodeString() + "\n" +
            "Exception: " + aResp.getException();
        TextArea textArea = new TextArea(); textArea.setText(str);

        // Add to ColView
        ColView colView = getView("ItemColView", ColView.class);
        colView.setAlign(Pos.CENTER);
        colView.addChild(textArea);
    }

    /**
     * Loads samples.
     */
    private void buildUI()
    {
        // Get ItemColView and remove children
        ColView colView = getView("ItemColView", ColView.class);
        colView.removeChildren();

        // Create RowViews
        RowView rowView = null;
        for (int i=0; i<_docNames.length; i++) { String name = _docNames[i];

            // Create/add new RowView for every three samples
            if (i%3==0) {
                rowView = new RowView(); rowView.setAlign(Pos.CENTER);
                colView.addChild(rowView);
            }

            // Create ImageViewX for sample
            ImageView iview = new ImageView();
            iview.setPrefSize(getDocSize(i));
            iview.setFill(Color.WHITE);
            iview.setName("ImageView" + i); iview.setEffect(i==0 ? SHADOW_SEL : SHADOW);

            // Create label for sample
            Label label = new Label(name + SAMPLES_EXT);
            label.setFont(Font.Arial13);
            label.setPadding(3,4,3,4);
            label.setLeanY(VPos.BOTTOM);
            if (i==0) {
                label.setFill(Color.BLUE);
                label.setTextFill(Color.WHITE);
            }

            // Create/add ItemBox for Sample and add ImageView + Label
            ColView ibox = new ColView();
            ibox.setPrefSize(175,175);
            ibox.setAlign(Pos.CENTER);
            ibox.setPadding(0,0,8,0);
            ibox.setName("ItemBox" + String.valueOf(i));
            ibox.addEventHandler(e -> itemBoxWasPressed(ibox, e), MousePress);
            ibox.setChildren(iview, label);
            rowView.addChild(ibox);
        }

        // Make sure all row views and image boxes are owned by ui
        for (View child : colView.getChildren())
            child.setOwner(this);

        // Load images
        loadImagesInBackground();
    }

    /**
     * Called when template ItemBox is clicked.
     */
    private void itemBoxWasPressed(ColView anItemBox, ViewEvent anEvent)
    {
        // Get name and index of pressed ItemBox
        String name = anItemBox.getName();
        int index = StringUtils.intValue(name);

        // Set attributes of current selection back to normal
        ColView oldItemBox = getView("ItemBox" + _selIndex, ColView.class);
        oldItemBox.getChild(0).setEffect(SHADOW);
        Label oldLabel = (Label)oldItemBox.getChild(1);
        oldLabel.setFill(null); oldLabel.setTextFill(null);

        // Set attributes of new selection to selected effect
        anItemBox.getChild(0).setEffect(SHADOW_SEL);
        Label newLabel = (Label)anItemBox.getChild(1);
        newLabel.setFill(Color.BLUE); newLabel.setTextFill(Color.WHITE);

        // Set new index
        _selIndex = index;

        // If double-click, confirm dialog box
        if (anEvent.getClickCount()>1) _dbox.confirm();
    }

    /**
     * Returns the number of docs.
     */
    private static int getDocCount()  { return _docNames.length; }

    /**
     * Returns the doc names.
     */
    private static String[] getDocNames()  { return _docNames; }

    /**
     * Returns the doc name at index.
     */
    private static String getDocName(int anIndex)  { return _docNames[anIndex]; }

    /**
     * Returns the doc url at given index.
     */
    private static WebURL getDocURL(int anIndex)
    {
        // Get document name, URL string and URL
        String name = getDocName(anIndex);
        String urls = SAMPLES_ROOT + name + '/' + name + SAMPLES_EXT;
        WebURL url = WebURL.getURL(urls);
        return url;
    }

    /**
     * Returns the doc thumnail image at given index.
     */
    private Image getDocImage(int anIndex)
    {
        // If image already set, just return
        Image img = _docImages[anIndex]; if (img!=null) return img;

        // Get image name, URL string, and URL
        String name = getDocName(anIndex);
        String urls = SAMPLES_ROOT + name + '/' + name + ".png";
        WebURL imgURL = WebURL.getURL(urls);

        // Create Image. Then make sure image is loaded by requesting Image.Native.
        img = _docImages[anIndex] = Image.get(imgURL);
        img.getNative();
        return img;
    }

    /**
     * Returns size of doc at given index.
     */
    private static Size getDocSize(int anIndex)
    {
        return new Size(102,102);
    }

    /**
     * Loads the thumbnail image for each sample in background thread.
     */
    private void loadImagesInBackground()  { new Thread(() -> loadImages()).start(); }

    /**
     * Loads the thumbnail image for each sample in background thread.
     */
    private void loadImages()
    {
        // Iterate over sample names and load/set images
        for (int i=0; i<getDocCount(); i++) { int index = i;
            Image img = getDocImage(i);
            runLater(() -> setImage(img, index));
        }
    }

    /** Called after an image is loaded to set in ImageView in app thread. */
    private void setImage(Image anImg, int anIndex)
    {
        String name = "ImageView" + String.valueOf(anIndex);
        ImageView iview = getView(name, ImageView.class);
        iview.setImage(anImg);
        iview.setPrefSize(-1,-1);
    }

    /**
     * A DialogBox subclass that shows as a sheet.
     */
    private class SheetDialogBox extends DialogBox {

        // The parent view hosting the SheetDialogBox
        ChildView     _hostView;

        // The BoxView to hold/clip the UI
        BoxView       _clipBox;

        // Whether the dialog box was cancelled
        boolean       _cancelled;

        /**
         * Show Dialog in sheet.
         */
        protected boolean showPanel(View aView)
        {
            // Get given view as HostView
            _hostView = aView instanceof ChildView ? (ChildView)aView : null;
            if (_hostView==null) return super.showPanel(aView);

            // Make Other views invisible to mouse clicks
            for (View v : _hostView.getChildren()) v.setPickable(false);

            // Create/configure UI
            View ui = getUI();
            ui.setManaged(false);
            ui.setFill(ViewUtils.getBackFill());
            ui.setBorder(Color.DARKGRAY, 1);
            Size size = ui.getPrefSize();
            ui.setSize(size);

            // Add Shadow
            ShadowEffect shadow = new ShadowEffect(10, Color.DARKGRAY, 0, 0).copySimple();
            ui.setEffect(shadow);

            // Create box to hold/clip UI
            _clipBox = new BoxView(ui);
            _clipBox.setSize(size.width + 10, size.height + 10);
            _clipBox.setPadding(0, 10, 10, 10);
            _clipBox.setManaged(false);
            _clipBox.setLeanX(HPos.CENTER);
            _clipBox.setClipToBounds(true);

            // Add UI box to HostView
            _hostView.addChild(_clipBox);

            // Configure UI to animate in and start
            ui.setTransY(-size.height);
            ui.getAnim(1000).setTransY(-1).play();

            // Make sure stage and Builder.FirstFocus are focused
            runLater(() -> notifyDidShow());

            return true;
        }

        /**
         * Hide dialog.
         */
        protected void hide()
        {
            // Configure UI to animate out and start
            View ui = getUI();
            ViewAnim anim = ui.getAnimCleared(1000);
            anim.setTransY(-ui.getHeight());
            anim.setOnFinish(() -> hideAnimDone()).needsFinish().play();
        }

        /**
         * Called when hide() animation finishes.
         */
        private void hideAnimDone()
        {
            // Remove UI, reset everything pickable and notify of close
            _hostView.removeChild(_clipBox);
            for (View v : _hostView.getChildren()) v.setPickable(true);
            dialogBoxClosed();
        }

        /** Override to set cancelled flag. */
        public void confirm()  { _cancelled = false; hide(); }

        /** Override to set cancelled flag. */
        public void cancel()  { _cancelled = true; hide(); }
    }

    /**
     * Returns the image paths.
     */
    public static String[] getImagePaths()
    {
        if (_imgPaths!=null) return _imgPaths;

        WebURL url = WebURL.getURL(SAMPLES_ROOT + "images/index.txt");
        String pathsStr = url.getText();
        String pathLines[] = pathsStr.split("\\s*\n\\s*");
        List <String> pathsList = new ArrayList();
        for (String line : pathLines) {
            if (line.length()>0)
                pathsList.add(line);
        }
        String paths[] = pathsList.toArray(new String[pathsList.size()]);
        return _imgPaths = paths;
    }

    /**
     * Returns an image path for given name.
     */
    public static String getImagePathForName(String aName)
    {
        String name = aName;
        int ind = name.lastIndexOf('/'); if (ind>=0) name = name.substring(ind+1);

        for (String path : getImagePaths()) {
            if (path.endsWith(name))
                return path;
        }
        return null;
    }

    /**
     * A ViewArchiver that looks for images in samples dir.
     */
    private static class SamplesViewArchiver extends ViewArchiver {

        /**
         * Override to look for images in samples dir.
         */
        public Image getImage(String aPath)
        {
            String path = getImagePathForName(aPath);
            if (path!=null) {
                String urls = SAMPLES_ROOT + "images/" + path;
                WebURL url = WebURL.getURL(urls);
                return Image.get(url);
            }

            // Do normal version
            return super.getImage(aPath);
        }
    }
}