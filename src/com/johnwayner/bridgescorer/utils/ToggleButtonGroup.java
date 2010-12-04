package com.johnwayner.bridgescorer.utils;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

public class ToggleButtonGroup<VALUETYPE> {
	
	private List<ToggleValue<VALUETYPE>> toggles = new ArrayList<ToggleValue<VALUETYPE>>();
	private VALUETYPE defaultValue = null;
	
	public ToggleButtonGroup(ToggleButton[] togglesArray, VALUETYPE[] valuesArray, VALUETYPE defaultValue)
	{
		if(togglesArray.length != valuesArray.length)
		{
			throw new IllegalArgumentException();
		}
		
		for(int i=0; i<togglesArray.length; i++)
		{
			toggles.add(new ToggleValue<VALUETYPE>(togglesArray[i], valuesArray[i]));
		}
		
		this.defaultValue = defaultValue;
		setSelectedValue(defaultValue);

		setupClickHandlers();
	}
	
	public ToggleButtonGroup(ToggleButton[] togglesArray, VALUETYPE[] valuesArray)
	{
		this(togglesArray, valuesArray, null);
	}
	
	private void setupClickHandlers()
	{
    	for (ToggleValue<VALUETYPE> toggleValue : toggles) {
    		toggleValue.toggle.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(((ToggleButton)v).isChecked()) {
						for (ToggleValue<VALUETYPE> toggleValue : toggles) {
							if(toggleValue.toggle.getId()!=v.getId()) {
								toggleValue.toggle.setChecked(false);
							}
						}
					}
					else
					{
						setSelectedValue(defaultValue);
					}
				}
			});
		}
	}
	
	public boolean hasNoValue()
	{
		return null==getSelectedValue();
	}
	
	public VALUETYPE getSelectedValue()
	{
		for (ToggleValue<VALUETYPE> toggleValue : toggles) {
			if(toggleValue.toggle.isChecked())
			{
				return toggleValue.value;
			}
		}
		
		return defaultValue;
	}
	
	public void setSelectedValue(VALUETYPE value)
	{
		for (ToggleValue<VALUETYPE> toggleValue : toggles) {
			toggleValue.toggle.setChecked(toggleValue.value.equals(value));
		}
	}
	
	public void reset()
	{
		setSelectedValue(defaultValue);
	}
	
	final private class ToggleValue<V> {
		public ToggleButton toggle;
		public V value;
		
		public ToggleValue(ToggleButton toggle, V value)
		{
			this.toggle = toggle;
			this.value = value;
		}
	}

}
