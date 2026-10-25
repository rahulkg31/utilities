package com.example.demo.model.response;

import java.util.UUID;

public class IdResponse {
    private UUID id;

    public IdResponse(UUID id) {
	this.id = id;
    }

    public IdResponse() {
    }

    public UUID getId() {
	return id;
    }

    public void setId(UUID id) {
	this.id = id;
    }

    @Override
    public String toString() {
	return "IdResponse{" + "id=" + id + '}';
    }
}
