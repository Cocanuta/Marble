package com.planet_ink.marble_mud.core.interfaces;

/* 
Copyright 2012 Ben Cherrington

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
/**
* A utility interface for applying "each" code to iterable objects
* @author Bo Zimmerman
*
*/
public interface EachApplicable<T>
{
	/**
	 * Implement the code that will apply to each object
	 * @param a the object to work on
	 */
	public void apply(final T a);
}
