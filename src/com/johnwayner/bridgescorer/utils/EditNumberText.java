package com.johnwayner.bridgescorer.utils;

import android.widget.EditText;
import android.widget.Toast;

public class EditNumberText {
	private EditText textField;
	private String label;
	private int minValue = Integer.MIN_VALUE;
	private int maxValue = Integer.MAX_VALUE;
	
	public EditNumberText(EditText textField, String label, int minValue, int maxValue)
	{
		this.textField = textField;
		this.label = label;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	public int getValue(boolean showErrorDialog) throws NumberFormatException
	{
		String valStr = textField.getText().toString();

		try {
			int val = Integer.parseInt(valStr);
			if(val>maxValue || val<minValue) {			
				throw new NumberFormatException();
			}
			return val;
		} catch (NumberFormatException n)
		{
			if(showErrorDialog)
			{
				Toast.makeText(textField.getContext(), label + " must be between " + minValue + " and " + maxValue + ", inclusive.", Toast.LENGTH_LONG).show();
			}
			throw n;
		}
	}
	
	public void reset()
	{
		textField.setText("");
	}
}
