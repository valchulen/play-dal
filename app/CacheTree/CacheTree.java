package CacheTree;

import model.Geotag;
import play.Logger;

import java.util.List;

public class CacheTree {
    float ALLOWED_DIFFERENCE = 0.0001f;
    private final static int REBUILD_COUNT = 1000000;
    private static int element_count = 0;
    private Tree cache = null;

    public CacheTree () {

    }

    private void constructFromDbOnly () {
        if (Geotag.find.all().size() > 0)
        {
            Geotag [] arr = null;
            Geotag.find.all().toArray(arr);
            cache = new Tree(arr);
        }
    }

    private void reconstructIfNecessary () {
        if (element_count >= REBUILD_COUNT) {
            Geotag[] arr = null;
            Geotag.find.all().toArray(arr);
            cache = new Tree(arr);
            element_count = 0;
        }
        element_count++;
    }

    public List<Geotag> rangeSearch (float minlat, float minlon, float maxlat, float maxlon) {
        if (cache == null)
            constructFromDbOnly();
        if (cache != null)
            return cache.rangeSearch(minlat, minlon, maxlat, maxlon);
        else
            return null;
    }

    public Geotag findById(long id) {
        if (cache == null)
            constructFromDbOnly();
        return cache.findById(id);
    }

    public void addGeotag (Geotag g) {
        if (cache == null)
            construct(g);
        else
            cache.addGeotag(g);
        Logger.debug("ADDED TO TREE");
        reconstructIfNecessary();
    }

    public boolean delete (float lat, float lon) {
        if (cache == null)
            constructFromDbOnly();
        if (cache != null)
            return cache.delete(lat, lon);
        else
            return false;
    }

    public Geotag getClosest(float lat, float lon) {
        if(cache == null)
            constructFromDbOnly();
        if (cache != null)
            return cache.getClosest(lat, lon);
        else
            return null;
    }

    public Geotag findByPos(float lat, float lon) {
        if(cache == null)
            constructFromDbOnly();
        if (cache != null){
            Geotag g = cache.getClosest(lat, lon);
            if (Math.abs(g.lat - lat) < ALLOWED_DIFFERENCE && Math.abs(g.lon - lon) < ALLOWED_DIFFERENCE)
                return g;
        }
        return null;
    }

    public boolean indexedByPos(float lat, float lon){
        if (cache == null)
            constructFromDbOnly();
        if (cache != null)
            return cache.indexed(lat, lon);
        else
            return false;
    }

    private void construct (Geotag g) {
        List<Geotag> lest = Geotag.find.all();
        if(lest!=null){
            lest.add(g);
            Geotag[] todos = new Geotag[lest.size()];
            lest.toArray(todos);
            cache = new Tree(todos);
        } else
            cache = new Tree(new Geotag[]{g});
        Logger.debug("CREATED TREE");
    }
}
