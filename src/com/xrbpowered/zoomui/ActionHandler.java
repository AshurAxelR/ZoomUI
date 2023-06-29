package com.xrbpowered.zoomui;

public interface ActionHandler {

	public void onAction();
	
	public default boolean isEnabled() {
		return true;
	}
	
}
