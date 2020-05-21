package co.uk.minecraftcapes;

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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PlayerConnectionHandler implements Listener {

    /**
     * Waterfall event handler
     * @param event LoginEvent
     */
    @EventHandler
    public void onPlayerJoin(LoginEvent event) {
        PendingConnection player = event.getConnection();

        String uuid = player.getUniqueId().toString().replace("-", "");
        String username = player.getName();

        event.registerIntent(Main.instance);
        ProxyServer.getInstance().getScheduler().runAsync(Main.instance, new Runnable() {
            @Override
            public void run() {
                event.setCancelReason(getResponse(uuid, username));
                event.setCancelled(true);
                event.completeIntent(Main.instance);
            }
        });
    }

    /**
     * Handles API response as a message for the player
     * @param uuid Players UUID
     * @param username Players username
     * @return
     */
    private BaseComponent[] getResponse(String uuid, String username) {
        ComponentBuilder response = new ComponentBuilder("===============================\n\n").color(ChatColor.DARK_GRAY).strikethrough(true).bold(true);
        JsonObject responseJson = getApiResponse(uuid, username).getAsJsonObject();

        if(responseJson != null && responseJson.get("success") != null && responseJson.get("success").getAsBoolean()) {
            response = response.append("Your authorization code is\n").reset();
            response = response.append("\u00BB ").color(ChatColor.RED).bold(true);
            response = response.append(responseJson.get("code").getAsString()).reset();
            response = response.append(" \u00AB").color(ChatColor.RED).bold(true);
        } else {
            //todo needs changing
            response = response.append("Something went wrong\nPlease reconnect to try again").reset().color(ChatColor.RED).bold(true);
        }

        //todo needs changing
        response = response.append("\n\n===============================\n").color(ChatColor.DARK_GRAY).strikethrough(true).bold(true);
        response = response.append("\nJoin our Minecraft Server").reset().color(ChatColor.AQUA);
        response = response.append("\n\u25A0").color(ChatColor.GREEN).append(" Play.CapeCraft.Net ").color(ChatColor.YELLOW).append("\u25A0").color(ChatColor.GREEN);

        return response.create();
    }

    /**
     * Sends and receives the API response from the server
     * @param uuid Players UUID
     * @param username Players username
     * @return
     */
    private JsonElement getApiResponse(String uuid, String username) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getApiEndpoint(uuid, username)))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement responseJson = new JsonParser().parse(response.body());
            return responseJson;
        } catch(Exception e) {
            return new JsonParser().parse("{ success: false }");
        }
    }

    /**
     * Gets the API endpoint for the player
     * @param uuid Players UUID
     * @param username Players username
     * @return
     */
    private String getApiEndpoint(String uuid, String username) {
        return String.format(
                Main.AUTH_ENDPOINT,
                uuid,
                Main.API_KEY,
                username
        );
    }
}