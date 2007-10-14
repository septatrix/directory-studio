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

package org.apache.directory.studio.ldapbrowser.core.jobs;


import java.util.HashSet;
import java.util.Set;

import org.apache.directory.studio.connection.core.StudioProgressMonitor;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.AttributeDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.ValueDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.ModelModificationException;


public class DeleteAttributesValueJob extends AbstractAttributeModificationJob
{

    private IEntry entry;

    private IAttribute[] attributes;

    private IValue[] values;

    private EntryModificationEvent event;


    public DeleteAttributesValueJob( IAttribute attributes[], IValue[] values )
    {
        this.attributes = attributes;
        this.values = values;
        for ( int i = 0; attributes != null && i < attributes.length; i++ )
        {
            if ( this.entry == null )
            {
                this.entry = attributes[i].getEntry();
            }
        }
        for ( int i = 0; values != null && i < values.length; i++ )
        {
            if ( this.entry == null )
            {
                this.entry = values[i].getAttribute().getEntry();
            }
        }

        setName( attributes.length + values.length == 1 ? BrowserCoreMessages.jobs__delete_attributes_name_1
            : BrowserCoreMessages.jobs__delete_attributes_name_n );
    }


    public DeleteAttributesValueJob( AttributeHierarchy ah )
    {
        this( ah.getAttributes(), new IValue[0] );
    }


    public DeleteAttributesValueJob( IValue value )
    {
        this( new IAttribute[0], new IValue[]
            { value } );
    }


    protected void executeAttributeModificationJob( StudioProgressMonitor monitor ) throws ModelModificationException
    {

        monitor.beginTask( attributes.length + values.length == 1 ? BrowserCoreMessages.jobs__delete_attributes_task_1
            : BrowserCoreMessages.jobs__delete_attributes_task_n, 2 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        for ( int i = 0; attributes != null && i < attributes.length; i++ )
        {
            attributes[i].getEntry().deleteAttribute( attributes[i] );
        }
        for ( int i = 0; values != null && i < values.length; i++ )
        {
            values[i].getAttribute().deleteValue( values[i] );
        }

        entry.getBrowserConnection().delete( attributes, monitor );
        entry.getBrowserConnection().delete( values, monitor );

        if ( values.length > 0 )
        {
            this.event = new ValueDeletedEvent( entry.getBrowserConnection(), entry, values[0].getAttribute(), values[0] );
        }
        else if ( attributes.length > 0 )
        {
            this.event = new AttributeDeletedEvent( entry.getBrowserConnection(), entry, attributes[0] );
        }
    }


    protected IEntry getModifiedEntry()
    {
        return entry;
    }


    protected String[] getAffectedAttributeDescriptions()
    {
        Set affectedAttributeNameSet = new HashSet();
        for ( int i = 0; i < attributes.length; i++ )
        {
            affectedAttributeNameSet.add( attributes[i].getDescription() );
        }
        for ( int i = 0; i < values.length; i++ )
        {
            affectedAttributeNameSet.add( values[i].getAttribute().getDescription() );
        }
        return ( String[] ) affectedAttributeNameSet.toArray( new String[affectedAttributeNameSet.size()] );
    }


    protected void runNotification()
    {
        if ( this.event != null )
        {
            EventRegistry.fireEntryUpdated( this.event, this );
        }
    }


    protected String getErrorMessage()
    {
        return attributes.length + values.length == 1 ? BrowserCoreMessages.jobs__delete_attributes_error_1
            : BrowserCoreMessages.jobs__delete_attributes_error_n;
    }

}
