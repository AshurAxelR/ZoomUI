package com.xrbpowered.zoomui;

import java.awt.event.KeyEvent;
import java.util.HashMap;

public class HotKeyMap implements KeyInputHandler {
	
	private HashMap<Integer, ActionHandler> map = new HashMap<>();

	private static int keyHash(int code, int mods) {
		return (code<<3) | (mods & 0x07);
	}
	
	public HotKeyMap addOk(ActionHandler action) {
		return add(KeyEvent.VK_ENTER, 0, action);
	}

	public HotKeyMap addCancel(ActionHandler action) {
		return add(KeyEvent.VK_ESCAPE, 0, action);
	}

	public HotKeyMap add(int code, int mods, ActionHandler action) {
		int key = keyHash(code, mods);
		if(action==null)
			map.remove(key);
		else
			map.put(key, action);
		return this;
	}

	public HotKeyMap addAll(HotKeyMap other) {
		map.putAll(other.map);
		return this;
	}
	
	public ActionHandler remove(int code, int mods) {
		return map.remove(keyHash(code, mods));
	}

	public void removeAll() {
		map.clear();
	}

	@Override
	public boolean onKeyPressed(char c, int code, InputInfo input) {
		ActionHandler action = map.get(keyHash(code, input.mods));
		if(action!=null) {
			if(action.isEnabled())
				action.onAction();
			return true;
		}
		else
			return false;
	}
}
