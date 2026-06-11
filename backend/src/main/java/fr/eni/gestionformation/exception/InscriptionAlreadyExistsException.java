package fr.eni.gestionformation.exception;

public class InscriptionAlreadyExistsException extends RuntimeException {
    public InscriptionAlreadyExistsException(Long eleveId, Long coursPlanifieId) {
        super("L'élève " + eleveId + " est déjà inscrit (directement ou via sa promotion) au cours planifié " + coursPlanifieId);
    }
}
