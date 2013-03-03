This release of Lux is integrated with Solr/Lucene 4.1.

Added low-level support for binary documents; you can now store images,
xquery, and other non-XML files in a Lux index.

Fixed a bug where a document could be stored twice at the same uri.

Index-assisted sorting by numeric fields

Enabled configurable analysis chain so users can control the kind of text
treatment performed by the XML indexing analyzers.  Specifically,
XmlTokenStreamBase now wraps an externally-supplied Analyzer.  We'll need
to follow up by exposing higher-level constructs to make this more usable,
eg: expose via configuration in schema.xml.  This may actually work already
and just require documentation?

Optimized deep pagination: we now skip over (not load into memory and
parse) documents we can prove to be unused.

Fixed an over-optimization bug when we optimized count (exists, not) and
its argument combined a search call and an additional optimizable
expression (like a path).

Added "querybox" - a query sandbox - to the demo.
