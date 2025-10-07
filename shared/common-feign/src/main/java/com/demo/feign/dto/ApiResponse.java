package com.demo.feign.dto;

/**
 * Common response DTOs for API communication
 */
public class ApiResponse {

    /**
     * Generic success response
     */
    public record SuccessResponse(
        String message,
        Object data
    ) {
        public static SuccessResponse of(String message, Object data) {
            return new SuccessResponse(message, data);
        }
        
        public static SuccessResponse of(String message) {
            return new SuccessResponse(message, null);
        }
    }

    /**
     * Error response
     */
    public record ErrorResponse(
        String error,
        String message,
        int code
    ) {
        public static ErrorResponse of(String error, String message, int code) {
            return new ErrorResponse(error, message, code);
        }
    }

    /**
     * Command response with ID
     */
    public record CommandResponse(
        String id,
        String message,
        String status
    ) {
        public static CommandResponse success(String id, String message) {
            return new CommandResponse(id, message, "SUCCESS");
        }
        
        public static CommandResponse failure(String id, String message) {
            return new CommandResponse(id, message, "FAILURE");
        }
    }

    /**
     * Paged response for queries
     */
    public record PagedResponse<T>(
        java.util.List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
    ) {
        public static <T> PagedResponse<T> of(java.util.List<T> content, int page, int size, long totalElements) {
            int totalPages = (int) Math.ceil((double) totalElements / size);
            boolean hasNext = page < totalPages - 1;
            boolean hasPrevious = page > 0;
            
            return new PagedResponse<>(
                content, page, size, totalElements, totalPages, hasNext, hasPrevious
            );
        }
    }
}