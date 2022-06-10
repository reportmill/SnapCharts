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
    public SliderBlock sliderBlock(Object anObj, KeyChain aKeyChain)
    {
        // Get the variable name
        String title = "Title:";
        double min = 0;
        double max = 100;
        return new SliderBlock(title, min, max);
    }

    /**
     * A ViewOwner subclass to hold a Label, Slider and TextField.
     */
    private static class SliderBlock extends ViewOwner {

        // The title
        private String  _title;

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
        public SliderBlock(String aTitle, double aMin, double aMax)
        {
            _title = aTitle;
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
            _label = new Label(_title);

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
