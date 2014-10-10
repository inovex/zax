package com.inovex.zabbixmobile.util;

import java.io.IOException;

import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * json object wrapper parses the attributes of json objects direct from stream
 */
public class JsonObjectReader {
	private final JsonParser parser;
	private boolean isParsed;

	/**
	 * @param parser
	 */
	public JsonObjectReader(JsonParser parser) {
		this.parser = parser;
	}

	/**
	 * reads the json object until its end. works recursively.
	 * 
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public void finishParsing() throws JsonParseException, IOException {
		if (isParsed)
			return;

		int offen = 1;
		do {
			JsonToken tk = parser.nextToken();
			if (tk == JsonToken.START_OBJECT)
				offen++;
			else if (tk == JsonToken.END_OBJECT)
				offen--;
		} while (offen > 0);
		parser.nextToken();
		isParsed = true;
	}

	/**
	 * @return current property
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public String getCurrentName() throws JsonParseException, IOException {
		return parser.getCurrentName();
	}

	/**
	 * @return JSon array or object reader for the current property
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public JsonArrayOrObjectReader getJsonArrayOrObjectReader()
			throws JsonParseException, IOException {
		boolean ok = false;
		if (parser.getCurrentToken() == JsonToken.START_ARRAY
				|| parser.getCurrentToken() == JsonToken.START_OBJECT) {
			ok = true;
		} else {
			parser.nextToken();
			if (parser.getCurrentToken() == JsonToken.START_ARRAY
					|| parser.getCurrentToken() == JsonToken.START_OBJECT) {
				ok = true;
			}
		}
		if (!ok) {
			Log.e("JsonObjectReader", "token: " + parser.getCurrentToken());
			Log.e("JsonObjectReader", "name: " + parser.getCurrentName());
			Log.e("JsonObjectReader", "text: " + parser.getText());
			throw new IllegalStateException();
		}
		return new JsonArrayOrObjectReader(parser);
	}

	/**
	 * @return value of current property as string
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public String getText() throws JsonParseException, IOException {
		return parser.getText();
	}

	/**
	 * @return true, if object was read to the end.
	 */
	public boolean isParsed() {
		return isParsed;
	}

	/**
	 * skips current property
	 * 
	 * @return true, if yet another property exists. false, if the object was
	 *         read to the end.
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public boolean nextProperty() throws JsonParseException, IOException {
		if (isParsed)
			return false;

		JsonToken tk;
		do {
			tk = parser.nextToken();
			if (tk == JsonToken.START_ARRAY || tk == JsonToken.START_OBJECT) {
				parser.skipChildren();
			}
			if (tk == JsonToken.END_OBJECT) {
				isParsed = true;
				return false;
			}
		} while (tk != JsonToken.FIELD_NAME);
		return true;
	}

	/**
	 * jumps to the next json token (low level)
	 * 
	 * @return false, if the object was read to the end.
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public boolean nextToken() throws JsonParseException, IOException {
		if (isParsed)
			return false;

		JsonToken tk = parser.nextToken();
		if (tk == JsonToken.END_OBJECT) {
			isParsed = true;
		}
		return !isParsed();
	}

	/**
	 * reads to the next property
	 * 
	 * @return false, if the object was read to the end
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public boolean nextValueToken() throws JsonParseException, IOException {
		if (isParsed)
			return false;

		JsonToken tk = parser.nextToken();
		if (tk == JsonToken.END_OBJECT) {
			isParsed = true;
			return false;
		}
		if (tk == JsonToken.FIELD_NAME) {
			return nextToken();
		}
		return true;
	}

}
