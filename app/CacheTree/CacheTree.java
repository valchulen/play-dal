package CacheTree;

import model.Geotag;
import play.Logger;

import java.util.List;

public class CacheTree {
    private Tree cache = null;

    public CacheTree () {

    }

    public Geotag findById(long id) {
        if (cache != null)
            return cache.findById(id);
        else
            return null;
    }

    public void addGeotag (Geotag g) {
        if (cache == null)
            construct(g);
        else
            cache.addGeotag(g);
        Logger.debug("ADDED TO TREE");
    }

    public boolean delete (float lat, float lon) {
        if (cache != null)
            return cache.delete(lat, lon);
        else
            return false;
    }

    public Geotag getClosest(float lat, float lon) {
        if(cache != null)
            return cache.getClosest(lat, lon);
        else
            return null;
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
