package com.servitrust.api;

import jakarta.servlet.http.HttpServletRequest;

import static com.servitrust.api.RequestIdFilter.REQ_ID;

public class ResponseUtil {

    public static <T> ApiResponse<T> ok(HttpServletRequest req, String message, T data) {
        String requestId = (String) req.getAttribute(REQ_ID);
        return new ApiResponse<>(requestId, message, req.getRequestURI(), System.currentTimeMillis(), data);
    }
}
