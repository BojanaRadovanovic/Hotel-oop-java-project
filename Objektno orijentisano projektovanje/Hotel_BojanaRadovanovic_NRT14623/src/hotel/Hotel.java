
package hotel;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.time.format.DateTimeParseException;


public class Hotel implements PretrazivacSoba {

    
    public static final String JSON_PATH = "src/hotel/hotel.json";

    private final Map<Integer, Soba> sobe = new LinkedHashMap<>();
    private final Map<String, Gost> gosti = new LinkedHashMap<>();
    private final Map<String, Recepcioner> recepcioneri = new LinkedHashMap<>();
    private final List<Rezervacija> rezervacije = new ArrayList<>();
    private int seqRezId = 1;

    public Hotel() {
        try { ucitaj(); } catch (Exception e) {
            System.err.println("Upozorenje: nije uspelo učitavanje JSON-a: " + e.getMessage());
        }
        // demo podaci ako je prazno
        if (sobe.isEmpty()) {
            try {
                dodajSobu(new Soba(101, TipSobe.JEDNOKREVETNA, 45));
                dodajSobu(new Soba(102, TipSobe.DVOKREVETNA, 65));
                dodajSobu(new Soba(201, TipSobe.APARTMAN, 120));
            } catch (SobaPostojiException ignored) {}
        }
        if (recepcioneri.isEmpty()) {
            recepcioneri.put("ana", new Recepcioner("Ana", "Antic", "ana", "ana"));
        }
        if (gosti.isEmpty()) {
            gosti.put("petar", new Gost("Petar", "Petrovic", "petar", "petar"));
        }
        if (!rezervacije.isEmpty()) {
            seqRezId = rezervacije.stream().mapToInt(Rezervacija::getId).max().orElse(0) + 1;
        }
    }

    //  INTERFEJS (pretraga)
    @Override public List<Soba> pretraziPoTipu(String tip) {
        String t = tip.toUpperCase().trim();
        return sobe.values().stream().filter(s -> s.getTip().name().equals(t)).collect(Collectors.toList());
    }
    @Override public List<Soba> pretraziPoMaxCeni(double maxCena) {
        return sobe.values().stream().filter(s -> s.getCenaPoNoci() <= maxCena).collect(Collectors.toList());
    }
    @Override public List<Soba> pretraziDostupne() {
        return sobe.values().stream().filter(s -> !s.isZauzeta()).collect(Collectors.toList());
    }

    //  OSNOVNE OPERACIJE 
    public void registrujGosta(Gost g) { gosti.putIfAbsent(g.getKorisnickoIme(), g); }
    public void dodajRecepcionera(Recepcioner r) { recepcioneri.putIfAbsent(r.getKorisnickoIme(), r); }

    public void dodajSobu(Soba s) throws SobaPostojiException {
        if (sobe.containsKey(s.getBroj())) throw new SobaPostojiException("Soba " + s.getBroj() + " vec postoji!");
        sobe.put(s.getBroj(), s);
    }

public Rezervacija rezervisi(int brojSobe, String korisnickoIme, LocalDate od, LocalDate _do)
        throws NedostupnaSobaException {
    Soba s = sobe.get(brojSobe);

    if (s == null) {
        throw new NedostupnaSobaException("Broj sobe ne postoji.");
    }
    if (s.isZauzeta()) {
        throw new NedostupnaSobaException("Soba je trenutno zauzeta.");
    }
    if (!gosti.containsKey(korisnickoIme)) {
        throw new NedostupnaSobaException("Nepoznat gost (korisnik nije registrovan).");
    }

    s.setZauzeta(true);
    Rezervacija r = new Rezervacija(seqRezId++, brojSobe, korisnickoIme, od, _do, s.getCenaPoNoci());
    rezervacije.add(r);

    try { sacuvaj(); } catch (IOException ignored) {}

    return r;
}

    public boolean odjava(int idRez) {
    Iterator<Rezervacija> it = rezervacije.iterator();
    while (it.hasNext()) {
        Rezervacija r = it.next();
        if (r.getId() == idRez) {
            int brojSobe = r.getBrojSobe();

            // 1) ukloni rezervaciju iz liste
            it.remove();

            // 2) oslobodi sobu samo ako za nju više nema rezervacija
            boolean postojiJos = rezervacije.stream()
                    .anyMatch(x -> x.getBrojSobe() == brojSobe);
            if (!postojiJos) {
                Soba s = sobe.get(brojSobe);
                if (s != null) s.setZauzeta(false);
            }

          
            try { sacuvaj(); } catch (IOException ignored) {}

            return true;
        }
    }
    return false; // nije pronađena rezervacija s tim ID-jem
}
    
    public boolean otkaziRezervacijuZaGostaPoId(String korisnickoIme, int idRez) {
    Iterator<Rezervacija> it = rezervacije.iterator();
    while (it.hasNext()) {
        Rezervacija r = it.next();
        if (r.getId() == idRez) {
            // zaštita: gost može da otkaže samo SVOJU rezervaciju
            if (!korisnickoIme.equals(r.getKorisnickoImeGosta())) {
                return false;
            }

            int brojSobe = r.getBrojSobe();
            it.remove(); // ukloni rezervaciju

            // oslobodi sobu samo ako više nema rezervacija za nju
            boolean postojiJos = rezervacije.stream()
                    .anyMatch(x -> x.getBrojSobe() == brojSobe);
            if (!postojiJos) {
                Soba s = sobe.get(brojSobe);
                if (s != null) s.setZauzeta(false);
            }

            try { sacuvaj(); } catch (IOException ignored) {}
            return true;
        }
    }
    return false; // nema rezervacije sa tim ID-jem
}



    public List<Soba> sveSobe() { return new ArrayList<>(sobe.values()); }
    public List<Rezervacija> sveRezervacije() { return new ArrayList<>(rezervacije); }

    // JSON I/O 

    public void sacuvaj() throws IOException {
        String json =
            "{\n" +
            "  \"sobe\": [" + sobe.values().stream().map(this::sobaToJson).collect(Collectors.joining(",")) + "],\n" +
            "  \"gosti\": [" + gosti.values().stream().map(this::gostToJson).collect(Collectors.joining(",")) + "],\n" +
            "  \"recepcioneri\": [" + recepcioneri.values().stream().map(this::recToJson).collect(Collectors.joining(",")) + "],\n" +
            "  \"rezervacije\": [" + rezervacije.stream().map(this::rezToJson).collect(Collectors.joining(",")) + "]\n" +
            "}\n";
        Files.writeString(Paths.get(JSON_PATH), json,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    public void ucitaj() throws IOException {
        Path p = Paths.get(JSON_PATH);
        if (!Files.exists(p)) return;
        String json = Files.readString(p);
        if (json.isBlank()) return;

        // sobe
        String sobeArr = getArray(json, "sobe");
        for (String obj : splitObjects(sobeArr)) {
            Soba s = sobaFromJson(obj);
            sobe.put(s.getBroj(), s);
        }
        // gosti
        String gostiArr = getArray(json, "gosti");
        for (String obj : splitObjects(gostiArr)) {
            Gost g = gostFromJson(obj);
            gosti.put(g.getKorisnickoIme(), g);
        }
        // recepcioneri
        String recArr = getArray(json, "recepcioneri");
        for (String obj : splitObjects(recArr)) {
            Recepcioner r = recFromJson(obj);
            recepcioneri.put(r.getKorisnickoIme(), r);
        }
        // rezervacije
        String rezArr = getArray(json, "rezervacije");
        for (String obj : splitObjects(rezArr)) {
            Rezervacija r = rezFromJson(obj);
            rezervacije.add(r);
        }
    }

    // pomoćne 
    private static String esc(String s){ return s.replace("\\","\\\\").replace("\"","\\\""); }

    private String sobaToJson(Soba s) {
        return "{"
            + "\"broj\":" + s.getBroj() + ","
            + "\"tip\":\"" + s.getTip().name() + "\","
            + "\"cenaPoNoci\":" + s.getCenaPoNoci() + ","
            + "\"zauzeta\":" + s.isZauzeta()
            + "}";
    }
    private String gostToJson(Gost g) {
        return "{"
            + "\"ime\":\"" + esc(g.getIme()) + "\","
            + "\"prezime\":\"" + esc(g.getPrezime()) + "\","
            + "\"korisnickoIme\":\"" + esc(g.getKorisnickoIme()) + "\""
            + "}";
    }
    private String recToJson(Recepcioner r) {
        return "{"
            + "\"ime\":\"" + esc(r.getIme()) + "\","
            + "\"prezime\":\"" + esc(r.getPrezime()) + "\","
            + "\"korisnickoIme\":\"" + esc(r.getKorisnickoIme()) + "\""
            + "}";
    }
    private String rezToJson(Rezervacija r) {
        return "{"
            + "\"id\":" + r.getId() + ","
            + "\"brojSobe\":" + r.getBrojSobe() + ","
            + "\"korisnickoImeGosta\":\"" + esc(r.getKorisnickoImeGosta()) + "\","
            + "\"od\":\"" + r.getOd() + "\","
            + "\"do\":\"" + r.getDo() + "\","
            + "\"ukupnaCena\":" + r.getUkupnaCena()
            + "}";
    }

    private static String getArray(String json, String key) {
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL);
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }
    private static List<String> splitObjects(String arrayContent) {
        List<String> out = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i=0;i<arrayContent.length();i++) {
            char c = arrayContent.charAt(i);
            if (c=='{') { if (depth==0) start = i; depth++; }
            else if (c=='}') { depth--; if (depth==0 && start!=-1) out.add(arrayContent.substring(start, i+1)); }
        }
        return out;
    }
    private static String getStr(String obj, String key) {
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"(.*?)\\\"", Pattern.DOTALL);
        Matcher m = p.matcher(obj);
        return m.find() ? m.group(1).replace("\\\"", "\"").replace("\\\\","\\") : "";
    }
    private static int getInt(String obj, String key) {
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(-?\\d+)");
        Matcher m = p.matcher(obj); return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }
    private static double getDouble(String obj, String key) {
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher m = p.matcher(obj); return m.find() ? Double.parseDouble(m.group(1)) : 0.0;
    }
    private static boolean getBool(String obj, String key) {
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(true|false)");
        Matcher m = p.matcher(obj); return m.find() && Boolean.parseBoolean(m.group(1));
    }

    private Soba sobaFromJson(String obj) {
        int broj = getInt(obj, "broj");
        TipSobe tip = TipSobe.fromString(getStr(obj, "tip"));
        double cena = getDouble(obj, "cenaPoNoci");
        boolean zauzeta = getBool(obj, "zauzeta");
        Soba s = new Soba(broj, tip, cena);
        s.setZauzeta(zauzeta);
        return s;
    }
    private Gost gostFromJson(String obj) {
        return new Gost(getStr(obj, "ime"), getStr(obj, "prezime"), getStr(obj, "korisnickoIme"), "");
    }
    private Recepcioner recFromJson(String obj) {
        return new Recepcioner(getStr(obj, "ime"), getStr(obj, "prezime"), getStr(obj, "korisnickoIme"), "");
    }
    
    private boolean jeRecepcioner(String korisnickoIme) {
    return recepcioneri.containsKey(korisnickoIme);
}

    
    private Rezervacija rezFromJson(String obj) {
        int id = getInt(obj, "id");
        int broj = getInt(obj, "brojSobe");
        String gost = getStr(obj, "korisnickoImeGosta");
        LocalDate od = LocalDate.parse(getStr(obj, "od"));
        LocalDate dd = LocalDate.parse(getStr(obj, "do"));
        double uk = getDouble(obj, "ukupnaCena");
        // Rekreiramo pa upišemo već obračunatu cenu:
        Rezervacija r = new Rezervacija(id, broj, gost, od, dd, 1.0);
        try {
            var f = Rezervacija.class.getDeclaredField("ukupnaCena");
            f.setAccessible(true); f.set(r, uk);
        } catch (Exception ignored) {}
        return r;
    }
    
    private boolean zahtevajRecepcionera(Scanner sc) {
    System.out.print("Recepcioner (korisnicko ime): ");
    String recUser = sc.nextLine().trim();
    if (!jeRecepcioner(recUser)) {
        System.out.println("Samo recepcioner ima pravo na ovu akciju.");
        return false;
    }
    return true;
}


    // MENI
    public void pokreniMeni(Scanner sc) {
        String o;
        do {
            System.out.println("\n=== HOTEL ===");
            System.out.println("1) Prikazi sve sobe");
            System.out.println("2) Prikazi dostupne sobe");
            System.out.println("3) Pretraga po tipu");
            System.out.println("4) Pretraga po max ceni");
            System.out.println("5) Dodaj sobu (recepcioner)");
            System.out.println("6) Registruj gosta(recepcioner)");
            System.out.println("7) Napravi rezervaciju");
            System.out.println("8) Odjava (recepcioner)");
            System.out.println("9) Prikazi rezervacije(recepcioner)");
            System.out.println("10) Otkazi rezervaciju");
            System.out.println("0) Sacuvaj i izadji");
            System.out.print("Izbor: ");
            o = sc.nextLine().trim();
            try {
                switch (o) {
                    case "1": sveSobe().forEach(System.out::println); break;
                    case "2": pretraziDostupne().forEach(System.out::println); break;
                    case "3":
                        System.out.print("Tip (JEDNOKREVETNA/DVOKREVETNA/APARTMAN): ");
                        pretraziPoTipu(sc.nextLine()).forEach(System.out::println);
                        break;
                    case "4":
                         System.out.print("Max cena: ");
                         double c = Double.parseDouble(sc.nextLine());

                         var rezultat = pretraziPoMaxCeni(c);

                         if (rezultat.isEmpty()) {
                         System.out.println("Nema slobodnih soba po toj ceni ili jeftinije.");
                         } else {
                         rezultat.forEach(System.out::println);
                         }
                         break;
                    case "5":
                      if (!zahtevajRecepcionera(sc)) break;
                       System.out.print("Broj sobe: "); int br = Integer.parseInt(sc.nextLine());
                       System.out.print("Tip (JEDNOKREVETNA/DVOKREVETNA/APARTMAN): ");
                       TipSobe t = TipSobe.fromString(sc.nextLine());
                       System.out.print("Cena/noc: "); double cn = Double.parseDouble(sc.nextLine());
                       dodajSobu(new Soba(br, t, cn));
                       System.out.println("Soba dodata.");
                       break;
                    case "6":
                        if (!zahtevajRecepcionera(sc)) break;
                        System.out.print("Ime: "); String ime = sc.nextLine();
                        System.out.print("Prezime: "); String prez = sc.nextLine();
                        System.out.print("Korisnicko ime: "); String kime = sc.nextLine();
                        registrujGosta(new Gost(ime, prez, kime, ""));
                        System.out.println("Registrovan gost.");
                        break;
                                     case "7": {
                                       try {
        System.out.print("Broj sobe: ");
        int bs = Integer.parseInt(sc.nextLine().trim());

        
        Soba s = sobe.get(bs);
        if (s == null) {
            System.out.println("GRESKA: Broj sobe ne postoji.");
            break;
        }
        if (s.isZauzeta()) {
            System.out.println("GRESKA: Soba je trenutno zauzeta.");
            break;
        }

        System.out.print("Gost (korisnicko ime): ");
        String g = sc.nextLine().trim();

        
        if (!gosti.containsKey(g)) {
            System.out.println("GRESKA: Nepoznat gost (korisnik nije registrovan).");
            break;
        }

        
        System.out.print("Od (YYYY-MM-DD): ");
        LocalDate odDatum = LocalDate.parse(sc.nextLine().trim());
        System.out.print("Do (YYYY-MM-DD): ");
        LocalDate doDatum = LocalDate.parse(sc.nextLine().trim());

        if (doDatum.isBefore(odDatum)) {
            System.out.println("GRESKA: Datum 'Do' ne može biti pre datuma 'Od'.");
            break;
        }

        Rezervacija r = rezervisi(bs, g, odDatum, doDatum);
        System.out.println("Rezervisano: " + r);

    } catch (NumberFormatException nfe) {
        System.out.println("GRESKA: Broj sobe mora biti ceo broj.");
    } catch (DateTimeParseException dtpe) {
        System.out.println("GRESKA: Datum mora biti u formatu YYYY-MM-DD (npr. 2025-08-25).");
    }
                         break;
                         }
                    case "8":
                        if (!zahtevajRecepcionera(sc)) break;
                        System.out.print("ID rezervacije: "); int id = Integer.parseInt(sc.nextLine());
                        System.out.println(odjava(id) ? "Odjava uspesna." : "Rezervacija nije pronadjena.");
                        break;
                    case "9":
                        if (!zahtevajRecepcionera(sc)) break;
                        if (sveRezervacije().isEmpty()) {
                        System.out.println("Nema evidentiranih rezervacija.");
                        } else {
                        sveRezervacije().forEach(System.out::println);
                        }
                        break;
                        case "10": {
                        System.out.print("Korisnicko ime gosta: ");
                        String kx = sc.nextLine().trim();
                        System.out.print("ID rezervacije za otkazivanje: ");
                        int idx = Integer.parseInt(sc.nextLine().trim());

                        boolean ok = otkaziRezervacijuZaGostaPoId(kx, idx);
                        System.out.println(ok ? "Rezervacija je otkazana." :
                        "Nije pronađena ta rezervacija za navedenog korisnika.");
                        break;
                        }
                       case "0":
                        sacuvaj();
                        System.out.println("Sacuvano u " + JSON_PATH + " Dovidjenja!");
                        break;
                    default:
                        System.out.println("Nepoznata opcija.");
                }
         } catch (SobaPostojiException | NedostupnaSobaException e) {
         System.out.println("GRESKA: " + e.getMessage());
          } catch (Exception e) {
         System.out.println("Neocekivana greska: " + e.getMessage());
            }


        } while(!"0".equals(o));
    }
}  

