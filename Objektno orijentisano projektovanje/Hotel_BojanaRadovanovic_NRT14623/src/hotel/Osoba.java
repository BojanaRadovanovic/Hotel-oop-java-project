
package hotel;

import java.util.Objects;

public abstract class Osoba {
    protected String ime, prezime, korisnickoIme, lozinka;

    public Osoba(String ime, String prezime, String korisnickoIme, String lozinka) {
        this.ime = ime;
        this.prezime = prezime;
        this.korisnickoIme = korisnickoIme;
        this.lozinka = lozinka;
    }

    public abstract String uloga();

    public String getIme() { 
        return ime;
    }
    public String getPrezime() {
        return prezime; 
    }
    public String getKorisnickoIme() {
        return korisnickoIme; 
    }

    @Override public String toString() {
        return uloga() + ": " + ime + " " + prezime + " (" + korisnickoIme + ")";
    }
    @Override public boolean equals(Object o) {
        if (!(o instanceof Osoba)) return false;
        return Objects.equals(korisnickoIme, ((Osoba) o).korisnickoIme);
    }
    @Override public int hashCode() {
        return Objects.hash(korisnickoIme); 
    }
}

