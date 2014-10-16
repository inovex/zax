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
    API_1_3(0), API_1_4(1), API_2_0_TO_2_3(2), API_GT_2_4(3);

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
                return API_GT_2_4;
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
