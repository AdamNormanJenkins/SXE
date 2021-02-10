package org.apache.xalan.templates;

import java.util.HashMap;

import javax.xml.transform.TransformerException;

import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.QName;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;

public class PassThroughVariableStack extends VariableStack {

	private VariableStack delegate;
	
	//TODO: this needs to be made threadsafe
	private HashMap<Integer, XObject> overrides = new HashMap<Integer,XObject>();
	
	public PassThroughVariableStack(VariableStack stack) {
		super(100);
		this.delegate = stack;
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public Object clone() throws CloneNotSupportedException {
		return delegate.clone();
	}

	public XObject elementAt(int i) {
		return delegate.elementAt(i);
	}

	public int size() {
		return delegate.size();
	}

	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	public void reset() {
		delegate.reset();
	}

	public void setStackFrame(int sf) {
		delegate.setStackFrame(sf);
	}

	public int getStackFrame() {
		return delegate.getStackFrame();
	}

	public int link(int size) {
		return delegate.link(size);
	}

	public void unlink() {
		delegate.unlink();
	}

	public void unlink(int currentFrame) {
		delegate.unlink(currentFrame);
	}

	public void setLocalVariable(int index, XObject val) {
		delegate.setLocalVariable(index, val);
	}

	public void setLocalVariable(int index, XObject val, int stackFrame) {
		delegate.setLocalVariable(index, val, stackFrame);
	}

	public XObject getLocalVariable(XPathContext xctxt, int index) throws TransformerException {
		if(overrides.containsKey(index)) {
			return overrides.get(index);
		}else {
			return delegate.getLocalVariable(xctxt, index);
		}
	}

	public XObject getLocalVariable(int index, int frame) throws TransformerException {
		if(overrides.containsKey(index)) {
			return overrides.get(index);
		}else {
			return delegate.getLocalVariable(index, frame);
		}
	}

	public String toString() {
		return delegate.toString();
	}

	public XObject getLocalVariable(XPathContext xctxt, int index, boolean destructiveOK) throws TransformerException {
		if(overrides.containsKey(index)) {
			return overrides.get(index);
		}else {
			return delegate.getLocalVariable(xctxt, index, destructiveOK);
		}
	}

	public boolean isLocalSet(int index) throws TransformerException {
		return delegate.isLocalSet(index);
	}

	public void clearLocalSlots(int start, int len) {
		delegate.clearLocalSlots(start, len);
	}

	public void setGlobalVariable(int index, XObject val) {
		delegate.setGlobalVariable(index, val);
	}

	public XObject getGlobalVariable(XPathContext xctxt, int index) throws TransformerException {
		return delegate.getGlobalVariable(xctxt, index);
	}

	public XObject getGlobalVariable(XPathContext xctxt, int index, boolean destructiveOK) throws TransformerException {
		return delegate.getGlobalVariable(xctxt, index, destructiveOK);
	}

	public XObject getVariableOrParam(XPathContext xctxt, QName qname) throws TransformerException {
		//need to get the Variable from the xctx so you can get the index
		ElemVariable var = getElemVariableForQName(xctxt, qname);
		if(var != null) {
			if(overrides.containsKey(var.getIndex())) {
				return overrides.get(var.getIndex());
			}
		}
		return delegate.getVariableOrParam(xctxt, qname);
	}
	
	private ElemVariable getElemVariableForQName(XPathContext xctxt, QName name) {
		TransformerImpl transformer = (TransformerImpl)xctxt.getOwnerObject();
		ElemTemplateElement template = transformer.getCurrentElement();
		while(template != null && !(template.getParentNode() instanceof Stylesheet)) {
          ElemTemplateElement saved = template;
          ElemVariable vvar;
          while (null != (template = template.getPreviousSiblingElem()))
          {
            if(template instanceof ElemVariable)
            {
              vvar = (ElemVariable) template;

              if (vvar.getName().equals(name))
              {
                return vvar;
              }
              vvar = null;
            }
          }
          template = saved.getParentElem();			
		}
		return null;
	}

	public void overrideLoadVariable(int index, XObject var) {
		overrides.put(index, var);
	}
	


}
