---
layout: post
title: Hierarchical Network Schema for CX (Version 0.1)
date: 2023-11-06
collection: excluded_posts
categories: schema
permalink: /:categories/:title/
published: true
---

This document outlines the CX schema designed to store hierarchical structures within a CX network. Within Ideker Lab's applications, a hierarchy typically encompasses two distinct data categories:

1. An interaction (or similarity) network.
2. A hierarchy that is derived from the aforementioned interaction network.

The hierarchy and the interaction network can be stored in one or more CX files, contingent upon specific application requirements. This CX schema is designed for Cytoscape-Hiview integration, it should also cover other common use cases in Ideker Lab.

Applicable solely to CX version 2 (CX2) only, this schema accommodates two primary storage models:

1. **Multi-file model** - In this approach, hierarchies and associated interaction network(s) reside within two or more CX files. This model aligns with our usage in Cytoscape Web.
2. **Single file model** - This model contains only one CX file. Its major use case is for archival. 

## **Multi-file model**

In this model, the complete data set contains one network for the hierarchy, and one or more networks for the interaction networks. These networks can be stored as flat files in the file system. They can also be stored as CX networks in NDEx or a Cytoscape Web workspace.  

![HCX 2 file model]({{ site.baseurl }}/assets/img/HCX-2-file-model.png)

### Hierarchy network

This network stores the hierarchy derived from the interaction network, including its visual styles and layouts. In general, users can put any attributes on nodes and edges, and the network. However, this schema adds these constraints to some of the core aspects.

#### networkAttributes 
It should contain these attributes
1. Attribute name: **ndexSchema**

   Attribute value type: string

   Attribute value: hierarchy_v0.1
   
2. Attribute name: **HCX::modelFileCount**
   
   Attribute value type: integer

   Attribute value: Should be greater than 1. It is the actual count of all the files that this hierarchy references, including the hierarchy itself. This is the value that can be used to decide the type of a model file. 1 is a single file model and all other values means this is a multi-file model.

   **Note**: nodes on this hierarchy can point to its own interaction network. It is normally a subnetwork of the top level interaction network, but it can also be any network.

3. One of these attributes to link to the interaction network. 
   
   - Attribute name: **HCX::interactionNetworkUUID**

     Value: uuid of the interaction network. If the interaction network is stored on a NDEx server, this attribute should contain the uuid of that network. If the interaction network is a local network in users current Cytoscape Web workspace, this attribute contains the local uuid of that network. 
   
<!---
   - Attribute name: **HCX::interactionNetworkHost**

     Attribute type: string

     Attribute value: host name of the NDEx server that stores the interaction network. 

     NOTE: For production NDEx, use www.ndexbio.org as the hostname.

     This attribute should only be populated when HCX::interactionNetworkUUID is defined. If this value is null or this attribute is missing, the HCX::interactionNetworkUUID value should be treated as a Cytoscape Web local network UUID in the currently opened workspace.
--->
   - Attribute name: **HCX::interactionNetworkName**
   
     Value: file name as a string, assume the file is located in the same directory of the hierarchy

   - Attribute name: **HCX::interactionNetworkURL**
      
     Value: a URL which points to the interaction network.

   **Note**: If each node in the hierarchy points to its own interaction network, it is possible to omit the interaction links at the network level.


#### nodes

Each node in the hierarchy should have this attribute:
 - Attribute name: **HCX::isRoot**

   Attribute value type: boolean
      	
   Attribute value: Root node should have value true and all other nodes should have false on this attribute.

A node in the hierarchy can link to a subnetwork in the parent interaction network or a separate network. The linkage can be established using one of these attributes on a node in the hierarchy.

  - Attribute name: **HCX::members**

    Attribute value type: list of long

    Attribute value: list of CX IDs of nodes in the interaction network. It is similar to the CD_memberList attribute in the hierarchy network generated by the CDAPS app in Cytoscape.

  - Attribute name: **HCX::memberEdges**

    Attribute value type: list of long

    Attribute value: list of CX IDs of edges in the interaction network. 

  - Attribute name: **HCX::interactionNetworkUUID**, **HCX::interactionNetworkName** or **HCX::interactionNetworkURL**

    Attribute value type: string

    Attribute value: Using one of these attributes, a node can declare its own interaction network. When one of these attributes exists, it overwrites the value declared in the networkAttributes aspect.

<!--
  - Attribute name: **HCX::size**

    type: integer

    Description: Hiview utilizes this value to determine the node size in the circle-packing layout. Typically, this value represents the number of genes (or proteins) within a subsystem. However, users have the flexibility to assign alternative meanings to this attribute, provided the value assigned to a parent node consistently remains larger than that assigned to a child node. In instances where this attribute is absent, Hiview defaults to utilizing the element count of the attribute **HCX::member** for the **HCX::size** value. Should neither **HCX::member** nor **HCX::size** be defined for at least one node, the circle-packing layout option will be disabled in Hiview.
 -->

- Attribute name: **HCX::memberNames**

    type: list of string

    Description: Hiview uses this value to tag each member node in the circle-packing layout. Each element in this list need to be unique. Typically, each value in this list is the gene symbol of a member within a subsystem. However, users have the flexibility to assign alternative identifiers to this attribute. If this attribute is absent, Hiview will default to using the name attribute of the nodes specified in the **HCX::members** attribute for the node label. If neither **HCX::members** nor **HCX::memberNames** is defined for at least one node, the circle-packing layout option will be deactivated in Hiview. If both **HCX::members** and **HCX::memberNames** exist on a node, Hiview uses **HCX::members**.
 

- Attribute name: **HCX::memberQuery**

    Attribute value type: string

    Attribute value: a JSON string which describes a query on the interaction network. It has this structure:

    ```javascript
    {
     "nodes":  { 
   	     "attribute": string   // name of the attribute
  	     "values": list of values
 	  },
     "edges": {
       "attribute": string 
  	   "operator": string  // supported values are ">", "=", "<" or "in"
       "value": single value or a list of values 
      }  
    }  
    ```

    For example: this JSON structure 


    ```javascript

    { 
      "nodes":
      { 
       "attribute": "name",
       "values": ["AKT1", "MTOR","EGFR" ]
      }
     "edges": {
       "Attribute":"score",
       "operator": ">",
       "value":  0.6
     }
    }
    ```

    means this node links to a subnetwork derived from a direct query starting from  nodes which have "AKT1","MTOR" or "EGFR" on its name,and the edges with score value above 0.6 between the starting nodes. 

#### edges

Edges are directed. In the majority of our hierarchical networks, we adhere to the convention of designating the child node as the target node for each edge. However, users can opt to reverse the edge direction if it aligns more effectively with their use case.

#### Visualization of the hierarchy

The core aspects store the ball-and-stick (classic) view of the hierarchy, which includes a layout and one or many visual styles. 

The default visual style for the classic view is stored in the visualProperties aspect. If the classic view has more than one style defined on it, these additional styles should be named and they should be stored in the otherVisualProperties aspect. This is the structure of the otherVisualProperties aspect:

   ```javascript
      {
        otherVisualProperties: [
          {
            "name": string    // name of the style 
            "visualProperties": [
                  //Visual properties and mapping rules defined in the style.
            ]
          }    
        ]
      }
   ```

Visualization data for circle-packing (Hiview) view are stored in these two aspects

1. circlePackingLayouts: This aspect contains the layout information for Hiview. It has this data structure:

    ```javascript
      {
         circlePackingLayouts: 
          [ {
              "id":  string    // unique id
              "cxId":      //CX Id of node this circle represents
    		  "X":  number   // x coordinate of the circle
    		  "Y":  number // y coordinate of the circle
        	  "R":  number // radius of the circle
              "depth": integer // steps to the root node. It should be a non-negative integer.
              "type":  gene or assembly //
  	        }
           ]
      }		
    ```

    For circle packing Depth is 0 for root node (blue) as seen in this figure

    ![]({{ site.baseurl }}/assets/img/ball_n_stick_depth.png)

2. circlePackingStyles:

```
  {
    visualProperties and mappings 
  }
```

### Interaction network

There is no special requirements for the interaction network(s) in the multi-file model.

##  **Single-file model**

In this model, a hierarchy and its interaction network are stored in a single CX network. The hierarchy is stored in the core aspects, such as "nodes" and "edges" aspects, and interaction network data are stored in opaque aspects prefixed with "interactionNetwork::" in the aspect name.

![HCX 2 file model]({{ site.baseurl }}/assets/img/HCX-1-file-model.png)

### Constraints on core CX aspects
In general, users can put any attributes on nodes and edges, and the network. However, this schema adds these constraints to some of the core aspects.

#### networkAttributes

It should contain these 2 attributes
 1. Attribute name: **ndexSchema**
    
    Attribute value type: string
    
    Attribute value: hierarchy_v0.1
 
 2. Attribute name: **HCX::modelFileCount**
    
    Attribute value type: integer
    
    Attribute value: 1

#### Nodes

All specifications regarding node attributes in the multi-file model apply to the nodes in the single-file model, with the exception that the HCX::interactionNetworkUUID, HCX::interactionNetworkName, and HCX::interactionNetworkURL attributes are not supported.

#### Interaction network

The data of the interaction network are stored in aspects that are prefixed with "interactionNetwork::". For example

interactionNetwsork::attributeDeclarations aspect stores the attributeDeclarations aspect of the interaction network, and interactionNetwork::nodes aspect stores the nodes aspect of the interaction network. The interaction network can contain any number of styles on it using the scheme we detailed in the "Visualization of the hierarchy" section. The only difference is that these aspects need to be prefixed with  "interactionNetwork::" in the single file model.

