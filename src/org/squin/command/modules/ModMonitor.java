/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.command.modules;

import arq.cmdline.ArgDecl;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;
import arq.cmdline.ModTime;


/**
 * A command line parameter module that allows
 * us to measure and to monitor query execution.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class ModMonitor extends ModTime
{
	final protected ArgDecl statisticsDecl = new ArgDecl( ArgDecl.NoValue, "statistics" );
	private boolean statistics = false;

	@Override
	public void registerWith ( CmdGeneral cmdline )
	{
		cmdline.getUsage().startCategory( "Monitoring" );
		cmdline.add( timeDecl,
		             "--time",
		             "Time the operation" );
		cmdline.add( statisticsDecl,
		             "--statistics",
		             "Print out statistics about the query system after query execution" );
	}

	@Override
	public void processArgs ( CmdArgModule cmdline ) throws IllegalArgumentException
	{
		super.processArgs( cmdline );
		statistics = cmdline.contains( statisticsDecl );
	}

	public boolean statisticsEnabled ()
	{
		return statistics;
	}
    
	public void setStatisticsEnabled ( boolean statisticsEnabled )
	{
		statistics = statisticsEnabled;
	}

}
