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
package one.util.huntbugs.detect;

import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.ast.AstCode;
import com.strobel.decompiler.ast.Expression;

import one.util.huntbugs.flow.Inf;
import one.util.huntbugs.flow.ValuesFlow;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.AstNodes;
import one.util.huntbugs.registry.anno.AstVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;
import one.util.huntbugs.util.Exprs;
import one.util.huntbugs.util.NodeChain;
import one.util.huntbugs.util.Nodes;
import one.util.huntbugs.util.Types;
import one.util.huntbugs.warning.Roles;

/**
 * @author Tagir Valeev
 *
 */
@WarningDefinition(category = "Performance", name = "WrongMapIterator", maxScore = 48)
@WarningDefinition(category = "Performance", name = "WrongMapIteratorValues", maxScore = 55)
public class WrongMapIterator {
    @AstVisitor(nodes = AstNodes.EXPRESSIONS)
    public void visit(Expression expr, NodeChain nc, MethodContext mc) {
        MethodReference getMr = getCalledMethod(expr);
        if (getMr == null || !getMr.getName().equals("get"))
            return;
        Expression mapArg = Exprs.getChildNoSpecial(expr, 0);
        TypeReference type = ValuesFlow.reduceType(Exprs.getChild(expr, 0));
        if (!Types.isInstance(type, "java/util/Map") || Types.isInstance(type, "java/util/EnumMap"))
            return;
        Expression key = Exprs.getChild(expr, 1);
        while (key.getCode() == AstCode.CheckCast || Nodes.isBoxing(key) || Nodes.isUnboxing(key))
            key = Exprs.getChild(key, 0);
        MethodReference nextMr = getCalledMethod(key);
        if (nextMr == null || !nextMr.getName().equals("next") || !Types.is(nextMr.getDeclaringType(), Iterator.class))
            return;
        Expression iter = Exprs.getChild(key, 0);
        MethodReference iterMr = getCalledMethod(iter);
        if (iterMr == null || !iterMr.getName().equals("iterator"))
            return;
        Expression keySet = Exprs.getChild(iter, 0);
        MethodReference keySetMr = getCalledMethod(keySet);
        if (keySetMr == null || !keySetMr.getName().equals("keySet"))
            return;
        if (!Nodes.isEquivalent(mapArg, Exprs.getChildNoSpecial(keySet, 0)))
            return;
        int priority = nc.isInLoop() ? 0 : 15;
        if(mc.isAnnotated() && usedForGetOnly(key, mapArg)) {
            mc.report("WrongMapIteratorValues", priority, expr, Roles.REPLACEMENT_METHOD.create("java/util/Map", "values", "()Ljava/util/Collection;"));
        } else {
            mc.report("WrongMapIterator", priority, expr, Roles.REPLACEMENT_METHOD.create("java/util/Map", "entrySet", "()Ljava/util/Set;"));
        }
    }
    
    private static boolean usedForGetOnly(Expression key, Expression mapArg) {
        HashSet<Expression> usages = Inf.BACKLINK.findTransitiveUsages(key, true).collect(Collectors.toCollection(HashSet::new));
        while(!usages.isEmpty()) {
            Expression usage = usages.iterator().next();
            usages.remove(usage);
            if(usage.getCode() == AstCode.CheckCast || Nodes.isBoxing(usage) || Nodes.isUnboxing(usage)) {
                Inf.BACKLINK.findTransitiveUsages(usage, true).forEach(usages::add);
            } else {
                MethodReference getMr = getCalledMethod(usage);
                if (getMr == null || !getMr.getName().equals("get"))
                    return false;
                if(!Nodes.isEquivalent(Exprs.getChildNoSpecial(usage, 0), mapArg))
                    return false;
            }
        }
        return true;
    }

    private static MethodReference getCalledMethod(Expression expr) {
        return (expr.getCode() == AstCode.InvokeVirtual || expr.getCode() == AstCode.InvokeInterface)
                ? (MethodReference) expr.getOperand() : null;
    }
}
