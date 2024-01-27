package com.xrbpowered.zoomui;

import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * UI element that can contain other UI elements as its children.
 * 
 * <p>For performance reasons, it uses thread-unsafe collection to store children.
 * Therefore, all modifications of the UI hierarchy should happen in the UI (Swing) thread.</p>
 * 
 * <p>Beware of concurrent modifications when adding or removing children from within
 * mouse event handlers or paint methods.</p>
 * 
 * <p>Default <code>UIContainer</code> has no visual representation. Subclasses
 * may override {@link #paintBackground(GraphAssist)} or {@link #paintForeground(GraphAssist)}
 * to draw contents in addition to child elements.</p>
 * 
 * <p>Default {@link #layout()} implementation calls <code>layout()</code> for child elements, but does not actually
 * position the children. Subclasses are expected to override this method to position child
 * elements and then propagate the method to its children after their positions and sizes are set.
 * (e.g., by calling <code>super.layout()</code>)</p>
 *
 * @see UIElement
 */
public abstract class UIContainer extends UIElement {

	protected ArrayList<UIElement> children = new ArrayList<>();

	/**
	 * Constructor, see {@link UIElement#UIElement(UIContainer)}.
	 * @param parent parent container
	 */
	public UIContainer(UIContainer parent) {
		super(parent);
	}

	/**
	 * Registers an element as a child element of this container.
	 * This function is automatically called from the {@link UIElement} constructor.
	 * @param c new child element
	 */
	protected void addChild(UIElement c) {
		children.add(c);
		invalidateLayout();
		invalidateTabIndex();
	}

	/**
	 * Returns the list of child elements as unmodifiable iterable.
	 * @return iterable over children
	 */
	public Iterable<UIElement> getChildren() {
		return children;
	}

	/**
	 * Returns the number of child elements.
	 * @return the number of children
	 */
	public int countChildren() {
		return children.size();
	}

	/**
	 * Returns a child element by its index.
	 * 
	 * @param index child index
	 * @return child element
	 * @throws ArrayIndexOutOfBoundsException if <code>index</code> is out of range
	 * 
	 * @see #countChildren()
	 * @see #getChildren()
	 */
	public UIElement getChild(int index) {
		return children.get(index);
	}

	/**
	 * Removes an element from the list of children, so it no longer participates in the UI hierarchy.
	 * Does nothing if <code>c</code> is not a child of this container.
	 * 
	 * <p>Changing UI hierarchy calls {@link #invalidateLayout()} and {@link #invalidateTabIndex()} automatically.</p>
	 * 
	 * @param c child element to remove
	 * 
	 * @see #removeAllChildren()
	 */
	public void removeChild(UIElement c) {
		if(children.remove(c)) {
			invalidateLayout();
			invalidateTabIndex();
		}
	}

	/**
	 * Removes all child elements from this container.
	 * 
	 * <p>Changing UI hierarchy calls {@link #invalidateLayout()} and {@link #invalidateTabIndex()} automatically.</p>
	 * 
	 * @see #removeChild(UIElement)
	 */
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
	public UIElement notifyMouseDown(float px, float py, MouseInfo mouse) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(px);
		float cy = parentToLocalY(py);
		for(int i = children.size() - 1; i>=0; i--) {
			UIElement e = children.get(i).notifyMouseDown(cx, cy, mouse);
			if(e!=null)
				return e;
		}
		return super.notifyMouseDown(px, py, mouse);
	}

	@Override
	public UIElement notifyMouseUp(float px, float py, MouseInfo mouse, UIElement initiator) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(px);
		float cy = parentToLocalY(py);
		for(int i = children.size() - 1; i>=0; i--) {
			UIElement e = children.get(i).notifyMouseUp(cx, cy, mouse, initiator);
			if(e!=null)
				return e;
		}
		return super.notifyMouseUp(px, py, mouse, initiator);
	}

	@Override
	public UIElement notifyMouseScroll(float px, float py, float delta, MouseInfo mouse) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(px);
		float cy = parentToLocalY(py);
		for(int i = children.size() - 1; i>=0; i--) {
			UIElement e = children.get(i).notifyMouseScroll(cx, cy, delta, mouse);
			if(e!=null)
				return e;
		}
		return super.notifyMouseScroll(px, py, delta, mouse);
	}

}
