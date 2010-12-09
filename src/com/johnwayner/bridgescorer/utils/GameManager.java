package com.johnwayner.bridgescorer.utils;

import java.io.File;
import java.text.SimpleDateFormat;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;

import com.johnwayner.bridgescorer.model.Game;

public class GameManager {
	public static final SimpleDateFormat fileNameDateFormat =
		new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
	
	public static void saveGame(Context context, Game game) throws Exception {
		Serializer serializer = new Persister();
		serializer.write(game, new File(getFilename(context, game)));
	}
	
	public static Game loadGame(String fileName) throws Exception {
		Serializer serializer = new Persister();
		File source = new File(fileName);

		return serializer.read(Game.class, source);
	}
	
	public static String getFilename(Context context, Game game) {
		return context.getFilesDir() + "/game_" + 
			fileNameDateFormat.format(game.getStartDate())  + ".xml";
	}
}
