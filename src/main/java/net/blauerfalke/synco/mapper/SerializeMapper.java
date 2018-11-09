/*
 * Copyright 2018 Michael Stößer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.blauerfalke.synco.mapper;

import lombok.extern.slf4j.Slf4j;
import net.blauerfalke.synco.SyncableMapper;
import net.blauerfalke.synco.model.Syncable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Slf4j
public class SerializeMapper implements SyncableMapper {

    @Override
    public Syncable fromString(String string) {
        return (Syncable) deserialize(string);
    }

    @Override
    public String toString(Syncable syncable) {
        return serialize(syncable);
    }

    private static String serialize(Object o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();
            return new String(baos.toByteArray());
//            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            log.error("error serialize object '{]'", o, e);
        }
        return null;
    }

    private static Object deserialize(String str) {
        try {
//            byte [] data = Base64.getDecoder().decode( str );
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(str.getBytes()));
            Object o  = ois.readObject();
            ois.close();
            return o;
        } catch (IOException e) {
            log.error("error deserialize object", e);
        } catch (ClassNotFoundException e) {
            log.error("error deserialize object, class not found", e);
        }
        return null;
    }
}
