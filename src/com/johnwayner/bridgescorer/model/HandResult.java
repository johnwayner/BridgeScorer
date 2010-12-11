package com.johnwayner.bridgescorer.model;

import org.simpleframework.xml.Element;

import com.johnwayner.bridgescorer.IMPTables;
import com.johnwayner.bridgescorer.IMPTables.VULNERABILITY;

public class HandResult {
	public enum PLAYER {
		NORTH("N"),
		EAST("E"),
		SOUTH("S"),
		WEST("W");
		
		private final String simpleName;
		
		PLAYER(String simpleName)
		{
			this.simpleName = simpleName;
		}
		
		public String getSimpleName()
		{
			return simpleName;
		}
		
		public PLAYER getNextDealer()
		{
			switch(this)
			{
			case NORTH: return EAST;
			case EAST: return SOUTH;
			case SOUTH: return WEST;
			default: return NORTH;
			}
		}
		
		public boolean isSamePartnership(PLAYER otherPlayer) {
			switch(otherPlayer) {
			case NORTH:
			case SOUTH:
				return (this == NORTH) || (this == SOUTH);
			default:
				return (this == EAST) || (this == WEST);			
			}
		}
	}
	
	public enum SUIT {
		CLUBS("\u2663", 20),
		DIAMONDS("\u2666", 20),
		HEARTS("\u2665", 30),
		SPADES("\u2660", 30),
		NOTRUMP("NT", 40, 30);
		
		private final String simpleName;
		private final int firstTrickValue;
		private final int subsequentTrickValue;
		
		SUIT(String simpleName, int trickValue, int subsequentTrickValue)
		{
			this.simpleName = simpleName;
			this.firstTrickValue = trickValue;
			this.subsequentTrickValue = subsequentTrickValue;
		}
		
		SUIT(String simpleName, int trickValue)
		{
			this(simpleName, trickValue, trickValue);
		}
		
		public String getSimpleName()
		{
			return simpleName;
		}
		
		public int getTricksValue(int numTricks)
		{
			if(numTricks < 0) {
				return 0;
			}
			
			return firstTrickValue + 
				(subsequentTrickValue * (numTricks - 1));
		}
	}
	
	@Element
	public SUIT suit;
	@Element
	public PLAYER player;
	@Element
	public int level;
	@Element
	public int multiplier = 1;
	@Element
	public VULNERABILITY vulnerability;
	@Element
	public int hcp; //playing partnership's high card points (A=4,K=3,Q=2,J=1)
	@Element
	public int tricksMade;
	@Element
	public int handNumber;
	
	
	
	public SUIT getSuit() {
		return suit;
	}

	public void setSuit(SUIT suit) {
		this.suit = suit;
	}

	public PLAYER getPlayer() {
		return player;
	}

	public void setPlayer(PLAYER player) {
		this.player = player;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(int multiplier) {
		this.multiplier = multiplier;
	}

	public VULNERABILITY getVulnerability() {
		return vulnerability;
	}

	public void setVulnerability(VULNERABILITY vulnerability) {
		this.vulnerability = vulnerability;
	}

	public int getHcp() {
		return hcp;
	}

	public void setHcp(int hcp) {
		this.hcp = hcp;
	}

	public int getTricksMade() {
		return tricksMade;
	}

	public void setTricksMade(int tricksMade) {
		this.tricksMade = tricksMade;
	}

	public int getHandNumber() {
		return handNumber;
	}

	public void setHandNumber(int handNumber) {
		this.handNumber = handNumber;
	}

	public HandResult(PLAYER player, SUIT suit, int level, int multiplier, VULNERABILITY vulnerability, int hcp, int tricksMade, int handNumber)
	{
		this.suit = suit;
		this.player = player;
		this.level = level;
		this.multiplier = multiplier;
		this.vulnerability = vulnerability;
		this.hcp = hcp;
		this.tricksMade = tricksMade;
		this.handNumber = handNumber;
	}
	
	
	
	public HandResult() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String toString()
	{
		int overUnders = (tricksMade-(6+level));
		int score = getScore();
		int targetScore = IMPTables.getTargetScore(hcp, vulnerability);
		
		return
		  handNumber + ". " +
		  player.getSimpleName() + " " +
		  level + suit.getSimpleName() + 
		  (multiplier==2?"X ":(multiplier==4?"XX":"  ")) +
		  (overUnders>=0?"+":"") + overUnders + " " +
		  hcp + " " +
		  (vulnerability==VULNERABILITY.VULNERABLE?" V":"NV") + " " +
		  score + " (" + targetScore + ")  " +
		  IMPTables.getIMPScore(score, targetScore);
	}
	
	/**
	 * Returns the contract bridge score for the hand.
	 * @return
	 */
	public int getScore()
	{
		int tricksOver6 = tricksMade - 6;
		int score = 0;
		
		if(tricksOver6 >= level) {
			//bid was made
			int bidScore = suit.getTricksValue(level) * multiplier;
			
			if(1==multiplier) {
				//no double
				score += suit.getTricksValue(tricksOver6);
			} else {
				//double or redouble
				score += bidScore;
				score += (vulnerability==VULNERABILITY.VULNERABLE?100:50) * 
					(tricksOver6 - level) *
					multiplier;  //X or XX over trick points.
			}
			
			if(bidScore < 100) score += 50; //partscore bonus
			if(bidScore >= 100) score += (vulnerability==VULNERABILITY.VULNERABLE?500:300); //game bonus
			if(6==level) score += (vulnerability==VULNERABILITY.VULNERABLE?750:500); //small slam
			if(7==level) score += (vulnerability==VULNERABILITY.VULNERABLE?1500:1000); //grand slam			
			if(multiplier>1) score += (25 * multiplier); //double insult bonus: 50 for X, 100 for XX.
		} else {
			//bid was not made
			int tricksUnder = (level + 6) - tricksMade;
			if(multiplier>1) {
				if(vulnerability==VULNERABILITY.VULNERABLE)
				{
					score -= 100 * multiplier; //first under
					score -= 150 * multiplier * (tricksUnder - 1); //the rest
				} else {
					score -= 50 * multiplier; //first under
					if(tricksUnder>1) {
						score -= 100 * multiplier; //second
					}
					if(tricksUnder>2) {
						score -= 100 * multiplier; //third
					}
					if(tricksUnder>3) {
						score -= 150 * multiplier * (tricksUnder - 3); //the rest
					}
				}
			} else {
				score -= 50 * tricksUnder * multiplier;
			}
		}
		
		return score;
	}
	
	public int getIMPScore()
	{
		return IMPTables.getIMPScore(getScore(), IMPTables.getTargetScore(hcp, vulnerability));
	}
}
