package me.mogubea.data;

import me.mogubea.main.Main;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The base abstract class for Data Sources. Allows for nice organisation of Data Source files.
 */
public abstract class PrivateDatasource {
	
	protected final Main plugin;
	protected final DatasourceCore dc;
	
	protected PrivateDatasource(Main plugin) {
		this.plugin = plugin;
		this.dc = plugin.getDatasourceCore();
		this.dc.registerDatasource(this);
	}
	
	protected Connection getNewConnection() throws SQLException {
		return dc.getNewConnection();
	}
	
	protected Main getPlugin() {
		return plugin;
	}

	/**
	 * Fires after this {@link PrivateDatasource} has been registered by the {@link DatasourceCore}.
	 */
	protected void postCreation() {
	}

	/**
	 * Load all the relevant objects this datasource is managing the data of.
	 */
	public void loadAll() {

	}
	
	/**
	 * Save all the relevant objects this datasource is managing the data of.
	 */
	public void saveAll() throws Exception {

	}

	protected void close(Object...c) {
		dc.close(c);
	}

	protected YamlConfiguration getDatasourceConfig() {
		return dc.getDatasourceConfig();
	}

	public boolean isOnline() {
		return dc.isOnline();
	}

}
