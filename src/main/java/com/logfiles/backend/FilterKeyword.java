package com.logfiles.backend;

import java.util.function.Predicate;

/**
 * Predicate to filter a line with the keyword specified.
 * @author alexdel
 *
 */
public class FilterKeyword implements Predicate<String>{

	private String keyword;
	
	
	/**
	 * Constructor
	 * @param keyword keyword to filter
	 */
	public FilterKeyword(String keyword) {
		super();
		this.keyword = keyword;
	}	
	
	/**
	 * Override test method to implements that the line contains
	 * the keyword.
	 * @param line  Line to be filtered.
	 */
	@Override
	public boolean test(String line) {
		return line.contains(keyword);
	}

}
