package net.adamjenkins.sxe.elements;

import net.adamjenkins.sxe.XSLTBasedTest;
import nl.altindag.log.LogCaptor;

public abstract class LoggingTest extends XSLTBasedTest {

	private LogCaptor logCaptor;
	
	@Override
	protected void setUp() {
        logCaptor = LogCaptor.forClass(Logging.class);
	}

	public LogCaptor getLogCaptor() {
		return logCaptor;
	}

	public void setLogCaptor(LogCaptor logCaptor) {
		this.logCaptor = logCaptor;
	}	
	
}
