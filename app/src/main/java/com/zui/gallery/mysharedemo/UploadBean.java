package com.zui.gallery.mysharedemo;

import java.io.Serializable;

/**
 * @author liuchao23 on 2017/8/23 11:11.
 */

public class UploadBean implements Serializable {

    private String fid;
    private String succ;
    private String error;
    private String error_code;
    private String request;
    private String http_code;

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getSucc() {
        return succ;
    }

    public void setSucc(String succ) {
        this.succ = succ;
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
