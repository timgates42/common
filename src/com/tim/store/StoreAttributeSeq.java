class StoreAttributeSeq(StoreAttribute):

    def __init__(self, name, attr_type=TYPE_STRING):
        StoreAttribute.__init__(self, name, attr_type, True)

    def name(self):
        return self.__name


