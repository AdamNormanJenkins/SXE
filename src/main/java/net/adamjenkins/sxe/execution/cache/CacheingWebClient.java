/*
 * Copyright 2020 Adam Norman Jenkins.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */
package net.adamjenkins.sxe.execution.cache;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A web client that caches results to speed things up.
 *
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class CacheingWebClient extends WebClient {

    private static final Logger log = LoggerFactory.getLogger(WebClient.class);

    private HashMap<DocumentCacheKey, Page> cache = new HashMap<DocumentCacheKey, Page>();

    public CacheingWebClient(BrowserVersion browserVersion, String proxyHost, int proxyPort) {
        super(browserVersion, proxyHost, proxyPort);
    }

    public CacheingWebClient(BrowserVersion browserVersion) {
        super(browserVersion);
    }

    public CacheingWebClient() {
    }   

    @Override
    public <P extends Page> P getPage(String url) throws IOException, FailingHttpStatusCodeException, MalformedURLException {
        DocumentCacheKey key = new DocumentCacheKey(new URL(url));
        if(log.isInfoEnabled()){
            log.info("Requesting document " + key + " from cache");
        }
        if(!cache.containsKey(key)){
            log.info("Page not in cache!!!  Adding now.");
            P p = super.<P>getPage(url);
            cache.put(key, p);
        }else{
            log.info("Retrieving cached version of page");
        }
        return (P)cache.get(key);
    }

    @Override
    public <P extends Page> P getPage(URL url) throws IOException, FailingHttpStatusCodeException {
        DocumentCacheKey key = new DocumentCacheKey(url);
        if(log.isInfoEnabled()){
            log.info("Requesting document " + key + " from cache");
        }
        if(!cache.containsKey(key)){
            log.info("Page not in cache!!!  Adding now.");
            P p = super.<P>getPage(url);
            cache.put(key, p);
        }else{
            log.info("Retrieving cached version of page");
        }
        return (P)cache.get(key);
    }

    @Override
    public <P extends Page> P getPage(WebRequest request) throws IOException, FailingHttpStatusCodeException {
        DocumentCacheKey key = new DocumentCacheKey(request.getUrl(), request.getRequestParameters(), request.getHttpMethod());
        if(log.isInfoEnabled()){
            log.info("Requesting document " + key + " from cache");
        }
        if(!cache.containsKey(key)){
            log.info("Page not in cache!!!  Adding now.");
            P p = super.<P>getPage(request);
            cache.put(key, p);
        }else{
            log.info("Retrieving cached version of page");
        }
        return (P)cache.get(key);
    }

}
