package CacheTree;

import model.Geotag;
import play.Play;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Tree {
    private final static float EPSILON = 0.001f;

    private class Node {
        boolean exists = true;
        private Geotag g = null;
        Node left = null;
        Node right = null;

        Node(Geotag g){
            this.g = g;
        }
        public float getLat () { return this.g.lat; }
        public float getLon () { return this.g.lon; }
        public Geotag getGeo () { return this.g; }
        public boolean between (float minLat, float minLon, float maxLat, float maxLon) {
            return (maxLat >= getLat()) && (maxLon >= getLon()) && (minLat <= getLat()) && (minLon <= getLon());
        }
    }

    private static boolean equal(Geotag a, Geotag b) {
        return (Math.abs(a.lat-b.lat)/(Math.abs(a.lat)+ Math.abs(b.lat))) < EPSILON && (Math.abs(a.lon-b.lon)/(Math.abs(a.lon)+ Math.abs(b.lon))) < EPSILON;
    }

    private static boolean equal(float a, float b){
        return (Math.abs(a-b)/(Math.abs(a)+ Math.abs(b))) < EPSILON;
    }

    private static float distance(Geotag a, float lat, float lon) {
        return (float) Math.sqrt( Math.pow(lat - a.lat, 2) + Math.pow(lon - a.lon, 2));
    }

    private Node root;

    private List<Geotag> found; //se usa en rangeSearch

    private Geotag minP; //se usa en get closest

    private float minDistance;  //se usa en get closest

    public Tree (Geotag[] nodes) {
        root = build(0, nodes);
    }

    public void addGeotag(Geotag g){
        _addGeotag(0, root, g);
    }

    public Geotag findById(long id){
        Geotag g = Geotag.find.byId(id);
        if (g == null)
            return null;
        return getClosest(g.lat, g.lon);
    }

    public boolean indexed(float lat, float lon) {
        return _indexed(0, root, lat, lon);
    }

    public boolean delete(float lat, float lon) {
        return _delete(0, root, lat, lon);
    }

    public Geotag getClosest (float lat, float lon){
        if (root.exists) {
            minP = root.getGeo();
            minDistance = distance(root.getGeo(), lat, lon);
        } else {
            minP = null;
            minDistance = 100000000000f;
        }
        _close(0, root, lat, lon);
        return minP;
    }

    public List<Geotag> rangeSearch(float minLat, float minLon, float maxLat, float maxLon) {
        found = new ArrayList<Geotag>();
        _rangeSearch(0, root, minLat, minLon, maxLat, maxLon);
        if (found.size() > 0)
            return found;
        else
            return null;
    }

    private void _rangeSearch(int depth, Node root, float minLat, float minLon, float maxLat, float maxLon) {
        float cmp = 0.0f, cmpMin = 0.0f, cmpMax = 0.0f;
        if (depth % 2 == 0) {
            cmp = root.getLat();
            cmpMin = minLat;
            cmpMax = maxLat;
        } else {
            cmp = root.getLon();
            cmpMin = minLon;
            cmpMax = maxLon;
        }

        if ((cmp > cmpMin) && (cmp > cmpMax) && (root.right!=null))
            _rangeSearch(depth+1, root.right, minLat, minLon, maxLat, maxLon);
        else if ((cmp > cmpMin) && (cmp < cmpMax)) {
            if (root.right != null)
                _rangeSearch(depth+1, root.right, minLat, minLon, maxLat, maxLon);
            if (root.left != null)
                _rangeSearch(depth+1, root.left, minLat, minLon, maxLat, maxLon);
        } else if ((cmp < cmpMin) && (cmp < cmpMax) && (root.left!=null))
            _rangeSearch(depth+1, root.left, minLat, minLon, maxLat, maxLon);

        if (root.between(minLat, minLon, maxLat, maxLon) && root.exists)
            found.add(root.getGeo());
    }

    private boolean _delete(int depth, Node sub, float lat, float lon){
        if(equal(sub.getLat(), lat) && equal(sub.getLon(), lon)) {
            sub.exists = false;
            return true;
        }

        float cmp = 0.0f, cmpVal = 0.0f;
        if(depth % 2 == 0) {
            cmp = sub.getLat();
            cmpVal = lat;
        } else {
            cmp = sub.getLon();
            cmpVal = lon;
        }

        Node subtree = null;
        if (cmpVal < cmp)
            subtree = sub.right;
        else
            subtree = sub.left;

        if (subtree == null)
            return false;

        return _delete(depth+1, subtree, lat, lon);
    }

    private void _close (int depth, Node root, float lat, float lon) {
        if (equal(root.getLat(), lat) && equal(root.getLon(), lon) && root.exists)
            return;
        float dis = distance(root.getGeo(), lat, lon);
        if (minDistance > dis) {
            minDistance = dis;
            minP = root.getGeo();
        }

        float cmp = 0.0f, cmpVal = 0.0f;
        if(depth % 2 == 0) {
            cmp = root.getLat();
            cmpVal = lat;
        } else {
            cmp = root.getLon();
            cmpVal = lon;
        }

        Node sub;
        if (cmpVal < cmp)
            sub = root.right;
        else
            sub = root.left;

        if (sub == null)
            return;
        _close(depth+1, sub, lat, lon);
    }

    private boolean _indexed(int depth, Node root, float lat, float lon) {
        if(equal(lat, root.getLat()) && equal(lon, root.getLon()) && root.exists)
            return true;

        float cmp = 0.0f, cmpVal = 0.0f;
        if(depth % 2 == 0) {
            cmp = root.getLat();
            cmpVal = lat;
        } else {
            cmp = root.getLon();
            cmpVal = lon;
        }

        Node sub;
        if (cmpVal < cmp)
            sub = root.right;
        else
            sub = root.left;

        if (sub == null)
            return false;
        return _indexed(depth + 1, sub, lat, lon);
    }

    private void _addGeotag(int depth, Node sub, Geotag g) {
        Node subtree = null;
        float cmp, cmpVal;
        if (depth % 2 == 0) {
            cmp = sub.getLat();
            cmpVal = g.lat;
        } else {
            cmp = sub.getLon();
            cmpVal = g.lon;
        }

        if (cmpVal < cmp) {
            if (sub.right == null)
                sub.right = new Node (g);
            else
                subtree = sub.right;
        } else {
            if (sub.left == null)
                sub.left = new Node (g);
            else
                subtree = sub.left;
        }

        if(subtree != null)
            _addGeotag(depth+1, subtree, g);
    }

    private Node build (int depth, Geotag[] nodes) {
        Node n = null;

        if (nodes.length == 1) {
            n = new Node(nodes[0]);
            System.out.println("try create node "+n.toString());
        }
        else if (nodes.length > 1){
            Geotag[] lefts, rights;

            int med = nodes.length / 2;

            if (depth % 2 == 0)
                sortByLat(nodes);
            else
                sortByLon(nodes);

            if (med == 1) {
                rights = new Geotag[] {nodes[0]};
                lefts = new Geotag[] {nodes[1]};
            } else {
                rights = (Geotag[]) ((Object[]) Arrays.copyOfRange(nodes, 0, med-1));
                lefts = (Geotag[]) ((Object[]) Arrays.copyOfRange(nodes, med+1, nodes.length-1));
            }

            n = new Node (nodes[med]);
            n.left = build(depth+1, lefts);
            n.right = build(depth+1, rights);
        }
        return n;
    }

    private void sortByLat (Geotag[] arr) {
        Geotag.byLat = true;
        Arrays.sort(arr);
    }

    private void sortByLon (Geotag[] arr) {
        Geotag.byLat = false;
        Arrays.sort(arr);
    }
}
