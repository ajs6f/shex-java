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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.univLille.cristal.shex.graph.TCProperty;
import fr.univLille.cristal.shex.schema.ShapeExprLabel;
import fr.univLille.cristal.shex.schema.analysis.ShapeExpressionVisitor;

/**
 * 
 * @author Iovka Boneva
 * 10 oct. 2017
 */
public class Shape extends ShapeExpr {
	private boolean closed;
	private Set<TCProperty> extra;
	private TripleExpr tripleExpr;
	/** Creates a {@link Shape} that is forward closed, inverse open, and has no extra properties.
	 * 
	 * @param tripleExpression
	 */
	protected Shape (TripleExpr tripleExpression) {
		this(tripleExpression, Collections.emptySet(), true);
	}
	
	public Shape(TripleExpr tripleExpression, Set<TCProperty> extraProps, boolean closed) {
		this.tripleExpr = tripleExpression;
		this.extra = Collections.unmodifiableSet(new HashSet<>(extraProps));
		this.closed = closed;
	}
	
	public TripleExpr getTripleExpression () {
		return tripleExpr;
	}
	
	public boolean isClosed () {
		return this.closed;
	}
	
	public Set<TCProperty> getExtraProperties () {
		return extra;
	}
	
	@Override
	public <ResultType> void accept(ShapeExpressionVisitor<ResultType> visitor, Object... arguments) {
		visitor.visitShape(this, arguments);
	}
	
	@Override
	public Object toJsonLD() {
		Map<String,Object> jsonObject = new LinkedHashMap<String,Object>();
		jsonObject.put("type", "Shape");
		if (! this.id.isGenerated()) {
			jsonObject.put("id", this.id.toString());
		}
		if (this.getExtraProperties().size()>0) {
			List<Object> extraprops = new LinkedList<Object>();
			for (TCProperty ex:this.getExtraProperties()) {
				extraprops.add(ex.toJsonLD());
			}
			jsonObject.put("extra", extraprops);
		}
		jsonObject.put("expression", this.tripleExpr.toJsonLD());
		return jsonObject;
	}

	@Override
	public String toString() {
		String closed = isClosed() ? "CLOSED" : "";
		String extraP = extra.isEmpty() ? "" : "EXTRA" + extra.toString();
		return String.format("(%s %s %s)", closed, extraP, tripleExpr);
	}

	
}