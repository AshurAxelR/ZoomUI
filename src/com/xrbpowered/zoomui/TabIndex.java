package com.xrbpowered.zoomui;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class TabIndex {

	public final BaseContainer base;
	
	// FIXME may violate consistency, should use base.children?
	protected ArrayList<KeyInputHandler> list = new ArrayList<>();
	protected int lastSelectedIndex = 0;
	
	public TabIndex(BaseContainer base) {
		this.base = base;
	}
	
	public void add(KeyInputHandler tab) {
		if(tab!=null && tab.asElement()!=null)
			list.add(tab);
	}
	
	protected int findIndex(KeyInputHandler tab) {
		if(tab==null)
			return -1;
		else
			return list.indexOf(tab);
	}
	
	public void updateLastSelected(KeyInputHandler tab) {
		int index = findIndex(tab);
		if(index>=0)
			lastSelectedIndex = index;
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
			int index = findIndex(base.getFocus());
			tab = selectTab(index, d);
		}
		return tab;
	}
	
	public KeyInputHandler next() {
		KeyInputHandler tab = selectTab(1);
		base.setFocus(tab);
		return tab;
	}

	public KeyInputHandler prev() {
		KeyInputHandler tab = selectTab(-1);
		base.setFocus(tab);
		return tab;
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
