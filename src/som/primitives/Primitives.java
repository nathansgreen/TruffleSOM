/**
 * Copyright (c) 2013 Stefan Marr,   stefan.marr@vub.ac.be
 * Copyright (c) 2009 Michael Haupt, michael.haupt@hpi.uni-potsdam.de
 * Software Architecture Group, Hasso Plattner Institute, Potsdam, Germany
 * http://www.hpi.uni-potsdam.de/swa/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package som.primitives;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.object.DynamicObject;

import som.compiler.MethodGenerationContext;
import som.interpreter.Primitive;
import som.interpreter.SomLanguage;
import som.interpreter.nodes.ArgumentReadNode.LocalArgumentReadNode;
import som.interpreter.nodes.ExpressionNode;
import som.primitives.MethodPrimsFactory.InvokeOnPrimFactory;
import som.primitives.arrays.PutAllNodeFactory;
import som.primitives.arrays.ToArgumentsArrayNodeGen;
import som.vm.Universe;
import som.vmobjects.SInvokable;
import som.vmobjects.SInvokable.SMethod;
import som.vmobjects.SObject;
import som.vmobjects.SSymbol;


public abstract class Primitives {

  protected final Universe universe;
  protected DynamicObject  holder;
  protected final boolean  displayWarning;

  public Primitives(final boolean displayWarning, final Universe universe) {
    this.universe = universe;
    this.displayWarning = displayWarning;
  }

  public final void installPrimitivesIn(final DynamicObject value) {
    holder = value;

    // Install the primitives from this primitives class
    installPrimitives();
  }

  public abstract void installPrimitives();

  public static SInvokable constructPrimitive(final SSymbol signature,
      final NodeFactory<? extends ExpressionNode> nodeFactory,
      final Universe universe, final DynamicObject holder) {
    CompilerAsserts.neverPartOfCompilation();
    int numArgs = signature.getNumberOfSignatureArguments();

    MethodGenerationContext mgen = new MethodGenerationContext(null);
    ExpressionNode[] args = new ExpressionNode[numArgs];
    for (int i = 0; i < numArgs; i++) {
      args[i] = new LocalArgumentReadNode(i, null);
    }

    ExpressionNode primNode;
    switch (numArgs) {
      case 1:
        try {
          primNode = nodeFactory.createNode(args[0]);
        } catch (IllegalArgumentException e) {
          primNode = nodeFactory.createNode(universe, args[0]);
        }
        break;
      case 2:
        // HACK for node class where we use `executeWith`
        if (nodeFactory == PutAllNodeFactory.getInstance()) {
          primNode = nodeFactory.createNode(args[0], args[1],
              LengthPrimFactory.create(null));
        } else {
          try {
            primNode = nodeFactory.createNode(args[0], args[1]);
          } catch (IllegalArgumentException e) {
            primNode = nodeFactory.createNode(universe, args[0], args[1]);
          }
        }
        break;
      case 3:
        // HACK for node class where we use `executeWith`
        if (nodeFactory == InvokeOnPrimFactory.getInstance()) {
          primNode = nodeFactory.createNode(args[0], args[1], args[2],
              ToArgumentsArrayNodeGen.create(null, null));
        } else {
          try {
            primNode = nodeFactory.createNode(args[0], args[1], args[2]);
          } catch (IllegalArgumentException e) {
            primNode = nodeFactory.createNode(universe, args[0], args[1], args[2]);
          }
        }
        break;
      case 4:
        try {
          primNode = nodeFactory.createNode(args[0], args[1], args[2], args[3]);
        } catch (IllegalArgumentException e) {
          primNode = nodeFactory.createNode(universe, args[0], args[1], args[2], args[3]);
        }
        break;
      default:
        throw new RuntimeException("Not supported by SOM.");
    }

    Primitive primMethodNode =
        new Primitive(signature.getString(), primNode,
            mgen.getCurrentLexicalScope().getFrameDescriptor(),
            (ExpressionNode) primNode.deepCopy(), universe.getLanguage());
    SInvokable prim = Universe.newMethod(signature, primMethodNode, true, new SMethod[0]);
    return prim;
  }

  public static SInvokable constructEmptyPrimitive(final SSymbol signature,
      final SomLanguage lang) {
    CompilerAsserts.neverPartOfCompilation();
    MethodGenerationContext mgen = new MethodGenerationContext(null);

    ExpressionNode primNode = EmptyPrim.create(new LocalArgumentReadNode(0, null));
    Primitive primMethodNode =
        new Primitive(signature.getString(), primNode,
            mgen.getCurrentLexicalScope().getFrameDescriptor(),
            (ExpressionNode) primNode.deepCopy(), lang);
    SInvokable prim = Universe.newMethod(signature, primMethodNode, true, new SMethod[0]);
    return prim;
  }

  protected final void installInstancePrimitive(final String selector,
      final NodeFactory<? extends ExpressionNode> nodeFactory) {
    SSymbol signature = universe.symbolFor(selector);
    SInvokable prim = constructPrimitive(signature, nodeFactory, universe, holder);

    // Install the given primitive as an instance primitive in the holder class
    universe.sclass.addInstancePrimitive(holder, prim, displayWarning);
  }

  protected final void installClassPrimitive(final String selector,
      final NodeFactory<? extends ExpressionNode> nodeFactory) {
    SSymbol signature = universe.symbolFor(selector);
    SInvokable prim = constructPrimitive(signature, nodeFactory, universe, holder);

    // Install the given primitive as an instance primitive in the class of
    // the holder class
    universe.sclass.addInstancePrimitive(SObject.getSOMClass(holder), prim, displayWarning);
  }

  public static SInvokable getEmptyPrimitive(final String selector,
      final Universe universe) {
    SSymbol signature = universe.symbolFor(selector);
    return constructEmptyPrimitive(signature, universe.getLanguage());
  }
}
