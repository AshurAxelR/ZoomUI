package com.xrbpowered.zoomui;

import java.awt.Toolkit;

import com.xrbpowered.zoomui.UIModalWindow.ResultHandler;

public abstract class UIWindowFactory {

	static {
		System.setProperty("sun.java2d.uiScale", "1.0");
	}
	
	public static UIWindowFactory instance = null;
	
	private float baseScale = getSystemScale();
	
	public float getBaseScale() {
		return baseScale;
	}
	
	public void setBaseScale(float scale) {
		baseScale = (scale > 0f) ? scale : getSystemScale();
	}
	
	public float globalPixelScale() {
		return 1f;
	}
	
	public static float getSystemScale() {
		return Toolkit.getDefaultToolkit().getScreenResolution() / 96f;
	}
	
	public abstract UIWindow create(String title, int w, int h, boolean canResize);
	public abstract <A> UIModalWindow<A> createModal(String title, int w, int h, boolean canResize, ResultHandler<A> onResult);
	public abstract UIPopupWindow createPopup();
	public abstract UIWindow createUndecorated(int w, int h);
	
}
