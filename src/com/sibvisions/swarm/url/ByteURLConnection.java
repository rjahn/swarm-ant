package com.sibvisions.swarm.url;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ByteURLConnection extends URLConnection
{
	/** the content. */
    private final byte[] byContent;

    public ByteURLConnection(URL pUrl, byte[] pContent)
    {
        super(pUrl);
        
        byContent = pContent;
        setUseCaches(false);
    }

    @Override
    public void connect()
    {
    }

    @Override
    public InputStream getInputStream()
    {
    	if (byContent == null)
    	{
    		return null;
    	}
    	else
    	{
    		return new ByteArrayInputStream(byContent);
    	}
    }

}	// ByteURLConnection
