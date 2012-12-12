xquery version "1.0";

declare namespace lux="http://luxproject.net";
declare namespace demo="http://luxproject.net/demo";

import module namespace layout="http://www.luxproject.net/layout" at "src/main/webapp/layout.xqy";

declare variable $lux:http as document-node() external;

let $path := $lux:http/http/path-extra
let $doc := doc($path)
let $doctype := name($doc/*)
let $stylesheet-name := concat("file:src/main/webapp/view-", $doctype, ".xsl")
return
  if (doc-available ($stylesheet-name)) then
    lux:transform (doc($stylesheet-name), $doc)
  else
    layout:outer (<textarea>{$doc}</textarea>)
