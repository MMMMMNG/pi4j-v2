package com.pi4j.io.spi;

import com.pi4j.provider.ProviderBase;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (CORE)
 * FILENAME      :  SpiProviderBase.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 *
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
/**
 * <p>Abstract SpiProviderBase class.</p>
 *
 * @author Robert Savage (<a href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public abstract class SpiProviderBase
        extends ProviderBase<SpiProvider, Spi, SpiConfig>
        implements SpiProvider {

    /**
     * <p>Constructor for SpiProviderBase.</p>
     */
    public SpiProviderBase(){
        super();
    }

    /**
     * <p>Constructor for SpiProviderBase.</p>
     *
     * @param id a {@link java.lang.String} object.
     */
    public SpiProviderBase(String id){
        super(id);
    }

    /**
     * <p>Constructor for SpiProviderBase.</p>
     *
     * @param id a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     */
    public SpiProviderBase(String id, String name){
        super(id, name);
    }
}
