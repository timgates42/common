package com.tim.store;

public class StoreAttribute {

    def __init__(self, name, Store.StoreType attr_type, sequence=False):
        self.__name = name
        self.__attr_type = attr_type
        self.__sequence = sequence

    def __call__(self, store, obj, objfact):
        handler = getattr(store, 'store%s%s' % (self.__attr_type, (self.__sequence and 'Seq' or '')))
        if hasattr(obj, self.__name):
            value = getattr(obj, self.__name)
        else:
            value = None
        setattr(obj, self.__name, handler(self.__name, value))

    def name(self):
        return self.__name

