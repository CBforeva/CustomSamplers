package mysql;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This class is the BeanInfoSupport for the MysqlSampler class.
 * */
public class MysqlSamplerBeanInfo extends BeanInfoSupport {

	/** Static logger instance from JMeter. */
	private static final Logger log = LoggingManager.getLoggerForClass();

	/** The constructor binds the given properties for the MysqlSampler class.
	 * <p>
	 * The property groups and properties are the following: <br>
	 * mysql <br>
	 * -- connectionId <br>
	 * sampler <br>
	 * -- assignmentInfo <br>
	 * -- useChunks <br>
	 * io <br>
	 * -- requestType <br>
	 * -- validateOperation <br>
	 * */
	public MysqlSamplerBeanInfo() {
		super(MysqlSampler.class);

		createPropertyGroup("mysql", new String[] {"connectionId"});
		createPropertyGroup("sampler", new String[] {"assignmentInfo", "useChunks"});
		createPropertyGroup("io", new String[] {"requestType", "validateOperation"});

		PropertyDescriptor p = property("connectionId");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "mysqlConn");

		p = property("assignmentInfo");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "assignInfo");
		p = property("useChunks");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(TAGS, new String[] {"True", "False"});
		p.setValue(DEFAULT, "False");

		p = property("requestType");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(TAGS, new String[] {"read", "write"});
		p.setValue(DEFAULT, "read");
		p = property("validateOperation");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(TAGS, new String[] {"True", "False"});
		p.setValue(NOT_OTHER, new String[] {"True", "False"});
		p.setValue(DEFAULT, "False");

		if(log.isDebugEnabled()) {
			for (PropertyDescriptor pd : getPropertyDescriptors()) {
				log.debug(pd.getName());
				log.debug(pd.getDisplayName());
			}
		}

	}

}
