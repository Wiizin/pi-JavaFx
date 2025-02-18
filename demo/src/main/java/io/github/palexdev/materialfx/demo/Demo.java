package io.github.palexdev.materialfx.demo;

import fr.brouillard.oss.cssfx.CSSFX;
import io.github.palexdev.materialfx.demo.controllers.DemoController;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Demo extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Initialize CSSFX
		CSSFX.start();

		// Set up MaterialFX theming
		UserAgentBuilder.builder()
				.themes(JavaFXThemes.MODENA)
				.themes(MaterialFXStylesheets.forAssemble(true))
				.setDeploy(true)
				.setResolveAssets(true)
				.build()
				.setGlobal();

		// Load the login FXML
		FXMLLoader loader = new FXMLLoader(MFXDemoResourcesLoader.loadURL("fxml/login.fxml"));
		Parent root = loader.load();

		// Create and configure the scene
		Scene scene = new Scene(root);
		//scene.setFill(Color.TRANSPARENT);

		// Configure and show the stage
		//primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Sportify - Login");
		primaryStage.show();
	}

	// Method to switch to main demo scene
	public static void loadMainScene(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(MFXDemoResourcesLoader.loadURL("fxml/Demo.fxml"));
		loader.setControllerFactory(c -> new DemoController(stage));
		Parent root = loader.load();

		Scene scene = new Scene(root);
		scene.setFill(Color.TRANSPARENT);
		//stage.initStyle(StageStyle.TRANSPARENT);
		stage.setScene(scene);
		stage.setTitle("Sportify");
	}

	public static void main(String[] args) {
		launch(args);
	}
}