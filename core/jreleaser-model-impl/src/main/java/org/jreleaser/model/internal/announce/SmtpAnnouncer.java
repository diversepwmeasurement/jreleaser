/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.model.internal.announce;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.Mail;
import org.jreleaser.model.internal.JReleaserContext;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.KEY_TAG_NAME;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.announce.SmtpAnnouncer.TYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class SmtpAnnouncer extends AbstractAnnouncer<SmtpAnnouncer, org.jreleaser.model.api.announce.SmtpAnnouncer> {
    private static final long serialVersionUID = -1477687439659358188L;

    private final Map<String, String> properties = new LinkedHashMap<>();

    private Mail.Transport transport;
    private String host;
    private Integer port;
    private Boolean auth;
    private String username;
    private String password;
    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String message;
    private String messageTemplate;
    private Mail.MimeType mimeType;

    private final org.jreleaser.model.api.announce.SmtpAnnouncer immutable = new org.jreleaser.model.api.announce.SmtpAnnouncer() {
        private static final long serialVersionUID = -7617403297991452674L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.SmtpAnnouncer.TYPE;
        }

        @Override
        public Mail.Transport getTransport() {
            return transport;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public Integer getPort() {
            return port;
        }

        @Override
        public boolean isAuth() {
            return SmtpAnnouncer.this.isAuth();
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getFrom() {
            return from;
        }

        @Override
        public String getTo() {
            return to;
        }

        @Override
        public String getCc() {
            return cc;
        }

        @Override
        public String getBcc() {
            return bcc;
        }

        @Override
        public String getSubject() {
            return subject;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String getMessageTemplate() {
            return messageTemplate;
        }

        @Override
        public Mail.MimeType getMimeType() {
            return mimeType;
        }

        @Override
        public Map<String, String> getProperties() {
            return unmodifiableMap(properties);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isSnapshotSupported() {
            return SmtpAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return SmtpAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(SmtpAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return SmtpAnnouncer.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }

        @Override
        public Integer getConnectTimeout() {
            return connectTimeout;
        }

        @Override
        public Integer getReadTimeout() {
            return readTimeout;
        }
    };

    public SmtpAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.SmtpAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(SmtpAnnouncer source) {
        super.merge(source);
        this.transport = merge(this.transport, source.transport);
        this.host = merge(this.host, source.host);
        this.port = merge(this.port, source.port);
        this.auth = merge(this.auth, source.auth);
        this.username = merge(this.username, source.username);
        this.password = merge(this.password, source.password);
        this.from = merge(this.from, source.from);
        this.to = merge(this.to, source.to);
        this.cc = merge(this.cc, source.cc);
        this.bcc = merge(this.bcc, source.bcc);
        this.subject = merge(this.subject, source.subject);
        this.message = merge(this.message, source.message);
        this.messageTemplate = merge(this.messageTemplate, source.messageTemplate);
        this.mimeType = merge(this.mimeType, source.mimeType);
        setProperties(merge(this.properties, source.properties));
    }

    public String getResolvedSubject(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getReleaser().getEffectiveTagName(context.getModel()));
        return resolveTemplate(subject, props);
    }

    public String getResolvedMessage(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getReleaser().getEffectiveTagName(context.getModel()));
        return resolveTemplate(message, props);
    }

    public String getResolvedMessageTemplate(JReleaserContext context, Map<String, Object> extraProps) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getReleaser()
            .getEffectiveTagName(context.getModel()));
        props.putAll(extraProps);

        Path templatePath = context.getBasedir().resolve(messageTemplate);
        try {
            Reader reader = java.nio.file.Files.newBufferedReader(templatePath);
            return applyTemplate(reader, props);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_reading_template",
                context.relativizeToBasedir(templatePath)));
        }
    }

    public Mail.Transport getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = Mail.Transport.valueOf(transport.replace(" ", "_")
            .replace("-", "_")
            .toUpperCase(Locale.ENGLISH));
    }

    public void setTransport(Mail.Transport transport) {
        this.transport = transport;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean isAuth() {
        return auth != null && auth;
    }

    public void setAuth(Boolean auth) {
        this.auth = auth;
    }

    public boolean isAuthSet() {
        return auth != null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public Mail.MimeType getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = Mail.MimeType.valueOf(mimeType.replace(" ", "_")
            .replace("-", "_")
            .toUpperCase(Locale.ENGLISH));
    }

    public void setMimeType(Mail.MimeType mimeType) {
        this.mimeType = mimeType;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("transport", transport);
        props.put("host", host);
        props.put("port", port);
        props.put("auth", isAuth());
        props.put("username", username);
        props.put("password", isNotBlank(password) ? HIDE : UNSET);
        props.put("from", from);
        props.put("to", to);
        props.put("cc", cc);
        props.put("bcc", bcc);
        props.put("subject", subject);
        props.put("message", message);
        props.put("messageTemplate", messageTemplate);
        props.put("mimeType", mimeType);
        props.put("properties", properties);
    }
}
