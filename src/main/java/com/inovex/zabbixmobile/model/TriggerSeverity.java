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

package com.inovex.zabbixmobile.model;

import android.util.SparseArray;

import com.inovex.zabbixmobile.R;

public enum TriggerSeverity {
	ALL(R.string.severity_all, -1, 0, R.drawable.severity_not_classified), DISASTER(
			R.string.severity_disaster, 5, 1, R.drawable.severity_disaster), HIGH(
			R.string.severity_high, 4, 2, R.drawable.severity_high), AVERAGE(
			R.string.severity_average, 3, 3, R.drawable.severity_average), WARNING(
			R.string.severity_warning, 2, 4, R.drawable.severity_warning), INFORMATION(
			R.string.severity_information, 1, 5,
			R.drawable.severity_information), NOT_CLASSIFIED(
			R.string.severity_not_classified, 0, 6,
			R.drawable.severity_not_classified);

	private final int nameResourceId;
	private final int number;
	private final int position;
	private final int imageResourceId;
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

	TriggerSeverity(int nameResourceId, int n, int position, int imageResourceId) {
		this.nameResourceId = nameResourceId;
		number = n;
		this.position = position;
		this.imageResourceId = imageResourceId;
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

	public int getImageResourceId() {
		return imageResourceId;
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