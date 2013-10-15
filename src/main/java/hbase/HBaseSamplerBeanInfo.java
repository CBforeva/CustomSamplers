package hbase;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class HBaseSamplerBeanInfo extends BeanInfoSupport {

	private static final Logger log = LoggingManager.getLoggerForClass();

	public HBaseSamplerBeanInfo() {
		super(HBaseSampler.class);

		createPropertyGroup("hbase", new String[] {"cluster"});
		createPropertyGroup("sampler", new String[] {"binaryInfo", "useChunks"});
		createPropertyGroup("reading", new String[] {"doRead", "useRandomAccess", "checkRead"});
		createPropertyGroup("writing", new String[] {"doWrite", "assignedWrite"});

		PropertyDescriptor p = property("cluster");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "TestHBase");

		p = property("binaryInfo");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "binInfo");
		p = property("useChunks");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(TAGS, new String[] {"true", "false", "bulk"});
		p.setValue(DEFAULT, "false");

		p = property("doRead");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, Boolean.FALSE);
		p = property("useRandomAccess");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, Boolean.FALSE);
		p = property("checkRead");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, Boolean.FALSE);

		p = property("doWrite");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, Boolean.FALSE);
		p = property("assignedWrite");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, Boolean.FALSE);

		if(log.isDebugEnabled()) {
			for (PropertyDescriptor pd : getPropertyDescriptors()) {
				log.debug(pd.getName());
				log.debug(pd.getDisplayName());
			}
		}

	}

}