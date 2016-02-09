package indi.zhangzqit.javaspider.handler;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.protocol.HttpContext;

public class ManualRedirectHandler implements RedirectHandler {

	@Override
	public URI getLocationURI(HttpResponse arg0, HttpContext arg1)
			throws ProtocolException {
		return null;
	}

	@Override
	public boolean isRedirectRequested(HttpResponse arg0, HttpContext arg1) {
		//手动处理 
		return false;
	} 
}
