/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.command.modules;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;

import arq.cmd.CmdException;
import arq.cmdline.ArgDecl;
import arq.cmdline.ArgModuleGeneral;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;

import org.squin.common.Priority;
import org.squin.dataset.QueriedDataset;
import org.squin.dataset.Triple;
import org.squin.dataset.hashimpl.combined.QueriedDatasetImpl;
import org.squin.dataset.jenacommon.JenaIOBasedQueriedDataset;
import org.squin.ldcache.AccessContext;
import org.squin.ldcache.DataRetrievedListener;
import org.squin.ldcache.jenaimpl.JenaIOBasedLinkedDataCache;


/**
 * A command line parameter module that allows us to specify what seed data
 * has to be loaded into the query-local dataset before executing the query.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class ModLDCache implements ArgModuleGeneral, DataRetrievedListener
{
	final protected ModLookup modLookup = new ModLookup ();

	final protected ArgDecl loadDatasetDecl = new ArgDecl( ArgDecl.HasValue, "load", "seed", "loadseed" );
	private String filenameSeedData = null;

	final protected ArgDecl lookupDecl = new ArgDecl( ArgDecl.HasValue, "lookup" );
	private List<URI> lookupURIs = null;

	final private List<Integer> pendingLookups = new ArrayList<Integer> ();

	protected JenaIOBasedQueriedDataset qds = null;
	protected JenaIOBasedLinkedDataCache ldcache = null;


	public void registerWith ( CmdGeneral cmdline )
	{
		modLookup.registerWith( cmdline );

		cmdline.getUsage().startCategory( "Initial Data" );
		cmdline.add( loadDatasetDecl,
		             "--load",
		             "Load a query-local dataset as seed data for the query execution" );
		cmdline.add( lookupDecl,
		             "--lookup",
		             "Look-up URI(s) to retrieve (additional) seed data for the query execution" );
	}

	public void processArgs ( CmdArgModule cmdline ) throws IllegalArgumentException
	{
		modLookup.processArgs( cmdline );

		if ( cmdline.contains(loadDatasetDecl) ) {
			filenameSeedData = cmdline.getValue( loadDatasetDecl );
		}

		if ( cmdline.contains(lookupDecl) ) {
			List<String> lookupURIStrings = cmdline.getValues( lookupDecl );
			lookupURIs = new ArrayList<URI> ();
			for ( String lookupURIString : lookupURIStrings ) {
				try {
					URI lookupURI = new URI( lookupURIString );
					if ( lookupURI.getScheme() == null  ||  ! lookupURI.getScheme().equals("http") ) {
						cmdline.cmdError( "The given URI <" + lookupURIString + "> is not an HTTP scheme based URI." );
					}
					lookupURIs.add( lookupURI );
				}
				catch ( URISyntaxException e ) {
					cmdline.cmdError( "Parsing the given URI <" + lookupURIString + "> caused an exception: " + e.getMessage() );
				}
			}
		}
	}

	public JenaIOBasedLinkedDataCache getLDCache () throws CmdException
	{
		if ( ldcache == null ) {
			ldcache = new JenaIOBasedLinkedDataCache( getQueriedDataset(),
			                                          modLookup.createURILookUpManager(getQueriedDataset()) );

			if ( lookupURIs != null && ! lookupURIs.isEmpty() )
			{
				AccessContext ac = ldcache.registerAccessContext();

				for ( URI lookupURI : lookupURIs ) {
					Node uriNode = Node.createURI( lookupURI.toString() );
					int uriId = qds.nodeDict.createId( uriNode );
					Integer uriID = Integer.valueOf( uriId );
					synchronized( pendingLookups ) {
						if ( ! pendingLookups.contains(uriID) ) {
							if ( ! ldcache.ensureAvailability(ac,uriId,Priority.HIGH,this) ) {
								pendingLookups.add( uriID );
							}
						}
					}
				}

				ldcache.unregisterAccessContext( ac );

				while ( hasPendingLookups() ) {
					try {
						Thread.sleep( 500 ); // 0.5 sec
					} catch ( InterruptedException e ) {
						throw new CmdException( "Waiting for pending look-ups caused an unexpected " + e.getClass().getName() + ": " + e.getMessage(), e );
					}
				}
			}
		}

		return ldcache;
	}

	protected JenaIOBasedQueriedDataset getQueriedDataset () throws CmdException
	{
		if ( qds == null )
		{
			try {
				QueriedDataset wrapped = new QueriedDatasetImpl();
				if ( filenameSeedData != null ) {
					qds = new JenaIOBasedQueriedDataset( filenameSeedData, wrapped );
				} else {
					qds = new JenaIOBasedQueriedDataset( wrapped );
				}
			}
			catch ( Exception e ) {
				throw new CmdException( "Creating the queried dataset caused a " + e.getClass().getName() + ": " + e.getMessage(), e );
			}
		}

		return qds;
	}

	private boolean hasPendingLookups ()
	{
		synchronized( pendingLookups ) {
			return ! pendingLookups.isEmpty();
		}
	}

	// implementation of the DataRetrievedListener interface

	public void ensureAvailabilityFinished ( int uriId )
	{
		synchronized ( pendingLookups ) {
			pendingLookups.remove( Integer.valueOf(uriId) );
		}
	}

}
