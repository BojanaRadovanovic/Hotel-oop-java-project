
package hotel;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Hotel app = new Hotel();
        try (Scanner sc = new Scanner(System.in)) {
            app.pokreniMeni(sc);
        }
    }
}

