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

package net.blauerfalke.synco.merge.conflict;

import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.SyncTriple;
import net.blauerfalke.synco.model.Syncable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(JUnit4.class)
public class TakeNewerOnMergeConflictStrategyTest {

    private TakeNewerOnMergeConflictStrategy takeNewerOnMergeConflictStrategy = new TakeNewerOnMergeConflictStrategy();

    @Test
    public void testMergeFieldReturnsLeftDiffOnLeftNewer() {
        Diff leftDiff = new Diff<>("a", "b");
        Diff rightDiff = new Diff<>("a", "c");
        Syncable left = mock(Syncable.class);
        when(left.getUpdated()).thenReturn(100L);
        Syncable right = mock(Syncable.class);
        when(right.getUpdated()).thenReturn(200L);
        SyncTriple syncTriple = new SyncTriple(mock(Syncable.class), left, right);

        Diff<?> result = takeNewerOnMergeConflictStrategy.mergeField(leftDiff, rightDiff, syncTriple);

        assertEquals(rightDiff, result);
    }

    @Test
    public void testMergeFieldReturnsRightDiffOnRightNewer() {
        Diff leftDiff = new Diff<>("a", "b");
        Diff rightDiff = new Diff<>("a", "c");
        Syncable left = mock(Syncable.class);
        when(left.getUpdated()).thenReturn(200L);
        Syncable right = mock(Syncable.class);
        when(right.getUpdated()).thenReturn(100L);
        SyncTriple syncTriple = new SyncTriple(mock(Syncable.class), left, right);

        Diff<?> result = takeNewerOnMergeConflictStrategy.mergeField(leftDiff, rightDiff, syncTriple);

        assertEquals(leftDiff, result);
    }

    @Test
    public void testMergeFieldReturnsLeftOnEquals() {
        Diff leftDiff = new Diff<>("a", "b");
        Diff rightDiff = new Diff<>("a", "c");
        Syncable left = mock(Syncable.class);
        when(left.getUpdated()).thenReturn(500L);
        Syncable right = mock(Syncable.class);
        when(right.getUpdated()).thenReturn(500L);
        SyncTriple syncTriple = new SyncTriple(mock(Syncable.class), left, right);

        Diff<?> result = takeNewerOnMergeConflictStrategy.mergeField(leftDiff, rightDiff, syncTriple);

        assertEquals(leftDiff, result);
    }

}
