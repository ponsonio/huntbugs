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
package one.util.huntbugs.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.ast.Expression;
import com.strobel.decompiler.ast.Variable;

/**
 * @author lan
 *
 */
public class Types {
    private static final Set<String> SIDE_EFFECT_FREE_TYPES = new HashSet<>(Arrays.asList("java/lang/Integer", "java/lang/Long",
        "java/lang/Short", "java/lang/Double", "java/lang/Byte", "java/lang/Character", "java/lang/Boolean", "java/lang/Float",
        "java/lang/Math"));

	public static List<TypeReference> getBaseTypes(TypeReference input) {
		List<TypeReference> result = new ArrayList<>();
		while (true) {
			result.add(input);
			TypeDefinition td = input.resolve();
			if (td == null)
				break;
			input = td.getBaseType();
			if (input == null)
				break;
		}
		Collections.reverse(result);
		return result;
	}
	
	public static boolean isInstance(TypeReference type, TypeReference wantedType) {
	    return isInstance(type, wantedType.getInternalName());
	}

	public static boolean isInstance(TypeReference type, String wantedType) {
	    if(type == null)
	        return false;
	    if(type.getInternalName().equals(wantedType))
	        return true;
	    TypeDefinition td = type.resolve();
	    if(td == null)
	        return false;
	    for(TypeReference iface : td.getExplicitInterfaces()) {
	        if(isInstance(iface, wantedType))
	            return true;
	    }
	    TypeReference bt = td.getBaseType();
	    if(bt == null)
	        return false;
	    return isInstance(bt, wantedType);
	}

	public static boolean isRandomClass(TypeReference type) {
	    String typeName = type.getInternalName();
		return typeName.equals("java/util/Random") || typeName.equals("java/security/SecureRandom") ||
		        typeName.equals("java/util/concurrent/ThreadLocalRandom") || typeName.equals("java/util/SplittableRandom");
	}

    public static TypeReference getExpressionType(Expression expr) {
        TypeReference exprType = expr.getInferredType();
        if(expr.getOperand() instanceof Variable) {
            Variable var = (Variable)expr.getOperand();
            exprType = var.getType();
            if(var.getOriginalParameter() != null)
                exprType = var.getOriginalParameter().getParameterType();
        }
        return exprType;
    }

    public static boolean isSideEffectFreeType(TypeReference type) {
        return SIDE_EFFECT_FREE_TYPES.contains(type.getInternalName());
    }

}