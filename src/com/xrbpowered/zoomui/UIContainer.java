package com.xrbpowered.zoomui;

import java.awt.Rectangle;
import java.util.ArrayList;

public abstract class UIContainer extends UIElement {

	protected ArrayList<UIElement> children = new ArrayList<>();

	public UIContainer(UIContainer parent) {
		super(parent);
	}

	public Iterable<UIElement> getChildren() {
		return children;
	}

	protected void addChild(UIElement c) {
		children.add(c);
		invalidateLayout();
		invalidateTabIndex();
	}

	public void removeChild(UIElement c) {
		if(children.remove(c)) {
			invalidateLayout();
			invalidateTabIndex();
		}
	}

	public void removeAllChildren() {
		children.clear();
		invalidateLayout();
		invalidateTabIndex();
	}

	@Override
	public void layout() {
		for(UIElement c : children) {
			c.layout();
		}
	}

	/**
	 * Draws the background or underlay contents. This method is called from {@link #paint(GraphAssist)}
	 * before drawing children.
	 * @param g wrapper for {@link Graphics2D} canvas in local coordinates
	 */
	protected void paintBackground(GraphAssist g) {
		// default container has no visible components 
	}

	/**
	 * Recursively draws child elements. This method is called from {@link #paint(GraphAssist)}.
	 * Does not automatically update clip area to this container's bounds, but can skip painting (cull) children
	 * that are outside of the existing clip area according to {@link UIElement#isVisible(Rectangle)}.
	 * 
	 * @param g wrapper for {@link Graphics2D} canvas in local coordinates
	 */
	protected void paintChildren(GraphAssist g) {
		Rectangle clip = g.getClip();
		for(UIElement c : children) {
			if(c.isVisible(clip)) {
				g.pushTx();
				g.translate(c.getX(), c.getY());
				c.paint(g);
				g.popTx();
			}
		}
	}

	/**
	 * Draws the foreground or overlay contents. This method is called from {@link #paint(GraphAssist)}
	 * after drawing children.
	 * @param g wrapper for {@link Graphics2D} canvas in local coordinates
	 */
	protected void paintForeground(GraphAssist g) {
		// default container has no visible components 
	}

	@Override
	public void paint(GraphAssist g) {
		paintBackground(g);
		paintChildren(g);
		paintForeground(g);
	}

	@Override
	public UIElement getElementAt(float px, float py) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(px);
		float cy = parentToLocalY(py);
		for(int i = children.size() - 1; i>=0; i--) {
			UIElement e = children.get(i).getElementAt(cx, cy);
			if(e!=null)
				return e;
		}
		return super.getElementAt(px, py);
	}

	@Override
	public UIElement notifyMouseDown(float px, float py, Button button, int mods) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(px);
		float cy = parentToLocalY(py);
		for(int i = children.size() - 1; i>=0; i--) {
			UIElement e = children.get(i).notifyMouseDown(cx, cy, button, mods);
			if(e!=null)
				return e;
		}
		return super.notifyMouseDown(px, py, button, mods);
	}

	@Override
	public UIElement notifyMouseUp(float px, float py, Button button, int mods, UIElement initiator) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(px);
		float cy = parentToLocalY(py);
		for(int i = children.size() - 1; i>=0; i--) {
			UIElement e = children.get(i).notifyMouseUp(cx, cy, button, mods, initiator);
			if(e!=null)
				return e;
		}
		return super.notifyMouseUp(px, py, button, mods, initiator);
	}

	@Override
	public UIElement notifyMouseScroll(float px, float py, float delta, int mods) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(px);
		float cy = parentToLocalY(py);
		for(int i = children.size() - 1; i>=0; i--) {
			UIElement e = children.get(i).notifyMouseScroll(cx, cy, delta, mods);
			if(e!=null)
				return e;
		}
		return super.notifyMouseScroll(px, py, delta, mods);
	}

}
