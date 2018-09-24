package net.blauerfalke.synco.model;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Diff<T> {
    public T from;
    public T to;
}
