/*
 * Copyright (C) 2022 Parisi Alessandro
 * This file is part of MaterialFX (https://github.com/palexdev/MaterialFX).
 *
 * MaterialFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MaterialFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MaterialFX.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.demo.controllers.AdminHomeController;

import io.github.palexdev.materialfx.controls.MFXIconWrapper;
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader;
import io.github.palexdev.materialfx.utils.ScrollUtils;
import io.github.palexdev.materialfx.utils.ToggleButtonsUtil;
import io.github.palexdev.materialfx.utils.others.loader.MFXLoader;
import io.github.palexdev.materialfx.utils.others.loader.MFXLoaderBean;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader.loadURL;

public class DemoController implements Initializable {
    private Stage stage;
    private double xOffset;
    private double yOffset;
    private ToggleGroup toggleGroup;

    @FXML
    private HBox windowHeader;

    @FXML
    private MFXFontIcon closeIcon;

    @FXML
    private MFXFontIcon minimizeIcon;

    @FXML
    private MFXFontIcon alwaysOnTopIcon;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private MFXScrollPane scrollPane;

    @FXML
    private VBox navBar;

    @FXML
    private StackPane contentPane;

    @FXML
    private StackPane logoContainer;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.toggleGroup = new ToggleGroup();
        ToggleButtonsUtil.addAlwaysOneSelectedSupport(toggleGroup);
        closeIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> Platform.exit());
        minimizeIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> ((Stage) rootPane.getScene().getWindow()).setIconified(true));
//		alwaysOnTopIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
//			boolean newVal = !stage.isFullScreen();
//			alwaysOnTopIcon.pseudoClassStateChanged(PseudoClass.getPseudoClass("fullscreen"), newVal);
//			stage.setFullScreen(newVal);
//		});


        initializeLoader();

        ScrollUtils.addSmoothScrolling(scrollPane);

        // The only way to get a fucking smooth image in this shitty framework
        Image image = new Image(MFXDemoResourcesLoader.load("sportify.png"), 64, 64, true, true);
        ImageView logo = new ImageView(image);
        Circle clip = new Circle(30);
        clip.centerXProperty().bind(logo.layoutBoundsProperty().map(Bounds::getCenterX));
        clip.centerYProperty().bind(logo.layoutBoundsProperty().map(Bounds::getCenterY));
        logo.setClip(clip);
        logoContainer.getChildren().add(logo);
    }

    private void initializeLoader() {
        MFXLoader loader = new MFXLoader();
        loader.addView(MFXLoaderBean.of("Users", loadURL("fxml/admin_home.fxml")).setBeanToNodeMapper(() -> createToggle("fas-users", "Users")).setDefaultRoot(true).get());
        loader.addView(MFXLoaderBean.of("Tournaments", loadURL("fxml/Buttons.fxml")).setBeanToNodeMapper(() -> createToggle("fas-trophy", "Tournaments")).setDefaultRoot(true).get());
        loader.addView(MFXLoaderBean.of("Products", loadURL("fxml/Buttons.fxml")).setBeanToNodeMapper(() -> createToggle("fas-box-open", "Products")).setDefaultRoot(true).get());
        loader.addView(MFXLoaderBean.of("Events", loadURL("fxml/Buttons.fxml")).setBeanToNodeMapper(() -> createToggle("fas-circle-dot", "Events")).setDefaultRoot(true).get());
        loader.addView(MFXLoaderBean.of("Teams", loadURL("fxml/Buttons.fxml")).setBeanToNodeMapper(() -> createToggle("fas-circle-dot", "Teams")).setDefaultRoot(true).get());
        loader.addView(MFXLoaderBean.of("Reclamations", loadURL("fxml/Buttons.fxml")).setBeanToNodeMapper(() -> createToggle("fas-circle-dot", "Reclamations")).setDefaultRoot(true).get());

        loader.setOnLoadedAction(beans -> {
            List<ToggleButton> nodes = beans.stream()
                    .map(bean -> {
                        ToggleButton toggle = (ToggleButton) bean.getBeanToNodeMapper().get();
                        toggle.setOnAction(event -> contentPane.getChildren().setAll(bean.getRoot()));
                        if (bean.isDefaultView()) {
                            contentPane.getChildren().setAll(bean.getRoot());
                            toggle.setSelected(true);
                        }
                        return toggle;
                    })
                    .toList();
            navBar.getChildren().setAll(nodes);
        });
        loader.start();
    }


    private ToggleButton createToggle(String icon, String text) {
        return createToggle(icon, text, 0);
    }

    private ToggleButton createToggle(String icon, String text, double rotate) {
        MFXIconWrapper wrapper = new MFXIconWrapper(icon, 24, 32);
        MFXRectangleToggleNode toggleNode = new MFXRectangleToggleNode(text, wrapper);
        toggleNode.setAlignment(Pos.CENTER_LEFT);
        toggleNode.setMaxWidth(Double.MAX_VALUE);
        toggleNode.setToggleGroup(toggleGroup);
        if (rotate != 0) wrapper.getIcon().setRotate(rotate);
        return toggleNode;
    }
}
