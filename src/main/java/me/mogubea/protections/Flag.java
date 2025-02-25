package me.mogubea.protections;

import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class Flag<T> {
	
	private final String identifier;
	private final String displayName;
	private final List<TextComponent> description = new ArrayList<>();
	private boolean needsPermission = false;
	private FlagCategory flagCategory = FlagCategory.MISCELLANEOUS; // Category for GUI
	
	protected final T def;

	private Consumer<Region> onUpdate;
	private boolean isPlayerFlag;

	protected Flag(@NotNull String identifier, @NotNull String displayName, @NotNull T defaultValue) {
		this.identifier = identifier;
		this.displayName = displayName;
		this.def = defaultValue;
	}

	@NotNull
	public String getIdentifier() {
		return identifier;
	}
	
	@NotNull
	public String getDisplayName() {
		return displayName;
	}

	@NotNull
	public T getDefault() {
		return def;
	}

	@NotNull
	public Flag<T> setDescription(TextComponent...desc) {
		if (!this.description.isEmpty()) throw new UnsupportedOperationException("A description was already given to Flag \""+ identifier +"\".");
		final int size = desc.length;
		for (int x = -1; ++x < size;)
			this.description.add(desc[x]);
		return this;
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public <F extends Flag<?>> F setDescription(List<TextComponent> desc) {
		if (!this.description.isEmpty()) throw new UnsupportedOperationException("A description was already given to Flag \""+ identifier +"\".");
		this.description.addAll(desc);
		return (F) this;
	}

	/**
	 * @return The description given to the flag on server boot.
	 */
	@NotNull
	public List<TextComponent> getDescription() {
		return description;
	}

	@NotNull
	@SuppressWarnings("unchecked")
	protected <F extends Flag<?>> F setConsumerOnUpdate(Consumer<Region> consumer) {
		if (this.onUpdate != null) throw new UnsupportedOperationException("An onUpdate consumer was already given to Flag \""+ identifier +"\".");
		this.onUpdate = consumer;
		return (F) this;
	}

	public void onUpdate(Region region) {
		if (this.onUpdate != null)
			onUpdate.accept(region);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	protected <F extends Flag<T>> F setNeedsPermission() {
		this.needsPermission = true;
		return (F) this;
	}

	@NotNull
	@SuppressWarnings("unchecked")
	protected <F extends Flag<T>> F setPlag() {
		this.isPlayerFlag = true;
		return (F) this;
	}

	public boolean isPlag() {
		return this.isPlayerFlag;
	}

	/**
	 * @return "bea.region.flag." + {@link #getIdentifier()}.
	 */
	@NotNull
	public String getPermission() {
		return "bea.region.flag." + getIdentifier();
	}
	
	public boolean needsPermission() {
		return this.needsPermission;
	}

	@NotNull
	@SuppressWarnings("unchecked")
	protected <F extends Flag<T>> F setFlagCategory(FlagCategory category) {
		this.flagCategory = category;
		return (F) this;
	}

	@NotNull
	public FlagCategory getCategory() {
		return this.flagCategory;
	}

	/**
	 * Validates the value given. For example; if there's an invalid entry, change it.
	 */
	public abstract T validateValue(T o);
	
	public abstract T parseInput(String input);
	
	public abstract T unmarshal(String o);
	
	public abstract String marshal(T o);

}
