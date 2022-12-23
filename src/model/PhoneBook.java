package model;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public class PhoneBook implements Serializable {
    private ConcurrentHashMap<String, String> book = new ConcurrentHashMap<>();

    public synchronized String load(File path) {
        try (var ois = new ObjectInputStream(new FileInputStream(path))) {
           var map =  (ConcurrentHashMap<String, String>) ois.readObject();
           book = map;
           return "Pomyślnie wczytano nową książkę telefoniczną";
        } catch (IOException | ClassNotFoundException e) {
            return "Błąd w wczytywaniu danych z pliku";
        }
    }

    public String save(Path path) {
        return null;
    }

    public String get(String name) {
        String phone = book.get(name);
        return phone == null ? "Nie znaleziono osoby " + name : "Numer osoby " + name + " : " + phone;
    }

    public String put(String name, String number) {
        String s = book.putIfAbsent(name, number);
        return "Zapisano numer do książki";
    }

    public String replace(String name, String newNumber) {
        return null;
    }

    String delete(String name) {
        return null;
    }

    String list() {
        return null;
    }
}
