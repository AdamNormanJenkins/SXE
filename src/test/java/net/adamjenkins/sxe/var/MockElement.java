package net.adamjenkins.sxe.var;

import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;

public class MockElement {
	
	public void run(XSLProcessorContext context, ElemExtensionCall extensionElement) throws TransformerException {
		//Variable v = new Variable();
		QName q = new QName(null, "testmock");
		/*v.setQName(q);
		XObject xobj = v.execute(context.getTransformer().getXPathContext());
		System.out.println(xobj.toString());*/
		XPathContext xctxt = context.getTransformer().getXPathContext();
		xctxt.setNamespaceContext(extensionElement);
		XObject result = xctxt.getVarStack().getVariableOrParam(xctxt,q);
		System.out.println(result.toString());
		/*System.out.println("running");
        XPathContext xCtx = context.getTransformer().getXPathContext();
		String selectExpressionString = null;
        boolean namespacePushed = false;
        boolean expressionPushed = false;
        try{   
            selectExpressionString = extensionElement.getAttribute("context"); 
            //XPath xpath = new XPath(selectExpressionString, xCtx.getSAXLocator(), extensionElement, XPath.SELECT);
            XPath xpath = new XPath(
            		selectExpressionString, 
            		extensionElement, 
            		extensionElement, 
            		XPath.SELECT,
            		xCtx.getErrorListener(), 
                    new FunctionTable());
            //VarBridge.fixupVariables(xpath, extensionElement.getStylesheet());
            xCtx.pushNamespaceContext(extensionElement);            
            namespacePushed = true;
            int current = xCtx.getCurrentNode();
            xCtx.pushCurrentNodeAndExpression(current, current);        
            expressionPushed=true;
            Expression expr = xpath.getExpression();
            XObject result = expr.execute(xCtx);
            ElemVariable var = (ElemVariable)result.exprGetParent();
            XObject actualValue = var.getValue(context.getTransformer(), current);
			System.out.println(actualValue.toString());
        }catch(Throwable t){
            t.printStackTrace();
        }
        finally
        {            
            if(namespacePushed) xCtx.popNamespaceContext();
            if(expressionPushed) xCtx.popCurrentNodeAndExpression();         
        }   
		XObject returnValue = XSLTUtil.getXObject("context", context, extensionElement);
		System.out.println(returnValue);*/
	}

}
