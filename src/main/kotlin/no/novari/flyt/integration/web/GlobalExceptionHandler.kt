package no.novari.flyt.integration.web

import io.github.oshai.kotlinlogging.KotlinLogging
import no.novari.flyt.integration.validation.ValidationErrorsFormattingService
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ResponseStatusException

@ControllerAdvice
class GlobalExceptionHandler(
    private val validationErrorsFormattingService: ValidationErrorsFormattingService,
) {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(exception: ResponseStatusException): ProblemDetail {
        logger.atWarn {
            message = "Handled response status exception with status={} and reason={}"
            arguments = arrayOf(exception.statusCode, exception.reason)
            cause = exception
        }

        return exception.reason
            ?.let { ProblemDetail.forStatusAndDetail(exception.statusCode, it) }
            ?: ProblemDetail.forStatus(exception.statusCode)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(exception: MethodArgumentNotValidException): ProblemDetail {
        val validationErrorMessage =
            validationErrorsFormattingService.format(exception.bindingResult)

        logger.atWarn {
            message = "Handled method argument validation failure: {}"
            arguments = arrayOf(validationErrorMessage)
            cause = exception
        }

        return ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            validationErrorMessage,
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(exception: Exception): ProblemDetail {
        logger.atError {
            message = "Handled unexpected exception"
            cause = exception
        }

        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred",
        )
    }
}
