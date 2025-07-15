package org.openjfx.customfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;

import com.github.kwhat.jnativehook.GlobalScreen;

import java.awt.AWTException;
import java.awt.Robot;

/* ===JLINK COMPILE INFO ===
 * jlink ^
  --module-path "C:\Program Files\Java\jdk-22\jmods";"C:\Program Files\javafx-sdk-24.0.1\lib" ^
  --add-modules java.base,java.desktop,java.logging,javafx.controls,javafx.fxml ^
  --output runtime ^
  --compress=2 --no-header-files --no-man-pages
 * 
 * 
 * Directly copy all .dlls from C:\Program Files\javafx-sdk-24.0.1\bin
 * 
 * Use the runtime folder generated from jlink as the primary jre used in Launch4j.
 * 
 */


/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("testing"), 600, 400);
        stage.setResizable(false);
        stage.setTitle("Advanced Autoclicker");
        stage.setScene(scene);
        
        stage.setOnCloseRequest(e -> {
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Platform.exit(); // Ensure JavaFX exits
            System.exit(0);  // Optional hard kill
        });
        
        stage.show();
        
        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
    	
    	//Setup the keybind listener. By default the keybind is F6, but can be changed in the menu.
    	KeybindHandler handler = new KeybindHandler();
    	handler.setController(fxmlLoader.getController());
        GlobalScreen.addNativeKeyListener(handler);
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }
    
    private static FXMLLoader fxmlLoader;
    private static Parent loadFXML(String fxml) throws IOException {
        fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}