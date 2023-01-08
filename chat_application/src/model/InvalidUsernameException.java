/*
 *  Laboratorium 4
 *
 *   Autor: Michal Maziarz, 263913
 *    Data: Styczeń 2023 r.
 */
package model;

public class InvalidUsernameException extends RuntimeException{
    public InvalidUsernameException(String message) {
        super(message);
    }
}
