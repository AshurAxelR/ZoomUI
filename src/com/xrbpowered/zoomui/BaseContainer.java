package com.xrbpowered.zoomui;

import java.awt.Color;
import java.awt.RenderingHints;

import com.xrbpowered.zoomui.base.UILayersContainer;

public class BaseContainer extends UILayersContainer implements Measurable {

	public static class ModalBaseContainer<A> extends BaseContainer {
		protected ModalBaseContainer(UIModalWindow<A> window, float scale) {
			super(window, scale);
		}
		@SuppressWarnings("unchecked")
		@Override
		public UIModalWindow<A> getWindow() {
			return (UIModalWindow<A>) super.getWindow();
		}
	}
	
	public KeyInputHandler hotKeys = null;
	
	private float baseScale;
	private UIWindow window;
	private TabIndex tabIndex;
	
	protected BaseContainer(UIWindow window, float scale) {
		super(null);
		this.baseScale = scale;
		this.window = window;
		this.tabIndex = new TabIndex(this);
	}

	public BaseContainer replaceTabIndex(TabIndex tabIndex) {
		tabIndex.copyState(this.tabIndex);
		this.tabIndex = tabIndex;
		return this;
	}
	
	@Override
	public BaseContainer getBase() {
		return this;
	}
	
	public UIWindow getWindow() {
		return window;
	}
	
	public TabIndex tabIndex() {
		return tabIndex;
	}
	
	private UIElement uiUnderMouse = null;
	
	private DragActor drag = null;
	private UIElement uiInitiator = null;
	private Button initiatorButton = Button.left;
	private int initiatorMods = 0;
	private int prevMouseX = 0;
	private int prevMouseY = 0;
	
	protected boolean invalidLayout = true;

	private int clientBorderWidth = 0;
	private Color clientBorderColor = null;
	
	public void setClientBorder(int thickness, Color color) {
		this.clientBorderWidth = thickness;
		this.clientBorderColor = color;
	}
	
	public void removeClientBorder() {
		this.clientBorderWidth = 0;
		this.clientBorderColor = null;
	}
	
	@Override
	public void repaint() {
		window.repaint();
	}
	
	public void invalidateLayout() {
		invalidLayout = true;
	}

	public boolean onKeyPressed(char c, int code, int mods) {
		tabIndex.validate();
		if(tabIndex.hasFocus() && tabIndex.getFocus().onKeyPressed(c, code, mods))
			return true;
		else if(hotKeys!=null && hotKeys.onKeyPressed(c, code, mods))
			return true;
		else
			return tabIndex.onKeyPressed(c, code, mods);
	}
	
	@Override
	public UIElement notifyMouseDown(float px, float py, Button button, int mods) {
		if(drag==null) {
			prevMouseX = getWindow().baseToScreenX(px);
			prevMouseY = getWindow().baseToScreenY(py);
			initiatorButton = button;
			initiatorMods = mods;
			UIElement ui = super.notifyMouseDown(px, py, button, mods);
			if(ui!=uiInitiator && uiInitiator!=null)
				uiInitiator.onMouseReleased();
			uiInitiator = ui;
			//if(uiFocused!=null && uiFocused!=uiInitiator) // FIXME better strategy for losing focus?
			//	resetFocus();
		}
		return this;
	}
	
	@Override
	public UIElement notifyMouseUp(float px, float py, Button button, int mods, UIElement initiator) {
		if(drag!=null) {
			UIElement ui = getElementAt(px, py);
			if(drag.notifyMouseUp(px, py, button, mods, ui))
				drag = null;
		}
		else {
			if(super.notifyMouseUp(px, py, button, mods, uiInitiator)!=uiInitiator && uiInitiator!=null)
				uiInitiator.onMouseReleased(); // FIXME release for multi-button scenarios
		}
		return this;
	}
	
	@Override
	public void onMouseOut() {
		if(drag==null && uiUnderMouse!=null) {
			if(uiUnderMouse!=this)
				uiUnderMouse.onMouseOut();
			uiUnderMouse = null;
		}
	}
	
	private void updateMouseMove(float x, float y) {
		UIElement ui = getElementAt(x, y);
		if(ui!=uiUnderMouse) {
			if(uiUnderMouse!=null && uiUnderMouse!=this)
				uiUnderMouse.onMouseOut();
			uiUnderMouse = ui;
			if(uiUnderMouse!=null && uiUnderMouse!=this)
				uiUnderMouse.onMouseIn();
		}
	}
	
	@Override
	public void onMouseMoved(float x, float y, int mods) {
		if(drag==null) {
			updateMouseMove(x, y);
			if(uiUnderMouse!=null && uiUnderMouse!=this)
				uiUnderMouse.onMouseMoved(uiUnderMouse.baseToLocalX(x), uiUnderMouse.baseToLocalY(y), mods);
		}
	}
	
	public void onMouseDragged(float x, float y) {
		int sx = getWindow().baseToScreenX(x);
		int sy = getWindow().baseToScreenY(y);
		if(drag==null && uiInitiator!=null) {
			drag = uiInitiator.acceptDrag(getWindow().screenToBaseX(prevMouseX), getWindow().screenToBaseY(prevMouseY), initiatorButton, initiatorMods);
		}
		if(drag!=null) {
			if(!drag.notifyMouseMove(sx-prevMouseX, sy-prevMouseY))
				drag = null;
			prevMouseX = sx;
			prevMouseY = sy;
		}
		updateMouseMove(x, y);
	}
	
	public void resetFocus() {
		tabIndex.resetFocus();
	}

	public void setFocus(KeyInputHandler handler) {
		tabIndex.setFocus(handler);
	}
	
	public KeyInputHandler getFocus() {
		return tabIndex.getFocus();
	}

	public float getBaseScale() {
		return baseScale;
	}
	
	public void setBaseScale(float scale) {
		this.baseScale = (scale>0f) ? scale : getWindow().getFactory().getBaseScale();
		invalidateLayout();
	}
	
	@Override
	public float getPixelScale() {
		return getWindow().getFactory().globalPixelScale() / baseScale;
	}
	
	@Override
	protected float parentToLocalX(float px) {
		return px / baseScale;
	}
	
	@Override
	protected float parentToLocalY(float py) {
		return py / baseScale;
	}
	
	@Override
	protected float localToParentX(float x) {
		return x * baseScale;
	}

	@Override
	protected float localToParentY(float y) {
		return y * baseScale;
	}

	@Override
	public void layout() {
		super.layout();
		invalidLayout = false;
	}
	
	@Override
	public float measureWidth() {
		float max = 0;
		for(UIElement c : children) {
			if(c instanceof Measurable) {
				float w = ((Measurable) c).measureWidth();
				if(w>max)
					max = w;
			}
		}
		return max;
	}
	
	@Override
	public float measureHeight() {
		float max = 0;
		for(UIElement c : children) {
			if(c instanceof Measurable) {
				float h = ((Measurable) c).measureHeight();
				if(h>max)
					max = h;
			}
		}
		return max;
	}
	
	@Override
	public float getWidth() {
		return getWindow().getClientWidth() / baseScale;
	}
	
	@Override
	public float getHeight() {
		return getWindow().getClientHeight() / baseScale;
	}
	
	@Override
	public float getX() {
		return 0;
	}
	
	@Override
	public float getY() {
		return 0;
	}
	
	@Override
	public boolean isInside(float px, float py) {
		return true;
	}
	
	@Override
	public void paint(GraphAssist g) {
		if(invalidLayout)
			layout();
		if(g.graph!=null) {
			g.graph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			g.graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.graph.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		}
		super.paint(g);
		
		if(clientBorderWidth>0 && clientBorderColor!=null && g.graph!=null) {
			g.graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.graph.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
			GraphAssist.pixelRect(g.graph, 0, 0, window.getClientWidth(), window.getClientHeight(), clientBorderWidth, clientBorderColor);
		}
	}
	
	@Override
	protected void paintChildren(GraphAssist g) {
		g.pushTx();
		g.scale(baseScale);
		super.paintChildren(g);
		g.popTx();
	}
	
}
