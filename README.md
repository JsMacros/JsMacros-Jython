# JsMacros-JEP

This extension adds `python 2.7` support to [JsMacros](https://github.com/wagyourtail/JsMacros) `1.2.3+`

# issues/notes

consumers aren't automatically created from py functions so use:
```python 
from java.util.function import Consumer

class jc(Consumer):
    def __init__(self, fn):
        self.accept=fn
```
same thing for BiConsumers basically.
