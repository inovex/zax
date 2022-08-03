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

package com.inovex.zabbixmobile.data;


/**
 * API VERSIONS:
 * From 1.8.3 (maybe earlier) to 2.0 (excluded)   - API 1.3
 * Zabbix 2.0   - API 1.4
 * Zabbix 2.0.4 - API 2.0.4
 * ...
 * Zabbix 2.3.2 - API 2.4
 * Zabbix 2.4 - API 2.4
 * ...
 */

public enum ZabbixAPIVersion {
	API_1_3(0),
	API_1_4(1),
	API_2_0_TO_2_3(2),
	API_2_4(3),
	API_GT_3(4);


	private final int value;

	ZabbixAPIVersion(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ZabbixAPIVersion get(int value) {
		switch (value) {
			case 0:
				return API_1_3;
			case 1:
				return API_1_4;
			case 2:
				return API_2_0_TO_2_3;
			case 3:
				return API_2_4;
			case 4:
				return API_GT_3;
			default:
				return API_1_3;
		}
	}

	public boolean isGreater1_4() {
		return value > 0;
	}

	public boolean isGreater2_3() {
		return value > 2;
	}
}
