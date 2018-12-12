package com.implementhit.OptimizeHIT.models;

import java.io.Serializable;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SerializableJsonObject extends JSONObject implements Serializable {
	private static final long serialVersionUID = 0L;
	
	public SerializableJsonObject() {
		super();
	}
	
	public SerializableJsonObject(JSONObject jo, java.lang.String[] names) throws JSONException  {
		super(jo, names);
	}
	
	public SerializableJsonObject(JSONTokener x) throws JSONException {
		super(x);
	}
	
	public SerializableJsonObject(java.util.Map map) throws JSONException {
		super(map);
	}
	
	public SerializableJsonObject(java.lang.String source) throws JSONException {
		super(source);
	}
	
	public SerializableJsonObject optSerializedJsonObject(String key) {
		JSONObject object = optJSONObject(key);
		
		if (object == null) {
			return null;
		}
		
		String[] keys = new String[object.length()];
		Iterator<?> iterator = object.keys();
		String copyKey = null;
		int index = 0;

		while (iterator.hasNext()) {
			copyKey = (String) iterator.next();
			keys[index] = copyKey;
			index++;
		}
		
		SerializableJsonObject serObject = null;
		
		try {
			serObject = new SerializableJsonObject(object, keys);
		} catch (JSONException e) {
			serObject = new SerializableJsonObject();
		}
		
		return serObject;
	}
}