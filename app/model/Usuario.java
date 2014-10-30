package model;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Usuario extends Model{
    @Id
    public Long id;

    public static Finder<Long, Usuario> find = new Finder<Long, Usuario>(Long.class, Usuario.class);

    public Usuario (Long id) {
        this.id = id;
    }

    public static Usuario ifExists(Long id) {
        Usuario u = null;
        if ((u = Usuario.find.byId(id))!=null)
            return u;
        u = new Usuario(id);
        u.save();
        return u;
    }
}
