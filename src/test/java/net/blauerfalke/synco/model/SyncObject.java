package net.blauerfalke.synco.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SyncObject implements Syncable {
    private String id;
    private String name;
    private Long number;
    private Double real;
    private Long updated;
    private boolean deleted;
}
