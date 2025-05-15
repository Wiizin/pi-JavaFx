package io.github.palexdev.materialfx.demo.controllers;

import com.google.gson.*;
import io.github.palexdev.materialfx.demo.model.MatchInfo;
import io.github.palexdev.materialfx.demo.net.ApiFootball;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FootballAPI {

    /* ─────────────  FXML  ───────────── */
    @FXML private TextArea apiOutput;

    /* ─── your key & base URL ─── */
    private static final String API_KEY = "b193a3d817a74a92655cf7d01e597f83febe3e9288821dcea782e1fab797451e";
    private static final String BASE    = "https://apiv3.apifootball.com/?action=get_events";
    private static final Logger LOGGER  = Logger.getLogger(FootballAPI.class.getName());

    /* ─────────────  Handler  ─────────── */
    @FXML
    private void handleFetchData() {

        String date = LocalDate.now().toString();                                 // today (yyyy-MM-dd)
        String url  = BASE + "&from=" + date + "&to=" + date + "&APIkey=" + API_KEY;

        Task<List<MatchInfo>> task = new Task<>() {
            @Override
            protected List<MatchInfo> call() throws Exception {

                /* 1️⃣ single network call – helper returns array or [] and logs errors */
                JsonArray arr = ApiFootball.getAsArray(url);

                /* 2️⃣ map JSON → MatchInfo */
                List<MatchInfo> list = new ArrayList<>();
                for (JsonElement el : arr) {
                    JsonObject o = el.getAsJsonObject();

                    String hName = o.get("match_hometeam_name").getAsString();
                    String aName = o.get("match_awayteam_name").getAsString();

                    list.add(new MatchInfo(
                            hName,
                            aName,
                            o.get("match_date").getAsString() + "T" + o.get("match_time").getAsString(),
                            o.get("league_name").getAsString(),
                            o.has("match_stadium") ? o.get("match_stadium").getAsString() : "",
                            "LIVE".equalsIgnoreCase(o.get("match_status").getAsString()),
                            hName.substring(0, 3).toUpperCase(),
                            aName.substring(0, 3).toUpperCase(),
                            o.get("match_hometeam_id").getAsInt(),
                            o.get("match_awayteam_id").getAsInt(),
                            o.get("match_id").getAsString(),
                            safeInt(o.get("match_hometeam_score")),
                            safeInt(o.get("match_awayteam_score")),
                            o.get("match_status").getAsString()
                    ));
                }
                return list;
            }
        };

        /* 3️⃣ UI update (success / failure) */
        task.setOnSucceeded(e -> {
            List<MatchInfo> matches = task.getValue();
            Platform.runLater(() -> {
                apiOutput.setText("Fetched " + matches.size() + " matches.");
                PlayerFrontController.getInstance().updateMatches("Today", matches);
            });
        });

        task.setOnFailed(e ->
                Platform.runLater(() ->
                        apiOutput.setText("Fetch failed: " + task.getException().getMessage())));

        new Thread(task).start();
    }

    /* ─────────────  util  ───────────── */
    private static int safeInt(JsonElement el) {
        return el != null && !el.isJsonNull() && !el.getAsString().isEmpty()
                ? el.getAsInt()
                : 0;
    }
}
