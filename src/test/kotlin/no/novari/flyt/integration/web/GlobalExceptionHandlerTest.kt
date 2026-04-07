package no.novari.flyt.integration.web

import no.novari.flyt.integration.api.dto.IntegrationPostDto
import no.novari.flyt.integration.validation.ValidationErrorsFormattingService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.server.ResponseStatusException

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler(ValidationErrorsFormattingService())

    @Test
    fun shouldReturnGenericProblemDetailForForbiddenWithoutBodyException() {
        val response = handler.handleResponseStatusException(ForbiddenWithoutBodyException())

        assertEquals(403, response.status)
        assertEquals("Forbidden", response.title)
        assertNull(response.detail)
    }

    @Test
    fun shouldRenderSafeSpecificMessageForResponseStatusExceptions() {
        val response =
            handler.handleResponseStatusException(
                UnprocessableEntityException("Validation errors: ['activeConfigurationId invalid']"),
            )

        assertEquals(422, response.status)
        assertEquals("Unprocessable Entity", response.title)
        assertEquals("Validation errors: ['activeConfigurationId invalid']", response.detail)
    }

    @Test
    fun shouldHandleResponseStatusExceptionThrownByDependencies() {
        val response =
            handler.handleResponseStatusException(
                ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission"),
            )

        assertEquals(403, response.status)
        assertEquals("Forbidden", response.title)
        assertEquals("You do not have permission", response.detail)
    }

    @Test
    fun shouldReturnFormattedValidationErrorsForMethodArgumentNotValidException() {
        val target = IntegrationPostDto()
        val bindingResult = BeanPropertyBindingResult(target, "integrationPostDto")
        bindingResult.addError(
            FieldError(
                "integrationPostDto",
                "sourceApplicationId",
                null,
                false,
                arrayOf("NotNull"),
                emptyArray(),
                "must not be null",
            ),
        )
        val method =
            ValidationTarget::class.java.getDeclaredMethod(
                "createIntegration",
                IntegrationPostDto::class.java,
            )
        val exception = MethodArgumentNotValidException(MethodParameter(method, 0), bindingResult)

        val response = handler.handleMethodArgumentNotValidException(exception)

        assertEquals(422, response.status)
        assertEquals("Unprocessable Entity", response.title)
        assertEquals("Validation error: ['sourceApplicationId must not be null']", response.detail)
    }

    @Test
    fun shouldHideUnexpectedExceptionDetails() {
        val response = handler.handleUnexpectedException(RuntimeException("boom"))

        assertEquals(500, response.status)
        assertEquals("Internal Server Error", response.title)
        assertEquals("An unexpected error occurred", response.detail)
    }

    private class ValidationTarget {
        @Suppress("unused")
        fun createIntegration(integrationPostDto: IntegrationPostDto) {
            integrationPostDto.toString()
        }
    }
}
