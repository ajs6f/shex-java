package fr.univLille.cristal.shex.schema.concrsynt;

import java.util.Set;

import org.eclipse.rdf4j.model.Value;

public class IRIStemRangeConstraint extends StemRangeConstraint {

	public IRIStemRangeConstraint(Constraint stem, Set<Value> exclusionsValues, Set<Constraint> exclusionsConstraints) {
		super(stem, new ValueSetValueConstraint (exclusionsValues,exclusionsConstraints));
	}

}