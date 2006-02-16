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

package org.opensaml.xml.encryption;

import org.opensaml.xml.XMLObject;
import org.opensaml.xml.util.Base64EncodedBinary;

/**
 * XMLObject representing XML Encryption, version 20021210, OAEParams element.
 */
public interface OAEParams extends XMLObject, Base64EncodedBinary {

    /** Element local name */
    public final static String LOCAL_NAME = "OAEParams";
}