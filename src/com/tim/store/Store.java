package com.tim.store;

import java.util.List;

public interface Store {

    public static enum StoreMode { Reading, Writing };
    public static enum StoreType { IntType, LongType, FloatType, StringType, StorableType }; 

    public boolean isReading();

    public int storeInt(String id, int value, int deflt);
    public long storeLong(String id, long value, long deflt);
    public float storeFloat(String id, float value, float deflt);
    public String storeString(String id, String value, String deflt);
    public Storable storeStorable(String id, Storable value, Storable deflt, Class<Storable> value_class);

    public List<Integer> storeIntSeq(String id, List<Integer> value, int deflt);
    public List<Long> storeLongSeq(String id, List<Long> value, long deflt);
    public List<Float> storeFloatSeq(String id, List<Float> value, float deflt);
    public List<String> storeStringSeq(String id, List<String> value, String deflt);
    public List<Storable> storeStorableSeq(String id, List<Storable> value, Storable deflt, Class<Storable> value_class);
    
}

