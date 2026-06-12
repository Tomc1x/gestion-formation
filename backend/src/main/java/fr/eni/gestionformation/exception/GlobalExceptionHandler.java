package fr.eni.gestionformation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FiliereNotFoundException.class)
    public ResponseEntity<String> handleNotFound(FiliereNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(FiliereAlreadyExistsException.class)
    public ResponseEntity<String> handleConflict(FiliereAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(FiliereInUseException.class)
    public ResponseEntity<String> handleFiliereInUse(FiliereInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(CursusNotFoundException.class)
    public ResponseEntity<String> handleCursusNotFound(CursusNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(CoursNotFoundException.class)
    public ResponseEntity<String> handleCoursNotFound(CoursNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidInvitationTokenException.class)
    public ResponseEntity<String> handleInvalidInvitationToken(InvalidInvitationTokenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CycleDetectedException.class)
    public ResponseEntity<String> handleCycleDetected(CycleDetectedException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(ex.getMessage());
    }

    @ExceptionHandler(PromotionNotFoundException.class)
    public ResponseEntity<String> handlePromotionNotFound(PromotionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(CoursPlanifieNotFoundException.class)
    public ResponseEntity<String> handleCoursPlanifieNotFound(CoursPlanifieNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InscriptionAlreadyExistsException.class)
    public ResponseEntity<String> handleInscriptionAlreadyExists(InscriptionAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InscriptionNotFoundException.class)
    public ResponseEntity<String> handleInscriptionNotFound(InscriptionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InscriptionHorsOrdreException.class)
    public ResponseEntity<String> handleInscriptionHorsOrdre(InscriptionHorsOrdreException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
