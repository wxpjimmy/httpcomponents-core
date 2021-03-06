/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.hc.core5.http.examples;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.SocketConfig;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.bootstrap.HttpRequester;
import org.apache.hc.core5.http.impl.bootstrap.RequesterBootstrap;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.util.TimeValue;

/**
 * Example of POST requests execution using classic I/O.
 */
public class ClassicPostExecutionExample {

    public static void main(String[] args) throws Exception {
        HttpRequester httpRequester = RequesterBootstrap.bootstrap()
                .setStreamListener(new Http1StreamListener() {

                    @Override
                    public void onRequestHead(final HttpConnection connection, final HttpRequest request) {
                        System.out.println(connection + " " + new RequestLine(request));

                    }

                    @Override
                    public void onResponseHead(final HttpConnection connection, final HttpResponse response) {
                        System.out.println(connection + " " + new StatusLine(response));
                    }

                    @Override
                    public void onExchangeComplete(final HttpConnection connection, final boolean keepAlive) {
                        if (keepAlive) {
                            System.out.println(connection + " can be kept alive");
                        } else {
                            System.out.println(connection + " cannot be kept alive");
                        }
                    }

                })
                .create();
        HttpCoreContext coreContext = HttpCoreContext.create();
        HttpHost target = new HttpHost("httpbin.org");

        HttpEntity[] requestBodies = {
                new StringEntity(
                        "This is the first test request",
                        ContentType.create("text/plain", StandardCharsets.UTF_8)),
                new ByteArrayEntity(
                        "This is the second test request".getBytes(StandardCharsets.UTF_8),
                        ContentType.APPLICATION_OCTET_STREAM),
                new InputStreamEntity(
                        new ByteArrayInputStream(
                                "This is the third test request (will be chunked)"
                                        .getBytes(StandardCharsets.UTF_8)),
                        ContentType.APPLICATION_OCTET_STREAM)
        };

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(5, TimeUnit.SECONDS)
                .build();

        String requestUri = "/post";
        for (int i = 0; i < requestBodies.length; i++) {
            ClassicHttpRequest request = new BasicClassicHttpRequest("POST", target,requestUri);
            request.setEntity(requestBodies[i]);
            try (ClassicHttpResponse response = httpRequester.execute(target, request, TimeValue.ofSeconds(5), coreContext)) {
                System.out.println(requestUri + "->" + response.getCode());
                System.out.println(EntityUtils.toString(response.getEntity()));
                System.out.println("==============");
            }
        }
    }

}
