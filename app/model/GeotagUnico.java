package model;

/**
 * Created by valchu on 20/10/14.
 */
public class GeotagUnico {
    public String incapacidad;
    public String usuario;

    public float lat;
    public float lon;

    public GeotagUnico(String incapacidad, String usuario, float lat, float lon) {
        this.incapacidad = incapacidad;
        this.usuario = usuario;
        this.lat = lat;
        this.lon = lon;
    }
    public GeotagUnico(){

    }
}
