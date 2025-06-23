package com.project.QLDSV_HTC;

import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


/**
 * JavaFX App
 */

@SpringBootApplication
public class App extends Application {

    private static Scene scene;
    private static App instance;
    private ConfigurableApplicationContext springContext;
    
    
    @Override
    public void init() {
    	instance = this;
        springContext = SpringApplication.run(App.class);
    }
    
    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("/fxml/LoginForm"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        fxmlLoader.setControllerFactory(App.getInstance().springContext::getBean);
        return fxmlLoader.load();
    }
    
    private static App getInstance() {
        return instance;
    }

    private static String currentUser;
    private static String currentRole;

    public static void setCurrentUser(String user, String role) {
        currentUser = user;
        currentRole = role;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentRole() {
        return currentRole;
    }
    
    @Override
    public void stop() throws Exception {
        if (springContext != null) {
            springContext.close();
        }
        System.out.println("Ứng dụng đã dừng.");
        System.exit(0);
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}