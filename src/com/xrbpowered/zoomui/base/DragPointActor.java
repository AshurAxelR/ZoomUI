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
		posx = ui.baseToLocalX(x);
		posy = ui.baseToLocalY(y);
		prevx = posx;
		prevy = posy;
		return true;
	}

	@Override
	public boolean notifyMouseMove(float dbx, float dby) {
		prevx = posx;
		prevy = posy;
		posx += dbx*pixelScale;
		posy += dby*pixelScale;
		return true;
	}

	@Override
	public boolean notifyMouseUp(float bx, float by, Button button, int mods, UIElement target) {
		prevx = posx;
		prevy = posy;
		posx = ui.baseToLocalX(bx);
		posy = ui.baseToLocalY(by);
		return true;
	}

}
