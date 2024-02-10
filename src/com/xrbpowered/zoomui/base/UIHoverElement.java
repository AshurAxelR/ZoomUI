package com.xrbpowered.zoomui.base;

import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;

public abstract class UIHoverElement extends UIElement {

	public UIHoverElement(UIContainer parent) {
		super(parent);
		repaintOnHover = true;
	}
	
}
