class StoreStorable:
    def __init__(self, name):
        self.__name = name

    def __call__(self, store, obj, objfact):
        if store.isReading():
            from xmlstore import StoreException
            try:
                objproxy = store.storeStorable(self.__name, None, _StorableTypeProxyFactory(objfact))
                result = objproxy.getObject()
                setattr(obj, self.__name, result)
            except StoreException:
                # If the item is not found then it will
                # not be set as an attribute of the parent.
                pass
        else:
            value = getattr(obj, self.__name)
            proxy = _StorableTypeProxy(objfact)
            proxy.setObject(value)
            store.storeStorable(self.__name, proxy, None)

    def name(self):
        return self.__name

