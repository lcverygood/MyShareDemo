package com.zui.gallery.mysharedemo;

import java.io.Serializable;

/**
 * @author liuchao23 on 2017/8/23 10:52.
 */

public class ShareBean implements Serializable {

    private String fileToken;
    private String length;
    private String urlTag;
    private String error;
    private String error_code;
    private String request;
    private String http_code;

    public String getFileToken() {
        return fileToken;
    }

    public void setFileToken(String fileToken) {
        this.fileToken = fileToken;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getUrlTag() {
        return urlTag;
    }

    public void setUrlTag(String urlTag) {
        this.urlTag = urlTag;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getHttp_code() {
        return http_code;
    }

    public void setHttp_code(String http_code) {
        this.http_code = http_code;
    }
}
