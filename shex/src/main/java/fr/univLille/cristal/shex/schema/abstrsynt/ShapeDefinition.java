/**
Copyright 2017 University of Lille

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package fr.univLille.cristal.shex.schema.abstrsynt;

/**
 * 
 * @author Iovka Boneva
 * 10 oct. 2017
 */
public class ShapeDefinition implements AbstractSyntaxElement {
	
	public final ShapeExpression expression;
	
	public ShapeDefinition (ShapeExpression expression) {
		this.expression = expression;
	}
	
	
	private final ASElementAttributes attributes = new ASElementAttributes();
	public final ASElementAttributes getAttributes() {
		return this.attributes;
	}
	
	
	@Override
	public String toString() {
		return expression.toString();
	}
}