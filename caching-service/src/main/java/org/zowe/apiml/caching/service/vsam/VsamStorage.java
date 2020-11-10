/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.apiml.caching.service.vsam;

import org.zowe.apiml.caching.model.KeyValue;
import org.zowe.apiml.caching.service.Storage;

import java.util.Map;

public class VsamStorage implements Storage {
    @Override
    public KeyValue create(String serviceId, KeyValue toCreate) {
        return null;
    }

    @Override
    public KeyValue read(String serviceId, String key) {
        return null;
    }

    @Override
    public KeyValue update(String serviceId, KeyValue toUpdate) {
        return null;
    }

    @Override
    public KeyValue delete(String serviceId, String toDelete) {
        return null;
    }

    @Override
    public Map<String, KeyValue> readForService(String serviceId) {
        return null;
    }
    // We need to verify the zOS page.

}
