package fr.eni.gestionformation.exception;

public class InscriptionNotFoundException extends RuntimeException {
    public InscriptionNotFoundException(Long eleveId, Long coursPlanifieId) {
        super("Aucune inscription individuelle trouvée pour l'élève " + eleveId + " sur le cours planifié " + coursPlanifieId);
    }
}
