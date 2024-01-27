package com.xrbpowered.zoomui;

public interface KeyInputHandler {

	public boolean onKeyPressed(char c, int code, InputInfo input);
	
	public default boolean isEnabled() {
		return true;
	}
	
	public default UIElement asElement() {
		if(this instanceof UIElement)
			return (UIElement) this;
		else
			return null;
	}

}
