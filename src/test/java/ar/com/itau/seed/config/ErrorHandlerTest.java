package ar.com.itau.seed.config;

import ar.com.itau.seed.adapter.rest.exception.BadRequestRestClientException;
import ar.com.itau.seed.adapter.rest.exception.RestClientGenericException;
import ar.com.itau.seed.adapter.rest.exception.TimeoutRestClientException;
import ar.com.itau.seed.config.exception.ForbiddenException;
import ar.com.itau.seed.config.exception.NotFoundException;
import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@DisplayName("ErrorHandler Test")
class ErrorHandlerTest {

    private static final String REQUEST_URL = "/api/v1/resources";
    private static final Long TRACE_ID = 128L;
    private static final Long SPAN_ID = 256L;
    private static final String TIMESTAMP_FIELD = "timestamp";

    private final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
    private final Tracer tracer = Mockito.mock(Tracer.class);
    private final Config config = new TestConfig().getConfig();
    private final Environment environment = Mockito.mock(Environment.class);

    private ErrorHandler handler;

    @BeforeEach
    void setup() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn(REQUEST_URL);
        final Span span = Mockito.mock(Span.class);
        Mockito.when(span.context()).thenReturn(TraceContext.newBuilder().traceId(TRACE_ID).spanId(SPAN_ID).build());
        Mockito.when(tracer.currentSpan()).thenReturn(span);

        Mockito.when(environment.getProperty(Mockito.anyString(), Mockito.anyString()))
                .thenAnswer(answer -> answer.getArgument(1));
        handler = new ErrorHandler(servletRequest, tracer, config, environment);
    }

    @Test
    @DisplayName("a Throwable should be mapped to a 500")
    void testHandleThrowable() {
        final Throwable ex = new Throwable("Some error");
        final ResponseEntity<ErrorHandler.ApiErrorResponse> response = handler.handleDefault(ex);

        final ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        final ErrorHandler.ApiErrorResponse expected =
                buildApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorCode.getReasonPhrase(), errorCode);

        Assertions.assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields(TIMESTAMP_FIELD)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("a BadRequestRestClientException should be mapped to a 400")
    void testHandleBadRequestRestClientException() {
        final BadRequestRestClientException ex = new BadRequestRestClientException(ErrorCode.BAD_REQUEST);
        final ResponseEntity<ErrorHandler.ApiErrorResponse> response = handler.handleBadRequest(ex);

        final ErrorHandler.ApiErrorResponse expected =
                buildApiErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ErrorCode.BAD_REQUEST);

        Assertions.assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields(TIMESTAMP_FIELD)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("a ConstraintViolationException should be mapped to a 400")
    void testHandleConstraintViolationException() {
        final ConstraintViolationException ex =
                new ConstraintViolationException("X must be positive", Collections.emptySet());
        final ResponseEntity<ErrorHandler.ApiErrorResponse> response = handler.handleBadRequest(ex);

        final ErrorHandler.ApiErrorResponse expected =
                buildApiErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ErrorCode.BAD_REQUEST);

        Assertions.assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields(TIMESTAMP_FIELD)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("a ForbiddenException should be mapped to a 403")
    void testHandleForbiddenException() {
        final ForbiddenException ex = new ForbiddenException(ErrorCode.FORBIDDEN);
        final ResponseEntity<ErrorHandler.ApiErrorResponse> response = handler.handleForbidden(ex);

        final ErrorHandler.ApiErrorResponse expected =
                buildApiErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), ErrorCode.FORBIDDEN);

        Assertions.assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields(TIMESTAMP_FIELD)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("a NotFoundException should be mapped to a 404")
    void testHandleNotFoundException() {
        final NotFoundException ex = new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND);
        final ResponseEntity<ErrorHandler.ApiErrorResponse> response = handler.handleNotFound(ex);

        final ErrorHandler.ApiErrorResponse expected =
                buildApiErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), ErrorCode.RESOURCE_NOT_FOUND);

        Assertions.assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields(TIMESTAMP_FIELD)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("a TimeoutRestClientException should be mapped to a 504")
    void testHandleTimeoutRestClientException() {
        final TimeoutRestClientException ex = new TimeoutRestClientException(ErrorCode.INTERNAL_ERROR);
        final ResponseEntity<ErrorHandler.ApiErrorResponse> response = handler.handleTimeout(ex);

        final ErrorHandler.ApiErrorResponse expected =
                buildApiErrorResponse(HttpStatus.GATEWAY_TIMEOUT, ex.getMessage(), ErrorCode.INTERNAL_ERROR);

        Assertions.assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields(TIMESTAMP_FIELD)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("a SocketTimeoutException wrapped in a ResourceAccessException should be mapped to a 504")
    void testHandleSocketTimeoutException() {
        final SocketTimeoutException cause = new SocketTimeoutException("");
        final ResourceAccessException ex = new ResourceAccessException("", cause);
        final ResponseEntity<ErrorHandler.ApiErrorResponse> response = handler.handle(ex);

        final ErrorCode errorCode = ErrorCode.TIMEOUT;
        final ErrorHandler.ApiErrorResponse expected =
                buildApiErrorResponse(HttpStatus.GATEWAY_TIMEOUT, errorCode.getReasonPhrase(), errorCode);

        Assertions.assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields(TIMESTAMP_FIELD)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("a ResourceAccessException without a cause should be mapped to a 500")
    void testHandleResourceAccessExceptionWithoutCause() {
        final ResourceAccessException ex = new ResourceAccessException("");
        final ResponseEntity<ErrorHandler.ApiErrorResponse> response = handler.handle(ex);

        final ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        final ErrorHandler.ApiErrorResponse expected =
                buildApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorCode.getReasonPhrase(), errorCode);

        Assertions.assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields(TIMESTAMP_FIELD)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("a ResourceAccessException should be mapped to a 500")
    void testHandleResourceAccessException() {
        final IOException cause = new IOException("");
        final ResourceAccessException ex = new ResourceAccessException("", cause);
        final ResponseEntity<ErrorHandler.ApiErrorResponse> response = handler.handle(ex);

        final ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        final ErrorHandler.ApiErrorResponse expected =
                buildApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorCode.getReasonPhrase(), errorCode);

        Assertions.assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields(TIMESTAMP_FIELD)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("a AsyncRequestTimeoutException should be mapped to a 504")
    void testHandleAsyncRequestTimeoutException() {
        final AsyncRequestTimeoutException ex = new AsyncRequestTimeoutException();
        final ResponseEntity<ErrorHandler.ApiErrorResponse> response = handler.handleTimeout(ex);

        final ErrorCode errorCode = ErrorCode.TIMEOUT;
        final ErrorHandler.ApiErrorResponse expected =
                buildApiErrorResponse(HttpStatus.GATEWAY_TIMEOUT, errorCode.getReasonPhrase(), errorCode);

        Assertions.assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields(TIMESTAMP_FIELD)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("a RestClientGenericException should be mapped to a 500")
    void testHandleRestClientGenericException() {
        final RestClientGenericException ex = new RestClientGenericException(ErrorCode.RESOURCE_NOT_FOUND);
        final ResponseEntity<ErrorHandler.ApiErrorResponse> response = handler.handleRestClient(ex);

        final ErrorHandler.ApiErrorResponse expected =
                buildApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ErrorCode.RESOURCE_NOT_FOUND);

        Assertions.assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields(TIMESTAMP_FIELD)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("a MissingServletRequestParameterException should be mapped to a 400")
    void testHandleMissingServletRequestParameterException() {
        final MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("aParameterName", "aParameterType");

        final ResponseEntity<ErrorHandler.ApiErrorResponse> response = handler.handleBadRequest(ex);

        final String expectedMessage = "Parameter " + ex.getParameterName() + " of type " +
                ex.getParameterType() + " is required";

        final ErrorHandler.ApiErrorResponse expected =
                buildApiErrorResponse(HttpStatus.BAD_REQUEST, expectedMessage, ErrorCode.BAD_REQUEST);

        Assertions.assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields(TIMESTAMP_FIELD)
                .isEqualTo(expected);
    }

    private ErrorHandler.ApiErrorResponse buildApiErrorResponse(
            HttpStatus httpStatus,
            String msg,
            ErrorCode errorCode
    ) {
        return ErrorHandler.ApiErrorResponse.builder()
                .timestamp(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC))
                .name(httpStatus.getReasonPhrase())
                .description(msg)
                .status(httpStatus.value())
                .code(config.getPrefix() + httpStatus.value() + ":" + errorCode.value())
                .resource(REQUEST_URL)
                .metadata(buildMetadata())
                .build();
    }

    private Map<String, String> buildMetadata() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("X-B3-TraceId", String.format("%016x", TRACE_ID));
        metadata.put("X-B3-SpanId", String.format("%016x", SPAN_ID));
        return Collections.unmodifiableMap(metadata);
    }

}
