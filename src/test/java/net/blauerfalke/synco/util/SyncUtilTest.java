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

package net.blauerfalke.synco.util;


import lombok.AllArgsConstructor;
import lombok.Data;
import net.blauerfalke.synco.conf.SyncConfiguration;
import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.Syncable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class SyncUtilTest {

    @Data
    @AllArgsConstructor
    public class SyncObjectWithGetterSetter implements Syncable {
        private String id;
        private Long updated;
        private boolean deleted;
        private String string;
        private int primitiveInt;
        private Integer objectInt;
        private long primitiveLong;
        private Long objectLong;
        private double primitiveDouble;
        private Double objectDouble;
        private float primitiveFloat;
        private Float objectFloat;
        private boolean primitiveBoolean;
        private Boolean objectBoolean;
        private List<?> list;

    }

    List<String> CHANGED_LIST = Arrays.asList("foo", "bar");

    List<String> syncObjectWithGetterSetterList = Arrays.asList("list");
    SyncObjectWithGetterSetter syncObjectWithGetterSetter = new SyncObjectWithGetterSetter("ID", 100L, false, "string",
            1, Integer.valueOf(1), 1l, Long.valueOf(1l), 1.5d, Double.valueOf(1.5d), 1.5f, Float.valueOf(1.5f),
            false, Boolean.FALSE, syncObjectWithGetterSetterList);

    SyncObjectWithGetterSetter SYNC_OBJECT_WITH_GETTER_SETTER_CHANGED = new SyncObjectWithGetterSetter("ID", 100L, false, "change",
            2, Integer.valueOf(2), 2l, Long.valueOf(2l), 2.5d, Double.valueOf(2.5d), 2.5f, Float.valueOf(2.5f),
            true, Boolean.TRUE,
            CHANGED_LIST);



    @AllArgsConstructor
    public class SyncObjectWithFields implements Syncable {
        public String id;
        public Long updated;
        public boolean deleted;
        public String string;
        public int primitiveInt;
        public Integer objectInt;
        public long primitiveLong;
        public Long objectLong;
        public double primitiveDouble;
        public Double objectDouble;
        public float primitiveFloat;
        public Float objectFloat;
        public boolean primitiveBoolean;
        public Boolean objectBoolean;
        public List<?> list;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Long getUpdated() {
            return updated;
        }

        @Override
        public boolean isDeleted() {
            return deleted;
        }
    }

    SyncObjectWithFields syncObjectWithFields = new SyncObjectWithFields("ID", 100L, false, "string",
            1, Integer.valueOf(1), 1l, Long.valueOf(1l), 1.5d, Double.valueOf(1.5d), 1.5f, Float.valueOf(1.5f),
            false, Boolean.FALSE,
            Arrays.asList("list"));

    SyncObjectWithFields SYNC_OBJECT_WITH_FIELDS_CHANGED = new SyncObjectWithFields("ID", 100L, false, "change",
            2, Integer.valueOf(2), 2l, Long.valueOf(2l), 2.5d, Double.valueOf(2.5d), 2.5f, Float.valueOf(2.5f),
            true, Boolean.TRUE,
            CHANGED_LIST);


    @Test
    public void testApplyDiffWithString() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "string", new Diff<>(null, "change"));
        assertEquals("change", syncObjectWithGetterSetter.getString());
    }

    @Test
    public void testApplyDiffWithStringNullValue() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "string", new Diff<>(null, null));
        assertNull(syncObjectWithGetterSetter.getString());
    }

    @Test
    public void testApplyDiffWithObjectInteger() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "objectInt", new Diff<>(null, Integer.valueOf(2)));
        assertEquals(Integer.valueOf(2), syncObjectWithGetterSetter.getObjectInt());
    }

    @Test
    public void testApplyDiffWithObjectIntegerNullValue() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "objectInt", new Diff<>(null, null));
        assertNull(syncObjectWithGetterSetter.getObjectInt());
    }

    @Test
    public void testApplyDiffWithPrimitiveInteger() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "primitiveInt", new Diff<>(null, 2));
        assertEquals(2, syncObjectWithGetterSetter.getPrimitiveInt());
    }

    @Test
    public void testApplyDiffWithPrimitiveIntegerNullValue() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "primitiveInt", new Diff<>(null, null));
        assertEquals(1, syncObjectWithGetterSetter.getPrimitiveInt());
    }

    @Test
    public void testApplyDiffWithObjectLong() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "objectLong", new Diff<>(null, Long.valueOf(2L)));
        assertEquals(Long.valueOf(2L), syncObjectWithGetterSetter.getObjectLong());
    }

    @Test
    public void testApplyDiffWithObjectLongNullValue() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "objectLong", new Diff<>(null, null));
        assertNull(syncObjectWithGetterSetter.getObjectLong());
    }

    @Test
    public void testApplyDiffWithPrimitiveLong() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "primitiveLong", new Diff<>(null, 2l));
        assertEquals(2l, syncObjectWithGetterSetter.getPrimitiveLong());
    }

    @Test
    public void testApplyDiffWithPrimitiveLongNullValue() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "primitiveLong", new Diff<>(null, null));
        assertEquals(1, syncObjectWithGetterSetter.getPrimitiveLong());
    }

    @Test
    public void testApplyDiffWithObjectFloat() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "objectFloat", new Diff<>(null, Float.valueOf(3.5f)));
        assertEquals(Float.valueOf(3.5f), syncObjectWithGetterSetter.getObjectFloat());
    }

    @Test
    public void testApplyDiffWithObjectFloatNullValue() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "objectFloat", new Diff<>(null, null));
        assertNull(syncObjectWithGetterSetter.getObjectFloat());
    }

    @Test
    public void testApplyDiffWithPrimitiveFloat() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "primitiveFloat", new Diff<>(null, 3.5f));
        assertEquals(3.5f, syncObjectWithGetterSetter.getPrimitiveFloat(), 0.0f);
    }

    @Test
    public void testApplyDiffWithPrimitiveFloatNullValue() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "primitiveFloat", new Diff<>(null, null));
        assertEquals(1.5f, syncObjectWithGetterSetter.getPrimitiveFloat(), 0.0f);
    }

    @Test
    public void testApplyDiffWithObjectDouble() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "objectDouble", new Diff<>(null, Double.valueOf(3.5d)));
        assertEquals(Double.valueOf(3.5d), syncObjectWithGetterSetter.getObjectDouble());
    }

    @Test
    public void testApplyDiffWithObjectDoubleNullValue() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "objectDouble", new Diff<>(null, null));
        assertNull(syncObjectWithGetterSetter.getObjectDouble());
    }

    @Test
    public void testApplyDiffWithPrimitiveDouble() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "primitiveDouble", new Diff<>(null, 3.5d));
        assertEquals(3.5d, syncObjectWithGetterSetter.getPrimitiveDouble(), 0.0d);
    }

    @Test
    public void testApplyDiffWithPrimitiveDoubleNullValue() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "primitiveDouble", new Diff<>(null, null));
        assertEquals(1.5d, syncObjectWithGetterSetter.getPrimitiveDouble(), 0.0d);
    }

    @Test
    public void testApplyDiffWithObjectBoolean() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "objectBoolean", new Diff<>(null, Boolean.TRUE));
        assertEquals(Boolean.TRUE, syncObjectWithGetterSetter.getObjectBoolean());
    }

    @Test
    public void testApplyDiffWithObjectBooleanNullValue() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "objectBoolean", new Diff<>(null, null));
        assertNull(syncObjectWithGetterSetter.getObjectBoolean());
    }

    @Test
    public void testApplyDiffWithPrimitiveBoolean() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "primitiveBoolean", new Diff<>(null, true));
        assertEquals(true, syncObjectWithGetterSetter.isPrimitiveBoolean());
    }

    @Test
    public void testApplyDiffWithPrimitiveBooleanNullValue() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "primitiveBoolean", new Diff<>(null, null));
        assertEquals(false, syncObjectWithGetterSetter.isPrimitiveBoolean());
    }

    @Test
    public void testApplyDiffWithList() {
        List<String> list = Arrays.asList("foo", "bar");
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "list", new Diff<>(null, list));
        assertEquals(list, syncObjectWithGetterSetter.getList());
    }

    @Test
    public void testApplyDiffWithListNullValue() {
        SyncUtil.applyDiff(syncObjectWithGetterSetter, "list", new Diff<>(null, null));
        assertNull(syncObjectWithGetterSetter.getList());
    }






    @Test
    public void testApplyDiffFieldWithString() {
        SyncUtil.applyDiff(syncObjectWithFields, "string", new Diff<>(null, "change"));
        assertEquals("change", syncObjectWithFields.string);
    }

    @Test
    public void testApplyDiffFieldWithStringNullValue() {
        SyncUtil.applyDiff(syncObjectWithFields, "string", new Diff<>(null, null));
        assertNull(syncObjectWithFields.string);
    }

    @Test
    public void testApplyDiffFieldWithObjectInteger() {
        SyncUtil.applyDiff(syncObjectWithFields, "objectInt", new Diff<>(null, Integer.valueOf(2)));
        assertEquals(Integer.valueOf(2), syncObjectWithFields.objectInt);
    }

    @Test
    public void testApplyDiffFieldWithObjectIntegerNullValue() {
        SyncUtil.applyDiff(syncObjectWithFields, "objectInt", new Diff<>(null, null));
        assertNull(syncObjectWithFields.objectInt);
    }

    @Test
    public void testApplyDiffFieldWithPrimitiveInteger() {
        SyncUtil.applyDiff(syncObjectWithFields, "primitiveInt", new Diff<>(null, 2));
        assertEquals(2, syncObjectWithFields.primitiveInt);
    }

    @Test
    public void testApplyDiffFieldWithPrimitiveIntegerNullValue() {
        SyncUtil.applyDiff(syncObjectWithFields, "primitiveInt", new Diff<>(null, null));
        assertEquals(1, syncObjectWithFields.primitiveInt);
    }

    @Test
    public void testApplyDiffFieldWithObjectLong() {
        SyncUtil.applyDiff(syncObjectWithFields, "objectLong", new Diff<>(null, Long.valueOf(2)));
        assertEquals(Long.valueOf(2), syncObjectWithFields.objectLong);
    }

    @Test
    public void testApplyDiffFieldWithObjectLongNullValue() {
        SyncUtil.applyDiff(syncObjectWithFields, "objectLong", new Diff<>(null, null));
        assertNull(syncObjectWithFields.objectLong);
    }

    @Test
    public void testApplyDiffFieldWithPrimitiveLong() {
        SyncUtil.applyDiff(syncObjectWithFields, "primitiveLong", new Diff<>(null, 2l));
        assertEquals(2l, syncObjectWithFields.primitiveLong);
    }

    @Test
    public void testApplyDiffFieldWithPrimitiveLongNullValue() {
        SyncUtil.applyDiff(syncObjectWithFields, "primitiveLong", new Diff<>(null, null));
        assertEquals(1l, syncObjectWithFields.primitiveLong);
    }

    @Test
    public void testApplyDiffFieldWithObjectFloat() {
        SyncUtil.applyDiff(syncObjectWithFields, "objectFloat", new Diff<>(null, Float.valueOf(3.5f)));
        assertEquals(Float.valueOf(3.5f), syncObjectWithFields.objectFloat);
    }

    @Test
    public void testApplyDiffFieldWithObjectFloatNullValue() {
        SyncUtil.applyDiff(syncObjectWithFields, "objectFloat", new Diff<>(null, null));
        assertNull(syncObjectWithFields.objectFloat);
    }

    @Test
    public void testApplyDiffFieldWithPrimitiveFloat() {
        SyncUtil.applyDiff(syncObjectWithFields, "primitiveFloat", new Diff<>(null, 3.5f));
        assertEquals(3.5f, syncObjectWithFields.primitiveFloat, 0.0f);
    }

    @Test
    public void testApplyDiffFieldWithPrimitiveFloatNullValue() {
        SyncUtil.applyDiff(syncObjectWithFields, "primitiveFloat", new Diff<>(null, null));
        assertEquals(1.5f, syncObjectWithFields.primitiveFloat, 0.0f);
    }

    @Test
    public void testApplyDiffFieldWithObjectDouble() {
        SyncUtil.applyDiff(syncObjectWithFields, "objectDouble", new Diff<>(null, Double.valueOf(3.5d)));
        assertEquals(Double.valueOf(3.5d), syncObjectWithFields.objectDouble);
    }

    @Test
    public void testApplyDiffFieldWithObjectDoubleNullValue() {
        SyncUtil.applyDiff(syncObjectWithFields, "objectDouble", new Diff<>(null, null));
        assertNull(syncObjectWithFields.objectDouble);
    }

    @Test
    public void testApplyDiffFieldWithPrimitiveDouble() {
        SyncUtil.applyDiff(syncObjectWithFields, "primitiveDouble", new Diff<>(null, 3.5d));
        assertEquals(3.5d, syncObjectWithFields.primitiveDouble, 0.0d);
    }

    @Test
    public void testApplyDiffFieldWithPrimitiveDoubleNullValue() {
        SyncUtil.applyDiff(syncObjectWithFields, "primitiveDouble", new Diff<>(null, null));
        assertEquals(1.5d, syncObjectWithFields.primitiveDouble, 0.0d);
    }

    @Test
    public void testApplyDiffFieldWithObjectBoolean() {
        SyncUtil.applyDiff(syncObjectWithFields, "objectBoolean", new Diff<>(null, Boolean.TRUE));
        assertEquals(Boolean.TRUE, syncObjectWithFields.objectBoolean);
    }

    @Test
    public void testApplyDiffFieldWithObjectBooleanNullValue() {
        SyncUtil.applyDiff(syncObjectWithFields, "objectBoolean", new Diff<>(null, null));
        assertNull(syncObjectWithFields.objectBoolean);
    }

    @Test
    public void testApplyDiffFieldWithPrimitiveBoolean() {
        SyncUtil.applyDiff(syncObjectWithFields, "primitiveBoolean", new Diff<>(null, true));
        assertEquals(true, syncObjectWithFields.primitiveBoolean);
    }

    @Test
    public void testApplyDiffFieldWithPrimitiveBooleanNullValue() {
        SyncUtil.applyDiff(syncObjectWithFields, "primitiveBoolean", new Diff<>(null, null));
        assertEquals(false, syncObjectWithFields.primitiveBoolean);
    }

    @Test
    public void testApplyDiffFieldWithList() {
        List<String> list = Arrays.asList("foo", "bar");
        SyncUtil.applyDiff(syncObjectWithFields, "list", new Diff<>(null, list));
        assertEquals(list, syncObjectWithFields.list);
    }

    @Test
    public void testApplyDiffFieldWithListNullValue() {
        SyncUtil.applyDiff(syncObjectWithFields, "list", new Diff<>(null, null));
        assertNull(syncObjectWithFields.list);
    }

    @Test
    public void testCalculateChangesGetterSetterObject() {
        Map<String,Diff> result = SyncUtil.calculateChanges(syncObjectWithGetterSetter, SYNC_OBJECT_WITH_GETTER_SETTER_CHANGED, new SyncConfiguration());
        assertChanges(result);
    }

    @Test
    public void testCalculateChangesGetterSetterObjectReturnOnlyChanges() {
        Map<String,Diff> result = SyncUtil.calculateChanges(syncObjectWithGetterSetter, syncObjectWithGetterSetter, new SyncConfiguration());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCalculateChangesGetterSetterObjectWithAllFieldConfiguration() {
        SyncConfiguration syncConfiguration = spy(new SyncConfiguration());
        when(syncConfiguration.getSyncableFieldsForType(any(Class.class))).thenReturn(Arrays.asList("string", "primitiveInt", "objectInt", "primitiveLong", "objectLong", "primitiveFloat", "objectFloat", "primitiveDouble", "objectDouble", "primitiveBoolean", "objectBoolean", "list"));

        Map<String,Diff> result = SyncUtil.calculateChanges(syncObjectWithGetterSetter, SYNC_OBJECT_WITH_GETTER_SETTER_CHANGED, syncConfiguration);

        assertChanges(result);
    }

    @Test
    public void testCalculateChangesGetterSetterObjectWithReducedFieldConfiguration() {
        SyncConfiguration syncConfiguration = spy(new SyncConfiguration());
        when(syncConfiguration.getSyncableFieldsForType(any(Class.class))).thenReturn(Arrays.asList("string"));

        Map<String,Diff> result = SyncUtil.calculateChanges(syncObjectWithGetterSetter, SYNC_OBJECT_WITH_GETTER_SETTER_CHANGED, syncConfiguration);

        assertEquals( 1, result.size());
        assertEquals("string", result.get("string").from);
        assertEquals("change", result.get("string").to);
    }

    @Test
    public void testCalculateChangesFieldObject() {
        Map<String,Diff> result = SyncUtil.calculateChanges(syncObjectWithFields, SYNC_OBJECT_WITH_FIELDS_CHANGED, new SyncConfiguration());
        assertChanges(result);
    }

    @Test
    public void testCalculateChangesFieldObjectReturnsOnlyChanges() {
        Map<String,Diff> result = SyncUtil.calculateChanges(syncObjectWithFields, syncObjectWithFields, new SyncConfiguration());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCalculateChangesFieldObjectWithAllFieldConfiguration() {
        SyncConfiguration syncConfiguration = spy(new SyncConfiguration());
        when(syncConfiguration.getSyncableFieldsForType(any(Class.class))).thenReturn(Arrays.asList("string", "primitiveInt", "objectInt", "primitiveLong", "objectLong", "primitiveFloat", "objectFloat", "primitiveDouble", "objectDouble", "primitiveBoolean", "objectBoolean", "list"));

        Map<String,Diff> result = SyncUtil.calculateChanges(syncObjectWithFields, SYNC_OBJECT_WITH_FIELDS_CHANGED, syncConfiguration);

        assertChanges(result);
    }

    @Test
    public void testCalculateChangesFieldObjectWithReducedFieldConfiguration() {
        SyncConfiguration syncConfiguration = spy(new SyncConfiguration());
        when(syncConfiguration.getSyncableFieldsForType(any(Class.class))).thenReturn(Arrays.asList("string"));

        Map<String,Diff> result = SyncUtil.calculateChanges(syncObjectWithFields, SYNC_OBJECT_WITH_FIELDS_CHANGED, syncConfiguration);

        assertEquals( 1, result.size());
        assertEquals("string", result.get("string").from);
        assertEquals("change", result.get("string").to);
    }

    private void assertChanges(Map<String,Diff> result) {
        assertEquals(12, result.size());
        assertEquals("string", result.get("string").from);
        assertEquals("change", result.get("string").to);
        assertEquals(1, result.get("primitiveInt").from);
        assertEquals(2, result.get("primitiveInt").to);
        assertEquals(Integer.valueOf(1), result.get("objectInt").from);
        assertEquals(Integer.valueOf(2), result.get("objectInt").to);
        assertEquals(1l, result.get("primitiveLong").from);
        assertEquals(2l, result.get("primitiveLong").to);
        assertEquals(Long.valueOf(1l), result.get("objectLong").from);
        assertEquals(Long.valueOf(2l), result.get("objectLong").to);
        assertEquals(1.5f, result.get("primitiveFloat").from);
        assertEquals(2.5f, result.get("primitiveFloat").to);
        assertEquals(Float.valueOf(1.5f), result.get("objectFloat").from);
        assertEquals(Float.valueOf(2.5f), result.get("objectFloat").to);
        assertEquals(1.5d, result.get("primitiveDouble").from);
        assertEquals(2.5d, result.get("primitiveDouble").to);
        assertEquals(Double.valueOf(1.5d), result.get("objectDouble").from);
        assertEquals(Double.valueOf(2.5d), result.get("objectDouble").to);
        assertEquals(false, result.get("primitiveBoolean").from);
        assertEquals(true, result.get("primitiveBoolean").to);
        assertEquals(Boolean.FALSE, result.get("objectBoolean").from);
        assertEquals(Boolean.TRUE, result.get("objectBoolean").to);
        assertEquals(syncObjectWithGetterSetterList, result.get("list").from);
        assertEquals(CHANGED_LIST, result.get("list").to);
    }

}