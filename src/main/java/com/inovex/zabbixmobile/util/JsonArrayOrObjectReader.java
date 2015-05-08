/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.inovex.zabbixmobile.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * json array wrapper parses a json array direct from stream
 */
public class JsonArrayOrObjectReader {
	private final JsonParser parser;
	private JsonObjectReader lastObject;
	private final boolean isObject;

	/**
	 * @param parser
	 */
	public JsonArrayOrObjectReader(JsonParser parser) {
		this.parser = parser;
		isObject = (parser.getCurrentToken() == JsonToken.START_OBJECT);
	}

	/**
	 * closes the json parser and the stream
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		parser.close();
	}

	/**
	 * reads until the array get closed. works recursively.
	 * 
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public void finishParsing() throws JsonParseException, IOException {
		int offen = 1;
		do {
			JsonToken tk = parser.nextToken();
			if (isObject) {
				if (tk == JsonToken.START_OBJECT)
					offen++;
				else if (tk == JsonToken.END_OBJECT)
					offen--;
			} else {
				if (tk == JsonToken.START_ARRAY)
					offen++;
				else if (tk == JsonToken.END_ARRAY)
					offen--;
			}
		} while (offen > 0);
	}

	/**
	 * reads until the next json object
	 * 
	 * @return json object wrapper
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public JsonObjectReader next() throws JsonParseException, IOException {
		JsonToken tk = parser.nextToken();
		if (isObject) {
			if (tk == JsonToken.END_OBJECT)
				return null;
		} else {
			if (tk == JsonToken.END_ARRAY)
				return null;
		}
		if (lastObject != null && !lastObject.isParsed()) {
			throw new IllegalStateException(
					"The last object must be read complete before the next object can be parsed.");
		}
		parser.nextToken(); // { Token
		return lastObject = new JsonObjectReader(parser);
	}
}
