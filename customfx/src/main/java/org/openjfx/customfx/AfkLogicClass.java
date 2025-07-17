package org.openjfx.customfx;


import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

public class AfkLogicClass {
	private Button afkStartButton;
	private boolean canGoAfk = true;
	
	
	//At least one of these need to be enabled in order for the anti-afk to function.
	private CheckBox useKeyboard;
	private CheckBox useMouse;
	
	private boolean keyboardActive;
	private boolean mouseActive;
	
	
	private RadioButton timedAFKKnob;
	private RadioButton infiniteAFKKnob;
	
	private TextField afkTimeTextBox;
	private ChoiceBox<String> timespan;
	
	private boolean timer = false;
	
	
	//Advanced:
	private TextField betweenActionsTextBox;
	private ChoiceBox<String> betweenActionsTimespan;
	
	private TextField beforeStartTextBox;
	private ChoiceBox<String> beforeStartTimespan;
	
	private String[] timeTypes = {"Hours", "Minutes", "Seconds", "Milliseconds"};
	
	public AfkLogicClass(Button afkStartButton, CheckBox useKeyboard, CheckBox useMouse, RadioButton timedAFKKnob, RadioButton infiniteAFKKnob, TextField afkTimeTextBox, ChoiceBox<String> timespan, TextField betweenActionsTextBox, ChoiceBox<String> betweenActionsTimespan, TextField beforeStartTextBox, ChoiceBox<String> beforeStartTimespan) {
		this.afkStartButton = afkStartButton;
		this.useKeyboard = useKeyboard;
		this.useMouse = useMouse;
		this.timedAFKKnob = timedAFKKnob;
		this.infiniteAFKKnob = infiniteAFKKnob;
		this.afkTimeTextBox = afkTimeTextBox;
		this.timespan = timespan;
		this.betweenActionsTextBox = betweenActionsTextBox;
		this.betweenActionsTimespan = betweenActionsTimespan;
		this.beforeStartTextBox = beforeStartTextBox;
		this.beforeStartTimespan = beforeStartTimespan;
		
		
		timespan.getItems().addAll(timeTypes);
		timespan.setValue(timeTypes[1]);
		
		betweenActionsTimespan.getItems().addAll(timeTypes);
		betweenActionsTimespan.setValue(timeTypes[1]);
		
		beforeStartTimespan.getItems().addAll(timeTypes);
		beforeStartTimespan.setValue(timeTypes[2]);
		
		useKeyboard.setOnAction(this::afkTypeLogic);
		useMouse.setOnAction(this::afkTypeLogic);
		
		timedAFKKnob.setOnAction(this::timeLogic);
		infiniteAFKKnob.setOnAction(this::timeLogic);
		
		afkTimeTextBox.setDisable(!timer);
		timespan.setDisable(!timer);
	}
	
	/**
	 * Returns an integer value for a given string.
	 * @param value the string to check for.
	 * @return 0 (if invalid), or positive value.
	 */
	private int getIntegerValue(String value) {
		if (value.matches("[-+]?\\d+")) {
	        return Math.abs(Integer.parseInt(value));
	    }
	    return 0;
	}
	
	
	
	private void afkTypeLogic(ActionEvent event) {
		keyboardActive = useKeyboard.isSelected();
		mouseActive = useMouse.isSelected();
		
		if(!(keyboardActive || mouseActive)) {
			canGoAfk = false;
			System.out.println("AFK IS NO");
			afkStartButton.setDisable(!canGoAfk);
			return;
		}
		
		canGoAfk = true;
		System.out.println("AFK IS YES");
		afkStartButton.setDisable(!canGoAfk);
	}
	
	
	private void timeLogic(ActionEvent event){
		
		timer = timedAFKKnob.isSelected();
		afkTimeTextBox.setDisable(!timer);
		timespan.setDisable(!timer);
		
	}
	
	
	/**
	 * Disables some GUI components during the running of the Anti-AFK system.
	 * 
	 */
	public void startAFK() {
		
	}
	
	/**
	 * Re-enables some GUI components that were disabled from the starting of the Anti-AFK system.
	 */
	public void stopAFK() {
		
	}
	
	
}
