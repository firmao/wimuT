/*
    This file is part of SQUIN and it falls under the
    copyright as specified for the whole SQUIN package.
*/
package org.squin;

import java.util.Properties;

/**
 * The configuration of the SQUIN service.
 *
 * @author Olaf Hartig (hartig@informatik.hu-berlin.de)
 */
public class Config
{
	// members
	protected boolean useURIPrefetching;
	protected boolean useQueryResultCache;
	protected String pathOfQueryResultCache;
	protected long maxQueryResultCacheEntryDuration; // in minutes
	
	// NOTE: if you add a variable above, also add a suitable loading call within init ()
	
	// config file related
	private static String KEY_PREFIX = "squin.servlet.";
	// initialization

	public Config () {
		init(new Properties());
	}
	
	public void init (Properties p)
	{
		this.useURIPrefetching = 
				Boolean.parseBoolean(p.getProperty(KEY_PREFIX + "useUriPrefetching","true"));
		this.useQueryResultCache = 
				Boolean.parseBoolean(p.getProperty(KEY_PREFIX + "useQueryResultCache","true"));
		this.pathOfQueryResultCache = 
				p.getProperty(KEY_PREFIX + "pathOfQueryResultCache","/tmp/squin/QueryResultCache");
		this.maxQueryResultCacheEntryDuration = 
				Integer.parseInt(p.getProperty(KEY_PREFIX + "maxQueryResultCacheEntryDuration","1440"));
		
		// NODE: add load call and default value for additional configuration parameters above
	}


	// accessor methods

	public boolean getUseURIPrefetching ()
	{
		return useURIPrefetching;
	}

	public boolean getUseQueryResultCache ()
	{
		return useQueryResultCache;
	}

	public String getPathOfQueryResultCache ()
	{
		return pathOfQueryResultCache;
	}

	public long getMaxQueryResultCacheEntryDuration ()
	{
		return maxQueryResultCacheEntryDuration;
	}

}