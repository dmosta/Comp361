package util;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

/**
 * Select box of tuples that displays the text in the first element of tuples,
 * and returns the value of the second elements of tuples.
 */
public class TupleSelectBox <S, T> extends SelectBox {
	private ArrayList<T> values;
	
	public TupleSelectBox(Skin skin){
		super(skin);
	}
	
	public void setValues(T[] vals){
		values.clear();
		for(T t:vals)
			values.add(t);
	}
	
	public void setValues(ArrayList<T> vals){
		this.values=vals;
	}
	
	@Override
	public Object getSelected() {
		int index=getSelectedIndex();
		if(index<values.size())
			return values.get(index);
		else return null;
	}
}
