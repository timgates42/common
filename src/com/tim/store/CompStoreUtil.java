from xmlstore import TYPE_INT, TYPE_LONG, TYPE_FLOAT, TYPE_STRING
import types

#--------------------------------------------------------#
#                    Public Interfaces                   #
#--------------------------------------------------------#

TYPE_LIST = 'List'
TYPE_NONE = 'None'


def getObjectFactory(typeDict):
    """
    typeDict is of the form {typeStr: objClass}
    """
    return _DefaultObjectFactory(typeDict)

#--------------------------------------------------------#
#                   Private Interfaces                   #
#--------------------------------------------------------#

class _DefaultObjectFactory:

    def __init__(self, typeDict):
        self.__typeDict = {}
        self.__classDict = {}
        for typeStr in typeDict:
            if typeStr in [TYPE_INT, TYPE_LONG, TYPE_FLOAT, TYPE_STRING, TYPE_LIST, TYPE_NONE]:
                raise Exception, '%s is an internal reserved type.'
            typeClass = typeDict[typeStr]
            self.__typeDict[typeStr] = typeClass
            self.__classDict[typeClass] = typeStr

    def getInitializer(self, typeStr):
        return self.__typeDict[typeStr]

    def getType(self, typeClass):
        return self.__classDict[typeClass]

class _StorableTypeProxyFactory:
    
    def __init__(self, objfact):
        self.__objfact = objfact
    
    def __call__(self):
        return _StorableTypeProxy(self.__objfact)
    
class _StorableTypeProxy:
    
    def __init__(self, objfact):
        self.__objfact = objfact
    
    def store(self, store):
        if store.isReading():
            success, xtype, obj = primitiveStoreRead(store)
            if not success:
                self.__object = complexStoreRead(store, xtype, self.__objfact)
            else:
                self.__object = obj
        else:
            success, ptype = primitiveStoreWrite(store, self.__object)
            if not success:
                complexStoreWrite(store, ptype, self.__objfact, self.__object)
    
    def getObject(self):
        return self.__object
    
    def setObject(self, obj):
        self.__object = obj

PY_TO_XML_MAP = {
    types.IntType:       TYPE_INT,
    types.LongType:      TYPE_LONG,
    types.FloatType:     TYPE_FLOAT,
    types.StringType:    TYPE_STRING,
    }
from xlib.alg.sequence import reverseMap
XML_TO_PY_MAP = reverseMap(PY_TO_XML_MAP)
PY_TO_XML_MAP.update({
    types.UnicodeType:   TYPE_STRING,
    })
def primitiveStoreWrite(store, obj):
    ptype = type(obj)
    try:
        xtype = PY_TO_XML_MAP[ptype]
    except KeyError:
        return False, ptype
    else:
        handler = getattr(store, 'store%s' % xtype)
        store.storeString('type', xtype)
        handler('value', obj)
        return True, ptype

def complexStoreWrite(store, ptype, objfact, obj):
    if ptype == types.NoneType:
        store.storeString('type', TYPE_NONE)
    elif ptype == types.ListType:
        store.storeString('type', TYPE_LIST)
        proxyset = []
        for elem in obj:
            proxyelem = _StorableTypeProxy(objfact)
            proxyelem.setObject(elem)
            proxyset.append(proxyelem)
        store.storeStorableSeq('object', proxyset, None)
    else:
        typestr = objfact.getType(obj.__class__)
        store.storeString('type', typestr)
        store.storeStorable('object', obj, None)

def primitiveStoreRead(store):
    xtype = store.storeString('type', '')
    try:
        XML_TO_PY_MAP[xtype]
    except KeyError:
        return False, xtype, None
    else:
        handler = getattr(store, 'store%s' % xtype)
        store.storeString('type', xtype)
        obj = handler('value', None)
        return True, xtype, obj

def complexStoreRead(store, xtype, objfact):
    if xtype == TYPE_NONE:
        return None
    if xtype == TYPE_LIST:
        result = []
        proxyelems = store.storeStorableSeq('object', None, _StorableTypeProxyFactory(objfact))
        return map(lambda x: x.getObject(), proxyelems)
    else:
        delegate_initializer = objfact.getInitializer(xtype)
        return store.storeStorable('object', None, delegate_initializer)

if __name__ == '__main__':
    class X(StoreComponent):
        ATTRS=[StoreAttribute('test', TYPE_STRING), StoreStorable('x')]
    objfact = getObjectFactory({'X': X})
    x=X(objfact)
    x.x = None
    x.test = 'hello'
    from xmlstore import saveXMLStore, loadXMLStore
    saveXMLStore(x, file('test.xml', 'w'))
    loadXMLStore(x, file('test.xml'))
    print type(x.x)

