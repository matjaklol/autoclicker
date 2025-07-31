package org.openjfx.customfx;

import java.io.IOException;

import com.github.kwhat.jnativehook.GlobalScreen;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

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
	//Generic scene
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
    	try {
    		Image icon = new Image(this.getClass().getResourceAsStream("/org/openjfx/customfx/images/mouseIcon.png"));
    		stage.getIcons().add(icon);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	
        scene = new Scene(loadFXML("testing"), 600, 400);
        stage.setResizable(false);
        
        stage.setTitle("matClicker v1.1.2");
        
        stage.setScene(scene);
        
        
        //On exit of the window, unregister the autoclicker/keybind listener.
        stage.setOnCloseRequest(_ -> {
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Platform.exit(); // Ensure JavaFX exits
            System.exit(0);  // Optional hard kill
        });
        
        //Go ahead and display it.
        stage.show();
        
        //Register the keybind listener.
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
    
    /**
     * Loads a given FXML file for a Stage.
     * @param fxml the file to load (excluding the .fxml format).
     * @return a {@linkplain Parent} object
     * @throws IOException
     */
    private static Parent loadFXML(String fxml) throws IOException {
        fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    /**
     * App standard starting point. Could be used to init the entire program but we're calling that from Main instead.
     * @param args
     */
    public static void main(String[] args) {
        launch();
    }

}