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
package fr.inria.lille.shexjava.graph;

import java.util.*;

import org.apache.commons.rdf.api.*;

/**
 * Defines the operations on an RDF graph that are needed for validation.
 * 
 * @author Iovka Boneva
 * @author Antonin Durey
 *
 */
public class RDFGraph {

    protected final Graph internalGraph;

    public RDFGraph(Graph internal) {
        this.internalGraph = internal;
    }

    /**
     * List all the triples that contain the given node as subject or object. This
     * is the union of {@link #itInNeighbours(Value)} and
     * {@link #itOutNeighbours(Value)}
     * 
     * @param focusNode
     * @return an iterator over the neighbors of focusNode
     */
    public Iterator<NeighborTriple> itAllNeighbours(RDFTerm focusNode) {
        List<Iterator<NeighborTriple>> result = new ArrayList<>();
        result.add(itInNeighbours(focusNode));
        result.add(itOutNeighbours(focusNode));
        return new ConcatIterator<>(result);
    }

    /**
     * List all the triples that have the given node as focus node or object node
     * and a predicate belonging to the set of allowed predicates.
     * 
     * @param focusNode
     * @return an iterator over the neighbors of focusNode connected with one of the
     *         allowedPredicates
     */
    public Iterator<NeighborTriple> itAllNeighboursWithPredicate(RDFTerm focusNode, Set<IRI> allowedPredicates) {
        List<Iterator<NeighborTriple>> result = new ArrayList<>();
        for (IRI predicate : allowedPredicates) {
            result.add(itOutNeighbours(focusNode, predicate));
            result.add(itInNeighbours(focusNode, predicate));
        }
        return new ConcatIterator<>(result);
    }

    /**
     * List all the triples that have the given node as focus node.
     * 
     * @param focusNode
     * @return an iterator over the outgoing neighbors of focusNode
     */
    public Iterator<NeighborTriple> itOutNeighbours(RDFTerm focusNode) {
        if (!(focusNode instanceof BlankNodeOrIRI)) return new EmptyIterator<>();
        return itOutNeighbours((BlankNodeOrIRI) focusNode, null);
    }

    /**
     * List all the triples that have the given node as focus node and a predicate
     * belonging to the set of allowed predicates.
     * 
     * @param focusNode
     * @return an iterator over the outgoing neighbors of focusNode connected with
     *         one of the allowedPredicates
     */
    public Iterator<NeighborTriple> itOutNeighboursWithPredicate(RDFTerm focusNode, Set<IRI> allowedPredicates) {
        List<Iterator<NeighborTriple>> result = new ArrayList<>();
        for (IRI predicate : allowedPredicates) {
            result.add(itOutNeighbours(focusNode, predicate));
        }
        return new ConcatIterator<>(result);
    }

    /**
     * List all the triples that have the given node as object node.
     * 
     * @param focusNode
     * @return an iterator over the incoming neighbors of focusNode
     */
    public Iterator<NeighborTriple> itInNeighbours(RDFTerm focusNode) {
        return itInNeighbours(focusNode, null);
    }

    /**
     * List all the triples that have the given node as object node and a predicate
     * belonging to the set of allowed predicates.
     * 
     * @param focusNode
     * @return an iterator over the incoming neighbors of focusNode connected with
     *         one of the allowedPredicates
     */
    public Iterator<NeighborTriple> itInNeighboursWithPredicate(RDFTerm focusNode, Set<IRI> allowedPredicates) {
        List<Iterator<NeighborTriple>> result = new ArrayList<>();
        for (IRI predicate : allowedPredicates) {
            result.add(itInNeighbours(focusNode, predicate));
        }
        return new ConcatIterator<>(result);
    }

    /**
     * List all the object and subject in the graph.
     * 
     * @return an iterator over the nodes of the graph
     */
    public Iterator<RDFTerm> listAllNodes() {
        List<Iterator<RDFTerm>> result = new ArrayList<>();
        result.add(listAllObjectNodes());
        result.add(listAllSubjectNodes());
        return new ConcatIterator<>(result);
    }

    /**
     * List all the triples that have the given node as object node and the
     * specified predicate.
     * 
     * @param focusNode
     * @param predicate
     * @return an iterator over the neighbors of focusNode connected with the
     *         predicate
     */
    protected Iterator<NeighborTriple> itOutNeighbours(RDFTerm focusNode, IRI predicate) {
        TCProperty prop = TCProperty.createFwProperty(predicate);
        return internalGraph.stream(null, predicate, focusNode).map(Triple::getObject)
                .map(object -> new NeighborTriple(object, prop, focusNode)).iterator();
    }

    /**
     * List all the triples that have the given node as subject node and the
     * specified predicate.
     * 
     * @param focusNode
     * @param predicate
     * @return an iterator over the neighbors of focusNode connected with the
     *         predicate
     */
    protected Iterator<NeighborTriple> itInNeighbours(RDFTerm focusNode, IRI predicate) {
        TCProperty prop = TCProperty.createInvProperty(predicate);
        return internalGraph.stream((BlankNodeOrIRI) focusNode, predicate, null).map(Triple::getObject)
                .map(object -> new NeighborTriple(focusNode, prop, object)).iterator();
    }

    /**
     * List all the object nodes in the graph.
     * 
     * @return an iterator over the object nodes of the graph
     */
    public Iterator<RDFTerm> listAllObjectNodes() {
        return internalGraph.stream().map(Triple::getObject).iterator();
    }

    /**
     * List all the subjects node in the graph.
     * 
     * @return an iterator over the subject nodes of the graph
     */
    public Iterator<RDFTerm> listAllSubjectNodes() {
        return internalGraph.stream().map(Triple::getSubject).map(RDFTerm.class::cast).iterator();
    }

    // ---------------------------------------------------------------------------
    // Iterators
    // ---------------------------------------------------------------------------

    protected class ConcatIterator<T> implements Iterator<T> {
        private List<Iterator<T>> iterators;

        public ConcatIterator(List<Iterator<T>> iterators) {
            this.iterators = iterators;
        }

        @Override
        public boolean hasNext() {
            if (iterators.size() == 0) return false;
            if (!iterators.get(0).hasNext()) {
                iterators.remove(0);
                return this.hasNext();
            } else {
                return true;
            }
        }

        @Override
        public T next() {
            if (iterators.size() == 0) throw new NoSuchElementException();
            if (!iterators.get(0).hasNext()) {
                iterators.remove(0);
                return this.next();
            } else {
                return iterators.get(0).next();
            }
        }
    }

    protected class EmptyIterator<T> implements Iterator<T> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public T next() {
            throw new NoSuchElementException();
        }
    }
}
