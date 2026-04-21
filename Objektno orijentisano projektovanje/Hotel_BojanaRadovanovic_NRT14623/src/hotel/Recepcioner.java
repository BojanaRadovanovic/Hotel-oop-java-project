
package hotel;

public class Recepcioner extends Osoba {
    public Recepcioner(String ime, String prezime, String korisnickoIme, String lozinka) {
        super(ime, prezime, korisnickoIme, lozinka);
    }
    @Override public String uloga() {
        return "Recepcioner"; 
    }
}

