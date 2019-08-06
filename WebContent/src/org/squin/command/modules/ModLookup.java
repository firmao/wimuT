/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.command.modules;

import arq.cmdline.ArgDecl;
import arq.cmdline.ArgModuleGeneral;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;

import org.squin.dataset.jenacommon.JenaIOBasedQueriedDataset;
import org.squin.lookup.jenaimpl.JenaIOBasedURILookUpManager;


/**
 * A command line parameter module that allows users to configure
 * different aspects of the URI lookup processes performed during
 * the query execution.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class ModLookup implements ArgModuleGeneral
{
	final protected ArgDecl uriSearchDecl = new ArgDecl( ArgDecl.NoValue, "enable-urisearch", "urisearch" );
	private boolean enableURISearch = false;


	public void registerWith ( CmdGeneral cmdline )
	{
		cmdline.getUsage().startCategory( "URI Lookup" );
		cmdline.add( uriSearchDecl,
		             "--enable-urisearch",
		             "Enable URI search as part of the URI look-up process during query execution" );
	}

	public void processArgs ( CmdArgModule cmdline )
	{
		enableURISearch = cmdline.contains( uriSearchDecl );
	}

	public boolean isURISearchEnabled ()
	{
		return enableURISearch;
	}

	public JenaIOBasedURILookUpManager createURILookUpManager ( JenaIOBasedQueriedDataset dataset )
	{
		return new JenaIOBasedURILookUpManager( dataset, isURISearchEnabled() );
	}
}
