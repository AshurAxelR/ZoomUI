package com.xrbpowered.zoomui.std;

import java.awt.Color;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.base.UISplitContainerBase;

public class UISplitContainer extends UISplitContainerBase {

	public static final float defaultHitSize = 8f;

	public static Color colorSplit = UIScrollContainer.colorBorder;

	public UISplitContainer(UIContainer parent, boolean vertical, float splitRatio, float hitSize) {
		super(parent, vertical, splitRatio, hitSize);
	}

	public UISplitContainer(UIContainer parent, boolean vertical, float splitRatio) {
		super(parent, vertical, splitRatio, defaultHitSize);
	}

	@Override
	protected void paintSplit(GraphAssist g, UIElement splitter) {
		if(vertical) {
			float y = splitter.getHeight()/2;
			g.line(0, y, splitter.getWidth(), y, UIScrollContainer.colorBorder);
		}
		else {
			float x = splitter.getWidth()/2;
			g.line(x, 0, x, splitter.getHeight(), UIScrollContainer.colorBorder);
		}
	}

}
