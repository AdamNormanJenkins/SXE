/**
 * This package has all the SXE elements within it.  Simply click on one of the classes
 * in this package and it will contain all the documentation about how to use the element
 * with Xalan from apache, to perform advanced integration activities such as calling spring
 * beans and EJBs, creating charts and graphs, performing assertions and logging and much much
 * more.
 * <br/><br/>
 * One thing you need to know, is that the SXE elements have 2 different types of attributes.  One
 * is called an <b>XPath</b> attribute, the other is called a <b>Template</b> attribute.  So,
 * what's the difference?
 * <br/><br/>
 * <h4>XPath Attributes</h4>
 * XPath attributes are attributes on elements that will be evaluated as a valid XPath.  An XPath
 * can be as simple as a number or a boolean, or as complex as a complete formula or reference to an
 * element within another document completely.  One special fact you must remember about XPath attributes,
 * is that you can't use Strings (text) in them, without it being in quotes.  Some valid values of an XPath
 * attribute may be:
 * <br/><br/>
 * <code>attribute="3"</code>
 * <br/>
 * <code>attribute="true"</code>
 * <br/>
 * <code>attribute="/someelement/@somevalue"</code>
 * <br/>
 * <code>attribute="$somevariable"</code>
 * <br/>
 * <code>attribute="'Some Piece of Text'"</code>
 * <br/>
 * <b>Note:</b> The text must be placed inside single quotes, within
 * the double quotes of the attribute definition (or vice verse if you've used single quotes for your attribute).
 * <br/><br/>
 * <h4>Template Attributes</h4>
 * Template attributes are not evaluated by the XPath engine, they are treated as plain text (so you don't have to use
 * the single quotes as with XPath attributes), e.g.
 * <br/><br/>
 * <code>attribute="Some Piece of Text"</code>    <b>Note:</b> No single quotes needed.
 * <br/><br/>
 * Because all template attributes are treated as text, you cannot use XPath expressions or literal data in them
 * as you can with XPath attributes, so the following:
 * <br/><br/>
 * <code>attribute="3"</code>
 * <br/><br/>
 * evaluates to a text string of "3".  And the following:
 * <br/><br/>
 * <code>attribute="/someelement/@someattribute</code>
 * <br/><br/>
 * evaluates to a text string of "/someelement/@someattribute", it does not look up the value of the
 * <i>someattribute</i> attribute on the <i>someelement</i> element as in the XPath attribute example.
 * <br/><br/>
 * So why aren't they called <b>Text Attributes</b> instead of <b>Template Attributes</b>?
 * Well, because of a special property of template attributes, which is the ability to embedd an XPath
 * expression into them using curly braces {}.  Anything inside curly braces within a Template attribute
 * will be evaluated <b>as if it was an XPath attribute</b>.
 * <br/><br/>
 * So, if we assume that in the following example the variable <i>name</i> had been initalized to the
 * value <code>world</code>, then the folling Template attribute would evaluate to <code>hello world</code>.
 * <br/><br/>
 * <code>attribute="hello {$name}"</code>
 * <br/><br/>
 * <b>Note:</b> That the dollar sign is <b>INSIDE</b> the curly braces.  That is because unlike other templating system
 * like velocity and jsp, which use the ${ } to seperate out expressions, the dollar sign is already reserved for variable
 * declarations in XSLT, so we have to do something different to introduce a templating system in XSLT.  If you weren't
 * referencing a variable, but an xpath, you would use something like <code>{/someelement/@someattribute}</code>
 * <br/><br/>
 * So please don't email me with telling me my dollar sign is in the wrong place if you see examples that look like
 * this {$somevar} :) .   Trust me, you'll see the value of it when you start using the logging framework and get to
 * write stuff like <code>&lt;log:debug message="The value of the attribute is {someelement/@someattribute}&gt;</code>
 * <br/><br/>
 * Now, onto the individual elements.
 * <br/><br/>
 * The following elements are available in SXE for use inside your Xalan compatible XSLT engine.
 * <br/><br/>
 * <table width="100%" border="1">
 *  <tr>
 *      <th align="left">Component</th>
 *      <th align="left">What it does</th>
 *      <th align="left">Doco</th>
 *  </tr>
 *  <tr>
 *      <td>Assertion</td>
 *      <td>Provides a range of assertion like functionality for rock solid XSLT integration</td>
 *      <td>{@link net.adamjenkins.sxe.elements.Assertion}</td>
 *  </tr>
 *  <tr>
 *      <td>Charting</td>
 *      <td>Provides a way to create Bar, Line, Pie, Timeseries and many more graphs/charts from XML
 *          and have them output to either a SVG, PNG or JPG file.
 *      </td>
 *      <td>{@link net.adamjenkins.sxe.elements.Charting}</td>
 *  </tr>
 *  <tr>
 *      <td>Concurrency</td>
 *      <td>A range of elements that assist with concurrency</td>
 *      <td>{@link net.adamjenkins.sxe.elements.Concurrency}</td>
 *  </tr>
 *  <tr>
 *      <td>EJB</td>
 *      <td>A range of elements for looking up Stateless and Stateful Entity Beans</td>
 *      <td>{@link net.adamjenkins.sxe.elements.EJB}</td>
 *  </tr>
 *  <tr>
 *      <td>Hibernate</td>
 *      <td>A range of elements for interacting with hiberante (loading, storing entities, running queries etc)
 *          and incorporating the hibernate entities into your XSLT.</td>
 *      <td>{@link net.adamjenkins.sxe.elements.Hibernate}</td>
 *  </tr>
 *  <tr>
 *      <td>Http</td>
 *      <td>A range of elements for working with HTTP query strings and form submissions.</td>
 *      <td>{@link net.adamjenkins.sxe.elements.Http}</td>
 *  </tr>
 *  <tr>
 *      <td>JMS</td>
 *      <td>A range of elements for publish to, and subscribing to, java compliant messaging servers.</td>
 *      <td>{@link net.adamjenkins.sxe.elements.JMS}</td>
 *  </tr>
 *  <tr>
 *      <td>JNDI</td>
 *      <td>A range of elements for reading things from, and binding things to, a JNDI tree.</td>
 *      <td>{@link net.adamjenkins.sxe.elements.JNDI}</td>
 *  </tr>
 *  <tr>
 *      <td>JPA</td>
 *      <td>A range of elements for interacting with EJB 3.0 JPA Entites and running EJBQL Queries and incorporating
 *          the entities into your XSLT.</td>
 *      <td>{@link net.adamjenkins.sxe.elements.JPA}</td>
 *  </tr>
 *  <tr>
 *      <td>JavaBean</td>
 *      <td>Utilities for calling methods on java beans and incorporating the results into your XSLT processing as well as
 *          methods for binding java beans to XSLT xpath evaluations.</td>
 *      <td>{@link net.adamjenkins.sxe.elements.JavaBean}</td>
 *  </tr>
 *  <tr>
 *      <td>Logging</td>
 *      <td>A logging framework for XSLT built ontop of SLF4j.</td>
 *      <td>{@link net.adamjenkins.sxe.elements.Logging}</td>
 *  </tr>
 *  <tr>
 *      <td>Spring</td>
 *      <td>A way to reference spring beans from XSLT</td>
 *      <td>{@link net.adamjenkins.sxe.elements.Spring}</td>
 *  </tr>
 *  <tr>
 *      <td>WebServices</td>
 *      <td>A way to incorporate webservice calls into your XSLT processing</td>
 *      <td>{@link net.adamjenkins.sxe.elements.WebServices}</td>
 *  </tr>
 * </table>
 *
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
package net.adamjenkins.sxe.elements;