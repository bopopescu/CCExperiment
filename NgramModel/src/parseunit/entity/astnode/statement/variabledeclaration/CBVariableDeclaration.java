package parseunit.entity.astnode.statement.variabledeclaration;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.entity.astnode.CBASTNode;
import parseunit.entity.astnode.expression.CBExpression;
import parseunit.util.MapUtil;
import parseunit.util.astnode.CBASTNodeBuilder;

/**
 * TODO need test
 * @author guzuxing
 *
 */
public class CBVariableDeclaration extends CBASTNode{
	protected String name;
	protected CBExpression initializer;
	public CBVariableDeclaration(VariableDeclaration n) {
		super(n);
		name = n.getName().getFullyQualifiedName();
		if(n.getInitializer() != null)
			initializer = (CBExpression) CBASTNodeBuilder.build(n.getInitializer());
	}
	

	
	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#toCBString()
	 */
	@Override
	public String toCBString() {
		// TODO Auto-generated method stub
		return super.toCBString();
	}

	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the initializer
	 */
	public CBExpression getInitializer() {
		return initializer;
	}

	
}