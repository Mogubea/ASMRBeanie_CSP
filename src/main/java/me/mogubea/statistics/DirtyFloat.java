package me.mogubea.statistics;

public class DirtyFloat extends DirtyVal<Float> {

	public DirtyFloat(float value) {
		super(value);
	}

	@Override
	public DirtyVal<Float> addToValue(Float add) {
		value += add;
		return setDirty(true);
	}

	@Override
	public DirtyFloat setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}

}
