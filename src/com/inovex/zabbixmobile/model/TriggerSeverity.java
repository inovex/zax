package com.inovex.zabbixmobile.model;

public enum TriggerSeverity {
	ALL("all", -1, 0),
	DISASTER("disaster", 5, 1),
	HIGH("high", 4, 2),
	AVERAGE("average", 3, 3),
	WARNING("warning", 2, 4),
	INFORMATION("information", 1, 5),
	NOT_CLASSIFIED("not classified", 0, 6);
	
	private final String name;
	private final int number;
	private final int position;
	
	TriggerSeverity(String name, int n, int position) {
		this.name = name;
		number = n;
		this.position= position; 
	}
	
	public int getPosition() {
		return position;
	}

	public String getName() {
		return name;
	}

	public int getNumber() {
		return number;
	}
	
	public static TriggerSeverity getSeverityByNumber(int n) {
		return ALL;
	}
	
	public String toString() {
		return "{" + name + ", " + number + ", " + position + "}";
	}
	
}