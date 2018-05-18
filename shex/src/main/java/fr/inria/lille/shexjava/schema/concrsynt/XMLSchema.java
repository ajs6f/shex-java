package fr.inria.lille.shexjava.schema.concrsynt;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;

import fr.inria.lille.shexjava.util.RDFFactory;

/**
 * Constants for the standard <a href="http://www.w3.org/TR/xmlschema-2/">XML
 * Schema datatypes</a>.
 * 
 * Translated to Commons RDF types from RDF4J's {@code XMLSchema} class.
 * 
 * @see <a href="http://www.w3.org/TR/xmlschema-2/">XML Schema Part 2: Datatypes
 *      Second Edition</a>
 */
public class XMLSchema {

    /*
     * The XML Schema namespace
     */

    /** The XML Schema namespace (<tt>http://www.w3.org/2001/XMLSchema#</tt>). */
    public static final String NAMESPACE = "http://www.w3.org/2001/XMLSchema#";

    /*
     * Primitive datatypes
     */

    /** <tt>http://www.w3.org/2001/XMLSchema#duration</tt> */
    public final static IRI DURATION;

    /** <tt>http://www.w3.org/2001/XMLSchema#dateTime</tt> */
    public final static IRI DATETIME;

    /** <tt>http://www.w3.org/2001/XMLSchema#dayTimeDuration</tt> */
    public static final IRI DAYTIMEDURATION;

    /** <tt>http://www.w3.org/2001/XMLSchema#time</tt> */
    public final static IRI TIME;

    /** <tt>http://www.w3.org/2001/XMLSchema#date</tt> */
    public final static IRI DATE;

    /** <tt>http://www.w3.org/2001/XMLSchema#gYearMonth</tt> */
    public final static IRI GYEARMONTH;

    /** <tt>http://www.w3.org/2001/XMLSchema#gYear</tt> */
    public final static IRI GYEAR;

    /** <tt>http://www.w3.org/2001/XMLSchema#gMonthDay</tt> */
    public final static IRI GMONTHDAY;

    /** <tt>http://www.w3.org/2001/XMLSchema#gDay</tt> */
    public final static IRI GDAY;

    /** <tt>http://www.w3.org/2001/XMLSchema#gMonth</tt> */
    public final static IRI GMONTH;

    /** <tt>http://www.w3.org/2001/XMLSchema#string</tt> */
    public final static IRI STRING;

    /** <tt>http://www.w3.org/2001/XMLSchema#boolean</tt> */
    public final static IRI BOOLEAN;

    /** <tt>http://www.w3.org/2001/XMLSchema#base64Binary</tt> */
    public final static IRI BASE64BINARY;

    /** <tt>http://www.w3.org/2001/XMLSchema#hexBinary</tt> */
    public final static IRI HEXBINARY;

    /** <tt>http://www.w3.org/2001/XMLSchema#float</tt> */
    public final static IRI FLOAT;

    /** <tt>http://www.w3.org/2001/XMLSchema#decimal</tt> */
    public final static IRI DECIMAL;

    /** <tt>http://www.w3.org/2001/XMLSchema#double</tt> */
    public final static IRI DOUBLE;

    /** <tt>http://www.w3.org/2001/XMLSchema#anyURI</tt> */
    public final static IRI ANYURI;

    /** <tt>http://www.w3.org/2001/XMLSchema#QName</tt> */
    public final static IRI QNAME;

    /** <tt>http://www.w3.org/2001/XMLSchema#NOTATION</tt> */
    public final static IRI NOTATION;

    /*
     * Derived datatypes
     */

    /** <tt>http://www.w3.org/2001/XMLSchema#normalizedString</tt> */
    public final static IRI NORMALIZEDSTRING;

    /** <tt>http://www.w3.org/2001/XMLSchema#token</tt> */
    public final static IRI TOKEN;

    /** <tt>http://www.w3.org/2001/XMLSchema#language</tt> */
    public final static IRI LANGUAGE;

    /** <tt>http://www.w3.org/2001/XMLSchema#NMTOKEN</tt> */
    public final static IRI NMTOKEN;

    /** <tt>http://www.w3.org/2001/XMLSchema#NMTOKENS</tt> */
    public final static IRI NMTOKENS;

    /** <tt>http://www.w3.org/2001/XMLSchema#Name</tt> */
    public final static IRI NAME;

    /** <tt>http://www.w3.org/2001/XMLSchema#NCName</tt> */
    public final static IRI NCNAME;

    /** <tt>http://www.w3.org/2001/XMLSchema#ID</tt> */
    public final static IRI ID;

    /** <tt>http://www.w3.org/2001/XMLSchema#IDREF</tt> */
    public final static IRI IDREF;

    /** <tt>http://www.w3.org/2001/XMLSchema#IDREFS</tt> */
    public final static IRI IDREFS;

    /** <tt>http://www.w3.org/2001/XMLSchema#ENTITY</tt> */
    public final static IRI ENTITY;

    /** <tt>http://www.w3.org/2001/XMLSchema#ENTITIES</tt> */
    public final static IRI ENTITIES;

    /** <tt>http://www.w3.org/2001/XMLSchema#integer</tt> */
    public final static IRI INTEGER;

    /** <tt>http://www.w3.org/2001/XMLSchema#long</tt> */
    public final static IRI LONG;

    /** <tt>http://www.w3.org/2001/XMLSchema#int</tt> */
    public final static IRI INT;

    /** <tt>http://www.w3.org/2001/XMLSchema#short</tt> */
    public final static IRI SHORT;

    /** <tt>http://www.w3.org/2001/XMLSchema#byte</tt> */
    public final static IRI BYTE;

    /** <tt>http://www.w3.org/2001/XMLSchema#nonPositiveInteger</tt> */
    public final static IRI NON_POSITIVE_INTEGER;

    /** <tt>http://www.w3.org/2001/XMLSchema#negativeInteger</tt> */
    public final static IRI NEGATIVE_INTEGER;

    /** <tt>http://www.w3.org/2001/XMLSchema#nonNegativeInteger</tt> */
    public final static IRI NON_NEGATIVE_INTEGER;

    /** <tt>http://www.w3.org/2001/XMLSchema#positiveInteger</tt> */
    public final static IRI POSITIVE_INTEGER;

    /** <tt>http://www.w3.org/2001/XMLSchema#unsignedLong</tt> */
    public final static IRI UNSIGNED_LONG;

    /** <tt>http://www.w3.org/2001/XMLSchema#unsignedInt</tt> */
    public final static IRI UNSIGNED_INT;

    /** <tt>http://www.w3.org/2001/XMLSchema#unsignedShort</tt> */
    public final static IRI UNSIGNED_SHORT;

    /** <tt>http://www.w3.org/2001/XMLSchema#unsignedByte</tt> */
    public final static IRI UNSIGNED_BYTE;

    /** <tt>http://www.w3.org/2001/XMLSchema#yearMonthDuration</tt> */
    public static final IRI YEARMONTHDURATION;

    static {
        RDF factory = RDFFactory.getInstance();

        DURATION = factory.createIRI(NAMESPACE + "duration");

        DATETIME = factory.createIRI(NAMESPACE + "dateTime");

        DAYTIMEDURATION = factory.createIRI(NAMESPACE + "dayTimeDuration");

        TIME = factory.createIRI(NAMESPACE + "time");

        DATE = factory.createIRI(NAMESPACE + "date");

        GYEARMONTH = factory.createIRI(NAMESPACE + "gYearMonth");

        GYEAR = factory.createIRI(NAMESPACE + "gYear");

        GMONTHDAY = factory.createIRI(NAMESPACE + "gMonthDay");

        GDAY = factory.createIRI(NAMESPACE + "gDay");

        GMONTH = factory.createIRI(NAMESPACE + "gMonth");

        STRING = factory.createIRI(NAMESPACE + "string");

        BOOLEAN = factory.createIRI(NAMESPACE + "boolean");

        BASE64BINARY = factory.createIRI(NAMESPACE + "base64Binary");

        HEXBINARY = factory.createIRI(NAMESPACE + "hexBinary");

        FLOAT = factory.createIRI(NAMESPACE + "float");

        DECIMAL = factory.createIRI(NAMESPACE + "decimal");

        DOUBLE = factory.createIRI(NAMESPACE + "double");

        ANYURI = factory.createIRI(NAMESPACE + "anyURI");

        QNAME = factory.createIRI(NAMESPACE + "QName");

        NOTATION = factory.createIRI(NAMESPACE + "NOTATION");

        NORMALIZEDSTRING = factory.createIRI(NAMESPACE + "normalizedString");

        TOKEN = factory.createIRI(NAMESPACE + "token");

        LANGUAGE = factory.createIRI(NAMESPACE + "language");

        NMTOKEN = factory.createIRI(NAMESPACE + "NMTOKEN");

        NMTOKENS = factory.createIRI(NAMESPACE + "NMTOKENS");

        NAME = factory.createIRI(NAMESPACE + "Name");

        NCNAME = factory.createIRI(NAMESPACE + "NCName");

        ID = factory.createIRI(NAMESPACE + "ID");

        IDREF = factory.createIRI(NAMESPACE + "IDREF");

        IDREFS = factory.createIRI(NAMESPACE + "IDREFS");

        ENTITY = factory.createIRI(NAMESPACE + "ENTITY");

        ENTITIES = factory.createIRI(NAMESPACE + "ENTITIES");

        INTEGER = factory.createIRI(NAMESPACE + "integer");

        LONG = factory.createIRI(NAMESPACE + "long");

        INT = factory.createIRI(NAMESPACE + "int");

        SHORT = factory.createIRI(NAMESPACE + "short");

        BYTE = factory.createIRI(NAMESPACE + "byte");

        NON_POSITIVE_INTEGER = factory.createIRI(NAMESPACE + "nonPositiveInteger");

        NEGATIVE_INTEGER = factory.createIRI(NAMESPACE + "negativeInteger");

        NON_NEGATIVE_INTEGER = factory.createIRI(NAMESPACE + "nonNegativeInteger");

        POSITIVE_INTEGER = factory.createIRI(NAMESPACE + "positiveInteger");

        UNSIGNED_LONG = factory.createIRI(NAMESPACE + "unsignedLong");

        UNSIGNED_INT = factory.createIRI(NAMESPACE + "unsignedInt");

        UNSIGNED_SHORT = factory.createIRI(NAMESPACE + "unsignedShort");

        UNSIGNED_BYTE = factory.createIRI(NAMESPACE + "unsignedByte");

        YEARMONTHDURATION = factory.createIRI(NAMESPACE + "yearMonthDuration");
    }

    private static final RDF4J rdf4j = new RDF4J();

    public static boolean isValidValue(String lexicalForm, IRI datatype) {
        // TODO factor out RDF4J entirely?
        return XMLDatatypeUtil.isValidValue(lexicalForm, (org.eclipse.rdf4j.model.IRI) rdf4j.asValue(datatype));
    }

    public static boolean isValidDouble(String lexicalForm) {
        // TODO factor out RDF4J entirely?
        return XMLDatatypeUtil.isValidDouble(lexicalForm);
    }

    public static String normalize(String lexicalForm, IRI datatype) {
        // TODO factor out RDF4J entirely?
        return XMLDatatypeUtil.normalize(lexicalForm, (org.eclipse.rdf4j.model.IRI) rdf4j.asValue(datatype));
    }
}
