package parseunit.entity.astnode.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import parseunit.entity.ASTNodeMappingElement;
import parseunit.entity.astnode.AbstractCBASTNode;
import parseunit.entity.astnode.statement.CBVariableDeclarationStatement;
import parseunit.entity.astnode.statement.variabledeclaration.CBVariableDeclaratrionFragment;
import parseunit.util.MapUtil;
import parseunit.util.astnode.CBASTNodeBuilder;

/**
 * TODO need test
 * @author guzuxing
 *
 */
public class CBVariableDeclarationExpression extends CBExpression {
	private Type type;
	private List<CBVariableDeclaratrionFragment> fragmentList;
	public CBVariableDeclarationExpression(VariableDeclarationExpression n) {
		super(n);
		type = n.getType();
		fragmentList = new ArrayList<>();
		for(int i = 0;i<n.fragments().size();i++){
			fragmentList.add(
					(CBVariableDeclaratrionFragment) CBASTNodeBuilder.build(
							(VariableDeclarationFragment)n.fragments().get(i)));
					
//					new CBVariableDeclaratrionFragment(
//					(VariableDeclarationFragment) n.fragments().get(i)));
		}
	}
	/**
	 * VariableDeclarationStatement mapTokens
	 * map type
	 * map fragmentList
	 */
	@Override
	public void mapTokens(AbstractCBASTNode tar, Map<String,List> tokenMap,
			Map<String,List<ASTNodeMappingElement>> nodemap, ASTNodeMappingElement e) {
		if(! (tar instanceof CBVariableDeclarationExpression) ){
			MapUtil.addTokenMapping(tokenMap,toCBString(),tar.toCBString()
					,nodemap,e);
			return;
		}
		
		CBVariableDeclarationExpression tarTem = (CBVariableDeclarationExpression)tar;
		// map type
		MapUtil.addTokenMapping(tokenMap, type.toString(), tarTem.getType().toString()
				,nodemap,e);
		// map fragments
		//TODO if size not match only compare the same size items
		int minSize = fragmentList.size();
		if(tarTem.fragmentList.size()<minSize)
			minSize = tarTem.fragmentList.size();
		for(int index = 0;index< minSize;index++){
			fragmentList.get(index).mapTokens(tarTem.fragmentList.get(index), tokenMap,
					nodemap,e);
		}
		
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
	 * @return the fragmentList
	 */
	public List<CBVariableDeclaratrionFragment> getFragmentList() {
		return fragmentList;
	}
}
