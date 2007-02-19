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

package org.apache.directory.ldapstudio.schemas.view.viewers;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.ldapstudio.schemas.Activator;
import org.apache.directory.ldapstudio.schemas.controller.actions.HideAttributeTypesAction;
import org.apache.directory.ldapstudio.schemas.controller.actions.HideObjectClassesAction;
import org.apache.directory.ldapstudio.schemas.model.AttributeType;
import org.apache.directory.ldapstudio.schemas.model.ObjectClass;
import org.apache.directory.ldapstudio.schemas.model.SchemaPool;
import org.apache.directory.ldapstudio.schemas.view.viewers.wrappers.AttributeTypeWrapper;
import org.apache.directory.ldapstudio.schemas.view.viewers.wrappers.DisplayableTreeElement;
import org.apache.directory.ldapstudio.schemas.view.viewers.wrappers.HierarchyViewFirstNameSorter;
import org.apache.directory.ldapstudio.schemas.view.viewers.wrappers.HierarchyViewOidSorter;
import org.apache.directory.ldapstudio.schemas.view.viewers.wrappers.IntermediateNode;
import org.apache.directory.ldapstudio.schemas.view.viewers.wrappers.ObjectClassWrapper;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;


/**
 * This class implements the content provider for the Hierarchy View.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class HierarchicalContentProvider implements IStructuredContentProvider, ITreeContentProvider
{
    /** The Schema Pool holding all schemas */
    private SchemaPool schemaPool;

    /** The HashTable containing all the object classes */
    private Hashtable<String, ObjectClass> objectClassTable;

    /** The HashTable containing all the attribute types */
    private Hashtable<String, AttributeType> attributeTypeTable;

    /** The FirstName Sorter */
    private HierarchyViewFirstNameSorter firstNameSorter;

    /** The OID Sorter */
    private HierarchyViewOidSorter oidSorter;


    /**
     * Creates a new instance of HierarchicalContentProvider.
     *
     * @param schemaPool
     *      the associated Schema Pool
     */
    public HierarchicalContentProvider()
    {
        this.schemaPool = SchemaPool.getInstance();

        objectClassTable = schemaPool.getObjectClassesAsHashTableByName();
        attributeTypeTable = schemaPool.getAttributeTypesAsHashTableByName();

        firstNameSorter = new HierarchyViewFirstNameSorter();
        oidSorter = new HierarchyViewOidSorter();
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements( Object inputElement )
    {
        return getChildren( inputElement );
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren( Object parentElement )
    {
        List<DisplayableTreeElement> children = new ArrayList<DisplayableTreeElement>();

        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        int group = store.getInt( HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_GROUPING );
        int sortBy = store.getInt( HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_BY );
        int sortOrder = store.getInt( HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_ORDER );

        if ( parentElement instanceof ObjectClassWrapper )
        {
            //we are looking for the childrens of the contained objectClass
            ObjectClass objectClass = ( ( ObjectClassWrapper ) parentElement ).getMyObjectClass();

            //-> we need to compare each and every other objectClass's sup against them 
            //-> we also need to find a better way to do this (complexity wise)
            Collection<ObjectClass> objectClasses = objectClassTable.values();
            for ( Iterator iter = objectClasses.iterator(); iter.hasNext(); )
            {
                ObjectClass oClass = ( ObjectClass ) iter.next();

                //not this object class
                if ( oClass.getOid() != objectClass.getOid() )
                {
                    String[] sups = oClass.getSuperiors();
                    for ( String sup : sups )
                    {
                        ObjectClass oClassSup = objectClassTable.get( sup );
                        if ( oClassSup != null )
                        {
                            //the current object class is a sup of oClass
                            if ( oClassSup.equals( objectClass ) )
                            {
                                //we use an objectClass wrapper
                                children
                                    .add( new ObjectClassWrapper( oClass, ( DisplayableTreeElement ) parentElement ) );
                                break; //break only the inner for
                            }
                        }
                    }
                }
            }

            // Sort by
            if ( sortBy == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_BY_FIRSTNAME )
            {

                Collections.sort( children, firstNameSorter );
            }
            else if ( sortBy == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_BY_OID )
            {

                Collections.sort( children, oidSorter );
            }

            // Sort order
            if ( sortOrder == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_ORDER_DESCENDING )
            {
                Collections.reverse( children );
            }
        }
        if ( parentElement instanceof AttributeTypeWrapper )
        {
            //we are looking for the childrens of the contained attribute type
            AttributeType attributeType = ( ( AttributeTypeWrapper ) parentElement ).getMyAttributeType();

            //-> we need to compare each and every other attribute type sup against them 
            //-> we also need to find a better way to do this (complexity wise)
            Collection<AttributeType> attributeTypes = attributeTypeTable.values();
            for ( Iterator iter = attributeTypes.iterator(); iter.hasNext(); )
            {
                AttributeType aType = ( AttributeType ) iter.next();

                //not this attribute type
                if ( aType.getOid() != attributeType.getOid() )
                {
                    String aTypeSupName = aType.getSuperior();
                    if ( aTypeSupName != null )
                    {
                        AttributeType aTypeSup = attributeTypeTable.get( aType.getSuperior() );
                        if ( aTypeSup != null )
                        {
                            //the current object class is a sup of oClass
                            if ( aTypeSup.equals( attributeType ) )
                            {
                                //we use an objectClass wrapper
                                children
                                    .add( new AttributeTypeWrapper( aType, ( DisplayableTreeElement ) parentElement ) );
                                break; //break only the inner for
                            }
                        }
                    }
                }
            }

            // Sort by
            if ( sortBy == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_BY_FIRSTNAME )
            {

                Collections.sort( children, firstNameSorter );
            }
            else if ( sortBy == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_BY_OID )
            {

                Collections.sort( children, oidSorter );
            }

            // Sort order
            if ( sortOrder == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_ORDER_DESCENDING )
            {
                Collections.reverse( children );
            }
        }
        else if ( parentElement instanceof IntermediateNode )
        {
            IntermediateNode intermediate = ( IntermediateNode ) parentElement;

            if ( intermediate.getName().equals( "**Primary Node**" ) ) //$NON-NLS-1$
            {
                refreshOcsAndAts();

                List<ObjectClassWrapper> ocList = new ArrayList<ObjectClassWrapper>();
                if ( !Activator.getDefault().getDialogSettings().getBoolean(
                    HideObjectClassesAction.HIDE_OBJECT_CLASSES_DS_KEY ) )
                {
                    //bootstrap the tree exploring process by adding the top node into the tree
                    ObjectClass top = schemaPool.getObjectClass( "top" ); //$NON-NLS-1$
                    if ( top != null )
                    {
                        ObjectClassWrapper topWrapper = new ObjectClassWrapper( top, intermediate );
                        ocList.add( topWrapper );
                    }

                    //add the unresolved object-classes to the top of the hierarchy
                    Collection<ObjectClass> objectClasses = objectClassTable.values();
                    for ( Iterator iter = objectClasses.iterator(); iter.hasNext(); )
                    {
                        ObjectClass oClass = ( ObjectClass ) iter.next();
                        String[] sups = oClass.getSuperiors();
                        //if no supperiors had been set
                        if ( sups.length == 0 )
                        {
                            ObjectClassWrapper wrapper = new ObjectClassWrapper( oClass, intermediate );
                            wrapper.setState( ObjectClassWrapper.State.unResolved );
                            ocList.add( wrapper );
                            this.hasChildren( wrapper );
                        }
                        else
                        {
                            for ( String sup : sups )
                            {
                                ObjectClass oClassSup = objectClassTable.get( sup );
                                if ( oClassSup == null )
                                {
                                    ObjectClassWrapper wrapper = new ObjectClassWrapper( oClass, intermediate );
                                    wrapper.setState( ObjectClassWrapper.State.unResolved );
                                    ocList.add( wrapper );
                                }
                            }
                        }
                    }
                }

                List<AttributeTypeWrapper> atList = new ArrayList<AttributeTypeWrapper>();
                if ( !Activator.getDefault().getDialogSettings().getBoolean(
                    HideAttributeTypesAction.HIDE_ATTRIBUTE_TYPES_DS_KEY ) )
                {
                    //add the unresolved object-classes to the top of the hierarchy
                    Collection<AttributeType> attributeTypes = attributeTypeTable.values();
                    for ( Iterator iter = attributeTypes.iterator(); iter.hasNext(); )
                    {
                        AttributeType aType = ( AttributeType ) iter.next();
                        String sup = aType.getSuperior();
                        //if no superior had been set
                        if ( sup == null )
                        {
                            AttributeTypeWrapper wrapper = new AttributeTypeWrapper( aType, intermediate );
                            atList.add( wrapper );
                        }
                    }
                }

                if ( group == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_GROUPING_ATFIRST )
                {
                    // Sort by
                    if ( sortBy == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_BY_FIRSTNAME )
                    {
                        Collections.sort( atList, firstNameSorter );
                        Collections.sort( ocList, firstNameSorter );
                    }
                    else if ( sortBy == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_BY_OID )
                    {
                        Collections.sort( atList, oidSorter );
                        Collections.sort( ocList, oidSorter );
                    }

                    // Sort Order
                    if ( sortOrder == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_ORDER_DESCENDING )
                    {
                        Collections.reverse( atList );
                        Collections.reverse( ocList );
                    }

                    // Group
                    children.addAll( atList );
                    children.addAll( ocList );
                }
                else if ( group == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_GROUPING_OCFIRST )
                {
                    // Sort by
                    if ( sortBy == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_BY_FIRSTNAME )
                    {
                        Collections.sort( atList, firstNameSorter );
                        Collections.sort( ocList, firstNameSorter );
                    }
                    else if ( sortBy == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_BY_OID )
                    {
                        Collections.sort( atList, oidSorter );
                        Collections.sort( ocList, oidSorter );
                    }

                    // Sort Order
                    if ( sortOrder == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_ORDER_DESCENDING )
                    {
                        Collections.reverse( atList );
                        Collections.reverse( ocList );
                    }

                    // Group
                    children.addAll( ocList );
                    children.addAll( atList );
                }
                else if ( group == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_GROUPING_MIXED )
                {
                    // Group
                    children.addAll( atList );
                    children.addAll( ocList );

                    // Sort by
                    if ( sortBy == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_BY_FIRSTNAME )
                    {

                        Collections.sort( children, firstNameSorter );
                    }
                    else if ( sortBy == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_BY_OID )
                    {

                        Collections.sort( children, oidSorter );
                    }

                    // Sort order
                    if ( sortOrder == HierarchyViewSorterDialog.PREFS_HIERARCHY_VIEW_SORTING_ORDER_DESCENDING )
                    {
                        Collections.reverse( children );
                    }
                }
            }
        }

        return children.toArray();
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent( Object element )
    {
        if ( element instanceof ObjectClassWrapper )
        {
            return ( ( ObjectClassWrapper ) element ).getParent();
        }
        else if ( element instanceof AttributeTypeWrapper )
        {
            return ( ( AttributeTypeWrapper ) element ).getParent();
        }
        else if ( element instanceof IntermediateNode )
        {
            return ( ( IntermediateNode ) element ).getParent();
        }

        return null;
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren( Object element )
    {
        if ( element instanceof ObjectClassWrapper )
        {
            return getChildren( ( ObjectClassWrapper ) element ).length > 0;
        }
        else if ( element instanceof AttributeTypeWrapper )
        {
            return getChildren( ( AttributeTypeWrapper ) element ).length > 0;
        }
        else if ( element instanceof IntermediateNode )
        {
            return getChildren( ( IntermediateNode ) element ).length > 0;
        }

        return false;
    }


    /**
     * Refreshes the object classes and attribute types HahshTables.
     */
    private void refreshOcsAndAts()
    {
        objectClassTable = schemaPool.getObjectClassesAsHashTableByName();

        attributeTypeTable = schemaPool.getAttributeTypesAsHashTableByName();
    }


    /**
     * Initialize a tree viewer to display the information provided by the specified content
     * provider.
     * 
     * @param viewer
     *      the tree viewer
     */
    public void bindToTreeViewer( TreeViewer viewer )
    {
        viewer.setContentProvider( this );
        viewer.setLabelProvider( new HierarchicalLabelProvider() );

        IntermediateNode invisibleNode = new IntermediateNode( "**Primary Node**", null ); //$NON-NLS-1$
        viewer.setInput( invisibleNode );
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose()
    {
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
    }
}
