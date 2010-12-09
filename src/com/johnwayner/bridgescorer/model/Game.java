package com.johnwayner.bridgescorer.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import com.johnwayner.bridgescorer.IMPTables.VULNERABILITY;
import com.johnwayner.bridgescorer.model.HandResult.PLAYER;

@Root
public class Game {
	@Element
	private Date startDate = new Date();
	@Element
	private int NSScore = 0;
	@Element
	private int EWScore = 0;
	@Element
	private PLAYER currentDealer = PLAYER.NORTH;
//	private int handNumber = 1;
	@ElementList
	private List<HandResult> handResults = new ArrayList<HandResult>();
	
	public Date getStartDate() {
		return startDate;
	}
	public int getNSScore() {
		return NSScore;
	}
	public void setNSScore(int nSScore) {
		NSScore = nSScore;
	}
	public void increaseNSScore(int additionalScore) {
		NSScore += additionalScore;
	}
	public int getEWScore() {
		return EWScore;
	}
	public void setEWScore(int eWScore) {
		EWScore = eWScore;
	}
	public void increaseEWScore(int additionalScore) {
		EWScore += additionalScore;
	}
	public PLAYER getCurrentDealer() {
		return currentDealer;
	}
	public void setCurrentDealer(PLAYER currentDealer) {
		this.currentDealer = currentDealer;
	}
	public void setNextDealer() {
		currentDealer = currentDealer.getNextDealer();
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
	public VULNERABILITY getVulnerability(PLAYER biddingPlayer) {
		switch(currentDealer) {
		case NORTH:
			return VULNERABILITY.NOT_VULNERABLE;
		case EAST:
		case SOUTH:
			return biddingPlayer.isSamePartnership(currentDealer)?
					VULNERABILITY.VULNERABLE:
					VULNERABILITY.NOT_VULNERABLE;
		case WEST:
			return VULNERABILITY.VULNERABLE;
		}
		
		throw new IllegalArgumentException("Unknown player position");
	}
	public Game() {
	}
	
}