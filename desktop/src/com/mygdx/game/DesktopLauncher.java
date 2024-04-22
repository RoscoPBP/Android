package com.mygdx.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.czyzby.websocket.CommonWebSockets;
import com.mygdx.game.MyGdxGame;

public class DesktopLauncher {
	public static void main(String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		CommonWebSockets.initiate();
		config.setForegroundFPS(60);
		config.setWindowedMode(AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
		config.setTitle(AppConfig.TITLE);
		new Lwjgl3Application(new MyGdxGame(), config);
	}
}
