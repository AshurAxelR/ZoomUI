package com.xrbpowered.zoomui;

import java.awt.Rectangle;

public abstract class UIElement {

	public enum Button {
		left, right, middle, unknown
	}
	public static final int modNone = 0;
	public static final int modCtrlMask = 1;
	public static final int modAltMask = 2;
	public static final int modShiftMask = 4;
	
	private final UIContainer parent;
	private final BaseContainer base;

	private boolean visible = true;
	private float x, y;
	private float width, height;
	
	public boolean hover = false;
	public boolean repaintOnHover = false;

	public UIElement(UIContainer parent) {
		this.parent = parent;
		this.base = (parent!=null) ? parent.getBase() : null;
		if(parent!=null)
			parent.addChild(this);
	}
	
	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public void setSize(float width, float height) {
		this.width = width;
		this.height = height;
	}
	
	public BaseContainer getBase() {
		return base;
	}
	
	protected float parentToLocalX(float px) {
		return px - this.x;
	}

	protected float parentToLocalY(float py) {
		return py - this.y;
	}
	
	protected float localToParentX(float x) {
		return x + this.x;
	}

	protected float localToParentY(float y) {
		return y + this.y;
	}

	public float baseToLocalX(float bx) {
		return parentToLocalX(parent==null ? bx : parent.baseToLocalX(bx));
	}

	public float baseToLocalY(float by) {
		return parentToLocalY(parent==null ? by : parent.baseToLocalY(by));
	}
	
	public float localToBaseX(float x) {
		return parent==null ? localToParentX(x) : parent.localToBaseX(localToParentX(x));
	}

	public float localToBaseY(float y) {
		return parent==null ? localToParentY(y) : parent.localToBaseY(localToParentY(y));
	}

	public float getPixelScale() {
		if(parent!=null)
			return parent.getPixelScale();
		else
			return 1f;
	}
	
	public void repaint() {
		if(parent!=null)
			parent.repaint();
	}
	
	public void invalidateTabIndex() {
		getBase().tabIndex().invalidate();
	}
	
	public void invalidateLayout() {
		getBase().invalidateLayout();
	}
	
	public void layout() {
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isParentVisible() {
		if(parent!=null)
			return parent.isVisible() && parent.isParentVisible();
		else
			return true;
	}
	
	public boolean isVisible(Rectangle clip) {
		return visible && (clip==null ||
			!(clip.x-x>getWidth() || clip.x-x+clip.width<0 ||
			clip.y-y>getHeight() || clip.y-y+clip.height<0));
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}

	public float getWidth() {
		return width;
	}
	
	public float getHeight() {
		return height;
	}
	
	public boolean isInside(float px, float py) {
		return isVisible() && px>=getX() && py>=getY() && px<=getX()+getWidth() && py<=getY()+getHeight();
	}
	
	public UIContainer getParent() {
		return parent;
	}
	
	public abstract void paint(GraphAssist g);
	
	public DragActor acceptDrag(float x, float y, Button button, int mods) {
		return null;
	}

	public UIElement getElementAt(float px, float py) {
		if(isInside(px, py))
			return this;
		else
			return null;
	}
	
	public UIElement notifyMouseDown(float px, float py, Button button, int mods) {
		if(isInside(px, py) && onMouseDown(px, py, button, mods))
			return this;
		else
			return null;
	}
	
	public UIElement notifyMouseUp(float px, float py, Button button, int mods, UIElement initiator) {
		if(isInside(px, py) && onMouseUp(px, py, button, mods, initiator))
			return this;
		else
			return null;
	}
	
	public UIElement notifyMouseScroll(float px, float py, float delta, int mods) {
		if(isInside(px, py) && onMouseScroll(px, py, delta, mods))
			return this;
		else
			return null;
	}

	public void onMouseIn() {
		hover = true;
		if(repaintOnHover)
			repaint();
	}
	
	public void onMouseOut() {
		hover = false;
		if(repaintOnHover)
			repaint();
	}
	
	public void onMouseReleased() {
	}
	
	public void onMouseMoved(float x, float y, int mods) {
	}
	
	public boolean onMouseDown(float px, float py, Button button, int mods) {
		// px, py are in parent space
		return false;
	}
	
	public boolean onMouseUp(float px, float py, Button button, int mods, UIElement initiator) {
		// px, py are in parent space
		return false;
	}
	
	public boolean onMouseScroll(float px, float py, float delta, int mods) {
		// px, py are in parent space
		return false;
	}
	
	public void onFocusGained() {
	}

	public void onFocusLost() {
	}
	
	public boolean isFocused() {
		return getBase().getFocus()==this;
	}
	
}
