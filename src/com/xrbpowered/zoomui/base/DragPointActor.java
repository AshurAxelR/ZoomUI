package com.xrbpowered.zoomui.base;

import com.xrbpowered.zoomui.DragActor;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIElement.Button;

public class DragPointActor implements DragActor {
	protected final UIElement ui;
	protected float posx, posy;
	protected float prevx, prevy;
	protected float pixelScale;
	
	public DragPointActor(UIElement ui) {
		this.ui = ui;
	}
	
	@Override
	public boolean notifyMouseDown(float x, float y, Button button, int mods) {
		pixelScale = ui.getPixelScale();
		posx = ui.rootToLocalX(x);
		posy = ui.rootToLocalY(y);
		prevx = posx;
		prevy = posy;
		return true;
	}

	@Override
	public boolean notifyMouseMove(float dx, float dy) {
		prevx = posx;
		prevy = posy;
		posx += dx*pixelScale;
		posy += dy*pixelScale;
		return true;
	}

	@Override
	public boolean notifyMouseUp(float x, float y, Button button, int mods, UIElement target) {
		prevx = posx;
		prevy = posy;
		posx = ui.rootToLocalX(x);
		posy = ui.rootToLocalY(y);
		return true;
	}

}
