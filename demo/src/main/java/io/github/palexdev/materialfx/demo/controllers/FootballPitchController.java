package io.github.palexdev.materialfx.demo.controllers;

import com.google.gson.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders a static 4-3-3 pitch.  When {@link #loadLineupForMatch(int)} is
 * called (from the popup), it replaces the shirt numbers with the real
 * starting-XI retrieved from API-Football v3.
 */
public class FootballPitchController {

    /* ─────────────  FXML  ───────────── */
    @FXML private AnchorPane rootPane;
    @FXML private Canvas     pitchCanvas;

    /* ──────────  INTERNAL STATE  ─────── */
    private final List<VBox> teamBlueNodes = new ArrayList<>();
    private final List<VBox> teamRedNodes  = new ArrayList<>();

    /* your API-Football v3 key */
    private static final String API_KEY =
            "b193a3d817a74a92655cf7d01e597f83febe3e9288821dcea782e1fab797451e";

    /* ─────────────────────────────────── */
    public void initialize() {
        drawPitch();
        drawPlayers();
        /* No network call here – the popup will invoke loadLineupForMatch(...) */
    }

    /* ───────────  basic pitch  ───────── */
    private void drawPitch() {
        double w = pitchCanvas.getWidth();
        double h = pitchCanvas.getHeight();
        GraphicsContext g = pitchCanvas.getGraphicsContext2D();

        g.setFill(Color.web("#4CAF50")); g.fillRect(0, 0, w, h);
        g.setStroke(Color.WHITE); g.setLineWidth(2);

        double m = 50;                      // margin
        g.strokeRect(m, m, w - 2*m, h - 2*m);   // boundaries
        g.strokeLine(m, h/2, w - m, h/2);       // mid-line
        g.strokeOval(w/2-50, h/2-50, 100, 100); // centre circle

        double pbW = 200, pbH = 100;
        g.strokeRect(w/2-pbW/2, m,          pbW, pbH);          // top box
        g.strokeRect(w/2-pbW/2, h-m-pbH,    pbW, pbH);          // bottom box
    }

    private void drawPlayers() {
        rootPane.getChildren().removeIf(n -> "playerNode".equals(n.getId()));
        teamBlueNodes.clear(); teamRedNodes.clear();

        double h = pitchCanvas.getHeight();
        // 4-3-3 coordinates (top half)
        double[][] top = {
                {300,100},
                {150,150},{250,150},{350,150},{450,150},
                {200,250},{300,250},{400,250},
                {200,350},{300,350},{400,350}
        };
        // mirrored for bottom team
        double[][] bot = new double[top.length][2];
        for (int i=0;i<top.length;i++){ bot[i][0]=top[i][0]; bot[i][1]=h-top[i][1]; }

        Image blue = new Image(getClass().getResource("/blue_shirt.png").toExternalForm());
        Image red  = new Image(getClass().getResource("/red_shirt.png").toExternalForm());

        addShirts(top, blue, teamBlueNodes);
        addShirts(bot, red , teamRedNodes);
    }
    private void addShirts(double[][] coords, Image img, List<VBox> store){
        double size = 50;
        for (int i=0;i<coords.length;i++){
            ImageView iv = new ImageView(img); iv.setFitWidth(size); iv.setPreserveRatio(true);

            Text lbl = new Text(String.valueOf(i+1));
            lbl.setFill(Color.WHITE);
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            VBox box = new VBox(2, iv, lbl);
            box.setId("playerNode");
            box.setPrefSize(size, size+15);
            box.setAlignment(javafx.geometry.Pos.CENTER);
            box.setLayoutX(coords[i][0]-size/2);
            box.setLayoutY(coords[i][1]-size/2);

            rootPane.getChildren().add(box);
            store.add(box);
        }
    }

    /* ───────────────────────── PUBLIC API ────────────────────── */

    /**
     * Replaces the placeholders with the real line-up for this fixture.
     *
     * @param fixtureId the <b>fixture_id</b> returned by API-Football v3
     */
    /**
     * Downloads the starting XI for the given match and calls
     * {@link #updateLineup(JsonArray, JsonArray)} on the FX thread.
     *
     * @param matchId the numeric match_id you get from the events endpoint
     */
    // -----------------------------------------------------------------------------
//  FootballPitchController.java   –   drop this in, replacing the old version
// -----------------------------------------------------------------------------
    public void loadLineupForMatch(int matchId) {

        Task<JsonArray> task = new Task<>() {
            @Override protected JsonArray call() throws Exception {

                String url = "https://apiv3.apifootball.com/?action=get_lineups"
                        + "&match_id=" + matchId            //  <- USE match_id !
                        + "&APIkey="   + API_KEY;

                HttpResponse<String> resp = HttpClient.newHttpClient()
                        .send(HttpRequest.newBuilder(URI.create(url)).GET().build(),
                                HttpResponse.BodyHandlers.ofString());

                JsonElement root = JsonParser.parseString(resp.body());

                /* --------------- (1) error object -------------------------------- */
                if (root.isJsonObject() && root.getAsJsonObject().has("error")) {
                    int    code = root.getAsJsonObject().get("error").getAsInt();
                    String msg  = root.getAsJsonObject().get("message").getAsString();
                    System.out.printf("⚠ APIFootball error %d – %s%n", code, msg);
                    return new JsonArray();                     // -> nothing to show
                }

                /* --------------- (2) v3 wrapper  { "response":[ … ] } ----------- */
                if (root.isJsonObject()
                        && root.getAsJsonObject().has("response")
                        && root.getAsJsonObject().get("response").isJsonArray()) {

                    return root.getAsJsonObject().getAsJsonArray("response");
                }

                /* --------------- (3) v2 bare array  [ … ] ----------------------- */
                if (root.isJsonArray()) return root.getAsJsonArray();

                /* --------------- (4) NEW shape: { "<id>":{ … } }  --------------- */
                String idKey = String.valueOf(matchId);
                if (root.isJsonObject()
                        && root.getAsJsonObject().has(idKey)
                        && root.getAsJsonObject().get(idKey).isJsonObject()) {

                    JsonObject lineupObj = root.getAsJsonObject()
                            .getAsJsonObject(idKey)
                            .getAsJsonObject("lineup");

                    JsonArray out = new JsonArray();           // we’ll fabricate v2
                    out.add(lineupObj.getAsJsonObject("home"));
                    out.add(lineupObj.getAsJsonObject("away"));
                    return out;
                }

                System.out.println("⚠ unexpected payload: " + resp.body());
                return new JsonArray();                         // -> nothing to show
            }
        };

        task.setOnSucceeded(e -> {

            JsonArray arr = task.getValue();    // our fabricated/normalised array
            if (arr.size() < 2) {
                System.out.printf("⚠ Line-up not yet available for %d%n", matchId);
                return;
            }

            JsonObject home = arr.get(0).getAsJsonObject();
            JsonObject away = arr.get(1).getAsJsonObject();

            // keys can be startingXI, startXI, starting_lineups (v4) …
            JsonArray blue = getPlayersArray(home);
            JsonArray red  = getPlayersArray(away);

            Platform.runLater(() -> updateLineup(blue, red));
        });

        task.setOnFailed(e ->
                System.err.println("Line-up fetch failed: " + task.getException()));

        new Thread(task, "lineup-"+matchId).start();
    }

    /* helper that is tolerant to all known key names */
    private JsonArray getPlayersArray(JsonObject side) {
        if (side.has("startingXI"))       return side.getAsJsonArray("startingXI");
        if (side.has("startXI"))          return side.getAsJsonArray("startXI");
        if (side.has("starting_lineups")) return side.getAsJsonArray("starting_lineups");
        return new JsonArray();
    }



    /* ───────────────────────── helpers ──────────────────────── */
    private void updateLineup(JsonArray blueXI, JsonArray redXI){
        for (int i=0;i<blueXI.size() && i<teamBlueNodes.size(); i++) {
            String n = blueXI.get(i).getAsJsonObject()
                    .getAsJsonObject("player")
                    .get("name").getAsString();
            ((Text) teamBlueNodes.get(i).getChildren().get(1)).setText(n);
        }
        for (int i=0;i<redXI.size() && i<teamRedNodes.size(); i++) {
            String n = redXI.get(i).getAsJsonObject()
                    .getAsJsonObject("player")
                    .get("name").getAsString();
            ((Text) teamRedNodes.get(i).getChildren().get(1)).setText(n);
        }
    }
}
