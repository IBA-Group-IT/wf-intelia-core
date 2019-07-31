package com.ibagroup.wf.intelia.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonStringUtils {
	public final static ObjectMapper objectMapper = new ObjectMapper();

	public static <T> T jsonAsObject(String json, Class<T> objClass) {
		try {
			return objectMapper.readValue(json, objClass);
		} catch (Exception e) {
			throw new IllegalArgumentException("Can not de-serialize json " + json + " to object " + objClass, e);
		}
	}

	public static String asJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Can not json-serialize object " + object, e);
		}
	}
}
