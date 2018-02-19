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
package fr.univLille.cristal.shex.schema.concrsynt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.rdf4j.model.Value;

/**
 * 
 * @author Iovka Boneva
 * 10 oct. 2017
 */
public class ConjunctiveSetOfNodes implements SetOfNodes {

	private List<SetOfNodes> conjuncts;
	
	public ConjunctiveSetOfNodes(Collection<SetOfNodes> conjuncts) {
		this.conjuncts = new ArrayList<>();
		this.conjuncts.addAll(conjuncts);
	}
	
	@Override
	public boolean contains(Value node) {
		for (SetOfNodes s : conjuncts)
			if (! s.contains(node))
				return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "CONJ" + conjuncts.toString();
	}

}
