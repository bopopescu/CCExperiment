package org.apache.xalan.xsltc.compiler.util;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.Visitor;
abstract class MarkerInstruction extends Instruction {
    public MarkerInstruction() {
        super(Constants.UNDEFINED, (short) 0);
    }
    public void accept(Visitor v) {
    }
    final public int consumeStack(ConstantPoolGen cpg) {
        return 0;
    }
    final public int produceStack(ConstantPoolGen cpg) {
        return 0;
    }
    public Instruction copy() {
        return this;
    }
    final public void dump(DataOutputStream out) throws IOException {
    }
}
