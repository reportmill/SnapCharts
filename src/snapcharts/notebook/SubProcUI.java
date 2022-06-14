/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.geom.Pos;
import snap.util.KeyChain;
import snap.view.*;

/**
 * This class processes UI requests.
 */
public class SubProcUI extends SubProc {

    /**
     * Constructor.
     */
    public SubProcUI(Processor aProcessor)
    {
        super(aProcessor);
    }

    /**
     * Handles processing functions supported by this class.
     */
    public Object getValueFunctionCall(Object aRoot, Object anObj, String functionName, KeyChain argListKC)
    {
        switch (functionName) {

            case "slider": return sliderBlock(anObj, argListKC);
        }

        return null;
    }

    /**
     * Returns a slider block.
     */
    public SliderBlock sliderBlock(Object anObj, KeyChain argListKey)
    {
        int argCount = argListKey.getChildCount();

        // Get the variable name
        String varName = "x";
        if (argCount > 0) {
            KeyChain varNameKey = argListKey.getChildKeyChain(0);
            if (varNameKey.getOp() == KeyChain.Op.Key)
                varName = varNameKey.getValueString();
        }

        // Get min
        Double minVal = getDoubleValueForArgListArg(anObj, argListKey, 1);
        double min = minVal != null ? minVal : 0;

        // Get max
        Double maxVal = getDoubleValueForArgListArg(anObj, argListKey, 2);
        double max = maxVal != null ? maxVal : 100;

        // Create/return SliderBlock
        return new SliderBlock(varName, min, max);
    }

    /**
     * A ViewOwner subclass to hold a Label, Slider and TextField.
     */
    private static class SliderBlock extends ViewOwner {

        // The variable name
        private String  _varName;

        // The Slider min/max
        private double  _min, _max;

        // The current value
        private double  _value;

        // The label
        private Label  _label;

        // The Slider
        private Slider  _slider;

        // The TextField
        private TextField  _textField;

        /**
         * Constructor.
         */
        public SliderBlock(String aVarName, double aMin, double aMax)
        {
            _varName = aVarName;
            _min = aMin;
            _max = aMax;
        }

        /**
         * Create UI.
         */
        @Override
        protected View createUI()
        {
            // Create label
            _label = new Label(_varName + ':');

            // Create Slider
            _slider = new Slider();
            _slider.setMin(_min);
            _slider.setMax(_max);
            _slider.setGrowWidth(true);

            // Create TextField
            _textField = new TextField();
            _textField.setAlign(Pos.CENTER);
            _textField.setColCount(6);

            // Create/config RowView
            RowView rowView = new RowView();
            rowView.setPadding(20, 20, 20, 20);
            rowView.setPrefWidth(340);
            rowView.setChildren(_label, _slider, _textField);

            // Return
            return rowView;
        }

        /**
         * Reset UI.
         */
        @Override
        protected void resetUI()
        {
            setViewValue(_slider, _value);
            setViewValue(_textField, _value);
        }

        /**
         * Respond UI.
         */
        @Override
        protected void respondUI(ViewEvent anEvent)
        {
            if (anEvent.equals(_slider) || anEvent.equals(_textField))
                _value = anEvent.getFloatValue();
        }
    }
}
