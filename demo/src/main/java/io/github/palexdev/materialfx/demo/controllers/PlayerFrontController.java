package io.github.palexdev.materialfx.demo.controllers;

import com.google.gson.*;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.demo.model.MatchInfo;
import io.github.palexdev.materialfx.demo.model.Tournois;
import io.github.palexdev.materialfx.demo.services.TournoisService;
import io.github.palexdev.materialfx.demo.model.Matches;
import io.github.palexdev.materialfx.demo.net.ApiFootball;
import io.github.palexdev.materialfx.demo.services.MatchStatusEnum;
import io.github.palexdev.materialfx.demo.services.MatchesService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

public class PlayerFrontController implements Initializable {

    /* ────────────────  FXML  ──────────────── */
    @FXML private BorderPane mainContainer;
    @FXML private VBox       centerContent;
    @FXML private VBox       rightSidebar;

    /* ─────────────  CONSTANTS  ────────────── */
    public  static final Logger  LOGGER  = Logger.getLogger(PlayerFrontController.class.getName());
    public  static final String  FALLBACK_LOGO_PATH = "/io/github/palexdev/materialfx/demo/sportify.png";
    /* ─── APIFootball credentials ─── */
    private static final String API_KEY  = "b193a3d817a74a92655cf7d01e597f83febe3e9288821dcea782e1fab797451e";   // <- no “Y” at the end
    private static final String BASE_URL = "https://apiv3.apifootball.com/";
   private static final int[]   TOP5_LEAGUE_IDS = {152, 302, 195, 207, 61};

    private final Map<Integer, String> badgeCache = new HashMap<>();
    @FXML private MFXButton   friendlyButton;



    /* ──────────────  STATE  ──────────────── */
    private final Map<String, List<MatchInfo>> matchesByDay = new HashMap<>();
    private List<Tournois> tournaments = new ArrayList<>();
    private static PlayerFrontController instance;
    public  static PlayerFrontController getInstance() { return instance; }
    public  PlayerFrontController() { instance = this; }

    @Override public void initialize(java.net.URL u, java.util.ResourceBundle r) {
        styleRootLayout();
        fetchTopFiveLeagues();
        MatchesController mc = new MatchesController();
        // Load tournaments from your database service
    // You need to implement this service method
        friendlyButton.setOnAction(e -> {
            // either copy-paste your entire MatchesController.handleAddMatch() here,
            // or simply instantiate a new MatchesController and call its method:
            new MatchesController().handleAddMatch();
        });
    }

    /** Exactly the same friendly-match popup you already have in MatchesController. */
    private void openAddMatchDialog() {
        Stage dlg = new Stage(StageStyle.UNDECORATED);
        dlg.initModality(Modality.APPLICATION_MODAL);

        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setStyle("""
            -fx-background-color:white;
            -fx-border-color:#ff9800;
            -fx-border-width:2;
            -fx-border-radius:12;
            -fx-background-radius:12;
        """);

        String tfStyle = "-fx-text-fill:#1B1B3B;-fx-prompt-text-fill:#1B1B3B;"
                + "-fx-background-color:transparent;-fx-border-color:#2A2F4FFF;";
        Label title = new Label("Match Amical");
        title.setStyle("-fx-font-size:18;-fx-text-fill:#1B1B3B;");

        MFXTextField teamA = new MFXTextField(); teamA.setFloatingText("Team A"); teamA.setStyle(tfStyle);
        MFXTextField teamB = new MFXTextField(); teamB.setFloatingText("Team B"); teamB.setStyle(tfStyle);
        MFXTextField scoreA = new MFXTextField("0"); scoreA.setFloatingText("Score A"); scoreA.setStyle(tfStyle);
        MFXTextField scoreB = new MFXTextField("0"); scoreB.setFloatingText("Score B"); scoreB.setStyle(tfStyle);

        MFXComboBox<String> status = new MFXComboBox<>();
        status.setFloatingText("Status");
        status.getItems().addAll(
                Arrays.stream(MatchStatusEnum.values())
                        .map(MatchStatusEnum::getDisplayValue)
                        .toList()
        );
        status.setValue(MatchStatusEnum.UPCOMING.getDisplayValue());
        status.setStyle(tfStyle);

        MFXDatePicker datePicker = new MFXDatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.setFloatingText("Date");
        datePicker.setStyle(tfStyle);

        MFXTextField timeField = new MFXTextField(
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
        timeField.setFloatingText("Time (HH:mm)");
        timeField.setStyle(tfStyle);

        MFXTextField location = new MFXTextField();
        location.setFloatingText("Location");
        location.setStyle(tfStyle);

        MFXComboBox<String> tourn = new MFXComboBox<>();
        tourn.setFloatingText("Tournament");
        tourn.getItems().addAll(
                tournaments.stream().map(Tournois::getNom).toList()
        );
        if (!tournaments.isEmpty())
            tourn.setValue(tournaments.get(0).getNom());
        tourn.setStyle(tfStyle);

        MFXButton save   = new MFXButton("Save");   save.setStyle("-fx-background-color:#ff9800;-fx-text-fill:white;");
        MFXButton cancel = new MFXButton("Cancel"); cancel.setStyle("-fx-background-color:#ff9800;-fx-text-fill:white;");

        HBox btns = new HBox(12, save, cancel);
        btns.setAlignment(Pos.CENTER);

        form.getChildren().addAll(
                title, teamA, teamB, scoreA, scoreB,
                status, datePicker, timeField, location, tourn, btns
        );

        save.setOnAction(ev -> {
            try {
                // … validate, build Matches instance …
                Matches m = new Matches();
                m.setTeamAName(teamA.getText());
                m.setTeamBName(teamB.getText());
                m.setScoreTeamA(Integer.parseInt(scoreA.getText()));
                m.setScoreTeamB(Integer.parseInt(scoreB.getText()));
                m.setStatus(status.getValue());
                LocalTime t = LocalTime.parse(timeField.getText());
                m.setMatchTime(LocalDateTime.of(datePicker.getValue(), t));
                m.setLocationMatch(location.getText());
                int tid = tournaments.stream()
                        .filter(x -> x.getNom().equals(tourn.getValue()))
                        .findFirst().orElseThrow().getId();
                m.setIdTournoi(tid);

                new MatchesService().insert(m);
                dlg.close();
            } catch (Exception ex) {
                // show an alert…
                System.err.println("Error adding match: "+ex.getMessage());
            }
        });
        cancel.setOnAction(e -> dlg.close());

        dlg.setScene(new Scene(form, 340, 620));
        dlg.showAndWait();
    }

    /* ───────────── Data fetch (unchanged) ───────────── */
    /* ------------------------------------------------------------------------------------------------- */
    /*                                       DATA FETCHING  (API-SPORTS)                                 */
    /* ------------------------------------------------------------------------------------------------- */
    private void fetchTopFiveLeagues() {

        Task<List<MatchInfo>> task = new Task<>() {
            @Override protected List<MatchInfo> call() throws Exception {

                List<MatchInfo> out = new ArrayList<>();
                String today = LocalDate.now().toString();               // yyyy-MM-dd
                int[] leagues = {152, 302, 195, 207, 61};               // PL,LL,BUN,SER,L1 (APIFootball)

                for (int lg : leagues) {

                    String url = BASE_URL +
                            "?action=get_events"
                            + "&from="   + today
                            + "&to="     + today
                            + "&league_id=" + lg
                            + "&APIkey="    + API_KEY;

                    JsonArray arr = ApiFootball.getAsArray(url);         // <— safe

                    for (JsonElement el : arr) {
                        JsonObject o = el.getAsJsonObject();
                        out.add(toMatchInfo(o));                         // mapping below
                    }
                }
                return out;
            }
        };
        task.setOnSucceeded(e -> updateMatches("Today", task.getValue()));
        task.setOnFailed(e -> LOGGER.warning(task.getException().toString()));
        new Thread(task).start();
    }

    /* helper that translates ONE APIFootball object → MatchInfo  */
    private MatchInfo toMatchInfo(JsonObject o) {

        String hName = o.get("match_hometeam_name").getAsString();
        String aName = o.get("match_awayteam_name").getAsString();

        return new MatchInfo(
                hName,
                aName,
                o.get("match_date").getAsString() + "T" + o.get("match_time").getAsString(),
                o.get("league_name").getAsString(),
                o.has("match_stadium") ? o.get("match_stadium").getAsString() : "",
                "LIVE".equalsIgnoreCase(o.get("match_status").getAsString()),
                hName.substring(0,3).toUpperCase(),
                aName.substring(0,3).toUpperCase(),
                o.get("match_hometeam_id").getAsInt(),
                o.get("match_awayteam_id").getAsInt(),
                o.get("match_id").getAsString(),
                safeInt(o.get("match_hometeam_score")),
                safeInt(o.get("match_awayteam_score")),
                o.get("match_status").getAsString()
        );
    }

    private static int safeInt(JsonElement el){return el!=null&&!el.isJsonNull()&&!el.getAsString().isEmpty()?el.getAsInt():0;}

    /* ──────────────  UI refresh  ───────────── */
    public void updateMatches(String day, List<MatchInfo> list) {
        matchesByDay.put(day, list);
        Platform.runLater(() -> {
            centerContent.setSpacing(25);
            centerContent.getChildren().setAll(
                    createSection("Featured Match", list, true),
                    createSection("Live Now",       list, false)
            );
            rightSidebar.getChildren().setAll(createUpcomingSection(list));
            FadeTransition f1 = new FadeTransition(Duration.millis(500), centerContent);
            FadeTransition f2 = new FadeTransition(Duration.millis(500), rightSidebar);
            f1.setFromValue(0); f1.setToValue(1); f2.setFromValue(0); f2.setToValue(1); f1.play(); f2.play();
        });
    }

    /* ──────────────  Sections  ─────────────── */
    private VBox createSection(String title, List<MatchInfo> list, boolean featured) {
        VBox v = new VBox(18); v.getChildren().add(titleLabel(title));
        if (list==null||list.isEmpty()){ v.getChildren().add(blank()); return v; }
        if (featured) v.getChildren().add(matchCard(list.get(0), true));
        else {
            FlowPane fp = new FlowPane(Orientation.HORIZONTAL, 18,18); fp.setPadding(new Insets(8));
            list.stream().filter(MatchInfo::isLive).forEach(m -> fp.getChildren().add(matchCard(m,false)));
            ScrollPane sp = new ScrollPane(fp); sp.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
            sp.setVbarPolicy(ScrollBarPolicy.NEVER); sp.setStyle("-fx-background:transparent");
            v.getChildren().add(sp);
        }
        return v;
    }
    private VBox createUpcomingSection(List<MatchInfo> list){
        VBox box=new VBox(15); box.getChildren().add(titleLabel("Upcoming"));
        if(list!=null) list.forEach(m->box.getChildren().add(upcomingRow(m)));
        ScrollPane sp=new ScrollPane(box); sp.setFitToWidth(true); sp.setMaxHeight(640);
        sp.setStyle("-fx-background:transparent;-fx-padding:0 4 0 0");
        VBox wrap=new VBox(sp); wrap.setPadding(new Insets(0,0,0,4));
        return wrap;
    }

    /* ──────────────  Card / row builders  ─────────────── */
    private VBox matchCard(MatchInfo m, boolean big){
        VBox card=new VBox(20); card.setPadding(new Insets(big?28:20)); card.setPrefWidth(big?620:300);
        card.setStyle("""
            -fx-background-color: rgba(255,255,255,0.55);
            -fx-background-radius:20;
            -fx-border-radius:20;
            -fx-border-width:1.5;
            -fx-border-color: #d1d9ff;
            -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.12),24,0,0,8);
        """);
        HBox head=new HBox(8); head.setAlignment(Pos.CENTER_LEFT);
        Label live=new Label(m.isLive()?"LIVE":"UPCOMING");
        live.setStyle("-fx-font-weight:600;-fx-text-fill:"+ (m.isLive()? "#00c853":"#ff9100"));
        Label time=new Label(m.getTime()); time.setStyle("-fx-text-fill:#607d8b");
        Region r=new Region(); HBox.setHgrow(r, Priority.ALWAYS);
        head.getChildren().addAll(live,time,r,new Label("⚽"));
        HBox vs=new HBox(35); vs.setAlignment(Pos.CENTER);
        vs.getChildren().addAll(teamBadge(m.getHomeTeamId(),big,m.getHomeTeam()),
                new Label("vs"), teamBadge(m.getAwayTeamId(),big,m.getAwayTeam()));
        Label league=new Label(m.getLeague()); league.setStyle("-fx-text-fill:#536dfe;-fx-font-weight:bold");
        card.getChildren().addAll(head,vs,league);
        return card;
    }
    private HBox upcomingRow(MatchInfo m){
        HBox h= new HBox(8); h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(6));
        h.setStyle("-fx-background-radius:10;-fx-border-radius:10;-fx-border-color:#e0e7ff;-fx-background-color:rgba(255,255,255,0.6)");
        String t=m.getTime().substring(11,16);
        h.getChildren().addAll(new Label(t),logo(m.getHomeTeamId(),false),
                new Label(m.getHomeTeam()),new Label("vs"),
                logo(m.getAwayTeamId(),false),new Label(m.getAwayTeam()));
        return h;
    }
    private VBox teamBadge(int id, boolean big, String n){
        VBox v=new VBox(6,logo(id,big),text(n,big?15:13)); v.setAlignment(Pos.CENTER); return v;
    }

    /* ──────────────  Logo loader  ─────────────── */
    /* ------------------------------------------------------------------ LOGO --------------------------------------------------------------- */

    /** Returns a circular ImageView with the team badge, falling back to sportify.png once per badge. */
    public ImageView logo(int teamId, boolean big) {

        /*  look-up (or cache) the badge url  */
        String badge = badgeCache.computeIfAbsent(teamId, id -> {
            try {
                String teamUrl = BASE_URL +
                        "?action=get_teams&team_id=" + id +
                        "&APIkey=" + API_KEY;

                JsonArray arr = ApiFootball.getAsArray(teamUrl);
                return (arr.size() == 0) ? null
                        : arr.get(0).getAsJsonObject()
                        .get("team_badge").getAsString();

            } catch (Exception ex) {            // network / quota / JSON error
                return null;
            }
        });

        int size = big ? 60 : 32;
        ImageView iv = new ImageView();
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setClip(new Circle(size / 2.0, size / 2.0, size / 2.0));

        if (badge != null)
            iv.setImage(new Image(badge, true));
        else
            iv.setImage(new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(FALLBACK_LOGO_PATH))));

        return iv;
    }



    /* ──────────────  helpers  ─────────────── */



    private Label titleLabel(String t){Label l=text(t,19); l.setStyle(l.getStyle()+";-fx-font-weight:700"); return l;}
    private Label text(String s,int sz){Label l=new Label(s); l.setStyle("-fx-font-size:"+sz+";"); return l;}
    private Label blank(){Label l=new Label("—"); l.setStyle("-fx-text-fill:#9e9e9e"); return l;}

    private void styleRootLayout(){
        mainContainer.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0,0,0,1,true,
                        CycleMethod.NO_CYCLE,
                        new Stop(0,Color.web("#eef2ff")),
                        new Stop(0.4,Color.web("#f7f9ff")),
                        new Stop(1,Color.WHITE)
                ),CornerRadii.EMPTY,Insets.EMPTY)));
        centerContent.setPadding(new Insets(30));
        rightSidebar .setPadding(new Insets(30,18,30,12));
    }
}
