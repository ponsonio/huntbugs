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
package one.util.huntbugs.detect;

import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.ast.AstCode;
import com.strobel.decompiler.ast.Expression;

import one.util.huntbugs.flow.ValuesFlow;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.AstExpressionVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;
import one.util.huntbugs.util.Types;
import one.util.huntbugs.warning.WarningAnnotation;

/**
 * @author lan
 *
 */
@WarningDefinition(category = "RedundantCode", name = "UnnecessaryInstanceOf", maxScore = 60)
@WarningDefinition(category = "RedundantCode", name = "UnnecessaryInstanceOfInferred", maxScore = 70)
public class UnnecessaryInstanceOf {
    @AstExpressionVisitor
    public void visit(Expression node, MethodContext mc, MethodDefinition md) {
        if(node.getCode() == AstCode.InstanceOf) {
            TypeReference typeRef = (TypeReference)node.getOperand();
            Expression expr = node.getArguments().get(0);
            TypeReference exprType = Types.getExpressionType(expr);
			if(Types.isInstance(exprType, typeRef)) {
                mc.report("UnnecessaryInstanceOf", 0, expr, new WarningAnnotation<>("INSTANCEOF_TYPE", typeRef.getFullName()), 
                    new WarningAnnotation<>("ACTUAL_TYPE", exprType.getFullName()));
            } else {
                TypeReference inferredType = ValuesFlow.reduceType(expr);
                if(typeRef != null && Types.isInstance(inferredType, typeRef)) {
                    mc.report("UnnecessaryInstanceOfInferred", 0, expr, new WarningAnnotation<>("INSTANCEOF_TYPE", typeRef.getFullName()),
                        new WarningAnnotation<>("ACTUAL_TYPE", exprType.getFullName()), new WarningAnnotation<>("INFERRED_TYPE", inferredType.getFullName()));
                }
            }
        }
    }

}