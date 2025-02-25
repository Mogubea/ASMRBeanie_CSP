package me.mogubea.statistics;

public class DirtyDouble extends DirtyVal<Double> {

	public DirtyDouble(double value) {
		super(value);
	}

	@Override
	public DirtyVal<Double> addToValue(Double add) {
		value += add;
		return setDirty(true);
	}

	@Override
	public DirtyDouble setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}
	
}
