package com.inovex.zabbixmobile.model;

import android.util.SparseArray;

import com.inovex.zabbixmobile.R;

public enum TriggerSeverity {
	ALL(R.string.severity_all, -1, 0), DISASTER(R.string.severity_disaster, 5,
			1), HIGH(R.string.severity_high, 4, 2), AVERAGE(
			R.string.severity_average, 3, 3), WARNING(
			R.string.severity_warning, 2, 4), INFORMATION(
			R.string.severity_information, 1, 5), NOT_CLASSIFIED(
			R.string.severity_not_classified, 0, 6);

	private final int nameResourceId;
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

	TriggerSeverity(int nameResourceId, int n, int position) {
		this.nameResourceId = nameResourceId;
		number = n;
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	public int getNameResourceId() {
		return nameResourceId;
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
		return "{" + nameResourceId + ", " + number + ", " + position + "}";
	}

}