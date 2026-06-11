package fr.eni.gestionformation.exception;

public class FiliereInUseException extends RuntimeException {
    public FiliereInUseException(Long id) {
        super("La filière avec l'id " + id + " est utilisée par au moins un cursus et ne peut pas être supprimée");
    }
}
