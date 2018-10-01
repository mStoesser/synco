# sync'O

## Overview
  Sync'O is a lightweight java object synchronisation lib. It is made to synchronice POJO.
  
## Install

## Using

### How to sync an Object

Just create an Sync instance and call sync with the object-id.

```
Sync sync = new Sync(localProvider, remoteProvider);
sync.sync(id);

```

### Sync lists

```java
List<Syncable> syncables = localProvider.list(MyPojo.class);
for(Syncable syncable : syncables) {

}

````




