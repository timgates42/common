class StoreComponent:

    def __init__(self, object_fact):
        self.__object_fact = object_fact

    def getType(self):
        return self.__object_fact.getType(self.__class__)

    def store(self, store):
        for attr in self.__class__.ATTRS:
            attr(store, self, self.__object_fact)
        if store.isReading():
            self.storeLoaded()

    def storeLoaded(self):
        pass

    def __str__(self):
        return repr(self)

    def __repr__(self):
        output = []
        for attr in self.__class__.ATTRS:
            attr = attr.name()
            if hasattr(self, attr):
                output.append('%s: %s' % (attr, getattr(self, attr)))
        return '<%s>(%s)' % (self.__class__, ', '.join(output))

