package com.softium.datacenter.paas.web.utils.easy.input;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * MAP
 * 2019/11/11
 *
 * @author paul
 */
public class MapExcelReadModel extends BaseExcelReadModel implements Map<Integer, Object> {

    public static final String L_KEY = "rowIndex";

    public static final String MESSAGE_KEY = "message";

    public final LinkedHashMap linkedHashMap;


    public MapExcelReadModel(Map map) {
        if (map instanceof LinkedHashMap) {
            this.linkedHashMap = (LinkedHashMap) map;
        } else {
            this.linkedHashMap = new LinkedHashMap(map);
        }

    }

    public MapExcelReadModel() {
        this.linkedHashMap = new LinkedHashMap();
    }

    public MapExcelReadModel(int initialCapacity) {
        this.linkedHashMap = new LinkedHashMap(initialCapacity);
    }

    @Override
    public Integer getRowIndex() {
        return (Integer) linkedHashMap.get(L_KEY);
    }

    @Override
    public void setRowIndex(Integer l) {
        linkedHashMap.put(L_KEY, l);
    }

    @Override
    public String getMessage() {
        return (String) linkedHashMap.get(MESSAGE_KEY);
    }

    @Override
    public void setMessage(String message) {
        linkedHashMap.put(MESSAGE_KEY, message);
    }

    @Override
    public int size() {
        return linkedHashMap.size();
    }

    @Override
    public boolean isEmpty() {
        return linkedHashMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return linkedHashMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return linkedHashMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return linkedHashMap.get(key);
    }

    @Override
    public Object put(Integer key, Object value) {
        return linkedHashMap.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return linkedHashMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends Integer, ?> m) {
        linkedHashMap.putAll(m);
    }

    @Override
    public void clear() {
        linkedHashMap.clear();
    }

    @Override
    public Set<Integer> keySet() {
        return linkedHashMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return linkedHashMap.values();
    }

    @Override
    public Set<Entry<Integer, Object>> entrySet() {
        return linkedHashMap.entrySet();
    }
}
