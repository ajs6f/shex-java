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
package fr.univLille.cristal.shex.validation;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.Value;

import fr.univLille.cristal.shex.graph.RDFGraph;
import fr.univLille.cristal.shex.schema.ShapeExprLabel;
import fr.univLille.cristal.shex.schema.ShexSchema;
import fr.univLille.cristal.shex.schema.abstrsynt.ShapeExpr;
import fr.univLille.cristal.shex.schema.abstrsynt.ShapeExprRef;
import fr.univLille.cristal.shex.schema.abstrsynt.TripleConstraint;
import fr.univLille.cristal.shex.schema.abstrsynt.TripleExpr;
import fr.univLille.cristal.shex.util.Pair;

/**
 * 
 * @author Iovka Boneva
 * 10 oct. 2017
 */
public class RefinementTyping implements Typing {
	
	private ShexSchema schema;
	private RDFGraph graph;
	private List<Set<Pair<Value, ShapeExprLabel>>> theTyping;
	private Set<ShapeExprLabel> selectedShape;
	
	public RefinementTyping(ShexSchema schema, RDFGraph graph) {
		this.schema = schema;
		this.graph = graph;
		this.theTyping = new ArrayList<>(schema.getNbStratums());
		for (int i = 0; i < schema.getNbStratums(); i++) {
			theTyping.add(new HashSet<>());
		}
		
		this.selectedShape = new HashSet<ShapeExprLabel>();
		this.selectedShape.addAll(schema.getRules().keySet());
		for (ShapeExpr expr:schema.getShapeMap().values())
			if (expr instanceof ShapeExprRef) 
				selectedShape.add(((ShapeExprRef) expr).getShapeDefinition().getId());
		for (TripleExpr expr:schema.getTripleMap().values())
			if (expr instanceof TripleConstraint)
				selectedShape.add(((TripleConstraint) expr).getShapeExpr().getId());
	}
	
	public Set<ShapeExprLabel> getSelectedShape(){
		return this.selectedShape;
	}

	public void addAllLabelsFrom(int stratum, Value focusNode) {
		Set<ShapeExprLabel> labels = schema.getStratum(stratum);
		Set<Pair<Value, ShapeExprLabel>> set = theTyping.get(stratum);
		for (ShapeExprLabel label: labels) {
			if (selectedShape.contains(label)) {
				for (Value res : graph.getAllNodes()) {
					set.add(new Pair<>(res, label));
				}
				if (focusNode != null)
					set.add(new Pair<>(focusNode, label));
			}
		}
	}
	
	public Iterator<Pair<Value, ShapeExprLabel>> typesIterator (int stratum) {
		return theTyping.get(stratum).iterator();
	}
	
	@Override
	public boolean contains (Value node, ShapeExprLabel label) {
		return theTyping.get(schema.hasStratum(label)).contains(new Pair<>(node, label));
	}
	
	
	@Override
	public Set<Pair<Value, ShapeExprLabel>> asSet() {
		Set<Pair<Value, ShapeExprLabel>> set = new HashSet<>();
		for (Set<Pair<Value, ShapeExprLabel>> subset : theTyping)
			set.addAll(subset);
		
		return set;
	}
	
	@Override
	public String toString() {
		return asSet().toString();
	}
}

