package me.mogubea.utils;

public interface Dirty<T> {

    boolean isDirty();

    T setDirty(boolean dirty);

}
