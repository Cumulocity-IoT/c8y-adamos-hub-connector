package com.adamos.hubconnector.model.events;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventMapping {

    private String c8yEventType;
    private List<String> c8yDevices;
    private String adamosEventType;
    private List<String> c8yFragments;

    private String id;
    private String name;
    private boolean enabled;

    public EventMapping() {
        this.id = UUID.randomUUID().toString();
        this.name = "";
        this.enabled = false;

        this.c8yEventType = "";
        this.c8yDevices = new ArrayList<>();
        this.adamosEventType = "";
        this.c8yFragments = new ArrayList<>();
    }

}
