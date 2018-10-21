# Community-CALayer7-CustomAssertions

This repository is for the community to create and develop new Custom Assertions that will be deployed on the CA Layer7 API Gateway.

Layer7 API Gateway is a fine tool, though a number of features are not available, and custom script assertion is still not there as of v9.3. While it is often possible to implement these feature via Regular Expression or XSLT, it is often inelegant and slow.

Custom Assertion is the answer to this. It is a great and simple extension mechanism that Layer7 has built.

This product is widely deployed, and probably many developers are willing to share their own work.

To compile the code, simply drop all these directories inside the SDK at the same level of the TrafficLoggerSample folder. Then run the build command.

## String Simple Transform

This assertion allows simple transformation such as:
* trim spaces
* to upper case and lower case
* encore and decode hexadecimal strings in UTF-8 and UTF-16 character sets
* encode and decode JSON and XML 1.0 and 1.1 strings

![Dialog Screenshot](/StringSimpleTransform/DialogScreenShot.png)

It has been designed to support easy addition of a new type transformation without the need of the Custom Assertion SDK. The 'transforms' package is independent and can be compiled without the SDK.

## No Duplicate JSON Name

Duplicate name in a JSON object is actually allowed but not recommended. For example `{"a":1, "a":2}`. For reference [JSON Object RFC 7159](https://tools.ietf.org/html/rfc7159#section-4) where SHOULD is defined in [Key words RFC 2119](https://tools.ietf.org/html/rfc2119).

To avoid any doubt, this assertion parses a JSON message inside the request object ${request.mainpart} and detects any possible duplicated name in any object contained in it. In the case the JSON request can not be parsed, this assertion does not fail, the service logic continues.

The first duplicated name found is stored in the context variable 'duplicatedName', in a JSON path format, for example `$.a`. In case no duplication found, this variable is set to empty.

**Warning:** use this assertion after the request JSON object size is bounded, typically via a 'Protect Against JSON Document Structure Assertion' and set 'container depth' and 'object entry count' with reasonable values.

## XML-JSON Transform

This assertion transforms XML / JSON based on JSON schema included in the swagger document. This document contains XML hints that adds more capability.

![Dialog Screenshot](/XMLJSONTransform/DialogScreenShot.png)

Schema caching is controlled with the 3 cluster properties:
* jsonxml.schemaCache.maxAge (in ms): entries older than this age are flushed. Set to -1 to avoid cache flush. Default is -1
* jsonxml.schemaCache.maxDownloadSize (in characters). Default is 128KB
* jsonxml.schemaCache.maxEntries. Set to 0 to avoid cache. Default is 128

Every 5 minutes these values will be refreshed.

TODO (by order of priority):
1. support for $ref
   * included ref entries in definitions section
2. JSON to XML
3. optimize code for path (used when raising exception)
4. XML to JSON: control space trim for string content (text node) and XML attributes (quote delimited)
5. additional elements / JSON property key behavior: error, ignore or map as string
6. Suppoer schema oneOf, anyOf, etc
7. support for $ref
   * external ref
   * support $id
