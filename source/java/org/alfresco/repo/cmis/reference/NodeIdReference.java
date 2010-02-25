/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.reference;

import org.alfresco.cmis.CMISRepositoryReference;
import org.alfresco.cmis.CMISServices;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * GUID Object Reference. This class decodes node 'paths' generated by links.lib.atom.ftl. These are not the same as
 * CMIS object IDs.
 * 
 * @author davidc
 */
public class NodeIdReference extends AbstractObjectReference
{
    private String id;
    private String[] reference;

    /**
     * Construct
     * 
     * @param cmisServices
     * @param repo
     * @param id
     */
    public NodeIdReference(CMISServices cmisServices, CMISRepositoryReference repo, String id)
    {
        super(cmisServices, repo);

        this.id = id;

        StoreRef storeRef = repo.getStoreRef();
        String[] idParts = this.id.split("/");
        reference = new String[2 + idParts.length];
        reference[0] = storeRef.getProtocol();
        reference[1] = storeRef.getIdentifier();
        System.arraycopy(idParts, 0, reference, 2, idParts.length);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISObjectReference#getNodeRef()
     */
    public NodeRef getNodeRef()
    {
        return cmisServices.getNode("node", reference);
    }

    /**
     * @return  id
     */
    public String getId()
    {
        return id;
    }
    
    @Override
    public String toString()
    {
        return "NodeIdReference[storeRef=" + repo.getStoreRef() + ",id=" + id + "]";
    }

}
