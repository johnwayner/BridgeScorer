package com.johnwayner.bridgescorer.model;

import com.johnwayner.bridgescorer.IMPTables.VULNERABILITY;
import com.johnwayner.bridgescorer.model.HandResult.PLAYER;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Root
public class Game {
	@Element
	private Date startDate = new Date();
	@ElementList
	private List<HandResult> handResults = new ArrayList<HandResult>();

	public Date getStartDate() {
		return startDate;
	}
	public int getNSScore() {
		int score = 0;
		for(HandResult result : handResults) {
			int impScore = result.getIMPScore();
			if(((result.player.isSamePartnership(PLAYER.NORTH)) &&
			   (impScore > 0))
				||
			   ((result.player.isSamePartnership(PLAYER.EAST)) &&
			   (impScore < 0))) {

				score += Math.abs(impScore);
			}
		}
		return score;
	}
	public int getEWScore() {
		int score = 0;
		for(HandResult result : handResults) {
			int impScore = result.getIMPScore();
			if(((result.player.isSamePartnership(PLAYER.EAST)) &&
			   (impScore > 0))
				||
			   ((result.player.isSamePartnership(PLAYER.NORTH)) &&
			   (impScore < 0))) {

				score += Math.abs(impScore);
			}
		}
		return score;
	}
	public PLAYER getCurrentDealer() {
		return PLAYER.getDealerForHand(getHandNumber());
	}
	public int getHandNumber() {
		return handResults.size() + 1;
	}
	public List<HandResult> getHandResults() {
		return handResults;
	}
	public void clearHandResults() {
		this.handResults.clear();
	}
	public void addHandResult(HandResult result) {
		this.handResults.add(result);
	}
	public HandResult getHandResult(int handNumber) {
		for(HandResult result : this.handResults) {
			if(handNumber == result.handNumber) {
				return result;
			}
		}
		throw new IllegalArgumentException("No such hand found.");
	}
	public void removeLastResult() {
		if(handResults.size() > 0) {
			handResults.remove(0);
		}
	}
	public static VULNERABILITY getVulnerability(int handNumber, PLAYER biddingPlayer) {
		PLAYER dealer = PLAYER.getDealerForHand(handNumber);

		switch(dealer) {
		case SOUTH:
			return VULNERABILITY.NOT_VULNERABLE;
		case WEST:
		case NORTH:
			return biddingPlayer.isSamePartnership(dealer)?
					VULNERABILITY.VULNERABLE:
					VULNERABILITY.NOT_VULNERABLE;
		case EAST:
			return VULNERABILITY.VULNERABLE;
		}

		throw new IllegalArgumentException("Unknown player position");
	}
	public VULNERABILITY getVulnerability(PLAYER biddingPlayer) {
		return getVulnerability(getHandNumber(), biddingPlayer);
	}
	public Game() {
	}

}
