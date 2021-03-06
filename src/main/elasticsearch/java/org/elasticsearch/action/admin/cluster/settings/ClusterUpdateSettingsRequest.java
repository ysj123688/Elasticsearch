/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.action.admin.cluster.settings;

import com.google.common.collect.Sets;
import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.action.ValidateActions.addValidationError;
import static org.elasticsearch.common.settings.Settings.Builder.EMPTY_SETTINGS;
import static org.elasticsearch.common.settings.Settings.readSettingsFromStream;
import static org.elasticsearch.common.settings.Settings.writeSettingsToStream;

/**
 * Request for an update cluster settings action
 */
public class ClusterUpdateSettingsRequest extends AcknowledgedRequest<ClusterUpdateSettingsRequest> {

    private Settings transientSettings = EMPTY_SETTINGS;
    private Settings persistentSettings = EMPTY_SETTINGS;
    private Set<String> transientSettingsToRemove = Sets.newHashSet();
    private Set<String> persistentSettingsToRemove = Sets.newHashSet();
    private String tenantName;

    public ClusterUpdateSettingsRequest() {
    }

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = null;
        if (transientSettings.getAsMap().isEmpty() && persistentSettings.getAsMap().isEmpty()
                && transientSettingsToRemove.size() == 0
                && persistentSettingsToRemove.size() == 0) {
            validationException = addValidationError("no settings to update", validationException);
        }
        return validationException;
    }

    public Settings transientSettings() {
        return transientSettings;
    }

    public Settings persistentSettings() {
        return persistentSettings;
    }
    
    public String tenantName() {
        return this.tenantName;
    }
    
    public void tenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    /**
     * Sets the transient settings to be updated. They will not survive a full cluster restart
     */
    public ClusterUpdateSettingsRequest transientSettings(Settings settings) {
        this.transientSettings = settings;
        return this;
    }

    /**
     * Sets the transient settings to be updated. They will not survive a full cluster restart
     */
    public ClusterUpdateSettingsRequest transientSettings(Settings.Builder settings) {
        this.transientSettings = settings.build();
        return this;
    }

    /**
     * Sets the source containing the transient settings to be updated. They will not survive a full cluster restart
     */
    public ClusterUpdateSettingsRequest transientSettings(String source) {
        this.transientSettings = Settings.settingsBuilder().loadFromSource(source).build();
        return this;
    }

    /**
     * Sets the transient settings to be updated. They will not survive a full cluster restart
     */
    @SuppressWarnings("unchecked")
    public ClusterUpdateSettingsRequest transientSettings(Map source) {
        try {
            XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
            builder.map(source);
            transientSettings(builder.string());
        } catch (IOException e) {
            throw new ElasticsearchGenerationException("Failed to generate [" + source + "]", e);
        }
        return this;
    }

    /**
     * Sets the persistent settings to be updated. They will get applied cross restarts
     */
    public ClusterUpdateSettingsRequest persistentSettings(Settings settings) {
        this.persistentSettings = settings;
        return this;
    }

    /**
     * Sets the persistent settings to be updated. They will get applied cross restarts
     */
    public ClusterUpdateSettingsRequest persistentSettings(Settings.Builder settings) {
        this.persistentSettings = settings.build();
        return this;
    }

    /**
     * Sets the source containing the persistent settings to be updated. They will get applied cross restarts
     */
    public ClusterUpdateSettingsRequest persistentSettings(String source) {
        this.persistentSettings = Settings.settingsBuilder().loadFromSource(source).build();
        return this;
    }

    /**
     * Sets the persistent settings to be updated. They will get applied cross restarts
     */
    @SuppressWarnings("unchecked")
    public ClusterUpdateSettingsRequest persistentSettings(Map source) {
        try {
            XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
            builder.map(source);
            persistentSettings(builder.string());
        } catch (IOException e) {
            throw new ElasticsearchGenerationException("Failed to generate [" + source + "]", e);
        }
        return this;
    }

    /**
     * Sets a set of transient setting names which should be removed
     */
    public ClusterUpdateSettingsRequest transientSettingsToRemove(Set<String> transientSettingsToRemove) {
        this.transientSettingsToRemove = transientSettingsToRemove;
        return this;
    }

    /**
     * Sets a set of persistent setting names which should be removed
     */
    public ClusterUpdateSettingsRequest persistentSettingsToRemove(Set persistentSettingsToRemove) {
        this.persistentSettingsToRemove = persistentSettingsToRemove;
        return this;
    }

    public Set<String> transientSettingsToRemove() {
        return transientSettingsToRemove;
    }

    public Set<String> persistentSettingsToRemove() {
        return persistentSettingsToRemove;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        tenantName = in.readNullableString();
        transientSettings = readSettingsFromStream(in);
        persistentSettings = readSettingsFromStream(in);
        readTimeout(in);

        int transientToRemoveSize = in.readVInt();
        for (int i = 0; i < transientToRemoveSize; i++) {
            transientSettingsToRemove.add(in.readString());
        }
        int persistentToRemoveSize = in.readVInt();
        for (int i = 0; i < persistentToRemoveSize; i++) {
            persistentSettingsToRemove.add(in.readString());
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeNullableString(tenantName);
        writeSettingsToStream(transientSettings, out);
        writeSettingsToStream(persistentSettings, out);
        writeTimeout(out);

        out.writeVInt(transientSettingsToRemove.size());
        for (String transientSettingName : transientSettingsToRemove) {
            out.writeString(transientSettingName);
        }
        out.writeVInt(persistentSettingsToRemove.size());
        for (String persistentSettingName : persistentSettingsToRemove) {
            out.writeString(persistentSettingName);
        }
    }
}
