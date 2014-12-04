package som.interpreter.nodes;

import static som.interpreter.SNodeFactory.createLocalVariableWrite;
import static som.interpreter.TruffleCompiler.transferToInterpreter;
import som.compiler.Variable;
import som.compiler.Variable.Local;
import som.interpreter.Inliner;
import som.interpreter.nodes.LocalVariableNodeFactory.LocalSuperReadNodeFactory;
import som.vm.constants.Nil;
import som.vmobjects.SObject;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;


public abstract class LocalVariableNode extends ExpressionNode {
  protected final FrameSlot slot;

  private LocalVariableNode(final FrameSlot slot, final SourceSection source) {
    super(source);
    this.slot = slot;
  }

  public final Object getSlotIdentifier() {
    return slot.getIdentifier();
  }

  public abstract static class LocalVariableReadNode extends LocalVariableNode {

    public LocalVariableReadNode(final Variable variable,
        final SourceSection source) {
      this(variable.slot, source);
    }

    public LocalVariableReadNode(final LocalVariableReadNode node) {
      this(node.slot, node.getSourceSection());
    }

    public LocalVariableReadNode(final FrameSlot slot,
        final SourceSection source) {
      super(slot, source);
    }

    @Specialization(guards = "isUninitialized")
    public final SObject doNil() {
      return Nil.nilObject;
    }

    @Specialization(guards = "isInitialized", rewriteOn = {FrameSlotTypeException.class})
    public final boolean doBoolean(final VirtualFrame frame) throws FrameSlotTypeException {
      return frame.getBoolean(slot);
    }


    @Specialization(guards = "isInitialized", rewriteOn = {FrameSlotTypeException.class})
    public final long doLong(final VirtualFrame frame) throws FrameSlotTypeException {
      return frame.getLong(slot);
    }

    @Specialization(guards = "isInitialized", rewriteOn = {FrameSlotTypeException.class})
    public final double doDouble(final VirtualFrame frame) throws FrameSlotTypeException {
      return frame.getDouble(slot);
    }

    @Specialization(guards = "isInitialized", rewriteOn = {FrameSlotTypeException.class})
    public final Object doObject(final VirtualFrame frame) throws FrameSlotTypeException {
      return CompilerDirectives.unsafeCast(frame.getObject(slot),
          Object.class, true, true);
    }

//    @Generic
//    public final Object doGeneric(final VirtualFrame frame) {
//      assert isInitialized();
//      return FrameUtil.getObjectSafe(frame, slot);
//    }

    protected final boolean isInitialized() {
      return slot.getKind() != FrameSlotKind.Illegal;
    }

    protected final boolean isUninitialized() {
      return slot.getKind() == FrameSlotKind.Illegal;
    }

    @Override
    public void replaceWithIndependentCopyForInlining(final Inliner inliner) {
      throw new RuntimeException("Should not be part of an uninitalized tree. And this should only be done with uninitialized trees.");
    }
  }

  public abstract static class LocalSuperReadNode
                       extends LocalVariableReadNode implements ISuperReadNode {
    private final SSymbol holderClass;
    private final boolean isClassSide;

    public LocalSuperReadNode(final Variable variable, final SSymbol holderClass,
        final boolean isClassSide, final SourceSection source) {
      this(variable.slot, holderClass, isClassSide, source);
    }

    public LocalSuperReadNode(final FrameSlot slot, final SSymbol holderClass,
        final boolean isClassSide, final SourceSection source) {
      super(slot, source);
      this.holderClass = holderClass;
      this.isClassSide = isClassSide;
    }

    public LocalSuperReadNode(final LocalSuperReadNode node) {
      this(node.slot, node.holderClass, node.isClassSide,
          node.getSourceSection());
    }

    @Override
    public final SSymbol getHolderClass() { return holderClass; }
    @Override
    public final boolean isClassSide()    { return isClassSide; }

    @Override
    public void replaceWithIndependentCopyForInlining(final Inliner inliner) {
      FrameSlot slot = inliner.getLocalFrameSlot(this.slot.getIdentifier());
      assert slot != null;
      replace(LocalSuperReadNodeFactory.create(slot, holderClass, isClassSide, getSourceSection()));
    }
  }

  @NodeChild(value = "exp", type = ExpressionNode.class)
  public abstract static class LocalVariableWriteNode extends LocalVariableNode {

    public LocalVariableWriteNode(final Local variable, final SourceSection source) {
      super(variable.slot, source);
    }

    public LocalVariableWriteNode(final LocalVariableWriteNode node) {
      super(node.slot, node.getSourceSection());
    }

    public LocalVariableWriteNode(final FrameSlot slot, final SourceSection source) {
      super(slot, source);
    }

    public abstract ExpressionNode getExp();

    @Specialization(guards = "isBoolKind")
    public final boolean writeBoolean(final VirtualFrame frame, final boolean expValue) {
      frame.setBoolean(slot, expValue);
      return expValue;
    }

    @Specialization(guards = "isLongKind")
    public final long writeLong(final VirtualFrame frame, final long expValue) {
      frame.setLong(slot, expValue);
      return expValue;
    }

    @Specialization(guards = "isDoubleKind")
    public final double writeDouble(final VirtualFrame frame, final double expValue) {
      frame.setDouble(slot, expValue);
      return expValue;
    }

    @Specialization(contains = {"writeBoolean", "writeLong", "writeDouble"})
    public final Object writeGeneric(final VirtualFrame frame, final Object expValue) {
      ensureObjectKind();
      frame.setObject(slot, expValue);
      return expValue;
    }

    protected final boolean isBoolKind() {
      if (slot.getKind() == FrameSlotKind.Boolean) {
        return true;
      }
      if (slot.getKind() == FrameSlotKind.Illegal) {
        transferToInterpreter("LocalVar.writeBoolToUninit");
        slot.setKind(FrameSlotKind.Boolean);
        return true;
      }
      return false;
    }

    protected final boolean isLongKind() {
      if (slot.getKind() == FrameSlotKind.Long) {
        return true;
      }
      if (slot.getKind() == FrameSlotKind.Illegal) {
        transferToInterpreter("LocalVar.writeIntToUninit");
        slot.setKind(FrameSlotKind.Long);
        return true;
      }
      return false;
    }

    protected final boolean isDoubleKind() {
      if (slot.getKind() == FrameSlotKind.Double) {
        return true;
      }
      if (slot.getKind() == FrameSlotKind.Illegal) {
        transferToInterpreter("LocalVar.writeDoubleToUninit");
        slot.setKind(FrameSlotKind.Double);
        return true;
      }
      return false;
    }

    protected final void ensureObjectKind() {
      if (slot.getKind() != FrameSlotKind.Object) {
        transferToInterpreter("LocalVar.writeObjectToUninit");
        slot.setKind(FrameSlotKind.Object);
      }
    }

    @Override
    public final void replaceWithIndependentCopyForInlining(final Inliner inliner) {
      CompilerAsserts.neverPartOfCompilation("replaceWithIndependentCopyForInlining");

      if (getParent() instanceof ArgumentInitializationNode) {
        FrameSlot varSlot = inliner.getLocalFrameSlot(getSlotIdentifier());
        assert varSlot != null;
        replace(createLocalVariableWrite(varSlot, getExp(), getSourceSection()));
      } else {
        throw new RuntimeException("Should not be part of an uninitalized tree. And this should only be done with uninitialized trees.");
      }
    }
  }
}
