package fr.inria.lille.shexjava.util;

import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.rdf4j.RDF4J;

public class RDFFactory {

    private static RDF instance = new RDF4J();

    public static RDF getInstance() {
        return instance;
    }
    
}
