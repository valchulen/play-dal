package model;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;

import java.util.ArrayList;
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

    @ElementCollection
    public List<String> usuarios;
    @ElementCollection
    public List<String> photoNames;

    public String incapacidad;
    public int importancia = 0;

    public static Finder<Long, Geotag> find = new Finder<Long, Geotag>(Long.class, Geotag.class);

    public Geotag (String lat, String lon,  String usuarios, String incapacidad) {
        setLat(lat);
        setLon(lon);

        this.usuarios.add(usuarios);
        this.incapacidad = incapacidad;
    }

    public void setLat(String lat) {
        this.lat = Float.parseFloat(lat);
    }

    public void setLon(String lon) {
        this.lon = Float.parseFloat(lon);
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
