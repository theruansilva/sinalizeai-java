package br.com.fiap.semaforo.exception;

import br.com.fiap.semaforo.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErroResponse(LocalDateTime.now(), 400, "Bad Request",
                "Dados inválidos", req.getRequestURI(), campos));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return erro(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regra(RegraNegocioException ex, HttpServletRequest req) {
        return erro(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class})
    public ResponseEntity<ErroResponse> malformado(Exception ex, HttpServletRequest req) {
        return erro(HttpStatus.BAD_REQUEST, "Requisição malformada ou parâmetro inválido", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponse> credenciais(AuthenticationException ex, HttpServletRequest req) {
        return erro(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos", req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> conflito(DataIntegrityViolationException ex, HttpServletRequest req) {
        return erro(HttpStatus.CONFLICT, "Operação viola uma restrição de integridade dos dados", req);
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus status, String msg, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(status.value(), status.getReasonPhrase(), msg, req.getRequestURI()));
    }
}
