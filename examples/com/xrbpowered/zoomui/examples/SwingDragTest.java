package com.xrbpowered.zoomui.examples;

import java.awt.Color;

import com.xrbpowered.zoomui.DragActor;
import com.xrbpowered.zoomui.DragWindowActor;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.MouseInfo;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.UIFormattedLabel;
import com.xrbpowered.zoomui.swing.SwingWindowFactory;

public class SwingDragTest extends UIContainer {

	private DragWindowActor dragActor = new DragWindowActor(this);

	private UIButton btnClose;
	private UIFormattedLabel label;

	public SwingDragTest(UIContainer parent) {
		super(parent);
		
		btnClose = new UIButton(this, "Close") {
			@Override
			public void onAction() {
				getRoot().getWindow().close();
			}
		};
		
		label = new UIFormattedLabel(this, "You can <b>drag</b> this window using <span style=\"color:#777777\">Left Mouse Button</span>.");
	}

	@Override
	public void layout() {
		btnClose.setPosition((getWidth()-btnClose.getWidth())/2f, getHeight()-btnClose.getHeight()-16);
		label.setPosition(16, 32);
		label.setSize(getWidth()-32, 0);
	}
	
	@Override
	public void paintBackground(GraphAssist g) {
		g.pixelBorder(this, 2, Color.WHITE, Color.BLACK);
	}

	@Override
	public DragActor acceptDrag(float x, float y, MouseInfo mouse) {
		if(dragActor.notifyMouseDown(x, y, mouse))
			return dragActor;
		else
			return null;
	}

	@Override
	public boolean onMouseDown(float x, float y, MouseInfo mouse) {
		return isInside(x, y) && dragActor.isTrigger(mouse);
	}
	
	public static void main(String[] args) {
		UIWindow frame = SwingWindowFactory.use().createUndecorated(200, 200);
		new SwingDragTest(frame.getContainer());
		frame.show();
	}

}
