package com.pitang.event;

import org.springframework.context.ApplicationEvent;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

public class RecursoCriadoEvent extends ApplicationEvent {

    private HttpServletResponse response;
    private UUID id;

    public RecursoCriadoEvent(Object source, HttpServletResponse response, UUID id) {
        super(source);
        this.response = response;
        this.id = id;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public UUID getId() {
        return id;
    }
}