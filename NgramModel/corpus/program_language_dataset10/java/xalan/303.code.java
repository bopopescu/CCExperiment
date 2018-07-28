package org.apache.xalan.xsltc.compiler;
import java.util.Vector;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GOTO_W;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.xalan.xsltc.compiler.util.BooleanType;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.ErrorMsg;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodType;
import org.apache.xalan.xsltc.compiler.util.NodeSetType;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
abstract class Expression extends SyntaxTreeNode {
    protected Type _type;
    protected FlowList _trueList = new FlowList();
    protected FlowList _falseList = new FlowList();
    public Type getType() {
	return _type;
    }
    public abstract String toString();
    public boolean hasPositionCall() {
	return false;		
    }
    public boolean hasLastCall() {
	return false;
    }
    public Object evaluateAtCompileTime() {
	return null;
    }
    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	return typeCheckContents(stable);
    }
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	ErrorMsg msg = new ErrorMsg(ErrorMsg.NOT_IMPLEMENTED_ERR,
				    getClass(), this);
	getParser().reportError(FATAL, msg);
    }
    public final InstructionList compile(ClassGenerator classGen,
					 MethodGenerator methodGen) {
	final InstructionList result, save = methodGen.getInstructionList();
	methodGen.setInstructionList(result = new InstructionList());
	translate(classGen, methodGen);
	methodGen.setInstructionList(save);
	return result;
    }
    public void translateDesynthesized(ClassGenerator classGen,
				       MethodGenerator methodGen) {
	translate(classGen, methodGen);
	if (_type instanceof BooleanType) {
	    desynthesize(classGen, methodGen);
	}
    }
    public void startIterator(ClassGenerator classGen,
				   MethodGenerator methodGen) {
	if (_type instanceof NodeSetType == false) {
	    return;
	}
	Expression expr = this;
	if (expr instanceof CastExpr) {
	    expr = ((CastExpr) expr).getExpr();
	}
	if (expr instanceof VariableRefBase == false) {
	    final InstructionList il = methodGen.getInstructionList();
	    il.append(methodGen.loadContextNode());
	    il.append(methodGen.setStartNode());
	}
    }
    public void synthesize(ClassGenerator classGen, MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();
	_trueList.backPatch(il.append(ICONST_1));
	final BranchHandle truec = il.append(new GOTO_W(null));
	_falseList.backPatch(il.append(ICONST_0));
	truec.setTarget(il.append(NOP));
    }
    public void desynthesize(ClassGenerator classGen,
			     MethodGenerator methodGen) {
	final InstructionList il = methodGen.getInstructionList();
	_falseList.add(il.append(new IFEQ(null)));
    }
    public FlowList getFalseList() {
	return _falseList;
    }
    public FlowList getTrueList() {
	return _trueList;
    }
    public void backPatchFalseList(InstructionHandle ih) {
	_falseList.backPatch(ih);
    }
    public void backPatchTrueList(InstructionHandle ih) {
	_trueList.backPatch(ih);
    }
    public MethodType lookupPrimop(SymbolTable stable, String op,
				   MethodType ctype) {
	MethodType result = null;
	final Vector primop = stable.lookupPrimop(op);
	if (primop != null) {
	    final int n = primop.size();
	    int minDistance = Integer.MAX_VALUE;
	    for (int i = 0; i < n; i++) {
		final MethodType ptype = (MethodType) primop.elementAt(i);
		if (ptype.argsCount() != ctype.argsCount()) {
		    continue;
		}
		if (result == null) {
		    result = ptype;		
		}
		final int distance = ctype.distanceTo(ptype);
		if (distance < minDistance) {
		    minDistance = distance;
		    result = ptype;
		}
	    }		
	}	
	return result;
    }	
}