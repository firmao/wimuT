/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.lookup.deref.impl;

import org.squin.common.Priority;
import org.squin.common.Task;
import org.squin.lookup.DataImporter;
import org.squin.lookup.deref.DataAnalyzer;
import org.squin.lookup.deref.DereferencingResult;


/**
 * This interface represents a task to
 * dereference an HTTP-scheme based URIs.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public interface DerefTask extends Task<DereferencingResult>
{
	/**
	 * Returns true if the given {@link org.squin.lookup.DataImporter} is the same
	 * as the {@link org.squin.lookup.DataImporter} registered with this task.
	 */
	public boolean isRegisteredDataImporter ( DataImporter importer );

	/**
	 * Returns true if this task already started importing retrieved data.
	 */
	public boolean isImporting ();

	/**
	 * Registers the given {@link org.squin.lookup.deref.DataAnalyzer} with this
	 * task.
	 *
	 * @throws DerefTaskAlreadyImportingException if this task already started
	 *                  importing retrieved data (i.e. if {@link #isImporting}
	 *                  returns true).
	 */
	public void registerDataAnalyzer ( DataAnalyzer da ) throws DerefTaskAlreadyImportingException;

	/**
	 * Upgrades the priority of this task to the given priority.
	 *
	 * @param newPriority the new priority
	 * @throws IllegalArgumentException if the given priority is less important
	 *                                  than the current priority of this task
	 */
	public void upgradePriority ( Priority newPriority ) throws IllegalArgumentException;
}
