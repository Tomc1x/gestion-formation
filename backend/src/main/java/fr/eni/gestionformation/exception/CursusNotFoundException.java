package fr.eni.gestionformation.exception;

public class CursusNotFoundException extends RuntimeException {
    public CursusNotFoundException(Long id) {
        super("Cursus non trouvé avec l'id : " + id);
    }
}
