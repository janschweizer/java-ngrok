/*
 * Copyright (c) 2023 Alex Laird
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

package com.github.alexdlaird.ngrok.protocol;

import com.github.alexdlaird.http.HttpClient;
import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.conf.JavaNgrokConfig;
import com.github.alexdlaird.ngrok.installer.NgrokVersion;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * An object that represents a <code>ngrok</code> Tunnel creation request. This object can be serialized
 * and passed to the {@link HttpClient}.
 *
 * <h3>Basic Usage</h3>
 * <pre>
 * final NgrokClient ngrokClient = new NgrokClient.Builder().build();
 *
 * final CreateTunnel createTunnel = new CreateTunnel.Builder()
 *         .withName("my-tunnel")
 *         .withProto(Proto.TCP)
 *         .withAddr(5000)
 *         .build();
 *
 * final HttpClient httpClient = new DefaultHttpClient.Builder().build()
 * final Response&lt;SomePOJOResponse&gt; postResponse = httpClient.post("http://localhost:4040/api/tunnels",
 *                                                                 createTunnel,
 *                                                                 Tunnel.class);
 * </pre>
 * <h2><code>ngrok</code> Version Compatibility</h2>
 * <code>java-ngrok</code> is compatible with <code>ngrok</code> v2 and v3, but by default it will install v3. To
 * install v2 instead, set the version with {@link JavaNgrokConfig.Builder#withNgrokVersion(NgrokVersion)}
 * and {@link CreateTunnel.Builder#withNgrokVersion(NgrokVersion)}.
 */
public class CreateTunnel {
    private transient final NgrokVersion ngrokVersion;
    private final String name;
    private final Proto proto;
    private final String addr;
    private final Boolean inspect;
    private final String auth;
    private final String hostHeader;
    private final BindTls bindTls;
    private final String subdomain;
    private final String hostname;
    private final String crt;
    private final String key;
    private final String clientCas;
    private final String remoteAddr;
    private final String metadata;
    private final List<String> schemes;
    private final List<String> basicAuth;
	private final OAuth oauth;

    private CreateTunnel(final Builder builder) {
        this.ngrokVersion = builder.ngrokVersion;
        this.name = builder.name;
        this.proto = builder.proto;
        this.addr = builder.addr;
        this.inspect = builder.inspect;
        this.auth = builder.auth;
        this.hostHeader = builder.hostHeader;
        this.bindTls = builder.bindTls;
        this.subdomain = builder.subdomain;
        this.hostname = builder.hostname;
        this.crt = builder.crt;
        this.key = builder.key;
        this.clientCas = builder.clientCas;
        this.remoteAddr = builder.remoteAddr;
        this.metadata = builder.metadata;
        this.schemes = builder.schemes;
        this.basicAuth = builder.basicAuth;
		this.oauth = builder.oauth;
    }

    /**
     * Get the version of <code>ngrok</code> for which the tunnel was created.
     */
    public NgrokVersion getNgrokVersion() {
        return ngrokVersion;
    }

    /**
     * Get the name of the tunnel.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the tunnel protocol.
     */
    public Proto getProto() {
        return proto;
    }

    /**
     * Get the local port to which the tunnel will forward traffic.
     */
    public String getAddr() {
        return addr;
    }

    /**
     * Whether HTTP request inspection on tunnels is enabled.
     */
    public Boolean isInspect() {
        return inspect;
    }

    /**
     * Get HTTP basic authentication credentials enforced on tunnel requests.
     */
    public String getAuth() {
        return auth;
    }

    /**
     * Get the HTTP Host header.
     */
    public String getHostHeader() {
        return hostHeader;
    }

    /**
     * Get <code>ngrok</code>'s <code>bind_tls</code> value.
     */
    public BindTls getBindTls() {
        return bindTls;
    }

    /**
     * Get the subdomain.
     */
    public String getSubdomain() {
        return subdomain;
    }

    /**
     * Get the hostname.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Get the PEM TLS certificate path that will be used to terminate TLS traffic before forwarding locally.
     */
    public String getCrt() {
        return crt;
    }

    /**
     * Get the PEM TLS private key path that will be used to terminate TLS traffic before forwarding locally.
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the PEM TLS certificate authority path that will be used to verify incoming TLS client
     * connection certificates.
     */
    public String getClientCas() {
        return clientCas;
    }

    /**
     * Get the bound remote TCP port on the given address.
     */
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /**
     * Get the arbitrary user-defined metadata that will appear in the ngrok service API when listing tunnels.
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * Get the schemes to be bound.
     */
    public List<String> getSchemes() {
        return schemes;
    }
	
	/**
     * Get the OAuth settings to be activated on the tunnel
     */
    public OAuth getOauth() {
      return oauth;
    }

    /**
     * Builder for a {@link CreateTunnel}, which can be used to construct a request that conforms to
     * <a href="https://ngrok.com/docs/ngrok-agent/api#start-tunnel" target="_blank"><code>ngrok</code>'s tunnel definition</a>.
     * See docs for that class for example usage.
     */
    public static class Builder {
        private boolean setDefaults = false;

        private NgrokVersion ngrokVersion;
        private String name;
        private Proto proto;
        private String addr;
        private Boolean inspect;
        private BindTls bindTls;
        private String auth;
        private String hostHeader;
        private String subdomain;
        private String hostname;
        private String crt;
        private String key;
        private String clientCas;
        private String remoteAddr;
        private String metadata;
        private List<String> schemes;
        private List<String> basicAuth;
		private OAuth oauth;

        /**
         * Use this constructor if default values should not be populated in required attributes when {@link #build()}
         * is called.
         * <p>
         * If required attributes are not set in the built {@link CreateTunnel}, default values will be used in methods
         * like {@link NgrokClient#connect(CreateTunnel)}.
         */
        public Builder() {
        }

        /**
         * Use this constructor if default values should be populated in required attributes when {@link #build()}
         * is called.
         *
         * @param setDefaults <code>true</code> to populate defaults.
         */
        public Builder(final boolean setDefaults) {
            this.setDefaults = setDefaults;
        }

        /**
         * Copy a {@link CreateTunnel} in to a new Builder. Using this constructor will also set default attributes
         * when {@link #build} is called.
         *
         * @param createTunnel The CreateTunnel to copy.
         */
        public Builder(final CreateTunnel createTunnel) {
            this.setDefaults = true;

            this.ngrokVersion = createTunnel.ngrokVersion;
            this.name = createTunnel.name;
            this.proto = createTunnel.proto;
            this.addr = createTunnel.addr;
            this.inspect = createTunnel.inspect;
            this.bindTls = createTunnel.bindTls;
            this.auth = createTunnel.auth;
            this.hostHeader = createTunnel.hostHeader;
            this.subdomain = createTunnel.subdomain;
            this.hostname = createTunnel.hostname;
            this.crt = createTunnel.crt;
            this.key = createTunnel.key;
            this.clientCas = createTunnel.clientCas;
            this.remoteAddr = createTunnel.remoteAddr;
            this.metadata = createTunnel.metadata;
            this.schemes = createTunnel.schemes;
            this.basicAuth = createTunnel.basicAuth;
			this.oauth = createTunnel.oauth;
        }

        /**
         * The major version of <code>ngrok</code> for which the tunnel will be created.
         */
        public Builder withNgrokVersion(final NgrokVersion ngrokVersion) {
            this.ngrokVersion = ngrokVersion;
            return this;
        }

        /**
         * The name of the tunnel.
         */
        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        /**
         * The tunnel protocol, defaults to {@link Proto#HTTP}.
         */
        public Builder withProto(final Proto proto) {
            this.proto = proto;
            return this;
        }

        /**
         * The local port to which the tunnel will forward traffic, or a
         * <a href="https://ngrok.com/docs#http-file-urls">local directory or network address</a>, defaults to "80"
         */
        public Builder withAddr(final String addr) {
            this.addr = addr;
            return this;
        }

        /**
         * See {@link #withAddr(String)}.
         */
        public Builder withAddr(final int addr) {
            return withAddr(String.valueOf(addr));
        }

        /**
         * Disable HTTP request inspection on tunnels.
         */
        public Builder withoutInspect() {
            this.inspect = false;
            return this;
        }

        /**
         * HTTP basic authentication credentials to enforce on tunneled requests.
         */
        public Builder withAuth(final String auth) {
            if (nonNull(basicAuth)) {
                throw new IllegalArgumentException("Cannot set both 'auth' and 'basicAuth'.");
            }

            this.auth = auth;
            return this;
        }

        /**
         * Rewrite the HTTP Host header to this value, or <code>preserve</code> to leave it unchanged.
         */
        public Builder withHostHeader(final String hostHeader) {
            this.hostHeader = hostHeader;
            return this;
        }

        /**
         * Bind an HTTPS ({@link BindTls#TRUE} or HTTP ({@link BindTls#FALSE}) endpoint, defaults
         * to {@link BindTls#BOTH}.
         */
        public Builder withBindTls(final BindTls bindTls) {
            if (nonNull(schemes)) {
                throw new IllegalArgumentException("Cannot set both 'schemes' and 'bindTls'.");
            }

            this.bindTls = bindTls;
            return this;
        }

        /**
         * See {@link #withBindTls(BindTls)}.
         */
        public Builder withBindTls(final boolean bindTls) {
            return withBindTls(BindTls.valueOf(String.valueOf(bindTls).toUpperCase()));
        }

        /**
         * Subdomain name to request. If unspecified, uses the tunnel name.
         */
        public Builder withSubdomain(final String subdomain) {
            this.subdomain = subdomain;
            return this;
        }

        /**
         * Hostname to request (requires reserved name and DNS CNAME).
         */
        public Builder withHostname(final String hostname) {
            this.hostname = hostname;
            return this;
        }

        /**
         * PEM TLS certificate at this path to terminate TLS traffic before forwarding locally.
         */
        public Builder withCrt(final String crt) {
            this.crt = crt;
            return this;
        }

        /**
         * PEM TLS private key at this path to terminate TLS traffic before forwarding locally.
         */
        public Builder withKey(final String key) {
            this.key = key;
            return this;
        }

        /**
         * PEM TLS certificate authority at this path will verify incoming TLS client connection certificates.
         */
        public Builder withClientCas(final String clientCas) {
            this.clientCas = clientCas;
            return this;
        }

        /**
         * Bind the remote TCP port on the given address.
         */
        public Builder withRemoteAddr(final String remoteAddr) {
            this.remoteAddr = remoteAddr;
            return this;
        }

        /**
         * Arbitrary user-defined metadata that will appear in the ngrok service API when listing tunnels.
         */
        public Builder withMetadata(final String metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * The schemes to be bound.
         */
        public Builder withSchemes(final List<String> schemes) {
            if (nonNull(bindTls)) {
                throw new IllegalArgumentException("Cannot set both 'schemes' and 'bindTls'.");
            }

            this.schemes = schemes;
            return this;
        }

        /**
         * List of HTTP basic authentication credentials to enforce on tunneled requests.
         */
        public Builder withBasicAuth(final List<String> basicAuth) {
            if (nonNull(auth)) {
                throw new IllegalArgumentException("Cannot set both 'auth' and 'basicAuth'.");
            }

            this.basicAuth = basicAuth;
            return this;
        }
		
		/**
		 * Set of OAuth settings to enable OAuth authentication on the tunnel endpoint
		 */
		public Builder withOAuth(OAuth oauth) {
          this.oauth = oauth;
          return this;
        }

        /**
         * Populate any <code>null</code> attributes (with the exception of <code>name</code>) in this Builder with
         * values from the given <code>tunnelDefinition</code>.
         *
         * @param tunnelDefinition The map from which <code>null</code> attributes will be populated.
         */
        public Builder withTunnelDefinition(Map<String, Object> tunnelDefinition) {
            if (isNull(this.proto) && tunnelDefinition.containsKey("proto")) {
                this.proto = Proto.valueOf(((String) tunnelDefinition.get("proto")).toUpperCase());
            }
            if (isNull(this.addr) && tunnelDefinition.containsKey("addr")) {
                this.addr = (String) tunnelDefinition.get("addr");
            }
            if (isNull(this.inspect) && tunnelDefinition.containsKey("inspect")) {
                this.inspect = Boolean.valueOf(String.valueOf(tunnelDefinition.get("inspect")));
            }
            if (isNull(this.bindTls) && tunnelDefinition.containsKey("bind_tls")) {
                this.bindTls = BindTls.valueOf((String.valueOf(tunnelDefinition.get("bind_tls"))).toUpperCase());
            }
            if (isNull(this.auth) && tunnelDefinition.containsKey("auth")) {
                this.auth = (String) tunnelDefinition.get("auth");
            }
            if (isNull(this.hostHeader) && tunnelDefinition.containsKey("host_header")) {
                this.hostHeader = (String) tunnelDefinition.get("host_header");
            }
            if (isNull(this.subdomain) && tunnelDefinition.containsKey("subdomain")) {
                this.subdomain = (String) tunnelDefinition.get("subdomain");
            }
            if (isNull(this.hostname) && tunnelDefinition.containsKey("hostname")) {
                this.hostname = (String) tunnelDefinition.get("hostname");
            }
            if (isNull(this.crt) && tunnelDefinition.containsKey("crt")) {
                this.crt = (String) tunnelDefinition.get("crt");
            }
            if (isNull(this.key) && tunnelDefinition.containsKey("key")) {
                this.key = (String) tunnelDefinition.get("key");
            }
            if (isNull(this.clientCas) && tunnelDefinition.containsKey("client_cas")) {
                this.clientCas = (String) tunnelDefinition.get("client_cas");
            }
            if (isNull(this.remoteAddr) && tunnelDefinition.containsKey("remote_addr")) {
                this.remoteAddr = (String) tunnelDefinition.get("remote_addr");
            }
            if (isNull(this.metadata) && tunnelDefinition.containsKey("metadata")) {
                this.metadata = (String) tunnelDefinition.get("metadata");
            }
            if (isNull(this.schemes) && tunnelDefinition.containsKey("schemes")) {
                this.schemes = (List<String>) tunnelDefinition.get("schemes");
            }
            if (isNull(this.schemes) && tunnelDefinition.containsKey("schemes")) {
                this.basicAuth = (List<String>) tunnelDefinition.get("basic_auth");
            }
            //Returning this to allow chained configuration of
            //properties not visible in ngrok's GET /api/tunnels endpoint 
            return this;
        }

        public CreateTunnel build() {
            if (isNull(ngrokVersion)) {
                ngrokVersion = NgrokVersion.V3;
            }

            if (setDefaults) {
                if (isNull(proto)) {
                    proto = Proto.HTTP;
                }
                if (isNull(addr)) {
                    addr = "80";
                }
                if (isNull(name)) {
                    if (!addr.startsWith("file://")) {
                        name = String.format("%s-%s-%s", proto, addr, UUID.randomUUID());
                    } else {
                        name = String.format("%s-file-%s", proto, UUID.randomUUID());
                    }
                }
                if (ngrokVersion == NgrokVersion.V2 && isNull(bindTls)) {
                    bindTls = BindTls.BOTH;
                }
                if (ngrokVersion == NgrokVersion.V3) {
                    if (nonNull(bindTls)) {
                        if (bindTls == BindTls.TRUE) {
                            schemes = List.of("https");
                        } else if (bindTls == BindTls.FALSE) {
                            schemes = List.of("http");
                        } else {
                            schemes = List.of("http", "https");
                        }

                        bindTls = null;
                    }
                    if (nonNull(auth)) {
                        basicAuth = List.of(auth);

                        auth = null;
                    }
                }
            }

            return new CreateTunnel(this);
        }
    }
}