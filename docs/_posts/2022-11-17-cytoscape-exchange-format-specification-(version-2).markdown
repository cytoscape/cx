---
layout: post
title:  "Cytoscape Exchange Format Specification (Version 2)"
date:   2025-07-22 
categories: CX2 Specification
---


## Motivation


The goals of this major revision of CX are
 
1. Making the CX data model simpler so that it can be easily understood and used by developers.
2. Better support for processing networks while streaming. The 2.0 spec will significantly reduce the memory footprint for operations such as filtering nodes and edges based on node/edge properties and converting the CX network to other formats. 
3. Making CX networks more compact to improve data transfer speed.

**_NOTE_**: CX version 2 is commonly referred to as CX2. In the Cytoscape ecosystem, CX2 files are typically denoted with the .cx2 extension, distinguishing them from CX version 1 networks, which are usually represented with the .cx suffix.

## Table of contents

1. [Top Level Structure of CX](#top-level-structure-of-cx)
2. [CX Descriptor](#cx-descriptor)
3. [Pre and Post Metadata in CX](#pre-and-post-metadata-in-cx)
4. [Aspect Data Blocks](#aspect-data-blocks)
5. [Status Aspect](#status-aspect)
6. [CX Core Aspects](#cx-core-aspects)
  - [attributeDeclarations](#attributedeclarations)
  - [nodes](#nodes)
  - [edges](#edges)
  - [visualProperties](#visualproperties)
  - [nodeBypasses](#nodebypasses)
  - [edgeBypasses](#edgebypasses)
  - [visualEditorProperties](#visualeditorproperties)
7. [Opaque aspects](#opaque-aspects) 
8. [Numeric values in this version of CX](#numeric-values-in-this-version-of-cx)
9. [Example CX files](#example-cx-files)
 

## Top-Level Structure of CX

A complete CX2 network is contained in a single JSON array. The first element in this array must be the CX descriptor object, followed by objects containing metadata, network data (aspects), and a final status object.


The overall structure is an array of objects, as shown below:

```
[
  { "CXVersion": "2.0", ... },  // The CX descriptor object
  { "metaData": [...] },       // An optional metadata object
  { "attributeDeclarations": [...] }, // Attribute declaration aspect (required for networks with attributes) 
  ...
  { "nodes": [...] },          // An aspect data block object
  { "edges": [...] },          // An aspect data block object
  ...
  { "metaData": [...] },       // Optional metadata (post-metadata)
  { "status": [...] }          // The final status object
]

```

**Note**: All aspect data blocks MUST appear after the `attributeDeclarations` block (so that attribute types are already known) and before the optional post-metadata section. See the [CX Descriptor](#cx-descriptor) section for details on fragmented aspects.


## CX Descriptor

As described in the top-level structure, the first element of the CX2 JSON array must be the CX descriptor object. This object defines global attributes for the entire document. It has these attributes in it


- **"CXVersion"** - the version number of the CX schema used in this document. Its format is "major.minor". Required
- **"hasFragments" (boolean)** - indicates whether any aspect is delivered in multiple fragments. If your file contains exactly one fragment for each aspect, set to false. The default value of this attribute is false.
    - Allowing fragmented aspects can be useful in applications that need to incrementally write out mixed types of elements when generating a CX network. One common use case is that an application needs to stream out a subnetwork from a large network when traversing the large network.
    - Non-fragmented CX2 networks are more compact and will allow users to write more efficient applications. For example, if an application only needs a certain aspect when reading a non-fragmented CX network from an input stream, the application can stop the reading when it sees the end of that aspect. 

Fragmented aspects. When the CX descriptor sets "hasFragments": true, any aspect may be split across multiple data blocks. A CX2 file may therefore contain a mixture of fragmented and non-fragmented aspects. Each fragment MUST appear somewhere after the required attributeDeclarations block (so that attribute types are already known) and before the optional post-metadata section. Within that span the producer may place fragments in any order; consumers MUST concatenate all fragments of the same aspect in the order they are encountered.

The CX descriptor in a non-fragmented CX2 network looks like this:


{% highlight json %}
[
  { 
    "CXVersion": 	"2.0"
    "hasFragments":	false
  }
   .... 
]  
{% endhighlight %}


## Pre and Post Metadata in CX


CX aspect metadata declaration is optional. It can be used for data integrity checks when users want to use a CX document. Aspect metadata can be declared in either pre or post metadata sections. A network creator should always put metadata in the pre-metadata section if it is possible. 

A metadata object has these attributes in it: 

{% highlight json %}
  {
    "name":         string,
    "elementCount": long 
  }
{% endhighlight %}


- **"name"**  -  the name of the aspect and is the only required attribute.
- **"elementCount"** - the number of elements in this aspect. It is optional.


This is an example of the metadata section in a CX2 document:

{% highlight json %}
[
    {  
        "CXVersion": "2.0",
        "hasFragments": false
    },
    {
        "metaData": [
            {
                "elementCount": 1,
                "name": "networkAttributes"
            },
            {
                "elementCount": 6,
                "name": "nodes"
            },
            {
                "elementCount": 6,
                "name": "edges"
            }
        ]
    },
    ...    
]
{% endhighlight %}



## Aspect Data Blocks
Aspect data blocks contain network data that are organized in aspects. When the aspect data blocks are completely missing, this CX document represents an empty network. 

Each data block is a JSON object that has a single attribute in it. The name of the attribute is the aspect name. Its value is a list of aspect elements. All elements in an aspect should have the same data structure. The format of an aspect data block is like this:


```
{ ASPECT_NAME: [ aspect_elements ] }
```

**Note**: All aspect data blocks MUST appear after the `attributeDeclarations` aspect (so that attribute types are already known) and before the optional post-metadata section, regardless of whether aspects are fragmented or not. When a CX2 network contains fragmented aspects (indicated by `"hasFragments": true` in the CX descriptor), consumers must concatenate all fragments of the same aspect in the order they are encountered. See the [CX Descriptor](#cx-descriptor) section for more details on fragmented aspects.

## Status Aspect
A complete CX stream ends with a status aspect. This aspect tells the recipient if the CX is successfully generated by the source. A use case is that a source is generating a CX network incrementally and transmits the data to the recipient through a stream, when an error occurs during the creation of the CX network, the source can write out a status aspect to the CX stream with some error message in it and finish the CX network immediately. The recipient will know from this aspect that the CX is a bad object and can stop the downstream process on this network. The status aspect has this structure:

{% highlight json %}
{
  "status" : [ {
    "error" : string,
    "success" : boolean
   } ]
}
{% endhighlight %}

The `"error"` field holds the error message when an error occurs. `"success"` fields tell if the CX document is successfully generated by the source. If the value of `"success"` is *true*, value in attribute `"error"` will be treated as a warning.

A CX2 producer MUST terminate every stream with exactly one status aspect; consumers SHOULD treat a missing status as an I/O error. 

## CX Core Aspects

Core aspects are the set of aspects that are supported by all Cytoscape Ecosystem compatible applications. These are the names and definition of core aspects in CX 


# attributeDeclarations

This aspect declares the data types, default values, and aliases for attributes used in other aspects.

**Key Rules:**

  * **Placement is critical**: The `attributeDeclarations` aspect **must appear before the aspects it declares attributes for**. For example, to declare attributes for `nodes` and `edges`, the `attributeDeclarations` block must be placed before the `nodes` and `edges` blocks in the top-level CX2 array.
  * **Declarations are comprehensive**: Declarations should be made for attributes within `networkAttributes`, `nodes`, and `edges`.
  * **All values of an attribute must have the same data type in an aspect.** 


### Structure of attributeDeclarations

The `attributeDeclarations` aspect is a JSON object where the key is `"attributeDeclarations"` and the value is an array of declaration objects. For a standard, non-fragmented network, this array contains **exactly one** declaration object.

This single object then contains keys for each aspect whose attributes are being declared (e.g., `"networkAttributes"`, `"nodes"`, `"edges"`).

### Example for a Single Network

Here is a complete example of an `attributeDeclarations` aspect for a typical network. It demonstrates how to declare network, node, and edge attributes, including the use of aliases and default values.

{% highlight json %}
[
   ...
   "attributeDeclarations": [
       {
         "networkAttributes": {
              "is_drug_network": { "d": "boolean" },
              "publication_count": { "d": "integer" }
           },
         "nodes": {
              "gene_name": { "d": "string", "a": "gn" },
              "expression_level": { "d": "double", "v": "0.0" }
           },
         "edges": {
              "interaction_type": { "d": "string", "a": "i", "v": "ppi" },
              "confidence_score": { "d": "double" }
           }
       }
     ]
   ... 
]
{% endhighlight %}

**In this example:**

  * The entire block is an object with a single key, `"attributeDeclarations"`.
  * Its value is an array with one object, as is standard for non-fragmented networks.
  * **networkAttributes**: Declares a boolean `is_drug_network` and an integer `publication_count`.
  * **nodes**: Declares a string `gene_name` with a shorter alias `gn` and a double `expression_level` with a default value of `0.0`.
  * **edges**: Declares a string `interaction_type` with an alias `i` and a default value of `ppi`, and a double `confidence_score`.


#### Declaration Properties

Within each aspect block (e.g., `nodes`), every attribute you want to declare is a key. The value is an object with one or more of the following properties:

  * **`"d"`** (data type): **Required.** Declares the data type of the attribute. Supported types are `string`, `long`, `integer`, `double`, `boolean`, and list counterparts (e.g., `list_of_string`, `list_of_long`, `list_of_integer`, `list_of_double`, or `list_of_boolean` ). 
  * **`"v"`** (default value): **Optional.** Declares a default value for an attribute in the `nodes` or `edges` aspects. If an element is missing this attribute, it will be automatically assigned the default value. This can help reduce file size but should be used carefully, as it cannot represent `null` values for that attribute.
  * **`"a"`** (alias): **Optional.** Declares a shorter alias for an attribute name in the `nodes` or `edges` aspects. If an alias is declared, the full attribute name can no longer be used in the `nodes` or `edges` data blocks; the alias must be used instead. Aliases are **not** permitted for `networkAttributes`.
  
  

The `attributeDeclarations` aspect can be fragmented, but when an attribute type is declared, it has to be declared before it is used in that aspect. 


# networkAttributes

For a single network, the networkAttributes aspect has only one element, and that element is itself a JSON object whose keys are the attribute names. In other words, the aspect data block looks like this:

{% highlight json %}
{
  "networkAttributes": [
    {
      "name": "Cell Cycle Map",            
      "description": "Yeast cell cycle regulatory network", 
      "version": "1.2"                      
      /* any additional attributes declared in
         networkAttributes appear here */
    }
  ]
}

{% endhighlight %}

**Rules**

* The surrounding aspect object must use the standard aspect data‑block shape: { "networkAttributes": [ … ] }.
* The array must contain exactly one object for single‑network CX2 files.
* Each key in that object is an attribute name.  The reserved names are name, description, and version.
* All keys must be declared beforehand in the attributeDeclarations.networkAttributes block and obey the declared data type.

# nodes

Elements in the `nodes` aspect have this struture:

{% highlight json %}
{
  "id":  long,
  "v":   object
  "x":   double,
  "y":  double,
  "z" :  double,
}
{% endhighlight %}

- **"id"** -  the unique id in the scope of the nodes aspect. 
- **"v"** - holds all node attributes as one JSON object. In this object, attributes "x","y" and "z" are treated as ordinary attributes instead of coordinates. To avoid confusion, the attribute name "id" is not allowed in this object. Also, there are 3 reserved node attribute names in CX:
  - **"name"**: node name. Is normally used as a label for the node when the network is visualized as a graph. For biological networks, we recommend using gene symbols as node names (if possible) to increase the interoperability of your network with other applications in the Cytoscape ecosystem. Its type is string.
  - **"represents"**:    a standard identifier defining what the node represents. Its type is string.
  - "alias": alternative identifiers for the node. Its type is list-of-string.
- **"x"** - the x coordinate of the node.  
- **"y"** - the y coordinate of the node. x and y attributes are optional, but CX has these constraints on them
If a node has an "x" value in it, it must also have a "y" value. 
If one node has an "x" and "y" value, all nodes in this CX network must have x and y values. 
- **“z”** -  z coordinate or z-order, depending on how the renderer interprets it. This property is optional. If a node has a ‘z’ attribute, it must also have x, and y. If one node has a ‘z’ coordinate, all nodes should have  "z" attributes.   

**Where do coordinates live?**

In CX2, each node element directly carries its layout coordinates through two optional numeric keys: x and y. Coordinates are rendered as CSS pixels in Cytoscape Web and Cytoscape Desktop, with (+x = right, +y = down). There is no cartesianLayout aspect in CX2 for single networks. That aspect existed only in CX (version 1) and has been removed.

CX2 node example
{% highlight json %}
{
  "nodes": [
    { "id": 1, "x": 150, "y": 75, "v": {"name": "GeneA"} },
    { "id": 2, "x": 320, "y": 210, "v": {"name": "GeneB"} }
  ]
}
{% endhighlight %}

Rules

1. x and y are optional – omit them if no fixed layout is intended. However, If any node specifies x/y, then all nodes MUST specify both x and y. Likewise, if any node specifies z, then all nodes MUST specify x, y and z.
2. Units are arbitrary but generally pixel coordinates suitable for Cytoscape.js and Desktop.
3. Additional layout attributes (e.g., width, height) may be stored as node attributes if declared in attributeDeclarations.nodes.
4. If you ingest an old CX file, your conversion tooling should:
   -  parse its cartesianLayout entries,
   -  bwrite their coordinates onto nodes,
   -  then drop the obsolete aspect.

⚠️ IMPORTANT: Do not use both coordinate storage methods simultaneously. 
Use nodes aspect coordinates for single networks OR cartesianLayout 
aspect for Collections - never both.

# edges

{% highlight json %}
{
  "id": long,
  "s" : long,
  "t" : long, 
  "v" : object
}	
{% endhighlight %}

- **"id"** - is the unique id of the edge in the scope of this aspect. Required
- **"s"** - id of the source node. Required.
- **"t"** -  id of the target node. Required.
- **"v"** - holds all attributes on this edge as a JSON object. Like the "v" attribute in nodes, this object doesn't allow the attribute "id" in it. It also treats "s" and "t" as normal attributes in it.

# visualProperties

This aspect defines the default visuals styles and data-driven mappings in this network. There is only one element in this aspect. This aspect should not be fragmented.

The graphic properties in this aspect only support Cytoscape Portable Graphic Styles. If a user wants to store non-portable visual styles such as native Cytoscape desktop application specific visual properties, they can be stored in an opaque aspect such as cyVisualProperties. It is recommended to put all portable visual properties and styles in the visualProperties aspect so that they can be supported by a broad range of tools in the Cytoscape ecosystem. 

The data type of visual attribute values is predefined either by Cytoscape or the portable visual style standard.

Elements in this aspect has this structure:
```
{
  "default": {
	"network": {
        attribute_1: value,
        attribute_2: value,
           ...
      },
     "node": {
        attribute_1: value,
        attribute_2: value,
          ...
    },
    "edge": {
        attribute_1: value,
        attribute_2: value,
         ... 
    }
   },
  "nodeMapping":{
      Attribute_1: {
        "type":  "DISCRETE" | "PASSTHROUGH" | "CONTINUOUS",  
        "definition": <mapping serialized to JSON>},
      Attribute_2: {
        "type": value
        "definition": <<mapping serialized to JSON>},
      ...        
  }
  "edgeMapping":{
     ...
  }
}
```
Each type of mapping has a specific schema.
The schema for the PASSTHROUGH mapping is:
{% highlight json %}
  {
    "attribute": string,
    "type": string 
  } 
{% endhighlight %}
   
- **"attribute"** - attribute name. This is the original attribute name as is used in node or edge attributes, or the expanded name defined in the attributeDefinitions aspect. ***NOTE:*** A shortened attribute name (alias) cannot be used here. 
- **"type"** - data type of that attribute. The data type of this attribute is required only when it is not defined in the attributeDeclarations aspect. In cases where the attribute is declared in the attribute declaration, its data type is inferred from the attribute name.
<!-- - **"selector"** - the expression tree of the selector. Cytoscape doesn't support this now, if Cytoscape can implement this, we can add this to portable style which will increase the compatibility between CytoscapeJS and Cytoscape data model.
-->

Schema for DISCRETE mapping is
```
{
  "attribute": string,
  "type": string,
  "map": [{ "v" : data_value, 
            "vp": visual_property_value }]  
}	
```		
The data types for 'data_value' and 'visual_property_value' can either be specified by the 'type' attribute or 
inferred from the names of the corresponding attributes and visual properties

Schema for CONTINUOUS mapping is

```
{
  "attribute": string,
  "type": string,
  "map": [{
             "min":       double,
             "includeMin": true | false,
             "max":       double,
             "includeMax": true | false,
             "minVPValue": visual_property_value,
             "maxVPValue": visual_property_value
          }]  
}	
 ```

# nodeBypasses           

This aspect stores all the bypass visual properties on nodes. Its element has this data structure

{% highlight json %}

{
   "id": long,
   "v":  Object
}
{% endhighlight %}

- **"id"** - is the node id.
- **"v"** - is an object that stores visual properties. The values are simple objects with name-value pairs in it. The attribute name is the visual property name. This is an example element in this aspect: 

{% highlight json %}

{ 
  "id": 245,
  "v" : {
        "NODE_BACKGROUND_COLOR": "#89D0F5"
        "NODE_SHAPE":   "round-rectangle"
     }
}
{% endhighlight %}

# edgeBypasses

Similar to the nodeBypasses aspect, this aspect stores all the bypass visual properties on edges. The data structure of its element is

{% highlight json %}
{
   "id": long,
   "v": Object
}
{% endhighlight %}

- **"id"** - is the edge id.
- **"v"** - is an object that stores visual properties. The values are simple objects with name-value pairs in it. The attribute name is the visual property name. This is an example element in this aspect: 

{% highlight json %}
{ 
  "id": 245,
  "v": {
      "EDGE_BEND": [
           0.8534737565017845, 
           0.5211358239103628, 
           0.6999978351246443
         ],
      "EDGE_WIDTH": 5
    }
}
{% endhighlight %}


# visualEditorProperties

This aspect determines the default visual styles for displaying the network in applications with editing capabilities.
Currently, it's utilized in both the Cytoscape desktop and web applications. This aspect should contain only one element. 
This singular element is an object endowed with the following attributes:

- **arrowColorMatchesEdge** - If true then Color (Unselected) is used for the whole edge, including its line and arrows.  
- **nodeSizeLocked**  - Lock node width and height when this value is set to true.  
- **NETWORK_CENTER_X_LOCATION** - The X location of network view center when opened. When opening a CX network in Cytoscape,
 Cytoscape will establish the the zoom level and the center of the network when **NETWORK_CENTER_X_LOCATION**, **NETWORK_CENTER_Y_LOCATION** and **NETWORK_SCALE_FACTOR** are all defined. Otherwise, when the nework is opened, Cytoscape will automatically fit the network within the viewport.
- **NETWORK_CENTER_Y_LOCATION** - The Y location of network view center when it opened.
- **NETWORK_SCALE_FACTOR** - The zoom level of the network view when opened.
- **tableDisplayConfiguration** - Stores user interface settings for displaying node and edge attribute tables in Cytoscape. This configuration preserves column visibility, ordering, sizing, and sorting preferences.

    **Schema**

    The `tableDisplayConfiguration` attribute contains an object with up to three table type configurations:

    ```json
    {
      "tableDisplayConfiguration": {
      "nodeTable": <table_config>,
      "edgeTable": <table_config>, 
     }
    }
    ```

    Each `<table_config>` object has the following structure:

    - **`columnConfiguration`** (required): An array of column configuration objects. The order of objects in this array determines the left-to-right display order of columns in the table.
    - **`sortColumn`** (optional): The attribute name of the currently sorted column. If null or omitted, no sorting is applied.
    - **`sortDirection`** (optional): The sort direction, either "ascending" or "descending". Only relevant when `sortColumn` is specified. Defaults to "ascending".
    

    **Column Configuration Object**

    Each object in the `columnConfiguration` array has these attributes:

    - **`attributeName`** (required): The name of the attribute. Must match an attribute name declared in the corresponding aspect (`nodes` or `edges`). It can not be an alias.
    - **`visible`** (required): Boolean indicating whether this column should be displayed in the table.
    - **`columnWidth`** (optional): The width of the column in pixels. If omitted, applications should use default column sizing.

    **Behavior Specifications**

    - **Column Ordering**: The position of column configuration objects in the `columnConfiguration` array determines the display order from left to right.
    - **Hidden Columns**: Columns with `visible: false` are not displayed but retain their position in the array to preserve intended ordering when made visible.
    - **Single Column Sorting**: Only one column per table can be sorted at a time. The sort state is maintained at the table level through `sortColumn` and `sortDirection` attributes.
    - **Missing Configuration**: If `tableDisplayConfiguration` is omitted or a specific table configuration is missing, applications should use their default table display behavior.
    - **Unknown Attributes**: If a column configuration references an attribute name that doesn't exist in the network, applications should ignore that column configuration.

    **Example**

    ```json
    {
      "visualEditorProperties": [
        {
          "arrowColorMatchesEdge": true,
          "nodeSizeLocked": false,
          "NETWORK_CENTER_X_LOCATION": 100.0,
          "NETWORK_CENTER_Y_LOCATION": 50.0,
          "NETWORK_SCALE_FACTOR": 1.2,
          "tableDisplayConfiguration": {
            "nodeTable": {
              "columnConfiguration": [
                {
                  "attributeName": "name",
                  "visible": true,
                  "columnWidth": 150
                },
                {
                  "attributeName": "degree",
                  "visible": true,
                  "columnWidth": 80
                },
                {
                  "attributeName": "betweenness_centrality",
                  "visible": false,
                  "columnWidth": 120
                },
                {
                  "attributeName": "gene_symbol",
                  "visible": true,
                  "columnWidth": 100
                }
              ],
              "sortColumn": "degree",
              "sortDirection": "descending"
            },
            "edgeTable": {
              "columnConfiguration": [
                {
                  "attributeName": "interaction",
                  "visible": true,
                  "columnWidth": 120
                },
                {
                  "attributeName": "weight",
                  "visible": true,
                  "columnWidth": 80
                },
                {
                  "attributeName": "confidence",
                  "visible": false,
                  "columnWidth": 90
                }
              ],
              "sortColumn": "weight",
              "sortDirection": "ascending"
            }
          }  
        }
      ]
    }
    ```

    In this example:
    - **Node table**: Displays columns in order: name, degree, gene_symbol (betweenness_centrality is hidden but maintains its position). Table is sorted by degree in descending order.
    - **Edge table**: Displays interaction and weight columns (confidence is hidden). Table is sorted by weight in ascending order.
    - **Network table**: Displays name and description columns (version is hidden). No sorting is applied.

     **Compatibility Notes**

    - This enhancement is backwards compatible. Applications that don't support table display configuration will simply ignore this attribute.
    - Applications should validate that `sortColumn` values reference existing attributes in the `columnConfiguration` array.
    - The `tableDisplayConfiguration` attribute is optional and can be omitted entirely or selectively (e.g., only including `nodeTable` configuration).

## Opaque aspects
Applications are allowed to add any non-core aspects to CX as extensions. These aspects are called 'opaque aspects'. The element of an opaque aspect is a JSON object, and all elements in an opaque aspect should have the same data structure. Attributes in opaque aspect elements can also be declared in the attributeDeclarations aspect.          

An opaque aspect is in this format in a CX document:

```
{
   Aspect_name: [
    Element1, 
    Element2,
	...
   ]
}
```

## Numeric values in this version of CX

CX supports these 3 numeric data types:

 - long
 - integer
 - double

A CX network is a JSON document that follows the JSON specification. However, the JSON specification only includes a generic number type, which can lead to imprecision in numeric data types for node and edge attribute values. To ensure precision, it is necessary to use the attribute declaration aspect to cast the CX2 numeric values to their declared data type. Note that the double data type has special values like `NaN`, `-Infinity`, and `Infinity`, which are not supported in the JSON specification. Consequently, when converting a CX network into CX2, NDEx server will convert these values to nulls. If your application requires the preservation of NaNs, you can save the corresponding column as a string and handle the string-to-double conversion in your application.

## Example CX files

[Example 1](https://github.com/cytoscape/cx/blob/CYTOSCAPE-13006/examples/DLoc%20Hierarchy%20demo.cx2)
 