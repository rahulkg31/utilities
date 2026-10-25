package com.example.demo.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class IpRequest {

    @NotBlank(message = "ip is required")
    @Pattern(
        regexp = "^((25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d?\\d)$",
        message = "ip must be a valid IPv4 address"
    )
    private String ip;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "IpRequest{" +
                "ip='" + ip + '\'' +
                '}';
    }
}