package com.example.demo.model.response;

public class AssignmentResponse {
    private String ip;
    private int assignedPort;

    public AssignmentResponse() {

    }

    public AssignmentResponse(String ip, int port) {
	this.ip = ip;
	this.assignedPort = port;
    }

    public String getIp() {
	return ip;
    }

    public void setIp(String ip) {
	this.ip = ip;
    }

    public int getAssignedPort() {
	return assignedPort;
    }

    public void setAssignedPort(int assignedPort) {
	this.assignedPort = assignedPort;
    }

    @Override
    public String toString() {
	return "PortAssignmentResponse{" + "ip='" + ip + '\'' + ", assignedPort=" + assignedPort + '}';
    }
}