package com.xrbpowered.zoomui;

import com.xrbpowered.zoomui.UIElement.Button;

public interface DragActor {

	public boolean notifyMouseDown(float x, float y, Button button, int mods);
	public boolean notifyMouseMove(float dbx, float dby);
	public boolean notifyMouseUp(float bx, float by, Button button, int mods, UIElement target);

}
