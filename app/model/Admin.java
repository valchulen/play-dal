package model;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.util.Random;

@Entity
public class Admin extends Model{
    private Random RANDOM = new Random();
    private final int PASSWORD_LENGTH = 15;

    @Id
    public long id;

    public String pass;

    public String jurisdiccion;

    public static Finder<Long, Admin> find = new Finder<Long, Admin>(Long.class, Admin.class);

    public Admin (String jurisdiccion) {
        this.jurisdiccion = jurisdiccion;

        pass = generateRandomPassword();
    }

    private String generateRandomPassword() {

        String letters = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";

        String pw = "";
        for (int i=0; i<PASSWORD_LENGTH; i++)
        {
            int index = (int)(RANDOM.nextDouble()*letters.length());
            pw += letters.substring(index, index+1);
        }
        return pw;
    }
}
