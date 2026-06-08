package fr.eni.gestionformation.exception;

public class FiliereAlreadyExistsException extends RuntimeException {
    public FiliereAlreadyExistsException(String name) {
        super("Une filière avec ce nom existe déjà : " + name);
    }
}
