package com.xrbpowered.zoomui.overlays;

import java.awt.Color;

import com.xrbpowered.zoomui.BaseContainer.ModalBaseContainer;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIElement;

public class OverlayBaseContainer<A> extends ModalBaseContainer<A> {

	public Color backgroundColor = new Color(0xeeeeee);
	
	private int boxX, boxY, boxWidth, boxHeight;
	
	public OverlayBaseContainer(OverlayWindow<A> window) {
		super(window, 1f);
	}

	public int getBoxX() {
		return boxX;
	}
	
	public int getBoxY() {
		return boxY;
	}
	
	public int getBoxWidth() {
		return boxWidth;
	}
	
	public int getBoxHeight() {
		return boxHeight;
	}

	public void setBoxLocation(int x, int y) {
		this.boxX = x;
		this.boxY = y;
		setLocation(x, y);
	}
	
	public void setBoxSize(int width, int height) {
		this.boxWidth = width;
		this.boxHeight = height;
		setSize(width, height);
	}
	
	public boolean isInsideBox(float x, float y) {
		return x>=boxX && y>=boxY && x<=boxX+boxWidth && y<=boxY+boxHeight;
	}
	
	@Override
	public float getX() {
		return boxX;
	}
	
	@Override
	public float getY() {
		return boxY;
	}
	
	@Override
	public float getWidth() {
		return boxWidth;
	}
	
	@Override
	public float getHeight() {
		return boxHeight;
	}
	
	@Override
	public UIElement getElementAt(float px, float py) {
		return super.getElementAt(px-boxX, py-boxY);
	}
	
	@Override
	public UIElement notifyMouseDown(float px, float py, Button button, int mods) {
		if(isInsideBox(px, py))
			return super.notifyMouseDown(px-boxX, py-boxY, button, mods);
		else
			return null;
	}
	
	@Override
	public UIElement notifyMouseUp(float px, float py, Button button, int mods, UIElement initiator) {
		if(isInsideBox(px, py))
			return super.notifyMouseUp(px-boxX, py-boxY, button, mods, initiator);
		else
			return null;
	}
	
	@Override
	public UIElement notifyMouseScroll(float px, float py, float delta, int mods) {
		if(isInsideBox(px, py))
			return super.notifyMouseScroll(px-boxX, py-boxY, delta, mods);
		else
			return null;
	}
	
	@Override
	protected void paintSelf(GraphAssist g) {
		g.fill(this, backgroundColor);
	}
	
}
