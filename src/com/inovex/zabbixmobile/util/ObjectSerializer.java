package com.inovex.zabbixmobile.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.inovex.zabbixmobile.model.ZabbixServer;

import android.util.Base64;

public class ObjectSerializer {

	public static <T extends Serializable> String objectToString(T object) {
		String serializedObject;
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream so = new ObjectOutputStream(bo);
			so.writeObject(object);
			so.close();
			serializedObject = new String(Base64.encode(bo.toByteArray(),
					Base64.DEFAULT));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return serializedObject;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T stringToObject(String str) {
		T deserializedObject;
		try {
			byte b[] = Base64.decode(str.getBytes(),
					Base64.DEFAULT);
			ByteArrayInputStream bi = new ByteArrayInputStream(b);
			ObjectInputStream si = new ObjectInputStream(bi);
			deserializedObject = (T) si.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return deserializedObject;
	}
}
