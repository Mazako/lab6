package model;

import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class PhoneBook implements Serializable {
    public static final String putInstruction = "PUT <imię> <numer> - zapisuje podaną osobę o podanym numerze telefonu do książeczki";
    public static final String listInstruction = "LIST - wyświetla listę wszystkich zapisanych imion";
    public static final String deleteInstruction = "DELETE <imię> - usuwa z książeczki numer o podanym imieniu";
    public static final String getInstruction = "GET <imię> - zwraca numer osoby o podanym imieniu";

    public static final String closeInstruction = "CLOSE - zamyka gniazdo serwera, co powoduje, że serwer nie przyjmie więcej połączeń i wyłączy się gdy reszta otwartych połączeń zostanie zakończona";
    public static final String byeInstruction = "BYE - zamyka połaczenie z serwerem";
    public static final String replaceInstruction = "REPLACE <imię> <numer> - zmienia numer podanej osoby na podany w poleceniu";
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
        return phone == null ? "ERROR Nie znaleziono osoby " + name : "OK " + phone;
    }

    public String put(String name, String number) {
        book.putIfAbsent(name, number);
        return "OK";
    }

    public String replace(String name, String newNumber) {
        if (book.get(name) == null) {
            return "ERROR Nie znaleziono osoby " + name;
        }
        book.replace(name, newNumber);
        return "OK";
    }

    public String delete(String name) {
        String removed = book.remove(name);
        return removed == null ? "ERROR Nie znaleziono osoby " + name : "OK";
    }

    public String list() {
        StringBuilder builder = new StringBuilder("OK ");
        Iterator<String> keysIterator = book.keys().asIterator();
        while (keysIterator.hasNext()) {
            String name = keysIterator.next();
            builder.append(name).append(" ");
        }
        return builder.toString();
    }
}
