/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.command;

import java.io.PrintStream;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModQueryIn;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;

import org.squin.engine.LinkedDataCacheWrappingDataset;
import org.squin.engine.LinkTraversalBasedQueryEngine;
import org.squin.engine.LinkTraversalBasedQueryEngineConfig;
import org.squin.ldcache.jenaimpl.JenaIOBasedLinkedDataCache;

import org.squin.command.modules.ModLDCache;
import org.squin.command.modules.ModMonitor;
import org.squin.command.modules.ModResultsOut;


/**
 * The command line tool for SQUIN.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class query extends CmdARQ
{
	protected ModQueryIn modQuery = new ModQueryIn ();
	protected ModLDCache modCache = new ModLDCache ();
	protected ModResultsOut modResults = new ModResultsOut ();
	protected ModMonitor modMonitor = new ModMonitor ();


	static public void main ( String[] args )
	{
		new query( args ).mainRun();
	}
    
	public query ( String[] args )
	{
		super( args );

		super.addModule( modQuery );
		super.addModule( modCache );
		super.addModule( modResults );
		super.addModule( modMonitor );
	}

	@Override
	protected String getCommandName ()
	{
		return getClass().getName();
	}
    
	@Override
	protected String getSummary ()
	{
		return getCommandName() + " --query=<queryfile> | <query>";
	}

// 	@Override
// 	protected void processModulesAndArgs ()
// 	{
// 		super.processModulesAndArgs();
// 	}
    
	@Override
	protected void exec ()
	{
		JenaIOBasedLinkedDataCache ldcache = null;
		try {
			LinkTraversalBasedQueryEngine.register();

			ldcache = modCache.getLDCache();
			Dataset dsARQ = new LinkedDataCacheWrappingDataset( ldcache );

			Query query = modQuery.getQuery() ;

			modMonitor.startTimer();
			QueryExecution qe = QueryExecutionFactory.create( query, dsARQ );

			LinkTraversalBasedQueryEngineConfig config = qe.getContext().isDefined( LinkTraversalBasedQueryEngine.ctxtKeyConfig ) ? (LinkTraversalBasedQueryEngineConfig) qe.getContext().get( LinkTraversalBasedQueryEngine.ctxtKeyConfig ) : new LinkTraversalBasedQueryEngineConfig();
			config.setValue( LinkTraversalBasedQueryEngineConfig.RECORD_PROVENANCE, modResults.isProvenanceRecordingEnabled() );
			qe.getContext().set( LinkTraversalBasedQueryEngine.ctxtKeyConfig, config );

			execQuery( query, qe );

			long time = modMonitor.endTimer();
			if ( modMonitor.timingEnabled() ) {
				System.out.println( "Time: " + modMonitor.timeStr(time) + " sec" );
			}

			if ( modMonitor.statisticsEnabled() ) {
				System.out.println( "Statistics:" );
				ldcache.getStatistics().print( System.out, 1 );
			}

			qe.close();
			ldcache.shutdownNow( 4000 ); // 4 sec.
		}
		catch ( Exception e ) {
			System.err.println( "Executing the query caused a " + e.getClass().getName() + ": " + e.getMessage() );
			e.printStackTrace( System.err );

			if ( ldcache != null && ! ldcache.isShutdown() ) {
				try {
					ldcache.shutdownNow( 4000 ); // 4 sec.
				} catch ( Exception e2 ) {
					System.err.println( "Emergency shut down of the Linked Data cache caused a " + e.getClass().getName() + ": " + e.getMessage() );
				}
			}
		}
	}

	protected void execQuery ( Query query, QueryExecution qe )
	{
		if ( query.isSelectType() && modResults.isStreamingEnabled() ) {
			PrintStream out = System.out;
			ResultSet results = qe.execSelect();
			System.out.println("If is empty, then, call WIMU to find a dataset to query. Need to confirm, need also a second Opnion, ask Saleem and Tommaso."); 
			while ( results.hasNext() ) {
				QuerySolution s = results.nextSolution();
				out.format( "%03d | ", results.getRowNumber() );
				for ( String var : results.getResultVars() ) {
					String b = ( s.contains(var) ) ? s.get(var).toString() : "     " ;
					out.format( "?%s: %s \t ", var, b );
				}
				out.println();
				out.flush();
			}
		}
		else {
			QueryExecUtils.executeQuery( query, qe, modResults.getResultsFormat() );
		}
	}

}
