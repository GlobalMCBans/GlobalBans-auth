package co.uk.minecraftcapes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

public class CodeGetter implements Listener
{
    String API_KEY;

    public CodeGetter(String API_KEY) {
        this.API_KEY = API_KEY;
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String url = "https://minecraftcapes.co.uk/api/" + this.API_KEY + "/auth/" + player.getUniqueId().toString().replace("-", "");

        JsonObject result = getAuthResult(url);

        if(result != null) {
            if (result.get("success").getAsBoolean()) {
                player.disconnect(new TextComponent("§eYour verification number is: §l" + result.get("authcode").getAsString() +
                        "§r\n§8§l-----------------------------------------------" +
                        "§r\nCheck out our survival server: Play.CapeCraft.Net"));
                } else {
                    player.disconnect(new TextComponent("§eYou don't have an account on our website!§r\nCheck out our survival server: Play.CapeCraft.Net"));
            }
        } else {
            player.disconnect(new TextComponent("§eAn error occurred, please try reconnect.§r\nCheck out our survival server: Play.CapeCraft.Net"));
        }
    }

    private static JsonObject getAuthResult(String url) {
        try {
            JsonObject result = readJsonFromUrl(url);
            return result;
        } catch (Exception localException) { }
        return null;
    }

    public static JsonObject readJsonFromUrl(String url) throws Exception {
        InputStream is = new URL(url).openStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        StringBuilder sb = new StringBuilder();

        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char)cp);
        }
        String jsonText = sb.toString();

        is.close();

        return (JsonObject)new JsonParser().parse(jsonText);
    }
}