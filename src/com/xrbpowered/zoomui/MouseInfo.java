package com.xrbpowered.zoomui;

public class MouseInfo extends InputInfo {

	public static final int LEFT = 1;
	public static final int RIGHT = 2;
	public static final int MIDDLE = 4;
	public static final int BUTTON4 = 8;
	public static final int BUTTON5 = 16;

	public static final int UNKNOWN = 128;

	public final int eventButton;
	public final int clickCount;
	public final int buttons;
	
	public MouseInfo(int eventButton, int buttons, int mods, int clickCount) {
		super(mods);
		this.eventButton = eventButton;
		this.clickCount = clickCount;
		this.buttons = buttons;
	}

	public MouseInfo(int eventButton, int buttons, int mods) {
		this(eventButton, buttons, mods, 1);
	}
	
	public boolean isButtonDown(int button) {
		return (buttons & button) == button;
	}
	
	public boolean isAnyButtonDown() {
		return buttons != NONE;
	}

}
