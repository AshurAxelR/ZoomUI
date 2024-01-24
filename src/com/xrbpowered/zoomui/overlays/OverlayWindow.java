package com.xrbpowered.zoomui.overlays;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;

import com.xrbpowered.zoomui.BaseContainer;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIModalWindow;
import com.xrbpowered.zoomui.UIWindow;

public class OverlayWindow<A> extends UIModalWindow<A> {

	public final boolean dismissable;

	public Color overlayColor = new Color(0x11000000, true);
	
	private class Overlay extends UIContainer {
		public Overlay(OverlayBaseContainer<A> base) {
			super(getHost());
			setLocation(0, 0);
			setSize(getParent().getWidth(), getParent().getHeight());
			addChild(base);
			setVisible(false);
		}
		
		@Override
		public boolean onMouseDown(float px, float py, Button button, int mods) {
			if(dismissable)
				close();
			return true;
		}
		
		@Override
		public boolean onMouseUp(float px, float py, Button button, int mods, UIElement initiator) {
			return true;
		}
		
		@Override
		public boolean onMouseScroll(float px, float py, float delta, int mods) {
			return true;
		}
		
		@Override
		protected void paintSelf(GraphAssist g) {
			if(overlayColor!=null)
				g.fill(this, overlayColor);
		}
	}
	
	private Overlay overlay;
	private OverlayBaseContainer<A> box;
	
	public OverlayWindow(OverlayWindowFactory factory, int w, int h, boolean dismissable) {
		super(factory);
		this.dismissable = dismissable;
		
		setClientSize(w, h);
		center();
	}
	
	public UIContainer getHost() {
		return ((OverlayWindowFactory) factory).host;
	}
	
	public UIWindow getHostWindow() {
		return getHost().getBase().getWindow();
	}
	
	@Override
	protected BaseContainer createContainer() {
		box = new OverlayBaseContainer<>(this);
		box.setClientBorder(1, Color.BLACK);
		overlay = new Overlay(box);
		return box;
	}
	
	@Override
	public void close() {
		overlay.getParent().removeChild(overlay);
		repaint();
		super.close();
	}
	
	@Override
	public void closeWithResult(A result) {
		overlay.getParent().removeChild(overlay);
		repaint();
		super.closeWithResult(result);
	}

	@Override
	public int getClientWidth() {
		return box.getBoxWidth();
	}

	@Override
	public int getClientHeight() {
		return box.getBoxHeight();
	}

	@Override
	public void setClientSize(int width, int height) {
		box.setBoxSize(width, height);
		box.invalidateLayout();
		repaint();
	}

	@Override
	public int getX() {
		return box.getBoxX();
	}

	@Override
	public int getY() {
		return box.getBoxY();
	}

	@Override
	public void moveTo(int x, int y) {
		box.setBoxLocation(x, y);
		repaint();
	}

	@Override
	public void center() {
		moveTo(
			(int)(getHost().getWidth() - box.getBoxWidth())/2,
			(int)(getHost().getHeight() - box.getBoxHeight())/2
		);
	}

	@Override
	public boolean isVisible() {
		return overlay.isVisible();
	}

	@Override
	public void show() {
		overlay.layout();
		overlay.setVisible(true);
		repaint();
	}

	@Override
	public void repaint() {
		getHost().repaint();
	}

	@Override
	public int baseToScreenX(float x) {
		return getHostWindow().baseToScreenX(x);
	}

	@Override
	public int baseToScreenY(float y) {
		return getHostWindow().baseToScreenY(y);
	}

	@Override
	public float screenToBaseX(int x) {
		return getHostWindow().screenToBaseX(x);
	}

	@Override
	public float screenToBaseY(int y) {
		return getHostWindow().screenToBaseY(y);
	}

	@Override
	public void setCursor(Cursor cursor) {
		getHostWindow().setCursor(cursor);
	}

	@Override
	public FontMetrics getFontMetrics(Font font) {
		return getHostWindow().getFontMetrics(font);
	}

}
