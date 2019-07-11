package com.pi4j.io.gpio.analog.impl;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (API)
 * FILENAME      :  AnalogOutputFactory.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2019 Pi4J
 * %%
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
 * #L%
 */

import com.pi4j.Pi4J;
import com.pi4j.io.gpio.analog.AnalogOutput;
import com.pi4j.io.gpio.analog.AnalogOutputConfig;
import com.pi4j.io.gpio.analog.AnalogOutputBuilder;
import com.pi4j.io.gpio.analog.AnalogOutputProvider;
import com.pi4j.provider.exception.ProviderException;
import com.pi4j.provider.exception.ProviderInstantiateException;

import java.util.Properties;

/**
 * Analog Output Factory - it returns instances of {@link AnalogOutput} interface.
 *
 * @author Robert Savage (<a
 *         href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 */
public class AnalogOutputFactory {

    // private constructor
    private AnalogOutputFactory() {
        // forbid object construction
    }

    public static boolean exists(String id) throws ProviderException {
        return false;
    }

    public static AnalogOutput get(String id) throws ProviderException {
        return null;
    }

    public static AnalogOutputBuilder builder() throws ProviderException {
        return new DefaultAnalogOutputBuilder();
    }

    public static AnalogOutputBuilder builder(Properties properties) throws ProviderException {
        return new DefaultAnalogOutputBuilder(properties);
    }

    public static AnalogOutput create(AnalogOutputConfig config) throws ProviderException {
        return AnalogOutputFactory.create((AnalogOutputProvider)null, config);
    }

    public static AnalogOutput create(String providerId, AnalogOutputConfig config) throws ProviderException {
        // if provided, lookup the specified io provider; else use the default io provider
        if(providerId == null) {
            return create(config);
        }
        else{
            var provider = Pi4J.providers().analogOutput().get(providerId);
            return create(provider, config);
        }
    }

    public static AnalogOutput create(AnalogOutputProvider provider, AnalogOutputConfig config) throws ProviderException {
        try {
            // get default provider if provider argument is null
            if(provider == null){
                provider = Pi4J.providers().analogOutput().getDefault();
            }
            // create an instance using the io provider
            return provider.instance(config);
        } catch(ProviderException pe){
            throw pe;
        } catch (Exception e) {
            //e.printStackTrace();
            throw new ProviderInstantiateException(provider, e);
        }
    }
}
