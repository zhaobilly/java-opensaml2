/*
 * Copyright [2007] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.saml2.binding.decoding;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.binding.decoding.BaseSAML1MessageDecoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusResponseType;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.decoder.BaseMessageDecoder;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.xml.parse.ParserPool;

/**
 * Base class for SAML 2 message decoders.
 */
public abstract class BaseSAML2MessageDecoder extends BaseMessageDecoder {

    /** Class logger. */
    private Logger log = Logger.getLogger(BaseSAML1MessageDecoder.class);
    
    /** Constructor. */
    public BaseSAML2MessageDecoder() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param pool parser pool used to deserialize messages
     */
    public BaseSAML2MessageDecoder(ParserPool pool) {
        super(pool);
    }

    /**
     * Populates the message context with the message ID, issue instant, and issuer as well as the peer's entity
     * descriptor if a metadata provider is present in the message context and the peer's role descriptor if its entity
     * descriptor was retrieved and the message context has a populated peer role name.
     * 
     * @param messageContext message context to populate
     * 
     * @throws MessageDecodingException thrown if there is a problem populating the message context
     */
    protected void populateMessageContext(SAMLMessageContext messageContext) throws MessageDecodingException {
        populateMessageIdIssueInstantIssuer(messageContext);
        populateRelyingPartyMetadata(messageContext);
    }
    
    /**
     * Extracts the message ID, issue instant, and issuer from the incoming SAML message and populates the message
     * context with it.
     * 
     * @param messageContext current message context
     * 
     * @throws MessageDecodingException thrown if there is a problem populating the message context
     */
    protected void populateMessageIdIssueInstantIssuer(SAMLMessageContext messageContext)
            throws MessageDecodingException {
        if (!(messageContext instanceof SAMLMessageContext)) {
            log.debug("Invalid message context type, this policy rule only support SAMLMessageContext");
            return;
        }
        SAMLMessageContext samlMsgCtx = (SAMLMessageContext) messageContext;

        SAMLObject samlMsg = samlMsgCtx.getInboundSAMLMessage();
        if (samlMsg == null) {
            log.error("Message context did not contain inbound SAML message");
            throw new MessageDecodingException("Message context did not contain inbound SAML message");
        }

        if (samlMsg instanceof RequestAbstractType) {
            log.debug("Extracting ID, issuer and issue instant from request");
            extractRequestInfo(samlMsgCtx, (RequestAbstractType) samlMsg);
        } else if (samlMsg instanceof StatusResponseType) {
            log.debug("Extracting ID, issuer and issue instant from status response");
            extractResponseInfo(samlMsgCtx, (StatusResponseType) samlMsg);
        } else {
            throw new MessageDecodingException("SAML 2 message was not a request or a response");
        }

        if (samlMsgCtx.getInboundMessageIssuer() == null) {
            log.warn("Issuer could not be extracted from SAML 2 message");
        }

    }

    /**
     * Extract information from a SAML StatusResponse message.
     * 
     * @param messageContext current message context
     * @param statusResponse the SAML message to process
     * 
     * @throws MessageDecodingException thrown if the response issuer has a format other than {@link NameIDType#ENTITY}
     *             or, if the response does not contain an issuer, if the contained assertions contain issuers that are
     *             not of {@link NameIDType#ENTITY} format or if the assertions contain different issuers
     */
    protected void extractResponseInfo(SAMLMessageContext messageContext, StatusResponseType statusResponse)
            throws MessageDecodingException {

        messageContext.setInboundSAMLMessageId(statusResponse.getID());
        messageContext.setInboundSAMLMessageIssueInstant(statusResponse.getIssueInstant());

        // If response doesn't have an issuer, look at the first
        // enclosed assertion
        String messageIssuer = null;
        if (statusResponse.getIssuer() != null) {
            messageIssuer = extractEntityId(statusResponse.getIssuer());
        } else if (statusResponse instanceof Response) {
            List<Assertion> assertions = ((Response) statusResponse).getAssertions();
            if (assertions != null && assertions.size() > 0) {
                log.info("Status response message had no issuer, "
                        + "attempting to extract issuer from enclosed Assertion(s)");
                String assertionIssuer;
                for (Assertion assertion : assertions) {
                    if (assertion != null && assertion.getIssuer() != null) {
                        assertionIssuer = extractEntityId(assertion.getIssuer());
                        if (messageIssuer != null && !messageIssuer.equals(assertionIssuer)) {
                            throw new MessageDecodingException("SAML 2 assertions, within response "
                                    + statusResponse.getID() + " contain different issuer IDs");
                        }
                        messageIssuer = assertionIssuer;
                    }
                }
            }
        }

        messageContext.setInboundMessageIssuer(messageIssuer);
    }

    /**
     * Extract information from a SAML RequestAbstractType message.
     * 
     * @param messageContext current message context
     * @param request the SAML message to process
     * 
     * @throws MessageDecodingException thrown if the request issuer has a format other than {@link NameIDType#ENTITY}
     */
    protected void extractRequestInfo(SAMLMessageContext messageContext, RequestAbstractType request)
            throws MessageDecodingException {
        messageContext.setInboundSAMLMessageId(request.getID());
        messageContext.setInboundSAMLMessageIssueInstant(request.getIssueInstant());
        messageContext.setInboundMessageIssuer(extractEntityId(request.getIssuer()));
    }

    /**
     * Extracts the entity ID from the SAML 2 Issuer.
     * 
     * @param issuer issuer to extract the entityID from
     * 
     * @return entity ID of the issuer
     * 
     * @throws MessageDecodingException thrown if the given issuer has a format other than {@link NameIDType#ENTITY}
     */
    protected String extractEntityId(Issuer issuer) throws MessageDecodingException {
        if (issuer != null) {
            if (issuer.getFormat() == null || issuer.getFormat().equals(NameIDType.ENTITY)) {
                return issuer.getValue();
            } else {
                throw new MessageDecodingException("SAML 2 Issuer is not of ENTITY format type");
            }
        }

        return null;
    }
    
    
    /**
     * Populates the peer's entity metadata if a metadata provide is present in the message context. Populates the
     * peer's role descriptor if the entity metadata was available and the role name is present in the message context.
     * 
     * @param messageContext current message context
     * 
     * @throws MessageDecodingException thrown if there is a problem populating the message context
     */
    protected void populateRelyingPartyMetadata(SAMLMessageContext messageContext) throws MessageDecodingException {
        MetadataProvider metadataProvider = messageContext.getMetadataProvider();
        try {
            if (metadataProvider != null) {
                EntityDescriptor relyingPartyMD = metadataProvider.getEntityDescriptor(messageContext
                        .getInboundMessageIssuer());
                messageContext.setPeerEntityMetadata(relyingPartyMD);

                QName relyingPartyRole = messageContext.getPeerEntityRole();
                if (relyingPartyMD != null && relyingPartyRole != null) {
                    List<RoleDescriptor> roles = relyingPartyMD.getRoleDescriptors(relyingPartyRole,
                            SAMLConstants.SAML11P_NS);
                    if (roles != null && roles.size() > 0) {
                        messageContext.setPeerEntityRoleMetadata(roles.get(0));
                    }
                }
            }
        } catch (MetadataProviderException e) {
            log.error("Error retrieving metadata for relying party " + messageContext.getInboundMessageIssuer(), e);
            throw new MessageDecodingException("Error retrieving metadata for relying party "
                    + messageContext.getInboundMessageIssuer(), e);
        }
    }
}