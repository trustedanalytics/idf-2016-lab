/**
 * Copyright (c) 2016 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.serviceinfo;

import org.springframework.cloud.cloudfoundry.CloudFoundryServiceInfoCreator;
import org.springframework.cloud.cloudfoundry.Tags;

import java.util.Map;
import java.util.Optional;

public class MqttServiceInfoCreator extends CloudFoundryServiceInfoCreator<MqttServiceInfo> {

    public static final String MQTT_ID = "mosquitto14";

    public MqttServiceInfoCreator() {
        super(new Tags(MQTT_ID));
    }

    @Override
    public boolean accept(Map<String, Object> serviceData) {
        Optional<String> label = Optional.ofNullable((String)serviceData.get("label"));
        if(label.isPresent()){
            return label.get().equals(MQTT_ID);
        }
        return false;
    }

    @Override
    public MqttServiceInfo createServiceInfo(Map<String, Object> serviceData) {
        MqttProperties mqttProperties = getMqttProperties(serviceData);
        return new MqttServiceInfo(MQTT_ID, mqttProperties);
    }

    private MqttProperties getMqttProperties(Map<String, Object> serviceData) {
        Map<String, Object> credentials = getCredentials(serviceData);
        return new MqttProperties(credentials);
    }
}
