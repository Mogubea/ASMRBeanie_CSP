package me.mogubea.statistics;

public class DirtyInteger extends DirtyVal<Integer> {
	
	public DirtyInteger(int value) {
		super(value);
	}

	@Override
	public DirtyVal<Integer> addToValue(Integer add) {
		value += add;
		return setDirty(true);
	}

	@Override
	public DirtyInteger setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}
	
}
