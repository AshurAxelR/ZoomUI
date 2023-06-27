package com.xrbpowered.zoomui;

public interface KeyInputHandler {

	public boolean onKeyPressed(char c, int code, int mods);
	public void onFocusGained();
	public void onFocusLost();
	
	public default boolean isFocused() {
		if(this instanceof UIElement)
			return ((UIElement) this).getBase().getFocus()==this;
		else
			return false;
	}
}
