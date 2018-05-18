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
package fr.inria.lille.shexjava.util;

import static java.util.Collections.singleton;
import static java.util.stream.Collector.Characteristics.UNORDERED;
import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

import org.apache.commons.rdf.api.*;
import fr.inria.lille.shexjava.schema.Label;

public 	class TestCase {
	private static final RDF RDF_FACTORY = RDFFactory.getInstance();
	private static final IRI RDF_TYPE = RDF_FACTORY.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	private static final IRI TEST_NAME_IRI = RDF_FACTORY.createIRI("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#name");
	private static final IRI ACTION_PROPERTY = RDF_FACTORY.createIRI("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#action");
	private static final IRI SCHEMA_PROPERTY = RDF_FACTORY.createIRI("http://www.w3.org/ns/shacl/test-suite#schema");
	private static final IRI DATA_PROPERTY = RDF_FACTORY.createIRI("http://www.w3.org/ns/shacl/test-suite#data");
	private static final IRI SHAPE_PROPERTY = RDF_FACTORY.createIRI("http://www.w3.org/ns/shacl/test-suite#shape");
	private static final IRI FOCUS_PROPERTY = RDF_FACTORY.createIRI("http://www.w3.org/ns/shacl/test-suite#focus");
	private static final IRI TEST_TRAIT_IRI = RDF_FACTORY.createIRI("http://www.w3.org/ns/shacl/test-suite#trait");
    private static final IRI RDFS_COMMENT = RDF_FACTORY.createIRI("http://www.w3.org/2000/01/rdf-schema#comment");

	public final BlankNodeOrIRI testKind;
	public final String testName;
	public final BlankNodeOrIRI schemaFileName;
	public final BlankNodeOrIRI dataFileName;
	public Label shapeLabel;
	public RDFTerm focusNode;
	public final String testComment;
	public final Set<RDFTerm> traits;

	public TestCase(Graph manifest, BlankNodeOrIRI testNode) {
		try {
		    BlankNodeOrIRI actionNode = (BlankNodeOrIRI) getProperty(manifest, testNode, ACTION_PROPERTY).get();
			traits = manifest.stream(testNode, TEST_TRAIT_IRI, null).map(Triple::getObject).collect(toSet());
			schemaFileName = (BlankNodeOrIRI) getProperty(manifest, actionNode, SCHEMA_PROPERTY).get();  
			dataFileName = (BlankNodeOrIRI) getProperty(manifest, actionNode, DATA_PROPERTY).get();
			if (getProperty(manifest, actionNode, SHAPE_PROPERTY).isPresent()) {
			    BlankNodeOrIRI labelRes = (BlankNodeOrIRI) getProperty(manifest, actionNode, SHAPE_PROPERTY).get();
				if (labelRes instanceof BlankNode)
					shapeLabel = new Label((BlankNode)labelRes);
				else
					shapeLabel = new Label((IRI)labelRes);

				focusNode = getProperty(manifest, actionNode, FOCUS_PROPERTY).get();
			}
			testComment = getPropertyLiteralString(manifest, testNode, RDFS_COMMENT).get();
            testName = getPropertyLiteralString(manifest, testNode, TEST_NAME_IRI).get();
			testKind = (BlankNodeOrIRI) getProperty(manifest, testNode, RDF_TYPE).get();
		} catch (Exception e) {
			System.out.println(" Error on test case " + testNode);
			throw e;
		}
	}
	
	public static Optional<RDFTerm> getProperty(Graph g, BlankNodeOrIRI subject, IRI property) {
	    return g.stream(subject, property, null).findFirst().map(Triple::getObject);
	}

	public static Optional<String> getPropertyLiteralString(Graph g, BlankNodeOrIRI subject, IRI property) {
	        return getProperty(g, subject, property).map(Literal.class::cast).map(Literal::getLexicalForm);
	}

	@Override
	public String toString() {
		String info = "";
		info += testName + "\n";
		info += testKind.toString() + "\n";
		info += "Comment    : " + testComment + "\n";
		info += "Schema file: " + schemaFileName + "\n";
		info += "Data file  : " + dataFileName + "\n";
		info += "Focus : " + focusNode + "\n";
		info += "Shape : " + shapeLabel + "\n";
		return info;
	}		

	public boolean isWellDefined () {
		return schemaFileName != null && dataFileName != null && shapeLabel != null && focusNode != null;
	}
	
	public Graph filter(Graph source, BlankNodeOrIRI subj, IRI pred, RDFTerm obj) {
	    Collector<? super Triple, Set<Triple>,Graph> col =  new TripleIntoGraphCollector(RDF_FACTORY);
	    return source.stream(subj, pred, obj).collect(col);
	}
	
    public static class TripleIntoGraphCollector implements Collector<Triple, Set<Triple>,Graph> {

	    private final RDF rdfFactory;
	    
	    TripleIntoGraphCollector(RDF rdfFactory) {
	        this.rdfFactory = rdfFactory;
	    }
	    
        @Override
        public Supplier<Set<Triple>> supplier() {
            return HashSet::new;
        }

        @Override
        public BiConsumer<Set<Triple>, Triple> accumulator() {
            return Set::add;
        }

        @Override
        public BinaryOperator<Set<Triple>> combiner() {
            return (l, r) -> { l.addAll(r); return l; };
        }

        @Override
        public Function<Set<Triple>, Graph> finisher() {
            return triples -> {
                Graph g = rdfFactory.createGraph();
                triples.forEach(g::add);
                return g;
            };
        }

        @Override
        public Set<Characteristics> characteristics() {
            return singleton(UNORDERED);
        }
	}
}
