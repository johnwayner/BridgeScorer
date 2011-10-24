package com.johnwayner.bridgescorer;

import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.johnwayner.bridgescorer.IMPTables.VULNERABILITY;
import com.johnwayner.bridgescorer.model.Game;
import com.johnwayner.bridgescorer.model.HandResult;
import com.johnwayner.bridgescorer.model.HandResult.PLAYER;
import com.johnwayner.bridgescorer.model.HandResult.SUIT;
import com.johnwayner.bridgescorer.utils.EditNumberText;
import com.johnwayner.bridgescorer.utils.GameManager;
import com.johnwayner.bridgescorer.utils.ToggleButtonGroup;

public class GameScreen extends Activity {

	public static final String SHOW_GAME_ACTIVITY_NAME = "com.johnwayner.bridgescorer.GameScreen.SHOW_GAME";

	public static final int EDIT_RESULT_DIALOG = 1;

	static final HandResult[] TEST_HANDS = new HandResult[] {
		new HandResult(PLAYER.NORTH, SUIT.HEARTS, 2, 1, VULNERABILITY.VULNERABLE, 23, 8, 1, false),
		new HandResult(PLAYER.NORTH, SUIT.HEARTS, 2, 2, VULNERABILITY.NOT_VULNERABLE, 23, 8, 1, false),
		new HandResult(PLAYER.NORTH, SUIT.NOTRUMP, 3, 1, VULNERABILITY.VULNERABLE, 23, 11, 1, false),
		new HandResult(PLAYER.NORTH, SUIT.DIAMONDS, 1, 2, VULNERABILITY.NOT_VULNERABLE, 23, 8, 1, false),
		new HandResult(PLAYER.NORTH, SUIT.SPADES, 5, 4, VULNERABILITY.VULNERABLE, 23, 12, 1, false),
		new HandResult(PLAYER.NORTH, SUIT.NOTRUMP, 6, 1, VULNERABILITY.NOT_VULNERABLE, 23, 13, 1, false),
		new HandResult(PLAYER.NORTH, SUIT.DIAMONDS, 4, 1, VULNERABILITY.NOT_VULNERABLE, 23, 7, 1, false),
		new HandResult(PLAYER.NORTH, SUIT.DIAMONDS, 4, 2, VULNERABILITY.NOT_VULNERABLE, 23, 7, 1, false),
		new HandResult(PLAYER.NORTH, SUIT.DIAMONDS, 4, 2, VULNERABILITY.VULNERABLE, 23, 7, 1, false),

	};

	//ToggleGroups
	private ToggleButtonGroup<PLAYER> contractPlayer;
	private ToggleButtonGroup<SUIT> contractSuit;
	private ToggleButtonGroup<Integer> contractLevel;
	private ToggleButtonGroup<Integer> contractMultiplier;
	private ToggleButtonGroup<Integer> tricksMadeQuick;
	private ToggleButton noBidToggle;
	private EditNumberText hcp;
	private EditNumberText tricksTaken;

	private Game currentGame = new Game();

	private HandResult resultToEdit = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //if we are resuming an existing game...load it
        if(null != getIntent().getData()) {
        	try {
				currentGame = GameManager.loadGame(getIntent().getData());
			} catch (Exception e) {
				Log.e("BridgeScorer", "GameScreen: Unable to load existing game: " + getIntent().getData().toString(), e);
				Toast.makeText(this, "Unable to load game!", Toast.LENGTH_LONG).show();
				finish();
			}
        }

        final Window thisWindow = this.getWindow();
        final View contractView = this.findViewById(R.id.ContractLayout);
        final View pointsView = this.findViewById(R.id.PointsLayout);
        final View resultView = this.findViewById(R.id.ResultLayout);

        OnClickListener showPointsViewListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(noBidToggle.isChecked()) {
					if(contractPlayer.hasNoValue() ||
							 !contractSuit.hasNoValue() ||
							 !contractLevel.hasNoValue() ||
							 contractMultiplier.getSelectedValue() > 1) {
						Toast.makeText(getApplicationContext(), "A no bid must have a player selected and nothing else.", Toast.LENGTH_LONG).show();
						return;
					}
				} else {
					//bail if contract not set
					if(contractPlayer.hasNoValue() ||
					   contractSuit.hasNoValue() ||
					   contractLevel.hasNoValue() ||
					   contractMultiplier.hasNoValue()) {
						Toast.makeText(GameScreen.this, "Contract not entirely selected.", Toast.LENGTH_LONG).show();
						return;
					}
				}
				pointsView.setVisibility(View.VISIBLE);
				contractView.setVisibility(View.GONE);
				resultView.setVisibility(View.GONE);
				thisWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				pointsView.requestFocus();
			}
		};

		contractView.findViewById(R.id.ContractOKButton).setOnClickListener(showPointsViewListener);
		resultView.findViewById(R.id.ResultBackButton).setOnClickListener(showPointsViewListener);

        pointsView.findViewById(R.id.BackButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(pointsView.findViewById(R.id.PointsOKButton).getWindowToken(), 0);
				contractView.setVisibility(View.VISIBLE);
				pointsView.setVisibility(View.GONE);
				resultView.setVisibility(View.GONE);
			}
		});

        pointsView.findViewById(R.id.PointsOKButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					hcp.getValue(true);
				}
				catch(NumberFormatException nfe) {
					return;
				}

				if(!noBidToggle.isChecked()) {
					int multiplier = contractMultiplier.getSelectedValue();

					((TextView)resultView.findViewById(R.id.ContractPlayerLabel)).setText(
							contractPlayer.getSelectedValue().getSimpleName());
					((TextView)resultView.findViewById(R.id.ContractLevelLabel)).setText(
							contractLevel.getSelectedValue().toString());
					TextView suitLabel = (TextView)resultView.findViewById(R.id.ContractSuitLabel);
					suitLabel.setText(contractSuit.getSelectedValue().getSimpleName());
					switch(contractSuit.getSelectedValue()) {
					case HEARTS:
					case DIAMONDS:
						suitLabel.setTextColor(0xFFFF0000);
						break;
					case CLUBS:
					case SPADES:
						suitLabel.setTextColor(0xFF000000);
						break;
					case NOTRUMP:
						suitLabel.setTextColor(0xFF008800);
						break;
					}

					((TextView)resultView.findViewById(R.id.ContractMultiplierLabel)).setText(
							(multiplier==2?"X ":(multiplier==4?"XX":"")));

					((TextView)resultView.findViewById(R.id.ContractVulnLabel)).setText(
							(currentGame.getVulnerability(contractPlayer.getSelectedValue())
								== VULNERABILITY.VULNERABLE)?
										"Vulnerable":
										"Not Vulnerable");
				}

				contractView.setVisibility(View.GONE);
				pointsView.setVisibility(View.GONE);
				resultView.setVisibility(View.VISIBLE);

				if(noBidToggle.isChecked()) {
					//just skip the result page
					//by clicking ok.
					((Button)resultView.findViewById(R.id.ResultOKButton)).performClick();
				}
			}
		});

        resultView.findViewById(R.id.ResultOKButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if(null == resultToEdit) {
					HandResult result = new HandResult(
							contractPlayer.getSelectedValue(),
							noBidToggle.isChecked()?SUIT.CLUBS:contractSuit.getSelectedValue(),
							noBidToggle.isChecked()?0:contractLevel.getSelectedValue(),
							contractMultiplier.getSelectedValue(),
							currentGame.getVulnerability(contractPlayer.getSelectedValue()),
							hcp.getValue(false),
							noBidToggle.isChecked()?0:getTricksMade(),
							currentGame.getHandNumber(),
							noBidToggle.isChecked());

					currentGame.addHandResult(result);

					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(pointsView.findViewById(R.id.PointsOKButton).getWindowToken(), 0);

				} else {
					//This was an edit.
					resultToEdit.player = contractPlayer.getSelectedValue();
					resultToEdit.suit = noBidToggle.isChecked()?SUIT.CLUBS:contractSuit.getSelectedValue();
					resultToEdit.level = noBidToggle.isChecked()?0:contractLevel.getSelectedValue();
					resultToEdit.multiplier = contractMultiplier.getSelectedValue();
					resultToEdit.vulnerability = Game.getVulnerability(resultToEdit.handNumber, resultToEdit.player);
					resultToEdit.hcp = hcp.getValue(false);
					resultToEdit.tricksMade = noBidToggle.isChecked()?0:getTricksMade();
					resultToEdit.setUnbid(noBidToggle.isChecked());

					GameScreen.this.findViewById(R.id.EditCancelButton).setVisibility(View.GONE);
					resultToEdit = null;
				}

				updateHistoryList();
				initializeUIElements();
				try {
					GameManager.saveGame(GameScreen.this, currentGame);
				} catch (Exception e) {
					Toast.makeText(GameScreen.this, e.toString(), Toast.LENGTH_LONG).show();
				}
			}
		});

        this.findViewById(R.id.EditCancelButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Cancel the current edit
				resultToEdit = null;
				initializeUIElements();
				v.setVisibility(View.GONE);
			}
		});

        ((ListView)this.findViewById(R.id.HistoryListView)).setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				if(contractView.getVisibility() == View.VISIBLE) {
					editResult(position);
				} else {
					Toast.makeText(GameScreen.this,
							"Can't edit history while creating new contract (hit back).",
							Toast.LENGTH_SHORT).show();
				}
			}
		});


        registerForContextMenu(this.findViewById(R.id.HistoryListView));


        setupToggleGroups();
        hcp = new EditNumberText((EditText)this.findViewById(R.id.PartnershipPointsEditText), "Points", 0, 40);
        tricksTaken = new EditNumberText((EditText)this.findViewById(R.id.TricksTakenEditText), "Tricks taken", 0, 13);

        updateHistoryList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = new MenuInflater(this);
    	inflater.inflate(R.menu.game_screen_options, menu);
    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.GameScreenMenuItem_RemoveLastHand:
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage("Are you sure you want to remove the last hand?")
    		       .setCancelable(false)
    		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    		           @Override
					public void onClick(DialogInterface dialog, int id) {
    		                currentGame.removeLastResult();
    		                initializeUIElements();
    		           }
    		       })
    		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
    		           @Override
					public void onClick(DialogInterface dialog, int id) {
    		                dialog.cancel();
    		           }
    		       });
    		builder.create().show();
    		return true;

    	case R.id.GameScreenMenuItem_NewGame:
    		currentGame = new Game();
    		initializeUIElements();
    		Toast.makeText(GameScreen.this, "Previous game saved.  New game loaded.", Toast.LENGTH_LONG).show();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      if(v.getId() == R.id.HistoryListView) {
    	  MenuInflater inflater = getMenuInflater();
    	  inflater.inflate(R.menu.result_context, menu);
      }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	switch(item.getItemId()) {
    	case R.id.ResultMenuItem_Edit:
    		editResult(info.position);
    		return true;
    	default:
    		return super.onContextItemSelected(item);
    	}
    }

    private void editResult(int position) {
    	resultToEdit = (HandResult)
		((ListView)this.findViewById(R.id.HistoryListView)).getItemAtPosition(position);
    	this.findViewById(R.id.EditCancelButton).setVisibility(View.VISIBLE);
    	loadResultToEdit(resultToEdit);
    }

    @Override
    protected void onResume() {
    	super.onRestart();
    	initializeUIElements();
    }

    private void initializeUIElements() {
		//Update score board
		((TextView)GameScreen.this.findViewById(R.id.NSScore)).setText(Integer.toString(currentGame.getNSScore()));
		((TextView)GameScreen.this.findViewById(R.id.EWScore)).setText(Integer.toString(currentGame.getEWScore()));

		//Clear all toggles and inputs.
		contractPlayer.reset();
		contractSuit.reset();
		contractLevel.reset();
		contractMultiplier.reset();
		hcp.reset();
		tricksMadeQuick.reset();
		tricksTaken.reset();
		noBidToggle.setChecked(false);

		((TextView)GameScreen.this.findViewById(
				R.id.HandCountLabel)).setText(
						"("+Integer.toString(currentGame.getHandNumber())+")");

        final View contractView = this.findViewById(R.id.ContractLayout);
        final View pointsView = this.findViewById(R.id.PointsLayout);
        final View resultView = this.findViewById(R.id.ResultLayout);

		contractView.setVisibility(View.VISIBLE);
		pointsView.setVisibility(View.GONE);
		resultView.setVisibility(View.GONE);

		updateDealerIndicator();
		updateHistoryList();
    }

    private void updateDealerIndicator()
    {
    	TextView n = (TextView)this.findViewById(R.id.North);
    	TextView e = (TextView)this.findViewById(R.id.East);
    	TextView s = (TextView)this.findViewById(R.id.South);
    	TextView w = (TextView)this.findViewById(R.id.West);

    	n.setBackgroundResource(android.R.color.white);
    	e.setBackgroundResource(android.R.color.white);
    	w.setBackgroundResource(android.R.color.white);
    	s.setBackgroundResource(android.R.color.white);

    	switch(currentGame.getCurrentDealer())
    	{
    	case NORTH: n.setBackgroundColor(0xFFFF0000); break;
    	case EAST: e.setBackgroundColor(0xFFFF0000); break;
    	case SOUTH: s.setBackgroundColor(0xFFFF0000); break;
    	case WEST: w.setBackgroundColor(0xFFFF0000); break;
    	}
    }

    private void loadResultToEdit(HandResult result) {
    	contractPlayer.setSelectedValue(result.player);
    	contractSuit.setSelectedValue(result.suit);
    	contractLevel.setSelectedValue(result.level);
    	contractMultiplier.setSelectedValue(result.multiplier);
    	hcp.setValue(result.hcp);

    	int trickDiff = result.tricksMade - (result.level+6);
    	if(Math.abs(trickDiff) <= 2) {
    		tricksMadeQuick.setSelectedValue(trickDiff);
    		tricksTaken.reset();
    	} else {
    		tricksMadeQuick.reset();
    		tricksTaken.setValue(result.tricksMade);
    	}

    	final View contractView = this.findViewById(R.id.ContractLayout);
        final View pointsView = this.findViewById(R.id.PointsLayout);
        final View resultView = this.findViewById(R.id.ResultLayout);

		contractView.setVisibility(View.VISIBLE);
		pointsView.setVisibility(View.GONE);
		resultView.setVisibility(View.GONE);
    }

    private void updateHistoryList()
    {
        ListView handListView = (ListView)this.findViewById(R.id.HistoryListView);
        Collections.sort(currentGame.getHandResults());
        handListView.setAdapter(new ArrayAdapter<HandResult>(this, R.layout.result_list_item, currentGame.getHandResults()));
    }


    private void setupToggleGroups()
    {
    	//Contract player
    	this.contractPlayer = new ToggleButtonGroup<PLAYER>(
    			new ToggleButton[] {
    					(ToggleButton)this.findViewById(R.id.NorthToggle),
    			    	(ToggleButton)this.findViewById(R.id.EastToggle),
    			    	(ToggleButton)this.findViewById(R.id.SouthToggle),
    			    	(ToggleButton)this.findViewById(R.id.WestToggle),
    			},
    			new PLAYER[] {
    					PLAYER.NORTH,
    					PLAYER.EAST,
    					PLAYER.SOUTH,
    					PLAYER.WEST,
    			});

    	//Contract suit
    	this.contractSuit = new ToggleButtonGroup<SUIT>(
    			new ToggleButton[] {
    					(ToggleButton)this.findViewById(R.id.ClubsToggle),
    			    	(ToggleButton)this.findViewById(R.id.DiamondsToggle),
    			    	(ToggleButton)this.findViewById(R.id.HeartsToggle),
    			    	(ToggleButton)this.findViewById(R.id.SpadesToggle),
    			    	(ToggleButton)this.findViewById(R.id.NoTrumpToggle),
    			},
    			new SUIT[] {
    					SUIT.CLUBS,
    					SUIT.DIAMONDS,
    					SUIT.HEARTS,
    					SUIT.SPADES,
    					SUIT.NOTRUMP,
    			});

    	//Contract level
    	this.contractLevel = new ToggleButtonGroup<Integer>(
    			new ToggleButton[] {
    					(ToggleButton)this.findViewById(R.id.Level1Toggle),
    			    	(ToggleButton)this.findViewById(R.id.Level2Toggle),
    			    	(ToggleButton)this.findViewById(R.id.Level3Toggle),
    			    	(ToggleButton)this.findViewById(R.id.Level4Toggle),
    			    	(ToggleButton)this.findViewById(R.id.Level5Toggle),
    			    	(ToggleButton)this.findViewById(R.id.Level6Toggle),
    			    	(ToggleButton)this.findViewById(R.id.Level7Toggle),
    			},
    			new Integer[] {
    					1,2,3,4,5,6,7
    			});

    	//Contract multiplier (double/redouble)
    	this.contractMultiplier = new ToggleButtonGroup<Integer>(
    			new ToggleButton[] {
    					(ToggleButton)this.findViewById(R.id.DoubleToggle),
    			    	(ToggleButton)this.findViewById(R.id.RedoubleToggle),
    			},
    			new Integer[] {
    					2,4
    			},
    			1); //default value

    	this.tricksMadeQuick = new ToggleButtonGroup<Integer>(
    			new ToggleButton[] {
    					(ToggleButton)this.findViewById(R.id.Minus2Toggle),
    					(ToggleButton)this.findViewById(R.id.Minus1Toggle),
    					(ToggleButton)this.findViewById(R.id.Plus0Toggle),
    					(ToggleButton)this.findViewById(R.id.Plus1Toggle),
    					(ToggleButton)this.findViewById(R.id.Plus2Toggle),
    			},
    			new Integer[] {
    					-2,-1,0,1,2
    			},
    			0); //default value

    	this.noBidToggle = (ToggleButton) this.findViewById(R.id.NoBidToggle);
    }

    private int getTricksMade()
    {
    	try {
    		return tricksTaken.getValue(false);
    	}
    	catch (NumberFormatException e) {
			//look at toggle group
    		return (6 + contractLevel.getSelectedValue()) + this.tricksMadeQuick.getSelectedValue();
		}
    }
}