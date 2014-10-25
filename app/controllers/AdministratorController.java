package controllers;

import model.Admin;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import static play.libs.Json.toJson;

public class AdministratorController extends Controller {

    public static Result createNew () {
        if (Form.form().bindFromRequest().get("jurisdiccion") != null) {
            Admin a = new Admin(Form.form().bindFromRequest().get("jurisdiccion"));
            a.save();
            return ok(toJson(a));
        } else
            return badRequest();
    }

    public static Result validate() {
        if (Form.form().bindFromRequest().get("id") != null && Form.form().bindFromRequest().get("pass") != null) {
            try {
                Admin a = Admin.find.where().eq("id", Form.form().bindFromRequest().get("id")).eq("pass", Form.form().bindFromRequest().get("pass")).findUnique();
                if (a.id == Long.parseLong(Form.form().bindFromRequest().get("id")) && a.pass == Form.form().bindFromRequest().get("pass"))
                    return ok("true");
                else
                    return ok("false");
            } catch (Exception e) {
                return badRequest();
            }
        } else
            return badRequest();
    }

    public static Result delete() {
        if (Form.form().bindFromRequest().get("id") != null && Form.form().bindFromRequest().get("pass") != null) {
            try {
                Admin.find.where().eq("id", Form.form().bindFromRequest().get("id")).eq("pass", Form.form().bindFromRequest().get("pass")).findUnique().delete();
            } catch (Exception e) {
                return badRequest();
            }
            return ok("delete");
        } else
            return badRequest();
    }
}
