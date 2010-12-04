package com.johnwayner.bridgescorer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.johnwayner.bridgescorer.IMPTables.VULNERABILITY;
import com.johnwayner.bridgescorer.model.HandResult;
import com.johnwayner.bridgescorer.model.HandResult.PLAYER;
import com.johnwayner.bridgescorer.model.HandResult.SUIT;
import com.johnwayner.bridgescorer.utils.EditNumberText;
import com.johnwayner.bridgescorer.utils.ToggleButtonGroup;

public class GameScreen extends Activity {
	
	static final HandResult[] TEST_HANDS = new HandResult[] {
		new HandResult(PLAYER.NORTH, SUIT.HEARTS, 2, 1, VULNERABILITY.VULNERABLE, 23, 8, 1),
		new HandResult(PLAYER.NORTH, SUIT.HEARTS, 2, 2, VULNERABILITY.NOT_VULNERABLE, 23, 8, 1),
		new HandResult(PLAYER.NORTH, SUIT.NOTRUMP, 3, 1, VULNERABILITY.VULNERABLE, 23, 11, 1),
		new HandResult(PLAYER.NORTH, SUIT.DIAMONDS, 1, 2, VULNERABILITY.NOT_VULNERABLE, 23, 8, 1),
		new HandResult(PLAYER.NORTH, SUIT.SPADES, 5, 4, VULNERABILITY.VULNERABLE, 23, 12, 1),
		new HandResult(PLAYER.NORTH, SUIT.NOTRUMP, 6, 1, VULNERABILITY.NOT_VULNERABLE, 23, 13, 1),
		new HandResult(PLAYER.NORTH, SUIT.DIAMONDS, 4, 1, VULNERABILITY.NOT_VULNERABLE, 23, 7, 1),
		new HandResult(PLAYER.NORTH, SUIT.DIAMONDS, 4, 2, VULNERABILITY.NOT_VULNERABLE, 23, 7, 1),
		new HandResult(PLAYER.NORTH, SUIT.DIAMONDS, 4, 2, VULNERABILITY.VULNERABLE, 23, 7, 1),
		
	};
	
	private static List<HandResult> history = new ArrayList<HandResult>();
	
	//ToggleGroups
	private ToggleButtonGroup<PLAYER> contractPlayer;
	private ToggleButtonGroup<SUIT> contractSuit;
	private ToggleButtonGroup<Integer> contractLevel;
	private ToggleButtonGroup<Integer> contractMultiplier;
	private ToggleButtonGroup<Integer> tricksMadeQuick;
	private EditNumberText hcp;
	private EditNumberText tricksTaken;
	
	//Game level values
	private int NSScore = 0;
	private int EWScore = 0;
	private PLAYER currentDealer = PLAYER.NORTH;
	private int handNumber = 1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final Window thisWindow = this.getWindow();        
        final View contractView = this.findViewById(R.id.ContractLayout);
        final View pointsView = this.findViewById(R.id.PointsLayout);
        final View resultView = this.findViewById(R.id.ResultLayout);
        
        OnClickListener showPointsViewListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//bail if contract not set
				if(contractPlayer.hasNoValue() ||
				   contractSuit.hasNoValue() ||
				   contractLevel.hasNoValue() ||
				   contractMultiplier.hasNoValue()) {
					Toast.makeText(GameScreen.this, "Contract not entirely selected.", Toast.LENGTH_LONG).show();
					return;
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
				
				int multiplier = contractMultiplier.getSelectedValue();
				
				((TextView)resultView.findViewById(R.id.ContractLabel)).setText(
						contractPlayer.getSelectedValue().getSimpleName() + " " +
						contractLevel.getSelectedValue() +
						contractSuit.getSelectedValue().getSimpleName() +
						(multiplier==2?"X ":(multiplier==4?"XX":"")));
						
				
				contractView.setVisibility(View.GONE);
				pointsView.setVisibility(View.GONE);			
				resultView.setVisibility(View.VISIBLE);
			}
		});
        
        resultView.findViewById(R.id.ResultOKButton).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				HandResult result = new HandResult(
						contractPlayer.getSelectedValue(), 
						contractSuit.getSelectedValue(), 
						contractLevel.getSelectedValue(), 
						contractMultiplier.getSelectedValue(), 
						VULNERABILITY.VULNERABLE, 
						hcp.getValue(false), 
						getTricksMade(),
						handNumber);
				updateHistoryList(result);
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(pointsView.findViewById(R.id.PointsOKButton).getWindowToken(), 0);
				
				int impScore = result.getIMPScore();
				switch(result.player) {
					case NORTH:
					case SOUTH:
						if(impScore>0) {
							NSScore += impScore;
						} else {
							EWScore -= impScore;
						}
						
						break;
					case EAST:
					case WEST:
						if(impScore>0) {
							EWScore += impScore;
						} else {
							NSScore -= impScore;
						}
						break;
				}
				
				//Update score board
				((TextView)GameScreen.this.findViewById(R.id.NSScore)).setText(Integer.toString(NSScore));
				((TextView)GameScreen.this.findViewById(R.id.EWScore)).setText(Integer.toString(EWScore));
				
				//Clear all toggles and inputs.
				contractPlayer.reset();
				contractSuit.reset();
				contractLevel.reset();
				contractMultiplier.reset();
				hcp.reset();
				tricksMadeQuick.reset();
				tricksTaken.reset();
				
				advanceDealerAndUpdateIndicator();
				advanceHandCountAndUpdateIndicator();
				
				
				contractView.setVisibility(View.VISIBLE);
				pointsView.setVisibility(View.GONE);			
				resultView.setVisibility(View.GONE);
			}
		});
        
        setupToggleGroups();
        hcp = new EditNumberText((EditText)this.findViewById(R.id.PartnershipPointsEditText), "Points", 0, 40);
        tricksTaken = new EditNumberText((EditText)this.findViewById(R.id.TricksTakenEditText), "Tricks taken", 0, 13);
        
        updateHistoryList();
    }
    
    private void advanceDealerAndUpdateIndicator()
    {
    	TextView n = (TextView)this.findViewById(R.id.North);
    	TextView e = (TextView)this.findViewById(R.id.East);
    	TextView s = (TextView)this.findViewById(R.id.South);
    	TextView w = (TextView)this.findViewById(R.id.West);
    	
    	n.setBackgroundResource(android.R.color.white);
    	e.setBackgroundResource(android.R.color.white);
    	w.setBackgroundResource(android.R.color.white);
    	s.setBackgroundResource(android.R.color.white);
    	
    	currentDealer = currentDealer.getNextDealer();
    	
    	switch(currentDealer)
    	{
    	case NORTH: n.setBackgroundColor(0xFFFF0000); break;
    	case EAST: e.setBackgroundColor(0xFFFF0000); break;
    	case SOUTH: s.setBackgroundColor(0xFFFF0000); break;
    	case WEST: w.setBackgroundColor(0xFFFF0000); break;
    	}
    }
    
    private void advanceHandCountAndUpdateIndicator()
    {
    	handNumber++;
    	((TextView)this.findViewById(R.id.HandCountLabel)).setText("("+Integer.toString(handNumber)+")");
    }
    
    private void updateHistoryList(HandResult newResult)
    {
    	if(null != newResult)
    	{
    		history.add(newResult);
    	}
        ListView handListView = (ListView)this.findViewById(R.id.HistoryListView);
        handListView.setAdapter(new ArrayAdapter<HandResult>(this, R.layout.result_list_item, history));
    }
    
    private void updateHistoryList()
    {
    	updateHistoryList(null);
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