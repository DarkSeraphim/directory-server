/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.shared.kerberos.codec.principalName;


import org.apache.directory.shared.asn1.ber.Asn1Container;
import org.apache.directory.shared.asn1.ber.grammar.AbstractGrammar;
import org.apache.directory.shared.asn1.ber.grammar.Grammar;
import org.apache.directory.shared.asn1.ber.grammar.GrammarAction;
import org.apache.directory.shared.asn1.ber.grammar.GrammarTransition;
import org.apache.directory.shared.asn1.ber.tlv.TLV;
import org.apache.directory.shared.asn1.ber.tlv.UniversalTag;
import org.apache.directory.shared.asn1.codec.DecoderException;
import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.kerberos.KerberosConstants;
import org.apache.directory.shared.kerberos.codec.actions.CheckNotNullLength;
import org.apache.directory.shared.kerberos.codec.principalName.actions.PrincipalNameNameString;
import org.apache.directory.shared.kerberos.codec.principalName.actions.PrincipalNameNameType;
import org.apache.directory.shared.kerberos.components.PrincipalName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class implements the PrincipalName. All the actions are declared
 * in this class. As it is a singleton, these declaration are only done once. If
 * an action is to be added or modified, this is where the work is to be done !
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class PrincipalNameGrammar extends AbstractGrammar
{
    /** The logger */
    static final Logger LOG = LoggerFactory.getLogger( PrincipalNameGrammar.class );

    /** A speedup for logger */
    static final boolean IS_DEBUG = LOG.isDebugEnabled();

    /** The instance of grammar. PrincipalNameGrammar is a singleton */
    private static Grammar instance = new PrincipalNameGrammar();


    /**
     * Creates a new PrincipalNameGrammar object.
     */
    private PrincipalNameGrammar()
    {
        setName( PrincipalNameGrammar.class.getName() );

        // Create the transitions table
        super.transitions = new GrammarTransition[PrincipalNameStatesEnum.LAST_PRINCIPAL_NAME_STATE.ordinal()][256];

        // ============================================================================================
        // PrincipalName 
        // ============================================================================================
        // --------------------------------------------------------------------------------------------
        // Transition from PrincipalName init to PrincipalName SEQ
        // --------------------------------------------------------------------------------------------
        // PrincipalName   ::= SEQUENCE
        super.transitions[PrincipalNameStatesEnum.START_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            PrincipalNameStatesEnum.START_STATE, PrincipalNameStatesEnum.PRINCIPAL_NAME_SEQ_STATE, UniversalTag.SEQUENCE.getValue(),
            new GrammarAction( "principalName SEQUENCE" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    PrincipalNameContainer principalNameContainer = ( PrincipalNameContainer ) container;

                    TLV tlv = principalNameContainer.getCurrentTLV();

                    // The Length should not be null
                    if ( tlv.getLength() == 0 )
                    {
                        LOG.error( I18n.err( I18n.ERR_04066 ) );

                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( I18n.err( I18n.ERR_04067 ) );
                    }
                    
                    PrincipalName principalName = new PrincipalName();
                    principalNameContainer.setPrincipalName( principalName );
                    
                    if ( IS_DEBUG )
                    {
                        LOG.debug( "PrincipalName created" );
                    }
                }
            } );
        
        // --------------------------------------------------------------------------------------------
        // Transition from PrincipalName SEQ to name-type tag
        // --------------------------------------------------------------------------------------------
        // PrincipalName   ::= SEQUENCE {
        //         name-type       [0] Int32,
        super.transitions[PrincipalNameStatesEnum.PRINCIPAL_NAME_SEQ_STATE.ordinal()][KerberosConstants.PRINCIPAL_NAME_NAME_TYPE_TAG] = new GrammarTransition(
            PrincipalNameStatesEnum.PRINCIPAL_NAME_SEQ_STATE, PrincipalNameStatesEnum.PRINCIPAL_NAME_NAME_TYPE_TAG_STATE, KerberosConstants.PRINCIPAL_NAME_NAME_TYPE_TAG,
            new CheckNotNullLength() );
        
        // --------------------------------------------------------------------------------------------
        // Transition from name-type tag to name-type value
        // --------------------------------------------------------------------------------------------
        // PrincipalName   ::= SEQUENCE {
        //         name-type       [0] Int32,
        super.transitions[PrincipalNameStatesEnum.PRINCIPAL_NAME_NAME_TYPE_TAG_STATE.ordinal()][UniversalTag.INTEGER.getValue()] = new GrammarTransition(
            PrincipalNameStatesEnum.PRINCIPAL_NAME_NAME_TYPE_TAG_STATE, PrincipalNameStatesEnum.PRINCIPAL_NAME_NAME_TYPE_STATE, UniversalTag.INTEGER.getValue(),
            new PrincipalNameNameType() );
        
        // --------------------------------------------------------------------------------------------
        // Transition from name-type value to name-string tag
        // --------------------------------------------------------------------------------------------
        // PrincipalName   ::= SEQUENCE {
        //         name-type       [0] Int32,
        //         name-string     [1]
        super.transitions[PrincipalNameStatesEnum.PRINCIPAL_NAME_NAME_TYPE_STATE.ordinal()][KerberosConstants.PRINCIPAL_NAME_NAME_STRING_TAG] = new GrammarTransition(
            PrincipalNameStatesEnum.PRINCIPAL_NAME_NAME_TYPE_STATE, PrincipalNameStatesEnum.PRINCIPAL_NAME_NAME_STRING_TAG_STATE, KerberosConstants.PRINCIPAL_NAME_NAME_STRING_TAG,
            new CheckNotNullLength() );
        
        // --------------------------------------------------------------------------------------------
        // Transition from name-string tag to name-string SEQ
        // --------------------------------------------------------------------------------------------
        // PrincipalName   ::= SEQUENCE {
        //         name-type       [0] Int32,
        //         name-string     [1] SEQUENCE OF
        super.transitions[PrincipalNameStatesEnum.PRINCIPAL_NAME_NAME_STRING_TAG_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            PrincipalNameStatesEnum.PRINCIPAL_NAME_NAME_STRING_TAG_STATE, PrincipalNameStatesEnum.PRINCIPAL_NAME_NAME_STRING_SEQ_STATE, UniversalTag.SEQUENCE.getValue(),
            new CheckNotNullLength() );
        
        // --------------------------------------------------------------------------------------------
        // Transition from name-string SEQ to name-string value
        // --------------------------------------------------------------------------------------------
        // PrincipalName   ::= SEQUENCE {
        //         name-type       [0] Int32,
        //         name-string     [1] SEQUENCE OF KerberosString
        super.transitions[PrincipalNameStatesEnum.PRINCIPAL_NAME_NAME_STRING_SEQ_STATE.ordinal()][UniversalTag.GENERAL_STRING.getValue()] = new GrammarTransition(
            PrincipalNameStatesEnum.PRINCIPAL_NAME_NAME_STRING_SEQ_STATE, PrincipalNameStatesEnum.PRINCIPAL_NAME_NAME_STRING_SEQ_STATE, UniversalTag.GENERAL_STRING.getValue(),
            new PrincipalNameNameString() );
    }


    // ~ Methods
    // ------------------------------------------------------------------------------------

    /**
     * Get the instance of this grammar
     * 
     * @return An instance on the PrincipalName Grammar
     */
    public static Grammar getInstance()
    {
        return instance;
    }
}