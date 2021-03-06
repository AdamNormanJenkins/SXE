package net.adamjenkins.sxe.elements;

import static org.assertj.core.api.Assertions.assertThat;

import net.adamjenkins.sxe.util.XSLTErrorListener;

public class LoggingErrorTest extends LoggingTest {

	
	@Override
	protected void processResults(String output, XSLTErrorListener listener) {
		String message1 = "(Line: 14): hello test";
		String message2 = "(Line: 15): hello world";
        assertThat(getLogCaptor().getErrorLogs()).contains(message1);
        assertThat(getLogCaptor().getErrorLogs()).contains(message2);
	}
}
