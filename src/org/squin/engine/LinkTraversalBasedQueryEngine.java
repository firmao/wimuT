/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Symbol;

import org.squin.common.Priority;
import org.squin.dataset.query.arq.QueryEngine;
import org.squin.dataset.query.arq.VarDictionary;
import org.squin.ldcache.AccessContext;
import org.squin.ldcache.jenaimpl.JenaIOBasedLinkedDataCache;


/**
 * A query engine that executes SPARQL queries over the Web of Linked Data
 * by applying the link traversal based query execution paradigm.
 * To use this engine within the ARQ query processing framework you simply
 * have i) to register it by calling its {@link #register} method and ii) the
 * {@link com.hp.hpl.jena.query.QueryExecution} object must have been created
 * with a dataset that is a {@link LinkedDataCacheWrappingDatasetGraph}.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class LinkTraversalBasedQueryEngine extends QueryEngine
{
	static private Logger log = LoggerFactory.getLogger( LinkTraversalBasedQueryEngine.class );
	static private QueryEngineFactory factory;

	static public final Symbol ctxtKeyConfig = Symbol.create( "org.squin.engine.LinkTraversalBasedQueryEngine.ctxtKeyConfig" );

	/**
	 * Returns a factory that creates a {@link LinkTraversalBasedQueryEngine}.
	 */
	static public QueryEngineFactory getFactory ()
	{
		if ( factory == null ) {
			factory = new LinkTraversalBasedQueryEngineFactory();
		}
		return factory; 
	}

	/**
	 * Registers this engine so that it can be selected for query execution.
	 */
	static public void register ()
	{
		QueryEngineRegistry.addFactory( getFactory() );
	}

	/**
	 * Unregisters this engine.
	 */
	static public void unregister ()
	{
		QueryEngineRegistry.removeFactory( getFactory() );
	}


	// initialization methods

	public LinkTraversalBasedQueryEngine ( Op op, DatasetGraph dataset, Binding input, Context context )
	{
		super( op, dataset, input, context );
		registerOpExecutor( this.context );
	}

	public LinkTraversalBasedQueryEngine( Query query, DatasetGraph dataset, Binding input, Context context )
	{
		super( query, dataset, input, context );
		registerOpExecutor( this.context );
	}

	static public void registerOpExecutor ( Context context )
	{
		QC.setFactory( context, OpExecutor.getFactory() );
	}


	// operations

	@Override
	protected Op modifyOp ( Op op )
	{
		// We need to apply the query rewriting rules that come with ARQ. In
		// particular, we need the rule TransformJoinStrategy because this one
		// replaces OpLeftJoin with OpConditional and this results in using
		// QueryIterOptionalIndex which implements a streaming evaluation of
		// left joins (instead of a materialization of the RHS implemented by
		// QueryIterLeftJoin). The materialization based left join does not work
		// for link traversal based query execution because it materializes the
		// intermediate solutions for the RHS too early.
		// However, we don't want the TransformFilterPlacement rule because it
		// breaks up BGPs. Hence, we have to disable filterPlacement.
		//                                                  Olaf,  June 9, 2010
		context.setFalse( ARQ.optFilterPlacement );
		return super.modifyOp( op );
	}

	@Override
	public QueryIterator eval ( Op op, DatasetGraph dsg, Binding input, Context context )
	{
		if ( SUBSTITUE && ! input.isEmpty() ) {
			op = Substitute.substitute( op, input );
		}

		LinkTraversalBasedExecutionContext execCxt = createExecutionContext( op, (LinkedDataCacheWrappingDatasetGraph) dsg, context );
		prefetchSeedURIs( op, execCxt );
		return createIteratorChain( op, input, execCxt );
	}


	// helpers

	protected LinkTraversalBasedExecutionContext createExecutionContext ( Op op, LinkedDataCacheWrappingDatasetGraph dsg, Context context )
	{
		LinkTraversalBasedQueryEngineConfig config = context.isDefined( ctxtKeyConfig ) ? (LinkTraversalBasedQueryEngineConfig) context.get( ctxtKeyConfig ) : new LinkTraversalBasedQueryEngineConfig();
		VarDictionary varDict = initializeVarDictionary( op );

		return new LinkTraversalBasedExecutionContext( config.isTrue(LinkTraversalBasedQueryEngineConfig.ENABLE_PREDICATE_LOOKUP),
		                                               dsg.ldcache.getNodeDictionary(),
		                                               varDict,
		                                               config.isTrue(LinkTraversalBasedQueryEngineConfig.RECORD_PROVENANCE),
		                                               context,
		                                               dsg.getDefaultGraph(),
		                                               dsg,
		                                               QC.getFactory(context) ) ;
	}

	/** initializes prefetching of the URIs in the query */
	protected void prefetchSeedURIs ( Op op, LinkTraversalBasedExecutionContext execCxt )
	{
		URICollector c = new URICollector( execCxt.predicateLookUpEnabled );
		OpWalker.walk( op, c );
		log.debug( "Prefetching seed URIs ..." );
		for ( Node uriNode : c.getURIs() ) {
			log.debug( "Requesting seed URI {}", uriNode.getURI() );
			( (JenaIOBasedLinkedDataCache) execCxt.ldcache ).ensureAvailability( execCxt.accessContext, uriNode, Priority.HIGH );
		}
		log.debug( "... requesting seed URIs finished." );
	}

}
