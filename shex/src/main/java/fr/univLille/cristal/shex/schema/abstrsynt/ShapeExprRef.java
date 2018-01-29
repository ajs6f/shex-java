/**
Copyright 2017 University of Lille

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/




package fr.univLille.cristal.shex.schema.abstrsynt;

import fr.univLille.cristal.shex.schema.ShapeExprLabel;
import fr.univLille.cristal.shex.schema.analysis.ShapeExpressionVisitor;

/**
 * 
 * @author Iovka Boneva
 * 10 oct. 2017
 */
public class ShapeExprRef extends ShapeExpr {
	
	private final ShapeExprLabel label;
	private ShapeExpr def;
	
	public ShapeExprRef(ShapeExprLabel label) {
		this.label = label;
	}

	public ShapeExprLabel getLabel () {
		return this.label;
	}
	

	@Override
	public <ResultType> void accept(ShapeExpressionVisitor<ResultType> visitor, Object... arguments) {
		visitor.visitShapeExprRef(this, arguments);
	}

	
	// FIXME : implement as an instrumentation
	public void setShapeDefinition(ShapeExpr def) {
		if (this.def != null)
			throw new IllegalStateException("Shape definition can be set at most once");
		this.def = def;
	}
	
	public ShapeExpr getShapeDefinition () {
		return this.def;
	}
		
	@Override
	public String toString() {
		return "@"+label.toString();
	}
}
