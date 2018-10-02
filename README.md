# sync'O

## Overview
  Sync'O is a lightweight java object synchronisation lib. It is made to synchronise POJO.

## Roadmap
  * Callbacks before merge and pull
  * REST default provider
  * Local File System Provider
  * Syncing lists
  * read a configuration file
  * Support maps
  * recursive objects
  
  
## Install
Currently the project is not published to public artifact repository like maven central. 
So the only way to install it is, download it by yourself and install it with maven:
 
 ```
 git pull git@github.com:mStoesser/synco.git
 cd synco
 mvn install
 ```


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




