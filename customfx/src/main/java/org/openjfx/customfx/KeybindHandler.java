package org.openjfx.customfx;

import java.awt.Robot;
import java.awt.event.InputEvent;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;


public class KeybindHandler implements NativeKeyListener {
	public static boolean running = false;
	
	private TestController controller;
	private long delay = 1L;
	private int buttonType = InputEvent.BUTTON1_DOWN_MASK;
	private boolean doubleClick = false;
	private boolean addRandomOffset = false;
	private int randomOffsetValue = 40;
	private int repeatCount = -1;
	
	public void setController(TestController controller) {
		this.controller = controller;
		controller.setKeybindHandler(this);
		// Disable noisy logging
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);
        
        
	}
	
	
	@Override
    public void nativeKeyPressed(NativeKeyEvent e) {
		//Check if the hotkey setup is enabled:
		if(controller.hotkeyRunning) {
			
			Platform.runLater(() -> {
				controller.setAutoClickerKeybind(e.getKeyCode());
				controller.disableHotkeyRunning();
			});
			return;
		}
		
		
        
		if(e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
			running = false;
			System.out.println("Autoclicker stopped. Emergency key pressed.");
			
			stopAutoClicker();
			return;
		}
		
        if (e.getKeyCode() == controller.getAutoClickerKeybind()) {
            running = !running;
            System.out.println("Autoclicker " + (running ? "started" : "stopped"));

            if (running) {
                startAutoClicker();
            } else {
                stopAutoClicker();
            }
        }
    }
	
	private Thread clickThread;
	public void startAutoClicker() {
		Platform.runLater(() -> {
			controller.remoteStartAutoclicker();
		});
		
		if(clickThread != null) {
			running = true;
			return;
		}
		
		running = true;
		delay = controller.calculateTotalDelay();
		addRandomOffset = controller.addingRandomOffset();
		randomOffsetValue = controller.getRandomOffsetAmount();
		doubleClick = controller.getClickType(null);
		buttonType = controller.getMouseType(null);
		repeatCount = controller.getRepeatCount();
		
		
		clickThread = new Thread(() -> {
		    try {
		        Robot robot = new Robot();
		        double offset = 0;
		        int repeatCounter = 0;
		        while (running && repeatCounter != repeatCount) {
		        	repeatCounter++;
		        	
		        	if(addRandomOffset) {
		        		offset = randomOffsetValue * Math.random();
		        	}
		        	
		        	if(doubleClick) {
		        		robot.mousePress(buttonType);
			            robot.mouseRelease(buttonType);
			            robot.delay(1);
		        	}
		            robot.mousePress(buttonType);
		            robot.mouseRelease(buttonType);
		            
		            
		            try {
		            	Thread.sleep(delay + (int) Math.floor(offset)); // xms delay between clicks.
		            } catch (InterruptedException e) {
		                System.out.println("Thread interrupted during sleep.");
		                break; // Exit the loop immediately
		            }
		            
		            
		            
		        }
		        
		        System.out.println("Exited safely.");
		        running = false;
		        stopAutoClicker();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		});
		clickThread.setDaemon(true); // Optional: closes with app
		clickThread.start();
		
		
		

	}
	
	public void stopAutoClicker() {
		//Visually re-enable the GUI buttons for the autoclicker.
		Platform.runLater(() -> {
			controller.remoteStopAutoclicker();
		});
		
		
		if(clickThread != null && clickThread.isAlive() && !clickThread.isInterrupted() && running) {
			
			clickThread.interrupt();
			
			try {
				clickThread.join();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		clickThread = null;
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}

}
