/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.csanchez.jenkins.plugins.kubernetes.volumes;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import io.fabric8.kubernetes.api.model.KeyToPath;
import io.fabric8.kubernetes.api.model.KeyToPathBuilder;
import io.fabric8.kubernetes.api.model.SecretVolumeSource;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;

public class SecretVolume extends PodVolume {
    private String mountPath;
    private String secretName;
    private String defaultMode;
    private String subPath;
    private Boolean optional;

    @DataBoundConstructor
    public SecretVolume(String mountPath, String secretName, String defaultMode, String subPath, Boolean optional) {
        this.mountPath = mountPath;
        this.secretName = secretName;
        this.defaultMode = defaultMode;
        this.subPath = subPath;
        this.optional = optional;
    }

    public SecretVolume(String mountPath, String secretName, String defaultMode, Boolean optional) {
        this(mountPath, secretName, defaultMode, null, optional);
    }

    public SecretVolume(String mountPath, String secretName, String subPath) {
        this(mountPath, secretName, null, subPath, false);
    }

    public SecretVolume(String mountPath, String secretName) {
        this(mountPath, secretName, null, null, false);
    }

    @Override
    public Volume buildVolume(String volumeName) {
        SecretVolumeSource secretVolumeSource = new SecretVolumeSource();
        secretVolumeSource.setSecretName(getSecretName());
        secretVolumeSource.setOptional(getOptional());

        if (StringUtils.isNotBlank(defaultMode)) {
            secretVolumeSource.setDefaultMode(Integer.parseInt(getDefaultMode()));
        }

        if (StringUtils.isNotBlank(subPath)) {
            List<KeyToPath> items = new ArrayList<>();
            items.add(new KeyToPathBuilder().withKey(getSubPath()).build());
            secretVolumeSource.setItems(items);
        }

        return new VolumeBuilder()
                .withName(volumeName)
                .withNewSecretLike(secretVolumeSource)
                .endSecret()
                .build();
    }

    public String getSecretName() {
        return secretName;
    }

    @Override
    public String getMountPath() {
        return mountPath;
    }

    public String getDefaultMode() {
        return defaultMode;
    }

    public Boolean getOptional() {
        return optional;
    }

    public String getSubPath() {
        return subPath;
    }
    
    @DataBoundSetter
    public void setSubPath(String subPath) {
        this.subPath = Util.fixEmpty(subPath);
    }

    protected Object readResolve() {
        this.subPath = Util.fixEmpty(subPath);
        return this;
    }

    @Override
    public String toString() {
        return "SecretVolume [mountPath=" + mountPath + ", secretName=" + secretName
            + ", defaultMode=" + defaultMode + ", optional=" + String.valueOf(optional)
            + ", subPath=" + subPath + "]";
    }

    @Extension
    @Symbol("secretVolume")
    public static class DescriptorImpl extends Descriptor<PodVolume> {
        @Override
        public String getDisplayName() {
            return "Secret Volume";
        }
    }
}
