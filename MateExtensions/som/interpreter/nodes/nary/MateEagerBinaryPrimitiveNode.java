package som.interpreter.nodes.nary;

import som.interpreter.nodes.ExpressionNode;
import som.matenodes.MateAbstractReflectiveDispatch.MateAbstractStandardDispatch;
import som.matenodes.MateAbstractSemanticNodes.MateSemanticCheckNode;
import som.matenodes.MateBehavior;
import som.vm.MateSemanticsException;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.frame.VirtualFrame;


public class MateEagerBinaryPrimitiveNode extends EagerBinaryPrimitiveNode implements MateBehavior {
  @Child MateSemanticCheckNode                   semanticCheck;
  @Child MateAbstractStandardDispatch     reflectiveDispatch;
  
  public MateEagerBinaryPrimitiveNode(SSymbol selector, ExpressionNode receiver, ExpressionNode argument,
      BinaryExpressionNode primitive) {
    super(selector, receiver, argument, primitive);
    this.initializeMateSemantics(this.getSourceSection(), this.reflectiveOperation());
    this.initializeMateDispatchForMessages(this.getSourceSection(), this.getSelector());
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    Object rcvr = this.getReceiver().executeGeneric(frame);
    Object arg  = this.getArgument().executeGeneric(frame);
    try{
      return this.doMateSemantics(frame, new Object[] {rcvr, arg});
    } catch(MateSemanticsException e){
      return executeEvaluated(frame, rcvr, arg);
    }
  }
  
  @Override
  public MateSemanticCheckNode getMateNode() {
    return semanticCheck;
  }

  @Override
  public MateAbstractStandardDispatch getMateDispatch() {
    return reflectiveDispatch;
  }
  
  @Override
  public void setMateNode(MateSemanticCheckNode node) {
    semanticCheck = node;
  }

  @Override
  public void setMateDispatch(MateAbstractStandardDispatch node) {
    reflectiveDispatch = node;
  }
}