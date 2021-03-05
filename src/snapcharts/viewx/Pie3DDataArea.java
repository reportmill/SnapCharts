package snapcharts.viewx;
import snap.geom.Arc;
import snap.geom.Path;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.view.ViewEvent;
import snap.gfx3d.Camera;
import snap.gfx3d.CameraView;
import snap.gfx3d.PathBox3D;
import snap.gfx3d.Scene3D;
import snapcharts.model.DataSet;
import snapcharts.view.ChartHelper;

/**
 * A DataArea subclass to display the contents of 3D a pie chart.
 */
public class Pie3DDataArea extends PieDataArea {

    // The Camera
    protected CameraView _camView;

    // The Camera
    private Camera _camera;

    // The Scene
    private Scene3D _scene;

    /**
     * Constructor.
     */
    public Pie3DDataArea(ChartHelper aChartHelper, DataSet aDataSet)
    {
        super(aChartHelper, aDataSet);

        // Create/add CameraView
        _camView = new CameraView() {
            protected void layoutImpl() { rebuildScene(); }
        };
        addChild(_camView);

        // Get/configure CameraView.Camera
        _camera = _camView.getCamera();
        _camera.setYaw(26);
        _camera.setPitch(10);
        _camera.setDepth(50);
        _camera.setFocalLength(8*72);
        _camera.setAdjustZ(true);

        // Get CameraView.Scene
        _scene = _camView.getScene();

        // Shouldn't need this when CameraView consumes events
        enableEvents(MouseEvents);
        enableEvents(Scroll);
    }

    /**
     * Rebuilds 3D representation of shapes from shapes list.
     */
    protected void rebuildScene()
    {
        // Remove Shape3Ds
        _scene.removeShapes();

        // Iterate over wedges and add them as 3D
        Wedge[] wedges = getWedges();
        for(int i=0; i<wedges.length; i++) {
            Wedge wedge = wedges[i];
            Color color = getDataColor(i);
            addWedgeToScene(wedge, color);
        }

        // Iterate over lines and add them as 3D
        //for(int i=0, iMax=_lines.size(); i<iMax; i++) addChild3D(_lines.get(i), getDepth()/3-5, getDepth()/3-5);

        // Create label shapes
        /*boolean fullRender = true; // !isValueAdjusting()
        for(int i=0, iMax=_labels.size(); i<iMax && fullRender; i++) {
            SGView label = _labels.get(i);
            addShapesForRMShape(label, -5, -5, false);
        }*/
    }

    /**
     * Adds a 3d wedge to scene.
     */
    protected void addWedgeToScene(Wedge aWedge, Color aColor)
    {
        // Get depth, and Z values for back/front
        double depth = Math.round(getWidth()*.12); //_camera.getDepth();
        double z0 = -depth/2;
        double z1 = depth/2;

        // Get wedge arc
        //double reveal = getReveal();
        Arc arc = aWedge.getArc(true, false, false, 1, .5);

        // Create/configure bar path/path3d and add to scene
        Path path = new Path(arc);
        PathBox3D bar = new PathBox3D(path, z0, z1, false);
        bar.setColor(aColor);
        bar.setStroke(Color.BLACK, 1);
        _scene.addShape(bar);
    }

    /**
     * Override to suppress.
     */
    @Override
    protected void paintChart(Painter aPntr)  { }

    /**
     * Override to suppress - shouldn't need this when CameraView consumes.
     */
    @Override
    protected void processEvent(ViewEvent anEvent)
    {
        anEvent.consume();
    }

    /**
     * Override to rebuild chart.
     */
    @Override
    public void setReveal(double aValue)
    {
        super.setReveal(aValue);
        _camView.relayout();
        if (aValue==0) {
            _camView.setYaw(90);
            _camView.setPitch(0);
            _camView.setOffsetZ(200);
            _camView.getAnimCleared(1000).setValue(CameraView.Yaw_Prop,-15);
            _camView.getAnim(1000).setValue(CameraView.Pitch_Prop,-8);
            _camView.getAnim(1000).setValue(CameraView.OffsetZ_Prop,0).setLinear().play();
        }
    }

    /**
     * Override to resize CamView.
     */
    @Override
    protected void layoutImpl()
    {
        double viewW = getWidth();
        double viewH = getHeight();
        _camView.setSize(viewW, viewH);
    }
}
