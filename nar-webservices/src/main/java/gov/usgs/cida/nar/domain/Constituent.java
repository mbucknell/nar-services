package gov.usgs.cida.nar.domain;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;


public class Constituent {
	protected String name;
	protected ConstituentCategory category;
	protected ConstituentSubcategory subcategory;


	protected static final ImmutableMap<ConstituentSubcategory, List<ConstituentCategory>> MAP;
	static {
		ImmutableMap.Builder<ConstituentSubcategory, List<ConstituentCategory>> builder = ImmutableMap.builder();
		builder.put(ConstituentSubcategory.HERBICIDE, Lists.newArrayList(ConstituentCategory.PESTICIDE));
		builder.put(ConstituentSubcategory.NON_HERBICIDE, Lists.newArrayList(ConstituentCategory.PESTICIDE));
		MAP = builder.build();
		
	}
	
	public Constituent(){}
	
	public Constituent(ConstituentCategory category, ConstituentSubcategory subcategory, String name){
		this.category = category;
		this.subcategory = subcategory;
		this.name = name;
	}
	/**
	 * 
	 * @param subcategory
	 * @param category
	 * @return 
	 */
	public static boolean isValidSubcategoryOfCategory(ConstituentSubcategory subcategory, ConstituentCategory category){
		List<ConstituentCategory> parentCategories = MAP.get(subcategory);
		if(null == parentCategories){
			throw new IllegalArgumentException("The following subcategory does not have a defined parent category:" + subcategory);
		} else {
			return parentCategories.contains(category);
		}
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return the category
	 */
	public ConstituentCategory getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(ConstituentCategory category) {
		
		if(null != this.subcategory && !isValidSubcategoryOfCategory(this.subcategory, category)){
			throw new IllegalArgumentException("Category " + category + " is invalid for the existing subcategory " + this.subcategory);
		}
		this.category = category;
		
	}

	/**
	 * @return the subcategory
	 */
	public ConstituentSubcategory getSubcategory() {
		return subcategory;
	}

	/**
	 * @param subcategory the subcategory to set
	 */
	public void setSubcategory(ConstituentSubcategory subcategory) {
		if(null != this.category && !isValidSubcategoryOfCategory(subcategory, this.category)){
			throw new IllegalArgumentException("Subcategory " + subcategory + " is invalid for the existing Category " + this.category);
		}
		this.subcategory = subcategory;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + Objects.hashCode(this.name);
		hash = 53 * hash + Objects.hashCode(this.category);
		hash = 53 * hash + Objects.hashCode(this.subcategory);
		return hash;
	}

	@Override
	public String toString() {
		return "Constituent{" + "name=" + name + ", category=" + category + ", subcategory=" + subcategory + '}';
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Constituent other = (Constituent) obj;
		if (!Objects.equals(this.name, other.name)) {
			return false;
		}
		if (this.category != other.category) {
			return false;
		}
		if (this.subcategory != other.subcategory) {
			return false;
		}
		return true;
	}
	
}
