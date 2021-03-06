/*
 * Copyright 2016 HuntBugs contributors
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
package one.util.huntbugs.util;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.VariableDefinition;

/**
 * @author Tagir Valeev
 *
 */
public class Variables {
    static final Field variableMethodDefinitionField;

    static {
        variableMethodDefinitionField = AccessController.doPrivileged((PrivilegedAction<Field>) () -> {
            try {
                Field f = VariableDefinition.class.getDeclaredField("_declaringMethod");
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException | SecurityException e) {
                throw new InternalError(e);
            }
        });
    }

    public static MethodDefinition getMethodDefinition(VariableDefinition vd) {
        try {
            return (MethodDefinition) variableMethodDefinitionField.get(vd);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InternalError(e);
        }
    }

}
