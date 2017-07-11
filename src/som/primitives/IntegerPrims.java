package som.primitives;

import java.math.BigInteger;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.primitives.arithmetic.ArithmeticPrim;
import som.vm.Universe;
import som.vmobjects.SArray;
import som.vmobjects.SSymbol;


public abstract class IntegerPrims {

  @GenerateNodeFactory
  public abstract static class RandomPrim extends UnaryExpressionNode {
    @Specialization
    public final long doLong(final long receiver) {
      return (long) (receiver * Math.random());
    }
  }

  @GenerateNodeFactory
  public abstract static class As32BitSignedValue extends UnaryExpressionNode {
    @Specialization
    public final long doLong(final long receiver) {
      return (int) receiver;
    }
  }

  @GenerateNodeFactory
  public abstract static class As32BitUnsignedValue extends UnaryExpressionNode {
    @Specialization
    public final long doLong(final long receiver) {
      return Integer.toUnsignedLong((int) receiver);
    }
  }

  @GenerateNodeFactory
  public abstract static class FromStringPrim extends ArithmeticPrim {
    protected final DynamicObject integerClass;

    public FromStringPrim(final Universe universe) {
      this.integerClass = universe.integerClass;
    }

    @Specialization(guards = "receiver == integerClass")
    public final Object doSClass(final DynamicObject receiver, final String argument) {
      return Long.parseLong(argument);
    }

    @Specialization(guards = "receiver == integerClass")
    public final Object doSClass(final DynamicObject receiver, final SSymbol argument) {
      return Long.parseLong(argument.getString());
    }
  }

  @GenerateNodeFactory
  public abstract static class LeftShiftPrim extends ArithmeticPrim {
    private final BranchProfile overflow = BranchProfile.create();

    @Specialization(rewriteOn = ArithmeticException.class)
    public final long doLong(final long receiver, final long right) {
      assert right >= 0; // currently not defined for negative values of right

      if (Long.SIZE - Long.numberOfLeadingZeros(receiver) + right > Long.SIZE - 1) {
        overflow.enter();
        throw new ArithmeticException("shift overflows long");
      }
      return receiver << right;
    }

    @Specialization
    public final BigInteger doLongWithOverflow(final long receiver, final long right) {
      assert right >= 0; // currently not defined for negative values of right
      assert right <= Integer.MAX_VALUE;

      return BigInteger.valueOf(receiver).shiftLeft((int) right);
    }
  }

  @GenerateNodeFactory
  public abstract static class UnsignedRightShiftPrim extends ArithmeticPrim {
    @Specialization
    public final long doLong(final long receiver, final long right) {
      return receiver >>> right;
    }
  }

  @GenerateNodeFactory
  public abstract static class MaxIntPrim extends ArithmeticPrim {
    @Specialization
    public final long doLong(final long receiver, final long right) {
      return Math.max(receiver, right);
    }
  }

  @GenerateNodeFactory
  public abstract static class ToPrim extends BinaryExpressionNode {
    @Specialization
    public final SArray doLong(final long receiver, final long right) {
      int cnt = (int) right - (int) receiver + 1;
      long[] arr = new long[cnt];
      for (int i = 0; i < cnt; i++) {
        arr[i] = i + receiver;
      }
      return SArray.create(arr);
    }
  }

  @GenerateNodeFactory
  public abstract static class AbsPrim extends UnaryExpressionNode {
    @Specialization
    public final long doLong(final long receiver) {
      return Math.abs(receiver);
    }
  }
}
