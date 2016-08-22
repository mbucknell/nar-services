package gov.usgs.cida.nar.service;

import gov.usgs.cida.nar.domain.AggregationType;
import gov.usgs.cida.nar.domain.ComparisonCategorization;
import gov.usgs.cida.nar.domain.ComparisonCategory;
import gov.usgs.cida.nar.domain.Herbicide;
import gov.usgs.cida.nar.domain.NonHerbicide;
import gov.usgs.cida.nar.domain.Pesticide;
import gov.usgs.cida.nar.domain.PesticideTimeSeriesAvailability;
import gov.usgs.cida.nar.mybatis.dao.PesticideSampleDao;
import gov.usgs.cida.nar.domain.TimeSeriesAvailability;
import gov.usgs.cida.nar.domain.TimeSeriesCategory;
import gov.usgs.cida.nar.domain.TimeStepDensity;
import gov.usgs.cida.nar.domain.constituent.ConstituentCategorization;
import gov.usgs.cida.nar.domain.constituent.ConstituentCategory;
import gov.usgs.cida.nar.domain.constituent.ConstituentSubcategory;
import gov.usgs.cida.nar.mybatis.model.DateIntervalWithConstituent;
import gov.usgs.cida.nar.mybatis.model.PestSites;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import org.joda.time.LocalDateTime;



class PesticideSampleService implements NARService<PestSites> {
	
	private PesticideSampleDao dao;
	protected List<String> siteQwId;
	private String startDate;
	private String endDate;
	private final PestSitesService sitesService;
	
	public PesticideSampleService() {
		this(new PesticideSampleDao(), new PestSitesService());
	}

	public PesticideSampleService(PesticideSampleDao dao, PestSitesService sitesService) {
		this.sitesService = sitesService;
		this.dao = dao;
		this.siteQwId = null;
		this.startDate = null;
		this.endDate = null;
	}
	
	@Override
	public List<? extends PestSites> request() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public TimeStepDensity getTimeStepDensity() {
		return TimeStepDensity.DISCRETE;
	}

	@Override
	public TimeSeriesCategory getTimeSeriesCategory() {
		return TimeSeriesCategory.CONCENTRATION;
	}
	
	@Override
	/**
	 * @throws javax.ws.rs.NotFoundException if pesticide information is unavailable for the site
	 */
	public List<TimeSeriesAvailability> getAvailability() {
		List<TimeSeriesAvailability> mostFrequentlyDetected = getMostFrequentlyDetectedAvailability();
		List<TimeSeriesAvailability> closestToBenchmarks = getAvailabilityOfPesticidesCloseToBenchmarks();
		List<TimeSeriesAvailability> allPestSampleAvailability = new ArrayList<TimeSeriesAvailability>();
		allPestSampleAvailability.addAll(mostFrequentlyDetected);
		allPestSampleAvailability.addAll(closestToBenchmarks);
		return allPestSampleAvailability;
	}
	
	
	public List<TimeSeriesAvailability> getAvailabilityOfPesticidesCloseToBenchmarks(){
				String siteQw = siteQwId.get(0);
		Map<ComparisonCategorization, String> pests = sitesService.getPesticidesCloseToBenchmarks(siteQw);
		
		if (null == pests || pests.isEmpty()){
			throw new NotFoundException("Could not determine the availability of pesticides close to benchmarks at site '" + siteQw +"'.");
		}
		List<TimeSeriesAvailability> availability = new ArrayList<>();
		for (Map.Entry<ComparisonCategorization, String> entry : pests.entrySet()) {
			String constit = entry.getValue();
			ComparisonCategorization comparisonCategorization = entry.getKey();
			List<DateIntervalWithConstituent> dateIntervalsWithConstits = this.dao.getAvailability(siteQw, constit);
			if (null != dateIntervalsWithConstits && !dateIntervalsWithConstits.isEmpty()) {
				for (DateIntervalWithConstituent dateIntervalWithConstit : dateIntervalsWithConstits) {
					LocalDateTime start = new LocalDateTime(dateIntervalWithConstit.getStart());
					LocalDateTime end = new LocalDateTime(dateIntervalWithConstit.getEnd());
					PesticideTimeSeriesAvailability tsa = new PesticideTimeSeriesAvailability(
						this.getTimeSeriesCategory(),
						this.getTimeStepDensity(),
						start,
						end,
						dateIntervalWithConstit.getConstit(),
						AggregationType.NONE,
						comparisonCategorization,
						new ConstituentCategorization(ConstituentCategory.PESTICIDE, null)//subcategory is unimportant here
					);
					availability.add(tsa);
				}
			}
		}
		return availability;
	}
	
	public List<TimeSeriesAvailability> getMostFrequentlyDetectedAvailability(){
		String siteQw = siteQwId.get(0);
		List<Pesticide> pests = sitesService.getMostFrequentlyDetectedPesticides(siteQw);
		
		if (null == pests || pests.isEmpty()){
			throw new NotFoundException("Could not determine the most frequently-detected pesticides for site '" + siteQw +"'.");
		}
		List<TimeSeriesAvailability> availability = new ArrayList<>();
		for(Pesticide pesticide : pests){
			ConstituentSubcategory constituentSubcategory = null;
			switch(pesticide.getType()){
				case Herbicide.TYPE:
					constituentSubcategory = ConstituentSubcategory.HERBICIDE;
					break;
				case NonHerbicide.TYPE:
					constituentSubcategory = ConstituentSubcategory.NON_HERBICIDE;
					break;
				default:
					throw new IllegalArgumentException(pesticide.getType()+ " is not a valid pesticide type");
			}
					
			//Note that pesticides, in addition to nutrients, 
			//are considered to be "constituents":
			//http://water.usgs.gov/nawqa/constituents/pesticides.html
			
			List<DateIntervalWithConstituent> dateIntervalsWithConstits = this.dao.getAvailability(siteQw, pesticide.getName());
			if (null != dateIntervalsWithConstits && !dateIntervalsWithConstits.isEmpty()) {
				for (DateIntervalWithConstituent dateIntervalWithConstit : dateIntervalsWithConstits) {
					LocalDateTime start = new LocalDateTime(dateIntervalWithConstit.getStart());
					LocalDateTime end = new LocalDateTime(dateIntervalWithConstit.getEnd());
					PesticideTimeSeriesAvailability tsa = new PesticideTimeSeriesAvailability(
						this.getTimeSeriesCategory(),
						this.getTimeStepDensity(),
						start,
						end,
						dateIntervalWithConstit.getConstit(),
						AggregationType.NONE,
						new ComparisonCategorization(ComparisonCategory.ABSOLUTE, 1),
						new ConstituentCategorization(ConstituentCategory.PESTICIDE, constituentSubcategory)
					);
					availability.add(tsa);
				}
			}
		}
		return availability;
	}

	@Override
	public void setSiteQwId(List<String> siteQwId) {
		this.siteQwId = siteQwId;
	}

}
