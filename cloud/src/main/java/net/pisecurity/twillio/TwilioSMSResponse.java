package net.pisecurity.twillio;

import java.util.Map;

public class TwilioSMSResponse {

	public String sid;
	public String date_created; // ":"Fri, 09 Mar 2018 16:14:05 +0000",
	public String date_updated;
	public String date_sent;
	public String account_sid;
	public String to;
	public String from;
	public String messaging_service_sid;

	public String body;
	public String status;
	public int num_segments;
	public int num_media;
	public String direction;
	public String api_version;
	public String price;
	public String price_unit;
	public String error_code;
	public String error_message;
	public String uri;
	public Map<String, String> subresource_uris;

	@Override
	public String toString() {
		return "TwilioSMSResponse [sid=" + sid + ", date_created=" + date_created + ", date_updated=" + date_updated
				+ ", date_sent=" + date_sent + ", account_sid=" + account_sid + ", to=" + to + ", from=" + from
				+ ", messaging_service_sid=" + messaging_service_sid + ", body=" + body + ", status=" + status
				+ ", num_segments=" + num_segments + ", num_media=" + num_media + ", direction=" + direction
				+ ", api_version=" + api_version + ", price=" + price + ", price_unit=" + price_unit + ", error_code="
				+ error_code + ", error_message=" + error_message + ", uri=" + uri + ", subresource_uris="
				+ subresource_uris + "]";
	}

}
