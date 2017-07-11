package som.primitives;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.primitives.SystemPrims.UnarySystemNode;
import som.vm.Universe;


public abstract class DoublePrims {

  @GenerateNodeFactory
  public abstract static class RoundPrim extends UnaryExpressionNode {
    @Specialization
    public final long doDouble(final double receiver) {
      return Math.round(receiver);
    }
  }

  @GenerateNodeFactory
  public abstract static class AsIntegerPrim extends UnaryExpressionNode {
    @Specialization
    public final long doDouble(final double receiver) {
      return (long) receiver;
    }
  }

  @GenerateNodeFactory
  public abstract static class PositiveInfinityPrim extends UnarySystemNode {
    public PositiveInfinityPrim(final Universe universe) {
      super(universe);
    }

    @Specialization(guards = "receiver == universe.doubleClass")
    public final double doSClass(final DynamicObject receiver) {
      return Double.POSITIVE_INFINITY;
    }
  }
}
