package com.tim.store;

import static com.tim.store.Store.*;
import com.tim.xml.XMLUtil;
import com.tim.xml.ElementNotFoundException;

import org.w3c.dom.*;
import java.util.List;
import java.util.Vector;

public class XMLNodeStore implements Store {

    private StoreMode mode;
    private Document document;
    private Element node;
    
    public XMLNodeStore(Document document, Element node, StoreMode mode) {
        this.document = document;
        this.node = node;
        this.mode = mode;
    }
    
    public boolean isReading() {
        return mode == StoreMode.Reading;
    }
    
    public int storeInt(String id, int value, int deflt) {
        if(isReading()) {
            return XMLUtil.getInt(node, id, deflt);
        } else {
            XMLUtil.setInt(node, id, value);
            return value;
        }
    }
    
    public long storeLong(String id, long value, long deflt) {
        if(isReading()) {
            return XMLUtil.getLong(node, id, deflt);
        } else {
            XMLUtil.setLong(node, id, value);
            return value;
        }
    }
    
    public float storeFloat(String id, float value, float deflt) {
        if(isReading()) {
            return XMLUtil.getFloat(node, id, deflt);
        } else {
            XMLUtil.setFloat(node, id, value);
            return value;
        }
    }
    
    public String storeString(String id, String value, String deflt) {
        if(isReading()) {
            return XMLUtil.getString(node, id, deflt);
        } else {
            XMLUtil.setString(node, id, value);
            return value;
        }
    }
    
    private Storable loadStorable(Element elem, Class<Storable> value_class) throws IllegalAccessException, InstantiationException {
        Store store = new XMLNodeStore(document, elem, mode);
        Storable instance = value_class.newInstance();
        instance.store(store);
        return instance;
    }
    
    private void saveStorable(Element elem, Storable value) {
        Store store = new XMLNodeStore(document, elem, mode);
        value.store(store);
    }
    
    public Storable storeStorable(String id, Storable value, Storable deflt, Class<Storable> value_class) {
        if(isReading()) {
            try {
                Element elem = XMLUtil.getElementE(node, id);
                return loadStorable(elem, value_class);
            } catch(IllegalAccessException e) {
                // Should Really Log this to avoid subtle bugs
                return deflt;
            } catch(InstantiationException e) {
                // Should Really Log this to avoid subtle bugs
                return deflt;
            } catch(ElementNotFoundException e) {
                return deflt;
            }
        } else {
            Element elem = document.createElement(id);
            node.appendChild(elem);
            saveStorable(elem, value);
            return value;
        }
    }
    
    public List<Integer> storeIntSeq(String id, List<Integer> value, int deflt) {
        if(isReading()) {
            List<Integer> result = new Vector<Integer>();
            for(Element elem : XMLUtil.getElements(node, id)) {
                result.add(XMLUtil.getInt(elem, id, deflt));
            }
            return result;
        } else {
            for(int i : value) {
                Element elem = document.createElement(id);
                node.appendChild(elem);
                XMLUtil.setInt(elem, "value", i);
            }
            return value;
        }
    }
    
    public List<Long> storeLongSeq(String id, List<Long> value, long deflt) {
        if(isReading()) {
            List<Long> result = new Vector<Long>();
            for(Element elem : XMLUtil.getElements(node, id)) {
                result.add(XMLUtil.getLong(elem, id, deflt));
            }
            return result;
        } else {
            for(long i : value) {
                Element elem = document.createElement(id);
                node.appendChild(elem);
                XMLUtil.setLong(elem, "value", i);
            }
            return value;
        }
    }
    
    public List<Float> storeFloatSeq(String id, List<Float> value, float deflt) {
        if(isReading()) {
            List<Float> result = new Vector<Float>();
            for(Element elem : XMLUtil.getElements(node, id)) {
                result.add(XMLUtil.getFloat(elem, id, deflt));
            }
            return result;
        } else {
            for(float i : value) {
                Element elem = document.createElement(id);
                node.appendChild(elem);
                XMLUtil.setFloat(elem, "value", i);
            }
            return value;
        }
    }

    public List<String> storeStringSeq(String id, List<String> value, String deflt) {
        if(isReading()) {
            List<String> result = new Vector<String>();
            for(Element elem : XMLUtil.getElements(node, id)) {
                result.add(XMLUtil.getString(elem, id, deflt));
            }
            return result;
        } else {
            for(String i : value) {
                Element elem = document.createElement(id);
                node.appendChild(elem);
                XMLUtil.setString(elem, "value", i);
            }
            return value;
        }
    }

    public List<Storable> storeStorableSeq(String id, List<Storable> value, Storable deflt, Class<Storable> value_class) {
        if(isReading()) {
            List<Storable> result = new Vector<Storable>();
            for(Element elem : XMLUtil.getElements(node, id)) {
                try {
                    result.add(loadStorable(elem, value_class));
                } catch(IllegalAccessException e) {
                    // Should Really Log this to avoid subtle bugs
                } catch(InstantiationException e) {
                    // Should Really Log this to avoid subtle bugs
                }
            }
            return result;
        } else {
            for(Storable i : value) {
                Element elem = document.createElement(id);
                node.appendChild(elem);
                saveStorable(elem, i);
            }
            return value;
        }
    }

}

