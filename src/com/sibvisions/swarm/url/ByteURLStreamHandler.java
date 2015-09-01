package com.sibvisions.swarm.url;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class ByteURLStreamHandler extends URLStreamHandler
{
	private final byte[] byContent;

    public ByteURLStreamHandler(byte[] pContent)
    {
        byContent = pContent;
    }

    @Override
    public URLConnection openConnection(URL url)
    {
        return new ByteURLConnection(url, byContent);
    }
    
}

