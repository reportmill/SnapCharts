package snapcharts.util;
import snap.geom.Line;
import snap.geom.Path2D;
import snap.geom.Point;
import snap.geom.Shape;
import snapcharts.model.DataSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class to return a contour shape for a triangle mesh.
 */
public class ContourMaker {


    public static class Triangle  {
        public int v1;
        public int v2;
        public int v3;
        public int[] vertexs;
        public Triangle(int ind1, int ind2, int ind3)
        {
            v1 = ind1; v2 = ind2; v3 = ind3;
            vertexs = new int[] { v1, v2, v3 };
        }
    }

    public static class Edge {
        public int e1;
        public int e2;
        public Edge(int ind1, int ind2)
        {
            e1 = ind1;
            e2 = ind2;
        }
    }

    /**
     * Return a list of contour lines that have a constant elevation near the specified
     * value. Lines will be truncated to the sepcified x/y range, and the elevation
     * function will be sampled at even intervals determined by the spacing parameter.
     */
    public Shape find_contours(DataSet dset, Triangle[] triangles, double valZ)
    {
        IntArray above = new IntArray();
        IntArray below = new IntArray();

        List<Line> contour_segments = new ArrayList<>(); //List<Edge>
        List<Point> contour_points = new ArrayList<>();

        // Iterate over triangles and find segments for valZ
        for (Triangle triangle : triangles) {

            // Fill above/below arrays with triangle verteces above/below value
            above.clear(); below.clear();
            for (int v : triangle.vertexs) {
                if (dset.getZ(v) < valZ)
                    below.add(v);
                else above.add(v);
            }

            // All above or all below means no contour line here
            if (below.size == 0 || above.size == 0)
                continue;

            // We have a contour line, let's find it
            IntArray minority = above.size < below.size ? above : below;
            IntArray majority = above.size > below.size ? above : below;

            Edge crossedEdge1 = new Edge(minority.items[0], majority.items[0]);
            Edge crossedEdge2 = new Edge(minority.items[0], majority.items[1]);
            Edge[] crossedEdges = { crossedEdge1, crossedEdge2 };

            contour_points.clear();
            for (Edge triangle_edge : crossedEdges) {

                // Get Z val for edge verteces and calc ratio
                double e1zval = dset.getZ(triangle_edge.e1);
                double e2zval = dset.getZ(triangle_edge.e2);
                double how_far = ((valZ - e2zval) / (e1zval - e2zval));

                // Get X/Y values for edge vertexes and interpolate X/Y value for valZ
                double e1xval = dset.getX(triangle_edge.e1);
                double e1yval = dset.getY(triangle_edge.e1);
                double e2xval = dset.getX(triangle_edge.e2);
                double e2yval = dset.getY(triangle_edge.e2);
                double dataX = how_far * e1xval + (1 - how_far) * e2xval;
                double dataY = how_far * e1yval + (1 - how_far) * e2yval;
                contour_points.add(new Point(dataX, dataY));
            }

            Point p0 = contour_points.get(0);
            Point p1 = contour_points.get(1);
            Line line = new Line(p0.x, p0.y, p1.x, p1.y);
            contour_segments.add(line);
        }

        // Create path (just return if no segments)
        Path2D path = new Path2D();
        if (contour_segments.size()==0)
            return path;

        // Remove first segment, append to path, track start point
        Line line = contour_segments.remove(0);
        path.moveTo(line.x0, line.y0);
        path.lineTo(line.x1, line.y1);
        double endX = line.x1;
        double endY = line.y1;

        // Keep iterating until all segments are placed
        while (contour_segments.size()!=0) {
            boolean found = false;
            for (Line line2 : contour_segments) {
                if (Point.equals(endX, endY, line2.x0, line2.y0))
                    path.lineTo(endX = line2.x1, endY = line2.y1);
                else if (Point.equals(endX, endY, line2.x1, line2.y1))
                    path.lineTo(endX = line2.x0, endY = line2.y0);
                else continue;
                found = true;
                contour_segments.remove(line2);
                break;
            }
            if (!found && contour_segments.size()>0) {
                path.close();
                line = contour_segments.remove(0);
                path.moveTo(line.x0, line.y0);
                path.lineTo(endX = line.x1, endY = line.y1);
            }
        }

        // Return path
        return path;
    }
}
