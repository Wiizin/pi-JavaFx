module MaterialFX.Demo {
	requires MaterialFX;
	requires VirtualizedFX;

	requires jdk.localedata;

	requires javafx.controls;
	requires javafx.fxml;
    requires javafx.media;

	requires fr.brouillard.oss.cssfx;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.ikonli.fontawesome5;
	requires org.scenicview.scenicview;
    requires java.sql;
    requires jakarta.mail;
    requires jbcrypt;
    requires java.net.http;
    requires javafx.swing;
    requires org.slf4j;


    opens io.github.palexdev.materialfx.demo;
	opens io.github.palexdev.materialfx.demo.controllers;
}