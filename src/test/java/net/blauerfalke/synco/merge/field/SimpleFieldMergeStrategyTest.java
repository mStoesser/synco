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

package net.blauerfalke.synco.merge.field;

import net.blauerfalke.synco.merge.conflict.MergeConflictStrategy;
import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.SyncTriple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class SimpleFieldMergeStrategyTest {

    private SimpleFieldMergeStrategy simpleFieldMergeStrategy = new SimpleFieldMergeStrategy();

    @Test
    public void testMergeFieldCallsMergeConflictStrategyOnConflict() {
        Diff<String> leftDiff = new Diff<>("a", "b");
        Diff<String> rightDiff = new Diff<>("a", "c");
        SyncTriple syncTriple = mock(SyncTriple.class);
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);
        Diff<?> expected = new Diff<>("a", "bc");
        doReturn(expected).when(mergeConflictStrategy).mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class));

        Diff<?> result = simpleFieldMergeStrategy.mergeField(leftDiff, rightDiff, syncTriple, mergeConflictStrategy);

        assertEquals(expected, result);
        verify(mergeConflictStrategy).mergeField(leftDiff, rightDiff, syncTriple);
    }

    @Test
    public void testMergeFieldDirectlyReturnsDiffOnEquals() {
        Diff<String> leftDiff = new Diff<>("a", "d");
        Diff<String> rightDiff = new Diff<>("a", "d");

        Diff<?> result = simpleFieldMergeStrategy.mergeField(leftDiff, rightDiff, mock(SyncTriple.class), mock(MergeConflictStrategy.class));

        assertEquals(leftDiff, result);
    }

    @Test
    public void testMergeFieldDirectlyReturnsDiffOnNullEquals() {
        Diff<String> leftDiff = new Diff<>("a", null);
        Diff<String> rightDiff = new Diff<>("a", null);

        Diff<?> result = simpleFieldMergeStrategy.mergeField(leftDiff, rightDiff, mock(SyncTriple.class), mock(MergeConflictStrategy.class));

        assertEquals(leftDiff, result);
    }
}
