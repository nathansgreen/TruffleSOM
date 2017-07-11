package som.primitives;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;

import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vmobjects.SClass;
import som.vmobjects.SObject;


@GenerateNodeFactory
@ImportStatic(SClass.class)
public abstract class NewObjectPrim extends UnaryExpressionNode {
  @Specialization(guards = "receiver == cachedClass")
  public final DynamicObject cachedClass(final DynamicObject receiver,
      @Cached("receiver") final DynamicObject cachedClass,
      @Cached("getFactory(cachedClass)") final DynamicObjectFactory factory) {
    return factory.newInstance();
  }

  @Specialization(replaces = "cachedClass")
  public final DynamicObject uncached(final DynamicObject receiver) {
    return SObject.create(receiver);
  }
}
