/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.geom.HPos;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.VPos;
import snap.util.PropChange;
import snap.util.Range;
import snap.util.StringUtils;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.model.ChartPart;

/**
 * This class provides UI editing for miscellaneous ChartPart properties.
 */
public class MiscInsp extends ViewOwner {

    // The ChartPane
    private ChartPane  _chartPane;

    // Points of interest
    public static final String POI_FirstPoint = "First Point";
    public static final String POI_LastPoint = "Last Point";
    public static final String POI_LocalMin = "Local Min";
    public static final String POI_LocalMax = "Local Max";
    public static final String POI_SlopeMin = "Slope Min";
    public static final String POI_SlopeMax = "Slope Max";

    // POI array
    public static String[] ALL_POI = { POI_FirstPoint, POI_LastPoint, POI_LocalMin, POI_LocalMax, POI_SlopeMin, POI_SlopeMax };

    /**
     * Constructor.
     */
    public MiscInsp(ChartPane aChartPane)
    {
        super();
        _chartPane = aChartPane;
    }

    @Override
    protected void initUI()
    {
        ListView<String> listView = getView("ListView", ListView.class);
        listView.setItems(ALL_POI);

        // Register MarginText, PadText to update
        getView("MarginText").addPropChangeListener(pc -> insetsTextFieldChanged(pc),
                TextField.Sel_Prop, View.Focused_Prop);
        getView("PadText").addPropChangeListener(pc -> insetsTextFieldChanged(pc),
                TextField.Sel_Prop, View.Focused_Prop);
    }

    /**
     * Update UI.
     */
    @Override
    protected void resetUI()
    {
        // Get selected ChartPart
        ChartPart selPart = _chartPane.getSelChartPart();

        // Update AlignLeftButton, AlignCenterButton, AlignRightButton
        Pos align = selPart.getAlign();
        HPos alignX = align.getHPos();
        setViewValue("AlignLeftButton", alignX == HPos.LEFT);
        setViewValue("AlignCenterButton", alignX == HPos.CENTER);
        setViewValue("AlignRightButton", alignX == HPos.RIGHT);

        // Update AlignTopButton, AlignMiddleButton, AlignBottomButton
        VPos alignY = align.getVPos();
        setViewValue("AlignTopButton", alignY == VPos.TOP);
        setViewValue("AlignMiddleButton", alignY == VPos.CENTER);
        setViewValue("AlignBottomButton", alignY == VPos.BOTTOM);

        // Update MarginText, PadText, SpacingText
        setViewValue("MarginLabel", getInsetsSelStringForTextFieldName("MarginText"));
        setViewValue("MarginText", getInsetsString(selPart.getMargin()));
        setViewValue("PadLabel", getInsetsSelStringForTextFieldName("PadText"));
        setViewValue("PadText", getInsetsString(selPart.getPadding()));
        setViewValue("SpacingText", selPart.getSpacing());
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Get selected ChartPart
        ChartPart selPart = _chartPane.getSelChartPart();

        // Handle AlignLeftButton, AlignCenterButton, AlignRightButton
        if (anEvent.equals("AlignLeftButton")) selPart.setAlignX(HPos.LEFT);
        if (anEvent.equals("AlignCenterButton")) selPart.setAlignX(HPos.CENTER);
        if (anEvent.equals("AlignRightButton")) selPart.setAlignX(HPos.RIGHT);

        // Handle AlignTopButton, AlignMiddleButton, AlignBottomButton
        if (anEvent.equals("AlignTopButton")) selPart.setAlignY(VPos.TOP);
        if (anEvent.equals("AlignMiddleButton")) selPart.setAlignY(VPos.CENTER);
        if (anEvent.equals("AlignBottomButton")) selPart.setAlignY(VPos.BOTTOM);

        // Handle MarginText, PadText, SpacingText
        if (anEvent.equals("MarginText"))
            selPart.setMargin(Insets.get(anEvent.getStringValue()));
        if (anEvent.equals("PadText"))
            selPart.setPadding(Insets.get(anEvent.getStringValue()));
        if (anEvent.equals("SpacingText"))
            selPart.setSpacing(anEvent.getFloatValue());

        // Handle MarginText, MarginAdd1Button, MarginSub1Button, MarginResetButton
        if (anEvent.equals("MarginText")) selPart.setMargin(Insets.get(anEvent.getStringValue()));
        if (anEvent.equals("MarginAdd1Button")) adjustMargin(selPart, 1);
        if (anEvent.equals("MarginSub1Button")) adjustMargin(selPart, -1);
        if (anEvent.equals("MarginResetButton")) selPart.setMargin(ChartPart.DEFAULT_MARGIN);

        // Handle PadText, PadAdd1Button, PadSub1Button, PadResetButton
        if (anEvent.equals("PadText")) selPart.setPadding(Insets.get(anEvent.getStringValue()));
        if (anEvent.equals("PadAdd1Button")) adjustPadding(selPart, 1);
        if (anEvent.equals("PadSub1Button")) adjustPadding(selPart, -1);
        if (anEvent.equals("PadResetButton")) selPart.setPadding(ChartPart.DEFAULT_PADDING);

        // Handle SpacingText, SpacingAdd5Button, SpacingResetButton
        if (anEvent.equals("SpacingText")) selPart.setSpacing(anEvent.getFloatValue());
        if (anEvent.equals("SpacingAdd1Button")) selPart.setSpacing(selPart.getSpacing()+1);
        if (anEvent.equals("SpacingSub1Button")) selPart.setSpacing(selPart.getSpacing()-1);
        if (anEvent.equals("SpacingResetButton")) selPart.setSpacing(0);
    }

    /**
     * Adjust margin for given view by given amount (based on textfield selection).
     */
    private void adjustMargin(ChartPart aPart, double aVal)
    {
        Insets ins = getAdjustedInsetsForTextFieldName("MarginText", aVal);
        aPart.setMargin(ins);
    }

    /**
     * Adjust padding for given view by given amount (based on textfield selection).
     */
    private void adjustPadding(ChartPart aPart, double aVal)
    {
        Insets ins = getAdjustedInsetsForTextFieldName("PadText", aVal);
        aPart.setPadding(ins);
    }

    /**
     * Returns the adjusted insets for given TextField name and value.
     */
    private Insets getAdjustedInsetsForTextFieldName(String aName, double aVal)
    {
        // Get TextField and insets
        TextField text = getView(aName, TextField.class);
        Insets ins = Insets.get(text.getText());

        // Get range
        Range range = getInsetsSelRangeForTextFieldName(aName);
        if(range==null) range = new Range(0,3);

        // Adjust range
        for(int i=range.start;i<=range.end;i++) {
            switch(i) {
                case 0: ins.top = Math.max(ins.top + aVal, 0); break;
                case 1: ins.right = Math.max(ins.right + aVal, 0); break;
                case 2: ins.bottom = Math.max(ins.bottom + aVal, 0); break;
                case 3: ins.left = Math.max(ins.left + aVal, 0); break;
            }
        }

        // Return insets
        return ins;
    }

    /**
     * Returns the selected range for insets string.
     */
    private Range getInsetsSelRangeForTextFieldName(String aName)
    {
        // Get text field (just return null if not focused)
        TextField text = getView(aName, TextField.class);
        if(!text.isFocused()) return null;

        // Get string and textfield sel start/end
        String str = text.getText();
        int selStart = text.getSelStart(), selEnd = text.getSelEnd();

        // Using commas as landmarks for insets fields return range of selection
        int start = countCommas(str, selStart);
        int end = countCommas(str, selEnd);
        return new Range(start, end);
    }

    /**
     * Returns the selected range for insets string.
     */
    private String getInsetsSelStringForTextFieldName(String aName)
    {
        Range range = getInsetsSelRangeForTextFieldName(aName);
        if(range==null) return "";
        StringBuffer sb = new StringBuffer();
        for(int i=range.start; i<=range.end; i++) {
            switch(i) {
                case 0: sb.append("top, "); break;
                case 1: sb.append("right, "); break;
                case 2: sb.append("bottom, "); break;
                case 3: sb.append("left, "); break;
            }
        }
        sb.delete(sb.length()-2, sb.length());
        return sb.toString();
    }

    /**
     * Returns a string representation of this Insets.
     */
    private String getInsetsString(Insets anIns)
    {
        String t = StringUtils.toString(anIns.top), r = StringUtils.toString(anIns.right);
        String b = StringUtils.toString(anIns.bottom), l = StringUtils.toString(anIns.left);
        return t + ", " + r + ", " + b + ", " + l;
    }

    /**
     * Called when MarginText or PadText change focus or selection to update mini-label.
     */
    private void insetsTextFieldChanged(PropChange aPC)
    {
        TextField text = (TextField)aPC.getSource();
        String name = text.getName(), name2 = name.replace("Text", "Label");
        Label label = getView(name2, Label.class);
        label.setText(getInsetsSelStringForTextFieldName(name));
    }

    /** Returns the number of commas in given string up to given index. */
    private int countCommas(String aStr, int anInd)
    {
        int cc = 0; for(int i=0;i<anInd;i++) if(aStr.charAt(i)==',') cc++;
        return cc;
    }
}
