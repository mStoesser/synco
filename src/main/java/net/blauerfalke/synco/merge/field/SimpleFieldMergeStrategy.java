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

public class SimpleFieldMergeStrategy implements FieldMergeStrategy {
    public Diff<?> mergeField(Diff<?> left, Diff<?> right, SyncTriple syncTriple, MergeConflictStrategy mergeConflictStrategy) {
        if((left.to != null && left.to.equals(right.to)) || (left.to == null && right.to == null)) {
            return left;
        } else {
            return mergeConflictStrategy.mergeField(left, right, syncTriple);
        }
    }
}