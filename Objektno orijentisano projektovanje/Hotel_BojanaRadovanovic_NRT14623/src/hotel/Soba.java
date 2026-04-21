
package hotel;

public class Soba {
    private int broj;
    private TipSobe tip;
    private double cenaPoNoci;
    private boolean zauzeta;

    public Soba(int broj, TipSobe tip, double cenaPoNoci) {
        this.broj = broj;
        this.tip = tip;
        this.cenaPoNoci = cenaPoNoci;
        this.zauzeta = false;
    }

    public int getBroj() {
        return broj;
    }
    public TipSobe getTip() { 
        return tip;
    }
    public double getCenaPoNoci() {
        return cenaPoNoci;
    }
    public boolean isZauzeta() {
        return zauzeta;
    }
    public void setZauzeta(boolean z) { 
        this.zauzeta = z; 
    }

    @Override public String toString() {
        return "Soba #" + broj + " [" + tip + "], " + cenaPoNoci + " evra/noc, " + (zauzeta ? "zauzeta" : "slobodna");
    }
}
