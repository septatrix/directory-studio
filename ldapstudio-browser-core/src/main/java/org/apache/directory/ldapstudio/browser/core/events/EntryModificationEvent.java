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

package org.apache.directory.ldapstudio.browser.core.events;


import org.apache.directory.ldapstudio.browser.core.model.IConnection;
import org.apache.directory.ldapstudio.browser.core.model.IEntry;


/**
 * The root of all events that indecate an {@link IEntry} modification.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public abstract class EntryModificationEvent
{

    /** The connection. */
    protected IConnection connection;

    /** The entry. */
    protected IEntry modifiedEntry;


    /**
     * Creates a new instance of EntryModificationEvent.
     * 
     * @param modifiedEntry the modified entry
     * @param connection the connection
     */
    public EntryModificationEvent( IConnection connection, IEntry modifiedEntry )
    {
        this.connection = connection;
        this.modifiedEntry = modifiedEntry;
    }


    /**
     * Gets the connection.
     * 
     * @return the connection
     */
    public IConnection getConnection()
    {
        return connection;
    }


    /**
     * Gets the modified entry.
     * 
     * @return the modified entry
     */
    public IEntry getModifiedEntry()
    {
        return modifiedEntry;
    }

}
