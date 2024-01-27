package com.xrbpowered.zoomui;

public class InputInfo {

	public static final int NONE = 0;

	public static final int CTRL = 1;
	public static final int ALT = 2;
	public static final int SHIFT = 4;

	public final int mods;
	
	public InputInfo(int mods) {
		this.mods = mods;
	}

	public boolean isModDown(int key) {
		return (mods & key) == key;
	}
	
	public boolean isCtrlDown() {
		return isModDown(CTRL);
	}

	public boolean isAltDown() {
		return isModDown(ALT);
	}

	public boolean isShiftDown() {
		return isModDown(SHIFT);
	}


}
