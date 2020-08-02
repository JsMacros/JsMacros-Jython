# JsMacros-Jython

This extension adds `python 2.7` support to [JsMacros](https://github.com/wagyourtail/JsMacros) `1.2.3+`

# issues/notes

jython doesn't auto-coerce functions to consumers, so use the extra library `consumer`

## 1.0.2+

### toConsumer(function) *1.0.2+*
*Example:* `consumer.toConsumer(func)`

### toBiConsumer(function) *1.0.2+*
*Example:* `consumer.toBiConsumer(func)`



## 1.0.1

consumers aren't automatically created from py functions so use:
```python 
from java.util.function import Consumer

class jc(Consumer):
    def __init__(self, fn):
        self.accept=fn
```
same thing for BiConsumers basically.
