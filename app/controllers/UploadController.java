package controllers;

import model.S3File;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;
import java.util.UUID;
import static play.libs.Json.toJson;

public class UploadController extends Controller {

    public static Result listImages() {
        List<S3File> uploads = new Model.Finder(UUID.class, S3File.class).all();
        return ok(toJson(uploads));
    }

    public static Result uploadMultipart() {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart uploadFilePart = body.getFile("upload");
        if (uploadFilePart != null) {
            S3File s3File = new S3File();
            s3File.name = uploadFilePart.getFilename();
            s3File.file = uploadFilePart.getFile();
            s3File.save();
            return redirect(routes.UploadController.listImages());
        }
        else {
            return badRequest("File upload error");
        }
    }
}
