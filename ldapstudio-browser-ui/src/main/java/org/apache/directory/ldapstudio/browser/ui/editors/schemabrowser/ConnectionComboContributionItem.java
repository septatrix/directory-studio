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

package org.apache.directory.ldapstudio.browser.ui.editors.schemabrowser;


import org.apache.directory.ldapstudio.browser.core.BrowserCorePlugin;
import org.apache.directory.ldapstudio.browser.core.events.ConnectionUpdateEvent;
import org.apache.directory.ldapstudio.browser.core.events.ConnectionUpdateListener;
import org.apache.directory.ldapstudio.browser.core.events.EventRegistry;
import org.apache.directory.ldapstudio.browser.core.model.IConnection;
import org.apache.directory.ldapstudio.browser.ui.widgets.connection.ConnectionContentProvider;
import org.apache.directory.ldapstudio.browser.ui.widgets.connection.ConnectionLabelProvider;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;


/**
 * A contribution item that adds a combo with connections to the toolbar.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class ConnectionComboContributionItem extends ContributionItem implements ConnectionUpdateListener
{
    /** The schema page */
    private SchemaPage schemaPage;

    /** Flag indicating if the combo's selection is changed programatically */
    private boolean inChange;

    /** The combo viewer */
    private ComboViewer comboViewer;

    /** The tool item */
    private ToolItem toolitem;


    /**
     * Creates a new instance of ConnectionContributionItem.
     *
     * @param schemaPage the schema page
     */
    public ConnectionComboContributionItem( SchemaPage schemaPage )
    {
        this.schemaPage = schemaPage;
        this.inChange = false;
    }


    /**
     * Creates and returns the control for this contribution item
     * under the given parent composite.
     *
     * @param parent the parent composite
     * @return the new control
     */
    private Control createControl( Composite parent )
    {
        comboViewer = new ComboViewer( parent, SWT.DROP_DOWN | SWT.READ_ONLY );
        comboViewer.setLabelProvider( new ConnectionLabelProvider() );
        comboViewer.setContentProvider( new ConnectionContentProvider() );
        comboViewer.setInput( BrowserCorePlugin.getDefault().getConnectionManager() );
        comboViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                // Do not set the input of the schema browser if 
                // the selection was changed programatically.
                if ( !inChange )
                {
                    IConnection connection = getConnection();
                    schemaPage.getSchemaBrowser().setInput( new SchemaBrowserInput( connection, null ) );
                }
            }
        } );

        EventRegistry.addConnectionUpdateListener( this );

        // Initialize width of combo
        toolitem.setWidth( comboViewer.getCombo().computeSize( SWT.DEFAULT, SWT.DEFAULT, true ).x );

        return comboViewer.getCombo();
    }


    /**
     * @see org.eclipse.jface.action.ContributionItem#dispose()
     */
    public void dispose()
    {
        EventRegistry.removeConnectionUpdateListener( this );
        comboViewer = null;
    }


    /**
     * The control item implementation of this <code>IContributionItem</code>
     * method calls the <code>createControl</code> method.
     * 
     * @param parent the parent of the control to fill
     */
    public final void fill( Composite parent )
    {
        createControl( parent );
    }


    /**
     * The control item implementation of this <code>IContributionItem</code>
     * method throws an exception since controls cannot be added to menus.
     * 
     * @param parent the menu
     * @param index menu index
     */
    public final void fill( Menu parent, int index )
    {
        Assert.isTrue( false, "Can't add a control to a menu" );//$NON-NLS-1$
    }


    /**
     * The control item implementation of this <code>IContributionItem</code>
     * method calls the <code>createControl</code>  method to
     * create a control under the given parent, and then creates
     * a new tool item to hold it.
     * 
     * @param parent the ToolBar to add the new control to
     * @param index the index
     */
    public void fill( ToolBar parent, int index )
    {
        toolitem = new ToolItem( parent, SWT.SEPARATOR, index );
        Control control = createControl( parent );
        toolitem.setControl( control );
    }


    /**
     * {@inheritDoc}
     */
    public void connectionUpdated( ConnectionUpdateEvent connectionUpdateEvent )
    {
        if ( comboViewer != null )
        {
            this.comboViewer.refresh();
        }
    }


    /**
     * Gets the connection.
     * 
     * @return the connection
     */
    public IConnection getConnection()
    {
        ISelection selection = comboViewer.getSelection();
        if ( !selection.isEmpty() )
        {
            return ( IConnection ) ( ( IStructuredSelection ) selection ).getFirstElement();
        }

        return null;
    }


    /**
     * Sets the connection.
     * 
     * @param connection the connection
     */
    public void setConnection( IConnection connection )
    {
        ISelection newSelection = new StructuredSelection( connection );
        ISelection oldSelection = comboViewer.getSelection();
        if ( !newSelection.equals( oldSelection ) )
        {
            inChange = true;
            comboViewer.setSelection( newSelection );
            inChange = false;
        }
    }


    /**
     * Updates the enabled state.
     */
    public void updateEnabledState()
    {
        comboViewer.getCombo().setEnabled( !schemaPage.isShowDefaultSchema() );
    }

}