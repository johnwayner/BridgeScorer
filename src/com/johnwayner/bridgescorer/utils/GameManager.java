package com.johnwayner.bridgescorer.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.johnwayner.bridgescorer.model.Game;

public class GameManager {
	public static final SimpleDateFormat fileNameDateFormat =
		new SimpleDateFormat("'game_'yyyy_MM_dd_HH_mm_ss'.xml'");
	
	public static void saveGame(Context context, Game game) throws Exception {
		Serializer serializer = new Persister();
		serializer.write(game, new File(getFullFilename(context, game.getStartDate())));
	}
	
	public static Game loadGame(Uri uri) throws Exception {
		return loadGame(uri.getPath());
	}
	
	private static Game loadGame(String fileName) throws Exception {
		Serializer serializer = new Persister();
		File source = new File(fileName);

		return serializer.read(Game.class, source);
	}
		
	public static String loadGameXML(Context context, Date date) {
		File gameFile = new File(getFullFilename(context, date));		
		try {
			FileReader reader = new FileReader(gameFile);
			return IOUtils.toString(reader);
		} catch (IOException e) {
			Log.w("BridgeScorer", "GameManager.loadGameXML: unable to load xml data", e);
			Toast.makeText(context, "Unable to load file.  It may be corrupted.", Toast.LENGTH_LONG);
		}
		
		return null;
	}
	
	public static void deleteGame(Context context, Date date) {
		File gameFile = new File(getFullFilename(context, date));
		String msg = "Could not delete!";
		
		if(gameFile.delete()) {
			msg = "Deleted.";
		}
		
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	
	private static Game loadGame(Date date) throws Exception {
		return loadGame(getSimpleFileName(date));
	}
	
	private static String getFullFilename(Context context, Date date) {
		return context.getFilesDir() + "/" +
			getSimpleFileName(date);
	}
	
	private static String getSimpleFileName(Date date) {
		return fileNameDateFormat.format(date);
	}
	
	public static Uri getUri(Context context, Date date) {
		return Uri.fromFile(new File(getFullFilename(context, date)));
	}
	
	public static List<Date> getGameDates(Context context) {
		List<Date> gameList = new ArrayList<Date>();
		File dir = context.getFilesDir();
		for(String fileName : dir.list(new FilenameFilter() {			
									@Override
									public boolean accept(File dir, String filename) {
										return filename.startsWith("game_");
									}})) {
			try {
				gameList.add(fileNameDateFormat.parse(fileName));
			} catch (ParseException e) {
				Log.w("BridgeScorer", "Unable to parse game file name: " + fileName, e);
			}
		}
		
		return gameList;
	}
}
