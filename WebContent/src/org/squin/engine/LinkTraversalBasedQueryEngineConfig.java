/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.engine;

import com.hp.hpl.jena.sparql.util.Symbol;


/**
 * The configuration of a {@link LinkTraversalBasedQueryEngine}.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class LinkTraversalBasedQueryEngineConfig
{
	// the available configuration options

	/** enables the look-up of URIs that are in predicate position */
	public static final ConfigOption ENABLE_PREDICATE_LOOKUP = new ConfigOption( "ENABLE_PREDICATE_LOOKUP" );

	/** enables the recording of provenance information during the query execution */
	public static final ConfigOption RECORD_PROVENANCE = new ConfigOption( "RECORD_PROVENANCE" );


	// default values for the configuration options

	public static final boolean ENABLE_PREDICATE_LOOKUP_DEFAULT = false;
	public static final boolean RECORD_PROVENANCE_DEFAULT = false;


	// current values

	private Boolean current_ENABLE_PREDICATE_LOOKUP = ENABLE_PREDICATE_LOOKUP_DEFAULT;
	private Boolean current_RECORD_PROVENANCE = RECORD_PROVENANCE_DEFAULT;


	// generic accessor methods

	/**
	 * Sets a configuration option.
	 *
	 * @param option denotes the configuration option to be set
	 * @param value a string representation of the value
	 * @exception IllegalArgumentException The given value cannot be parsed or
	 *                                     the given config option is unknown.
	 */
	public void setValue ( ConfigOption option, String value ) throws IllegalArgumentException
	{
		if ( option.equals(ENABLE_PREDICATE_LOOKUP) ) {
			current_ENABLE_PREDICATE_LOOKUP = Boolean.valueOf( "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value) || "1".equals(value) );
		}
		else if ( option.equals(RECORD_PROVENANCE) ) {
			current_RECORD_PROVENANCE = Boolean.valueOf( "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value) || "1".equals(value) );
		}
		else {
			throw new IllegalArgumentException( "Unknown config option (" + option.toString() + ")." );
		}
	}

	/**
	 * Sets a boolean configuration option.
	 *
	 * @param option denotes the configuration option to be set
	 * @param value the value
	 */
	public void setValue ( ConfigOption option, boolean value ) throws IllegalArgumentException
	{
		setValue( option,
		          value ? "true" : "false" );
	}

	/**
	 * Returns an object representation of the value for the given configuration
	 * option.
	 *
	 * @param option denotes the configuration option in question
	 */
	public Object getValue ( ConfigOption option )
	{
		if ( option.equals(ENABLE_PREDICATE_LOOKUP) ) {
			return current_ENABLE_PREDICATE_LOOKUP;
		}
		else if ( option.equals(RECORD_PROVENANCE) ) {
			return current_RECORD_PROVENANCE;
		}

		throw new IllegalArgumentException();
	}

	/**
	 * Returns true if the given configuration option is set to true (for
	 * boolean options only).
	 *
	 * @exception IllegalArgumentException The given option is not boolean.
	 */
	public boolean isTrue ( ConfigOption option ) throws IllegalArgumentException
	{
		Object v = getValue( option );
		if ( v instanceof Boolean ) {
			return ( (Boolean) v ).booleanValue();
		}
		else {
			throw new IllegalArgumentException( "The config option '" + option.toString() + "' does not has a boolean value." );
		}
	}

	// helpers

	static class ConfigOption extends Symbol
	{
		protected ConfigOption ( String name ) { super("org.squin.engine.LinkTraversalBasedQueryEngineConfig."+name); }
	}

}
