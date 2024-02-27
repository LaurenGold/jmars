package edu.asu.jmars.lmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import edu.asu.jmars.Main;
import edu.asu.jmars.layer.LManager;
import edu.asu.jmars.layer.LayerParameters;
import edu.asu.jmars.layer.map2.custom.CustomMapBackendInterface;

public class SearchProvider {

    public static final int TAG_NAME = 0;
    public static final int TAG_DESC = 1;
    public static final int TAG_CITATION = 2;
    public static final int TAG_LINKS = 3;
    public static final int TAG_ALL = 4;
    
    private ArrayList<String> searchOptions = new ArrayList<String>();
    private ArrayList<LayerParameters> searchParams = new ArrayList<LayerParameters>();
    private HashMap<String, SearchResultRow> searchRowsById = new HashMap<String, SearchResultRow>();
    private HashMap<String, SearchResultRow> browseRowsById = new HashMap<String, SearchResultRow>();
    private ArrayList<SearchResultRow> favoriteRows = new ArrayList<SearchResultRow>();//used for favorites tab
    private HashMap<String, ArrayList<LayerParameters>> uniqueRows = new HashMap<String,ArrayList<LayerParameters>>();
    private HashMap<String, ArrayList<String>> idsByPath = new HashMap<String, ArrayList<String>>();
    private ArrayList<String> categories = new ArrayList<String>();
    private HashMap<String, ArrayList<String>> subcatsByCat = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<String>> topicsBySubcat = new HashMap<String, ArrayList<String>>();
    private ArrayList<String> favorites = new ArrayList<String>();//list of ids used for search and browse
    private ArrayList<String> customMapFavorites = new ArrayList<String>();
    private HashMap<String,String> customMapNameIdMap = new HashMap<String,String>();
    private HashMap<String,String> layerNameIdMap = new HashMap<String,String>();//for searching on name
    
    
    private static SearchProvider instance = null;
    
    
    public static SearchProvider getInstance() {
        if (instance == null) {
           instance = new SearchProvider(); 
        }
        return instance;
    }
    private SearchProvider() {
        populateSearchOptions();
    }
    private void loadLayers() {
        searchParams.addAll(LayerParameters.lParameters);
        for (LayerParameters layer : searchParams) {
            ArrayList<LayerParameters> list = uniqueRows.get(layer.id);
            if (list == null) {
                list = new ArrayList<LayerParameters>();
                uniqueRows.put(layer.id,list);
            }
            list.add(layer);
            layerNameIdMap.put(layer.name, layer.id);//for searching on name
            
            //build a full hierarchy to prevent having to loop after this
            LayerParameters lp = layer;
            String cat = lp.category;
            if (cat != null && cat.trim().length() > 0) {
                String subcat = (lp.subcategory == null || lp.subcategory.trim().length() == 0 ? "nosubcat" : lp.subcategory);
                String topic = lp.topic;
                if (!categories.contains(cat)) {
                    categories.add(cat);
                }
                ArrayList<String> subcats = subcatsByCat.get(cat);
                if (subcats == null) {
                    subcats = new ArrayList<String>();
                    subcatsByCat.put(cat,subcats);
                }
                if (!subcats.contains(subcat) && !"nosubcat".equals(subcat)) {
                    subcats.add(subcat);
                }
                String catSubcat = cat + "-" + subcat;
                ArrayList<String> topics = topicsBySubcat.get(catSubcat);
                if (topics == null) {
                    topics = new ArrayList<String>();
                    topicsBySubcat.put(catSubcat,topics);
                }
                if (!topics.contains(topic)) {
                    topics.add(topic);
                }
                String fullPath =  catSubcat + "-" + topic;
                ArrayList<String> ids = idsByPath.get(fullPath);
                if (ids == null) {
                    ids = new ArrayList<String>();
                    idsByPath.put(fullPath,ids);
                }
                if (lp.id != null && !ids.contains(lp.id)) {//custom map ids are null and will be handled elsewhere
                    ids.add(lp.id);
                }
            }
        }
        
        for (ArrayList<LayerParameters> list : uniqueRows.values()) {
            String id = list.get(0).id;
            boolean favorite = false;

            if (favorites != null && favorites.contains(id)) {
                favorite = true;
            }
            
            //build SearchResultRows to prevent building them later - need one per tab
            SearchResultRow sRow = new SearchResultRow(list,favorite);
            SearchResultRow bRow = new SearchResultRow(list,favorite);
            this.searchRowsById.put(id, sRow);
            this.browseRowsById.put(id, bRow);
        }
        
        if(userLoggedIn()){
            searchParams.addAll(LayerParameters.customlParameters);
            ArrayList<String> customIds = new ArrayList<String>();
            for (LayerParameters customLayer : LayerParameters.customlParameters) {
                String id = null;
                if (customMapNameIdMap != null) {
                    id = customMapNameIdMap.get(customLayer.name);
                }
                id = "cm_"+id;
                    
                boolean favorite = false;
                if (customMapFavorites != null && customMapFavorites.contains(id)) {
                    favorite = true;
                }

                SearchResultRow sRow = new SearchResultRow(customLayer, favorite);
                SearchResultRow bRow = new SearchResultRow(customLayer, favorite);
                sRow.setIsCustom(true);
                bRow.setIsCustom(true);
                
                customIds.add(id);
                this.searchRowsById.put(id,sRow);
                this.browseRowsById.put(id,bRow);
                customLayer.setCustomId(id);
                ArrayList<LayerParameters> customRow = new ArrayList<LayerParameters>();
                customRow.add(customLayer);
                this.uniqueRows.put(id, customRow);
                
            }
            categories.add("Custom");
            subcatsByCat.put("Custom",new ArrayList<String>());
            ArrayList<String> cmTopics = new ArrayList<String>();
            cmTopics.add("Custom");
            topicsBySubcat.put("Custom-nosubcat",cmTopics);
            this.idsByPath.put("Custom-nosubcat-Custom",customIds);
        }
    }
    public static void prepareSearch() {
        Runnable run = new Runnable() {
            
            @Override
            public void run() {
                while (!LayerParameters.isInitializationComplete() || LayerParameters.lParameters.size() < 1) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                SearchProvider sp = getInstance();
                try {
                    sp.reset();
                    if(userLoggedIn()){
                        sp.loadCustomMaps();
                        sp.loadFavoriteIds();
                    }
                    sp.loadLayers();
                    if(userLoggedIn()){
                        sp.loadFavoriteLayers();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        Thread t = new Thread(run);
        t.start();
        
    }
    public static boolean userLoggedIn() {
        if (Main.USER!=null && Main.USER.length()>0) {
            return true;
        }
        return false;
    }
    private void reset() {
        searchParams = new ArrayList<LayerParameters>();
        searchRowsById = new HashMap<String, SearchResultRow>();
        browseRowsById = new HashMap<String, SearchResultRow>();
        uniqueRows = new HashMap<String,ArrayList<LayerParameters>>();
        idsByPath = new HashMap<String, ArrayList<String>>();
        categories = new ArrayList<String>();
        subcatsByCat = new HashMap<String, ArrayList<String>>();
        topicsBySubcat = new HashMap<String, ArrayList<String>>();
        layerNameIdMap = new HashMap<String, String>();
        this.favoriteRows.clear();
        favorites.clear();
        customMapFavorites.clear();
        
    }

    public String getCustomMapId(String mapName) {
        return this.customMapNameIdMap.get(mapName);
    }
    private void loadCustomMaps() {
        customMapNameIdMap = CustomMapBackendInterface.getExistingNameIdMap();
    }
    private void loadFavoriteIds() {
        HashMap<String, ArrayList<String>> loadFavorites = CustomMapBackendInterface.loadFavorites();
        favorites.addAll(loadFavorites.get("layers"));
        customMapFavorites.addAll(loadFavorites.get("custom"));
    }
    public void addFavorite(String layerId) {
        if (layerId.startsWith("cm_")) {
            if (!customMapFavorites.contains(layerId)) {
                customMapFavorites.add(layerId);
                SearchResultRow faveRow = new SearchResultRow(uniqueRows.get(layerId), true);
                faveRow.setIsCustom(true);
                faveRow.buildRow();
                this.favoriteRows.add(faveRow);
            }
        } else {
            //add to the list of favorite ids and add a new row to the favoriteRows for the favorite tab
            if (!favorites.contains(layerId)) {
                favorites.add(layerId);
                SearchResultRow faveRow = new SearchResultRow(uniqueRows.get(layerId), true);
                faveRow.buildRow();
                this.favoriteRows.add(faveRow);
            }
        }
        
        //need to change the icon for the proper rows on browse and search tabs
        toggleFaveIconForRow(layerId, true);
    }
    
    public void deleteFavorite(String layerId) {
        if (layerId.startsWith("cm_")) {
            ArrayList<SearchResultRow> rowsForDelete = new ArrayList<SearchResultRow>();
            if (customMapFavorites.contains(layerId)) {
                customMapFavorites.remove(layerId);
                for (SearchResultRow row : this.favoriteRows) {
                    if (row.getLayerId().equals(layerId)) {
                        rowsForDelete.add(row);
                    }
                }
                this.favoriteRows.removeAll(rowsForDelete);
            }
        } else {
            //remove from the list of ids for favorites, and from the favoriteRows (rows displayed on the favorites tab)
            ArrayList<SearchResultRow> rowsForDelete = new ArrayList<SearchResultRow>();
            if (favorites.contains(layerId)) {
                favorites.remove(layerId);
                for (SearchResultRow row : this.favoriteRows) {
                    if (row.getLayerId().equals(layerId)) {
                        rowsForDelete.add(row);
                    }
                }
                this.favoriteRows.removeAll(rowsForDelete);
            }
        }
        
        //Need to change the icon for the proper rows on all tabs
        toggleFaveIconForRow(layerId,false);
    }
    private void toggleFaveIconForRow(String layerId, boolean on) {            
        for(SearchResultRow row : this.favoriteRows) {
            if (row.getLayerId().equals(layerId)) {
                if ((row.isFavoriteIconOn() && !on) || (!row.isFavoriteIconOn() && on)) {
                    row.toggleFavoriteIcon(on);//only toggle it if it needs to be (need to update 2 other tabs but not the same one twice)
                }
            }
        }
        
        SearchResultRow bRow = this.browseRowsById.get(layerId);
        SearchResultRow sRow = this.searchRowsById.get(layerId);
        
        if ((bRow.isFavoriteIconOn() && !on) || (!bRow.isFavoriteIconOn() && on)) {
            bRow.toggleFavoriteIcon(on);//only toggle it if it needs to be (need to update 2 other tabs but not the same one twice)
        }
        if ((sRow.isFavoriteIconOn() && !on) || (!sRow.isFavoriteIconOn() && on)) {
            sRow.toggleFavoriteIcon(on);//only toggle it if it needs to be (need to update 2 other tabs but not the same one twice)
        }
    }
    private void loadFavoriteLayers() {
        for(String faveId : this.favorites) {
            if (this.uniqueRows.containsKey(faveId)) {//faves currently are not by body
                ArrayList<LayerParameters> list = this.uniqueRows.get(faveId);
                SearchResultRow fRow = new SearchResultRow(list,true);
                fRow.buildRow();
                this.favoriteRows.add(fRow);
            }
        }
        
        for(String faveId : this.customMapFavorites) {
            if (this.uniqueRows.containsKey(faveId)) {//faves currently are not by body
                ArrayList<LayerParameters> list = this.uniqueRows.get(faveId);
                SearchResultRow fRow = new SearchResultRow(list,true);
                fRow.setIsCustom(true);
                fRow.buildRow();
                this.favoriteRows.add(fRow);
            }
        }
        
    }
    public ArrayList<SearchResultRow> getFavoriteLayers() {
        return this.favoriteRows;
    }
    private SearchResultRow buildRow(String id, boolean searchFlag) {
        HashMap<String, SearchResultRow> rowMap = null;
        if (searchFlag) {
            rowMap = this.searchRowsById;
        } else {
            rowMap = this.browseRowsById;
        }
        SearchResultRow row = rowMap.get(id);
        row.buildRow();
        return row;
    }

    public ArrayList<SearchResultRow> buildInitialLayerList(boolean search) {
        ArrayList<SearchResultRow> resultSet = new ArrayList<SearchResultRow>();
        String catSubcat = "Home-nosubcat";
        ArrayList<String> topics = topicsBySubcat.get(catSubcat);
        if (topics != null) {
            for (String topic : topics) { 
                String path = catSubcat + "-" + topic;
                ArrayList<String> ids = idsByPath.get(path);
                for (String id : ids) {
                    resultSet.add(buildRow(id,search));
                }
            }
        }

        return resultSet;
    }
    public ArrayList<SearchResultRow> getLayersByTopic(String category, String subcategory) {
        ArrayList<SearchResultRow> resultSet = new ArrayList<SearchResultRow>();

        if (category != null) {
            if (subcategory == null || subcategory.trim().length() == 0) {
                subcategory = "nosubcat";
            }
            String catSubcat = category+"-"+subcategory;
            
            ArrayList<String> topics = topicsBySubcat.get(catSubcat);
            if (topics.size() == 0) {
                ArrayList<String> ids = idsByPath.get(catSubcat);
                for (String id : ids) {
                    resultSet.add(buildRow(id,false));
                }
            } else {
                for (String topic : topics) { 
                    String path = catSubcat + "-" + topic;
                    ArrayList<String> ids = idsByPath.get(path);
                    for (String id : ids) {
                        resultSet.add(buildRow(id,false));
                    }
                }
            }
        } else {
            resultSet = this.buildInitialLayerList(false);
        }
        
        return resultSet;
    }
    
    private void populateSearchOptions() {
        searchOptions.add("name:");
        searchOptions.add("instrument:");
        searchOptions.add("imagery:");
        searchOptions.add("category:");
        searchOptions.add("subcategory:");
        searchOptions.add("topic:");
        searchOptions.add("desc:");
        searchOptions.add("citation:");
        searchOptions.add("links:");
        if (userLoggedIn()) {
            searchOptions.add("custom:");
            searchOptions.add("favorite:");
        }
    }
    public ArrayList<String> getSearchOptions() {
    	if (Main.getCurrentBody().equalsIgnoreCase("mars")) {
    		return searchOptions;
    	} else {
    		ArrayList<String> options = new ArrayList<String>();
    		options.addAll(searchOptions);
    		for (ArrayList<String> subcats : subcatsByCat.values()) {
    			if (subcats.size() > 0) {
    				break;
    			}
    			options.remove("subcategory:");
    		}
    		for (ArrayList<String> topics : topicsBySubcat.values()) {
    			if (topics.size() > 0) {
    				break;
    			}
    			options.remove("topic:");
    		}
    		if (!categories.contains("Instrument")) {
    			options.remove("instrument:");
    		}
    		if (!categories.contains("Imagery")) {
    			options.remove("imagery:");
    		}
    		return options;
    	}
    }
    public ArrayList<String> getCategories() {
        return categories;
    }
    public ArrayList<String> getSubcategories(String category) {
        ArrayList<String> list = subcatsByCat.get(category);
        if (list != null) {
            Collections.sort(list);
        }
        return list;
    }
    
    public ArrayList<String> getSuggestionHome() {
        ArrayList<String> results = new ArrayList<String>();
        String catSubcat = "Home-nosubcat";
        ArrayList<String> topics = topicsBySubcat.get(catSubcat);
        if (topics != null) {
            for (String topic : topics) { 
                String path = catSubcat + "-" + topic;
                ArrayList<String> ids = idsByPath.get(path);
                for (String id : ids) {
                    results.add(" name:  "+this.uniqueRows.get(id).get(0).name);
                }
            }
        }
        return results;
    }
    public ArrayList<String> getSuggestionCategory() {
        ArrayList<String> results = new ArrayList<String>();
        for(String cat : this.categories) {
            results.add(" category:  "+cat);
        }
        return results;
    }
    
    public ArrayList<String> getSuggestionSubcategory() {
        ArrayList<String> results = new ArrayList<String>();
        ArrayList<String> sorted = new ArrayList<String>();
        sorted.addAll(this.categories);
        Collections.sort(sorted);
        for(String cat : sorted) {
            results.add(" category: "+cat);
            ArrayList<String> subcats = this.subcatsByCat.get(cat);
            if (subcats.size() > 0) {
                ArrayList<String> sortedSubs = new ArrayList<String>();
                sortedSubs.addAll(subcats);
                Collections.sort(sortedSubs);
                for (String subcat : sortedSubs) {
                    results.add("    subcategory: " + subcat);
                }
            }
        }
        return results;
    }
    
    public ArrayList<String> getSuggestionTopic() {
        ArrayList<String> results = new ArrayList<String>();
        ArrayList<String> sorted = new ArrayList<String>();
        sorted.addAll(this.categories);
        Collections.sort(sorted);
        for(String cat : sorted) {
            results.add(" category: "+cat);
            ArrayList<String> subcats = this.subcatsByCat.get(cat);
            if (subcats.size() > 0) {
                ArrayList<String> sortedSubs = new ArrayList<String>();
                sortedSubs.addAll(subcats);
                Collections.sort(sortedSubs);
                for (String subcat : sortedSubs) {
                    ArrayList<String> topics = this.topicsBySubcat.get(cat+"-"+subcat);
                    if (topics.size() > 0) {
                        results.add("    subcategory: " + subcat);
                        ArrayList<String> sortedTopics = new ArrayList<String>();
                        sortedTopics.addAll(topics);
                        for (String topic : sortedTopics) {
                            results.add("        topic: "+topic);
                        }
                    } 
                }
            } else {
                ArrayList<String> topics = this.topicsBySubcat.get(cat+"-nosubcat");
                if (topics.size() > 0) {
                    ArrayList<String> sortedTopics = new ArrayList<String>();
                    sortedTopics.addAll(topics);
                    for (String topic : sortedTopics) {
                        results.add("        topic: "+topic);
                    }
                }
            }
        }
        return results;
    }
    public ArrayList<String> getPartialSuggestionCat(String search) {
        ArrayList<String> results = new ArrayList<String>();
        for (String cat : this.categories) {
            if (cat.toLowerCase().startsWith(search.toLowerCase())) {
                results.add(" category: "+cat);
                ArrayList<String> subcats = this.subcatsByCat.get(cat);
                if (subcats.size() > 0) {
                    ArrayList<String> sortedSubs = new ArrayList<String>();
                    sortedSubs.addAll(subcats);
                    Collections.sort(sortedSubs);
                    for (String subcat : sortedSubs) {
                        results.add("    subcategory: " + subcat);
                    }
                }    
            }
        }
        
        return results;
    }
    public ArrayList<String> getSuggestionWithHierarchy(String category, String subcategory, String topicValue, String[] search, int tag) {
        ArrayList<String> dupes = new ArrayList<String>();
        ArrayList<String> results = new ArrayList<String>();
        ArrayList<String> sorted = new ArrayList<String>();
        sorted.addAll(this.categories);
        Collections.sort(sorted);
        for(String cat : sorted) {
            if (category == null || cat.toLowerCase().contains(category.toLowerCase().trim())) {//matches category or no category sent
                boolean catAdded = false;
                ArrayList<String> subcats = this.subcatsByCat.get(cat);
                if (subcats.size() > 0) {
                    ArrayList<String> sortedSubs = new ArrayList<String>();
                    sortedSubs.addAll(subcats);
                    Collections.sort(sortedSubs);
                    for (String subcat : sortedSubs) {
                        if (subcategory == null || subcat.toLowerCase().contains(subcategory.toLowerCase().trim())) {//subcat matches or no subcat sent
                            boolean subcatAdded = false;
                            doTopicSearching(cat+"-"+subcat, topicValue, search, tag, results, dupes, cat, subcat, catAdded, subcatAdded);
                        }//end subcat matches
                    }
                } else {//case of topics not in a subcategory
                    doTopicSearching(cat+"-nosubcat", topicValue, search, tag, results, dupes, cat, null, catAdded, false);
                }
            }//end matches category
        }
        return results;
    }
    private void doTopicSearching(String subcatPath, String topicValue, String[] search, int tag, 
            ArrayList<String> results, ArrayList<String> dupes, String cat, String subcat, boolean catAdded, boolean subcatAdded) {
        ArrayList<String> topics = this.topicsBySubcat.get(subcatPath);
        if (topics.size() > 0) {
            ArrayList<String> sortedTopics = new ArrayList<String>();
            sortedTopics.addAll(topics);
            for (String topic : sortedTopics) {
                if (topicValue == null || topic.toLowerCase().contains(topicValue.toLowerCase().trim())) {//topic matches or no topic sent
                    boolean topicAdded = false;
                    ArrayList<String> layerIds = this.idsByPath.get(subcatPath+"-"+topic);
                    for (String id : layerIds) {
                        ArrayList<LayerParameters> lps = this.uniqueRows.get(id);
                        for (LayerParameters lp : lps) {
                            boolean allFound = true;
                            boolean atLeastOneFound = false;
                            String suffix = "";
                            for (String val : search) {
                                val = val.trim();
                                String attr = null;
                                switch(tag) {
                                    case TAG_NAME:
                                        attr = lp.name;
                                        break;
                                    case TAG_DESC:
                                        attr = lp.description;
                                        suffix = " <description match>";
                                        break;
                                    case TAG_CITATION:
                                        attr = lp.citation;
                                        suffix = " <citation match>";
                                        break;    
                                    case TAG_LINKS:
                                        attr = lp.links;
                                        suffix = " <links match>";
                                        break;
                                    default : 
                                        throw new IllegalArgumentException("Invalid tag in getSuggestionWithHierarchy");
                                }
                                if (!attr.toLowerCase().contains(val.toLowerCase())) {
                                    allFound = false;
                                    break;
                                } else if (val.length() > 0){
                                    atLeastOneFound = true;
                                }
                            }
                            if (allFound) {
                                if (!catAdded) {
                                    results.add(" category: "+cat);
                                    catAdded = true;
                                }
                                if (subcat != null) {
                                    if (!subcatAdded) {
                                        results.add("    subcategory: " + subcat);
                                        subcatAdded = true;
                                    }
                                }
                                if (!topicAdded) {
                                    results.add("        topic: "+topic);
                                    topicAdded = true;
                                }
                                if (!dupes.contains(lp.name)) {
                                    results.add("            name: "+lp.name + (atLeastOneFound ? suffix : ""));
                                    dupes.add(lp.name);
                                }
                            }
                        }
                    }
                }//end topic matches
            }
        }
        
    }
    public ArrayList<String> getPartialSuggestionTopic(String category, String subcategory, String search) {
        ArrayList<String> results = new ArrayList<String>();
        ArrayList<String> sorted = new ArrayList<String>();
        sorted.addAll(this.categories);
        Collections.sort(sorted);
        for(String cat : sorted) {
            if (category == null || cat.toLowerCase().contains(category.toLowerCase().trim())) {//matches category
                boolean catAdded = false;
                ArrayList<String> subcats = this.subcatsByCat.get(cat);
                if (subcats.size() > 0) {
                    ArrayList<String> sortedSubs = new ArrayList<String>();
                    sortedSubs.addAll(subcats);
                    Collections.sort(sortedSubs);
                    for (String subcat : sortedSubs) {
                        if (subcategory == null || subcat.toLowerCase().contains(subcategory.toLowerCase().trim())) {//subcat matches
                            boolean subcatAdded = false;
                            ArrayList<String> topics = this.topicsBySubcat.get(cat+"-"+subcat);
                            if (topics.size() > 0) {
                                ArrayList<String> sortedTopics = new ArrayList<String>();
                                sortedTopics.addAll(topics);
                                for (String topic : sortedTopics) {
                                    if (topic.toLowerCase().contains(search.toLowerCase().trim())) {//topic matches
                                        if (!catAdded) {
                                            results.add(" category: "+cat);
                                            catAdded = true;
                                        }
                                        if (!subcatAdded) {
                                            results.add("    subcategory: " + subcat);
                                            subcatAdded = true;
                                        }
                                        results.add("        topic: "+topic);
                                    }//end topic matches
                                }
                            } 
                        }//end subcat matches
                    }
                } else {
                    ArrayList<String> topics = this.topicsBySubcat.get(cat+"-nosubcat");
                    if (topics.size() > 0) {
                        ArrayList<String> sortedTopics = new ArrayList<String>();
                        sortedTopics.addAll(topics);
                        for (String topic : sortedTopics) {
                            if (topic.toLowerCase().contains(search.toLowerCase().trim())) {//topic matches
                                if (!catAdded) {
                                    results.add(" category: "+cat);
                                    catAdded = true;
                                }
                                results.add("        topic: "+topic);
                            }//end topic matches
                        }
                    }
                }
            }//end matches category
        }
        return results;
    }
    public ArrayList<String> getPartialSuggestionSubcat(String category, String search) {
        ArrayList<String> results = new ArrayList<String>();
        
        for (String cat : this.categories) {
            if (category == null || cat.toLowerCase().contains(category.toLowerCase())) {
                boolean catPrinted = false;
                ArrayList<String> subcats = this.subcatsByCat.get(cat);
                if (subcats.size() > 0) {
                    ArrayList<String> sortedSubs = new ArrayList<String>();
                    sortedSubs.addAll(subcats);
                    Collections.sort(sortedSubs);
                    for (String subcat : sortedSubs) {
                        if (subcat.toLowerCase().startsWith(search.toLowerCase())) {
                            if (!catPrinted) {
                                results.add(" category: "+cat);
                                catPrinted = true;
                            }
                            results.add("    subcategory: " + subcat);
                        }
                    }
                }
            }
        }
        return results;
    }
    
    public ArrayList<String> getPartialSuggestion(String[] split, int tag) {
        ArrayList<String> results = new ArrayList<String>();
        if (tag == TAG_NAME) {
            for (String name : layerNameIdMap.keySet()) {
                boolean found = false;
                boolean containsAll = true;
                for (String one : split) {
                    if (name.toLowerCase().contains(one.trim().toLowerCase())) {
                        found = true;
                    } else {
                        containsAll = false;
                        break;
                    }
                }
                if (found && containsAll) {
                    results.add(" name: "+name);
                }
            }
        } else {
            int count = 0;
            for (ArrayList<LayerParameters> lps : this.uniqueRows.values()) {
                if (count > 300) {
                    break;
                }
                LayerParameters lp =  lps.get(0);
                String value = null;
                String suffix = "";
                String name = lp.name;
                switch(tag) {
                    case TAG_DESC:
                        value = lp.description;
                        suffix = "<description match>";
                        break;
                    case TAG_CITATION:
                        value = lp.citation;
                        suffix = "<citation match>";
                        break;    
                    case TAG_LINKS:
                        value = lp.links;
                        suffix = "<links match>";
                        break;    
                }
                boolean found = false;
                boolean containsAll = true;
                for (String one : split) {
                    if (value.toLowerCase().contains(one.trim().toLowerCase())) {
                        found = true;
                    } else {
                        containsAll = false;
                        break;
                    }
                }
                if (found && containsAll) {
                    results.add(" name: "+name+" "+suffix);
                    count++;
                }
            }
        }
        return results;
    }
    public ArrayList<String> getPartialSuggestionAll(String[] split) {
        ArrayList<String> results = new ArrayList<String>();

        int count = 0;
        for (String name : layerNameIdMap.keySet()) {
            if (count > 300) {
                break;
            }
            boolean found = false;
            boolean containsAll = true;
            for (String one : split) {
                if (name.toLowerCase().contains(one.trim().toLowerCase())) {
                    found = true;
                } else {
                    containsAll = false;
                    break;
                }
            }
            if (found && containsAll) {
                results.add(" name: "+name);
                count++;
            }
        }
        if (count < 300) {
            ArrayList<Integer> searchTags = new ArrayList<Integer>();
            searchTags.add(TAG_LINKS);
            searchTags.add(TAG_CITATION);
            searchTags.add(TAG_DESC);
            for (ArrayList<LayerParameters> lps : this.uniqueRows.values()) {
                if (count > 300) {
                    break;
                }
                LayerParameters lp =  lps.get(0);
                String value = null;
                String suffix = "";
                String name = lp.name;
                for (int tag : searchTags) {
                    switch(tag) {
                        case TAG_DESC:
                            value = lp.description;
                            suffix = "<description match>";
                            break;
                        case TAG_CITATION:
                            value = lp.citation;
                            suffix = "<citation match>";
                            break;    
                        case TAG_LINKS:
                            value = lp.links;
                            suffix = "<links match>";
                            break;    
                    }
                    boolean found = false;
                    boolean containsAll = true;
                    for (String one : split) {
                        if (value.toLowerCase().contains(one.trim().toLowerCase())) {
                            found = true;
                        } else {
                            containsAll = false;
                            break;
                        }
                    }
                    if (found && containsAll) {
                        results.add(" name: "+name+" "+suffix);
                        count++;
                        break;//break out of loop through these tags for this LP
                    }
                }
                
            }
        }
        return results;
    }
    public ArrayList<String> getSuggestionSubcatsAndTopicsForCat(String cat) {
        ArrayList<String> results = new ArrayList<String>();
        if (cat.trim().length() > 0) {
            results.add(" category: "+cat);
            ArrayList<String> subcats = this.subcatsByCat.get(cat);
            if (subcats != null && subcats.size() > 0) {
                ArrayList<String> sortedSubs = new ArrayList<String>();
                sortedSubs.addAll(subcats);
                Collections.sort(sortedSubs);
                for (String subcat : sortedSubs) {
                    results.add("    subcategory: " + subcat);
                }
            }
            String catSubcat = cat+"-nosubcat";
            ArrayList<String> topics = topicsBySubcat.get(catSubcat);
            if (topics != null && topics.size() > 0) {
                for (String topic : topics) {
                    results.add("    topic:  "+topic);
                }
            }
        }
        return results;
    }
    
    public ArrayList<String> getSuggestionCustom() {
    	ArrayList<String> results = new ArrayList<String>();
    	if (userLoggedIn()) {
    		String path = "Custom-nosubcat-Custom";
    		ArrayList<String> ids = this.idsByPath.get(path);
    		ArrayList<String> sorted = new ArrayList<String>();
    		for (String id : ids) {
    		    sorted.add(this.uniqueRows.get(id).get(0).name);
            }
    		Collections.sort(sorted);
    		for (String name : sorted) {
                results.add(" custom map:  "+name);
    		}
    	}
    	return results;
    }
    public ArrayList<String> getPartialSuggestionCustom(String search) {
        ArrayList<String> results = new ArrayList<String>();
        if (userLoggedIn()) {
            String path = "Custom-nosubcat-Custom";
            ArrayList<String> ids = this.idsByPath.get(path);
            ArrayList<String> sorted = new ArrayList<String>();
            for (String id : ids) {
                sorted.add(this.uniqueRows.get(id).get(0).name);
            }
            Collections.sort(sorted);
            for (String name : sorted) {
                if (search.trim().length() == 0 || name.toLowerCase().contains(search.toLowerCase().trim())) {
                    results.add(" custom map:  "+name);
                }
            }
        }
        return results;
    }
    
    public ArrayList<String> getSuggestionFavorite() {
    	ArrayList<String> results = new ArrayList<String>();
    	if (userLoggedIn()) {
    		for (String fave : this.favorites) {
    			results.add(" favorite:  "+this.uniqueRows.get(fave).get(0).name);
    		}
    	}
    	return results;
    }
    public ArrayList<String> getPartialSuggestionFavorite(String search) {
        ArrayList<String> results = new ArrayList<String>();
        if (userLoggedIn()) {
            for (String fave : this.favorites) {
                String name = this.uniqueRows.get(fave).get(0).name;
                if (search.trim().length() == 0 || name.toLowerCase().contains(search.toLowerCase().trim())) {
                    results.add(" favorite:  "+name);
                }
            }
        }
        return results;
    }
    
    public ArrayList<SearchResultRow> searchLayers(String searchText) {
        searchText = searchText.replace("favorite:", "name:");
        searchText = searchText.replace("custom map:","name:");
        ArrayList<SearchResultRow> resultSet = new ArrayList<SearchResultRow>();
        searchText = searchText.trim();
        if (searchText.length() == 0) {
            return this.buildInitialLayerList(true);//put the home layers back if they search for nothing
        }
        
        HashMap<String,ArrayList<String>> params = new HashMap<String,ArrayList<String>>();
        String temp = searchText;
        
        //parse out something that might look like:
        //name: mola color category: instrument topic: bob name: themis
        ArrayList<Integer> indices = new ArrayList<Integer>();//list of all indices of search categories
        //Loop through each search category/option and list the index of each in the search string
        String[] splitVals = searchText.split(" ",0);
        int start = 0;
        for (String val : splitVals) {
            val = val.trim();
            if (searchOptions.contains(val)) {
                start = searchText.indexOf(val,start);
                indices.add(start);
                start += val.length();
            }
        }
        Collections.sort(indices);//order the list of indices
        if (indices.size() > 0) {
            if (indices.get(0) != 0) {//bob name: themis
                //our first search text is not a tag
                String value = temp.substring(0,indices.get(0));
                if (value != null) {
                    ArrayList<String> vals = params.get("all");
                    if (vals == null) {
                        vals = new ArrayList<String>();
                        params.put("all", vals);
                    }
                    vals.add(value.trim());
                }
            }
            
            for(int i=0; i<indices.size(); i++) {
                String entry;
                if (i+1 == indices.size()) {
                    //last entry
                    entry = temp.substring(indices.get(i));
                } else {
                    entry = temp.substring(indices.get(i), indices.get(i+1));
                }
                String p = entry.substring(0,entry.indexOf(":"));
                String v = entry.substring(entry.indexOf(":")+1);
                p = p.trim();
                v = v.trim();
                if (v.length() > 0) {
                    ArrayList<String> vals = params.get(p);
                    if (vals == null) {
                        vals = new ArrayList<String>();
                        params.put(p, vals);
                    }
                    vals.add(v.trim());
                }
            }
        } else{
            //no tags, just search terms
            ArrayList<String> vals = params.get("all");
            if (vals == null) {
                vals = new ArrayList<String>();
                params.put("all", vals);
            }
            vals.add(temp);
        }
       
        Set<String> keySet = params.keySet();
        if (keySet == null || keySet.size() == 0) {
            return this.buildInitialLayerList(true);
        } 
        HashMap<String,ArrayList<LayerParameters>> layerRows = new HashMap<String,ArrayList<LayerParameters>>();
        HashMap<String,ArrayList<LayerParameters>> layerRowsTop = new HashMap<String,ArrayList<LayerParameters>>();
        
        for (LayerParameters layer : searchParams) {//looping once through each layer parameter
            boolean mainFlag = true;
            boolean foundFlag = false;
            boolean topFlag = true;
            for (String key : keySet) {//for each search tag, get the array list of entries
                boolean tagFlag = true;
                for (String val : params.get(key)) {//loop through the ArrayList
                    boolean entryFlag = true;
                    val = val.trim().toLowerCase();
                    String[] split = val.split(" ", 0);
                    for (String value : split) {
                        if (value == null || value.trim().length() == 0) {
                            continue;
                        }
                        value = value.trim();
                        switch(key) {
                            case "name":
                                if (layer.name.toLowerCase().indexOf(value) > -1) {
                                    foundFlag = true;
                                    topFlag = topFlag && true;
                                } else {
                                    entryFlag = false;
                                }
                                break;
                            case "category":
                                if (layer.category.toLowerCase().indexOf(value) > -1) {
                                    foundFlag = true;
                                    topFlag = false;
                                } else {
                                    entryFlag = false;
                                }
                                break;
                            case "subcategory":
                                if (layer.subcategory.toLowerCase().indexOf(value) > -1) {
                                    foundFlag = true;
                                    topFlag = false;
                                } else {
                                    entryFlag = false;
                                }
                                break;
                            case "topic":
                                if (layer.topic.toLowerCase().indexOf(value) > -1) {
                                    foundFlag = true;
                                    topFlag = false;
                                } else {
                                    entryFlag = false;
                                }
                                break;
                            case "desc":
                            case "description":
                                if (layer.description.toLowerCase().indexOf(value) > -1) {
                                    foundFlag = true;
                                    topFlag = false;
                                } else {
                                    entryFlag = false;
                                }
                                break;
                            case "citation":
                                if (layer.citation.toLowerCase().indexOf(value) > -1) {
                                    foundFlag = true;
                                    topFlag = false;
                                } else {
                                    entryFlag = false;
                                }
                                break;    
                            case "links":
                                if (layer.links.toLowerCase().indexOf(value) > -1) {
                                    foundFlag = true;
                                    topFlag = false;
                                } else {
                                    entryFlag = false;
                                }
                                break;
                            case "instrument":
                                if (layer.category.toLowerCase().equals("instrument")) {
                                    if (layer.subcategory == null || layer.subcategory.trim().length() == 0) {
                                        if (layer.topic.toLowerCase().indexOf(value) > -1) {
                                            foundFlag = true;
                                            topFlag = false;
                                        }
                                    } else if (layer.subcategory.toLowerCase().indexOf(value) > -1) {
                                        foundFlag = true;
                                        topFlag = false;
                                    }
                                    
                                }
                                if (!foundFlag) {
                                    entryFlag = false;
                                }
                                break;
                            case "imagery":
                                if (layer.category.toLowerCase().equals("imagery")) {
                                    if (layer.subcategory == null || layer.subcategory.trim().length() == 0) {
                                        if (layer.topic.toLowerCase().indexOf(value) > -1) {
                                            foundFlag = true;
                                            topFlag = false;
                                        }
                                    } else if (layer.subcategory.toLowerCase().indexOf(value) > -1) {
                                        foundFlag = true;
                                        topFlag = false;
                                    }
                                    
                                }
                                if (!foundFlag) {
                                    entryFlag = false;
                                }
                                break;
                            case "custom":
                                if (layer.category.toLowerCase().equals("custom")) {
                                    foundFlag = true;
                                    topFlag = true;
                                } else {
                                    entryFlag = false;
                                }
                                break;
                            case "all":
                                if (layer.name.toLowerCase().indexOf(value) > -1) {
                                    foundFlag = true;
                                    topFlag = topFlag && true;
                                } else if (layer.category.toLowerCase().indexOf(value) > -1 ||
                                    layer.subcategory.toLowerCase().indexOf(value) > -1 ||
                                    layer.topic.toLowerCase().indexOf(value) > -1 ||
                                    layer.description.toLowerCase().indexOf(value) > -1 ||
                                    layer.citation.toLowerCase().indexOf(value) > -1 ||
                                    layer.links.toLowerCase().indexOf(value) > -1) {
                                    foundFlag = true;
                                    topFlag = false;
                                } else {
                                    entryFlag = false;
                                }
                                break;
                        }
                    }
                    tagFlag = tagFlag && entryFlag;
                }
                mainFlag = mainFlag && tagFlag;
            
            }
            if (mainFlag && foundFlag) {
                if (topFlag) {
                    if (!layerRows.containsKey(layer.id)) {
                        ArrayList<LayerParameters> layers = layerRowsTop.get(layer.id);
                        if (layers == null) {
                            layers = new ArrayList<LayerParameters>();
                            layerRowsTop.put(layer.id,layers);
                        }
                        layers.add(layer);
                    }
                } else {
                    if (!layerRowsTop.containsKey(layer.id)) {
                        ArrayList<LayerParameters> layers = layerRows.get(layer.id);
                        if (layers == null) {
                            layers = new ArrayList<LayerParameters>();
                            layerRows.put(layer.id,layers);
                        }
                        layers.add(layer);
                    }
                }
            }
            
        }
        for (String key : layerRowsTop.keySet()) {
            SearchResultRow row = buildRow(key,true);
            resultSet.add(row);
        }
        for (String key : layerRows.keySet()) {
            SearchResultRow row = buildRow(key,true);
            resultSet.add(row);
        }
        return resultSet;
    }
    
}
