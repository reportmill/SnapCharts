package snapcharts.model;

/**
 * A ChartTypeProps subclass for Contour chart properties.
 */
public class ContourProps extends ChartTypeProps {

    // The number of levels of contours
    private int  _levelCount = 16;

    // Whether to show mesh
    private boolean  _showMesh;

    // Constants for properties
    public final String LevelCount_Prop = "LevelCount";
    public final String ShowMesh_Prop = "ShowMesh";

    /**
     * Returns the number of levels of contours.
     */
    public int getLevelCount()  { return _levelCount; }

    /**
     * Sets the number of levels of contours.
     */
    public void setLevelCount(int aValue)
    {
        if (aValue == getLevelCount()) return;
        firePropChange(LevelCount_Prop, _levelCount, _levelCount = aValue);
    }

    /**
     * Returns whether to show mesh.
     */
    public boolean isShowMesh()  { return _showMesh; }

    /**
     * Sets whether to show mesh.
     */
    public void setShowMesh(boolean aValue)
    {
        if (aValue == isShowMesh()) return;
        firePropChange(ShowMesh_Prop, _showMesh, _showMesh = aValue);
    }
}
