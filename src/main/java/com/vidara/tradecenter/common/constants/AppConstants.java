package com.vidara.tradecenter.common.constants;

public final class AppConstants {

    // Private constructor - prevent instantiation
    private AppConstants() {
        throw new IllegalStateException("Constants class - cannot be instantiated");
    }

    // PAGINATION
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final int MAX_PAGE_SIZE = 50;

    // JWT
    public static final long JWT_EXPIRATION_MS = 86400000; // 24 hours
    public static final long JWT_REFRESH_EXPIRATION_MS = 604800000; // 7 days
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";

    // FILE UPLOAD
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final String UPLOAD_DIR = "uploads";
    public static final String PRODUCT_IMAGE_DIR = "products";
    public static final String PROFILE_IMAGE_DIR = "profiles";
    public static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    };

    // ROLES
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_AGENT = "ROLE_AGENT";

    // API PATHS
    public static final String API_BASE = "/api";
    public static final String AUTH_PATH = API_BASE + "/auth";
    public static final String USERS_PATH = API_BASE + "/users";
    public static final String PRODUCTS_PATH = API_BASE + "/products";
    public static final String CATEGORIES_PATH = API_BASE + "/categories";
    public static final String BRANDS_PATH = API_BASE + "/brands";
    public static final String FILES_PATH = API_BASE + "/files";
    public static final String ADMIN_PATH = "/admin";

    // VALIDATION
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 100;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 5000;
    public static final int MAX_SHORT_DESCRIPTION_LENGTH = 500;
}