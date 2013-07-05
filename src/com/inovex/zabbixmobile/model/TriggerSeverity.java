package com.inovex.zabbixmobile.model;

import android.util.SparseArray;

public enum TriggerSeverity {
	ALL("all", -1, 0), DISASTER("disaster", 5, 1), HIGH("high", 4, 2), AVERAGE(
			"average", 3, 3), WARNING("warning", 2, 4), INFORMATION(
			"information", 1, 5), NOT_CLASSIFIED("not classified", 0, 6);

	private final String name;
	private final int number;
	private final int position;
	private static final SparseArray<TriggerSeverity> valuesByNumber;
	private static final SparseArray<TriggerSeverity> valuesByPosition;

	static {
		valuesByNumber = new SparseArray<TriggerSeverity>(
				TriggerSeverity.values().length);
		for (TriggerSeverity t : TriggerSeverity.values()) {
			valuesByNumber.put(t.getNumber(), t);
		}
		valuesByPosition = new SparseArray<TriggerSeverity>(
				TriggerSeverity.values().length);
		for (TriggerSeverity t : TriggerSeverity.values()) {
			valuesByPosition.put(t.getPosition(), t);
		}
	}

	TriggerSeverity(String name, int n, int position) {
		this.name = name;
		number = n;
		this.position = position;
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
		return valuesByNumber.get(n);
	}
	
	public static TriggerSeverity getSeverityByPosition(int n) {
		return valuesByPosition.get(n);
	}

	@Override
	public String toString() {
		return "{" + name + ", " + number + ", " + position + "}";
	}

}