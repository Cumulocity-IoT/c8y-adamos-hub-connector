package com.adamos.hubconnector.model.events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
//@JsonSubTypes({
//    @JsonSubTypes.Type(value = AdamosEventProcessor.class, name = "AdamosEventProcessor")}
//)
public abstract class EventProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(EventProcessor.class);
	protected static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JodaModule());
	protected static final Configuration JSONPATH_CONFIG = Configuration.defaultConfiguration().addOptions(com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL);

	public abstract boolean processMessage(String message, EventRule rule);
	
	@JsonIgnore
	public abstract String getDefaultJsonTemplate(String message);
	
	protected DocumentContext getJsonContext(String payloadAsJson) {
		return JsonPath.using(JSONPATH_CONFIG).parse(payloadAsJson);
	}
	
	protected String getJsonData(Object data) {
		try {
			if (data instanceof net.minidev.json.JSONArray) {
				net.minidev.json.JSONArray array = (net.minidev.json.JSONArray) data;
				if (array.size() == 1 && !(array.get(0) instanceof java.util.LinkedHashMap)) {
					//return MAPPER.valueToTree(array.get(0)).toString();
					return array.get(0).toString();
				}
				return MAPPER.valueToTree(array).toString(); 
			}
//			return MAPPER.valueToTree(data).toString();
			return data.toString();
		} catch (Exception ex) {
			LOGGER.error("Error while converting selected jsonPath-Data: " + data.toString(), ex);
		}
		return "";
	}
	
	protected String getParsedJson(EventRule rule, DocumentContext deviceContext, DocumentContext messageContext) {
		try {
			String customJson = rule.getOutput();
			Pattern pattern = Pattern.compile("\\{\\{(.*)\\}\\}");
			Matcher matcher = pattern.matcher(customJson);
	        while (matcher.find()) {
	            String jpathStatement = matcher.group(1);
	            if (jpathStatement.toLowerCase().startsWith("device")) {
	            	customJson = customJson.replace(matcher.group(0), getJsonData(deviceContext.read(matcher.group(1).substring(6))).toString());
	            } else if (jpathStatement.toLowerCase().startsWith("message")) {
	            	customJson = customJson.replace(matcher.group(0), getJsonData(messageContext.read(matcher.group(1).substring(7))).toString());
	            }
	        }
	        return customJson;
		} catch (Exception ex) {
			LOGGER.error("Error while parsing the JSON-template of rule " + rule.getName() + " (id: "  + rule.getId() + ")", ex);
		}
		return "";
   }	
}
