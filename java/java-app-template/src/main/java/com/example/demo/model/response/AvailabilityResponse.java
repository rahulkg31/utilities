package com.example.demo.model.response;

import java.util.List;

public class AvailabilityResponse {
    private String ip;
    private List<Integer> availablePorts;

    public String getIp() {
	return ip;
    }

    public void setIp(String ip) {
	this.ip = ip;
    }

    public List<Integer> getAvailablePorts() {
	return availablePorts;
    }

    public void setAvailablePorts(List<Integer> availablePorts) {
	this.availablePorts = availablePorts;
    }

    @Override
    public String toString() {
	return "PortsAvailabilityResponse{" + "ip='" + ip + '\'' + ", availablePorts=" + availablePorts + '}';
    }
}
