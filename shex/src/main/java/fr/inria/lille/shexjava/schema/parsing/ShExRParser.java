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
package fr.inria.lille.shexjava.schema.parsing;

import static org.eclipse.rdf4j.rio.RDFFormat.TURTLE;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;

import org.apache.commons.rdf.api.*;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.ParseErrorLogger;

import fr.inria.lille.shexjava.graph.TCProperty;
import fr.inria.lille.shexjava.schema.Label;
import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.abstrsynt.Annotation;
import fr.inria.lille.shexjava.schema.abstrsynt.EachOf;
import fr.inria.lille.shexjava.schema.abstrsynt.EmptyShape;
import fr.inria.lille.shexjava.schema.abstrsynt.EmptyTripleExpression;
import fr.inria.lille.shexjava.schema.abstrsynt.NodeConstraint;
import fr.inria.lille.shexjava.schema.abstrsynt.OneOf;
import fr.inria.lille.shexjava.schema.abstrsynt.RepeatedTripleExpression;
import fr.inria.lille.shexjava.schema.abstrsynt.Shape;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeAnd;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeExpr;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeExprRef;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeNot;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeOr;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleConstraint;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleExpr;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleExprRef;
import fr.inria.lille.shexjava.schema.concrsynt.*;
import fr.inria.lille.shexjava.util.Interval;
import fr.inria.lille.shexjava.util.RDFFactory;


/** Parses a {@link ShexSchema} from its rdf representation. 
 * 
 * This implementation does not support: external definitions, semantic actions and anonymous "start" shapes.
 * The base IRI for shex object is http://www.w3.org/ns/shex#.
 * 
 * @author Jérémie Dusart
 */
public class ShExRParser implements Parser {
	public static final List<RDFFormat> RDFFormats = Arrays.asList(new RDFFormat[] {
			RDFFormat.BINARY,
			RDFFormat.JSONLD,
			RDFFormat.N3,
			RDFFormat.NQUADS,
			RDFFormat.NTRIPLES,
			RDFFormat.RDFA,
			RDFFormat.RDFJSON,
			RDFFormat.RDFXML,
			RDFFormat.TRIG,
			RDFFormat.TRIX,
			RDFFormat.TURTLE
	});

	private static final RDF rdfFactory = RDFFactory.getInstance();

	private IRI TYPE_IRI = rdfFactory.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	
	private Graph model;
	private List<String> imports;
	private Set<RDFTerm> shapeSeen;
	private Set<RDFTerm> tripleSeen;
	
	/** Used the first format that contains an extension that ends the provided path in the list of RDFFormats.
	 * @see fr.inria.lille.shexjava.schema.parsing.Parser#getRules(java.nio.file.Path)
	 */
	@Override
	public Map<Label, ShapeExpr> getRules(Path path) throws Exception {
		RDFFormat foundformat = null;
		for (RDFFormat format:ShExRParser.RDFFormats) {
			for (String ext:format.getFileExtensions()) {
				if (path.toString().endsWith(ext)) {
					foundformat = format;	
				}
			}
		}	
		InputStream is = new FileInputStream(path.toFile());
		return getRules(is,foundformat);
	}
	
	/** Used null as format.
	 * @see fr.inria.lille.shexjava.schema.parsing.Parser#getRules(java.nio.file.Path)
	 */
	@Override
	public Map<Label, ShapeExpr> getRules(InputStream is) throws Exception {
		return getRules(is,null);
	}
	
	private static String BASE_IRI = "http://base.shex.fr/shex/";
		
	public Map<Label, ShapeExpr> getRules(InputStream is, RDFFormat format) throws Exception {
		Reader isr = new InputStreamReader(is,Charset.defaultCharset().name());
		
		SimpleValueFactory svf = SimpleValueFactory.getInstance();
        Model rdf = Rio.parse(isr, BASE_IRI, TURTLE, new ParserConfig(), svf, new ParseErrorLogger());
		Graph model = new RDF4J().asGraph(rdf);
		BlankNodeOrIRI root = model.stream(null, TYPE_IRI, SCHEMA).map(Triple::getSubject).findFirst().get();
		
		Map<Label,ShapeExpr> rules = new HashMap<>();
		shapeSeen = new HashSet<RDFTerm>();
		tripleSeen = new HashSet<RDFTerm>();
		
		model.stream(root, SHAPES, null).forEach(stat -> {
			ShapeExpr shape = parseShapeExpr(stat.getObject());
			if (!(shape instanceof ShapeExprRef)) {
				if (rules.containsKey(shape.getId()))
					throw new IllegalArgumentException("Label "+shape.getId()+" allready used.");
				rules.put(shape.getId(), shape);
			}		
		});
		parseImports(root);
		
		return rules;
	}

	@Override
	public List<String> getImports() {
		return imports;
	}

    private static Optional<RDFTerm> getObject(Graph g, RDFTerm subj, IRI pred) {
        return g.stream((BlankNodeOrIRI) subj, pred, null).findFirst().map(Triple::getObject);
    }
	
	//---------------------------------------------------------
	// Schema
	//---------------------------------------------------------

	private static IRI IMPORTS = rdfFactory.createIRI("http://www.w3.org/ns/shex#imports");
	private void parseImports(RDFTerm value) {
		imports = new ArrayList<String>();
		if (!model.contains((BlankNodeOrIRI) value,IMPORTS,null))
			return ;
		
		RDFTerm val = getObject(model, value, IMPORTS).get();
		List<RDFTerm> list = computeListOfObject(val);

		for(RDFTerm obj:list) {
			imports.add(((IRI)obj).getIRIString().substring(BASE_IRI.length())); 
		}
	}
	
	
	
	//----------------------------------------------------------
	// Shape
	//----------------------------------------------------------
	
	private static IRI SCHEMA = rdfFactory.createIRI("http://www.w3.org/ns/shex#Schema");
	private static IRI SHAPE = rdfFactory.createIRI("http://www.w3.org/ns/shex#Shape");
	private static IRI SHAPES = rdfFactory.createIRI("http://www.w3.org/ns/shex#shapes");
	private static IRI SHAPE_AND = rdfFactory.createIRI("http://www.w3.org/ns/shex#ShapeAnd");
	private static IRI SHAPE_OR = rdfFactory.createIRI("http://www.w3.org/ns/shex#ShapeOr");
	private static IRI SHAPE_NOT = rdfFactory.createIRI("http://www.w3.org/ns/shex#ShapeNot");
	private static IRI NODE_CONSTRAINT = rdfFactory.createIRI("http://www.w3.org/ns/shex#NodeConstraint");
	private ShapeExpr parseShapeExpr(RDFTerm value) {
		if (shapeSeen.contains(value) |  model.stream((BlankNodeOrIRI) value,null,null).count()==0 )
			return new ShapeExprRef(createLabel(value));
		
		RDFTerm type = model.stream((BlankNodeOrIRI) value,TYPE_IRI,null).map(Triple::getObject).findFirst().get();
		
		shapeSeen.add(value);
		if (type.equals(SHAPE_AND))
			return parseShapeAnd(value);
		if (type.equals(SHAPE_OR))
			return parseShapeOr(value);
		if (type.equals(SHAPE))
			return parseShape(value);
		if (type.equals(SHAPE_NOT))
			return parseShapeNot(value);
		if (type.equals(NODE_CONSTRAINT))
			return parseNodeConstraint(value);
		
		System.err.println("Unknown shape type: "+type);
		return null;
	}
	
	
	private static IRI SHAPE_EXPRS = rdfFactory.createIRI("http://www.w3.org/ns/shex#shapeExprs");
	private ShapeExpr parseShapeAnd(RDFTerm value) {
	    RDFTerm val = getObject(model, value, SHAPE_EXPRS).get();
		List<ShapeExpr> subExpr = new ArrayList<>();
		for (Object obj:computeListOfObject(val))
			subExpr.add(parseShapeExpr((RDFTerm) obj));
		
		ShapeExpr res = new ShapeAnd(subExpr);
		setLabel(res,value);		
		return res;
	}
	
	
	private ShapeExpr parseShapeOr(RDFTerm value) {
	    RDFTerm val = getObject(model, value, SHAPE_EXPRS).get();
		List<ShapeExpr> subExpr = new ArrayList<>();
		for (Object obj:computeListOfObject(val))
			subExpr.add(parseShapeExpr((RDFTerm) obj));
		
		ShapeExpr res = new ShapeOr(subExpr);
		setLabel(res,value);	
		return res;
	}
	
	
	private static IRI SHAPE_EXPR = rdfFactory.createIRI("http://www.w3.org/ns/shex#shapeExpr");
	private ShapeExpr parseShapeNot(RDFTerm value) {
	    RDFTerm val = getObject(model, value, SHAPE_EXPR).get();
		
		ShapeExpr res = new ShapeNot(parseShapeExpr(val));
		setLabel(res,value);
		return res;
	}
	
	
	private static IRI TRIPLE_EXPRESSION = rdfFactory.createIRI("http://www.w3.org/ns/shex#expression");
	private static IRI CLOSED = rdfFactory.createIRI("http://www.w3.org/ns/shex#closed");
	private static IRI EXTRA = rdfFactory.createIRI("http://www.w3.org/ns/shex#extra");
	private ShapeExpr parseShape(RDFTerm value) {
		List<Annotation> annotations = parseAnnotations(value);

		if (!model.contains((BlankNodeOrIRI) value, TRIPLE_EXPRESSION,null)) {
			ShapeExpr shtmp = new Shape(new EmptyTripleExpression(),Collections.emptySet(),false);
			setLabel(shtmp,value);
			return shtmp;
		}
		
		boolean closed = false;
		if (model.contains((BlankNodeOrIRI) value, CLOSED,null)) {
			Literal val = (Literal) getObject(model, value, CLOSED).get();
			closed = Boolean.parseBoolean(val.getLexicalForm());
		}
		
		Set<TCProperty> extras = new HashSet<>();
        if (model.contains((BlankNodeOrIRI) value, EXTRA, null)) {
            model.stream((BlankNodeOrIRI) value, EXTRA, null).forEach(ext -> {
                extras.add(TCProperty.createFwProperty((IRI) ext.getObject()));
            });
        }
		RDFTerm val = getObject(model, value, TRIPLE_EXPRESSION).get();
		
		Shape res = new Shape(parseTripleExpr(val),extras,closed,annotations);
		setLabel(res,value);	
		return res;
	}
	
	
	private static IRI DATATYPE = rdfFactory.createIRI("http://www.w3.org/ns/shex#datatype");
	private ShapeExpr parseNodeConstraint(RDFTerm value) {
		List<Constraint> constraints = new ArrayList<>();
		
		Constraint constraint = parseNodeKind(value);
		if (constraint != null)
			constraints.add(constraint);
		
		if (model.contains((BlankNodeOrIRI) value,DATATYPE,null)) {
		    RDFTerm val = getObject(model, value, DATATYPE).get();
			constraints.add(new DatatypeConstraint((IRI) val));
		}
		
		constraint = parseStringFacet(value);
		if (constraint!=null)
			constraints.add(constraint);
		
		constraint = parseNumericFacet(value);
		if (constraint!=null)
			constraints.add(constraint);
				
		constraint = parseRDFTerms(value);
		if (constraint!=null)
			constraints.add(constraint);
				
		ShapeExpr res = new NodeConstraint(constraints);
		setLabel(res,value);
		return res;
	}
	
	
	private static IRI RDFTermS = rdfFactory.createIRI("http://www.w3.org/ns/shex#RDFTerms");
	private Constraint parseRDFTerms(RDFTerm value) {
		if (!model.contains((BlankNodeOrIRI) value, RDFTermS, null))
			return null;
		
		RDFTerm val = getObject(model, value, RDFTermS).get();
		
		List<RDFTerm> RDFTerms_tmp = computeListOfObject(val);
		Set<RDFTerm> explicitRDFTerms = new HashSet<>();
		Set<Constraint> constraints = new HashSet<>();
		for (RDFTerm obj:RDFTerms_tmp) {
			if (obj instanceof Literal | obj instanceof IRI) {
				explicitRDFTerms.add(obj);
				continue;
			} else {
				Constraint tmp = parseLanguage(obj);
				if (tmp!=null)
					constraints.add(tmp);
				tmp = parseLiteralStem(obj);
				if (tmp!=null)
					constraints.add(tmp);
				tmp = parseIRIStem(obj);
				if (tmp!=null)
					constraints.add(tmp);
			}
		}
				
		return new ValueSetValueConstraint(explicitRDFTerms, constraints);
	}
	
	
	private static IRI IRI_STEM = rdfFactory.createIRI("http://www.w3.org/ns/shex#IriStem");
	private static IRI IRI_STEM_RANGE = rdfFactory.createIRI("http://www.w3.org/ns/shex#IriStemRange");
	private Constraint parseIRIStem(RDFTerm value) {
		RDFTerm type = getObject(model, value,TYPE_IRI).get();
		if (type.equals(IRI_STEM)) {
			Literal tmp = (Literal) getObject(model, value, STEM).get();
			return new IRIStemConstraint(tmp.getLexicalForm());
		}
		if (type.equals(IRI_STEM_RANGE)) {
			Constraint stem;
			if (model.contains((BlankNodeOrIRI) value, STEM, null)) {
				RDFTerm tmp = getObject(model, value, STEM).get();
				if (tmp instanceof Literal)
					stem = new IRIStemConstraint(((Literal) tmp).getLexicalForm());
				else
					stem = new WildcardConstraint();
			} else {
				stem = new WildcardConstraint();
			}
			
			Set<RDFTerm> explicitRDFTerms = new HashSet<>();
			Set<Constraint> constraints = new HashSet<>();
			
			RDFTerm exclu = getObject(model, value, EXCLUSION).get();
			List<RDFTerm> exclusions = computeListOfObject(exclu);
			for (RDFTerm excl:exclusions) {
				if (excl instanceof IRI) {
					explicitRDFTerms.add((IRI) excl);
				} else {
					Literal tmp = (Literal) getObject(model, value, STEM).get();
					constraints.add(new IRIStemConstraint(tmp.getLexicalForm()));
				}
			}

			return new IRIStemRangeConstraint(stem, explicitRDFTerms, constraints);
		}
		return null;
	}
	
	
	private static IRI LITERAL_STEM = rdfFactory.createIRI("http://www.w3.org/ns/shex#LiteralStem");
	private static IRI LITERAL_STEM_RANGE = rdfFactory.createIRI("http://www.w3.org/ns/shex#LiteralStemRange");
	private Constraint parseLiteralStem(RDFTerm value) {
		RDFTerm type = getObject(model, value, TYPE_IRI).get();
		if (type.equals(LITERAL_STEM)) {
			Literal tmp = (Literal) getObject(model, value, STEM).get();
			return new LiteralStemConstraint(tmp.getLexicalForm());
		}
		if (type.equals(LITERAL_STEM_RANGE)) {
			Constraint stem;
			if (model.contains((BlankNodeOrIRI) value, STEM, null)) {
				Literal tmp = (Literal) getObject(model, value, STEM).get();
				stem = new LiteralStemConstraint(tmp.getLexicalForm());
			} else {
				stem = new WildcardConstraint();
			}
			
			Set<RDFTerm> explicitRDFTerms = new HashSet<>();
			Set<Constraint> constraints = new HashSet<>();
			
			RDFTerm exclu = getObject(model, value, EXCLUSION).get();
			List<RDFTerm> exclusions = computeListOfObject(exclu);
			for (RDFTerm excl:exclusions) {
				if (excl instanceof Literal) {
					explicitRDFTerms.add((Literal) excl);
					//constraints.add(new LanguageSetOfNodes(((Literal)excl).stringRDFTerm()));
				} else {
					Literal tmp = (Literal) getObject(model, (IRI) excl, STEM).get();
					constraints.add(new LiteralStemConstraint(tmp.getLexicalForm()));
				}
			}
			return new LiteralStemRangeConstraint(stem, explicitRDFTerms, constraints);
		}
		return null;
	}
	
	
	private static IRI STEM = rdfFactory.createIRI("http://www.w3.org/ns/shex#stem");
	private static IRI EXCLUSION = rdfFactory.createIRI("http://www.w3.org/ns/shex#exclusion");
	private static IRI LANGUAGE = rdfFactory.createIRI("http://www.w3.org/ns/shex#Language");
	private static IRI LANGUAGE_TAG = rdfFactory.createIRI("http://www.w3.org/ns/shex#languageTag");
	private static IRI LANGUAGE_STEM = rdfFactory.createIRI("http://www.w3.org/ns/shex#LanguageStem");
	private static IRI LANGUAGE_STEM_RANGE = rdfFactory.createIRI("http://www.w3.org/ns/shex#LanguageStemRange");
	private Constraint parseLanguage(RDFTerm value) {
		//System.err.println(model.filter((BlankNodeOrIRI) RDFTerm,null,null));
		RDFTerm type = getObject(model, value, TYPE_IRI).get();
		if (type.equals(LANGUAGE)) {
			Literal tmp = (Literal) getObject(model, value, LANGUAGE_TAG).get();
			return new LanguageConstraint(tmp.getLexicalForm());
		}
		if (type.equals(LANGUAGE_STEM)) {
			Literal tmp = (Literal) getObject(model, value, STEM).get();
			return new LanguageStemConstraint(tmp.getLexicalForm());
		}
		if (type.equals(LANGUAGE_STEM_RANGE)) {
			Constraint stem ;
			if (model.contains((BlankNodeOrIRI) value, STEM, null)) {
				Literal tmp = (Literal) getObject(model, value, STEM).get();
				stem = new LanguageStemConstraint(tmp.getLexicalForm());
			} else {
				stem = new WildcardConstraint();
			}
			
			Set<RDFTerm> explicitRDFTerms = new HashSet<>();
			Set<Constraint> constraints = new HashSet<>();
			
			RDFTerm exclu = getObject(model, value, EXCLUSION).get();
			List<RDFTerm> exclusions = computeListOfObject(exclu);
			for (RDFTerm excl:exclusions) {
				if (excl instanceof Literal) {
					constraints.add(new LanguageConstraint(((Literal)excl).getLexicalForm()));
				} else {
					Literal tmp = (Literal) getObject(model, excl, STEM).get();
					constraints.add(new LanguageStemConstraint(tmp.getLexicalForm()));
				}
			}
			return new LanguageStemRangeConstraint(stem, explicitRDFTerms, constraints);
		}
		return null;
	}
	
	
	private static IRI NODEKIND = rdfFactory.createIRI("http://www.w3.org/ns/shex#nodeKind");	
	private static IRI BlankNode = rdfFactory.createIRI("http://www.w3.org/ns/shex#BlankNode");
	private static IRI IRI = rdfFactory.createIRI("http://www.w3.org/ns/shex#iri");
	private static IRI LITERAL = rdfFactory.createIRI("http://www.w3.org/ns/shex#literal");
	private static IRI NONLITERAL = rdfFactory.createIRI("http://www.w3.org/ns/shex#nonliteral");
	private Constraint parseNodeKind(RDFTerm value) {
		if (!model.contains((BlankNodeOrIRI) value,NODEKIND,null))
			return null;
		
		RDFTerm val = getObject(model, value, NODEKIND).get();
		if (val.equals(BlankNode))
			return NodeKindConstraint.Blank;
		if (val.equals(IRI))
			return NodeKindConstraint.AllIRI;
		if (val.equals(LITERAL))
			return NodeKindConstraint.AllLiteral;
		if (val.equals(NONLITERAL))
			return NodeKindConstraint.AllNonLiteral;
		System.err.println("Unknown nodekind: "+val);
		return null;
	}
	
	
	private static IRI LENGTH = rdfFactory.createIRI("http://www.w3.org/ns/shex#length");
	private static IRI MINLENGTH = rdfFactory.createIRI("http://www.w3.org/ns/shex#minlength");
	private static IRI MAXLENGTH = rdfFactory.createIRI("http://www.w3.org/ns/shex#maxlength");
	private static IRI PATTERN = rdfFactory.createIRI("http://www.w3.org/ns/shex#pattern");
	private static IRI FLAGS = rdfFactory.createIRI("http://www.w3.org/ns/shex#flags");
	
	private Constraint parseStringFacet(RDFTerm value) {
		FacetStringConstraint facet = new FacetStringConstraint();
		boolean changed = false;
		
		if (model.contains((BlankNodeOrIRI) value, LENGTH,null)) {
			Literal val = (Literal) getObject(model, value, LENGTH).get();
			facet.setLength(Integer.parseInt(val.getLexicalForm()));
			changed=true;
		}
		if (model.contains((BlankNodeOrIRI) value, MINLENGTH,null)) {
			Literal val = (Literal) getObject(model, value, MINLENGTH).get();
			facet.setMinLength(Integer.parseInt(val.getLexicalForm()));
			changed=true;
		}
		if (model.contains((BlankNodeOrIRI) value, MAXLENGTH,null)) {
			Literal val = (Literal) getObject(model, value, MAXLENGTH).get();
			facet.setMaxLength(Integer.parseInt(val.getLexicalForm()));
			changed=true;
		}
		if (model.contains((BlankNodeOrIRI) value, PATTERN,null)) {
			Literal val = (Literal) getObject(model, value, PATTERN).get();
			facet.setPattern(val.getLexicalForm());
			changed=true;
		}
		if (model.contains((BlankNodeOrIRI) value, FLAGS, null)) {
			Literal val = (Literal) getObject(model, value, FLAGS).get();
			facet.setFlags(val.getLexicalForm());
			changed=true;
		}
		
		if (changed)
			return facet;
		return null;
	}
	
	
	private static IRI MININCLUSIVE = rdfFactory.createIRI("http://www.w3.org/ns/shex#mininclusive");
	private static IRI MINEXCLUSIVE = rdfFactory.createIRI("http://www.w3.org/ns/shex#minexclusive");
	private static IRI MAXINCLUSIVE = rdfFactory.createIRI("http://www.w3.org/ns/shex#maxinclusive");
	private static IRI MAXEXCLUSIVE = rdfFactory.createIRI("http://www.w3.org/ns/shex#maxexclusive");
	private static IRI FRACTIONDIGITS = rdfFactory.createIRI("http://www.w3.org/ns/shex#fractiondigits");
	private static IRI TOTALDIGITS = rdfFactory.createIRI("http://www.w3.org/ns/shex#totaldigits");
	private Constraint parseNumericFacet(RDFTerm value) {
		FacetNumericConstraint facet = new FacetNumericConstraint();
		boolean changed = false;
				
		if (model.contains((BlankNodeOrIRI) value, MININCLUSIVE,null)) {
			Literal val = (Literal) getObject(model, value, MININCLUSIVE).get();
			facet.setMinincl(new BigDecimal(val.getLexicalForm()));
			changed=true;
		}
		
		if (model.contains((BlankNodeOrIRI) value, MINEXCLUSIVE,null)) {
			Literal val = (Literal) getObject(model, value, MINEXCLUSIVE).get();
			facet.setMinexcl(new BigDecimal(val.getLexicalForm()));
			changed=true;
		}
		
		if (model.contains((BlankNodeOrIRI) value, MAXINCLUSIVE,null)) {
			Literal val = (Literal) getObject(model, value, MAXINCLUSIVE).get();
			facet.setMaxincl(new BigDecimal(val.getLexicalForm()));
			changed=true;
		}
		
		if (model.contains((BlankNodeOrIRI) value, MAXEXCLUSIVE,null)) {
			Literal val = (Literal) getObject(model, value, MAXEXCLUSIVE).get();
			facet.setMaxexcl(new BigDecimal(val.getLexicalForm()));
			changed=true;
		}
		
		if (model.contains((BlankNodeOrIRI) value, FRACTIONDIGITS,null)) {
			Literal val = (Literal) getObject(model, value, FRACTIONDIGITS).get();
			facet.setFractionDigits(Integer.parseInt(val.getLexicalForm()));
			changed=true;
		}
		
		if (model.contains((BlankNodeOrIRI) value, TOTALDIGITS,null)) {
			Literal val = (Literal) getObject(model, value, TOTALDIGITS).get();
			facet.setTotalDigits(Integer.parseInt(val.getLexicalForm()));
			changed=true;
		}
		
		if (changed)
			return facet;
		return null;
	}
	
	
	
	//--------------------------------------------------------
	// Triple
	//--------------------------------------------------------

	private static IRI TRIPLE_CONSTRAINT = rdfFactory.createIRI("http://www.w3.org/ns/shex#TripleConstraint");
	private static IRI EACH_OF = rdfFactory.createIRI("http://www.w3.org/ns/shex#EachOf");
	private static IRI ONE_OF = rdfFactory.createIRI("http://www.w3.org/ns/shex#OneOf");
	
	private TripleExpr parseTripleExpr(RDFTerm value) {
		if (tripleSeen.contains(value) | !model.contains((BlankNodeOrIRI) value,null,null))
			return new TripleExprRef(createLabel(value));
		
		RDFTerm type = getObject(model, value, TYPE_IRI).get();

		tripleSeen.add(value);
		if (type.equals(ONE_OF))
			return parseOneOf(value);
		if (type.equals(EACH_OF))
			return parseEachOf(value);
		if (type.equals(TRIPLE_CONSTRAINT))
			return parseTripleConstraint(value);
		System.err.println("Unknown triple type: "+type);
		return null;
	}
	
	
	private static IRI EXPRESSIONS = rdfFactory.createIRI("http://www.w3.org/ns/shex#expressions");
	
	private TripleExpr parseEachOf(RDFTerm value) {
		List<Annotation> annotations = parseAnnotations(value);

		RDFTerm val = getObject(model, value, EXPRESSIONS).get();
		List<TripleExpr> subExpr = new ArrayList<TripleExpr>();
		for (Object obj:computeListOfObject(val))
			subExpr.add(parseTripleExpr((RDFTerm) obj));
		
		TripleExpr res = new EachOf(subExpr,annotations);
		setLabel(res,value);

		Interval card = getInterval(value);
		if (card!=null)
			res = new RepeatedTripleExpression(res, card);
		
		return res;
	}
	
	private TripleExpr parseOneOf(RDFTerm value) {
		List<Annotation> annotations = parseAnnotations(value);

		RDFTerm val = getObject(model, value, EXPRESSIONS).get();
		List<TripleExpr> subExpr = new ArrayList<TripleExpr>();
		for (Object obj:computeListOfObject(val))
			subExpr.add(parseTripleExpr((RDFTerm) obj));
		
		TripleExpr res = new OneOf(subExpr,annotations);
		setLabel(res,value);

		Interval card = getInterval(value);
		if (card!=null)
			res = new RepeatedTripleExpression(res, card);
		
		return res;
	}
	
	
	private static IRI PREDICATE = rdfFactory.createIRI("http://www.w3.org/ns/shex#predicate");
	private static IRI RDFTerm_EXPR = rdfFactory.createIRI("http://www.w3.org/ns/shex#RDFTermExpr");
	private static IRI INVERSE = rdfFactory.createIRI("http://www.w3.org/ns/shex#inverse");
	private TripleExpr parseTripleConstraint(RDFTerm value) {
		List<Annotation> annotations = parseAnnotations(value);
		
		boolean inverse = false;
		if (model.contains((BlankNodeOrIRI) value, INVERSE, null)) {
			Literal inv = (Literal) getObject(model, value, INVERSE).get();
			inverse = Boolean.parseBoolean(inv.getLexicalForm());
		}
		
		RDFTerm pred = getObject(model, value, PREDICATE).get();
		TCProperty predicate;
		if (inverse)
			predicate = TCProperty.createInvProperty((IRI) pred);
		else
			predicate = TCProperty.createFwProperty((IRI) pred);
		
		ShapeExpr RDFTermExpr;
		if (model.contains((BlankNodeOrIRI) value, RDFTerm_EXPR, null)) {
			RDFTerm val = getObject(model, value, RDFTerm_EXPR).get();
			RDFTermExpr = parseShapeExpr(val);
		} else {
			RDFTermExpr = new EmptyShape();
		}
		
		TripleExpr res = new TripleConstraint(predicate,RDFTermExpr,annotations);
		setLabel(res,value);
		
		Interval card = getInterval(value);
		if (card!=null)
			res = new RepeatedTripleExpression(res, card);
		
		return res;
	}
	
	
	private static IRI ANNOTATION = rdfFactory.createIRI("http://www.w3.org/ns/shex#annotation");
	private static IRI OBJECT = rdfFactory.createIRI("http://www.w3.org/ns/shex#object");
	private List<Annotation> parseAnnotations(RDFTerm value){
		List<Annotation> annotations = null;
		if (model.contains((BlankNodeOrIRI) value, ANNOTATION, null)) {
			RDFTerm ann = getObject(model, value, ANNOTATION).get();
			List<RDFTerm> lannot = computeListOfObject(ann);
			annotations = new ArrayList<Annotation>();
			for (RDFTerm obj:lannot) {
				IRI predicate = (IRI) getObject(model, obj, PREDICATE).get();
				RDFTerm object = getObject(model, obj, OBJECT).get();
				annotations.add(new Annotation(predicate,object));
			}
		}
		return annotations;
		
	}
	
	
	private static IRI MIN = rdfFactory.createIRI("http://www.w3.org/ns/shex#min");
	private static IRI MAX = rdfFactory.createIRI("http://www.w3.org/ns/shex#max");
	private Interval getInterval(RDFTerm value) {
		Integer  min=null,max=null;
		if (model.contains((BlankNodeOrIRI) value,MIN,null))
			min =  Integer.parseInt(((Literal) getObject(model, value, MIN).get()).getLexicalForm());
		if (model.contains((BlankNodeOrIRI) value,MAX,null))
			max =  Integer.parseInt(((Literal) getObject(model, value, MAX).get()).getLexicalForm());
		
		if (min==null & max==null)
			return null;
		
		if (min==0 & max==1)
			return Interval.OPT;
		if (min==0 & max==-1)
			return Interval.STAR;
		if (min==1 & max==-1)
			return Interval.PLUS;
		if (max ==-1)
			max = Interval.UNBOUND;
		return new Interval(min,max);
	}
	
	
	
	//--------------------------------------------------------
	// Utils
	//--------------------------------------------------------
	
	private static IRI FIRST = rdfFactory.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#first");
	private static IRI REST = rdfFactory.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest");	
	private static IRI NIL = rdfFactory.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");
	private List<RDFTerm> computeListOfObject(RDFTerm value) {
		List<RDFTerm> result = new ArrayList<>();
		result.add(getObject(model, value, FIRST).get());
		RDFTerm rest = getObject(model, value, REST).get();
		if (!rest.equals(NIL))
			result.addAll(computeListOfObject(rest));
		return result;
	}
	
	
	private Label createLabel(RDFTerm value) {
		if (value instanceof IRI)
			return new Label((IRI) value);
		if (value instanceof BlankNode)
			return new Label((BlankNode) value);
		return null;
	}
	
	
	private void setLabel(ShapeExpr shape,RDFTerm value) {
		if (value instanceof IRI)
			shape.setId(new Label((IRI) value));
		if (value instanceof BlankNode & ! ((BlankNode)value).uniqueReference().startsWith("gen-id"))
			shape.setId(new  Label((BlankNode) value));
	}
	
	
	private void setLabel(TripleExpr triple,RDFTerm value) {
		if (value instanceof IRI)
			triple.setId(new Label((IRI) value));
		if (value instanceof BlankNode & ! ((BlankNode)value).uniqueReference().startsWith("gen-id"))
			triple.setId(new Label((BlankNode) value));
	}
	
}
