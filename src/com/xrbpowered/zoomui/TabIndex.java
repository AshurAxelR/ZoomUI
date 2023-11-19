package com.xrbpowered.zoomui;

import java.awt.event.KeyEvent;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class TabIndex {

	public final BaseContainer base;

	private KeyInputHandler uiFocused = null;
	
	protected KeyInputHandler stickyFocus = null;
	protected ArrayList<KeyInputHandler> list = new ArrayList<>();
	protected int lastSelectedIndex = 0;
	protected boolean invalidList = true;
	
	public TabIndex(BaseContainer base) {
		this.base = base;
	}
	
	public void copyState(TabIndex src) {
		this.stickyFocus = src.stickyFocus;
		this.uiFocused = src.uiFocused;
		this.lastSelectedIndex = src.lastSelectedIndex;
		this.invalidList = true;
	}
	
	public void setStickyFocus(KeyInputHandler tab) {
		if(tab.asElement()==null)
			throw new InvalidParameterException();
		
		resetFocus();
		this.stickyFocus = tab;
		updateIndex();
		setFocus(tab);
	}
	
	public void invalidate() {
		invalidList = true;
	}
	
	public void validate() {
		if(invalidList)
			updateIndex();
	}
	
	protected void autoUpdateIndex(UIContainer container) {
		for(UIElement c : container.children) {
			if(c instanceof KeyInputHandler)
				list.add((KeyInputHandler) c);
			else if(c instanceof UIContainer)
				autoUpdateIndex((UIContainer) c);
		}
	}
	
	protected void finishUpdateIndex() {
		int index = findIndex(uiFocused);
		if(index>=0)
			lastSelectedIndex = index;
		else
			resetFocus();
		invalidList = false;
	}
	
	public void updateIndex() {
		list.clear();
		if(stickyFocus!=null)
			list.add(stickyFocus);
		else
			autoUpdateIndex(base);
		finishUpdateIndex();
	}
	
	protected int findIndex(KeyInputHandler tab) {
		if(tab==null)
			return -1;
		else
			return list.indexOf(tab);
	}
	
	public KeyInputHandler selectTab(int index, int d) {
		int num = list.size();
		for(int i=0; i<num; i++) {
			index = (index>=0) ? (index+num+d) % num : lastSelectedIndex;
			KeyInputHandler tab = list.get(index);
			UIElement e = tab.asElement();
			if(tab.isEnabled() && e.isVisible() && e.isParentVisible()) {
				lastSelectedIndex = index;
				return tab;
			}
		}
		return null;
	}
	
	public KeyInputHandler selectTab(int d) {
		KeyInputHandler tab = null;
		if(!list.isEmpty()) {
			int index = findIndex(uiFocused);
			tab = selectTab(index, d);
		}
		return tab;
	}
	
	public KeyInputHandler next() {
		KeyInputHandler tab = selectTab(1);
		setFocus(tab);
		return tab;
	}

	public KeyInputHandler prev() {
		KeyInputHandler tab = selectTab(-1);
		setFocus(tab);
		return tab;
	}
	
	public void resetFocus() {
		if(uiFocused!=null && uiFocused!=stickyFocus)
			uiFocused.asElement().onFocusLost();
		if(stickyFocus==null)
			uiFocused = null;
		else
			setFocus(stickyFocus);
	}

	public void setFocus(KeyInputHandler handler) {
		if(stickyFocus!=null)
			handler = stickyFocus;
		else if(handler!=null && handler.asElement()==null)
			throw new InvalidParameterException();
		
		if(uiFocused!=null && uiFocused!=handler)
			resetFocus();
		uiFocused = handler;
		int index = findIndex(uiFocused);
		if(index>=0)
			lastSelectedIndex = index;
		if(uiFocused!=null)
			uiFocused.asElement().onFocusGained();
	}
	
	public boolean hasFocus() {
		return uiFocused!=null;
	}
	
	public KeyInputHandler getFocus() {
		return uiFocused;
	}
	
	public boolean onKeyPressed(char c, int code, int modifiers) {
		switch(code) {
			case KeyEvent.VK_TAB:
				base.setFocus(selectTab(getDir(modifiers)));
				return true;
			default:
				return false;
		}
	}

	public static int getDir(int modifiers) {
		return (modifiers&UIElement.modShiftMask)>0 ? -1 : 1;
	}
	
}
