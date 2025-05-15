package io.github.palexdev.materialfx.demo.net;

import com.google.gson.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 *  Minimal helper for all API-Football v3 requests.
 *  - Always returns a JsonArray (empty if server sent an error object).
 *  - Logs the server object once instead of crashing your app.
 */
public final class ApiFootball {
    private static final Logger LOG = Logger.getLogger(ApiFootball.class.getName());
    private ApiFootball() {}

    public static JsonArray getAsArray(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(10_000);
        c.setReadTimeout   (10_000);
        c.setRequestMethod("GET");

        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(c.getInputStream()))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);

            JsonElement root = JsonParser.parseString(sb.toString());
            if (!root.isJsonArray()) {                // 404, quota, plan-limit …
                LOG.warning("API-Football replied: " + root);
                return new JsonArray();
            }
            return root.getAsJsonArray();
        }
    }
}
