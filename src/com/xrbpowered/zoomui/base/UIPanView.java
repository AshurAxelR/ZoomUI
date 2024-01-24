package com.xrbpowered.zoomui.base;

import com.xrbpowered.zoomui.DragActor;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;

public class UIPanView extends UIContainer {

	public static final int UNLIMITED = -1;
	public static final int DISABLED = 0;
	
	private DragActor panActor = new DragActor() {
		@Override
		public boolean notifyMouseDown(float x, float y, Button button, int mods) {
			if(button==Button.right) {
				return true;
			}
			return false;
		}

		@Override
		public boolean notifyMouseMove(float dbx, float dby) {
			float pix = getPixelScale();
			pan(dbx*pix, dby*pix);
			repaint();
			return true;
		}

		@Override
		public boolean notifyMouseUp(float bx, float by, Button button, int mods, UIElement target) {
			return true;
		}
	};
	
	protected float panX = 0;
	protected float panY = 0;
	protected int maxPanX = -1;
	protected int maxPanY = -1;

	public UIPanView(UIContainer parent) {
		super(parent);
	}
	
	private void checkPanRange() {
		if(maxPanX!=DISABLED) {
			if(maxPanX>0) {
				if(panX<0) panX = 0f;
				if(panX>maxPanX) panX = maxPanX;
			}
		}
		else
			panX = 0;
		if(maxPanY!=DISABLED) {
			if(maxPanY>0) {
				if(panY<0) panY = 0f;
				if(panY>maxPanY) panY = maxPanY;
			}
		}
		else
			panY = 0;
	}
	
	public void setPan(float x, float y) {
		panX = x;
		panY = y;
		checkPanRange();
	}
	
	public void pan(float dx, float dy) {
		panX -= dx;
		panY -= dy;
		checkPanRange();
	}
	
	public void resetPan() {
		panX = 0f;
		panY = 0f;
	}
	
	public float getPanX() {
		return panX;
	}
	
	public float getPanY() {
		return panY;
	}
	
	public int getMaxPanX() {
		return maxPanX;
	}
	
	public int getMaxPanY() {
		return maxPanY;
	}
	
	public void setPanRange(int h, int v) {
		this.maxPanX = h;
		this.maxPanY = v;
		checkPanRange();
	}
	
	public void setPanRangeForClient(float width, float height) {
		int h = (int)(width-getWidth());
		if(h<0) h = 0;
		int v = (int)(height-getHeight());
		if(v<0) v = 0;
		setPanRange(h, v);
	}
	
	@Override
	protected float parentToLocalX(float px) {
		return super.parentToLocalX(px)+panX;
	}

	@Override
	protected float parentToLocalY(float py) {
		return super.parentToLocalY(py)+panY;
	}
	
	@Override
	protected float localToParentX(float x) {
		return super.localToParentX(x-panX);
	}
	
	@Override
	protected float localToParentY(float y) {
		return super.localToParentY(y-panY);
	}

	protected void applyTransform(GraphAssist g) {
		g.translate(-panX, -panY);
	}
	
	@Override
	protected void paintChildren(GraphAssist g) {
		if(g.pushClip(0, 0, getWidth(), getHeight())) {
			g.pushTx();
			applyTransform(g);
			super.paintChildren(g);
			g.popTx();
			g.popClip();
		}
	}
	
	@Override
	public UIElement getElementAt(float px, float py) {
		if(isInside(px, py))
			return super.getElementAt(px, py);
		else
			return null;
	}
	
	@Override
	public UIElement notifyMouseDown(float px, float py, Button button, int mods) {
		if(isInside(px, py))
			return super.notifyMouseDown(px, py, button, mods);
		else
			return null;
	}
	
	@Override
	public UIElement notifyMouseUp(float px, float py, Button button, int mods, UIElement initiator) {
		if(isInside(px, py))
			return super.notifyMouseUp(px, py, button, mods, initiator);
		else
			return null;
	}

	@Override
	public UIElement notifyMouseScroll(float px, float py, float delta, int mods) {
		if(isInside(px, py))
			return super.notifyMouseScroll(px, py, delta, mods);
		else
			return null;
	}

	@Override
	public DragActor acceptDrag(float x, float y, Button button, int mods) {
		if(panActor.notifyMouseDown(x, y, button, mods))
			return panActor;
		else
			return null;
	}
	
	@Override
	public boolean onMouseDown(float px, float py, Button button, int mods) {
		if(button==Button.right)
			return true;
		return false;
	}
	
}
