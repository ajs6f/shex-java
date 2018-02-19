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
package fr.univLille.cristal.shex.schema.abstrsynt;

import java.util.Set;

import fr.univLille.cristal.shex.graph.TCProperty;

/**
 * 
 * @author Iovka Boneva
 * 10 oct. 2017
 */
public interface PropertySet {

	public boolean contains (TCProperty p);
	
	public Set<TCProperty> getAsFiniteSet();
	public Set<TCProperty> getComplementAsFiniteSet();
	
	
}
