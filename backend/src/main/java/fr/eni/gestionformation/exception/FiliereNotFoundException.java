package fr.eni.gestionformation.exception;

public class FiliereNotFoundException extends RuntimeException {
    public FiliereNotFoundException(Long id) {
        super("Filière non trouvée avec l'id : " + id);
    }
}
