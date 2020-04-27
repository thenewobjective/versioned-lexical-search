# Versioned-Lexical-Search

*Caveat emptor: this is an initial, minimal implementation of the features described in the document referenced below. It will probably eat your left socks from time to time*

The Versioned Lexical Search system  provides a user the capability to perform a
syntactically aware search over a codebase and across time through an IDE and VCS.
This enables the ability to not only find a relevant syntactic form in the codebase,
but also see how that syntactic form changed over time as the application evolved
and displays it in a user-friendly manner. For example: one could search for a class
named: `Foo`, and issue a query to locate the earliest instance of that class in the
repository and track its evolution through branches and merges.

For details view the SeniorThesis.pdf document.

Prerequisites:
  - Eclipse <http://eclipse.org>
  - Windows Server with Visual SVN <http://www.visualsvn.com/server/download/>
  - Semantic Designs Search Tool: <https://www.semanticdesigns.com/Products/SearchEngine/>
