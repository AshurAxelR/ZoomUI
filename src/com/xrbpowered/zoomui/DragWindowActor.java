package com.xrbpowered.zoomui;

import static com.xrbpowered.zoomui.InputInfo.NONE;
import static com.xrbpowered.zoomui.MouseInfo.LEFT;

public class DragWindowActor implements DragActor {
	public final UIElement element;
	public final int triggerButton;
	public final int triggerMods;
	
	public DragWindowActor(UIElement element, int button, int mods) {
		this.element = element;
		triggerButton = button;
		triggerMods = mods;
	}

	public DragWindowActor(UIElement element) {
		this(element, LEFT, NONE);
	}

	public boolean isTrigger(MouseInfo mouse) {
		return (mouse.eventButton==triggerButton && mouse.mods==triggerMods);
	}
	
	@Override
	public boolean notifyMouseDown(float x, float y, MouseInfo mouse) {
		return isTrigger(mouse);
	}

	@Override
	public boolean notifyMouseMove(float rx, float ry, float drx, float dry, MouseInfo mouse) {
		element.getRoot().getWindow().move((int)drx, (int)dry);
		return true;
	}

	@Override
	public void notifyMouseUp(float rx, float ry, MouseInfo mouse, UIElement target) {
		// do nothing
	}
}