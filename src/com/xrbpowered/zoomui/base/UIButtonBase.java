package com.xrbpowered.zoomui.base;

import static com.xrbpowered.zoomui.MouseInfo.LEFT;

import com.xrbpowered.zoomui.ActionHandler;
import com.xrbpowered.zoomui.MouseInfo;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;

public abstract class UIButtonBase extends UIElement implements ActionHandler {

	public boolean down = false;
	private boolean enabled = true;

	public UIButtonBase(UIContainer parent) {
		super(parent);
	}
	
	@Override
	public boolean repaintOnHover() {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if(!enabled) {
			resetHover();
			down = false;
		}
	}
	
	public UIButtonBase disable() {
		setEnabled(false);
		return this;
	}

	@Override
	public void onAction() {
	}
	
	@Override
	public void onMouseIn() {
		if(isEnabled())
			super.onMouseIn();
	}
	
	@Override
	public void onMouseReleased() {
		down = false;
		repaint();
	}
	
	@Override
	public boolean onMouseDown(float x, float y, MouseInfo mouse) {
		if(mouse.eventButton==LEFT) {
			if(isEnabled()) {
				down = true;
				repaint();
			}
			return true;
		}
		else
			return false;
	}
	
	@Override
	public boolean onMouseUp(float x, float y, MouseInfo mouse, UIElement initiator) {
		if(initiator!=this)
			return false;
		if(mouse.eventButton==LEFT) {
			down = false;
			if(isEnabled())
				onAction();
			repaint();
			return true;
		}
		else
			return false;
	}

}
