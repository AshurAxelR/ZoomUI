package com.xrbpowered.zoomui.base;

import java.util.Arrays;
import java.util.List;

import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;

public abstract class UIListBoxBase<T extends UIElement> extends UIScrollContainerBase {

	protected UIElement[] listItems;
	
	private int selectedIndex = -1;

	public UIListBoxBase(UIContainer parent, List<?> objects) {
		super(parent);
		setItems(objects);
	}

	public UIListBoxBase(UIContainer parent, Object[] objects) {
		this(parent, Arrays.asList(objects));
	}

	public UIListBoxBase(UIContainer parent) {
		this(parent, (List<?>) null);
	}

	public void setItems(List<?> objects) {
		getView().removeAllChildren();
		if(objects==null) {
			listItems = new UIElement[0];
		}
		else {
			listItems = new UIElement[objects.size()];
			int i = 0;
			for(Object obj : objects) {
				listItems[i] = createItem(i, obj);
				i++;
			}
		}
		deselect();
	}

	public void setItems(Object[] objects) {
		setItems(objects==null ? null : Arrays.asList(objects));
	}

	protected abstract T createItem(int index, Object object);
	
	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	@SuppressWarnings("unchecked")
	public T getSelectedItem() {
		return selectedIndex<0 ? null : (T)listItems[selectedIndex];
	}
	
	public void deselect() {
		this.selectedIndex = -1;
		onNothingSelected();
	}
	
	@SuppressWarnings("unchecked")
	public void select(int index) {
		if(index>=0 && index<listItems.length) {
			this.selectedIndex = index;
			onItemSelected((T)listItems[index]);
		}
	}
	
	public int getNumItems() {
		return listItems.length;
	}
	
	@SuppressWarnings("unchecked")
	public T getItem(int index) {
		return (T)listItems[index];
	}
	
	public void onItemSelected(T item) {
	}

	public void onNothingSelected() {
	}

	public void onClickSelected() {
	}
	
	@Override
	protected float layoutView() {
		float w = getView().getWidth();
		float y = 0;
		for(int i=0; i<listItems.length; i++) {
			listItems[i].setPosition(0, y);
			float h = listItems[i].getHeight();
			listItems[i].setSize(w, h);
			y += h;
		}
		return y;
	}
	
}
