/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.command.modules;

import com.hp.hpl.jena.sparql.resultset.ResultsFormat;

import arq.cmdline.ArgDecl;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;


/**
 * A command line parameter module that allows us to specify how query results
 * have to be reported.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class ModResultsOut extends arq.cmdline.ModResultsOut
{
	final protected ArgDecl disableStreamingOutputDecl = new ArgDecl( ArgDecl.NoValue, "disable-streaming-output", "no-streaming-output" );
	private boolean streaming;

	final protected ArgDecl enableProvenanceRecordingDecl = new ArgDecl( ArgDecl.NoValue, "provenance", "enable-provenance-recording" );
	private boolean provenance;

	@Override
	public void registerWith ( CmdGeneral cmdline )
	{
		super.registerWith( cmdline );
		cmdline.add( disableStreamingOutputDecl,
		             "--disable-streaming-output",
		             "Disables streaming output if result set format is 'text'" );  
		cmdline.add( enableProvenanceRecordingDecl,
		             "--provenance",
		             "Enables the recording of provenance information during the query execution" );  
	}

	@Override
	public void processArgs ( CmdArgModule cmdline ) throws IllegalArgumentException
	{
		super.processArgs( cmdline );

		streaming =    ( getResultsFormat().equals(ResultsFormat.FMT_TEXT) || getResultsFormat().equals(ResultsFormat.FMT_UNKNOWN) )
		            && ! cmdline.contains( disableStreamingOutputDecl );

		provenance = cmdline.contains( enableProvenanceRecordingDecl );
	}

	public boolean isStreamingEnabled ()
	{
		return streaming;
	}

	public boolean isProvenanceRecordingEnabled ()
	{
		return provenance;
	}

}
