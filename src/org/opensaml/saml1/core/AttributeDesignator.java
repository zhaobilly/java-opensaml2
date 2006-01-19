/*
 * Copyright [2005] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.saml1.core;

import org.opensaml.common.SAMLObject;

/**
 * This interface defines how the object representing a SAML 1 <code> AttributeDesignator </code> element behaves.
 */
public interface AttributeDesignator extends SAMLObject {

    /** Element name, no namespace. */

    public final static String LOCAL_NAME = "AttributeStatement";
    
    /** Name for the AttributeName attribute */
    public final static String ATTRIBUTENAME_ATTRIB_NAME = "AttributeName";

    /** Name for the AttributeNamespace attribute */
    public final static String ATTRIBUTENAMESPACE_ATTRIB_NAME = "AttributeNamespace";

    /** Get the contents of the AttributeName attribute */
    public String getAttributeName();
    
    /** Set the contents of the AttributeName attribute */
    public void setAttributeName(String attributeName);
    
    /** Get the contents of the AttributeNamespace attribute */
    public String getAttributeNamespace();
    
    /** Set the contents of the AttributeNamespace attribute */
    public void setAttributeNamespace(String attributeNamespace);
}