package com.johnwayner.bridgescorer;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.johnwayner.bridgescorer.utils.GameManager;

public class GameChooser extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerForContextMenu(getListView());
	}

	@Override
	protected void onResume() {
		super.onResume();

		List<Date> games = GameManager.getGameDates(this);

		if(games.size() == 0) {
			startActivity(new Intent(getApplicationContext(), GameScreen.class));
		} else {
			Collections.sort(games, Collections.reverseOrder());

			setListAdapter(new ArrayAdapter<Date>(this,
					android.R.layout.simple_list_item_1, games));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chooser_options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.NewGameMenuItem:
			Intent intent = new Intent();
			intent.setClass(this, GameScreen.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		playGame(position);
	}

	private Date getDateAtPosition(int position) {
		return (Date) getListView().getItemAtPosition(position);
	}

	private void playGame(int position) {
		Intent playIntent = new Intent(GameScreen.SHOW_GAME_ACTIVITY_NAME);
		playIntent.setData(GameManager
				.getUri(this, getDateAtPosition(position)));
		this.startActivity(playIntent);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.chooser_ctx, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

	  final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	  switch (item.getItemId()) {
	  case R.id.PlayMenuItem:
		  playGame(info.position);
	    return true;
	  case R.id.DeleteMenuItem:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure you want to delete this file?")
			       .setCancelable(false)
			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			           @Override
					public void onClick(DialogInterface dialog, int id) {
			                GameManager.deleteGame(GameChooser.this, getDateAtPosition(info.position));
			                GameChooser.this.onResume();
			           }
			       })
			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
			           @Override
					public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
			return true;
	  case R.id.EmailMenuItem:
		  String xml = GameManager.loadGameXML(this, getDateAtPosition(info.position));
		  if(null == xml) {
			  Toast.makeText(this, "Unable to load file data.", Toast.LENGTH_LONG).show();
			  return true;
		  }
		  Intent emailIntent = new Intent(Intent.ACTION_SEND);
		  emailIntent.setType("text/xml");
		  emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bridge Game Record");
		  emailIntent.putExtra(Intent.EXTRA_TEXT, xml);
		  startActivity(Intent.createChooser(emailIntent, "Email:"));
		  return true;
	  default:
	    return super.onContextItemSelected(item);
	  }
	}
}
