package edu.asu.jmars.swing;

import edu.asu.jmars.layer.Layer.LView;
import edu.asu.jmars.layer.Layer.LView3D;
import edu.asu.jmars.ui.image.factory.ImageCatalogItem;


public class Hidden3DToggle extends HiddenToggleButton {
	
	private LView3D view3D;

	public Hidden3DToggle(LView view, ImageCatalogItem threedEnabledImg, ImageCatalogItem threedDisabledImg) {
		super(view, threedEnabledImg, threedDisabledImg);
		view3D = view.getLView3D();	
		if(!view3D.exists()){			
			setState(ButtonState.DISABLED);
		}
	}

	public LView3D getView3D()
	{
		return view3D;
	}
	
    @Override
	public void initState() {
		LView3D view3D;		
		if (this.rowview.isPresent())
		{
			view3D = this.getView().get().getLView3D();
			if(!view3D.exists())
			{		   
			    setState(ButtonState.DISABLED);
			    return;
			}
			if (view3D.isVisible()) setState(ButtonState.ON);
	        else setState(ButtonState.OFF);			
	    }	
	}   
		
	@Override
	public void toggle()
	{
		LView3D view;
		if (state == ButtonState.DISABLED) return;
		if (!isEnabled()) return;
		if (this.rowview.isPresent())
		{
			toggleState();
			view = this.getView().get().getLView3D();			
			if (getState() == ButtonState.ON)
				view.setVisible(true);
			else if (getState() == ButtonState.OFF)
				view.setVisible(false);
		}
	}
	
	@Override
	public void toggleState()
	{		
		if (state == ButtonState.DISABLED) return;
		ButtonState newstate = state.toggleState(); 
		setState(newstate);
	}	

}
