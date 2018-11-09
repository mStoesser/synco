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

package net.blauerfalke.synco.provider;

import lombok.extern.slf4j.Slf4j;
import net.blauerfalke.synco.SyncableMapper;
import net.blauerfalke.synco.SyncableProvider;
import net.blauerfalke.synco.mapper.SerializeMapper;
import net.blauerfalke.synco.model.Syncable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class LocalFileProvider implements SyncableProvider {

    protected File localDirectory;

    protected SyncableMapper syncableMapper;

    public LocalFileProvider() {
        this.localDirectory = new File(".");
        this.syncableMapper = new SerializeMapper();
    }

    public Enumeration<String> list() {
//        return Collections.enumeration(Stream.of(localDirectory.list()).filter((name)-> !"..".equals(name) && ".".equals(name) ).collect(Collectors.toList()));
        final List<String> result = new ArrayList<>();
        for (String fileName : localDirectory.list()) {
            if (!"..".equals(fileName) && !".".equals(fileName)) {
                result.add(fileName);
            }
        }
        return Collections.enumeration(result);
    }

    @Override
    public Syncable save(String id, Syncable syncable) {
        writeString(new File(localDirectory, id), syncableMapper.toString(syncable));
        return syncable;
    }

    @Override
    public Syncable load(String id) {
        return syncableMapper.fromString(readString(new File(localDirectory, id)));
    }

    private static String readString(final File file) {
        final StringBuilder builder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } catch (IOException e) {
            log.error("error reading file '{}'",file, e);
        }
        return builder.toString();
    }

    private static void writeString(File file, String string) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(string);
        } catch (IOException e) {
            log.error("error saving file: '{}'", file, e);
        }
    }
}
