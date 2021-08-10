package com.github.alexdlaird.ngrok.http;

import java.util.List;

public class CaptureRequestResponse {
    private List<CaptureRequest> requests;
    private String uri;

    public List<CaptureRequest> getRequests() {
        return requests;
    }

    public String getUri() {
        return uri;
    }
}