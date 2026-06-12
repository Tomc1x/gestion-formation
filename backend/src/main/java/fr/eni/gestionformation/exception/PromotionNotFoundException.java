package fr.eni.gestionformation.exception;

public class PromotionNotFoundException extends RuntimeException {
    public PromotionNotFoundException(Long id) {
        super("Promotion non trouvée avec l'id : " + id);
    }
}
