package com.om.bo.base;

import lombok.Data;

@Data
public class HttpResult implements java.io.Serializable {
    private int code;
    private String body;
}
