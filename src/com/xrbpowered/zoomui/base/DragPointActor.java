package com.xrbpowered.zoomui.base;

import com.xrbpowered.zoomui.DragActor;
import com.xrbpowered.zoomui.MouseInfo;
import com.xrbpowered.zoomui.UIElement;

public class DragPointActor implements DragActor {
	protected final UIElement ui;
	protected float posx, posy;
	protected float prevx, prevy;
	
	public DragPointActor(UIElement ui) {
		this.ui = ui;
	}
	
	@Override
	public boolean startDrag(float x, float y, MouseInfo mouse) {
		posx = x;
		posy = y;
		prevx = posx;
		prevy = posy;
		return true;
	}

	@Override
	public boolean onMouseDrag(float rx, float ry, float drx, float dry, MouseInfo mouse) {
		prevx = posx;
		prevy = posy;
		posx = ui.rootToLocalX(rx);
		posy = ui.rootToLocalY(ry);
		return true;
	}

	@Override
	public void onDragFinish(float rx, float ry, MouseInfo mouse, UIElement target) {
		prevx = posx;
		prevy = posy;
		posx = ui.rootToLocalX(rx);
		posy = ui.rootToLocalY(ry);
	}

}
