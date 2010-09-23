/*
 * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.saml2.metadata.support;

import java.util.List;

import org.opensaml.saml2.metadata.IndexedEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility helper class for SAML 2 metadata objects.
 */
public final class SAML2MetadataHelper {
    
    /** Class logger. */
    private static final Logger LOG = LoggerFactory.getLogger(SAML2MetadataHelper.class);
    
    /** Constructor. */
    private SAML2MetadataHelper() { }

    /**
     * Select the default {@link IndexedEndpoint} from a list of candidates.
     * 
     * <p>
     * The algorithm used is:
     * <ol>
     * <li>Select the first endpoint with an explicit <code>isDefault=true</code></li>
     * <li>Select the first endpoint with no explicit <code>isDefault</code></li>
     * <li>Select the first endpoint</li>
     * </ol>
     * </p>
     * 
     * 
     * @param candidates the list of candidate indexed endpoints
     * @return the selected candidate (or null if the list is null or empty)
     * 
     * @param <T> the subtype of IndexedType
     * 
     */
    public static <T extends IndexedEndpoint> T getDefaultIndexedEndpoint(List<T> candidates) {
        LOG.debug("Selecting default IndexedEndpoint");
        
        if (candidates == null || candidates.isEmpty()) {
            LOG.debug("IndexedEndpoint list was null or empty, returning null");
            return null;
        }
        
        T firstNoDefault = null;
        for (T endpoint : candidates) {
            if (endpoint.isDefault()) {
                LOG.debug("Selected IndexedEndpoint with explicit isDefault of true");
                return endpoint;
            }
            
            // This records the first element whose isDefault is not explicitly false
            if (firstNoDefault == null && endpoint.isDefaultXSBoolean() == null) {
                firstNoDefault = endpoint;
            }
        }
        
        if (firstNoDefault != null) {
            LOG.debug("Selected first IndexedEndpoint with no explicit isDefault");
            return firstNoDefault;
        } else  {
            LOG.debug("Selected first IndexedEndpoint with explicit isDefault of false");
            return candidates.get(0);
        }
        
    }
}