package parseunit.entity.astnode.expression;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Type;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.util.MapUtil;
import parseunit.util.astnode.CBASTNodeBuilder;

public class CBCastExpression extends CBExpression {
	private Type type;
	private CBExpression expression;
	
	public CBCastExpression(CastExpression n) {
		super(n);
		type = n.getType();
		expression = (CBExpression) CBASTNodeBuilder.build(n.getExpression());
	}

	/* (non-Javadoc)
	 * @see parseunit.entity.astnode.CBASTNode#mapTokens(parseunit.entity.astnode.AbstractCBASTNode, java.util.Map)
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String,List> tokenMap,
			Map<String,List<ASTNodeMappingElement>> nodemap, ASTNodeMappingElement e) {
		// ASTNode type not match
		if(! (tar instanceof CBCastExpression)){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		
		CBCastExpression temTar = (CBCastExpression)tar;
		
		// map type
		MapUtil.addTokenMapping(tokenMap, type.toString(), temTar.getType().toString()
				,nodemap,e);
		
		// map expression
		expression.mapTokens(temTar.getExpression(), tokenMap,nodemap,e);
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
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return the expression
	 */
	public CBExpression getExpression() {
		return expression;
	}
	
	
	
	
}
