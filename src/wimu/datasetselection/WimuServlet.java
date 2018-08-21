/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.wimu.datasetselection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squin.Constants;
import org.squin.cache.QueryResultCache;
import org.squin.cache.impl.QueryResultCacheImpl;
import org.squin.engine.LinkTraversalBasedQueryEngine;
import org.squin.engine.LinkedDataCacheWrappingDataset;
import org.squin.ldcache.jenaimpl.JenaIOBasedLinkedDataCache;
import org.squin.servlet.DirectResultRequestParameters;
import org.squin.servlet.Servlet;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;
import com.hp.hpl.jena.sparql.util.Utils;

/**
 * The servlet that processes requests to the SQUIN service.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class WimuServlet extends Servlet {
	// members

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static private Logger logger = LoggerFactory.getLogger(WimuServlet.class);

	// initialization

	public WimuServlet() {
	}

	// implementation of the HttpServlet interface

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		if (req.getParameter("chkDataset") != null) {
			try {
				String query = req.getParameter("txtQuery");
				String[] datasets = req.getParameterValues("chkDataset");
				for (String dataset : datasets) {
					if (dataset.contains("hdt.lod.labs.vu.nl")) {
						resp.getOutputStream().println("<!DOCTYPE html>");
						resp.getOutputStream().println("<html>");
						resp.getOutputStream().println("<body>");
						resp.getOutputStream().println("Dataset not compatible with SPARQL:<br>"+query+"<br><br>");
						resp.getOutputStream().println("You can generate the CBD from the URIs using <a href='http://wimu.aksw.org/'>wimu.aksw.org</a>");
						resp.getOutputStream().println("</body>");
						resp.getOutputStream().println("</html>");
						resp.flushBuffer();
					}else if(!dataset.toLowerCase().contains("hdt")){
						resp.getOutputStream().println("<!DOCTYPE html>");
						resp.getOutputStream().println("<html>");
						resp.getOutputStream().println("<body>");
						resp.getOutputStream().println("Dataset is not an HDT file<br>");
						resp.getOutputStream().println("Please execute the query:<br>"+query+"<br><br> at <a href='"+dataset+"'>"+dataset+"</a>");
						resp.getOutputStream().println("</body>");
						resp.getOutputStream().println("</html>");
						resp.flushBuffer();
					} else {
						org.apache.jena.query.ResultSet res = WimuSelection.execQueryHDTRes(query, dataset);
						org.apache.jena.query.ResultSetFormatter.outputAsJSON(resp.getOutputStream(), res);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		QueryResultCache cache = null;
		if (getConfig().getUseQueryResultCache()) {
			cache = new QueryResultCacheImpl(getConfig().getPathOfQueryResultCache(),
					getConfig().getMaxQueryResultCacheEntryDuration());
		}

		logger.info("Start processing request {} with {}.", req.hashCode(), getLinkedDataCache().toString());
		// logger.info( "real path: {}", getServletContext().getRealPath("WW")
		// );
		logger.info("getInitialFilesDirectory: {}", getInitialFilesDirectory());

		String accept = req.getHeader("ACCEPT");
		if (accept != null && !accept.contains(Constants.MIME_TYPE_RESULT_XML)
				&& !accept.contains(Constants.MIME_TYPE_XML1) && !accept.contains(Constants.MIME_TYPE_XML2)
				&& !accept.contains(Constants.MIME_TYPE_RESULT_JSON) && !accept.contains(Constants.MIME_TYPE_JSON)
				&& !accept.contains("application/*") && !accept.contains("*/*")) {
			logger.info("NOT ACCEPTABLE for request {} (ACCEPT header field: {})", req.hashCode(), accept);

			try {
				resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE,
						"Your client does not seem to accept one of the possible content types (e.g. '"
								+ Constants.MIME_TYPE_RESULT_XML + "', '" + Constants.MIME_TYPE_RESULT_JSON + "').");
			} catch (IOException e) {
				logger.error("Sending the error reponse to request " + req.hashCode() + " caused a "
						+ Utils.className(e) + ": " + e.getMessage(), e);
			}

			return;
		}

		// get (and check) the request parameters
		DirectResultRequestParameters params = new DirectResultRequestParameters();
		if (!params.process(req)) {
			logger.info("BAD REQUEST for request {}: {}", req.hashCode(), params.getErrorMsgs());

			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, params.getErrorMsgs());
			} catch (IOException e) {
				logger.error("Sending the error reponse to request " + req.hashCode() + " caused a "
						+ Utils.className(e) + ": " + e.getMessage(), e);
			}

			return;
		}

		InputStream cachedResultSet = null;
		ResultSetMem resultSet = null;
		if (!params.getIgnoreQueryCache() && cache != null
				&& cache.hasResults(params.getQueryString(), params.getResponseContentType())) {
			logger.info("Found cached result set for request {} with query: {}", req.hashCode(),
					params.getQueryString());

			cachedResultSet = cache.getResults(params.getQueryString(), params.getResponseContentType());
		} else {
			// execute the query
			logger.info("Start executing request {} with query:", req.hashCode());
			logger.info(params.getQueryString());

			LinkTraversalBasedQueryEngine.register();
			JenaIOBasedLinkedDataCache ldcache = getLinkedDataCache();
			QueryExecution qe = QueryExecutionFactory.create(params.getQuery(),
					new LinkedDataCacheWrappingDataset(ldcache));
			resultSet = new ResultSetMem(qe.execSelect());
			if (resultSet.size() < 1) {
				wimuAction(resp, params.getQuery());
				return;
			}
			logger.info("Created the result set (size: {}) for request {}.", resultSet.size(), req.hashCode());
		}

		// create the response
		OutputStream out = null;
		try {
			out = resp.getOutputStream();
		} catch (IOException e) {
			logger.error("Getting the response output stream for request " + req.hashCode() + " caused a "
					+ Utils.className(e) + ": " + e.getMessage(), e);

			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (IOException e2) {
				logger.error("Sending the error reponse for request " + req.hashCode() + " caused a "
						+ Utils.className(e2) + ": " + e2.getMessage(), e2);
			}

			return;
		}

		// write the response
		resp.setContentType(params.getResponseContentType());
		try {
			if (cachedResultSet == null) {
				if (params.getResponseContentType() == Constants.MIME_TYPE_RESULT_JSON) {
					ResultSetFormatter.outputAsJSON(out, resultSet);
				} else {
					ResultSetFormatter.outputAsXML(out, resultSet);
				}
			} else {
				copy(cachedResultSet, out);
			}

			logger.info("Result written to the response stream for request {}.", req.hashCode());
		} catch (Exception e) {
			logger.error("Writing the model to the response stream for request {} caused a {}: {}",
					new Object[] { req.hashCode(), Utils.className(e), e.getMessage() });
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (IOException e2) {
				logger.error("Sending an error response for request " + req.hashCode() + " caused a "
						+ Utils.className(e2) + ": " + e2.getMessage(), e2);
			}
		}

		// finish
		try {
			out.flush();
			resp.flushBuffer();
			out.close();
			logger.info("Response buffer for request {} flushed.", req.hashCode());
		} catch (IOException e) {
			logger.error("Flushing the response buffer for request " + req.hashCode() + " caused a "
					+ Utils.className(e) + ": " + e.getMessage(), e);
		}

		logger.info("Finished processing request {} with {}.", req.hashCode(), getLinkedDataCache().toString());

		if (cache != null && cachedResultSet == null) {
			cache.cacheResults(params.getQueryString(), resultSet);
		}
	}

	// helper methods

	private void wimuAction(HttpServletResponse resp, Query query) {
		try {
			boolean onlyDatasets = true;
			WimuResult wResult = WimuSelection.execQuery(query.toString(), onlyDatasets);
			resp.getOutputStream().println("<!DOCTYPE html>");
			resp.getOutputStream().println("<html>");
			resp.getOutputStream().println("<body>");
			resp.getOutputStream().println("<h3>No results with SQUIN</h3><br> "
					+ "<h3>You can still execute your query because the URIs were found on Datasets from WIMU..</h3>");
			resp.getOutputStream().println("<form action='querywimu'>");
			resp.getOutputStream().println("Query:<br>");
			resp.getOutputStream().println(
					"<textarea name='txtQuery' id='txtQuery' rows='8' cols='80'>" + query.toString() + "</textarea>");
			resp.getOutputStream().println("<br>---------------");
			resp.getOutputStream().println("<br>Choose the dataset to execute the query: <br>");
			wResult.getDatasets().forEach((uri, dataset) -> {
				try {
					// resp.getOutputStream().println("URI: " + uri);
					// resp.getOutputStream().println("Dataset: " +dataset);
					resp.getOutputStream().println("<input type='checkbox' name='chkDataset' value='" + dataset + "'> "
							+ "<a href='" + dataset + "'>" + dataset + "</a><br>");
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			resp.getOutputStream().println("<input type='submit' value='Download the dataset and execute the query'>");
			resp.getOutputStream().println("</form>");
			resp.getOutputStream().println("</body>");
			resp.getOutputStream().println("</html>");
			resp.flushBuffer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int n = 0;
		while ((n = in.read(buffer)) != -1) {
			out.write(buffer, 0, n);
		}
	}
}