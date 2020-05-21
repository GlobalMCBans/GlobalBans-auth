package co.uk.minecraftcapes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CodeGetter implements Listener {

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        player.disconnect(getResponse(player));
    }

    private BaseComponent[] getResponse(ProxiedPlayer player) {
        ComponentBuilder response = new ComponentBuilder("===============================\n\n").color(ChatColor.DARK_GRAY).strikethrough(true).bold(true);
        JsonObject responseJson = getApiResponse(player).getAsJsonObject();

        if(responseJson != null) {
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

    private JsonElement getApiResponse(ProxiedPlayer player) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getApiEndpoint(player)))
                    .POST(HttpRequest.BodyPublishers.ofString("username=" + player.getName()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            JsonElement responseJson = new JsonParser().parse(response.body());
            return responseJson;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the API endpoint for a user
     * @param player Player object
     * @return Formatted API Endpoint
     */
    private String getApiEndpoint(ProxiedPlayer player) {
        return String.format(Main.API_ENDPOINT, player.getUniqueId().toString().replace("-", ""));
    }
}