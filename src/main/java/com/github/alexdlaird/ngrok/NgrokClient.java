/*
 * Copyright (c) 2021 Alex Laird
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.alexdlaird.ngrok;

import com.github.alexdlaird.exception.JavaNgrokException;
import com.github.alexdlaird.exception.JavaNgrokHTTPException;
import com.github.alexdlaird.http.DefaultHttpClient;
import com.github.alexdlaird.http.HttpClient;
import com.github.alexdlaird.http.HttpClientException;
import com.github.alexdlaird.http.Response;
import com.github.alexdlaird.ngrok.conf.JavaNgrokConfig;
import com.github.alexdlaird.ngrok.installer.NgrokInstaller;
import com.github.alexdlaird.ngrok.process.NgrokProcess;
import com.github.alexdlaird.ngrok.protocol.BindTls;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Proto;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import com.github.alexdlaird.ngrok.protocol.Tunnels;
import com.github.alexdlaird.ngrok.protocol.Version;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * A client for interacting with <a href="https://ngrok.com/docs">ngrok</a>, its binary, and its APIs.
 * Can be configured with {@link JavaNgrokConfig}.
 *
 * <h2>Open a Tunnel</h2>
 * To open a tunnel, use the {@link NgrokClient#connect(CreateTunnel) NgrokClient.connect()} method, which returns a {@link Tunnel}, and
 * this returned object has a reference to the public URL generated by <code>ngrok</code> in its
 * {@link Tunnel#getPublicUrl()} method.
 * <p>
 * <pre>
 * final NgrokClient ngrokClient = new NgrokClient.Builder().build();
 *
 * // Open a HTTP tunnel on the default port 80
 * // &lt;Tunnel: "http://&lt;public_sub&gt;.ngrok.io" -&gt; "http://localhost:80"&gt;
 * final Tunnel httpTunnel = ngrokClient.connect();
 *
 * // Open a SSH tunnel
 * // &lt;Tunnel: "tcp://0.tcp.ngrok.io:12345" -&gt; "localhost:22"&gt;
 * final CreateTunnel sshCreateTunnel = new CreateTunnel.Builder()
 *         .withProto(Proto.TCP)
 *         .withAddr(22)
 *         .build();
 * final Tunnel sshTunnel = ngrokClient.connect(sshCreateTunnel);
 *
 * // Open a tunnel to MySQL with a Reserved TCP Address
 * // &lt;NgrokTunnel: "tcp://1.tcp.ngrok.io:12345" -&gt; "localhost:3306"&gt;
 * final CreateTunnel mysqlCreateTunnel = new CreateTunnel.Builder()
 *         .withProto(Proto.TCP)
 *         .withAddr(3306)
 *         .withRemoteAddr("1.tcp.ngrok.io:12345")
 *         .build();
 * final Tunnel mysqlTunnel = ngrokClient.connect(mysqlCreateTunnel);
 *
 * // Open a tunnel to a local file server
 * // &lt;NgrokTunnel: "http://&lt;public_sub&gt;.ngrok.io" -&gt; "file:///"&gt;
 * final CreateTunnel fileserverCreateTunnel = new CreateTunnel.Builder()
 *         .withAddr("file:///)
 *         .build();
 * final Tunnel fileserverTunnel = ngrokClient.connect(fileserverCreateTunnel);
 * </pre>
 * <p>
 * The {@link NgrokClient#connect(CreateTunnel) NgrokClient.connect()} method can also take a {@link CreateTunnel}
 * (which can be built through {@link CreateTunnel.Builder its Builder}) that allows us to pass additional properties
 * that are <a href="https://ngrok.com/docs#tunnel-definitions">supported by ngrok</a>.
 * <p>
 * <strong>Note:</strong> <code>ngrok</code>'s default behavior for <code>http</code> when no additional properties
 * are passed is to open <em>two</em> tunnels, one <code>http</code> and one <code>https</code>. This method will
 * return a reference to the <code>http</code> tunnel in this case. If only a single tunnel is needed, call
 * {@link CreateTunnel.Builder#withBindTls(BindTls)} with {@link BindTls#TRUE} and a reference to the
 * <code>https</code> tunnel will be returned.
 *
 * <h2>Get Active Tunnels</h2>
 * It can be useful to ask the <code>ngrok</code> client what tunnels are currently open. This can be accomplished
 * with the {@link NgrokClient#getTunnels()} method, which returns a list of {@link Tunnel} objects.
 * <p>
 * <pre>
 * [&lt;Tunnel: "http://&lt;public_sub&gt;.ngrok.io" -&gt; "http://localhost:80"&gt;]
 * final List&lt;Tunnel&gt; tunnels = ngrokClient.getTunnels();
 * </pre>
 *
 * <h2>Close a Tunnel</h2>
 * All open tunnels will automatically be closed when the Java process terminates, but we can also close them
 * manually with {@link NgrokClient#disconnect(String)}.
 * <p>
 * <pre>
 * // The Tunnel returned from methods like connect(), getTunnels(), etc. contains the public URL
 * ngrokClient.disconnect(publicUrl);
 * </pre>
 *
 * <h2>Integration Examples</h2>
 * <code>java-ngrok</code> is useful in any number of integrations, for instance to test locally without having to
 * deploy or configure. Here are some common usage examples.
 * <p>
 * <ul>
 *     <li><a href="https://github.com/alexdlaird/java-ngrok-example-sprint">Spring</a>
 *     <li><a href="https://github.com/alexdlaird/java-ngrok-example-dropwizard">Dropwizard</a>
 *     <li><a href="https://github.com/alexdlaird/java-ngrok-example-play">Play (Scala)</a>
 *     <li><a href="https://gist.github.com/alexdlaird/522cba505b0a9f935f65036355c46f4a">Java HTTP Server</a></li>
 *     <li><a href="https://github.com/alexdlaird/java-ngrok-example-tcp-server-and-client">Java TCP Server and Client</a></li>
 * </ul>
 */
public class NgrokClient {

    private static final Logger LOGGER = Logger.getLogger(String.valueOf(NgrokClient.class));

    private static final String VERSION = "1.5.1";

    private final JavaNgrokConfig javaNgrokConfig;
    private final NgrokProcess ngrokProcess;
    private final HttpClient httpClient;

    private final Map<String, Tunnel> currentTunnels = new HashMap<>();

    private NgrokClient(final Builder builder) {
        this.javaNgrokConfig = builder.javaNgrokConfig;
        this.ngrokProcess = builder.ngrokProcess;
        this.httpClient = builder.httpClient;
    }

    /**
     * Establish a new <code>ngrok</code> tunnel for the tunnel definition, returning an object representing
     * the connected tunnel.
     * <p>
     * If <code>ngrok</code> is not running, calling this method will first start a process with
     * {@link JavaNgrokConfig}.
     * <p>
     * <strong>Note:</strong> <code>ngrok</code>'s default behavior for <code>http</code> when no additional properties
     * are passed is to open <em>two</em> tunnels, one <code>http</code> and one <code>https</code>. This method will
     * return a reference to the <code>http</code> tunnel in this case. If only a single tunnel is needed, call
     * {@link CreateTunnel.Builder#withBindTls(BindTls)} with {@link BindTls#TRUE} and a reference to the
     * <code>https</code> tunnel will be returned.
     *
     * @param createTunnel The tunnel definition.
     * @return The created Tunnel.
     */
    public Tunnel connect(final CreateTunnel createTunnel) {
        ngrokProcess.start();

        final CreateTunnel finalTunnel = interpolateTunnelDefinition(createTunnel);

        LOGGER.info(String.format("Opening tunnel named: %s", finalTunnel.getName()));

        final Response<Tunnel> response;
        try {
            response = httpClient.post(String.format("%s/api/tunnels", ngrokProcess.getApiUrl()), finalTunnel, Tunnel.class);
        } catch (HttpClientException e) {
            throw new JavaNgrokHTTPException(String.format("An error occurred when POSTing to create the tunnel %s.", finalTunnel.getName()),
                    e, e.getUrl(), e.getStatusCode(), e.getBody());
        }

        final Tunnel tunnel;
        if (finalTunnel.getProto() == Proto.HTTP && finalTunnel.getBindTls() == BindTls.BOTH) {
            try {
                final Response<Tunnel> getResponse = httpClient.get(ngrokProcess.getApiUrl() + response.getBody().getUri() + "%20%28http%29", Tunnel.class);
                tunnel = getResponse.getBody();
            } catch (HttpClientException e) {
                throw new JavaNgrokHTTPException(String.format("An error occurred when GETing the HTTP tunnel %s.", response.getBody().getName()),
                        e, e.getUrl(), e.getStatusCode(), e.getBody());
            }
        } else {
            tunnel = response.getBody();
        }

        currentTunnels.put(tunnel.getPublicUrl(), tunnel);

        return tunnel;
    }

    /**
     * See {@link #connect(CreateTunnel)}.
     */
    public Tunnel connect() {
        return connect(new CreateTunnel.Builder().build());
    }

    /**
     * Disconnect the <code>ngrok</code> tunnel for the given URL, if open.
     *
     * @param publicUrl The public URL of the tunnel to disconnect.
     */
    public void disconnect(final String publicUrl) {
        // If ngrok is not running, there are no tunnels to disconnect
        if (!ngrokProcess.isRunning()) {
            return;
        }

        if (!currentTunnels.containsKey(publicUrl)) {
            getTunnels();

            // One more check, if the given URL is still not in the list of tunnels, it is not active
            if (!currentTunnels.containsKey(publicUrl)) {
                return;
            }
        }

        final Tunnel tunnel = currentTunnels.get(publicUrl);

        ngrokProcess.start();

        LOGGER.info(String.format("Disconnecting tunnel: %s", tunnel.getPublicUrl()));

        try {
            httpClient.delete(ngrokProcess.getApiUrl() + tunnel.getUri());
        } catch (HttpClientException e) {
            throw new JavaNgrokHTTPException(String.format("An error occurred when DELETing the tunnel %s.", publicUrl),
                    e, e.getUrl(), e.getStatusCode(), e.getBody());
        }
    }

    /**
     * Get a list of active <code>ngrok</code> tunnels.
     * <p>
     * If <code>ngrok</code> is not running, calling this method will first start a process with
     * {@link JavaNgrokConfig}.
     *
     * @return The active <code>ngrok</code> tunnels.
     */
    public List<Tunnel> getTunnels() {
        ngrokProcess.start();

        try {
            final Response<Tunnels> response = httpClient.get(String.format("%s/api/tunnels", ngrokProcess.getApiUrl()), Tunnels.class);

            currentTunnels.clear();
            for (final Tunnel tunnel : response.getBody().getTunnels()) {
                currentTunnels.put(tunnel.getPublicUrl(), tunnel);
            }

            return new ArrayList<>(currentTunnels.values());
        } catch (HttpClientException e) {
            throw new JavaNgrokHTTPException("An error occurred when GETing the tunnels.", e, e.getUrl(),
                    e.getStatusCode(), e.getBody());
        }
    }

    /**
     * Get the latest metrics for the given {@link Tunnel} and update its <code>metrics</code> attribute.
     *
     * @param tunnel The Tunnel to update.
     */
    public void refreshMetrics(final Tunnel tunnel) {
        Response<Tunnel> latestTunnel = httpClient.get(String.format("%s%s", ngrokProcess.getApiUrl(), tunnel.getUri()), Tunnel.class);

        if (isNull(latestTunnel.getBody().getMetrics()) || latestTunnel.getBody().getMetrics().isEmpty()) {
            throw new JavaNgrokException("The ngrok API did not return \"metrics\" in the response");
        }

        tunnel.setMetrics(latestTunnel.getBody().getMetrics());
    }

    /**
     * Terminate the <code>ngrok</code> processes, if running. This method will not block, it will
     * just issue a kill request.
     */
    public void kill() {
        ngrokProcess.stop();

        currentTunnels.clear();
    }

    /**
     * Set the <code>ngrok</code> auth token in the config file, enabling authenticated features (for instance,
     * more concurrent tunnels, custom subdomains, etc.).
     *
     * @param authToken The auth token.
     */
    public void setAuthToken(final String authToken) {
        ngrokProcess.setAuthToken(authToken);
    }

    /**
     * Update <code>ngrok</code>, if an update is available.
     */
    public void update() {
        ngrokProcess.update();
    }

    /**
     * Get the <code>ngrok</code> and <code>java-ngrok</code> version.
     *
     * @return The versions.
     */
    public Version getVersion() {
        final String ngrokVersion = ngrokProcess.getVersion();

        return new Version(ngrokVersion, VERSION);
    }

    /**
     * Get the <code>java-ngrok</code> to use when interacting with the <code>ngrok</code> binary.
     */
    public JavaNgrokConfig getJavaNgrokConfig() {
        return javaNgrokConfig;
    }

    /**
     * Get the class used to manage the <code>ngrok</code> binary.
     */
    public NgrokProcess getNgrokProcess() {
        return ngrokProcess;
    }

    /**
     * Get the class used to make HTTP requests to <code>ngrok</code>'s APIs.
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    private CreateTunnel interpolateTunnelDefinition(final CreateTunnel createTunnel) {
        final CreateTunnel.Builder createTunnelBuilder = new CreateTunnel.Builder(createTunnel);

        final Map<String, Object> config;
        if (Files.exists(javaNgrokConfig.getConfigPath())) {
            config = ngrokProcess.getNgrokInstaller().getNgrokConfig(javaNgrokConfig.getConfigPath());
        } else {
            config = Collections.emptyMap();
        }

        final String name;
        final Map<String, Object> tunnelDefinitions = (Map<String, Object>) config.getOrDefault("tunnels", Collections.emptyMap());
        if (isNull(createTunnel.getName()) && tunnelDefinitions.containsKey("java-ngrok-default")) {
            name = "java-ngrok-default";
            createTunnelBuilder.withName(name);
        } else {
            name = createTunnel.getName();
        }

        if (nonNull(name) && tunnelDefinitions.containsKey(name)) {
            createTunnelBuilder.withTunnelDefinition((Map<String, Object>) tunnelDefinitions.get(name));
        }

        return createTunnelBuilder.build();
    }

    /**
     * Builder for a {@link NgrokClient}, see docs for that class for example usage.
     */
    public static class Builder {

        private JavaNgrokConfig javaNgrokConfig;
        private NgrokInstaller ngrokInstaller;
        private NgrokProcess ngrokProcess;
        private HttpClient httpClient;

        /**
         * The <code>java-ngrok</code> to use when interacting with the <code>ngrok</code> binary.
         */
        public Builder withJavaNgrokConfig(final JavaNgrokConfig javaNgrokConfig) {
            this.javaNgrokConfig = javaNgrokConfig;
            return this;
        }

        /**
         * The class used to download and install <code>ngrok</code>. Only needed if
         * {@link #withNgrokProcess(NgrokProcess)} is not called.
         */
        public Builder withNgrokInstaller(final NgrokInstaller ngrokInstaller) {
            this.ngrokInstaller = ngrokInstaller;
            return this;
        }

        /**
         * The class used to manage the <code>ngrok</code> binary.
         */
        public Builder withNgrokProcess(final NgrokProcess ngrokProcess) {
            this.ngrokProcess = ngrokProcess;
            return this;
        }

        /**
         * The class used to make HTTP requests to <code>ngrok</code>'s APIs.
         */
        public Builder withHttpClient(final HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public NgrokClient build() {
            if (isNull(javaNgrokConfig)) {
                javaNgrokConfig = new JavaNgrokConfig.Builder().build();
            }
            if (isNull(ngrokInstaller)) {
                ngrokInstaller = new NgrokInstaller();
            }
            if (isNull(ngrokProcess)) {
                ngrokProcess = new NgrokProcess(javaNgrokConfig, ngrokInstaller);
            }
            if (isNull(httpClient)) {
                httpClient = new DefaultHttpClient.Builder().build();
            }

            return new NgrokClient(this);
        }
    }
}
