package net.blauerfalke.synco.model;

public interface Syncable {

    String getId();
    Long getUpdated();
    boolean isDeleted();

}
