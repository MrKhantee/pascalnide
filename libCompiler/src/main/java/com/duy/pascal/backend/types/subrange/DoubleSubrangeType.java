/*
 *  Copyright (c) 2017 Tran Le Duy
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

package com.duy.pascal.backend.types.subrange;

import android.support.annotation.Nullable;

import com.duy.pascal.backend.parse_exception.index.LowerGreaterUpperBoundException;

public class DoubleSubrangeType extends SubrangeType<Double> {
    private static final String TAG = "IntegerSubrangeType";

    public DoubleSubrangeType(Double first, Double last) throws LowerGreaterUpperBoundException {
        super(first, last);
        this.first = first;
        this.last = last;
    }

    @Override
    public boolean contains(SubrangeType other) {
        return other instanceof DoubleSubrangeType
                && first <= ((DoubleSubrangeType) other).first
                && last >= ((DoubleSubrangeType) other).last;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + first.hashCode();
        result = prime * result + last.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return null;
    }

    @Nullable
    @Override
    public Class<?> getStorageClass() {
        return Double.class;
    }
}
