package fr.eni.gestionformation.exception;

public class CoursNotFoundException extends RuntimeException {
    public CoursNotFoundException(Long id) {
        super("Cours non trouvé avec l'id : " + id);
    }
}
