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
package org.apache.directory.server.core.subtree;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.directory.server.core.annotations.ApplyLdifs;
import org.apache.directory.server.core.annotations.CreateDS;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.apache.directory.shared.ldap.entry.Entry;
import org.apache.directory.shared.ldap.ldif.LdifUtils;
import org.apache.directory.shared.ldap.message.AddResponse;
import org.apache.directory.shared.ldap.message.DeleteResponse;
import org.apache.directory.shared.ldap.message.ResultCodeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test cases for the AdministrativePoint interceptor delete operation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(FrameworkRunner.class)
@CreateDS(name = "AdministrativePointServiceIT")
@ApplyLdifs(
    {
        // An entry used to create a User session
        "dn: cn=testUser,ou=system",
        "objectClass: top",
        "objectClass: person",
        "cn: testUser",
        "sn: test User",
        "userpassword: test"
    })
public class SubentryDeleteOperationIT extends AbstractSubentryUnitTest
{
    // ===================================================================
    // Test the Delete operation on APs
    // -------------------------------------------------------------------
    // Failure expected
    // -------------------------------------------------------------------
    /**
     * Test the deletion of an AP with children
     */
    @Test
    public void testDeleteAPWithChildren() throws Exception
    {
        Entry autonomousArea = LdifUtils.createEntry( 
            "ou=AAP,ou=system", 
            "ObjectClass: top",
            "ObjectClass: organizationalUnit", 
            "ou: AAP", 
            "administrativeRole: autonomousArea" );

        // It should succeed
        AddResponse response = adminConnection.add( autonomousArea );

        assertEquals( ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode() );

        // Add a subentry now
        Entry subentry = LdifUtils.createEntry( 
            "cn=test,ou=AAP,ou=system", 
            "ObjectClass: top",
            "ObjectClass: subentry", 
            "ObjectClass: collectiveAttributeSubentry",
            "cn: test",
            "subtreeSpecification: {}", 
            "c-o: Test Org" );

        response = adminConnection.add( subentry );
        assertEquals( ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode() );
        
        assertTrue( checkIsPresent( "cn=test,ou=AAP,ou=system" ) );

        Entry ap = adminConnection.lookup( "ou=AAP,ou=system", "+", "*" );
        assertNotNull( ap );
        assertEquals( "1", ap.get( "collectiveAttributeSeqNumber" ).getString() );
        
        // Now try to delete the AP
        DeleteResponse delResponse = adminConnection.delete( "ou=AAP,ou=system" );
        
        assertEquals( ResultCodeEnum.NOT_ALLOWED_ON_NON_LEAF, delResponse.getLdapResult().getResultCode() );
    }
    
    
    /**
     * Delete a SAP with a non admin user
     */
    @Test
    public void testDeleteSAPNonAdmin() throws Exception
    {
        Entry sap = LdifUtils.createEntry( 
            "ou=SAP,ou=system", 
            "ObjectClass: top",
            "ObjectClass: organizationalUnit", 
            "ou: SAP", 
            "administrativeRole: collectiveAttributeSpecificArea" );

        // It should succeed
        AddResponse response = adminConnection.add( sap );

        assertEquals( ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode() );

        // Now try to delete the AP with another user
        DeleteResponse delResponse = userConnection.delete( "ou=SAP,ou=system" );
        
        assertEquals( ResultCodeEnum.UNWILLING_TO_PERFORM, delResponse.getLdapResult().getResultCode() );
        
        // Check that the SAP is still present
        assertTrue( checkIsPresent( "ou=SAP,ou=system" ) );
    }

    
    // -------------------------------------------------------------------
    // Success expected
    // -------------------------------------------------------------------
    /**
     * Delete an AAP
     */
    @Test
    public void testDeleteAAP() throws Exception
    {
        Entry autonomousArea = LdifUtils.createEntry( 
            "ou=AAP2,ou=system", 
            "ObjectClass: top",
            "ObjectClass: organizationalUnit", 
            "ou: AAP", 
            "administrativeRole: autonomousArea" );

        // It should succeed
        AddResponse response = adminConnection.add( autonomousArea );

        assertEquals( ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode() );

        // Now try to delete the AP
        DeleteResponse delResponse = adminConnection.delete( "ou=AAP2,ou=system" );
        
        assertEquals( ResultCodeEnum.SUCCESS, delResponse.getLdapResult().getResultCode() );
        
        // Check that the AAP is not anymore present
        assertTrue( checkIsAbsent( "ou=AAP2,ou=system" ) );
    }
    
    
    /**
     * Delete a SAP
     */
    @Test
    public void testDeleteSAP() throws Exception
    {
        Entry sap = LdifUtils.createEntry( 
            "ou=SAP,ou=system", 
            "ObjectClass: top",
            "ObjectClass: organizationalUnit", 
            "ou: SAP", 
            "administrativeRole: collectiveAttributeSpecificArea" );

        // It should succeed
        AddResponse response = adminConnection.add( sap );

        assertEquals( ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode() );

        // Now try to delete the AP
        DeleteResponse delResponse = adminConnection.delete( "ou=SAP,ou=system" );
        
        assertEquals( ResultCodeEnum.SUCCESS, delResponse.getLdapResult().getResultCode() );
        
        // Check that the SAP is not anymore present
        assertTrue( checkIsAbsent( "ou=SAP,ou=system" ) );
    }
    
    
    /** 
     * Delete an IAP
     */
    @Test
    public void testDeleteIAP() throws Exception
    {
        Entry sap = LdifUtils.createEntry( 
            "ou=SAP1,ou=system", 
            "ObjectClass: top",
            "ObjectClass: organizationalUnit", 
            "ou: SAP1", 
            "administrativeRole: collectiveAttributeSpecificArea" );

        // It should succeed
        AddResponse response = adminConnection.add( sap );

        assertEquals( ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode() );
        
        // Add the IAP
        Entry iap = LdifUtils.createEntry( 
            "ou=IAP1,ou=SAP1,ou=system", 
            "ObjectClass: top",
            "ObjectClass: organizationalUnit", 
            "ou: IAP1", 
            "administrativeRole: collectiveAttributeInnerArea" );

        // It should succeed
        response = adminConnection.add( iap );

        assertEquals( ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode() );

        // Now try to delete the SAP (it should fail)
        DeleteResponse delResponse = adminConnection.delete( "ou=SAP1,ou=system" );
        
        assertEquals( ResultCodeEnum.NOT_ALLOWED_ON_NON_LEAF, delResponse.getLdapResult().getResultCode() );
        
        // Remove the IAP first
        delResponse = adminConnection.delete( "ou=IAP1,ou=SAP1,ou=system" );
        
        assertEquals( ResultCodeEnum.SUCCESS, delResponse.getLdapResult().getResultCode() );
        
        // Check that the IAP is not anymore present
        assertTrue( checkIsAbsent( "ou=IA1P,ou=SAP1,ou=system" ) );
        
        // Remove the SAP
        delResponse = adminConnection.delete( "ou=SAP1,ou=system" );
        
        assertEquals( ResultCodeEnum.SUCCESS, delResponse.getLdapResult().getResultCode() );
        
        // Check that the SAP is not anymore present
        assertTrue( checkIsAbsent( "ou=SAP1,ou=system" ) );
    }
    
    
    // ===================================================================
    // Test the Delete operation on subentries
    // -------------------------------------------------------------------
    // -------------------------------------------------------------------
    // Success expected
    // -------------------------------------------------------------------
    /**
     * Test the deletion of a subentry under an AAP
     */
    @Test
    public void testDeleteSubentryUnderAAP() throws Exception
    {
        // First add an AAP
        Entry autonomousArea = LdifUtils.createEntry( 
            "ou=AAP1,ou=system", 
            "ObjectClass: top",
            "ObjectClass: organizationalUnit", 
            "ou: AAP1", 
            "administrativeRole: autonomousArea" );

        AddResponse response = adminConnection.add( autonomousArea );
        assertEquals( ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode() );
        
        assertEquals( -1L, getACSeqNumber( "ou=AAP1,ou=system" ) );
        assertEquals( -1L, getCASeqNumber( "ou=AAP1,ou=system" ) );
        
        // Add a subentry now
        Entry subentry = LdifUtils.createEntry( 
            "cn=test,ou=AAP1,ou=system", 
            "ObjectClass: top",
            "ObjectClass: subentry", 
            "ObjectClass: collectiveAttributeSubentry",
            "cn: test",
            "subtreeSpecification: {}", 
            "c-o: Test Org" );

        response = adminConnection.add( subentry );
        assertEquals( ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode() );

        long seqNumber = getCASeqNumber( "ou=AAP1,ou=system" );
        assertEquals( -1L, getACSeqNumber( "ou=AAP1,ou=system" ) );
        
        // Now delete it
        DeleteResponse delResponse = adminConnection.delete( "cn=test,ou=AAP1,ou=system" );
        assertEquals( ResultCodeEnum.SUCCESS, delResponse.getLdapResult().getResultCode() );
        
        // Check the CASeqNumber, it must be 1 now
        assertEquals( seqNumber + 1, getCASeqNumber( "ou=AAP1,ou=system" ) );
    }
    
    
    /**
     * Test the deletion of a subentry under a SAP
     */
    @Test
    public void testDeleteSubentryUnderSAP() throws Exception
    {
        // First add an AAP
        Entry autonomousArea = LdifUtils.createEntry( 
            "ou=SAP1,ou=system", 
            "ObjectClass: top",
            "ObjectClass: organizationalUnit", 
            "ou: SAP1", 
            "administrativeRole: CollectiveAttributeSpecificArea" );

        AddResponse response = adminConnection.add( autonomousArea );
        assertEquals( ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode() );
        
        assertEquals( Long.MIN_VALUE, getACSeqNumber( "ou=SAP1,ou=system" ) );
        assertEquals( -1L, getCASeqNumber( "ou=SAP1,ou=system" ) );
        
        // Add a subentry now
        Entry subentry = LdifUtils.createEntry( 
            "cn=test,ou=SAP1,ou=system", 
            "ObjectClass: top",
            "ObjectClass: subentry", 
            "ObjectClass: collectiveAttributeSubentry",
            "cn: test",
            "subtreeSpecification: {}", 
            "c-o: Test Org" );

        response = adminConnection.add( subentry );
        assertEquals( ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode() );

        long seqNumber = getCASeqNumber( "ou=SAP1,ou=system" );
        assertEquals( Long.MIN_VALUE, getACSeqNumber( "ou=SAP1,ou=system" ) );
        
        // Now delete it
        DeleteResponse delResponse = adminConnection.delete( "cn=test,ou=SAP1,ou=system" );
        assertEquals( ResultCodeEnum.SUCCESS, delResponse.getLdapResult().getResultCode() );
        
        // Check the CASeqNumber, it must be 1 now
        assertEquals( seqNumber + 1, getCASeqNumber( "ou=SAP1,ou=system" ) );
    }
    
    
    /**
     * Test the deletion of a subentry under an IAP
     */
    @Test
    public void testDeleteSubentryUnderIAP() throws Exception
    {
        Entry sap = LdifUtils.createEntry( 
            "ou=SAP1,ou=system", 
            "ObjectClass: top",
            "ObjectClass: organizationalUnit", 
            "ou: SAP1", 
            "administrativeRole: collectiveAttributeSpecificArea" );

        // It should succeed
        AddResponse response = adminConnection.add( sap );

        assertEquals( ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode() );
        
        // Add the IAP
        Entry iap = LdifUtils.createEntry( 
            "ou=IAP1,ou=SAP1,ou=system", 
            "ObjectClass: top",
            "ObjectClass: organizationalUnit", 
            "ou: IAP1", 
            "administrativeRole: collectiveAttributeInnerArea" );

        // It should succeed
        response = adminConnection.add( iap );

        assertEquals( ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode() );
        
        assertEquals( -1L, getCASeqNumber( "ou=SAP1,ou=system" ) );
        assertEquals( -1L, getCASeqNumber( "ou=IAP1,ou=SAP1,ou=system" ) );
        
        // Add a subentry now
        Entry subentry = LdifUtils.createEntry( 
            "cn=test,ou=IAP1,ou=SAP1,ou=system", 
            "ObjectClass: top",
            "ObjectClass: subentry", 
            "ObjectClass: collectiveAttributeSubentry",
            "cn: test",
            "subtreeSpecification: {}", 
            "c-o: Test Org" );

        response = adminConnection.add( subentry );
        assertEquals( ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode() );

        long seqNumberSAP = getCASeqNumber( "ou=SAP1,ou=system" );
        assertEquals( -1L, seqNumberSAP );
        
        long seqNumberIAP = getCASeqNumber( "ou=IAP1,ou=SAP1,ou=system" );
        assertTrue( seqNumberIAP > -1L );

        // Now delete it
        DeleteResponse delResponse = adminConnection.delete( "cn=test,ou=IAP1,ou=SAP1,ou=system" );
        assertEquals( ResultCodeEnum.SUCCESS, delResponse.getLdapResult().getResultCode() );
        
        // Check the CASeqNumbers, it must be 1 now
        assertEquals( -1L, getCASeqNumber( "ou=SAP1,ou=system" ) );
        assertEquals( seqNumberIAP + 1, getCASeqNumber( "ou=IAP1,ou=SAP1,ou=system" ) );
        
        assertTrue( checkIsAbsent( "cn=test,ou=IAP1,ou=SAP1,ou=system" ) );
    }
}