package edu.asu.jmars.lmanager;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import edu.asu.jmars.Main;
import edu.asu.jmars.layer.LViewFactory;
import edu.asu.jmars.layer.LayerParameters;
import edu.asu.jmars.layer.map2.custom.CustomMapBackendInterface;
import edu.asu.jmars.swing.IconButtonUI;
import edu.asu.jmars.ui.image.factory.ImageCatalogItem;
import edu.asu.jmars.ui.image.factory.ImageFactory;
import edu.asu.jmars.ui.looknfeel.GUITheme;
import edu.asu.jmars.ui.looknfeel.theme.component.ThemeImages;
import edu.asu.jmars.ui.looknfeel.theme.component.ThemePanel;
import edu.asu.jmars.ui.looknfeel.theme.component.ThemeText;


public class SearchResultRow extends JButton {
    public static final int LAYER_NAME_PANEL_WIDTH = 415;
    private ArrayList<LayerParameters> layerList;
    private LayerParameters layer = null;
    private static Color imgIconColor = ((ThemeImages) GUITheme.get("images")).getSelectedfill();
    private static Color imgColor = ((ThemeImages) GUITheme.get("images")).getFill();    
    private static Color border = ((ThemePanel) GUITheme.get("panel")).getBackgroundhi();
    private static Color disabledtext = ((ThemeText) GUITheme.get("text")).getTextDisabled();
    private static HashMap<String,Icon> typeIcons = new HashMap<String, Icon>(); 
    private boolean builtFlag = false;
    private boolean favoriteFlag = false;
    private FavoriteButton favoriteButton = null;
    private boolean isCustom = false;     
    private JLabel layerNameLbl = new JLabel();
    private static Icon plusIcon = new ImageIcon(ImageFactory.createImage(ImageCatalogItem.ADD_LAYER.withStrokeColor(imgColor)));           
    private static Icon tooltipIcon = new ImageIcon(ImageFactory.createImage(ImageCatalogItem.INFO));            
    private static Icon favoriteIcon = new ImageIcon(ImageFactory.createImage(ImageCatalogItem.FAVORITE.withStrokeColor(imgColor)));           
    private static Icon favoriteSelectedIcon = new ImageIcon(ImageFactory.createImage(ImageCatalogItem.FAVORITED.withDisplayColor(imgIconColor)));
    private static Icon favoriteDisabledIcon = new ImageIcon(ImageFactory.createImage(ImageCatalogItem.FAVORITE.withStrokeColor(disabledtext)));    
    
    
    public SearchResultRow(LayerParameters layer, boolean favorite) {
        if (this.layerList == null) {
            this.layerList = new ArrayList<LayerParameters>();
            this.layerList.add(layer);
        }
        this.layer = layer;
        this.favoriteFlag = favorite;        
        addActionListener(new AddLayerActionListener(SearchResultRow.this.layer, SearchResultRow.this.isCustom));        
        setUI(new IconButtonUI());
    }
    
    
    public SearchResultRow(ArrayList<LayerParameters> layerData, boolean favorite) {
        this(layerData.get(0),favorite);
        this.layerList = layerData;      
    }
    
    public String getTopic(String cat, String subcat) {
        if (this.isCustom) {
            return "Custom";
        }
        for (LayerParameters lp : layerList) {
            if (lp.category.equals(cat)) {
                if (subcat != null) {
                    if (subcat.equals(lp.subcategory)) {
                        return lp.topic;
                    }
                } else {
                    if (lp.subcategory == null || lp.subcategory.trim().length() == 0) {
                        return lp.topic;
                    }
                }
            }
        }
        return this.layer.topic;
    }
    public String getLayerId() {
        return this.layer.id;
    }

    public boolean isFavoriteIconOn() {
        return this.favoriteFlag;
    }
    public void toggleFavoriteIcon(boolean on) {
        if (!this.builtFlag) {
            this.buildRow();
        }
        if (on) {
            this.favoriteButton.changed(1);
        } else {
            this.favoriteButton.changed(0);
        }
        this.favoriteFlag = on;
    }
    public void buildRow() {
        if (builtFlag) {
            return;
        }
        builtFlag = true;
        
        JLabel addLayerImg = new JLabel();
        addLayerImg.setIcon(plusIcon);        
        addLayerImg.setOpaque(false);                 
        
        if (this.favoriteFlag) {
            favoriteButton = new FavoriteButton(1,favoriteIcon, favoriteSelectedIcon);
        } else {
            favoriteButton = new FavoriteButton(0,favoriteIcon, favoriteSelectedIcon);
        }
        
        if(Main.USER == null || Main.USER.trim().length() == 0) {
            favoriteButton.setEnabled(false);
            favoriteButton.setToolTipText("Login to have access to favorites");
            
            if(layer.type.equalsIgnoreCase("upload_map") || layer.type.contains("roi")
                    || layer.type.contains("plan") || layer.type.equalsIgnoreCase("tes") 
                    || layer.name.equalsIgnoreCase("custom stamps" )) {
            	layerNameLbl.setEnabled(false);
                this.setEnabled(false);
            }
        } else {
            //valid user in here
            if ((layer.type.equalsIgnoreCase("stamp") && layer.layergroup.equalsIgnoreCase("dawn_team"))) {
                if (Main.AUTH_DOMAIN.equalsIgnoreCase("dawn")) {
                    layerNameLbl.setEnabled(true);   
                    this.setEnabled(true);
                } else {
                      layerNameLbl.setEnabled(false);
                      this.setEnabled(false);
                }
            }
                
        }     
        
        Icon typeIcon = getImageIcon(layer.type);        
        
        JLabel typeLbl = new JLabel(typeIcon);
        typeLbl.setOpaque(false);
        
        JLabel layerTooltip = new JLabel(tooltipIcon);
        layerTooltip.setOpaque(false);
        
        String tooltipText = "<html>";
        String layerDesc = "Layer Description: <br />";
        if (layer.description == null || layer.description.trim().length() == 0) {
            layerDesc += "none";
        } else {
            layerDesc += layer.description;
        }
        if (layerDesc.length() > 500) {
            layerDesc = layerDesc.substring(0,500) + "...";
        }
        tooltipText += "<p width=\"400px\">"+layerDesc+"</p><p width=\"400px\">";
        HashSet<String> uniqueSet = new HashSet<String>();
        for (LayerParameters lp : this.layerList) {
            String categoryText = (lp.category != null ? lp.category : "");
            String subcatText = (lp.subcategory != null ? lp.subcategory : "");
            String topicText = (lp.topic != null ? lp.topic : "");
            String firstArrow = (!"".equals(categoryText) && (!"".equals(subcatText) || !"".equals(topicText)) ? " -> " : "");
            String secondArrow = ((!"".equals(categoryText) && !"".equals(subcatText)) && !"".equals(topicText) ? " -> " : "");
            String hierarchyText = categoryText+firstArrow+subcatText+secondArrow+topicText;
            uniqueSet.add(hierarchyText);
        }
        tooltipText += "<br /><br />Categorization:<br />";
        for (String hierarchy : uniqueSet) {
            tooltipText += hierarchy+"<br/>";
        }
        tooltipText += "</p></html>";
        layerTooltip.setToolTipText(tooltipText);        
       
        layerNameLbl.setOpaque(false);
        layerNameLbl.setText(this.layer.name);        

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGap(10,10,10)
            .addComponent(addLayerImg)
            .addGap(10,10,10)
            .addComponent(typeLbl,25,25,25)
            .addGap(10,10,10)
            .addComponent(layerTooltip)
            .addGap(10,10,10)
            .addComponent(layerNameLbl,LAYER_NAME_PANEL_WIDTH,LAYER_NAME_PANEL_WIDTH,LAYER_NAME_PANEL_WIDTH)
            .addComponent(favoriteButton)
        );
        
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(addLayerImg)
            .addComponent(typeLbl)
            .addComponent(layerTooltip)
            .addComponent(layerNameLbl)
            .addComponent(favoriteButton)
        );
        this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, border));
    }
    
    class FavoriteButton extends JButton {
        private int myMode;
        private Icon img;
        private Icon selImg;        
        
        FavoriteButton (int tmode, Icon normal, Icon selected)
        {
            super(normal);
            img = normal;
            selImg = selected;
            myMode = tmode;
            if (myMode == 1) {
                changed(1);
            }
            
            setFocusPainted(false);
            setDisabledIcon(favoriteDisabledIcon);
            
			addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (Main.USER != null && Main.USER.trim().length() > 0) {
						SearchProvider sp = SearchProvider.getInstance();
						String name = SearchResultRow.this.layer.name;
						if (favoriteFlag) {
							String delLayerId = SearchResultRow.this.layer.id;
							if (SearchResultRow.this.isCustom) {
								delLayerId = sp.getCustomMapId(name);
							}
							CustomMapBackendInterface.deleteFavorite(delLayerId, SearchResultRow.this.isCustom);
							sp.deleteFavorite(SearchResultRow.this.layer.id);
							favoriteFlag = false;
						} else {
							if (SearchResultRow.this.isCustom()) {
								String id = sp.getCustomMapId(name);
								CustomMapBackendInterface.addCustomFavorite(id);
								sp.addFavorite("cm_" + id);
							} else {
								CustomMapBackendInterface.addFavorite(SearchResultRow.this.layer.id);
								sp.addFavorite(SearchResultRow.this.layer.id);
							}
							favoriteFlag = true;
						}
					}
				}
			});
			addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
					if (myMode == 1) {
						changed(0);
					} else {
						changed(1);
					}
				}

				@Override
				public void mouseExited(MouseEvent e) {	
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseClicked(MouseEvent e) {
				}
			});
			setUI(new IconButtonUI());
		}

        public void changed (int newMode)
        {
        	if (!isEnabled())
        		return;
        	
            if (newMode == 1){
                setIcon(selImg);
            }
            else{
                setIcon(img);
            }
            myMode = newMode;
       }  
    }    
  
       
	
    public void setIsCustom(boolean flag) {
        this.isCustom = flag;
    }
    public boolean isCustom() {
        return this.isCustom;
    }
    private Icon getImageIcon(String type) {
        Icon typeIcon = typeIcons.get(type);
        if (typeIcon == null) {
            LViewFactory factoryObject = LViewFactory.findFactoryType(type);
            
            if (factoryObject != null) {
                typeIcon = factoryObject.getLayerIcon();
            }
            if (typeIcon == null) {        
                typeIcon = LViewFactory.getDefaultLayerIcon();
            }
            typeIcons.put(type,typeIcon);
        }
        return typeIcon;
    }
    
  
}
