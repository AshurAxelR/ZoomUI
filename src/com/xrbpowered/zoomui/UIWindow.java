package com.xrbpowered.zoomui;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;

public abstract class UIWindow {

	protected final UIWindowFactory factory;
	protected final RootContainer container;

	protected boolean exitOnClose = false;
	
	public UIWindow(UIWindowFactory factory) {
		this.factory = factory;
		this.container = createContainer();
	}
	
	public UIWindowFactory getFactory() {
		return factory;
	}
	
	protected RootContainer createContainer() {
		return new RootContainer(this, factory.getBaseScale());
	}
	
	public RootContainer getContainer() {
		return this.container;
	}

	public abstract int getClientWidth();
	public abstract int getClientHeight();
	public abstract void setClientSize(int width, int height);
	
	public boolean setClientSizeFor(Measurable m) {
		int w = (int)m.measureWidth();
		int h = (int)m.measureHeight();
		if(w>0 && h>0) {
			setClientSize(w, h);
			return true;
		}
		else
			return false;
	}
	
	public boolean setClientSizeToContent() {
		return setClientSizeFor(getContainer());
	}
	
	public abstract int getX();
	public abstract int getY();
	public abstract void moveTo(int x, int y);

	public abstract void center();
	
	public void move(int dx, int dy) {
		moveTo(getX()+dx, getY()+dy);
	}
	
	public void notifyResized() {
		getContainer().invalidateLayout();
		repaint();
	}
	
	public abstract boolean isVisible();
	public abstract void show();
	public abstract void repaint();
	
	public abstract int rootToScreenX(float x);
	public abstract int rootToScreenY(float y);
	public abstract float screenToRootX(int x);
	public abstract float screenToRootY(int y);
	
	public abstract void setCursor(Cursor cursor);
	
	public abstract FontMetrics getFontMetrics(Font font);
	
	public FontMetrics getFontMetrics(Font font, float size, float pixelScale) {
		return getFontMetrics(font.deriveFont(Math.round(size/pixelScale)));
	}

	
	public UIWindow exitOnClose(boolean exit) {
		this.exitOnClose = exit;
		return this;
	}
	
	public boolean onClosing() {
		return true;
	}
	
	public void onClose() {
		if(exitOnClose)
			System.exit(0);
	}
	
	public boolean requestClosing() {
		if(onClosing()) {
			close();
			return true;
		}
		else
			return false;
	}
	
	public void close() {
		onClose();
	}
	
	public void confirmClosing() {
		// FIXME confirmation dialog independent from Std components
		/*
		UIMessageBox.show("Exit", "Do you want to close the application?",
			UIMessageBox.iconQuestion, new MessageResult[] {MessageResult.ok, MessageResult.cancel},
			new MessageResultHandler() {
				@Override
				public void onResult(MessageResult result) {
					if(result==MessageResult.ok)
						close();
				}
			});
		*/
		close();
	}
	
}
