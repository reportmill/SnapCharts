package snapcharts.viewx;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.PropChange;
import snapcharts.model.*;
import snapcharts.modelx.BarStyle;
import snapcharts.view.ChartHelper;
import snapcharts.view.DataArea;

/**
 * A DataArea subclass to display the contents of bar chart.
 */
public class BarDataArea extends DataArea {

    // The BarStyle
    private BarStyle  _barStyle;

    // The number of datasets to display
    protected int  _dsetCount;

    // The number of values in dataset(s)
    protected int  _pointCount;

    // The cached sections
    private Section[]  _sections;

    /**
     * Constructor.
     */
    public BarDataArea(ChartHelper aChartHelper, DataSet aDataSet)
    {
        super(aChartHelper, aDataSet);

        // Add top so top axis line isn't on edge
        //setPadding(5,0,0,0);
    }

    /**
     * Returns the BarStyle.
     */
    public BarStyle getBarStyle()
    {
        if (_barStyle != null) return _barStyle;
        return _barStyle = getChart().getDataStyleHelper().getBarStyle();
    }

    /**
     * Returns the cached section (and section bars) objects.
     */
    protected Section[] getSections()
    {
        // If recalc not needed, just return
        DataSetList dataSetList = getDataSetList();
        DataSet[] dataSets = dataSetList.getEnabledDataSets();
        int dsetCount = dataSets.length;
        int pointCount = dataSetList.getPointCount();
        if (_sections != null && _sections.length == pointCount && _dsetCount == dsetCount)
            return _sections;

        // Get DataAreaBar info
        BarStyle barStyle = getBarStyle();
        double groupPad = barStyle.getGroupPadding();
        double barPad = barStyle.getBarPadding();
        double viewHeight = getHeight();
        boolean colorDataSets = !barStyle.isColorValues();

        // Get number of datasets, points and section width
        _dsetCount = dsetCount;
        _pointCount = pointCount;
        double sectionWidth = getWidth()/_pointCount;

        // Get group widths
        double groupWidthRatio = 1 - groupPad*2;
        double groupWidth = groupWidthRatio>=0 ? groupWidthRatio*sectionWidth : 1;
        double groupPadWidth = (sectionWidth - groupWidth)/2;

        // Get width of individual bar (bar count + bar spaces + bar&space at either end)
        double barWidthRatio = 1 - barPad*2;
        double barWidth = barWidthRatio>=0 ? barWidthRatio*groupWidth/ _dsetCount : 1;
        double barPadWidth = barWidthRatio>=0 ? barPad*groupWidth/ _dsetCount : 1;

        // Create new bars array
        Section[] sections = new Section[pointCount];

        // Iterate over sections
        for (int i=0;i<_pointCount;i++) {

            // Create/set new section and section.bars
            Section section = sections[i] = new Section(i*sectionWidth, 0, sectionWidth, viewHeight);
            section.bars = new Bar[_dsetCount];

            // Iterate over datasets
            for (int j = 0; j< _dsetCount; j++) {

                // Get data point
                DataSet dataSet = dataSets[j];
                DataSetPoint dataPoint = dataSet.getPoint(i);
                double dataY = dataPoint.getY();
                double dispY = dataToViewY(dataY);

                // Draw bar
                DataStyle dataStyle = dataSet.getDataStyle();
                Color color = colorDataSets ? dataStyle.getLineColor() : getColorMapColor(i);
                double barX = i*sectionWidth + groupPadWidth + (j*2+1)*barPadWidth + j*barWidth;
                double barHeight = viewHeight - dispY;
                section.bars[j] = new Bar(dataPoint, barX, dispY, barWidth, barHeight, color);
            }
        }

        // Return sections
        return _sections = sections;
    }

    /**
     * Clears the sections when needed (change of data, size)
     */
    protected void clearSections()
    {
        _sections = null;
        repaint();
    }

    /**
     * Paints chart.
     */
    protected void paintDataArea(Painter aPntr)
    {
        // Get selected point index (section index)
        DataSetPoint dataPoint = getChartView().getTargDataPoint();
        int selIndex = dataPoint!=null ? dataPoint.getIndex() : -1;

        double viewW = getWidth();
        double viewH = getHeight();
        Section[] sections = getSections();

        // If reveal is not full (1) then clip
        if (getReveal() < 1) {
            aPntr.save();
            aPntr.clipRect(0,viewH*(1-getReveal()), viewW,viewH*getReveal());
        }

        // Iterate over sections
        for (int i=0; i<_pointCount; i++) { Section section = sections[i];

            // If selected section, draw background
            if (i == selIndex) {
                aPntr.setColor(Color.get("#4488FF09"));
                aPntr.fillRect(i*section.width, 0, section.width, viewH);
            }

            // Iterate over datasets and draw bars
            for (int j=0; j<_dsetCount; j++) { Bar bar = section.bars[j];
                aPntr.setColor(bar.color);
                aPntr.fillRect(bar.x, bar.y, bar.width, bar.height - .5);
            }
        }

        // If reveal not full, resture gstate
        if (getReveal() < 1) aPntr.restore();
    }

    /**
     * Returns the data point best associated with given x/y (null if none).
     * @return
     */
    @Override
    public DataSetPoint getDataPointForLocalXY(double aX, double aY)
    {
        // Get sections array
        Section[] sections = getSections();

        // Iterate over sections (points) and bars (dataset) and if bar contains point, return data point
        for (int i = 0; i < _pointCount; i++) { Section section = sections[i];
            for (int j = 0; j < _dsetCount; j++) { Bar bar = section.bars[j];
                if (bar.contains(aX, aY))
                    return bar.point;
            }
        }

        // Return null since bar not found for point
        return null;
    }

    /**
     * Returns the given data point X/Y in this view coords.
     * @param aDP
     */
    @Override
    public Point getLocalXYForDataPoint(DataSetPoint aDP)
    {
        // Get sections array
        Section[] sections = getSections();

        // Iterate over sections (points) and bars (dataset) and if bar contains point, return data point
        for (int i=0; i<_pointCount; i++) { Section section = sections[i];
            for (int j=0; j<_dsetCount; j++) { Bar bar = section.bars[j];
                if (bar.point == aDP) {
                    double dispX = Math.round(bar.x + bar.width/2);
                    double dispY = Math.round(bar.y);
                    return new Point(dispX, dispY);
                }
            }
        }

        // Return null since bar not found for point
        return null;
    }

    /**
     * Called when a ChartPart changes.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Handle Data changes
        Object src = aPC.getSource();
        if (src instanceof DataSet || src instanceof DataSetList || src instanceof Axis) {
            clearSections();
        }
    }

    /**
     * Override to clear sections.
     */
    @Override
    public void setWidth(double aValue)
    {
        if (aValue==getWidth()) return;
        super.setWidth(aValue);
        clearSections();
    }

    /**
     * Override to clear sections.
     */
    @Override
    public void setHeight(double aValue)
    {
        if (aValue==getHeight()) return;
        super.setHeight(aValue);
        clearSections();
    }

    /**
     * A class to hold section information.
     */
    protected class Section {

        // Points
        double x, y, width, height;
        Bar bars[];

        /** Creates a Section. */
        public Section(double aX, double aY, double aW, double aH)
        {
            x = aX; y = aY; width = aW; height = aH;
        }

        /** Returns whether section contains point. */
        public boolean contains(double aX, double aY)
        {
            return Rect.contains(x, y, width, height, aX, aY);
        }
    }

    /**
     * A class to hold bar information.
     */
    protected class Bar {

        // Points
        DataSetPoint point;
        double x, y, width, height;
        Color color;

        /** Creates a bar. */
        public Bar(DataSetPoint aDP, double aX, double aY, double aW, double aH, Color aColor)
        {
            point = aDP;
            x = aX; y = aY; width = aW; height = aH;
            color = aColor;
        }

        /** Returns whether bar contains point. */
        public boolean contains(double aX, double aY)
        {
            return Rect.contains(x, y, width, height, aX, aY);
        }
    }
}