package com.adamos.hubconnector.model.events;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class EventRules {
	private EventDirection direction = EventDirection.FROM_HUB;
	private List<EventRule> rules;

	public EventRules(EventDirection direction) {
		this.direction = direction;
		this.rules = new ArrayList<EventRule>();
	}
}
