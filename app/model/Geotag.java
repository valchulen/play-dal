package model;

import play.db.ebean.Model;

import javax.persistence.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static play.data.validation.Constraints.Required;

@Entity
public class Geotag extends Model implements Comparable {
    public static boolean byLat = false;

    @Id
    public long id;

    @Required
    public float lat;
    public float lon;

    public String usuarios="";

    public String photoNames="";

    public String incapacidad;
    public int importancia = 1;

    public static Finder<Long, Geotag> find = new Finder<Long, Geotag>(Long.class, Geotag.class);

    public Geotag (float lat, float lon,  String usuario) {
        //setLat(lat);
        //setLon(lon);

        this.lat = lat;
        this.lon = lon;

        this.usuarios+=usuario;
        this.incapacidad = "auditiva;|;motora;|;intelectual;|;visual;|;visceral;|;acondroplastia;|";
    }

    public void concatIncapacidad (String key, String value) {

        String[] vals = incapacidad.split(";");
        String res = "";

        for(int i = 0; i < vals.length; i++) {
            if (vals[i].equals(key)) {
                if ((vals[i+1].equals("|")) || (vals[i+1].equals("+") && value.equals("-")))
                        vals[i+1] = value;
            }
            res += vals[i] + ";";
        }
        incapacidad = res;
    }

    public void setLat(String lat) {
        this.lat = Float.parseFloat(lat);
    }

    public void setLon(String lon) {
        this.lon = Float.parseFloat(lon);
    }

    public List<String> getUsuarios () {
        return Arrays.asList(usuarios.split(";"));
    }

    public List<String> getPhotoNames () {
        return Arrays.asList(photoNames.split(";"));
    }

    @Override
    public int compareTo(Object o) {
        if (Geotag.byLat) {
            if(this.lat > ((Geotag) o).lat)
                return 1;
            else if (Float.compare(this.lat, ((Geotag) o).lat) == 0)
                return 0;
            else
                return -1;
        } else {
            if(this.lon > ((Geotag) o).lon)
                return 1;
            else if (Float.compare(this.lon, ((Geotag) o).lon) == 0)
                return 0;
            else
                return -1;
        }
    }
}
