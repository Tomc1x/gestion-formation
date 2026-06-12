package fr.eni.gestionformation.exception;

import java.util.List;

public class InscriptionHorsOrdreException extends RuntimeException {
    public InscriptionHorsOrdreException(Long eleveId, Long coursPlanifieId, List<String> coursManquants) {
        super("L'élève " + eleveId + " ne peut pas être inscrit au cours planifié " + coursPlanifieId
                + " hors-ordre : prérequis non couverts = " + coursManquants
                + ". Utilisez 'forcer=true' pour inscrire malgré tout.");
    }
}
