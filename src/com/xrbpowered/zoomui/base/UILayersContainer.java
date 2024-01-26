package com.xrbpowered.zoomui.base;

import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;

/**
 * UI container that layouts all children to fill its client area.
 * As the result, the children are stacked on top of each other.
 *
 */
public class UILayersContainer extends UIContainer {

	/**
	 * Constructor, see {@link UIElement#UIElement(UIContainer)}.
	 * @param parent parent container
	 */
	public UILayersContainer(UIContainer parent) {
		super(parent);
	}

	@Override
	public void layout() {
		for(UIElement c : children) {
			c.setPosition(0, 0);
			c.setSize(getWidth(), getHeight());
			c.layout();
		}
	}

}
