package fr.eni.gestionformation.exception;

public class CoursPlanifieNotFoundException extends RuntimeException {
    public CoursPlanifieNotFoundException(Long id) {
        super("Cours planifié non trouvé avec l'id : " + id);
    }
}
