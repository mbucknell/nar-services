package gov.usgs.cida.nar.domain;

import org.junit.Test;
import org.force66.beantester.BeanTester;
import org.joda.time.LocalDateTime;

public class TimeSeriesAvailabilityTest {
	
	@Test
	public void standardBeanCompliance() {
		BeanTester beanTester = new BeanTester();
		beanTester.testBean(TimeSeriesAvailability.class);
		beanTester.testBean(TimeSeriesAvailability.class, new Object[]{
			TimeSeriesCategory.LOAD,
			TimeStepDensity.ANNUAL,
			new LocalDateTime(1990, 1, 1, 0, 0),
			new LocalDateTime(2000, 1, 1, 0, 0),
			"NO2_NO3"
		});
	}
}
