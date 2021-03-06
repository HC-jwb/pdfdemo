package hc.fms.api.addon.report.model;

import java.util.List;

import hc.fms.api.addon.model.ResponseStatus;
import hc.fms.api.addon.report.model.fuel.ReportDesc;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ReportResponse<T> {
	private boolean success;
	private ResponseStatus status;
	private List<ResponseError> errors;
	private ReportDesc<T> report;
}