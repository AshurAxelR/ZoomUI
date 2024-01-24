package com.xrbpowered.zoomui.base;

import java.awt.Cursor;

import com.xrbpowered.zoomui.DragActor;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;

public abstract class UISplitContainerBase extends UIContainer {

	private DragActor splitDragActor = new DragActor() {
		@Override
		public boolean notifyMouseDown(float x, float y, Button button, int mods) {
			if(button==Button.left && getWidth()>0 && getHeight()>0) {
				return true;
			}
			return false;
		}

		@Override
		public boolean notifyMouseMove(float dx, float dy) {
			float pix = getPixelScale();
			float delta = vertical ? (dy*pix)/getHeight() : (dx*pix)/getWidth();
			setSplitRatio(splitRatio + delta);
			repaint();
			return true;
		}

		@Override
		public boolean notifyMouseUp(float x, float y, Button button, int mods, UIElement target) {
			return true;
		}
	};
	
	private class UISplitter extends UIElement {
		public UISplitter(float hitSize) {
			super(UISplitContainerBase.this);
			setSize(hitSize, hitSize);
		}
		
		@Override
		public void paint(GraphAssist g) {
			paintSplit(g, UISplitter.this);
		}
		
		@Override
		public void onMouseIn() {
			getRoot().getWindow().setCursor(cursor);
		}
		
		@Override
		public void onMouseOut() {
			getRoot().getWindow().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		
		@Override
		public DragActor acceptDrag(float x, float y, Button button, int mods) {
			if(splitDragActor.notifyMouseDown(x, y, button, mods))
				return splitDragActor;
			else
				return null;
		}
		
		@Override
		public boolean onMouseDown(float x, float y, Button button, int mods) {
			if(button==Button.left)
				return true;
			return false;
		}
	}
	
	public final boolean vertical;
	public final UIContainer first, second;

	protected final UISplitter splitter;
	protected Cursor cursor;

	private float splitRatio;
	
	public UISplitContainerBase(UIContainer parent, boolean vertical, float splitRatio, float hitSize) {
		super(parent);
		this.vertical = vertical;
		this.splitRatio = splitRatio;
		first = new UILayersContainer(this);
		second = new UILayersContainer(this);
		splitter = new UISplitter(hitSize);
		cursor = vertical ? Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR) : Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
	}
	
	public float getSplitRatio() {
		return splitRatio;
	}
	
	public void setSplitRatio(float splitRatio) {
		if(splitRatio<0f)
			splitRatio = 0f;
		else if(splitRatio>1f)
			splitRatio = 1f;
		this.splitRatio = splitRatio;
		invalidateLayout();
	}

	@Override
	public void layout() {
		if(vertical) {
			float split = getHeight() * splitRatio; 
			first.setSize(getWidth(), split);
			first.setPosition(0, 0);
			second.setSize(getWidth(), getHeight()-split);
			second.setPosition(0, split);
			splitter.setSize(getWidth(), splitter.getHeight());
			splitter.setPosition(0, split-splitter.getHeight()/2);
		}
		else {
			float split = getWidth() * splitRatio; 
			first.setSize(split, getHeight());
			first.setPosition(0, 0);
			second.setSize(getWidth()-split, getHeight());
			second.setPosition(split, 0);
			splitter.setSize(splitter.getWidth(), getHeight());
			splitter.setPosition(split-splitter.getWidth()/2, 0);
		}
		super.layout();
	}
	
	@Override
	protected void paintChildren(GraphAssist g) {
		super.paintChildren(g);
		paintBorder(g);
	}
	
	protected void paintBorder(GraphAssist g) {
	}
	
	protected abstract void paintSplit(GraphAssist g, UIElement splitter);

}
