package net.nasku.synco.converter.interf;

/**
 * Created by Seven on 23.08.2016.
 */
public interface Converter<From,To> {

    To convert(From from);
}
