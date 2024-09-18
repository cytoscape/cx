---
layout: post
title: CX2 Visual Styles
date: 2024-09-18
collection: excluded_posts
categories: cx2
permalink: /:categories/:title/
published: true
---

# Introduction

This document provides a comprehensive overview of **CX2 Visual Styles**.

**Definitions:**

- **CX2 Visual Style**: A standardized set of visual properties defined in the CX 2.0 format, ensuring consistent visualization across different platforms and clients within the Cytoscape ecosystem.

- **Cytoscape DT**: Cytoscape Desktop application.

- **Cytoscape.js**: A JavaScript library for graph theory network visualization and analysis.

- **JSON Schema**: A declarative format for defining the structure and validation rules of JSON data, specifying constraints such as data types, required fields, and value formats. See [json-schema.org](https://json-schema.org/).

# Table of Contents

1. [Introduction](#introduction)
2. [Style Properties](#style-properties)
   - [2.1. Network Properties](#21-network-properties)
     - [2.1.1. Network Background Color](#211-network-background-color)
   - [2.2. Node Properties](#22-node-properties)
     - [2.2.1. Node Shape](#221-node-shape)
     - [2.2.2. Node Background Color](#222-node-background-color)
     - [2.2.3. Node Width and Height](#223-node-width-and-height)
     - [2.2.4. Node Background Opacity](#224-node-background-opacity)
     - [2.2.5. Node Visibility](#225-node-visibility)
     - [2.2.6. Node Border Properties](#226-node-border-properties)
       - [2.2.6.1. Node Border Color](#2261-node-border-color)
       - [2.2.6.2. Node Border Style](#2262-node-border-style)
       - [2.2.6.3. Node Border Width](#2263-node-border-width)
       - [2.2.6.4. Node Border Opacity](#2264-node-border-opacity)
     - [2.2.7. Node Label Properties](#227-node-label-properties)
       - [2.2.7.1. Node Label Text](#2271-node-label-text)
       - [2.2.7.2. Node Label Background Color](#2272-node-label-background-color)
       - [2.2.7.3. Node Label Background Shape](#2273-node-label-background-shape)
       - [2.2.7.4. Node Label Background Opacity](#2274-node-label-background-opacity)
       - [2.2.7.5. Node Label Color](#2275-node-label-color)
       - [2.2.7.6. Node Label Font Face](#2276-node-label-font-face)
       - [2.2.7.7. Node Label Font Size](#2277-node-label-font-size)
       - [2.2.7.8. Node Label Position](#2278-node-label-position)
       - [2.2.7.9. Node Label Rotation](#2279-node-label-rotation)
       - [2.2.7.10. Node Label Opacity](#22710-node-label-opacity)
       - [2.2.7.11. Node Label Max Width](#22711-node-label-max-width)
     - [2.2.8. Node Image](#228-node-image)
     - [2.2.9. Node Pie Chart](#229-node-pie-chart)
     - [2.2.10. Node Selected Properties](#2210-node-selected-properties)
       - [2.2.10.1. Node Selected](#22101-node-selected)
       - [2.2.10.2. Node Selected Paint](#22102-node-selected-paint)
     - [2.2.11. Node Position Properties](#2211-node-position-properties)
       - [2.2.11.1. Node X Location](#22111-node-x-location)
       - [2.2.11.2. Node Y Location](#22112-node-y-location)
       - [2.2.11.3. Node Z Location](#22113-node-z-location)
     - [2.2.12. Compound Node Properties](#2212-compound-node-properties)
       - [2.2.12.1. Compound Node Shape](#22121-compound-node-shape)
       - [2.2.12.2. Compound Node Padding](#22122-compound-node-padding)
   - [2.3. Edge Properties](#23-edge-properties)
     - [2.3.1. Edge Width](#231-edge-width)
     - [2.3.2. Edge Line Color and Opacity](#232-edge-line-color-and-opacity)
       - [2.3.2.1. Edge Line Color](#2321-edge-line-color)
       - [2.3.2.2. Edge Opacity](#2322-edge-opacity)
     - [2.3.3. Edge Curve](#233-edge-curve)
       - [2.3.3.1 Edge Curved](#2331-edge-curved)
       - [2.3.3.2 Edge Curve Style](#2332-edge-curve-style)
     - [2.3.4. Edge Bezier and Segment Points](#234-edge-bezier-and-segment-points)
       - [2.3.4.1. Edge Bezier Point Distances](#2341-edge-bezier-point-distances)
       - [2.3.4.2. Edge Bezier Point Weights](#2342-edge-bezier-point-weights)
       - [2.3.4.3. Edge Segment Distances](#2343-edge-segment-distances)
       - [2.3.4.4. Edge Segment Weights](#2344-edge-segment-weights)
     - [2.3.5. Edge Line Style](#235-edge-line-style)
     - [2.3.6. Edge Arrow Properties](#236-edge-arrow-properties)
       - [2.3.6.1. Edge Source Arrow Shape](#2361-edge-source-arrow-shape)
       - [2.3.6.2. Edge Source Arrow Color](#2362-edge-source-arrow-color)
       - [2.3.6.3. Edge Source Arrow Size](#2363-edge-source-arrow-size)
       - [2.3.6.4. Edge Source Arrow Selected Paint](#2364-edge-source-arrow-selected-paint)
       - [2.3.6.5. Edge Target Arrow Shape](#2365-edge-target-arrow-shape)
       - [2.3.6.6. Edge Target Arrow Color](#2366-edge-target-arrow-color)
       - [2.3.6.7. Edge Target Arrow Size](#2367-edge-target-arrow-size)
       - [2.3.6.8. Edge Target Arrow Selected Paint](#2368-edge-target-arrow-selected-paint)
     - [2.3.7. Edge Visibility](#237-edge-visibility)
     - [2.3.8. Edge Label Properties](#238-edge-label-properties)
       - [2.3.8.1. Edge Label Text](#2381-edge-label-text)
       - [2.3.8.2. Edge Label Autorotate](#2382-edge-label-autorotate)
       - [2.3.8.3. Edge Label Background Color](#2383-edge-label-background-color)
       - [2.3.8.4. Edge Label Background Shape](#2384-edge-label-background-shape)
       - [2.3.8.5. Edge Label Background Opacity](#2385-edge-label-background-opacity)
       - [2.3.8.6. Edge Label Color](#2386-edge-label-color)
       - [2.3.8.7. Edge Label Font Face](#2387-edge-label-font-face)
       - [2.3.8.8. Edge Label Font Size](#2388-edge-label-font-size)
       - [2.3.8.9. Edge Label Position](#2389-edge-label-position)
       - [2.3.8.10. Edge Label Rotation](#23810-edge-label-rotation)
       - [2.3.8.11. Edge Label Opacity](#23811-edge-label-opacity)
       - [2.3.8.12. Edge Label Max Width](#23812-edge-label-max-width)
     - [2.3.9. Edge Selected Properties](#239-edge-selected-properties)
       - [2.3.9.1. Edge Selected](#2391-edge-selected)
       - [2.3.9.2. Edge Selected Paint](#2392-edge-selected-paint)
       - [2.3.9.3. Edge Stroke Selected Paint](#2393-edge-stroke-selected-paint)
     - [2.3.10. Edge Stacking Properties](#2310-edge-stacking-properties)
       - [2.3.10.1. Edge Stacking](#23101-edge-stacking)
       - [2.3.10.2. Edge Stacking Density](#23102-edge-stacking-density)
     - [2.3.11. Edge Z-Order](#2311-edge-z-order)
   - [2.4. Setting Visual Properties](#24-setting-visual-properties)
     - [2.4.1. Default Value](#241-default-value)
     - [2.4.2. Mapping](#242-mapping)
     - [2.4.3. Bypass](#243-bypass)
   - [2.5. Editor Properties](#25-editor-properties)

---

# Style Properties

This section provides detailed descriptions of CX2 Visual Style properties, including their JSON-schema representations and mappings to Cytoscape DT.

## 2.1. Network Properties

### 2.1.1. Network Background Color

- **CX2 Visual Property Name**: `NETWORK_BACKGROUND_COLOR`
- **Description**: Sets the background color of the network canvas.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "NETWORK_BACKGROUND_COLOR": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `NETWORK_BACKGROUND_PAINT`
- **Example**:

  ```json
  {
    "network": {
      "NETWORK_BACKGROUND_COLOR": "#FFFFFF"
    }
  }
  ```

## 2.2. Node Properties

### 2.2.1. Node Shape

- **CX2 Visual Property Name**: `NODE_SHAPE`
- **Description**: Determines the shape of nodes.
- **Data Type**: `string`
- **Enum**:

  - `"ellipse"`
  - `"triangle"`
  - `"rectangle"`
  - `"round-rectangle"`
  - `"parallelogram"`
  - `"diamond"`
  - `"hexagon"`
  - `"octagon"`
  - `"vee"`

- **JSON Schema**:

  ```json
  {
    "NODE_SHAPE": {
      "type": "string",
      "enum": ["ellipse", "triangle", "rectangle", "round-rectangle", "parallelogram", "diamond", "hexagon", "octagon", "vee"]
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_SHAPE`
- **Example**:

  ```json
  {
    "node": {
      "NODE_SHAPE": "round-rectangle"
    }
  }
  ```

### 2.2.2. Node Background Color

- **CX2 Visual Property Name**: `NODE_BACKGROUND_COLOR`
- **Description**: Sets the fill color of nodes.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "NODE_BACKGROUND_COLOR": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_FILL_COLOR`
- **Example**:

  ```json
  {
    "node": {
      "NODE_BACKGROUND_COLOR": "#89D0F5"
    }
  }
  ```

### 2.2.3. Node Width and Height

- **CX2 Visual Property Names**:
  - `NODE_WIDTH`
  - `NODE_HEIGHT`
- **Description**: Define the dimensions of nodes. `NODE_WIDTH` defines the width of the node, and `NODE_HEIGHT` defines the height of the node.
- **Data Type**: `number`
- **Constraints**:
  - **Exclusive Minimum**: `0`
- **JSON Schema**:

  ```json
  {
    "NODE_WIDTH": {
      "type": "number",
      "exclusiveMinimum": 0
    },
    "NODE_HEIGHT": {
      "type": "number",
      "exclusiveMinimum": 0
    }
  }
  ```

- **Cytoscape DT Names**:
  - `NODE_WIDTH`
  - `NODE_HEIGHT`
- **Comment**: Node height and width are generally independent from each other. However, in Cytoscape DT, node width and height can be locked so that changing one automatically changes the other.
- **Example**:

  ```json
  {
    "node": {
      "NODE_WIDTH": 75.0,
      "NODE_HEIGHT": 35.0
    }
  }
  ```

### 2.2.4. Node Background Opacity

- **CX2 Visual Property Name**: `NODE_BACKGROUND_OPACITY`
- **Description**: Sets the opacity of node backgrounds.
- **Data Type**: `number`
- **Constraints**:
  - **Minimum**: `0`
  - **Maximum**: `1`
- **JSON Schema**:

  ```json
  {
    "NODE_BACKGROUND_OPACITY": {
      "type": "number",
      "minimum": 0,
      "maximum": 1
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_TRANSPARENCY` (Note: In Cytoscape DT, the value of `NODE_TRANSPARENCY` is an integer from 0 to 255.)
- **Example**:

  ```json
  {
    "node": {
      "NODE_BACKGROUND_OPACITY": 1.0
    }
  }
  ```

### 2.2.5. Node Visibility

- **CX2 Visual Property Name**: `NODE_VISIBILITY`
- **Description**: Controls node rendering and space occupancy in layouts.
- **Data Type**: `string`
- **Enum**:
  - `"none"`
  - `"element"`
- **JSON Schema**:

  ```json
  {
    "NODE_VISIBILITY": {
      "type": "string",
      "enum": ["none", "element"]
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_VISIBLE` (In Cytoscape DT, the value is `true` or `false`.)
- **Example**:

  ```json
  {
    "node": {
      "NODE_VISIBILITY": "element"
    }
  }
  ```

### 2.2.6. Node Border Properties

#### 2.2.6.1. Node Border Color

- **CX2 Visual Property Name**: `NODE_BORDER_COLOR`
- **Description**: Sets the color of node borders.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "NODE_BORDER_COLOR": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_BORDER_PAINT`
- **Example**:

  ```json
  {
    "node": {
      "NODE_BORDER_COLOR": "#005A32"
    }
  }
  ```

#### 2.2.6.2. Node Border Style

- **CX2 Visual Property Name**: `NODE_BORDER_STYLE`
- **Description**: Defines the style of node borders.
- **Data Type**: `string`
- **Enum**:
  - `"solid"`
  - `"dashed"` (`"long_dash"` in Cytoscape DT)
  - `"dotted"` (`"dot"` in Cytoscape DT)
  - `"double"`
- **JSON Schema**:

  ```json
  {
    "NODE_BORDER_STYLE": {
      "type": "string",
      "enum": ["solid", "dashed", "dotted", "double"]
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_BORDER_LINE_TYPE`
- **Note**: The following values are only supported in Cytoscape DT and are not supported in Cytoscape Web:
  - `"marquee_dash"`
  - `"backward_slash"`
  - `"vertical_slash"`
  - `"marquee_dash_dot"`
  - `"contiguous_arrow"`
  - `"zigzag"`
  - `"marquee_equal"`
  - `"dash_dot"`
  - `"separate_arrow"`
  - `"equal_dash"`
  - `"sinewave"`
  - `"forward_slash"`
- **Example**:

  ```json
  {
    "node": {
      "NODE_BORDER_STYLE": "solid"
    }
  }
  ```

#### 2.2.6.3. Node Border Width

- **CX2 Visual Property Name**: `NODE_BORDER_WIDTH`
- **Description**: Sets the width of node borders.
- **Data Type**: `number`
- **Constraints**:
  - **Exclusive Minimum**: `0`
- **JSON Schema**:

  ```json
  {
    "NODE_BORDER_WIDTH": {
      "type": "number",
      "exclusiveMinimum": 0
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_BORDER_WIDTH`
- **Example**:

  ```json
  {
    "node": {
      "NODE_BORDER_WIDTH": 2.0
    }
  }
  ```

#### 2.2.6.4. Node Border Opacity

- **CX2 Visual Property Name**: `NODE_BORDER_OPACITY`
- **Description**: Sets the opacity of node borders.
- **Data Type**: `number`
- **Constraints**:
  - **Minimum**: `0`
  - **Maximum**: `1`
- **JSON Schema**:

  ```json
  {
    "NODE_BORDER_OPACITY": {
      "type": "number",
      "minimum": 0,
      "maximum": 1
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_BORDER_TRANSPARENCY` (In Cytoscape DT, the value is from 0 to 255.)
- **Example**:

  ```json
  {
    "node": {
      "NODE_BORDER_OPACITY": 1.0
    }
  }
  ```

### 2.2.7. Node Label Properties

#### 2.2.7.1. Node Label Text

- **CX2 Visual Property Name**: `NODE_LABEL`
- **Description**: The text content of node labels.
- **Data Type**: `string`
- **JSON Schema**:

  ```json
  {
    "NODE_LABEL": {
      "type": "string"
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_LABEL`
- **Example**:

  ```json
  {
    "nodeMapping": {
      "NODE_LABEL": {
        "type": "PASSTHROUGH",
        "definition": {
          "attribute": "name",
          "type": "string"
        }
      }
    }
  }
  ```

#### 2.2.7.2. Node Label Background Color

- **CX2 Visual Property Name**: `NODE_LABEL_BACKGROUND_COLOR`
- **Description**: Defines the background color for node labels.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "NODE_LABEL_BACKGROUND_COLOR": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_LABEL_BACKGROUND_COLOR`
- **Example**:

  ```json
  {
    "node": {
      "NODE_LABEL_BACKGROUND_COLOR": "#B6B6B6"
    }
  }
  ```

#### 2.2.7.3. Node Label Background Shape

- **CX2 Visual Property Name**: `NODE_LABEL_BACKGROUND_SHAPE`
- **Description**: Specifies the shape of the label's background.
- **Data Type**: `string`
- **Enum**:
  - `"none"`
  - `"rectangle"`
  - `"round-rectangle"`
- **JSON Schema**:

  ```json
  {
    "NODE_LABEL_BACKGROUND_SHAPE": {
      "type": "string",
      "enum": ["none", "rectangle", "round-rectangle"]
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_LABEL_BACKGROUND_SHAPE`
- **Example**:

  ```json
  {
    "node": {
      "NODE_LABEL_BACKGROUND_SHAPE": "none"
    }
  }
  ```

#### 2.2.7.4. Node Label Background Opacity

- **CX2 Visual Property Name**: `NODE_LABEL_BACKGROUND_OPACITY`
- **Description**: Specifies the opacity of the label's background.
- **Data Type**: `number`
- **Constraints**:
  - **Minimum**: `0`
  - **Maximum**: `1`
- **JSON Schema**:

  ```json
  {
    "NODE_LABEL_BACKGROUND_OPACITY": {
      "type": "number",
      "minimum": 0,
      "maximum": 1
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_LABEL_BACKGROUND_TRANSPARENCY` (In Cytoscape DT, the value is from 0 to 255.)
- **Example**:

  ```json
  {
    "node": {
      "NODE_LABEL_BACKGROUND_OPACITY": 1.0
    }
  }
  ```

#### 2.2.7.5. Node Label Color

- **CX2 Visual Property Name**: `NODE_LABEL_COLOR`
- **Description**: Sets the color of node labels.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "NODE_LABEL_COLOR": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_LABEL_COLOR`
- **Example**:

  ```json
  {
    "node": {
      "NODE_LABEL_COLOR": "#333333"
    }
  }
  ```

#### 2.2.7.6. Node Label Font Face

- **CX2 Visual Property Name**: `NODE_LABEL_FONT_FACE`
- **Description**: Specifies the font family, style, and weight for node labels.
- **Data Type**: `object`
- **Properties**:
  - `FONT_FAMILY`: `string` (Enum: `"serif"`, `"sans-serif"`, `"monospace"`)
  - `FONT_STYLE`: `string` (Enum: `"normal"`, `"italic"`)
  - `FONT_WEIGHT`: `string` (Enum: `"normal"`, `"bold"`)
- **JSON Schema**:

  ```json
  {
    "NODE_LABEL_FONT_FACE": {
      "type": "object",
      "properties": {
        "FONT_FAMILY": {
          "type": "string",
          "enum": ["serif", "sans-serif", "monospace"]
        },
        "FONT_STYLE": {
          "type": "string",
          "enum": ["normal", "italic"]
        },
        "FONT_WEIGHT": {
          "type": "string",
          "enum": ["normal", "bold"]
        }
      }
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_LABEL_FONT_FACE` (In Cytoscape DT, some fonts may not always be available, as Java relies on what the OS has installed. Cytoscape DT supports only the style combinations available in Java Fonts: PLAIN, BOLD, ITALIC, or BOLD+ITALIC.)
- **Example**:

  ```json
  {
    "node": {
      "NODE_LABEL_FONT_FACE": {
        "FONT_FAMILY": "sans-serif",
        "FONT_STYLE": "normal",
        "FONT_WEIGHT": "normal"
      }
    }
  }
  ```

#### 2.2.7.7. Node Label Font Size

- **CX2 Visual Property Name**: `NODE_LABEL_FONT_SIZE`
- **Description**: Defines the font size (in pixels) for node labels.
- **Data Type**: `integer`
- **Constraints**:
  - **Exclusive Minimum**: `0`
- **JSON Schema**:

  ```json
  {
    "NODE_LABEL_FONT_SIZE": {
      "type": "integer",
      "exclusiveMinimum": 0
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_LABEL_FONT_SIZE`
- **Example**:

  ```json
  {
    "node": {
      "NODE_LABEL_FONT_SIZE": 12
    }
  }
  ```

#### 2.2.7.8. Node Label Position

- **CX2 Visual Property Name**: `NODE_LABEL_POSITION`
- **Description**: Determines the placement of node labels.
- **Data Type**: `object`
- **Properties**:
  - `HORIZONTAL_ALIGN`: `string` (Enum: `"left"`, `"center"`, `"right"`)
  - `VERTICAL_ALIGN`: `string` (Enum: `"top"`, `"center"`, `"bottom"`)
  - `HORIZONTAL_ANCHOR`: `string` (Enum: `"left"`, `"center"`, `"right"`)
  - `VERTICAL_ANCHOR`: `string` (Enum: `"top"`, `"center"`, `"bottom"`)
  - `MARGIN_X`: `number`
  - `MARGIN_Y`: `number`
  - `JUSTIFICATION`: `string` (Enum: `"left"`, `"center"`, `"right"`)
- **JSON Schema**:

  ```json
  {
    "NODE_LABEL_POSITION": {
      "type": "object",
      "properties": {
        "HORIZONTAL_ALIGN": {
          "type": "string",
          "enum": ["left", "center", "right"]
        },
        "VERTICAL_ALIGN": {
          "type": "string",
          "enum": ["top", "center", "bottom"]
        },
        "HORIZONTAL_ANCHOR": {
          "type": "string",
          "enum": ["left", "center", "right"]
        },
        "VERTICAL_ANCHOR": {
          "type": "string",
          "enum": ["top", "center", "bottom"]
        },
        "MARGIN_X": {
          "type": "number"
        },
        "MARGIN_Y": {
          "type": "number"
        },
        "JUSTIFICATION": {
          "type": "string",
          "enum": ["left", "center", "right"]
        }
      }
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_LABEL_POSITION`
- **Example**:

  ```json
  {
    "node": {
      "NODE_LABEL_POSITION": {
        "HORIZONTAL_ALIGN": "center",
        "VERTICAL_ALIGN": "center",
        "HORIZONTAL_ANCHOR": "center",
        "VERTICAL_ANCHOR": "center",
        "MARGIN_X": 0.0,
        "MARGIN_Y": 0.0,
        "JUSTIFICATION": "center"
      }
    }
  }
  ```

#### 2.2.7.9. Node Label Rotation

- **CX2 Visual Property Name**: `NODE_LABEL_ROTATION`
- **Description**: Specifies the rotation angle of node labels in degrees.
- **Data Type**: `number`
- **Constraints**:
  - **Minimum**: `-360`
  - **Maximum**: `360`
- **JSON Schema**:

  ```json
  {
    "NODE_LABEL_ROTATION": {
      "type": "number",
      "minimum": -360,
      "maximum": 360
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_LABEL_ROTATION`
- **Example**:

  ```json
  {
    "node": {
      "NODE_LABEL_ROTATION": 0.0
    }
  }
  ```

#### 2.2.7.10. Node Label Opacity

- **CX2 Visual Property Name**: `NODE_LABEL_OPACITY`
- **Description**: Specifies the opacity of node labels.
- **Data Type**: `number`
- **Constraints**:
  - **Minimum**: `0`
  - **Maximum**: `1`
- **JSON Schema**:

  ```json
  {
    "NODE_LABEL_OPACITY": {
      "type": "number",
      "minimum": 0,
      "maximum": 1
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_LABEL_TRANSPARENCY` (In Cytoscape DT, the value is from 0 to 255.)
- **Example**:

  ```json
  {
    "node": {
      "NODE_LABEL_OPACITY": 1.0
    }
  }
  ```

#### 2.2.7.11. Node Label Max Width

- **CX2 Visual Property Name**: `NODE_LABEL_MAX_WIDTH`
- **Description**: Defines the maximum width of node labels before text wrapping occurs.
- **Data Type**: `number`
- **Constraints**:
  - **Exclusive Minimum**: `0`
- **JSON Schema**:

  ```json
  {
    "NODE_LABEL_MAX_WIDTH": {
      "type": "number",
      "exclusiveMinimum": 0
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_LABEL_WIDTH`
- **Example**:

  ```json
  {
    "node": {
      "NODE_LABEL_MAX_WIDTH": 200.0
    }
  }
  ```

### 2.2.8. Node Image

- **CX2 Visual Property Name**: `NODE_IMAGE_<i>` (NODE_CUSTOMGRAPHICS_?)
- **Description**: Specifies images associated with nodes and their placement.
- **Data Type**: `string`
- **Format**: `dataURI`
- **Additional Properties**:

  - **Image Size** (`NODE_IMAGE_<i>_SIZE`):
    - **Data Type**: `object`
    - **Properties**:
      - `WIDTH`: `number`
      - `HEIGHT`: `number`
  - **Image Position** (`NODE_IMAGE_<i>_POSITION`):
    - **Data Type**: `object`
    - **Properties**:
      - `HORIZONTAL_ALIGN`: `string` (Enum: `"left"`, `"center"`, `"right"`)
      - `VERTICAL_ALIGN`: `string` (Enum: `"top"`, `"center"`, `"bottom"`)
      - `HORIZONTAL_ANCHOR`: `string` (Enum: `"left"`, `"center"`, `"right"`)
      - `VERTICAL_ANCHOR`: `string` (Enum: `"top"`, `"center"`, `"bottom"`)
      - `MARGIN_X`: `number`
      - `MARGIN_Y`: `number`

- **JSON Schema**:

  ```json
  {
    "NODE_IMAGE_<i>": {
      "type": "string",
      "format": "dataURI"
    },
    "NODE_IMAGE_<i>_SIZE": {
      "type": "object",
      "properties": {
        "WIDTH": {
          "type": "number"
        },
        "HEIGHT": {
          "type": "number"
        }
      }
    },
    "NODE_IMAGE_<i>_POSITION": {
      "type": "object",
      "properties": {
        "HORIZONTAL_ALIGN": {
          "type": "string",
          "enum": ["left", "center", "right"]
        },
        "VERTICAL_ALIGN": {
          "type": "string",
          "enum": ["top", "center", "bottom"]
        },
        "HORIZONTAL_ANCHOR": {
          "type": "string",
          "enum": ["left", "center", "right"]
        },
        "VERTICAL_ANCHOR": {
          "type": "string",
          "enum": ["top", "center", "bottom"]
        },
        "MARGIN_X": {
          "type": "number"
        },
        "MARGIN_Y": {
          "type": "number"
        }
      }
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_CUSTOMGRAPHICS_<i>`

**Notes:**

- `<i>` represents the index of the image when multiple images are used.
- In Cytoscape DT, node images are handled as custom graphics layers.
- **Example**:

  ```json
  {
    "node": {
      "NODE_CUSTOMGRAPHICS_SIZE_1": 50.0,
      "NODE_CUSTOMGRAPHICS_POSITION_1": {
        "HORIZONTAL_ALIGN": "center",
        "VERTICAL_ALIGN": "center",
        "HORIZONTAL_ANCHOR": "center",
        "VERTICAL_ANCHOR": "center",
        "MARGIN_X": 0.0,
        "MARGIN_Y": 0.0
      }
    }
  }
  ```

### 2.2.9. Node Pie Chart

- **CX2 Visual Property Name**: `NODE_PIE_CHART` (NODE_CUSTOMGRAPHICS_?)
- **Description**: Defines pie charts within nodes, including slice colors and sizes.
- **Data Type**: `object`
- **Properties**:
  - `PIE_SIZE`: `number` (Exclusive Minimum: `0`)
  - `SLICE_<i>_COLOR`: `string` (Format: `rgbColor`)
  - `SLICE_<i>_SIZE`: `number` (Minimum: `0`, Maximum: `1`)
- **JSON Schema**:

  ```json
  {
    "NODE_PIE_CHART": {
      "type": "object",
      "properties": {
        "PIE_SIZE": {
          "type": "number",
          "exclusiveMinimum": 0
        },
        "SLICE_<i>_COLOR": {
          "type": "string",
          "format": "rgbColor"
        },
        "SLICE_<i>_SIZE": {
          "type": "number",
          "minimum": 0,
          "maximum": 1
        }
      }
    }
  }
  ```

- **Cytoscape DT Name**: N/A (Pie charts are implemented via custom graphics in Cytoscape DT)
- **Notes:**
  - `<i>` represents the index of the pie slice.

### 2.2.10. Node Selected Properties

#### 2.2.10.1. Node Selected

- **CX2 Visual Property Name**: `NODE_SELECTED`
- **Description**: Indicates whether the node is selected.
- **Data Type**: `boolean`
- **JSON Schema**:

  ```json
  {
    "NODE_SELECTED": {
      "type": "boolean"
    }
  }
  ```

- **Example**:

  ```json
  {
    "node": {
      "NODE_SELECTED": false
    }
  }
  ```

#### 2.2.10.2. Node Selected Paint

- **CX2 Visual Property Name**: `NODE_SELECTED_PAINT`
- **Description**: Specifies the color of the node when selected.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "NODE_SELECTED_PAINT": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_SELECTED_PAINT`
- **Example**:

  ```json
  {
    "node": {
      "NODE_SELECTED_PAINT": "#FFFF00"
    }
  }
  ```

### 2.2.11. Node Position Properties

#### 2.2.11.1. Node X Location

- **CX2 Visual Property Name**: `NODE_X_LOCATION`
- **Description**: Specifies the X-coordinate of the node's position.
- **Data Type**: `number`
- **JSON Schema**:

  ```json
  {
    "NODE_X_LOCATION": {
      "type": "number"
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_X_LOCATION`
- **Example**:

  ```json
  {
    "node": {
      "NODE_X_LOCATION": 100.0
    }
  }
  ```

#### 2.2.11.2. Node Y Location

- **CX2 Visual Property Name**: `NODE_Y_LOCATION`
- **Description**: Specifies the Y-coordinate of the node's position.
- **Data Type**: `number`
- **JSON Schema**:

  ```json
  {
    "NODE_Y_LOCATION": {
      "type": "number"
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_Y_LOCATION`
- **Example**:

  ```json
  {
    "node": {
      "NODE_Y_LOCATION": 200.0
    }
  }
  ```

#### 2.2.11.3. Node Z Location

- **CX2 Visual Property Name**: `NODE_Z_LOCATION`
- **Description**: Specifies the Z-order of the node (stacking order).
- **Data Type**: `number`
- **JSON Schema**:

  ```json
  {
    "NODE_Z_LOCATION": {
      "type": "number"
    }
  }
  ```

- **Cytoscape DT Name**: `NODE_Z_ORDER`
- **Example**:

  ```json
  {
    "node": {
      "NODE_Z_LOCATION": 0.0
    }
  }
  ```

### 2.2.12. Compound Node Properties

#### 2.2.12.1. Compound Node Shape

- **CX2 Visual Property Name**: `COMPOUND_NODE_SHAPE`
- **Description**: Defines the shape of compound nodes.
- **Data Type**: `string`
- **Enum**:
  - `"ellipse"`
  - `"triangle"`
  - `"rectangle"`
  - `"round-rectangle"`
  - `"parallelogram"`
  - `"diamond"`
  - `"hexagon"`
  - `"octagon"`
  - `"vee"`
- **JSON Schema**:

  ```json
  {
    "COMPOUND_NODE_SHAPE": {
      "type": "string",
      "enum": ["rectangle", "round-rectangle"]
    }
  }
  ```

- **Cytoscape DT Name**: `COMPOUND_NODE_SHAPE`
- **Example**:

  ```json
  {
    "node": {
      "COMPOUND_NODE_SHAPE": "round-rectangle"
    }
  }
  ```

#### 2.2.12.2. Compound Node Padding

- **CX2 Visual Property Name**: `COMPOUND_NODE_PADDING`
- **Description**: Specifies the padding around compound nodes.
- **Data Type**: `number`
- **Constraints**:
  - **Minimum**: `0`
- **JSON Schema**:

  ```json
  {
    "COMPOUND_NODE_PADDING": {
      "type": "number",
      "minimum": 0
    }
  }
  ```

- **Cytoscape DT Name**: `COMPOUND_NODE_PADDING`
- **Example**:

  ```json
  {
    "node": {
      "COMPOUND_NODE_PADDING": 10.0
    }
  }
  ```

## 2.3. Edge Properties

### 2.3.1. Edge Width

- **CX2 Visual Property Name**: `EDGE_WIDTH`
- **Description**: Sets the thickness of edges.
- **Data Type**: `number`
- **Constraints**:
  - **Exclusive Minimum**: `0`
- **JSON Schema**:

  ```json
  {
    "EDGE_WIDTH": {
      "type": "number",
      "exclusiveMinimum": 0
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_WIDTH`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_WIDTH": 3.0
    }
  }
  ```

### 2.3.2. Edge Line Color and Opacity

#### 2.3.2.1. Edge Line Color

- **CX2 Visual Property Name**: `EDGE_LINE_COLOR`
- **Description**: Sets the color of edge lines.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "EDGE_LINE_COLOR": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_UNSELECTED_PAINT`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_LINE_COLOR": "#666666"
    }
  }
  ```

#### 2.3.2.2. Edge Opacity

- **CX2 Visual Property Name**: `EDGE_OPACITY`
- **Description**: Defines the transparency of edges.
- **Data Type**: `number`
- **Constraints**:
  - **Minimum**: `0`
  - **Maximum**: `1`
- **JSON Schema**:

  ```json
  {
    "EDGE_OPACITY": {
      "type": "number",
      "minimum": 0,
      "maximum": 1
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_TRANSPARENCY` (In Cytoscape DT, the value is from 0 to 255.)
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_OPACITY": 1.0
    }
  }
  ```

### 2.3.3 Edge Curve

### 2.3.3.1 Edge Curved

- **CX2 Visual Property Name**: `EDGE_CURVED`
- **Description**: Specifies whether the edge is rendered as a curved line.
- **Data Type**: `boolean`
- **JSON Schema**:

  ```json
  {
    "EDGE_CURVED": {
      "type": "boolean"
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_CURVED`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_CURVED": true
    }
  }
  ```

### 2.3.3.2 Edge Curve Style

- **CX2 Visual Property Name**: `EDGE_CURVE_STYLE`
- **Description**: Determines how edges curve or deviate from straight lines.
- **Data Type**: `string`
- **Enum**:
  - `"straight"`
  - `"segments"`
  - `"bezier"`
  - `"unbundled-bezier"`
- **JSON Schema**:

  ```json
  {
    "EDGE_CURVE_STYLE": {
      "type": "string",
      "enum": ["straight", "segments", "bezier", "unbundled-bezier"]
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_BEND`
- **Descriptions of Values**:
  - **`straight`**: The edge is rendered as a single straight line.
  - **`segments`**: The edge is rendered as a series of straight lines defined by `EDGE_SEGMENT_DISTANCES` and `EDGE_SEGMENT_WEIGHTS`.
  - **`bezier`**: The edge is rendered as a single bezier curve; curvature is calculated automatically.
  - **`unbundled-bezier`**: The edge is rendered as a bezier curve with control points specified.
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_CURVE_STYLE": "bezier"
    }
  }
  ```

### 2.3.4. Edge Bezier and Segment Points

#### 2.3.4.1. Edge Bezier Point Distances

- **CX2 Visual Property Name**: `EDGE_BEZIER_POINT_DISTANCES`
- **Description**: Distances that, with weights, define bezier curvature.
- **Data Type**: `array`
- **Items**: `number`
- **JSON Schema**:

  ```json
  {
    "EDGE_BEZIER_POINT_DISTANCES": {
      "type": "array",
      "items": {
        "type": "number"
      }
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_BEND`

#### 2.3.4.2. Edge Bezier Point Weights

- **CX2 Visual Property Name**: `EDGE_BEZIER_POINT_WEIGHTS`
- **Description**: Weights that, with distances, define bezier curvature.
- **Data Type**: `array`
- **Items**: `number`
- **JSON Schema**:

  ```json
  {
    "EDGE_BEZIER_POINT_WEIGHTS": {
      "type": "array",
      "items": {
        "type": "number"
      }
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_BEND`

#### 2.3.4.3. Edge Segment Distances

- **CX2 Visual Property Name**: `EDGE_SEGMENT_DISTANCES`
- **Description**: Distances that, with weights, define edge segments.
- **Data Type**: `array`
- **Items**: `number`
- **JSON Schema**:

  ```json
  {
    "EDGE_SEGMENT_DISTANCES": {
      "type": "array",
      "items": {
        "type": "number"
      }
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_BEND`

#### 2.3.4.4. Edge Segment Weights

- **CX2 Visual Property Name**: `EDGE_SEGMENT_WEIGHTS`
- **Description**: Weights that, with distances, define edge segments.
- **Data Type**: `array`
- **Items**: `number`
- **JSON Schema**:

  ```json
  {
    "EDGE_SEGMENT_WEIGHTS": {
      "type": "array",
      "items": {
        "type": "number"
      }
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_BEND`

### 2.3.5. Edge Line Style

- **CX2 Visual Property Name**: `EDGE_LINE_STYLE`
- **Description**: Specifies the pattern with which the edge line is painted.
- **Data Type**: `string`
- **Enum**:
  - `"solid"`
  - `"dotted"`
  - `"dashed"`
  - `"double"`
- **JSON Schema**:

  ```json
  {
    "EDGE_LINE_STYLE": {
      "type": "string",
      "enum": ["solid", "dotted", "dashed", "double"]
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_LINE_TYPE`
- **Note** Only selected in Cytoscape DT:
   - "marquee_dash",
   - "backward_slash",
   - "vertical_slash",
   - "marquee_dash_dot",
   - "contiguous_arrow",
   - "zigzag",
   - "marquee_equal",
   - "dash_dot",
   - "separate_arrow",
   - "equal_dash",
   - "sinewave",
   - "Forward_slash",
   - "parallel_lines"

- **Example**:

  ```json
  {
    "edge": {
      "EDGE_LINE_STYLE": "solid"
    }
  }
  ```

### 2.3.6. Edge Arrow Properties

#### 2.3.6.1. Edge Source Arrow Shape

- **CX2 Visual Property Name**: `EDGE_SOURCE_ARROW_SHAPE`
- **Description**: Defines the shape of the arrow at the edge's source.
- **Data Type**: `string`
- **Enum**:
  - `"none"`
  - `"triangle"` (In Cytoscape DT - `"delta"`)
  - `"triangle-cross"` (In Cytoscape DT - `"cross-delta"`)
  - `"square"`
  - `"diamond"`
  - `"circle"`
  - `"arrow"` (rendered as `"triangle"`)
  - `"open_circle"`
  - `"cross_open_delta"`
  - `"open_diamond"`
  - `"open_square"`
  - `"open_delta"`
  - `"tee"`
- **JSON Schema**:

  ```json
  {
    "EDGE_SOURCE_ARROW_SHAPE": {
      "type": "string",
      "enum": ["none", "triangle", "triangle-cross", "square", "diamond", "circle", "vee", "tee"]
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_SOURCE_ARROW_SHAPE`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_SOURCE_ARROW_SHAPE": "none"
    }
  }
  ```

#### 2.3.6.2. Edge Source Arrow Color

- **CX2 Visual Property Name**: `EDGE_SOURCE_ARROW_COLOR`
- **Description**: Sets the color of the arrow at the edge's source.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "EDGE_SOURCE_ARROW_COLOR": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_SOURCE_ARROW_UNSELECTED_PAINT`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_SOURCE_ARROW_COLOR": "#000000"
    }
  }
  ```

#### 2.3.6.3. Edge Source Arrow Size

- **CX2 Visual Property Name**: `EDGE_SOURCE_ARROW_SIZE`
- **Description**: Sets the size of the arrow at the edge's source.
- **Data Type**: `number`
- **Constraints**:
  - **Minimum**: `0`
- **JSON Schema**:

  ```json
  {
    "EDGE_SOURCE_ARROW_SIZE": {
      "type": "number",
      "minimum": 0
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_SOURCE_ARROW_SIZE`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_SOURCE_ARROW_SIZE": 6.0
    }
  }
  ```

#### 2.3.6.4. Edge Source Arrow Selected Paint

- **CX2 Visual Property Name**: `EDGE_SOURCE_ARROW_SELECTED_PAINT`
- **Description**: Specifies the color of the source arrow when the edge is selected.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "EDGE_SOURCE_ARROW_SELECTED_PAINT": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_SOURCE_ARROW_SELECTED_PAINT`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_SOURCE_ARROW_SELECTED_PAINT": "#FFFF00"
    }
  }
  ```

#### 2.3.6.5. Edge Target Arrow Shape

- **CX2 Visual Property Name**: `EDGE_TARGET_ARROW_SHAPE`
- **Description**: Defines the shape of the arrow at the edge's target.
- **Data Type**: `string`
- **Enum**:
  - `"none"`
  - `"triangle"` (In Cytoscape DT - `"delta"`)
  - `"triangle-cross"` (In Cytoscape DT - `"cross-delta"`)
  - `"square"`
  - `"diamond"`
  - `"circle"`
  - `"arrow"` (rendered as `"triangle"`)
  - `"open_circle"`
  - `"cross_open_delta"`
  - `"open_diamond"`
  - `"open_square"`
  - `"open_delta"`
  - `"tee"`
- **JSON Schema**:

  ```json
  {
    "EDGE_TARGET_ARROW_SHAPE": {
      "type": "string",
      "enum": ["none", "triangle", "triangle-cross", "square", "diamond", "circle", "vee", "tee"]
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_TARGET_ARROW_SHAPE`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_TARGET_ARROW_SHAPE": "none"
    }
  }
  ```

#### 2.3.6.6. Edge Target Arrow Color

- **CX2 Visual Property Name**: `EDGE_TARGET_ARROW_COLOR`
- **Description**: Sets the color of the arrow at the edge's target.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "EDGE_TARGET_ARROW_COLOR": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_TARGET_ARROW_UNSELECTED_PAINT`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_TARGET_ARROW_COLOR": "#000000"
    }
  }
  ```

#### 2.3.6.7. Edge Target Arrow Size

- **CX2 Visual Property Name**: `EDGE_TARGET_ARROW_SIZE`
- **Description**: Sets the size of the arrow at the edge's target.
- **Data Type**: `number`
- **Constraints**:
  - **Minimum**: `0`
- **JSON Schema**:

  ```json
  {
    "EDGE_TARGET_ARROW_SIZE": {
      "type": "number",
      "minimum": 0
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_TARGET_ARROW_SIZE`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_TARGET_ARROW_SIZE": 6.0
    }
  }
  ```

#### 2.3.6.8. Edge Target Arrow Selected Paint

- **CX2 Visual Property Name**: `EDGE_TARGET_ARROW_SELECTED_PAINT`
- **Description**: Specifies the color of the target arrow when the edge is selected.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "EDGE_TARGET_ARROW_SELECTED_PAINT": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_TARGET_ARROW_SELECTED_PAINT`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_TARGET_ARROW_SELECTED_PAINT": "#FFFF00"
    }
  }
  ```

### 2.3.7. Edge Visibility

- **CX2 Visual Property Name**: `EDGE_VISIBILITY`
- **Description**: Controls edge rendering.
- **Data Type**: `string`
- **Enum**:
  - `"none"`
  - `"element"`
- **JSON Schema**:

  ```json
  {
    "EDGE_VISIBILITY": {
      "type": "string",
      "enum": ["none", "element"]
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_VISIBLE` (**Values**: `true` or `false`)
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_VISIBILITY": "element"
    }
  }
  ```

## 2.3.8. Edge Label Properties

### 2.3.8.1. Edge Label Text

- **CX2 Visual Property Name**: `EDGE_LABEL`
- **Description**: The text content of edge labels.
- **Data Type**: `string`
- **JSON Schema**:

  ```json
  {
    "EDGE_LABEL": {
      "type": "string"
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_LABEL`
- **Example**:

  ```json
  {
    "edgeMapping": {
      "EDGE_LABEL": {
        "type": "PASSTHROUGH",
        "definition": {
          "attribute": "interaction",
          "type": "string"
        }
      }
    }
  }
  ```

### 2.3.8.2. Edge Label Autorotate

- **CX2 Visual Property Name**: `EDGE_LABEL_AUTOROTATE`
- **Description**: Determines whether the edge label should automatically rotate to align with the edge.
- **Data Type**: `boolean`
- **JSON Schema**:

  ```json
  {
    "EDGE_LABEL_AUTOROTATE": {
      "type": "boolean"
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_LABEL_AUTOROTATE`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_LABEL_AUTOROTATE": true
    }
  }
  ```

### 2.3.8.3. Edge Label Background Color

- **CX2 Visual Property Name**: `EDGE_LABEL_BACKGROUND_COLOR`
- **Description**: Defines the background color for edge labels.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "EDGE_LABEL_BACKGROUND_COLOR": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_LABEL_BACKGROUND_COLOR`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_LABEL_BACKGROUND_COLOR": "#B6B6B6"
    }
  }
  ```

### 2.3.8.4. Edge Label Background Shape

- **CX2 Visual Property Name**: `EDGE_LABEL_BACKGROUND_SHAPE`
- **Description**: Specifies the shape of the label's background.
- **Data Type**: `string`
- **Enum**:
  - `"none"`
  - `"rectangle"`
  - `"round-rectangle"`
- **JSON Schema**:

  ```json
  {
    "EDGE_LABEL_BACKGROUND_SHAPE": {
      "type": "string",
      "enum": ["none", "rectangle", "round-rectangle"]
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_LABEL_BACKGROUND_SHAPE`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_LABEL_BACKGROUND_SHAPE": "none"
    }
  }
  ```

### 2.3.8.5. Edge Label Background Opacity

- **CX2 Visual Property Name**: `EDGE_LABEL_BACKGROUND_OPACITY`
- **Description**: Sets the opacity of the edge label background.
- **Data Type**: `number`
- **Constraints**:
  - **Minimum**: `0`
  - **Maximum**: `1`
- **JSON Schema**:

  ```json
  {
    "EDGE_LABEL_BACKGROUND_OPACITY": {
      "type": "number",
      "minimum": 0,
      "maximum": 1
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_LABEL_BACKGROUND_TRANSPARENCY` (In Cytoscape DT, the value is from 0 to 255.)
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_LABEL_BACKGROUND_OPACITY": 1.0
    }
  }
  ```

### 2.3.8.6. Edge Label Color

- **CX2 Visual Property Name**: `EDGE_LABEL_COLOR`
- **Description**: Sets the color of edge labels.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "EDGE_LABEL_COLOR": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_LABEL_COLOR`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_LABEL_COLOR": "#000000"
    }
  }
  ```

### 2.3.8.7. Edge Label Font Face

- **CX2 Visual Property Name**: `EDGE_LABEL_FONT_FACE`
- **Description**: Specifies the font family, style, and weight for edge labels.
- **Data Type**: `object`
- **Properties**:
  - `FONT_FAMILY`: `string` (Enum: `"serif"`, `"sans-serif"`, `"monospace"`)
  - `FONT_STYLE`: `string` (Enum: `"normal"`, `"italic"`)
  - `FONT_WEIGHT`: `string` (Enum: `"normal"`, `"bold"`)
  - `FONT_NAME`: `string` (Optional; specifies the exact font name)
- **JSON Schema**:

  ```json
  {
    "EDGE_LABEL_FONT_FACE": {
      "type": "object",
      "properties": {
        "FONT_FAMILY": {
          "type": "string",
          "enum": ["serif", "sans-serif", "monospace"]
        },
        "FONT_STYLE": {
          "type": "string",
          "enum": ["normal", "italic"]
        },
        "FONT_WEIGHT": {
          "type": "string",
          "enum": ["normal", "bold"]
        },
        "FONT_NAME": {
          "type": "string"
        }
      }
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_LABEL_FONT_FACE`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_LABEL_FONT_FACE": {
        "FONT_FAMILY": "sans-serif",
        "FONT_STYLE": "normal",
        "FONT_WEIGHT": "normal",
        "FONT_NAME": "Dialog"
      }
    }
  }
  ```

### 2.3.8.8. Edge Label Font Size

- **CX2 Visual Property Name**: `EDGE_LABEL_FONT_SIZE`
- **Description**: Defines the font size (in pixels) for edge labels.
- **Data Type**: `integer`
- **Constraints**:
  - **Exclusive Minimum**: `0`
- **JSON Schema**:

  ```json
  {
    "EDGE_LABEL_FONT_SIZE": {
      "type": "integer",
      "exclusiveMinimum": 0
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_LABEL_FONT_SIZE`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_LABEL_FONT_SIZE": 10
    }
  }
  ```

### 2.3.8.9. Edge Label Position

- **CX2 Visual Property Name**: `EDGE_LABEL_POSITION`
- **Description**: Determines the placement of edge labels.
- **Data Type**: `object`
- **Properties**:
  - `JUSTIFICATION`: `string` (Enum: `"left"`, `"center"`, `"right"`)
  - `MARGIN_X`: `number`
  - `MARGIN_Y`: `number`
  - `EDGE_ANCHOR`: `string` (Enum: `"N"` etc.)
  - `LABEL_ANCHOR`: `string` (Enum: `"S"`, `"C"`, `"E"`, etc.)

- **JSON Schema**:

  ```json
  {
    "EDGE_LABEL_POSITION": {
      "type": "object",
      "properties": {
        "JUSTIFICATION": {
          "type": "string",
          "enum": ["left", "center", "right"]
        },
        "MARGIN_X": {
          "type": "number"
        },
        "MARGIN_Y": {
          "type": "number"
        },
        "EDGE_ANCHOR": {
          "type": "string"
        },
        "LABEL_ANCHOR": {
          "type": "string"
        }
      }
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_LABEL_POSITION`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_LABEL_POSITION": {
        "JUSTIFICATION": "center",
        "MARGIN_X": 0.0,
        "MARGIN_Y": 0.0,
        "EDGE_ANCHOR": "N",
        "LABEL_ANCHOR": "S"
      }
    }
  }
  ```

### 2.3.8.10. Edge Label Rotation

- **CX2 Visual Property Name**: `EDGE_LABEL_ROTATION`
- **Description**: Specifies the rotation angle of edge labels in degrees.
- **Data Type**: `number`
- **Constraints**:
  - **Minimum**: `-360`
  - **Maximum**: `360`
- **JSON Schema**:

  ```json
  {
    "EDGE_LABEL_ROTATION": {
      "type": "number",
      "minimum": -360,
      "maximum": 360
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_LABEL_ROTATION` 
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_LABEL_ROTATION": 0.0
    }
  }
  ```

### 2.3.8.11. Edge Label Opacity

- **CX2 Visual Property Name**: `EDGE_LABEL_OPACITY`
- **Description**: Specifies the opacity of edge labels.
- **Data Type**: `number`
- **Constraints**:
  - **Minimum**: `0`
  - **Maximum**: `1`
- **JSON Schema**:

  ```json
  {
    "EDGE_LABEL_OPACITY": {
      "type": "number",
      "minimum": 0,
      "maximum": 1
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_LABEL_TRANSPARENCY` (In Cytoscape DT, the value is from 0 to 255.)
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_LABEL_OPACITY": 1.0
    }
  }
  ```

### 2.3.8.12. Edge Label Max Width

- **CX2 Visual Property Name**: `EDGE_LABEL_MAX_WIDTH`
- **Description**: Defines the maximum width of edge labels before text wrapping occurs.
- **Data Type**: `number`
- **Constraints**:
  - **Exclusive Minimum**: `0`
- **JSON Schema**:

  ```json
  {
    "EDGE_LABEL_MAX_WIDTH": {
      "type": "number",
      "exclusiveMinimum": 0
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_LABEL_WIDTH`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_LABEL_MAX_WIDTH": 200.0
    }
  }
  ```

### 2.3.9. Edge Selected Properties

#### 2.3.9.1. Edge Selected

- **CX2 Visual Property Name**: `EDGE_SELECTED`
- **Description**: Indicates whether the edge is selected.
- **Data Type**: `boolean`
- **JSON Schema**:

  ```json
  {
    "EDGE_SELECTED": {
      "type": "boolean"
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_SELECTED`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_SELECTED": false
    }
  }
  ```

#### 2.3.9.2. Edge Selected Paint

- **CX2 Visual Property Name**: `EDGE_SELECTED_PAINT`
- **Description**: Specifies the color of the edge when selected.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "EDGE_SELECTED_PAINT": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_SELECTED_PAINT`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_SELECTED_PAINT": "#FF0000"
    }
  }
  ```

#### 2.3.9.3. Edge Stroke Selected Paint

- **CX2 Visual Property Name**: `EDGE_STROKE_SELECTED_PAINT`
- **Description**: Specifies the color of the edge's stroke when selected.
- **Data Type**: `string`
- **Format**: `rgbColor`
- **JSON Schema**:

  ```json
  {
    "EDGE_STROKE_SELECTED_PAINT": {
      "type": "string",
      "format": "rgbColor"
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_STROKE_SELECTED_PAINT`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_STROKE_SELECTED_PAINT": "#FF0000"
    }
  }
  ```

### 2.3.10. Edge Stacking Properties

#### 2.3.10.1. Edge Stacking

- **CX2 Visual Property Name**: `EDGE_STACKING`
- **Description**: Determines how overlapping edges are rendered.
- **Data Type**: `string`
- **Enum**:
  - `"none"`
  - `"AUTO_BEND"`
- **JSON Schema**:

  ```json
  {
    "EDGE_STACKING": {
      "type": "string",
      "enum": ["none", "AUTO_BEND"]
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_STACKING`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_STACKING": "AUTO_BEND"
    }
  }
  ```

#### 2.3.10.2. Edge Stacking Density

- **CX2 Visual Property Name**: `EDGE_STACKING_DENSITY`
- **Description**: Specifies the density of edge stacking.
- **Data Type**: `number`
- **Constraints**:
  - **Minimum**: `0`
  - **Maximum**: `1`
- **JSON Schema**:

  ```json
  {
    "EDGE_STACKING_DENSITY": {
      "type": "number",
      "minimum": 0,
      "maximum": 1
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_STACKING_DENSITY`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_STACKING_DENSITY": 0.5
    }
  }
  ```

### 2.3.11. Edge Z-Order

- **CX2 Visual Property Name**: `EDGE_Z_ORDER`
- **Description**: Specifies the Z-order of the edge (stacking order).
- **Data Type**: `number`
- **JSON Schema**:

  ```json
  {
    "EDGE_Z_ORDER": {
      "type": "number"
    }
  }
  ```

- **Cytoscape DT Name**: `EDGE_Z_ORDER`
- **Example**:

  ```json
  {
    "edge": {
      "EDGE_Z_ORDER": 0.0
    }
  }
  ```

## 2.4. Setting Visual Properties

Visual properties can be set on three levels:

### 2.4.1. Default Value

- The baseline value applied when no other settings are specified.

### 2.4.2. Mapping

- Associates visual properties with data attributes, allowing dynamic styling based on data values. This value overrides the default value.

### 2.4.3. Bypass

- Directly sets visual properties for individual nodes or edges, overriding mappings and defaults.

## 2.5. Editor Properties

- **Purpose**: Dictate editor behavior without altering visual styles directly.
- **Examples**:
  - `nodeSizeLocked`: Synchronizes node width and height.
  - `nodeImageFit`: Adjusts images to fit within node boundaries in Cytoscape DT.
- **Storage**: Saved in the `visualEditorProperties` aspect.
