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
        GeotagUnico g;
        try {
            g = Form.form(GeotagUnico.class).bindFromRequest().get();
        } catch (Exception e) {
            Logger.error("addGeotag#error on bindFromRequest");
            return badRequest();
        }

        if (!g.incapacidad.contains(";")) {
            Logger.error("addGeotag#error on incapacidad:'"+g.incapacidad+"' format - 1");
            return badRequest();
        }
        String[] vals = g.incapacidad.split(";");

        boolean puede = true;
        if (!(vals[0]=="auditiva" || vals[0]=="motora" || vals[0]=="intelectual" || vals[0]=="visual" || vals[0]=="visceral" || vals[0]=="acondroplastia"))
            puede = false;
        if (!(vals[1]=="|" || vals[1]=="+" || vals[1]=="-"))
            puede = false;

        if (g == null || g.usuario.isEmpty() || puede) {
            Logger.error("addGeotag#error on incapacidad:'"+g.incapacidad+"' format - 2");
            return badRequest();
        }

        if (tree.indexedByPos(g.lat, g.lon)){

            Geotag geoT = tree.getClosest(g.lat, g.lon);

            Geotag realGeo = Geotag.find.byId(geoT.id);

            if (realGeo.usuarios == null && g.usuario == null) {
                Logger.error("addGeotag#error on usuario = null");
                return badRequest();
            }

            realGeo.concatIncapacidad(vals[0], vals[1]);
            geoT.concatIncapacidad(vals[0], vals[1]);

            List<String> ids = realGeo.getUsuarios();

            for(String id : ids)
                if (id == g.usuario) {
                    Logger.info("trying double tag");
                    return ok(toJson(realGeo));
                }

            realGeo.importancia++;
            geoT.importancia++;

            realGeo.usuarios += ";" + g.usuario;
            geoT.usuarios += ";" + g.usuario;

            try {
                realGeo.update();
            } catch (Exception e){
                Logger.error("addGeotag#error on realgeo.update() info:"+ e.toString());
                return badRequest();
            }
            Logger.debug("UPDATED");

            return ok(toJson(realGeo));
        } else {
            Geotag geo = new Geotag(g.lat, g.lon, g.usuario);

            geo.concatIncapacidad(vals[0], vals[1]);

            try {
                geo.save();
            } catch (Exception e){
                Logger.error("addGeotag#error on geo.save() info:"+ e.toString());
                return badRequest();
            }
            tree.addGeotag(geo);

            Logger.debug("SAVED");
            return ok(toJson(geo));
        }
    }

    public static Result getAllGeotags() {
        List<Geotag>  geotags;

        try {
            geotags = Geotag.find.all();
        } catch (Exception e){
            Logger.error("getAllGeos#error on find all info:"+ e.toString());
            return badRequest();
        }

        Logger.debug("GETTING ALL GEOS");
        return ok(toJson(geotags));
    }

    public static Result deleteGeotag() {
        float lat = 0.0f, lon = 0.0f;
        try {
            lat = Float.parseFloat(Form.form().bindFromRequest().get("lat"));
            lon = Float.parseFloat(Form.form().bindFromRequest().get("lon"));
        } catch (Exception e) {
            Logger.error("deleteGeotag#error GET vars failed info:"+ e.toString());
            return badRequest();
        }
        Logger.debug("DELETE FOR LAT" + lat + " AND LON " + lon);
        if(tree.delete(lat, lon)) {
            Logger.debug("LAT: "+lat+" LON: "+lon+" FOUND IN TREE");

            try {
                Geotag g = Geotag.find.where().eq("lat", lat).eq("lon", lon).findUnique();
                g.delete();
            } catch (Exception e) {
                Logger.error("deleteGeotag#error db acess or delete info:"+ e.toString());
                return badRequest();
            }
            Logger.debug("DELETED");
            return ok("deleted");
        }
        Logger.debug("NOT FOUND");
        return ok("not found");
    }

    public static Result getClosest() {
        float lat = 0.0f, lon = 0.0f;
        try {
            lat = Float.parseFloat(Form.form().bindFromRequest().get("lat"));
            lon = Float.parseFloat(Form.form().bindFromRequest().get("lon"));
        } catch (Exception e) {
            Logger.error("getClosest#error GET vars failed info:"+ e.toString());
            return badRequest();
        }

        Geotag g = tree.getClosest(lat, lon);
        if (g != null) {
            Logger.debug("FOUND");
            return ok(toJson(g));
        }
        Logger.debug("NOT FOUND");
        return ok("not found");
    }

    //falta debugging
    public static Result uploadPic() {
        long id = 0;
        try {
            id = Long.parseLong(Form.form().bindFromRequest().get("id"));
        } catch (Exception e) {
            return badRequest();
        }
        if (id == 0)
            return badRequest();
        Geotag g = tree.findById(id);
        if (g == null)
            return badRequest("bad id");
        String filename = String.valueOf(g.getPhotoNames().size()+1);

        File file = request().body().asRaw().asFile();
        if (file == null)
            return badRequest();
        file.renameTo(new File(Play.application().path().getPath()+"/public/images", filename));
        return ok("uploaded");
    }

    public static Result rangeSearch() {
        float minlat = 0.0f, minlon = 0.0f; float maxlat = 0.0f, maxlon = 0.0f;
        try {
            minlat = Float.parseFloat(Form.form().bindFromRequest().get("minlat"));
            minlon = Float.parseFloat(Form.form().bindFromRequest().get("minlon"));
            maxlat = Float.parseFloat(Form.form().bindFromRequest().get("maxlat"));
            maxlon = Float.parseFloat(Form.form().bindFromRequest().get("maxlon"));
        } catch (Exception e) {
            Logger.error("rangeSearch#error GET vars failed info:"+ e.toString());
            return badRequest();
        }

        Logger.debug("RANGE SEARCHING minlat "+ minlat +" minlon "+ minlon +" maxlat "+ maxlat +" maxlon "+maxlon);
        List<Geotag> lis = tree.rangeSearch(minlat, minlon, maxlat, maxlon);
        if (lis != null) {
            Logger.debug("FOUND");
            return ok(toJson(lis));
        }
        Logger.debug("NOT FOUND");
        return ok("nothing found");
    }
}