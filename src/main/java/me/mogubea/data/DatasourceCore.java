package me.mogubea.data;

import me.mogubea.main.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class DatasourceCore {

    private final Set<PrivateDatasource> registeredSources = new HashSet<>();
//    private final Set<PrivateLogger<?>> registeredLoggers = new HashSet<>();

    private final Main plugin;
    private final String host, database, username, password;
    private final int port;

    private Connection connection;
    private boolean sqlConnected;

    public DatasourceCore(Main plugin) {
        this.plugin = plugin;
        FileConfiguration datasourceConfig = getDatasourceConfig();

        host = datasourceConfig.getString("host");
        port = datasourceConfig.getInt("port");
        username = datasourceConfig.getString("username");
        database = datasourceConfig.getString("database");
        password = datasourceConfig.getString("password");

        try {
            synchronized (plugin) {
                if (connection != null && !connection.isClosed()) return;

                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = getNewConnection();
                plugin.getSLF4JLogger().info("Successfully established an MySQL Connection!");
                sqlConnected = true;
            }
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getSLF4JLogger().trace("Failed to establish a MySQL Connection!", e);
        }
    }

    /**
     * Establish a new MySQL {@link Connection}.
     * @return the new {@link Connection}.
     */
    public Connection getNewConnection() throws SQLException {
        try {
            if (connection != null)
                connection.close();
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&character_set_server=utf8mb4", username, password);
        } catch (SQLException e) {
            plugin.getSLF4JLogger().trace("Could not establish a new MySQL Connection instance.", e);
        }
        return connection;
    }

    public void close(Object... c) {
        try {
            for (int i = 0; c != null && i < c.length; i++) {
                if (c[i] instanceof ResultSet && !((ResultSet) c[i]).isClosed()) {
                    ((ResultSet) c[i]).close();
                }
            }
            for (int i = 0; c != null && i < c.length; i++) {
                if (c[i] instanceof Statement && !((Statement) c[i]).isClosed()) {
                    ((Statement) c[i]).close();
                }
            }
            for (int i = 0; c != null && i < c.length; i++) {
                if (c[i] instanceof Connection && !((Connection) c[i]).isClosed()) {
                    ((Connection) c[i]).close();
                }
            }
        } catch (Exception e) {
            plugin.getSLF4JLogger().trace("Error closing object.", e);
        }
    }

    public @NotNull Main getPlugin() {
        return plugin;
    }

    protected void registerDatasource(PrivateDatasource source) {
        this.registeredSources.add(source);
    }

    public void doPostCreation() {
        registeredSources.forEach(PrivateDatasource::postCreation);
    }

    protected YamlConfiguration getDatasourceConfig() {
        File trackingFile = new File(plugin.getDataFolder() + "/DatasourceConfig.yml");
        if (!trackingFile.exists()) {
            try {
                if (trackingFile.createNewFile())
                    plugin.getSLF4JLogger().info("Created DatasourceConfig.yml");
            } catch (IOException e) {
                plugin.getSLF4JLogger().trace("There was an error creating the DatasourceConfig.yml", e);
            }
        }
        return YamlConfiguration.loadConfiguration(trackingFile);
    }

    /**
     * Save everything, assuming {@link #isOnline()} is true.
     */
    public void saveAll() {
        if (!isOnline()) return; // Don't bother if offline

        registeredSources.forEach(datasource -> {
            try {
                datasource.saveAll();
            } catch (Exception e) {
                plugin.getSLF4JLogger().trace("There was a problem with saving " + datasource.getClass().getPackageName(), e);
            }
        });

//        saveLogs();
    }

    /**
     * @return If the server is connected to the database and should be attempting loading and saving.
     */
    public boolean isOnline() {
        return sqlConnected;
    }

}
