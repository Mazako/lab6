/*
 *  Laboratorium 4
 *
 *   Autor: Michal Maziarz, 263913
 *    Data: Styczeń 2023 r.
 */
package server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        new PhoneBookServer().start();
    }
}
