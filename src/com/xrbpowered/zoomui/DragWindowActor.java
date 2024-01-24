package com.xrbpowered.zoomui;

import com.xrbpowered.zoomui.UIElement.Button;

public class DragWindowActor implements DragActor {
	public final UIElement element;
	public final Button triggerButton;
	public final int triggerMods;
	
	public DragWindowActor(UIElement element, Button button, int mods) {
		this.element = element;
		triggerButton = button;
		triggerMods = mods;
	}

	public DragWindowActor(UIElement element) {
		this(element, Button.left, UIElement.modNone);
	}

	public boolean isTrigger(Button button, int mods) {
		return (button==triggerButton && mods==triggerMods);
	}
	
	@Override
	public boolean notifyMouseDown(float x, float y, Button button, int mods) {
		return isTrigger(button, mods);
	}

	@Override
	public boolean notifyMouseMove(float dbx, float dby) {
		element.getBase().getWindow().move((int)dbx, (int)dby);
		return true;
	}

	@Override
	public boolean notifyMouseUp(float bx, float by, Button button, int mods, UIElement target) {
		return true;
	}
}