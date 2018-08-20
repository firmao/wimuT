/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin.lookup.deref.impl;

import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.squin.common.Priority;
import org.squin.common.impl.TaskBase;
import org.squin.dataset.RDFGraphProvenance;
import org.squin.dataset.Triple;
import org.squin.lookup.DataImporter;
import org.squin.lookup.deref.DataAnalyzer;
import org.squin.lookup.deref.DereferencingResult;
import org.squin.lookup.deref.DiscoveredURI;
import org.squin.lookup.deref.URIDerefContext;


/**
 * Base class for implementations of {@link DerefTask}.
 * Primarily, this base class handles the data importing.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
abstract public class DerefTaskBase extends TaskBase<DereferencingResult>
                                    implements DerefTask
{
	// members

	final private Logger log = LoggerFactory.getLogger( DerefTaskBase.class );

	final protected URIDerefContext derefCxt;

	/**
	 * The URL that has to be dereferenced.
	 */
	final public URL url;

// TODO: do we really need IDs at this level?
// Yes, because comparing IDs in the deref manager is more efficient.
// However, maybe we could have an IdentifiedURL that is a URL and has an ID.
	/**
	 * Identifier of {@link #url}.
	 */
	final public int uriID;

	final private DataImporter importer;
	final private Set<DataAnalyzer> analyzers = new HashSet<DataAnalyzer> ();

	protected int tripleCount = 0;
	private boolean startedImporting = false;


	// initialization

	/**
	 * Constructs a dereferencing task.
	 *
	 * @param derefCxt the context of the dereferencing task
	 *                 (mandatory, i.e. must not be null)
	 * @param url the URL that has to be dereferenced
	 *            (mandatory, i.e. must not be null)
	 * @param uriID identifier of the URL that has to be dereferenced
	 * @param priority priority of this task
	 *                 (mandatory, i.e. must not be null)
	 * @param importer data importer for this task
	 *                 (mandatory, i.e. must not be null)
	 */
	public DerefTaskBase ( URIDerefContext derefCxt, URL url, int uriID, Priority priority, DataImporter importer )
	{
		this( derefCxt, url, uriID, priority, importer, null );
	}

	/**
	 * Constructs a dereferencing task.
	 *
	 * @param derefCxt the context of the dereferencing task
	 *                 (mandatory, i.e. must not be null)
	 * @param url the URL that has to be dereferenced
	 *            (mandatory, i.e. must not be null)
	 * @param uriID identifier of the URI that has to be dereferenced
	 * @param priority priority of this task
	 *                 (mandatory, i.e. must not be null)
	 * @param importer data importer for this task
	 *                 (mandatory, i.e. must not be null)
	 * @param analyzer a data analyzer to be registered with this task
	 *                 (optional, i.e. may be null)
	 */
	public DerefTaskBase ( URIDerefContext derefCxt, URL url, int uriID, Priority priority, DataImporter importer, DataAnalyzer analyzer )
	{
		super( priority );

		assert derefCxt != null;
		assert url != null;
		assert importer != null;

		this.derefCxt = derefCxt;
		this.url = url;
		this.uriID = uriID;
		this.importer = importer;

		if ( analyzer != null ) {
			registerDataAnalyzer( analyzer );
		}
	}


	// implementation of the DerefTask interface

	synchronized final public boolean isImporting ()
	{
		return startedImporting;
	}

	synchronized final public void registerDataAnalyzer ( DataAnalyzer da ) throws DerefTaskAlreadyImportingException
	{
		assert da != null;

		if ( isImporting() ) {
			throw new DerefTaskAlreadyImportingException( uriID );
		}

		analyzers.add( da );
	}

	final public boolean isRegisteredDataImporter ( DataImporter importer )
	{
		return this.importer == importer;
	}


	// implementation of the TaskBase<DereferencingResult> abstract methods

	public DereferencingResult createFailureResult ( Exception e )
	{
		return new Failure( uriID, getTimestamp(), getExecutionStartTimestamp(), e );
	}


	// implementation of the Callable interface

	public DereferencingResult call ()
	{
		log.debug( "Started to dereference the URI <{}> (ID: {}).", url.toString(), uriID );

		try {
			HttpURLConnection con = getConnection();
			configureConnection( con );
			establishConnection( con );

			if ( log.isDebugEnabled() ) {
				int responseCode = -1;
				String responseMessage = "?";
				try {
					responseCode = con.getResponseCode();
					responseMessage = con.getResponseMessage();
				} catch ( Exception e ) {
					// ignore this exception
				}

				log.debug( "Connection for URI <{}> (ID: {}) established (response code: {}, response message: {}, content type: {}, content length: {}).",
				           new Object[] { url.toString(),
				                          uriID,
				                          responseCode,
				                          responseMessage,
				                          con.getContentType(),
				                          con.getContentLength() } );
			}

			return handleResponse( con );
		}
		catch ( DereferencingException e ) {
			return new Failure( uriID, getTimestamp(), getExecutionStartTimestamp(), e );
		}
		catch ( Exception e ) {
			String errmsg = "Unexpected exception (type: " + e.getClass().getName() + ", first stack trace element: " + e.getStackTrace()[0].toString() + ") caught while dereferencing URI <" + url.toString() + "> (ID: " + uriID + "): " + e.getMessage();
			log.error( errmsg );
			return new Failure( uriID, getTimestamp(), getExecutionStartTimestamp(), new DereferencingException(errmsg,e) );
		}
	}


	// helpers

	protected HttpURLConnection getConnection () throws DereferencingException
	{
		try {
			return (HttpURLConnection) url.openConnection();
		}
		catch ( IOException e ) {
			String errmsg = "I/O error (type: " + e.getClass().getName() + ") caught while creating a connection for URI <" + url.toString() + "> (ID: " + uriID + "): " + e.getMessage();
			log.error( errmsg );
			throw new DereferencingException( errmsg, e );
		}
		catch ( Exception e ) {
			String errmsg = "Unexpected exception (type: " + e.getClass().getName() + ", first stack trace element: " + e.getStackTrace()[0].toString() + ") caught while creating a connection for URI <" + url.toString() + "> (ID: " + uriID + "): " + e.getMessage();
			log.error( errmsg );
			throw new DereferencingException( errmsg, e );
		}
	}

	protected void configureConnection ( HttpURLConnection con ) throws DereferencingException
	{
		try {
			con.setConnectTimeout( derefCxt.CONNECT_TIMEOUT );
			con.setReadTimeout( derefCxt.READ_TIMEOUT );
			con.setInstanceFollowRedirects( false );
			con.setRequestProperty( "Accept", derefCxt.ACCEPT_HEADER );
			con.setRequestProperty( "User-Agent", derefCxt.USERAGENT_HEADER );
		}
		catch ( Exception e ) {
			String errmsg = "Unexpected exception (type: " + e.getClass().getName() + ", first stack trace element: " + e.getStackTrace()[0].toString() + ") caught while configuring the connection for URI <" + url.toString() + "> (ID: " + uriID + "): " + e.getMessage();
			log.error( errmsg );
			throw new DereferencingException( errmsg, e );
		}
	}

	protected void establishConnection ( HttpURLConnection con ) throws DereferencingException
	{
		try {
			con.connect();
		}
		catch ( SocketTimeoutException e ) {
			String errmsg = "Establishing the connection for URI <" + url.toString() + "> (ID: " + uriID + ") timed out.";
			log.debug( errmsg );
			throw new DereferencingException( errmsg, e );
		}
		catch ( IOException e ) {
			String errmsg = "I/O error (type: " + e.getClass().getName() + ") caught while establishing the connection for URI <" + url.toString() + "> (ID: " + uriID + "): " + e.getMessage();
			log.warn( errmsg );
			throw new DereferencingException( errmsg, e );
		}
	}

	protected int getResponseCode ( HttpURLConnection con ) throws DereferencingException
	{
		int responseCode = -1;
		try {
			responseCode = con.getResponseCode();
		}
		catch ( IOException e ) {
			String errmsg = "I/O error (type: " + e.getClass().getName() + ") caught while getting the response code from the connection for URI <" + url.toString() + "> (ID: " + uriID + "): " + e.getMessage();
			log.warn( errmsg );
			throw new DereferencingException( errmsg, e );
		}

		if ( responseCode == -1 ) {
			String errmsg = "No response code could be discerned from the connection for URI <" + url.toString() + "> (ID: " + uriID + ").";
			log.warn( errmsg );
			throw new DereferencingException( errmsg );
		}

		return responseCode;
	}

	protected DereferencingResult handleResponse ( HttpURLConnection con ) throws DereferencingException
	{
		int responseCode = getResponseCode( con );
		if (    (responseCode == HttpURLConnection.HTTP_MOVED_PERM)   // 301
		     || (responseCode == HttpURLConnection.HTTP_MOVED_TEMP)   // 302
		     || (responseCode == HttpURLConnection.HTTP_SEE_OTHER) )  // 303
		{
			return handleRedirection( con, responseCode );
		}
		else if ( responseCode == HttpURLConnection.HTTP_NOT_MODIFIED ) // 304
		{ 
			throw new UnsupportedOperationException( "JenaIOBasedDerefTask.handleResponse responseCode=304" );
		}
		else if ( responseCode != HttpURLConnection.HTTP_OK ) // not 200
		{
			String errmsg = "Unexpected response code (" + responseCode + ") for URI <" + url.toString() + "> (ID: " + uriID + ").";
			log.debug( errmsg );
			return new Failure( uriID,
			                    getTimestamp(),
			                    getExecutionStartTimestamp(),
			                    new DereferencingException(errmsg),
		                       provideHeaderFields(con) );
		}

		if ( con.getContentLength() > derefCxt.MAXFILESIZE_DEFAULT ) {
			String errmsg = "Content length " + con.getContentLength() + " exceeds the threshold of " + derefCxt.MAXFILESIZE_DEFAULT + " for URI <" + url.toString() + "> (ID: " + uriID + ").";
			log.debug( errmsg );
			return new Failure( uriID,
			                    getTimestamp(),
			                    getExecutionStartTimestamp(),
			                    new DereferencingException(errmsg),
		                       provideHeaderFields(con) );
		}

		InputStream in = null;
		try {
			in = con.getInputStream();
		}
		catch ( IOException e ) {
			String errmsg = "Exception (type: " + e.getClass().getName() + ") caught while obtaining the input stream for URI <" + url.toString() + "> (ID: " + uriID + "): " + e.getMessage();
			log.debug( errmsg );
			return new Failure( uriID,
			                    getTimestamp(),
			                    getExecutionStartTimestamp(),
			                    new DereferencingException(errmsg,e),
		                       provideHeaderFields(con) );
		}

		Set<DiscoveredURI> discoveredURIs;
		try {
			discoveredURIs = handleContent( con.getInputStream(), con.getContentType(), con.getContentEncoding() );
		}
		catch ( SocketTimeoutException e ) {
			String errmsg = "Reading the content retrieved for URI <" + url.toString() + "> (ID: " + uriID + ") timed out.";
			log.debug( errmsg );
			return new Failure( uriID,
			                    getTimestamp(),
			                    getExecutionStartTimestamp(),
			                    new DereferencingException(errmsg,e),
		                       provideHeaderFields(con) );
		}
		catch ( DereferencingException e ) {
			log.debug( e.getMessage() );
			return new Failure( uriID,
			                    getTimestamp(),
			                    getExecutionStartTimestamp(),
			                    e,
		                       provideHeaderFields(con) );
		}
		catch ( Exception e ) {
			String errmsg = "Unexpected exception (type: " + e.getClass().getName() + ", first stack trace element: " + e.getStackTrace()[0].toString() + ") caught while processing the content retrieved for URI <" + url.toString() + "> (ID: " + uriID + "): " + e.getMessage();
			log.debug( errmsg );
			return new Failure( uriID,
			                    getTimestamp(),
			                    getExecutionStartTimestamp(),
			                    new DereferencingException(errmsg,e),
		                       provideHeaderFields(con) );
		}
		finally {
			try {
				in.close();
			}
			catch ( IOException e ) {
				// ignore
				log.warn( "Closing the input stream for URI <{}> (ID: {}) caused a {}: {}",
				           new Object[] { url.toString(),
				                          uriID,
				                          e.getClass().getName(),
				                          e.getMessage() } );
			}
		}

		log.debug( "Successfully finished dereferencing of URI <{}> (ID: {}) - number of imported triples: {}.",
		           new Object[] { url.toString(),
		                          uriID,
		                          tripleCount } );


		Set<URL> sourceURLs = new HashSet<URL> ();
		sourceURLs.add( url );
		return new DataRetrieved( uriID,
		                          getTimestamp(),
		                          getExecutionStartTimestamp(),
		                          sourceURLs,
		                          discoveredURIs,
		                          provideHeaderFields(con) );
	}

	protected DereferencingResult handleRedirection ( HttpURLConnection con, int responseCode )
	{
		String locationField = con.getHeaderField( "Location" );
		if ( locationField == null ) {
			String errmsg = "Redirection (response code: " + responseCode + ") without Location header for URI <" + url.toString() + "> (ID: " + uriID + ").";
			log.debug( errmsg );
// TODO: add getErrorMessage to Failure to avoid creating exceptions where it is unnecessary
			return new Failure( uriID,
			                    getTimestamp(),
			                    getExecutionStartTimestamp(),
			                    new DereferencingException(errmsg),
		                       provideHeaderFields(con) );
		}

		URI redirectionUri;
		try {
			redirectionUri = new URI( locationField );
		}
		catch ( Exception e ) {
			String errmsg = "Exception (type: " + e.getClass().getName() + ") caught while parsing the Location header field for URI <" + url.toString() + "> (ID: " + uriID + "): " + e.getMessage();
			log.debug( errmsg );
			return new Failure( uriID,
			                    getTimestamp(),
			                    getExecutionStartTimestamp(),
			                    new DereferencingException(errmsg),
		                       provideHeaderFields(con) );
		}

		int redirectionUriID = getUriID( redirectionUri );

		log.debug( "URI <{}> (ID: {}) has been redirected to <{}> (ID: {}).",
		           new Object[] { url.toString(),
		                          uriID,
		                          locationField,
		                          redirectionUriID } );

		return new Redirected( uriID,
		                       getTimestamp(),
		                       getExecutionStartTimestamp(),
		                       responseCode,
		                       redirectionUriID,
		                       provideHeaderFields(con) );
	}

	/**
	 * Returns the identifier for the URI with the given string representation.
	 */
	abstract protected int getUriID ( URI uri );

	/**
	 * Handles the content retrieved by dereferencing {@link #url}.
	 * Implementations of this method must use {@link #initiateDataImport} to
	 * initiate the import of data discovered in the content.
	 *
	 * @param inStream the input stream with the content of the HTTP response
	 * @param contentType the content type as provided by the HTTP response
	 *                    (may be null)
	 * @param contentEncoding the encoding of the content as provided by the
	 *                        HTTP response (may be null)
	 * @return a set of additionally relevant URIs discovered in the content
	 *         (may be null)
	 * @throws DereferencingException if it was impossible to retrieve and
	 *                                import data from the content
	 */
	abstract protected Set<DiscoveredURI> handleContent ( InputStream inStream, String contentType, String contentEncoding ) throws DereferencingException;

	/**
	 * To be called by specializations of this class whenever retrieved
	 * data has to be imported by the {@link DataImporter} registered with
	 * this task.
	 *
	 * @param src the URL of the Web resource from which the data has been
	 *            retrieved (to be passed to the {@link DataImporter})
	 * @param data the triples to be imported
	 *             (to be passed to the {@link DataImporter})
	 */
	final protected void initiateDataImport ( URL src, Iterator<Triple> data )
	{
		synchronized ( this ) {
			startedImporting = true;
		}

		Iterator<Triple> it = data;
		for ( DataAnalyzer da : analyzers ) {
			it = da.createDataAnalyzingIterator( it );
		}

		if ( log.isDebugEnabled() ) {
			it = new TripleCountingIterator( it );
		}

		importer.importData( it, new SimpleRDFGraphProvenanceImpl(src) );
	}

	@Override
	public String toString ()
	{
		return "DerefTask(uriID=" + uriID + ")";
	}

	/**
	 * If provenance recording is enabled, this method returns a copy of the
	 * header fields from the response retrieved by the given connection.
	 * If provenance recording is disabled, this method returns null.
	 */
	protected Map<String,List<String>> provideHeaderFields ( HttpURLConnection con )
	{
		if ( ! derefCxt.RECORD_PROVENANCE ) {
			return null;
		}

		Map<String,List<String>> headerFieldsCopy = new HashMap<String,List<String>> ();
		for ( Map.Entry<String,List<String>> headerField : con.getHeaderFields().entrySet() ) {
			headerFieldsCopy.put( headerField.getKey(), new ArrayList<String>(headerField.getValue()) );
		}
		return headerFieldsCopy;
	}


	static public class DereferencingException extends Exception
	{
		public DereferencingException ( String message ) { super(message); }
		public DereferencingException ( String message, Throwable cause ) { super(message,cause); }
	}


	// implementations of the DereferencingResult interface

	static abstract class DereferencingResultBase implements DereferencingResult
	{
		final public int uriID;
		final public long queueTime;
		final public long execTime;
		final public Map<String,List<String>> headerFields;

		public DereferencingResultBase ( int uriID, long taskInitTimestamp, long taskStartTimestamp ) { this( uriID, taskInitTimestamp, taskStartTimestamp, null ); }
		public DereferencingResultBase ( int uriID, long taskInitTimestamp, long taskStartTimestamp, Map<String,List<String>> headerFields )
		{
			this.uriID = uriID;
			queueTime = taskStartTimestamp - taskInitTimestamp;
			execTime = System.currentTimeMillis() - taskStartTimestamp;
			this.headerFields = headerFields;
		}

		public int getURIID () { return uriID; }
		public int getRedirectionCode () { throw new UnsupportedOperationException(); }
		public int getRedirectionURI () { throw new UnsupportedOperationException(); }
		public Set<DiscoveredURI> getDiscoveredURIs () { throw new UnsupportedOperationException(); }
		public Exception getException () { throw new UnsupportedOperationException(); }
		public long getQueueTime () { return queueTime; }
		public long getExecutionTime () { return execTime; }
	}

	static protected class URIsDiscovered extends DereferencingResultBase
	{
		final public Set<DiscoveredURI> discoveredURIs;

		public URIsDiscovered ( int uriID, long taskInitTimestamp, long taskStartTimestamp, Set<DiscoveredURI> discoveredURIs ) { super( uriID, taskInitTimestamp, taskStartTimestamp ); this.discoveredURIs = discoveredURIs; }
		public URIsDiscovered ( int uriID, long taskInitTimestamp, long taskStartTimestamp, Set<DiscoveredURI> discoveredURIs, Map<String,List<String>> headerFields ) { super( uriID, taskInitTimestamp, taskStartTimestamp, headerFields ); this.discoveredURIs = discoveredURIs; }

		public boolean hasBeenRedirected () { return false; }
		public boolean hasDiscoveredOtherURIs () { return true; }
		public boolean isFailure () { return false; }

		@Override
		public Set<DiscoveredURI> getDiscoveredURIs () { return discoveredURIs; }
	}

	static protected class DataRetrieved extends URIsDiscovered
	{
		final public Set<URL> sourceURLs;

		public DataRetrieved ( int uriID, long taskInitTimestamp, long taskStartTimestamp, Set<URL> sourceURLs, Set<DiscoveredURI> discoveredURIs, Map<String,List<String>> headerFields ) { super( uriID, taskInitTimestamp, taskStartTimestamp, discoveredURIs, headerFields ); this.sourceURLs = sourceURLs; }
		public DataRetrieved ( int uriID, long taskInitTimestamp, long taskStartTimestamp, Set<URL> sourceURLs, Set<DiscoveredURI> discoveredURIs ) { this( uriID, taskInitTimestamp, taskStartTimestamp, sourceURLs, discoveredURIs, null ); }
		public DataRetrieved ( int uriID, long taskInitTimestamp, long taskStartTimestamp, Set<URL> sourceURLs ) { this( uriID, taskInitTimestamp, taskStartTimestamp, sourceURLs, null, null ); }
		public DataRetrieved ( int uriID, long taskInitTimestamp, long taskStartTimestamp, Set<URL> sourceURLs, Map<String,List<String>> headerFields ) { this( uriID, taskInitTimestamp, taskStartTimestamp, sourceURLs, null, headerFields ); }

		public boolean hasBeenRedirected () { return false; }
		public boolean hasDiscoveredOtherURIs () { return false; }
		public boolean isFailure () { return false; }
	}

	static protected class Redirected extends DereferencingResultBase
	{
		final public int redirectionCode;
		final public int redirectionUriID;

		public Redirected ( int uriID, long taskInitTimestamp, long taskStartTimestamp, int redirectionCode, int redirectionUriID ) { this( uriID, taskInitTimestamp, taskStartTimestamp, redirectionCode, redirectionUriID, null ); }
		public Redirected ( int uriID, long taskInitTimestamp, long taskStartTimestamp, int redirectionCode, int redirectionUriID, Map<String,List<String>> headerFields ) { super( uriID, taskInitTimestamp, taskStartTimestamp, headerFields ); this.redirectionCode = redirectionCode; this.redirectionUriID = redirectionUriID; }

		public boolean hasBeenRedirected () { return true; }
		public boolean hasDiscoveredOtherURIs () { return false; }
		public boolean isFailure () { return false; }

		@Override
		public int getRedirectionCode () { return redirectionCode; }

		@Override
		public int getRedirectionURI () { return redirectionUriID; }
	}

	static protected class Failure extends DereferencingResultBase
	{
		final public Exception exception;
		public Failure ( int uriID, long taskInitTimestamp, long taskStartTimestamp, Exception exception ) { super( uriID, taskInitTimestamp, taskStartTimestamp ); this.exception = exception; }
		public Failure ( int uriID, long taskInitTimestamp, long taskStartTimestamp, Exception exception, Map<String,List<String>> headerFields ) { super( uriID, taskInitTimestamp, taskStartTimestamp, headerFields ); this.exception = exception; }
		public boolean hasBeenRedirected () { return false; }
		public boolean hasDiscoveredOtherURIs () { return false; }
		public boolean isFailure () { return true; }

		@Override
		public Exception getException () { return exception; }
	}

	class TripleCountingIterator extends DataAnalyzer.DataAnalyzingIterator
	{
		public TripleCountingIterator ( Iterator<Triple> input ) { super( input ); }
		protected void analyze ( Triple t ) { tripleCount++; }
	}

	static protected class SimpleRDFGraphProvenanceImpl implements RDFGraphProvenance
	{
		final public URL accessedResourceURL;
		final public Date retrievalTime;

		public SimpleRDFGraphProvenanceImpl ( URL accessedResourceURL ) {
			assert accessedResourceURL != null;
			this.accessedResourceURL = accessedResourceURL;
			this.retrievalTime = new Date();
		}

		public URL getAccessedResourceURL () { return accessedResourceURL; }
		public Date getRetrievalTime () { return retrievalTime; }
	}

}
