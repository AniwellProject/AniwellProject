package com.example.RSW.dto;

import java.util.List;
import java.util.Map;

public class OcrParseResponse {

	public enum DocType { LAB, RECEIPT, PRESCRIPTION, DIAGNOSIS, UNKNOWN }

	private final DocType docType;
	private final List<Group> groups;
	private final String ascii;

	public OcrParseResponse(DocType docType, List<Group> groups, String ascii) {
		this.docType = docType;
		this.groups = groups;
		this.ascii = ascii;
	}

	public DocType getDocType() { return docType; }
	public List<Group> getGroups() { return groups; }
	public String getAscii() { return ascii; }

	public static class Group {
		private final String date;
		private final List<Map<String, Object>> items;

		public Group(String date, List<Map<String, Object>> items) {
			this.date = date;
			this.items = items;
		}

		public String getDate() { return date; }
		public List<Map<String, Object>> getItems() { return items; }
	}
}
