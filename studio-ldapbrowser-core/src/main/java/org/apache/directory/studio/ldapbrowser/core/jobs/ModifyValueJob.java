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


import org.apache.directory.studio.connection.core.StudioProgressMonitor;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.ValueModifiedEvent;
import org.apache.directory.studio.ldapbrowser.core.internal.model.Value;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.ModelModificationException;


public class ModifyValueJob extends AbstractAttributeModificationJob
{

    private IAttribute attribute;

    private IValue oldValue;

    private Object newRawValue;

    private ValueModifiedEvent event;


    public ModifyValueJob( IAttribute attribute, IValue oldValue, Object newRawValue )
    {
        this.attribute = attribute;
        this.oldValue = oldValue;
        this.newRawValue = newRawValue;
        setName( BrowserCoreMessages.jobs__modify_value_name );
    }


    protected void executeAttributeModificationJob( StudioProgressMonitor monitor ) throws ModelModificationException
    {

        monitor.beginTask( BrowserCoreMessages.jobs__modify_value_task, 2 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        IValue newValue = new Value( attribute, newRawValue );
        attribute.modifyValue( oldValue, newValue );
        attribute.getEntry().getBrowserConnection().modify( oldValue, newValue, monitor );

        this.event = new ValueModifiedEvent( attribute.getEntry().getBrowserConnection(), attribute.getEntry(), attribute,
            oldValue, newValue );
    }


    protected IEntry getModifiedEntry()
    {
        return attribute.getEntry();
    }


    protected String[] getAffectedAttributeDescriptions()
    {
        return new String[]
            { this.attribute.getDescription() };
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
        return BrowserCoreMessages.jobs__modify_value_error;
    }

}
