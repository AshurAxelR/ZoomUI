package com.xrbpowered.zoomui.base;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.MouseInfo;
import com.xrbpowered.zoomui.UIContainer;

public abstract class UIScrollContainerBase extends UIContainer {

	public int scrollStep = GraphAssist.ptToPixels(9f);
	public int wheelStep = 3*scrollStep;
	
	private UIPanView view;
	private UIScrollBarBase scroll;
	
	public UIScrollContainerBase(UIContainer parent) {
		super(parent);
		view = new UIPanView(this);
		scroll = createScroll();
	}
	
	protected abstract UIScrollBarBase createScroll();
	
	public void updateScrollRange(UIScrollBarBase scroll) {
		if(view.getMaxPanY()>0) {
			scroll.setThumbSpan(Math.round(getHeight()));
			scroll.setRange(0, view.getMaxPanY(), scrollStep);
			scroll.setValue(Math.round(view.getPanY()));
		}
		else
			scroll.setRange(0, 0, 0);
	}
	
	public UIPanView getView() {
		return view;
	}
	
	@Override
	public
	final void layout() {
		scroll.setLength(getHeight());
		scroll.setPosition(getWidth()-scroll.getWidth(), 0);
		scroll.layout();
		
		view.setPosition(0, 0);
		view.setSize(getWidth()-scroll.getWidth(), getHeight());
		view.setPanRangeForClient(0, layoutView());
		view.layout();
	}
	
	protected abstract float layoutView();
	
	@Override
	protected void paintChildren(GraphAssist g) {
		super.paintChildren(g);
		paintBorder(g);
	}
	
	protected void paintBorder(GraphAssist g) {
	}
	
	@Override
	public boolean onMouseScroll(float x, float y, float delta, MouseInfo mouse) {
		if(mouse.mods==MouseInfo.NONE) {
			view.pan(0, -delta*wheelStep);
			repaint();
			return true;
		}
		else
			return false;
	}

}
