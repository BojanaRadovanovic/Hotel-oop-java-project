
package hotel;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Rezervacija {
    private int id;
    private int brojSobe;
    private String korisnickoImeGosta;
    private LocalDate od, _do;
    private double ukupnaCena;

    public Rezervacija(int id, int brojSobe, String korisnickoImeGosta, LocalDate od, LocalDate _do, double cenaPoNoci) {
        this.id = id;
        this.brojSobe = brojSobe;
        this.korisnickoImeGosta = korisnickoImeGosta;
        this.od = od;
        this._do = _do;
        long noci = Math.max(1, ChronoUnit.DAYS.between(od, _do));
        this.ukupnaCena = noci * cenaPoNoci;
    }

    public int getId() {
        return id; 
    }
    public int getBrojSobe() {
        return brojSobe; 
    }
    public String getKorisnickoImeGosta() {
        return korisnickoImeGosta; 
    }
    public LocalDate getOd() {
        return od;
    }
    public LocalDate getDo() {
        return _do; 
    }
    public double getUkupnaCena() { 
        return ukupnaCena;
    }

    @Override public String toString() {
        return "Rezervacija " + id + " | Soba " + brojSobe + " | " + korisnickoImeGosta + " | " + od + " - " + _do + " | " + ukupnaCena + " evra";
    }
}

