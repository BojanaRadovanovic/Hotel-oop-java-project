
package hotel;

public class Gost extends Osoba {
    public Gost(String ime, String prezime, String korisnickoIme, String lozinka) {
        super(ime, prezime, korisnickoIme, lozinka);
    }
    @Override public String uloga() { return "Gost"; }
}

