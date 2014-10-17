package controllers;

import CacheTree.CacheTree;
import model.Geotag;
import play.*;
import play.data.Form;
import play.mvc.*;

import views.html.*;

import java.util.List;

import static play.libs.Json.toJson;

public class Application extends Controller {

    private static CacheTree tree = new CacheTree();

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result addGeotag() {
        Geotag g = Form.form(Geotag.class).bindFromRequest().get();
        tree.addGeotag(g);
        g.save();
        Logger.debug("SAVED");
        return redirect(routes.Application.index());
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
            return ok(toJson("deleted"));
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

}
