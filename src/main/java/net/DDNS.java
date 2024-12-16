package net;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

@SuppressWarnings("deprecation")
public final class DDNS extends JavaPlugin {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onEnable() {
        saveDefaultConfig();
        password = getConfig().getString("password");
        domain = getConfig().getString("domain");
        new BukkitRunnable() {
            public void run() {
                try {
                    updateIP();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskTimerAsynchronously(this, 20*10, 20*10);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private static final String DYNAMIC_DNS_URL = "https://dynamicdns.park-your-domain.com/update";
    private static String domain = "";
    private static String password = "";
    private static String IpNow = null;

    public String getPublicIP() throws Exception {
        String url = "https://api.ipify.org";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }

    public void updateIP() {
        if (!isInternetAvailable()) return;
        try {
            if (IpNow == null) {
                IpNow = getPublicIP();
                getLogger().info("IP: " + IpNow);
            }
            if (!IpNow.equals(getPublicIP())) { // Si son diferentes es a cambiado
                String url = DYNAMIC_DNS_URL + "?host=@&domain=" + domain + "&password=" + password + "&ip=" + getPublicIP();
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    getLogger().info(String.format("La ip Se actualizó %s -> %s",IpNow ,getPublicIP()));
                    IpNow = getPublicIP();
                } else {
                    getLogger().severe("Error al actualizar la IP. Código de respuesta:" + responseCode);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isInternetAvailable() {
        try {
            URL url = new URL("https://www.google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000); // Tiempo límite para conectarse
            connection.connect();
            return connection.getResponseCode() >= 200 && connection.getResponseCode() < 300;
        } catch (IOException e) {
            return false;
        }
    }
}
