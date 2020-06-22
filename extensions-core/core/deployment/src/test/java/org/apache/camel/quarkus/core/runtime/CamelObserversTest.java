/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.quarkus.core.runtime;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.test.QuarkusUnitTest;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.event.RouteStartedEvent;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class CamelObserversTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelObserversTest.class);

    @RegisterExtension
    static final QuarkusUnitTest CONFIG = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    EventHandler handler;

    @Test
    public void testObservers() {
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(handler.routes()).contains("myRoute");
        });
    }

    @ApplicationScoped
    public static class EventHandler {
        private final Set<String> routes = new CopyOnWriteArraySet<>();

        public void onRouteStarted(@Observes RouteStartedEvent event) {
            routes.add(event.getRoute().getRouteId());
        }

        public Set<String> routes() {
            return routes;
        }
    }

    @ApplicationScoped
    public static class MyRoutes extends RouteBuilder {
        @Override
        public void configure() throws Exception {
            from("direct:start")
                    .routeId("myRoute")
                    .log("${body}");
        }
    }
}