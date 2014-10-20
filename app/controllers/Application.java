package controllers;

import CacheTree.CacheTree;
import model.Geotag;
import model.GeotagUnico;
import play.*;
import play.data.Form;
import play.mvc.*;

import views.html.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static play.libs.Json.toJson;

public class Application extends Controller {

    private static CacheTree tree = new CacheTree();

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result addGeotag() {
        GeotagUnico g = Form.form(GeotagUnico.class).bindFromRequest().get();

        if (g == null || g.usuario.isEmpty())
            return badRequest();

        if (tree.indexedByPos(g.lat, g.lon)){
            Geotag geoT = tree.getClosest(g.lat, g.lon);

            Geotag realGeo = Geotag.find.byId(geoT.id);

            if (realGeo.usuarios != null && g.usuario != null)
                return badRequest();

            realGeo.importancia++;
            geoT.importancia++;

            realGeo.usuarios += " " + g.usuario;
            geoT.usuarios += " " + g.usuario;

            //concatenar incapacidad

            realGeo.save();

            Logger.debug("UPDATED");
        } else {
            Geotag geo = new Geotag(g.lat, g.lon, g.usuario, g.incapacidad);
            geo.save();
            tree.addGeotag(geo);

            Logger.debug("SAVED");
        }
        return ok(toJson(g));
    }

    public static Result getAllGeotags() {
        List<Geotag>  geotags = Geotag.find.all();
        return ok(toJson(geotags));
    }

    public static Result deleteGeotag() {
        float lat = 0.0f, lon = 0.0f;
        lat = Float.parseFloat(Form.form().bindFromRequest().get("lat"));
        lon = Float.parseFloat(Form.form().bindFromRequest().get("lon"));
        if (lat==0.0f || lon == 0.0f)
            return badRequest();
        if(tree.delete(lat, lon)) {
            Logger.debug("LAT: "+lat+" LON: "+lon+" FOUND IN TREE");
            Geotag g = Geotag.find.where().eq("lat", lat).eq("lon", lon).findUnique();
            g.delete();
            Logger.debug("DELETED");
            return ok("deleted");
        }
        return notFound();
    }

    public static Result getClosest(){
        float lat = 0.0f, lon = 0.0f;
        lat = Float.parseFloat(Form.form().bindFromRequest().get("lat"));
        lon = Float.parseFloat(Form.form().bindFromRequest().get("lon"));
        if (lat==0.0f || lon == 0.0f)
            return badRequest();
        return ok(toJson(tree.getClosest(lat, lon)));
    }

    public static Result uploadPic(){
        long id = 0;
        id = Long.parseLong(Form.form().bindFromRequest().get("id"));
        if (id == 0)
            return badRequest();
        Geotag g = tree.findById(id);
        if (g == null)
            return badRequest("bad id");
        String filename = String.valueOf(g.photoNames.size()+1);

        File file = request().body().asRaw().asFile();
        if (file == null)
            return badRequest();
        file.renameTo(new File(Play.application().path().getPath()+"/public/images", filename));
        return ok("uploaded");
    }

}
