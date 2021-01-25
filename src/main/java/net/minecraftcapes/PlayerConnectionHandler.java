package net.minecraftcapes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PlayerConnectionHandler implements Listener {

    /**
     * Waterfall event handler
     * @param event LoginEvent
     */
    @EventHandler
    public void onPlayerJoin(LoginEvent event) {
        PendingConnection connection = event.getConnection();
        ProxyServer.getInstance().getLogger().info(String.format("[%s] [%s] Processing auth request", connection.getSocketAddress(), connection.getName()));
        event.setCancelReason(getResponse(connection));
        event.setCancelled(true);
    }

    /**
     * Handles API response as a message for the player
     * @param connection PendingConnection of player
     * @return
     */
    private BaseComponent[] getResponse(PendingConnection connection) {
        ComponentBuilder response = new ComponentBuilder("===============================\n\n").color(ChatColor.DARK_GRAY).strikethrough(true).bold(true);
        JsonObject responseJson = getApiResponse(connection).getAsJsonObject();

        if(responseJson != null && responseJson.get("success") != null && responseJson.get("success").getAsBoolean()) {
            response = response.append("Your authorization code is\n").reset();
            response = response.append("\u00BB ").color(ChatColor.RED).bold(true);
            response = response.append(responseJson.get("code").getAsString()).reset();
            response = response.append(" \u00AB").color(ChatColor.RED).bold(true);
        } else {
            if(responseJson != null && responseJson.get("banned") != null && responseJson.get("banned").getAsBoolean()) {
                response = response.append("The specified account has been banned for violating our terms of service.").reset().color(ChatColor.RED).bold(true);
            } else {
                response = response.append("Something went wrong\nPlease reconnect to try again").reset().color(ChatColor.RED).bold(true);
            }
        }

        response = response.append("\n\n===============================\n").color(ChatColor.DARK_GRAY).strikethrough(true).bold(true);
        response = response.append("\nJoin our Minecraft Server").reset().color(ChatColor.AQUA);
        response = response.append("\n\u25A0").color(ChatColor.GREEN).append(" Play.CapeCraft.Net ").color(ChatColor.YELLOW).append("\u25A0").color(ChatColor.GREEN);

        return response.create();
    }

    /**
     * Sends and receives the API response from the server
     * @param connection PendingConnection of player
     * @return
     */
    private JsonElement getApiResponse(PendingConnection connection) {
        ProxyServer.getInstance().getLogger().info(String.format("[%s] [%s] Processing api response", connection.getSocketAddress(), connection.getName()));
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(Main.AUTH_ENDPOINT)
                    .POST(getRequestData(connection))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ProxyServer.getInstance().getLogger().info(response.body());
            JsonElement responseJson = new JsonParser().parse(response.body());
            ProxyServer.getInstance().getLogger().info(String.format("[%s] [%s] Processed api response", connection.getSocketAddress(), connection.getName()));
            return responseJson;
        } catch(Exception e) {
            ProxyServer.getInstance().getLogger().severe(String.format("[%s] [%s] Processing api response failed", connection.getSocketAddress(), connection.getName()));
            return new JsonParser().parse("{ success: false }");
        }
    }

    /**
     * Gets the request data for the player
     * @param connection PendingConnection of player
     * @return
     */
    private HttpRequest.BodyPublisher getRequestData(PendingConnection connection) {
        String uuid = connection.getUniqueId().toString().replace("-", "");
        String username = connection.getName();

        Map<Object, Object> data = new HashMap<>();
        data.put("key", Main.API_KEY);
        data.put("uuid", uuid);
        data.put("username", username);

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
}