/*
 * Copyright 2015, 2016 Tagir Valeev
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
package one.util.huntbugs.warning;

import one.util.huntbugs.registry.anno.WarningDefinition;

/**
 * @author lan
 *
 */
public class WarningType {
    private final String category;
    private final String name;
    private final int baseRank;

    public WarningType(String category, String name, int baseRank) {
        this.category = category;
        this.name = name;
        this.baseRank = baseRank;
    }
    
    public WarningType(WarningDefinition def) {
        this(def.category(), def.name(), def.baseRank());
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public int getBaseRank() {
        return baseRank;
    }

    @Override
    public String toString() {
        return "WarningType [category=" + category + ", name=" + name + ", baseRank=" + baseRank + "]";
    }
}