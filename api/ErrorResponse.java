package com.servitrust.api;

public class ErrorResponse {
    private String requestId;
    private String code;
    private String message;
    private String path;
    private long timestamp;

    public ErrorResponse() {}

    public ErrorResponse(String requestId, String code, String message, String path, long timestamp) {
        this.requestId = requestId;
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
