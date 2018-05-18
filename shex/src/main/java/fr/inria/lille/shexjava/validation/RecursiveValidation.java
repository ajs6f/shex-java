/*******************************************************************************
 * Copyright (C) 2018 Université de Lille - Inria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.inria.lille.shexjava.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;

import fr.inria.lille.shexjava.graph.NeighborTriple;
import fr.inria.lille.shexjava.graph.RDFGraph;
import fr.inria.lille.shexjava.schema.Label;
import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.abstrsynt.NodeConstraint;
import fr.inria.lille.shexjava.schema.abstrsynt.Shape;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeAnd;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeExpr;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeExprRef;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeExternal;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeNot;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeOr;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleConstraint;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleExpr;
import fr.inria.lille.shexjava.schema.analysis.ShapeExpressionVisitor;
import fr.inria.lille.shexjava.util.Pair;


/** Implements the Recursive validation algorithm.
 * This algorithm will check only the shape definition necessary, but can return false positive.
 * 
 * @author Jérémie Dusart 
 */
public class RecursiveValidation implements ValidationAlgorithm {
	private RDFGraph graph;
	private SORBEGenerator sorbeGenerator;
	private ShexSchema schema;
	private RecursiveTyping typing;
	
	private DynamicCollectorOfTripleConstraint collectorTC;
	
	
	public RecursiveValidation(ShexSchema schema, RDFGraph graph) {
		super();
		this.graph = graph;
		this.sorbeGenerator = new SORBEGenerator();
		this.schema = schema;
		this.collectorTC = new DynamicCollectorOfTripleConstraint();
		this.typing = new RecursiveTyping();
	}
	
	public void resetTyping() {
		this.typing = new RecursiveTyping();
	}
	
	@Override
	public Typing getTyping() {
		return typing;
	}	
	
	@Override
	public boolean validate(RDFTerm focusNode, Label label) throws Exception {
		if (label == null || !schema.getShapeMap().containsKey(label))
		    throw new Exception("Unknown label: "+label);
		this.resetTyping();
		boolean result = recursiveValidation(focusNode,label);
		if (result) {
			this.typing.addHypothesis(focusNode, label);
		}
		return result;
	}
	
	protected boolean recursiveValidation(RDFTerm focusNode, Label label) {
		this.typing.addHypothesis(focusNode, label);
		EvaluateShapeExpressionVisitor visitor = new EvaluateShapeExpressionVisitor(focusNode);
		schema.getShapeMap().get(label).accept(visitor);
		this.typing.removeHypothesis(focusNode, label);
		return visitor.result;
		
	}
	
	class EvaluateShapeExpressionVisitor extends ShapeExpressionVisitor<Boolean> {
		private RDFTerm node; 
		private Boolean result;
		
		public EvaluateShapeExpressionVisitor(RDFTerm one) {
			this.node = one;
		}

		@Override
		public Boolean getResult() {
			if (result == null) return false;
			return result;
		}
		
		@Override
		public void visitShapeAnd(ShapeAnd expr, Object... arguments) {
			for (ShapeExpr e : expr.getSubExpressions()) {
				e.accept(this);
				if (!result) break;
			}
		}

		@Override
		public void visitShapeOr(ShapeOr expr, Object... arguments) {
			for (ShapeExpr e : expr.getSubExpressions()) {
				e.accept(this);
				if (result) break;
			}
		}
		
		@Override
		public void visitShapeNot(ShapeNot expr, Object... arguments) {
			expr.getSubExpression().accept(this);
			result = !result;
		}
		
		@Override
		public void visitShape(Shape expr, Object... arguments) {
			result = isLocallyValid(node, expr);
		}

		@Override
		public void visitNodeConstraint(NodeConstraint expr, Object... arguments) {
			result = expr.contains(node);
		}

		@Override
		public void visitShapeExprRef(ShapeExprRef ref, Object[] arguments) {
			ref.getShapeDefinition().accept(this);
		}

		@Override
		public void visitShapeExternal(ShapeExternal shapeExt, Object[] arguments) {
			throw new UnsupportedOperationException("Not yet implemented.");
		}
	}
	
	
	private boolean isLocallyValid (RDFTerm node, Shape shape) {
		TripleExpr tripleExpression = this.sorbeGenerator.getSORBETripleExpr(shape);
		Iterator<NeighborTriple> tmp ;

		List<TripleConstraint> constraints = collectorTC.getResult(tripleExpression);
		if (constraints.size() == 0) {
			if (!shape.isClosed()) {
				return true;
			} else {
				tmp = graph.itOutNeighbours(node);
				if (! tmp.hasNext()) {
					return true;
				} else {
					return false;
				}
			}
		}
		
		Set<IRI> inversePredicate = new HashSet<IRI>();
		Set<IRI> forwardPredicate = new HashSet<IRI>();
		for (TripleConstraint tc:constraints) {
			if (tc.getProperty().isForward()) {
				forwardPredicate.add(tc.getProperty().getIri());
			}else {
				inversePredicate.add(tc.getProperty().getIri());
			}
		}
		
		List<NeighborTriple> neighbourhood = new ArrayList<NeighborTriple>();
		tmp = graph.itInNeighboursWithPredicate(node, inversePredicate);
		while(tmp.hasNext()) neighbourhood.add(tmp.next());
		if (shape.isClosed()) {
			tmp = graph.itOutNeighbours(node);
			while(tmp.hasNext()) neighbourhood.add(tmp.next());
		} else {
			tmp = graph.itOutNeighboursWithPredicate(node,forwardPredicate);
			while(tmp.hasNext()) neighbourhood.add(tmp.next());
		}
		
		// Match using only predicate and recursive test. The following line are the only difference with refine validation
		Set<Pair<RDFTerm, Label>> shapeMap = new HashSet<>();
		Matcher matcher1 = new MatcherPredicateOnly();
		LinkedHashMap<NeighborTriple,List<TripleConstraint>> matchingTC1 = Matcher.collectMatchingTC(neighbourhood, constraints, matcher1);

		for(Entry<NeighborTriple,List<TripleConstraint>> entry:matchingTC1.entrySet()) {
			List<TripleConstraint> possibility = entry.getValue();
			if (possibility.isEmpty() & ! shape.getExtraProperties().contains(entry.getKey().getPredicate()))
				return false;
			for (TripleConstraint tc:possibility) {
			    RDFTerm destNode = entry.getKey().getOpposite();
				if (! this.typing.contains(destNode, tc.getShapeExpr().getId())) {
					if (this.recursiveValidation(destNode, tc.getShapeExpr().getId()))
						shapeMap.add(new Pair<>(destNode, tc.getShapeExpr().getId()));
				}
			}
		}
		
		// Add the detected node value to the typing
		this.typing.addHypothesis(shapeMap);


		Matcher matcher2 = new MatcherPredicateAndValue(this.getTyping()); 
		LinkedHashMap<NeighborTriple,List<TripleConstraint>> matchingTC2 = Matcher.collectMatchingTC(neighbourhood, constraints, matcher2);

		// Check that the neighbor that cannot be match to a constraint are in extra
		Iterator<Map.Entry<NeighborTriple,List<TripleConstraint>>> iteMatchingTC = matchingTC2.entrySet().iterator();
		while(iteMatchingTC.hasNext()) {
			Entry<NeighborTriple, List<TripleConstraint>> listTC = iteMatchingTC.next();
			if (listTC.getValue().isEmpty()) {
				if (! shape.getExtraProperties().contains(listTC.getKey().getPredicate())){
					this.typing.removeHypothesis(shapeMap);
					return false;
				}
				iteMatchingTC.remove();
			}
		}
		
		// Create a BagIterator for all possible bags induced by the matching triple constraints
		ArrayList<List<TripleConstraint>> listMatchingTC = new ArrayList<List<TripleConstraint>>();
		for(NeighborTriple nt:matchingTC2.keySet())
			listMatchingTC.add(matchingTC2.get(nt));
		
		BagIterator bagIt = new BagIterator(listMatchingTC);
		IntervalComputation intervalComputation = new IntervalComputation(this.collectorTC);
		
		while(bagIt.hasNext()){
			Bag bag = bagIt.next();
			tripleExpression.accept(intervalComputation, bag, this);
			if (intervalComputation.getResult().contains(1)) {
				this.typing.removeHypothesis(shapeMap);
				return true;
			}
		}

		this.typing.removeHypothesis(shapeMap);
		return false;
	}	


}
