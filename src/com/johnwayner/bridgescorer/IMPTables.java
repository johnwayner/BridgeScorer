package com.johnwayner.bridgescorer;

public class IMPTables {
	static final private int[][] IMPHCPTargets = {
			{0, 0},
			{50, 50},
			{70, 70},
			{110, 110},
			{200, 290},
			{300, 440},
			{350, 520},
			{400, 600},
			{430, 630},
			{460, 660},
			{490, 690},
			{600, 900},
			{700, 1050}, 
			{900, 1350},
			{1000, 1500},
			{1100, 1650},
			{1200, 1800},
			{1300, 1950},
			{1300, 1950},
			{1300, 1950},
			{1300, 1950}
	};
	
	static final private int[] IMPValues = {
			 10, 
			 40,
			 80,
			 120,
			 160,
			 210,
			 260,
			 310,
			 360,
			 420,
			 490,
			 590,
			 740,
			 890,
			 1090,
			 1290,
			 1490,
			 1740,
			 1990,
			 2240,
			 2490,
			 2990,
			 3490,
			 3990,
			 Integer.MAX_VALUE			
	};
	
	public enum VULNERABILITY {
		VULNERABLE(1),
		NOT_VULNERABLE(0);
		
		public int index;
		private VULNERABILITY(int index) {
			this.index = index;
		}
	};
	
	static public int getTargetScore(int hcp, VULNERABILITY vulnerability)
	{
		int index = hcp - 20;

		return ((index < 0)?-1:1) * IMPHCPTargets[Math.abs(index)][vulnerability.index];		
	}
	
	static public int getIMPScore(int actualScore, int targetScore)
	{
		int difference = actualScore - targetScore;
		int absDifference = Math.abs(difference);
		
		int score;		
		for(score=0; absDifference > IMPValues[score]; score++);
		
		if(difference < 0)
		{
			return -1 * score;
		} else {
			return score;
		}
	}
}
