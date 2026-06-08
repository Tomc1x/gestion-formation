package fr.eni.gestionformation.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("Un utilisateur avec l'email " + email + " existe déjà.");
    }
}
