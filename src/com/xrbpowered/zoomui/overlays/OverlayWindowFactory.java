package com.xrbpowered.zoomui.overlays;

import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIModalWindow;
import com.xrbpowered.zoomui.UIModalWindow.ResultHandler;
import com.xrbpowered.zoomui.UIPopupWindow;
import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.UIWindowFactory;

public class OverlayWindowFactory extends UIWindowFactory {

	public final UIContainer host;
	
	public OverlayWindowFactory(UIContainer host) {
		this.host = host;
		setBaseScale(1f);
	}

	@Override
	public float globalPixelScale() {
		return host.getPixelScale();
	}
	
	@Override
	public UIWindow create(String title, int w, int h, boolean canResize) {
		return new OverlayWindow<>(this, w, h, false);
	}

	@Override
	public <A> UIModalWindow<A> createModal(String title, int w, int h, boolean canResize, ResultHandler<A> onResult) {
		OverlayWindow<A> dlg = new OverlayWindow<>(this, w, h, true);
		dlg.onResult = onResult;
		return dlg;
	}

	@Override
	public UIPopupWindow createPopup() {
		throw new UnsupportedOperationException();
		// TODO create overlay popup
		// return new OverlayWindow<>(this, 0, 0, true);
	}

	@Override
	public UIWindow createUndecorated(int w, int h) {
		return new OverlayWindow<>(this, w, h, false);
	}
	
	public OverlayWindowFactory use() {
		UIWindowFactory.instance = this;
		return this;
	}

}
