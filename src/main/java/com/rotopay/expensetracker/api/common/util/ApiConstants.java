package com.rotopay.expensetracker.api.common.util;

/**
 * API constants and configurations for all versions.
 */
public class ApiConstants {

    // API Base Paths
    public static final String API_BASE = "/api";
    public static final String API_V1 = "/api/v1";
    public static final String API_V2 = "/api/v2";

    // API Versions
    public static final String VERSION_1 = "1.0.0";
    public static final String VERSION_2 = "2.0.0";

    // HTTP Headers
    public static final String HEADER_API_VERSION = "X-API-Version";
    public static final String HEADER_USER_ID = "X-User-ID";
    public static final String HEADER_AUTH = "Authorization";

    // Pagination Constants
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // File Upload Constants
    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    public static final String ALLOWED_FILE_TYPE = "application/pdf";

    // Processing Queue Constants
    public static final int MAX_RETRIES = 3;
    public static final long PROCESSING_TIMEOUT = 120000; // 2 minutes

    // Cache Constants
    public static final long CACHE_EXPIRY_HOURS = 1;

    // Status Messages
    public static final String SUCCESS = "Success";
    public static final String ERROR = "Error";
    public static final String PENDING = "pending";
    public static final String PROCESSING = "processing";
    public static final String COMPLETED = "completed";
    public static final String FAILED = "failed";

    // Error Messages
    public static final String MSG_RESOURCE_NOT_FOUND = "Resource not found";
    public static final String MSG_UNAUTHORIZED = "User not authorized";
    public static final String MSG_INVALID_REQUEST = "Invalid request";
    public static final String MSG_INTERNAL_ERROR = "Internal server error";
    public static final String MSG_FILE_UPLOAD_ERROR = "File upload failed";
}