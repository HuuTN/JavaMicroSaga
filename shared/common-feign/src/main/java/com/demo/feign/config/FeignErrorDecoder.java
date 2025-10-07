package com.demo.feign.config;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * Custom Feign error decoder to handle different types of errors
 * and provide meaningful exception messages.
 */
public class FeignErrorDecoder implements ErrorDecoder {

    private static final Logger logger = LoggerFactory.getLogger(FeignErrorDecoder.class);
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        logger.debug("Decoding error for method: {}, status: {}", methodKey, response.status());

        HttpStatus httpStatus = HttpStatus.valueOf(response.status());
        String errorMessage = String.format("Error calling %s: %s", methodKey, httpStatus.getReasonPhrase());

        switch (httpStatus) {
            case BAD_REQUEST:
                return new FeignBadRequestException(errorMessage, response);
            
            case UNAUTHORIZED:
                return new FeignUnauthorizedException(errorMessage, response);
            
            case FORBIDDEN:
                return new FeignForbiddenException(errorMessage, response);
            
            case NOT_FOUND:
                return new FeignNotFoundException(errorMessage, response);
            
            case CONFLICT:
                return new FeignConflictException(errorMessage, response);
            
            case INTERNAL_SERVER_ERROR:
                return new FeignInternalServerErrorException(errorMessage, response);
            
            case SERVICE_UNAVAILABLE:
                return new FeignServiceUnavailableException(errorMessage, response);
            
            case GATEWAY_TIMEOUT:
                return new FeignTimeoutException(errorMessage, response);
            
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }

    /**
     * Base class for Feign exceptions
     */
    public static abstract class FeignClientException extends FeignException {
        protected FeignClientException(String message, Response response) {
            super(response.status(), message, response.request(), null, response.headers());
        }
    }

    /**
     * 400 Bad Request
     */
    public static class FeignBadRequestException extends FeignClientException {
        public FeignBadRequestException(String message, Response response) {
            super(message, response);
        }
    }

    /**
     * 401 Unauthorized
     */
    public static class FeignUnauthorizedException extends FeignClientException {
        public FeignUnauthorizedException(String message, Response response) {
            super(message, response);
        }
    }

    /**
     * 403 Forbidden
     */
    public static class FeignForbiddenException extends FeignClientException {
        public FeignForbiddenException(String message, Response response) {
            super(message, response);
        }
    }

    /**
     * 404 Not Found
     */
    public static class FeignNotFoundException extends FeignClientException {
        public FeignNotFoundException(String message, Response response) {
            super(message, response);
        }
    }

    /**
     * 409 Conflict
     */
    public static class FeignConflictException extends FeignClientException {
        public FeignConflictException(String message, Response response) {
            super(message, response);
        }
    }

    /**
     * 500 Internal Server Error
     */
    public static class FeignInternalServerErrorException extends FeignClientException {
        public FeignInternalServerErrorException(String message, Response response) {
            super(message, response);
        }
    }

    /**
     * 503 Service Unavailable
     */
    public static class FeignServiceUnavailableException extends FeignClientException {
        public FeignServiceUnavailableException(String message, Response response) {
            super(message, response);
        }
    }

    /**
     * 504 Gateway Timeout
     */
    public static class FeignTimeoutException extends FeignClientException {
        public FeignTimeoutException(String message, Response response) {
            super(message, response);
        }
    }
}