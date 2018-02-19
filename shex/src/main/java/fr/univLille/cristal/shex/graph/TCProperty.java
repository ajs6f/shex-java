package fr.univLille.cristal.shex.graph;

import org.eclipse.rdf4j.model.IRI;

/** Represents a property with an orientation which is forward or backward.
 * Used as predicate in a {@link NeighborTriple}.
 * 
 * @author Iovka Boneva
 * @author Antonin Durey
 *
 */
public class TCProperty{

	private IRI iri;
	private boolean isFwd;
	
	/** Creates a new forward property.
	 * 
	 * @param iri
	 * @return
	 */
	public static TCProperty createFwProperty (IRI iri) {
		return new TCProperty(iri, true);
	}
	
	/** Creates a new backward property.
	 * 
	 * @param iri
	 * @return
	 */
	public static TCProperty createInvProperty (IRI iri) {
		return new TCProperty(iri, false);
	}
	
	private TCProperty (IRI iri, boolean isFwd) {
		this.iri = iri;
		this.isFwd = isFwd;
	}

	/** Tests whether the property is forward.
	 * 
	 * @return
	 */
	public boolean isForward() {
		return isFwd;
	}
	
	/** The encapsulated property. 
	 * 
	 * @return
	 */
	public IRI getIri() {
		return iri;
	}
	
	@Override
	public String toString() {
		return (isFwd ? "" : "^") + iri; 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isFwd ? 1231 : 1237);
		result = prime * result
				+ ((iri == null) ? 0 : iri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TCProperty other = (TCProperty) obj;
		if (isFwd != other.isFwd)
			return false;
		if (iri == null) {
			if (other.iri != null)
				return false;
		} else if (!iri.equals(other.iri))
			return false;
		return true;
	}

}
