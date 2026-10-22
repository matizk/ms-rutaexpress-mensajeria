package com.rutaexpress.messaging.admin;

import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail invalid(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
    @ExceptionHandler({AmqpConnectException.class, DataAccessException.class})
    ProblemDetail unavailable(Exception exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "Mensajería temporalmente no disponible");
    }
    @ExceptionHandler(AmqpException.class)
    ProblemDetail declarationConflict(AmqpException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "RabbitMQ rechazó la operación; revisa los recursos y su configuración");
    }
}
