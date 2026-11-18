package br.com.fiap.semaforo.exception;

import br.com.fiap.semaforo.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
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

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
            HandlerMethodValidationException.class, PropertyReferenceException.class})
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex, HttpServletRequest req) {
        if (ex instanceof ErrorResponse er) {
            int status = er.getStatusCode().value();
            String msg = switch (status) {
                case 404 -> "Rota não encontrada";
                case 405 -> "Método HTTP não suportado nesta rota";
                case 415 -> "Content-Type não suportado; use application/json";
                default -> "Requisição não pôde ser processada";
            };
            return erro(HttpStatus.valueOf(status), msg, req);
        }
        log.error("Erro inesperado em {}", req.getRequestURI(), ex);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", req);
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus status, String msg, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(status.value(), status.getReasonPhrase(), msg, req.getRequestURI()));
    }
}
