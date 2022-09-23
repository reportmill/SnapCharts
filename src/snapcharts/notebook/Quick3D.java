/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx3d.Camera;
import snap.gfx3d.CameraView;
import snap.gfx3d.PathBox3D;
import snap.gfx3d.Scene3D;

/**
 * Utility methods to create basic 3D quickly.
 */
public class Quick3D {

    /**
     * Create a Cube quickly.
     */
    public static CameraView createCube()
    {
        // Create/configure CameraView
        CameraView cameraView = new CameraView();
        cameraView.setPrefSize(360, 240);
        cameraView.setFill(Color.WHITE);
        cameraView.setShowCubeView(true);

        // Configure camera
        Camera camera = cameraView.getCamera();
        camera.setYaw(-30);
        camera.setPitch(30);

        // Add a simple rect path
        Shape path = new Rect(100, 100, 100, 100);
        PathBox3D pathBox3D = new PathBox3D(path, 0, 100);
        pathBox3D.setColor(Color.GREEN);
        pathBox3D.setStroke(Color.BLACK, 1);

        // Add to scene
        Scene3D scene3D = cameraView.getScene();
        scene3D.addChild(pathBox3D);

        // Return
        return cameraView;
    }
}
