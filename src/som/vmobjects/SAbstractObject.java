package som.vmobjects;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.object.DynamicObject;

import som.interpreter.SomLanguage;
import som.interpreter.Types;
import som.vm.Universe;


public abstract class SAbstractObject {

  public abstract DynamicObject getSOMClass(Universe universe);

  @Override
  public String toString() {
    CompilerAsserts.neverPartOfCompilation();
    Universe universe = SomLanguage.getCurrentContext();
    DynamicObject clazz = getSOMClass(universe);
    if (clazz == null) {
      return "an Object(clazz==null)";
    }
    return "a " + universe.sclass.getName(clazz).getString();
  }

  public static final Object send(
      final String selectorString,
      final Object[] arguments, final Universe universe) {
    CompilerAsserts.neverPartOfCompilation("SAbstractObject.send()");
    SSymbol selector = universe.symbolFor(selectorString);

    // Lookup the invokable
    SInvokable invokable =
        universe.sclass.lookupInvokable(Types.getClassOf(arguments[0], universe), selector);

    return invokable.invoke(arguments);
  }

  public static final Object sendUnknownGlobal(final Object receiver,
      final SSymbol globalName, final Universe universe) {
    Object[] arguments = {receiver, globalName};
    return send("unknownGlobal:", arguments, universe);
  }

  public static final Object sendEscapedBlock(final Object receiver,
      final SBlock block, final Universe universe) {
    Object[] arguments = {receiver, block};
    return send("escapedBlock:", arguments, universe);
  }

}
