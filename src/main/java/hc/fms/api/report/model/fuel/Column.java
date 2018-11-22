package hc.fms.api.report.model.fuel;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Column {
	private String field;
	private String title;
	private Integer weight;
	private Integer width;
	private String align;
	@JsonProperty("highlight_min_max")
	private Boolean highlightMinMax;
}