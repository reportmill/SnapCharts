package snapcharts.util;
import snap.geom.Path2D;
import snap.geom.Point;
import snap.geom.Shape;
import snapcharts.model.DataSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to represent a Triangle mesh.
 */
public class Mesh {

    // The DataSet
    private DataSet  _dset;

    // The triangles
    private Triangle[]  _triangles;

    // A map of edges
    private Map<Long,Edge>  _edges = new HashMap<>();

    /**
     * Constructor to create mesh for given DataSet and array of triangle vertex indexes.
     */
    public Mesh(DataSet aDataSet, int[] triangles)
    {
        _dset = aDataSet;

        // Get number of triangles
        int pointCount = triangles.length;
        int triangleCount = pointCount / 3;

        // Create triangles array
        _triangles = new Triangle[triangleCount];

        // Iterate over triangle index triplets
        for (int i=0; i<triangleCount; i++) {
            int v1 = triangles[i*3];
            int v2 = triangles[i*3+1];
            int v3 = triangles[i*3+2];
            _triangles[i] = new Triangle(v1, v2, v3);
        }
    }

    /**
     * Return a list of contour lines that have a constant elevation near the specified
     * value. Lines will be truncated to the sepcified x/y range, and the elevation
     * function will be sampled at even intervals determined by the spacing parameter.
     */
    public Shape getContourShape(double valZ)
    {
        // Get line segments
        List<Isoline> isolines = getIsolines(valZ);

        // Create path (just return if no segments)
        Path2D path = new Path2D();
        if (isolines.size() == 0)
            return path;

        // Remove first isoline, append to path, track start point
        Isoline iso = isolines.remove(0);
        path.moveTo(iso.point1.x, iso.point1.y);
        path.lineTo(iso.point2.x, iso.point2.y);
        double endX = iso.point2.x;
        double endY = iso.point2.y;

        // Keep iterating until all isolines are placed
        while (isolines.size() != 0) {
            boolean found = false;
            for (Isoline iso2 : isolines) {
                if (Point.equals(endX, endY, iso2.point1.x, iso2.point1.y))
                    path.lineTo(endX = iso2.point2.x, endY = iso2.point2.y);
                else if (Point.equals(endX, endY, iso2.point2.x, iso2.point2.y))
                    path.lineTo(endX = iso2.point1.x, endY = iso2.point1.y);
                else continue;
                found = true;
                isolines.remove(iso2);
                break;
            }
            if (!found && isolines.size() > 0) {
                path.close();
                iso = isolines.remove(0);
                path.moveTo(iso.point1.x, iso.point1.y);
                path.lineTo(endX = iso.point2.x, endY = iso.point2.y);
            }
        }

        // Return path
        return path;
    }

    /**
     * Returns an array of isolines for given Z alue.
     */
    public List<Isoline> getIsolines(double valZ)
    {
        // A couple of int arrays to hold indexes above/below given threshold Z value for each triangle
        IntArray above = new IntArray();
        IntArray below = new IntArray();

        // Create array of Segments to hold
        List<Isoline> isolines = new ArrayList<>();

        // Iterate over triangles and find segments for valZ
        for (Triangle triangle : _triangles) {

            // Fill above/below arrays with triangle verteces above/below value
            above.clear(); below.clear();
            for (int v : triangle.vertices) {
                if (_dset.getZ(v) < valZ)
                    below.add(v);
                else above.add(v);
            }

            // All above or all below means no contour line here
            if (below.size == 0 || above.size == 0)
                continue;

            // Recategorize above/below arrays as minority/majority
            IntArray minority = above.size < below.size ? above : below;
            IntArray majority = above.size > below.size ? above : below;

            // Get edges hit by valZ
            Edge edge1 = getEdge(minority.items[0], majority.items[0]);
            Edge edge2 = getEdge(minority.items[0], majority.items[1]);

            // Get points at either end of contour lines
            Point point1 = edge1.getPointAlongEdgeForZ(valZ);
            Point point2 = edge2.getPointAlongEdgeForZ(valZ);

            // Create/add line
            Isoline isoline = new Isoline(triangle, edge1, point1, edge2, point2);
            isolines.add(isoline);
        }

        // Return segments
        return isolines;
    }

    /**
     * Returns an edge for given vertex indexes.
     */
    public Edge getEdge(int index1, int index2)
    {
        int min = Math.min(index1, index2);
        int max = Math.max(index1, index2);
        Long hash = ((long) min) << 32 + max;
        Edge edge = _edges.get(hash);
        if (edge==null)
            _edges.put(hash, edge = new Edge(min, max));
        return edge;
    }

    /**
     * A class to represent a triangle in the mesh (as 3 vertex indexes).
     */
    public class Triangle {

        // Triangle vertices
        public int v1, v2, v3;

        // Vertices array
        public int[] vertices;

        // Triangle edges
        public Edge e1, e2, e3;

        /**
         * Constructor.
         */
        public Triangle(int ind1, int ind2, int ind3)
        {
            // Set vertices
            v1 = ind1; v2 = ind2; v3 = ind3;
            vertices = new int[] { v1, v2, v3 };

            // Get/set edges
            e1 = getEdge(v1, v2);
            e2 = getEdge(v2, v3);
            e3 = getEdge(v3, v1);
        }
    }

    /**
     * A class to represent a triangle edge (as two vertex indexes).
     */
    public class Edge {

        // The edge vertexes (indexes into DataSet records)
        public int v1, v2;

        /**
         * Constructor.
         */
        public Edge(int ind1, int ind2)
        {
            v1 = ind1; v2 = ind2;
        }

        /**
         * Returns an X/Y point along edge for given DataSet Z value.
         */
        public Point getPointAlongEdgeForZ(double valZ)
        {
            // Get Z val for edge vertices and calc ratio
            double e1zval = _dset.getZ(v1);
            double e2zval = _dset.getZ(v2);
            double how_far = ((valZ - e2zval) / (e1zval - e2zval));

            // Get X/Y values for edge vertexes and interpolate X/Y value for valZ
            double e1xval = _dset.getX(v1);
            double e1yval = _dset.getY(v1);
            double e2xval = _dset.getX(v2);
            double e2yval = _dset.getY(v2);
            double dataX = how_far * e1xval + (1 - how_far) * e2xval;
            double dataY = how_far * e1yval + (1 - how_far) * e2yval;
            return new Point(dataX, dataY);
        }
    }

    /**
     * A class to represent a line through two triangle edges for a contour value.
     */
    public class Isoline {

        // The Triangle
        public Triangle  triangle;

        // The Edges
        public Edge  edge1, edge2;

        // The points
        public Point point1, point2;

        /**
         * Constructor.
         */
        public Isoline(Triangle aTriangle, Edge anEdge1, Point aPoint1, Edge anEdge2, Point aPoint2)
        {
            triangle = aTriangle;
            edge1 = anEdge1; point1 = aPoint1;
            edge2 = anEdge2; point2 = aPoint2;
        }
    }
}
