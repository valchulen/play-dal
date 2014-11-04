package controllers;

import CacheTree.CacheTree;
import model.Geotag;
import model.GeotagUnico;
import model.S3File;
import model.Usuario;
import org.jboss.netty.handler.codec.base64.Base64Decoder;
import play.*;
import play.data.Form;
import play.mvc.*;

import sun.misc.BASE64Decoder;
import views.html.*;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
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

        Long id;
        try {
            String a = null;
            if (g.usuario.length() > 20)
                a = g.usuario.substring(0, 20);
            else
                a = g.usuario;
            id = Long.parseLong(a);
        } catch (NumberFormatException e) {
            Logger.error("Bad format of usuario:"+g.usuario);
            return badRequest();
        }

        Logger.info("adding lat: "+ g.lat + " lon:" + g.lon);

        if (tree.indexedByPos(g.lat, g.lon)){

            Geotag geoT = tree.getClosest(g.lat, g.lon);

            Geotag realGeo = Geotag.find.byId(geoT.id);

            if (realGeo.usuarios == null && g.usuario == null)
                return badRequest();

            realGeo.concatIncapacidad(vals[0], vals[1]);
            geoT.concatIncapacidad(vals[0], vals[1]);

            for(Usuario user : realGeo.usuarios){
                if (user.id == id) {
                    Logger.info("trying double tag");
                    return ok(toJson(realGeo.id));
                }
            }

            realGeo.importancia++;
            geoT.importancia++;

            Usuario u = Usuario.ifExists(id);

            realGeo.usuarios.add(u);

            geoT.usuarios.add(u);

            try {
                realGeo.update();
                realGeo.saveManyToManyAssociations("usuarios");
            } catch (Exception e){
                Logger.error("addGeotag#error on realgeo.update() info:"+ e.toString());
                return badRequest();
            }
            Logger.debug("UPDATED");

            return ok(toJson(realGeo.id));
        } else {
            Geotag geo = new Geotag(g.lat, g.lon, id);

            geo.concatIncapacidad(vals[0], vals[1]);

            try {
                geo.save();
                geo.saveManyToManyAssociations("usuarios");
            } catch (Exception e){
                Logger.error("addGeotag#error on geo.save() info:"+ e.toString());
                return badRequest();
            }
            tree.addGeotag(geo);

            Logger.debug("SAVED");
            return ok(toJson(geo.id));
        }
    }

    public static Result getAllGeotags() {
        List<Geotag>  geotags = Geotag.find.all();
        Logger.debug("GETTING ALL GEOS");
        return ok(toJson(geotags));
    }

    //eliminar las fotos
    public static Result deleteGeotag() {
        float lat = 0.0f, lon = 0.0f;
        lat = Float.parseFloat(Form.form().bindFromRequest().get("lat"));
        lon = Float.parseFloat(Form.form().bindFromRequest().get("lon"));
        Logger.debug("DELETE FOR LAT" + lat + " AND LON " + lon);
        if (lat==0.0f || lon == 0.0f)
            return badRequest();
        if(tree.delete(lat, lon)) {
            Logger.debug("LAT: "+lat+" LON: "+lon+" FOUND IN TREE");
            Geotag g = Geotag.find.where().eq("lat", lat).eq("lon", lon).findUnique();
            g.delete();
            Logger.debug("DELETED");
            return ok("deleted");
        }
        Logger.debug("NOT FOUND");
        return notFound();
    }

    public static Result getClosest() {
        float lat = 0.0f, lon = 0.0f;
        lat = Float.parseFloat(Form.form().bindFromRequest().get("lat"));
        lon = Float.parseFloat(Form.form().bindFromRequest().get("lon"));
        if (lat==0.0f || lon == 0.0f)
            return badRequest();
        Geotag g = tree.getClosest(lat, lon);
        if (g != null) {
            Logger.debug("FOUND");
            return ok(toJson(g));
        }
        Logger.debug("NOT FOUND");
        return ok("not found");
    }

    public static Result uploadMultipart () {
        long id = 0;
        try {
            id = Long.parseLong(Form.form().bindFromRequest().get("id"));
        } catch (Exception e) {
            Logger.debug("Bad format");
            return badRequest("bad id - 1");
        }

        if (tree.findById(id) == null)
            return badRequest("bad id - 1");

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart uploadFilePart = body.getFile("upload");
        if (uploadFilePart != null) {
            S3File s3File = new S3File();
            s3File.name = uploadFilePart.getFilename();
            s3File.file = uploadFilePart.getFile();

            Geotag g = Geotag.find.byId(id);
            g.photos.add(s3File);
            g.update();
            s3File.save();

            return ok("uploaded");
        }
        else {
            return badRequest("File upload error");
        }
    }

    public static Result rangeSearch() {
        float minlat = 0.0f, minlon = 0.0f; float maxlat = 0.0f, maxlon = 0.0f;
        minlat = Float.parseFloat(Form.form().bindFromRequest().get("minlat"));
        minlon = Float.parseFloat(Form.form().bindFromRequest().get("minlon"));
        maxlat = Float.parseFloat(Form.form().bindFromRequest().get("maxlat"));
        maxlon = Float.parseFloat(Form.form().bindFromRequest().get("maxlon"));
        if (minlat == 0.0f || minlon == 0.0f || maxlat == 0.0f || maxlon == 0.0f)
            return badRequest();
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