package com.xrbpowered.zoomui.base;

import static com.xrbpowered.zoomui.MouseInfo.LEFT;

import com.xrbpowered.zoomui.DragActor;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.MouseInfo;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;

public abstract class UIScrollBarBase extends UIContainer {

	public class Thumb extends UIElement {
		public int span = 0;
		public float top, bottom;
		public boolean down = false;
		
		public Thumb() {
			super(UIScrollBarBase.this);
		}

		@Override
		public boolean repaintOnHover() {
			return true;
		}

		public void updateLocation() {
			top = decButton==null ? 0 : (vertical ? decButton.getHeight() : decButton.getWidth());
			bottom = vertical ? (incButton==null ? getParent().getHeight() : incButton.getY()) : (incButton==null ? getParent().getWidth() : incButton.getX());
			if(isEnabled()) {
				float s = vertical ? getParent().getWidth() : getParent().getHeight();
				float h = span*(bottom-top)/(max-min+span);
				if(h<s) {
					span = (int)Math.ceil(s*(max-min)/(bottom-top-s));
					h = s;
				}
				float pos = value*(bottom-top)/(max-min+span)+top;
				
				if(vertical) {
					setSize(s, h);
					setPosition(0, pos);
				}
				else {
					setSize(h, s);
					setPosition(pos, 0);
				}
			}
		}
		
		@Override
		public DragActor acceptDrag(float x, float y, MouseInfo mouse) {
			if(dragThumbActor.startDrag(x, y, mouse))
				return dragThumbActor;
			else
				return null;
		}
		
		@Override
		public boolean onMouseDown(float x, float y, MouseInfo mouse) {
			if(mouse.eventButton==LEFT) {
				down = true;
				repaint();
				return true;
			}
			else
				return false;
		}
		
		@Override
		public boolean onMouseUp(float x, float y, MouseInfo mouse, UIElement initiator) {
			if(initiator==this) {
				down = false;
				repaint();
				return true;
			}
			else
				return false;
		}
		
		@Override
		public void paint(GraphAssist g) {
			paintThumb(g, this);
		}
	}
	
	private DragActor dragThumbActor = new DragActor() {
		private float pos;
		@Override
		public boolean startDrag(float x, float y, MouseInfo mouse) {
			if(mouse.eventButton==LEFT) {
				pos = vertical ? thumb.getY() : thumb.getX();
				thumb.down = true;
				return true;
			}
			return false;
		}

		@Override
		public boolean onMouseDrag(float rx, float ry, float drx, float dry, MouseInfo mouse) {
			pos += (vertical ? dry : drx) * getPixelSize();
			float s = (pos-thumb.top) / (thumb.bottom-thumb.top);
			if(setValue(Math.round(min + s*(max-min+thumb.span))))
				onChanged();
			repaint();
			return true;
		}

		@Override
		public void onDragFinish(float rx, float ry, MouseInfo mouse, UIElement target) {
			thumb.down = false;
			repaint();
		}

		@Override
		public void onDragCancel(float rx, float ry) {
			thumb.down = false;
			repaint();
		}
	};
	
	public final boolean vertical;
	public final UIButtonBase decButton, incButton;
	protected final Thumb thumb;
	
	private int thumbWidth;
	
	protected int min = 0;
	protected int max = 100;
	protected int step = 1;
	protected int value = 0;
	
	public UIScrollBarBase(UIContainer parent, boolean vertical, int thumbWidth) {
		super(parent);
		this.vertical = vertical;
		this.thumbWidth = thumbWidth;
		decButton = createArrowButton(-1);
		incButton = createArrowButton(+1);
		thumb = new Thumb();
	}
	
	protected abstract UIArrowButtonBase createArrowButton(final int delta);
	
	public void onChanged() {
	}
	
	public void setRange(int min, int max, int step) {
		this.min = min;
		this.max = max;
		this.step = step;
		checkRange();
	}

	private void checkRange() {
		if(max<min)
			max = min;
		if(value<min)
			value = min;
		if(value>max)
			value = max;
		if(decButton!=null)
			decButton.setEnabled(isEnabled());
		if(incButton!=null)
			incButton.setEnabled(isEnabled());
		thumb.setVisible(isEnabled());
	}
	
	public int getValue() {
		return value;
	}
	
	public boolean setValue(int v) {
		int old = this.value;
		this.value = v;
		checkRange();
		return old!=this.value;
	}
	
	public int getStep() {
		return step;
	}
	
	public boolean isEnabled() {
		return max>min && step>0;
	}
	
	public void setThumbSpan(int v) {
		thumb.span = v;
	}
	
	public void setThumbWidth(int thumbWidth) {
		this.thumbWidth = thumbWidth;
		if(vertical)
			setSize(thumbWidth, getHeight());
		else
			setSize(getWidth(), thumbWidth);
	}
	
	public int getThumbWidth() {
		return thumbWidth;
	}
	
	public void setLength(float length) {
		if(vertical)
			setSize(thumbWidth, length);
		else
			setSize(length, thumbWidth);
	}
	
	@Override
	public void layout() {
		float s = vertical ? getWidth() : getHeight();
		decButton.setPosition(0, 0);
		decButton.setSize(s, s);
		if(vertical)
			incButton.setPosition(0, getHeight()-s);
		else
			incButton.setPosition(getWidth()-s, 0);
		incButton.setSize(s, s);
	}
	
	protected void updateRange() {
	}
	
	@Override
	protected void paintBackground(GraphAssist g) {
		updateRange();
	}
	
	@Override
	protected void paintChildren(GraphAssist g) {
		thumb.updateLocation();
		super.paintChildren(g);
	}

	protected abstract void paintThumb(GraphAssist g, Thumb thumb);
	

}
