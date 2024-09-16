package snapcharts.app;
import snap.geom.HPos;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * Manages WelcomePanelAnim view.
 */
public class WelcomePanelAnim extends ViewOwner {

    /**
     * Constructor.
     */
    public WelcomePanelAnim()
    {
        super();
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Configure MainTitleText
        TextArea mainTitleText = getView("MainTitleText", TextArea.class);
        mainTitleText.setDefaultTextStyleString("Font: Arial Black 58; CharSpacing: -1.9;");
        mainTitleText.setDefaultLineStyle(mainTitleText.getDefaultLineStyle().copyFor(HPos.CENTER));
        mainTitleText.addChars("SnapCharts");

        // Configure MainTitleText2
        TextArea mainTitleText2 = getView("MainTitleText2", TextArea.class);
        mainTitleText2.setRichText(true);
        mainTitleText2.setDefaultTextStyleString("Font: Arial Black 58; CharSpacing: -1.9;");
        mainTitleText2.setDefaultLineStyle(mainTitleText2.getDefaultLineStyle().copyFor(HPos.CENTER));
        mainTitleText2.addCharsWithStyleString("Snap", "Color: #ff5a5a");
        mainTitleText2.addCharsWithStyleString("Charts", "Color: #bed0ff");

        // Configure TagLineText, TagLineText2
        TextArea tagLineTextArea = getView("TagLineText", TextArea.class);
        tagLineTextArea.setDefaultTextStyleString("Font: Arial Bold 14");
        tagLineTextArea.setDefaultLineStyle(tagLineTextArea.getDefaultLineStyle().copyFor(HPos.CENTER));
        tagLineTextArea.addChars("An app for building charts");
        TextArea tagLineTextArea2 = getView("TagLineText2", TextArea.class);
        tagLineTextArea2.setDefaultTextStyleString("Font: Arial Bold 14; Color: #FF");
        tagLineTextArea2.setDefaultLineStyle(tagLineTextArea2.getDefaultLineStyle().copyFor(HPos.CENTER));
        tagLineTextArea2.addChars("An app for building charts");

        // Configure JVMText
        TextArea jvmText = getView("JVMText", TextArea.class);
        jvmText.setDefaultTextStyleString("Font: Arial Bold 10; Color: #FF");
        jvmText.setDefaultLineStyle(jvmText.getDefaultLineStyle().copyFor(HPos.CENTER));
        jvmText.addChars("JVM: " + (SnapUtils.isTeaVM ? "TeaVM" : System.getProperty("java.runtime.version")));

        // Configure BuildText
        TextArea buildText = getView("BuildText", TextArea.class);
        buildText.setDefaultTextStyleString("Font: Arial Bold 10; Color: #FF");
        buildText.setDefaultLineStyle(buildText.getDefaultLineStyle().copyFor(HPos.CENTER));
        buildText.addChars("Build: " + SnapUtils.getBuildInfo());

        // Configure anim for hammer and screwdriver
        View hammer = getView("Hammer");
        hammer.setAnimString("T:1000; R:40; T:1320; R:-6; T:1640; R:40; T:5000");
        hammer.getAnim(0).setLoops();
        View screwdriver = getView("ScrewDriver");
        screwdriver.setAnimString("T:1760; R:-35; TX:0; TY:0; T:2080; R:0; TX:-9; TY:-14; T:2400; TX:-9; TY:16; R:0; " +
                "T:2640; TY:16; R:0; TX:-9; T:2960; R:0; TX:-9; TY:0; T:3280; R:-35; TX:0; TY:0; T:5000;");
        screwdriver.getAnim(0).setLoops();
    }
}
