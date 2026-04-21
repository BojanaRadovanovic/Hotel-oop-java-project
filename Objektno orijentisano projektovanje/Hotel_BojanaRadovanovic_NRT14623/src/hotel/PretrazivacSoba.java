
package hotel;

import java.util.List;

public interface PretrazivacSoba {
    List<Soba> pretraziPoTipu(String tip);
    List<Soba> pretraziPoMaxCeni(double maxCena);
    List<Soba> pretraziDostupne();
}

