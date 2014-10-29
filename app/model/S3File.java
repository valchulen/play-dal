package model;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import play.Logger;
import play.db.ebean.Model;
import plugins.S3Plugin;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class S3File {

    public UUID id;

    private String bucket;

    public String name;

    public File file;

    public URL getUrl() throws MalformedURLException {
        return new URL("https://s3.amazonaws.com/" + bucket + "/" + getActualFileName());
    }

    private String getActualFileName() {
        return id + "/" + name;
    }

    public void save() {
        if (S3Plugin.amazonS3 == null) {
            Logger.error("Could not save because amazonS3 was null");
            throw new RuntimeException("Could not save");
        }
        else {
            this.bucket = S3Plugin.s3Bucket;

            id = UUID.randomUUID();

            //super.save(); // assigns an id

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, getActualFileName(), file);
            putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // public for all
            S3Plugin.amazonS3.putObject(putObjectRequest); // upload file
        }
    }

    public static void delete(String actualFileName) {
        if (S3Plugin.amazonS3 == null) {
            Logger.error("Could not delete because amazonS3 was null");
            throw new RuntimeException("Could not delete");
        }
        else {
            S3Plugin.amazonS3.deleteObject(S3Plugin.s3Bucket, actualFileName);
            //super.delete();
        }
    }

    public S3File(File file, String name) {
        this.file = file; this.name = name;
    }

}