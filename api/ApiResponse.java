package com.servitrust.api;

public class ApiResponse<T> {
    private String requestId;
    private String message;
    private String path;
    private long timestamp;
    private T data;

    public ApiResponse() {}

    public ApiResponse(String requestId, String message, String path, long timestamp, T data) {
        this.requestId = requestId;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
        this.data = data;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
