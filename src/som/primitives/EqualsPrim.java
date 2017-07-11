package som.primitives;

import java.math.BigInteger;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.vm.Universe;
import som.vmobjects.SSymbol;


@GenerateNodeFactory
public abstract class EqualsPrim extends BinaryExpressionNode {

  private final DynamicObject trueObject;
  private final DynamicObject falseObject;

  public EqualsPrim(final Universe universe) {
    this.trueObject = universe.getTrueObject();
    this.falseObject = universe.getFalseObject();
  }

  @Specialization
  public final boolean doBoolean(final boolean left, final boolean right) {
    return left == right;
  }

  @Specialization
  public final boolean doBoolean(final boolean left, final DynamicObject right) {
    return (left && right == trueObject) ||
        (!left && right == falseObject);
  }

  @Specialization
  public final boolean doLong(final long left, final long right) {
    return left == right;
  }

  @Specialization
  public final boolean doBigInteger(final BigInteger left, final BigInteger right) {
    return left.compareTo(right) == 0;
  }

  @Specialization
  public final boolean doString(final String receiver, final String argument) {
    return receiver.equals(argument);
  }

  @Specialization
  public final boolean doDouble(final double left, final double right) {
    return left == right;
  }

  @Specialization
  public final boolean doSSymbol(final SSymbol left, final SSymbol right) {
    return left == right;
  }

  @Specialization
  public final boolean doLong(final long left, final double right) {
    return left == right;
  }

  @Specialization
  public final boolean doBigInteger(final BigInteger left, final long right) {
    return doBigInteger(left, BigInteger.valueOf(right));
  }

  @Specialization
  public final boolean doLong(final long left, final BigInteger right) {
    return doBigInteger(BigInteger.valueOf(left), right);
  }

  @Specialization
  public final boolean doDouble(final double left, final long right) {
    return doDouble(left, (double) right);
  }

  @Specialization
  public final boolean doString(final String receiver, final SSymbol argument) {
    return receiver.equals(argument.getString());
  }

  @Specialization
  public final boolean doSSymbol(final SSymbol receiver, final String argument) {
    return receiver.getString().equals(argument);
  }

  @Specialization
  public final boolean doLong(final long left, final String right) {
    return false;
  }

  @Specialization
  public final boolean doLong(final long left, final DynamicObject right) {
    return false;
  }

  @Specialization
  public final boolean doLong(final long left, final SSymbol right) {
    return false;
  }

  @Specialization
  public final boolean doString(final String receiver, final long argument) {
    return false;
  }

  @Specialization
  public final boolean doString(final String receiver, final DynamicObject argument) {
    return false;
  }

  @Specialization
  public final boolean doSSymbol(final SSymbol receiver, final long argument) {
    return false;
  }

  @Specialization
  public final boolean doSSymbol(final SSymbol receiver, final DynamicObject argument) {
    return false;
  }
}
