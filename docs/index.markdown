---
# Feel free to add content and custom Front Matter to this file.
# To modify the layout, see https://jekyllrb.com/docs/themes/#overriding-theme-defaults

layout: home
list_title: ' '

---


Ctyoscape Exchange (CX) format is a network exchange format, designed as a flexible structure for transmission of networks. It is designed for flexibility, modularity, and extensibility, and as a message payload in common REST protocols. It is not intended as an in-memory data model for use in applications.

CX is "aspect-oriented", meaning that different types of information about network elements are separated into types of modules ("aspects"). Each aspect type specifies a schema for the information that it contains, typically a set of elements where the elements for that aspect type also have a defined schema. There are design guidelines for dependencies between aspects; the most basic guideline is that dependencies should be simple and minimal.

The flexibility of CX enables straightforward strategies for lossless encoding of potentially any network. At the most basic level, this means that CX imposes very few restrictions: graphs can be cyclic or acyclic and edges are implicitly directed, but formats can choose annotations schemes to override this. CX does not, itself, make any commitment to a single “correct” model of biology or graphic markup scheme.

CX is designed to facilitate streaming, potentially reducing memory footprint burden on applications processing large CX networks. In particular, in a CX stream, the elements of each aspect are broken into fragments and the fragments can be transmitted in any order.

The first version, version 1, of CX specification is released in 2015. In 2018, we released the version 2 of CX (CX2). If you are developing a new application we strongly recommend you to use CX2.



