# Community-CALayer7-CustomAssertions

This repository is for the community to create and develop new Custom Assertions to be deployed on the CA Layer7 API Gateway.

Layer7 API Gateway is a fine tool, though a number of features are not available, and custom script assertion is still not there as of v9.3. While it is often possible to implement these feature via Regular Expression or XSLT, it is often inelegant and slow.

Custom Assertion is the answer to this. It is a great and simple extension mechanism that Layer7 has built.

This product is widely deployed, and probably many developers are willing to share their own work.

To compile the code, simply drop all these directories inside the SDK at the same level of the TrafficLoggerSample folder. Then run the build command.

## String Simple Transform

This assertion allows simple transformation such as:
* trim
* to upper case and lower case
* encore and decode hexadecimal strings in UTF-8 and UTF-16 character sets
* encode and decode JSON and XML strings

It has been designed to supported easy addition of a new type without the need of the Custom Assertion SDK. The 'transforms' package is independent and can be compiled without the SDK.