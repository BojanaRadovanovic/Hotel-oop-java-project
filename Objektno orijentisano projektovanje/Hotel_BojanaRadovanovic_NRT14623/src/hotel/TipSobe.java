
package hotel;

public enum TipSobe {
    JEDNOKREVETNA, DVOKREVETNA, APARTMAN;

    public static TipSobe fromString(String s) {
        return TipSobe.valueOf(s.toUpperCase().trim());
    }
}
