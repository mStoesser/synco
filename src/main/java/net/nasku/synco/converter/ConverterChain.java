package net.nasku.synco.converter;

import net.nasku.synco.converter.interf.Converter;

import java.util.List;
import java.util.Vector;

/**
 * Created by Seven on 24.08.2016.
 */
public class ConverterChain<From, To> implements Converter<From, To> {

    List<Converter> converters = new Vector<Converter>();

    public void add(Converter converter) {
        converters.add(converter);
    }

    @Override
    public To convert(From from) {
        Object o = from;
        for(Converter converter : converters) {
            o = converter.convert(o);
        }
        return (To) o;
    }
}
