package fr.eni.gestionformation.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Utilisateur non trouvé avec l'id : " + id);
    }
}
